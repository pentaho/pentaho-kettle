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


package org.pentaho.di.trans.step;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.Metrics;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;

public class StepInitThread implements Runnable {
  private static Class<?> PKG = Trans.class; // for i18n purposes, needed by Translator2!!

  public boolean ok;
  public boolean finished;
  public boolean doIt;

  private StepMetaDataCombi combi;

  private LogChannelInterface log;

  public StepInitThread( StepMetaDataCombi combi, LogChannelInterface log ) {
    this.combi = combi;
    this.log = combi.step.getLogChannel();
    this.ok = false;
    this.finished = false;
    this.doIt = true;
  }

  public String toString() {
    return combi.stepname;
  }

  public void run() {
    // Set the internal variables also on the initialization thread!
    // ((BaseStep)combi.step).setInternalVariables();

    if ( !doIt ) {
      // An extension point plugin decided we should not initialize the step.
      // Logging, error handling, finished flag... should all be handled in the extension point.
      //
      return;
    }

    try {
      combi.step.getLogChannel().snap( Metrics.METRIC_STEP_INIT_START );

      if ( combi.step.init( combi.meta, combi.data ) ) {
        combi.data.setStatus( StepExecutionStatus.STATUS_IDLE );
        ok = true;
      } else {
        combi.step.setErrors( 1 );
        log.logError( BaseMessages.getString( PKG, "Trans.Log.ErrorInitializingStep", combi.step.getStepname() ) );
      }
    } catch ( Throwable e ) {
      log.logError( BaseMessages.getString( PKG, "Trans.Log.ErrorInitializingStep", combi.step.getStepname() ) );
      log.logError( Const.getStackTracker( e ) );
    } finally {
      combi.step.getLogChannel().snap( Metrics.METRIC_STEP_INIT_STOP );
    }

    finished = true;
  }

  public boolean isFinished() {
    return finished;
  }

  public boolean isOk() {
    return ok;
  }

  /**
   * @return Returns the combi.
   */
  public StepMetaDataCombi getCombi() {
    return combi;
  }

  /**
   * @param combi
   *          The combi to set.
   */
  public void setCombi( StepMetaDataCombi combi ) {
    this.combi = combi;
  }

  /**
   * @return the doIt
   */
  public boolean isDoIt() {
    return doIt;
  }

  /**
   * @param doIt the doIt to set
   */
  public void setDoIt( boolean doIt ) {
    this.doIt = doIt;
  }
}
