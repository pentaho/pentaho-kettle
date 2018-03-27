/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans;

import org.apache.commons.lang.ArrayUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.util.CurrentDirectoryResolver;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.HasRepositoryDirectories;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.metastore.api.IMetaStore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class is supposed to use in steps where the mapping to sub transformations takes place
 *
 * @since 02-jan-2017
 * @author Yury Bakhmutski
 */
public abstract class StepWithMappingMeta extends BaseStepMeta implements HasRepositoryDirectories {
  //default value
  private static Class<?> PKG = StepWithMappingMeta.class;

  protected ObjectLocationSpecificationMethod specificationMethod;
  protected String transName;
  protected String fileName;
  protected String directoryPath;
  protected ObjectId transObjectId;

  public static TransMeta loadMappingMeta( StepWithMappingMeta mappingMeta, Repository rep,
                                           IMetaStore metaStore, VariableSpace space ) throws KettleException {
    return loadMappingMeta( mappingMeta, rep, metaStore, space, true );
  }

  public static synchronized TransMeta loadMappingMeta( StepWithMappingMeta executorMeta, Repository rep,
                                                        IMetaStore metaStore, VariableSpace space, boolean share ) throws KettleException {
    TransMeta mappingTransMeta = null;

    CurrentDirectoryResolver r = new CurrentDirectoryResolver();
    // send parentVariables = null we don't need it here for resolving resolveCurrentDirectory.
    // Otherwise we destroy child variables and the option "Inherit all variables from the transformation" is enabled always.
    VariableSpace tmpSpace =
      r.resolveCurrentDirectory( executorMeta.getSpecificationMethod(), null, rep, executorMeta.getParentStepMeta(),
        executorMeta.getFileName() );

    switch ( executorMeta.getSpecificationMethod() ) {
      case FILENAME:
        String realFilename = tmpSpace.environmentSubstitute( executorMeta.getFileName() );
        try {
          // OK, load the meta-data from file...
          // Don't set internal variables: they belong to the parent thread!
          if ( rep != null ) {
            // need to try to load from the repository
            realFilename = r.normalizeSlashes( realFilename );
            try {
              String dirStr = realFilename.substring( 0, realFilename.lastIndexOf( "/" ) );
              String tmpFilename = realFilename.substring( realFilename.lastIndexOf( "/" ) + 1 );
              RepositoryDirectoryInterface dir = rep.findDirectory( dirStr );
              mappingTransMeta = rep.loadTransformation( tmpFilename, dir, null, true, null );
            } catch ( KettleException ke ) {
              // try without extension
              if ( realFilename.endsWith( Const.STRING_TRANS_DEFAULT_EXT ) ) {
                try {
                  String tmpFilename =
                    realFilename.substring( realFilename.lastIndexOf( "/" ) + 1, realFilename.indexOf( "."
                      + Const.STRING_TRANS_DEFAULT_EXT ) );
                  String dirStr = realFilename.substring( 0, realFilename.lastIndexOf( "/" ) );
                  RepositoryDirectoryInterface dir = rep.findDirectory( dirStr );
                  mappingTransMeta = rep.loadTransformation( tmpFilename, dir, null, true, null );
                } catch ( KettleException ke2 ) {
                  // fall back to try loading from file system (transMeta is going to be null)
                }
              }
            }
          }
          if ( mappingTransMeta == null ) {
            mappingTransMeta = new TransMeta( realFilename, metaStore, rep, true, tmpSpace, null );
            LogChannel.GENERAL.logDetailed( "Loading transformation from repository",
              "Transformation was loaded from XML file [" + realFilename + "]" );
          }
        } catch ( Exception e ) {
          throw new KettleException( BaseMessages.getString( PKG, "StepWithMappingMeta.Exception.UnableToLoadTrans" ),
            e );
        }
        break;

      case REPOSITORY_BY_NAME:
        String realTransname = tmpSpace.environmentSubstitute( executorMeta.getTransName() );
        String realDirectory = tmpSpace.environmentSubstitute( executorMeta.getDirectoryPath() );

        if ( rep != null ) {
          if ( !Utils.isEmpty( realTransname ) && !Utils.isEmpty( realDirectory ) ) {
            realDirectory = r.normalizeSlashes( realDirectory );
            RepositoryDirectoryInterface repdir = rep.findDirectory( realDirectory );
            if ( repdir != null ) {
              try {
                // reads the last revision in the repository...
                mappingTransMeta = rep.loadTransformation( realTransname, repdir, null, true, null );
                // TODO: FIXME: pass in metaStore to repository?

                LogChannel.GENERAL.logDetailed( "Loading transformation from repository", "Executor transformation ["
                  + realTransname + "] was loaded from the repository" );
              } catch ( Exception e ) {
                throw new KettleException( "Unable to load transformation [" + realTransname + "]", e );
              }
            }
          }
        } else {
          // rep is null, let's try loading by filename
          try {
            mappingTransMeta =
              new TransMeta( realDirectory + "/" + realTransname, metaStore, rep, true, tmpSpace, null );
          } catch ( KettleException ke ) {
            try {
              // add .ktr extension and try again
              mappingTransMeta =
                new TransMeta( realDirectory + "/" + realTransname + "." + Const.STRING_TRANS_DEFAULT_EXT, metaStore,
                  rep, true, tmpSpace, null );
            } catch ( KettleException ke2 ) {
              throw new KettleException( BaseMessages.getString( PKG, "StepWithMappingMeta.Exception.UnableToLoadTrans",
                realTransname ) + realDirectory );
            }
          }
        }
        break;

      case REPOSITORY_BY_REFERENCE:
        // Read the last revision by reference...
        mappingTransMeta = rep.loadTransformation( executorMeta.getTransObjectId(), null );
        break;
      default:
        break;
    }
    if ( mappingTransMeta == null ) {  //skip warning
      return null;
    }

    //  When the child parameter does exist in the parent parameters, overwrite the child parameter by the
    // parent parameter.
    replaceVariableValues( mappingTransMeta, space );
    if ( share ) {
      // All other parent parameters need to get copied into the child parameters  (when the 'Inherit all
      // variables from the transformation?' option is checked)
      addMissingVariables( mappingTransMeta, space );
    }
    mappingTransMeta.setRepository( rep );
    mappingTransMeta.setMetaStore( metaStore );
    mappingTransMeta.setFilename( mappingTransMeta.getFilename() );

    return mappingTransMeta;
  }

