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

package org.pentaho.di.trans.steps.filestoresult;

import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
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
 * Writes filenames to a next job entry in a Job
 *
 * @author matt
 * @since 26-may-2006
 */
public class FilesToResult extends BaseStep implements StepInterface {
  private static Class<?> PKG = FilesToResultMeta.class; // for i18n purposes, needed by Translator2!!

  private FilesToResultMeta meta;

  private FilesToResultData data;

  public FilesToResult( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (FilesToResultMeta) smi;
    data = (FilesToResultData) sdi;

    Object[] r = getRow(); // get row, set busy!
    if ( r == null ) { // no more input to be expected...

      for ( ResultFile resultFile : data.filenames ) {
        addResultFile( resultFile );
      }
      logBasic( BaseMessages.getString( PKG, "FilesToResult.Log.AddedNrOfFiles", String.valueOf( data.filenames
        .size() ) ) );
      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;

      data.filenameIndex = getInputRowMeta().indexOfValue( meta.getFilenameField() );

      if ( data.filenameIndex < 0 ) {
        logError( BaseMessages.getString( PKG, "FilesToResult.Log.CouldNotFindField", meta.getFilenameField() ) );
        setErrors( 1 );
        stopAll();
        return false;
      }
    }

    // OK, get the filename field from the row
    String filename = getInputRowMeta().getString( r, data.filenameIndex );

    try {
      ResultFile resultFile =
        new ResultFile( meta.getFileType(), KettleVFS.getFileObject( filename, getTransMeta() ), getTrans()
          .getName(), getStepname() );

      // Add all rows to rows buffer...
      data.filenames.add( resultFile );
    } catch ( Exception e ) {
      throw new KettleException( e );
    }

    // Copy to any possible next steps...
    data.outputRowMeta = getInputRowMeta().clone();
    meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );
    putRow( data.outputRowMeta, r ); // copy row to possible alternate
    // rowset(s).

    if ( checkFeedback( getLinesRead() ) ) {
      logBasic( BaseMessages.getString( PKG, "FilesToResult.Log.LineNumber" ) + getLinesRead() );
    }

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (FilesToResultMeta) smi;
    data = (FilesToResultData) sdi;

    if ( super.init( smi, sdi ) ) {
      // Add init code here.
      return true;
    }
    return false;
  }

}
