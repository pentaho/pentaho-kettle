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

package org.pentaho.di.job.entries.deletefiles;

import java.io.IOException;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileType;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
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

/**
 * This defines a 'delete files' job entry.
 *
 * @author Samatar Hassan
 * @since 06-05-2007
 */
public class JobEntryDeleteFiles extends JobEntryBase implements Cloneable, JobEntryInterface {
  private boolean ignoreErrors;

  public boolean argFromPrevious;

  public boolean deleteFolder;

  public boolean includeSubfolders;

  public String arguments[];

  public String filemasks[];

  public JobEntryDeleteFiles(String n) {
    super(n, ""); //$NON-NLS-1$
    ignoreErrors = false;
    argFromPrevious = false;
    arguments = null;
    deleteFolder = false;
    includeSubfolders = false;
    setID(-1L);
    setType(JobEntryInterface.TYPE_JOBENTRY_DELETE_FILES);
  }

  public JobEntryDeleteFiles() {
    this(""); //$NON-NLS-1$
  }

  public JobEntryDeleteFiles(JobEntryBase jeb) {
    super(jeb);
  }

  public Object clone() {
    JobEntryDeleteFiles je = (JobEntryDeleteFiles) super.clone();
    return je;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer(300);

    retval.append(super.getXML());
    retval.append("      ").append(XMLHandler.addTagValue("ignore_errors", ignoreErrors)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("arg_from_previous", argFromPrevious)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("delete_folder", deleteFolder)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("include_subfolders", includeSubfolders)); //$NON-NLS-1$ //$NON-NLS-2$

    retval.append("      <fields>").append(Const.CR); //$NON-NLS-1$
    if (arguments != null) {
      for (int i = 0; i < arguments.length; i++) {
        retval.append("        <field>").append(Const.CR); //$NON-NLS-1$
        retval.append("          ").append(XMLHandler.addTagValue("name", arguments[i])); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("          ").append(XMLHandler.addTagValue("filemask", filemasks[i])); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("        </field>").append(Const.CR); //$NON-NLS-1$
      }
    }
    retval.append("      </fields>").append(Const.CR); //$NON-NLS-1$

    return retval.toString();
  }

