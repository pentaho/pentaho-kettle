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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.JobEntryBase;
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
	private String              jobname;
	private String              filename;
	private RepositoryDirectory directory;

	public  String  arguments[];
	public  boolean argFromPrevious;
    private boolean runEveryResultRow;

    
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
	
	public JobEntryJob(JobEntryBase jeb)
	{
		super(jeb);
	}

	public void setFileName(String n)
	{
		filename=n;
	}
	
	public String getFileName()
	{
		return filename;
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
        StringBuffer retval = new StringBuffer();

		retval.append(super.getXML());
		
		retval.append("      "+XMLHandler.addTagValue("filename",          filename));
		retval.append("      "+XMLHandler.addTagValue("jobname",           jobname));
        if (directory!=null)
        {
            retval.append("      "+XMLHandler.addTagValue("directory",         directory.getPath()));
        }
        else
        if (directoryPath!=null)
        {
            retval.append("      "+XMLHandler.addTagValue("directory",         directoryPath)); // don't loose this info (backup/recovery)
        }
		retval.append("      "+XMLHandler.addTagValue("arg_from_previous", argFromPrevious));
        retval.append("      "+XMLHandler.addTagValue("run_every_result_row", runEveryResultRow));
		retval.append("      "+XMLHandler.addTagValue("set_logfile",       setLogfile));
		retval.append("      "+XMLHandler.addTagValue("logfile",           logfile));
		retval.append("      "+XMLHandler.addTagValue("logext",            logext));
		retval.append("      "+XMLHandler.addTagValue("add_date",          addDate));
		retval.append("      "+XMLHandler.addTagValue("add_time",          addTime));
		retval.append("      "+XMLHandler.addTagValue("loglevel",          LogWriter.getLogLevelDesc(loglevel)));

		if (arguments!=null)
		for (int i=0;i<arguments.length;i++)
		{
			retval.append("      "+XMLHandler.addTagValue("argument"+i, arguments[i]));
		}
	
		return retval.toString();
	}
					
	public void loadXML(Node entrynode, ArrayList databases) throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
	
			setFileName( XMLHandler.getTagValue(entrynode, "filename") );
			setJobName( XMLHandler.getTagValue(entrynode, "jobname") );
			argFromPrevious = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "arg_from_previous") );
            runEveryResultRow = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "run_every_result_row") );
            setLogfile = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "set_logfile") );
			addDate = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "add_date") );
			addTime = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "add_time") );
			logfile = XMLHandler.getTagValue(entrynode, "logfile");
			logext = XMLHandler.getTagValue(entrynode, "logext");
			loglevel = LogWriter.getLogLevel( XMLHandler.getTagValue(entrynode, "loglevel"));
	
            directoryPath = XMLHandler.getTagValue(entrynode, "directory");
            
            // Sorry, mixing XML and repositories is not going to work.
            // directory = rep.getDirectoryTree().findDirectory(directoryPath);
	
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
			
			filename          = rep.getJobEntryAttributeString(id_jobentry, "filename");
			argFromPrevious   = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_from_previous");
            runEveryResultRow = rep.getJobEntryAttributeBoolean(id_jobentry, "run_every_result_row");
	
			setLogfile       = rep.getJobEntryAttributeBoolean(id_jobentry, "set_logfile");
			addDate          = rep.getJobEntryAttributeBoolean(id_jobentry, "add_date");
			addTime          = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_time");
			logfile           = rep.getJobEntryAttributeString(id_jobentry, "logfile");
			logext            = rep.getJobEntryAttributeString(id_jobentry, "logext");
			loglevel          = LogWriter.getLogLevel( rep.getJobEntryAttributeString(id_jobentry, "loglevel") );
	
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
            rep.saveJobEntryAttribute(id_job, getID(), "run_every_result_row", runEveryResultRow);
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
	
	public Result execute(Result prev_result, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();

		Result result = prev_result;
		result.setEntryNr( nr );
		
		LogWriter logwriter = log;
		if (setLogfile) logwriter = LogWriter.getInstance(getLogFilename(), true, loglevel);
		
		try
		{
            JobMeta jobMeta = null;
            if (rep!=null && jobname!=null && jobname.length()>0 && directory!=null) // load from the repository...
            {
                log.logDetailed(toString(), "Loading job from repository : ["+directory+" : "+jobname+"]");
                jobMeta = new JobMeta(logwriter, rep, jobname, directory);
            }
            else // Get it from the XML file
            if (filename!=null)
            {
                log.logDetailed(toString(), "Loading job from XML file : ["+filename+"]");
                jobMeta = new JobMeta(logwriter, filename);
            }
            
            if (jobMeta==null)
            {
                throw new KettleException("Unable to load the job: please specify the name and repository directory OR a filename");
            }
            
            Job job = new Job(logwriter, StepLoader.getInstance(), rep, jobMeta);
            
            parentJob.getJobTracker().addJobTracker(job.getJobTracker()); // Link the job with the sub-job
            job.getJobTracker().setParentJobTracker(parentJob.getJobTracker()); // Link both ways!
            
            if (parentJob.getJobMeta().isBatchIdPassed())
            {
                job.getJobMeta().setBatchId(parentJob.getJobMeta().getBatchId());
            }
			
            int runNr = 0;
            int nrRuns = 1;
            boolean forEvery = false;
            if (prev_result.getRows()!=null) nrRuns = prev_result.getRows().size();
            
            while (runNr<nrRuns && result.getResult())
            {
                // So, for every result row, the job is executed with the one line as the resultset.
                // We can then make a step that sets environment variables etc.
                // 
                Result pass;
                if (forEvery)
                {
                    pass = (Result) prev_result.clone();
                    ArrayList passRows = new ArrayList();
                    passRows.add(prev_result.getRows().get(runNr));
                    pass.setRows(passRows);
                }
                else
                {
                    pass = prev_result;
                }
                
                JobEntryJobRunner runner = new JobEntryJobRunner( job, prev_result, nr);
    			new Thread(runner).start();
    			
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
    				job.endProcessing("stop");
    			}
    			else
    			{
    				job.endProcessing("end");
    			}
    			
    			Result oneResult = runner.getResult();
                if (runNr==0)
                {
                    result = oneResult;
                }
                else
                {
                    result.add(oneResult);
                }
                
                runNr++;
            }
		}
		catch(KettleException je)
		{
			log.logError(toString(), "Unable to open job entry job with name ["+getName()+"] : "+Const.CR+je.toString());
			result.setNrErrors(1);
		}
		
		if (setLogfile) logwriter.close();
		
		if (result.getNrErrors() > 0)
		{
			result.setResult( false );
		}
		else
		{
			result.setResult( true );
		}

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
            return new JobMeta(LogWriter.getInstance(), getFileName());
        }
    }

    /**
     * @return Returns the runEveryResultRow.
     */
    public boolean isRunEveryResultRow()
    {
        return runEveryResultRow;
    }

    /**
     * @param runEveryResultRow The runEveryResultRow to set.
     */
    public void setRunEveryResultRow(boolean runEveryResultRow)
    {
        this.runEveryResultRow = runEveryResultRow;
    }

}

