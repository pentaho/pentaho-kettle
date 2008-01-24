/*************************************************************************************** 
 * Copyright (C) 2007 Samatar  All rights reserved. 
 * This software was developed by Samatar and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar.  
 * The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/

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
 * This defines a MSSQL Bulk job entry.
 *
 * @author Samatar Hassan
 * @since Jan-2007
 */
public class JobEntryMssqlBulkLoad extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String schemaname;
	private String tablename;
	private String filename;
	private String datafiletype;
	private String fieldterminator;
	private String lineterminated;
	private String codepage;
	private String specificcodepage;
	private String startfile;
	private String endfile;
	private String orderby;
	private boolean addfiletoresult;

	private DatabaseMeta connection;

	public JobEntryMssqlBulkLoad(String n)
	{
		super(n, "");
		tablename=null;
		schemaname=null;
		filename=null;
		datafiletype="char";
		fieldterminator=null;
		lineterminated=null;
		codepage="RAW";
		specificcodepage=null;
		startfile = null;
		endfile= null;
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
		
		
		
		retval.append("      ").append(XMLHandler.addTagValue("datafiletype", datafiletype));
		retval.append("      ").append(XMLHandler.addTagValue("fieldterminator", fieldterminator));
		retval.append("      ").append(XMLHandler.addTagValue("lineterminated",  lineterminated));
		retval.append("      ").append(XMLHandler.addTagValue("codepage",  codepage));
		retval.append("      ").append(XMLHandler.addTagValue("specificcodepage",  specificcodepage));
		
		
		
		retval.append("      ").append(XMLHandler.addTagValue("startfile",     startfile));
		retval.append("      ").append(XMLHandler.addTagValue("endfile",     endfile));
		
		
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
			
			
			datafiletype   = XMLHandler.getTagValue(entrynode, "datafiletype");
			fieldterminator   = XMLHandler.getTagValue(entrynode, "fieldterminator");

			lineterminated  = XMLHandler.getTagValue(entrynode, "lineterminated");
			codepage  = XMLHandler.getTagValue(entrynode, "codepage");
			specificcodepage  = XMLHandler.getTagValue(entrynode, "specificcodepage");
			
			startfile     = XMLHandler.getTagValue(entrynode, "startfile");
			endfile     = XMLHandler.getTagValue(entrynode, "endfile");
			
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
			
			
			
			datafiletype       =      rep.getJobEntryAttributeString(id_jobentry,  "datafiletype");
			fieldterminator       =      rep.getJobEntryAttributeString(id_jobentry,  "fieldterminator");
			
			lineterminated  =      rep.getJobEntryAttributeString(id_jobentry,  "lineterminated");
			codepage  =      rep.getJobEntryAttributeString(id_jobentry,  "codepage");
			specificcodepage  =      rep.getJobEntryAttributeString(id_jobentry,  "specificcodepage");
			
			startfile     =      rep.getJobEntryAttributeString(id_jobentry,  "startfile");
			endfile     =      rep.getJobEntryAttributeString(id_jobentry,  "endfile");
			
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
			
			
			rep.saveJobEntryAttribute(id_job, getID(), "datafiletype",      datafiletype);
			rep.saveJobEntryAttribute(id_job, getID(), "fieldterminator",      fieldterminator);
			rep.saveJobEntryAttribute(id_job, getID(), "lineterminated", lineterminated);
			rep.saveJobEntryAttribute(id_job, getID(), "codepage", codepage);
			rep.saveJobEntryAttribute(id_job, getID(), "specificcodepage", specificcodepage);
			
			rep.saveJobEntryAttribute(id_job, getID(), "startfile",    startfile);
			rep.saveJobEntryAttribute(id_job, getID(), "endfile",    endfile);
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
		FileObject fileObject = null;
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
				fileObject = KettleVFS.getFileObject(vfsFilename);
				if (!(fileObject instanceof LocalFile)) {
					// MSSQL BUKL INSERT can only use local files, so that's what we limit ourselves to.
					//
					throw new KettleException(Messages.getString("JobMssqlBulkLoad.Error.OnlyLocalFileSupported",vfsFilename));
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
					if (log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobMssqlBulkLoad.FileExists.Label",realFilename));
	
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
								log.logDetailed(toString(), Messages.getString("JobMssqlBulkLoad.TableExists.Label",realTablename));
	
								// Add schemaname (Most the time Schemaname.Tablename)
								if (schemaname !=null)
									realTablename= realSchemaname + "." + realTablename;
	
								// Take the X first rows
								String nblinesTake=getRealTakelines();
								if(Const.toInt(nblinesTake,0)>0)
									TakeFirstNbrLines="FIRSTROW="+nblinesTake;
								
								// FIELDTERMINATOR
								String Fieldterminator=getRealFieldTerminator();
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
	
									// Everything is OK...we can disconnect now
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
									result.setNrErrors(1);
									log.logError(toString(), "An error occurred executing this job entry : "+je.getMessage());
								}
								catch (IOException e)
								{
					       			log.logError(toString(), "An error occurred executing this job entry : " + e.getMessage());
									result.setNrErrors(1);
								}
								finally
								{
									if(db!=null)
									{
										db.disconnect();
										db=null;
									}
								}
							}
							else
							{
								// Of course, the table should have been created already before the bulk load operation
								db.disconnect();
								result.setNrErrors(1);
								log.logDetailed(toString(), Messages.getString("JobMssqlBulkLoad.Error.TableNotExists",realTablename));
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
					log.logError(toString(),Messages.getString("JobMssqlBulkLoad.Error.FileNotExists",realFilename));
				}
			}
			catch(Exception e)
			{
				// An unexpected error occurred
				result.setNrErrors(1);
				log.logError(toString(), Messages.getString("JobMssqlBulkLoad.UnexpectedError.Label"), e);
			}finally{
				try{
				if(fileObject!=null) fileObject.close();
				}catch (Exception e){}
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

	public void setFieldTerminator(String fieldterminator)
	{
		this.fieldterminator = fieldterminator;
	}

	public void setLineterminated(String lineterminated)
	{
		this.lineterminated = lineterminated;
	}

	public void setCodePage(String codepage)
	{
		this.codepage = codepage;
	}
	public String getCodePage()
	{
		return codepage;
	}
	public void setSpecificCodePage(String specificcodepage)
	{
		this.specificcodepage =specificcodepage;
	}
	public String getSpecificCodePage()
	{
		return specificcodepage;
	}
	public String getFieldTerminator()
	{
		return fieldterminator;
	}

	public String getLineterminated()
	{
		return lineterminated;
	}


	public String getDataFileType()
	{
		return datafiletype;
	}
	public void setDataFileType(String datafiletype)
	{
		this.datafiletype = datafiletype;
	}
	public String getRealLineterminated()
	{
		return environmentSubstitute(getLineterminated());
	}

	public String getRealFieldTerminator()
	{
		return environmentSubstitute(getFieldTerminator());
	}

	public void setStartFile(String startfile)
	{
		this.startfile = startfile;
	}

	public String getStartFile()
	{
		return startfile;
	}
	public void setEndFile(String endfile)
	{
		this.endfile = endfile;
	}
	public String getEndFile()
	{
		return endfile;
	}

	public String getRealTakelines()
	{
		return environmentSubstitute(getStartFile());
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