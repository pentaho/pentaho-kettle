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

package org.pentaho.amazon.hive.job;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileObject;
import org.pentaho.amazon.AbstractAmazonJobEntry;
import org.pentaho.amazon.AmazonSpoonPlugin;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.Log4jFileAppender;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.model.AddJobFlowStepsRequest;
import com.amazonaws.services.elasticmapreduce.model.DescribeJobFlowsRequest;
import com.amazonaws.services.elasticmapreduce.model.DescribeJobFlowsResult;
import com.amazonaws.services.elasticmapreduce.model.HadoopJarStepConfig;
import com.amazonaws.services.elasticmapreduce.model.JobFlowDetail;
import com.amazonaws.services.elasticmapreduce.model.JobFlowInstancesConfig;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowRequest;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowResult;
import com.amazonaws.services.elasticmapreduce.model.ScriptBootstrapActionConfig;
import com.amazonaws.services.elasticmapreduce.model.StepConfig;
import com.amazonaws.services.elasticmapreduce.model.BootstrapActionConfig;
import com.amazonaws.services.elasticmapreduce.model.TerminateJobFlowsRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

/**
 * AmazonHiveJobExecutor
 * A job entry plug-in class to submits a Hive job into the AWS Elastic MapReduce service from Pentaho Data Integration (Kettle).
 */
@JobEntry(id = "HiveJobExecutorPlugin", name = "Amazon Hive Job Executor", categoryDescription = "Big Data", description = "Execute Hive jobs in Amazon EMR", image = "AWS-HIVE.png")
public class AmazonHiveJobExecutor extends AbstractAmazonJobEntry implements Cloneable, JobEntryInterface {

  private static Class<?> PKG = AmazonHiveJobExecutor.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  protected String qUrl = "";
  protected String bootstrapActions = "";
  protected boolean alive;

  public AmazonHiveJobExecutor() {
  }
  
  public String getQUrl() {
    return qUrl;
  }

  public void setQUrl(String qUrl) {
    this.qUrl = qUrl;
  }

  public String getBootstrapActions() {
    return bootstrapActions;
  }

  public void setBootstrapActions(String bootstrapActions) {
    this.bootstrapActions = bootstrapActions;
  }

  public boolean isAlive() {
    return alive;
  }

  public void setAlive(boolean alive) {
    this.alive = alive;
  }

