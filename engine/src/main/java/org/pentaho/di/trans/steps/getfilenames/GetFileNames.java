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

package org.pentaho.di.trans.steps.getfilenames;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
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
public class GetFileNames extends BaseStep implements StepInterface {
  private static Class<?> PKG = GetFileNamesMeta.class; // for i18n purposes, needed by Translator2!!

  private GetFileNamesMeta meta;

  private GetFileNamesData data;

  public GetFileNames( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
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

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    if ( !meta.isFileField() ) {
      if ( data.filenr >= data.filessize ) {
        setOutputDone();
        return false;
      }
    } else {
      if ( data.filenr >= data.filessize ) {
        // Grab one row from previous step ...
        data.readrow = getRow();
      }

      if ( data.readrow == null ) {
        setOutputDone();
        return false;
      }

      if ( first ) {
        first = false;

        data.inputRowMeta = getInputRowMeta();
        data.outputRowMeta = data.inputRowMeta.clone();
        meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

        // Get total previous fields
        data.totalpreviousfields = data.inputRowMeta.size();

        // Check is filename field is provided
        if ( Utils.isEmpty( meta.getDynamicFilenameField() ) ) {
          logError( BaseMessages.getString( PKG, "GetFileNames.Log.NoField" ) );
          throw new KettleException( BaseMessages.getString( PKG, "GetFileNames.Log.NoField" ) );
        }

        // cache the position of the field
        if ( data.indexOfFilenameField < 0 ) {
          data.indexOfFilenameField = data.inputRowMeta.indexOfValue( meta.getDynamicFilenameField() );
          if ( data.indexOfFilenameField < 0 ) {
            // The field is unreachable !
            logError( BaseMessages.getString( PKG, "GetFileNames.Log.ErrorFindingField", meta
              .getDynamicFilenameField() ) );
            throw new KettleException( BaseMessages.getString(
              PKG, "GetFileNames.Exception.CouldnotFindField", meta.getDynamicFilenameField() ) );
          }
        }

        // If wildcard field is specified, Check if field exists
        if ( !Utils.isEmpty( meta.getDynamicWildcardField() ) ) {
          if ( data.indexOfWildcardField < 0 ) {
            data.indexOfWildcardField = data.inputRowMeta.indexOfValue( meta.getDynamicWildcardField() );
            if ( data.indexOfWildcardField < 0 ) {
              // The field is unreachable !
              logError( BaseMessages.getString( PKG, "GetFileNames.Log.ErrorFindingField" )
                + "[" + meta.getDynamicWildcardField() + "]" );
              throw new KettleException( BaseMessages.getString(
                PKG, "GetFileNames.Exception.CouldnotFindField", meta.getDynamicWildcardField() ) );
            }
          }
        }
        // If ExcludeWildcard field is specified, Check if field exists
        if ( !Utils.isEmpty( meta.getDynamicExcludeWildcardField() ) ) {
          if ( data.indexOfExcludeWildcardField < 0 ) {
            data.indexOfExcludeWildcardField =
              data.inputRowMeta.indexOfValue( meta.getDynamicExcludeWildcardField() );
            if ( data.indexOfExcludeWildcardField < 0 ) {
              // The field is unreachable !
              logError( BaseMessages.getString( PKG, "GetFileNames.Log.ErrorFindingField" )
                + "[" + meta.getDynamicExcludeWildcardField() + "]" );
              throw new KettleException( BaseMessages.getString(
                PKG, "GetFileNames.Exception.CouldnotFindField", meta.getDynamicExcludeWildcardField() ) );
            }
          }
        }
      }
    } // end if first

    try {
      Object[] outputRow = buildEmptyRow();
      int outputIndex = 0;
      Object[] extraData = new Object[data.nrStepFields];
      if ( meta.isFileField() ) {
        if ( data.filenr >= data.filessize ) {
          // Get value of dynamic filename field ...
          String filename = getInputRowMeta().getString( data.readrow, data.indexOfFilenameField );
          String wildcard = "";
          if ( data.indexOfWildcardField >= 0 ) {
            wildcard = getInputRowMeta().getString( data.readrow, data.indexOfWildcardField );
          }
          String excludewildcard = "";
          if ( data.indexOfExcludeWildcardField >= 0 ) {
            excludewildcard = getInputRowMeta().getString( data.readrow, data.indexOfExcludeWildcardField );
          }

          String[] filesname = { filename };
          String[] filesmask = { wildcard };
          String[] excludefilesmask = { excludewildcard };
          String[] filesrequired = { "N" };
          boolean[] includesubfolders = { meta.isDynamicIncludeSubFolders() };
          // Get files list
          data.files =
            meta.getDynamicFileList(
              this, filesname, filesmask, excludefilesmask, filesrequired, includesubfolders );
          data.filessize = data.files.nrOfFiles();
          data.filenr = 0;
        }

        // Clone current input row
        outputRow = data.readrow.clone();
      }
      if ( data.filessize > 0 ) {
        data.file = data.files.getFile( data.filenr );

        if ( meta.isAddResultFile() ) {
          // Add this to the result file names...
          ResultFile resultFile =
            new ResultFile( ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), getStepname() );
          resultFile.setComment( BaseMessages.getString( PKG, "GetFileNames.Log.FileReadByStep" ) );
          addResultFile( resultFile );
        }

        // filename
        extraData[outputIndex++] = KettleVFS.getFilename( data.file );

        // short_filename
        extraData[outputIndex++] = data.file.getName().getBaseName();

        try {
          // Path
          extraData[outputIndex++] = KettleVFS.getFilename( data.file.getParent() );

          // type
          extraData[outputIndex++] = data.file.getType().toString();

          // exists
          extraData[outputIndex++] = Boolean.valueOf( data.file.exists() );

          // ishidden
          extraData[outputIndex++] = Boolean.valueOf( data.file.isHidden() );

          // isreadable
          extraData[outputIndex++] = Boolean.valueOf( data.file.isReadable() );

          // iswriteable
          extraData[outputIndex++] = Boolean.valueOf( data.file.isWriteable() );

          // lastmodifiedtime
          extraData[outputIndex++] = new Date( data.file.getContent().getLastModifiedTime() );

          // size
          Long size = null;
          if ( data.file.getType().equals( FileType.FILE ) ) {
            size = new Long( data.file.getContent().getSize() );
          }

          extraData[outputIndex++] = size;

        } catch ( IOException e ) {
          throw new KettleException( e );
        }

        // extension
        extraData[outputIndex++] = data.file.getName().getExtension();

        // uri
        extraData[outputIndex++] = data.file.getName().getURI();

        // rooturi
        extraData[outputIndex++] = data.file.getName().getRootURI();

        // See if we need to add the row number to the row...
        if ( meta.includeRowNumber() && !Utils.isEmpty( meta.getRowNumberField() ) ) {
          extraData[outputIndex++] = new Long( data.rownr );
        }

        data.rownr++;
        // Add row data
        outputRow = RowDataUtil.addRowData( outputRow, data.totalpreviousfields, extraData );
        // Send row
        putRow( data.outputRowMeta, outputRow );

        if ( meta.getRowLimit() > 0 && data.rownr >= meta.getRowLimit() ) { // limit has been reached: stop now.
          setOutputDone();
          return false;
        }

      }
    } catch ( Exception e ) {
      throw new KettleStepException( e );
    }

