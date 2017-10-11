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

import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;

/**
 * This defines the basic interface for the data used by a thread. This will allow us to stop execution of threads and
 * restart them later on without loosing track of the situation. Typically the StepDataInterface implementing class will
 * contain result sets, temporary data, caching indexes, etc.
 *
 * @author Matt
 * @since 20-jan-2005
 */
public interface StepDataInterface {

  /**
   * Sets the status.
   *
   * @param status
   *          the new status
   */
  public void setStatus( StepExecutionStatus status );

  /**
   * Gets the status.
   *
   * @return the status
   */
  public StepExecutionStatus getStatus();

  /**
   * Checks if is empty.
   *
   * @return true, if is empty
   */
  public boolean isEmpty();

  /**
   * Checks if is initialising.
   *
   * @return true, if is initialising
   */
  public boolean isInitialising();

  /**
   * Checks if is running.
   *
   * @return true, if is running
   */
  public boolean isRunning();

  /**
   * Checks if is idle.
   *
   * @return true, if is idle
   */
  public boolean isIdle();

  /**
   * Checks if is finished.
   *
   * @return true, if is finished
   */
  public boolean isFinished();

  /**
   * Checks if is disposed.
   *
   * @return true, if is disposed
   */
  public boolean isDisposed();
}