  /**
   * Executes a Hive job into the AWS Elastic MapReduce service.
   */
  public Result execute(Result result, int arg1) throws KettleException {
    
    // Setup a log file.
    Log4jFileAppender appender = null;
    String logFileName = "pdi-" + this.getName(); //$NON-NLS-1$
    try {
      appender = LogWriter.createFileAppender(logFileName, true, false);
      LogWriter.getInstance().addAppender(appender);
      log.setLogLevel(parentJob.getLogLevel());
    } catch (Exception e) {
      logError(BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.FailedToOpenLogFile", logFileName, e.toString())); //$NON-NLS-1$
      logError(Const.getStackTracker(e));
    }

    try {
      // Create and connect an AWS service.
      AmazonElasticMapReduceClient emrClient = new AmazonElasticMapReduceClient(awsCredentials);
      AmazonS3 s3Client = new AmazonS3Client(awsCredentials);
      
      // Get bucket name and S3 URL.
      String stagingBucketName = GetBucketName(stagingDir);
      String stagingS3BucketUrl = "s3://" + stagingBucketName; //$NON-NLS-1$

      // Prepare staging S3 URL for Hive script file.
      String stagingS3qUrl = "";
      if (qUrl.startsWith(AmazonSpoonPlugin.S3_SCHEME + "://")) { //$NON-NLS-1$

        // If the .q file is in S3, its staging S3 URL is s3://{bucketname}/{path}
        if (qUrl.indexOf("@s3") > 0) { //$NON-NLS-1$
          stagingS3qUrl = AmazonSpoonPlugin.S3_SCHEME + "://" + qUrl.substring(qUrl.indexOf("@s3") + 4); //$NON-NLS-1$ //$NON-NLS-1$
        } else {
          stagingS3qUrl = qUrl;
        }
        
      } else {
        // A local filename is given for the Hive script file. It should be copied to the S3 Log Directory.
        // First, check for the correct protocol.
        if (!qUrl.startsWith("file:")) { //$NON-NLS-1$
          if (log.isBasic()) logBasic(BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.HiveScriptFilename.Error") + qUrl); //$NON-NLS-1$
        }
        // pull down .q file from VSF
        FileObject qFile = KettleVFS.getFileObject(buildFilename(qUrl));
        File tmpFile = File.createTempFile("customEMR", "q"); //$NON-NLS-1$ //$NON-NLS-1$
        tmpFile.deleteOnExit();
        FileOutputStream tmpFileOut = new FileOutputStream(tmpFile);
        IOUtils.copy(qFile.getContent().getInputStream(), tmpFileOut);
        // Get key name for the script file S3 destination. Key is defined as path name after {bucket}/
        String key = GetKeyFromS3Url(stagingDir);
        if (key == null) {
          key = qFile.getName().getBaseName();
        } else {
          key += "/" + qFile.getName().getBaseName(); //$NON-NLS-1$
        }
        
        // delete the previous .q file in S3
        try {
          s3Client.deleteObject(stagingBucketName, key);
        } catch (Exception ex) {
        }

        // Put .q file in S3 Log Directory.
        s3Client.putObject(new PutObjectRequest(stagingBucketName, key, tmpFile));
        stagingS3qUrl = stagingS3BucketUrl + "/" + key; //$NON-NLS-1$
      }
      
      // AWS provides script-runner.jar (in its public bucket), which should be used as a MapReduce jar for Hive EMR job. 
      jarUrl = "s3://elasticmapreduce/libs/script-runner/script-runner.jar"; //$NON-NLS-1$
      
      RunJobFlowRequest runJobFlowRequest = null;
      RunJobFlowResult runJobFlowResult = null;
      if (StringUtil.isEmpty(hadoopJobFlowId)) {
        // create an EMR job flow, start a step to setup Hive and get the job flow ID.
        runJobFlowRequest = createJobFlow();
        runJobFlowResult = emrClient.runJobFlow(runJobFlowRequest);
        hadoopJobFlowId = runJobFlowResult.getJobFlowId();
      }
      
      // Now EMR job flow is ready to accept a Run Hive Script step.
      // First, prepare a Job Flow ID list.
      List<String> jobFlowIds = new ArrayList<String>();
      jobFlowIds.add(hadoopJobFlowId);

      // Configure a HadoopJarStep.
      String args = "s3://elasticmapreduce/libs/hive/hive-script --base-path s3://elasticmapreduce/libs/hive/ --hive-version 0.7 --run-hive-script --args -f "
                         + environmentSubstitute(stagingS3qUrl) + " " + environmentSubstitute(cmdLineArgs); //$NON-NLS-1$ //$NON-NLS-1$
      List<StepConfig> steps = ConfigHadoopJarStep(hadoopJobName, jarUrl, args);

      // Add a Run Hive Script step to the existing job flow.
      AddJobFlowStepsRequest addJobFlowStepsRequest = new AddJobFlowStepsRequest();
      addJobFlowStepsRequest.setJobFlowId(hadoopJobFlowId);
      addJobFlowStepsRequest.setSteps(steps);
      emrClient.addJobFlowSteps(addJobFlowStepsRequest);
      
      // Set a logging interval.
      String loggingIntervalS = environmentSubstitute(loggingInterval);
      int logIntv = 10;
      try {
        logIntv = Integer.parseInt(loggingIntervalS);
      } catch (NumberFormatException ex) {
        logError(BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.LoggingInterval.Error", loggingIntervalS)); //$NON-NLS-1$
      }

      // monitor and log if intended.
      if (blocking) {
        try {
          if (log.isBasic()) {

            String executionState = "RUNNING";  //$NON-NLS-1$

            while (isRunning(executionState)) {
              DescribeJobFlowsRequest describeJobFlowsRequest = new DescribeJobFlowsRequest();
              describeJobFlowsRequest.setJobFlowIds(jobFlowIds);

              DescribeJobFlowsResult describeJobFlowsResult = emrClient.describeJobFlows(describeJobFlowsRequest);
              boolean found = false;
              for (JobFlowDetail jobFlowDetail : describeJobFlowsResult.getJobFlows()) {
                if (jobFlowDetail.getJobFlowId().equals(hadoopJobFlowId)) {
                  executionState = jobFlowDetail.getExecutionStatusDetail().getState();
                  found = true;
                }
              }

              if (!found) {
                break;
              }
              logBasic(hadoopJobName + " " + BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.JobFlowExecutionStatus", hadoopJobFlowId) + executionState);  //$NON-NLS-1$ //$NON-NLS-1$
              
              if (parentJob.isStopped()) {
                if (!alive) {
                  TerminateJobFlowsRequest terminateJobFlowsRequest = new TerminateJobFlowsRequest();
                  terminateJobFlowsRequest.withJobFlowIds(hadoopJobFlowId);
                  emrClient.terminateJobFlows(terminateJobFlowsRequest);
                }
                break;
              }
              
              try {
                if (isRunning(executionState)) {
                  Thread.sleep(logIntv * 1000);
                }
              } catch (InterruptedException ie) {
                }
            }

            if ("FAILED".equalsIgnoreCase(executionState)) { //$NON-NLS-1$
              result.setStopped(true);
              result.setNrErrors(1);
              result.setResult(false);

              S3Object outObject = s3Client.getObject(stagingBucketName, hadoopJobFlowId + "/steps/1/stdout"); //$NON-NLS-1$
              ByteArrayOutputStream outStream = new ByteArrayOutputStream();
              IOUtils.copy(outObject.getObjectContent(), outStream);
              logError(outStream.toString());

              S3Object errorObject = s3Client.getObject(stagingBucketName, hadoopJobFlowId + "/steps/1/stderr"); //$NON-NLS-1$
              ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
              IOUtils.copy(errorObject.getObjectContent(), errorStream);
              logError(errorStream.toString());
            }
          }
        } catch (Exception e) {
          logError(e.getMessage(), e);
        }
      }

    } 
    catch (Throwable t) {
      t.printStackTrace();
      result.setStopped(true);
      result.setNrErrors(1);
      result.setResult(false);
      logError(t.getMessage(), t);
    }

    if (appender != null) {
      LogWriter.getInstance().removeAppender(appender);
      appender.close();

      ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_LOG, appender.getFile(), parentJob.getJobname(), getName());
      result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
    }

    return result;
  }

  /**
   * Prepare to create a EMR job flow.
   * @return RunJobFlowRequest    The object to request an EMR job flow
   */
  public RunJobFlowRequest createJobFlow() {
    
    // Create a RunJobFlowRequest object, set a name for the job flow.
    RunJobFlowRequest runJobFlowRequest = new RunJobFlowRequest();
    runJobFlowRequest.setName(hadoopJobName);
    
    // Set a log URL.
    String logUrl = stagingDir;
    if (stagingDir.indexOf("@s3") > 0) { //$NON-NLS-1$
      logUrl = AmazonSpoonPlugin.S3_SCHEME + "://" + stagingDir.substring(stagingDir.indexOf("@s3") + 4); //$NON-NLS-1$ //$NON-NLS-1$
    }
    runJobFlowRequest.setLogUri(logUrl);
    
    // Determine the instances for Hadoop cluster.
    String numInstancesS = environmentSubstitute(numInstances);
    int numInsts = 2;
    try {
      numInsts = Integer.parseInt(numInstancesS);
    } catch (NumberFormatException e) {
      logError(BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.InstanceNumber.Error", numInstancesS)); //$NON-NLS-1$
    }
    JobFlowInstancesConfig instances = new JobFlowInstancesConfig();
    instances.setInstanceCount(numInsts);
    instances.setMasterInstanceType(getInstanceType(masterInstanceType));
    instances.setSlaveInstanceType(getInstanceType(slaveInstanceType));
    instances.setHadoopVersion("0.20"); //$NON-NLS-1$
    instances.setKeepJobFlowAliveWhenNoSteps(alive);
    runJobFlowRequest.setInstances(instances);
    
    // Set bootstrap actions.
    runJobFlowRequest.setBootstrapActions(ConfigBootstrapActions());

    // Create an EMR step to setup Hive.
    String args = "s3://elasticmapreduce/libs/hive/hive-script --base-path s3://elasticmapreduce/libs/hive/ --hive-versions 0.7 --install-hive"; //$NON-NLS-1$
    List<StepConfig> steps = ConfigHadoopJarStep("Setup Hive", jarUrl, args); //$NON-NLS-1$
    runJobFlowRequest.setSteps(steps);
    
    return runJobFlowRequest;
  }

  /**
   * Configure the bootstrap actions, which are executed before Hadoop starts.
   * @return List<StepConfig> configuration data for the bootstrap actions
   * 
   */
  public List<BootstrapActionConfig> ConfigBootstrapActions() {

    List<BootstrapActionConfig> bootstrapActionConfigs = new ArrayList<BootstrapActionConfig>();

    if (!StringUtil.isEmpty(bootstrapActions)) {
      
      StringTokenizer st = new StringTokenizer(bootstrapActions, " "); //$NON-NLS-1$
      String path = "";
      String name = "";
      List<String> args = null;
      int actionCount = 0;
      
      while (st.hasMoreTokens()) {
        
        // Take a key/value pair.
        String key = st.nextToken();
        String value = st.nextToken();
        
        // If an argument is enclosed by double quote, take the string without double quote.
        if (value.startsWith("\"")) { //$NON-NLS-1$
          while (!value.endsWith("\"")) { //$NON-NLS-1$
            if (st.hasMoreTokens()) {
              value += " " + st.nextToken(); //$NON-NLS-1$
            } else {
              if (log.isBasic()) logBasic(BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.BootstrapActionArgument.Error", key, value)); //$NON-NLS-1$
              return null;
            }
          }
          value = value.substring(1, value.length() -1);
        }
        
        //if (log.isBasic()) logBasic("adding args: " + key + " " + value);

        if (key.equals("--bootstrap-action")) { //$NON-NLS-1$
          if (path != "") {
            actionCount++;
            if (name.equals("")) name = "Bootstrap Action " + actionCount;
            // Enter data for one bootstrap action.
            BootstrapActionConfig bootstrapActionConfig = ConfigureBootstrapAction(path, name, args);
            bootstrapActionConfigs.add(bootstrapActionConfig);
            name = "";
            args = null;
          }
          if (value.startsWith("s3://")) { //$NON-NLS-1$
            path = value;
          } else { // The value for a bootstrap action does not start with "s3://".
            if (log.isBasic()) logBasic(BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.BootstrapActionPath.Error", key, value)); //$NON-NLS-1$
            return null;
          }
        }
        if (key.equals("--bootstrap-name")) { //$NON-NLS-1$
          name = value;
        }
        if (key.equals("--args")) { //$NON-NLS-1$
          args = ConfigArgs(value, ","); //$NON-NLS-1$
        }  
      }
      
      if (path != "") {
        actionCount++;
        if (name.equals("")) name = "Bootstrap Action " + actionCount; //$NON-NLS-1$ //$NON-NLS-1$
        // Enter data for the last bootstrap action.
        BootstrapActionConfig bootstrapActionConfig = ConfigureBootstrapAction(path, name, args);
        bootstrapActionConfigs.add(bootstrapActionConfig);
      }
    }

    return bootstrapActionConfigs;
  }

  /**
   * Configure a bootstrap action object, given its name, path and arguments.
   * @param path - path for the bootstrap action program in S3
   * @param name - name of the bootstrap action
   * @param args - arguments for the bootstrap action
   * @return configuration data object for one bootstrap action
   * 
   */
  BootstrapActionConfig ConfigureBootstrapAction (String path, String name, List<String> args) {
    
    ScriptBootstrapActionConfig scriptBootstrapActionConfig = new ScriptBootstrapActionConfig();
    BootstrapActionConfig bootstrapActionConfig = new BootstrapActionConfig();
    scriptBootstrapActionConfig.setPath(path);
    scriptBootstrapActionConfig.setArgs(args);
    bootstrapActionConfig.setName(name);
    bootstrapActionConfig.setScriptBootstrapAction(scriptBootstrapActionConfig);
    
    return bootstrapActionConfig;
  }
  
  /**
   * Configure the HadoopJarStep, which is one Hadoop step of an EMR job to be submitted to AWS.
   * @param  stepName         name of step
   * @param  stagingS3JarUrl  URL for MapReduce jar file
   * @param  args             arguments for MapReduce jar
   * @return configuration data object for the step
   * 
   */
  public List<StepConfig> ConfigHadoopJarStep (String stepName, String stagingS3JarUrl, String args) {

    List<String> jarStepArgs = new ArrayList<String>();
    jarStepArgs = ConfigArgs(args, " "); //$NON-NLS-1$

    HadoopJarStepConfig hadoopJarStep = new HadoopJarStepConfig();
    hadoopJarStep.setJar(stagingS3JarUrl);
    hadoopJarStep.setArgs(jarStepArgs);

    StepConfig stepConfig = new StepConfig();
    stepConfig.setName(stepName);
    stepConfig.setHadoopJarStep(hadoopJarStep);
    if (isAlive()) { // Job flow stays in "WAITING" state if this step fails.
      stepConfig.setActionOnFailure("CANCEL_AND_WAIT"); //$NON-NLS-1$
    } else { // Job flow is terminated if this step fails.
      stepConfig.setActionOnFailure("TERMINATE_JOB_FLOW"); //$NON-NLS-1$
    }
    
    List<StepConfig> steps = new ArrayList<StepConfig>();
    steps.add(stepConfig);

    return steps;
  }

  /**
   * Given a unparsed arguments and a separator, print log for each argument and return a list of arguments.
   * @param args - unparsed arguments
   * @param separator - separates one argument from another.
   * @return A list of arguments
   */
  public List<String> ConfigArgs (String args, String separator) {
    
    List<String> argList = new ArrayList<String>();
    if (!StringUtil.isEmpty(args)) {
      StringTokenizer st = new StringTokenizer(args, separator);
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        if (log.isBasic()) logBasic(BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.AddingArgument") + token); //$NON-NLS-1$
        argList.add(token);
      }
    }
    return argList;
  }
  
  /**
   * Get an instance type.
   * @param unparsedInstanceType - unparsed instance type
   * @return A string for the instance type
   */
  public static String getInstanceType(String unparsedInstanceType) {
    return unparsedInstanceType.substring(unparsedInstanceType.lastIndexOf("[") + 1, unparsedInstanceType.lastIndexOf("]")); //$NON-NLS-1$ //$NON-NLS-1$
  }

  /**
   * Determine if the job flow is in a running state.
   * @param state - state of job low
   * @return true if it is not in COMPLETED or FAILED or TERMINATED, and false otherwise.
   */
  public static boolean isRunning(String state) {
    // * <b>Pattern: </b>COMPLETED|FAILED|TERMINATED|RUNNING|SHUTTING_DOWN|STARTING|WAITING|BOOTSTRAPPING<br/>
    if ("COMPLETED".equalsIgnoreCase(state)) { //$NON-NLS-1$
      return false;
    }
    if ("FAILED".equalsIgnoreCase(state)) { //$NON-NLS-1$
      return false;
    }
    if ("TERMINATED".equalsIgnoreCase(state)) { //$NON-NLS-1$
      return false;
    }
    return true;
  }

    
  /**
   * Load attributes
   */
  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException {
    super.loadXML(entrynode, databases, slaveServers);
    hadoopJobName = XMLHandler.getTagValue(entrynode, "hadoop_job_name"); //$NON-NLS-1$
    hadoopJobFlowId = XMLHandler.getTagValue(entrynode, "hadoop_job_flow_id"); //$NON-NLS-1$
    qUrl = XMLHandler.getTagValue(entrynode, "q_url"); //$NON-NLS-1$
    accessKey = Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(entrynode, "access_key")); //$NON-NLS-1$
    secretKey = Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(entrynode, "secret_key")); //$NON-NLS-1$
    bootstrapActions = XMLHandler.getTagValue(entrynode, "bootstrap_actions"); //$NON-NLS-1$
    stagingDir = XMLHandler.getTagValue(entrynode, "staging_dir"); //$NON-NLS-1$
    numInstances = XMLHandler.getTagValue(entrynode, "num_instances"); //$NON-NLS-1$
    masterInstanceType = XMLHandler.getTagValue(entrynode, "master_instance_type"); //$NON-NLS-1$
    slaveInstanceType = XMLHandler.getTagValue(entrynode, "slave_instance_type"); //$NON-NLS-1$
    cmdLineArgs = XMLHandler.getTagValue(entrynode, "command_line_args"); //$NON-NLS-1$
    alive = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "alive")); //$NON-NLS-1$
    blocking = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "blocking")); //$NON-NLS-1$
    loggingInterval = XMLHandler.getTagValue(entrynode, "logging_interval"); //$NON-NLS-1$
  }

