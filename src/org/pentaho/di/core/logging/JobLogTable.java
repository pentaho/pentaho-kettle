/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.core.logging;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.repository.RepositoryAttributeInterface;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.w3c.dom.Node;

/**
 * This class describes a job logging table
 * 
 * @author matt
 *
 */
public class JobLogTable extends BaseLogTable implements Cloneable, LogTableInterface {

	private static Class<?> PKG = JobLogTable.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final String	XML_TAG	= "job-log-table";
	
	public enum ID {
		
		ID_JOB("ID_JOB"),
		CHANNEL_ID("CHANNEL_ID"),
		JOBNAME("JOBNAME"),
		STATUS("STATUS"),
		LINES_READ("LINES_READ"),
		LINES_WRITTEN("LINES_WRITTEN"),
		LINES_UPDATED("LINES_UPDATED"),
		LINES_INPUT("LINES_INPUT"),
		LINES_OUTPUT("LINES_OUTPUT"),
		LINES_REJECTED("LINES_REJECTED"),
		ERRORS("ERRORS"),
		STARTDATE("STARTDATE"),
		ENDDATE("ENDDATE"),
		LOGDATE("LOGDATE"),
		DEPDATE("DEPDATE"),
		REPLAYDATE("REPLAYDATE"),
		LOG_FIELD("LOG_FIELD");
		
		private String id;
		private ID(String id) {
			this.id = id;
		}

		public String toString() {
			return id;
		}
	}
	
	private String logInterval;
	
	private String logSizeLimit;
	
	private JobLogTable(VariableSpace space, HasDatabasesInterface databasesInterface) {
		super(space, databasesInterface, null, null, null);
	}
	
	@Override
	public Object clone() {
		try {
			JobLogTable table = (JobLogTable) super.clone();
			table.fields = new ArrayList<LogTableField>();
			for (LogTableField field : this.fields) {
				table.fields.add((LogTableField) field.clone());
			}
			return table;
		}
		catch(CloneNotSupportedException e) {
			return null;
		}
	}
	
