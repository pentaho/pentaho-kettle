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

package org.pentaho.hadoop.shim.common;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.hadoop.util.VersionInfo;
import org.junit.Test;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.hadoop.mapreduce.GenericTransCombiner;
import org.pentaho.hadoop.mapreduce.GenericTransReduce;
import org.pentaho.hadoop.mapreduce.PentahoMapRunnable;
import org.pentaho.hadoop.shim.ConfigurationException;
import org.pentaho.hadoop.shim.HadoopConfiguration;
import org.pentaho.hadoop.shim.HadoopConfigurationFileSystemManager;
import org.pentaho.hadoop.shim.MockHadoopConfigurationProvider;
import org.pentaho.hadoop.shim.api.Configuration;
import org.pentaho.hadoop.shim.spi.HadoopConfigurationProvider;
import org.pentaho.hadoop.shim.spi.HadoopShim;

public class CommonHadoopShimTest {

  @Test
  public void getHadoopVersion() {
    CommonHadoopShim shim = new CommonHadoopShim();
    assertEquals(VersionInfo.getVersion(), shim.getHadoopVersion());
  }
  
  @Test(timeout = 10000)
  public void getNamenodeConnectionInfo() {
    CommonHadoopShim shim = new CommonHadoopShim();
    Configuration c = shim.createConfiguration();
    c.set("fs.default.name", "localhost:54310");
    String[] info = shim.getNamenodeConnectionInfo(c);

    assertEquals(2, info.length);
    assertEquals("localhost", info[0]);
    assertEquals("54310", info[1]);
  }
  
  @Test
  public void getNamenodeConnectionInfo_local() {
    CommonHadoopShim shim = new CommonHadoopShim();
    Configuration c = shim.createConfiguration();
    c.set("fs.default.name", "file:///");
    String[] info = shim.getNamenodeConnectionInfo(c);
    
    assertEquals(2, info.length);
    assertNull(info[0]);
    assertNull(info[1]);
  }

  @Test(timeout = 10000)
  public void getJobtrackerConnectionInfo() {
    CommonHadoopShim shim = new CommonHadoopShim();
    Configuration c = shim.createConfiguration();
    c.set("mapred.job.tracker", "anotherhost:54311");
    String[] info = shim.getJobtrackerConnectionInfo(c);

    assertEquals(2, info.length);
    assertEquals("anotherhost", info[0]);
    assertEquals("54311", info[1]);
  }
  
  @Test
  public void getJobtrackerConnectionInfo_local() {
    CommonHadoopShim shim = new CommonHadoopShim();
    Configuration c = shim.createConfiguration();
    c.set("mapred.job.tracker", "local");
    String[] info = shim.getJobtrackerConnectionInfo(c);
    
    assertEquals(2, info.length);
    assertNull(info[0]);
    assertNull(info[1]);
  }
  
  @Test
  public void configureConnectionInformation() throws Exception {
    CommonHadoopShim shim = new CommonHadoopShim();
    Configuration conf = new ConfigurationProxy();
    List<String> logMessages = new ArrayList<String>();
    shim.configureConnectionInformation("namenodeHost", "namenodePort", "jobtrackerHost", "jobtrackerPort", conf, logMessages);
    assertEquals(conf.get("fs.default.name"), "hdfs://namenodeHost:namenodePort");
    assertEquals(conf.get("mapred.job.tracker"), "jobtrackerHost:jobtrackerPort");
    assertEquals(0, logMessages.size());
  }
  
  @Test(expected=Exception.class)
  public void configureConnectionInformation_null_namenodeHost() throws Exception {
    CommonHadoopShim shim = new CommonHadoopShim();
    Configuration conf = new ConfigurationProxy();
    List<String> logMessages = new ArrayList<String>();
    shim.configureConnectionInformation(null, "namenodePort", "jobtrackerHost", "jobtrackerPort", conf, logMessages);
  }
  
  @Test(expected=Exception.class)
  public void configureConnectionInformation_empty_namenodeHost() throws Exception {
    CommonHadoopShim shim = new CommonHadoopShim();
    Configuration conf = new ConfigurationProxy();
    List<String> logMessages = new ArrayList<String>();
    shim.configureConnectionInformation("", "namenodePort", "jobtrackerHost", "jobtrackerPort", conf, logMessages);
  }
  
  @Test
  public void configureConnectionInformation_null_namenodePort() throws Exception {
    CommonHadoopShim shim = new CommonHadoopShim();
    Configuration conf = new ConfigurationProxy();
    List<String> logMessages = new ArrayList<String>();
    shim.configureConnectionInformation("namenodeHost", null, "jobtrackerHost", "jobtrackerPort", conf, logMessages);
    assertEquals(conf.get("fs.default.name"), "hdfs://namenodeHost:" + shim.getDefaultNamenodePort());
    assertEquals(conf.get("mapred.job.tracker"), "jobtrackerHost:jobtrackerPort");
    assertEquals(1, logMessages.size());
    String message = logMessages.get(0);
    assertTrue("Unexpected message: " + message, message.contains("using default"));

    logMessages = new ArrayList<String>();
    shim.configureConnectionInformation("namenodeHost", "", "jobtrackerHost", "jobtrackerPort", conf, logMessages);
    assertEquals(conf.get("fs.default.name"), "hdfs://namenodeHost:" + shim.getDefaultNamenodePort());
    assertEquals(conf.get("mapred.job.tracker"), "jobtrackerHost:jobtrackerPort");
    assertEquals(1, logMessages.size());
    message = logMessages.get(0);
    assertTrue("Unexpected message: " + message, message.contains("using default"));
  }
  
