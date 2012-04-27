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

package org.pentaho.amazon.hive.ui;

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
import org.pentaho.amazon.hive.job.AmazonHiveJobExecutor;
import org.pentaho.amazon.s3.S3VfsFileChooserDialog;

/**
 * AmazonHiveJobExecutorController:
 *   Handles the attribute dialog box UI for AmazonHiveJobExecutor class.
 */
public class AmazonHiveJobExecutorController extends AbstractXulEventHandler {

  private static final Class<?> PKG = AmazonHiveJobExecutor.class;

  // Define string names for the attributes.
  public static final String JOB_ENTRY_NAME = "jobEntryName"; //$NON-NLS-1$
  public static final String HADOOP_JOB_NAME = "hadoopJobName"; //$NON-NLS-1$
  public static final String HADOOP_JOB_FLOW_ID = "hadoopJobFlowId"; //$NON-NLS-1$
  public static final String JAR_URL = "jarUrl"; //$NON-NLS-1$
  public static final String Q_URL = "qUrl"; //$NON-NLS-1$
  public static final String ACCESS_KEY = "accessKey"; //$NON-NLS-1$
  public static final String SECRET_KEY = "secretKey"; //$NON-NLS-1$
  public static final String BOOTSTRAP_ACTIONS = "bootstrapActions"; //$NON-NLS-1$
  public static final String STAGING_DIR = "stagingDir"; //$NON-NLS-1$
  public static final String NUM_INSTANCES = "numInstances"; //$NON-NLS-1$
  public static final String MASTER_INSTANCE_TYPE = "masterInstanceType"; //$NON-NLS-1$
  public static final String SLAVE_INSTANCE_TYPE = "slaveInstanceType"; //$NON-NLS-1$
  public static final String USER_DEFINED = "userDefined"; //$NON-NLS-1$
  public static final String CMD_LINE_ARGS = "commandLineArgs"; //$NON-NLS-1$
  public static final String ALIVE = "alive"; //$NON-NLS-1$
  public static final String BLOCKING = "blocking"; //$NON-NLS-1$
  public static final String LOGGING_INTERVAL = "loggingInterval"; //$NON-NLS-1$

  // Attributes
  private String jobEntryName;
  private String hadoopJobName;
  private String hadoopJobFlowId;
  private String jarUrl = "";
  private String qUrl = "";
  private String accessKey = "";
  private String secretKey = "";
  private String bootstrapActions = "";
  private String stagingDir = "";
  private String numInstances = "2"; //$NON-NLS-1$
  private String masterInstanceType = "m1.small"; //$NON-NLS-1$
  private String slaveInstanceType = "m1.small"; //$NON-NLS-1$
  private String cmdLineArgs;
  private boolean alive = false;
  private boolean blocking;
  private String loggingInterval = "10"; // 10 seconds //$NON-NLS-1$

  private AmazonHiveJobExecutor jobEntry;

