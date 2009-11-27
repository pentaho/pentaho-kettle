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
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.ObjectId;
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
  private static Class<?> PKG = JobEntryCreateFile.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
  private String filename;

  private boolean failIfFileExists;
  private boolean addfilenameresult;

  public JobEntryCreateFile(String n)
  {
    super(n, "");
    filename = null;
    failIfFileExists = true;
    addfilenameresult=false;
    setID(-1L);
  }

  public JobEntryCreateFile()
  {
    this("");
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
    retval.append("      ").append(XMLHandler.addTagValue("add_filename_result", addfilenameresult));
    

    return retval.toString();
  }

  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
  {
    try
    {
      super.loadXML(entrynode, databases, slaveServers);
      filename = XMLHandler.getTagValue(entrynode, "filename");
      failIfFileExists = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "fail_if_file_exists"));
      addfilenameresult = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "add_filename_result"));
      
    } catch (KettleXMLException xe)
    {
      throw new KettleXMLException("Unable to load job entry of type 'create file' from XML node", xe);
    }
  }

  public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
  {
    try
    {
      filename = rep.getJobEntryAttributeString(id_jobentry, "filename");
      failIfFileExists = rep.getJobEntryAttributeBoolean(id_jobentry, "fail_if_file_exists");
      addfilenameresult = rep.getJobEntryAttributeBoolean(id_jobentry, "add_filename_result");
      
    } catch (KettleException dbe)
    {
      throw new KettleException("Unable to load job entry of type 'create file' from the repository for id_jobentry="
          + id_jobentry, dbe);
    }
  }

  public void saveRep(Repository rep, ObjectId id_job) throws KettleException
  {
    try
    {
      rep.saveJobEntryAttribute(id_job, getObjectId(), "filename", filename);
      rep.saveJobEntryAttribute(id_job, getObjectId(), "fail_if_file_exists", failIfFileExists);
      rep.saveJobEntryAttribute(id_job, getObjectId(), "add_filename_result", addfilenameresult);
      
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

  public Result execute(Result previousResult, int nr) throws KettleException
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
        fileObject = KettleVFS.getFileObject(realFilename, this);

        if (fileObject.exists())
        {
          if (isFailIfFileExists())
          {
            // File exists and fail flag is on.
            result.setResult(false);
            logError("File [" + realFilename + "] exists, failing.");
          } else
          {
            // File already exists, no reason to try to create it
            result.setResult(true);
            logBasic("File [" + realFilename + "] already exists, not recreating.");
          }
          // add filename to result filenames if needed
          if(isAddFilenameToResult())
        	  addFilenameToResult(realFilename,log,result, parentJob);
        } else
        {
          //  No file yet, create an empty file.
          fileObject.createFile();
          logBasic("File [" + realFilename + "] created!");
          // add filename to result filenames if needed
          if(isAddFilenameToResult())
        	  addFilenameToResult(realFilename,log,result, parentJob);
          result.setResult(true);
        }
      } catch (IOException e)
      {
        logError("Could not create file [" + realFilename + "], exception: " + e.getMessage());
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
      logError("No filename is defined.");
    }

    return result;
  }
private void addFilenameToResult(String targetFilename,LogWriter log,Result result, Job parentJob) throws  KettleException
{
	FileObject targetFile=null;
	try
	{
		targetFile = KettleVFS.getFileObject(targetFilename, this);
		
		// Add to the result files...
		ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, targetFile, parentJob.getJobname(), toString());
        resultFile.setComment(""); //$NON-NLS-1$
		result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
		
        if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobEntryCreateFile.FileAddedToResult",targetFilename)); //$NON-NLS-1$
	} catch(Exception e)
	{
		throw new KettleException(e);
	}
	finally
	{
		try {
			targetFile.close();
			targetFile=null;
		} catch(Exception e){}
	}
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
  public boolean isAddFilenameToResult()
  {
    return addfilenameresult;
  }
  
  public void setAddFilenameToResult(boolean addfilenameresult)
  {
    this.addfilenameresult = addfilenameresult;
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