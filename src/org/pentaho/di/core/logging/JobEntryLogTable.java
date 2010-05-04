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
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.gui.JobTracker;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobEntryResult;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.w3c.dom.Node;

/**
 * This class describes a job entry logging table
 * 
 * @author matt
 *
 */
public class JobEntryLogTable extends BaseLogTable implements Cloneable, LogTableInterface {

	private static Class<?> PKG = JobEntryLogTable.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final String	XML_TAG	= "jobentry-log-table";
	
	public enum ID {
		
		ID_BATCH("ID_BATCH"),
		CHANNEL_ID("CHANNEL_ID"),
		LOG_DATE("LOG_DATE"),
		JOBNAME("JOBNAME"),
		JOBENTRYNAME("JOBENTRYNAME"),
		LINES_READ("LINES_READ"),
		LINES_WRITTEN("LINES_WRITTEN"),
		LINES_UPDATED("LINES_UPDATED"),
		LINES_INPUT("LINES_INPUT"),
		LINES_OUTPUT("LINES_OUTPUT"),
		LINES_REJECTED("LINES_REJECTED"),
		ERRORS("ERRORS"),
		RESULT("RESULT"),
		NR_RESULT_ROWS("NR_RESULT_ROWS"),
		NR_RESULT_FILES("NR_RESULT_FILES"),
		LOG_FIELD("LOG_FIELD");
		;
		
		private String id;
		private ID(String id) {
			this.id = id;
		}

		public String toString() {
			return id;
		}
	}

	private JobEntryLogTable(VariableSpace space, HasDatabasesInterface databasesInterface) {
		super(space, databasesInterface, null, null, null);
	}
	
