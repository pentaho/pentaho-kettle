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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.sql.Driver;
import java.util.List;

import org.apache.hadoop.hive.jdbc.HiveDriver;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobTracker;
import org.apache.hadoop.util.VersionInfo;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.hadoop.mapreduce.GenericTransCombiner;
import org.pentaho.hadoop.mapreduce.GenericTransReduce;
import org.pentaho.hadoop.mapreduce.PentahoMapRunnable;
import org.pentaho.hadoop.mapreduce.converter.TypeConverterFactory;
import org.pentaho.hadoop.shim.ConfigurationException;
import org.pentaho.hadoop.shim.HadoopConfiguration;
import org.pentaho.hadoop.shim.HadoopConfigurationFileSystemManager;
import org.pentaho.hadoop.shim.ShimVersion;
import org.pentaho.hadoop.shim.api.Configuration;
import org.pentaho.hadoop.shim.api.DistributedCacheUtil;
import org.pentaho.hadoop.shim.api.fs.FileSystem;
import org.pentaho.hadoop.shim.api.mapred.RunningJob;
import org.pentaho.hadoop.shim.common.fs.FileSystemProxy;
import org.pentaho.hadoop.shim.common.mapred.RunningJobProxy;
import org.pentaho.hadoop.shim.spi.HadoopShim;
import org.pentaho.hdfs.vfs.HDFSFileProvider;

@SuppressWarnings("deprecation")
public class CommonHadoopShim implements HadoopShim {

  private DistributedCacheUtil dcUtil;
  
  @Override
  public ShimVersion getVersion() {
    return new ShimVersion(1, 0);
  }

  @Override
  public String getHadoopVersion() {
    return VersionInfo.getVersion();
  }

  @Override
  public void onLoad(HadoopConfiguration config, HadoopConfigurationFileSystemManager fsm) throws Exception {
    fsm.addProvider(config, "hdfs", config.getIdentifier(), new HDFSFileProvider());
    setDistributedCacheUtil(new DistributedCacheUtilImpl(config));
  }
  
  @Override
  public Driver getHiveJdbcDriver() {
    try {
      return new HiveDriver();
    } catch (Exception ex) {
      throw new RuntimeException("Unable to load Hive JDBC driver", ex);
    }
  }
  
  @Override
  public Configuration createConfiguration() {
    // Set the context class loader when instantiating the configuration
    // since org.apache.hadoop.conf.Configuration uses it to load resources
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    try {
      return new org.pentaho.hadoop.shim.common.ConfigurationProxy();
    } finally {
      Thread.currentThread().setContextClassLoader(cl);
    }
  }
  
  @Override
  public FileSystem getFileSystem(Configuration conf) throws IOException {
    // Set the context class loader when instantiating the configuration
    // since org.apache.hadoop.conf.Configuration uses it to load resources
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    try {
      return new FileSystemProxy(org.apache.hadoop.fs.FileSystem.get(ShimUtils.asConfiguration(conf)));
    } finally {
      Thread.currentThread().setContextClassLoader(cl);
    }
  }

  public void setDistributedCacheUtil(DistributedCacheUtil dcUtil) {
    if (dcUtil == null) {
      throw new NullPointerException();
    }
    this.dcUtil = dcUtil;
  }

  @Override
  public DistributedCacheUtil getDistributedCacheUtil() throws ConfigurationException {
    if (dcUtil == null) {
      throw new ConfigurationException(BaseMessages.getString(CommonHadoopShim.class, "CommonHadoopShim.DistributedCacheUtilMissing"));
    }
    return dcUtil;
  }
  
  @Override
  public String[] getNamenodeConnectionInfo(Configuration c) {
    URI namenode = org.apache.hadoop.fs.FileSystem.getDefaultUri(ShimUtils.asConfiguration(c));
    String[] result = new String[2];
    if (namenode != null) {
      result[0] = namenode.getHost();
      if (namenode.getPort() != -1) {
        result[1] = String.valueOf(namenode.getPort());
      }
    }
    return result;
  }

  @Override
  public String[] getJobtrackerConnectionInfo(Configuration c) {
    String[] result = new String[2];
    if (!"local".equals(c.get("mapred.job.tracker", "local"))) {
      InetSocketAddress jobtracker = JobTracker.getAddress(ShimUtils.asConfiguration(c));
      result[0] = jobtracker.getHostName();
      result[1] = String.valueOf(jobtracker.getPort());
    }
    return result;
  }
  
  @Override
  public void configureConnectionInformation(String namenodeHost, String namenodePort, String jobtrackerHost,
      String jobtrackerPort, Configuration conf, List<String> logMessages) throws Exception {

    if (namenodeHost == null || namenodeHost.trim().length() == 0) {
      throw new Exception("No hdfs host specified!");
    }
    if (jobtrackerHost == null || jobtrackerHost.trim().length() == 0) {
      throw new Exception("No job tracker host specified!");
    }
    
    if (namenodePort == null || namenodePort.trim().length() == 0) {
      namenodePort = getDefaultNamenodePort();
      logMessages.add("No hdfs port specified - using default: " + namenodePort);
    }
    
    if (jobtrackerPort == null || jobtrackerPort.trim().length() == 0) {
      jobtrackerPort = getDefaultJobtrackerPort();
      logMessages.add("No job tracker port specified - using default: " + jobtrackerPort);
    }
    
    String fsDefaultName = "hdfs://" + namenodeHost + ":" + namenodePort;
    String jobTracker = jobtrackerHost + ":" + jobtrackerPort;
    
    conf.set("fs.default.name", fsDefaultName);
    conf.set("mapred.job.tracker", jobTracker);    
  }
  
  /**
   * @return the default port of the namenode
   */
  protected String getDefaultNamenodePort() {
    return "9000";
  }

  /**
   * @return the default port of the jobtracker 
   */
  protected String getDefaultJobtrackerPort() {
    return "9001";
  }

  @Override
  public RunningJob submitJob(Configuration c) throws IOException {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    try {
      @SuppressWarnings("deprecation")
      JobConf conf = ShimUtils.asConfiguration(c);
      JobClient jobClient = new JobClient(conf);
      return new RunningJobProxy(jobClient.submitJob(conf));
    } finally {
      Thread.currentThread().setContextClassLoader(cl);
    }
  }

  @Override
  public Class<? extends Writable> getHadoopWritableCompatibleClass(ValueMetaInterface kettleType) {
    return TypeConverterFactory.getWritableForKettleType(kettleType);
  }
  
  @Override
  public Class<?> getPentahoMapReduceCombinerClass() {
    return GenericTransCombiner.class;
  }
  
  @Override
  public Class<?> getPentahoMapReduceReducerClass() {
    return GenericTransReduce.class;
  }
  
  @Override
  public Class<?> getPentahoMapReduceMapRunnerClass() {
    return PentahoMapRunnable.class;
  }
}
