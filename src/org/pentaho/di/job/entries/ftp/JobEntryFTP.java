/**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

package org.pentaho.di.job.entries.ftp;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;

/**
 * This defines an FTP job entry.
 *
 * @author Matt
 * @since 05-11-2003
 *
 */
public class JobEntryFTP extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Logger log4j = Logger.getLogger(JobEntryFTP.class);

  private String serverName;

  private String userName;

  private String password;

  private String ftpDirectory;

  private String targetDirectory;

  private String wildcard;

  private boolean binaryMode;

  private int timeout;

  private boolean remove;

  private boolean onlyGettingNewFiles; /* Don't overwrite files */

  private boolean activeConnection;

  private String controlEncoding; /* how to convert list of filenames e.g. */

  /**
   * Implicit encoding used before PDI v2.4.1
   */
  static private String LEGACY_CONTROL_ENCODING = "US-ASCII"; //$NON-NLS-1$

  /**
   * Default encoding when making a new ftp job entry instance.
   */
  static private String DEFAULT_CONTROL_ENCODING = "ISO-8859-1"; //$NON-NLS-1$

  public JobEntryFTP(String n) {
    super(n, ""); //$NON-NLS-1$
    serverName = null;
    setID(-1L);
    setType(JobEntryInterface.TYPE_JOBENTRY_FTP);
    setControlEncoding(DEFAULT_CONTROL_ENCODING);
  }

  public JobEntryFTP() {
    this(""); //$NON-NLS-1$
  }

  public JobEntryFTP(JobEntryBase jeb) {
    super(jeb);
  }

  public Object clone() {
    JobEntryFTP je = (JobEntryFTP) super.clone();
    return je;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer(128);

    retval.append(super.getXML());

    retval.append("      ").append(XMLHandler.addTagValue("servername", serverName)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("username", userName)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append( //$NON-NLS-1$
        XMLHandler.addTagValue("password", Encr.encryptPasswordIfNotUsingVariables(getPassword()))); //$NON-NLS-1$
    retval.append("      ").append(XMLHandler.addTagValue("ftpdirectory", ftpDirectory)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("targetdirectory", targetDirectory)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("wildcard", wildcard)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("binary", binaryMode)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("timeout", timeout)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("remove", remove)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("only_new", onlyGettingNewFiles)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("active", activeConnection)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("control_encoding", controlEncoding)); //$NON-NLS-1$ //$NON-NLS-2$

    return retval.toString();
  }

  public void loadXML(Node entrynode, List<DatabaseMeta> databases, Repository rep) throws KettleXMLException {
    try {
      super.loadXML(entrynode, databases);
      serverName = XMLHandler.getTagValue(entrynode, "servername"); //$NON-NLS-1$
      userName = XMLHandler.getTagValue(entrynode, "username"); //$NON-NLS-1$
      password = Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(entrynode, "password")); //$NON-NLS-1$
      ftpDirectory = XMLHandler.getTagValue(entrynode, "ftpdirectory"); //$NON-NLS-1$
      targetDirectory = XMLHandler.getTagValue(entrynode, "targetdirectory"); //$NON-NLS-1$
      wildcard = XMLHandler.getTagValue(entrynode, "wildcard"); //$NON-NLS-1$
      binaryMode = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "binary")); //$NON-NLS-1$ //$NON-NLS-2$
      timeout = Const.toInt(XMLHandler.getTagValue(entrynode, "timeout"), 10000); //$NON-NLS-1$
      remove = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "remove")); //$NON-NLS-1$ //$NON-NLS-2$
      onlyGettingNewFiles = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "only_new")); //$NON-NLS-1$ //$NON-NLS-2$
      activeConnection = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "active")); //$NON-NLS-1$ //$NON-NLS-2$
      controlEncoding = XMLHandler.getTagValue(entrynode, "control_encoding"); //$NON-NLS-1$
      if (controlEncoding == null) {
        // if we couldn't retrieve an encoding, assume it's an old instance and
        // put in the the encoding used before v 2.4.0
        controlEncoding = LEGACY_CONTROL_ENCODING;
      }
    } catch (KettleXMLException xe) {
      throw new KettleXMLException(Messages.getString("JobEntryFTP.UnableToLoadFromXml"), xe); //$NON-NLS-1$
    }
  }

  public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases) throws KettleException {
    try {
      super.loadRep(rep, id_jobentry, databases);
      serverName = rep.getJobEntryAttributeString(id_jobentry, "servername"); //$NON-NLS-1$
      userName = rep.getJobEntryAttributeString(id_jobentry, "username"); //$NON-NLS-1$
      password = rep.getJobEntryAttributeString(id_jobentry, "password"); //$NON-NLS-1$
      ftpDirectory = rep.getJobEntryAttributeString(id_jobentry, "ftpdirectory"); //$NON-NLS-1$
      targetDirectory = rep.getJobEntryAttributeString(id_jobentry, "targetdirectory"); //$NON-NLS-1$
      wildcard = rep.getJobEntryAttributeString(id_jobentry, "wildcard"); //$NON-NLS-1$
      binaryMode = rep.getJobEntryAttributeBoolean(id_jobentry, "binary"); //$NON-NLS-1$
      timeout = (int) rep.getJobEntryAttributeInteger(id_jobentry, "timeout"); //$NON-NLS-1$
      remove = rep.getJobEntryAttributeBoolean(id_jobentry, "remove"); //$NON-NLS-1$
      onlyGettingNewFiles = rep.getJobEntryAttributeBoolean(id_jobentry, "only_new"); //$NON-NLS-1$
      activeConnection = rep.getJobEntryAttributeBoolean(id_jobentry, "active"); //$NON-NLS-1$
      controlEncoding = rep.getJobEntryAttributeString(id_jobentry, "control_encoding"); //$NON-NLS-1$
      if (controlEncoding == null) {
        // if we couldn't retrieve an encoding, assume it's an old instance and
        // put in the the encoding used before v 2.4.0
        controlEncoding = LEGACY_CONTROL_ENCODING;
      }
    } catch (KettleException dbe) {
      throw new KettleException(
          Messages.getString("JobEntryFTP.UnableToLoadFromRepo", String.valueOf(id_jobentry)), dbe); //$NON-NLS-1$
    }
  }

  public void saveRep(Repository rep, long id_job) throws KettleException {
    try {
      super.saveRep(rep, id_job);

      rep.saveJobEntryAttribute(id_job, getID(), "servername", serverName); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "username", userName); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "password", password); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "ftpdirectory", ftpDirectory); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "targetdirectory", targetDirectory); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "wildcard", wildcard); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "binary", binaryMode); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "timeout", timeout); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "remove", remove); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "only_new", onlyGettingNewFiles); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "active", activeConnection); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "control_encoding", controlEncoding); //$NON-NLS-1$
    } catch (KettleDatabaseException dbe) {
      throw new KettleException(Messages.getString("JobEntryFTP.UnableToSaveToRepo", String.valueOf(id_job)), dbe); //$NON-NLS-1$
    }
  }

  /**
   * @return Returns the binaryMode.
   */
  public boolean isBinaryMode() {
    return binaryMode;
  }

  /**
   * @param binaryMode The binaryMode to set.
   */
  public void setBinaryMode(boolean binaryMode) {
    this.binaryMode = binaryMode;
  }

  /**
   * @return Returns the directory.
   */
  public String getFtpDirectory() {
    return ftpDirectory;
  }

  /**
   * @param directory The directory to set.
   */
  public void setFtpDirectory(String directory) {
    this.ftpDirectory = directory;
  }

  /**
   * @return Returns the password.
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password The password to set.
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * @return Returns the serverName.
   */
  public String getServerName() {
    return serverName;
  }

  /**
   * @param serverName The serverName to set.
   */
  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  /**
   * @return Returns the userName.
   */
  public String getUserName() {
    return userName;
  }

  /**
   * @param userName The userName to set.
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /**
   * @return Returns the wildcard.
   */
  public String getWildcard() {
    return wildcard;
  }

  /**
   * @param wildcard The wildcard to set.
   */
  public void setWildcard(String wildcard) {
    this.wildcard = wildcard;
  }

  /**
   * @return Returns the targetDirectory.
   */
  public String getTargetDirectory() {
    return targetDirectory;
  }

  /**
   * @param targetDirectory The targetDirectory to set.
   */
  public void setTargetDirectory(String targetDirectory) {
    this.targetDirectory = targetDirectory;
  }

  /**
   * @param timeout The timeout to set.
   */
  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  /**
   * @return Returns the timeout.
   */
  public int getTimeout() {
    return timeout;
  }

  /**
   * @param remove The remove to set.
   */
  public void setRemove(boolean remove) {
    this.remove = remove;
  }

  /**
   * @return Returns the remove.
   */
  public boolean getRemove() {
    return remove;
  }

  /**
   * @return Returns the onlyGettingNewFiles.
   */
  public boolean isOnlyGettingNewFiles() {
    return onlyGettingNewFiles;
  }

  /**
   * @param onlyGettingNewFiles The onlyGettingNewFiles to set.
   */
  public void setOnlyGettingNewFiles(boolean onlyGettingNewFiles) {
    this.onlyGettingNewFiles = onlyGettingNewFiles;
  }

  /**
   * Get the control encoding to be used for ftp'ing
   *
   * @return the used encoding
   */
  public String getControlEncoding() {
    return controlEncoding;
  }

  /**
   *  Set the encoding to be used for ftp'ing. This determines how
   *  names are translated in dir e.g. It does impact the contents
   *  of the files being ftp'ed.
   *
   *  @param encoding The encoding to be used.
   */
  public void setControlEncoding(String encoding) {
    this.controlEncoding = encoding;
  }

  public Result execute(Result previousResult, int nr, Repository rep, Job parentJob) {
    LogWriter log = LogWriter.getInstance();

    log4j.info(Messages.getString("JobEntryFTP.Started", serverName)); //$NON-NLS-1$

    Result result = previousResult;
    result.setResult(false);
    long filesRetrieved = 0;

    log.logDetailed(toString(), Messages.getString("JobEntryFTP.Start")); //$NON-NLS-1$

    FTPClient ftpclient = null;

    try {
      // Create ftp client to host:port ...
      ftpclient = new FTPClient();
      String realServername = environmentSubstitute(serverName);
      ftpclient.setRemoteAddr(InetAddress.getByName(realServername));

      log.logDetailed(toString(), Messages.getString("JobEntryFTP.OpenedConnection", realServername)); //$NON-NLS-1$

      // set activeConnection connectmode ...
      if (activeConnection) {
        ftpclient.setConnectMode(FTPConnectMode.ACTIVE);
        log.logDetailed(toString(), Messages.getString("JobEntryFTP.SetActive")); //$NON-NLS-1$
      } else {
        ftpclient.setConnectMode(FTPConnectMode.PASV);
        log.logDetailed(toString(), Messages.getString("JobEntryFTP.SetPassive")); //$NON-NLS-1$
      }

      // Set the timeout
      ftpclient.setTimeout(timeout);
      log.logDetailed(toString(), Messages.getString("JobEntryFTP.SetTimeout", String.valueOf(timeout))); //$NON-NLS-1$

      ftpclient.setControlEncoding(controlEncoding);
      log.logDetailed(toString(), Messages.getString("JobEntryFTP.SetEncoding", controlEncoding)); //$NON-NLS-1$

      // login to ftp host ...
      ftpclient.connect();
      String realUsername = environmentSubstitute(userName);
      String realPassword = environmentSubstitute(password);
      ftpclient.login(realUsername, realPassword);
      //  Remove password from logging, you don't know where it ends up.
      log.logDetailed(toString(), Messages.getString("JobEntryFTP.LoggedIn", realUsername)); //$NON-NLS-1$

      // move to spool dir ...
      if (!Const.isEmpty(ftpDirectory)) {
        String realFtpDirectory = environmentSubstitute(ftpDirectory);
        ftpclient.chdir(realFtpDirectory);
        log.logDetailed(toString(), Messages.getString("JobEntryFTP.ChangedDir", realFtpDirectory)); //$NON-NLS-1$
      }

      // Get all the files in the current directory...
      String[] filelist = ftpclient.dir();
      log.logDetailed(toString(), Messages.getString("JobEntryFTP.FoundNFiles", String.valueOf(filelist.length))); //$NON-NLS-1$

      // set transfertype ...
      if (binaryMode) {
        ftpclient.setType(FTPTransferType.BINARY);
        log.logDetailed(toString(), Messages.getString("JobEntryFTP.SetBinary")); //$NON-NLS-1$
      } else {
        ftpclient.setType(FTPTransferType.ASCII);
        log.logDetailed(toString(), Messages.getString("JobEntryFTP.SetAscii")); //$NON-NLS-1$
      }

      // Some FTP servers return a message saying no files found as a string in the filenlist
      // e.g. Solaris 8
      // CHECK THIS !!!
      if (filelist.length == 1) {
        String translatedWildcard = environmentSubstitute(wildcard);
        if (filelist[0].startsWith(translatedWildcard)) {
          throw new FTPException(filelist[0]);
        }
      }

      Pattern pattern = null;
      if (!Const.isEmpty(wildcard)) {
        String realWildcard = environmentSubstitute(wildcard);
        pattern = Pattern.compile(realWildcard);
      }

      // Get the files in the list...
      for (int i = 0; i < filelist.length && !parentJob.isStopped(); i++) {
        boolean getIt = true;

        // First see if the file matches the regular expression!
        if (pattern != null) {
          Matcher matcher = pattern.matcher(filelist[i]);
          getIt = matcher.matches();
        }

        if (getIt) {
          log.logDebug(toString(), Messages.getString(
              "JobEntryFTP.GettingFile", filelist[i], environmentSubstitute(targetDirectory))); //$NON-NLS-1$
          String targetFilename = getTargetFilename(filelist[i]);
          FileObject targetFile = KettleVFS.getFileObject(targetFilename);

          if ((onlyGettingNewFiles == false) || (onlyGettingNewFiles == true) && needsDownload(filelist[i])) {
            ftpclient.get(targetFilename, filelist[i]);
            filesRetrieved++;

            // Add to the result files...
            ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, targetFile, parentJob.getJobname(),
                toString());
            resultFile.setComment(Messages.getString("JobEntryFTP.Downloaded", serverName)); //$NON-NLS-1$
            result.getResultFiles().put(resultFile.getFile().toString(), resultFile);

            log.logDetailed(toString(), Messages.getString("JobEntryFTP.GotFile", filelist[i])); //$NON-NLS-1$
          }

          // Delete the file if this is needed!
          if (remove) {
            ftpclient.delete(filelist[i]);
            log.logDetailed(toString(), Messages.getString("JobEntryFTP.DeletedFile", filelist[i])); //$NON-NLS-1$
          }
        }
      }

      result.setResult(true);
      result.setNrFilesRetrieved(filesRetrieved);
    } catch (Exception e) {
      result.setNrErrors(1);
      log.logError(toString(), Messages.getString("JobEntryFTP.ErrorGetting", e.getMessage())); //$NON-NLS-1$
      log.logError(toString(), Const.getStackTracker(e));
    } finally {
      if (ftpclient != null && ftpclient.connected()) {
        try {
          ftpclient.quit();
        } catch (Exception e) {
          log.logError(toString(), Messages.getString("JobEntryFTP.ErrorQuitting", e.getMessage())); //$NON-NLS-1$
        }
      }
    }

    return result;
  }

  /**
   * @param string the filename from the FTP server
   *
   * @return the calculated target filename
   */
  protected String getTargetFilename(String string) {
    return environmentSubstitute(targetDirectory) + Const.FILE_SEPARATOR + string;
  }

  public boolean evaluates() {
    return true;
  }

  /**
   * See if the filename on the FTP server needs downloading.
   * The default is to check the presence of the file in the target directory.
   * If you need other functionality, extend this class and build it into a plugin.
   *
   * @param filename The filename to check
   * @return true if the file needs downloading
   */
  protected boolean needsDownload(String filename) {
    File file = new File(getTargetFilename(filename));
    return !file.exists();
  }

  public JobEntryDialogInterface getDialog(Shell shell, JobEntryInterface jei, JobMeta jobMeta, String jobName,
      Repository rep) {
    return new JobEntryFTPDialog(shell, this, jobMeta);
  }

  /**
   * @return the activeConnection
   */
  public boolean isActiveConnection() {
    return activeConnection;
  }

  /**
   * @param activeConnection the activeConnection to set
   */
  public void setActiveConnection(boolean passive) {
    this.activeConnection = passive;
  }

  public void check(List<CheckResult> remarks, JobMeta jobMeta) {
    LogWriter log = LogWriter.getInstance();

    // required
    if (StringUtils.isBlank(serverName)) {
      remarks.add(new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages
          .getString("JobEntryFTP.CheckResult.ServerNameIsBlank"), this)); //$NON-NLS-1$
    }
    if (StringUtils.isBlank(targetDirectory)) {
      remarks.add(new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages
          .getString("JobEntryFTP.CheckResult.TargetDirIsBlank"), this)); //$NON-NLS-1$
    } else {
      String realTargetDir = environmentSubstitute(targetDirectory);
      FileObject targetDirObject;
      try {
        targetDirObject = KettleVFS.getFileObject(realTargetDir);
        if (!targetDirObject.exists()) {
          remarks.add(new CheckResult(CheckResult.TYPE_RESULT_WARNING, Messages.getString(
              "JobEntryFTP.CheckResult.TargetDirDoesNotExist", targetDirectory), this)); //$NON-NLS-1$
        }
      } catch (IOException e) {
        log.logError(toString(), e.getMessage());
        remarks.add(new CheckResult(CheckResult.TYPE_RESULT_WARNING, Messages
            .getString("JobEntryFTP.CheckResult.CannotCheckTargetDirExistence"), this)); //$NON-NLS-1$
      }
    }
    if (StringUtils.isBlank(userName)) {
      remarks.add(new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages
          .getString("JobEntryFTP.CheckResult.UsernameIsBlank"), this)); //$NON-NLS-1$
    }
    if (null == password) {
      remarks.add(new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages
          .getString("JobEntryFTP.CheckResult.PasswordIsNull"), this)); //$NON-NLS-1$
    }

    // optional
    remarks.add(new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString(
        "JobEntryFTP.CheckResult.ActiveConnection", String.valueOf(activeConnection)), this)); //$NON-NLS-1$
    remarks.add(new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString(
        "JobEntryFTP.CheckResult.Timeout", String.valueOf(timeout)), this)); //$NON-NLS-1$
    remarks.add(new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString(
        "JobEntryFTP.CheckResult.Encoding", controlEncoding), this)); //$NON-NLS-1$
    remarks.add(new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString(
        "JobEntryFTP.CheckResult.BinaryMode", String.valueOf(binaryMode)), this)); //$NON-NLS-1$
    remarks.add(new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString(
        "JobEntryFTP.CheckResult.FtpDir", ftpDirectory), this)); //$NON-NLS-1$
    remarks.add(new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString(
        "JobEntryFTP.CheckResult.OnlyGetNew", String.valueOf(onlyGettingNewFiles)), this)); //$NON-NLS-1$
    remarks.add(new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString(
        "JobEntryFTP.CheckResult.Remove", String.valueOf(remove)), this)); //$NON-NLS-1$
    remarks.add(new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString(
        "JobEntryFTP.CheckResult.Wildcard", wildcard), this)); //$NON-NLS-1$

  }
}