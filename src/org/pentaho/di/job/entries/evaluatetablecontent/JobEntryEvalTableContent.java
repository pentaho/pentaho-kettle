/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar HASSAN.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/


package org.pentaho.di.job.entries.evaluatetablecontent;
import java.util.ArrayList;

import org.w3c.dom.Node;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
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


/**
 * This defines a Table content evaluation job entry
 * 
 * @author Samatar
 * @since 22-07-2008
 *
 */
public class JobEntryEvalTableContent extends JobEntryBase implements Cloneable, JobEntryInterface
{
	public boolean isAddRowsResult;
	
	public boolean isClearResultList;
	
	public boolean isUseVars;  
	
	public boolean iscustomSQL;
	
	public String customSQL;
	
	private DatabaseMeta connection;
	
	public String tablename;

	public String schemaname;
	
	private static final String selectCount="SELECT count(*) FROM ";
	
	public static final String[] successConditionsDesc = new String[] { 
		Messages.getString("JobEntryEvalTableContent.SuccessWhenRowCountEqual.Label"), 
		Messages.getString("JobEntryEvalTableContent.SuccessWhenRowCountDifferent.Label"),
		Messages.getString("JobEntryEvalTableContent.SuccessWhenRowCountSmallerThan.Label"),
		Messages.getString("JobEntryEvalTableContent.SuccessWhenRowCountSmallerOrEqualThan.Label"),
		Messages.getString("JobEntryEvalTableContent.SuccessWhenRowCountGreaterThan.Label"),
		Messages.getString("JobEntryEvalTableContent.SuccessWhenRowCountGreaterOrEqual.Label")
	
	};
	public static final String[] successConditionsCode = new String[] { 
		"rows_count_equal", 
		"rows_count_different",
		"rows_count_smaller",
		"rows_count_smaller_equal",
		"rows_count_greater",
		"rows_count_greater_equal"
	};
	
	public static final int SUCCESS_CONDITION_ROWS_COUNT_EQUAL=0;
	public static final int SUCCESS_CONDITION_ROWS_COUNT_DIFFERENT=1;
	public static final int SUCCESS_CONDITION_ROWS_COUNT_SMALLER=2;
	public static final int SUCCESS_CONDITION_ROWS_COUNT_SMALLER_EQUAL=3;
	public static final int SUCCESS_CONDITION_ROWS_COUNT_GREATER=4;
	public static final int SUCCESS_CONDITION_ROWS_COUNT_GREATER_EQUAL=5;
	
	public String limit;	
	public int successCondition;


	public JobEntryEvalTableContent(String n)
	{
	    super(n, "");
	    limit="0";
	    successCondition=SUCCESS_CONDITION_ROWS_COUNT_GREATER;
	    iscustomSQL=false;
	    isUseVars=false;
	    isAddRowsResult=false;
	    isClearResultList=true;
	    customSQL=null;
	    schemaname=null;
	    tablename=null;
		connection=null;
		setID(-1L);
		setJobEntryType(JobEntryType.EVAL_TABLE_CONTENT);
	}

	public JobEntryEvalTableContent()
	{
		this("");
	}

	public JobEntryEvalTableContent(JobEntryBase jeb)
	{
		super(jeb);
	}
    
    public Object clone()
    {
    	JobEntryEvalTableContent je = (JobEntryEvalTableContent) super.clone();
        return je;
    }
    public int getSuccessCobdition() {
		return successCondition;
	}
	public static int getSuccessConditionByDesc(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < successConditionsDesc.length; i++) {
			if (successConditionsDesc[i].equalsIgnoreCase(tt))
				return i;
		}

