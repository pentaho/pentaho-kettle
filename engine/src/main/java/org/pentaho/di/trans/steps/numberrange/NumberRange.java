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

package org.pentaho.di.trans.steps.numberrange;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Business logic for the NumberRange
 *
 * @author ronny.roeller@fredhopper.com
 *
 */
public class NumberRange extends BaseStep implements StepInterface {
  private static Class<?> PKG = NumberRangeMeta.class; // for i18n purposes, needed by Translator2!!

  private NumberRangeData data;
  private NumberRangeMeta meta;

  private NumberRangeSet numberRange;

  /**
   * Column number where the input value is stored
   */

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    Object[] row = getRow();
    if ( row == null ) {
      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;

      numberRange = new NumberRangeSet( meta.getRules(), meta.getFallBackValue() );
      data.outputRowMeta = getInputRowMeta().clone();
      // Prepare output fields
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      // Find column numbers
      data.inputColumnNr = data.outputRowMeta.indexOfValue( meta.getInputField() );

      // Check if a field was not available
      if ( data.inputColumnNr < 0 ) {
        logError( "Field for input could not be found: " + meta.getInputField() );
        return false;
      }
    }
    try {
      // get field value
      Double value = getInputRowMeta().getNumber( row, data.inputColumnNr );

      // return range
      String ranges = numberRange.evaluate( value );
      // add value to output
      row = RowDataUtil.addRowData( row, getInputRowMeta().size(), new Object[] { ranges } );
      putRow( data.outputRowMeta, row );
      if ( checkFeedback( getLinesRead() ) ) {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "NumberRange.Log.LineNumber" ) + getLinesRead() );
        }
      }
    } catch ( KettleException e ) {
      boolean sendToErrorRow = false;
      String errorMessage = null;

      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        logError( BaseMessages.getString( PKG, "NumberRange.Log.ErrorInStepRunning" ) + e.getMessage() );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), row, 1, errorMessage, null, "NumberRange001" );
      }
    }

    return true;
  }

  public NumberRange( StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis ) {
    super( s, stepDataInterface, c, t, dis );
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (NumberRangeMeta) smi;
    data = (NumberRangeData) sdi;

    return super.init( smi, sdi );
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (NumberRangeMeta) smi;
    data = (NumberRangeData) sdi;

    super.dispose( smi, sdi );
  }

}
