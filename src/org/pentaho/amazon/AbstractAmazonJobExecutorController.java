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

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.amazon.emr.job.AmazonElasticMapReduceJobExecutor;
import org.pentaho.amazon.s3.S3VfsFileChooserHelper;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
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
import org.pentaho.vfs.ui.VfsFileChooserDialog;

/**
 * created by: rfellows
 * date:       5/24/12
 */
public abstract class AbstractAmazonJobExecutorController extends AbstractXulEventHandler {

  private static final Class<?> PKG = AmazonElasticMapReduceJobExecutor.class;

  /* property change names */
  public static final String JOB_ENTRY_NAME = "jobEntryName"; //$NON-NLS-1$
  public static final String HADOOP_JOB_NAME = "hadoopJobName"; //$NON-NLS-1$
  public static final String HADOOP_JOB_FLOW_ID = "hadoopJobFlowId"; //$NON-NLS-1$
  public static final String JAR_URL = "jarUrl"; //$NON-NLS-1$
  public static final String ACCESS_KEY = "accessKey"; //$NON-NLS-1$
  public static final String SECRET_KEY = "secretKey"; //$NON-NLS-1$
  public static final String STAGING_DIR = "stagingDir"; //$NON-NLS-1$
  public static final String STAGING_DIR_FILE = "stagingDirFile"; //$NON-NLS-1$
  public static final String NUM_INSTANCES = "numInstances"; //$NON-NLS-1$
  public static final String MASTER_INSTANCE_TYPE = "masterInstanceType"; //$NON-NLS-1$
  public static final String SLAVE_INSTANCE_TYPE = "slaveInstanceType"; //$NON-NLS-1$
  public static final String CMD_LINE_ARGS = "commandLineArgs"; //$NON-NLS-1$
  public static final String BLOCKING = "blocking"; //$NON-NLS-1$
  public static final String LOGGING_INTERVAL = "loggingInterval"; //$NON-NLS-1$


  /* XUL Element id's */
  public static final String XUL_JOBENTRY_HADOOPJOB_NAME = "jobentry-hadoopjob-name";
  public static final String XUL_ACCESS_KEY1 = "access-key";
  public static final String XUL_SECRET_KEY1 = "secret-key";
  public static final String XUL_JOBENTRY_HADOOPJOB_FLOW_ID = "jobentry-hadoopjob-flow-id";
  public static final String XUL_S3_STAGING_DIRECTORY = "s3-staging-directory";
  public static final String XUL_COMMAND_LINE_ARGUMENTS = "command-line-arguments";
  public static final String XUL_NUM_INSTANCES1 = "num-instances";
  public static final String XUL_LOGGING_INTERVAL1 = "logging-interval";
  public static final String XUL_AMAZON_EMR_JOB_ENTRY_DIALOG = "amazon-emr-job-entry-dialog";
  public static final String XUL_AMAZON_EMR_ERROR_DIALOG = "amazon-emr-error-dialog";
  public static final String XUL_AMAZON_EMR_ERROR_MESSAGE = "amazon-emr-error-message";
  public static final String XUL_JAR_URL = "jar-url";

  /* Messages */
  public static final String MSG_AMAZON_ELASTIC_MAP_REDUCE_JOB_EXECUTOR_JOB_ENTRY_NAME_ERROR = "AmazonElasticMapReduceJobExecutor.JobEntryName.Error";
  public static final String MSG_AMAZON_ELASTIC_MAP_REDUCE_JOB_EXECUTOR_JOB_FLOW_NAME_ERROR = "AmazonElasticMapReduceJobExecutor.JobFlowName.Error";
  public static final String MSG_AMAZON_ELASTIC_MAP_REDUCE_JOB_EXECUTOR_ACCESS_KEY_ERROR = "AmazonElasticMapReduceJobExecutor.AccessKey.Error";
  public static final String MSG_AMAZON_ELASTIC_MAP_REDUCE_JOB_EXECUTOR_SECRET_KEY_ERROR = "AmazonElasticMapReduceJobExecutor.SecretKey.Error";
  public static final String MSG_AMAZON_ELASTIC_MAP_REDUCE_JOB_EXECUTOR_STAGING_DIR_ERROR = "AmazonElasticMapReduceJobExecutor.StagingDir.Error";
  public static final String MSG_AMAZON_ELASTIC_MAP_REDUCE_JOB_EXECUTOR_MASTER_INSTANCE_TYPE_ERROR = "AmazonElasticMapReduceJobExecutor.MasterInstanceType.Error";
  public static final String MSG_AMAZON_ELASTIC_MAP_REDUCE_JOB_EXECUTOR_SLAVE_INSTANCE_TYPE_ERROR = "AmazonElasticMapReduceJobExecutor.SlaveInstanceType.Error";
  public static final String MSG_DIALOG_ERROR = "Dialog.Error";
  public static final String MSG_AMAZON_ELASTIC_MAP_REDUCE_JOB_EXECUTOR_JAR_URL_ERROR = "AmazonElasticMapReduceJobExecutor.JarURL.Error";


