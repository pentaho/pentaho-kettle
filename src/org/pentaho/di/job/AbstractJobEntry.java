/*
 * ******************************************************************************
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************
 */

package org.pentaho.di.job;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

import java.util.List;

/**
 * User: RFellows
 * Date: 6/5/12
 */
public abstract class AbstractJobEntry<T extends BlockableJobConfig> extends JobEntryBase implements Cloneable, JobEntryInterface {

  protected T jobConfig = null;

  protected AbstractJobEntry() {
    jobConfig = createJobConfig();
  }

  public T getJobConfig() {
    jobConfig.setJobEntryName(getName());
    return jobConfig;
  }

  public void setJobConfig(T jobConfig) {
    this.jobConfig = jobConfig;
    setName(jobConfig.getJobEntryName());
  }

  /**
   * @return {@code true} if this job entry yields a success or failure result
   */
  @Override
  public boolean evaluates() {
    return true;
  }

  /**
   * @return {@code true} if this job entry supports and unconditional hop from it
   */
  @Override
  public boolean isUnconditional() {
    return true;
  }

  /**
   * @return an portion of XML describing the current state of this job entry
   */
  @Override
  public String getXML() {
    StringBuffer buffer = new StringBuffer(1024);
    buffer.append(super.getXML());
    JobEntrySerializationHelper.write(getJobConfig(), 1, buffer);
    return buffer.toString();
  }

  /**
   * Set the state of this job entry from an XML document node containing a previous state.
   *
   * @param node
   * @param databaseMetas
   * @param slaveServers
   * @param repository
   * @throws org.pentaho.di.core.exception.KettleXMLException
   */
  @Override
  public void loadXML(Node node, List<DatabaseMeta> databaseMetas, List<SlaveServer> slaveServers, Repository repository) throws KettleXMLException {
    super.loadXML(node, databaseMetas, slaveServers);
    T loaded = createJobConfig();
    JobEntrySerializationHelper.read(loaded, node);
    setJobConfig(loaded);
  }


  /**
   * Load the state of this job entry from a repository.
   *
   * @param rep
   * @param id_jobentry
   * @param databases
   * @param slaveServers
   * @throws org.pentaho.di.core.exception.KettleException
   */
  @Override
  public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
    super.loadRep(rep, id_jobentry, databases, slaveServers);
    T loaded = createJobConfig();
    JobEntrySerializationHelper.loadRep(loaded, rep, id_jobentry, databases, slaveServers);
    setJobConfig(loaded);
  }

  /**
   * Save the state of this job entry to a repository.
   *
   * @param rep
   * @param id_job
   * @throws KettleException
   */
  @Override
  public void saveRep(Repository rep, ObjectId id_job) throws KettleException {
    JobEntrySerializationHelper.saveRep(getJobConfig(), rep, id_job, getObjectId());
  }

  @Override
  public Result execute(Result result, int i) throws KettleException {
    if(!isValid(getJobConfig())) {
      setJobResultFailed(result);
      return result;
    }
    final Result jobResult = result;
    result.setResult(true);

    Thread t = new Thread(getExecutionRunnable(jobResult));

    t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread t, Throwable e) {
        handleUncaughtThreadException(t, e, jobResult);
      }
    });

    t.start();

    if (JobEntryUtils.asBoolean(getJobConfig().getBlockingExecution(), variables)) {
      while (!parentJob.isStopped() && t.isAlive()) {
        try {
          t.join(JobEntryUtils.asLong(getJobConfig().getBlockingPollingInterval(), variables));
        } catch (InterruptedException ex) {
          // ignore
          break;
        }
      }
      // If the parent job is stopped and the thread is still running make sure to interrupt it
      if (t.isAlive()) {
        t.interrupt();
        setJobResultFailed(result);
      }
      // Wait for thread to die so we get the proper return status set in jobResult before returning
      try {
        t.join(10 * 1000); // Don't wait for more than 10 seconds in case the thread is really blocked
      } catch (InterruptedException e) {
        // ignore
      }
    }

    return result;
  }

  /**
   * Flag a job result as failed
   *
   * @param jobResult
   */
  public void setJobResultFailed(Result jobResult) {
    jobResult.setNrErrors(1);
    jobResult.setResult(false);
  }

  /**
   * Creates a job configuration
   * @return
   */
  protected abstract T createJobConfig();

  /**
   * Ensures that the configuration is valid for execution
   * @param config
   * @return
   */
  protected abstract boolean isValid(T config);

  /**
   * Get the {@link Runnable} that does the execution of the job
   *
   * @param jobResult Job result for the execution to use
   * @return
   */
  protected abstract Runnable getExecutionRunnable(final Result jobResult);

  /**
   * Handle any clean up required when our execution thread encounters an unexpected {@link Exception}.
   *
   * @param t         Thread that encountered the uncaught exception
   * @param e         Exception that was encountered
   * @param jobResult Job result for the execution that spawned the thread
   */
  protected abstract void handleUncaughtThreadException(Thread t, Throwable e, Result jobResult);
}
