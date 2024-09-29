/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.trans.steps.getpreviousrowfield;

import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
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
 * return field value from previous row.
 *
 * @author Samatar Hassan
 * @since 07 September 2008
 */
public class GetPreviousRowField extends BaseStep implements StepInterface {

  private static Class<?> PKG = GetPreviousRowFieldMeta.class; // for i18n purposes, needed by Translator2!!

  private GetPreviousRowFieldMeta meta;

  private GetPreviousRowFieldData data;

  public GetPreviousRowField( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
    TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  private synchronized Object[] getOutputRowData( Object[] row ) throws KettleException {

    Object[] outputRowData = RowDataUtil.allocateRowData( data.outputRowMeta.size() );
    for ( int i = 0; i < data.NrPrevFields; i++ ) {
      outputRowData[i] = row[i];
    }
    if ( first ) {
      first = false;
    } else {
      for ( int i = 0; i < data.inStreamNrs.length; i++ ) {
        outputRowData[data.NrPrevFields + i] = data.previousRow[data.inStreamNrs[i]];
      }
    }
    data.previousRow = row;
    return outputRowData;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (GetPreviousRowFieldMeta) smi;
    data = (GetPreviousRowFieldData) sdi;

    Object[] r = getRow(); // Get row from input rowset & set row busy!
    if ( r == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }

    if ( first ) {
      data.inputRowMeta = getInputRowMeta();
      data.NrPrevFields = data.inputRowMeta.size();
      data.outputRowMeta = data.inputRowMeta.clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      data.inStreamNrs = new int[meta.getFieldInStream().length];
      for ( int i = 0; i < meta.getFieldInStream().length; i++ ) {
        data.inStreamNrs[i] = data.inputRowMeta.indexOfValue( meta.getFieldInStream()[i] );
        if ( data.inStreamNrs[i] < 0 ) { // couldn't find field!

          throw new KettleException( BaseMessages.getString(
            PKG, "GetPreviousRowField.Exception.FieldRequired", meta.getFieldInStream()[i] ) );
        }
      }

      data.outStreamNrs = new String[meta.getFieldInStream().length];
      for ( int i = 0; i < meta.getFieldInStream().length; i++ ) {
        data.outStreamNrs[i] = meta.getFieldOutStream()[i];
        if ( Utils.isEmpty( data.outStreamNrs[i] ) ) {
          throw new KettleStepException( BaseMessages.getString(
            PKG, "GetPreviousRowField.Exception.OutputFieldEmpty", "" + i ) );
        }
      }
    } // end if first

    try {
      Object[] outputRow = getOutputRowData( r );

      putRow( data.outputRowMeta, outputRow ); // copy row to output rowset(s);

      if ( checkFeedback( getLinesRead() ) ) {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "GetPreviousRowField.Log.LineNumber" ) + getLinesRead() );
        }
      }
    } catch ( KettleException e ) {
      boolean sendToErrorRow = false;
      String errorMessage = null;
      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        logError( BaseMessages.getString( PKG, "GetPreviousRowField.Log.ErrorInStep", e.getMessage() ) );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), r, 1, errorMessage, null, "GetPreviousRowField001" );
      }
    }
    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    boolean rCode = true;

    meta = (GetPreviousRowFieldMeta) smi;
    data = (GetPreviousRowFieldData) sdi;

    if ( super.init( smi, sdi ) ) {

      return rCode;
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (GetPreviousRowFieldMeta) smi;
    data = (GetPreviousRowFieldData) sdi;

    super.dispose( smi, sdi );
  }
}
