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

package org.pentaho.di.trans.steps.loadfileinput;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
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
 * Read files, parse them and convert them to rows and writes these to one or more output streams.
 *
 * @author Samatar
 * @since 20-06-2007
 */
public class LoadFileInput extends BaseStep implements StepInterface {
  private static Class<?> PKG = LoadFileInputMeta.class; // for i18n purposes, needed by Translator2!!

  LoadFileInputMeta meta;
  LoadFileInputData data;

  public LoadFileInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  private void addFileToResultFilesname( FileObject file ) throws Exception {
    if ( meta.addResultFile() ) {
      // Add this to the result file names...
      ResultFile resultFile =
        new ResultFile( ResultFile.FILE_TYPE_GENERAL, file, getTransMeta().getName(), getStepname() );
      resultFile.setComment( "File was read by a LoadFileInput step" );
      addResultFile( resultFile );
    }
  }

  boolean openNextFile() {
    try {
      if ( meta.getIsInFields() ) {
        data.readrow = getRow(); // Grab another row ...

        if ( data.readrow == null ) { // finished processing!

          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "LoadFileInput.Log.FinishedProcessing" ) );
          }
          return false;
        }

        if ( first ) {
          first = false;

          data.inputRowMeta = getInputRowMeta();
          data.outputRowMeta = data.inputRowMeta.clone();
          meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

          // Create convert meta-data objects that will contain Date & Number formatters
          // All non binary content is handled as a String. It would be converted to the target type after the processing.
          data.convertRowMeta = data.outputRowMeta.cloneToType( ValueMetaInterface.TYPE_STRING );

          if ( meta.getIsInFields() ) {
            // Check is filename field is provided
            if ( Utils.isEmpty( meta.getDynamicFilenameField() ) ) {
              logError( BaseMessages.getString( PKG, "LoadFileInput.Log.NoField" ) );
              throw new KettleException( BaseMessages.getString( PKG, "LoadFileInput.Log.NoField" ) );
            }

            // cache the position of the field
            if ( data.indexOfFilenameField < 0 ) {
              data.indexOfFilenameField = data.inputRowMeta.indexOfValue( meta.getDynamicFilenameField() );
              if ( data.indexOfFilenameField < 0 ) {
                // The field is unreachable !
                logError( BaseMessages.getString( PKG, "LoadFileInput.Log.ErrorFindingField" )
                  + "[" + meta.getDynamicFilenameField() + "]" );
                throw new KettleException( BaseMessages.getString(
                  PKG, "LoadFileInput.Exception.CouldnotFindField", meta.getDynamicFilenameField() ) );
              }
            }
            // Get the number of previous fields
            data.totalpreviousfields = data.inputRowMeta.size();

          }
        } // end if first