	@Override
	public Object clone() {
		try {
			JobEntryLogTable table = (JobEntryLogTable) super.clone();
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
        retval.append(XMLHandler.addTagValue("timeout_days", timeoutInDays)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append(super.getFieldsXML());
		retval.append(XMLHandler.closeTag(XML_TAG)).append(Const.CR);
		
		return retval.toString();
	}
	
	public void loadXML(Node node, List<DatabaseMeta> databases) {
		connectionName = XMLHandler.getTagValue(node, "connection");
		schemaName = XMLHandler.getTagValue(node, "schema");
		tableName = XMLHandler.getTagValue(node, "table");
		timeoutInDays = XMLHandler.getTagValue(node, "timeout_days");
		
		super.loadFieldsXML(node);
	}

	public static JobEntryLogTable getDefault(VariableSpace space, HasDatabasesInterface databasesInterface) {
		JobEntryLogTable table = new JobEntryLogTable(space, databasesInterface);
		
		table.fields.add( new LogTableField(ID.ID_BATCH.id, true, false, "ID_BATCH", BaseMessages.getString(PKG, "JobEntryLogTable.FieldName.IdBatch"), BaseMessages.getString(PKG, "JobEntryLogTable.FieldDescription.IdBatch"), ValueMetaInterface.TYPE_INTEGER, 8) );
		table.fields.add( new LogTableField(ID.CHANNEL_ID.id, true, false, "CHANNEL_ID", BaseMessages.getString(PKG, "JobEntryLogTable.FieldName.ChannelId"), BaseMessages.getString(PKG, "JobEntryLogTable.FieldDescription.ChannelId"), ValueMetaInterface.TYPE_STRING, 255) );
		table.fields.add( new LogTableField(ID.LOG_DATE.id, true, false, "LOG_DATE", BaseMessages.getString(PKG, "JobEntryLogTable.FieldName.LogDate"), BaseMessages.getString(PKG, "JobEntryLogTable.FieldDescription.LogDate"), ValueMetaInterface.TYPE_DATE, -1) );
		table.fields.add( new LogTableField(ID.JOBNAME.id, true, false, "TRANSNAME", BaseMessages.getString(PKG, "JobEntryLogTable.FieldName.JobName"), BaseMessages.getString(PKG, "JobEntryLogTable.FieldDescription.JobName"), ValueMetaInterface.TYPE_STRING, 255) );
		table.fields.add( new LogTableField(ID.JOBENTRYNAME.id, true, false, "STEPNAME", BaseMessages.getString(PKG, "JobEntryLogTable.FieldName.JobEntryName"), BaseMessages.getString(PKG, "JobEntryLogTable.FieldDescription.JobEntryName"), ValueMetaInterface.TYPE_STRING, 255) );
		table.fields.add( new LogTableField(ID.LINES_READ.id, true, false, "LINES_READ", BaseMessages.getString(PKG, "JobEntryLogTable.FieldName.LinesRead"), BaseMessages.getString(PKG, "JobEntryLogTable.FieldDescription.LinesRead"), ValueMetaInterface.TYPE_INTEGER, 18) );
		table.fields.add( new LogTableField(ID.LINES_WRITTEN.id, true, false, "LINES_WRITTEN", BaseMessages.getString(PKG, "JobEntryLogTable.FieldName.LinesWritten"), BaseMessages.getString(PKG, "JobEntryLogTable.FieldDescription.LinesWritten"), ValueMetaInterface.TYPE_INTEGER, 18) );
		table.fields.add( new LogTableField(ID.LINES_UPDATED.id, true, false, "LINES_UPDATED", BaseMessages.getString(PKG, "JobEntryLogTable.FieldName.LinesUpdated"), BaseMessages.getString(PKG, "JobEntryLogTable.FieldDescription.LinesUpdated"), ValueMetaInterface.TYPE_INTEGER, 18) );
		table.fields.add( new LogTableField(ID.LINES_INPUT.id, true, false, "LINES_INPUT", BaseMessages.getString(PKG, "JobEntryLogTable.FieldName.LinesInput"), BaseMessages.getString(PKG, "JobEntryLogTable.FieldDescription.LinesInput"), ValueMetaInterface.TYPE_INTEGER, 18) );
		table.fields.add( new LogTableField(ID.LINES_OUTPUT.id, true, false, "LINES_OUTPUT", BaseMessages.getString(PKG, "JobEntryLogTable.FieldName.LinesOutput"), BaseMessages.getString(PKG, "JobEntryLogTable.FieldDescription.LinesOutput"), ValueMetaInterface.TYPE_INTEGER, 18) );
		table.fields.add( new LogTableField(ID.LINES_REJECTED.id, true, false, "LINES_REJECTED", BaseMessages.getString(PKG, "JobEntryLogTable.FieldName.LinesRejected"), BaseMessages.getString(PKG, "JobEntryLogTable.FieldDescription.LinesRejected"), ValueMetaInterface.TYPE_INTEGER, 18) );
		table.fields.add( new LogTableField(ID.ERRORS.id, true, false, "ERRORS", BaseMessages.getString(PKG, "JobEntryLogTable.FieldName.Errors"), BaseMessages.getString(PKG, "JobEntryLogTable.FieldDescription.Errors"), ValueMetaInterface.TYPE_INTEGER, 18) );
		table.fields.add( new LogTableField(ID.RESULT.id, true, false, "RESULT", BaseMessages.getString(PKG, "JobEntryLogTable.FieldName.Result"), BaseMessages.getString(PKG, "JobEntryLogTable.FieldDescription.Result"), ValueMetaInterface.TYPE_BOOLEAN, -1) );
		table.fields.add( new LogTableField(ID.NR_RESULT_ROWS.id, true, false, "NR_RESULT_ROWS", BaseMessages.getString(PKG, "JobEntryLogTable.FieldName.NrResultRows"), BaseMessages.getString(PKG, "JobEntryLogTable.FieldDescription.NrResultRows"), ValueMetaInterface.TYPE_INTEGER, 18) );
		table.fields.add( new LogTableField(ID.NR_RESULT_FILES.id, true, false, "NR_RESULT_FILES", BaseMessages.getString(PKG, "JobEntryLogTable.FieldName.NrResultFiles"), BaseMessages.getString(PKG, "JobEntryLogTable.FieldDescription.NrResultFiles"), ValueMetaInterface.TYPE_INTEGER, 18) );
		table.fields.add( new LogTableField(ID.LOG_FIELD.id, false, false, "LOG_FIELD", BaseMessages.getString(PKG, "JobEntryLogTable.FieldName.LogField"), BaseMessages.getString(PKG, "JobEntryLogTable.FieldDescription.LogField"), ValueMetaInterface.TYPE_STRING, DatabaseMeta.CLOB_LENGTH) );

		table.findField(ID.LOG_DATE.id).setLogDateField(true);
		table.findField(ID.CHANNEL_ID.id).setVisible(false);
		table.findField(ID.LOG_FIELD.id).setLogField(true);
		table.findField(ID.ERRORS.id).setErrorsField(true);

		return table;
	}
		
	/**
	 * This method calculates all the values that are required
	 * @param id the id to use or -1 if no id is needed
	 * @param status the log status to use
	 */
	public RowMetaAndData getLogRecord(LogStatus status, Object subject) {
		if (subject==null || subject instanceof JobEntryCopy) {
			
			JobEntryCopy jobEntryCopy = (JobEntryCopy) subject; 
			
			RowMetaAndData row = new RowMetaAndData();
			
			for (LogTableField field : fields) {
				if (field.isEnabled()) {
					Object value = null;
					if (subject!=null) {
						
						JobEntryInterface jobEntry = (JobEntryInterface) subject;
						JobTracker jobTracker = jobEntry.getParentJob().getJobTracker();
						JobTracker entryTracker = jobTracker.findJobTracker(jobEntryCopy);
						JobEntryResult jobEntryResult = null;
						if (entryTracker!=null)  {
							jobEntryResult = entryTracker.getJobEntryResult();
						}
						Result result = null;
						if (jobEntryResult!=null) {
							result = jobEntryResult.getResult();
						}
						
						switch(ID.valueOf(field.getId())){
						
						case ID_BATCH : value = new Long(jobEntry.getParentJob().getBatchId()); break;
						case CHANNEL_ID : value = jobEntry.getLogChannel().getLogChannelId(); break;
						case LOG_DATE : value = new Date(); break;
						case JOBNAME : value = jobEntry.getParentJob().getJobname(); break;
						case JOBENTRYNAME : value = jobEntry.getName(); break;
						case LINES_READ : value = new Long(result.getNrLinesRead()); break;
						case LINES_WRITTEN : value = new Long(result.getNrLinesWritten()); break;
						case LINES_UPDATED : value = new Long(result.getNrLinesUpdated()); break;
						case LINES_INPUT : value = new Long(result.getNrLinesInput()); break;
						case LINES_OUTPUT : value = new Long(result.getNrLinesOutput()); break;
						case LINES_REJECTED : value = new Long(result.getNrLinesRejected()); break;
						case ERRORS : value = new Long(result.getNrErrors()); break;
						case RESULT : value = new Boolean(result.getResult()); break;
						case NR_RESULT_FILES : value = new Long(result.getResultFiles().size()); break;
						case NR_RESULT_ROWS : value = new Long(result.getRows().size()); break;
						case LOG_FIELD : 
							StringBuffer buffer = CentralLogStore.getAppender().getBuffer(jobEntry.getLogChannel().getLogChannelId(), false);
							value = buffer.toString();
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
		return "JOB_ENTRY";
	}

	public String getLogTableType() {
		return BaseMessages.getString(PKG, "JobEntryLogTable.Type.Description");
	}

	public String getConnectionNameVariable() {
		return Const.KETTLE_JOBENTRY_LOG_DB; // $NON-NLS-1$
	}

	public String getSchemaNameVariable() {
		return Const.KETTLE_JOBENTRY_LOG_SCHEMA; // $NON-NLS-1$
	}

	public String getTableNameVariable() {
		return Const.KETTLE_JOBENTRY_LOG_TABLE; // $NON-NLS-1$
	}
}
