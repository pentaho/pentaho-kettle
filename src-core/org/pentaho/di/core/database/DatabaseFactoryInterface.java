package org.pentaho.di.core.database;

import org.pentaho.di.core.exception.KettleDatabaseException;

public interface DatabaseFactoryInterface {
	
	public String getConnectionTestReport(DatabaseMeta databaseMeta) throws KettleDatabaseException;

}
