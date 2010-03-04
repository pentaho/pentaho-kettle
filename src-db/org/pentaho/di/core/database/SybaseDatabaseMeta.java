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
import org.pentaho.di.core.plugins.DatabaseMetaPlugin;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Contains Sybase specific information through static final members 
 * 
 * @author Matt
 * @since  11-mrt-2005
 */

@DatabaseMetaPlugin( type="SYBASE", typeDescription="Sybase" )
public class SybaseDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
{
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_JNDI };
	}
	
	public int getDefaultDatabasePort()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE) return 5001;
		return -1;
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

	public String getDriverClass()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "sun.jdbc.odbc.JdbcOdbcDriver";
		}
		else
		{
			return "net.sourceforge.jtds.jdbc.Driver";
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
		    // jdbc:jtds:<server_type>://<server>[:<port>][/<database>][;<property>=<value>[;...]]
			return "jdbc:jtds:sybase://"+hostname+":"+port+"/"+databaseName;
		}
	}


	/**
	 * @see org.pentaho.di.core.database.DatabaseInterface#getSchemaTableCombination(java.lang.String, java.lang.String)
	 */
	public String getSchemaTableCombination(String schema_name, String table_part)
	{
		return table_part;
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
}
