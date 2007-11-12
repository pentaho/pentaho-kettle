/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.job.entries.createfile;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileDoesNotExistValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

/**
 * This defines a 'create file' job entry. Its main use would be to create empty
 * trigger files that can be used to control the flow in ETL cycles.
 *
 * @author Sven Boden
 * @since 28-01-2007
 *
 */
public class JobEntryCreateFile extends JobEntryBase implements Cloneable, JobEntryInterface
{
  private String filename;

  private boolean failIfFileExists;

  public JobEntryCreateFile(String n)
  {
    super(n, "");
    filename = null;
    failIfFileExists = true;
    setID(-1L);
    setJobEntryType(JobEntryType.CREATE_FILE);
  }

  public JobEntryCreateFile()
  {
    this("");
  }

  public JobEntryCreateFile(JobEntryBase jeb)
  {
    super(jeb);
  }

  public Object clone()
  {
    JobEntryCreateFile je = (JobEntryCreateFile) super.clone();
    return je;
  }

  public String getXML()
  {
    StringBuffer retval = new StringBuffer(50);

    retval.append(super.getXML());
    retval.append("      ").append(XMLHandler.addTagValue("filename", filename));
    retval.append("      ").append(XMLHandler.addTagValue("fail_if_file_exists", failIfFileExists));

    return retval.toString();
  }

  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
  {
    try
    {
      super.loadXML(entrynode, databases, slaveServers);
      filename = XMLHandler.getTagValue(entrynode, "filename");
      failIfFileExists = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "fail_if_file_exists"));
    } catch (KettleXMLException xe)
    {
      throw new KettleXMLException("Unable to load job entry of type 'create file' from XML node", xe);
    }
  }

  public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
  {
    try
    {
      super.loadRep(rep, id_jobentry, databases, slaveServers);
      filename = rep.getJobEntryAttributeString(id_jobentry, "filename");
      failIfFileExists = rep.getJobEntryAttributeBoolean(id_jobentry, "fail_if_file_exists");
    } catch (KettleException dbe)
    {
      throw new KettleException("Unable to load job entry of type 'create file' from the repository for id_jobentry="
          + id_jobentry, dbe);
    }
  }

  public void saveRep(Repository rep, long id_job) throws KettleException
  {
    try
    {
      super.saveRep(rep, id_job);

      rep.saveJobEntryAttribute(id_job, getID(), "filename", filename);
      rep.saveJobEntryAttribute(id_job, getID(), "fail_if_file_exists", failIfFileExists);
    } catch (KettleDatabaseException dbe)
    {
      throw new KettleException(
          "Unable to save job entry of type 'create file' to the repository for id_job=" + id_job, dbe);
    }
  }

  public void setFilename(String filename)
  {
    this.filename = filename;
  }

  public String getFilename()
  {
    return filename;
  }

  public String getRealFilename()
  {
    return environmentSubstitute(getFilename());
  }

  public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
  {
    LogWriter log = LogWriter.getInstance();
    Result result = previousResult;
    result.setResult(false);

    if (filename != null)
    {
      String realFilename = getRealFilename();
      FileObject fileObject = null;
      try
      {
        fileObject = KettleVFS.getFileObject(realFilename);

        if (fileObject.exists())
        {
          if (isFailIfFileExists())
          {
            // File exists and fail flag is on.
            result.setResult(false);
            log.logError(toString(), "File [" + realFilename + "] exists, failing.");
          } else
          {
            // File already exists, no reason to try to create it
            result.setResult(true);
            log.logBasic(toString(), "File [" + realFilename + "] already exists, not recreating.");
          }
        } else
        {
          //  No file yet, create an empty file.
          fileObject.createFile();
          log.logBasic(toString(), "File [" + realFilename + "] created!");
          result.setResult(true);
        }
      } catch (IOException e)
      {
        log.logError(toString(), "Could not create file [" + realFilename + "], exception: " + e.getMessage());
        result.setResult(false);
        result.setNrErrors(1);
      } finally
      {
        if (fileObject != null)
        {
          try
          {
            fileObject.close();
          } catch (IOException ex)
          {
          }
          ;
        }
      }
    } else
    {
      log.logError(toString(), "No filename is defined.");
    }

    return result;
  }

  public boolean evaluates()
  {
    return true;
  }

  public boolean isFailIfFileExists()
  {
    return failIfFileExists;
  }

  public void setFailIfFileExists(boolean failIfFileExists)
  {
    this.failIfFileExists = failIfFileExists;
  }

  public static void main(String[] args)
  {
    List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>();
    new JobEntryCreateFile().check(remarks, null);
    System.out.printf("Remarks: %s\n", remarks);
  }

  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
  {
    ValidatorContext ctx = new ValidatorContext();
    putVariableSpace(ctx, getVariables());
    putValidators(ctx, notNullValidator(), fileDoesNotExistValidator());
    andValidator().validate(this, "filename", remarks, ctx); //$NON-NLS-1$
  }

}