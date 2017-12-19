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

package org.pentaho.di.trans.steps.datagrid;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Generates a number of (empty or the same) rows
 *
 * @author Matt
 * @since 4-apr-2003
 */
public class DataGrid extends BaseStep implements StepInterface {
  private static Class<?> PKG = DataGridMeta.class; // for i18n purposes, needed by Translator2!!

  private DataGridMeta meta;
  private DataGridData data;

  public DataGrid( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );

    meta = (DataGridMeta) getStepMeta().getStepMetaInterface();
    data = (DataGridData) stepDataInterface;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    if ( data.linesWritten >= meta.getDataLines().size() ) {
      // no more rows to be written
      setOutputDone();
      return false;
    }

    if ( first ) {
      // The output meta is the original input meta + the
      // additional constant fields.

      first = false;
      data.linesWritten = 0;

      data.outputRowMeta = new RowMeta();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      // Use these metadata values to convert data...
      //
      data.convertMeta = data.outputRowMeta.cloneToType( ValueMetaInterface.TYPE_STRING );
    }

    Object[] outputRowData = RowDataUtil.allocateRowData( data.outputRowMeta.size() );
    List<String> outputLine = meta.getDataLines().get( data.linesWritten );

    for ( int i = 0; i < data.outputRowMeta.size(); i++ ) {
      if ( meta.isSetEmptyString()[ i ] ) {
        // Set empty string
        outputRowData[ i ] = StringUtil.EMPTY_STRING;
      } else {

        ValueMetaInterface valueMeta = data.outputRowMeta.getValueMeta( i );
        ValueMetaInterface convertMeta = data.convertMeta.getValueMeta( i );
        String valueData = outputLine.get( i );

        if ( valueData != null && valueMeta.isNull( valueData ) ) {
          valueData = null;
        }
        outputRowData[ i ] = valueMeta.convertDataFromString( valueData, convertMeta, null, null, 0 );
      }
    }

    putRow( data.outputRowMeta, outputRowData );
    data.linesWritten++;

    if ( log.isRowLevel() ) {
      log.logRowlevel( toString(), BaseMessages.getString( PKG, "DataGrid.Log.Wrote.Row", Long
        .toString( getLinesWritten() ), data.outputRowMeta.getString( outputRowData ) ) );
    }

    if ( checkFeedback( getLinesWritten() ) ) {
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "DataGrid.Log.LineNr", Long.toString( getLinesWritten() ) ) );
      }
    }

    return true;
  }

  public boolean isWaitingForData() {
    return true;
  }

}
