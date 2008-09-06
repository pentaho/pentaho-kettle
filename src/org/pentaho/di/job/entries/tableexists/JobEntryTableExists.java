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

package org.pentaho.di.job.entries.tableexists;

import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.w3c.dom.Node;




/**
 * This defines a table exists job entry.
 *
 * @author Matt
 * @since 05-11-2003
 *
 */
public class JobEntryTableExists extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String tablename;
	private String schemaname;
	private DatabaseMeta connection;

	public JobEntryTableExists(String n)
	{
	    super(n, "");
	    schemaname=null;
		tablename=null;
		connection=null;
		setID(-1L);
		setJobEntryType(JobEntryType.TABLE_EXISTS);
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
		retval.append("      ").append(XMLHandler.addTagValue("schemaname",  schemaname));
		retval.append("      ").append(XMLHandler.addTagValue("connection", connection==null?null:connection.getName()));

		return retval.toString();
	}

	public void loadXML(Node entrynode, List<DatabaseMeta>  databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases, slaveServers);

			tablename     = XMLHandler.getTagValue(entrynode, "tablename");
			schemaname     = XMLHandler.getTagValue(entrynode, "schemaname");
			String dbname = XMLHandler.getTagValue(entrynode, "connection");
			connection    = DatabaseMeta.findDatabase(databases, dbname);
		}
		catch(KettleException e)
		{
			throw new KettleXMLException(Messages.getString("TableExists.Meta.UnableLoadXml"), e);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases, slaveServers);

			tablename  = rep.getJobEntryAttributeString(id_jobentry, "tablename");
			schemaname  = rep.getJobEntryAttributeString(id_jobentry, "schemaname");
			
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
			throw new KettleException(Messages.getString("TableExists.Meta.UnableLoadRep",""+id_jobentry), dbe);
		}
	}

	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);

			rep.saveJobEntryAttribute(id_job, getID(), "tablename", tablename);
			rep.saveJobEntryAttribute(id_job, getID(), "schemaname", schemaname);
			
			if (connection!=null) rep.saveJobEntryAttribute(id_job, getID(), "connection", connection.getName());
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("TableExists.Meta.UnableSaveRep",""+id_job), dbe);
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

	public String getSchemaname()
	{
		return schemaname;
	}
	public void setSchemaname(String schemaname)
	{
		this.schemaname = schemaname;
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
			db.shareVariablesWith(this);
			try
			{
				db.connect();
                String realTablename = environmentSubstitute(tablename);
                String realSchemaname = environmentSubstitute(schemaname);
                if(!Const.isEmpty(realSchemaname))
                {
                	realTablename = db.getDatabaseMeta().getQuotedSchemaTableCombination(realSchemaname, realTablename);
                    if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("TableExists.Log.SchemaTable",realTablename));
                }else
                	realTablename = db.getDatabaseMeta().quoteField(realTablename);
                
				if (db.checkTableExists(realTablename))
				{
					if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("TableExists.Log.TableExists",realTablename));
					result.setResult(true);
				}
				else
				{
					if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("TableExists.Log.TableNotExists",realTablename));
				}
			}
			catch(KettleDatabaseException dbe)
			{
				result.setNrErrors(1);
				log.logError(toString(), Messages.getString("TableExists.Error.RunningJobEntry",dbe.getMessage()));
			}
			finally
			{
				if(db!=null) try{db.disconnect();} catch(Exception e){};
			}
		}
		else
		{
			result.setNrErrors(1);
			log.logError(toString(), Messages.getString("TableExists.Error.NoConnectionDefined"));
		}

		return result;
	}

    public DatabaseMeta[] getUsedDatabaseConnections()
    {
        return new DatabaseMeta[] { connection, };
    }

    public List<ResourceReference> getResourceDependencies(JobMeta jobMeta) {
      List<ResourceReference> references = super.getResourceDependencies(jobMeta);
      if (connection != null) {
        ResourceReference reference = new ResourceReference(this);
        reference.getEntries().add( new ResourceEntry(connection.getHostname(), ResourceType.SERVER));
        reference.getEntries().add( new ResourceEntry(connection.getDatabaseName(), ResourceType.DATABASENAME));
        references.add(reference);
      }
      return references;
    }

    @Override
    public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
    {
      andValidator().validate(this, "tablename", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
    }

}