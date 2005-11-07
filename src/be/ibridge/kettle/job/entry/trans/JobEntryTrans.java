 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** It belongs to, is maintained by and is copyright 1999-2005 by     **
 **                                                                   **
 **      i-Bridge bvba                                                **
 **      Fonteinstraat 70                                             **
 **      9400 OKEGEM                                                  **
 **      Belgium                                                      **
 **      http://www.kettle.be                                         **
 **      info@kettle.be                                               **
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
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.entry.JobEntryBase;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.trans.Trans;


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
	public  boolean arg_from_previous;

	public  boolean set_logfile;
	public  String  logfile, logext;
	public  boolean add_date, add_time;
	public  int     loglevel;
	
	public  boolean parallel;
	
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
		if (set_logfile)
		{
			retval+=logfile;
			Calendar cal = Calendar.getInstance();
			if (add_date)
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				retval+="_"+sdf.format(cal.getTime());
			}
			if (add_time)
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
		retval+="      "+XMLHandler.addTagValue("directory",         directory.getPath());
		retval+="      "+XMLHandler.addTagValue("arg_from_previous", arg_from_previous);
		retval+="      "+XMLHandler.addTagValue("set_logfile",       set_logfile);
		retval+="      "+XMLHandler.addTagValue("logfile",           logfile);
		retval+="      "+XMLHandler.addTagValue("logext",            logext);
		retval+="      "+XMLHandler.addTagValue("add_date",          add_date);
		retval+="      "+XMLHandler.addTagValue("add_time",          add_time);
		retval+="      "+XMLHandler.addTagValue("loglevel",          LogWriter.getLogLevelDesc(loglevel));

		if (arguments!=null)
		for (int i=0;i<arguments.length;i++)
		{
			retval+="      "+XMLHandler.addTagValue("argument"+i, arguments[i]);
		}

		return retval;
	}

	public void loadXML(Node entrynode, ArrayList databases, RepositoryDirectory directory_tree)
		throws KettleXMLException
	{
		try 
		{
			super.loadXML(entrynode, databases);
			
			setFileName( XMLHandler.getTagValue(entrynode, "filename") );
			setTransname( XMLHandler.getTagValue(entrynode, "transname") );
			arg_from_previous = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "arg_from_previous") );
			set_logfile = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "set_logfile") );
			add_date = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "add_date") );
			add_time = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "add_time") );
			logfile = XMLHandler.getTagValue(entrynode, "logfile");
			logext = XMLHandler.getTagValue(entrynode, "logext");
			loglevel = LogWriter.getLogLevel( XMLHandler.getTagValue(entrynode, "loglevel"));

			// The directory...
		    directory = directory_tree.findDirectory( XMLHandler.getTagValue(entrynode, "directory") );

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
	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
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
			arg_from_previous = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_from_previous");
	
			set_logfile       = rep.getJobEntryAttributeBoolean(id_jobentry, "set_logfile");
			add_date          = rep.getJobEntryAttributeBoolean(id_jobentry, "add_date");
			add_time          = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_time");
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
			rep.saveJobEntryAttribute(id_job, getID(), "arg_from_previous", arg_from_previous);
			rep.saveJobEntryAttribute(id_job, getID(), "set_logfile", set_logfile);
			rep.saveJobEntryAttribute(id_job, getID(), "add_date", add_date);
			rep.saveJobEntryAttribute(id_job, getID(), "add_time", add_time);
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
		arg_from_previous=false;
		add_date=false;
		add_time=false;
		logfile=null;
		logext=null;
		set_logfile=false;
	}

	
	/**
	 * Execute this job entry and return the result.
	 * In this case it means, just set the result boolean in the Result class.
	 * @param prev_result The result of the previous execution
	 * @return The Result of the execution.
	 */
	public Result execute(Result prev_result, int nr, Repository rep, Job parentJob)
	{
		LogWriter log       = LogWriter.getInstance();
		Result result = prev_result;
		result.setEntryNr( nr );

		LogWriter logwriter = log;
		if (set_logfile) logwriter = LogWriter.getInstance(getLogFilename(), true, loglevel);
		
		log.logDetailed(toString(), "Starting transformation...(file="+getFileName()+", name="+getName()+"), repinfo="+getDescription());
		Trans trans = new Trans(logwriter, getFileName(), getTransname(), arguments);
		
		// Set the result rows for the next one...
		trans.getTransMeta().setSourceRows(prev_result.rows);
		
		// Open the transformation...
		// Default directory for now...
		
		log.logBasic(toString(), "Opening transformation: ["+getTransname()+"] in directory ["+directory.getPath()+"]");
		
		try
		{
			trans.open(rep, getTransname(), directory.getPath(), getFileName());
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
		
		if (set_logfile) logwriter.close();
		
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
