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

package org.pentaho.di.trans.steps.getvariable;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Get information from the System or the supervising transformation.
 *
 * @author Matt
 * @since 4-aug-2003
 */
public class GetVariable extends BaseStep implements StepInterface {
  private GetVariableMeta meta;
  private GetVariableData data;

  public GetVariable( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    Object[] rowData;

    if ( data.readsRows ) {
      rowData = getRow();
      if ( rowData == null ) {
        setOutputDone();
        return false;
      }
    } else {
      rowData = RowDataUtil.allocateRowData( 0 );
      incrementLinesRead();
    }

    // initialize
    if ( first && rowData != null ) {
      first = false;

      // Make output meta data
      //
      if ( data.readsRows ) {
        data.inputRowMeta = getInputRowMeta();
      } else {
        data.inputRowMeta = new RowMeta();
      }
      data.outputRowMeta = data.inputRowMeta.clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      // Create a copy of the output row metadata to do the data conversion...
      //
      data.conversionMeta = data.outputRowMeta.cloneToType( ValueMetaInterface.TYPE_STRING );

      // Add the variables to the row...
      //
      // Keep the Object[] for speed. Although this step will always be used in "small" amounts, there's always going to
      // be those cases where performance is required.
      //
      int fieldsLength = meta.getFieldDefinitions().length;
      data.extraData = new Object[fieldsLength];
      for ( int i = 0; i < fieldsLength; i++ ) {
        String newValue = environmentSubstitute( meta.getFieldDefinitions()[i].getVariableString() );
        if ( log.isDetailed() ) {
          logDetailed( "field [" + meta.getFieldDefinitions()[i].getFieldName() + "] has value [" + newValue + "]" );
        }

        // Convert the data to the desired data type...
        //
        ValueMetaInterface targetMeta = data.outputRowMeta.getValueMeta( data.inputRowMeta.size() + i );
        ValueMetaInterface sourceMeta = data.conversionMeta.getValueMeta( data.inputRowMeta.size() + i ); // String type
                                                                                                          // +
                                                                                                          // conversion
                                                                                                          // masks,
                                                                                                          // symbols,
                                                                                                          // trim type,
                                                                                                          // etc
        data.extraData[i] = targetMeta.convertData( sourceMeta, newValue );
      }
    }

    rowData = RowDataUtil.addRowData( rowData, data.inputRowMeta.size(), data.extraData );

    putRow( data.outputRowMeta, rowData );

    if ( !data.readsRows ) { // Just one row and then stop!

      setOutputDone();
      return false;
    }

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (GetVariableMeta) smi;
    data = (GetVariableData) sdi;

    if ( super.init( smi, sdi ) ) {
      // Add init code here.
      data.readsRows = getStepMeta().getRemoteInputSteps().size() > 0;
      List<StepMeta> previous = getTransMeta().findPreviousSteps( getStepMeta() );
      if ( previous != null && previous.size() > 0 ) {
        data.readsRows = true;
      }

      return true;
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    super.dispose( smi, sdi );
  }

}
