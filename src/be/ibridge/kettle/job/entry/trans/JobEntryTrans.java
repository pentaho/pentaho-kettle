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
 
package be.ibridge.kettle.job.entry.trans;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.cluster.SlaveServer;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.ResultFile;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleJobException;
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
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransExecutionConfiguration;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.cluster.TransSplitter;
import be.ibridge.kettle.www.SlaveServerTransStatus;
import be.ibridge.kettle.www.WebResult;


/**
 * This is the job entry that defines a transformation to be run.
 * 
 * @author Matt
 * @since 1-10-2003, rewritten on 18-06-2004
 * 
 */
public class JobEntryTrans extends JobEntryBase implements Cloneable, JobEntryInterface
{	
	private String              transname;
	private String              filename;
	private RepositoryDirectory directory;
	
	public  String  arguments[];
	public  boolean argFromPrevious;
    public  boolean execPerRow;
    
    public  boolean clearResultRows;
    public  boolean clearResultFiles;
    
	public  boolean setLogfile;
	public  String  logfile, logext;
	public  boolean addDate, addTime;
	public  int     loglevel;
	
    private String  directoryPath;
    
    private boolean clustering;
	
	public JobEntryTrans(String name)
	{
		super(name, "");
		setType(JobEntryInterface.TYPE_JOBENTRY_TRANSFORMATION);
	}

	public JobEntryTrans()
	{
		this("");
		clear();
	}
    
    public Object clone()
    {
        JobEntryTrans je = (JobEntryTrans) super.clone();
        return je;
    }
	
	public JobEntryTrans(JobEntryBase jeb)
	{
		super(jeb);
	}
	
	public void setFileName(String n)
	{
		filename=n;
	}
	
