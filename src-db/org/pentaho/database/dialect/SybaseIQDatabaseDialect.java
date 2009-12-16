package org.pentaho.database.dialect;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaInterface;

public class SybaseIQDatabaseDialect extends AbstractDatabaseDialect {

  public static final IDatabaseType DBTYPE = 
    new DatabaseType(
        "SybaseIQ",
        "SYBASEIQ",
        DatabaseAccessType.getList(
            DatabaseAccessType.NATIVE, 
            DatabaseAccessType.ODBC, 
            DatabaseAccessType.JNDI
        ), 
        5001, 
        null
    );
  
  public IDatabaseType getDatabaseType() {
    return DBTYPE;
  }

  @Override
  public String getNativeDriver() {
    return "com.sybase.jdbc3.jdbc.SybDriver";
  }
  
  @Override
  public String getNativeJdbcPre() {
    return "jdbc:sybase:Tds://";
  }
  
  @Override
  public String getURL(IDatabaseConnection connection)
    {
    if (connection.getAccessType()==DatabaseAccessType.ODBC) {
      return "jdbc:odbc:"+connection.getDatabaseName();
    }
    else
    {
      return getNativeJdbcPre() + connection.getHostname()+":"+connection.getDatabasePort()+"/"+connection.getDatabaseName();
    }
  }
  
	/**
	 * @see org.pentaho.di.core.database.DatabaseInterface#getNotFoundTK(boolean)
	 */
	public int getNotFoundTK(boolean use_autoinc)
	{
		if ( supportsAutoInc() && use_autoinc)
		{
			return 1;
		}
		return super.getNotFoundTK(use_autoinc);
	}

	/**
	 * @see org.pentaho.di.core.database.DatabaseInterface#getSchemaTableCombination(java.lang.String, java.lang.String)
	 */
	public String getSchemaTableCombination(String schema_name, String table_part)
	{
		return schema_name+"."+table_part;
	}
	
	/**
	 * @return true if Kettle can create a repository on this type of database.
	 */
	public boolean supportsRepository()
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
	public String getAddColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		return "ALTER TABLE "+tablename+" ADD "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
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
		return "ALTER TABLE "+tablename+" MODIFY "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
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
		case ValueMetaInterface.TYPE_DATE   : retval+="DATETIME NULL"; break;
		case ValueMetaInterface.TYPE_BOOLEAN:
			if (supportsBooleanDataType()) {
				retval+="BOOLEAN"; 
			} else {
				retval+="CHAR(1)";
			}
			break;
		case ValueMetaInterface.TYPE_NUMBER :
		case ValueMetaInterface.TYPE_INTEGER: 
        case ValueMetaInterface.TYPE_BIGNUMBER: 
			if (fieldname.equalsIgnoreCase(tk)  ||  // Technical key: auto increment field!
			    fieldname.equalsIgnoreCase(pk)      // Primary key 
			   ) 
			{
				if (use_autoinc)
				{
					retval+="INTEGER IDENTITY NOT NULL";
				}
				else
				{
					retval+="INTEGER NOT NULL PRIMARY KEY";
				}
			} 
			else
			{
				if ( precision!=0 || (precision==0 && length>9))
				{
					if (precision>0 && length>0)
					{
						retval+="DECIMAL("+length+", "+precision+") NULL";
					}
					else
					{
						retval+="DOUBLE PRECISION NULL";
					}
				}
				else // Precision == 0 && length<=9
				{
					if (length<3)
					{
						retval+="TINYINT NULL";
					}
					else
					if (length<5)
					{
						retval+="SMALLINT NULL";
					}
					else
					{
						retval+="INTEGER NULL";
					}
				}
			}
			break;
		case ValueMetaInterface.TYPE_STRING:
			if (length>=2048)
			{
				retval+="TEXT NULL";
			}
			else
			{
				retval+="VARCHAR"; 
				if (length>0)
				{
					retval+="("+length+")";
				}
				retval+=" NULL";
			}
			break;
		default:
			retval+=" UNKNOWN";
			break;
		}
		
		if (add_cr) retval+=Const.CR;
		
		return retval;
	}
    
    public String getExtraOptionsHelpText()
    {
        return "http://jtds.sourceforge.net/faq.html#urlFormat";
    }
    
    public String[] getUsedLibraries()
    {
        return new String[] { "jtds-1.2.jar" };
    }
    
	/**
	 * @return true if we need to supply the schema-name to getTables in order to get a correct list of items.
	 */
	public boolean useSchemaNameForTableList()
	{
		return true;
	}
	
	/**
	 * Returns the minimal SQL to launch in order to determine the layout of the resultset for a given database table
	 * Note: added WHERE clause in SQL (just to make sure in case the sql is exec'd it will not clatter the db)
	 * 
	 * @param tableName The name of the table to determine the layout for
	 * @return The SQL to launch.
	 */
	// 
	public String getSQLQueryFields(String tableName)
	{
	    return "SELECT * FROM "+tableName+" WHERE 1=2";
	}
}
