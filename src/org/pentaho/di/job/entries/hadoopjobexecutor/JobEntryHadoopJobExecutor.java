/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
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

package org.pentaho.di.job.entries.hadoopjobexecutor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.RunningJob;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.SerializationHelper;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.job.entries.hadoopjobexecutor.UserDefinedItem;
import org.pentaho.di.ui.spoon.Spoon;
import org.w3c.dom.Node;

@JobEntry(id = "HadoopJobExecutorPlugin", name = "Hadoop Job Executor", categoryDescription = "Hadoop", description = "Execute Map/Reduce jobs in Hadoop", image = "HDE.png")
public class JobEntryHadoopJobExecutor extends JobEntryBase implements Cloneable, JobEntryInterface {

  private static Class<?> PKG = JobEntryHadoopJobExecutor.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  private String hadoopJobName;

  private String jarUrl = "";

  private boolean isSimple = false;

  private String cmdLineArgs;

  private String outputKeyClass;
  private String outputValueClass;
  private String mapperClass;
  private String combinerClass;
  private String reducerClass;
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

  private int numMapTasks = 1;
  private int numReduceTasks = 1;

  private List<UserDefinedItem> userDefined = new ArrayList<UserDefinedItem>();

  public String getHadoopJobName() {
    return hadoopJobName;
  }

  public void setHadoopJobName(String hadoopJobName) {
    this.hadoopJobName = hadoopJobName;
  }

  public String getJarUrl() {
    return jarUrl;
  }

  public void setJarUrl(String jarUrl) {
    this.jarUrl = jarUrl;
  }

  public boolean isSimple() {
    return isSimple;
  }

  public void setSimple(boolean isSimple) {
    this.isSimple = isSimple;
  }

  public String getCmdLineArgs() {
    return cmdLineArgs;
  }

