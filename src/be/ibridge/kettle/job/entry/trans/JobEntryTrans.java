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
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

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
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.JobEntryBase;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;


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
	
    private String directoryPath;
	
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
	
	public JobEntryTrans(JobEntryBase jeb)
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
        StringBuffer retval = new StringBuffer();

		retval.append(super.getXML());
		
		retval.append("      "+XMLHandler.addTagValue("filename",          filename));
		retval.append("      "+XMLHandler.addTagValue("transname",         transname));
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
        retval.append("      "+XMLHandler.addTagValue("exec_per_row",      execPerRow));
        retval.append("      "+XMLHandler.addTagValue("clear_rows",        clearResultRows));
        retval.append("      "+XMLHandler.addTagValue("clear_files",       clearResultFiles));
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

			// How many arguments?
			int argnr = 0;
			while ( XMLHandler.getTagValue(entrynode, "argument"+argnr)!=null) argnr++;
			arguments = new String[argnr];
			
			// Read them all...
			for (int a=0;a<argnr;a++) arguments[a]=XMLHandler.getTagValue(entrynode, "argument"+a);
		}
		catch(KettleException e)
		{
			throw new KettleXMLException("Unable to load transformation job entry from XML node", e);
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
			throw new KettleException("Unable to load job entry of type transMeta from the repository for id_jobentry="+id_jobentry, dbe);
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
			throw new KettleException("unable to save job entry of type transMeta to the repository for id_job="+id_job, dbe);
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
	 * @param prev_result The result of the previous execution
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
                appender = LogWriter.createFileAppender(getLogFilename(), true);
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
		
        log.logBasic(toString(), "Opening filename : ["+StringUtil.environmentSubstitute(getFileName())+"]");
        
        if (!Const.isEmpty(getFileName()))
        {
            log.logBasic(toString(), "Opening transformation: ["+StringUtil.environmentSubstitute(getFileName())+"]");
        }
        else
        {
            log.logBasic(toString(), "Opening transformation: ["+StringUtil.environmentSubstitute(getTransname())+"] in directory ["+directory.getPath()+"]");
        }
		
        int iteration = 0;
        String args[] = arguments;
        Row resultRow = null;
        boolean first = true;
        List rows = result.getRows();
        
        while( ( first && !execPerRow ) || ( execPerRow && rows!=null && iteration<rows.size() && result.getNrErrors()==0 ) )
        {
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
                log.logDetailed(toString(), "Starting transformation...(file="+getFileName()+", name="+getName()+"), repinfo="+getDescription());
                
                TransMeta transMeta = getTransMeta(rep);
                if (parentJob.getJobMeta().isBatchIdPassed())
                {
                    transMeta.setJobBatchId(parentJob.getJobMeta().getBatchId());
                }
    
                // Create the transformation from meta-data
                Trans trans = new Trans(logwriter, transMeta);
                
                // Set the result rows for the next one...
                trans.getTransMeta().setPreviousResult(result);

                if (clearResultRows)
                {
                	trans.getTransMeta().getPreviousResult().setRows(new ArrayList());
                }

                if (clearResultFiles)
                {
                	trans.getTransMeta().getPreviousResult().getResultFiles().clear();
                }

                // set the parent job on the transformation, variables are taken from here...
                trans.setParentJob(parentJob);
                
                // Pass along the kettle variables...
                // LocalVariables localVariables = LocalVariables.getInstance();
                // localVariables.createKettleVariables(Thread.currentThread(), parentJob, false);

                
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
                        trans.getTransMeta().getPreviousResult().getRows().addAll(newList);
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
                        // Keep it as it was: set all rows on the previous result
                    	// However, in this case, it doesn't make sense to do so if you selected 
                    	// to clear the result rows before running...
                    	if (!clearResultRows) 
                    	{
                    		trans.getTransMeta().getPreviousResult().getRows().addAll(result.getRows());
                    	}
                    }
                }
                
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
                    	ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_LOG, new File(getLogFilename()), parentJob.getName(), toString());
    					result.getResultFiles().add(resultFile);
    				}
    			}
    		}
    		catch(KettleException e)
    		{
    			log.logError(toString(), "Unable to open transformation: "+e.getMessage());
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

		return result;
	}

	private TransMeta getTransMeta(Repository rep) throws KettleException
    {
        LogWriter log = LogWriter.getInstance();
        
        TransMeta transMeta = null;
        if (!Const.isEmpty(getFileName())) // Load from an XML file
        {
            log.logBasic(toString(), "Loading transformation from XML file ["+StringUtil.environmentSubstitute(getFileName())+"]");
            transMeta = new TransMeta(StringUtil.environmentSubstitute(getFileName()));
        }
        else
        if (!Const.isEmpty(getTransname()) && getDirectory() != null)  // Load from the repository
        {
            log.logBasic(toString(), "Loading transformation from repository ["+StringUtil.environmentSubstitute(getTransname())+"] in directory ["+getDirectory()+"]");
            transMeta = new TransMeta(rep, StringUtil.environmentSubstitute(getTransname()), getDirectory());
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
}
