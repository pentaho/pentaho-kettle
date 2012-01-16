/*******************************************************************************
 *
 * Pentaho Data Integration
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

package org.pentaho.di.cluster;

import java.io.File;
import java.util.UUID;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.www.SlaveSequence;
import org.pentaho.di.www.SlaveServerConfig;

public class SlaveSequenceTest extends TestCase {

  private LoggingObjectInterface loggingObject;
  
  protected void init() throws Exception {
    // Bootstrap the Kettle API...
    //
    KettleEnvironment.init();
    CentralLogStore.init(5000, 60); // Keep 5000 log rows for at least 60 minutes
    
    loggingObject = new SimpleLoggingObject("SlaveSequenceTest", LoggingObjectType.GENERAL, null);
  }
  
	/**
	 * This test retrieves next values from a slave sequence.<br>
	 */
	public void testSlaveSequenceRetrieval_Specifed() throws Exception {
		init();
		
		String SLAVE_SEQUENCE_NAME = "test";
		
		SlaveServerConfig slaveConfig = new SlaveServerConfig("localhost", 8282, false);
		slaveConfig.getSlaveServer().setUsername("cluster");
    slaveConfig.getSlaveServer().setPassword("cluster");

    String dbDir = System.getProperty("java.io.tmpdir")+"/"+UUID.randomUUID().toString()+"-slaveSeqTest-H2-DB";
		DatabaseMeta databaseMeta = new DatabaseMeta("H2", "H2", "Native", null, dbDir, null, null, null);
		slaveConfig.getDatabases().add(databaseMeta);
		
		String table = "SLAVE_SEQUENCE";
		String nameField = "SEQ_NAME";
		String valueField = "SEQ_VALUE";
		
		SlaveSequence slaveSequence = new SlaveSequence(SLAVE_SEQUENCE_NAME, 1L, databaseMeta, null, table, nameField, valueField);
		slaveConfig.getSlaveSequences().add(slaveSequence);
				
		Database db = new Database(loggingObject, databaseMeta);
		db.connect();
		db.execStatement("CREATE TABLE SLAVE_SEQUENCE(SEQ_NAME VARCHAR(100), SEQ_VALUE INTEGER);");
		db.disconnect();
		
		// Start the Carte launcher
		CarteLauncher carteLauncher = new CarteLauncher(slaveConfig);
    Thread thread = new Thread(carteLauncher);
    thread.start();
    
    // Wait until the carte object is available...
    //
    while (carteLauncher.getCarte()==null && !carteLauncher.isFailure()) {
      Thread.sleep(100);
    }
    
    long value = slaveConfig.getSlaveServer().getNextSlaveSequenceValue(SLAVE_SEQUENCE_NAME, 1000);
    assertEquals(1L, value);
    value = slaveConfig.getSlaveServer().getNextSlaveSequenceValue(SLAVE_SEQUENCE_NAME, 1000);
    assertEquals(1001L, value);
    
    try {
      slaveConfig.getSlaveServer().getNextSlaveSequenceValue("Unknown sequence", 1000);
      fail("No error was thrown for retrieval of an unknown sequence");
    } catch(Exception e) {
      // OK!
    }
    
    // After the test, stop the server
    //
    carteLauncher.getCarte().getWebServer().stopServer();
    
    // Remove the database + table in the temporary directory
    //
    File dir = new File(dbDir);
    if (dir.exists()) {
      for (File child : dir.listFiles()) {
        if (child.isFile()) {
          child.delete();
        }
      }
      dir.delete();
    }
	}
	
	 /**
   * This test retrieves next values from a slave sequence.<br>
   */
  public void testSlaveSequenceRetrieval_AutoCreation() throws Exception {
    init();
    
    String SLAVE_SEQUENCE_NAME = "test";
    
    SlaveServerConfig slaveConfig = new SlaveServerConfig("localhost", 8282, false);
    slaveConfig.getSlaveServer().setUsername("cluster");
    slaveConfig.getSlaveServer().setPassword("cluster");

    String dbDir = System.getProperty("java.io.tmpdir")+"/"+UUID.randomUUID().toString()+"-slaveSeqTest-H2-DB";
    DatabaseMeta databaseMeta = new DatabaseMeta("H2", "H2", "Native", null, dbDir, null, null, null);
    slaveConfig.getDatabases().add(databaseMeta);
    
    String table = "SLAVE_SEQUENCE";
    String nameField = "SEQ_NAME";
    String valueField = "SEQ_VALUE";
    
    SlaveSequence slaveSequence = new SlaveSequence(SLAVE_SEQUENCE_NAME, 1L, databaseMeta, null, table, nameField, valueField);
    slaveConfig.setAutomaticCreationAllowed(true);
    slaveConfig.setAutoSequence(slaveSequence);
        
    Database db = new Database(loggingObject, databaseMeta);
    db.connect();
    db.execStatement("CREATE TABLE SLAVE_SEQUENCE(SEQ_NAME VARCHAR(100), SEQ_VALUE INTEGER);");
    db.disconnect();
    
    // Start the Carte launcher
    CarteLauncher carteLauncher = new CarteLauncher(slaveConfig);
    Thread thread = new Thread(carteLauncher);
    thread.start();
    
    // Wait until the carte object is available...
    //
    while (carteLauncher.getCarte()==null && !carteLauncher.isFailure()) {
      Thread.sleep(100);
    }
    
    try {
      long value = slaveConfig.getSlaveServer().getNextSlaveSequenceValue(SLAVE_SEQUENCE_NAME, 1000);
      assertEquals(1L, value);
      value = slaveConfig.getSlaveServer().getNextSlaveSequenceValue(SLAVE_SEQUENCE_NAME, 1000);
      assertEquals(1001L, value);
      value = slaveConfig.getSlaveServer().getNextSlaveSequenceValue("new sequence", 1000);
      assertEquals(1L, value);
      value = slaveConfig.getSlaveServer().getNextSlaveSequenceValue("new sequence", 1000);
      assertEquals(1001L, value);
      value = slaveConfig.getSlaveServer().getNextSlaveSequenceValue(SLAVE_SEQUENCE_NAME, 1000);
      assertEquals(2001L, value);
    } catch(Exception e) {
      fail("And error was thrown for retrieval of an unknown sequence, auto-creation expected");
    }
    
    // After the test, stop the server
    //
    carteLauncher.getCarte().getWebServer().stopServer();
    
    // Remove the database + table in the temporary directory
    //
    File dir = new File(dbDir);
    if (dir.exists()) {
      for (File child : dir.listFiles()) {
        if (child.isFile()) {
          child.delete();
        }
      }
      dir.delete();
    }
  }

}
