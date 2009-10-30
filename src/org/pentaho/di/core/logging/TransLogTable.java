package org.pentaho.di.core.logging;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;

/**
 * This class describes a transformation logging table
 * 
 * @author matt
 *
 */
public class TransLogTable implements Cloneable, LogTableInterface {

	private static Class<?> PKG = TransLogTable.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	public static final String	ID_BATCH_ID			= "BATCH_ID";
	public static final String	ID_CHANNEL_ID		= "CHANNEL_ID";
	public static final String	ID_TRANSNAME		= "TRANSNAME";
	public static final String	ID_STATUS			= "STATUS";
	public static final String	ID_LINES_READ		= "LINES_READ";
	public static final String	ID_LINES_WRITTEN	= "LINES_WRITTEN";
	public static final String	ID_LINES_UPDATED	= "LINES_UPDATED";
	public static final String	ID_LINES_INPUT		= "LINES_INPUT";
	public static final String	ID_LINES_OUTPUT		= "LINES_OUTPUT";
	public static final String	ID_LINES_REJECTED	= "LINES_REJECTED";
	public static final String	ID_ERRORS			= "ERRORS";
	public static final String	ID_STARTDATE		= "STARTDATE";
	public static final String	ID_ENDDATE			= "ENDDATE";
	public static final String	ID_LOGDATE			= "LOGDATE";
	public static final String	ID_DEPDATE			= "DEPDATE";
	public static final String	ID_REPLAYDATE		= "REPLAYDATE";
	public static final String	ID_LOG_FIELD		= "LOG_FIELD";
	
	private DatabaseMeta databaseMeta;
	private String schemaName;
	private String tableName;
	
	private List<LogTableField> fields;
	
	private String logInterval;
	
	private String logSizeLimit;
	
	/**
	 * Create a new transformation logging table description.
	 * It contains an empty list of log table fields.
	 * 
	 * @param databaseMeta
	 * @param schemaName
	 * @param tableName
	 */
	public TransLogTable(DatabaseMeta databaseMeta, String schemaName, String tableName) {
		this.databaseMeta = databaseMeta;
		this.schemaName = schemaName;
		this.tableName = tableName;
		this.fields = new ArrayList<LogTableField>();
		this.logInterval = null;
	}
	
	public TransLogTable() {
		this(null, null, null);
	}
	
