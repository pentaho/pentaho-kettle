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

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryCreationHelper;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryMeta;
import org.w3c.dom.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * User: RFellows
 * Date: 6/5/12
 */
public class AbstractJobEntryTest {

  class TestJobEntry extends AbstractJobEntry<BlockableJobConfig> {
    private long waitTime = 0L;

    TestJobEntry() { }

    TestJobEntry(long waitTime) {
      this.waitTime = waitTime;
    }

    @Override
    protected BlockableJobConfig createJobConfig() {
      return new BlockableJobConfig();
    }

    @Override
    protected boolean isValid(BlockableJobConfig config) {
      return true;
    }

    @Override
    protected Runnable getExecutionRunnable(Result jobResult) {
      return new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(waitTime);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }
      };
    }

    @Override
    protected void handleUncaughtThreadException(Thread t, Throwable e, Result jobResult) {
      logError("Error executing Job", e);
      setJobResultFailed(jobResult);
    }

  };

  @Test
  public void testLoadXml() throws Exception {

    TestJobEntry jobEntry = new TestJobEntry();
    BlockableJobConfig jobConfig = new BlockableJobConfig();

    jobConfig.setJobEntryName("Job Name");

    jobEntry.setJobConfig(jobConfig);

    JobEntryCopy jec = new JobEntryCopy(jobEntry);
    jec.setLocation(0, 0);
    String xml = jec.getXML();

    Document d = XMLHandler.loadXMLString(xml);

    TestJobEntry jobEntry2 = new TestJobEntry();
    jobEntry2.loadXML(d.getDocumentElement(), null, null, null);

    BlockableJobConfig jobConfig2 = jobEntry2.getJobConfig();
    assertEquals(jobConfig.getJobEntryName(), jobConfig2.getJobEntryName());
  }

  @Test
  public void testLoadRep() throws Exception {
    TestJobEntry je = new TestJobEntry();
    BlockableJobConfig config = new BlockableJobConfig();

    config.setJobEntryName("testing");

    je.setJobConfig(config);

    KettleEnvironment.init();
    String filename = File.createTempFile(getClass().getSimpleName() + "-export-dbtest", "").getAbsolutePath();

    try {
      DatabaseMeta databaseMeta = new DatabaseMeta("H2Repo", "H2", "JDBC", null, filename, null, null, null);
      RepositoryMeta repositoryMeta = new KettleDatabaseRepositoryMeta("KettleDatabaseRepository", "H2Repo", "H2 Repository", databaseMeta);
      KettleDatabaseRepository repository = new KettleDatabaseRepository();
      repository.init(repositoryMeta);
      repository.connectionDelegate.connect(true, true);
      KettleDatabaseRepositoryCreationHelper helper = new KettleDatabaseRepositoryCreationHelper(repository);
      helper.createRepositorySchema(null, false, new ArrayList<String>(), false);
      repository.disconnect();

      // Test connecting...
      //
      repository.connect("admin", "admin");
      assertTrue(repository.isConnected());

      // A job entry must have an ID if we're going to save it to a repository
      je.setObjectId(new LongObjectId(1));
      ObjectId id_job = new LongObjectId(1);

      // Save the original job entry into the repository
      je.saveRep(repository, id_job);

      // Load it back into a new job entry
      TestJobEntry je2 = new TestJobEntry();
      je2.loadRep(repository, id_job, null, null);

      // Make sure all settings we set are properly loaded
      BlockableJobConfig config2 = je2.getJobConfig();
      Assert.assertEquals(config.getJobEntryName(), config2.getJobEntryName());
    } finally {
      // Delete test database
      new File(filename+".h2.db").delete();
      new File(filename+".trace.db").delete();
    }
  }

  @Test
  public void testEvaluates() throws Exception {
    TestJobEntry jobEntry = new TestJobEntry();
    assertTrue(jobEntry.evaluates());
  }

  @Test
  public void testIsUnconditional() throws Exception {
    TestJobEntry jobEntry = new TestJobEntry();
    assertTrue(jobEntry.isUnconditional());
  }


  @Test
  public void execute_blocking() throws KettleException {
    final long waitTime = 1000;
    TestJobEntry je = new TestJobEntry(waitTime);

    je.setParentJob(new Job("test", null, null));
    Result result = new Result();
    long start = System.currentTimeMillis();
    je.execute(result, 0);
    long end = System.currentTimeMillis();
    assertTrue("Total runtime should be >= the wait time if we are blocking", (end - start) >= waitTime);

    Assert.assertEquals(0, result.getNrErrors());
    assertTrue(result.getResult());
  }

  @Test
  public void execute_nonblocking() throws KettleException {
    final long waitTime = 1000;
    TestJobEntry je = new TestJobEntry(waitTime);

    je.setParentJob(new Job("test", null, null));
    je.getJobConfig().setBlockingExecution("false");
    Result result = new Result();
    long start = System.currentTimeMillis();
    je.execute(result, 0);
    long end = System.currentTimeMillis();
    assertTrue("Total runtime should be less than the wait time if we're not blocking", (end - start) < waitTime);

    Assert.assertEquals(0, result.getNrErrors());
    assertTrue(result.getResult());
  }

  @Test
  public void execute_interrupted() throws KettleException {
    final long waitTime = 1000 * 10;
    final List<String> loggedErrors = new ArrayList<String>();
    TestJobEntry je = new TestJobEntry(waitTime) {
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

    // Start another thread to stop the parent job and unblock the job entry in 1 second
    t.start();

    long start = System.currentTimeMillis();
    je.execute(result, 0);
    long end = System.currentTimeMillis();
    assertTrue("Total runtime should be less than the wait time if we were properly interrupted", (end - start) < waitTime);

    Assert.assertEquals(1, result.getNrErrors());
    assertFalse(result.getResult());

    // Make sure when an uncaught exception occurs an error log message is generated
    Assert.assertEquals(1, loggedErrors.size());
  }

}
