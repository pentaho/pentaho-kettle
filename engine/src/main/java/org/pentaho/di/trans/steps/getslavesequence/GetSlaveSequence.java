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

package org.pentaho.di.trans.steps.getslavesequence;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Adds a sequential number to a stream of rows.
 *
 * @author Matt
 * @since 13-may-2003
 */
public class GetSlaveSequence extends BaseStep implements StepInterface {
  private static Class<?> PKG = GetSlaveSequence.class; // i18n

  private GetSlaveSequenceMeta meta;
  private GetSlaveSequenceData data;

  public GetSlaveSequence( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
    TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public Object[] addSequence( RowMetaInterface inputRowMeta, Object[] inputRowData ) throws KettleException {
    Object next = null;

    // Are we still in the sequence range?
    //
    if ( data.value >= ( data.startValue + data.increment ) ) {
      // Get a new value from the service...
      //
      data.startValue = data.slaveServer.getNextSlaveSequenceValue( data.sequenceName, data.increment );
      data.value = data.startValue;
    }

    next = Long.valueOf( data.value );
    data.value++;

    if ( next != null ) {
      Object[] outputRowData = inputRowData;
      if ( inputRowData.length < inputRowMeta.size() + 1 ) {
        outputRowData = RowDataUtil.resizeArray( inputRowData, inputRowMeta.size() + 1 );
      }
      outputRowData[inputRowMeta.size()] = next;
      return outputRowData;
    } else {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "GetSequence.Exception.CouldNotFindNextValueForSequence" )
        + meta.getValuename() );
    }
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (GetSlaveSequenceMeta) smi;
    data = (GetSlaveSequenceData) sdi;

    Object[] r = getRow(); // Get row from input rowset & set row busy!
    if ( r == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;

      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      data.startValue = data.slaveServer.getNextSlaveSequenceValue( data.sequenceName, data.increment );
      data.value = data.startValue;
    }

    if ( log.isRowLevel() ) {
      logRowlevel( BaseMessages.getString( PKG, "GetSequence.Log.ReadRow" )
        + getLinesRead() + " : " + getInputRowMeta().getString( r ) );
    }

    try {
      putRow( data.outputRowMeta, addSequence( getInputRowMeta(), r ) );

      if ( log.isRowLevel() ) {
        logRowlevel( BaseMessages.getString( PKG, "GetSequence.Log.WriteRow" )
          + getLinesWritten() + " : " + getInputRowMeta().getString( r ) );
      }
      if ( checkFeedback( getLinesRead() ) ) {
        if ( log.isBasic() ) {
          logBasic( BaseMessages.getString( PKG, "GetSequence.Log.LineNumber" ) + getLinesRead() );
        }
      }
    } catch ( KettleException e ) {
      logError( BaseMessages.getString( PKG, "GetSequence.Log.ErrorInStep" ) + e.getMessage() );
      setErrors( 1 );
      stopAll();
      setOutputDone(); // signal end to receiver(s)
      return false;
    }

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (GetSlaveSequenceMeta) smi;
    data = (GetSlaveSequenceData) sdi;

    if ( super.init( smi, sdi ) ) {
      data.increment = Const.toLong( environmentSubstitute( meta.getIncrement() ), 1000 );
      data.slaveServer = getTransMeta().findSlaveServer( environmentSubstitute( meta.getSlaveServerName() ) );
      data.sequenceName = environmentSubstitute( meta.getSequenceName() );
      data.value = -1;

      return true;
    }
    return false;
  }
}
