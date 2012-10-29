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

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryCreationHelper;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryMeta;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.*;

public class SqoopExportJobEntryTest {

  @BeforeClass
  public static void init() throws KettleException {
    KettleEnvironment.init();
  }

  @Test
  public void conditionalsForUIInteractions() {
    SqoopExportJobEntry je = new SqoopExportJobEntry();

    assertTrue(je.evaluates());
    assertTrue(je.isUnconditional());
  }

  @Test
  public void getTool() {
    SqoopExportJobEntry je = new SqoopExportJobEntry();

    assertEquals("export", je.getToolName());
  }

  @Test
  public void saveLoadTest_xml() throws KettleXMLException {
    SqoopExportJobEntry je = new SqoopExportJobEntry();
    SqoopExportConfig config = new SqoopExportConfig();
    String connectValue = "jdbc:mysql://localhost:3306/test";
    String myPassword = "my-password";
    DatabaseMeta databaseMeta = new DatabaseMeta("test database", "H2", null, null, null, null, null, null);

    config.setJobEntryName("testing");
    config.setBlockingExecution("false");
    config.setBlockingPollingInterval("100");
    config.setConnect(connectValue);
    config.setExportDir("/test-export");
    config.setPassword(myPassword);

    config.setDatabase(databaseMeta.getName());

    je.setJobConfig(config);

    JobEntryCopy jec = new JobEntryCopy(je);
    jec.setLocation(0, 0);
    String xml = jec.getXML();

    assertTrue("Password not encrypted upon save to xml", !xml.contains(myPassword));

    Document d = XMLHandler.loadXMLString(xml);

    SqoopExportJobEntry je2 = new SqoopExportJobEntry();
    je2.loadXML(d.getDocumentElement(), Collections.singletonList(databaseMeta), null, null);

    SqoopExportConfig config2 = je2.getJobConfig();
    assertEquals(config.getJobEntryName(), config2.getJobEntryName());
    assertEquals(config.getBlockingExecution(), config2.getBlockingExecution());
    assertEquals(config.getBlockingPollingInterval(), config2.getBlockingPollingInterval());
    assertEquals(config.getConnect(), config2.getConnect());
    assertEquals(config.getExportDir(), config2.getExportDir());
    assertEquals(config.getPassword(), config2.getPassword());
    assertEquals(config.getDatabase(), config2.getDatabase());

    assertNotNull(je2.getDatabaseMeta());
    assertEquals(databaseMeta.getName(), je2.getDatabaseMeta().getName());
  }

  @Test
  public void saveLoadTest_xml_advanced_options() throws KettleXMLException {
    SqoopExportJobEntry je = new SqoopExportJobEntry();
    SqoopExportConfig config = new SqoopExportConfig();
    String connectValue = "jdbc:mysql://localhost:3306/test";
    String myPassword = "my-password";

    config.setJobEntryName("testing");
    config.setBlockingExecution("false");
    config.setBlockingPollingInterval("100");
    config.setConnect(connectValue);
    config.setExportDir("/test-export");
    config.setPassword(myPassword);

    config.copyConnectionInfoToAdvanced();
    
    je.setJobConfig(config);

    JobEntryCopy jec = new JobEntryCopy(je);
    jec.setLocation(0, 0);
    String xml = jec.getXML();

    assertTrue("Password not encrypted upon save to xml", !xml.contains(myPassword));

    Document d = XMLHandler.loadXMLString(xml);

    SqoopExportJobEntry je2 = new SqoopExportJobEntry();
    je2.loadXML(d.getDocumentElement(), null, null, null);

    SqoopExportConfig config2 = je2.getJobConfig();
    assertEquals(config.getJobEntryName(), config2.getJobEntryName());
    assertEquals(config.getBlockingExecution(), config2.getBlockingExecution());
    assertEquals(config.getBlockingPollingInterval(), config2.getBlockingPollingInterval());
    assertEquals(config.getConnect(), config2.getConnect());
    assertEquals(config.getExportDir(), config2.getExportDir());
    assertEquals(config.getPassword(), config2.getPassword());
    assertNull(config2.getDatabase());

    assertEquals(config.getConnectFromAdvanced(), config2.getConnectFromAdvanced());
    assertEquals(config.getUsernameFromAdvanced(), config2.getUsernameFromAdvanced());
    assertEquals(config.getPasswordFromAdvanced(), config2.getPasswordFromAdvanced());
    
    assertNull(je2.getDatabaseMeta());
  }