  /*
   * Process attribute data when OK button is presses in the attribute dialog box.
   */
  public void accept() {

    // Get the attribute data from the dialog box.
    ExtTextbox tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById("jobentry-hadoopjob-name"); //$NON-NLS-1$
    this.hadoopJobName = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById("access-key"); //$NON-NLS-1$
    this.accessKey = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById("secret-key"); //$NON-NLS-1$
    this.secretKey = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById("jobentry-hadoopjob-flow-id"); //$NON-NLS-1$
    this.hadoopJobFlowId = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById("bootstrap-actions"); //$NON-NLS-1$
    this.stagingDir = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById("s3-staging-directory"); //$NON-NLS-1$
    this.stagingDir = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById("q-url"); //$NON-NLS-1$
    this.qUrl = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById("command-line-arguments"); //$NON-NLS-1$
    this.cmdLineArgs = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById("num-instances"); //$NON-NLS-1$
    this.numInstances = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById("logging-interval"); //$NON-NLS-1$
    this.loggingInterval = ((Text) tempBox.getTextControl()).getText();

    // Check if any required data is missing.
    String validationErrors = "";
    if (StringUtil.isEmpty(jobEntryName)) {
      validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.JobEntryName.Error") + "\n"; //$NON-NLS-1$ //$NON-NLS-1$
    }
    if (StringUtil.isEmpty(hadoopJobName)) {
      validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.JobFlowName.Error") + "\n"; //$NON-NLS-1$ //$NON-NLS-1$
    }
    if (StringUtil.isEmpty(qUrl)) {
      validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.QURL.Error") + "\n"; //$NON-NLS-1$ //$NON-NLS-1$
    }
    if (StringUtil.isEmpty(accessKey)) {
      validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.AccessKey.Error") + "\n"; //$NON-NLS-1$ //$NON-NLS-1$
    }
    if (StringUtil.isEmpty(secretKey)) {
      validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.SecretKey.Error") + "\n"; //$NON-NLS-1$ //$NON-NLS-1$
    }
    if (StringUtil.isEmpty(stagingDir)) {
      validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.StagingDir.Error") + "\n"; //$NON-NLS-1$ //$NON-NLS-1$
    } else {
      try {
        if (!AmazonSpoonPlugin.S3_SCHEME.equalsIgnoreCase(KettleVFS.getFileObject(stagingDir).getName().getScheme()) && stagingDir.indexOf("://") != -1) { //$NON-NLS-1$
          // Check if the stagingDir name contains "s3://".
          validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.StagingDir.Error") + "\n"; //$NON-NLS-1$ //$NON-NLS-1$
        }
      } catch (Throwable t) {
        if (stagingDir.indexOf("://") == -1) { //$NON-NLS-1$
          // Check if the stagingDir name contains "://".
          validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.StagingDir.Error") + "\n"; //$NON-NLS-1$ //$NON-NLS-1$
        }
      }
    }
    if (StringUtil.isEmpty(masterInstanceType)) {
      validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.MasterInstanceType.Error") + "\n"; //$NON-NLS-1$ //$NON-NLS-1$
    }
    if (StringUtil.isEmpty(slaveInstanceType)) {
      validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.SlaveInstanceType.Error") + "\n"; //$NON-NLS-1$ //$NON-NLS-1$
    }

    if (!StringUtil.isEmpty(validationErrors)) {
      openErrorDialog(BaseMessages.getString(PKG, "Dialog.Error"), validationErrors); //$NON-NLS-1$
      // show validation errors dialog
      return;
    }

    jobEntry.setName(jobEntryName);
    jobEntry.setHadoopJobName(hadoopJobName);
    jobEntry.setHadoopJobFlowId(hadoopJobFlowId);
    jobEntry.setJarUrl(jarUrl);
    jobEntry.setQUrl(qUrl);
    jobEntry.setAccessKey(accessKey);
    jobEntry.setSecretKey(secretKey);
    jobEntry.setBootstrapActions(bootstrapActions);
    jobEntry.setStagingDir(stagingDir);
    jobEntry.setNumInstances(numInstances);
    jobEntry.setMasterInstanceType(masterInstanceType);
    jobEntry.setSlaveInstanceType(slaveInstanceType);
    jobEntry.setCmdLineArgs(getCommandLineArgs());
    jobEntry.setAlive(isAlive());
    jobEntry.setBlocking(isBlocking());
    jobEntry.setLoggingInterval(getLoggingInterval());

    jobEntry.setChanged();

    cancel();
  }

  /*
   * Initialize attributes.
   */
  public void init() {
    if (jobEntry != null) {
      setName(jobEntry.getName());
      setJobEntryName(jobEntry.getName());
      setHadoopJobName(jobEntry.getHadoopJobName());
      setHadoopJobFlowId(jobEntry.getHadoopJobFlowId());
      setQUrl(jobEntry.getQUrl());
      setAccessKey(jobEntry.getAccessKey());
      setSecretKey(jobEntry.getSecretKey());
      setStagingDir(jobEntry.getStagingDir());
      setBootstrapActions(jobEntry.getBootstrapActions());
      setNumInstances(jobEntry.getNumInstances());
      setMasterInstanceType(jobEntry.getMasterInstanceType());
      setSlaveInstanceType(jobEntry.getSlaveInstanceType());
      setCommandLineArgs(jobEntry.getCmdLineArgs());
      setAlive(jobEntry.isAlive());
      setBlocking(jobEntry.isBlocking());
      setLoggingInterval(jobEntry.getLoggingInterval());
    }
  }

  /*
   * Close the attribute dialog box when Cancel button is presses.
   */
  public void cancel() {
    XulDialog xulDialog = (XulDialog) getXulDomContainer().getDocumentRoot().getElementById("amazon-emr-job-entry-dialog"); //$NON-NLS-1$
    Shell shell = (Shell) xulDialog.getRootObject();
    if (!shell.isDisposed()) {
      WindowProperty winprop = new WindowProperty(shell);
      PropsUI.getInstance().setScreen(winprop);
      ((Composite) xulDialog.getManagedObject()).dispose();
      shell.dispose();
    }
  }

