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
 
package org.pentaho.di.job.entries.tableexists;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.StringUtil;




/**
 * This defines an SQL job entry.
 * 
 * @author Matt
 * @since 05-11-2003
 *
 */
public class JobEntryTableExists extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String tablename;
	private DatabaseMeta connection;

	public JobEntryTableExists(String n)
	{
	    super(n, "");
		tablename=null;
		connection=null;
		setID(-1L);
		setType(JobEntryInterface.TYPE_JOBENTRY_TABLE_EXISTS);
	}

	public JobEntryTableExists()
	{
		this("");
	}

	public JobEntryTableExists(JobEntryBase jeb)
	{
		super(jeb);
	}
    
    public Object clone()
    {
        JobEntryTableExists je = (JobEntryTableExists) super.clone();
        return je;
    }

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(200);
		
		retval.append(super.getXML());
		
		retval.append("      ").append(XMLHandler.addTagValue("tablename",  tablename));
		retval.append("      ").append(XMLHandler.addTagValue("connection", connection==null?null:connection.getName()));
		
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, ArrayList databases, Repository rep) throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			
			tablename     = XMLHandler.getTagValue(entrynode, "tablename");
			String dbname = XMLHandler.getTagValue(entrynode, "connection");
			connection    = DatabaseMeta.findDatabase(databases, dbname);
		}
		catch(KettleException e)
		{
			throw new KettleXMLException("Unable to load job entry of type 'table exists' from XML node", e);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			
			tablename  = rep.getJobEntryAttributeString(id_jobentry, "tablename");
			long id_db = rep.getJobEntryAttributeInteger(id_jobentry, "id_database");
			if (id_db>0)
			{
				connection = DatabaseMeta.findDatabase(databases, id_db);
			}
			else
			{
				// This is were we end up in normally, the previous lines are for backward compatibility.
				connection = DatabaseMeta.findDatabase(databases, rep.getJobEntryAttributeString(id_jobentry, "connection"));
			}

		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'table exists' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}
	
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			rep.saveJobEntryAttribute(id_job, getID(), "tablename", tablename);
			if (connection!=null) rep.saveJobEntryAttribute(id_job, getID(), "connection", connection.getName());
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'table exists' to the repository for id_job="+id_job, dbe);
		}
	}

	
	public void setTablename(String tablename)
	{
		this.tablename = tablename;
	}
	
	public String getTablename()
	{
		return tablename;
	}
	
	public void setDatabase(DatabaseMeta database)
	{
		this.connection = database;
	}
	
	public DatabaseMeta getDatabase()
	{
		return connection;
	}
	
	public boolean evaluates()
	{
		return true;
	}

	public boolean isUnconditional()
	{
		return false;
	}

	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();

		Result result = previousResult;
		result.setResult(false);
		
		if (connection!=null)
		{
			Database db = new Database(connection);
			try
			{
				db.connect();
                String realTablename = StringUtil.environmentSubstitute(tablename);
				if (db.checkTableExists(realTablename))
				{
					log.logDetailed(toString(), "Table ["+realTablename+"] exists.");
					result.setResult(true);
				}
				else
				{
					log.logDetailed(toString(), "Table ["+realTablename+"] doesn't exist!");
				}
				db.disconnect();
			}
			catch(KettleDatabaseException dbe)
			{
				result.setNrErrors(1);
				log.logError(toString(), "An error occurred executing this step: "+dbe.getMessage());
			}
		}
		else
		{
			result.setNrErrors(1);
			log.logError(toString(), "No database connection is defined.");
		}
		
		return result;
	}

    public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) {
        return new JobEntryTableExistsDialog(shell,this,jobMeta);
    }
    
    public DatabaseMeta[] getUsedDatabaseConnections()
    {
        return new DatabaseMeta[] { connection, };
    }
}