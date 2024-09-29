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


package org.pentaho.di.trans.steps.abort;

import org.pentaho.di.core.Const;
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
 * Step that will abort after having seen 'x' number of rows on its input.
 *
 * @author Sven Boden
 */
public class Abort extends BaseStep implements StepInterface {

  private static Class<?> PKG = Abort.class; // for i18n purposes, needed by Translator2!!

  private AbortMeta meta;
  private int nrInputRows;
  private int nrThresholdRows;

  public Abort( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (AbortMeta) smi;

    if ( super.init( smi, sdi ) ) {
      // Add init code here.
      nrInputRows = 0;
      String threshold = environmentSubstitute( meta.getRowThreshold() );
      nrThresholdRows = Const.toInt( threshold, -1 );
      if ( nrThresholdRows < 0 ) {
        logError( BaseMessages.getString( PKG, "Abort.Log.ThresholdInvalid", threshold ) );
      }

      return true;
    }
    return false;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (AbortMeta) smi;

    Object[] r = getRow(); // Get row from input rowset & set row busy!
    // no more input to be expected...
    if ( r == null ) {
      setOutputDone();
      return false;
    } else {
      putRow( getInputRowMeta(), r );
      nrInputRows++;
      if ( nrInputRows > nrThresholdRows ) {
        //
        // Here we abort!!
        //
        String abortOptionMessageProperty = "AbortDialog.Options.Abort.Label";
        if ( meta.isAbortWithError() ) {
          abortOptionMessageProperty = "AbortDialog.Options.AbortWithError.Label";
        } else if ( meta.isSafeStop() ) {
          abortOptionMessageProperty = "AbortDialog.Options.SafeStop.Label";
        }
        logError( BaseMessages.getString(
          PKG, "Abort.Log.Wrote.AbortRow", Long.toString( nrInputRows ),
          BaseMessages.getString( PKG, abortOptionMessageProperty ), getInputRowMeta().getString( r ) ) );

        String message = environmentSubstitute( meta.getMessage() );
        if ( message == null || message.length() == 0 ) {
          logError( BaseMessages.getString( PKG, "Abort.Log.DefaultAbortMessage", "" + nrInputRows ) );
        } else {
          logError( message );
        }
        if ( meta.isSafeStop() ) {

          getTrans().safeStop();
        } else {
          if ( meta.isAbortWithError() ) {
            setErrors( 1 );
          }

          stopAll();
        }
      } else {
        // seen a row but not yet reached the threshold
        if ( meta.isAlwaysLogRows() ) {
          logMinimal( BaseMessages.getString(
            PKG, "Abort.Log.Wrote.Row", Long.toString( nrInputRows ), getInputRowMeta().getString( r ) ) );
        } else {
          if ( log.isRowLevel() ) {
            logRowlevel( BaseMessages.getString(
              PKG, "Abort.Log.Wrote.Row", Long.toString( nrInputRows ), getInputRowMeta().getString( r ) ) );
          }
        }
      }
    }

    return true;
  }
}
