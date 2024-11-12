/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
