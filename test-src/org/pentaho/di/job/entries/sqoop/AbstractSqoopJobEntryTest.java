/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.job.entries.sqoop;

import org.apache.hadoop.conf.Configuration;
import org.junit.Test;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.Job;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AbstractSqoopJobEntryTest {

  private static class TestSqoopJobEntry extends AbstractSqoopJobEntry<SqoopConfig> {

    private long waitTime = 0L;

    /**
     * Create a {@link SqoopImportJobEntry} that will simply wait for {@code waitTime} instead of executing Sqoop.
     *
     * @param waitTime Time in milliseconds to block during {@link AbstractSqoopJobEntry#executeSqoop(SqoopConfig, org.apache.hadoop.conf.Configuration, org.pentaho.di.core.Result)}
     */
    private TestSqoopJobEntry(long waitTime) {
      this.waitTime = waitTime;
    }

    @Override
    protected void executeSqoop(SqoopConfig config, Configuration hadoopConfig, Result jobResult) {
      // Don't actually execute sqoop, just block for the requested time
      try {
        Thread.sleep(waitTime);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    protected SqoopConfig buildSqoopConfig() {
      return new SqoopConfig() {
      };
    }

    @Override
    protected String getToolName() {
      return null;
    }
  }

  @Test
  public void execute_blocking() throws KettleException {
    final long waitTime = 1000;
    AbstractSqoopJobEntry je = new TestSqoopJobEntry(waitTime);

    je.setParentJob(new Job("test", null, null));
    Result result = new Result();
    long start = System.currentTimeMillis();
    je.execute(result, 0);
    long end = System.currentTimeMillis();
    assertTrue("Total runtime should be >= the wait time if we are blocking", (end - start) >= waitTime);

    assertEquals(0, result.getNrErrors());
    assertTrue(result.getResult());
  }

  @Test
  public void execute_nonblocking() throws KettleException {
    final long waitTime = 1000;
    AbstractSqoopJobEntry je = new TestSqoopJobEntry(waitTime);

    je.setParentJob(new Job("test", null, null));
    je.getSqoopConfig().setBlockingExecution(false);
    Result result = new Result();
    long start = System.currentTimeMillis();
    je.execute(result, 0);
    long end = System.currentTimeMillis();
    assertTrue("Total runtime should be less than the wait time if we're not blocking", (end - start) < waitTime);

    assertEquals(0, result.getNrErrors());
    assertTrue(result.getResult());
  }

  @Test
  public void execute_interrupted() throws KettleException {
    final long waitTime = 1000 * 10;
    final List<String> loggedErrors = new ArrayList<String>();
    AbstractSqoopJobEntry je = new TestSqoopJobEntry(waitTime) {
      @Override
      public void logError(String message, Throwable e) {
        loggedErrors.add(message);
      }
    };

    final Job parentJob = new Job("test", null, null);

    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        parentJob.stopAll();
      }
    };

    je.setParentJob(parentJob);
    Result result = new Result();

    // Start another thread to stop the parent job and unblock the Sqoop job entry in 1 second
    t.start();

    long start = System.currentTimeMillis();
    je.execute(result, 0);
    long end = System.currentTimeMillis();
    assertTrue("Total runtime should be less than the wait time if we were properly interrupted", (end - start) < waitTime);

    assertEquals(1, result.getNrErrors());
    assertFalse(result.getResult());

    // Make sure when an uncaught exception occurs an error log message is generated
    assertEquals(1, loggedErrors.size());
  }
}
