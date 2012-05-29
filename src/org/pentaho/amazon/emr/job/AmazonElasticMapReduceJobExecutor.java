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

package org.pentaho.amazon.emr.job;

import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.pentaho.amazon.AbstractAmazonJobEntry;
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
import org.pentaho.di.job.entries.hadoopjobexecutor.JarUtility;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

@JobEntry(id = "EMRJobExecutorPlugin", name = "Amazon EMR Job Executor", categoryDescription = "Big Data", description = "Execute MapReduce jobs in Amazon EMR", image = "EMR.png")
public class AmazonElasticMapReduceJobExecutor extends AbstractAmazonJobEntry implements Cloneable, JobEntryInterface {

  private static Class<?> PKG = AmazonElasticMapReduceJobExecutor.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  public AmazonElasticMapReduceJobExecutor() {
  }
  
  public String getMainClass(String localJarUrl) throws Exception {
    List<Class<?>> classesWithMains = JarUtility.getClassesInJarWithMain(localJarUrl, getClass().getClassLoader());

    for (final Class<?> clazz : classesWithMains) {
      try {
        Method mainMethod = clazz.getMethod("main", new Class[] { String[].class });
        if (mainMethod != null) {
          return clazz.getName();
        }
      } catch (Throwable ignored) {
        // skip, try the next one
      }
    }
    throw new RuntimeException("Could not find main class in: " + localJarUrl);
  }

  public Result execute(Result result, int arg1) throws KettleException {
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
      // create/connect aws service
      AmazonElasticMapReduceClient emrClient = new AmazonElasticMapReduceClient(awsCredentials);

      // pull down jar from vfs
      FileObject jarFile = KettleVFS.getFileObject(buildFilename(jarUrl));
      File tmpFile = File.createTempFile("customEMR", "jar");
      tmpFile.deleteOnExit();
      FileOutputStream tmpFileOut = new FileOutputStream(tmpFile);
      IOUtils.copy(jarFile.getContent().getInputStream(), tmpFileOut);
      String localJarUrl = tmpFile.toURI().toURL().toExternalForm();

      // find main class in jar
      String mainClass = getMainClass(localJarUrl);

      // create staging bucket
      AmazonS3 s3Client = new AmazonS3Client(awsCredentials);

      FileSystemOptions opts = new FileSystemOptions();
      DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, new StaticUserAuthenticator(null, awsCredentials.getAWSAccessKeyId(), awsCredentials.getAWSSecretKey()));
      FileObject stagingDirFileObject = KettleVFS.getFileObject(stagingDir, getVariables(), opts);

      String stagingBucketName = stagingDirFileObject.getName().getBaseName();
      if (!s3Client.doesBucketExist(stagingBucketName)) {
        s3Client.createBucket(stagingBucketName);
      }

      // delete old jar if needed
      try {
        s3Client.deleteObject(stagingBucketName, jarFile.getName().getBaseName());
      } catch (Exception ex) {
      }

      // put jar in s3 staging bucket
      s3Client.putObject(new PutObjectRequest(stagingBucketName, jarFile.getName().getBaseName(), tmpFile));
      // create non-vfs s3 url to jar
      String stagingS3JarUrl = "s3://" + stagingBucketName + "/" + jarFile.getName().getBaseName();
      String stagingS3BucketUrl = "s3://" + stagingBucketName;

      RunJobFlowRequest runJobFlowRequest = null;
      RunJobFlowResult runJobFlowResult = null;
      if (StringUtil.isEmpty(hadoopJobFlowId)) {
        // create EMR job flow
        runJobFlowRequest = createJobFlow(stagingS3BucketUrl, stagingS3JarUrl, mainClass);
        // start EMR job
        runJobFlowResult = emrClient.runJobFlow(runJobFlowRequest);
      } else {
        List<String> jarStepArgs = new ArrayList<String>();
        if (!StringUtil.isEmpty(cmdLineArgs)) {
          StringTokenizer st = new StringTokenizer(cmdLineArgs, " ");
          while (st.hasMoreTokens()) {
            String token = st.nextToken();
            logBasic("adding args: " + token);
            jarStepArgs.add(token);
          }
        }

        HadoopJarStepConfig hadoopJarStep = new HadoopJarStepConfig();
        hadoopJarStep.setJar(stagingS3JarUrl);
        hadoopJarStep.setMainClass(mainClass);
        hadoopJarStep.setArgs(jarStepArgs);

        StepConfig stepConfig = new StepConfig();
        stepConfig.setName("custom jar: " + jarUrl);
        stepConfig.setHadoopJarStep(hadoopJarStep);

        List<StepConfig> steps = new ArrayList<StepConfig>();
        steps.add(stepConfig);

        AddJobFlowStepsRequest addJobFlowStepsRequest = new AddJobFlowStepsRequest();
        addJobFlowStepsRequest.setJobFlowId(hadoopJobFlowId);
        addJobFlowStepsRequest.setSteps(steps);

        emrClient.addJobFlowSteps(addJobFlowStepsRequest);
      }
      
