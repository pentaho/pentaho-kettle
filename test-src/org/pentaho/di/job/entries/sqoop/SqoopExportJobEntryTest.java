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

import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SqoopExportJobEntryTest {

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

    config.setJobEntryName("testing");
    config.setBlockingExecution(false);
    config.setBlockingPollingInterval("100");
    config.setArgumentValue("connect", connectValue);
    config.setArgumentValue("export-dir", "/test-export");

    je.setSqoopConfig(config);

    JobEntryCopy jec = new JobEntryCopy(je);
    jec.setLocation(0, 0);
    String xml = jec.getXML();

    Document d = XMLHandler.loadXMLString(xml);

    SqoopExportJobEntry je2 = new SqoopExportJobEntry();
    je2.loadXML(d.getDocumentElement(), null, null, null);

    SqoopConfig config2 = je2.getSqoopConfig();
    assertEquals(config.getJobEntryName(), config2.getJobEntryName());
    assertEquals(config.isBlockingExecution(), config2.isBlockingExecution());
    assertEquals(config.getBlockingPollingInterval(), config2.getBlockingPollingInterval());
    assertEquals(config.getArgument("connect").getValue(), config2.getArgument("connect").getValue());
    assertEquals(config.getArgument("export-dir").getValue(), config2.getArgument("export-dir").getValue());
  }

  @Test
  public void saveLoadTest_rep() throws KettleException, IOException {
    SqoopExportJobEntry je = new SqoopExportJobEntry();
    SqoopExportConfig config = new SqoopExportConfig();
    String connectValue = "jdbc:mysql://localhost:3306/test";

    config.setJobEntryName("testing");
    config.setBlockingExecution(false);
    config.setBlockingPollingInterval("100");
    config.setArgumentValue("connect", connectValue);
    config.setArgumentValue("export-dir", "/test-export");

    je.setSqoopConfig(config);

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
      SqoopConfig config2 = je2.getSqoopConfig();
      assertEquals(config.getJobEntryName(), config2.getJobEntryName());
      assertEquals(config.isBlockingExecution(), config2.isBlockingExecution());
      assertEquals(config.getBlockingPollingInterval(), config2.getBlockingPollingInterval());
      assertEquals(config.getArgument("connect").getValue(), config2.getArgument("connect").getValue());
      assertEquals(config.getArgument("export-dir").getValue(), config2.getArgument("export-dir").getValue());
    } finally {
      // Delete test database
      new File(filename+".h2.db").delete();
      new File(filename+".trace.db").delete();
    }
  }
}