        // get field value
        String Fieldvalue = data.inputRowMeta.getString( data.readrow, data.indexOfFilenameField );

        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString(
            PKG, "LoadFileInput.Log.Stream", meta.getDynamicFilenameField(), Fieldvalue ) );
        }

        FileObject file = null;
        try {
          // Source is a file.
          data.file = KettleVFS.getFileObject( Fieldvalue );
        } catch ( Exception e ) {
          throw new KettleException( e );
        } finally {
          try {
            if ( file != null ) {
              file.close();
            }
          } catch ( Exception e ) {
            // Ignore errors
          }
        }
      } else {
        if ( data.filenr >= data.files.nrOfFiles() ) {
          // finished processing!

          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "LoadFileInput.Log.FinishedProcessing" ) );
          }
          return false;
        }

        // Is this the last file?
        data.last_file = ( data.filenr == data.files.nrOfFiles() - 1 );
        data.file = data.files.getFile( data.filenr );
      }

      // Check if file exists
      if ( meta.isIgnoreMissingPath() && !data.file.exists() ) {
        logBasic( BaseMessages.getString( PKG, "LoadFileInput.Error.FileNotExists", "" + data.file.getName() ) );
        return openNextFile();
      }

      // Check if file is empty
      data.fileSize = data.file.getContent().getSize();
      // Move file pointer ahead!
      data.filenr++;

      if ( meta.isIgnoreEmptyFile() && data.fileSize == 0 ) {
        logError( BaseMessages.getString( PKG, "LoadFileInput.Error.FileSizeZero", "" + data.file.getName() ) );
        return openNextFile();

      } else {
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "LoadFileInput.Log.OpeningFile", data.file.toString() ) );
        }
        data.filename = KettleVFS.getFilename( data.file );
        // Add additional fields?
        if ( meta.getShortFileNameField() != null && meta.getShortFileNameField().length() > 0 ) {
          data.shortFilename = data.file.getName().getBaseName();
        }
        if ( meta.getPathField() != null && meta.getPathField().length() > 0 ) {
          data.path = KettleVFS.getFilename( data.file.getParent() );
        }
        if ( meta.isHiddenField() != null && meta.isHiddenField().length() > 0 ) {
          data.hidden = data.file.isHidden();
        }
        if ( meta.getExtensionField() != null && meta.getExtensionField().length() > 0 ) {
          data.extension = data.file.getName().getExtension();
        }
        if ( meta.getLastModificationDateField() != null && meta.getLastModificationDateField().length() > 0 ) {
          data.lastModificationDateTime = new Date( data.file.getContent().getLastModifiedTime() );
        }
        if ( meta.getUriField() != null && meta.getUriField().length() > 0 ) {
          data.uriName = data.file.getName().getURI();
        }
        if ( meta.getRootUriField() != null && meta.getRootUriField().length() > 0 ) {
          data.rootUriName = data.file.getName().getRootURI();
        }
        // get File content
        getFileContent();

        addFileToResultFilesname( data.file );

        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "LoadFileInput.Log.FileOpened", data.file.toString() ) );
        }
      }

    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "LoadFileInput.Log.UnableToOpenFile", "" + data.filenr, data.file
        .toString(), e.toString() ) );
      stopAll();
      setErrors( 1 );
      return false;
    }
    return true;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    try {
      // Grab a row
      Object[] outputRowData = getOneRow();
      if ( outputRowData == null ) {
        setOutputDone(); // signal end to receiver(s)
        return false; // end of data or error.
      }

      if ( isRowLevel() ) {
        logRowlevel( BaseMessages.getString( PKG, "LoadFileInput.Log.ReadRow", data.outputRowMeta
          .getString( outputRowData ) ) );
      }

      putRow( data.outputRowMeta, outputRowData );

      if ( meta.getRowLimit() > 0 && data.rownr > meta.getRowLimit() ) { // limit has been reached: stop now.
        setOutputDone();
        return false;
      }
    } catch ( KettleException e ) {
      logError( BaseMessages.getString( PKG, "LoadFileInput.ErrorInStepRunning", e.getMessage() ) );
      logError( Const.getStackTracker( e ) );
      setErrors( 1 );
      stopAll();
      setOutputDone(); // signal end to receiver(s)
      return false;
    }
    return true;

  }

  void getFileContent() throws KettleException {
    try {
      data.filecontent = getFileBinaryContent( data.file.toString() );
    } catch ( java.lang.OutOfMemoryError o ) {
      logError( "There is no enaugh memory to load the content of the file [" + data.file.getName() + "]" );
      throw new KettleException( o );
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  /**
   * Read a file.
   *
   * @param vfsFilename
   *          the filename or URL to read from
   * @return The content of the file as a byte[]
   * @throws KettleException
   */
  public static byte[] getFileBinaryContent( String vfsFilename ) throws KettleException {
    InputStream inputStream = null;

    byte[] retval = null;
    try {
      inputStream = KettleVFS.getInputStream( vfsFilename );
      retval = IOUtils.toByteArray( new BufferedInputStream( inputStream ) );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "LoadFileInput.Error.GettingFileContent", vfsFilename, e.toString() ) );
    } finally {
      if ( inputStream != null ) {
        try {
          inputStream.close();
        } catch ( Exception e ) { /* Ignore */
        }
      }
    }

    return retval;
  }

  private void handleMissingFiles() throws KettleException {
    List<FileObject> nonExistantFiles = data.files.getNonExistantFiles();

    if ( nonExistantFiles.size() != 0 ) {
      String message = FileInputList.getRequiredFilesDescription( nonExistantFiles );
      logError( BaseMessages.getString( PKG, "LoadFileInput.Log.RequiredFilesTitle" ), BaseMessages.getString(
        PKG, "LoadFileInput.Log.RequiredFiles", message ) );

      throw new KettleException( BaseMessages.getString( PKG, "LoadFileInput.Log.RequiredFilesMissing", message ) );
    }

    List<FileObject> nonAccessibleFiles = data.files.getNonAccessibleFiles();
    if ( nonAccessibleFiles.size() != 0 ) {
      String message = FileInputList.getRequiredFilesDescription( nonAccessibleFiles );
      logError( BaseMessages.getString( PKG, "LoadFileInput.Log.RequiredFilesTitle" ), BaseMessages.getString(
        PKG, "LoadFileInput.Log.RequiredNotAccessibleFiles", message ) );
      throw new KettleException( BaseMessages.getString(
        PKG, "LoadFileInput.Log.RequiredNotAccessibleFilesMissing", message ) );
    }
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

  Object[] getOneRow() throws KettleException {
    if ( !openNextFile() ) {
      return null;
    }

    // Build an empty row based on the meta-data
    Object[] outputRowData = buildEmptyRow();

    try {
      // Create new row or clone
      if ( meta.getIsInFields() ) {
        System.arraycopy( data.readrow, 0, outputRowData, 0, data.readrow.length );
      }

      // Read fields...
      for ( int i = 0; i < data.nrInputFields; i++ ) {
        // Get field
        LoadFileInputField loadFileInputField = meta.getInputFields()[i];

        Object o = null;
        int indexField = data.totalpreviousfields + i;
        ValueMetaInterface targetValueMeta = data.outputRowMeta.getValueMeta( indexField );
        ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta( indexField );

        switch ( loadFileInputField.getElementType() ) {
          case LoadFileInputField.ELEMENT_TYPE_FILECONTENT:

            // DO Trimming!
            switch ( loadFileInputField.getTrimType() ) {
              case LoadFileInputField.TYPE_TRIM_LEFT:
                if ( meta.getEncoding() != null ) {
                  data.filecontent = Const.ltrim( new String( data.filecontent, meta.getEncoding() ) ).getBytes();
                } else {
                  data.filecontent = Const.ltrim( new String( data.filecontent ) ).getBytes();
                }
                break;
              case LoadFileInputField.TYPE_TRIM_RIGHT:
                if ( meta.getEncoding() != null ) {
                  data.filecontent = Const.rtrim( new String( data.filecontent, meta.getEncoding() ) ).getBytes();
                } else {
                  data.filecontent = Const.rtrim( new String( data.filecontent ) ).getBytes();
                }
                break;
              case LoadFileInputField.TYPE_TRIM_BOTH:
                if ( meta.getEncoding() != null ) {
                  data.filecontent = Const.trim( new String( data.filecontent, meta.getEncoding() ) ).getBytes();
                } else {
                  data.filecontent = Const.trim( new String( data.filecontent ) ).getBytes();
                }
                break;
              default:
                break;
            }
            if ( targetValueMeta.getType() != ValueMetaInterface.TYPE_BINARY ) {
              // handle as a String
              if (  meta.getEncoding() != null ) {
                o = new String( data.filecontent, meta.getEncoding() );
              } else {
                o = new String( data.filecontent );
              }
            } else {
              // save as byte[] without any conversion
              o = data.filecontent;
            }
            break;
          case LoadFileInputField.ELEMENT_TYPE_FILESIZE:
            o = String.valueOf( data.fileSize );
            break;
          default:
            break;
        }

        if ( targetValueMeta.getType() == ValueMetaInterface.TYPE_BINARY ) {
          // save as byte[] without any conversion
          outputRowData[indexField] = o;
        } else {
          // convert string (processing type) to the target type
          outputRowData[indexField] = targetValueMeta.convertData( sourceValueMeta, o );
        }

        // Do we need to repeat this field if it is null?
        if ( loadFileInputField.isRepeated() ) {
          if ( data.previousRow != null && o == null ) {
            outputRowData[indexField] = data.previousRow[indexField];
          }
        }
      } // End of loop over fields...
      int rowIndex = data.totalpreviousfields + data.nrInputFields;

      // See if we need to add the filename to the row...
      if ( meta.includeFilename() && meta.getFilenameField() != null && meta.getFilenameField().length() > 0 ) {
        outputRowData[rowIndex++] = data.filename;
      }

      // See if we need to add the row number to the row...
      if ( meta.includeRowNumber() && meta.getRowNumberField() != null && meta.getRowNumberField().length() > 0 ) {
        outputRowData[rowIndex++] = new Long( data.rownr );
      }
      // Possibly add short filename...
      if ( meta.getShortFileNameField() != null && meta.getShortFileNameField().length() > 0 ) {
        outputRowData[rowIndex++] = data.shortFilename;
      }
      // Add Extension
      if ( meta.getExtensionField() != null && meta.getExtensionField().length() > 0 ) {
        outputRowData[rowIndex++] = data.extension;
      }
      // add path
      if ( meta.getPathField() != null && meta.getPathField().length() > 0 ) {
        outputRowData[rowIndex++] = data.path;
      }

      // add Hidden
      if ( meta.isHiddenField() != null && meta.isHiddenField().length() > 0 ) {
        outputRowData[rowIndex++] = new Boolean( data.hidden );
      }
      // Add modification date
      if ( meta.getLastModificationDateField() != null && meta.getLastModificationDateField().length() > 0 ) {
        outputRowData[rowIndex++] = data.lastModificationDateTime;
      }
      // Add Uri
      if ( meta.getUriField() != null && meta.getUriField().length() > 0 ) {
        outputRowData[rowIndex++] = data.uriName;
      }
      // Add RootUri
      if ( meta.getRootUriField() != null && meta.getRootUriField().length() > 0 ) {
        outputRowData[rowIndex++] = data.rootUriName;
      }
      RowMetaInterface irow = getInputRowMeta();

      data.previousRow = irow == null ? outputRowData : irow.cloneRow( outputRowData ); // copy it to make
      // surely the next step doesn't change it in between...

      incrementLinesInput();
      data.rownr++;

    } catch ( Exception e ) {
      throw new KettleException( "Impossible de charger le fichier", e );
    }

    return outputRowData;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (LoadFileInputMeta) smi;
    data = (LoadFileInputData) sdi;

    if ( super.init( smi, sdi ) ) {
      if ( !meta.getIsInFields() ) {
        try {
          data.files = meta.getFiles( this );
          handleMissingFiles();
          // Create the output row meta-data
          data.outputRowMeta = new RowMeta();
          meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore ); // get the
                                                                                                        // metadata
                                                                                                        // populated

          // Create convert meta-data objects that will contain Date & Number formatters
          // All non binary content is handled as a String. It would be converted to the target type after the processing.
          data.convertRowMeta = data.outputRowMeta.cloneToType( ValueMetaInterface.TYPE_STRING );
        } catch ( Exception e ) {
          logError( "Error at step initialization: " + e.toString() );
          logError( Const.getStackTracker( e ) );
          return false;
        }
      }
      data.rownr = 1L;
      data.nrInputFields = meta.getInputFields().length;

      return true;
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (LoadFileInputMeta) smi;
    data = (LoadFileInputData) sdi;
    if ( data.file != null ) {
      try {
        data.file.close();
      } catch ( Exception e ) {
        // Ignore errors
      }
    }
    super.dispose( smi, sdi );
  }

}