  public void setCmdLineArgs(String cmdLineArgs) {
    this.cmdLineArgs = cmdLineArgs;
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

  public String getMapperClass() {
    return mapperClass;
  }

  public void setMapperClass(String mapperClass) {
    this.mapperClass = mapperClass;
  }

  public String getCombinerClass() {
    return combinerClass;
  }

  public void setCombinerClass(String combinerClass) {
    this.combinerClass = combinerClass;
  }

  public String getReducerClass() {
    return reducerClass;
  }

  public void setReducerClass(String reducerClass) {
    this.reducerClass = reducerClass;
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

  public List<UserDefinedItem> getUserDefined() {
    return userDefined;
  }

  public void setUserDefined(List<UserDefinedItem> userDefined) {
    this.userDefined = userDefined;
  }

  public int getNumMapTasks() {
    return numMapTasks;
  }

  public void setNumMapTasks(int numMapTasks) {
    this.numMapTasks = numMapTasks;
  }

  public int getNumReduceTasks() {
    return numReduceTasks;
  }

  public void setNumReduceTasks(int numReduceTasks) {
    this.numReduceTasks = numReduceTasks;
  }

  public Result execute(Result result, int arg1) throws KettleException {
    try {
      URL resolvedJarUrl = null;
      if (jarUrl.indexOf("://") == -1) {
        // default to file://
        File jarFile = new File(jarUrl);
        resolvedJarUrl = jarFile.toURI().toURL();
      } else {
        resolvedJarUrl = new URL(jarUrl);
      }

      if (log.isDetailed())
        logDetailed(BaseMessages.getString(PKG, "JobEntryHadoopJobExecutor.ResolvedJar", resolvedJarUrl.toExternalForm()));

      if (isSimple) {
        if (log.isDetailed())
          logDetailed(BaseMessages.getString(PKG, "JobEntryHadoopJobExecutor.SimpleMode"));
        List<Class<?>> classesWithMains = JarUtility.getClassesInJarWithMain(resolvedJarUrl.toExternalForm(), getClass().getClassLoader());
        for (final Class<?> clazz : classesWithMains) {
          Runnable r = new Runnable() {
            public void run() {
              try {
                Method mainMethod = clazz.getMethod("main", new Class[] { String[].class });
                Object[] args = (cmdLineArgs != null) ? new Object[] { cmdLineArgs.split(" ") } : new Object[0];
                mainMethod.invoke(null, args);
              } catch (Throwable ignored) {
                // skip, try the next one
              }
            }
          };
          Thread t = new Thread(r);
          t.start();
        }

      } else {
        if (log.isDetailed())
          logDetailed(BaseMessages.getString(PKG, "JobEntryHadoopJobExecutor.AdvancedMode"));

        URL[] urls = new URL[] { resolvedJarUrl };
        URLClassLoader loader = new URLClassLoader(urls, getClass().getClassLoader());

        JobConf conf = new JobConf();
        conf.setJobName(hadoopJobName);

        conf.setOutputKeyClass(loader.loadClass(outputKeyClass));
        conf.setOutputValueClass(loader.loadClass(outputValueClass));

        Class<? extends Mapper> mapper = (Class<? extends Mapper>) loader.loadClass(mapperClass);
        conf.setMapperClass(mapper);
        Class<? extends Reducer> combiner = (Class<? extends Reducer>) loader.loadClass(combinerClass);
        conf.setCombinerClass(combiner);
        Class<? extends Reducer> reducer = (Class<? extends Reducer>) loader.loadClass(reducerClass);
        conf.setReducerClass(reducer);

        Class<? extends InputFormat> inputFormat = (Class<? extends InputFormat>) loader.loadClass(inputFormatClass);
        conf.setInputFormat(inputFormat);
        Class<? extends OutputFormat> outputFormat = (Class<? extends OutputFormat>) loader.loadClass(outputFormatClass);
        conf.setOutputFormat(outputFormat);

        String hdfsBaseUrl = "hdfs://" + hdfsHostname + ":" + hdfsPort;
        conf.set("fs.default.name", hdfsBaseUrl);
        conf.set("mapred.job.tracker", jobTrackerHostname + ":" + jobTrackerPort);

        // TODO: this could be a list of input paths apparently
        FileInputFormat.setInputPaths(conf, new Path(hdfsBaseUrl + inputPath));
        FileOutputFormat.setOutputPath(conf, new Path(hdfsBaseUrl + outputPath));

        // process user defined values
        for (UserDefinedItem item : userDefined) {
          if (item.getName() != null && !"".equals(item.getName()) && item.getValue() != null && !"".equals(item.getValue())) {
            conf.set(item.getName(), item.getValue());
          }
        }

        conf.setWorkingDirectory(new Path(hdfsBaseUrl + workingDirectory));
        conf.setJar(jarUrl);

        conf.setNumMapTasks(numMapTasks);
        conf.setNumReduceTasks(numReduceTasks);

        JobClient jobClient = new JobClient(conf);
        RunningJob runningJob = jobClient.submitJob(conf);

        if (blocking) {
          try {
            while (!runningJob.isComplete()) {
              printJobStatus(runningJob);
              Thread.sleep(1000);
            }
            printJobStatus(runningJob);
          } catch (InterruptedException ie) {
            log.logError(ie.getMessage(), ie);
          }
        }

      }
    } catch (Throwable t) {
      t.printStackTrace();
      result.setStopped(true);
      result.setNrErrors(1);
      result.setResult(false);
      logError(t.getMessage(), t);
    }
    return result;
  }

  public void printJobStatus(RunningJob runningJob) throws IOException {
    if (log.isBasic()) {
      float setupPercent = runningJob.setupProgress() * 100f;
      float mapPercent = runningJob.mapProgress() * 100f;
      float reducePercent = runningJob.reduceProgress() * 100f;
      logBasic(BaseMessages.getString(PKG, "JobEntryHadoopJobExecutor.RunningPercent", setupPercent, mapPercent, reducePercent));
    }
  }

  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException {
    super.loadXML(entrynode, databases, slaveServers);
    hadoopJobName = XMLHandler.getTagValue(entrynode, "hadoop_job_name");
    isSimple = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "simple"));
    jarUrl = XMLHandler.getTagValue(entrynode, "jar_url");
    cmdLineArgs = XMLHandler.getTagValue(entrynode, "command_line_args");
    blocking = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "blocking"));

    mapperClass = XMLHandler.getTagValue(entrynode, "mapper_class");
    combinerClass = XMLHandler.getTagValue(entrynode, "combiner_class");
    reducerClass = XMLHandler.getTagValue(entrynode, "reducer_class");
    inputPath = XMLHandler.getTagValue(entrynode, "input_path");
    inputFormatClass = XMLHandler.getTagValue(entrynode, "input_format_class");
    outputPath = XMLHandler.getTagValue(entrynode, "output_path");
    outputKeyClass = XMLHandler.getTagValue(entrynode, "output_key_class");
    outputValueClass = XMLHandler.getTagValue(entrynode, "output_value_class");
    outputFormatClass = XMLHandler.getTagValue(entrynode, "output_format_class");

    hdfsHostname = XMLHandler.getTagValue(entrynode, "hdfs_hostname");
    hdfsPort = XMLHandler.getTagValue(entrynode, "hdfs_port");
    jobTrackerHostname = XMLHandler.getTagValue(entrynode, "job_tracker_hostname");
    jobTrackerPort = XMLHandler.getTagValue(entrynode, "job_tracker_port");
    numMapTasks = Integer.parseInt(XMLHandler.getTagValue(entrynode, "num_map_tasks"));
    numReduceTasks = Integer.parseInt(XMLHandler.getTagValue(entrynode, "num_reduce_tasks"));
    workingDirectory = XMLHandler.getTagValue(entrynode, "working_dir");

    // How many user defined elements?
    userDefined = new ArrayList<UserDefinedItem>();
    Node userDefinedList = XMLHandler.getSubNode(entrynode, "user_defined_list");
    int nrUserDefined = XMLHandler.countNodes(userDefinedList, "user_defined");
    for (int i = 0; i < nrUserDefined; i++) {
      Node userDefinedNode = XMLHandler.getSubNodeByNr(userDefinedList, "user_defined", i);
      String name = XMLHandler.getTagValue(userDefinedNode, "name");
      String value = XMLHandler.getTagValue(userDefinedNode, "value");
      UserDefinedItem item = new UserDefinedItem();
      item.setName(name);
      item.setValue(value);
      userDefined.add(item);
    }
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer(1024);
    retval.append(super.getXML());
    retval.append("      ").append(XMLHandler.addTagValue("hadoop_job_name", hadoopJobName));

    retval.append("      ").append(XMLHandler.addTagValue("simple", isSimple));
    retval.append("      ").append(XMLHandler.addTagValue("jar_url", jarUrl));
    retval.append("      ").append(XMLHandler.addTagValue("command_line_args", cmdLineArgs));
    retval.append("      ").append(XMLHandler.addTagValue("blocking", blocking));
    retval.append("      ").append(XMLHandler.addTagValue("hadoop_job_name", hadoopJobName));

    retval.append("      ").append(XMLHandler.addTagValue("mapper_class", mapperClass));
    retval.append("      ").append(XMLHandler.addTagValue("combiner_class", combinerClass));
    retval.append("      ").append(XMLHandler.addTagValue("reducer_class", reducerClass));
    retval.append("      ").append(XMLHandler.addTagValue("input_path", inputPath));
    retval.append("      ").append(XMLHandler.addTagValue("input_format_class", inputFormatClass));
    retval.append("      ").append(XMLHandler.addTagValue("output_path", outputPath));
    retval.append("      ").append(XMLHandler.addTagValue("output_key_class", outputKeyClass));
    retval.append("      ").append(XMLHandler.addTagValue("output_value_class", outputValueClass));
    retval.append("      ").append(XMLHandler.addTagValue("output_format_class", outputFormatClass));

    retval.append("      ").append(XMLHandler.addTagValue("hdfs_hostname", hdfsHostname));
    retval.append("      ").append(XMLHandler.addTagValue("hdfs_port", hdfsPort));
    retval.append("      ").append(XMLHandler.addTagValue("job_tracker_hostname", jobTrackerHostname));
    retval.append("      ").append(XMLHandler.addTagValue("job_tracker_port", jobTrackerPort));
    retval.append("      ").append(XMLHandler.addTagValue("num_map_tasks", numMapTasks));
    retval.append("      ").append(XMLHandler.addTagValue("num_reduce_tasks", numReduceTasks));
    retval.append("      ").append(XMLHandler.addTagValue("working_dir", workingDirectory));

    retval.append("      <user_defined_list>").append(Const.CR);
    if (userDefined != null) {
      for (UserDefinedItem item : userDefined) {
        if (item.getName() != null && !"".equals(item.getName()) && item.getValue() != null && !"".equals(item.getValue())) {
          retval.append("        <user_defined>").append(Const.CR);
          retval.append("          ").append(XMLHandler.addTagValue("name", item.getName()));
          retval.append("          ").append(XMLHandler.addTagValue("value", item.getValue()));
          retval.append("        </user_defined>").append(Const.CR);
        }
      }
    }
    retval.append("      </user_defined_list>").append(Const.CR);
    return retval.toString();
  }

  public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
    SerializationHelper.readJobRep(this, rep, id_jobentry, databases);
  }

  public void saveRep(Repository rep, ObjectId id_job) throws KettleException {
    SerializationHelper.saveJobRep(this, rep, id_job, getObjectId());
  }

}