    /**
     * @deprecated use getFilename() instead
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

	public void setTransname(String transname)
	{
		this.transname=transname;
	}
	
	public String getTransname()
	{
		return transname;
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
        StringBuffer retval = new StringBuffer(300);

		retval.append(super.getXML());
		
		retval.append("      ").append(XMLHandler.addTagValue("filename",          filename));
		retval.append("      ").append(XMLHandler.addTagValue("transname",         transname));
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
        retval.append("      ").append(XMLHandler.addTagValue("clear_rows",        clearResultRows));
        retval.append("      ").append(XMLHandler.addTagValue("clear_files",       clearResultFiles));
		retval.append("      ").append(XMLHandler.addTagValue("set_logfile",       setLogfile));
		retval.append("      ").append(XMLHandler.addTagValue("logfile",           logfile));
		retval.append("      ").append(XMLHandler.addTagValue("logext",            logext));
		retval.append("      ").append(XMLHandler.addTagValue("add_date",          addDate));
		retval.append("      ").append(XMLHandler.addTagValue("add_time",          addTime));
        retval.append("      ").append(XMLHandler.addTagValue("loglevel",          LogWriter.getLogLevelDesc(loglevel)));
        retval.append("      ").append(XMLHandler.addTagValue("cluster",           clustering));

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
			
			filename = XMLHandler.getTagValue(entrynode, "filename") ;
			transname = XMLHandler.getTagValue(entrynode, "transname") ;
            
            directoryPath = XMLHandler.getTagValue(entrynode, "directory");
            if (rep!=null) // import from XML into a repository for example... (or copy/paste) 
            {
            	directory = rep.getDirectoryTree().findDirectory(directoryPath);
            }
            
            argFromPrevious = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "arg_from_previous") );
            execPerRow = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "exec_per_row") );
            clearResultRows = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "clear_rows") );
            clearResultFiles = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "clear_files") );
			setLogfile = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "set_logfile") );
			addDate = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "add_date") );
			addTime = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "add_time") );
			logfile = XMLHandler.getTagValue(entrynode, "logfile");
			logext = XMLHandler.getTagValue(entrynode, "logext");
			loglevel = LogWriter.getLogLevel( XMLHandler.getTagValue(entrynode, "loglevel"));
            clustering = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "cluster") );

			// How many arguments?
			int argnr = 0;
			while ( XMLHandler.getTagValue(entrynode, "argument"+argnr)!=null) argnr++;
			arguments = new String[argnr];
			
			// Read them all...
			for (int a=0;a<argnr;a++) arguments[a]=XMLHandler.getTagValue(entrynode, "argument"+a);
		}
		catch(KettleException e)
		{
			throw new KettleXMLException("Unable to load job entry of type 'trans' from XML node", e);
		}
	}
    	
	// Load the jobentry from repository
	public void loadRep(Repository rep, long id_jobentry, ArrayList databases) throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			
	        transname = rep.getJobEntryAttributeString(id_jobentry, "name");
            String dirPath = rep.getJobEntryAttributeString(id_jobentry, "dir_path");
            directory = rep.getDirectoryTree().findDirectory(dirPath);
     	
			filename         = rep.getJobEntryAttributeString(id_jobentry, "file_name");
			argFromPrevious  = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_from_previous");
            execPerRow       = rep.getJobEntryAttributeBoolean(id_jobentry, "exec_per_row");
            clearResultRows  = rep.getJobEntryAttributeBoolean(id_jobentry, "clear_rows", true);
            clearResultFiles = rep.getJobEntryAttributeBoolean(id_jobentry, "clear_files", true);
			setLogfile       = rep.getJobEntryAttributeBoolean(id_jobentry, "set_logfile");
			addDate          = rep.getJobEntryAttributeBoolean(id_jobentry, "add_date");
			addTime          = rep.getJobEntryAttributeBoolean(id_jobentry, "add_time");
			logfile          = rep.getJobEntryAttributeString(id_jobentry, "logfile");
			logext           = rep.getJobEntryAttributeString(id_jobentry, "logext");
			loglevel         = LogWriter.getLogLevel( rep.getJobEntryAttributeString(id_jobentry, "loglevel") );
            clustering       = rep.getJobEntryAttributeBoolean(id_jobentry, "cluster");
	
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
			throw new KettleException("Unable to load job entry of type 'trans' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}
	
	// Save the attributes of this job entry
	//
	public void saveRep(Repository rep, long id_job) throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			long id_transformation = rep.getTransformationID(transname, directory.getID());
			rep.saveJobEntryAttribute(id_job, getID(), "id_transformation", id_transformation);
            rep.saveJobEntryAttribute(id_job, getID(), "name", getTransname());
            rep.saveJobEntryAttribute(id_job, getID(), "dir_path", getDirectory()!=null?getDirectory().getPath():"");
			rep.saveJobEntryAttribute(id_job, getID(), "file_name", filename);
			rep.saveJobEntryAttribute(id_job, getID(), "arg_from_previous", argFromPrevious);
            rep.saveJobEntryAttribute(id_job, getID(), "exec_per_row", execPerRow);
            rep.saveJobEntryAttribute(id_job, getID(), "clear_rows", clearResultRows);
            rep.saveJobEntryAttribute(id_job, getID(), "clear_files", clearResultFiles);
			rep.saveJobEntryAttribute(id_job, getID(), "set_logfile", setLogfile);
			rep.saveJobEntryAttribute(id_job, getID(), "add_date", addDate);
			rep.saveJobEntryAttribute(id_job, getID(), "add_time", addTime);
			rep.saveJobEntryAttribute(id_job, getID(), "logfile", logfile);
			rep.saveJobEntryAttribute(id_job, getID(), "logext", logext);
			rep.saveJobEntryAttribute(id_job, getID(), "loglevel", LogWriter.getLogLevelDesc(loglevel));
            rep.saveJobEntryAttribute(id_job, getID(), "cluster", clustering);
			
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
			throw new KettleException("Unable to save job entry of type 'trans' to the repository for id_job="+id_job, dbe);
		}
	}
	
	public void clear()
	{
		super.clear();
		
		transname=null;
		filename=null;
		directory = new RepositoryDirectory();
		arguments=null;
		argFromPrevious=false;
        execPerRow=false;
		addDate=false;
		addTime=false;
		logfile=null;
		logext=null;
		setLogfile=false;
		clearResultRows=true;
		clearResultFiles=true;
	}


    /**
     * Execute this job entry and return the result.
     * In this case it means, just set the result boolean in the Result class.
     * @param result The result of the previous execution
     * @param nr the job entry number
     * @param rep the repository connection to use
     * @param parentJob the parent job
     * @return The Result of the execution.
     */
    public Result execute(Result result, int nr, Repository rep, Job parentJob) throws KettleException
	{
		LogWriter log       = LogWriter.getInstance();
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
        }
		
		// Open the transformation...
		// Default directory for now...
		
        log.logBasic(toString(), "Opening filename : ["+StringUtil.environmentSubstitute(getFilename())+"]");
        
        if (!Const.isEmpty(getFilename()))
        {
            log.logBasic(toString(), "Opening transformation: ["+StringUtil.environmentSubstitute(getFilename())+"]");
        }
        else
        {
            log.logBasic(toString(), "Opening transformation: ["+StringUtil.environmentSubstitute(getTransname())+"] in directory ["+directory.getPath()+"]");
        }
		
        // Load the transformation only once for the complete loop!
        TransMeta transMeta = getTransMeta(rep);
        
        int iteration = 0;
        String args1[] = arguments;
        if (args1==null || args1.length==0) // No arguments set, look at the parent job.
        {
            args1 = parentJob.getJobMeta().getArguments();
        }

        //
        // For the moment only do variable translation at the start of a job, not
        // for every input row (if that would be switched on). This is for safety,
        // the real argument setting is later on.
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
        