  /**
   * Get attributes
   */
  public String getXML() {
    StringBuffer retval = new StringBuffer(1024);
    retval.append(super.getXML());
    retval.append("      ").append(XMLHandler.addTagValue("hadoop_job_name", hadoopJobName)); //$NON-NLS-1$ //$NON-NLS-1$
    retval.append("      ").append(XMLHandler.addTagValue("hadoop_job_flow_id", hadoopJobFlowId)); //$NON-NLS-1$ //$NON-NLS-1$
    retval.append("      ").append(XMLHandler.addTagValue("q_url", qUrl)); //$NON-NLS-1$ //$NON-NLS-1$
    retval.append("      ").append(XMLHandler.addTagValue("access_key", Encr.encryptPasswordIfNotUsingVariables(accessKey))); //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$
    retval.append("      ").append(XMLHandler.addTagValue("secret_key", Encr.encryptPasswordIfNotUsingVariables(secretKey))); //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$
    retval.append("      ").append(XMLHandler.addTagValue("bootstrap_actions", bootstrapActions)); //$NON-NLS-1$ //$NON-NLS-1$
    retval.append("      ").append(XMLHandler.addTagValue("staging_dir", stagingDir)); //$NON-NLS-1$ //$NON-NLS-1$
    retval.append("      ").append(XMLHandler.addTagValue("num_instances", numInstances)); //$NON-NLS-1$ //$NON-NLS-1$
    retval.append("      ").append(XMLHandler.addTagValue("master_instance_type", masterInstanceType)); //$NON-NLS-1$ //$NON-NLS-1$
    retval.append("      ").append(XMLHandler.addTagValue("slave_instance_type", slaveInstanceType)); //$NON-NLS-1$ //$NON-NLS-1$
    retval.append("      ").append(XMLHandler.addTagValue("command_line_args", cmdLineArgs)); //$NON-NLS-1$ //$NON-NLS-1$
    retval.append("      ").append(XMLHandler.addTagValue("alive", alive)); //$NON-NLS-1$ //$NON-NLS-1$
    retval.append("      ").append(XMLHandler.addTagValue("blocking", blocking)); //$NON-NLS-1$ //$NON-NLS-1$
    retval.append("      ").append(XMLHandler.addTagValue("logging_interval", loggingInterval)); //$NON-NLS-1$ //$NON-NLS-1$
    retval.append("      ").append(XMLHandler.addTagValue("hadoop_job_name", hadoopJobName)); //$NON-NLS-1$ //$NON-NLS-1$

    return retval.toString();
  }
  
