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
 
package be.ibridge.kettle.job.entry.shell;
import java.io.IOException;
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


/**
 * Shell type of Job Entry.  You can define shell scripts to be executed in a Job.
 * 
 * @author Matt
 * @since 01-10-2003, rewritten on 18-06-2004
 * 
 */

public class JobEntryShell extends JobEntryBase implements Cloneable, JobEntryInterface
{	
	private String  filename;
	public  String  arguments[];
	public  boolean argFromPrevious;

	public  boolean setLogfile;
	public  String  logfile, logext;
	public  boolean addDate, addTime;
	public  int     loglevel;
	
	public  boolean parallel;
	
	public JobEntryShell(String name)
	{
		super(name, "");
		setType(JobEntryInterface.TYPE_JOBENTRY_SHELL);
	}


	public JobEntryShell()
	{
		this("");
		clear();
	}
	
	public JobEntryShell(JobEntryBase jeb)
	{
		super(jeb);
		setType(JobEntryInterface.TYPE_JOBENTRY_SHELL);
	}

	public String getXML()
	{
		String retval ="";
		
		retval+=super.getXML();
		
		retval+="      "+XMLHandler.addTagValue("filename",          filename);
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
				
	public void loadXML(Node entrynode, ArrayList databases)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			setFileName( XMLHandler.getTagValue(entrynode, "filename") );
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
			throw new KettleXMLException("Unable to load shell job entry from XML node", e);
		}
	}
	
	// Load the jobentry from repository
	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			
			setFileName( rep.getJobEntryAttributeString(id_jobentry, "file_name")  );
			argFromPrevious = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_from_previous");
	
			setLogfile = rep.getJobEntryAttributeBoolean(id_jobentry, "set_logfile");
			addDate = rep.getJobEntryAttributeBoolean(id_jobentry, "add_date");
			addTime = rep.getJobEntryAttributeBoolean(id_jobentry, "add_time");
			logfile = rep.getJobEntryAttributeString(id_jobentry, "logfile");
			logext = rep.getJobEntryAttributeString(id_jobentry, "logext");
			loglevel = LogWriter.getLogLevel( rep.getJobEntryAttributeString(id_jobentry, "loglevel") );
	
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
			throw new KettleException("Unable to load job entry of type shell from the repository with id_jobentry="+id_jobentry, dbe);
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
			throw new KettleException("Error save shell job entry attributes.", dbe);
		}
	}
	
	public void clear()
	{
		super.clear();
		
		filename=null;
		arguments=null;
		argFromPrevious=false;
		addDate=false;
		addTime=false;
		logfile=null;
		logext=null;
		setLogfile=false;
	}

	public void setFileName(String n)
	{
		filename=n;
	}
	
	public String getFileName()
	{
		return filename;
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
	
	public Result execute(Result prev_result, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();

		Result result = prev_result;
		result.setEntryNr( nr );
		
		try
		{
			// What's the exact command?
			String cmd[] = null;
			String base[] = null;
			
			if (System.getProperty("os.name").startsWith("Windows"))
			{
				 base = new String[] { getFileName() };
			}
			else
			{ 
				base = new String[] { getFileName() };
			}
			//String env[] = new String[] { };
								
			if (argFromPrevious && prev_result.rows!=null)
			{
				cmd = new String[prev_result.rows.size()+base.length];
				//env = new String[result.rows.size()+1];

				for (int i=0;i<base.length;i++) cmd[i]=base[i];
				//env[0]="ARGS="+result.rows.size();
				for (int i=0;i<prev_result.rows.size();i++) 
				{
					Row r = (Row)prev_result.rows.get(i);
					cmd[i+base.length]=r.toString();
					//env[i+1]="ARG"+i+"=\""+r.toString()+"\"";
				} 
			}
			else
			if (arguments!=null)
			{
				// System.out.println("base == null? "+(base==null)+", args == null ? "+(jobentry.arguments==null));
				cmd = new String[arguments.length+base.length];
				// env = new String[jobentry.arguments.length+1];
				for (int i=0;i<base.length;i++) cmd[i]=base[i];
				//if (result.rows!=null) env[0]="ARGS="+result.rows.size();
				//else		           env[0]="ARGS=0";
				for (int i=0;i<arguments.length;i++) 
				{
					cmd[i+base.length]=arguments[i];
					// env[i+1]="ARG"+i+"=\""+jobentry.arguments[i]+"\"";
				} 
			}
			// Launch the script!
			log.logDetailed(toString(), "Passing "+(cmd.length-1)+" arguments to command : ["+cmd[0]+"]");
			Process proc;
			//System.out.println("executing command: ["+cmd[0]+"]");
			proc = java.lang.Runtime.getRuntime().exec(cmd);
			
			proc.waitFor();
			log.logDetailed(toString(), "command ["+cmd[0]+"] has finished");
			
			// What's the exit status?
			result.exitStatus = proc.exitValue();
			if (result.exitStatus!=0) 
			{
				log.logDetailed(toString(), "Exit status of shell ["+getFileName()+"] was "+result.exitStatus);
				result.setNrErrors(1);
			} 
		}
		catch(IOException ioe)
		{
			log.logError(toString(), "Error running shell ["+getFileName()+"] : "+ioe.toString());
			result.setNrErrors(1);
		}
		catch(InterruptedException ie)
		{
			log.logError(toString(), "Shell ["+getFileName()+"] was interupted : "+ie.toString());
			result.setNrErrors(1);
		}
		catch(Exception e)
		{
			log.logError(toString(), "Unexpected error running shell ["+getFileName()+"] : "+e.toString());
			result.setNrErrors(1);
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

	public boolean evaluates()
	{
		return true;
	}

	public boolean isUnconditional()
	{
		return true;
	}
}
