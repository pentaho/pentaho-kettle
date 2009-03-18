package org.pentaho.di.core.database;

public class InfobrightDatabaseMeta extends MySQLDatabaseMeta implements DatabaseInterface {
	
	public String getDatabaseTypeDesc()
	{
		return "INFOBRIGHT";
	}

	public String getDatabaseTypeDescLong()
	{
		return "Infobright";
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_INFOBRIGHT;
	}


}
