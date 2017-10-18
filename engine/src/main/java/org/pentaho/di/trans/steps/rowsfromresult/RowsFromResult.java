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

package org.pentaho.di.trans.steps.rowsfromresult;

import org.pentaho.di.core.Result;
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
 * Reads results from a previous transformation in a Job
 *
 * @author Matt
 * @since 2-jun-2003
 */
public class RowsFromResult extends BaseStep implements StepInterface {
  private static Class<?> PKG = RowsFromResult.class; // for i18n purposes, needed by Translator2!!

  private RowsFromResultData data;

  public RowsFromResult( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );

    data = (RowsFromResultData) stepDataInterface;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    Result previousResult = getTrans().getPreviousResult();
    if ( previousResult == null || getLinesRead() >= previousResult.getRows().size() ) {
      setOutputDone();
      return false;
    }
    RowMetaAndData row = previousResult.getRows().get( (int) getLinesRead() );
    incrementLinesRead();

    data = (RowsFromResultData) sdi;

    // We don't get the meta-data from the previous steps (there aren't any) but from the previous transformation or job
    //
    data.outputRowMeta = row.getRowMeta();

    // copy row to possible alternate rowset(s).
    //
    putRow( data.outputRowMeta, row.getData() );

    if ( checkFeedback( getLinesRead() ) ) {
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "RowsFromResult.Log.LineNumber" ) + getLinesRead() );
      }
    }

    return true;
  }
}