  protected String jobEntryName;
  protected String hadoopJobName;
  protected String hadoopJobFlowId;
  protected String accessKey = "";
  protected String secretKey = "";

  protected String stagingDir = "";
  protected FileObject stagingDirFile = null;
  protected String jarUrl = "";

  protected String numInstances = "2";
  protected String masterInstanceType = "m1.small";
  protected String slaveInstanceType = "m1.small";
  protected String cmdLineArgs;
  protected boolean blocking;
  protected String loggingInterval = "60"; // 60 seconds

  protected VfsFileChooserDialog fileChooserDialog;
  protected S3VfsFileChooserHelper helper;

  public void accept() {

    syncModel();

    String validationErrors = buildValidationErrorMessages();

    if (!StringUtil.isEmpty(validationErrors)) {
      openErrorDialog(BaseMessages.getString(PKG, MSG_DIALOG_ERROR), validationErrors);
      // show validation errors dialog
      return;
    }

    configureJobEntry();

    cancel();
  }

  protected void syncModel() {
    ExtTextbox tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById(XUL_JOBENTRY_HADOOPJOB_NAME);
    this.hadoopJobName = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById(XUL_ACCESS_KEY1);
    this.accessKey = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById(XUL_SECRET_KEY1);
    this.secretKey = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById(XUL_JOBENTRY_HADOOPJOB_FLOW_ID);
    this.hadoopJobFlowId = ((Text) tempBox.getTextControl()).getText();

    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById(XUL_S3_STAGING_DIRECTORY);
    this.stagingDir = ((Text) tempBox.getTextControl()).getText();
    try {
      this.stagingDirFile = resolveFile(this.stagingDir);
    } catch (Exception e) {
      this.stagingDirFile = null;
    }

    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById(XUL_COMMAND_LINE_ARGUMENTS);
    this.cmdLineArgs = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById(XUL_NUM_INSTANCES1);
    this.numInstances = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById(XUL_LOGGING_INTERVAL1);
    this.loggingInterval = ((Text) tempBox.getTextControl()).getText();
  }

