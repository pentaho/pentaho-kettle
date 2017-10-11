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
