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

package org.pentaho.di.job.entries.mssqlbulkload;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.provider.local.LocalFile;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.w3c.dom.Node;

/**
 * This defines a MySQL job entry.
 *
 * @author Samatar Hassan
 * @since Jan-2007
 */
public class JobEntryMssqlBulkLoad extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String schemaname;
	private String tablename;
	private String filename;
	private String separator;
	private String lineterminated;
	private String takelines;
	private String orderby;
	private boolean addfiletoresult;

	private DatabaseMeta connection;

	public JobEntryMssqlBulkLoad(String n)
	{
		super(n, "");
		tablename=null;
		schemaname=null;
		filename=null;
		separator=null;
		lineterminated=null;
		takelines = "0";
		orderby=null;
		connection=null;
		addfiletoresult = false;
		setID(-1L);
		setJobEntryType(JobEntryType.MSSQL_BULK_LOAD);
	}

	public JobEntryMssqlBulkLoad()
	{
		this("");
	}

	public JobEntryMssqlBulkLoad(JobEntryBase jeb)
	{
		super(jeb);
	}

	public Object clone()
	{
		JobEntryMssqlBulkLoad je = (JobEntryMssqlBulkLoad) super.clone();
		return je;
	}

	public String getXML()
	{
		StringBuffer retval = new StringBuffer(200);

		retval.append(super.getXML());
		retval.append("      ").append(XMLHandler.addTagValue("schemaname",      schemaname));
		retval.append("      ").append(XMLHandler.addTagValue("tablename",       tablename));
		retval.append("      ").append(XMLHandler.addTagValue("filename",        filename));
		retval.append("      ").append(XMLHandler.addTagValue("separator",       separator));
		retval.append("      ").append(XMLHandler.addTagValue("lineterminated",  lineterminated));
		retval.append("      ").append(XMLHandler.addTagValue("takelines",     takelines));
		retval.append("      ").append(XMLHandler.addTagValue("orderby",    orderby));		
		retval.append("      ").append(XMLHandler.addTagValue("addfiletoresult",  addfiletoresult));
		retval.append("      ").append(XMLHandler.addTagValue("connection",      connection==null?null:connection.getName()));

		return retval.toString();
	}

	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			schemaname  = XMLHandler.getTagValue(entrynode, "schemaname");
			tablename   = XMLHandler.getTagValue(entrynode, "tablename");
			filename    = XMLHandler.getTagValue(entrynode, "filename");
			separator   = XMLHandler.getTagValue(entrynode, "separator");

			lineterminated  = XMLHandler.getTagValue(entrynode, "lineterminated");
			takelines     = XMLHandler.getTagValue(entrynode, "takelines");
			orderby    = XMLHandler.getTagValue(entrynode, "orderby");

			String dbname   = XMLHandler.getTagValue(entrynode, "connection");
			addfiletoresult = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "addfiletoresult"));
			connection      = DatabaseMeta.findDatabase(databases, dbname);
		}
		catch(KettleException e)
		{
			throw new KettleXMLException("Unable to load job entry of type 'Mysql bulk load' from XML node", e);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases, slaveServers);
			schemaname      =      rep.getJobEntryAttributeString(id_jobentry,  "schemaname");
			tablename       =      rep.getJobEntryAttributeString(id_jobentry,  "tablename");
			filename        =      rep.getJobEntryAttributeString(id_jobentry,  "filename");
			separator       =      rep.getJobEntryAttributeString(id_jobentry,  "separator");
			lineterminated  =      rep.getJobEntryAttributeString(id_jobentry,  "lineterminated");
			takelines     =      rep.getJobEntryAttributeString(id_jobentry,  "takelines");
			orderby    =      rep.getJobEntryAttributeString(id_jobentry,  "orderby");

			addfiletoresult=rep.getJobEntryAttributeBoolean(id_jobentry, "addfiletoresult");

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
			throw new KettleException("Unable to load job entry of type 'Mysql bulk load' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}

	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			rep.saveJobEntryAttribute(id_job, getID(), "schemaname",     schemaname);
			rep.saveJobEntryAttribute(id_job, getID(), "tablename",      tablename);
			rep.saveJobEntryAttribute(id_job, getID(), "filename",       filename);
			rep.saveJobEntryAttribute(id_job, getID(), "separator",      separator);
			rep.saveJobEntryAttribute(id_job, getID(), "lineterminated", lineterminated);
			rep.saveJobEntryAttribute(id_job, getID(), "takelines",    takelines);
			rep.saveJobEntryAttribute(id_job, getID(), "orderby",   orderby);
			rep.saveJobEntryAttribute(id_job, getID(), "addfiletoresult", addfiletoresult);

			if (connection!=null) rep.saveJobEntryAttribute(id_job, getID(), "connection", connection.getName());
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'Mysql Bulk Load' to the repository for id_job="+id_job, dbe);
		}
	}

	public void setTablename(String tablename)
	{
		this.tablename = tablename;
	}

	public void setSchemaname(String schemaname)
	{
		this.schemaname = schemaname;
	}

	public String getSchemaname()
	{
		return schemaname;
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
		String TakeFirstNbrLines=null;
		String LineTerminatedby=null;
		String FieldTerminatedby=null;

		LogWriter log = LogWriter.getInstance();

		Result result = previousResult;
		result.setResult(false);

		String vfsFilename = environmentSubstitute(filename);

		// Let's check the filename ...
		if (!Const.isEmpty(vfsFilename))
		{
			try
			{
				// User has specified a file, We can continue ...
				//
				
				// This is running over VFS but we need a normal file.
				// As such, we're going to verify that it's a local file...
				// We're also going to convert VFS FileObject to File
				//
				FileObject fileObject = KettleVFS.getFileObject(vfsFilename);
				if (!(fileObject instanceof LocalFile)) {
					// MSSQL BUKL INSERT can only use local files, so that's what we limit ourselves to.
					//
					throw new KettleException("Only local files are supported at this time, file ["+vfsFilename+"] is not a local file.");
				}
				
				// Convert it to a regular platform specific file name
				//
				String realFilename = KettleVFS.getFilename(fileObject);
				
				// Here we go... back to the regular scheduled program...
				//
				File file = new File(realFilename);
				if (file.exists() && file.canRead())
				{
					// User has specified an existing file, We can continue ...
					log.logDetailed(toString(), "File ["+realFilename+"] exists.");
	
					if (connection!=null)
					{
						// User has specified a connection, We can continue ...
						Database db = new Database(connection);
						
						if(db.getDatabaseMeta().getDatabaseType()!=DatabaseMeta.TYPE_DATABASE_MSSQL)
							{
								log.logError(toString(),Messages.getString("JobMssqlBulkLoad.Error.DbNotMSSQL",connection.getDatabaseName()));
								return result;
							}
						db.shareVariablesWith(this);
						try
						{
							db.connect();
							// Get schemaname
							String realSchemaname = environmentSubstitute(schemaname);
							// Get tablename
							String realTablename = environmentSubstitute(tablename);
	
							if (db.checkTableExists(realTablename))
							{
								// The table existe, We can continue ...
								log.logDetailed(toString(), "Table ["+realTablename+"] exists.");
	
								// Add schemaname (Most the time Schemaname.Tablename)
								if (schemaname !=null)
								{
									realTablename= realSchemaname + "." + realTablename;
								}
	
								// Take the X first rows
								String nblinesTake=getRealTakelines();
								if(Const.toInt(nblinesTake,0)>0)
									TakeFirstNbrLines="FIRSTROW="+nblinesTake;
								
								// FIELDTERMINATOR
								String Fieldterminator=getRealSeparator();
								if(!Const.isEmpty(Fieldterminator))	
									FieldTerminatedby="FIELDTERMINATOR='"+Fieldterminator+"'";
								
								// ROWTERMINATOR
								String Rowterminator=getRealLineterminated();
								if(!Const.isEmpty(Rowterminator))	
									LineTerminatedby="ROWTERMINATOR='"+Rowterminator+"'";
						
								// Build BULK Command
								String SQLBULKLOAD="BULK INSERT " + realTablename + " FROM " + "'"+realFilename.replace('\\', '/')+"'" ;
								SQLBULKLOAD=SQLBULKLOAD+" WITH (";
								if(TakeFirstNbrLines!=null) SQLBULKLOAD=SQLBULKLOAD+TakeFirstNbrLines+",";
								if(FieldTerminatedby!=null)  SQLBULKLOAD=SQLBULKLOAD+FieldTerminatedby;//" INTO TABLE " + realTablename + " " + FieldTerminatedby + " " + LineTerminatedby + " " + IgnoreNbrLignes + " " +  ListOfColumn  + ";";
								if(LineTerminatedby!=null) SQLBULKLOAD=SQLBULKLOAD+","+LineTerminatedby;
								SQLBULKLOAD=SQLBULKLOAD+")";
								
								try
								{
									// Run the SQL
									db.execStatements(SQLBULKLOAD);
	
									// Everything is OK...we can deconnect now
									db.disconnect();
									
									if (isAddFileToResult())
									{
										// Add filename to output files
					                	ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject(realFilename), parentJob.getName(), toString());
					                    result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
									}
									
									result.setResult(true);
								}
								catch(KettleDatabaseException je)
								{
									db.disconnect();
									result.setNrErrors(1);
									log.logError(toString(), "An error occurred executing this job entry : "+je.getMessage());
								}
								catch (IOException e)
								{
					       			log.logError(toString(), "An error occurred executing this job entry : " + e.getMessage());
									result.setNrErrors(1);
								}
							}
							else
							{
								// Of course, the table should have been created already before the bulk load operation
								db.disconnect();
								result.setNrErrors(1);
								log.logDetailed(toString(), "Table ["+realTablename+"] doesn't exist!");
							}
						}
						catch(KettleDatabaseException dbe)
						{
							db.disconnect();
							result.setNrErrors(1);
							log.logError(toString(), "An error occurred executing this entry: "+dbe.getMessage());
						}
					}
					else
					{
						// No database connection is defined
						result.setNrErrors(1);
						log.logError(toString(),  Messages.getString("JobMssqlBulkLoad.Nodatabase.Label"));
					}
				}
				else
				{
					// the file doesn't exist
					result.setNrErrors(1);
					log.logError(toString(), "File ["+realFilename+"] doesn't exist!");
				}
			}
			catch(Exception e)
			{
				// An unexpected error occurred
				result.setNrErrors(1);
				log.logError(toString(), Messages.getString("JobMssqlBulkLoad.UnexpectedError.Label"), e);
			}
		}
		else
		{
			// No file was specified
			result.setNrErrors(1);
			log.logError(toString(), Messages.getString("JobMssqlBulkLoad.Nofilename.Label"));
		}
		return result;
	}

	public DatabaseMeta[] getUsedDatabaseConnections()
	{
		return new DatabaseMeta[] { connection, };
	}


	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public String getFilename()
	{
		return filename;
	}

	public void setSeparator(String separator)
	{
		this.separator = separator;
	}

	public void setLineterminated(String lineterminated)
	{
		this.lineterminated = lineterminated;
	}


	public String getSeparator()
	{
		return separator;
	}

	public String getLineterminated()
	{
		return lineterminated;
	}

	public String getRealLineterminated()
	{
		return environmentSubstitute(getLineterminated());
	}

	public String getRealSeparator()
	{
		return environmentSubstitute(getSeparator());
	}

	public void setTakelines(String takelines)
	{
		this.takelines = takelines;
	}

	public String getTakelines()
	{
		return takelines;
	}

	public String getRealTakelines()
	{
		return environmentSubstitute(getTakelines());
	}

	public void setOrderBy(String orderby)
	{
		this.orderby = orderby;
	}

	public String getOrderBy()
	{
		return orderby;
	}

	public String getRealOrderBy()
	{
		return environmentSubstitute(getOrderBy());
	}
	
	public void setAddFileToResult(boolean addfiletoresultin)
	{
		this.addfiletoresult = addfiletoresultin;
	}

	public boolean isAddFileToResult()
	{
		return addfiletoresult;
	}

	private String MysqlString(String listcolumns)
	{
		/*
		 * Handle forbiden char like '
		 */
		String returnString="";
		String[] split = listcolumns.split(",");

		for (int i=0;i<split.length;i++)
		{
			if (returnString.equals(""))
				returnString =  "`" + Const.trim(split[i]) + "`";
			else
				returnString = returnString +  ", `" + Const.trim(split[i]) + "`";
		}

		return returnString;
	}

  public List<ResourceReference> getResourceDependencies(JobMeta jobMeta) {
    List<ResourceReference> references = super.getResourceDependencies(jobMeta);
    ResourceReference reference = null;
    if (connection != null) {
      reference = new ResourceReference(this);
      references.add(reference);
      reference.getEntries().add( new ResourceEntry(connection.getHostname(), ResourceType.SERVER));
      reference.getEntries().add( new ResourceEntry(connection.getDatabaseName(), ResourceType.DATABASENAME));
    }
    if ( filename != null) {
      String realFilename = getRealFilename();
      if (reference == null) {
        reference = new ResourceReference(this);
        references.add(reference);
      }
      reference.getEntries().add( new ResourceEntry(realFilename, ResourceType.FILE));
    }
    return references;
  }

  @Override
  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
  {
    ValidatorContext ctx = new ValidatorContext();
    putVariableSpace(ctx, getVariables());
    putValidators(ctx, notBlankValidator(), fileExistsValidator());
    andValidator().validate(this, "filename", remarks, ctx);//$NON-NLS-1$

    andValidator().validate(this, "tablename", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
  }

}