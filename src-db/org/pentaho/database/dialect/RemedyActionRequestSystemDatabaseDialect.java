package org.pentaho.database.dialect;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.di.core.exception.KettleDatabaseException;

public class RemedyActionRequestSystemDatabaseDialect extends GenericDatabaseDialect {

	public static final IDatabaseType DBTYPE = new DatabaseType(
			"Remedy Action Request System", 
			"REMEDY-AR-SYSTEM", 
			DatabaseAccessType.getList(DatabaseAccessType.ODBC, DatabaseAccessType.JNDI), 
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
	/**
	 * Checks whether or not the command setFetchSize() is supported by the JDBC driver...
	 * @return true is setFetchSize() is supported!
	 */
	public boolean isFetchSizeSupported()
	{
		return false;
	}
	
	/**
	 * @return true if the database supports bitmap indexes
	 */
	public boolean supportsBitmapIndex()
	{
		return false;
	}
	
	/**
	 * @return true if Kettle can create a repository on this type of database.
	 */
	public boolean supportsRepository()
	{
		return false;
	}

	/**
	 * @return true if this database needs a transaction to perform a query (auto-commit turned off).
	 */
	public boolean isRequiringTransactionsOnQueries()
	{
		return false;
	}
}
