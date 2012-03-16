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

package org.pentaho.di.job.entries.hadooptransjobexecutor;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.vfs.FileObject;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapred.*;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.Log4jFileAppender;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.hadoopexit.HadoopExitMeta;
import org.pentaho.di.ui.job.entries.hadoopjobexecutor.UserDefinedItem;
import org.pentaho.hadoop.PluginPropertiesUtil;
import org.pentaho.hadoop.jobconf.HadoopConfigurer;
import org.pentaho.hadoop.jobconf.HadoopConfigurerFactory;
import org.pentaho.hadoop.mapreduce.*;
import org.pentaho.hadoop.mapreduce.converter.TypeConverterFactory;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@SuppressWarnings("deprecation")
@JobEntry(id = "HadoopTransJobExecutorPlugin", name = "Pentaho MapReduce", categoryDescription = "Big Data", description = "Execute Transformation Based MapReduce Jobs in Hadoop", image = "HDT.png")
public class JobEntryHadoopTransJobExecutor extends JobEntryBase implements Cloneable, JobEntryInterface {

  private static Class<?> PKG = JobEntryHadoopTransJobExecutor.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  private DistributedCacheUtil dcUtil;

  private String hadoopJobName;

  private String mapRepositoryDir;
  private String mapRepositoryFile;
  private ObjectId mapRepositoryReference;
  private String mapTrans;

  private String combinerRepositoryDir;
  private String combinerRepositoryFile;
  private ObjectId combinerRepositoryReference;
  private String combinerTrans;
  private boolean combiningSingleThreaded;
  
  private String reduceRepositoryDir;
  private String reduceRepositoryFile;
  private ObjectId reduceRepositoryReference;
  private String reduceTrans;
  private boolean reducingSingleThreaded;

  private String mapInputStepName;
  private String mapOutputStepName;
  private String combinerInputStepName;
  private String combinerOutputStepName;
  private String reduceInputStepName;
  private String reduceOutputStepName;
  
  private boolean suppressOutputMapKey;
  private boolean suppressOutputMapValue;

  private boolean suppressOutputKey;
  private boolean suppressOutputValue;

  private String inputFormatClass;
  private String outputFormatClass;

  private String workingDirectory;

  private String hdfsHostname;
  private String hdfsPort;

  private String jobTrackerHostname;
  private String jobTrackerPort;

  private String inputPath;
  private String outputPath;

  private boolean cleanOutputPath;

  private boolean blocking;
  private String loggingInterval = "60";

  private String numMapTasks = "1";
  private String numReduceTasks = "1";
  
  private String hadoopDistribution = "generic";

  private List<UserDefinedItem> userDefined = new ArrayList<UserDefinedItem>();
  
  public static final String PENTAHO_MAPREDUCE_PROPERTY_USE_DISTRIBUTED_CACHE = "pmr.use.distributed.cache";
  public static final String PENTAHO_MAPREDUCE_PROPERTY_PMR_LIBRARIES_ARCHIVE_FILE = "pmr.libraries.archive.file";
  public static final String PENTAHO_MAPREDUCE_PROPERTY_KETTLE_HDFS_INSTALL_DIR = "pmr.kettle.dfs.install.dir";
  public static final String PENTAHO_MAPREDUCE_PROPERTY_KETTLE_INSTALLATION_ID = "pmr.kettle.installation.id";
  public static final String PENTAHO_MAPREDUCE_PROPERTY_ADDITIONAL_PLUGINS = "pmr.kettle.additional.plugins";

