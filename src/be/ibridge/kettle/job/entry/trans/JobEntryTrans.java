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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleJobException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.entry.JobEntryBase;
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

	public  boolean setLogfile;
	public  String  logfile, logext;
	public  boolean addDate, addTime;
	public  int     loglevel;
	
	public  boolean parallel;
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
		String retval = "";

		retval+=super.getXML();
		
		retval+="      "+XMLHandler.addTagValue("filename",          filename);
		retval+="      "+XMLHandler.addTagValue("transname",         transname);
        if (directory!=null)
        {
            retval+="      "+XMLHandler.addTagValue("directory",         directory.getPath());
        }
        else
        if (directoryPath!=null)
        {
            retval+="      "+XMLHandler.addTagValue("directory",         directoryPath); // don't loose this info (backup/recovery)
        }
		retval+="      "+XMLHandler.addTagValue("arg_from_previous", argFromPrevious);
		retval+="      "+XMLHandler.addTagValue("set_logfile",       setLogfile);
		retval+="      "+XMLHandler.addTagValue("logfile",           logfile);
		retval+="      "+XMLHandler.addTagValue("logext",            logext);
		retval+="      "+XMLHandler.addTagValue("add_date",          addDate);
		retval+="      "+XMLHandler.addTagValue("add_time",          addTime);
		retval+="      "+XMLHandler.addTagValue("loglevel",          LogWriter.getLogLevelDesc(loglevel));

		if (arguments!=null)
		for (int i=0;i<arguments.length;i++)
		{
			retval+="      "+XMLHandler.addTagValue("argument"+i, arguments[i]);
		}

		return retval;
	}

    public void loadXML(Node entrynode, ArrayList databases) throws KettleXMLException
	{
		try 
		{
            super.loadXML(entrynode, databases);
			
			filename = XMLHandler.getTagValue(entrynode, "filename") ;
			transname = XMLHandler.getTagValue(entrynode, "transname") ;
            
            directoryPath = XMLHandler.getTagValue(entrynode, "directory");
            
            // Sorry, mixing XML and repositories is not going to work.
            // directory = rep.getDirectoryTree().findDirectory(directoryPath);

            argFromPrevious = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "arg_from_previous") );
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
			
			long id_transformation = rep.getJobEntryAttributeInteger(id_jobentry, "id_transformation");
			Row r = rep.getTransformation(id_transformation);
			if (r!=null) 
			{
				transname = r.getString("NAME", null);
				long id_directory =  r.getInteger("ID_DIRECTORY", 0L);
				if (id_directory>0)
				{
					directory = rep.getDirectoryTree().findDirectory(id_directory);
				}
				else
				{
					directory = rep.getDirectoryTree();
				}
				
				// System.out.println("Loading transformation in directory ["+directory+"], id_directory="+id_directory);
			}
			
			filename          = rep.getJobEntryAttributeString(id_jobentry, "filename");
			argFromPrevious = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_from_previous");
	
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
			rep.saveJobEntryAttribute(id_job, getID(), "file_name", filename);
			rep.saveJobEntryAttribute(id_job, getID(), "arg_from_previous", argFromPrevious);
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
		addDate=false;
		addTime=false;
		logfile=null;
		logext=null;
		setLogfile=false;
	}

	
	/**
	 * Execute this job entry and return the result.
	 * In this case it means, just set the result boolean in the Result class.
	 * @param prev_result The result of the previous execution
	 * @return The Result of the execution.
	 */
	public Result execute(Result prev_result, int nr, Repository rep, Job parentJob) throws KettleJobException
	{
		LogWriter log       = LogWriter.getInstance();
		Result result = prev_result;
		result.setEntryNr( nr );

		LogWriter logwriter = log;
		if (setLogfile) logwriter = LogWriter.getInstance(getLogFilename(), true, loglevel);
		
		// Open the transformation...
		// Default directory for now...
		
		log.logBasic(toString(), "Opening transformation: ["+getTransname()+"] in directory ["+directory.getPath()+"]");
		
		try
		{
            log.logDetailed(toString(), "Starting transformation...(file="+getFileName()+", name="+getName()+"), repinfo="+getDescription());
            
            TransMeta transMeta;
            if (getTransname()!=null && getTransname().length()>0 &&  // Load from the repository
                getDirectory()!=null
               )
            {
                transMeta = new TransMeta(rep, getTransname(), getDirectory());
            }
            else  
            if (getFileName()!=null && getFileName().length()>0) // Load from an XML file
            {
               
                transMeta = new TransMeta(getFileName());
            }
            else
            {
                throw new KettleJobException("The transformation to execute is not specified!");
            }

            // Set the arguments...
            transMeta.setArguments(arguments);

            // Create the transformation from meta-data
            Trans trans = new Trans(logwriter, transMeta);
            
            // Set the result rows for the next one...
            trans.getTransMeta().setSourceRows(prev_result.rows);
            
			// Set rows of previous result for this transformations input!
			trans.setSourceRows(prev_result.rows);
				
			// Execute!
			if (!trans.execute(arguments))
			{
				result.setNrErrors(1);
			}
			else
			{
				while (!trans.isFinished() && !parentJob.isStopped())
				{
					try { Thread.sleep(100);}
					catch(InterruptedException e) { }
				}
				
				if (parentJob.isStopped())
				{
					trans.stopAll();
					trans.waitUntilFinished();
					trans.endProcessing("stop");
				}
				else
				{
					trans.endProcessing("end");
				}
				result = trans.getResult();
				result.setEntryNr( nr );
			}
		}
		catch(KettleException e)
		{
			log.logError(toString(), "Unable to open transformation: "+e.getMessage());
			result.setNrErrors(1);
		}
		
		if (setLogfile) logwriter.close();
		
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

	public boolean evaluates()
	{
		return true;
	}

	public boolean isUnconditional()
	{
		return true;
	}

}
