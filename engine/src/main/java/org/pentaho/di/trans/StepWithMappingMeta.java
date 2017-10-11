/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.util.CurrentDirectoryResolver;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.metastore.api.IMetaStore;

/**
 * This class is supposed to use in steps where the mapping to sub transformations takes place
 *
 * @since 02-jan-2017
 * @author Yury Bakhmutski
 */
public abstract class StepWithMappingMeta extends BaseStepMeta {
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
    VariableSpace tmpSpace =
      r.resolveCurrentDirectory( executorMeta.getSpecificationMethod(), space, rep, executorMeta.getParentStepMeta(),
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

    // Pass some important information to the mapping transformation metadata:
    if ( share ) {
      mappingTransMeta.copyVariablesFrom( space );
    }
    mappingTransMeta.setRepository( rep );
    mappingTransMeta.setMetaStore( metaStore );
    mappingTransMeta.setFilename( mappingTransMeta.getFilename() );

    return mappingTransMeta;
  }

  /**
   * @return the specificationMethod
   */
  public ObjectLocationSpecificationMethod getSpecificationMethod() {
    return specificationMethod;
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

}
