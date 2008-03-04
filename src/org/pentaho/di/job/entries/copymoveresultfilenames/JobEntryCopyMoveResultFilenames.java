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
 
package org.pentaho.di.job.entries.copymoveresultfilenames;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileDoesNotExistValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Node;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileUtil;
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
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.addresultfilenames.Messages;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.Repository;



/**
 * This defines a 'copymoveresultfilenames' job entry. Its main use would be to copy or move files in the
 * result filenames to a destination folder.
 * that can be used to control the flow in ETL cycles.
 * 
 * @author Samatar
 * @since 25-02-2008
 *
 */
public class JobEntryCopyMoveResultFilenames extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String foldername;
	private boolean specifywilcard;
	private String wildcard;
	private String wildcardexclude;
	private String destination_folder;
	public boolean IgnoreRestOfFiles;
	private String nr_errors_less_than;
	private String success_condition;
	private boolean add_date;
	private boolean add_time;
	private boolean SpecifyFormat;
	private String date_time_format;
	private boolean AddDateBeforeExtension;
	private String action;

	private boolean OverwriteFile;
	private boolean CreateDestinationFolder;
	boolean RemovedSourceFilename;
	boolean AddDestinationFilename;
	
	int NrErrors=0;
	boolean DoNotProcessRest=false;
	
	public JobEntryCopyMoveResultFilenames(String n)
	{
		super(n, "");
		RemovedSourceFilename=true;
		AddDestinationFilename=true;
		CreateDestinationFolder=false;
		foldername=null;
		wildcardexclude=null;
		wildcard=null;
		specifywilcard=false;
		
		OverwriteFile=false;
		add_date=false;
		add_time=false;
		SpecifyFormat=false;
		date_time_format=null;
		AddDateBeforeExtension=false;
		destination_folder=null;
		IgnoreRestOfFiles=true;
		nr_errors_less_than="10";

		action="copy";
		success_condition="success_when_all_works_fine";
		setID(-1L);
		setJobEntryType(JobEntryType.COPY_MOVE_RESULT_FILENAMES);
		
		
	}


	public JobEntryCopyMoveResultFilenames()
	{
		this("");
	}


	public JobEntryCopyMoveResultFilenames(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
    	JobEntryCopyMoveResultFilenames je = (JobEntryCopyMoveResultFilenames) super.clone();
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
		retval.append("      ").append(XMLHandler.addTagValue("destination_folder",   destination_folder));
		retval.append("      ").append(XMLHandler.addTagValue("IgnoreRestOfFiles", IgnoreRestOfFiles));
		retval.append("      ").append(XMLHandler.addTagValue("nr_errors_less_than", nr_errors_less_than));
		retval.append("      ").append(XMLHandler.addTagValue("success_condition", success_condition));
		retval.append("      ").append(XMLHandler.addTagValue("add_date", add_date));
		retval.append("      ").append(XMLHandler.addTagValue("add_time", add_time));
		retval.append("      ").append(XMLHandler.addTagValue("SpecifyFormat", SpecifyFormat));
		retval.append("      ").append(XMLHandler.addTagValue("date_time_format", date_time_format));
		retval.append("      ").append(XMLHandler.addTagValue("action",   action));
		retval.append("      ").append(XMLHandler.addTagValue("AddDateBeforeExtension", AddDateBeforeExtension));
		retval.append("      ").append(XMLHandler.addTagValue("OverwriteFile", OverwriteFile));
		retval.append("      ").append(XMLHandler.addTagValue("CreateDestinationFolder", CreateDestinationFolder));
		retval.append("      ").append(XMLHandler.addTagValue("RemovedSourceFilename", RemovedSourceFilename));
		retval.append("      ").append(XMLHandler.addTagValue("AddDestinationFilename", AddDestinationFilename));
		
		
				
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
			destination_folder = XMLHandler.getTagValue(entrynode, "destination_folder");
			IgnoreRestOfFiles = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "IgnoreRestOfFiles") );
			nr_errors_less_than          = XMLHandler.getTagValue(entrynode, "nr_errors_less_than");
			success_condition          = XMLHandler.getTagValue(entrynode, "success_condition");
			add_date = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "add_date"));
			add_time = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "add_time"));
			SpecifyFormat = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "SpecifyFormat"));
			AddDateBeforeExtension = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "AddDateBeforeExtension"));
			
			date_time_format          = XMLHandler.getTagValue(entrynode, "date_time_format");
			action = XMLHandler.getTagValue(entrynode, "action");
			
			OverwriteFile = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "OverwriteFile"));
			CreateDestinationFolder = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "CreateDestinationFolder"));
			RemovedSourceFilename = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "RemovedSourceFilename"));
			AddDestinationFilename = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "AddDestinationFilename"));
			
			
			
			
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException(Messages.getString("JobEntryCopyMoveResultFilenames.CanNotLoadFromXML", xe.getMessage()));
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
			destination_folder = rep.getJobEntryAttributeString(id_jobentry, "destination_folder");
			IgnoreRestOfFiles = rep.getJobEntryAttributeBoolean(id_jobentry, "IgnoreRestOfFiles");
			nr_errors_less_than  = rep.getJobEntryAttributeString(id_jobentry, "nr_errors_less_than");
			success_condition  = rep.getJobEntryAttributeString(id_jobentry, "success_condition");
			add_date = rep.getJobEntryAttributeBoolean(id_jobentry, "add_date"); 
			add_time = rep.getJobEntryAttributeBoolean(id_jobentry, "add_time"); 
			SpecifyFormat = rep.getJobEntryAttributeBoolean(id_jobentry, "SpecifyFormat"); 
			date_time_format  = rep.getJobEntryAttributeString(id_jobentry, "date_time_format");
			AddDateBeforeExtension = rep.getJobEntryAttributeBoolean(id_jobentry, "AddDateBeforeExtension"); 
			action = rep.getJobEntryAttributeString(id_jobentry, "action");
			
			OverwriteFile = rep.getJobEntryAttributeBoolean(id_jobentry, "OverwriteFile"); 
			CreateDestinationFolder = rep.getJobEntryAttributeBoolean(id_jobentry, "CreateDestinationFolder");
			RemovedSourceFilename = rep.getJobEntryAttributeBoolean(id_jobentry, "RemovedSourceFilename");
			AddDestinationFilename = rep.getJobEntryAttributeBoolean(id_jobentry, "AddDestinationFilename");
			
			
			
			
		}
		catch(KettleException dbe)
		{
			throw new KettleXMLException(Messages.getString("JobEntryCopyMoveResultFilenames.CanNotLoadFromRep",""+id_jobentry, dbe.getMessage()));
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

			rep.saveJobEntryAttribute(id_job, getID(), "destination_folder", destination_folder);
			rep.saveJobEntryAttribute(id_job, getID(), "IgnoreRestOfFiles", IgnoreRestOfFiles);
			rep.saveJobEntryAttribute(id_job, getID(), "nr_errors_less_than",      nr_errors_less_than);
			rep.saveJobEntryAttribute(id_job, getID(), "success_condition",      success_condition);
			rep.saveJobEntryAttribute(id_job, getID(), "add_date", add_date);
			rep.saveJobEntryAttribute(id_job, getID(), "add_time", add_time);
			rep.saveJobEntryAttribute(id_job, getID(), "SpecifyFormat", SpecifyFormat);
			rep.saveJobEntryAttribute(id_job, getID(), "date_time_format",      date_time_format);
			rep.saveJobEntryAttribute(id_job, getID(), "AddDateBeforeExtension", AddDateBeforeExtension);
			rep.saveJobEntryAttribute(id_job, getID(), "action", action);
		
			rep.saveJobEntryAttribute(id_job, getID(), "OverwriteFile", OverwriteFile);
			rep.saveJobEntryAttribute(id_job, getID(), "CreateDestinationFolder", CreateDestinationFolder);
			rep.saveJobEntryAttribute(id_job, getID(), "RemovedSourceFilename", RemovedSourceFilename);
			rep.saveJobEntryAttribute(id_job, getID(), "AddDestinationFilename", AddDestinationFilename);
			
				
	
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleXMLException(Messages.getString("JobEntryCopyMoveResultFilenames.CanNotSaveToRep",""+id_job, dbe.getMessage()));
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
  
    public void setAddDate(boolean adddate)
    {
    	this.add_date=adddate;
    }
    
    public boolean  isAddDate()
    {
    	return add_date;
    }
    
    
    public void setAddTime(boolean addtime)
    {
    	this.add_time=addtime;
    }
    
    public boolean  isAddTime()
    {
    	return add_time;
    }
    
    public void setAddDateBeforeExtension(boolean AddDateBeforeExtension)
    {
    	this.AddDateBeforeExtension=AddDateBeforeExtension;
    }
    
    
    public boolean  isAddDateBeforeExtension()
    {
    	return AddDateBeforeExtension;
    }
    public boolean  isOverwriteFile()
    {
    	return OverwriteFile;
    } 
    
    public void setOverwriteFile(boolean OverwriteFile)
    {
    	this.OverwriteFile=OverwriteFile;
    }
    public void setCreateDestinationFolder(boolean CreateDestinationFolder)
    {
    	this.CreateDestinationFolder=CreateDestinationFolder;
    }
    public boolean  isCreateDestinationFolder()
    {
    	return CreateDestinationFolder;
    } 
    public boolean  isRemovedSourceFilename()
    {
    	return RemovedSourceFilename;
    } 
    public void setRemovedSourceFilename(boolean RemovedSourceFilename)
    {
    	this.RemovedSourceFilename=RemovedSourceFilename;
    }
    public void setAddDestinationFilename(boolean AddDestinationFilename)
    {
    	this.AddDestinationFilename=AddDestinationFilename;
    }
    public boolean  isAddDestinationFilename()
    {
    	return AddDestinationFilename;
    }
    
    
    public boolean  isSpecifyFormat()
    {
    	return SpecifyFormat;
    }
    
    public void setSpecifyFormat(boolean SpecifyFormat)
    {
    	this.SpecifyFormat=SpecifyFormat;
    }
    
    public void setDestinationFolder(String destinationFolder)
    {
    	this.destination_folder=destinationFolder;
    }
    
    public String  getDestinationFolder()
    {
    	return destination_folder;
    }
    
    public boolean isIgnoreRestOfFiles()
    {
    	return IgnoreRestOfFiles;
    }
    
    
    
    public void setIgnoreRestOfFiles(boolean IgnoreRestOfFiles)
	{
		this.IgnoreRestOfFiles=IgnoreRestOfFiles;
	}
	
	public void setNrErrorsLessThan(String nr_errors_less_than)
	{
		this.nr_errors_less_than=nr_errors_less_than;
	}
	
	public String getNrErrorsLessThan()
	{
		return nr_errors_less_than;
	}
	
	
	public void setSuccessCondition(String success_condition)
	{
		this.success_condition=success_condition;
	}
	public String getSuccessCondition()
	{
		return success_condition;
	}
	
	public void setAction(String action)
	{
		this.action=action;
	}
	
	public String getAction()
	{
		return action;
	}
	
	public String getDateTimeFormat()
	{
		return date_time_format;
	}
	public void setDateTimeFormat(String date_time_format)
	{
		this.date_time_format=date_time_format;
	}
	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();
		Result result = previousResult;
		result.setResult(false);
		String realdestinationFolder=environmentSubstitute(getDestinationFolder());
		
		if(!CreateDestinationFolder(realdestinationFolder, log))
		{
			result.setResult( false );
			result.setNrErrors(1);
		}
			
		
		if(previousResult!=null)
		{
			NrErrors=0;
			DoNotProcessRest=false;
			FileObject file = null;

			try
			{
				int size=result.getResultFiles().size();
				log.logBasic(toString(),Messages.getString("JobEntryCopyMoveResultFilenames.log.FilesFound",""+size));

				List <ResultFile> resultFiles = result.getResultFilesList();
			    if (resultFiles != null && resultFiles.size() > 0)
			    {
			      	for (Iterator <ResultFile>  it = resultFiles.iterator() ; it.hasNext();)
			        {
			       	  ResultFile resultFile = (ResultFile) it.next();
			          file = resultFile.getFile();
			          if (file != null && file.exists())
			          {
			           	if(!specifywilcard || 
			           			(CheckFileWildcard(file.getName().getBaseName(), environmentSubstitute(wildcard),true) 
			           			&& !CheckFileWildcard(file.getName().getBaseName(),  environmentSubstitute(wildcardexclude),false)
			           			&&specifywilcard))
			  			{
			           		if(!DoNotProcessRest)
			           		{
				        		// Copy or Move file
								if(!ProcessFile(file,realdestinationFolder,log,result,parentJob))
								{
									// Update Errors
									updateErrors();
								}
			           		}else
			           		{
			           			if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("JobEntryCopyMoveResultFilenames.log.IgnoringFile",file.toString()));
			           	
			           		}
			           	}  
			           	 
			           }else
			           {
			        	   log.logError(toString(),Messages.getString("JobEntryCopyMoveResultFilenames.log.ErrorCanNotFindFile",file.toString()));  
						   // Update Errors
						   updateErrors();
			           }
			         }// end for
			     }     
				
			}
			catch(Exception e)
			{
				log.logError(toString(), Messages.getString("JobEntryCopyMoveResultFilenames.Error",e.toString()));
			}	
			finally 
			{
				if ( file != null )
				{
					try  
					{
						file.close();
						
					}
					catch ( Exception ex ) {};
				}
			}
		}
		// Success Condition
		if (getStatus())
		{
			result.setResult( false );
			result.setNrErrors(NrErrors);	
		}else
			result.setResult(true);
		
		return result;
	}
	private void updateErrors()
	{
		NrErrors++;
		if(isIgnoreRestOfFiles()) 
		{
			if(getStatus()) DoNotProcessRest=true;
		}
	}
	private boolean getStatus()
	{
		boolean retval=false;
		int limitErrors=Const.toInt(environmentSubstitute(getNrErrorsLessThan()),10);
		if ((NrErrors>0 && getSuccessCondition().equals("success_when_all_works_fine"))
				|| (NrErrors>=limitErrors && !getSuccessCondition().equals("success_when_all_works_fine")))
				//|| (NrErrors>0 &&  limitErrors==0))
			{
				retval=true;	
			}
		
		return retval;
	}
	private boolean CreateDestinationFolder(String foldername,LogWriter log)
	{
		FileObject folder=null;
		try
		{
			folder=KettleVFS.getFileObject(foldername);
			
    		if(!folder.exists())	
    		{
    			log.logError(toString(), Messages.getString("JobEntryCopyMoveResultFilenames.Log.FolderNotExists", foldername));
    			if(isCreateDestinationFolder())
    				folder.createFolder();
    			else
    				return false;
    			log.logBasic(toString(), Messages.getString("JobEntryCopyMoveResultFilenames.Log.FolderCreated",foldername));
    		}else
    		{
    			if(log.isDetailed())
    				log.logDetailed(toString(), Messages.getString("JobEntryCopyMoveResultFilenames.Log.FolderExists",foldername));	
    		}
    		return true;
		}
		catch (Exception e) {
			log.logError(toString(), Messages.getString("JobEntryCopyMoveResultFilenames.Log.CanNotCreatedFolder", foldername,e.toString()));
			
		}
		 finally {
         	if ( folder != null )
         	{
         		try  {
         			folder.close();
         		}
         		catch (Exception ex ) {};
         	}
         }
		 return false;
	}

	private boolean ProcessFile(FileObject sourcefile,String destinationFolder,LogWriter log,Result result,Job parentJob) 
	{
		boolean retval=false;
		boolean filexists=false;
		try
		{
			// return destination short filename
			String shortfilename=getDestinationFilename(sourcefile.getName().getBaseName());
			// build full destination filename
			String destinationFilename=destinationFolder+Const.FILE_SEPARATOR+shortfilename;
			FileObject destinationfile=KettleVFS.getFileObject(destinationFilename);
			filexists=destinationfile.exists();
			if(filexists)
			{
				if(log.isDetailed())
					log.logDetailed(toString(),Messages.getString("JobEntryCopyMoveResultFilenames.Log.FileExists",destinationFilename));
			}
			if((!filexists)  || (filexists && isOverwriteFile()))
			{
				if(getAction().equals("copy"))
				{
					// Copy file
					FileUtil.copyContent(sourcefile, destinationfile);
					if(log.isDetailed()) 
						log.logDetailed(toString(),Messages.getString("JobEntryCopyMoveResultFilenames.log.CopiedFile",sourcefile.toString(),destinationFolder));
				}else{
					// Move file
					sourcefile.moveTo(destinationfile);	
					if(log.isDetailed()) 
						log.logDetailed(toString(),Messages.getString("JobEntryCopyMoveResultFilenames.log.MovedFile",sourcefile.toString(),destinationFolder));
				}
				
				if(isRemovedSourceFilename())
				{
					// Remove source file from result files list
					result.getResultFiles().remove(sourcefile.toString());
					if(log.isDetailed())
						log.logDetailed(toString(),Messages.getString("JobEntryCopyMoveResultFilenames.RemovedFileFromResult",sourcefile.toString()));
				}
				if(isAddDestinationFilename())
				{
					// Add destination filename to Resultfilenames ...
					ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject(destinationfile.toString()), parentJob.getName(), toString());
					result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
					if(log.isDetailed())
						log.logDetailed(toString(),Messages.getString("JobEntryCopyMoveResultFilenames.AddedFileToResult",destinationfile.toString()));
				}
			}
			retval=true;
		}catch(Exception e)
		{
			log.logError(toString(), Messages.getString("JobEntryCopyMoveResultFilenames.Log.ErrorProcessing", e.toString()));
		}
		
		return retval;
	}
	
	private String getDestinationFilename(String shortsourcefilename) throws Exception
	{
		String shortfilename=shortsourcefilename;
		int lenstring=shortsourcefilename.length();
		int lastindexOfDot=shortfilename.lastIndexOf('.');
		if(isAddDateBeforeExtension())
			shortfilename=shortfilename.substring(0, lastindexOfDot);
		
			
		SimpleDateFormat daf  = new SimpleDateFormat();
		Date now = new Date();
	
		if(isSpecifyFormat() && !Const.isEmpty(getDateTimeFormat()))
		{
			daf.applyPattern(getDateTimeFormat());
			String dt = daf.format(now);
			shortfilename+=dt;
		}else
		{
			if (isAddDate())
			{
				daf.applyPattern("yyyyMMdd");
				String d = daf.format(now);
				shortfilename+="_"+d;
			}
			if (isAddTime())
			{
				daf.applyPattern("HHmmssSSS");
				String t = daf.format(now);
				shortfilename+="_"+t;
			}
		}
		if(isAddDateBeforeExtension())
			shortfilename+=shortsourcefilename.substring(lastindexOfDot, lenstring);
		
		
		return shortfilename;
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