  @Test(expected=Exception.class)
  public void configureConnectionInformation_null_jobtrackerHost() throws Exception {
    CommonHadoopShim shim = new CommonHadoopShim();
    Configuration conf = new ConfigurationProxy();
    List<String> logMessages = new ArrayList<String>();
    shim.configureConnectionInformation("namenodeHost", "namenodePort", null, "jobtrackerPort", conf, logMessages);
  }
  
  @Test(expected=Exception.class)
  public void configureConnectionInformation_empty_jobtrackerHost() throws Exception {
    CommonHadoopShim shim = new CommonHadoopShim();
    Configuration conf = new ConfigurationProxy();
    List<String> logMessages = new ArrayList<String>();
    shim.configureConnectionInformation("namenodeHost", "namenodePort", "", "jobtrackerPort", conf, logMessages);
  }
  
  @Test
  public void configureConnectionInformation_null_jobtrackerPort() throws Exception {
    CommonHadoopShim shim = new CommonHadoopShim();
    Configuration conf = new ConfigurationProxy();
    List<String> logMessages = new ArrayList<String>();
    shim.configureConnectionInformation("namenodeHost", "namenodePort", "jobtrackerHost", null, conf, logMessages);
    assertEquals(conf.get("fs.default.name"), "hdfs://namenodeHost:namenodePort");
    assertEquals(conf.get("mapred.job.tracker"), "jobtrackerHost:" + shim.getDefaultJobtrackerPort());
    assertEquals(1, logMessages.size());
    String message = logMessages.get(0);
    assertTrue("Unexpected message: " + message, message.contains("using default"));

    logMessages = new ArrayList<String>();
    shim.configureConnectionInformation("namenodeHost", "namenodePort", "jobtrackerHost", "", conf, logMessages);
    assertEquals(conf.get("fs.default.name"), "hdfs://namenodeHost:namenodePort");
    assertEquals(conf.get("mapred.job.tracker"), "jobtrackerHost:" + shim.getDefaultJobtrackerPort());
    assertEquals(1, logMessages.size());
    message = logMessages.get(0);
    assertTrue("Unexpected message: " + message, message.contains("using default"));
  }
  
  @Test
  public void onLoad() throws Exception {
    HadoopConfigurationProvider configProvider = new HadoopConfigurationProvider() {
      @Override
      public boolean hasConfiguration(String id) {
        throw new UnsupportedOperationException();
      }
      
      @Override
      public List<? extends HadoopConfiguration> getConfigurations() {
        throw new UnsupportedOperationException();
      }
      
      @Override
      public HadoopConfiguration getConfiguration(String id) throws ConfigurationException {
        throw new UnsupportedOperationException();
      }
      
      @Override
      public HadoopConfiguration getActiveConfiguration() throws ConfigurationException {
        throw new UnsupportedOperationException();
      }
    };
    DefaultFileSystemManager delegate = new DefaultFileSystemManager();
    HadoopConfigurationFileSystemManager fsm = new HadoopConfigurationFileSystemManager(configProvider, delegate);
    assertFalse(fsm.hasProvider("hdfs"));

    CommonHadoopShim shim = new CommonHadoopShim();
    HadoopConfiguration config = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", shim, null, null, null);

    shim.onLoad(config, fsm);
    assertTrue(fsm.hasProvider("hdfs"));
    assertTrue(fsm.hasProvider("hdfs-id"));
  }
  
  @Test
  public void getDistributedCacheUtil() throws Exception {
    HadoopShim shim = new CommonHadoopShim();
    try {
      shim.getDistributedCacheUtil();
      fail("expected exception");
    } catch (ConfigurationException ex) {
      assertEquals(ex.getMessage(), BaseMessages.getString(CommonHadoopShim.class, "CommonHadoopShim.DistributedCacheUtilMissing"));
    }

    shim.onLoad(new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", shim), new HadoopConfigurationFileSystemManager(new MockHadoopConfigurationProvider(), new DefaultFileSystemManager()));
    assertNotNull(shim.getDistributedCacheUtil());
  }
  
  @Test
  public void setDistributedCacheUtil_null() {
    CommonHadoopShim shim = new CommonHadoopShim();
    try {
      shim.setDistributedCacheUtil(null);
      fail("expected exception");
    } catch (NullPointerException ex) {
      assertNotNull(ex);
    }
  }
  
  @Test
  public void getPentahoMapReduceReducerClass() {
    assertEquals(GenericTransReduce.class, new CommonHadoopShim().getPentahoMapReduceReducerClass());
  }

  @Test
  public void getPentahoMapReduceCombinerClass() {
    assertEquals(GenericTransCombiner.class, new CommonHadoopShim().getPentahoMapReduceCombinerClass());
  }

  @Test
  public void getPentahoMapReduceMapRunnerClass() {
    assertEquals(PentahoMapRunnable.class, new CommonHadoopShim().getPentahoMapReduceMapRunnerClass());
  }
}
