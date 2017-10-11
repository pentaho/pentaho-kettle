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

package org.pentaho.di.core.listeners;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;

public interface SubComponentExecutionListener {

  /**
   * This method is called right before a sub-transformation, mapping, single threader template, ... is to be executed
   * in a parent job or transformation.
   *
   * @param trans
   *          The transformation that is about to be executed.
   * @throws KettleException
   *           In case something goes wrong
   */
  public void beforeTransformationExecution( Trans trans ) throws KettleException;

  /**
   * This method is called right after a sub-transformation, mapping, single threader template, ... was executed in a
   * parent job or transformation.
   *
   * @param trans
   *          The transformation that was just executed.
   * @throws KettleException
   *           In case something goes wrong
   */
  public void afterTransformationExecution( Trans trans ) throws KettleException;

  /**
   * This method is called right before a job is to be executed in a parent job or transformation (Job job-entry, Job
   * Executor step).
   *
   * @param trans
   *          The job that is about to be executed.
   * @throws KettleException
   *           In case something goes wrong
   */
  public void beforeJobExecution( Job job ) throws KettleException;

  /**
   * This method is called right after a job was executed in a parent job or transformation (Job job-entry, Job Executor
   * step).
   *
   * @param trans
   *          The job that was executed.
   * @throws KettleException
   *           In case something goes wrong
   */
  public void afterJobExecution( Job job ) throws KettleException;
}