  /**
   * Load attributes from a repository
   */
  public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
    if (rep != null) {
      super.loadRep(rep, id_jobentry, databases, slaveServers);

      setHadoopJobName(rep.getJobEntryAttributeString(id_jobentry, "hadoop_job_name")); //$NON-NLS-1$
      setHadoopJobFlowId(rep.getJobEntryAttributeString(id_jobentry, "hadoop_job_flow_id")); //$NON-NLS-1$
      setQUrl(rep.getJobEntryAttributeString(id_jobentry, "q_url")); //$NON-NLS-1$
      setAccessKey(Encr.decryptPasswordOptionallyEncrypted(rep.getJobEntryAttributeString(id_jobentry, "access_key"))); //$NON-NLS-1$
      setSecretKey(Encr.decryptPasswordOptionallyEncrypted(rep.getJobEntryAttributeString(id_jobentry, "secret_key"))); //$NON-NLS-1$
      setBootstrapActions(rep.getJobEntryAttributeString(id_jobentry, "bootstrap_actions")); //$NON-NLS-1$
      setStagingDir(rep.getJobEntryAttributeString(id_jobentry, "staging_dir")); //$NON-NLS-1$
      setNumInstances(rep.getJobEntryAttributeString(id_jobentry, "num_instances")); //$NON-NLS-1$
      setMasterInstanceType(rep.getJobEntryAttributeString(id_jobentry, "master_instance_type")); //$NON-NLS-1$
      setSlaveInstanceType(rep.getJobEntryAttributeString(id_jobentry, "slave_instance_type")); //$NON-NLS-1$
      setCmdLineArgs(rep.getJobEntryAttributeString(id_jobentry, "command_line_args")); //$NON-NLS-1$
      setAlive(rep.getJobEntryAttributeBoolean(id_jobentry, "alive")); //$NON-NLS-1$
      setBlocking(rep.getJobEntryAttributeBoolean(id_jobentry, "blocking")); //$NON-NLS-1$
      setLoggingInterval(rep.getJobEntryAttributeString(id_jobentry, "logging_interval")); //$NON-NLS-1$

    } else {
      throw new KettleException(BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.LoadFromRepository.Error")); //$NON-NLS-1$
    }
  }

