package org.pentaho.di.core.logging;

import java.util.List;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;

public interface LogTableInterface {

	public DatabaseMeta getDatabaseMeta();
	public List<LogTableField> getFields();
	public String getSchemaName();
	public String getTableName();
	
	public RowMetaAndData getLogRecord(LogStatus status, Object subject);
	
	/**
	 * If the table has a key field (id)
	 * 
	 * @return true if the table contains a key (we can do updates)
	 */
	public boolean containsKeyField();
	
	public String getLogTableType();
	
	public boolean isDefined();
}
