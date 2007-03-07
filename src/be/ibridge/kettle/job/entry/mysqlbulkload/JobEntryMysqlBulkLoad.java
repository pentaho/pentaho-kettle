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
 
package be.ibridge.kettle.job.entry.mysqlbulkload;
import java.io.File;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.JobEntryBase;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;


/**
 * This defines an SQL job entry.
 * 
 * @author Matt
 * @since 05-11-2003
 *
 */
public class JobEntryMysqlBulkLoad extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String schemaname;
	private String tablename;
	private String filename;
	private String separator;
	private String ignorelines;
	private boolean replacedata;
	private String listattribut;
	private boolean localinfile;
	public int prorityvalue;

	private DatabaseMeta connection;

	public JobEntryMysqlBulkLoad(String n)
	{
		super(n, "");
		tablename=null;
		schemaname=null;
		filename=null;
		separator=null;
		replacedata=true;
		ignorelines = "0";
		listattribut=null;
		localinfile=true;
		connection=null;
		setID(-1L);
		setType(JobEntryInterface.TYPE_JOBENTRY_MYSQL_BULK_LOAD);
	}

	public JobEntryMysqlBulkLoad()
	{
		this("");
	}

	public JobEntryMysqlBulkLoad(JobEntryBase jeb)
	{
		super(jeb);
	}
    
	public Object clone()
	{
		JobEntryMysqlBulkLoad je = (JobEntryMysqlBulkLoad) super.clone();
		return je;
	}

	public String getXML()
	{
		StringBuffer retval = new StringBuffer(200);
		
		retval.append(super.getXML());
		retval.append("      ").append(XMLHandler.addTagValue("schemaname",  schemaname));
		retval.append("      ").append(XMLHandler.addTagValue("tablename",  tablename));
		retval.append("      ").append(XMLHandler.addTagValue("filename",  filename));
		retval.append("      ").append(XMLHandler.addTagValue("separator",  separator));
		retval.append("      ").append(XMLHandler.addTagValue("replacedata",  replacedata));
		retval.append("      ").append(XMLHandler.addTagValue("ignorelines",  ignorelines));
		retval.append("      ").append(XMLHandler.addTagValue("listattribut",  listattribut));

		retval.append("      ").append(XMLHandler.addTagValue("localinfile",  localinfile));
		retval.append("      ").append(XMLHandler.addTagValue("prorityvalue",  prorityvalue));
		
		retval.append("      ").append(XMLHandler.addTagValue("connection", connection==null?null:connection.getName()));
		
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, ArrayList databases, Repository rep) throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			schemaname     = XMLHandler.getTagValue(entrynode, "schemaname");
			tablename     = XMLHandler.getTagValue(entrynode, "tablename");
			filename     = XMLHandler.getTagValue(entrynode, "filename");
			separator     = XMLHandler.getTagValue(entrynode, "separator");
			replacedata = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "replacedata"));
			ignorelines     = XMLHandler.getTagValue(entrynode, "ignorelines");
			listattribut     = XMLHandler.getTagValue(entrynode, "listattribut");

			localinfile = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "localinfile"));
			
			prorityvalue     = Const.toInt(XMLHandler.getTagValue(entrynode, "prorityvalue"), -1);

			String dbname = XMLHandler.getTagValue(entrynode, "connection");
			connection    = DatabaseMeta.findDatabase(databases, dbname);
		}
		catch(KettleException e)
		{
			throw new KettleXMLException("Unable to load job entry of type 'Mysql bulk load' from XML node", e);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			schemaname  = rep.getJobEntryAttributeString(id_jobentry, "schemaname");
			tablename  = rep.getJobEntryAttributeString(id_jobentry, "tablename");
			filename  = rep.getJobEntryAttributeString(id_jobentry, "filename");
			separator  = rep.getJobEntryAttributeString(id_jobentry, "separator");
			replacedata = rep.getJobEntryAttributeBoolean(id_jobentry, "replacedata");
			ignorelines  = rep.getJobEntryAttributeString(id_jobentry, "ignorelines");
			listattribut  = rep.getJobEntryAttributeString(id_jobentry, "listattribut");

			localinfile=rep.getJobEntryAttributeBoolean(id_jobentry, "localinfile");

			prorityvalue=Const.toInt(rep.getJobEntryAttributeString(id_jobentry, "prorityvalue"),-1);
			
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
			rep.saveJobEntryAttribute(id_job, getID(), "schemaname", schemaname);
			rep.saveJobEntryAttribute(id_job, getID(), "tablename", tablename);
			rep.saveJobEntryAttribute(id_job, getID(), "filename", filename);
			rep.saveJobEntryAttribute(id_job, getID(), "separator", separator);
			rep.saveJobEntryAttribute(id_job, getID(), "replacedata", replacedata);
			rep.saveJobEntryAttribute(id_job, getID(), "ignorelines", ignorelines);
			rep.saveJobEntryAttribute(id_job, getID(), "listattribut", listattribut);	
	
			rep.saveJobEntryAttribute(id_job, getID(), "localinfile", localinfile);
			
			rep.saveJobEntryAttribute(id_job, getID(), "prorityvalue", prorityvalue);	




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

	public Result execute(Result prev_result, int nr, Repository rep, Job parentJob)
	{
		String ReplaceIgnore;
		String IgnoreNbrLignes;
		String ListOfColumn="";
		String LocalExec="";
		String PriorityText="";

		LogWriter log = LogWriter.getInstance();
		
		Result result = new Result(nr);
		result.setResult(false);

		// Let's check  the filename ...
		if (filename!=null)
		{
			// User has specified a file, We can continue ...
			String realFilename = getRealFilename(); 
			File file = new File(realFilename);
			if ((file.exists() && file.canRead()) ||  isLocalInfile()==false)
			{
				// User has specified an existing file, We can continue ...
				log.logDetailed(toString(), "File ["+realFilename+"] exists.");


				if (connection!=null)
				{
					// User has specified a connection, We can continue ...
					Database db = new Database(connection);
					try
					{
						db.connect();
						// Get schemaname
						String realSchemaname = StringUtil.environmentSubstitute(schemaname);
						// Get tablename
						String realTablename = StringUtil.environmentSubstitute(tablename);

						if (db.checkTableExists(realTablename))
						{
							// The table existe, We can continue ...
							log.logDetailed(toString(), "Table ["+realTablename+"] exists.");

							// Add schemaname (Most the time Schemaname.Tablename) 
							if (schemaname !=null)
								realTablename= realSchemaname + "." + realTablename;


							// Set the REPLACE or IGNORE 
							if (isReplacedata())
								ReplaceIgnore="REPLACE";
							else
								ReplaceIgnore="IGNORE";

						
							// Set the IGNORE LINES
							if (Const.toInt(getRealIgnorelines(),0)>0)
								IgnoreNbrLignes = " IGNORE " + getRealIgnorelines() + " LINES ";
							else
								IgnoreNbrLignes =" ";

							// Set list of Column 
							if (getRealListattribut()!= null)
							{
								ListOfColumn="(" + getRealListattribut() + ")";	
								
							}
							

							// Local File execution
							if (isLocalInfile())
							{
								LocalExec = " LOCAL ";

							}
						

							// Prority
							if (prorityvalue == 0)
							{
								// NORMAL
								PriorityText ="";
							}
							else if (prorityvalue == 1)
							{
								//LOW
								PriorityText = " LOW_PRIORITY ";

							}
							else
							{
								//CONCURRENT
								PriorityText = " CONCURRENT ";
							}






							// Let's built Bulk Load String
							String SQLBULKLOAD="LOAD DATA " + PriorityText + " " + LocalExec + " INFILE '" + realFilename + 	"' " + ReplaceIgnore + 
								" INTO TABLE " + realTablename + " FIELDS TERMINATED BY  '" + getRealSeparator() + "' " + IgnoreNbrLignes + " " + ListOfColumn + ";";


							try
							{
								// Run the SQL
								db.execStatements(SQLBULKLOAD);


								// Everything is OK...we can deconnect now
								db.disconnect();
								result.setResult(true);

							
							}
							catch(KettleDatabaseException je)
							{
								db.disconnect();
								result.setNrErrors(1);
								log.logError(toString(), "An error occurred executing this job entry : "+je.getMessage());
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
					log.logError(toString(),  Messages.getString("JobMysqlBulkLoad.Nodatabase.Label"));
				}


			}
			else
			{
				// the file doesn't exist
				result.setNrErrors(1);
				log.logDetailed(toString(), "File ["+realFilename+"] doesn't exist!");

			}


		}
		else
		{
			// No file was specified
			result.setNrErrors(1);
			log.logError(toString(), Messages.getString("JobMysqlBulkLoad.Nofilename.Label"));
		}

		return result;

	}

	public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) 
	{
		return new JobEntryMysqlBulkLoadDialog(shell,this,jobMeta);
	}
    
	public DatabaseMeta[] getUsedDatabaseConnections()
	{
		return new DatabaseMeta[] { connection, };
	}

	public boolean isReplacedata() 
	{
		return replacedata;
	}

	public void setReplacedata(boolean replacedata) 
	{
		this.replacedata = replacedata;
	}


	public void setLocalInfile(boolean localinfile) 
	{
		this.localinfile = localinfile;
	}


	public boolean isLocalInfile() 
	{
		return localinfile;
	}


	public void setFilename(String filename)
	{
		this.filename = filename;
	}
	
	public String getFilename()
	{
		return filename;
	}
    
	public String getRealFilename()
	{
 
		String RealFile= StringUtil.environmentSubstitute(getFilename());
		return RealFile.replace('\\','/');
	}
	public void setSeparator(String separator)
	{
		this.separator = separator;
	}
	
	public String getSeparator()
	{
		return separator;
	}
    
	public String getRealSeparator()
	{
		return StringUtil.environmentSubstitute(getSeparator());
	}

	public void setIgnorelines(String ignorelines)
	{
		this.ignorelines = ignorelines;
	}
	public String getIgnorelines()
	{
		return ignorelines;
	}
    
	public String getRealIgnorelines()
	{
		return StringUtil.environmentSubstitute(getIgnorelines());
	}



	public void setListattribut(String listattribut)
	{
		this.listattribut = listattribut;
	}
	public String getListattribut()
	{
		return listattribut;
	}
    
	public String getRealListattribut()
	{
		return StringUtil.environmentSubstitute(getListattribut());
	}


	
}