  @Test
  public void saveLoadTest_rep() throws KettleException, IOException {
    SqoopExportJobEntry je = new SqoopExportJobEntry();
    SqoopExportConfig config = new SqoopExportConfig();
    String connectValue = "jdbc:mysql://localhost:3306/test";
    DatabaseMeta meta = new DatabaseMeta("test database", "H2", null, null, null, null, null, null);

    config.setJobEntryName("testing");
    config.setBlockingExecution("${blocking}");
    config.setBlockingPollingInterval("100");
    config.setConnect(connectValue);
    config.setExportDir("/test-export");
    config.setPassword("my-password");
    config.setDatabase(meta.getName());

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
      SqoopExportJobEntry je2 = new SqoopExportJobEntry();
      je2.loadRep(repository, id_job, Collections.singletonList(meta), null);

      // Make sure all settings we set are properly loaded
      SqoopExportConfig config2 = je2.getJobConfig();
      assertEquals(config.getJobEntryName(), config2.getJobEntryName());
      assertEquals(config.getBlockingExecution(), config2.getBlockingExecution());
      assertEquals(config.getBlockingPollingInterval(), config2.getBlockingPollingInterval());
      assertEquals(config.getConnect(), config2.getConnect());
      assertEquals(config.getExportDir(), config2.getExportDir());
      assertEquals(config.getPassword(), config2.getPassword());

      assertNotNull(je2.getDatabaseMeta());
      assertEquals(meta.getName(), je2.getDatabaseMeta().getName());
    } finally {
      // Delete test database
      new File(filename+".h2.db").delete();
      new File(filename+".trace.db").delete();
    }
  }

  @Test
  public void saveLoadTest_rep_advanced_options() throws KettleException, IOException {
    SqoopExportJobEntry je = new SqoopExportJobEntry();
    SqoopExportConfig config = new SqoopExportConfig();
    String connectValue = "jdbc:mysql://localhost:3306/test";
    
    config.setJobEntryName("testing");
    config.setBlockingExecution("${blocking}");
    config.setBlockingPollingInterval("100");
    config.setConnect(connectValue);
    config.setExportDir("/test-export");
    config.setPassword("my-password");
    config.copyConnectionInfoToAdvanced();
    
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
      SqoopExportJobEntry je2 = new SqoopExportJobEntry();
      je2.loadRep(repository, id_job, null, null);
      
      // Make sure all settings we set are properly loaded
      SqoopExportConfig config2 = je2.getJobConfig();
      assertEquals(config.getJobEntryName(), config2.getJobEntryName());
      assertEquals(config.getBlockingExecution(), config2.getBlockingExecution());
      assertEquals(config.getBlockingPollingInterval(), config2.getBlockingPollingInterval());
      assertEquals(config.getConnect(), config2.getConnect());
      assertEquals(config.getExportDir(), config2.getExportDir());
      assertEquals(config.getPassword(), config2.getPassword());
      assertNull(config2.getDatabase());
      
      assertEquals(config.getConnectFromAdvanced(), config2.getConnectFromAdvanced());
      assertEquals(config.getUsernameFromAdvanced(), config2.getUsernameFromAdvanced());
      assertEquals(config.getPasswordFromAdvanced(), config2.getPasswordFromAdvanced());

      assertNull(je2.getDatabaseMeta());
    } finally {
      // Delete test database
      new File(filename+".h2.db").delete();
      new File(filename+".trace.db").delete();
    }
  }

  @Test
  public void getDatabaseMeta() throws KettleException {
    SqoopExportJobEntry entry = new SqoopExportJobEntry();
    DatabaseMeta meta = new DatabaseMeta("test", "H2", null, null, null, null, null, null);

    assertNull(entry.getDatabaseMeta());

    entry.setDatabaseMeta(meta);

    assertEquals(meta, entry.getDatabaseMeta());
  }
}
