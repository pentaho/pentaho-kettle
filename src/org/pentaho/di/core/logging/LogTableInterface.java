package org.pentaho.di.core.logging;

import java.util.List;

import org.pentaho.di.core.database.DatabaseMeta;

public interface LogTableInterface {

	public DatabaseMeta getDatabaseMeta();
	public List<LogTableField> getFields();
	public String getSchemaName();
	public String getTableName();
}