  public JobEntryHadoopTransJobExecutor() throws Throwable {
    dcUtil = new DistributedCacheUtil();
    reducingSingleThreaded = true;
    combiningSingleThreaded = true;
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
  
  public void setSuppressOutputOfMapKey(boolean suppress) {
    suppressOutputMapKey = suppress;
  }
  
  public boolean getSuppressOutputOfMapKey() {
    return suppressOutputMapKey;
  }  
  
  public void setSuppressOutputOfMapValue(boolean suppress) {
    suppressOutputMapValue = suppress;
  }
  
  public boolean getSuppressOutputOfMapValue() {
    return suppressOutputMapValue;
  }
  
  public void setSuppressOutputOfKey(boolean suppress) {
    suppressOutputKey = suppress;
  }
  
  public boolean getSuppressOutputOfKey() {
    return suppressOutputKey;
  }
  
  public void setSuppressOutputOfValue(boolean suppress) {
    suppressOutputValue = suppress;
  }
  
  public boolean getSuppressOutputOfValue() {
    return suppressOutputValue;
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

  public boolean isCleanOutputPath() {
    return cleanOutputPath;
  }

  public void setCleanOutputPath(boolean cleanOutputPath) {
    this.cleanOutputPath = cleanOutputPath;
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

      try {
        verifyTransMeta(transMeta, mapInputStepNameS, mapOutputStepNameS);
      } catch (Exception ex) {
        throw new KettleException(BaseMessages.getString(PKG, "JobEntryHadoopTransJobExecutor.MapConfiguration.Error"), ex);
      }

      conf.set("transformation-map-xml", transConfig.getXML()); //$NON-NLS-1$
      conf.set("transformation-map-input-stepname", mapInputStepNameS); //$NON-NLS-1$
      conf.set("transformation-map-output-stepname", mapOutputStepNameS); //$NON-NLS-1$
      
      conf.set(PentahoMapReduceBase.STRING_COMBINE_SINGLE_THREADED, combiningSingleThreaded ? "true" : "false");
      
      // Pass the single threaded reduction to the configuration...
      //
      conf.set(PentahoMapReduceBase.STRING_REDUCE_SINGLE_THREADED, reducingSingleThreaded ? "true" : "false");
      
      if (getSuppressOutputOfMapKey()) {
        conf.setMapOutputKeyClass(NullWritable.class);
      }
      if (getSuppressOutputOfMapValue()) {
        conf.setMapOutputValueClass(NullWritable.class);
      }
      
      // auto configure the output mapper key and value classes
      if (!getSuppressOutputOfMapKey() || !getSuppressOutputOfMapValue() && 
          transMeta != null) {
        StepMeta mapOut = transMeta.findStep(mapOutputStepNameS);
        RowMetaInterface prevStepFields = transMeta.getPrevStepFields(mapOut);
        if (mapOut.getStepMetaInterface() instanceof HadoopExitMeta) {
          String keyName = ((HadoopExitMeta)mapOut.getStepMetaInterface()).getOutKeyFieldname();
          String valName = ((HadoopExitMeta)mapOut.getStepMetaInterface()).getOutValueFieldname();
          int keyI = prevStepFields.indexOfValue(keyName);
          ValueMetaInterface keyVM = (keyI >= 0) ? prevStepFields.getValueMeta(keyI) : null;
          int valI = prevStepFields.indexOfValue(valName);
          ValueMetaInterface valueVM = (valI >= 0) ? prevStepFields.getValueMeta(valI) : null;
          if (!getSuppressOutputOfMapKey()) {
            if (keyVM == null) {
              throw new KettleException(BaseMessages.getString(PKG, 
                  "JobEntryHadoopTransJobExecutor.NoMapOutputKeyDefined.Error"));
            }
            Class<?> hadoopWritableKey = TypeConverterFactory.getWritableForKettleType(keyVM);
            conf.setMapOutputKeyClass(hadoopWritableKey);
            logDebug(BaseMessages.getString(PKG, 
                "JobEntryHadoopTransJobExecutor.Message.MapOutputKeyMessage", 
                hadoopWritableKey.getName()));
          }
          
          if (!getSuppressOutputOfMapValue()) {
            if (valueVM == null) {
              throw new KettleException(BaseMessages.getString(PKG, 
                "JobEntryHadoopTransJobExecutor.NoMapOutputValueDefined.Error"));
            }
            Class<?> hadoopWritableValue = TypeConverterFactory.getWritableForKettleType(valueVM);
            conf.setMapOutputValueClass(hadoopWritableValue);
            logDebug(BaseMessages.getString(PKG, 
                "JobEntryHadoopTransJobExecutor.Message.MapOutputValueMessage", 
                hadoopWritableValue.getName()));
          }
        }
      }

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
        
        if (combiningSingleThreaded) {
          verifySingleThreadingValidity(transMeta);
        }

        transConfig = new TransConfiguration(transMeta, transExecConfig);
        conf.set("transformation-combiner-xml", transConfig.getXML()); //$NON-NLS-1$
        String combinerInputStepNameS = environmentSubstitute(combinerInputStepName);
        String combinerOutputStepNameS = environmentSubstitute(combinerOutputStepName);
        conf.set("transformation-combiner-input-stepname", combinerInputStepNameS); //$NON-NLS-1$
        conf.set("transformation-combiner-output-stepname", combinerOutputStepNameS); //$NON-NLS-1$
        conf.setCombinerClass(GenericTransCombiner.class);

        try {
          verifyTransMeta(transMeta, combinerInputStepNameS, combinerOutputStepNameS);
        } catch (Exception ex) {
          throw new KettleException(BaseMessages.getString(PKG, "JobEntryHadoopTransJobExecutor.CombinerConfiguration.Error"), ex);
        }
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

        // See if this is a valid single threading reducer
        //
        if (reducingSingleThreaded) {
          verifySingleThreadingValidity(transMeta);
        }

        transConfig = new TransConfiguration(transMeta, transExecConfig);
        conf.set("transformation-reduce-xml", transConfig.getXML()); //$NON-NLS-1$
        String reduceInputStepNameS = environmentSubstitute(reduceInputStepName);
        String reduceOutputStepNameS = environmentSubstitute(reduceOutputStepName);
        conf.set("transformation-reduce-input-stepname", reduceInputStepNameS); //$NON-NLS-1$
        conf.set("transformation-reduce-output-stepname", reduceOutputStepNameS); //$NON-NLS-1$
        conf.setReducerClass(GenericTransReduce.class);

        try {
          verifyTransMeta(transMeta, reduceInputStepNameS, reduceOutputStepNameS);
        } catch (Exception ex) {
          throw new KettleException(BaseMessages.getString(PKG, "JobEntryHadoopTransJobExecutor.ReducerConfiguration.Error"), ex);
        }
        
        if (getSuppressOutputOfKey()) {
          conf.setOutputKeyClass(NullWritable.class);
        }
        if (getSuppressOutputOfValue()) {
          conf.setOutputValueClass(NullWritable.class);
        }
        
        // auto configure the output reduce key and value classes
        if (!getSuppressOutputOfKey() || !getSuppressOutputOfValue()) {
          StepMeta reduceOut = transMeta.findStep(reduceOutputStepNameS);
          RowMetaInterface prevStepFields = transMeta.getPrevStepFields(reduceOut);
          if (reduceOut.getStepMetaInterface() instanceof HadoopExitMeta) {
            String keyName = ((HadoopExitMeta)reduceOut.getStepMetaInterface()).getOutKeyFieldname();
            String valName = ((HadoopExitMeta)reduceOut.getStepMetaInterface()).getOutValueFieldname();
            int keyI = prevStepFields.indexOfValue(keyName);
            ValueMetaInterface keyVM = (keyI >= 0) ? prevStepFields.getValueMeta(keyI) : null;
            int valI = prevStepFields.indexOfValue(valName);
            ValueMetaInterface valueVM = (valI >= 0) ? prevStepFields.getValueMeta(valI) : null;
           
            if (!getSuppressOutputOfKey()) {
              if (keyVM == null) {
                throw new KettleException(BaseMessages.getString(PKG, 
                    "JobEntryHadoopTransJobExecutor.NoOutputKeyDefined.Error"));
              }
              Class<?> hadoopWritableKey = TypeConverterFactory.getWritableForKettleType(keyVM);
              conf.setOutputKeyClass(hadoopWritableKey);
              logDebug(BaseMessages.getString(PKG, 
                  "JobEntryHadoopTransJobExecutor.Message.OutputKeyMessage", 
                  hadoopWritableKey.getName()));
            }
           
            if (!getSuppressOutputOfValue()) {
              if (valueVM == null) {
                throw new KettleException(BaseMessages.getString(PKG, 
                  "JobEntryHadoopTransJobExecutor.NoOutputValueDefined.Error"));
              }
              Class<?> hadoopWritableValue = TypeConverterFactory.getWritableForKettleType(valueVM);
              conf.setOutputValueClass(hadoopWritableValue);
              logDebug(BaseMessages.getString(PKG, 
                  "JobEntryHadoopTransJobExecutor.Message.OutputValueMessage", 
                  hadoopWritableValue.getName()));
            }
            
          }
        }
      }            

      conf.setMapRunnerClass(PentahoMapRunnable.class);

      if(inputFormatClass != null) {
        String inputFormatClassS = environmentSubstitute(inputFormatClass);
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Class<? extends InputFormat> inputFormat = (Class<? extends InputFormat>) loader.loadClass(inputFormatClassS);
        conf.setInputFormat(inputFormat);
      }
      if(outputFormatClass != null) {
        String outputFormatClassS = environmentSubstitute(outputFormatClass);
        @SuppressWarnings({ "rawtypes", "unchecked" })
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
        
        // if the user-specified distribution is detectable, make sure it is still
        // the current distribution!
        if (configurer != null && configurer.isDetectable()) {
          if (!configurer.isAvailable()) {
            throw new KettleException(BaseMessages.getString(PKG, 
                "JobEntryHadoopTransJobExecutor.Error.DistroNoLongerPresent", configurer.distributionName()));
          }
        }
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

      final Path outputPathPath = new Path(configurer.getFilesystemURL() + environmentSubstitute(outputPath));
      FileInputFormat.setInputPaths(conf, finalPaths);
      FileOutputFormat.setOutputPath(conf, outputPathPath);

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
      
      String numMapTasksS = environmentSubstitute(numMapTasks);
      try {
        if (Integer.parseInt(numMapTasksS) < 0) {
          throw new KettleException(BaseMessages.getString(PKG, 
              "JobEntryHadoopTransJobExecutor.NumMapTasks.Error"));
        }
      } catch (NumberFormatException e) { }
      
      String numReduceTasksS = environmentSubstitute(numReduceTasks);
      try {
        if (Integer.parseInt(numReduceTasksS) < 0) {
          throw new KettleException(BaseMessages.getString(PKG, 
              "JobEntryHadoopTransJobExecutor.NumReduceTasks.Error"));
        }
      } catch (NumberFormatException e) { }

      conf.setNumMapTasks(Const.toInt(numMapTasksS, 1));
      conf.setNumReduceTasks(Const.toInt(numReduceTasksS, 1));

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

      FileSystem fs = FileSystem.get(conf);

      if(isCleanOutputPath()) {
        if (log.isBasic()) {
          logBasic(BaseMessages.getString(PKG, "JobEntryHadoopTransJobExecutor.CleaningOutputPath", outputPathPath.toUri().toString()));
        }
        try {
          if (!cleanOutputPath(fs, outputPathPath)) {
            logBasic(BaseMessages.getString(PKG, "JobEntryHadoopTransJobExecutor.FailedToCleanOutputPath", outputPathPath.toUri().toString()));
          }
        } catch (IOException ex) {
          result.setStopped(true);
          result.setNrErrors(1);
          result.setResult(false);
          logError(BaseMessages.getString(PKG, "JobEntryHadoopTransJobExecutor.ErrorCleaningOutputPath", outputPathPath.toUri().toString()), ex);
          return result;
        }
      }

      Properties pmrProperties = loadPMRProperties();
      // Only configure our job to use the Distributed Cache if the pentaho-mapreduce
      if (useDistributedCache(conf, pmrProperties)) {
        String installPath = getProperty(conf, pmrProperties, PENTAHO_MAPREDUCE_PROPERTY_KETTLE_HDFS_INSTALL_DIR, null);
        String installId = getProperty(conf, pmrProperties, PENTAHO_MAPREDUCE_PROPERTY_KETTLE_INSTALLATION_ID, Const.VERSION);
        try {
          if (Const.isEmpty(installPath)) {
            throw new IllegalArgumentException(BaseMessages.getString(PKG, "JobEntryHadoopTransJobExecutor.KettleHdfsInstallDirMissing"));
          }
          if (Const.isEmpty(installId)) {
            installId = Const.VERSION;
          }
          if (!installPath.endsWith(Const.FILE_SEPARATOR)) {
            installPath += Const.FILE_SEPARATOR;
          }
          Path kettleEnvInstallDir = new Path(installPath + installId);
          PluginInterface plugin = getPluginInterface();
          FileObject pmrLibArchive = KettleVFS.getFileObject(plugin.getPluginDirectory().getPath() + Const.FILE_SEPARATOR + getProperty(conf, pmrProperties, PENTAHO_MAPREDUCE_PROPERTY_PMR_LIBRARIES_ARCHIVE_FILE, null));
          // Make sure the version we're attempting to use is installed
          if (dcUtil.isKettleEnvironmentInstalledAt(fs, kettleEnvInstallDir)) {
            logDetailed(BaseMessages.getString(PKG, "JobEntryHadoopTransJobExecutor.UsingKettleInstallationFrom", kettleEnvInstallDir.toUri().getPath()));
          } else {
            // Load additional plugin folders as requested
            List<FileObject> additionalPluginFolders = findAdditionalPluginFolders(conf, pmrProperties);
            installKettleEnvironment(pmrLibArchive, fs, kettleEnvInstallDir, additionalPluginFolders);
          }
          configureWithKettleEnvironment(conf, fs, kettleEnvInstallDir);
        } catch (Exception ex) {
          result.setStopped(true);
          result.setNrErrors(1);
          result.setResult(false);
          logError(BaseMessages.getString(PKG, "JobEntryHadoopTransJobExecutor.InstallationOfKettleFailed"), ex);
          return result;
        }
      }
      
      JobClient jobClient = new JobClient(conf);
      RunningJob runningJob = jobClient.submitJob(conf);
      
      String loggingIntervalS = environmentSubstitute(loggingInterval);
      int logIntv = 60;
      try {
        logIntv = Integer.parseInt(loggingIntervalS);
      } catch (NumberFormatException e) {
        logError("Can't parse logging interval '" + loggingIntervalS + "'. Setting " +
          "logging interval to 60");
      }

      if (blocking) {
        try {
          int taskCompletionEventIndex = 0;
          while (!parentJob.isStopped() && !runningJob.isComplete()) {
            if (logIntv >= 1) {
              printJobStatus(runningJob);
              taskCompletionEventIndex += logTaskMessages(runningJob, taskCompletionEventIndex);
              Thread.sleep(logIntv * 1000);
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
          // Log any messages we may have missed while polling
          logTaskMessages(runningJob, taskCompletionEventIndex);
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

  /**
   * Log messages indicating completion (success/failure) of component tasks for the provided running job.
   *
   * @param runningJob Running job to poll for completion events
   * @param startIndex Start at this event index to poll from
   * @return Total events consumed
   * @throws IOException Error fetching events
   */
  private int logTaskMessages(RunningJob runningJob, int startIndex) throws IOException {
    TaskCompletionEvent[] tcEvents = runningJob.getTaskCompletionEvents(startIndex);
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
        }break;
        case SUCCEEDED: {
          logDetailed(BaseMessages.getString(PKG, "JobEntryHadoopTransJobExecutor.TaskDetails", TaskCompletionEvent.Status.SUCCEEDED, tcEvents[i].getTaskAttemptId().getTaskID().getId(), tcEvents[i].getTaskAttemptId().getId(), tcEvents[i].getEventId(), diagsOutput)); //$NON-NLS-1$
        }break;
      }
    }
    return tcEvents.length;
  }

  /**
   * Should the DistributedCache be used for this job execution?
   *
   * @param conf Configuration to check for the property
   * @param pmrProperties Properties to check for the property
   * @return {@code true} if either {@code conf} or {@code pmrProperties} contains {@code PENTAHO_MAPREDUCE_PROPERTY_USE_DISTRIBUTED_CACHE}
   */
  public boolean useDistributedCache(JobConf conf, Properties pmrProperties) {
    return Boolean.parseBoolean(getProperty(conf, pmrProperties, PENTAHO_MAPREDUCE_PROPERTY_USE_DISTRIBUTED_CACHE, Boolean.toString(true)));
  }

  /**
   * Find plugin folders declared in the property {@code PENTAHO_MAPREDUCE_PROPERTY_ADDITIONAL_PLUGINS} to be included
   * as part of the Kettle Environment copied into DFS for use with Pentaho MapReduce.
   *
   * @param conf Configuration to check for the property
   * @param pmrProperties Properties to check for the property if not found in {@code conf}
   * @return List of file objects that point to the plugin directories that are declared as "additional required plugins"
   * for the Kettle Environment Pentaho MapReduce will run within.
   * @throws KettleFileException
   */
  public List<FileObject> findAdditionalPluginFolders(JobConf conf, Properties pmrProperties) throws KettleFileException {
    List<FileObject> additionalPluginFolders = new ArrayList<FileObject>();
    String additionalPluginNames = getProperty(conf, pmrProperties, PENTAHO_MAPREDUCE_PROPERTY_ADDITIONAL_PLUGINS, null);
    if (!Const.isEmpty(additionalPluginNames)) {
      for(String pluginName : additionalPluginNames.split(",")) {
        pluginName = pluginName.trim();
        if (!Const.isEmpty(pluginName)) {
          FileObject pluginFolder = dcUtil.findPluginFolder(pluginName);
          if (pluginFolder != null) {
            additionalPluginFolders.add(pluginFolder);
          }
        }
      }
    }
    return additionalPluginFolders;
  }

  /**
   * Gets a property from the configuration. If it is missing it will load it from the properties provided. If it cannot 
   * be found there the default value provided will be used.
   *
   * @param conf Configuration to check for property first.
   * @param properties Properties to check for property second.
   * @param propertyName Name of the property to return
   * @param defaultValue Default value to use if no property by the given name could be found in {@code conf} or {@code properties}
   * @return Value of {@code propertyName}
   */
  public String getProperty(JobConf conf, Properties properties, String propertyName, String defaultValue) {
    String fromConf = conf.get(propertyName);
    return !Const.isEmpty(fromConf) ? fromConf : properties.getProperty(propertyName, defaultValue);
  }

  /**
   * @return The pentaho-mapreduce.properties from the plugin installation directory
   * @throws KettleFileException
   * @throws IOException
   */
  public Properties loadPMRProperties() throws KettleFileException, IOException {
    PluginInterface plugin = getPluginInterface();
    return new PluginPropertiesUtil().loadPluginProperties(plugin, "pentaho-mapreduce.properties");
  }

  /**
   * @return the plugin interface for this job entry.
   */
  public PluginInterface getPluginInterface() {
    String pluginId = PluginRegistry.getInstance().getPluginId(this);
    return PluginRegistry.getInstance().findPluginWithId(JobEntryPluginType.class, pluginId);
  }

  /**
   * Install the Kettle environment, packaged in {@code pmrLibArchive} into the destination within the file systme provided.
   *
   * @param pmrLibArchive Archive that contains the libraries required to run Pentaho MapReduce (Kettle's dependencies)
   * @param fs File system to install the Kettle environment into
   * @param destination Destination path within {@code fs} to install into
   * @param additionalPlugins Any additional plugin directories to copy into the installation
   * @throws KettleException
   * @throws IOException
   */
  public void installKettleEnvironment(FileObject pmrLibArchive, FileSystem fs, Path destination, List<FileObject> additionalPlugins) throws KettleException, IOException {
    if (pmrLibArchive == null) {
      throw new KettleException(BaseMessages.getString(PKG, "JobEntryHadoopTransJobExecutor.UnableToLocateArchive", pmrLibArchive));
    }

    logBasic(BaseMessages.getString(PKG, "JobEntryHadoopTransJobExecutor.InstallingKettleAt", destination));

    FileObject bigDataPluginFolder = KettleVFS.getFileObject(getPluginInterface().getPluginDirectory().getPath());
    dcUtil.installKettleEnvironment(pmrLibArchive, fs, destination, bigDataPluginFolder, additionalPlugins);
    
    logBasic(BaseMessages.getString(PKG, "JobEntryHadoopTransJobExecutor.InstallationOfKettleSuccessful", destination));
  }

  /**
   * Configure the provided configuration to use the Distributed Cache backed by the Kettle Environment installed at the
   * installation directory provided.
   *
   * @param conf Configuration to update
   * @param fs File system that contains the Kettle environment to use
   * @param kettleEnvInstallDir Kettle environment installation path
   * @throws IOException
   * @throws KettleException
   */
  private void configureWithKettleEnvironment(JobConf conf, FileSystem fs, Path kettleEnvInstallDir) throws IOException, KettleException {
    if (!dcUtil.isKettleEnvironmentInstalledAt(fs, kettleEnvInstallDir)) {
      throw new KettleException(BaseMessages.getString(PKG, "JobEntryHadoopTransJobExecutor.KettleInstallationMissingFrom", kettleEnvInstallDir.toUri().getPath()));
    }

    logBasic(BaseMessages.getString(PKG, "JobEntryHadoopTransJobExecutor.ConfiguringJobWithKettleAt", kettleEnvInstallDir.toUri().getPath()));
    dcUtil.configureWithKettleEnvironment(conf, fs, kettleEnvInstallDir);
  }

  /**
   * Verify the validity of a transformation to be used in Pentaho MapReduce.
   * 1)
   * @param transMeta
   * @param inputStepName Name of the input step to be passed data from the {@link org.apache.hadoop.mapred.RecordReader}
   * @param outputStepName Name of output step to listen for output and pass to the {@link org.apache.hadoop.mapred.OutputCollector}
   * @throws KettleException
   */
  public static void verifyTransMeta(TransMeta transMeta, String inputStepName, String outputStepName) throws KettleException {

    // Verify the input step: see that the key/value fields are present...
    //
    if (Const.isEmpty(inputStepName)) {
      throw new KettleException("The input step was not specified");
    }
    StepMeta inputStepMeta = transMeta.findStep(inputStepName);
    if (inputStepMeta==null) {
      throw new KettleException("The input step with name '"+inputStepName+"' could not be found");
    }    
    
    // Get the fields coming out of the input step...
    //
    RowMetaInterface injectorRowMeta = transMeta.getStepFields(inputStepMeta);
    
    // Verify that the key and value fields are found
    //
    PentahoMapReduceBase.InKeyValueOrdinals inOrdinals = new PentahoMapReduceBase.InKeyValueOrdinals(injectorRowMeta);
    if(inOrdinals.getKeyOrdinal() < 0 || inOrdinals.getValueOrdinal() < 0) {
      throw new KettleException("key or value is not defined in input step");
    }
    
    // make sure that the input step is enabled (i.e. its outgoing hop
    // hasn't been disabled)
    Trans t = new Trans(transMeta);
    t.prepareExecution(null);
    if (t.getStepInterface(inputStepName, 0) == null) {
      throw new KettleException("Input step '" + inputStepName 
          + "' does not seem to be enabled in the map "
          + "transformation.");
    }
    
    // Now verify the output step output of the reducer...
    //
    if (Const.isEmpty(outputStepName)) {
      throw new KettleException("The output step was not specified");
    }

    StepMeta outputStepMeta = transMeta.findStep(outputStepName);
    if (outputStepMeta==null) {
      throw new KettleException("The output step with name '"+outputStepName+"' could not be found");
    }
    
    // It's a special step designed to map the output key/value pair fields...
    //
    if (outputStepMeta.getStepMetaInterface() instanceof HadoopExitMeta) {
      // Get the row fields entering the output step...
      //
      RowMetaInterface outputRowMeta = transMeta.getPrevStepFields(outputStepMeta);
      HadoopExitMeta exitMeta = (HadoopExitMeta) outputStepMeta.getStepMetaInterface();
      
      List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>();
      exitMeta.check(remarks, transMeta, outputStepMeta, outputRowMeta, null, null, null);
      StringBuilder message = new StringBuilder();
      for (CheckResultInterface remark : remarks) {
        if (remark.getType() == CheckResultInterface.TYPE_RESULT_ERROR) {
          message.append(message.toString()).append(Const.CR);
        }
      }
      if (message.length()>0) {
        throw new KettleException("There was a validation error with the Hadoop Output step:"+Const.CR+message);
      }
    } else {
      // Any other step: verify that the outKey and outValue fields exist...
      //
      RowMetaInterface outputRowMeta = transMeta.getStepFields(outputStepMeta);
      PentahoMapReduceBase.OutKeyValueOrdinals outOrdinals = new PentahoMapReduceBase.OutKeyValueOrdinals(outputRowMeta);
      if (outOrdinals.getKeyOrdinal() < 0 || outOrdinals.getValueOrdinal() < 0) {
        throw new KettleException("outKey or outValue is not defined in output stream"); //$NON-NLS-1$
      }
    }
    

  }

  private void verifySingleThreadingValidity(TransMeta transMeta) throws KettleException {
    for (StepMeta stepMeta : transMeta.getSteps()) {
      TransformationType[] types = stepMeta.getStepMetaInterface().getSupportedTransformationTypes();
      boolean ok = false;
      for (TransformationType type : types) {
        if (type == TransformationType.SingleThreaded) ok = true; 
      }
      if (!ok) {
        throw new KettleException("Step '"+stepMeta.getName()+"' of type '"+stepMeta.getStepID()+"' is not supported in a Single Threaded transformation engine.");
      }
    }
  }

  private boolean cleanOutputPath(FileSystem fs, Path path) throws IOException {
    if (!fs.exists(path)) {
      // If the path does not exist one could think of it as "already cleaned"
      return true;
    }
    return fs.delete(path, true);
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
    final String combinerSingleThreaded = XMLHandler.getTagValue(entrynode, "combiner_single_threaded");  //$NON-NLS-1$
    if (!Const.isEmpty(combinerSingleThreaded)) {
      setCombiningSingleThreaded("Y".equalsIgnoreCase(combinerSingleThreaded));  //$NON-NLS-1$
    }
    
    reduceRepositoryDir = XMLHandler.getTagValue(entrynode, "reduce_trans_repo_dir"); //$NON-NLS-1$
    reduceRepositoryFile = XMLHandler.getTagValue(entrynode, "reduce_trans_repo_file"); //$NON-NLS-1$
    String reduceTransId = XMLHandler.getTagValue(entrynode, "reduce_trans_repo_reference"); //$NON-NLS-1$
    reduceRepositoryReference = Const.isEmpty(reduceTransId) ? null : new StringObjectId(reduceTransId);
    reduceTrans = XMLHandler.getTagValue(entrynode, "reduce_trans"); //$NON-NLS-1$
    String single = XMLHandler.getTagValue(entrynode, "reduce_single_threaded");  //$NON-NLS-1$
    if (Const.isEmpty(single)) {
      reducingSingleThreaded = true;
    } else {
      reducingSingleThreaded = "Y".equalsIgnoreCase(single);  //$NON-NLS-1$
    }
    
    mapInputStepName = XMLHandler.getTagValue(entrynode, "map_input_step_name"); //$NON-NLS-1$
    mapOutputStepName = XMLHandler.getTagValue(entrynode, "map_output_step_name"); //$NON-NLS-1$
    combinerInputStepName = XMLHandler.getTagValue(entrynode, "combiner_input_step_name"); //$NON-NLS-1$
    combinerOutputStepName = XMLHandler.getTagValue(entrynode, "combiner_output_step_name"); //$NON-NLS-1$
    reduceInputStepName = XMLHandler.getTagValue(entrynode, "reduce_input_step_name"); //$NON-NLS-1$
    reduceOutputStepName = XMLHandler.getTagValue(entrynode, "reduce_output_step_name"); //$NON-NLS-1$

    blocking = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "blocking")); //$NON-NLS-1$ //$NON-NLS-2$

    loggingInterval = XMLHandler.getTagValue(entrynode, "logging_interval"); //$NON-NLS-1$
    inputPath = XMLHandler.getTagValue(entrynode, "input_path"); //$NON-NLS-1$
    inputFormatClass = XMLHandler.getTagValue(entrynode, "input_format_class"); //$NON-NLS-1$
    outputPath = XMLHandler.getTagValue(entrynode, "output_path"); //$NON-NLS-1$

    final String cleanOutputPath = XMLHandler.getTagValue(entrynode, "clean_output_path");
    if (!Const.isEmpty(cleanOutputPath)) { //$NON-NLS-1$
      setCleanOutputPath(cleanOutputPath.equalsIgnoreCase("Y")); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    if (!Const.isEmpty(XMLHandler.getTagValue(entrynode, "suppress_output_map_key"))) {
      suppressOutputMapKey = XMLHandler.getTagValue(entrynode, "suppress_output_map_key").equalsIgnoreCase("Y");    }
    if (!Const.isEmpty(XMLHandler.getTagValue(entrynode, "suppress_output_map_value"))) {
      suppressOutputMapValue = XMLHandler.getTagValue(entrynode, "suppress_output_map_value").equalsIgnoreCase("Y");
    }
    
    if (!Const.isEmpty(XMLHandler.getTagValue(entrynode, "suppress_output_key"))) {
      suppressOutputKey = XMLHandler.getTagValue(entrynode, "suppress_output_key").equalsIgnoreCase("Y");
    }
    if (!Const.isEmpty(XMLHandler.getTagValue(entrynode, "suppress_output_value"))) {
      suppressOutputValue = XMLHandler.getTagValue(entrynode, "suppress_output_value").equalsIgnoreCase("Y");
    }
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
    retval.append("      ").append(XMLHandler.addTagValue("combiner_single_threaded", combiningSingleThreaded)); //$NON-NLS-1$ //$NON-NLS-2$
    
    retval.append("      ").append(XMLHandler.addTagValue("reduce_trans_repo_dir", reduceRepositoryDir)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("reduce_trans_repo_file", reduceRepositoryFile)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("reduce_trans_repo_reference", reduceRepositoryReference == null ? null : reduceRepositoryReference.toString())); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("reduce_trans", reduceTrans)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("reduce_single_threaded", reducingSingleThreaded)); //$NON-NLS-1$ //$NON-NLS-2$

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

    retval.append("      ").append(XMLHandler.addTagValue("clean_output_path", cleanOutputPath)); //$NON-NLS-1$ //$NON-NLS-2$
    
    retval.append("      ").append(XMLHandler.addTagValue("suppress_output_map_key", suppressOutputMapKey)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("suppress_output_map_value", suppressOutputMapValue)); //$NON-NLS-1$ //$NON-NLS-2$

    retval.append("      ").append(XMLHandler.addTagValue("suppress_output_key", suppressOutputKey)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("suppress_output_value", suppressOutputValue)); //$NON-NLS-1$ //$NON-NLS-2$
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
      setReducingSingleThreaded(rep.getJobEntryAttributeBoolean(id_jobentry, "reduce_single_threaded", true)); //$NON-NLS-1$

      setCombinerRepositoryDir(rep.getJobEntryAttributeString(id_jobentry, "combiner_trans_repo_dir")); //$NON-NLS-1$
      setCombinerRepositoryFile(rep.getJobEntryAttributeString(id_jobentry, "combiner_trans_repo_file")); //$NON-NLS-1$
      String combinerTransId = rep.getJobEntryAttributeString(id_jobentry, "combiner_trans_repo_reference"); //$NON-NLS-1$
      setCombinerRepositoryReference(Const.isEmpty(combinerTransId) ? null : new StringObjectId(combinerTransId));
      setCombinerTrans(rep.getJobEntryAttributeString(id_jobentry, "combiner_trans")); //$NON-NLS-1$
      setCombiningSingleThreaded(rep.getJobEntryAttributeBoolean(id_jobentry, "combiner_single_threaded", true)); //$NON-NLS-1$
      
      setMapInputStepName(rep.getJobEntryAttributeString(id_jobentry, "map_input_step_name")); //$NON-NLS-1$
      setMapOutputStepName(rep.getJobEntryAttributeString(id_jobentry, "map_output_step_name")); //$NON-NLS-1$
      setCombinerInputStepName(rep.getJobEntryAttributeString(id_jobentry, "combiner_input_step_name")); //$NON-NLS-1$
      setCombinerOutputStepName(rep.getJobEntryAttributeString(id_jobentry, "combiner_output_step_name")); //$NON-NLS-1$
      setReduceInputStepName(rep.getJobEntryAttributeString(id_jobentry, "reduce_input_step_name")); //$NON-NLS-1$
      setReduceOutputStepName(rep.getJobEntryAttributeString(id_jobentry, "reduce_output_step_name")); //$NON-NLS-1$

      setBlocking(rep.getJobEntryAttributeBoolean(id_jobentry, "blocking")); //$NON-NLS-1$
      setLoggingInterval(rep.getJobEntryAttributeString(id_jobentry, "logging_interval")); //$NON-NLS-1$

      setInputPath(rep.getJobEntryAttributeString(id_jobentry, "input_path")); //$NON-NLS-1$
      setInputFormatClass(rep.getJobEntryAttributeString(id_jobentry, "input_format_class")); //$NON-NLS-1$
      setOutputPath(rep.getJobEntryAttributeString(id_jobentry, "output_path")); //$NON-NLS-1$
      setCleanOutputPath(rep.getJobEntryAttributeBoolean(id_jobentry, "clean_output_path")); //$NON-NLS-1$
      
      setSuppressOutputOfMapKey(rep.getJobEntryAttributeBoolean(id_jobentry, "suppress_output_map_key")); //$NON-NLS-1$
      setSuppressOutputOfMapValue(rep.getJobEntryAttributeBoolean(id_jobentry, "suppress_output_map_value")); //$NON-NLS-1$

      setSuppressOutputOfKey(rep.getJobEntryAttributeBoolean(id_jobentry, "suppress_output_key")); //$NON-NLS-1$
      setSuppressOutputOfValue(rep.getJobEntryAttributeBoolean(id_jobentry, "suppress_output_value")); //$NON-NLS-1$
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
      rep.saveJobEntryAttribute(id_job, getObjectId(),"reduce_single_threaded", reducingSingleThreaded); //$NON-NLS-1$

      rep.saveJobEntryAttribute(id_job, getObjectId(),"combiner_trans_repo_dir", combinerRepositoryDir); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"combiner_trans_repo_file", combinerRepositoryFile); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"combiner_trans_repo_reference", combinerRepositoryReference == null ? null : combinerRepositoryReference.toString()); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"combiner_trans", combinerTrans); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"combiner_single_threaded", combiningSingleThreaded); //$NON-NLS-1$
      
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
      rep.saveJobEntryAttribute(id_job, getObjectId(),"clean_output_path", cleanOutputPath); //$NON-NLS-1$
      
      rep.saveJobEntryAttribute(id_job, getObjectId(),"suppress_output_map_key", suppressOutputMapKey); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"suppress_output_map_value", suppressOutputMapValue); //$NON-NLS-1$

      rep.saveJobEntryAttribute(id_job, getObjectId(),"suppress_output_key", suppressOutputKey); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"suppress_output_value", suppressOutputValue); //$NON-NLS-1$
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

  /**
   * @return the reduceSingleThreaded
   */
  public boolean isReducingSingleThreaded() {
    return reducingSingleThreaded;
  }

  /**
   * @param reducingSingleThreaded the reducing single threaded to set
   */
  public void setReducingSingleThreaded(boolean reducingSingleThreaded) {
    this.reducingSingleThreaded = reducingSingleThreaded;
  }

  public boolean isCombiningSingleThreaded() {
    return combiningSingleThreaded;
  }

  public void setCombiningSingleThreaded(boolean combiningSingleThreaded) {
    this.combiningSingleThreaded = combiningSingleThreaded;
  }
}
