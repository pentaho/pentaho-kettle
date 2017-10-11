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

package org.pentaho.di.trans.steps.rowstoresult;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Writes results to a next transformation in a Job
 *
 * @author Matt
 * @since 2-jun-2003
 */
public class RowsToResult extends BaseStep implements StepInterface {
  private static Class<?> PKG = RowsToResult.class; // for i18n purposes, needed by Translator2!!

  private RowsToResultMeta meta;

  private RowsToResultData data;

  public RowsToResult( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (RowsToResultMeta) smi;
    data = (RowsToResultData) sdi;

    Object[] r = getRow(); // get row, set busy!
    if ( r == null ) { // no more input to be expected...

      getTrans().getResultRows().addAll( data.rows );

      setOutputDone();
      return false;
    }

    // Add all rows to rows buffer...
    data.rows.add( new RowMetaAndData( getInputRowMeta(), r ) );
    data.outputRowMeta = getInputRowMeta().clone();
    meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );
    putRow( data.outputRowMeta, r ); // copy row to possible alternate
    // rowset(s).

    if ( checkFeedback( getLinesRead() ) ) {
      logBasic( BaseMessages.getString( PKG, "RowsToResult.Log.LineNumber" ) + getLinesRead() );
    }

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (RowsToResultMeta) smi;
    data = (RowsToResultData) sdi;

    if ( super.init( smi, sdi ) ) {
      // Add init code here.
      return true;
    }
    return false;
  }

}
