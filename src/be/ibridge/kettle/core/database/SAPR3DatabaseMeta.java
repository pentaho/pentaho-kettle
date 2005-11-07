
package be.ibridge.kettle.core.database;

import be.ibridge.kettle.core.value.Value;

/**
 * Contains SAP R/3 system specific information through static final members 
 * 
 * @author Matt
 * @since  03-07-2005
 */
public class SAPR3DatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
{
	/**
	 * Construct a new database connection.
	 * 
	 */
	public SAPR3DatabaseMeta(String name, String access, String host, String db, int port, String user, String pass)
	{
		super(name, access, host, db, port, user, pass);
	}
	
	public SAPR3DatabaseMeta()
	{
	}
	
	public String getDatabaseTypeDesc()
	{
		return "SAPR3";
	}

	public String getDatabaseTypeDescLong()
	{
		return "SAP R/3 System";
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_SAPR3;
	}
		
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_PLUGIN };
	}
	
	public int getDefaultDatabasePort()
	{
		return -1;
	}

	/**
	 * @return Whether or not the database can use auto increment type of fields (pk)
	 */
	public boolean supportsAutoInc()
	{
		return false;
	}
	
	public String getDriverClass()
	{
			return null;
	}

	public String getURL()
	{
		return null;
	}

	/**
	 * @return true if the database supports bitmap indexes
	 */
	public boolean supportsBitmapIndex()
	{
		return false;
	}

	/**
	 * @return true if the database supports synonyms
	 */
	public boolean supportsSynonyms()
	{
		return false;
	}

	/**
	 * Generates the SQL statement to add a column to the specified table
	 * @param tablename The table to add
	 * @param v The column defined as a value
	 * @param tk the name of the technical key field
	 * @param use_autoinc whether or not this field uses auto increment
	 * @param pk the name of the primary key field
	 * @param semicolon whether or not to add a semi-colon behind the statement.
	 * @return the SQL statement to add a column to the specified table
	 */
	public String getAddColumnStatement(String tablename, Value v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		return null;
	}

	/**
	 * Generates the SQL statement to modify a column in the specified table
	 * @param tablename The table to add
	 * @param v The column defined as a value
	 * @param tk the name of the technical key field
	 * @param use_autoinc whether or not this field uses auto increment
	 * @param pk the name of the primary key field
	 * @param semicolon whether or not to add a semi-colon behind the statement.
	 * @return the SQL statement to modify a column in the specified table
	 */
	public String getModifyColumnStatement(String tablename, Value v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		return null;
	}

	public String getFieldDefinition(Value v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr)
	{
	    return null;
	}
	
	public String [] getReservedWords()
	{
		return null;
	}
}
