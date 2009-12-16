package org.pentaho.database.dialect;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaInterface;

public class CacheDatabaseDialect extends AbstractDatabaseDialect {
  
  public static final IDatabaseType DBTYPE = 
    new DatabaseType(
        "Intersystems Cache", 
        "CACHE", 
        DatabaseAccessType.getList(
            DatabaseAccessType.NATIVE, 
            DatabaseAccessType.ODBC, 
            DatabaseAccessType.OCI, 
            DatabaseAccessType.JNDI
        ), 
        1972, 
        null
      );
  
  public IDatabaseType getDatabaseType() {
    return DBTYPE;
  }

  @Override
  public String getNativeDriver() {
    return "com.intersys.jdbc.CacheDriver";
  }
  
  @Override
  public String getURL(IDatabaseConnection connection)
    {
    if (connection.getAccessType()==DatabaseAccessType.ODBC) {
      return "jdbc:odbc:"+connection.getDatabaseName();
    }
    else
    {
            if (Const.isEmpty(connection.getDatabasePort()))
            {
                return getNativeJdbcPre() + connection.getHostname()+"/"+connection.getDatabaseName();
            }
            else
            {
                return getNativeJdbcPre() + connection.getHostname()+":"+connection.getDatabasePort()+"/"+connection.getDatabaseName();
            }
    }
  }

    
  public String getNativeJdbcPre() {
    return "jdbc:Cache://";
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
	public String getAddColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		return "ALTER TABLE "+tablename+" ADD COLUMN ( "+getFieldDefinition(v, tk, pk, use_autoinc, true, false)+" ) ";
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
	public String getDropColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		return "ALTER TABLE "+tablename+" DROP COLUMN "+v.getName()+Const.CR;
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
	public String getModifyColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		return "ALTER TABLE "+tablename+" ALTER COLUMN "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
	}

	public String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr)
	{
		String retval="";
		
		String fieldname = v.getName();
		int    length    = v.getLength();
		int    precision = v.getPrecision();
		
		if (add_fieldname) retval+=fieldname+" ";
		
		int type         = v.getType();
		switch(type)
		{
		case ValueMetaInterface.TYPE_DATE   : retval+="TIMESTAMP"; break;
		case ValueMetaInterface.TYPE_BOOLEAN: retval+="CHAR(1)"; break;
		case ValueMetaInterface.TYPE_NUMBER :
		case ValueMetaInterface.TYPE_INTEGER: 
      case ValueMetaInterface.TYPE_BIGNUMBER: 
			if (fieldname.equalsIgnoreCase(tk)) // Technical & primary key : see at bottom
			{
				retval+="DECIMAL";
			} 
			else
			{
				if ( length<0 || precision<0 )
				{
					retval+="DOUBLE";
				}
				else if ( precision>0 || length>9)
				{
					retval+="DECIMAL("+length;
					if (precision>0)
					{
						retval+=", "+precision;
					}
					retval+=")";
				}
				else // Precision == 0 && length<=9
				{
					retval+="INT"; 
				}
			}
			break;
		case ValueMetaInterface.TYPE_STRING: // CLOBs are just VARCHAR in the Cache database: can be very large!
			retval+="VARCHAR";				
			if (length>0)
			{
				retval+="("+length+")";
			}
			break;
		default:
			retval+=" UNKNOWN";
			break;
		}
		
		if (add_cr) retval+=Const.CR;
		
		return retval;
	}

  public String[] getUsedLibraries()
  {
      return new String[] { "CacheDB.jar" };    
  }
}