      String loggingIntervalS = environmentSubstitute(loggingInterval);
      int logIntv = 60;
      try {
        logIntv = Integer.parseInt(loggingIntervalS);
      } catch (NumberFormatException ex) {
        logError("Unable to parse logging interval '" + loggingIntervalS + "' - using " +
        		"default of 60");
      }

      // monitor it / blocking / logging if desired
      if (blocking) {
        try {
          if (log.isBasic()) {

            String executionState = "RUNNING";

            List<String> jobFlowIds = new ArrayList<String>();
            String id = hadoopJobFlowId;
            if (StringUtil.isEmpty(hadoopJobFlowId)) {
              id = runJobFlowResult.getJobFlowId();
              jobFlowIds.add(id);
            }
            
            while (isRunning(executionState)) {
              DescribeJobFlowsRequest describeJobFlowsRequest = new DescribeJobFlowsRequest();
              describeJobFlowsRequest.setJobFlowIds(jobFlowIds);

              DescribeJobFlowsResult describeJobFlowsResult = emrClient.describeJobFlows(describeJobFlowsRequest);
              boolean found = false;
              for (JobFlowDetail jobFlowDetail : describeJobFlowsResult.getJobFlows()) {
                if (jobFlowDetail.getJobFlowId().equals(id)) {
                  executionState = jobFlowDetail.getExecutionStatusDetail().getState();
                  found = true;
                }
              }

              if (!found) {
                break;
              }
              // logBasic(BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.RunningPercent", setupPercent, mapPercent, reducePercent));
              logBasic(hadoopJobName + " execution status: " + executionState);
              try {
                if (isRunning(executionState)) {
                  Thread.sleep(logIntv * 1000);
                }
              } catch (InterruptedException ie) {
              }
            }

            if ("FAILED".equalsIgnoreCase(executionState)) {
              result.setStopped(true);
              result.setNrErrors(1);
              result.setResult(false);

              S3Object outObject = s3Client.getObject(stagingBucketName, id + "/steps/1/stdout");
              ByteArrayOutputStream outStream = new ByteArrayOutputStream();
              IOUtils.copy(outObject.getObjectContent(), outStream);
              logError(outStream.toString());

              S3Object errorObject = s3Client.getObject(stagingBucketName, id + "/steps/1/stderr");
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

  public RunJobFlowRequest createJobFlow(String stagingS3BucketUrl, String stagingS3Jar, String mainClass) {
    List<String> jarStepArgs = new ArrayList<String>();
    if (!StringUtil.isEmpty(cmdLineArgs)) {
      StringTokenizer st = new StringTokenizer(cmdLineArgs, " ");
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        logBasic("adding args: " + token);
        jarStepArgs.add(token);
      }
    }

    HadoopJarStepConfig hadoopJarStep = new HadoopJarStepConfig();
    hadoopJarStep.setJar(stagingS3Jar);
    hadoopJarStep.setMainClass(mainClass);
    hadoopJarStep.setArgs(jarStepArgs);

    StepConfig stepConfig = new StepConfig();
    stepConfig.setName("custom jar: " + jarUrl);
    stepConfig.setHadoopJarStep(hadoopJarStep);

    List<StepConfig> steps = new ArrayList<StepConfig>();
    steps.add(stepConfig);

    String numInstancesS = environmentSubstitute(numInstances);
    int numInsts = 2;
    try {
      numInsts = Integer.parseInt(numInstancesS);
    } catch (NumberFormatException e) {
      logError("Unable to parse number of instances to use '" + numInstancesS + "' - " +
      		"using 2 instances...");
    }
    JobFlowInstancesConfig instances = new JobFlowInstancesConfig();
    instances.setInstanceCount(numInsts);
    instances.setMasterInstanceType(getInstanceType(masterInstanceType));
    instances.setSlaveInstanceType(getInstanceType(slaveInstanceType));
    instances.setHadoopVersion("0.20");

    RunJobFlowRequest runJobFlowRequest = new RunJobFlowRequest();
    runJobFlowRequest.setSteps(steps);
    runJobFlowRequest.setLogUri(stagingS3BucketUrl);
    runJobFlowRequest.setName(hadoopJobName);
    runJobFlowRequest.setInstances(instances);

    // ScriptBootstrapActionConfig scriptBootstrapAction = new ScriptBootstrapActionConfig();
    // scriptBootstrapAction.setPath("s3://mddwordcount/bootstrap.sh");
    // List<String> bootstrapArgs = new ArrayList<String>();
    // bootstrapArgs.add("http://pdi-node-dist.s3.amazonaws.com");
    // // bootstrapArgs.add("http://ci.pentaho.com/view/Data%20Integration/job/Kettle/lastSuccessfulBuild/artifact/Kettle/");
    // bootstrapArgs.add("pdi-hadoop-node-TRUNK-SNAPSHOT.zip");
    // scriptBootstrapAction.setArgs(bootstrapArgs);
    // BootstrapActionConfig bootstrapActionConfig = new BootstrapActionConfig();
    // bootstrapActionConfig.setName("mdd bootstrap");
    // bootstrapActionConfig.setScriptBootstrapAction(scriptBootstrapAction);
    // List<BootstrapActionConfig> bootstrapActions = new ArrayList<BootstrapActionConfig>();
    // bootstrapActions.add(bootstrapActionConfig);
    // runJobFlowRequest.setBootstrapActions(bootstrapActions);

    return runJobFlowRequest;
  }

  public static String getInstanceType(String unparsedInstanceType) {
    return unparsedInstanceType.substring(unparsedInstanceType.lastIndexOf("[") + 1, unparsedInstanceType.lastIndexOf("]"));
  }

  public static boolean isRunning(String state) {
    // * <b>Pattern: </b>COMPLETED|FAILED|TERMINATED|RUNNING|SHUTTING_DOWN|STARTING|WAITING|BOOTSTRAPPING<br/>
    if ("COMPLETED".equalsIgnoreCase(state)) {
      return false;
    }
    if ("FAILED".equalsIgnoreCase(state)) {
      return false;
    }
    if ("TERMINATED".equalsIgnoreCase(state)) {
      return false;
    }
    return true;
  }

  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException {
    super.loadXML(entrynode, databases, slaveServers);
    hadoopJobName = XMLHandler.getTagValue(entrynode, "hadoop_job_name");
    hadoopJobFlowId = XMLHandler.getTagValue(entrynode, "hadoop_job_flow_id");
    jarUrl = XMLHandler.getTagValue(entrynode, "jar_url");
    accessKey = Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(entrynode, "access_key"));
    secretKey = Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(entrynode, "secret_key"));
    stagingDir = XMLHandler.getTagValue(entrynode, "staging_dir");
    // numInstances = Integer.parseInt(XMLHandler.getTagValue(entrynode, "num_instances"));
    numInstances = XMLHandler.getTagValue(entrynode, "num_instances");
    masterInstanceType = XMLHandler.getTagValue(entrynode, "master_instance_type");
    slaveInstanceType = XMLHandler.getTagValue(entrynode, "slave_instance_type");

    cmdLineArgs = XMLHandler.getTagValue(entrynode, "command_line_args");
    blocking = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "blocking"));
    /*try {
      loggingInterval = Integer.parseInt(XMLHandler.getTagValue(entrynode, "logging_interval"));
    } catch (NumberFormatException nfe) {
    }*/
    loggingInterval = XMLHandler.getTagValue(entrynode, "logging_interval");
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer(1024);
    retval.append(super.getXML());
    retval.append("      ").append(XMLHandler.addTagValue("hadoop_job_name", hadoopJobName));
    retval.append("      ").append(XMLHandler.addTagValue("hadoop_job_flow_id", hadoopJobFlowId));