		// If this fails, try to match using the code.
		return getSuccessConditionByCode(tt);
	}
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(200);
		
		retval.append(super.getXML());
		retval.append("      ").append(XMLHandler.addTagValue("connection", connection==null?null:connection.getName()));
		retval.append("      ").append(XMLHandler.addTagValue("schemaname", schemaname));
		retval.append("      ").append(XMLHandler.addTagValue("tablename", tablename));
		retval.append("      ").append(XMLHandler.addTagValue("success_condition",getSuccessConditionCode(successCondition)));
		retval.append("      ").append(XMLHandler.addTagValue("limit", limit));
		retval.append("      ").append(XMLHandler.addTagValue("is_custom_sql", iscustomSQL));
		retval.append("      ").append(XMLHandler.addTagValue("is_usevars", isUseVars));
		retval.append("      ").append(XMLHandler.addTagValue("custom_sql", customSQL));
		retval.append("      ").append(XMLHandler.addTagValue("add_rows_result", isAddRowsResult));
		retval.append("      ").append(XMLHandler.addTagValue("clear_result_rows", isClearResultList));
		
		return retval.toString();
	}
	private static String getSuccessConditionCode(int i) {
		if (i < 0 || i >= successConditionsCode.length)
			return successConditionsCode[0];
		return successConditionsCode[i];
	}
	private static int getSucessConditionByCode(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < successConditionsCode.length; i++) {
			if (successConditionsCode[i].equalsIgnoreCase(tt))
				return i;
		}
		return 0;
	}
	public static String getSuccessConditionDesc(int i) {
		if (i < 0 || i >= successConditionsDesc.length)
			return successConditionsDesc[0];
		return successConditionsDesc[i];
	}
	public void loadXML(Node entrynode, List<DatabaseMeta>  databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			String dbname = XMLHandler.getTagValue(entrynode, "connection");
			connection    = DatabaseMeta.findDatabase(databases, dbname);
			schemaname =XMLHandler.getTagValue(entrynode, "schemaname"); 
			tablename =XMLHandler.getTagValue(entrynode, "tablename"); 
			successCondition = getSucessConditionByCode(Const.NVL(XMLHandler.getTagValue(entrynode,	"success_condition"), ""));
			limit = Const.NVL(XMLHandler.getTagValue(entrynode,	"limit"), "0");	
			iscustomSQL = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "is_custom_sql"));
			isUseVars = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "is_usevars"));
			customSQL =XMLHandler.getTagValue(entrynode, "custom_sql"); 
			isAddRowsResult = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "add_rows_result")); 
			isClearResultList = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "clear_result_rows")); 
			
		}
		catch(KettleException e)
		{
			throw new KettleXMLException(Messages.getString("JobEntryEvalTableContent.UnableLoadXML"),e);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers)
	throws KettleException
	{
	try
	{
		super.loadRep(rep, id_jobentry, databases, slaveServers);
			
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

			schemaname = rep.getJobEntryAttributeString(id_jobentry, "schemaname");
			tablename = rep.getJobEntryAttributeString(id_jobentry, "tablename");
			successCondition = getSuccessConditionByCode(Const.NVL(rep.getJobEntryAttributeString(id_jobentry,"success_condition"), ""));
			limit = rep.getJobEntryAttributeString(id_jobentry, "limit");
			iscustomSQL = rep.getJobEntryAttributeBoolean(id_jobentry, "is_custom_sql");
			isUseVars = rep.getJobEntryAttributeBoolean(id_jobentry, "is_usevars");
			isAddRowsResult = rep.getJobEntryAttributeBoolean(id_jobentry, "add_rows_result");
			isClearResultList = rep.getJobEntryAttributeBoolean(id_jobentry, "clear_result_rows");
			
			
			customSQL = rep.getJobEntryAttributeString(id_jobentry, "custom_sql");
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("JobEntryEvalTableContent.UnableLoadRep",""+id_jobentry), dbe);
		}
	}
	private static int getSuccessConditionByCode(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < successConditionsCode.length; i++) {
			if (successConditionsCode[i].equalsIgnoreCase(tt))
				return i;
		}
		return 0;
	}

	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			if (connection!=null) rep.saveJobEntryAttribute(id_job, getID(), "connection", connection.getName());

			rep.saveJobEntryAttribute(id_job, getID(), "schemaname", schemaname);
			rep.saveJobEntryAttribute(id_job, getID(), "tablename", tablename);
			rep.saveJobEntryAttribute(id_job, getID(),"success_condition", getSuccessConditionCode(successCondition));
			rep.saveJobEntryAttribute(id_job, getID(), "limit", limit); 
			rep.saveJobEntryAttribute(id_job, getID(), "custom_sql", customSQL);
			rep.saveJobEntryAttribute(id_job, getID(), "is_custom_sql", iscustomSQL);
			rep.saveJobEntryAttribute(id_job, getID(), "is_usevars", isUseVars);
			rep.saveJobEntryAttribute(id_job, getID(), "add_rows_result", isAddRowsResult);
			rep.saveJobEntryAttribute(id_job, getID(), "clear_result_rows", isClearResultList);			
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("JobEntryEvalTableContent.UnableSaveRep",""+id_job), dbe);
		}
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
		result.setNrErrors(1);
		String countSQLStatement=null;
		long rowsCount=0;
		
		boolean successOK=false;
		
		int nrRowsLimit=Const.toInt(environmentSubstitute(limit),0);
		if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobEntryEvalTableContent.Log.nrRowsLimit",""+nrRowsLimit));
    	

		if (connection!=null)
		{ 
			Database db = new Database(connection);
			db.shareVariablesWith(this);
		
			try
			{
				db.connect();
				
		        if(iscustomSQL)
		        {
		        	String realCustomSQL=customSQL;
		        	if(isUseVars) realCustomSQL=environmentSubstitute(realCustomSQL);
		        	if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobEntryEvalTableContent.Log.EnteredCustomSQL",realCustomSQL));
		        	
		        	if(!Const.isEmpty(realCustomSQL))
		        	{
		        		countSQLStatement=realCustomSQL;
		        	}else
		        		log.logError(toString(), Messages.getString("JobEntryEvalTableContent.Error.NoCustomSQL"));
		        	
		        }else
		        {
			        String realTablename = environmentSubstitute(tablename);                
			        String realSchemaname = environmentSubstitute(schemaname); 
			        
			        if(!Const.isEmpty(realTablename))
		        	{
			        	if(!Const.isEmpty(realSchemaname))
			        	{
			        		countSQLStatement=selectCount + db.getDatabaseMeta().getQuotedSchemaTableCombination(realSchemaname,realTablename);
			        	}else
			        	{
			        		countSQLStatement=selectCount + db.getDatabaseMeta().quoteField(realTablename);
			        	}
		        	}else
		        		log.logError(toString(), Messages.getString("JobEntryEvalTableContent.Error.NoTableName"));
		        }
		       
				if(countSQLStatement!=null)
				{
					if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobEntryEvalTableContent.Log.RunSQLStatement",countSQLStatement));
						
					if(iscustomSQL)
					{
						if(isClearResultList) result.getRows().clear();
						
						List<Object[]> ar =db.getRows(countSQLStatement, 0);
						if(ar!=null)
						{
							rowsCount=ar.size();
							
							// ad rows to result
							RowMetaInterface rowMeta =db.getQueryFields(countSQLStatement, false);
							
							List<RowMetaAndData> rows=new ArrayList<RowMetaAndData>();;
							for(int i=0;i<ar.size();i++)
							{
								rows.add(new RowMetaAndData(rowMeta,ar.get(i)));
							}
							if(isAddRowsResult && iscustomSQL)  if(rows!=null) result.getRows().addAll(rows);	
						}else
						{
							if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobEntryEvalTableContent.Log.customSQLreturnedNothing",countSQLStatement));
						}
						
					}else
					{
						RowMetaAndData row=db.getOneRow(countSQLStatement);
						if(row!=null)
						{
							rowsCount=row.getInteger(0);
						}
					}
					if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobEntryEvalTableContent.Log.NrRowsReturned",""+rowsCount));
					switch(successCondition)
		             {				
		                case JobEntryEvalTableContent.SUCCESS_CONDITION_ROWS_COUNT_EQUAL: 
		                	successOK=(rowsCount==nrRowsLimit);
		                	break;
		                case JobEntryEvalTableContent.SUCCESS_CONDITION_ROWS_COUNT_DIFFERENT: 
		                	successOK=(rowsCount!=nrRowsLimit);
		                	break;
		                case JobEntryEvalTableContent.SUCCESS_CONDITION_ROWS_COUNT_SMALLER:
		                	successOK=(rowsCount<nrRowsLimit);
		                	break;
		                case JobEntryEvalTableContent.SUCCESS_CONDITION_ROWS_COUNT_SMALLER_EQUAL:
		                	successOK=(rowsCount<=nrRowsLimit);
		                	break;
		                case JobEntryEvalTableContent.SUCCESS_CONDITION_ROWS_COUNT_GREATER:
		                	successOK=(rowsCount>nrRowsLimit);
		                	break;
		                case JobEntryEvalTableContent.SUCCESS_CONDITION_ROWS_COUNT_GREATER_EQUAL:
		                	successOK=(rowsCount>=nrRowsLimit);
		                	break;
		                default: 
		                	break;
		             }	
				} // end if countSQLStatement!=null    
			}
			catch(KettleException dbe)
			{
				log.logError(toString(), Messages.getString("JobEntryEvalTableContent.Error.RunningEntry",dbe.getMessage()));
			}finally{
				if(db!=null) db.disconnect();
			}
		}
		else
		{
			log.logError(toString(),Messages.getString("JobEntryEvalTableContent.NoDbConnection"));
		}

		if(successOK)
		{
			result.setResult(true);
			result.setNrErrors(0);
		}
		result.setNrLinesRead(rowsCount);
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
        andValidator().validate(this, "WaitForSQL", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
      }
    
}