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

package org.pentaho.di.trans.steps.xbaseinput;

import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.Utils;
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
 * Reads data from an XBase (dBase, foxpro, ...) file.
 *
 * @author Matt
 * @since 8-sep-2004
 */
public class XBaseInput extends BaseStep implements StepInterface {
  private static Class<?> PKG = XBaseInputMeta.class; // for i18n purposes, needed by Translator2!!

  private XBaseInputMeta meta;
  private XBaseInputData data;

  public XBaseInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (XBaseInputMeta) smi;
    data = (XBaseInputData) sdi;

    // See if we need to get a list of files from input...
    if ( first ) { // we just got started

      first = false;

      // The output row meta data, what does it look like?
      //
      data.outputRowMeta = new RowMeta();

      if ( meta.isAcceptingFilenames() ) {
        // Read the files from the specified input stream...
        data.files.getFiles().clear();

        int idx = -1;

        RowSet rowSet = findInputRowSet( meta.getAcceptingStepName() );
        Object[] fileRowData = getRowFrom( rowSet );
        while ( fileRowData != null ) {
          RowMetaInterface fileRowMeta = rowSet.getRowMeta();
          if ( idx < 0 ) {
            idx = fileRowMeta.indexOfValue( meta.getAcceptingField() );
            if ( idx < 0 ) {
              logError( BaseMessages.getString( PKG, "XBaseInput.Log.Error.UnableToFindFilenameField", meta
                .getAcceptingField() ) );
              setErrors( 1 );
              stopAll();
              return false;
            }
          }
          try {
            String filename = fileRowMeta.getString( fileRowData, idx );
            data.files.addFile( KettleVFS.getFileObject( filename, getTransMeta() ) );
          } catch ( Exception e ) {
            throw new KettleException( e );
          }

          // Grab another row
          //
          fileRowData = getRowFrom( rowSet );
        }

        if ( data.files.nrOfFiles() == 0 ) {
          logBasic( BaseMessages.getString( PKG, "XBaseInput.Log.Error.NoFilesSpecified" ) );
          setOutputDone();
          return false;
        }
      }

      data.outputRowMeta = meta.getOutputFields( data.files, getStepname() );

      // Open the first file & read the required rows in the buffer, stop
      // if it fails, exception will stop processLoop
      //
      openNextFile();
    }

    // Allocate the output row in advance, because we possibly want to add a few extra fields...
    //
    Object[] row = data.xbi.getRow( RowDataUtil.allocateRowData( data.outputRowMeta.size() ) );
    while ( row == null && data.fileNr < data.files.nrOfFiles() ) { // No more rows left in this file
      openNextFile();
      row = data.xbi.getRow( RowDataUtil.allocateRowData( data.outputRowMeta.size() ) );
    }

    if ( row == null ) {
      setOutputDone(); // signal end to receiver(s)
      return false; // end of data or error.
    }

    // OK, so we have read a line: increment the input counter
    incrementLinesInput();
    int outputIndex = data.fields.size();

    // Possibly add a filename...
    if ( meta.includeFilename() ) {
      row[outputIndex++] = data.file_dbf.getName().getURI();
    }

    // Possibly add a row number...
    if ( meta.isRowNrAdded() ) {
      row[outputIndex++] = new Long( getLinesInput() );
    }

    putRow( data.outputRowMeta, row ); // fill the rowset(s). (wait for empty)

    if ( checkFeedback( getLinesInput() ) ) {
      logBasic( BaseMessages.getString( PKG, "XBaseInput.Log.LineNr" ) + getLinesInput() );
    }

    if ( meta.getRowLimit() > 0 && getLinesInput() >= meta.getRowLimit() ) { // limit has been reached: stop now.
      setOutputDone();
      return false;
    }

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (XBaseInputMeta) smi;
    data = (XBaseInputData) sdi;

    if ( super.init( smi, sdi ) ) {
      data.files = meta.getTextFileList( this );
      data.fileNr = 0;

      if ( data.files.nrOfFiles() == 0 && !meta.isAcceptingFilenames() ) {
        logError( BaseMessages.getString( PKG, "XBaseInput.Log.Error.NoFilesSpecified" ) );
        return false;
      }
      if ( meta.isAcceptingFilenames() ) {
        try {
          if ( Utils.isEmpty( meta.getAcceptingStepName() )
            || findInputRowSet( meta.getAcceptingStepName() ) == null ) {
            logError( BaseMessages.getString( PKG, "XBaseInput.Log.Error.InvalidAcceptingStepName" ) );
            return false;
          }

          if ( Utils.isEmpty( meta.getAcceptingField() ) ) {
            logError( BaseMessages.getString( PKG, "XBaseInput.Log.Error.InvalidAcceptingFieldName" ) );
            return false;
          }
        } catch ( Exception e ) {
          logError( e.getMessage() );
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private void openNextFile() throws KettleException {
    // Close the last file before opening the next...
    if ( data.xbi != null ) {
      logBasic( BaseMessages.getString( PKG, "XBaseInput.Log.FinishedReadingRecords" ) );
      data.xbi.close();
    }

    // Replace possible environment variables...
    data.file_dbf = data.files.getFile( data.fileNr );
    data.fileNr++;

    try {
      data.xbi = new XBase( log, KettleVFS.getInputStream( data.file_dbf ) );
      data.xbi.setDbfFile( data.file_dbf.getName().getURI() );
      data.xbi.open();
      if ( !Utils.isEmpty( meta.getCharactersetName() ) ) {
        data.xbi.getReader().setCharactersetName( meta.getCharactersetName() );
      }

      logBasic( BaseMessages.getString( PKG, "XBaseInput.Log.OpenedXBaseFile" ) + " : [" + data.xbi + "]" );
      data.fields = data.xbi.getFields();

      // Add this to the result file names...
      ResultFile resultFile =
        new ResultFile( ResultFile.FILE_TYPE_GENERAL, data.file_dbf, getTransMeta().getName(), getStepname() );
      resultFile.setComment( BaseMessages.getString( PKG, "XBaseInput.ResultFile.Comment" ) );
      addResultFile( resultFile );
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "XBaseInput.Log.Error.CouldNotOpenXBaseFile1" )
        + data.file_dbf + BaseMessages.getString( PKG, "XBaseInput.Log.Error.CouldNotOpenXBaseFile2" )
        + e.getMessage() );
      throw new KettleException( e );
    }
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    closeLastFile();

    super.dispose( smi, sdi );
  }

  private void closeLastFile() {
    logBasic( BaseMessages.getString( PKG, "XBaseInput.Log.FinishedReadingRecords" ) );
    if ( data.xbi != null ) {
      data.xbi.close();
    }
  }

}
