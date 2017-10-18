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

package org.pentaho.di.trans.steps.accessoutput;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
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
 * Writes rows to a database table.
 *
 * @author Matt
 * @since 6-apr-2003
 */
public class AccessOutput extends BaseStep implements StepInterface {
  private static Class<?> PKG = AccessOutput.class; // for i18n purposes, needed by Translator2!!

  public static final int COMMIT_SIZE = 500;

  private AccessOutputMeta meta;
  private AccessOutputData data;

  public AccessOutput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (AccessOutputMeta) smi;
    data = (AccessOutputData) sdi;

    Object[] r = getRow(); // this also waits for a previous step to be finished.
    if ( r == null ) {
      // no more input to be expected...
      setOutputDone();
      return false;
    }

    if ( first && meta.isDoNotOpenNewFileInit() ) {
      try {
        if ( !openFile() ) {
          return false;
        }

      } catch ( Exception e ) {
        logError( "An error occurred intialising this step: " + e.getMessage() );
        stopAll();
        setErrors( 1 );
      }
    }
    try {
      writeToTable( r );
      putRow( data.outputRowMeta, r ); // in case we want it go further...

      if ( checkFeedback( getLinesOutput() ) ) {
        if ( log.isBasic() ) {
          logBasic( "linenr " + getLinesOutput() );
        }
      }
    } catch ( KettleException e ) {
      logError( "Because of an error, this step can't continue: " + e.getMessage() );
      setErrors( 1 );
      stopAll();
      setOutputDone(); // signal end to receiver(s)
      return false;
    }

    return true;
  }

  private boolean writeToTable( Object[] rowData ) throws KettleValueException {
    if ( rowData == null ) {
      // Stop: last line or error encountered
      if ( log.isDetailed() ) {
        logDetailed( "Last line inserted: stop" );
      }
      return false;
    }

    if ( first ) {
      first = false;

      data.outputRowMeta = getInputRowMeta();

      // First open or create the table
      try {
        String realTablename = environmentSubstitute( meta.getTablename() );
        data.table = data.db.getTable( realTablename );
        if ( data.table == null ) {
          if ( meta.isTableCreated() ) {
            // Create the table
            data.createTable( realTablename, data.outputRowMeta );
          } else {
            logError( BaseMessages.getString( PKG, "AccessOutput.Error.TableDoesNotExist", realTablename ) );
            setErrors( 1 );
            stopAll();
            return false;
          }
        } else if ( meta.isTableTruncated() ) {
          data.truncateTable();
        }
        // All OK: we have an open database and a table to write to.
        //
        // Apparently it's not yet possible to remove rows from the table
        // So truncate is out for the moment as well.

      } catch ( Exception e ) {
        logError( BaseMessages
          .getString( PKG, "AccessOutput.Exception.UnexpectedErrorCreatingTable", e.toString() ) );
        logError( Const.getStackTracker( e ) );
        setErrors( 1 );
        stopAll();
        return false;
      }
    }

    // Let's write a row to the database.
    Object[] columnValues = AccessOutputMeta.createObjectsForRow( data.outputRowMeta, rowData );
    try {
      data.rows.add( columnValues );
      if ( meta.getCommitSize() > 0 ) {
        if ( data.rows.size() >= meta.getCommitSize() ) {
          data.addRowsToTable( data.rows );
          data.rows.clear();
        }
      } else {
        data.addRowToTable( columnValues );
      }
    } catch ( IOException e ) {
      logError( BaseMessages.getString(
        PKG, "AccessOutput.Exception.UnexpectedErrorWritingRow", data.outputRowMeta.getString( rowData ) ) );
      logError( Const.getStackTracker( e ) );
      setErrors( 1 );
      stopAll();
      return false;
    }

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (AccessOutputMeta) smi;
    data = (AccessOutputData) sdi;

    if ( super.init( smi, sdi ) ) {
      if ( !meta.isDoNotOpenNewFileInit() ) {
        try {
          return openFile();

        } catch ( Exception e ) {
          logError( "An error occurred intialising this step: " + e.getMessage() );
          stopAll();
          setErrors( 1 );
        }
      } else {
        return true;
      }
    }
    return false;
  }

  boolean openFile() throws Exception {
    data.oneFileOpened = true;
    String realFilename = environmentSubstitute( meta.getFilename() );
    if ( log.isBasic() ) {
      logBasic( BaseMessages.getString( PKG, "AccessOutput.log.WritingToFile", realFilename ) );
    }
    FileObject fileObject = KettleVFS.getFileObject( realFilename, getTransMeta() );
    File file = FileUtils.toFile( fileObject.getURL() );

    // First open or create the access file
    if ( !file.exists() ) {
      if ( meta.isFileCreated() ) {
        data.createDatabase( file );
      } else {
        logError( BaseMessages.getString( PKG, "AccessOutput.InitError.FileDoesNotExist", realFilename ) );
        return false;
      }
    } else {
      data.openDatabase( file );
    }

    // Add the filename to the result object...
    //
    if ( meta.isAddToResultFiles() ) {
      ResultFile resultFile =
        new ResultFile( ResultFile.FILE_TYPE_GENERAL, fileObject, getTransMeta().getName(), toString() );
      resultFile.setComment( "This file was created with an access output step" );
      addResultFile( resultFile );
    }

    return true;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (AccessOutputMeta) smi;
    data = (AccessOutputData) sdi;
    if ( data.oneFileOpened ) {
      try {
        // Put the last records in the table as well!
        if ( data.table != null ) {
          data.addRowsToTable( data.rows );
        }

        // Just for good measure.
        data.rows.clear();

        if ( data.db != null ) {
          data.closeDatabase();
        }
      } catch ( IOException e ) {
        logError( "Error closing the database: " + e.toString() );
        setErrors( 1 );
        stopAll();
      }
    }
    super.dispose( smi, sdi );
  }
}