    retval.append("      ").append(XMLHandler.addTagValue("jar_url", jarUrl));
    retval.append("      ").append(XMLHandler.addTagValue("access_key", Encr.encryptPasswordIfNotUsingVariables(accessKey)));
    retval.append("      ").append(XMLHandler.addTagValue("secret_key", Encr.encryptPasswordIfNotUsingVariables(secretKey)));
    retval.append("      ").append(XMLHandler.addTagValue("staging_dir", stagingDir));
    retval.append("      ").append(XMLHandler.addTagValue("num_instances", numInstances));
    retval.append("      ").append(XMLHandler.addTagValue("master_instance_type", masterInstanceType));
    retval.append("      ").append(XMLHandler.addTagValue("slave_instance_type", slaveInstanceType));
    retval.append("      ").append(XMLHandler.addTagValue("command_line_args", cmdLineArgs));
    retval.append("      ").append(XMLHandler.addTagValue("blocking", blocking));
    retval.append("      ").append(XMLHandler.addTagValue("logging_interval", loggingInterval));
    retval.append("      ").append(XMLHandler.addTagValue("hadoop_job_name", hadoopJobName));

    return retval.toString();
  }

  public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
    if (rep != null) {
      super.loadRep(rep, id_jobentry, databases, slaveServers);

      setHadoopJobName(rep.getJobEntryAttributeString(id_jobentry, "hadoop_job_name"));
      setHadoopJobFlowId(rep.getJobEntryAttributeString(id_jobentry, "hadoop_job_flow_id"));

      setJarUrl(rep.getJobEntryAttributeString(id_jobentry, "jar_url"));
      setAccessKey(Encr.decryptPasswordOptionallyEncrypted(rep.getJobEntryAttributeString(id_jobentry, "access_key")));
      setSecretKey(Encr.decryptPasswordOptionallyEncrypted(rep.getJobEntryAttributeString(id_jobentry, "secret_key")));
      setStagingDir(rep.getJobEntryAttributeString(id_jobentry, "staging_dir"));

      // setNumInstances(new Long(rep.getJobEntryAttributeInteger(id_jobentry, "num_instances")).intValue());
      setNumInstances(rep.getJobEntryAttributeString(id_jobentry, "num_instances"));
      setMasterInstanceType(rep.getJobEntryAttributeString(id_jobentry, "master_instance_type"));
      setSlaveInstanceType(rep.getJobEntryAttributeString(id_jobentry, "slave_instance_type"));

      setCmdLineArgs(rep.getJobEntryAttributeString(id_jobentry, "command_line_args"));
      setBlocking(rep.getJobEntryAttributeBoolean(id_jobentry, "blocking"));
      //setLoggingInterval(new Long(rep.getJobEntryAttributeInteger(id_jobentry, "logging_interval")).intValue());
      setLoggingInterval(rep.getJobEntryAttributeString(id_jobentry, "logging_interval"));

    } else {
      throw new KettleException("Unable to save to a repository. The repository is null."); //$NON-NLS-1$
    }
  }

  public void saveRep(Repository rep, ObjectId id_job) throws KettleException {
    if (rep != null) {
      super.saveRep(rep, id_job);

      rep.saveJobEntryAttribute(id_job, getObjectId(), "hadoop_job_name", hadoopJobName); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "hadoop_job_flow_id", hadoopJobFlowId); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "jar_url", jarUrl); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "secret_key", Encr.encryptPasswordIfNotUsingVariables(secretKey)); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "access_key", Encr.encryptPasswordIfNotUsingVariables(accessKey)); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "staging_dir", stagingDir); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "num_instances", numInstances); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "master_instance_type", masterInstanceType); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "slave_instance_type", slaveInstanceType); //$NON-NLS-1$

      rep.saveJobEntryAttribute(id_job, getObjectId(), "command_line_args", cmdLineArgs); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "blocking", blocking); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "logging_interval", loggingInterval); //$NON-NLS-1$

    } else {
      throw new KettleException("Unable to save to a repository. The repository is null."); //$NON-NLS-1$
    }
  }

  public String buildFilename(String filename) {
    filename = environmentSubstitute(filename);
    return filename;
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isUnconditional() {
    return true;
  }

  @Override
  public String getDialogClassName() {
    String className = getClass().getCanonicalName();
    className = className.replaceFirst("\\.job\\.", ".ui.");
    className += "Dialog";
    return className;
  }

}
