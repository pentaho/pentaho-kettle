/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/

package org.pentaho.di.core.database;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Contains Informix specific information through static final members 
 * 
 * @author Matt
 * @since  11-mrt-2005
 */

public class InformixDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
{
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_JNDI };
	}
	
	public int getDefaultDatabasePort()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE) return 1526;
		return -1;
	}

	/**
	 * @see DatabaseInterface#getNotFoundTK(boolean)
	 */
	public int getNotFoundTK(boolean use_autoinc)
	{
		if ( supportsAutoInc() && use_autoinc)
		{
			return 1;
		}
		return super.getNotFoundTK(use_autoinc);
	}

	public String getDriverClass()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "sun.jdbc.odbc.JdbcOdbcDriver";
		}
		else
		{
			return "com.informix.jdbc.IfxDriver";
		}
	}
	
    public String getURL(String hostname, String port, String databaseName)
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "jdbc:odbc:"+databaseName;
		}
		else
		{
			return "jdbc:informix-sqli://"+hostname+":"+port+"/"+databaseName+":INFORMIXSERVER="+getServername();
		}
	}

	/**
	 * Indicates the need to insert a placeholder (0) for auto increment fields.
	 * @return true if we need a placeholder for auto increment fields in insert statements.
	 */
	public boolean needsPlaceHolder()
	{
		return true;
	}
    
    public boolean needsToLockAllTables()
    {
        return false;
    }
    
    public String getSQLQueryFields(String tableName)
    {
        return "SELECT FIRST 1 * FROM "+tableName;
    }
    
    public String getSQLTableExists(String tablename)
    {
        return getSQLQueryFields(tablename);
    }
    public String getSQLColumnExists(String columnname, String tablename)
    {
        return  getSQLQueryColumnFields(columnname, tablename);
    }
    public String getSQLQueryColumnFields(String columnname, String tableName)
    {
        return "SELECT FIRST 1 " + columnname + " FROM "+tableName;
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
		case ValueMetaInterface.TYPE_DATE   : retval+="DATETIME YEAR to FRACTION"; break;
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
			if (fieldname.equalsIgnoreCase(tk) ||   // Technical key
			    fieldname.equalsIgnoreCase(pk)      // Primary key
			    )
			{
				if (use_autoinc)
				{
					retval+="SERIAL8";
				}
				else
				{
					retval+="INTEGER PRIMARY KEY";
				}
			} 
			else
			{
				if ( (length<0 && precision<0) || precision>0 || length>9)
				{
					retval+="FLOAT";
				}
				else // Precision == 0 && length<=9
				{
					retval+="INTEGER"; 
				}
			}
			break;
		case ValueMetaInterface.TYPE_STRING:
			if (length>=DatabaseMeta.CLOB_LENGTH)
			{
				retval+="CLOB";
			}
			else
			{
				if (length<256)
				{
					retval+="VARCHAR"; 
					if (length>0)
					{
						retval+="("+length+")";
					}
				}
				else
				{
					if (length<32768)
					{
						retval+="LVARCHAR"; 
					}
					else
					{
						retval+="TEXT";
					}
				}
			}
			break;
		default:
			retval+=" UNKNOWN";
			break;
		}
		
		if (add_cr) retval+=Const.CR;
		
		return retval;
	}
    
    public String getSQLLockTables(String tableNames[])
    {
        String sql="";
        for (int i=0;i<tableNames.length;i++)
        {
            sql+="LOCK TABLE "+tableNames[i]+" IN EXCLUSIVE MODE;"+Const.CR;
        }
        return sql;
    }

    public String getSQLUnlockTables(String tableNames[])
    {
        return null;
        /*
        String sql="";
        for (int i=0;i<tableNames.length;i++)
        {
            sql+="UNLOCK TABLE "+tableNames[i]+";"+Const.CR;
        }
        return sql;
        */
    }

    public String[] getUsedLibraries()
    {
        return new String[] { "ifxjdbc.jar" };
    }

	/**
	 * Get the SQL to insert a new empty unknown record in a dimension.
	 * @param schemaTable the schema-table name to insert into
	 * @param keyField The key field
	 * @param versionField the version field
	 * @return the SQL to insert the unknown record into the SCD.
	 */
	public String getSQLInsertAutoIncUnknownDimensionRow(String schemaTable, String keyField, String versionField) {
		return "insert into "+schemaTable+"("+keyField+", "+versionField+") values (1, 1)";		
	}

}
