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

package org.pentaho.di.trans.steps.getfilesrowscount;

import org.apache.commons.vfs2.FileType;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
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
 * Read all files, count rows number
 *
 * @author Samatar
 * @since 24-05-2007
 */
public class GetFilesRowsCount extends BaseStep implements StepInterface {
  private static Class<?> PKG = GetFilesRowsCountMeta.class; // for i18n purposes, needed by Translator2!!

  private GetFilesRowsCountMeta meta;
  private GetFilesRowsCountData data;

  // private static final int BUFFER_SIZE_INPUT_STREAM = 500;

  public GetFilesRowsCount( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
    TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  private Object[] getOneRow() throws KettleException {
    if ( !openNextFile() ) {
      return null;
    }

    // Build an empty row based on the meta-data
    Object[] r;
    try {
      // Create new row or clone
      if ( meta.isFileField() ) {
        r = data.readrow.clone();
        r = RowDataUtil.resizeArray( r, data.outputRowMeta.size() );
      } else {
        r = RowDataUtil.allocateRowData( data.outputRowMeta.size() );
      }

      if ( meta.isSmartCount() && data.foundData ) {
        // We have data right the last separator,
        // we need to update the row count
        data.rownr++;
      }

      r[data.totalpreviousfields] = data.rownr;

      if ( meta.includeCountFiles() ) {
        r[data.totalpreviousfields + 1] = data.filenr;
      }

      incrementLinesInput();

    } catch ( Exception e ) {
      throw new KettleException( "Unable to read row from file", e );
    }

    return r;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    try {
      // Grab one row
      Object[] outputRowData = getOneRow();
      if ( outputRowData == null ) {
        setOutputDone(); // signal end to receiver(s)
        return false; // end of data or error.
      }
      if ( ( !meta.isFileField() && data.last_file ) || meta.isFileField() ) {
        putRow( data.outputRowMeta, outputRowData ); // copy row to output rowset(s);
        if ( log.isDetailed() ) {
          logDetailed(
            BaseMessages.getString( PKG, "GetFilesRowsCount.Log.TotalRowsFiles" ), data.rownr, data.filenr );
        }
      }

    } catch ( KettleException e ) {

      logError( BaseMessages.getString( PKG, "GetFilesRowsCount.ErrorInStepRunning", e.getMessage() ) );
      setErrors( 1 );
      stopAll();
      setOutputDone(); // signal end to receiver(s)
      return false;
    }
    return true;

  }

  private void getRowNumber() throws KettleException {
    try {

      if ( data.file.getType() == FileType.FILE ) {
        data.fr = KettleVFS.getInputStream( data.file );
        // Avoid method calls - see here:
        // http://java.sun.com/developer/technicalArticles/Programming/PerfTuning/
        byte[] buf = new byte[8192]; // BufferedaInputStream default buffer size
        int n;
        boolean prevCR = false;
        while ( ( n = data.fr.read( buf ) ) != -1 ) {
          for ( int i = 0; i < n; i++ ) {
            data.foundData = true;
            if ( meta.getRowSeparatorFormat().equals( "CRLF" ) ) {
              // We need to check for CRLF
              if ( buf[i] == '\r' || buf[i] == '\n' ) {
                if ( buf[i] == '\r' ) {
                  // we have a carriage return
                  // keep track of it..maybe we will have a line feed right after :-)
                  prevCR = true;
                } else if ( buf[i] == '\n' ) {
                  // we have a line feed
                  // let's see if we had previously a carriage return
                  if ( prevCR ) {
                    // we have a carriage return followed by a line feed
                    data.rownr++;
                    // Maybe we won't have data after
                    data.foundData = false;
                    prevCR = false;
                  }
                }
              } else {
                // we have another char (other than \n , \r)
                prevCR = false;
              }

            } else {
              if ( buf[i] == data.separator ) {
                data.rownr++;
                // Maybe we won't have data after
                data.foundData = false;
              }
            }
          }
        }
      }
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "GetFilesRowsCount.Log.RowsInFile", data.file.toString(), ""
          + data.rownr ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( e );
    } finally {
      // Close inputstream - not used except for counting
      if ( data.fr != null ) {
        BaseStep.closeQuietly( data.fr );
        data.fr = null;
      }
    }

  }

  private boolean openNextFile() {
    if ( data.last_file ) {
      return false; // Done!
    }

    try {
      if ( !meta.isFileField() ) {
        if ( data.filenr >= data.files.nrOfFiles() ) {
          // finished processing!

          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "GetFilesRowsCount.Log.FinishedProcessing" ) );
          }
          return false;
        }

        // Is this the last file?
        data.last_file = ( data.filenr == data.files.nrOfFiles() - 1 );
        data.file = data.files.getFile( (int) data.filenr );

      } else {
        data.readrow = getRow(); // Get row from input rowset & set row busy!
        if ( data.readrow == null ) {
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "GetFilesRowsCount.Log.FinishedProcessing" ) );
          }
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
          if ( Utils.isEmpty( meta.setOutputFilenameField() ) ) {
            logError( BaseMessages.getString( PKG, "GetFilesRowsCount.Log.NoField" ) );
            throw new KettleException( BaseMessages.getString( PKG, "GetFilesRowsCount.Log.NoField" ) );
          }

          // cache the position of the field
          if ( data.indexOfFilenameField < 0 ) {
            data.indexOfFilenameField = getInputRowMeta().indexOfValue( meta.setOutputFilenameField() );
            if ( data.indexOfFilenameField < 0 ) {
              // The field is unreachable !
              logError( BaseMessages.getString( PKG, "GetFilesRowsCount.Log.ErrorFindingField", meta
                .setOutputFilenameField() ) );
              throw new KettleException( BaseMessages.getString(
                PKG, "GetFilesRowsCount.Exception.CouldnotFindField", meta.setOutputFilenameField() ) );
            }
          }

        } // End if first

        String filename = getInputRowMeta().getString( data.readrow, data.indexOfFilenameField );
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "GetFilesRowsCount.Log.FilenameInStream", meta
            .setOutputFilenameField(), filename ) );
        }

        data.file = KettleVFS.getFileObject( filename, getTransMeta() );

        // Init Row number
        if ( meta.isFileField() ) {
          data.rownr = 0;
        }
      }

      // Move file pointer ahead!
      data.filenr++;

      if ( meta.isAddResultFile() ) {
        // Add this to the result file names...
        ResultFile resultFile =
          new ResultFile( ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), getStepname() );
        resultFile.setComment( BaseMessages.getString( PKG, "GetFilesRowsCount.Log.FileAddedResult" ) );
        addResultFile( resultFile );
      }

      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "GetFilesRowsCount.Log.OpeningFile", data.file.toString() ) );
      }
      getRowNumber();
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "GetFilesRowsCount.Log.FileOpened", data.file.toString() ) );
      }

    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "GetFilesRowsCount.Log.UnableToOpenFile", "" + data.filenr, data.file
        .toString(), e.toString() ) );
      stopAll();
      setErrors( 1 );
      return false;
    }
    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (GetFilesRowsCountMeta) smi;
    data = (GetFilesRowsCountData) sdi;

    if ( super.init( smi, sdi ) ) {
      //Set Embedded NamedCluter MetatStore Provider Key so that it can be passed to VFS
      if ( getTransMeta().getNamedClusterEmbedManager() != null ) {
        getTransMeta().getNamedClusterEmbedManager()
          .passEmbeddedMetastoreKey( this, getTransMeta().getEmbeddedMetastoreProviderKey() );
      }
      if ( ( meta.getRowSeparatorFormat().equals( "CUSTOM" ) ) && ( Utils.isEmpty( meta.getRowSeparator() ) ) ) {
        logError( BaseMessages.getString( PKG, "GetFilesRowsCount.Error.NoSeparator.Title" ), BaseMessages
          .getString( PKG, "GetFilesRowsCount.Error.NoSeparator.Msg" ) );
        setErrors( 1 );
        stopAll();
      } else {
        // Checking for 'LF' for backwards compatibility.
        if ( meta.getRowSeparatorFormat().equals( "CARRIAGERETURN" ) || meta.getRowSeparatorFormat().equals( "LF" ) ) {
          data.separator = '\r';
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "GetFilesRowsCount.Log.Separator.Title" ), BaseMessages
              .getString( PKG, "GetFilesRowsCount.Log.Separatoris.Infos" )
              + " \\n" );
          }
        } else if ( meta.getRowSeparatorFormat().equals( "LINEFEED" )
          || meta.getRowSeparatorFormat().equals( "CR" ) ) {
          // Checking for 'CR' for backwards compatibility.
          data.separator = '\n';
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "GetFilesRowsCount.Log.Separator.Title" ), BaseMessages
              .getString( PKG, "GetFilesRowsCount.Log.Separatoris.Infos" )
              + " \\r" );
          }
        } else if ( meta.getRowSeparatorFormat().equals( "TAB" ) ) {
          data.separator = '\t';
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "GetFilesRowsCount.Log.Separator.Title" ), BaseMessages
              .getString( PKG, "GetFilesRowsCount.Log.Separatoris.Infos" )
              + " \\t" );
          }
        } else if ( meta.getRowSeparatorFormat().equals( "CRLF" ) ) {
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "GetFilesRowsCount.Log.Separator.Title" ), BaseMessages
              .getString( PKG, "GetFilesRowsCount.Log.Separatoris.Infos" )
              + " \\r\\n" );
          }
        } else {

          data.separator = environmentSubstitute( meta.getRowSeparator() ).charAt( 0 );

          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "GetFilesRowsCount.Log.Separator.Title" ), BaseMessages
              .getString( PKG, "GetFilesRowsCount.Log.Separatoris.Infos" )
              + " " + data.separator );
          }
        }
      }

      if ( !meta.isFileField() ) {
        data.files = meta.getFiles( this );
        if ( data.files == null || data.files.nrOfFiles() == 0 ) {
          logError( BaseMessages.getString( PKG, "GetFilesRowsCount.Log.NoFiles" ) );
          return false;
        }
        try {
          // Create the output row meta-data
          data.outputRowMeta = new RowMeta();
          meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore ); // get the
                                                                                                        // metadata
                                                                                                        // populated

        } catch ( Exception e ) {
          logError( "Error initializing step: " + e.toString() );
          logError( Const.getStackTracker( e ) );
          return false;
        }
      }
      data.rownr = 0;
      data.filenr = 0;
      data.totalpreviousfields = 0;

      return true;
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (GetFilesRowsCountMeta) smi;
    data = (GetFilesRowsCountData) sdi;
    if ( data.file != null ) {
      try {
        data.file.close();
        data.file = null;
      } catch ( Exception e ) {
        log.logError( "Error closing file", e );
      }
    }
    if ( data.fr != null ) {
      BaseStep.closeQuietly( data.fr );
      data.fr = null;
    }
    if ( data.lineStringBuilder != null ) {
      data.lineStringBuilder = null;
    }

    super.dispose( smi, sdi );
  }

}
