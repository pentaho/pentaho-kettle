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

package org.pentaho.di.trans.steps.detectlastrow;

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

/**
 * Detect last row in a stream
 *
 * @author Samatar
 * @since 03June2008
 */
public class DetectLastRow extends BaseStep implements StepInterface {
  private static Class<?> PKG = DetectLastRowMeta.class; // for i18n purposes, needed by Translator2!!

  private DetectLastRowMeta meta;

  private DetectLastRowData data;

  private Object[] previousRow;

  public DetectLastRow( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (DetectLastRowMeta) smi;
    data = (DetectLastRowData) sdi;

    Object[] r = getRow(); // Get row from input rowset & set row busy!

    if ( first ) {
      if ( getInputRowMeta() == null ) {
        setOutputDone();
        return false;
      }

      // get the RowMeta
      data.previousRowMeta = getInputRowMeta().clone();
      data.NrPrevFields = data.previousRowMeta.size();
      data.outputRowMeta = data.previousRowMeta;
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );
    }
    Object[] outputRow = null;

    if ( r == null ) { // no more input to be expected...

      if ( previousRow != null ) {
        //
        // Output the last row with last row indicator set to true.
        //
        if ( !Utils.isEmpty( meta.getResultFieldName() ) ) {
          outputRow = RowDataUtil.addRowData( previousRow, getInputRowMeta().size(), data.getTrueArray() );
        } else {
          outputRow = previousRow;
        }

        putRow( data.outputRowMeta, outputRow ); // copy row to output rowset(s);

        if ( log.isRowLevel() ) {
          logRowlevel( BaseMessages.getString( PKG, "DetectLastRow.Log.WroteRowToNextStep" )
            + data.outputRowMeta.getString( outputRow ) );
        }

        if ( checkFeedback( getLinesRead() ) ) {
          logBasic( BaseMessages.getString( PKG, "DetectLastRow.Log.LineNumber" ) + getLinesRead() );
        }
      }

      setOutputDone();
      return false;
    }

    if ( !first ) {
      outputRow = RowDataUtil.addRowData( previousRow, getInputRowMeta().size(), data.getFalseArray() );
      putRow( data.outputRowMeta, outputRow ); // copy row to output rowset(s);

      if ( log.isRowLevel() ) {
        logRowlevel( BaseMessages.getString( PKG, "DetectLastRow.Log.WroteRowToNextStep" )
          + data.outputRowMeta.getString( outputRow ) );
      }

      if ( checkFeedback( getLinesRead() ) ) {
        logBasic( BaseMessages.getString( PKG, "DetectLastRow.Log.LineNumber" ) + getLinesRead() );
      }
    }
    // keep track of the current row
    previousRow = r;
    if ( first ) {
      first = false;
    }

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (DetectLastRowMeta) smi;
    data = (DetectLastRowData) sdi;

    if ( super.init( smi, sdi ) ) {
      if ( Utils.isEmpty( meta.getResultFieldName() ) ) {
        logError( BaseMessages.getString( PKG, "DetectLastRow.Error.ResultFieldMissing" ) );
        return false;
      }

      return true;
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (DetectLastRowMeta) smi;
    data = (DetectLastRowData) sdi;

    super.dispose( smi, sdi );
  }

}