	@Override
	public Object clone() {
		try {
			TransLogTable table = (TransLogTable) super.clone();
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

	/**
	 * @return the databaseMeta
	 */
	public DatabaseMeta getDatabaseMeta() {
		return databaseMeta;
	}

	/**
	 * @param databaseMeta the databaseMeta to set
	 */
	public void setDatabaseMeta(DatabaseMeta databaseMeta) {
		this.databaseMeta = databaseMeta;
	}

	/**
	 * @return the schemaName
	 */
	public String getSchemaName() {
		return schemaName;
	}

	/**
	 * @param schemaName the schemaName to set
	 */
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * @return the fields
	 */
	public List<LogTableField> getFields() {
		return fields;
	}

	/**
	 * @param fields the fields to set
	 */
	public void setFields(List<LogTableField> fields) {
		this.fields = fields;
	}

	public static TransLogTable getDefault() {
		TransLogTable table = new TransLogTable();
		
		table.fields.add( new LogTableField(ID_BATCH_ID, true, false, "ID_BATCH", BaseMessages.getString(PKG, "TransLogTable.FieldDescription.BatchID"), ValueMetaInterface.TYPE_INTEGER, 10) );
		table.fields.add( new LogTableField(ID_CHANNEL_ID, false, false, "CHANNEL_ID", BaseMessages.getString(PKG, "TransLogTable.FieldDescription.ChannelID"), ValueMetaInterface.TYPE_INTEGER, 10) );
		table.fields.add( new LogTableField(ID_TRANSNAME, true, false, "TRANSNAME", BaseMessages.getString(PKG, "TransLogTable.FieldDescription.TransName"), ValueMetaInterface.TYPE_STRING, 10) );
		table.fields.add( new LogTableField(ID_STATUS, true, false, "STATUS", BaseMessages.getString(PKG, "TransLogTable.FieldDescription.Status"), ValueMetaInterface.TYPE_STRING, 10) );
		table.fields.add( new LogTableField(ID_LINES_READ, true, true, "LINES_READ", BaseMessages.getString(PKG, "TransLogTable.FieldDescription.LinesRead"), ValueMetaInterface.TYPE_INTEGER, 10) );
		table.fields.add( new LogTableField(ID_LINES_WRITTEN, true, true, "LINES_WRITTEN", BaseMessages.getString(PKG, "TransLogTable.FieldDescription.LinesWritten"), ValueMetaInterface.TYPE_INTEGER, 10) );
		table.fields.add( new LogTableField(ID_LINES_UPDATED, true, true, "LINES_UPDATED", BaseMessages.getString(PKG, "TransLogTable.FieldDescription.LinesUpdated"), ValueMetaInterface.TYPE_INTEGER, 10) );
		table.fields.add( new LogTableField(ID_LINES_INPUT, true, true, "LINES_INPUT", BaseMessages.getString(PKG, "TransLogTable.FieldDescription.LinesInput"), ValueMetaInterface.TYPE_INTEGER, 10) );
		table.fields.add( new LogTableField(ID_LINES_OUTPUT, true, true, "LINES_OUTPUT", BaseMessages.getString(PKG, "TransLogTable.FieldDescription.LinesOutput"), ValueMetaInterface.TYPE_INTEGER, 10) );
		table.fields.add( new LogTableField(ID_LINES_REJECTED, false, true, "LINES_REJECTED", BaseMessages.getString(PKG, "TransLogTable.FieldDescription.LinesRejected"), ValueMetaInterface.TYPE_INTEGER, 10) );
		table.fields.add( new LogTableField(ID_ERRORS, true, false, "ERRORS", BaseMessages.getString(PKG, "TransLogTable.FieldDescription.Errors"), ValueMetaInterface.TYPE_INTEGER, 10) );
		table.fields.add( new LogTableField(ID_STARTDATE, true, false, "STARTDATE", BaseMessages.getString(PKG, "TransLogTable.FieldDescription.StartDateRange"), ValueMetaInterface.TYPE_DATE, -1) );
		table.fields.add( new LogTableField(ID_ENDDATE, true, false, "ENDDATE", BaseMessages.getString(PKG, "TransLogTable.FieldDescription.EndDateRange"), ValueMetaInterface.TYPE_DATE, -1) );
		table.fields.add( new LogTableField(ID_LOGDATE, true, false, "LOGDATE", BaseMessages.getString(PKG, "TransLogTable.FieldDescription.LogDate"), ValueMetaInterface.TYPE_DATE, -1) );
		table.fields.add( new LogTableField(ID_DEPDATE, true, false, "DEPDATE", BaseMessages.getString(PKG, "TransLogTable.FieldDescription.DepDate"), ValueMetaInterface.TYPE_DATE, -1) );
		table.fields.add( new LogTableField(ID_REPLAYDATE, true, false, "REPLAYDATE", BaseMessages.getString(PKG, "TransLogTable.FieldDescription.ReplayDate"), ValueMetaInterface.TYPE_DATE, -1) );
		table.fields.add( new LogTableField(ID_LOG_FIELD, true, false, "LOG_FIELD", BaseMessages.getString(PKG, "TransLogTable.FieldDescription.LogField"), ValueMetaInterface.TYPE_STRING, DatabaseMeta.CLOB_LENGTH) );
		
		return table;
	}

	/**
	 * Find a log table field in this log table definition.
	 * Use the id of the field to do the lookup.
	 * @param id the id of the field to search for
	 * @return the log table field or null if nothing was found.
	 */
	public LogTableField findField(String id) {
		for (LogTableField field : fields) {
			if (field.getId().equals(id)) {
				return field;
			}
		}
		return null;
	}
	
	/**
	 * Get the subject of a field with the specified ID
	 * @param id
	 * @return the subject or null if no field could be find with the specified id
	 */
	public Object getSubject(String id) {
		LogTableField field = findField(id);
		if (field==null) return null;
		return field.getSubject();
	}

	/**
	 * Return the subject in the form of a string for the specified ID.
	 * @param id the id of the field to look for.
	 * @return the string of the subject (name of step) or null if nothing was found.
	 */
	public String getSubjectString(String id) {
		LogTableField field = findField(id);
		if (field==null) return null;
		if (field.getSubject()==null) return null;
		return field.getSubject().toString();
	}
	
	public void setBatchIdUsed(boolean use) {
		findField(ID_BATCH_ID).setEnabled(use);
	}

	public boolean isBatchIdUsed() {
		return findField(ID_BATCH_ID).isEnabled();
	}

	public void setLogFieldUsed(boolean use) {
		findField(ID_LOG_FIELD).setEnabled(use);
	}

	public boolean isLogFieldUsed() {
		return findField(ID_LOG_FIELD).isEnabled();
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

}
