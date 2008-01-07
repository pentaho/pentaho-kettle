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
 
package be.ibridge.kettle.job.entry.job;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LocalVariables;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.ResultFile;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.logging.Log4jFileAppender;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.vfs.KettleVFS;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.JobEntryBase;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.trans.StepLoader;


/**
 * Recursive definition of a Job.  This step means that an entire Job has to be executed.
 * It can be the same Job, but just make sure that you don't get an endless loop.
 * Provide an escape routine using JobEval.
 * 
 * @author Matt
 * @since 01-10-2003, Rewritten on 18-06-2004
 * 
 */

public class JobEntryJob extends JobEntryBase implements Cloneable, JobEntryInterface
{	
    private static final LogWriter log = LogWriter.getInstance();
    
	private String              jobname;
	private String              filename;
	private RepositoryDirectory directory;

	public  String  arguments[];
	public  boolean argFromPrevious;
    public  boolean execPerRow;
    
	public  boolean setLogfile;
	public  String  logfile, logext;
	public  boolean addDate, addTime;
	public  int     loglevel;
	
	public  boolean parallel;
    private String directoryPath;

    public JobEntryJob(String name)
	{
		super(name, "");
		setType(JobEntryInterface.TYPE_JOBENTRY_JOB);
	}
	
	public JobEntryJob()
	{
		this("");
		clear();
	}
    
    public Object clone()
    {
        JobEntryJob je = (JobEntryJob) super.clone();
        return je;
    }
	
	public JobEntryJob(JobEntryBase jeb)
	{
		super(jeb);
	}

	public void setFileName(String n)
	{
		filename=n;
	}
	
    /**
     * @deprecated use getFilename() instead.
     * @return the filename
     */
	public String getFileName()
	{
		return filename;
	}
    
    public String getFilename()
    {
        return filename;
    }
    
    public String getRealFilename()
    {
        return StringUtil.environmentSubstitute(getFilename());
    }
    
	public void setJobName(String jobname)
	{
		this.jobname=jobname;
	}
	
	public String getJobName()
	{
		return jobname;
	}
	
	public RepositoryDirectory getDirectory()
	{
		return directory;
	}
	
	public void setDirectory(RepositoryDirectory directory)
	{
		this.directory = directory;
	}
				
