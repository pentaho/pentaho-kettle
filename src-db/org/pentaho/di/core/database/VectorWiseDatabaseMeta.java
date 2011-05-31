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
 * Contains Computer Associates Ingres specific information through static final members 
 * 
 * @author Matt
 * @since  11-mrt-2005
 */

public class VectorWiseDatabaseMeta extends IngresDatabaseMeta implements DatabaseInterface
{
    
  public String getURL(String hostname, String port, String databaseName) {
    if (getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC) {
      return "jdbc:odbc:" + databaseName;
    } else {
      if (Const.isEmpty(port)) {
        return "jdbc:ingres://" + hostname + ":VW7/" + databaseName;
      } else {
        return "jdbc:ingres://" + hostname + ":" + port + "/" + databaseName;
      }
    }
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
	public String getModifyColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
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
	public String getDropColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		return "ALTER TABLE "+tablename+" DROP COLUMN "+v.getName()+Const.CR;
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
			if (fieldname.equalsIgnoreCase(tk) ||  // Technical key
			    fieldname.equalsIgnoreCase(pk)     // Primary key
			    ) 
			{
				if (use_autoinc)
				{
					retval+="GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1";
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
                            if (length>2)
                            {
                                retval+="SMALLINT";
                            }
                            else
                            {
                                retval+="INTEGER1";
                            }
                        }
                    }
                }
                else
                {
                    retval+="FLOAT8";
                }
			}
			break;
		case ValueMetaInterface.TYPE_STRING:
			//	Maybe use some default DB String length in case length<=0
			if (length>0)
			{
				retval+="VARCHAR("+length+")";	
			}
			else
			{
				retval+="VARCHAR(9999)";
			} 
			break;
		default:
			retval+=" UNKNOWN";
			break;
		}
		
		if (add_cr) retval+=Const.CR;
		
		return retval;
	}
	
    /**
     * @param tableName The table to be truncated.
     * @return The SQL statement to truncate a table: remove all rows from it without a transaction
     */
    public String getTruncateTableStatement(String tableName)
    {
        return "DELETE FROM "+tableName;
    }

    public String[] getUsedLibraries()
    {
        return new String[] { "iijdbc.jar" };
    }

    @Override
    public boolean supportsGetBlob() {
    	return false;
    }

}