	public String getXML() {
		StringBuffer retval = new StringBuffer();

		retval.append(XMLHandler.openTag(XML_TAG));
        retval.append(XMLHandler.addTagValue("connection", connectionName)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        retval.append(XMLHandler.addTagValue("schema", schemaName)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append(XMLHandler.addTagValue("table", tableName)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append(XMLHandler.addTagValue("size_limit_lines", logSizeLimit)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append(XMLHandler.addTagValue("interval", logInterval)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append(XMLHandler.addTagValue("timeout_days", timeoutInDays)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append(super.getFieldsXML());
		retval.append(XMLHandler.closeTag(XML_TAG)).append(Const.CR);
		
		return retval.toString();
	}

	public void loadXML(Node node, List<DatabaseMeta> databases) {
		connectionName = XMLHandler.getTagValue(node, "connection");
		schemaName = XMLHandler.getTagValue(node, "schema");
		tableName = XMLHandler.getTagValue(node, "table");
		logSizeLimit = XMLHandler.getTagValue(node, "size_limit_lines");
		logInterval = XMLHandler.getTagValue(node, "interval");
		timeoutInDays = XMLHandler.getTagValue(node, "timeout_days");

		super.loadFieldsXML(node);
	}
	
	public void saveToRepository(RepositoryAttributeInterface attributeInterface) throws KettleException {
		super.saveToRepository(attributeInterface);
		
		// Also save the log interval and log size limit
		//
		attributeInterface.setAttribute(getLogTableCode()+PROP_LOG_TABLE_INTERVAL, logInterval);
		attributeInterface.setAttribute(getLogTableCode()+PROP_LOG_TABLE_SIZE_LIMIT, logSizeLimit);
	}

	public void loadFromRepository(RepositoryAttributeInterface attributeInterface) throws KettleException {
		super.loadFromRepository(attributeInterface);
		
		logInterval = attributeInterface.getAttributeString(getLogTableCode()+PROP_LOG_TABLE_INTERVAL);
		logSizeLimit = attributeInterface.getAttributeString(getLogTableCode()+PROP_LOG_TABLE_SIZE_LIMIT);
	}

	public static JobLogTable getDefault(VariableSpace space, HasDatabasesInterface databasesInterface) {
		JobLogTable table = new JobLogTable(space, databasesInterface);
		
		table.fields.add( new LogTableField(ID.ID_JOB.id, true, false, "ID_JOB", BaseMessages.getString(PKG, "JobLogTable.FieldName.BatchID"), BaseMessages.getString(PKG, "JobLogTable.FieldDescription.BatchID"), ValueMetaInterface.TYPE_INTEGER, 8) );
		table.fields.add( new LogTableField(ID.CHANNEL_ID.id, true, false, "CHANNEL_ID", BaseMessages.getString(PKG, "JobLogTable.FieldName.ChannelID"), BaseMessages.getString(PKG, "JobLogTable.FieldDescription.ChannelID"), ValueMetaInterface.TYPE_STRING, 255) );
		table.fields.add( new LogTableField(ID.JOBNAME.id, true, false, "JOBNAME", BaseMessages.getString(PKG, "JobLogTable.FieldName.JobName"), BaseMessages.getString(PKG, "JobLogTable.FieldDescription.JobName"), ValueMetaInterface.TYPE_STRING, 255) );
		table.fields.add( new LogTableField(ID.STATUS.id, true, false, "STATUS", BaseMessages.getString(PKG, "JobLogTable.FieldName.Status"), BaseMessages.getString(PKG, "JobLogTable.FieldDescription.Status"), ValueMetaInterface.TYPE_STRING, 15) );
		table.fields.add( new LogTableField(ID.LINES_READ.id, true, false, "LINES_READ", BaseMessages.getString(PKG, "JobLogTable.FieldName.LinesRead"), BaseMessages.getString(PKG, "JobLogTable.FieldDescription.LinesRead"), ValueMetaInterface.TYPE_INTEGER, 18) );
		table.fields.add( new LogTableField(ID.LINES_WRITTEN.id, true, false, "LINES_WRITTEN", BaseMessages.getString(PKG, "JobLogTable.FieldName.LinesWritten"), BaseMessages.getString(PKG, "JobLogTable.FieldDescription.LinesWritten"), ValueMetaInterface.TYPE_INTEGER, 18) );
		table.fields.add( new LogTableField(ID.LINES_UPDATED.id, true, false, "LINES_UPDATED", BaseMessages.getString(PKG, "JobLogTable.FieldName.LinesUpdated"), BaseMessages.getString(PKG, "JobLogTable.FieldDescription.LinesUpdated"), ValueMetaInterface.TYPE_INTEGER, 18) );
		table.fields.add( new LogTableField(ID.LINES_INPUT.id, true, false, "LINES_INPUT", BaseMessages.getString(PKG, "JobLogTable.FieldName.LinesInput"), BaseMessages.getString(PKG, "JobLogTable.FieldDescription.LinesInput"), ValueMetaInterface.TYPE_INTEGER, 18) );
		table.fields.add( new LogTableField(ID.LINES_OUTPUT.id, true, false, "LINES_OUTPUT", BaseMessages.getString(PKG, "JobLogTable.FieldName.LinesOutput"), BaseMessages.getString(PKG, "JobLogTable.FieldDescription.LinesOutput"), ValueMetaInterface.TYPE_INTEGER, 18) );
		table.fields.add( new LogTableField(ID.LINES_REJECTED.id, true, false, "LINES_REJECTED", BaseMessages.getString(PKG, "JobLogTable.FieldName.LinesRejected"), BaseMessages.getString(PKG, "JobLogTable.FieldDescription.LinesRejected"), ValueMetaInterface.TYPE_INTEGER, 18) );
		table.fields.add( new LogTableField(ID.ERRORS.id, true, false, "ERRORS", BaseMessages.getString(PKG, "JobLogTable.FieldName.Errors"), BaseMessages.getString(PKG, "JobLogTable.FieldDescription.Errors"), ValueMetaInterface.TYPE_INTEGER, 18) );
		table.fields.add( new LogTableField(ID.STARTDATE.id, true, false, "STARTDATE", BaseMessages.getString(PKG, "JobLogTable.FieldName.StartDateRange"), BaseMessages.getString(PKG, "JobLogTable.FieldDescription.StartDateRange"), ValueMetaInterface.TYPE_DATE, -1) );
		table.fields.add( new LogTableField(ID.ENDDATE.id, true, false, "ENDDATE", BaseMessages.getString(PKG, "JobLogTable.FieldName.EndDateRange"), BaseMessages.getString(PKG, "JobLogTable.FieldDescription.EndDateRange"), ValueMetaInterface.TYPE_DATE, -1) );
		table.fields.add( new LogTableField(ID.LOGDATE.id, true, false, "LOGDATE", BaseMessages.getString(PKG, "JobLogTable.FieldName.LogDate"), BaseMessages.getString(PKG, "JobLogTable.FieldDescription.LogDate"), ValueMetaInterface.TYPE_DATE, -1) );
		table.fields.add( new LogTableField(ID.DEPDATE.id, true, false, "DEPDATE", BaseMessages.getString(PKG, "JobLogTable.FieldName.DepDate"), BaseMessages.getString(PKG, "JobLogTable.FieldDescription.DepDate"), ValueMetaInterface.TYPE_DATE, -1) );
		table.fields.add( new LogTableField(ID.REPLAYDATE.id, true, false, "REPLAYDATE", BaseMessages.getString(PKG, "JobLogTable.FieldName.ReplayDate"), BaseMessages.getString(PKG, "JobLogTable.FieldDescription.ReplayDate"), ValueMetaInterface.TYPE_DATE, -1) );
		table.fields.add( new LogTableField(ID.LOG_FIELD.id, true, false, "LOG_FIELD", BaseMessages.getString(PKG, "JobLogTable.FieldName.LogField"), BaseMessages.getString(PKG, "JobLogTable.FieldDescription.LogField"), ValueMetaInterface.TYPE_STRING, DatabaseMeta.CLOB_LENGTH) );
		
		table.findField(ID.ID_JOB).setKey(true);
		table.findField(ID.LOGDATE).setLogDateField(true);
		table.findField(ID.LOG_FIELD).setLogField(true);
		table.findField(ID.CHANNEL_ID).setVisible(false);
		table.findField(ID.JOBNAME).setVisible(false);
		table.findField(ID.STATUS).setStatusField(true);
		table.findField(ID.ERRORS).setErrorsField(true);
		table.findField(ID.JOBNAME).setNameField(true);

		return table;
	}
	
	public LogTableField findField(ID id) {
		return super.findField(id.id);
	}
	
	public Object getSubject(ID id) {
		return super.getSubject(id.id);
	}
	
	public String getSubjectString(ID id) {
		return super.getSubjectString(id.id);
	}

	public void setBatchIdUsed(boolean use) {
		findField(ID.ID_JOB).setEnabled(use);
	}

	public boolean isBatchIdUsed() {
		return findField(ID.ID_JOB).isEnabled();
	}

	public void setLogFieldUsed(boolean use) {
		findField(ID.LOG_FIELD).setEnabled(use);
	}

	public boolean isLogFieldUsed() {
		return findField(ID.LOG_FIELD).isEnabled();
	}
	
	public String getStepnameRead() {
		return getSubjectString(ID.LINES_READ);
	}

	public String getStepnameWritten() {
		return getSubjectString(ID.LINES_WRITTEN);
	}

	public String getStepnameInput() {
		return getSubjectString(ID.LINES_INPUT);
	}

	public String getStepnameOutput() {
		return getSubjectString(ID.LINES_OUTPUT);
	}

	public String getStepnameUpdated() {
		return getSubjectString(ID.LINES_UPDATED);
	}

	public String getStepnameRejected() {
		return getSubjectString(ID.LINES_REJECTED);
	}

	/**
	 * Sets the logging interval in seconds.
	 * Disabled if the logging interval is <=0.
	 * 
	 * @param logInterval The log interval value.  A value higher than 0 means that the log table is updated every 'logInterval' seconds.
	 */
	public void setLogInterval(String logInterval) {
		this.logInterval = logInterval;
	}

	/**
	 * Get the logging interval in seconds.
	 * Disabled if the logging interval is <=0.
	 * A value higher than 0 means that the log table is updated every 'logInterval' seconds.
	 * 
	 * @param logInterval The log interval, 
	 */
	public String getLogInterval() {
		return logInterval;
	}

	/**
	 * @return the logSizeLimit
	 */
	public String getLogSizeLimit() {
		return logSizeLimit;
	}

	/**
	 * @param logSizeLimit the logSizeLimit to set
	 */
	public void setLogSizeLimit(String logSizeLimit) {
		this.logSizeLimit = logSizeLimit;
	}

	/**
	 * This method calculates all the values that are required
	 * @param id the id to use or -1 if no id is needed
	 * @param status the log status to use
	 */
	public RowMetaAndData getLogRecord(LogStatus status, Object subject, Object parent) {
		if (subject==null || subject instanceof Job) {
			Job job = (Job) subject;
			Result result = null;
			if (job!=null) result = job.getResult();
			
			RowMetaAndData row = new RowMetaAndData();
			
			for (LogTableField field : fields) {
				if (field.isEnabled()) {
					Object value = null;
					if (job!=null) {
						
						switch(ID.valueOf(field.getId())){
						case ID_JOB : value = new Long(job.getBatchId()); break;
						case CHANNEL_ID : value = job.getLogChannelId(); break;
						case JOBNAME : value = job.getJobname(); break;
						case STATUS : value = status.getStatus(); break;
						case LINES_READ : value = result==null ? null : new Long(result.getNrLinesRead()); break;
						case LINES_WRITTEN : value = result==null ? null : new Long(result.getNrLinesWritten()); break;
						case LINES_INPUT : value = result==null ? null : new Long(result.getNrLinesInput()); break;
						case LINES_OUTPUT : value = result==null ? null : new Long(result.getNrLinesOutput()); break;
						case LINES_UPDATED : value = result==null ? null : new Long(result.getNrLinesUpdated()); break;
						case LINES_REJECTED : value = result==null ? null : new Long(result.getNrLinesRejected()); break;
						case ERRORS: value = result==null ? null : new Long(result.getNrErrors()); break;
						case STARTDATE: value = job.getStartDate(); break;
						case LOGDATE: value = job.getLogDate(); break;
						case ENDDATE: value = job.getEndDate(); break;
						case DEPDATE: value = job.getDepDate(); break;
						case REPLAYDATE: value = job.getCurrentDate(); break;
						case LOG_FIELD: 
							StringBuffer buffer = CentralLogStore.getAppender().getBuffer(job.getLogChannelId(), true);
							value = buffer.append(Const.CR+status.getStatus().toUpperCase()+Const.CR).toString();
							break;
						}
					}

					row.addValue(field.getFieldName(), field.getDataType(), value);
					row.getRowMeta().getValueMeta(row.size()-1).setLength(field.getLength());
				}
			}
				
			return row;
		}
		else {
			return null;
		}
	}

	public String getLogTableCode() {
		return "JOB";
	}

	public String getLogTableType() {
		return BaseMessages.getString(PKG, "JobLogTable.Type.Description");
	}

	public String getConnectionNameVariable() {
		return Const.KETTLE_JOB_LOG_DB;
	}

	public String getSchemaNameVariable() {
		return Const.KETTLE_JOB_LOG_SCHEMA;
	}

	public String getTableNameVariable() {
		return Const.KETTLE_JOB_LOG_TABLE;
	}

    public List<RowMetaInterface> getRecommendedIndexes() {
      List<RowMetaInterface> indexes = new ArrayList<RowMetaInterface>();
      
      // First index : ID_JOB if any is used.
      //
      if (isBatchIdUsed()) {
        RowMetaInterface batchIndex = new RowMeta();
        LogTableField keyField = getKeyField();
        
        ValueMetaInterface keyMeta = new ValueMeta(keyField.getFieldName(), keyField.getDataType());
        keyMeta.setLength(keyField.getLength());
        batchIndex.addValueMeta(keyMeta);
        
        indexes.add(batchIndex);
      }
      
      // The next index includes : ERRORS, STATUS, JOBNAME:
      
      RowMetaInterface lookupIndex = new RowMeta();
      LogTableField errorsField = findField(ID.ERRORS);
      if (errorsField!=null) {
        ValueMetaInterface valueMeta = new ValueMeta(errorsField.getFieldName(), errorsField.getDataType());
        valueMeta.setLength(errorsField.getLength());
        lookupIndex.addValueMeta(valueMeta);
      }
      LogTableField statusField = findField(ID.STATUS);
      if (statusField!=null) {
        ValueMetaInterface valueMeta = new ValueMeta(statusField.getFieldName(), statusField.getDataType());
        valueMeta.setLength(statusField.getLength());
        lookupIndex.addValueMeta(valueMeta);
      }
      LogTableField transNameField = findField(ID.JOBNAME);
      if (transNameField!=null) {
        ValueMetaInterface valueMeta = new ValueMeta(transNameField.getFieldName(), transNameField.getDataType());
        valueMeta.setLength(transNameField.getLength());
        lookupIndex.addValueMeta(valueMeta);
      }
      
      indexes.add(lookupIndex);
      
      return indexes;
  }


}
