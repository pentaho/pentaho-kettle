
package be.ibridge.kettle.core.database;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.value.Value;

/**
 * Contains Computer Associates Ingres specific information through static final members 
 * 
 * @author Matt
 * @since  11-mrt-2005
 */
public class IngresDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
{
	public IngresDatabaseMeta(String name, String access, String host, String db, int port, String user, String pass)
	{
		super(name, access, host, db, port, user, pass);
	}
	
	public IngresDatabaseMeta()
	{
	}
	
	public String getDatabaseTypeDesc()
	{
		return "INGRES";
	}

	public String getDatabaseTypeDescLong()
	{
		return "CA Ingres";
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_INGRES;
	}
		
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC };
	}
	
	public int getDefaultDatabasePort()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE) return -1;
		return -1;
	}

	public String getDriverClass()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "sun.jdbc.odbc.JdbcOdbcDriver";
		}
		else
		{
			return "ca.edbc.jdbc.EdbcDriver";
		}
	}

	public String getURL()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "jdbc:odbc:"+getDatabaseName();
		}
		else
		{
			return "jdbc:edbc://"+getHostname()+":"+getDatabasePortNumber()+"/"+getDatabaseName();
		}
	}

	/**
	 * @see be.ibridge.kettle.core.database.DatabaseInterface#getSchemaTableCombination(java.lang.String, java.lang.String)
	 */
	public String getSchemaTableCombination(String schema_name, String table_part)
	{
		return table_part;
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
		return "ALTER TABLE "+tablename+" ADD COLUMN "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
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
		return "ALTER TABLE "+tablename+" ALTER COLUMN "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
	}

	/**
	 * Generates the SQL statement to drop a column from the specified table
	 * @param tablename The table to add
	 * @param v The column defined as a value
	 * @param tk the name of the technical key field
	 * @param use_autoinc whether or not this field uses auto increment
	 * @param pk the name of the primary key field
	 * @param semicolon whether or not to add a semi-colon behind the statement.
	 * @return the SQL statement to drop a column from the specified table
	 */
	public String getDropColumnStatement(String tablename, Value v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		return "ALTER TABLE "+tablename+" DROP COLUMN "+v.getName()+Const.CR;
	}

	public String getFieldDefinition(Value v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr)
	{
		String retval="";
		
		String fieldname = v.getName();
		int    length    = v.getLength();
		int    precision = v.getPrecision();
		
		if (add_fieldname) retval+=fieldname+" ";
		
		int type         = v.getType();
		switch(type)
		{
		case Value.VALUE_TYPE_DATE   : retval+="DATE"; break;
		case Value.VALUE_TYPE_BOOLEAN: retval+="CHAR(1)"; break;
		case Value.VALUE_TYPE_NUMBER :
		case Value.VALUE_TYPE_INTEGER: 
        case Value.VALUE_TYPE_BIGNUMBER: 
			if (fieldname.equalsIgnoreCase(tk) ||  // Technical key
			    fieldname.equalsIgnoreCase(pk)     // Primary key
			    ) 
			{
				if (use_autoinc)
				{
					retval+="BIGINT PRIMARY KEY IDENTITY(0,1)";
				}
				else
				{
					retval+="BIGINT PRIMARY KEY NOT NULL";
				}
			} 
			else
			{
                if (precision==0)  // integer numbers
                {
                    if (length>9)
                    {
                        retval+="FLOAT";
                    }
                    else
                    {
                        if (length>4)
                        {
                            retval+="INTEGER";
                        }
                        else
                        {
                            if (length>2)
                            {
                                retval+="smallint";
                            }
                            else
                            {
                                retval+="integer1";
                            }
                        }
                    }
                }
                else
                {
                    retval+="FLOAT";
                }
			}
			break;
		case Value.VALUE_TYPE_STRING:
			//	Maybe use some default DB String length in case length<=0
			if (length>0)
			{
				retval+="VARCHAR("+length+")";	
			}
			else
			{
				retval+="VARCHAR(2000)";
			} 
			break;
		default:
			retval+=" UNKNOWN";
			break;
		}
		
		if (add_cr) retval+=Const.CR;
		
		return retval;
	}
	
	/* (non-Javadoc)
	 * @see be.ibridge.kettle.core.database.DatabaseInterface#getSQLListOfProcedures()
	 */
	public String getSQLListOfProcedures()
	{
		return null;
	}
    
    /**
     * @param tableName The table to be truncated.
     * @return The SQL statement to truncate a table: remove all rows from it without a transaction
     */
    public String getTruncateTableStatement(String tableName)
    {
        return "DELETE FROM "+tableName;
    }


}
