package org.pentaho.database.dialect;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.di.core.exception.KettleDatabaseException;

public class PALODatabaseDialect extends GenericDatabaseDialect {

	public static final IDatabaseType DBTYPE = new DatabaseType(
			"Palo MOLAP Server", 
			"PALO", 
			DatabaseAccessType.getList(DatabaseAccessType.PLUGIN), 
			7777, 
			null
		);

	public IDatabaseType getDatabaseType() {
		return DBTYPE;
	}

	public String getNativeDriver() {
		return null;
	}

	protected String getNativeJdbcPre() {
		return null;
	}

	public String getURL(IDatabaseConnection connection) throws KettleDatabaseException {
		return null;
	}
	
	public String getDatabaseFactoryName() {
		return "plugin.palo.core.PaloHelper";
	}
}
