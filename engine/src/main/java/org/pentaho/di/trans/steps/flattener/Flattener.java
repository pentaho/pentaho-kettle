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

package org.pentaho.di.trans.steps.flattener;

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
 * Pivots data based on key-value pairs
 *
 * @author Matt
 * @since 17-jan-2006
 */
public class Flattener extends BaseStep implements StepInterface {
  private static Class<?> PKG = FlattenerMeta.class; // for i18n purposes, needed by Translator2!!

  private FlattenerMeta meta;
  private FlattenerData data;

  public Flattener( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );

    meta = (FlattenerMeta) getStepMeta().getStepMetaInterface();
    data = (FlattenerData) stepDataInterface;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    Object[] r = getRow(); // get row!
    if ( r == null ) { // no more input to be expected...

      // Don't forget the last set of rows...
      if ( data.processed > 0 ) {
        Object[] outputRowData = createOutputRow( data.previousRow );

        // send out inputrow + the flattened part
        //
        putRow( data.outputRowMeta, outputRowData );
      }

      setOutputDone();
      return false;
    }

    if ( first ) {
      data.inputRowMeta = getInputRowMeta();
      data.outputRowMeta = data.inputRowMeta.clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      data.fieldNr = data.inputRowMeta.indexOfValue( meta.getFieldName() );
      if ( data.fieldNr < 0 ) {
        logError( BaseMessages.getString( PKG, "Flattener.Log.FieldCouldNotFound", meta.getFieldName() ) );
        setErrors( 1 );
        stopAll();
        return false;
      }

      // Allocate the result row...
      //
      data.targetResult = new Object[meta.getTargetField().length];

      first = false;
    }

    // set it to value # data.processed
    //
    data.targetResult[data.processed++] = r[data.fieldNr];

    if ( data.processed >= meta.getTargetField().length ) {
      Object[] outputRowData = createOutputRow( r );

      // send out input row + the flattened part
      putRow( data.outputRowMeta, outputRowData );

      // clear the result row
      data.targetResult = new Object[meta.getTargetField().length];

      data.processed = 0;
    }

    // Keep track in case we want to send out the last couple of flattened values.
    data.previousRow = r;

    if ( checkFeedback( getLinesRead() ) ) {
      logBasic( BaseMessages.getString( PKG, "Flattener.Log.LineNumber" ) + getLinesRead() );
    }

    return true;
  }

  private Object[] createOutputRow( Object[] rowData ) {

    Object[] outputRowData = RowDataUtil.allocateRowData( data.outputRowMeta.size() );
    int outputIndex = 0;

    // copy the values from previous, but don't take along index 'data.fieldNr'...
    //
    for ( int i = 0; i < data.inputRowMeta.size(); i++ ) {
      if ( i != data.fieldNr ) {
        outputRowData[outputIndex++] = rowData[i];
      }
    }

    // Now add the fields we flattened...
    //
    for ( int i = 0; i < data.targetResult.length; i++ ) {
      outputRowData[outputIndex++] = data.targetResult[i];
    }

    return outputRowData;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (FlattenerMeta) smi;
    data = (FlattenerData) sdi;

    if ( super.init( smi, sdi ) ) {
      return true;
    }
    return false;
  }

}