	public String getLogFilename()
	{
		String retval="";
		if (setLogfile)
		{
			retval+=logfile;
			Calendar cal = Calendar.getInstance();
			if (addDate)
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				retval+="_"+sdf.format(cal.getTime());
			}
			if (addTime)
			{
				SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
				retval+="_"+sdf.format(cal.getTime());
			}
			if (logext!=null && logext.length()>0)
			{
				retval+="."+logext;
			}
		}
		return retval;
	}
	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(200);

		retval.append(super.getXML());
		
		retval.append("      ").append(XMLHandler.addTagValue("filename",          filename));
		retval.append("      ").append(XMLHandler.addTagValue("jobname",           jobname));
        if (directory!=null)
        {
            retval.append("      ").append(XMLHandler.addTagValue("directory",         directory.getPath()));
        }
        else
        if (directoryPath!=null)
        {
            retval.append("      ").append(XMLHandler.addTagValue("directory",         directoryPath)); // don't loose this info (backup/recovery)
        }
		retval.append("      ").append(XMLHandler.addTagValue("arg_from_previous", argFromPrevious));
        retval.append("      ").append(XMLHandler.addTagValue("exec_per_row",      execPerRow));
		retval.append("      ").append(XMLHandler.addTagValue("set_logfile",       setLogfile));
		retval.append("      ").append(XMLHandler.addTagValue("logfile",           logfile));
		retval.append("      ").append(XMLHandler.addTagValue("logext",            logext));
		retval.append("      ").append(XMLHandler.addTagValue("add_date",          addDate));
		retval.append("      ").append(XMLHandler.addTagValue("add_time",          addTime));
		retval.append("      ").append(XMLHandler.addTagValue("loglevel",          LogWriter.getLogLevelDesc(loglevel)));

		if (arguments!=null)
		for (int i=0;i<arguments.length;i++)
		{
			retval.append("      ").append(XMLHandler.addTagValue("argument"+i, arguments[i]));
		}
	
		return retval.toString();
	}
					
	public void loadXML(Node entrynode, ArrayList databases, Repository rep) throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
	
			setFileName( XMLHandler.getTagValue(entrynode, "filename") );
			setJobName( XMLHandler.getTagValue(entrynode, "jobname") );
			argFromPrevious = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "arg_from_previous") );
            execPerRow = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "exec_per_row") );
            setLogfile = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "set_logfile") );
			addDate = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "add_date") );
			addTime = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "add_time") );
			logfile = XMLHandler.getTagValue(entrynode, "logfile");
			logext = XMLHandler.getTagValue(entrynode, "logext");
			loglevel = LogWriter.getLogLevel( XMLHandler.getTagValue(entrynode, "loglevel"));
	
            directoryPath = XMLHandler.getTagValue(entrynode, "directory");
            if (rep!=null) // import from XML into a repository for example... (or copy/paste)  
            {
            	directory = rep.getDirectoryTree().findDirectory(directoryPath);
            }
	
			// How many arguments?
			int argnr = 0;
			while ( XMLHandler.getTagValue(entrynode, "argument"+argnr)!=null) argnr++;
			arguments = new String[argnr];
			
			// Read them all...
			for (int a=0;a<argnr;a++) arguments[a]=XMLHandler.getTagValue(entrynode, "argument"+a);
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load 'job' job entry from XML node", xe);
		}
	}
	
	/*
	 * Load the jobentry from repository
	 */
	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);

            jobname = rep.getJobEntryAttributeString(id_jobentry, "name");
            String dirPath = rep.getJobEntryAttributeString(id_jobentry, "dir_path");
            directory = rep.getDirectoryTree().findDirectory(dirPath);
			
			filename          = rep.getJobEntryAttributeString(id_jobentry, "file_name");
			argFromPrevious   = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_from_previous");
            execPerRow        = rep.getJobEntryAttributeBoolean(id_jobentry, "exec_per_row");
	
			setLogfile       = rep.getJobEntryAttributeBoolean(id_jobentry, "set_logfile");
			addDate          = rep.getJobEntryAttributeBoolean(id_jobentry, "add_date");
			addTime          = rep.getJobEntryAttributeBoolean(id_jobentry, "add_time");
			logfile          = rep.getJobEntryAttributeString(id_jobentry, "logfile");
			logext           = rep.getJobEntryAttributeString(id_jobentry, "logext");
			loglevel         = LogWriter.getLogLevel( rep.getJobEntryAttributeString(id_jobentry, "loglevel") );
	
			// How many arguments?
			int argnr = rep.countNrJobEntryAttributes(id_jobentry, "argument");
			arguments = new String[argnr];
			
			// Read them all...
			for (int a=0;a<argnr;a++) 
			{
				arguments[a]= rep.getJobEntryAttributeString(id_jobentry, a, "argument");
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'job' from the repository with id_jobentry="+id_jobentry, dbe);
		}
	}
	
	// Save the attributes of this job entry
	//
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);

			long id_job_attr = rep.getJobID(jobname, directory.getID());
			rep.saveJobEntryAttribute(id_job, getID(), "id_job", id_job_attr);
            rep.saveJobEntryAttribute(id_job, getID(), "name", getJobName());
            rep.saveJobEntryAttribute(id_job, getID(), "dir_path", getDirectory()!=null?getDirectory().getPath():"");
            rep.saveJobEntryAttribute(id_job, getID(), "file_name", filename);
			rep.saveJobEntryAttribute(id_job, getID(), "arg_from_previous", argFromPrevious);
            rep.saveJobEntryAttribute(id_job, getID(), "exec_per_row", execPerRow);
			rep.saveJobEntryAttribute(id_job, getID(), "set_logfile", setLogfile);
			rep.saveJobEntryAttribute(id_job, getID(), "add_date", addDate);
			rep.saveJobEntryAttribute(id_job, getID(), "add_time", addTime);
			rep.saveJobEntryAttribute(id_job, getID(), "logfile", logfile);
			rep.saveJobEntryAttribute(id_job, getID(), "logext", logext);
			rep.saveJobEntryAttribute(id_job, getID(), "loglevel", LogWriter.getLogLevelDesc(loglevel));
			
			// save the arguments...
			if (arguments!=null)
			{
				for (int i=0;i<arguments.length;i++) 
				{
					rep.saveJobEntryAttribute(id_job, getID(), i, "argument", arguments[i]);
				}
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type job to the repository with id_job="+id_job, dbe);
		}
	}	
	
	public Result execute(Result result, int nr, Repository rep, Job parentJob) throws KettleException
	{
		result.setEntryNr( nr );

        LogWriter logwriter = log;
        
        Log4jFileAppender appender = null;
        int backupLogLevel = log.getLogLevel();
        if (setLogfile)
        {
            try
            {
                appender = LogWriter.createFileAppender(StringUtil.environmentSubstitute(getLogFilename()), true);
            }
            catch(KettleException e)
            {
                log.logError(toString(), "Unable to open file appender for file ["+getLogFilename()+"] : "+e.toString());
                log.logError(toString(), Const.getStackTracker(e));
                result.setNrErrors(1);
                result.setResult(false);
                return result;
            }
            log.addAppender(appender);
            log.setLogLevel(loglevel);
            
            logwriter = LogWriter.getInstance(StringUtil.environmentSubstitute(getLogFilename()), true, loglevel);
        }

        try
        {
            // First load the job, outside of the loop...
        	if ( parentJob.getJobMeta() != null )
        	{
        		// reset the internal variables again.
        		// Maybe we should split up the variables even more like in UNIX shells.
        		// The internal variables need to be reset to be able use them properly in 2 sequential sub jobs.
        		parentJob.getJobMeta().setInternalKettleVariables();
        	}
            
            JobMeta jobMeta = null;
            boolean fromRepository = rep!=null && !Const.isEmpty(jobname) && directory!=null;
            boolean fromXMLFile = !Const.isEmpty(filename); 
            if (fromRepository) // load from the repository...
            {
                log.logDetailed(toString(), "Loading job from repository : ["+directory+" : "+StringUtil.environmentSubstitute(jobname)+"]");
                jobMeta = new JobMeta(logwriter, rep, StringUtil.environmentSubstitute(jobname), directory);
            }
            else // Get it from the XML file
            if (fromXMLFile)
            {
                log.logDetailed(toString(), "Loading job from XML file : ["+StringUtil.environmentSubstitute(filename)+"]");
                jobMeta = new JobMeta(logwriter, StringUtil.environmentSubstitute(filename), rep);
            }
            
            if (jobMeta==null)
            {
                throw new KettleException("Unable to load the job: please specify the name and repository directory OR a filename");
            }
            
            // Tell logging what job entry we are launching...
            if (fromRepository)
            {
                log.logBasic(toString(), "Starting job, loaded from repository : ["+directory+" : "+StringUtil.environmentSubstitute(jobname)+"]");
            }
            else
            if (fromXMLFile)
            {
                log.logDetailed(toString(), "Starting job, loaded from XML file : ["+StringUtil.environmentSubstitute(filename)+"]");
            }

            int iteration = 0;
            String args1[] = arguments;
            if (args1==null || args1.length==0) // no arguments?  Check the parent jobs arguments
            {
                args1 = parentJob.getJobMeta().getArguments();
            }

            //
            // For the moment only do variable translation at the start of a job, not
            // for every input row (if that would be switched on)
            //
            String args[] = null;
            if ( args1 != null )
            {
                args = new String[args1.length];
                for ( int idx = 0; idx < args1.length; idx++ )
                {
                	args[idx] = StringUtil.environmentSubstitute(args1[idx]);
                }
            }
            
            Row resultRow = null;
            boolean first = true;
            List rows = new ArrayList(result.getRows());
            
            while( ( first && !execPerRow ) || ( execPerRow && rows!=null && iteration<rows.size() && result.getNrErrors()==0 ) )
            {
            	if (execPerRow)
            	{
            		result.getRows().clear();
            	}
                first=false;
                if (rows!=null && execPerRow)
                {
                	resultRow = (Row) rows.get(iteration);
                }
                else
                {
                	resultRow = null;
                }
                
                // Create a new job 
                Job job = new Job(logwriter, StepLoader.getInstance(), rep, jobMeta);
                
                // Don't forget the logging...
                job.beginProcessing();
                
                
                // Link the job with the sub-job
                parentJob.getJobTracker().addJobTracker(job.getJobTracker()); 
                
                // Link both ways!
                job.getJobTracker().setParentJobTracker(parentJob.getJobTracker()); 
                
                // Tell this sub-job about its parent...
                job.setParentJob(parentJob);
                
                // Variables are passed down automagically now...
                LocalVariables localVariables = LocalVariables.getInstance();
                
                // Create a new KettleVariables instance here...
                localVariables.createKettleVariables(job.getName(), parentJob.getName(), false);
                
                if (parentJob.getJobMeta().isBatchIdPassed())
                {
                    job.setPassedBatchId(parentJob.getBatchId());
                }
    			
                if (execPerRow) // Execute for each input row
                {
                    if (argFromPrevious) // Copy the input row to the (command line) arguments
                    {
                        args = null;
                        if (resultRow!=null)
                        {
                            args = new String[resultRow.size()];
                            for (int i=0;i<resultRow.size();i++)
                            {
                                args[i] = resultRow.getValue(i).getString();
                            }
                        }
                    }
                    else
                    {
                        // Just pass a single row
                        ArrayList newList = new ArrayList();
                        newList.add(resultRow);
                        job.setSourceRows(newList);
                    }
                }
                else
                {
                    if (argFromPrevious)
                    {
                        // Only put the first Row on the arguments
                        args = null;
                        if (resultRow!=null)
                        {
                            args = new String[resultRow.size()];
                            for (int i=0;i<resultRow.size();i++)
                            {
                                args[i] = resultRow.getValue(i).toString();
                            }
                        }
                    }
                    else
                    {
                        // Keep it as it was...
                        job.setSourceRows(result.getRows());
                    }
                }
    
                job.getJobMeta().setArguments( args );
                
                JobEntryJobRunner runner = new JobEntryJobRunner( job, result, nr);
    			Thread jobRunnerThread = new Thread(runner);
                jobRunnerThread.setName( Const.NVL(job.getJobMeta().getName(), job.getJobMeta().getFilename()) );
                jobRunnerThread.start();
        		
                try
                {
        			while (!runner.isFinished() && !parentJob.isStopped())
        			{
        				try { Thread.sleep(100);}
        				catch(InterruptedException e) { }
        			}
            			
        			// if the parent-job was stopped, stop the sub-job too... 
        			if (parentJob.isStopped())
        			{
        				job.stopAll();
        				runner.waitUntilFinished(); // Wait until finished!
        				job.endProcessing("stop", new Result()); // dummy result
        			}
        			else
        			{
        				job.endProcessing("end", runner.getResult()); // the result of the execution to be stored in the log file.
        			}
                }
        		catch(KettleException je)
        		{
        			log.logError(toString(), "Unable to open job entry job with name ["+getName()+"] : "+Const.CR+je.toString());
        			result.setNrErrors(1);
        		}
    
                Result oneResult = runner.getResult();
                if (iteration==0)
                {
                    result.clear();
                }
                result.add(oneResult);
                if (oneResult.getResult()==false) // if one of them fails, set the number of errors
                {
                    result.setNrErrors(result.getNrErrors()+1);
                }
    
                iteration++;
            }
    		
        }
        catch(KettleException ke)
        {
            log.logError(toString(), "Error running job entry 'job' : "+ke.toString());
            log.logError(toString(), Const.getStackTracker(ke));
            
            result.setResult(false);
            result.setNrErrors(1L);
        }

        if (setLogfile)
        {
            if (appender!=null) 
            {
                log.removeAppender(appender);
                appender.close();
                try
                {
                    ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_LOG, KettleVFS.getFileObject(appender.getFile().getAbsolutePath()), parentJob.getJobname(), getName());
                    result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
                }
                catch(IOException e)
                {
                    log.logError(toString(), "Error getting file object from file ["+appender.getFile()+"] : "+e.toString());
                }
            }
            log.setLogLevel(backupLogLevel);
            
        }
        
        if (result.getNrErrors() > 0)
        {
            result.setResult( false );
        }
        else
        {
            result.setResult( true );
        }
        
		// Hint the VM to release handles.
		System.gc();

        return result;
	}

    public void clear()
	{
		super.clear();
		
		jobname=null;
		filename=null;
		directory = new RepositoryDirectory();
		arguments=null;
		argFromPrevious=false;
		addDate=false;
		addTime=false;
		logfile=null;
		logext=null;
		setLogfile=false;
	}

	public boolean evaluates()
	{
		return true;
	}

	public boolean isUnconditional()
	{
		return true;
	}

    public ArrayList getSQLStatements(Repository repository) throws KettleException
    {
        JobMeta jobMeta = getJobMeta(repository);
        
        return jobMeta.getSQLStatements(repository, null);
    }
    
    private JobMeta getJobMeta(Repository rep) throws KettleException
    {
        if (rep!=null && getDirectory()!=null)
        {
            return new JobMeta(LogWriter.getInstance(), rep, getJobName(), getDirectory());
        }
        else
        {
            return new JobMeta(LogWriter.getInstance(), getFilename(), rep);
        }
    }

    /**
     * @return Returns the runEveryResultRow.
     */
    public boolean isExecPerRow()
    {
        return execPerRow;
    }

    /**
     * @param runEveryResultRow The runEveryResultRow to set.
     */
    public void setExecPerRow(boolean runEveryResultRow)
    {
        this.execPerRow = runEveryResultRow;
    }

    public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) 
    {
        return new JobEntryJobDialog(shell,this,rep);
    }
}