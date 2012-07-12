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

package org.pentaho.hadoop.shim.spi;

import java.io.IOException;
import java.sql.Driver;
import java.util.List;

import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.hadoop.shim.HadoopConfiguration;
import org.pentaho.hadoop.shim.api.Configuration;
import org.pentaho.hadoop.shim.api.DistributedCacheUtil;
import org.pentaho.hadoop.shim.api.HadoopConfigurationFileSystemManager;
import org.pentaho.hadoop.shim.api.fs.FileSystem;
import org.pentaho.hadoop.shim.api.mapred.RunningJob;
import org.pentaho.hadoop.shim.spi.HadoopShim;

public class MockHadoopShim implements HadoopShim {

  @Override
  public void onLoad(HadoopConfiguration config, HadoopConfigurationFileSystemManager fsm) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public String[] getNamenodeConnectionInfo(Configuration c) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getJobtrackerConnectionInfo(Configuration c) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getHadoopVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Configuration createConfiguration() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FileSystem getFileSystem(Configuration conf) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Driver getHiveJdbcDriver() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void configureConnectionInformation(String namenodeHost, String namenodePort, String jobtrackerHost,
      String jobtrackerPort, Configuration conf, List<String> logMessages) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public DistributedCacheUtil getDistributedCacheUtil() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RunningJob submitJob(Configuration c) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Class<?> getHadoopWritableCompatibleClass(ValueMetaInterface kettleType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Class<?> getPentahoMapReduceCombinerClass() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Class<?> getPentahoMapReduceReducerClass() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Class<?> getPentahoMapReduceMapRunnerClass() {
    // TODO Auto-generated method stub
    return null;
  }

}