  public static void activateParams( VariableSpace childVariableSpace, NamedParams childNamedParams, VariableSpace parent, String[] listParameters,
                                     String[] mappingVariables, String[] inputFields ) {
    Map<String, String> parameters = new HashMap<>();
    Set<String> subTransParameters = new HashSet<>( Arrays.asList( listParameters ) );

    if ( mappingVariables != null ) {
      for ( int i = 0; i < mappingVariables.length; i++ ) {
        parameters.put( mappingVariables[ i ], parent.environmentSubstitute( inputFields[ i ] ) );
      }
    }

    for ( String variableName : parent.listVariables() ) {
      // When the child parameter does exist in the parent parameters, overwrite the child parameter by the
      // parent parameter.
      if ( parameters.containsKey( variableName ) ) {
        parameters.put( variableName, parent.getVariable( variableName ) );
      } else if ( ArrayUtils.contains( listParameters, variableName ) ) {
        // there is a definition only in Transformation properties - params tab
        parameters.put( variableName, parent.getVariable( variableName ) );
      }
    }

    for ( Map.Entry<String, String> entry : parameters.entrySet() ) {
      String key = entry.getKey();
      String value = Const.NVL( entry.getValue(), "" );
      if ( subTransParameters.contains( key ) ) {
        try {
          childNamedParams.setParameterValue( key, Const.NVL( entry.getValue(), "" ) );
        } catch ( UnknownParamException e ) {
          // this is explicitly checked for up front
        }
      } else {
        childVariableSpace.setVariable( key, value );
      }
    }

    childNamedParams.activateParameters();
  }

