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
 * Contains Generic Database Connection information through static final members 
 * 
 * @author Matt
 * @since  11-mrt-2005
 */
public class DerbyDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
{
    /**
	 * Construct a new database connection.
	 * 
	 */
	public DerbyDatabaseMeta(String name, String access, String host, String db, String port, String user, String pass)
	{
		super(name, access, host, db, port, user, pass);
	}
	
	public DerbyDatabaseMeta()
	{
	}
	
	public String getDatabaseTypeDesc()
	{
		return "DERBY";
	}

	public String getDatabaseTypeDescLong()
	{
		return "Apache Derby";
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_DERBY;
	}
		
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_JNDI };
	}
	
	/**
	 * @see DatabaseInterface#getNotFoundTK(boolean)
	 */
	public int getNotFoundTK(boolean use_autoinc)
	{
		if ( supportsAutoInc() && use_autoinc)
		{
			return 0;
		}
		return super.getNotFoundTK(use_autoinc);
	}
	
	public String getDriverClass()
	{
        if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE)
        {
            return "org.apache.derby.jdbc.ClientDriver";            
        }
        else
        {
            return "sun.jdbc.odbc.JdbcOdbcDriver"; // ODBC bridge
        }

	}
	
    public String getURL(String hostname, String port, String databaseName)
    {
        if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE)
        {
            if (!Const.isEmpty(hostname))
            {
                String url="jdbc:derby://"+hostname;
                if (!Const.isEmpty(port)) url+=":"+port;
                url+="/"+databaseName;
                return url;
            }
            else // Simple format: jdbc:derby:<dbname>
            {
                return "jdbc:derby:"+databaseName;
            }
        }
        else
        {
            return "jdbc:odbc:"+databaseName;
        }
	}

	/**
	 * Checks whether or not the command setFetchSize() is supported by the JDBC driver...
	 * @return true is setFetchSize() is supported!
	 */
	public boolean isFetchSizeSupported()
	{
		return true;
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
		return true;
	}
	
	/**
	 * @param tableName The table to be truncated.
	 * @return The SQL statement to truncate a table: remove all rows from it without a transaction
	 */
	public String getTruncateTableStatement(String tableName)
	{
	    return "DELETE FROM "+tableName;
	}


	/**
	 * Generates the SQL statement to add a column to the specified table
	 * For this generic type, i set it to the most common possibility.
	 * 
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
		return "ALTER TABLE "+tablename+" ALTER "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
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
        case ValueMetaInterface.TYPE_NUMBER    :
        case ValueMetaInterface.TYPE_INTEGER   : 
        case ValueMetaInterface.TYPE_BIGNUMBER : 
            if (fieldname.equalsIgnoreCase(tk) || // Technical key
                fieldname.equalsIgnoreCase(pk)    // Primary key
                ) 
            {
                if (use_autoinc)
                {
                    retval+="BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1)";
                }
                else
                {
                    retval+="BIGINT NOT NULL PRIMARY KEY";
                }
            } 
            else
            {
                // Integer values...
                if (precision==0)
                {
                    if (length>9)
                    {
                        retval+="BIGINT";
                    }
                    else
                    {
                        if (length>4)
                        {
                            retval+="INTEGER";
                        }
                        else
                        {
                            retval+="SMALLINT";
                        }
                    }
                }
                // Floating point values...
                else  
                {
                    if (length>18)
                    {
                        retval+="DECIMAL("+length;
                        if (precision>0) retval+=", "+precision;
                        retval+=")";
                    }
                    else
                    {
                        retval+="FLOAT";
                    }
                }
            }
            break;
		case ValueMetaInterface.TYPE_STRING:
			if (length>=DatabaseMeta.CLOB_LENGTH || length>32700)
			{
				retval+="CLOB";
			}
			else
			{
				retval+="VARCHAR";
				if (length>0)
				{
					retval+="("+length;
				}
				else
				{
					retval+="("; // Maybe use some default DB String length?
				}
				retval+=")";
			}
			break;
        case ValueMetaInterface.TYPE_BINARY:
            retval+="BLOB";
            break;
		default:
			retval+="UNKNOWN";
			break;
		}
		
		if (add_cr) retval+=Const.CR;
		
		return retval;
	}

    public String[] getUsedLibraries()
    {
        return new String[] { "derbyclient.jar" };
    }
    
    public int getDefaultDatabasePort()
    {
        return 1527;
    }
    
    public boolean supportsGetBlob()
    {
        return false;
    }
    
    public String getExtraOptionsHelpText()
    {
        return "http://db.apache.org/derby/papers/DerbyClientSpec.html";
    }
}
