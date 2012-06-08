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
import org.apache.log4j.Logger;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryCreationHelper;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryMeta;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class SqoopImportJobEntryTest {

  @Test
  public void buildSqoopConfig() {
    SqoopImportJobEntry je = new SqoopImportJobEntry();
    assertEquals(SqoopImportConfig.class, je.getSqoopConfig().getClass());
  }

  @Test
  public void getToolName() {
    SqoopImportJobEntry je = new SqoopImportJobEntry();
    assertEquals("import", je.getToolName());
  }

  @Test
  public void saveLoadTest_xml() throws KettleXMLException {
    SqoopImportJobEntry je = new SqoopImportJobEntry();
    SqoopImportConfig config = new SqoopImportConfig();
    String connectValue = "jdbc:mysql://localhost:3306/test";
    String myPassword = "my-password";

    config.setJobEntryName("testing");
    config.setBlockingExecution("false");
    config.setBlockingPollingInterval("100");
    config.setConnect(connectValue);
    config.setTargetDir("/test-import-target");
    config.setPassword(myPassword);

    je.setSqoopConfig(config);

    JobEntryCopy jec = new JobEntryCopy(je);
    jec.setLocation(0, 0);
    String xml = jec.getXML();

    assertTrue("Password not encrypted upon save to xml", !xml.contains(myPassword));

    Document d = XMLHandler.loadXMLString(xml);

    SqoopImportJobEntry je2 = new SqoopImportJobEntry();
    je2.loadXML(d.getDocumentElement(), null, null, null);

    SqoopImportConfig config2 = je2.getSqoopConfig();
    assertEquals(config.getJobEntryName(), config2.getJobEntryName());
    assertEquals(config.getBlockingExecution(), config2.getBlockingExecution());
    assertEquals(config.getBlockingPollingInterval(), config2.getBlockingPollingInterval());
    assertEquals(config.getConnect(), config2.getConnect());
    assertEquals(config.getTargetDir(), config2.getTargetDir());
    assertEquals(config.getPassword(), config2.getPassword());
  }

  @Test
  public void saveLoadTest_rep() throws KettleException, IOException {
    SqoopImportJobEntry je = new SqoopImportJobEntry();
    SqoopImportConfig config = new SqoopImportConfig();
    String connectValue = "jdbc:mysql://localhost:3306/test";

    config.setJobEntryName("testing");
    config.setBlockingExecution("false");
    config.setBlockingPollingInterval("100");
    config.setConnect(connectValue);
    config.setTargetDir("/test-import-target");

    je.setSqoopConfig(config);

    KettleEnvironment.init();
    String filename = File.createTempFile(getClass().getSimpleName() + "-import-dbtest", "").getAbsolutePath();

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
      SqoopImportJobEntry je2 = new SqoopImportJobEntry();
      je2.loadRep(repository, id_job, null, null);

      // Make sure all settings we set are properly loaded
      SqoopImportConfig config2 = je2.getSqoopConfig();
      assertEquals(config.getJobEntryName(), config2.getJobEntryName());
      assertEquals(config.getBlockingExecution(), config2.getBlockingExecution());
      assertEquals(config.getBlockingPollingInterval(), config2.getBlockingPollingInterval());
      assertEquals(config.getConnect(), config2.getConnect());
      assertEquals(config.getTargetDir(), config2.getTargetDir());
    } finally {
      // Delete test database
      new File(filename+".h2.db").delete();
      new File(filename+".trace.db").delete();
    }
  }

  @Test
  public void setJobResultFailed() {
    SqoopImportJobEntry je = new SqoopImportJobEntry();

    Result jobResult = new Result();
    je.setJobResultFailed(jobResult);
    assertEquals(1L, jobResult.getNrErrors());
    assertFalse(jobResult.getResult());
  }

  @Test
  public void configure() throws KettleException {
    SqoopImportJobEntry je = new SqoopImportJobEntry();

    Configuration conf = new Configuration();
    try {
      je.configure(conf);
      fail("Expected exception");
    } catch (KettleException ex) {
      if (!ex.getMessage().contains("hdfs host")) {
        ex.printStackTrace();
        fail("Wrong exception");
      }
    }

    je.getSqoopConfig().setNamenodeHost("localhost");
    try {
      je.configure(conf);
      fail("Expected exception");
    } catch (KettleException ex) {
      if (!ex.getMessage().contains("job tracker")) {
        ex.printStackTrace();
        fail("Wrong exception");
      }
    }

    je.getSqoopConfig().setNamenodePort("54310");
    je.getSqoopConfig().setJobtrackerHost("anotherhost");
    je.getSqoopConfig().setJobtrackerPort("54311");
    je.configure(conf);

    assertEquals("localhost", je.getSqoopConfig().getNamenodeHost());
    assertEquals("54310", je.getSqoopConfig().getNamenodePort());
    assertEquals("anotherhost", je.getSqoopConfig().getJobtrackerHost());
    assertEquals("54311", je.getSqoopConfig().getJobtrackerPort());
  }

  @Test
  public void attachAndRemoveLoggingAppenders() {
    SqoopImportJobEntry je = new SqoopImportJobEntry();

    PrintStream stderr = System.err;
    Logger sqoopLogger = SqoopUtils.findLogger("org.apache.sqoop");
    Logger hadoopLogger = SqoopUtils.findLogger("org.apache.hadoop");

    assertFalse(sqoopLogger.getAllAppenders().hasMoreElements());
    assertFalse(hadoopLogger.getAllAppenders().hasMoreElements());

    try {
      je.attachLoggingAppenders();

      assertTrue(sqoopLogger.getAllAppenders().hasMoreElements());
      assertTrue(hadoopLogger.getAllAppenders().hasMoreElements());

      assertEquals(LoggingProxy.class, System.err.getClass());

      je.removeLoggingAppenders();
      assertFalse(sqoopLogger.getAllAppenders().hasMoreElements());
      assertFalse(hadoopLogger.getAllAppenders().hasMoreElements());
      assertEquals(stderr, System.err);

    } finally {
      System.setErr(stderr);
      sqoopLogger.removeAllAppenders();
      hadoopLogger.removeAllAppenders();
    }
  }
}
