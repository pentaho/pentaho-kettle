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
 * Contains SAP DB specific information through static final members 
 * 
 * @author Matt
 * @since  30-06-2005
 */

public class SAPDBDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
{
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_JNDI };
	}
	
	public int getDefaultDatabasePort()
	{
		// if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE) return 3050;
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
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "sun.jdbc.odbc.JdbcOdbcDriver";
		}
		else
		{
			return "com.sap.dbtech.jdbc.DriverSapDB";
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
		    return "jdbc:sapdb://"+hostname+"/"+databaseName;
		}
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
		return "ALTER TABLE "+tablename+" ALTER COLUMN "+v.getName()+" TYPE "+getFieldDefinition(v, tk, pk, use_autoinc, false, false);
	}

	public String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr)
	{
		String retval="";
		
		String fieldname = v.getName();
		int    length    = v.getLength();
		int    precision = v.getPrecision();
		
		if (add_fieldname) 
		{
			if (Const.indexOfString(fieldname, getReservedWords())>=0)
			{
				retval+=getStartQuote()+fieldname+getEndQuote();
			}
			else
			{
				retval+=fieldname+" ";
			}
		}
		
		int type         = v.getType();
		switch(type)
		{
		case ValueMetaInterface.TYPE_DATE   : retval+="TIMESTAMP"; break;
		case ValueMetaInterface.TYPE_BOOLEAN: retval+="CHAR(1)"; break;
		case ValueMetaInterface.TYPE_NUMBER : 
		case ValueMetaInterface.TYPE_INTEGER: 
        case ValueMetaInterface.TYPE_BIGNUMBER: 
			if (fieldname.equalsIgnoreCase(tk) || // Technical key
			    fieldname.equalsIgnoreCase(pk)    // Primary key
			    ) 
			{
				retval+="BIGINT NOT NULL PRIMARY KEY";
			} 
			else
			{
				if (length>0)
				{
					if (precision>0 || length>18)
					{
						retval+="DECIMAL("+length;
						if (precision>0)
						{
							retval+=", "+precision;
						}
						retval+=")";
					}
					else
					{
						if (length>9)
						{
							retval+="INT64";
						}
						else
						{
							if (length<5)
							{
								retval+="SMALLINT";
							}
							else
							{
								retval+="INTEGER";
							}
						}
					}
					
				}
				else
				{
					retval+="DOUBLE";
				}
			}
			break;
		case ValueMetaInterface.TYPE_STRING:
			if (length<32720)
			{
				retval+="VARCHAR"; 
				if (length>0)
				{
					retval+="("+length+")";
				}
				else
				{
					retval+="(8000)"; // Maybe use some default DB String length?
				}
			}
			else
			{
				retval+="BLOB SUB_TYPE TEXT";
			}
			break;
		default:
			retval+=" UNKNOWN";
			break;
		}
		
		if (add_cr) retval+=Const.CR;
		
		return retval;
	}
	
	public String [] getReservedWords()
	{
		return new String[] {
		        "ABS", "ABSOLUTE", "ACOS", "ADDDATE", "ADDTIME", "ALL", "ALPHA", "ALTER", "ANY",
		        "ASCII", "ASIN", "ATAN", "ATAN2", "AVG", "BINARY", "BIT", "BOOLEAN", "BYTE",
		        "CASE", "CEIL", "CEILING", "CHAR", "CHARACTER", "CHECK", "CHR", "COLUMN", "CONCAT",
		        "CONSTRAINT", "COS", "COSH", "COT", "COUNT", "CROSS", "CURDATE", "CURRENT", "CURTIME",
		        "DATABASE", "DATE", "DATEDIFF", "DAY", "DAYNAME", "DAYOFMONTH", "DAYOFWEEK", "DAYOFYEAR", "DEC",
		        "DECIMAL", "DECODE", "DEFAULT", "DEGREES", "DELETE", "DIGITS", "DISTINCT", "DOUBLE", "EXCEPT",
		        "EXISTS", "EXP", "EXPAND", "FIRST", "FIXED", "FLOAT", "FLOOR", "FOR", "FROM",
		        "FULL", "GET_OBJECTNAME", "GET_SCHEMA", "GRAPHIC", "GREATEST", "GROUP", "HAVING", "HEX", "HEXTORAW",
		        "HOUR", "IFNULL", "IGNORE", "INDEX", "INITCAP", "INNER", "INSERT", "INT", "INTEGER",
		        "INTERNAL", "INTERSECT", "INTO", "JOIN", "KEY", "LAST", "LCASE", "LEAST", "LEFT",
		        "LENGTH", "LFILL", "LIST", "LN", "LOCATE", "LOG", "LOG10", "LONG", "LONGFILE",
		        "LOWER", "LPAD", "LTRIM", "MAKEDATE", "MAKETIME", "MAPCHAR", "MAX", "MBCS", "MICROSECOND",
		        "MIN", "MINUTE", "MOD", "MONTH", "MONTHNAME", "NATURAL", "NCHAR", "NEXT", "NO",
		        "NOROUND", "NOT", "NOW", "NULL", "NUM", "NUMERIC", "OBJECT", "OF", "ON",
		        "ORDER", "PACKED", "PI", "POWER", "PREV", "PRIMARY", "RADIANS", "REAL", "REJECT",
		        "RELATIVE", "REPLACE", "RFILL", "RIGHT", "ROUND", "ROWID", "ROWNO", "RPAD", "RTRIM",
		        "SECOND", "SELECT", "SELUPD", "SERIAL", "SET", "SHOW", "SIGN", "SIN", "SINH",
		        "SMALLINT", "SOME", "SOUNDEX", "SPACE", "SQRT", "STAMP", "STATISTICS", "STDDEV", "SUBDATE",
		        "SUBSTR", "SUBSTRING", "SUBTIME", "SUM", "SYSDBA", "TABLE", "TAN", "TANH", "TIME",
		        "TIMEDIFF", "TIMESTAMP", "TIMEZONE", "TO", "TOIDENTIFIER", "TRANSACTION", "TRANSLATE", "TRIM", "TRUNC",
		        "TRUNCATE", "UCASE", "UID", "UNICODE", "UNION", "UPDATE", "UPPER", "USER", "USERGROUP",
		        "USING", "UTCDATE", "UTCDIFF", "VALUE", "VALUES", "VARCHAR", "VARGRAPHIC", "VARIANCE", "WEEK",
		        "WEEKOFYEAR", "WHEN", "WHERE", "WITH", "YEAR", "ZONED"		
		 };
	}

    public String[] getUsedLibraries()
    {
        return new String[] { "sapdbc.jar" };
    }

}
