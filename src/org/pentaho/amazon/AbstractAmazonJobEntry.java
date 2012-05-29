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

package org.pentaho.amazon;

import com.amazonaws.auth.AWSCredentials;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;

/**
 * created by: rfellows
 * date:       5/24/12
 */
public abstract class AbstractAmazonJobEntry extends JobEntryBase implements Cloneable, JobEntryInterface {

  protected String hadoopJobName;
  protected String hadoopJobFlowId;
  protected String accessKey = "";
  protected String secretKey = "";
  protected String jarUrl = "";
  protected String stagingDir = "";
  protected String numInstances = "2";
  protected String masterInstanceType = "Small [m1.small]";
  protected String slaveInstanceType = "Small [m1.small]";
  protected String cmdLineArgs;
  protected boolean blocking;
  protected String loggingInterval = "60"; // 60 seconds default


  public String getHadoopJobName() {
    return hadoopJobName;
  }

  public void setHadoopJobName(String hadoopJobName) {
    this.hadoopJobName = hadoopJobName;
  }

  public String getHadoopJobFlowId() {
    return hadoopJobFlowId;
  }

  public void setHadoopJobFlowId(String hadoopJobFlowId) {
    this.hadoopJobFlowId = hadoopJobFlowId;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getJarUrl() {
    return jarUrl;
  }

  public void setJarUrl(String jarUrl) {
    this.jarUrl = jarUrl;
  }

  public String getStagingDir() {
    return stagingDir;
  }

  public void setStagingDir(String stagingDir) {
    this.stagingDir = stagingDir;
  }

  public String getNumInstances() {
    return numInstances;
  }

  public void setNumInstances(String numInstances) {
    this.numInstances = numInstances;
  }

  public String getMasterInstanceType() {
    return masterInstanceType;
  }

  public void setMasterInstanceType(String masterInstanceType) {
    this.masterInstanceType = masterInstanceType;
  }

  public String getSlaveInstanceType() {
    return slaveInstanceType;
  }

  public void setSlaveInstanceType(String slaveInstanceType) {
    this.slaveInstanceType = slaveInstanceType;
  }

  public String getCmdLineArgs() {
    return cmdLineArgs;
  }

  public void setCmdLineArgs(String cmdLineArgs) {
    this.cmdLineArgs = cmdLineArgs;
  }

  public boolean isBlocking() {
    return blocking;
  }

  public void setBlocking(boolean blocking) {
    this.blocking = blocking;
  }

  public String getLoggingInterval() {
    return loggingInterval;
  }

  public void setLoggingInterval(String loggingInterval) {
    this.loggingInterval = loggingInterval;
  }

  protected AWSCredentials awsCredentials = new AWSCredentials() {

    public String getAWSSecretKey() {
      return environmentSubstitute(secretKey);
    }

    public String getAWSAccessKeyId() {
      return environmentSubstitute(accessKey);
    }
  };
}
