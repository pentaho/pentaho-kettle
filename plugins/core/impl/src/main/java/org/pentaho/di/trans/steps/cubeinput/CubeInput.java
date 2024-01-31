/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.cubeinput;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.zip.GZIPInputStream;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
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

public class CubeInput extends BaseStep implements StepInterface {
  private static Class<?> PKG = CubeInputMeta.class; // for i18n purposes, needed by Translator2!!

  private CubeInputMeta meta;
  private CubeInputData data;
  private int realRowLimit;

  public CubeInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    if ( first ) {
      first = false;
      meta = (CubeInputMeta) smi;
      data = (CubeInputData) sdi;
      realRowLimit = Const.toInt( environmentSubstitute( meta.getRowLimit() ), 0 );
    }


    try {
      Object[] r = data.meta.readData( data.dis );
      putRow( data.meta, r ); // fill the rowset(s). (sleeps if full)
      incrementLinesInput();

      if ( realRowLimit > 0 && getLinesInput() >= realRowLimit ) { // finished!
        setOutputDone();
        return false;
      }
    } catch ( KettleEOFException eof ) {
      setOutputDone();
      return false;
    } catch ( SocketTimeoutException e ) {
      throw new KettleException( e ); // shouldn't happen on files
    }

    if ( checkFeedback( getLinesInput() ) ) {
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "CubeInput.Log.LineNumber" ) + getLinesInput() );
      }
    }

    return true;
  }

  @Override public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (CubeInputMeta) smi;
    data = (CubeInputData) sdi;

    if ( super.init( smi, sdi ) ) {
      try {
        String filename = environmentSubstitute( meta.getFilename() );

        // Add filename to result filenames ?
        if ( meta.isAddResultFile() ) {
          ResultFile resultFile =
            new ResultFile(
              ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject( filename, getTransMeta() ),
              getTransMeta().getName(), toString() );
          resultFile.setComment( "File was read by a Cube Input step" );
          addResultFile( resultFile );
        }

        data.fis = KettleVFS.getInputStream( filename, this );
        data.zip = new GZIPInputStream( data.fis );
        data.dis = new DataInputStream( data.zip );

        try {
          data.meta = new RowMeta( data.dis );
          return true;
        } catch ( KettleFileException kfe ) {
          logError( BaseMessages.getString( PKG, "CubeInput.Log.UnableToReadMetadata" ), kfe );
          return false;
        }
      } catch ( Exception e ) {
        logError( BaseMessages.getString( PKG, "CubeInput.Log.ErrorReadingFromDataCube" ), e );
      }
    }
    return false;
  }

  @Override public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (CubeInputMeta) smi;
    data = (CubeInputData) sdi;

    try {
      if ( data.dis != null ) {
        data.dis.close();
        data.dis = null;
      }
      if ( data.zip != null ) {
        data.zip.close();
        data.zip = null;
      }
      if ( data.fis != null ) {
        data.fis.close();
        data.fis = null;
      }
    } catch ( IOException e ) {
      logError( BaseMessages.getString( PKG, "CubeInput.Log.ErrorClosingCube" ) + e.toString() );
      setErrors( 1 );
      stopAll();
    }
    // HACK! This is a temporary workaround for commons-vfs bug in 2.8.0 where a null pointer gets thrown
    // when the stream gets closed from a different thread. This has been fixed in 2.9.0, but we can't move
    // to that version due to a bug involving truncation on writes. This should be removed when can update
    // this library. See BACKLOG-39189, https://github.com/apache/commons-vfs/pull/167
    catch ( NullPointerException e ) {
      logDebug( "Catching exception to work around commons-vfs 2.8.0 close() bug: " + e.toString() );
    }

    super.dispose( smi, sdi );
  }
}