  protected String buildValidationErrorMessages() {
    String validationErrors = "";
    if (StringUtil.isEmpty(getJobEntryName())) {
      validationErrors += BaseMessages.getString(PKG, MSG_AMAZON_ELASTIC_MAP_REDUCE_JOB_EXECUTOR_JOB_ENTRY_NAME_ERROR) + "\n";
    }
    if (StringUtil.isEmpty(getHadoopJobName())) {
      validationErrors += BaseMessages.getString(PKG, MSG_AMAZON_ELASTIC_MAP_REDUCE_JOB_EXECUTOR_JOB_FLOW_NAME_ERROR) + "\n";
    }
    if (StringUtil.isEmpty(getAccessKey())) {
      validationErrors += BaseMessages.getString(PKG, MSG_AMAZON_ELASTIC_MAP_REDUCE_JOB_EXECUTOR_ACCESS_KEY_ERROR) + "\n";
    }
    if (StringUtil.isEmpty(getSecretKey())) {
      validationErrors += BaseMessages.getString(PKG, MSG_AMAZON_ELASTIC_MAP_REDUCE_JOB_EXECUTOR_SECRET_KEY_ERROR) + "\n";
    }
    String s3Protocol = AmazonSpoonPlugin.S3_SCHEME + "://";
    String sdir = getVariableSpace().environmentSubstitute(stagingDir);
    if (StringUtil.isEmpty(getStagingDir())) {
      validationErrors += BaseMessages.getString(PKG, MSG_AMAZON_ELASTIC_MAP_REDUCE_JOB_EXECUTOR_STAGING_DIR_ERROR) + "\n";
    } else if (!sdir.startsWith(s3Protocol)) {
      validationErrors += BaseMessages.getString(PKG, MSG_AMAZON_ELASTIC_MAP_REDUCE_JOB_EXECUTOR_STAGING_DIR_ERROR) + "\n";
    }

    /*if (numInstances <= 0) {
      validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.NumInstances.Error") + "\n";
    }*/
    if (StringUtil.isEmpty(getMasterInstanceType())) {
      validationErrors += BaseMessages.getString(PKG, MSG_AMAZON_ELASTIC_MAP_REDUCE_JOB_EXECUTOR_MASTER_INSTANCE_TYPE_ERROR) + "\n";
    }
    if (StringUtil.isEmpty(getSlaveInstanceType())) {
      validationErrors += BaseMessages.getString(PKG, MSG_AMAZON_ELASTIC_MAP_REDUCE_JOB_EXECUTOR_SLAVE_INSTANCE_TYPE_ERROR) + "\n";
    }
    return validationErrors;
  }

  protected void configureJobEntry() {
    // common/simple
    getJobEntry().setName(getJobEntryName());
    getJobEntry().setHadoopJobName(getHadoopJobName());
    getJobEntry().setHadoopJobFlowId(getHadoopJobFlowId());
    getJobEntry().setAccessKey(getAccessKey());
    getJobEntry().setSecretKey(getSecretKey());
    getJobEntry().setStagingDir(getStagingDir());
    getJobEntry().setJarUrl(getJarUrl());
    getJobEntry().setNumInstances(getNumInstances());
    getJobEntry().setMasterInstanceType(getMasterInstanceType());
    getJobEntry().setSlaveInstanceType(getSlaveInstanceType());
    getJobEntry().setCmdLineArgs(getCommandLineArgs());
    // advanced config
    getJobEntry().setBlocking(isBlocking());
    getJobEntry().setLoggingInterval(getLoggingInterval());

    getJobEntry().setChanged();
  }

  public void init() {
    if (getJobEntry() != null) {
      // common/simple
      setName(getJobEntry().getName());
      setJobEntryName(getJobEntry().getName());
      setHadoopJobName(getJobEntry().getHadoopJobName());
      setHadoopJobFlowId(getJobEntry().getHadoopJobFlowId());
      setAccessKey(getJobEntry().getAccessKey());
      setSecretKey(getJobEntry().getSecretKey());
      setStagingDir(getJobEntry().getStagingDir());
      setJarUrl(getJobEntry().getJarUrl());
      setNumInstances(getJobEntry().getNumInstances());
      setMasterInstanceType(getJobEntry().getMasterInstanceType());
      setSlaveInstanceType(getJobEntry().getSlaveInstanceType());
      setCommandLineArgs(getJobEntry().getCmdLineArgs());
      setBlocking(getJobEntry().isBlocking());
      setLoggingInterval(getJobEntry().getLoggingInterval());
    }
  }

  public void cancel() {
    XulDialog xulDialog = (XulDialog) getXulDomContainer().getDocumentRoot().getElementById(XUL_AMAZON_EMR_JOB_ENTRY_DIALOG);
    Shell shell = (Shell) xulDialog.getRootObject();
    if (!shell.isDisposed()) {
      WindowProperty winprop = new WindowProperty(shell);
      PropsUI.getInstance().setScreen(winprop);
      ((Composite) xulDialog.getManagedObject()).dispose();
      shell.dispose();
    }
  }

