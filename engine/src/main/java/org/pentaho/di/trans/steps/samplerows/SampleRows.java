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

package org.pentaho.di.trans.steps.samplerows;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;

/**
 * Sample rows. Filter rows based on line number
 *
 * @author Samatar
 * @since 2-jun-2003
 */

public class SampleRows extends BaseStep implements StepInterface {
  private static Class<?> PKG = SampleRowsMeta.class; // for i18n purposes, needed by Translator2!!

  private SampleRowsMeta meta;
  private SampleRowsData data;

  public SampleRows( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                     Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (SampleRowsMeta) smi;
    data = (SampleRowsData) sdi;

    Object[] r = getRow(); // get row, set busy!
    if ( r == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }
    if ( first ) {
      first = false;

      String realRange = environmentSubstitute( meta.getLinesRange() );
      data.addlineField = ( !Utils.isEmpty( environmentSubstitute( meta.getLineNumberField() ) ) );

      // get the RowMeta
      data.previousRowMeta = getInputRowMeta().clone();
      data.NrPrevFields = data.previousRowMeta.size();
      data.outputRowMeta = data.previousRowMeta;
      if ( data.addlineField ) {
        meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );
      }

      String[] rangePart = realRange.split( "," );
      ImmutableRangeSet.Builder<Integer> setBuilder = ImmutableRangeSet.builder();

      for ( String part : rangePart ) {
        if ( part.matches( "\\d+" ) ) {
          if ( log.isDebug() ) {
            logDebug( BaseMessages.getString( PKG, "SampleRows.Log.RangeValue", part ) );
          }
          int vpart = Integer.valueOf( part );
          setBuilder.add( Range.singleton( vpart ) );

        } else if ( part.matches( "\\d+\\.\\.\\d+" ) ) {
          String[] rangeMultiPart = part.split( "\\.\\." );
          Integer start = Integer.valueOf( rangeMultiPart[0] );
          Integer end = Integer.valueOf( rangeMultiPart[1] );
          Range<Integer> range = Range.closed( start, end );
          if ( log.isDebug() ) {
            logDebug( BaseMessages.getString( PKG, "SampleRows.Log.RangeValue", range ) );
          }
          setBuilder.add( range );
        }
      }
      data.rangeSet = setBuilder.build();
    } // end if first

    if ( data.addlineField ) {
      data.outputRow = RowDataUtil.allocateRowData( data.outputRowMeta.size() );
      for ( int i = 0; i < data.NrPrevFields; i++ ) {
        data.outputRow[i] = r[i];
      }
    } else {
      data.outputRow = r;
    }

    int linesRead = (int) getLinesRead();
    if ( data.rangeSet.contains( linesRead ) ) {
      if ( data.addlineField ) {
        data.outputRow[data.NrPrevFields] = getLinesRead();
      }

      // copy row to possible alternate rowset(s).
      //
      putRow( data.outputRowMeta, data.outputRow );

      if ( log.isRowLevel() ) {
        logRowlevel( BaseMessages.getString( PKG, "SampleRows.Log.LineNumber", linesRead
          + " : " + getInputRowMeta().getString( r ) ) );
      }
    }

    // Check if maximum value has been exceeded
    if ( data.rangeSet.isEmpty() || linesRead >= data.rangeSet.span().upperEndpoint() ) {
      setOutputDone();
    }

    // Allowed to continue to read in data
    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (SampleRowsMeta) smi;
    data = (SampleRowsData) sdi;

    if ( super.init( smi, sdi ) ) {
      // Add init code here.
      return true;
    }
    return false;
  }

}