    data.filenr++;

    if ( checkFeedback( getLinesInput() ) ) {
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "GetFileNames.Log.NrLine", "" + getLinesInput() ) );
      }
    }

    return true;
  }

  private void handleMissingFiles() throws KettleException {
    if ( meta.isdoNotFailIfNoFile() && data.files.nrOfFiles() == 0 ) {
      logBasic( BaseMessages.getString( PKG, "GetFileNames.Log.NoFile" ) );
      return;
    }
    List<FileObject> nonExistantFiles = data.files.getNonExistantFiles();

    if ( nonExistantFiles.size() != 0 ) {
      String message = FileInputList.getRequiredFilesDescription( nonExistantFiles );
      logBasic( "ERROR: Missing " + message );
      throw new KettleException( "Following required files are missing: " + message );
    }

    List<FileObject> nonAccessibleFiles = data.files.getNonAccessibleFiles();
    if ( nonAccessibleFiles.size() != 0 ) {
      String message = FileInputList.getRequiredFilesDescription( nonAccessibleFiles );
      logBasic( "WARNING: Not accessible " + message );
      throw new KettleException( "Following required files are not accessible: " + message );
    }
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (GetFileNamesMeta) smi;
    data = (GetFileNamesData) sdi;


    if ( super.init( smi, sdi ) ) {
      //Set Embedded NamedCluter MetatStore Provider Key so that it can be passed to VFS
      if ( getTransMeta().getNamedClusterEmbedManager() != null ) {
        getTransMeta().getNamedClusterEmbedManager()
          .passEmbeddedMetastoreKey( this, getTransMeta().getEmbeddedMetastoreProviderKey() );
      }

      try {
        // Create the output row meta-data
        data.outputRowMeta = new RowMeta();
        meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore ); // get the
                                                                                                      // metadata
                                                                                                      // populated
        data.nrStepFields = data.outputRowMeta.size();

        if ( !meta.isFileField() ) {
          data.files = meta.getFileList( this );
          data.filessize = data.files.nrOfFiles();
          handleMissingFiles();
        } else {
          data.filessize = 0;
        }

      } catch ( Exception e ) {
        logError( "Error initializing step: " + e.toString() );
        logError( Const.getStackTracker( e ) );
        return false;
      }

      data.rownr = 1L;
      data.filenr = 0;
      data.totalpreviousfields = 0;

      return true;

    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (GetFileNamesMeta) smi;
    data = (GetFileNamesData) sdi;
    if ( data.file != null ) {
      try {
        data.file.close();
        data.file = null;
      } catch ( Exception e ) {
        // Ignore close errors
      }

    }
    super.dispose( smi, sdi );
  }

}
