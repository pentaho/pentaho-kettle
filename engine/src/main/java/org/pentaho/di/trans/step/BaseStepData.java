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

import org.pentaho.di.i18n.BaseMessages;

/**
 * This class is the base class for the StepDataInterface and contains the methods to set and retrieve the status of the
 * step data.
 *
 * @author Matt
 * @since 20-jan-2005
 */
public abstract class BaseStepData implements StepDataInterface {

  /** The pkg used for i18n */
  private static Class<?> PKG = BaseStep.class; // for i18n purposes, needed by Translator2!!

  /**
   * The Enum StepExecutionStatus.
   */
  public enum StepExecutionStatus {

    /** The status empty. */
    STATUS_EMPTY( BaseMessages.getString( PKG, "BaseStep.status.Empty" ) ),

      /** The status init. */
      STATUS_INIT( BaseMessages.getString( PKG, "BaseStep.status.Init" ) ),

      /** The status running. */
      STATUS_RUNNING( BaseMessages.getString( PKG, "BaseStep.status.Running" ) ),

      /** The status idle. */
      STATUS_IDLE( BaseMessages.getString( PKG, "BaseStep.status.Idle" ) ),

      /** The status finished. */
      STATUS_FINISHED( BaseMessages.getString( PKG, "BaseStep.status.Finished" ) ),

      /** The status stopped. */
      STATUS_STOPPED( BaseMessages.getString( PKG, "BaseStep.status.Stopped" ) ),

      /** The status disposed. */
      STATUS_DISPOSED( BaseMessages.getString( PKG, "BaseStep.status.Disposed" ) ),

      /** The status halted. */
      STATUS_HALTED( BaseMessages.getString( PKG, "BaseStep.status.Halted" ) ),

      /** The status paused. */
      STATUS_PAUSED( BaseMessages.getString( PKG, "BaseStep.status.Paused" ) ),

      /** The status halting. */
      STATUS_HALTING( BaseMessages.getString( PKG, "BaseStep.status.Halting" ) );

    /** The description. */
    private String description;

    /**
     * Instantiates a new step execution status.
     *
     * @param description
     *          the description
     */
    private StepExecutionStatus( String description ) {
      this.description = description;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
      return description;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
      return description;
    }
  }

  /** The status. */
  private StepExecutionStatus status;

  /**
   * Instantiates a new base step data.
   */
  public BaseStepData() {
    status = StepExecutionStatus.STATUS_EMPTY;
  }

  /**
   * Set the status of the step data.
   *
   * @param status
   *          the new status.
   */
  public void setStatus( StepExecutionStatus status ) {
    this.status = status;
  }

  /**
   * Get the status of this step data.
   *
   * @return the status of the step data
   */
  public StepExecutionStatus getStatus() {
    return status;
  }

  /**
   * Checks if is empty.
   *
   * @return true, if is empty
   */
  public boolean isEmpty() {
    return status == StepExecutionStatus.STATUS_EMPTY;
  }

  /**
   * Checks if is initialising.
   *
   * @return true, if is initialising
   */
  public boolean isInitialising() {
    return status == StepExecutionStatus.STATUS_INIT;
  }

  /**
   * Checks if is running.
   *
   * @return true, if is running
   */
  public boolean isRunning() {
    return status == StepExecutionStatus.STATUS_RUNNING;
  }

  /**
   * Checks if is idle.
   *
   * @return true, if is idle
   */
  public boolean isIdle() {
    return status == StepExecutionStatus.STATUS_IDLE;
  }

  /**
   * Checks if is finished.
   *
   * @return true, if is finished
   */
  public boolean isFinished() {
    return status == StepExecutionStatus.STATUS_FINISHED;
  }

  /**
   * Checks if is stopped.
   *
   * @return true, if is stopped
   */
  public boolean isStopped() {
    return status == StepExecutionStatus.STATUS_STOPPED;
  }

  /**
   * Checks if is disposed.
   *
   * @return true, if is disposed
   */
  public boolean isDisposed() {
    return status == StepExecutionStatus.STATUS_DISPOSED;
  }
}
