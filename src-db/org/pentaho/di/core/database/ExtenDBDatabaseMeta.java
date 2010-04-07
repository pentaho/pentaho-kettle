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
 * Contains ExtenDB specific information through static final members 
 * 
 * @author amart
 * @since  11-Apr-2006
 */

public class ExtenDBDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
{
    private static final String [] RESERVED_WORDS =
    {
        "AFTER", "BINARY", "BOOLEAN", "DATABASES", "DBA", "ESTIMATE", "MODIFY",
        "NODE", "NODES", "OWNER", "PARENT", "PARTITION", "PARTITIONING", 
        "PASSWORD", "PERCENT", "PUBLIC", "RENAME", "REPLICATED", "RESOURCE",
        "SAMPLE", "SERIAL", "SHOW", "STANDARD", "STAT", "STATISTICS", "TABLES",
        "TEMP", "TRAN", "UNSIGNED", "ZEROFILL"
    };
    
    /**
     * @see DatabaseInterface#getAccessTypeList()
     */
    public int[] getAccessTypeList()
    {
        return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_JNDI };
    }

    public int getDefaultDatabasePort()
    {
        if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE) return 6453;
        return -1;
    }

    /**
     * @see DatabaseInterface#getDriverClass()
     */
    public String getDriverClass()
    {
        if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
        {
            return "sun.jdbc.odbc.JdbcOdbcDriver";
        }
        else
        {
            return "com.extendb.connect.XDBDriver";
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
            return "jdbc:xdb://"+hostname+":"+port+"/"+databaseName;
        }
    }

    /**
     * @return an array of reserved words for the database type...
     */
    public String[] getReservedWords()
    {
        return RESERVED_WORDS;
    }
    
    /**
     * Checks whether or not the command setFetchSize() is supported by the JDBC driver...
     * @return true is setFetchSize() is supported!
     */
    public boolean isFetchSizeSupported()
    {
        return false;
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
        return "ALTER TABLE "+tablename+" DROP "+v.getName()+Const.CR;
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
        String retval="";
        retval+="ALTER TABLE "+tablename+" DROP "+v.getName()+Const.CR+";"+Const.CR;
        retval+="ALTER TABLE "+tablename+" ADD "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
        return retval;
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
            if (fieldname.equalsIgnoreCase(tk) || // Technical key
                fieldname.equalsIgnoreCase(pk)    // Primary key
                ) 
            {
                if (length > 9)
                {
                    retval+="BIGSERIAL";
                }
                else
                {
                    retval+="SERIAL";
                }
            } 
            else
            {
                if (length>0)
                {
                    if (precision>0 || length>18)
                    {
                        retval+="NUMERIC("+length+", "+precision+")";
                    }
                    else
                    {
                        if (length>9)
                        {
                            retval+="BIGINT";
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
                    retval+="DOUBLE PRECISION";
                }
            }
            break;
        case ValueMetaInterface.TYPE_STRING:
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
        return new String[] { "xdbjdbc.jar" };
    }

}