  public void openErrorDialog(String title, String message) {
    XulDialog errorDialog = (XulDialog) getXulDomContainer().getDocumentRoot().getElementById(XUL_AMAZON_EMR_ERROR_DIALOG);
    errorDialog.setTitle(title);

    XulTextbox errorMessage = (XulTextbox) getXulDomContainer().getDocumentRoot().getElementById(XUL_AMAZON_EMR_ERROR_MESSAGE);
    errorMessage.setValue(message);

    errorDialog.show();
  }

  public void closeErrorDialog() {
    XulDialog errorDialog = (XulDialog) getXulDomContainer().getDocumentRoot().getElementById(XUL_AMAZON_EMR_ERROR_DIALOG);
    errorDialog.hide();
  }

  protected VfsFileChooserDialog getFileChooserDialog() throws KettleFileException {
    if (this.fileChooserDialog == null) {
      FileObject rootFile = null;
      FileObject initialFile = null;
      FileObject defaultInitialFile = KettleVFS.getFileObject("file:///c:/");

      VfsFileChooserDialog fileChooserDialog = Spoon.getInstance().getVfsFileChooserDialog(defaultInitialFile, initialFile);
      this.fileChooserDialog = fileChooserDialog;
    }
    return this.fileChooserDialog;
  }

  protected FileSystemOptions getFileSystemOptions() throws FileSystemException {
    FileSystemOptions opts = new FileSystemOptions();

    if(!Const.isEmpty(getAccessKey()) || !Const.isEmpty(getSecretKey())) {
      // create a FileSystemOptions with user & password
      StaticUserAuthenticator userAuthenticator =
          new StaticUserAuthenticator(null,
              getVariableSpace().environmentSubstitute(getAccessKey()),
              getVariableSpace().environmentSubstitute(getSecretKey())
          );

      DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, userAuthenticator);
    }
    return opts;
  }

  public FileObject browse(String[] fileFilters, String[] fileFilterNames, String fileUri) throws KettleException, FileSystemException {
    return browse(fileFilters, fileFilterNames, fileUri, new FileSystemOptions());
  }

  public FileObject browse(String[] fileFilters, String[] fileFilterNames, String fileUri, int fileDialogMode) throws KettleException, FileSystemException {
    return browse(fileFilters, fileFilterNames, fileUri, new FileSystemOptions(), fileDialogMode);
  }

  public FileObject browse(String[] fileFilters, String[] fileFilterNames, String fileUri, FileSystemOptions opts) throws KettleException, FileSystemException {
    return browse(fileFilters, fileFilterNames, fileUri, opts, VfsFileChooserDialog.VFS_DIALOG_OPEN_DIRECTORY);
  }
  public FileObject browse(String[] fileFilters, String[] fileFilterNames, String fileUri, FileSystemOptions opts, int fileDialogMode) throws KettleException, FileSystemException {
    return getFileChooserHelper().browse(fileFilters, fileFilterNames, fileUri, opts, fileDialogMode);
  }

  public void browseS3StagingDir() throws KettleException, FileSystemException {
    String[] fileFilters = new String[] { "*.*" };
    String[] fileFilterNames = new String[] { "All" };

    String stagingDirText = getVariableSpace().environmentSubstitute(stagingDir);
    FileSystemOptions opts = getFileSystemOptions();

    FileObject selectedFile = browse(fileFilters, fileFilterNames, stagingDirText, opts);

    if (selectedFile != null) {
      setStagingDir(selectedFile.getName().getURI());
    }
  }

  public VariableSpace getVariableSpace() {
    if (Spoon.getInstance().getActiveTransformation() != null) {
      return Spoon.getInstance().getActiveTransformation();
    } else if (Spoon.getInstance().getActiveJob() != null) {
      return Spoon.getInstance().getActiveJob();
    } else {
      return new Variables();
    }
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
    firePropertyChange(JOB_ENTRY_NAME, previousVal, newVal);
  }

  public String getHadoopJobName() {
    return hadoopJobName;
  }

  public void setHadoopJobName(String hadoopJobName) {
    String previousVal = this.hadoopJobName;
    String newVal = hadoopJobName;

    this.hadoopJobName = hadoopJobName;
    firePropertyChange(HADOOP_JOB_NAME, previousVal, newVal);
  }

  public String getHadoopJobFlowId() {
    return hadoopJobFlowId;
  }

  public void setHadoopJobFlowId(String hadoopJobFlowId) {
    String previousVal = this.hadoopJobFlowId;
    String newVal = hadoopJobFlowId;

    this.hadoopJobFlowId = hadoopJobFlowId;
    firePropertyChange(HADOOP_JOB_FLOW_ID, previousVal, newVal);
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    String previousVal = this.accessKey;
    String newVal = accessKey;

    this.accessKey = accessKey;
    firePropertyChange(ACCESS_KEY, previousVal, newVal);
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    String previousVal = this.secretKey;
    String newVal = secretKey;

    this.secretKey = secretKey;
    firePropertyChange(SECRET_KEY, previousVal, newVal);
  }

  public String getStagingDir() {
    return stagingDir;
  }

  public void setStagingDir(String stagingDir) {
    String previousVal = this.stagingDir;
    String newVal = stagingDir;

    this.stagingDir = stagingDir;
    firePropertyChange(STAGING_DIR, previousVal, newVal);
  }

  public FileObject getStagingDirFile() {
    return stagingDirFile;
  }

  public void setStagingDirFile(FileObject stagingDirFile) {
    FileObject previousVal = this.stagingDirFile;
    FileObject newVal = stagingDirFile;

    this.stagingDirFile = stagingDirFile;
    firePropertyChange(STAGING_DIR_FILE, previousVal, newVal);
  }

  public String getJarUrl() {
    return jarUrl;
  }

  public void setJarUrl(String jarUrl) {
    String previousVal = this.jarUrl;
    String newVal = jarUrl;

    this.jarUrl = jarUrl;
    firePropertyChange(JAR_URL, previousVal, newVal);
  }

  public String getNumInstances() {
    return numInstances;
  }

  public void setNumInstances(String numInstances) {
    String previousVal = this.numInstances;
    String newVal = numInstances;

    this.numInstances = numInstances;
    firePropertyChange(NUM_INSTANCES, previousVal, newVal);
  }

  public String getMasterInstanceType() {
    return masterInstanceType;
  }

  public void setMasterInstanceType(String masterInstanceType) {
    String previousVal = this.masterInstanceType;
    String newVal = masterInstanceType;

    this.masterInstanceType = masterInstanceType;
    firePropertyChange(MASTER_INSTANCE_TYPE, previousVal, newVal);
  }

  public String getSlaveInstanceType() {
    return slaveInstanceType;
  }

  public void setSlaveInstanceType(String slaveInstanceType) {
    String previousVal = this.slaveInstanceType;
    String newVal = slaveInstanceType;

    this.slaveInstanceType = slaveInstanceType;
    firePropertyChange(SLAVE_INSTANCE_TYPE, previousVal, newVal);
  }

  public void invertBlocking() {
    setBlocking(!isBlocking());
  }

  public abstract <T extends AbstractAmazonJobEntry> T getJobEntry();

  public abstract <T extends AbstractAmazonJobEntry> void setJobEntry(T jobEntry);

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

  public FileObject resolveFile(String fileUri) throws FileSystemException, KettleFileException {
    VariableSpace vs = getVariableSpace();
    FileSystemOptions opts = new FileSystemOptions();
    DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(
        opts,
        new StaticUserAuthenticator(null, getAccessKey(), getSecretKey())
    );
    FileObject file = KettleVFS.getFileObject(fileUri, vs, opts);
    return file;
  }

  protected S3VfsFileChooserHelper getFileChooserHelper() throws KettleFileException, FileSystemException {
    if (helper == null) {
      XulDialog xulDialog = (XulDialog) getXulDomContainer().getDocumentRoot().getElementById(XUL_AMAZON_EMR_JOB_ENTRY_DIALOG);
      Shell shell = (Shell) xulDialog.getRootObject();

      helper = new S3VfsFileChooserHelper(shell, getFileChooserDialog(), getVariableSpace(), getFileSystemOptions());
    }
    return helper;
  }

}
