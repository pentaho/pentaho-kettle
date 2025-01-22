/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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

  private void addFileToResultFilesName( FileObject file ) throws Exception {
    if ( meta.getAddResultFile() ) {
      // Add this to the result file names...
      ResultFile resultFile =
        new ResultFile( ResultFile.FILE_TYPE_GENERAL, file, getTransMeta().getName(), getStepname() );
      resultFile.setComment( "File was read by a LoadFileInput step" );
      addResultFile( resultFile );
    }
  }

  boolean openNextFile() {
    try {
      if ( meta.getFileInFields() ) {
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

          if ( meta.getFileInFields() ) {
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
        String fieldvalue = data.inputRowMeta.getString( data.readrow, data.indexOfFilenameField );

        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString(
            PKG, "LoadFileInput.Log.Stream", meta.getDynamicFilenameField(), fieldvalue ) );
        }

        try {
          // Source is a file.
          data.file = KettleVFS.getFileObject( fieldvalue );
        } catch ( Exception e ) {
          throw new KettleException( e );
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
        if ( !Utils.isEmpty( meta.getShortFileNameField() ) ) {
          data.shortFilename = data.file.getName().getBaseName();
        }
        if ( !Utils.isEmpty( meta.getPathField() ) ) {
          data.path = KettleVFS.getFilename( data.file.getParent() );
        }
        if ( !Utils.isEmpty( meta.isHiddenField() ) ) {
          data.hidden = data.file.isHidden();
        }
        if ( !Utils.isEmpty( meta.getExtensionField() ) ) {
          data.extension = data.file.getName().getExtension();
        }
        if ( !Utils.isEmpty( meta.getLastModificationDateField() ) ) {
          data.lastModificationDateTime = new Date( data.file.getContent().getLastModifiedTime() );
        }
        if ( !Utils.isEmpty( meta.getUriField() ) ) {
          data.uriName = Const.optionallyDecodeUriString( data.file.getName().getURI() );
        }
        if ( !Utils.isEmpty( meta.getRootUriField() ) ) {
          data.rootUriName = data.file.getName().getRootURI();
        }
        // get File content
        getFileContent();

        addFileToResultFilesName( data.file );

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

    if ( !nonExistantFiles.isEmpty() ) {
      String message = FileInputList.getRequiredFilesDescription( nonExistantFiles );
      logError( BaseMessages.getString( PKG, "LoadFileInput.Log.RequiredFilesTitle" ), BaseMessages.getString(
        PKG, "LoadFileInput.Log.RequiredFiles", message ) );

      throw new KettleException( BaseMessages.getString( PKG, "LoadFileInput.Log.RequiredFilesMissing", message ) );
    }

    List<FileObject> nonAccessibleFiles = data.files.getNonAccessibleFiles();
    if ( !nonAccessibleFiles.isEmpty() ) {
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
    return RowDataUtil.allocateRowData( data.outputRowMeta.size() );
  }

  Object[] getOneRow() throws KettleException {
    if ( !openNextFile() ) {
      return null;
    }

    // Build an empty row based on the meta-data
    Object[] outputRowData = buildEmptyRow();

    try {
      // Create new row or clone
      if ( meta.getFileInFields() ) {
        outputRowData = copyOrCloneArrayFromLoadFile( outputRowData, data.readrow );
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
                if ( !Utils.isEmpty( meta.getEncoding() ) ) {
                  data.filecontent = Const.ltrim( new String( data.filecontent, meta.getEncoding() ) ).getBytes();
                } else {
                  data.filecontent = Const.ltrim( new String( data.filecontent ) ).getBytes();
                }
                break;
              case LoadFileInputField.TYPE_TRIM_RIGHT:
                if ( !Utils.isEmpty( meta.getEncoding() ) ) {
                  data.filecontent = Const.rtrim( new String( data.filecontent, meta.getEncoding() ) ).getBytes();
                } else {
                  data.filecontent = Const.rtrim( new String( data.filecontent ) ).getBytes();
                }
                break;
              case LoadFileInputField.TYPE_TRIM_BOTH:
                if ( !Utils.isEmpty( meta.getEncoding() ) ) {
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
              if ( !Utils.isEmpty( meta.getEncoding() ) ) {
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
      if ( meta.getIncludeFilename() && !Utils.isEmpty( meta.getFilenameField() ) ) {
        outputRowData[rowIndex++] = data.filename;
      }

      // See if we need to add the row number to the row...
      if ( meta.getIncludeRowNumber() && !Utils.isEmpty( meta.getRowNumberField() ) ) {
        outputRowData[rowIndex++] = new Long( data.rownr );
      }
      // Possibly add short filename...
      if ( !Utils.isEmpty( meta.getShortFileNameField() ) ) {
        outputRowData[rowIndex++] = data.shortFilename;
      }
      // Add Extension
      if ( !Utils.isEmpty( meta.getExtensionField() ) ) {
        outputRowData[rowIndex++] = data.extension;
      }
      // add path
      if ( !Utils.isEmpty( meta.getPathField() ) ) {
        outputRowData[rowIndex++] = data.path;
      }

      // add Hidden
      if ( !Utils.isEmpty( meta.isHiddenField() ) ) {
        outputRowData[rowIndex++] = new Boolean( data.hidden );
      }
      // Add modification date
      if ( !Utils.isEmpty( meta.getLastModificationDateField() ) ) {
        outputRowData[rowIndex++] = data.lastModificationDateTime;
      }
      // Add Uri
      if ( !Utils.isEmpty( meta.getUriField() ) ) {
        outputRowData[rowIndex++] = data.uriName;
      }
      // Add RootUri
      if ( !Utils.isEmpty( meta.getRootUriField() ) ) {
        outputRowData[rowIndex++] = data.rootUriName;
      }
      RowMetaInterface irow = getInputRowMeta();

      data.previousRow = irow == null ? outputRowData : irow.cloneRow( outputRowData ); // copy it to make
      // surely the next step doesn't change it in between...

      incrementLinesInput();
      data.rownr++;

    } catch ( Exception e ) {
      throw new KettleException( "Error during processing a row", e );
    }

    return outputRowData;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (LoadFileInputMeta) smi;
    data = (LoadFileInputData) sdi;

    if ( super.init( smi, sdi ) ) {
      if ( !meta.getFileInFields() ) {
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

  protected Object[] copyOrCloneArrayFromLoadFile( Object[] outputRowData, Object[] readrow ) {
    // if readrow array is shorter than outputRowData reserved space, then we can not clone it because we have to
    // preserve the outputRowData reserved space. Clone, creates a new array with a new length, equals to the
    // readRow length and with that set we lost our outputRowData reserved space - needed for future additions.
    // The equals case works in both clauses, but arraycopy is up to 5 times faster for smaller arrays.
    if ( readrow.length <= outputRowData.length ) {
      System.arraycopy( readrow, 0, outputRowData, 0, readrow.length );
    } else {
      // if readrow array is longer than outputRowData reserved space, then we can only clone it.
      // Copy does not work here and will return an error since we are trying to copy a bigger array into a shorter one.
      outputRowData = readrow.clone();
    }
    return outputRowData;
  }

}
