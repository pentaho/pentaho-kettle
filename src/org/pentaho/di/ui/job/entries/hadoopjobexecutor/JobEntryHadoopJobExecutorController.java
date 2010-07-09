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

package org.pentaho.di.ui.job.entries.hadoopjobexecutor;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.job.entries.hadoopjobexecutor.JobEntryHadoopJobExecutor;
import org.pentaho.di.job.entries.hadoopjobexecutor.JobEntryHadoopJobExecutor.UserDefinedItem;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class JobEntryHadoopJobExecutorController extends AbstractXulEventHandler {
  public static final String STEP_NAME = "stepName"; //$NON-NLS-1$
  public static final String HADOOP_JOB_NAME = "hadoopJobName"; //$NON-NLS-1$
  public static final String JAR_URL = "jarUrl"; //$NON-NLS-1$
  public static final String IS_SIMPLE = "isSimple"; //$NON-NLS-1$

  private String stepName;
  private String hadoopJobName;
  private String jarUrl;

  private boolean isSimple;

  private SimpleConfiguration sConf = new SimpleConfiguration();
  private AdvancedConfiguration aConf = new AdvancedConfiguration();

  private JobEntryHadoopJobExecutor jobEntry;

  public void accept() {
    // common/simple
    jobEntry.setName(stepName);
    jobEntry.setHadoopJobName(hadoopJobName);
    jobEntry.setSimple(isSimple);
    jobEntry.setJarUrl(jarUrl);
    jobEntry.setCmdLineArgs(sConf.getCommandLineArgs());
    // advanced config
    jobEntry.setBlocking(aConf.isBlocking());
    jobEntry.setMapperClass(aConf.getMapperClass());
    jobEntry.setCombinerClass(aConf.getCombinerClass());
    jobEntry.setReducerClass(aConf.getReducerClass());
    jobEntry.setInputPath(aConf.getInputPath());
    jobEntry.setInputFormatClass(aConf.getInputFormatClass());
    jobEntry.setOutputPath(aConf.getOutputPath());
    jobEntry.setOutputKeyClass(aConf.getOutputKeyClass());
    jobEntry.setOutputValueClass(aConf.getOutputValueClass());
    jobEntry.setOutputFormatClass(aConf.getOutputFormatClass());
    jobEntry.setHdfsHostname(aConf.getHdfsHostname());
    jobEntry.setHdfsPort(aConf.getHdfsPort());
    jobEntry.setJobTrackerHostname(aConf.getJobTrackerHostname());
    jobEntry.setJobTrackerPort(aConf.getJobTrackerPort());
    jobEntry.setNumMapTasks(aConf.getNumMapTasks());
    jobEntry.setNumReduceTasks(aConf.getNumReduceTasks());
    jobEntry.setUserDefined(aConf.getUserDefined());
    jobEntry.setWorkingDirectory(aConf.getWorkingDirectory());
    cancel();
  }

  public void cancel() {
    XulDialog xulDialog = (XulDialog) getXulDomContainer().getDocumentRoot().getRootElement();
    Shell shell = (Shell) xulDialog.getRootObject();
    if (!shell.isDisposed()) {
      WindowProperty winprop = new WindowProperty(shell);
      PropsUI.getInstance().setScreen(winprop);
      ((Composite) xulDialog.getManagedObject()).dispose();
      shell.dispose();
    }
  }

  public void setMode(String type) {
    System.out.println(type);
  }

  public void validateJarUrl() {
    // TODO:
    throw new RuntimeException("This method has not been implemented");
  }

  public SimpleConfiguration getSimpleConfiguration() {
    return sConf;
  }

  public AdvancedConfiguration getAdvancedConfiguration() {
    return aConf;
  }

  @Override
  public String getName() {
    return "jobEntryController"; //$NON-NLS-1$
  }

  public String getStepName() {
    return stepName;
  }

  public void setStepName(String stepName) {
    String previousVal = this.stepName;
    String newVal = stepName;

    this.stepName = stepName;
    firePropertyChange(JobEntryHadoopJobExecutorController.STEP_NAME, previousVal, newVal);
  }

  public String getHadoopJobName() {
    return hadoopJobName;
  }

  public void setHadoopJobName(String hadoopJobName) {
    String previousVal = this.hadoopJobName;
    String newVal = hadoopJobName;

    this.hadoopJobName = hadoopJobName;
    firePropertyChange(JobEntryHadoopJobExecutorController.HADOOP_JOB_NAME, previousVal, newVal);
  }

  public String getJarUrl() {
    return jarUrl;
  }

  public void setJarUrl(String jarUrl) {
    String previousVal = this.jarUrl;
    String newVal = jarUrl;

    this.jarUrl = jarUrl;
    firePropertyChange(JobEntryHadoopJobExecutorController.JAR_URL, previousVal, newVal);
  }

  public boolean isSimple() {
    return isSimple;
  }

  public void setSimple(boolean isSimple) {
    boolean previousVal = this.isSimple;
    boolean newVal = isSimple;

    this.isSimple = isSimple;
    firePropertyChange(JobEntryHadoopJobExecutorController.IS_SIMPLE, previousVal, newVal);
  }

  public JobEntryHadoopJobExecutor getJobEntry() {
    return jobEntry;
  }

  public void setJobEntry(JobEntryHadoopJobExecutor jobEntry) {
    this.jobEntry = jobEntry;
  }

  public class SimpleConfiguration extends XulEventSourceAdapter {
    public static final String CMD_LINE_ARGS = "commandLineArgs"; //$NON-NLS-1$

    private String cmdLineArgs;

    public String getCommandLineArgs() {
      return cmdLineArgs;
    }

    public void setCommandLineArgs(String cmdLineArgs) {
      String previousVal = this.cmdLineArgs;
      String newVal = cmdLineArgs;

      this.cmdLineArgs = cmdLineArgs;

      firePropertyChange(SimpleConfiguration.CMD_LINE_ARGS, previousVal, newVal);
    }
  }

  public class AdvancedConfiguration extends XulEventSourceAdapter {
    public static final String OUTPUT_KEY_CLASS = "outputKeyClass"; //$NON-NLS-1$
    public static final String OUTPUT_VALUE_CLASS = "outputValueClass"; //$NON-NLS-1$
    public static final String MAPPER_CLASS = "mapperClass"; //$NON-NLS-1$
    public static final String COMBINER_CLASS = "combinerClass"; //$NON-NLS-1$
    public static final String REDUCER_CLASS = "reducerClass"; //$NON-NLS-1$
    public static final String INPUT_FORMAT_CLASS = "inputFormatClass"; //$NON-NLS-1$
    public static final String OUTPUT_FORMAT_CLASS = "outputFormatClass"; //$NON-NLS-1$
    public static final String WORKING_DIRECTORY = "workingDirectory"; //$NON-NLS-1$
    public static final String INPUT_PATH = "inputPath"; //$NON-NLS-1$
    public static final String OUTPUT_PATH = "outputPath"; //$NON-NLS-1$
    public static final String USER_DEFINED = "userDefined"; //$NON-NLS-1$
    public static final String BLOCKING = "blocking"; //$NON-NLS-1$
    public static final String HDFS_HOSTNAME = "hdfsHostname"; //$NON-NLS-1$
    public static final String HDFS_PORT = "hdfsPort"; //$NON-NLS-1$
    public static final String JOB_TRACKER_HOSTNAME = "jobTrackerHostname"; //$NON-NLS-1$
    public static final String JOB_TRACKER_PORT = "jobTrackerPort"; //$NON-NLS-1$
    public static final String NUM_MAP_TASKS = "numMapTasks"; //$NON-NLS-1$
    public static final String NUM_REDUCE_TASKS = "numReduceTasks"; //$NON-NLS-1$

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

    private int numMapTasks;
    private int numReduceTasks;

    private boolean blocking;

    private List<UserDefinedItem> userDefined;

    public String getOutputKeyClass() {
      return outputKeyClass;
    }

    public void setOutputKeyClass(String outputKeyClass) {
      String previousVal = this.outputKeyClass;
      String newVal = outputKeyClass;

      this.outputKeyClass = outputKeyClass;
      firePropertyChange(AdvancedConfiguration.OUTPUT_KEY_CLASS, previousVal, newVal);
    }

    public String getOutputValueClass() {
      return outputValueClass;
    }

    public void setOutputValueClass(String outputValueClass) {
      String previousVal = this.outputValueClass;
      String newVal = outputValueClass;

      this.outputValueClass = outputValueClass;
      firePropertyChange(AdvancedConfiguration.OUTPUT_VALUE_CLASS, previousVal, newVal);
    }

    public String getMapperClass() {
      return mapperClass;
    }

    public void setMapperClass(String mapperClass) {
      String previousVal = this.mapperClass;
      String newVal = mapperClass;

      this.mapperClass = mapperClass;
      firePropertyChange(AdvancedConfiguration.MAPPER_CLASS, previousVal, newVal);
    }

    public String getCombinerClass() {
      return combinerClass;
    }

    public void setCombinerClass(String combinerClass) {
      String previousVal = this.combinerClass;
      String newVal = combinerClass;

      this.combinerClass = combinerClass;
      firePropertyChange(AdvancedConfiguration.COMBINER_CLASS, previousVal, newVal);
    }

    public String getReducerClass() {
      return reducerClass;
    }

    public void setReducerClass(String reducerClass) {
      String previousVal = this.reducerClass;
      String newVal = reducerClass;

      this.reducerClass = reducerClass;
      firePropertyChange(AdvancedConfiguration.REDUCER_CLASS, previousVal, newVal);
    }

    public String getInputFormatClass() {
      return inputFormatClass;
    }

    public void setInputFormatClass(String inputFormatClass) {
      String previousVal = this.inputFormatClass;
      String newVal = inputFormatClass;

      this.inputFormatClass = inputFormatClass;
      firePropertyChange(AdvancedConfiguration.INPUT_FORMAT_CLASS, previousVal, newVal);
    }

    public String getOutputFormatClass() {
      return outputFormatClass;
    }

    public void setOutputFormatClass(String outputFormatClass) {
      String previousVal = this.outputFormatClass;
      String newVal = outputFormatClass;

      this.outputFormatClass = outputFormatClass;
      firePropertyChange(AdvancedConfiguration.OUTPUT_FORMAT_CLASS, previousVal, newVal);
    }

    public String getWorkingDirectory() {
      return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
      String previousVal = this.workingDirectory;
      String newVal = workingDirectory;

      this.workingDirectory = workingDirectory;
      firePropertyChange(AdvancedConfiguration.WORKING_DIRECTORY, previousVal, newVal);
    }

    public String getHdfsHostname() {
      return hdfsHostname;
    }

    public void setHdfsHostname(String hdfsHostname) {
      String previousVal = this.hdfsHostname;
      String newVal = hdfsHostname;

      this.hdfsHostname = hdfsHostname;
      firePropertyChange(AdvancedConfiguration.HDFS_HOSTNAME, previousVal, newVal);
    }

    public String getHdfsPort() {
      return hdfsPort;
    }

    public void setHdfsPort(String hdfsPort) {
      String previousVal = this.hdfsPort;
      String newVal = hdfsPort;

      this.hdfsPort = hdfsPort;
      firePropertyChange(AdvancedConfiguration.HDFS_PORT, previousVal, newVal);
    }

    public String getJobTrackerHostname() {
      return jobTrackerHostname;
    }

    public void setJobTrackerHostname(String jobTrackerHostname) {
      String previousVal = this.jobTrackerHostname;
      String newVal = jobTrackerHostname;

      this.jobTrackerHostname = jobTrackerHostname;
      firePropertyChange(AdvancedConfiguration.JOB_TRACKER_HOSTNAME, previousVal, newVal);
    }

    public String getJobTrackerPort() {
      return jobTrackerPort;
    }

    public void setJobTrackerPort(String jobTrackerPort) {
      String previousVal = this.jobTrackerPort;
      String newVal = jobTrackerPort;

      this.jobTrackerPort = jobTrackerPort;
      firePropertyChange(AdvancedConfiguration.JOB_TRACKER_PORT, previousVal, newVal);
    }

    public String getInputPath() {
      return inputPath;
    }

    public void setInputPath(String inputPath) {
      String previousVal = this.inputPath;
      String newVal = inputPath;

      this.inputPath = inputPath;
      firePropertyChange(AdvancedConfiguration.INPUT_PATH, previousVal, newVal);
    }

    public String getOutputPath() {
      return outputPath;
    }

    public void setOutputPath(String outputPath) {
      String previousVal = this.outputPath;
      String newVal = outputPath;

      this.outputPath = outputPath;
      firePropertyChange(AdvancedConfiguration.OUTPUT_PATH, previousVal, newVal);
    }

    public List<UserDefinedItem> getUserDefined() {
      return userDefined;
    }

    public void setUserDefined(List<UserDefinedItem> userDefined) {
      List<UserDefinedItem> previousVal = this.userDefined;
      List<UserDefinedItem> newVal = userDefined;

      this.userDefined = userDefined;
      firePropertyChange(AdvancedConfiguration.USER_DEFINED, previousVal, newVal);
    }

    public boolean isBlocking() {
      return blocking;
    }

    public void setBlocking(boolean blocking) {
      boolean previousVal = this.blocking;
      boolean newVal = blocking;

      this.blocking = blocking;
      firePropertyChange(AdvancedConfiguration.BLOCKING, previousVal, newVal);
    }

    public int getNumMapTasks() {
      return numMapTasks;
    }

    public void setNumMapTasks(int numMapTasks) {
      int previousVal = this.numMapTasks;
      int newVal = numMapTasks;

      this.numMapTasks = numMapTasks;
      firePropertyChange(AdvancedConfiguration.NUM_MAP_TASKS, previousVal, newVal);
    }

    public int getNumReduceTasks() {
      return numReduceTasks;
    }

    public void setNumReduceTasks(int numReduceTasks) {
      int previousVal = this.numReduceTasks;
      int newVal = numReduceTasks;

      this.numReduceTasks = numReduceTasks;
      firePropertyChange(AdvancedConfiguration.NUM_REDUCE_TASKS, previousVal, newVal);
    }
  }
}