  /**
   * Save attributes to a repository
   */
  public void saveRep(Repository rep, ObjectId id_job) throws KettleException {
    if (rep != null) {
      super.saveRep(rep, id_job);

      rep.saveJobEntryAttribute(id_job, getObjectId(), "hadoop_job_name", hadoopJobName); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "hadoop_job_flow_id", hadoopJobFlowId); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "q_url", qUrl); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "secret_key", Encr.encryptPasswordIfNotUsingVariables(secretKey)); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "access_key", Encr.encryptPasswordIfNotUsingVariables(accessKey)); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "bootstrap_actions", bootstrapActions); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "staging_dir", stagingDir); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "num_instances", numInstances); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "master_instance_type", masterInstanceType); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "slave_instance_type", slaveInstanceType); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "command_line_args", cmdLineArgs); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "alive", alive); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "blocking", blocking); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "logging_interval", loggingInterval); //$NON-NLS-1$

    } else {
      throw new KettleException(BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.SaveToRepository.Error")); //$NON-NLS-1$
    }
  }

  /**
   * Build S3 URL. Replace "/" and "\" with ASCII equivalents within the access/secret keys,
   *    otherwise VFS will have trouble in parsing the filename.
   * @param filename - S3 URL of a file with access/secret keys in it
   * @return S3 URL with "/" and "\" with ASCII equivalents within the access/secret keys
   */
  public String buildFilename(String filename) {
    filename = environmentSubstitute(filename);
    if (filename.startsWith(AmazonSpoonPlugin.S3_SCHEME)) {
      String authPart = filename.substring(AmazonSpoonPlugin.S3_SCHEME.length() + 3, filename.indexOf("@s3")).replaceAll("\\+", "%2B").replaceAll("/", "%2F"); //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$
      filename = AmazonSpoonPlugin.S3_SCHEME + "://" + authPart + "@s3" + filename.substring(filename.indexOf("@s3") + 3); //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$
    }
    return filename;
  }

  /**
   * Build full S3 URL by inserting the access/secret keys. 
   * Replace "/" and "\" with ASCII equivalents within the access/secret keys,
   *    otherwise VFS will have trouble in parsing the filename.
   */
  public String buildFullS3Url(String filename) {
    if (filename.startsWith(AmazonSpoonPlugin.S3_SCHEME + "://") && !(filename.startsWith(AmazonSpoonPlugin.S3_SCHEME + ":///"))) { //$NON-NLS-1$ //$NON-NLS-1$
      String authPart = accessKey + ":" + secretKey; //$NON-NLS-1$
      authPart = authPart.replaceAll("\\+", "%2B").replaceAll("/", "%2F"); //$NON-NLS-1$ //$NON-NLS-1$
      filename = AmazonSpoonPlugin.S3_SCHEME + "://" + authPart + "@s3" + filename.substring(5); //$NON-NLS-1$ //$NON-NLS-1$
    }
    return filename;
  }

  /**
   * Get a bucket name from S3 URL.
   * @param filename - S3 URL with or without access/secret keys
   * @return a string for bucket name
   */
  public String GetBucketName(String filename) {
    int i = filename.indexOf("@s3/") + 4; // URL with access/secret keys //$NON-NLS-1$
    if (i > 4) {
      int j = filename.indexOf("/", i); //$NON-NLS-1$
      if (i < j) {
        return filename.substring(i, j); // URL ends with file or folder
      } else {
        return filename.substring(i); //URL ends with bucket name itself
      }
    }
    
    // URL without access/secret keys
    i = filename.indexOf("/", 5); //$NON-NLS-1$
    if (i > 5) {
      return filename.substring(5, i);  // URL ends with file or folder
    } else {
      return filename.substring(5); //URL ends with bucket name itself
    }
  }
  
  /**
   * Get a file key from full S3 URL, which is a string after "{bucketname}/".
   * @param filename - S3 URL with access/secret keys
   * @return key, which is a string after "{bucketname}/"
   */
  public String GetKeyFromS3Url(String filename) {
    int i = filename.indexOf("@s3/") + 4; //$NON-NLS-1$
    if (i > 4)
      filename = filename.substring(filename.indexOf("/", i) + 1); //$NON-NLS-1$
    else
      filename = filename.substring(filename.indexOf("/", 5) + 1); //$NON-NLS-1$
    
    return filename;
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isUnconditional() {
    return true;
  }

  /**
   * Get the class name for the dialog box of this plug-in.
   */
  @Override
  public String getDialogClassName() {
    String className = getClass().getCanonicalName();
    className = className.replaceFirst("\\.job\\.", ".ui."); //$NON-NLS-1$ //$NON-NLS-1$
    className += "Dialog"; //$NON-NLS-1$
    return className;
  }
  
}