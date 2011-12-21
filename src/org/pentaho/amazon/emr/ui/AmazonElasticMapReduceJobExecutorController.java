/* Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.amazon.emr.ui;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.database.dialog.tags.ExtTextbox;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.vfs.ui.CustomVfsUiPanel;
import org.pentaho.vfs.ui.IVfsFileChooser;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

import org.pentaho.amazon.AmazonSpoonPlugin;
import org.pentaho.amazon.emr.job.AmazonElasticMapReduceJobExecutor;
import org.pentaho.amazon.s3.S3VfsFileChooserDialog;

public class AmazonElasticMapReduceJobExecutorController extends AbstractXulEventHandler {

  private static final Class<?> PKG = AmazonElasticMapReduceJobExecutor.class;

  public static final String JOB_ENTRY_NAME = "jobEntryName"; //$NON-NLS-1$
  public static final String HADOOP_JOB_NAME = "hadoopJobName"; //$NON-NLS-1$
  public static final String HADOOP_JOB_FLOW_ID = "hadoopJobFlowId"; //$NON-NLS-1$
  public static final String JAR_URL = "jarUrl"; //$NON-NLS-1$
  public static final String ACCESS_KEY = "accessKey"; //$NON-NLS-1$
  public static final String SECRET_KEY = "secretKey"; //$NON-NLS-1$
  public static final String STAGING_DIR = "stagingDir"; //$NON-NLS-1$
  public static final String NUM_INSTANCES = "numInstances"; //$NON-NLS-1$
  public static final String MASTER_INSTANCE_TYPE = "masterInstanceType"; //$NON-NLS-1$
  public static final String SLAVE_INSTANCE_TYPE = "slaveInstanceType"; //$NON-NLS-1$
  public static final String USER_DEFINED = "userDefined"; //$NON-NLS-1$
  public static final String CMD_LINE_ARGS = "commandLineArgs"; //$NON-NLS-1$
  public static final String BLOCKING = "blocking"; //$NON-NLS-1$
  public static final String LOGGING_INTERVAL = "loggingInterval"; //$NON-NLS-1$

  private String jobEntryName;
  private String hadoopJobName;
  private String hadoopJobFlowId;
  private String jarUrl = "";
  private String accessKey = "";
  private String secretKey = "";
  private String stagingDir = "";
  private String numInstances = "2";
  private String masterInstanceType = "m1.small";
  private String slaveInstanceType = "m1.small";
  private String cmdLineArgs;
  private boolean blocking;
  private String loggingInterval = "60"; // 60 seconds

  private AmazonElasticMapReduceJobExecutor jobEntry;

  public void accept() {

    ExtTextbox tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById("jobentry-hadoopjob-name");
    this.hadoopJobName = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById("access-key");
    this.accessKey = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById("secret-key");
    this.secretKey = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById("jobentry-hadoopjob-flow-id");
    this.hadoopJobFlowId = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById("s3-staging-directory");
    this.stagingDir = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById("jar-url");
    this.jarUrl = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById("command-line-arguments");
    this.cmdLineArgs = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById("num-instances");
    this.numInstances = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById("logging-interval");
    this.loggingInterval = ((Text) tempBox.getTextControl()).getText();

    String validationErrors = "";
    if (StringUtil.isEmpty(jobEntryName)) {
      validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.JobEntryName.Error") + "\n";
    }
    if (StringUtil.isEmpty(hadoopJobName)) {
      validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.JobFlowName.Error") + "\n";
    }
    if (StringUtil.isEmpty(jarUrl)) {
      validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.JarURL.Error") + "\n";
    }
    if (StringUtil.isEmpty(accessKey)) {
      validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.AccessKey.Error") + "\n";
    }
    if (StringUtil.isEmpty(secretKey)) {
      validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.SecretKey.Error") + "\n";
    }
    if (StringUtil.isEmpty(stagingDir)) {
      validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.StagingDir.Error") + "\n";
    } else {
      try {
        if (!AmazonSpoonPlugin.S3_SCHEME.equalsIgnoreCase(KettleVFS.getFileObject(stagingDir).getName().getScheme()) && stagingDir.indexOf("://") != -1) {
          // if the stagingDir name does not contain a :// then we know there is no protocol
          // we will just use the aws keys and use the dir as it is given
          validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.StagingDir.Error") + "\n";
        }
      } catch (Throwable t) {
        if (stagingDir.indexOf("://") != -1) {
          // if the stagingDir name does not contain a :// then we know there is no protocol
          // we will just use the aws keys and use the dir as it is given
          validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.StagingDir.Error") + "\n";
        }
      }
    }
    /*if (numInstances <= 0) {
      validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.NumInstances.Error") + "\n";
    }*/
    if (StringUtil.isEmpty(masterInstanceType)) {
      validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.MasterInstanceType.Error") + "\n";
    }
    if (StringUtil.isEmpty(slaveInstanceType)) {
      validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.SlaveInstanceType.Error") + "\n";
    }

    if (!StringUtil.isEmpty(validationErrors)) {
      openErrorDialog(BaseMessages.getString(PKG, "Dialog.Error"), validationErrors);
      // show validation errors dialog
      return;
    }

    // common/simple
    jobEntry.setName(jobEntryName);
    jobEntry.setHadoopJobName(hadoopJobName);
    jobEntry.setHadoopJobFlowId(hadoopJobFlowId);
    jobEntry.setJarUrl(jarUrl);
    jobEntry.setAccessKey(accessKey);
    jobEntry.setSecretKey(secretKey);
    jobEntry.setStagingDir(stagingDir);
    jobEntry.setNumInstances(numInstances);
    jobEntry.setMasterInstanceType(masterInstanceType);
    jobEntry.setSlaveInstanceType(slaveInstanceType);
    jobEntry.setCmdLineArgs(getCommandLineArgs());
    // advanced config
    jobEntry.setBlocking(isBlocking());
    jobEntry.setLoggingInterval(getLoggingInterval());

    jobEntry.setChanged();

    cancel();
  }

  public void init() {
    if (jobEntry != null) {
      // common/simple
      setName(jobEntry.getName());
      setJobEntryName(jobEntry.getName());
      setHadoopJobName(jobEntry.getHadoopJobName());
      setHadoopJobFlowId(jobEntry.getHadoopJobFlowId());
      setJarUrl(jobEntry.getJarUrl());
      setAccessKey(jobEntry.getAccessKey());
      setSecretKey(jobEntry.getSecretKey());
      setStagingDir(jobEntry.getStagingDir());
      setNumInstances(jobEntry.getNumInstances());
      setMasterInstanceType(jobEntry.getMasterInstanceType());
      setSlaveInstanceType(jobEntry.getSlaveInstanceType());
      setCommandLineArgs(jobEntry.getCmdLineArgs());
      setBlocking(jobEntry.isBlocking());
      setLoggingInterval(jobEntry.getLoggingInterval());
    }
  }

  public void cancel() {
    XulDialog xulDialog = (XulDialog) getXulDomContainer().getDocumentRoot().getElementById("amazon-emr-job-entry-dialog");
    Shell shell = (Shell) xulDialog.getRootObject();
    if (!shell.isDisposed()) {
      WindowProperty winprop = new WindowProperty(shell);
      PropsUI.getInstance().setScreen(winprop);
      ((Composite) xulDialog.getManagedObject()).dispose();
      shell.dispose();
    }
  }

  public void openErrorDialog(String title, String message) {
    XulDialog errorDialog = (XulDialog) getXulDomContainer().getDocumentRoot().getElementById("amazon-emr-error-dialog");
    errorDialog.setTitle(title);

    XulTextbox errorMessage = (XulTextbox) getXulDomContainer().getDocumentRoot().getElementById("amazon-emr-error-message");
    errorMessage.setValue(message);

    errorDialog.show();
  }

  public void closeErrorDialog() {
    XulDialog errorDialog = (XulDialog) getXulDomContainer().getDocumentRoot().getElementById("amazon-emr-error-dialog");
    errorDialog.hide();
  }

  public void browseJar() throws KettleException, FileSystemException {
    String[] fileFilters = new String[] { "*.jar;*.zip" };
    String[] fileFilterNames = new String[] { "Java Archives (jar)" };

    // Get current file
    FileObject rootFile = null;
    FileObject initialFile = null;
    FileObject defaultInitialFile = null;

    if (jarUrl != null) {
      if (jarUrl.startsWith(AmazonSpoonPlugin.S3_SCHEME)) {
        try {
          String str = getVariableSpace().environmentSubstitute(jarUrl).substring(AmazonSpoonPlugin.S3_SCHEME.length() + 3);
          str = str.substring(0, str.indexOf("@s3")).replaceAll("\\+", "%2B").replaceAll("/", "%2F");
          str = AmazonSpoonPlugin.S3_SCHEME + "://" + str + "@s3" + jarUrl.substring(jarUrl.indexOf("@s3") + 3);
          initialFile = KettleVFS.getFileObject(str);
        } catch (Throwable ignored) {
          initialFile = KettleVFS.getFileObject(Spoon.getInstance().getLastFileOpened());
        }
      } else {
        initialFile = KettleVFS.getFileObject(getVariableSpace().environmentSubstitute(jarUrl));
      }
    } else {
      initialFile = KettleVFS.getFileObject(Spoon.getInstance().getLastFileOpened());
    }
    rootFile = initialFile.getFileSystem().getRoot();

    defaultInitialFile = KettleVFS.getFileObject("file:///c:/");
    if (rootFile == null) {
      rootFile = defaultInitialFile.getFileSystem().getRoot();
      initialFile = defaultInitialFile;
    }

    XulDialog xulDialog = (XulDialog) getXulDomContainer().getDocumentRoot().getElementById("amazon-emr-job-entry-dialog");
    Shell shell = (Shell) xulDialog.getRootObject();

    IVfsFileChooser fileChooserDialog = Spoon.getInstance().getVfsFileChooserDialog(rootFile, initialFile);
    FileObject selectedFile = fileChooserDialog.open(shell, defaultInitialFile, null, fileFilters, fileFilterNames,
        VfsFileChooserDialog.VFS_DIALOG_OPEN_FILE_OR_DIRECTORY);

    if (selectedFile != null) {

      String filename = selectedFile.getName().getURI();

      if (selectedFile.getName().getScheme().equals(AmazonSpoonPlugin.S3_SCHEME)) {
        // try S3 name substitution
        for (CustomVfsUiPanel panel : Spoon.getInstance().getVfsFileChooserDialog(null, null).getCustomVfsUiPanels()) {
          if (panel.getVfsScheme().equals(AmazonSpoonPlugin.S3_SCHEME)) {
            S3VfsFileChooserDialog s3panel = (S3VfsFileChooserDialog) panel;
            filename = buildS3FileSystemUrlString(s3panel.getAccessKey(), s3panel.getSecretKey(), selectedFile.getName().getPath());
            break;
          }
        }
      }

      setJarUrl(filename);
    }
  }

  public void browseS3StagingDir() throws KettleException, FileSystemException {
    String[] fileFilters = new String[] { "*.*" };
    String[] fileFilterNames = new String[] { "All" };

    // Get current file
    FileObject rootFile = null;
    FileObject initialFile = null;
    FileObject defaultInitialFile = null;

    if (stagingDir != null) {
      if (stagingDir.startsWith(AmazonSpoonPlugin.S3_SCHEME)) {
        try {
          String str = getVariableSpace().environmentSubstitute(stagingDir).substring(AmazonSpoonPlugin.S3_SCHEME.length() + 3);
          str = str.substring(0, str.indexOf("@s3")).replaceAll("\\+", "%2B").replaceAll("/", "%2F");
          str = AmazonSpoonPlugin.S3_SCHEME + "://" + str + "@s3" + jarUrl.substring(jarUrl.indexOf("@s3") + 3);
          initialFile = KettleVFS.getFileObject(str);
        } catch (Throwable ignored) {
          initialFile = KettleVFS.getFileObject(Spoon.getInstance().getLastFileOpened());
        }
      } else {
        initialFile = KettleVFS.getFileObject(getVariableSpace().environmentSubstitute(stagingDir));
      }
    } else {
      initialFile = KettleVFS.getFileObject(Spoon.getInstance().getLastFileOpened());
    }
    rootFile = initialFile.getFileSystem().getRoot();

    defaultInitialFile = KettleVFS.getFileObject("file:///c:/");
    if (rootFile == null) {
      rootFile = defaultInitialFile.getFileSystem().getRoot();
      initialFile = defaultInitialFile;
    }
    XulDialog xulDialog = (XulDialog) getXulDomContainer().getDocumentRoot().getElementById("amazon-emr-job-entry-dialog");
    Shell shell = (Shell) xulDialog.getRootObject();

    VfsFileChooserDialog fileChooserDialog = Spoon.getInstance().getVfsFileChooserDialog(rootFile, initialFile);
    fileChooserDialog.defaultInitialFile = defaultInitialFile;
    FileObject selectedFile = fileChooserDialog.open(shell, null, AmazonSpoonPlugin.S3_SCHEME, false, null, fileFilters, fileFilterNames,
        VfsFileChooserDialog.VFS_DIALOG_OPEN_DIRECTORY);

    if (selectedFile != null) {

      String filename = selectedFile.getName().getURI();

      if (selectedFile.getName().getScheme().equals(AmazonSpoonPlugin.S3_SCHEME)) {
        // try S3 name substitution
        for (CustomVfsUiPanel panel : Spoon.getInstance().getVfsFileChooserDialog(null, null).getCustomVfsUiPanels()) {
          if (panel.getVfsScheme().equals(AmazonSpoonPlugin.S3_SCHEME)) {
            S3VfsFileChooserDialog s3panel = (S3VfsFileChooserDialog) panel;
            filename = buildS3FileSystemUrlString(s3panel.getAccessKey(), s3panel.getSecretKey(), selectedFile.getName().getPath());
            break;
          }
        }
      }

      setStagingDir(filename);
    }
  }

  protected VariableSpace getVariableSpace() {
    if (Spoon.getInstance().getActiveTransformation() != null) {
      return Spoon.getInstance().getActiveTransformation();
    } else if (Spoon.getInstance().getActiveJob() != null) {
      return Spoon.getInstance().getActiveJob();
    } else {
      return new Variables();
    }
  }

  private String buildS3FileSystemUrlString(String accessKey, String secretKey, String path) {
    return AmazonSpoonPlugin.S3_SCHEME + "://" + accessKey + ":" + secretKey + "@s3" + path;
  }

  @Override
  public String getName() {
    return "jobEntryController"; //$NON-NLS-1$
  }

  public String getJobEntryName() {
    return jobEntryName;
  }

  public void setJobEntryName(String jobEntryName) {
    String previousVal = this.jobEntryName;
    String newVal = jobEntryName;

    this.jobEntryName = jobEntryName;
    firePropertyChange(AmazonElasticMapReduceJobExecutorController.JOB_ENTRY_NAME, previousVal, newVal);
  }

  public String getHadoopJobName() {
    return hadoopJobName;
  }

  public void setHadoopJobName(String hadoopJobName) {
    String previousVal = this.hadoopJobName;
    String newVal = hadoopJobName;

    this.hadoopJobName = hadoopJobName;
    firePropertyChange(AmazonElasticMapReduceJobExecutorController.HADOOP_JOB_NAME, previousVal, newVal);
  }

  public String getHadoopJobFlowId() {
    return hadoopJobFlowId;
  }

  public void setHadoopJobFlowId(String hadoopJobFlowId) {
    String previousVal = this.hadoopJobFlowId;
    String newVal = hadoopJobFlowId;

    this.hadoopJobFlowId = hadoopJobFlowId;
    firePropertyChange(AmazonElasticMapReduceJobExecutorController.HADOOP_JOB_FLOW_ID, previousVal, newVal);
  }

  public String getJarUrl() {
    return jarUrl;
  }

  public void setJarUrl(String jarUrl) {
    String previousVal = this.jarUrl;
    String newVal = jarUrl;

    this.jarUrl = jarUrl;
    firePropertyChange(AmazonElasticMapReduceJobExecutorController.JAR_URL, previousVal, newVal);
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    String previousVal = this.accessKey;
    String newVal = accessKey;

    this.accessKey = accessKey;
    firePropertyChange(AmazonElasticMapReduceJobExecutorController.ACCESS_KEY, previousVal, newVal);
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    String previousVal = this.secretKey;
    String newVal = secretKey;

    this.secretKey = secretKey;
    firePropertyChange(AmazonElasticMapReduceJobExecutorController.SECRET_KEY, previousVal, newVal);
  }

  public String getStagingDir() {
    return stagingDir;
  }

  public void setStagingDir(String stagingDir) {
    String previousVal = this.stagingDir;
    String newVal = stagingDir;

    this.stagingDir = stagingDir;
    firePropertyChange(AmazonElasticMapReduceJobExecutorController.STAGING_DIR, previousVal, newVal);
  }

  public String getNumInstances() {
    return numInstances;
  }

  public void setNumInstances(String numInstances) {
    String previousVal = this.numInstances;
    String newVal = numInstances;

    this.numInstances = numInstances;
    firePropertyChange(AmazonElasticMapReduceJobExecutorController.NUM_INSTANCES, previousVal, newVal);
  }

  public String getMasterInstanceType() {
    return masterInstanceType;
  }

  public void setMasterInstanceType(String masterInstanceType) {
    String previousVal = this.masterInstanceType;
    String newVal = masterInstanceType;

    this.masterInstanceType = masterInstanceType;
    firePropertyChange(AmazonElasticMapReduceJobExecutorController.MASTER_INSTANCE_TYPE, previousVal, newVal);
  }

  public String getSlaveInstanceType() {
    return slaveInstanceType;
  }

  public void setSlaveInstanceType(String slaveInstanceType) {
    String previousVal = this.slaveInstanceType;
    String newVal = slaveInstanceType;

    this.slaveInstanceType = slaveInstanceType;
    firePropertyChange(AmazonElasticMapReduceJobExecutorController.SLAVE_INSTANCE_TYPE, previousVal, newVal);
  }

  public void invertBlocking() {
    setBlocking(!isBlocking());
  }

  public AmazonElasticMapReduceJobExecutor getJobEntry() {
    return jobEntry;
  }

  public void setJobEntry(AmazonElasticMapReduceJobExecutor jobEntry) {
    this.jobEntry = jobEntry;
  }

  public String getCommandLineArgs() {
    return cmdLineArgs;
  }

  public void setCommandLineArgs(String cmdLineArgs) {
    String previousVal = this.cmdLineArgs;
    String newVal = cmdLineArgs;

    this.cmdLineArgs = cmdLineArgs;

    firePropertyChange(CMD_LINE_ARGS, previousVal, newVal);
  }

  public boolean isBlocking() {
    return blocking;
  }

  public void setBlocking(boolean blocking) {
    boolean previousVal = this.blocking;
    boolean newVal = blocking;

    this.blocking = blocking;
    firePropertyChange(BLOCKING, previousVal, newVal);
  }

  public String getLoggingInterval() {
    return loggingInterval;
  }

  public void setLoggingInterval(String loggingInterval) {
    String previousVal = this.loggingInterval;
    String newVal = loggingInterval;

    this.loggingInterval = loggingInterval;
    firePropertyChange(LOGGING_INTERVAL, previousVal, newVal);
  }

}