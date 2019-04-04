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

package org.pentaho.di.trans.steps.getrepositorynames;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryExtended;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Read all sorts of text files, convert them to rows and writes these to one or more output streams.
 *
 * @author Matt
 * @since 4-apr-2003
 */
public class GetRepositoryNames extends BaseStep implements StepInterface {
  private static Class<?> PKG = GetRepositoryNamesMeta.class; // for i18n purposes, needed by Translator2!!

  private GetRepositoryNamesMeta meta;

  private GetRepositoryNamesData data;

  public GetRepositoryNames( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  /**
   * Build an empty row based on the meta-data...
   *
   * @return
   */

  private Object[] buildEmptyRow() {
    Object[] rowData = RowDataUtil.allocateRowData( data.outputRowMeta.size() );

    return rowData;
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    if ( first ) {
      first = false;

      data.outputRowMeta = new RowMeta();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );
    }

    if ( data.filenr >= data.list.size() ) {
      setOutputDone();
      return false;
    }

    // Get the next repository object from the list...
    //
    RepositoryElementMetaInterface repositoryObject = data.list.get( data.filenr++ );

    Object[] outputRow = buildEmptyRow();
    int outputIndex = 0;

    String directory = repositoryObject.getRepositoryDirectory().getPath();
    String name = repositoryObject.getName();
    String path = directory.endsWith( "/" ) ? directory + name : directory + "/" + name;

    outputRow[outputIndex++] = path; // the directory and name of the object
    outputRow[outputIndex++] = directory; // the directory
    outputRow[outputIndex++] = name; // the name
    outputRow[outputIndex++] = repositoryObject.getObjectType().getTypeDescription(); // the object type
    outputRow[outputIndex++] = repositoryObject.getObjectId().toString(); // the object ID
    outputRow[outputIndex++] = repositoryObject.getModifiedUser(); // modified user
    outputRow[outputIndex++] = repositoryObject.getModifiedDate(); // modified date
    outputRow[outputIndex++] = repositoryObject.getDescription(); // description

    if ( meta.isIncludeRowNumber() ) {
      outputRow[outputIndex++] = Long.valueOf( data.rownr++ );
    }

    // Finally, let's give this row of data to the next steps...
    //
    putRow( data.outputRowMeta, outputRow );
    if ( checkFeedback( getLinesInput() ) ) {
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "GetRepositoryNames.Log.NrLine", "" + getLinesInput() ) );
      }
    }

    return true;
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (GetRepositoryNamesMeta) smi;
    data = (GetRepositoryNamesData) sdi;

    if ( super.init( smi, sdi ) ) {

      try {
        // Get the repository objects from the repository...
        //
        data.list = getRepositoryObjects();

      } catch ( Exception e ) {
        logError( "Error initializing step: ", e );
        return false;
      }

      data.rownr = 1L;
      data.filenr = 0;

      return true;
    }
    return false;
  }

  @SuppressWarnings( "deprecation" )
  private List<RepositoryElementMetaInterface> getRepositoryObjects() throws KettleException {

    try {

      // The repository is available in the parent transformation metadata
      //
      Repository repository = getTransMeta().getRepository();

      // Now populate the list...
      //
      List<RepositoryElementMetaInterface> list = new ArrayList<RepositoryElementMetaInterface>();

      if ( repository instanceof RepositoryExtended ) {
        RepositoryExtended extendedRep = (RepositoryExtended) repository;
        for ( int i = 0; i < meta.getDirectory().length; i++ ) {
          String directoryPath = environmentSubstitute( meta.getDirectory()[i] );
          String filter = null;
          // by default we look for current level
          int depth = 0;
          if ( meta.getObjectTypeSelection().areTransformationsSelected() ) {
            filter = "*.ktr";
          }
          if ( meta.getObjectTypeSelection().areJobsSelected() ) {
            // if we have selected the job and transformation than we have applied filter with both condition
            filter = Utils.isEmpty( filter ) ? "*.kjb" : filter + "|*.kjb";
          }
          // should include unlimited subfolder
          if ( meta.getIncludeSubFolders()[i] ) {
            depth = -1;
          }
          RepositoryDirectoryInterface directory =
              extendedRep.loadRepositoryDirectoryTree( directoryPath, filter, depth, BooleanUtils
                .isTrue( repository.getUserInfo().isAdmin() ), false, false );

          list.addAll( getRepositoryObjects( directory, environmentSubstitute( meta.getNameMask()[i] ),
              environmentSubstitute( meta.getExcludeNameMask()[i] ) ) );
        }
      } else {
        // Get the repository directory tree.
        //
        RepositoryDirectoryInterface tree = repository.loadRepositoryDirectoryTree();

        // Loop over the directories and add the discovered objects to the list...
        //
        for ( int i = 0; i < meta.getDirectory().length; i++ ) {

          RepositoryDirectoryInterface dir = tree.findDirectory( environmentSubstitute( meta.getDirectory()[i] ) );
          if ( dir != null ) {
            List<RepositoryElementMetaInterface> objects =
                getRepositoryObjects( repository, dir, meta.getIncludeSubFolders()[i], environmentSubstitute( meta
                    .getNameMask()[i] ), environmentSubstitute( meta.getExcludeNameMask()[i] ) );
            list.addAll( objects );
          }
        }
      }

      return list;
    } catch ( Exception e ) {
      throw new KettleException( "Unable to get the list of repository objects from the repository", e );
    }
  }

  private List<RepositoryElementMetaInterface> getRepositoryObjects( Repository repository,
      RepositoryDirectoryInterface directory, boolean subdirs, String nameMask, String excludeNameMask )
    throws KettleException {
    List<RepositoryElementMetaInterface> list = new ArrayList<RepositoryElementMetaInterface>();
    List<RepositoryElementMetaInterface> objects = new ArrayList<RepositoryElementMetaInterface>();
    if ( meta.getObjectTypeSelection().areTransformationsSelected() ) {
      objects.addAll( repository.getTransformationObjects( directory.getObjectId(), false ) );
    }
    if ( meta.getObjectTypeSelection().areJobsSelected() ) {
      objects.addAll( repository.getJobObjects( directory.getObjectId(), false ) );
    }

    for ( RepositoryElementMetaInterface object : objects ) {
      boolean add = false;
      if ( Utils.isEmpty( nameMask ) || object.getName().matches( nameMask ) ) {
        add = true;
      }
      if ( !Utils.isEmpty( excludeNameMask ) && object.getName().matches( excludeNameMask ) ) {
        add = false;
      }
      if ( add ) {
        list.add( object );
      }
    }

    if ( subdirs ) {
      for ( RepositoryDirectoryInterface child : directory.getChildren() ) {
        list.addAll( getRepositoryObjects( repository, child, subdirs, nameMask, excludeNameMask ) );
      }
    }

    return list;
  }

  private List<RepositoryElementMetaInterface> getRepositoryObjects( RepositoryDirectoryInterface directory,
      String nameMask, String excludeNameMask ) throws KettleException {
    List<RepositoryElementMetaInterface> list = new ArrayList<RepositoryElementMetaInterface>();
    if ( directory.getRepositoryObjects() != null ) {
      for ( RepositoryElementMetaInterface object : directory.getRepositoryObjects() ) {
        boolean add = false;
        if ( Utils.isEmpty( nameMask ) || object.getName().matches( nameMask ) ) {
          add = true;
        }
        if ( !Utils.isEmpty( excludeNameMask ) && object.getName().matches( excludeNameMask ) ) {
          add = false;
        }
        if ( add ) {
          list.add( object );
        }
      }
    }
    if ( directory.getChildren() != null ) {
      for ( RepositoryDirectoryInterface subdir : directory.getChildren() ) {
        list.addAll( getRepositoryObjects( subdir, nameMask, excludeNameMask ) );
      }
    }
    return list;
  }

}