  /*
   * Open the error message box.
   */
  public void openErrorDialog(String title, String message) {
    XulDialog errorDialog = (XulDialog) getXulDomContainer().getDocumentRoot().getElementById("amazon-emr-error-dialog"); //$NON-NLS-1$
    errorDialog.setTitle(title);

    XulTextbox errorMessage = (XulTextbox) getXulDomContainer().getDocumentRoot().getElementById("amazon-emr-error-message"); //$NON-NLS-1$
    errorMessage.setValue(message);

    errorDialog.show();
  }

  /*
   * Close the error message box.
   */
  public void closeErrorDialog() {
    XulDialog errorDialog = (XulDialog) getXulDomContainer().getDocumentRoot().getElementById("amazon-emr-error-dialog"); //$NON-NLS-1$
    errorDialog.hide();
  }

  
  /*
   * Open VFS Browser when the "Browse..." button next to the "Hive Script" text box is pressed in the attribute dialog box.
   */
  public void browseQ() throws KettleException, FileSystemException {
    String[] fileFilters = new String[] { "*.*" }; //$NON-NLS-1$
    String[] fileFilterNames = new String[] { "All" }; //$NON-NLS-1$

    // Get current file
    FileObject rootFile = null;
    FileObject initialFile = null;
    FileObject defaultInitialFile = null;

    if (qUrl != null) {
      if (qUrl.startsWith(AmazonSpoonPlugin.S3_SCHEME)) {
        try {
          String str = getVariableSpace().environmentSubstitute(qUrl).substring(AmazonSpoonPlugin.S3_SCHEME.length() + 3);
          str = str.substring(0, str.indexOf("@s3")).replaceAll("\\+", "%2B").replaceAll("/", "%2F"); //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$
          str = AmazonSpoonPlugin.S3_SCHEME + "://" + str + "@s3" + qUrl.substring(qUrl.indexOf("@s3") + 3); //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$
          initialFile = KettleVFS.getFileObject(str);
        } catch (Throwable ignored) {
          initialFile = KettleVFS.getFileObject(Spoon.getInstance().getLastFileOpened());
        }
      } else {
        initialFile = KettleVFS.getFileObject(getVariableSpace().environmentSubstitute(qUrl));
      }
    } else {
      initialFile = KettleVFS.getFileObject(Spoon.getInstance().getLastFileOpened());
    }
    rootFile = initialFile.getFileSystem().getRoot();

    defaultInitialFile = KettleVFS.getFileObject("file:///c:/"); //$NON-NLS-1$
    if (rootFile == null) {
      rootFile = defaultInitialFile.getFileSystem().getRoot();
      initialFile = defaultInitialFile;
    }

    XulDialog xulDialog = (XulDialog) getXulDomContainer().getDocumentRoot().getElementById("amazon-emr-job-entry-dialog"); //$NON-NLS-1$
    Shell shell = (Shell) xulDialog.getRootObject();

    // Open the browser dialog box.
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

      setQUrl(filename);
    }
  }

  /*
   * Open VFS Browser when the "Browse..." button next to the "S3 Staging Directory" text box is pressed in the attribute dialog box.
   */
  public void browseS3StagingDir() throws KettleException, FileSystemException {
    String[] fileFilters = new String[] { "*.*" }; //$NON-NLS-1$
    String[] fileFilterNames = new String[] { "All" }; //$NON-NLS-1$

    // Get current file
    FileObject rootFile = null;
    FileObject initialFile = null;
    FileObject defaultInitialFile = null;

    if (stagingDir != null) {
      if (stagingDir.startsWith(AmazonSpoonPlugin.S3_SCHEME)) {
        try {
          String str = getVariableSpace().environmentSubstitute(stagingDir).substring(AmazonSpoonPlugin.S3_SCHEME.length() + 3);
          str = str.substring(0, str.indexOf("@s3")).replaceAll("\\+", "%2B").replaceAll("/", "%2F"); //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$
          str = AmazonSpoonPlugin.S3_SCHEME + "://" + str + "@s3" + jarUrl.substring(jarUrl.indexOf("@s3") + 3); //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$
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

    defaultInitialFile = KettleVFS.getFileObject("file:///c:/"); //$NON-NLS-1$
    if (rootFile == null) {
      rootFile = defaultInitialFile.getFileSystem().getRoot();
      initialFile = defaultInitialFile;
    }
    XulDialog xulDialog = (XulDialog) getXulDomContainer().getDocumentRoot().getElementById("amazon-emr-job-entry-dialog"); //$NON-NLS-1$
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
    return AmazonSpoonPlugin.S3_SCHEME + "://" + accessKey + ":" + secretKey + "@s3" + path; //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$
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
    firePropertyChange(AmazonHiveJobExecutorController.JOB_ENTRY_NAME, previousVal, newVal);
  }

  public String getHadoopJobName() {
    return hadoopJobName;
  }

  public void setHadoopJobName(String hadoopJobName) {
    String previousVal = this.hadoopJobName;
    String newVal = hadoopJobName;

    this.hadoopJobName = hadoopJobName;
    firePropertyChange(AmazonHiveJobExecutorController.HADOOP_JOB_NAME, previousVal, newVal);
  }

  public String getHadoopJobFlowId() {
    return hadoopJobFlowId;
  }

  public void setHadoopJobFlowId(String hadoopJobFlowId) {
    String previousVal = this.hadoopJobFlowId;
    String newVal = hadoopJobFlowId;

    this.hadoopJobFlowId = hadoopJobFlowId;
    firePropertyChange(AmazonHiveJobExecutorController.HADOOP_JOB_FLOW_ID, previousVal, newVal);
  }

  public String getJarUrl() {
    return jarUrl;
  }

  public void setJarUrl(String jarUrl) {
    String previousVal = this.jarUrl;
    String newVal = jarUrl;

    this.jarUrl = jarUrl;
    firePropertyChange(AmazonHiveJobExecutorController.JAR_URL, previousVal, newVal);
  }

  public String getQUrl() {
    return jarUrl;
  }

  public void setQUrl(String qUrl) {
    String previousVal = this.qUrl;
    String newVal = qUrl;

    this.qUrl = qUrl;
    firePropertyChange(AmazonHiveJobExecutorController.Q_URL, previousVal, newVal);
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    String previousVal = this.accessKey;
    String newVal = accessKey;

    this.accessKey = accessKey;
    firePropertyChange(AmazonHiveJobExecutorController.ACCESS_KEY, previousVal, newVal);
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    String previousVal = this.secretKey;
    String newVal = secretKey;

    this.secretKey = secretKey;
    firePropertyChange(AmazonHiveJobExecutorController.SECRET_KEY, previousVal, newVal);
  }

  public String getBootstrapActions() {
    return bootstrapActions;
  }

  public void setBootstrapActions(String bootstrapActions) {
    String previousVal = this.bootstrapActions;
    String newVal = bootstrapActions;

    this.bootstrapActions = bootstrapActions;
    firePropertyChange(AmazonHiveJobExecutorController.BOOTSTRAP_ACTIONS, previousVal, newVal);
  }

  public String getStagingDir() {
    return stagingDir;
  }

  public void setStagingDir(String stagingDir) {
    String previousVal = this.stagingDir;
    String newVal = stagingDir;

    this.stagingDir = stagingDir;
    firePropertyChange(AmazonHiveJobExecutorController.STAGING_DIR, previousVal, newVal);
  }

  public String getNumInstances() {
    return numInstances;
  }

  public void setNumInstances(String numInstances) {
    String previousVal = this.numInstances;
    String newVal = numInstances;

    this.numInstances = numInstances;
    firePropertyChange(AmazonHiveJobExecutorController.NUM_INSTANCES, previousVal, newVal);
  }

  public String getMasterInstanceType() {
    return masterInstanceType;
  }

  public void setMasterInstanceType(String masterInstanceType) {
    String previousVal = this.masterInstanceType;
    String newVal = masterInstanceType;

    this.masterInstanceType = masterInstanceType;
    firePropertyChange(AmazonHiveJobExecutorController.MASTER_INSTANCE_TYPE, previousVal, newVal);
  }

  public String getSlaveInstanceType() {
    return slaveInstanceType;
  }

  public void setSlaveInstanceType(String slaveInstanceType) {
    String previousVal = this.slaveInstanceType;
    String newVal = slaveInstanceType;

    this.slaveInstanceType = slaveInstanceType;
    firePropertyChange(AmazonHiveJobExecutorController.SLAVE_INSTANCE_TYPE, previousVal, newVal);
  }

  public void invertAlive() {
    setAlive(!isAlive());
  }

  public void invertBlocking() {
    setBlocking(!isBlocking());
  }

  public AmazonHiveJobExecutor getJobEntry() {
    return jobEntry;
  }

  public void setJobEntry(AmazonHiveJobExecutor jobEntry) {
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

  public boolean isAlive() {
    return alive;
  }

  public void setAlive(boolean alive) {
    boolean previousVal = this.alive;
    this.alive = alive;
    firePropertyChange(ALIVE, previousVal, alive);
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