/*
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.job.entries.hadooptransjobexecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TaskCompletionEvent;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.Log4jFileAppender;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.job.entries.hadoopjobexecutor.UserDefinedItem;
import org.pentaho.hadoop.jobconf.HadoopConfigurer;
import org.pentaho.hadoop.jobconf.HadoopConfigurerFactory;
import org.pentaho.hadoop.mapreduce.GenericTransCombiner;
import org.pentaho.hadoop.mapreduce.GenericTransReduce;
import org.pentaho.hadoop.mapreduce.PentahoMapRunnable;
import org.w3c.dom.Node;

import com.thoughtworks.xstream.XStream;

@JobEntry(id = "HadoopTransJobExecutorPlugin", name = "Pentaho MapReduce", categoryDescription = "Hadoop", description = "Execute Transformation Based Map/Reduce Jobs in Hadoop", image = "HDT.png")
public class JobEntryHadoopTransJobExecutor extends JobEntryBase implements Cloneable, JobEntryInterface {

  private static Class<?> PKG = JobEntryHadoopTransJobExecutor.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$
  
  private String hadoopJobName;

  private String mapRepositoryDir;
  private String mapRepositoryFile;
  private ObjectId mapRepositoryReference;
  private String mapTrans;

  private String combinerRepositoryDir;
  private String combinerRepositoryFile;
  private ObjectId combinerRepositoryReference;
  private String combinerTrans;
  
  private String reduceRepositoryDir;
  private String reduceRepositoryFile;
  private ObjectId reduceRepositoryReference;
  private String reduceTrans;

  private String mapInputStepName;
  private String mapOutputStepName;
  private String combinerInputStepName;
  private String combinerOutputStepName;
  private String reduceInputStepName;
  private String reduceOutputStepName;

  private String outputKeyClass;
  private String outputValueClass;
  private String inputFormatClass;
  private String outputFormatClass;

  private String workingDirectory;

  private String hdfsHostname;
  private String hdfsPort;

  private String jobTrackerHostname;
  private String jobTrackerPort;

  private String inputPath;
  private String outputPath;

  private boolean blocking;
  private int loggingInterval = 60;

  private String numMapTasks = "1";
  private String numReduceTasks = "1";
  
  private String hadoopDistribution = "generic";

  private List<UserDefinedItem> userDefined = new ArrayList<UserDefinedItem>();
  
  public JobEntryHadoopTransJobExecutor() throws Throwable {
  }
  
  public String getHadoopJobName() {
    return hadoopJobName;
  }

  public void setHadoopJobName(String hadoopJobName) {
    this.hadoopJobName = hadoopJobName;
  }

  public String getMapTrans() {
    return mapTrans;
  }

  public void setMapTrans(String mapTrans) {
    this.mapTrans = mapTrans;
  }

  public String getCombinerTrans() {
    return combinerTrans;
  }

  public void setCombinerTrans(String combinerTrans) {
    this.combinerTrans = combinerTrans;
  }
  
  public String getReduceTrans() {
    return reduceTrans;
  }

  public void setReduceTrans(String reduceTrans) {
    this.reduceTrans = reduceTrans;
  }

  public String getMapRepositoryDir() {
    return mapRepositoryDir;
  }

  public void setMapRepositoryDir(String mapRepositoryDir) {
    this.mapRepositoryDir = mapRepositoryDir;
  }

  public String getMapRepositoryFile() {
    return mapRepositoryFile;
  }

  public void setMapRepositoryFile(String mapRepositoryFile) {
    this.mapRepositoryFile = mapRepositoryFile;
  }

  public ObjectId getMapRepositoryReference() {
    return mapRepositoryReference;
  }

  public void setMapRepositoryReference(ObjectId mapRepositoryReference) {
    this.mapRepositoryReference = mapRepositoryReference;
  }
  
  public String getCombinerRepositoryDir() {
    return combinerRepositoryDir;
  }

  public void setCombinerRepositoryDir(String combinerRepositoryDir) {
    this.combinerRepositoryDir = combinerRepositoryDir;
  }

  public String getCombinerRepositoryFile() {
    return combinerRepositoryFile;
  }

  public void setCombinerRepositoryFile(String combinerRepositoryFile) {
    this.combinerRepositoryFile = combinerRepositoryFile;
  }

  public ObjectId getCombinerRepositoryReference() {
    return combinerRepositoryReference;
  }

  public void setCombinerRepositoryReference(ObjectId combinerRepositoryReference) {
    this.combinerRepositoryReference = combinerRepositoryReference;
  }
  
  public String getReduceRepositoryDir() {
    return reduceRepositoryDir;
  }

  public void setReduceRepositoryDir(String reduceRepositoryDir) {
    this.reduceRepositoryDir = reduceRepositoryDir;
  }

  public String getReduceRepositoryFile() {
    return reduceRepositoryFile;
  }

  public void setReduceRepositoryFile(String reduceRepositoryFile) {
    this.reduceRepositoryFile = reduceRepositoryFile;
  }

  public ObjectId getReduceRepositoryReference() {
    return reduceRepositoryReference;
  }

  public void setReduceRepositoryReference(ObjectId reduceRepositoryReference) {
    this.reduceRepositoryReference = reduceRepositoryReference;
  }

  public String getMapInputStepName() {
    return mapInputStepName;
  }

  public void setMapInputStepName(String mapInputStepName) {
    this.mapInputStepName = mapInputStepName;
  }

  public String getMapOutputStepName() {
    return mapOutputStepName;
  }

  public void setMapOutputStepName(String mapOutputStepName) {
    this.mapOutputStepName = mapOutputStepName;
  }
  
  public String getCombinerInputStepName() {
    return combinerInputStepName;
  }

  public void setCombinerInputStepName(String combinerInputStepName) {
    this.combinerInputStepName = combinerInputStepName;
  }

  public String getCombinerOutputStepName() {
    return combinerOutputStepName;
  }

  public void setCombinerOutputStepName(String combinerOutputStepName) {
    this.combinerOutputStepName = combinerOutputStepName;
  }  
  
  public String getReduceInputStepName() {
    return reduceInputStepName;
  }

  public void setReduceInputStepName(String reduceInputStepName) {
    this.reduceInputStepName = reduceInputStepName;
  }

  public String getReduceOutputStepName() {
    return reduceOutputStepName;
  }

  public void setReduceOutputStepName(String reduceOutputStepName) {
    this.reduceOutputStepName = reduceOutputStepName;
  }

  public String getOutputKeyClass() {
    return outputKeyClass;
  }

  public void setOutputKeyClass(String outputKeyClass) {
    this.outputKeyClass = outputKeyClass;
  }

  public String getOutputValueClass() {
    return outputValueClass;
  }

  public void setOutputValueClass(String outputValueClass) {
    this.outputValueClass = outputValueClass;
  }

  public String getInputFormatClass() {
    return inputFormatClass;
  }

  public void setInputFormatClass(String inputFormatClass) {
    this.inputFormatClass = inputFormatClass;
  }

  public String getOutputFormatClass() {
    return outputFormatClass;
  }

  public void setOutputFormatClass(String outputFormatClass) {
    this.outputFormatClass = outputFormatClass;
  }

  public String getWorkingDirectory() {
    return workingDirectory;
  }

  public void setWorkingDirectory(String workingDirectory) {
    this.workingDirectory = workingDirectory;
  }

  public String getHdfsHostname() {
    return hdfsHostname;
  }

  public void setHdfsHostname(String hdfsHostname) {
    this.hdfsHostname = hdfsHostname;
  }

  public String getHdfsPort() {
    return hdfsPort;
  }

  public void setHdfsPort(String hdfsPort) {
    this.hdfsPort = hdfsPort;
  }

  public String getJobTrackerHostname() {
    return jobTrackerHostname;
  }

  public void setJobTrackerHostname(String jobTrackerHostname) {
    this.jobTrackerHostname = jobTrackerHostname;
  }

  public String getJobTrackerPort() {
    return jobTrackerPort;
  }

  public void setJobTrackerPort(String jobTrackerPort) {
    this.jobTrackerPort = jobTrackerPort;
  }

  public String getInputPath() {
    return inputPath;
  }

  public void setInputPath(String inputPath) {
    this.inputPath = inputPath;
  }

  public String getOutputPath() {
    return outputPath;
  }

  public void setOutputPath(String outputPath) {
    this.outputPath = outputPath;
  }

  public boolean isBlocking() {
    return blocking;
  }

  public void setBlocking(boolean blocking) {
    this.blocking = blocking;
  }

  public int getLoggingInterval() {
    return loggingInterval;
  }

  public void setLoggingInterval(int loggingInterval) {
    this.loggingInterval = loggingInterval;
  }

  public List<UserDefinedItem> getUserDefined() {
    return userDefined;
  }

  public void setUserDefined(List<UserDefinedItem> userDefined) {
    this.userDefined = userDefined;
  }

  public String getNumMapTasks() {
    return numMapTasks;
  }

  public void setNumMapTasks(String numMapTasks) {
    this.numMapTasks = numMapTasks;
  }

  public String getNumReduceTasks() {
    return numReduceTasks;
  }

  public void setNumReduceTasks(String numReduceTasks) {
    this.numReduceTasks = numReduceTasks;
  }
  
  public void setHadoopDistribution(String hadoopDistro) {
    hadoopDistribution = hadoopDistro;
  }
  
  public String getHadoopDistribution() {
    return hadoopDistribution;
  }
  
  public Result execute(Result result, int arg1) throws KettleException {
    
    result.setNrErrors(0);

    Log4jFileAppender appender = null;
    String logFileName = "pdi-" + this.getName(); //$NON-NLS-1$

    String hadoopDistro = System.getProperty("hadoop.distribution.name", hadoopDistribution);
    hadoopDistro = environmentSubstitute(hadoopDistro);
    if (Const.isEmpty(hadoopDistro)) {
      hadoopDistro = "generic";
    }        
    
    try
    {
      appender = LogWriter.createFileAppender(logFileName, true, false);
      LogWriter.getInstance().addAppender(appender);
      log.setLogLevel(parentJob.getLogLevel());
    } catch (Exception e)
    {
      logError(BaseMessages.getString(PKG, "JobEntryHadoopTransJobExecutor.FailedToOpenLogFile", logFileName, e.toString())); //$NON-NLS-1$
      logError(Const.getStackTracker(e));
    }    
    
    try {
      ClassLoader loader = getClass().getClassLoader();

      JobConf conf = new JobConf();
      String hadoopJobNameS = environmentSubstitute(hadoopJobName);
      conf.setJobName(hadoopJobNameS);

      // mapper
      TransExecutionConfiguration transExecConfig = new TransExecutionConfiguration();
      TransMeta transMeta = null;
      if(!Const.isEmpty(mapTrans)) {
        String mapTransS = environmentSubstitute(mapTrans);
        transMeta = new TransMeta(mapTransS);
      } else if(mapRepositoryReference != null) {
        if(rep != null) {
          transMeta = rep.loadTransformation(mapRepositoryReference, null);
        }
      } else if(!Const.isEmpty(mapRepositoryDir) && !Const.isEmpty(mapRepositoryFile)){
        if(rep != null) {
          String mapRepositoryDirS = environmentSubstitute(mapRepositoryDir);
          String mapRepositoryFileS = environmentSubstitute(mapRepositoryFile);
          RepositoryDirectoryInterface repositoryDirectory = rep.loadRepositoryDirectoryTree().findDirectory(mapRepositoryDirS);
          transMeta = rep.loadTransformation(mapRepositoryFileS, repositoryDirectory, null, true, null);
        }
      }
      TransConfiguration transConfig = new TransConfiguration(transMeta, transExecConfig);
      String mapInputStepNameS = environmentSubstitute(mapInputStepName);
      String mapOutputStepNameS = environmentSubstitute(mapOutputStepName);
      conf.set("transformation-map-xml", transConfig.getXML()); //$NON-NLS-1$
      conf.set("transformation-map-input-stepname", mapInputStepNameS); //$NON-NLS-1$
      conf.set("transformation-map-output-stepname", mapOutputStepNameS); //$NON-NLS-1$

      // combiner
      transMeta = null;
      if(!Const.isEmpty(combinerTrans)) {
        String combinerTransS = environmentSubstitute(combinerTrans);
        transMeta = new TransMeta(combinerTransS);
      } else if(combinerRepositoryReference != null) {
        if(rep != null) {
          transMeta = rep.loadTransformation(combinerRepositoryReference, null);
        }
      } else if(!Const.isEmpty(combinerRepositoryDir) && !Const.isEmpty(combinerRepositoryFile)){
        if(rep != null) {
          String combinerRepositoryDirS = environmentSubstitute(combinerRepositoryDir);
          String combinerRepositoryFileS = environmentSubstitute(combinerRepositoryFile);
          RepositoryDirectoryInterface repositoryDirectory = rep.loadRepositoryDirectoryTree().findDirectory(combinerRepositoryDirS);
          transMeta = rep.loadTransformation(combinerRepositoryFileS, repositoryDirectory, null, true, null);
        }
      }
      if (transMeta != null) {
        transConfig = new TransConfiguration(transMeta, transExecConfig);
        conf.set("transformation-combiner-xml", transConfig.getXML()); //$NON-NLS-1$
        conf.set("transformation-combiner-input-stepname", combinerInputStepName); //$NON-NLS-1$
        conf.set("transformation-combiner-output-stepname", combinerOutputStepName); //$NON-NLS-1$
        conf.setCombinerClass(GenericTransCombiner.class);
      }
      
      // reducer
      transMeta = null;
      if(!Const.isEmpty(reduceTrans)) {
        String reduceTransS = environmentSubstitute(reduceTrans);
        transMeta = new TransMeta(reduceTransS);
      } else if(reduceRepositoryReference != null) {
        if(rep != null) {
          transMeta = rep.loadTransformation(reduceRepositoryReference, null);
        }
      } else if(!Const.isEmpty(reduceRepositoryDir) && !Const.isEmpty(reduceRepositoryFile)){
        if(rep != null) {
          String reduceRepositoryDirS = environmentSubstitute(reduceRepositoryDir);
          String reduceRepositoryFileS = environmentSubstitute(reduceRepositoryFile);
          RepositoryDirectoryInterface repositoryDirectory = rep.loadRepositoryDirectoryTree().findDirectory(reduceRepositoryDirS);
          transMeta = rep.loadTransformation(reduceRepositoryFileS, repositoryDirectory, null, true, null);
        }
      }
      if (transMeta != null) {
        transConfig = new TransConfiguration(transMeta, transExecConfig);
        conf.set("transformation-reduce-xml", transConfig.getXML()); //$NON-NLS-1$
        conf.set("transformation-reduce-input-stepname", reduceInputStepName); //$NON-NLS-1$
        conf.set("transformation-reduce-output-stepname", reduceOutputStepName); //$NON-NLS-1$
        conf.setReducerClass(GenericTransReduce.class);
      }

      if(outputKeyClass != null) {
        String outputKeyClassS = environmentSubstitute(outputKeyClass);
        conf.setOutputKeyClass(loader.loadClass(outputKeyClassS));
      }
      if(outputValueClass != null) {
        String outputValueClassS = environmentSubstitute(outputValueClass);
        conf.setOutputValueClass(loader.loadClass(outputValueClassS));
      }

      conf.setMapRunnerClass(PentahoMapRunnable.class);

      if(inputFormatClass != null) {
        String inputFormatClassS = environmentSubstitute(inputFormatClass);
        Class<? extends InputFormat> inputFormat = (Class<? extends InputFormat>) loader.loadClass(inputFormatClassS);
        conf.setInputFormat(inputFormat);
      }
      if(outputFormatClass != null) {
        String outputFormatClassS = environmentSubstitute(outputFormatClass);
        Class<? extends OutputFormat> outputFormat = (Class<? extends OutputFormat>) loader.loadClass(outputFormatClassS);
        conf.setOutputFormat(outputFormat);
      }

      String hdfsHostnameS = environmentSubstitute(hdfsHostname);
      String hdfsPortS = environmentSubstitute(hdfsPort);
      String jobTrackerHostnameS = environmentSubstitute(jobTrackerHostname);
      String jobTrackerPortS = environmentSubstitute(jobTrackerPort);
      
      // See if we can auto detect the distribution first
      HadoopConfigurer configurer = HadoopConfigurerFactory.locateConfigurer();
      
      if (configurer == null) {        
        // go with what has been selected by the user
        configurer = HadoopConfigurerFactory.getConfigurer(hadoopDistro);
      }
      if (configurer == null) {
        throw new KettleException(BaseMessages.
            getString(PKG, "JobEntryHadoopTransJobExecutor.Error.UnknownHadoopDistribution", 
                hadoopDistro));
      }
      logBasic(BaseMessages.getString(PKG, "JobEntryHadoopTransJobExecutor.Message.DistroConfigMessage", 
          configurer.distributionName()));
      
      List<String> configMessages = new ArrayList<String>();
      configurer.configure(hdfsHostnameS, hdfsPortS, 
          jobTrackerHostnameS, jobTrackerPortS, conf, configMessages);
      for (String m : configMessages) {
        logBasic(m);
      }            

      String inputPathS = environmentSubstitute(inputPath);
      String[] inputPathParts = inputPathS.split(",");
      List<Path> paths = new ArrayList<Path>();
      for (String path : inputPathParts) {
        paths.add(new Path(configurer.getFilesystemURL() + path));
      }
      Path[] finalPaths = paths.toArray(new Path[paths.size()]);
      
      String outputPathS = environmentSubstitute(outputPath);
      //FileInputFormat.setInputPaths(conf, new Path(configurer.getFilesystemURL() + inputPathS));
      FileInputFormat.setInputPaths(conf, finalPaths);
      FileOutputFormat.setOutputPath(conf, new Path(configurer.getFilesystemURL() + outputPathS));

      // process user defined values
      for (UserDefinedItem item : userDefined) {
        if (item.getName() != null && !"".equals(item.getName()) && item.getValue() != null && !"".equals(item.getValue())) { //$NON-NLS-1$ //$NON-NLS-2$
          String nameS = environmentSubstitute(item.getName());
          String valueS = environmentSubstitute(item.getValue());
          conf.set(nameS, valueS);
        }
      }

      String workingDirectoryS = environmentSubstitute(workingDirectory);
      conf.setWorkingDirectory(new Path(configurer.getFilesystemURL() + workingDirectoryS));
      conf.setJarByClass(PentahoMapRunnable.class);

      conf.setNumMapTasks(Const.toInt(environmentSubstitute(numMapTasks), 1));
      conf.setNumReduceTasks(Const.toInt(environmentSubstitute(numReduceTasks), 1));

      //  get a reference to the variable space
      VariableSpace variableSpace = this.getVariables();
      XStream xStream = new XStream();
      
      //  this is optional - doing it since the 2 minute tutorial does it
      xStream.alias("variableSpace", VariableSpace.class);
      
      //  serialize the variable space to XML
      String xmlVariableSpace = xStream.toXML(variableSpace);
      
      //  set a string in the job configuration as the serialized variablespace
      conf.setStrings("variableSpace", xmlVariableSpace);
      
      //  we now tell the job what level of logging this job is running at
      conf.setStrings("logLevel", this.getLogLevel().toString());
      
      JobClient jobClient = new JobClient(conf);
      RunningJob runningJob = jobClient.submitJob(conf);

      if (blocking) {
        try {
          int taskCompletionEventIndex = 0;
          while (!parentJob.isStopped() && !runningJob.isComplete()) {
            if (loggingInterval >= 1) {
              printJobStatus(runningJob);
              
              TaskCompletionEvent[] tcEvents = runningJob.getTaskCompletionEvents(taskCompletionEventIndex);
              for(int i = 0; i < tcEvents.length; i++) {
                String[] diags = runningJob.getTaskDiagnostics(tcEvents[i].getTaskAttemptId());
                StringBuilder diagsOutput = new StringBuilder();
                
                if(diags != null && diags.length > 0) {
                  diagsOutput.append(Const.CR);
                  for(String s : diags) {
                    diagsOutput.append(s);
                    diagsOutput.append(Const.CR);
                  }
                }
                
                switch(tcEvents[i].getTaskStatus()) {
                  case KILLED: {
                    logError(BaseMessages.getString(PKG, "JobEntryHadoopTransJobExecutor.TaskDetails", TaskCompletionEvent.Status.KILLED, tcEvents[i].getTaskAttemptId().getTaskID().getId(), tcEvents[i].getTaskAttemptId().getId(), tcEvents[i].getEventId(), diagsOutput)); //$NON-NLS-1$
                  }break;
                  case FAILED: {
                    logError(BaseMessages.getString(PKG, "JobEntryHadoopTransJobExecutor.TaskDetails", TaskCompletionEvent.Status.FAILED, tcEvents[i].getTaskAttemptId().getTaskID().getId(), tcEvents[i].getTaskAttemptId().getId(), tcEvents[i].getEventId(), diagsOutput)); //$NON-NLS-1$
                    //result.setResult(false);
                  }break;
                  case SUCCEEDED: {
                    logDetailed(BaseMessages.getString(PKG, "JobEntryHadoopTransJobExecutor.TaskDetails", TaskCompletionEvent.Status.SUCCEEDED, tcEvents[i].getTaskAttemptId().getTaskID().getId(), tcEvents[i].getTaskAttemptId().getId(), tcEvents[i].getEventId(), diagsOutput)); //$NON-NLS-1$
                  }break;
                }
              }
              taskCompletionEventIndex += tcEvents.length;
              
              Thread.sleep(loggingInterval * 1000);
            } else {
              Thread.sleep(60000);
            }
          }

          if (parentJob.isStopped() && !runningJob.isComplete()) {
            // We must stop the job running on Hadoop
            runningJob.killJob();
            // Indicate this job entry did not complete
            result.setResult(false);
          }

          printJobStatus(runningJob);
        } catch (InterruptedException ie) {
          logError(ie.getMessage(), ie);
        }
        
        // Entry is successful if the MR job is successful overall
        result.setResult(runningJob.isSuccessful());
      }

    } catch (Throwable t) {
      t.printStackTrace();
      result.setStopped(true);
      result.setNrErrors(1);
      result.setResult(false);
      logError(t.getMessage(), t);
    }
    
    if (appender != null)
    {
      LogWriter.getInstance().removeAppender(appender);
      appender.close();
      
      ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_LOG, appender.getFile(), parentJob.getJobname(), getName());
      result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
    }
    
    return result;
  }

  public void printJobStatus(RunningJob runningJob) throws IOException {
    if (log.isBasic()) {
      float setupPercent = runningJob.setupProgress() * 100f;
      float mapPercent = runningJob.mapProgress() * 100f;
      float reducePercent = runningJob.reduceProgress() * 100f;
      logBasic(BaseMessages.getString(PKG, "JobEntryHadoopTransJobExecutor.RunningPercent", setupPercent, mapPercent, reducePercent)); //$NON-NLS-1$
    }
  }

  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException {
    super.loadXML(entrynode, databases, slaveServers);
    hadoopJobName = XMLHandler.getTagValue(entrynode, "hadoop_job_name"); //$NON-NLS-1$
    
    if (!Const.isEmpty(XMLHandler.getTagValue(entrynode, "hadoop_distribution"))) {
      hadoopDistribution = XMLHandler.getTagValue(entrynode, "hadoop_distribution"); //$NON-NLS-1$
    }

    mapRepositoryDir = XMLHandler.getTagValue(entrynode, "map_trans_repo_dir"); //$NON-NLS-1$
    mapRepositoryFile = XMLHandler.getTagValue(entrynode, "map_trans_repo_file"); //$NON-NLS-1$
    String mapTransId = XMLHandler.getTagValue(entrynode, "map_trans_repo_reference"); //$NON-NLS-1$
    mapRepositoryReference = Const.isEmpty(mapTransId) ? null : new StringObjectId(mapTransId);
    mapTrans = XMLHandler.getTagValue(entrynode, "map_trans"); //$NON-NLS-1$

    combinerRepositoryDir = XMLHandler.getTagValue(entrynode, "combiner_trans_repo_dir"); //$NON-NLS-1$
    combinerRepositoryFile = XMLHandler.getTagValue(entrynode, "combiner_trans_repo_file"); //$NON-NLS-1$
    String combinerTransId = XMLHandler.getTagValue(entrynode, "combiner_trans_repo_reference"); //$NON-NLS-1$
    combinerRepositoryReference = Const.isEmpty(combinerTransId) ? null : new StringObjectId(combinerTransId);
    combinerTrans = XMLHandler.getTagValue(entrynode, "combiner_trans"); //$NON-NLS-1$
    
    reduceRepositoryDir = XMLHandler.getTagValue(entrynode, "reduce_trans_repo_dir"); //$NON-NLS-1$
    reduceRepositoryFile = XMLHandler.getTagValue(entrynode, "reduce_trans_repo_file"); //$NON-NLS-1$
    String reduceTransId = XMLHandler.getTagValue(entrynode, "reduce_trans_repo_reference"); //$NON-NLS-1$
    reduceRepositoryReference = Const.isEmpty(reduceTransId) ? null : new StringObjectId(reduceTransId);
    reduceTrans = XMLHandler.getTagValue(entrynode, "reduce_trans"); //$NON-NLS-1$

    mapInputStepName = XMLHandler.getTagValue(entrynode, "map_input_step_name"); //$NON-NLS-1$
    mapOutputStepName = XMLHandler.getTagValue(entrynode, "map_output_step_name"); //$NON-NLS-1$
    combinerInputStepName = XMLHandler.getTagValue(entrynode, "combiner_input_step_name"); //$NON-NLS-1$
    combinerOutputStepName = XMLHandler.getTagValue(entrynode, "combiner_output_step_name"); //$NON-NLS-1$
    reduceInputStepName = XMLHandler.getTagValue(entrynode, "reduce_input_step_name"); //$NON-NLS-1$
    reduceOutputStepName = XMLHandler.getTagValue(entrynode, "reduce_output_step_name"); //$NON-NLS-1$

    blocking = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "blocking")); //$NON-NLS-1$ //$NON-NLS-2$
    try {
      loggingInterval = Integer.parseInt(XMLHandler.getTagValue(entrynode, "logging_interval")); //$NON-NLS-1$
    } catch (NumberFormatException nfe) {
    }
    inputPath = XMLHandler.getTagValue(entrynode, "input_path"); //$NON-NLS-1$
    inputFormatClass = XMLHandler.getTagValue(entrynode, "input_format_class"); //$NON-NLS-1$
    outputPath = XMLHandler.getTagValue(entrynode, "output_path"); //$NON-NLS-1$
    outputKeyClass = XMLHandler.getTagValue(entrynode, "output_key_class"); //$NON-NLS-1$
    outputValueClass = XMLHandler.getTagValue(entrynode, "output_value_class"); //$NON-NLS-1$
    outputFormatClass = XMLHandler.getTagValue(entrynode, "output_format_class"); //$NON-NLS-1$

    hdfsHostname = XMLHandler.getTagValue(entrynode, "hdfs_hostname"); //$NON-NLS-1$
    hdfsPort = XMLHandler.getTagValue(entrynode, "hdfs_port"); //$NON-NLS-1$
    jobTrackerHostname = XMLHandler.getTagValue(entrynode, "job_tracker_hostname"); //$NON-NLS-1$
    jobTrackerPort = XMLHandler.getTagValue(entrynode, "job_tracker_port"); //$NON-NLS-1$
    numMapTasks = XMLHandler.getTagValue(entrynode, "num_map_tasks"); //$NON-NLS-1$
    numReduceTasks = XMLHandler.getTagValue(entrynode, "num_reduce_tasks"); //$NON-NLS-1$
    workingDirectory = XMLHandler.getTagValue(entrynode, "working_dir"); //$NON-NLS-1$

    // How many user defined elements?
    userDefined = new ArrayList<UserDefinedItem>();
    Node userDefinedList = XMLHandler.getSubNode(entrynode, "user_defined_list"); //$NON-NLS-1$
    int nrUserDefined = XMLHandler.countNodes(userDefinedList, "user_defined"); //$NON-NLS-1$
    for (int i = 0; i < nrUserDefined; i++) {
      Node userDefinedNode = XMLHandler.getSubNodeByNr(userDefinedList, "user_defined", i); //$NON-NLS-1$
      String name = XMLHandler.getTagValue(userDefinedNode, "name"); //$NON-NLS-1$
      String value = XMLHandler.getTagValue(userDefinedNode, "value"); //$NON-NLS-1$
      UserDefinedItem item = new UserDefinedItem();
      item.setName(name);
      item.setValue(value);
      userDefined.add(item);
    }
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer(1024);
    retval.append(super.getXML());
    retval.append("      ").append(XMLHandler.addTagValue("hadoop_job_name", hadoopJobName)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("hadoop_distribution", hadoopDistribution)); //$NON-NLS-1$ //$NON-NLS-2$
    
    retval.append("      ").append(XMLHandler.addTagValue("map_trans_repo_dir", mapRepositoryDir)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("map_trans_repo_file", mapRepositoryFile)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("map_trans_repo_reference", mapRepositoryReference == null ? null : mapRepositoryReference.toString())); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("map_trans", mapTrans)); //$NON-NLS-1$ //$NON-NLS-2$

    retval.append("      ").append(XMLHandler.addTagValue("combiner_trans_repo_dir", combinerRepositoryDir)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("combiner_trans_repo_file", combinerRepositoryFile)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("combiner_trans_repo_reference", combinerRepositoryReference == null ? null : combinerRepositoryReference.toString())); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("combiner_trans", combinerTrans)); //$NON-NLS-1$ //$NON-NLS-2$
    
    retval.append("      ").append(XMLHandler.addTagValue("reduce_trans_repo_dir", reduceRepositoryDir)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("reduce_trans_repo_file", reduceRepositoryFile)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("reduce_trans_repo_reference", reduceRepositoryReference == null ? null : reduceRepositoryReference.toString())); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("reduce_trans", reduceTrans)); //$NON-NLS-1$ //$NON-NLS-2$

    retval.append("      ").append(XMLHandler.addTagValue("map_input_step_name", mapInputStepName)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("map_output_step_name", mapOutputStepName)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("combiner_input_step_name", combinerInputStepName)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("combiner_output_step_name", combinerOutputStepName)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("reduce_input_step_name", reduceInputStepName)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("reduce_output_step_name", reduceOutputStepName)); //$NON-NLS-1$ //$NON-NLS-2$

    retval.append("      ").append(XMLHandler.addTagValue("blocking", blocking)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("logging_interval", loggingInterval)); //$NON-NLS-1$ //$NON-NLS-2$

    retval.append("      ").append(XMLHandler.addTagValue("input_path", inputPath)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("input_format_class", inputFormatClass)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("output_path", outputPath)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("output_key_class", outputKeyClass)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("output_value_class", outputValueClass)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("output_format_class", outputFormatClass)); //$NON-NLS-1$ //$NON-NLS-2$

    retval.append("      ").append(XMLHandler.addTagValue("hdfs_hostname", hdfsHostname)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("hdfs_port", hdfsPort)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("job_tracker_hostname", jobTrackerHostname)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("job_tracker_port", jobTrackerPort)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("num_map_tasks", numMapTasks)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("num_reduce_tasks", numReduceTasks)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("working_dir", workingDirectory)); //$NON-NLS-1$ //$NON-NLS-2$

    retval.append("      <user_defined_list>").append(Const.CR); //$NON-NLS-1$
    if (userDefined != null) {
      for (UserDefinedItem item : userDefined) {
        if (item.getName() != null && !"".equals(item.getName()) && item.getValue() != null && !"".equals(item.getValue())) { //$NON-NLS-1$ //$NON-NLS-2$
          retval.append("        <user_defined>").append(Const.CR); //$NON-NLS-1$
          retval.append("          ").append(XMLHandler.addTagValue("name", item.getName())); //$NON-NLS-1$ //$NON-NLS-2$
          retval.append("          ").append(XMLHandler.addTagValue("value", item.getValue())); //$NON-NLS-1$ //$NON-NLS-2$
          retval.append("        </user_defined>").append(Const.CR); //$NON-NLS-1$
        }
      }
    }
    retval.append("      </user_defined_list>").append(Const.CR); //$NON-NLS-1$
    return retval.toString();
  }

  public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
    if(rep != null) {
      setHadoopJobName(rep.getJobEntryAttributeString(id_jobentry, "hadoop_job_name")); //$NON-NLS-1$
     
      if (!Const.isEmpty(rep.getJobEntryAttributeString(id_jobentry, "hadoop_distribution"))) {
        setHadoopDistribution(rep.getJobEntryAttributeString(id_jobentry, "hadoop_distribution")); //$NON-NLS-1$
      }
      
      setMapRepositoryDir(rep.getJobEntryAttributeString(id_jobentry, "map_trans_repo_dir")); //$NON-NLS-1$
      setMapRepositoryFile(rep.getJobEntryAttributeString(id_jobentry, "map_trans_repo_file")); //$NON-NLS-1$
      String mapTransId = rep.getJobEntryAttributeString(id_jobentry, "map_trans_repo_reference"); //$NON-NLS-1$
      setMapRepositoryReference(Const.isEmpty(mapTransId) ? null : new StringObjectId(mapTransId));
      setMapTrans(rep.getJobEntryAttributeString(id_jobentry, "map_trans")); //$NON-NLS-1$
      
      setReduceRepositoryDir(rep.getJobEntryAttributeString(id_jobentry, "reduce_trans_repo_dir")); //$NON-NLS-1$
      setReduceRepositoryFile(rep.getJobEntryAttributeString(id_jobentry, "reduce_trans_repo_file")); //$NON-NLS-1$
      String reduceTransId = rep.getJobEntryAttributeString(id_jobentry, "reduce_trans_repo_reference"); //$NON-NLS-1$
      setReduceRepositoryReference(Const.isEmpty(reduceTransId) ? null : new StringObjectId(reduceTransId));
      setReduceTrans(rep.getJobEntryAttributeString(id_jobentry, "reduce_trans")); //$NON-NLS-1$

      setCombinerRepositoryDir(rep.getJobEntryAttributeString(id_jobentry, "combiner_trans_repo_dir")); //$NON-NLS-1$
      setCombinerRepositoryFile(rep.getJobEntryAttributeString(id_jobentry, "combiner_trans_repo_file")); //$NON-NLS-1$
      String combinerTransId = rep.getJobEntryAttributeString(id_jobentry, "combiner_trans_repo_reference"); //$NON-NLS-1$
      setCombinerRepositoryReference(Const.isEmpty(combinerTransId) ? null : new StringObjectId(combinerTransId));
      setCombinerTrans(rep.getJobEntryAttributeString(id_jobentry, "combiner_trans")); //$NON-NLS-1$
      
      setMapInputStepName(rep.getJobEntryAttributeString(id_jobentry, "map_input_step_name")); //$NON-NLS-1$
      setMapOutputStepName(rep.getJobEntryAttributeString(id_jobentry, "map_output_step_name")); //$NON-NLS-1$
      setCombinerInputStepName(rep.getJobEntryAttributeString(id_jobentry, "combiner_input_step_name")); //$NON-NLS-1$
      setCombinerOutputStepName(rep.getJobEntryAttributeString(id_jobentry, "combiner_output_step_name")); //$NON-NLS-1$
      setReduceInputStepName(rep.getJobEntryAttributeString(id_jobentry, "reduce_input_step_name")); //$NON-NLS-1$
      setReduceOutputStepName(rep.getJobEntryAttributeString(id_jobentry, "reduce_output_step_name")); //$NON-NLS-1$

      setBlocking(rep.getJobEntryAttributeBoolean(id_jobentry, "blocking")); //$NON-NLS-1$
      setLoggingInterval(new Long(rep.getJobEntryAttributeInteger(id_jobentry, "logging_interval")).intValue()); //$NON-NLS-1$

      setInputPath(rep.getJobEntryAttributeString(id_jobentry, "input_path")); //$NON-NLS-1$
      setInputFormatClass(rep.getJobEntryAttributeString(id_jobentry, "input_format_class")); //$NON-NLS-1$
      setOutputPath(rep.getJobEntryAttributeString(id_jobentry, "output_path")); //$NON-NLS-1$
      setOutputKeyClass(rep.getJobEntryAttributeString(id_jobentry, "output_key_class")); //$NON-NLS-1$
      setOutputValueClass(rep.getJobEntryAttributeString(id_jobentry, "output_value_class")); //$NON-NLS-1$
      setOutputFormatClass(rep.getJobEntryAttributeString(id_jobentry, "output_format_class")); //$NON-NLS-1$

      setHdfsHostname(rep.getJobEntryAttributeString(id_jobentry, "hdfs_hostname")); //$NON-NLS-1$
      setHdfsPort(rep.getJobEntryAttributeString(id_jobentry, "hdfs_port")); //$NON-NLS-1$
      setJobTrackerHostname(rep.getJobEntryAttributeString(id_jobentry, "job_tracker_hostname")); //$NON-NLS-1$
      setJobTrackerPort(rep.getJobEntryAttributeString(id_jobentry, "job_tracker_port")); //$NON-NLS-1$
      long mapTasks = rep.getJobEntryAttributeInteger(id_jobentry, "num_map_tasks");
      if (mapTasks>0) {
        setNumMapTasks(Long.toString(mapTasks));
      } else {
        setNumMapTasks(rep.getJobEntryAttributeString(id_jobentry, "num_map_tasks"));
      }
      long reduceTasks = rep.getJobEntryAttributeInteger(id_jobentry, "num_reduce_tasks");
      if (reduceTasks>0) {
        setNumReduceTasks(Long.toString(reduceTasks));
      } else {
        setNumReduceTasks(rep.getJobEntryAttributeString(id_jobentry, "num_reduce_tasks"));
      }
      setWorkingDirectory(rep.getJobEntryAttributeString(id_jobentry, "working_dir")); //$NON-NLS-1$

      int argnr = rep.countNrJobEntryAttributes(id_jobentry, "user_defined_name");//$NON-NLS-1$
      if(argnr > 0) {
        userDefined = new ArrayList<UserDefinedItem>();
        
        UserDefinedItem item = null;
        for(int i = 0; i < argnr; i++) {
          item = new UserDefinedItem();
          item.setName(rep.getJobEntryAttributeString(id_jobentry, i,"user_defined_name")); //$NON-NLS-1$
          item.setValue(rep.getJobEntryAttributeString(id_jobentry, i,"user_defined_value")); //$NON-NLS-1$
          userDefined.add(item);
        }
      }

    } else {
      throw new KettleException("Unable to save to a repository. The repository is null."); //$NON-NLS-1$
    }
  }

  public void saveRep(Repository rep, ObjectId id_job) throws KettleException {
    if(rep != null) {
      rep.saveJobEntryAttribute(id_job, getObjectId(),"hadoop_job_name", hadoopJobName); //$NON-NLS-1$
      
      rep.saveJobEntryAttribute(id_job, getObjectId(),"hadoop_distribution", hadoopDistribution); //$NON-NLS-1$
      
      rep.saveJobEntryAttribute(id_job, getObjectId(),"map_trans_repo_dir", mapRepositoryDir); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"map_trans_repo_file", mapRepositoryFile); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"map_trans_repo_reference", mapRepositoryReference == null ? null : mapRepositoryReference.toString()); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"map_trans", mapTrans); //$NON-NLS-1$
      
      rep.saveJobEntryAttribute(id_job, getObjectId(),"reduce_trans_repo_dir", reduceRepositoryDir); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"reduce_trans_repo_file", reduceRepositoryFile); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"reduce_trans_repo_reference", reduceRepositoryReference == null ? null : reduceRepositoryReference.toString()); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"reduce_trans", reduceTrans); //$NON-NLS-1$

      rep.saveJobEntryAttribute(id_job, getObjectId(),"combiner_trans_repo_dir", combinerRepositoryDir); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"combiner_trans_repo_file", combinerRepositoryFile); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"combiner_trans_repo_reference", combinerRepositoryReference == null ? null : combinerRepositoryReference.toString()); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"combiner_trans", combinerTrans); //$NON-NLS-1$
      
      rep.saveJobEntryAttribute(id_job, getObjectId(),"map_input_step_name", mapInputStepName); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"map_output_step_name", mapOutputStepName); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"combiner_input_step_name", combinerInputStepName); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"combiner_output_step_name", combinerOutputStepName); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"reduce_input_step_name", reduceInputStepName); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"reduce_output_step_name", reduceOutputStepName); //$NON-NLS-1$

      rep.saveJobEntryAttribute(id_job, getObjectId(),"blocking", blocking); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"logging_interval", loggingInterval); //$NON-NLS-1$

      rep.saveJobEntryAttribute(id_job, getObjectId(),"input_path", inputPath); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"input_format_class", inputFormatClass); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"output_path", outputPath); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"output_key_class", outputKeyClass); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"output_value_class", outputValueClass); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"output_format_class", outputFormatClass); //$NON-NLS-1$

      rep.saveJobEntryAttribute(id_job, getObjectId(),"hdfs_hostname", hdfsHostname); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"hdfs_port", hdfsPort); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"job_tracker_hostname", jobTrackerHostname); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"job_tracker_port", jobTrackerPort); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"num_map_tasks", numMapTasks); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"num_reduce_tasks", numReduceTasks); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"working_dir", workingDirectory); //$NON-NLS-1$

      if (userDefined != null) {
        for (int i = 0; i < userDefined.size(); i++) {
          UserDefinedItem item = userDefined.get(i);
          if (item.getName() != null && !"".equals(item.getName()) && item.getValue() != null && !"".equals(item.getValue())) { //$NON-NLS-1$ //$NON-NLS-2$
            rep.saveJobEntryAttribute(id_job, getObjectId(), i,"user_defined_name", item.getName()); //$NON-NLS-1$
            rep.saveJobEntryAttribute(id_job, getObjectId(), i,"user_defined_value", item.getValue()); //$NON-NLS-1$
          }
        }
      }

    } else {
      throw new KettleException("Unable to save to a repository. The repository is null."); //$NON-NLS-1$
    }
  }
  
  public boolean evaluates()
  {
    return true;
  }

  public boolean isUnconditional()
  {
    return true;
  }

}
