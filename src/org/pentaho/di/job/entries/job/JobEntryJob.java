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

package org.pentaho.di.job.entries.job;

import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.Log4jFileAppender;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.www.SlaveServerJobStatus;
import org.w3c.dom.Node;


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
    
    private SlaveServer remoteSlaveServer;

    public JobEntryJob(String name)
	{
		super(name, "");
		setJobEntryType(JobEntryType.JOB);
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
        return environmentSubstitute(getFilename());
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
		retval.append("      ").append(XMLHandler.addTagValue("slave_server_name", remoteSlaveServer!=null ? remoteSlaveServer.getName() : null));

		if (arguments!=null)
		for (int i=0;i<arguments.length;i++)
		{
			retval.append("      ").append(XMLHandler.addTagValue("argument"+i, arguments[i]));
		}

		return retval.toString();
	}

	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases, slaveServers);

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
			
			String remoteSlaveServerName = XMLHandler.getTagValue(entrynode, "slave_server_name");
			remoteSlaveServer = SlaveServer.findSlaveServer(slaveServers, remoteSlaveServerName);

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
	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases, slaveServers);

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

			String remoteSlaveServerName = rep.getJobEntryAttributeString(id_jobentry, "slave_server_name");
			remoteSlaveServer = SlaveServer.findSlaveServer(slaveServers, remoteSlaveServerName);

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
	public void saveRep(Repository rep, long id_job) throws KettleException
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
			rep.saveJobEntryAttribute(id_job, getID(), "slave_server_name", remoteSlaveServer!=null ? remoteSlaveServer.getName() : null);

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
                appender = LogWriter.createFileAppender(environmentSubstitute(getLogFilename()), true);
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

            logwriter = LogWriter.getInstance(environmentSubstitute(getLogFilename()), true, loglevel);
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
                log.logDetailed(toString(), "Loading job from repository : ["+directory+" : "+environmentSubstitute(jobname)+"]");
                jobMeta = new JobMeta(logwriter, rep, environmentSubstitute(jobname), directory);
                jobMeta.setParentVariableSpace(parentJob);
            }
            else // Get it from the XML file
            if (fromXMLFile)
            {
                log.logDetailed(toString(), "Loading job from XML file : ["+environmentSubstitute(filename)+"]");
                jobMeta = new JobMeta(logwriter, environmentSubstitute(filename), rep, null);
                jobMeta.setParentVariableSpace(parentJob);
            }

            if (jobMeta==null)
            {
                throw new KettleException("Unable to load the job: please specify the name and repository directory OR a filename");
            }
            
            verifyRecursiveExecution(parentJob, jobMeta);
    		
            // Tell logging what job entry we are launching...
            if (fromRepository)
            {
                log.logBasic(toString(), "Starting job, loaded from repository : ["+directory+" : "+environmentSubstitute(jobname)+"]");
            }
            else
            if (fromXMLFile)
            {
                log.logDetailed(toString(), "Starting job, loaded from XML file : ["+environmentSubstitute(filename)+"]");
            }

            int iteration = 0;
            String args1[] = arguments;
            if (args1==null || args1.length==0) // no arguments?  Check the parent jobs arguments
            {
                args1 = parentJob.getJobMeta().getArguments();
            }

            copyVariablesFrom(parentJob);
            setParentVariableSpace(parentJob);

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
                	args[idx] = environmentSubstitute(args1[idx]);
                }
            }

            RowMetaAndData resultRow = null;
            boolean first = true;
            List<RowMetaAndData> rows = result.getRows();

            while( ( first && !execPerRow ) || ( execPerRow && rows!=null && iteration<rows.size() && result.getNrErrors()==0 ) )
            {
                first=false;
                if (rows!=null && execPerRow)
                {
                	resultRow = (RowMetaAndData) rows.get(iteration);
                }
                else
                {
                	resultRow = null;
                }
                
                Result oneResult = new Result();
            	
            	List<RowMetaAndData> sourceRows = null;
                
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
                                args[i] = resultRow.getString(i, null);
                            }
                        }
                    }
                    else
                    {
                        // Just pass a single row
                        List<RowMetaAndData> newList = new ArrayList<RowMetaAndData>();
                        newList.add(resultRow);
                        sourceRows = newList;
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
                                args[i] = resultRow.getString(i, null);
                            }
                        }
                    }
                    else
                    {
                        // Keep it as it was...
                        sourceRows = result.getRows();
                    }
                }

                if (remoteSlaveServer==null)
                {
                	// Local execution...
                	//
                	
	                // Create a new job
	                Job job = new Job(logwriter, StepLoader.getInstance(), rep, jobMeta);
	
	                job.shareVariablesWith(this);
	                
	                // Set the source rows we calculated above...
	                //
	                job.setSourceRows(sourceRows);
	
	                // Don't forget the logging...
	                job.beginProcessing();
	
	                // Link the job with the sub-job
	                parentJob.getJobTracker().addJobTracker(job.getJobTracker());
	
	                // Link both ways!
	                job.getJobTracker().setParentJobTracker(parentJob.getJobTracker());
	
	                // Tell this sub-job about its parent...
	                job.setParentJob(parentJob);
	
	                if (parentJob.getJobMeta().isBatchIdPassed())
	                {
	                    job.setPassedBatchId(parentJob.getBatchId());
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
	        		
	        		oneResult = runner.getResult();
                }
                else
                {
                	// Remote execution...
                	//
                	JobExecutionConfiguration jobExecutionConfiguration = new JobExecutionConfiguration();
                	jobExecutionConfiguration.setPreviousResult(result.clone());
                	jobExecutionConfiguration.getPreviousResult().setRows(sourceRows);
                	jobExecutionConfiguration.setArgumentStrings(args);
                	jobExecutionConfiguration.setVariables(this);
                	jobExecutionConfiguration.setRemoteServer(remoteSlaveServer);
                	jobExecutionConfiguration.setRepository(rep);
                	
                	// Send the XML over to the slave server
                	// Also start the job over there...
                	//
                	Job.sendXMLToSlaveServer(jobMeta, jobExecutionConfiguration);
                	
                	// Now start the monitoring...
                	//
                	while (!parentJob.isStopped())
                	{
                		try 
                		{
							SlaveServerJobStatus jobStatus = remoteSlaveServer.getJobStatus(jobMeta.getName());
							if (jobStatus.getResult()!=null)
							{
								// The job is finished, get the result...
								//
								oneResult = jobStatus.getResult();
								break;
							}
						} 
                		catch (Exception e1) {
							log.logError(toString(), "Unable to contact slave server ["+remoteSlaveServer+"] to verify the status of job ["+jobMeta.getName()+"]");
							oneResult.setNrErrors(1L);
							break; // Stop looking too, chances are too low the server will come back on-line
						}
                		
                		try { Thread.sleep(10000); } catch(InterruptedException e) {} ; // sleep for 10 seconds
                	}
                }

                
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

                ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_LOG, appender.getFile(), parentJob.getJobname(), getName());
                result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
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

        return result;
	}
	
	
	
	
	/**
	 * Make sure that we are not loading jobs recursively...
	 * 
	 * @param parentJobMeta the parent job metadata
	 * @param jobMeta the job metadata
	 * @throws KettleException in case both jobs are loaded from the same source
	 */
    private void verifyRecursiveExecution(Job parentJob, JobMeta jobMeta) throws KettleException {
    	
    	if (parentJob==null) return; // OK!
    	
    	JobMeta parentJobMeta = parentJob.getJobMeta();
    	
    	if (parentJobMeta.getName()==null && jobMeta.getName()!=null) return; // OK
    	if (parentJobMeta.getName()!=null && jobMeta.getName()==null) return; // OK as well.
    	
		// Not from the repository? just verify the filename
		//
		if (jobMeta.getFilename()!=null && jobMeta.getFilename().equals(parentJobMeta.getFilename()))
		{
			throw new KettleException(Messages.getString("JobJobError.Recursive", jobMeta.getFilename()));
		}

		// Different directories: OK
		if (parentJobMeta.getDirectory()==null && jobMeta.getDirectory()!=null) return; 
		if (parentJobMeta.getDirectory()!=null && jobMeta.getDirectory()==null) return; 
		if (jobMeta.getDirectory().getID() != parentJobMeta.getDirectory().getID()) return;
		
		// Same names, same directories : loaded from same location in the repository: 
		// --> recursive loading taking place!
		//
		if (parentJobMeta.getName().equals(jobMeta.getName()))
		{
			throw new KettleException(Messages.getString("JobJobError.Recursive", jobMeta.getFilename()));
		}
		
		// Also compare with the grand-parent (if there is any)
		verifyRecursiveExecution(parentJob.getParentJob(), jobMeta);
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

    public List<SQLStatement> getSQLStatements(Repository repository) throws KettleException
    {
        return getSQLStatements(repository, null);
    }

    public List<SQLStatement> getSQLStatements(Repository repository, VariableSpace space) throws KettleException
    {
        JobMeta jobMeta = getJobMeta(repository, space);

        return jobMeta.getSQLStatements(repository, null);
    }
    
    
    private JobMeta getJobMeta(Repository rep, VariableSpace space) throws KettleException
    {   	
    	try
    	{
	        if (rep!=null && getDirectory()!=null)
	        {
	            return new JobMeta(LogWriter.getInstance(), 
	            		           rep, 
	            		           (space != null ? space.environmentSubstitute(getJobName()): getJobName()), 
	            		           getDirectory());
	        }
	        else
	        {
	            return new JobMeta(LogWriter.getInstance(), 
	            		           (space != null ? space.environmentSubstitute(getFilename()) : getFilename()), 
	            		           rep, null);
	        }
    	}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error during job metadata load", e);
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

    public List<ResourceReference> getResourceDependencies(JobMeta jobMeta) {
      List<ResourceReference> references = super.getResourceDependencies(jobMeta);
      if (!Const.isEmpty(filename)) {
        String realFileName = jobMeta.environmentSubstitute(filename);
        ResourceReference reference = new ResourceReference(this);
        reference.getEntries().add( new ResourceEntry(realFileName, ResourceType.ACTIONFILE));
        references.add(reference);
      }
      return references;
    }

    /**
     * We're going to load the transformation meta data referenced here.
     * Then we're going to give it a new filename, modify that filename in this entries.
     * The parent caller will have made a copy of it, so it should be OK to do so.
     */
    public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface namingInterface) throws KettleException {
		// Try to load the transformation from repository or file.
		// Modify this recursively too...
		//
		if (!Const.isEmpty(filename)) {
			// AGAIN: there is no need to clone this job entry because the caller is responsible for this.
			//
			// First load the job meta data...
			//
			JobMeta jobMeta = getJobMeta(null, null);

			// Also go down into the job and export the files there. (going down recursively)
			//
			String newFilename = namingInterface.nameResource(jobMeta.getName(), this.filename, "kjb");

			// Set the correct filename inside the XML.
			// Replace if BEFORE XML generation occurs.
			//
			jobMeta.setFilename(newFilename);

			// change it in the job entry
			//
			filename = newFilename;

			//
			// Don't save it, that has already been done a few lines above, in jobMeta.exportResources()
			//
			// String xml = jobMeta.getXML();
			// definitions.put(newFilename, new ResourceDefinition(newFilename, xml));

			return newFilename;
		}
		else {
			return null;
		}
    }

    @Override
    public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
    {
      if (setLogfile) {
        andValidator().validate(this, "logfile", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
      }

      if (null != directory) {
        // if from repo
        andValidator().validate(this, "directory", remarks, putValidators(notNullValidator())); //$NON-NLS-1$
        andValidator().validate(this, "jobName", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
      } else {
        // else from xml file
        andValidator().validate(this, "filename", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
      }
    }

      public static void main(String[] args) {
    List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>();
    new JobEntryJob().check(remarks, null);
    System.out.printf("Remarks: %s\n", remarks);
  }

    protected String getLogfile()
    {
      return logfile;
    }

	/**
	 * @return the remoteSlaveServer
	 */
	public SlaveServer getRemoteSlaveServer() {
		return remoteSlaveServer;
	}

	/**
	 * @param remoteSlaveServer the remoteSlaveServer to set
	 */
	public void setRemoteSlaveServer(SlaveServer remoteSlaveServer) {
		this.remoteSlaveServer = remoteSlaveServer;
	}
}