  public void loadXML(Node entrynode, List<DatabaseMeta> databases, Repository rep) throws KettleXMLException {
    try {
      super.loadXML(entrynode, databases);
      ignoreErrors = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "ignore_errors")); //$NON-NLS-1$ //$NON-NLS-2$
      argFromPrevious = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "arg_from_previous")); //$NON-NLS-1$ //$NON-NLS-2$
      deleteFolder = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "delete_folder")); //$NON-NLS-1$ //$NON-NLS-2$
      includeSubfolders = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "include_subfolders")); //$NON-NLS-1$ //$NON-NLS-2$

      Node fields = XMLHandler.getSubNode(entrynode, "fields"); //$NON-NLS-1$

      // How many field arguments?
      int nrFields = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$
      arguments = new String[nrFields];
      filemasks = new String[nrFields];

      // Read them all...
      for (int i = 0; i < nrFields; i++) {
        Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$

        arguments[i] = XMLHandler.getTagValue(fnode, "name"); //$NON-NLS-1$
        filemasks[i] = XMLHandler.getTagValue(fnode, "filemask"); //$NON-NLS-1$
      }
    } catch (KettleXMLException xe) {
      throw new KettleXMLException(Messages.getString("JobEntryDeleteFiles.UnableToLoadFromXml"), xe); //$NON-NLS-1$
    }
  }

  public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases) throws KettleException {
    try {
      super.loadRep(rep, id_jobentry, databases);
      ignoreErrors = rep.getJobEntryAttributeBoolean(id_jobentry, "ignore_errors"); //$NON-NLS-1$
      argFromPrevious = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_from_previous"); //$NON-NLS-1$
      deleteFolder = rep.getJobEntryAttributeBoolean(id_jobentry, "delete_folder"); //$NON-NLS-1$
      includeSubfolders = rep.getJobEntryAttributeBoolean(id_jobentry, "include_subfolders"); //$NON-NLS-1$

      // How many arguments?
      int argnr = rep.countNrJobEntryAttributes(id_jobentry, "name"); //$NON-NLS-1$
      arguments = new String[argnr];
      filemasks = new String[argnr];

      // Read them all...
      for (int a = 0; a < argnr; a++) {
        arguments[a] = rep.getJobEntryAttributeString(id_jobentry, a, "name"); //$NON-NLS-1$
        filemasks[a] = rep.getJobEntryAttributeString(id_jobentry, a, "filemask"); //$NON-NLS-1$
      }
    } catch (KettleException dbe) {
      throw new KettleException(Messages.getString(
          "JobEntryDeleteFiles.UnableToLoadFromRepo", String.valueOf(id_jobentry)), dbe); //$NON-NLS-1$
    }
  }

  public void saveRep(Repository rep, long id_job) throws KettleException {
    try {
      super.saveRep(rep, id_job);

      rep.saveJobEntryAttribute(id_job, getID(), "ignore_errors", ignoreErrors); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "arg_from_previous", argFromPrevious); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "delete_folder", deleteFolder); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "include_subfolders", includeSubfolders); //$NON-NLS-1$

      // save the arguments...
      if (arguments != null) {
        for (int i = 0; i < arguments.length; i++) {
          rep.saveJobEntryAttribute(id_job, getID(), i, "name", arguments[i]); //$NON-NLS-1$
          rep.saveJobEntryAttribute(id_job, getID(), i, "filemask", filemasks[i]); //$NON-NLS-1$
        }
      }
    } catch (KettleDatabaseException dbe) {
      throw new KettleException(
          Messages.getString("JobEntryDeleteFiles.UnableToSaveToRepo", String.valueOf(id_job)), dbe); //$NON-NLS-1$
    }
  }

  public Result execute(Result result, int nr, Repository rep, Job parentJob) throws KettleException {
    LogWriter log = LogWriter.getInstance();

    List<RowMetaAndData> rows = result.getRows();
    RowMetaAndData resultRow = null;

    boolean rcode = true;

    String args[] = arguments;
    String fmasks[] = filemasks;
    result.setResult(true);

    rcode = true;

    if (argFromPrevious) {
      log.logDetailed(toString(), Messages.getString(
          "JobEntryDeleteFiles.FoundPreviousRows", String.valueOf((rows != null ? rows.size() : 0)))); //$NON-NLS-1$
    }

    if (argFromPrevious && rows != null) // Copy the input row to the (command line) arguments
    {
      for (int iteration = 0; iteration < rows.size(); iteration++) {
        resultRow = rows.get(iteration);
        args = new String[resultRow.size()];
        fmasks = new String[resultRow.size()];

        args[iteration] = resultRow.getString(0, null);
        fmasks[iteration] = resultRow.getString(1, null);

        if (rcode) {
          // ok we can process this file/folder
          log.logDetailed(toString(), Messages.getString(
              "JobEntryDeleteFiles.ProcessingRow", args[iteration], fmasks[iteration])); //$NON-NLS-1$

          if (!ProcessFile(args[iteration], fmasks[iteration])) {
            rcode = false;
          }
        } else {
          log.logDetailed(toString(), Messages.getString(
              "JobEntryDeleteFiles.IgnoringRow", args[iteration], fmasks[iteration])); //$NON-NLS-1$
        }
      }
    } else if (arguments != null) {
      for (int i = 0; i < arguments.length; i++) {
        if (rcode) {
          // ok we can process this file/folder
          log.logDetailed(toString(), Messages.getString(
              "JobEntryDeleteFiles.ProcessingArg", arguments[i], filemasks[i])); //$NON-NLS-1$
          if (!ProcessFile(arguments[i], filemasks[i])) {
            rcode = false;
          }
        } else {
          log
              .logDetailed(toString(), Messages
                  .getString("JobEntryDeleteFiles.IgnoringArg", arguments[i], filemasks[i])); //$NON-NLS-1$
        }

      }
    }
    if (!rcode && ignoreErrors) {
      result.setResult(false);
      result.setNrErrors(1);
    }
    //  String realFilefoldername = environmentSubstitute(filename);
    //  String realwilcard = environmentSubstitute(wildcard);

    result.setResult(rcode);

    return result;
  }

  private boolean ProcessFile(String filename, String wildcard) {
    LogWriter log = LogWriter.getInstance();

    boolean rcode = false;
    FileObject filefolder = null;
    String realFilefoldername = environmentSubstitute(filename);
    String realwilcard = environmentSubstitute(wildcard);

    try {
      filefolder = KettleVFS.getFileObject(realFilefoldername);

      // Here gc() is explicitly called if e.g. createfile is used in the same
      // job for the same file. The problem is that after creating the file the
      // file object is not properly garbaged collected and thus the file cannot
      // be deleted anymore. This is a known problem in the JVM.

      System.gc();

      if (filefolder.exists()) {
        // the file or folder exists
        if (filefolder.getType() == FileType.FOLDER) {
          // It's a folder
          if (log.isDetailed())
            log.logDetailed(toString(), Messages.getString("JobEntryDeleteFiles.ProcessingFolder", realFilefoldername)); //$NON-NLS-1$
          // Delete Files
          int Nr = filefolder.delete(new TextFileSelector(realwilcard));

          if (log.isDetailed())
            log.logDetailed(toString(), Messages.getString("JobEntryDeleteFiles.TotalDeleted", String.valueOf(Nr))); //$NON-NLS-1$
          rcode = true;
        } else {
          // It's a file
          log.logDetailed(toString(), Messages.getString("JobEntryDeleteFiles.ProcessingFile", realFilefoldername)); //$NON-NLS-1$
          boolean deleted = filefolder.delete();
          if (!deleted) {
            log.logError(toString(), Messages.getString("JobEntryDeleteFiles.CouldNotDeleteFile", realFilefoldername)); //$NON-NLS-1$
          } else {
            log.logBasic(toString(), Messages.getString("JobEntryDeleteFiles.FileDeleted", filename)); //$NON-NLS-1$
            rcode = true;
          }
        }
      } else {
        // File already deleted, no reason to try to delete it
        log.logBasic(toString(), Messages.getString("JobEntryDeleteFiles.FileAlreadyDeleted", realFilefoldername)); //$NON-NLS-1$
        rcode = true;
      }
    } catch (IOException e) {
      log.logError(toString(), Messages.getString(
          "JobEntryDeleteFiles.CouldNotProcess", realFilefoldername, e.getMessage())); //$NON-NLS-1$
    } finally {
      if (filefolder != null) {
        try {
          filefolder.close();
        } catch (IOException ex) {
        }
        ;
      }
    }

    return rcode;
  }

  private class TextFileSelector implements FileSelector {
    LogWriter log = LogWriter.getInstance();

    String fileExtension;

    public TextFileSelector(String extension) {
      if (!Const.isEmpty(extension)) {
        fileExtension = extension.replace('.', ' ').replace('*', ' ').replace('$', ' ').trim();
      }
    }

    public boolean includeFile(FileSelectInfo info) {
      boolean rcode = false;
      try {
        String extension = info.getFile().getName().getExtension();
        if (extension.equals(fileExtension) || Const.isEmpty(fileExtension)) {
          if (info.getFile().getType() == FileType.FOLDER) {
            if (deleteFolder && includeSubfolders) {
              rcode = true;
            } else {
              rcode = false;
            }
          } else {
            rcode = true;
          }
        } else {
          rcode = false;
        }
      } catch (Exception e) {
        log.logError(toString(), Messages.getString("JobEntryDeleteFiles.GeneralException", e.getMessage())); //$NON-NLS-1$
      }
      return rcode;
    }

    public boolean traverseDescendents(FileSelectInfo info) {
      return includeSubfolders;
    }
  }

  public JobEntryDialogInterface getDialog(Shell shell, JobEntryInterface jei, JobMeta jobMeta, String jobName,
      Repository rep) {
    return new JobEntryDeleteFilesDialog(shell, this, jobMeta);
  }

  public boolean isIgnoreErrors() {
    return ignoreErrors;
  }

  public void setIgnoreErrors(boolean ignoreErrors) {
    this.ignoreErrors = ignoreErrors;
  }

  public void setDeleteFolder(boolean deleteFolder) {
    this.deleteFolder = deleteFolder;
  }

  public void setIncludeSubfolders(boolean includeSubfolders) {
    this.includeSubfolders = includeSubfolders;
  }

  public boolean evaluates() {
    return true;
  }

  public void check(List<CheckResult> remarks, JobMeta jobMeta) {
    LogWriter log = LogWriter.getInstance();
    for (int i = 0; i < arguments.length; i++) {
      FileObject fileObject = null;
      String filename = environmentSubstitute(arguments[i]);
      try {
        fileObject = KettleVFS.getFileObject(filename);
        if (null != fileObject && fileObject.exists()) {
          // folder
          remarks.add(new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString(
              "JobEntryDeleteFiles.CheckResult.Exists", filename), this)); //$NON-NLS-1$
          if (fileObject.getType() == FileType.FOLDER) {
            remarks.add(new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString(
                "JobEntryDeleteFiles.CheckResult.IsFolder", filename), this)); //$NON-NLS-1$
            String wildcard = environmentSubstitute(filemasks[i]);
            remarks.add(new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString(
                "JobEntryDeleteFiles.CheckResult.Wildcard", wildcard), this)); //$NON-NLS-1$
          }
        } else {
          // already deleted
          remarks.add(new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString(
              "JobEntryDeleteFiles.FileAlreadyDeleted", filename), this)); //$NON-NLS-1$
        }
      } catch (IOException e) {
        log.logError(toString(), e.getMessage());
        remarks.add(new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString(
            "JobEntryDeleteFiles.CouldNotProcess", filename, e.getMessage()), this)); //$NON-NLS-1$
      }

    }
  }

}