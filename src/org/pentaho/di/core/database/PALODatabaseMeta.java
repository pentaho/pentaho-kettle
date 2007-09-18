
package org.pentaho.di.core.database;


/**
 * Contains Database Connection information through static final members for a PALO database.
 * These connections are typically custom-made.
 * That means that reading, writing, etc, is not done through JDBC. 
 * 
 * @author Matt
 * @since  18-Sep-2007
 */
public class PALODatabaseMeta extends GenericDatabaseMeta implements DatabaseInterface
{
	/**
	 * Construct a new database connection.
	 * 
	 */
	public PALODatabaseMeta(String name, String access, String host, String db, String port, String user, String pass)
	{
		super(name, access, host, db, port, user, pass);
	}
	
	public PALODatabaseMeta()
	{
	}
	
	public String getDatabaseTypeDesc()
	{
		return "PALO";
	}

	public String getDatabaseTypeDescLong()
	{
		return "Palo MOLAP Server";
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_PALO;
	}
		
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_CUSTOM, };
	}
	
	public int getDefaultDatabasePort() {
		return 7777;
	}	
}
