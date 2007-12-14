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
 
package org.pentaho.di.job.entries.deleteresultfilenames;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileDoesNotExistValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;


import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Node;
import org.apache.commons.vfs.FileObject;

import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.Const;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.Repository;



/**
 * This defines a 'deleteresultfilenames' job entry. Its main use would be to create empty folder
 * that can be used to control the flow in ETL cycles.
 * 
 * @author Samatar
 * @since 26-10-2007
 *
 */
public class JobEntryDeleteResultFilenames extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String foldername;
	private boolean specifywilcard;
	private String wildcard;
	private String wildcardexclude;
	
	public JobEntryDeleteResultFilenames(String n)
	{
		super(n, "");
		foldername=null;
		wildcardexclude=null;
		wildcard=null;
		specifywilcard=false;
		setID(-1L);
		setJobEntryType(JobEntryType.DELETE_RESULT_FILENAMES);
	}


	public JobEntryDeleteResultFilenames()
	{
		this("");
	}


	public JobEntryDeleteResultFilenames(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
    	JobEntryDeleteResultFilenames je = (JobEntryDeleteResultFilenames) super.clone();
        return je;
    }
    
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(50);
		
		retval.append(super.getXML());		
		retval.append("      ").append(XMLHandler.addTagValue("foldername",   foldername));
		retval.append("      ").append(XMLHandler.addTagValue("specify_wilcard", specifywilcard));
		retval.append("      ").append(XMLHandler.addTagValue("wildcard",   wildcard));
		retval.append("      ").append(XMLHandler.addTagValue("wildcardexclude",   wildcardexclude));
		
		
		return retval.toString();
	}
	
	  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	  {
	    try
	    {
	      super.loadXML(entrynode, databases, slaveServers);
			foldername = XMLHandler.getTagValue(entrynode, "foldername");
			specifywilcard = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "specify_wilcard"));
			wildcard = XMLHandler.getTagValue(entrynode, "wildcard");
			wildcardexclude = XMLHandler.getTagValue(entrynode, "wildcardexclude");
			
			
			
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException(Messages.getString("JobEntryDeleteResultFilenames.CanNotLoadFromXML", xe.getMessage()));
		}
	}

	  public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	  {
	    try
	    {
	      super.loadRep(rep, id_jobentry, databases, slaveServers);
			foldername = rep.getJobEntryAttributeString(id_jobentry, "foldername");
			specifywilcard = rep.getJobEntryAttributeBoolean(id_jobentry, "specify_wilcard");  
			wildcard = rep.getJobEntryAttributeString(id_jobentry, "wildcard");
			wildcardexclude = rep.getJobEntryAttributeString(id_jobentry, "wildcardexclude");
			
			
			 
		}
		catch(KettleException dbe)
		{
			throw new KettleXMLException(Messages.getString("JobEntryDeleteResultFilenames.CanNotLoadFromRep",""+id_jobentry, dbe.getMessage()));
		}
	}
	
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			rep.saveJobEntryAttribute(id_job, getID(), "foldername", foldername);
			rep.saveJobEntryAttribute(id_job, getID(), "specify_wilcard", specifywilcard);
			rep.saveJobEntryAttribute(id_job, getID(), "wildcard", wildcard);
			rep.saveJobEntryAttribute(id_job, getID(), "wildcardexclude", wildcardexclude);
			
	
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleXMLException(Messages.getString("JobEntryDeleteResultFilenames.CanNotSaveToRep",""+id_job, dbe.getMessage()));
		}
	}
	
	public void setSpecifyWilcard(boolean specifywilcard)
	{
		this.specifywilcard=specifywilcard;
	}

	public boolean isSpecifyWilcard()
	{
		return specifywilcard;
	}
	public void setFoldername(String foldername)
	{
		this.foldername = foldername;
	}
	
	public String getFoldername()
	{
		return foldername;
	}

    
    public String getWildcard()
    {
    	return wildcard;
    }
    
    public String getWildcardExclude()
    {
    	return wildcardexclude;
    }
    
    public String getRealWildcard()
    {
    	return environmentSubstitute(getWildcard());
    }
    public void setWildcard(String wildcard)
    {
    	this.wildcard=wildcard;
    }
    public void setWildcardExclude(String wildcardexclude)
    {
    	this.wildcardexclude=wildcardexclude;
    }
  

	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();
		Result result = previousResult;
		result.setResult(false);
		
		if(previousResult!=null)
		{
			try
			{
				int size=previousResult.getResultFiles().size();
				log.logBasic(toString(),Messages.getString("JobEntryDeleteResultFilenames.log.FilesFound",""+size));
				if(!specifywilcard)
				{
					// Delete all files
					previousResult.getResultFiles().clear();
					if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("JobEntryDeleteResultFilenames.log.DeletedFiles",""+size));
				}
				else
				{

					 List <ResultFile> resultFiles = result.getResultFilesList();
			        if (resultFiles != null && resultFiles.size() > 0)
			        {
			        	for (Iterator <ResultFile>  it = resultFiles.iterator(); it.hasNext();)
			            {
			        	  ResultFile resultFile = (ResultFile) it.next();
			              FileObject file = resultFile.getFile();
			              if (file != null && file.exists())
			              {
			            	if(CheckFileWildcard(file.getName().getBaseName(), environmentSubstitute(wildcard),true) 
			            			&& !CheckFileWildcard(file.getName().getBaseName(),  environmentSubstitute(wildcardexclude),false))
			  				{
			            		// Remove file from result files list
								ResultFile removeFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, file, parentJob.getName(), toString());
								previousResult.getResultFiles().remove(removeFile);
			            		if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("JobEntryDeleteResultFilenames.log.DeletedFile",file.toString()));
			  				}  
			            	 
			              }
			            }
			        }     
				}
				result.setResult(true);
			}
			catch(Exception e)
			{
				log.logError(toString(), Messages.getString("JobEntryDeleteResultFilenames.Error",e.toString()));
			}
		}	
		return result;
	}
	/**********************************************************
	 * 
	 * @param selectedfile
	 * @param wildcard
	 * @return True if the selectedfile matches the wildcard
	 **********************************************************/
	private boolean CheckFileWildcard(String selectedfile, String wildcard,boolean include)
	{
		Pattern pattern = null;
		boolean getIt=include;
	
        if (!Const.isEmpty(wildcard))
        {
        	 pattern = Pattern.compile(wildcard);
			// First see if the file matches the regular expression!
			if (pattern!=null)
			{
				Matcher matcher = pattern.matcher(selectedfile);
				getIt = matcher.matches();
			}
        }
		
		return getIt;
	}


	public boolean evaluates()
	{
		return true;
	}
    
	  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
	  {
	    ValidatorContext ctx = new ValidatorContext();
	    putVariableSpace(ctx, getVariables());
	    putValidators(ctx, notNullValidator(), fileDoesNotExistValidator());
	    andValidator().validate(this, "filename", remarks, ctx); //$NON-NLS-1$
	  }

}