  /**
   * @return the specificationMethod
   */
  public ObjectLocationSpecificationMethod getSpecificationMethod() {
    return specificationMethod;
  }

  @Override
  public ObjectLocationSpecificationMethod[] getSpecificationMethods() {
    return new ObjectLocationSpecificationMethod[] { specificationMethod };
  }

  /**
   * @param specificationMethod the specificationMethod to set
   */
  public void setSpecificationMethod( ObjectLocationSpecificationMethod specificationMethod ) {
    this.specificationMethod = specificationMethod;
  }

  /**
   * @return the directoryPath
   */
  public String getDirectoryPath() {
    return directoryPath;
  }

  /**
   * @param directoryPath the directoryPath to set
   */
  public void setDirectoryPath( String directoryPath ) {
    this.directoryPath = directoryPath;
  }

  @Override
  public String[] getDirectories() {
    return new String[]{ directoryPath };
  }

  @Override
  public void setDirectories( String[] directories ) {
    this.directoryPath = directories[0];
  }

  /**
   * @return the fileName
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * @param fileName the fileName to set
   */
  public void setFileName( String fileName ) {
    this.fileName = fileName;
  }

  /**
   * @return the transName
   */
  public String getTransName() {
    return transName;
  }

  /**
   * @param transName the transName to set
   */
  public void setTransName( String transName ) {
    this.transName = transName;
  }

  /**
   * @return the transObjectId
   */
  public ObjectId getTransObjectId() {
    return transObjectId;
  }

  /**
   * @param transObjectId the transObjectId to set
   */
  public void setTransObjectId( ObjectId transObjectId ) {
    this.transObjectId = transObjectId;
  }

  @Override
  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
                                 ResourceNamingInterface resourceNamingInterface, Repository repository,
                                 IMetaStore metaStore ) throws KettleException {
    try {
      // Try to load the transformation from repository or file.
      // Modify this recursively too...
      //
      // NOTE: there is no need to clone this step because the caller is
      // responsible for this.
      //
      // First load the mapping transformation...
      //
      TransMeta mappingTransMeta = loadMappingMeta( this, repository, metaStore, space );

      // Also go down into the mapping transformation and export the files
      // there. (mapping recursively down)
      //
      String proposedNewFilename =
              mappingTransMeta.exportResources(
                      mappingTransMeta, definitions, resourceNamingInterface, repository, metaStore );

      // To get a relative path to it, we inject
      // ${Internal.Entry.Current.Directory}
      //
      String newFilename =
              "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}/" + proposedNewFilename;

      // Set the correct filename inside the XML.
      //
      mappingTransMeta.setFilename( newFilename );

      // exports always reside in the root directory, in case we want to turn
      // this into a file repository...
      //
      mappingTransMeta.setRepositoryDirectory( new RepositoryDirectory() );

      // change it in the job entry
      //
      fileName = newFilename;

      setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );

      return proposedNewFilename;
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "StepWithMappingMeta.Exception.UnableToLoadTrans", fileName ) );
    }
  }

  public static  void addMissingVariables( VariableSpace fromSpace, VariableSpace toSpace ) {
    if ( toSpace == null ) {
      return;
    }
    String[] variableNames = toSpace.listVariables();
    for ( String variable : variableNames ) {
      if ( fromSpace.getVariable( variable ) == null ) {
        fromSpace.setVariable( variable, toSpace.getVariable( variable ) );
      }
    }
  }

  public static void replaceVariableValues( VariableSpace childTransMeta, VariableSpace replaceBy ) {
    if ( replaceBy == null ) {
      return;
    }
    String[] variableNames = replaceBy.listVariables();
    for ( String variableName : variableNames ) {
      if ( childTransMeta.getVariable( variableName ) != null ) {
        childTransMeta.setVariable( variableName, replaceBy.getVariable( variableName ) );
      }
    }
  }
}
