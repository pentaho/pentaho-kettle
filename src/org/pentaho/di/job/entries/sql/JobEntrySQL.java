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

package org.pentaho.di.job.entries.sql;

import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;

import java.util.List;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.InputStream;

import org.pentaho.di.core.vfs.KettleVFS;
import org.apache.commons.vfs.FileObject;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Result;
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

import org.pentaho.di.core.Const;




/**
 * This defines an SQL job entry.
 *
 * @author Matt
 * @since 05-11-2003
 *
 */
public class JobEntrySQL extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String sql;
	private DatabaseMeta connection;
	private boolean useVariableSubstitution = false;
	private boolean sqlfromfile=false;
	private String sqlfilename;

	public JobEntrySQL(String n)
	{
		super(n, "");
		sql=null;
		connection=null;
		setID(-1L);
		setJobEntryType(JobEntryType.SQL);
	}

	public JobEntrySQL()
	{
		this("");
	}

	public JobEntrySQL(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntrySQL je = (JobEntrySQL) super.clone();
        return je;
    }

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(200);

		retval.append(super.getXML());

		retval.append("      ").append(XMLHandler.addTagValue("sql",      sql));
		retval.append("      ").append(XMLHandler.addTagValue("useVariableSubstitution", useVariableSubstitution ? "T" : "F"));
		retval.append("      ").append(XMLHandler.addTagValue("sqlfromfile", sqlfromfile ? "T" : "F"));
		retval.append("      ").append(XMLHandler.addTagValue("sqlfilename",      sqlfilename));
		
		
		retval.append("      ").append(XMLHandler.addTagValue("connection", connection==null?null:connection.getName()));

		return retval.toString();
	}

	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			sql           = XMLHandler.getTagValue(entrynode, "sql");
			String dbname = XMLHandler.getTagValue(entrynode, "connection");
			String sSubs  = XMLHandler.getTagValue(entrynode, "useVariableSubstitution");

			if (sSubs != null && sSubs.equalsIgnoreCase("T"))
				useVariableSubstitution = true;
			connection    = DatabaseMeta.findDatabase(databases, dbname);
			
			
			String ssql  = XMLHandler.getTagValue(entrynode, "sqlfromfile");
			if (ssql != null && ssql.equalsIgnoreCase("T"))
				sqlfromfile = true;
			
			sqlfilename    = XMLHandler.getTagValue(entrynode, "sqlfilename");
			

		}
		catch(KettleException e)
		{
			throw new KettleXMLException("Unable to load job entry of type 'sql' from XML node", e);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases, slaveServers);

			sql = rep.getJobEntryAttributeString(id_jobentry, "sql");
			String sSubs = rep.getJobEntryAttributeString(id_jobentry, "useVariableSubstitution");
			if (sSubs != null && sSubs.equalsIgnoreCase("T"))
				useVariableSubstitution = true;
			
			String ssql = rep.getJobEntryAttributeString(id_jobentry, "sqlfromfile");
			if (ssql != null && ssql.equalsIgnoreCase("T"))
				sqlfromfile = true;
			
			sqlfilename = rep.getJobEntryAttributeString(id_jobentry, "sqlfilename");
			
			
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
			throw new KettleException("Unable to load job entry of type 'sql' from the repository with id_jobentry="+id_jobentry, dbe);
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

			if (connection!=null) rep.saveJobEntryAttribute(id_job, getID(), "connection", connection.getName());
			rep.saveJobEntryAttribute(id_job, getID(), "sql", sql);
			rep.saveJobEntryAttribute(id_job, getID(), "useVariableSubstitution", useVariableSubstitution ? "T" : "F" );
			rep.saveJobEntryAttribute(id_job, getID(), "sqlfromfile", sqlfromfile ? "T" : "F" );
			rep.saveJobEntryAttribute(id_job, getID(), "sqlfilename", sqlfilename);
	
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'sql' to the repository for id_job="+id_job, dbe);
		}
	}

	public void setSQL(String sql)
	{
		this.sql = sql;
	}

	public String getSQL()
	{
		return sql;
	}
	
	 public String getSQLFilename()
	 {
	    return sqlfilename;
	 }

	 public void setSQLFilename(String sqlfilename)
	{
		this.sqlfilename = sqlfilename;
	}
	 
	public boolean getUseVariableSubstitution()
	{
		return useVariableSubstitution;
	}

	public void setUseVariableSubstitution(boolean subs)
	{
		useVariableSubstitution = subs;
	}
	
	public void setSQLFromFile(boolean sqlfromfilein)
	{
		sqlfromfile = sqlfromfilein;
	}
	public boolean getSQLFromFile()
	{
		return sqlfromfile;
	}
	

	public void setDatabase(DatabaseMeta database)
	{
		this.connection = database;
	}

	public DatabaseMeta getDatabase()
	{
		return connection;
	}

	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();

		Result result = previousResult;

		if (connection!=null)
		{
			Database db = new Database(connection);
			FileObject SQLfile=null;
			db.shareVariablesWith(this);
			try
			{
				db.connect();
				if(sqlfromfile)
				{
					if(sqlfilename==null)
						throw new KettleDatabaseException(Messages.getString("JobSQL.NoSQLFileSpecified"));
					
					try{
						String realfilename=environmentSubstitute(sqlfilename);
						SQLfile=KettleVFS.getFileObject(realfilename, this);
						if(!SQLfile.exists()) 
						{
							log.logError(toString(),Messages.getString("JobSQL.SQLFileNotExist",realfilename));
							throw new KettleDatabaseException(Messages.getString("JobSQL.SQLFileNotExist",realfilename));
						}
						if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("JobSQL.SQLFileExists",realfilename));
						
						InputStream IS = KettleVFS.getInputStream(SQLfile);
						try {
  						InputStreamReader BIS = new InputStreamReader(new BufferedInputStream(IS, 500));
  						
  						StringBuffer lineStringBuffer = new StringBuffer(256);
  						lineStringBuffer.setLength(0);
  						
  						BufferedReader buff = new BufferedReader(BIS);
  						String sLine = null;
  						String SFullLine=Const.CR;;
  
  						while((sLine=buff.readLine())!=null) 
  						{
  							if(Const.isEmpty(sLine))
  							{
  								SFullLine= SFullLine +  Const.CR;	
  							}
  							else
  							{
  								SFullLine=SFullLine+  Const.CR + sLine;
  							}
  						}
  						if(!Const.isEmpty(SFullLine))
  						{
  							if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("JobSQL.Log.SQlStatement",SFullLine));
  							db.execStatements(SFullLine);
  						}
            } finally {
              IS.close();
            }
					}catch (Exception e)
					{
						throw new KettleDatabaseException(Messages.getString("JobSQL.ErrorRunningSQLfromFile"),e);
					}
					
				}else
				{
					String mySQL = null;
					if (useVariableSubstitution)
						mySQL = environmentSubstitute(sql);
					else
						mySQL = sql;
					db.execStatements(mySQL);
				}
			}
			catch(KettleDatabaseException je)
			{
				result.setNrErrors(1);
				log.logError(toString(), Messages.getString("JobSQL.ErrorRunJobEntry",je.getMessage()));
			}
			finally
			{
				db.disconnect();
				if(SQLfile!=null) 
				{
					try{
					SQLfile.close();
					}catch(Exception e){}
				}
			}
		}
		else
		{
			result.setNrErrors(1);
			log.logError(toString(), Messages.getString("JobSQL.NoDatabaseConnection"));
		}

		if (result.getNrErrors()==0)
		{
			result.setResult(true);
		}
		else
		{
			result.setResult(false);
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
      andValidator().validate(this, "SQL", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
    }


}