        while( ( first && !execPerRow ) || ( execPerRow && rows!=null && iteration<rows.size() && result.getNrErrors()==0 ) && !parentJob.isStopped() )
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
            
    		try
    		{
                log.logDetailed(toString(), "Starting transformation...(file="+getFilename()+", name="+getName()+"), repinfo="+getDescription());
                
                // Set the result rows for the next one...
                transMeta.setPreviousResult(result);

                if (clearResultRows)
                {
                    transMeta.getPreviousResult().setRows(new ArrayList());
                }

                if (clearResultFiles)
                {
                    transMeta.getPreviousResult().getResultFiles().clear();
                }

                /*
                 * Set one or more "result" rows on the transformation...
                 */
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
                        
                        // This previous result rows list can be either empty or not.
                        // Depending on the checkbox "clear result rows"
                        // In this case, it would execute the transformation with one extra row each time
                        // Can't figure out a real use-case for it, but hey, who am I to decide that, right?
                        // :-)
                        //
                        transMeta.getPreviousResult().getRows().addAll(newList);
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
                    	// do nothing
                    }
                }

                if (clustering)
                {
                    TransExecutionConfiguration executionConfiguration = new TransExecutionConfiguration();
                    executionConfiguration.setClusterPosting(true);
                    executionConfiguration.setClusterPreparing(true);
                    executionConfiguration.setClusterStarting(true);
                    executionConfiguration.setClusterShowingTransformation(false);
                    executionConfiguration.setSafeModeEnabled(false);
                    TransSplitter transSplitter = Trans.executeClustered(transMeta, executionConfiguration );
                    // 
                    // See if the remote transformations have finished.
                    // We could just look at the master, but I doubt that that is enough in all situations.
                    //
                    SlaveServer[] slaveServers = transSplitter.getSlaveTargets(); // <-- ask these guys
                    TransMeta[] slaves = transSplitter.getSlaves();
                    
                    SlaveServer masterServer = transSplitter.getMasterServer(); // <-- AND this one
                    TransMeta master = transSplitter.getMaster();
                    
                    boolean allFinished = false;
                    long errors = 0L;
                    
                    while (!allFinished && !parentJob.isStopped() && errors==0)
                    {
                        allFinished = true;
                        errors=0L;
                        
                        // Slaves first...
                        //
                        for (int s=0;s<slaveServers.length && allFinished && errors==0;s++)
                        {
                            try
                            {
                                SlaveServerTransStatus transStatus = slaveServers[s].getTransStatus(slaves[s].getName());
                                if (transStatus.isRunning()) allFinished = false;
                                errors+=transStatus.getNrStepErrors();
                            }
                            catch(Exception e)
                            {
                                errors+=1;
                                log.logError(toString(), "Unable to contact slave server '"+slaveServers[s].getName()+"' to check slave transformation : "+e.toString());
                            }
                        }
                        
                        // Check the master too
                        if (allFinished && errors==0)
                        {
                            try
                            {
                                SlaveServerTransStatus transStatus = masterServer.getTransStatus(master.getName());
                                if (transStatus.isRunning()) allFinished = false;
                                errors+=transStatus.getNrStepErrors();
                            }
                            catch(Exception e)
                            {
                                errors+=1;
                                log.logError(toString(), "Unable to contact slave server '"+masterServer.getName()+"' to check master transformation : "+e.toString());
                            }
                        }

                        if (parentJob.isStopped() || errors != 0)
                        {
                            //
                            // Stop all slaves and the master on the slave servers
                            //
                            for (int s=0;s<slaveServers.length && allFinished && errors==0;s++)
                            {
                                try
                                {
                                    WebResult webResult = slaveServers[s].stopTransformation(slaves[s].getName());
                                    if (!WebResult.STRING_OK.equals(webResult.getResult()))
                                    {
                                        log.logError(toString(), "Unable to stop slave transformation '"+slaves[s].getName()+"' : "+webResult.getMessage());
                                    }
                                }
                                catch(Exception e)
                                {
                                    errors+=1;
                                    log.logError(toString(), "Unable to contact slave server '"+slaveServers[s].getName()+"' to stop transformation : "+e.toString());
                                }
                            }

                            try
                            {
                                WebResult webResult = masterServer.stopTransformation(master.getName());
                                if (!WebResult.STRING_OK.equals(webResult.getResult()))
                                {
                                    log.logError(toString(), "Unable to stop master transformation '"+masterServer.getName()+"' : "+webResult.getMessage());
                                }
                            }
                            catch(Exception e)
                            {
                                errors+=1;
                                log.logError(toString(), "Unable to contact master server '"+masterServer.getName()+"' to stop the master : "+e.toString());
                            }
                        }

                        //
                        // Keep waiting until all transformations have finished
                        // If needed, we stop them again and again until they yield.
                        //
                        if (!allFinished)
                        {
                            // Not finished or error: wait a bit longer
                            log.logDetailed(toString(), "Clustered transformation is still running, waiting 10 seconds...");
                            try { Thread.sleep(10000); } catch(Exception e) {} // Check all slaves every 10 seconds. TODO: add 10s as parameter
                        }
                    }
                    
                    result.setNrErrors(errors);
                    
                }
                else // Local execution...
                {
                    // Create the transformation from meta-data
                    Trans trans = new Trans(logwriter, transMeta);
                    
                    if (parentJob.getJobMeta().isBatchIdPassed())
                    {
                        trans.setPassedBatchId(parentJob.getPassedBatchId());
                    }
    
    
                    // set the parent job on the transformation, variables are taken from here...
                    //
                    trans.setParentJob(parentJob);
                    
                    // First get the root job
                    //
                    Job rootJob = parentJob;
                    while (rootJob.getParentJob()!=null) rootJob=rootJob.getParentJob();
                    
                    // Get the start and end-date from the root job...
                    //
                    trans.setJobStartDate( rootJob.getStartDate() );
                    trans.setJobEndDate( rootJob.getEndDate() );
                    
        			// Execute!
        			if (!trans.execute(args))
        			{
                        log.logError(toString(), "Unable to prepare for execution of the transformation");
        				result.setNrErrors(1);
        			}
        			else
        			{
        				while (!trans.isFinished() && !parentJob.isStopped() && trans.getErrors() == 0)
        				{
        					try { Thread.sleep(100);}
        					catch(InterruptedException e) { }
        				}
        				
        				if (parentJob.isStopped() || trans.getErrors() != 0)
        				{
        					trans.stopAll();
        					trans.waitUntilFinished();
        					trans.endProcessing("stop");
                            result.setNrErrors(1);
        				}
        				else
        				{
        					trans.endProcessing("end");
        				}
        				Result newResult = trans.getResult();
                        
                        result.clear(); // clear only the numbers, NOT the files or rows.
                        result.add(newResult);
                        
                        // Set the result rows too...
                        result.setRows(newResult.getRows());
                        
                        if (setLogfile) 
                        {
                        	ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_LOG, KettleVFS.getFileObject(getLogFilename()), parentJob.getName(), toString());
                            result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
        				}
        			}
                }
    		}
    		catch(Exception e)
    		{
    			log.logError(toString(), "Unable to open transformation: "+e.getMessage());
                log.logError(toString(), Const.getStackTracker(e));
    			result.setNrErrors(1);
    		}
            iteration++;
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
		
		if (result.getNrErrors()==0)
		{
			result.setResult( true );
		}
		else
		{
			result.setResult( false );
		}
		
		// Hint the VM to release handles.
		System.gc();

		return result;
	}

	private TransMeta getTransMeta(Repository rep) throws KettleException
    {
        LogWriter log = LogWriter.getInstance();
        
        TransMeta transMeta = null;
        if (!Const.isEmpty(getFilename())) // Load from an XML file
        {
            log.logBasic(toString(), "Loading transformation from XML file ["+StringUtil.environmentSubstitute(getFilename())+"]");
            transMeta = new TransMeta(StringUtil.environmentSubstitute(getFilename()));
        }
        else
        if (!Const.isEmpty(getTransname()) && getDirectory() != null)  // Load from the repository
        {
            log.logBasic(toString(), "Loading transformation from repository ["+StringUtil.environmentSubstitute(getTransname())+"] in directory ["+getDirectory()+"]");
            
            if ( rep != null )
            {
            	//
            	// It only makes sense to try to load from the repository when the repository is also filled in.
            	//
                transMeta = new TransMeta(rep, StringUtil.environmentSubstitute(getTransname()), getDirectory());
            }
            else
            {
            	throw new KettleException("No repository defined!");
            }
        }
        else
        {
            throw new KettleJobException("The transformation to execute is not specified!");
        }

        // Set the arguments...
        transMeta.setArguments(arguments);

        return transMeta;
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
        TransMeta transMeta = getTransMeta(repository);
        
        return transMeta.getSQLStatements();
    }

    /**
     * @return Returns the directoryPath.
     */
    public String getDirectoryPath()
    {
        return directoryPath;
    }

    /**
     * @param directoryPath The directoryPath to set.
     */
    public void setDirectoryPath(String directoryPath)
    {
        this.directoryPath = directoryPath;
    }
    
    public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) {
        return new JobEntryTransDialog(shell,this,rep);
    }

    /**
     * @return the clustering
     */
    public boolean isClustering()
    {
        return clustering;
    }

    /**
     * @param clustering the clustering to set
     */
    public void setClustering(boolean clustering)
    {
        this.clustering = clustering;
    }
}