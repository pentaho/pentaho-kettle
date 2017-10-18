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

package org.pentaho.di.trans.steps.cubeoutput;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
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
 * Outputs a stream/series of rows to a file, effectively building a sort of (compressed) microcube.
 *
 * @author Matt
 * @since 4-apr-2003
 */

public class CubeOutput extends BaseStep implements StepInterface {
  private static Class<?> PKG = CubeOutputMeta.class; // for i18n purposes, needed by Translator2!!

  private CubeOutputMeta meta;
  private CubeOutputData data;

  public CubeOutput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (CubeOutputMeta) smi;
    data = (CubeOutputData) sdi;

    Object[] r;
    boolean result = true;

    r = getRow(); // This also waits for a row to be finished.

    if ( first ) { // Always run this code once, even if stream is empty (r==null)
      if ( getInputRowMeta() != null ) {
        data.outputMeta = getInputRowMeta().clone();
      } else {
        // If the stream is empty, then row metadata probably hasn't been received. In this case, use
        // the design-time algorithm to calculate the output metadata.
        data.outputMeta = getTransMeta().getPrevStepFields( getStepMeta() );
      }

      // If input stream is empty, but file was already opened in init(), then
      // write metadata so as to create a valid, empty cube file.
      if ( r == null && data.oneFileOpened ) {
        result = writeHeaderToFile();
        if ( !result ) {
          setErrors( 1 );
          stopAll();
          return false;
        }
      }
    }

    if ( r == null ) {
      setOutputDone();
      return false;
    }
    if ( first ) {
      if ( meta.isDoNotOpenNewFileInit() ) {
        try {
          prepareFile();
          data.oneFileOpened = true;
        } catch ( KettleFileException ioe ) {
          logError( BaseMessages.getString( PKG, "CubeOutput.Log.ErrorOpeningCubeOutputFile" ) + ioe.toString() );
          setErrors( 1 );
          return false;
        }
      }

      result = writeHeaderToFile();
      if ( !result ) {
        setErrors( 1 );
        stopAll();
        return false;
      }

      first = false;
    }
    result = writeRowToFile( r );
    if ( !result ) {
      setErrors( 1 );
      stopAll();
      return false;
    }

    putRow( data.outputMeta, r ); // in case we want it to go further...

    if ( checkFeedback( getLinesOutput() ) ) {
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "CubeOutput.Log.LineNumber" ) + getLinesOutput() );
      }
    }

    return result;
  }

  private synchronized boolean writeHeaderToFile() {
    try {
      data.outputMeta.writeMeta( data.dos );
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "CubeOutput.Log.ErrorWritingLine" ) + e.toString() );
      return false;
    }

    return true;
  }

  private synchronized boolean writeRowToFile( Object[] r ) {
    try {
      // Write data to the cube file...
      data.outputMeta.writeData( data.dos, r );
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "CubeOutput.Log.ErrorWritingLine" ) + e.toString() );
      return false;
    }

    incrementLinesOutput();

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (CubeOutputMeta) smi;
    data = (CubeOutputData) sdi;

    if ( super.init( smi, sdi ) ) {
      if ( !meta.isDoNotOpenNewFileInit() ) {
        try {
          prepareFile();
          data.oneFileOpened = true;
          return true;
        } catch ( KettleFileException ioe ) {
          logError( BaseMessages.getString( PKG, "CubeOutput.Log.ErrorOpeningCubeOutputFile" ) + ioe.toString() );
        }
      } else {
        return true;
      }

    }
    return false;
  }

  private void prepareFile() throws KettleFileException {
    try {
      String filename = environmentSubstitute( meta.getFilename() );
      if ( meta.isAddToResultFiles() ) {
        // Add this to the result file names...
        ResultFile resultFile =
          new ResultFile(
            ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject( filename, getTransMeta() ), getTransMeta()
              .getName(), getStepname() );
        resultFile.setComment( "This file was created with a cube file output step" );
        addResultFile( resultFile );
      }

      data.fos = KettleVFS.getOutputStream( filename, getTransMeta(), false );
      data.zip = new GZIPOutputStream( data.fos );
      data.dos = new DataOutputStream( data.zip );
    } catch ( Exception e ) {
      throw new KettleFileException( e );
    }
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    if ( data.oneFileOpened ) {
      try {
        if ( data.dos != null ) {
          data.dos.close();
          data.dos = null;
        }
        if ( data.zip != null ) {
          data.zip.close();
          data.zip = null;
        }
        if ( data.fos != null ) {
          data.fos.close();
          data.fos = null;
        }
      } catch ( IOException e ) {
        logError( BaseMessages.getString( PKG, "CubeOutput.Log.ErrorClosingFile" ) + meta.getFilename() );
        setErrors( 1 );
        stopAll();
      }
    }

    super.dispose( smi, sdi );
  }
}
