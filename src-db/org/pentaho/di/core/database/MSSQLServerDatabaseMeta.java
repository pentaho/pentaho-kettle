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

import java.sql.ResultSet;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.DatabaseMetaPlugin;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Contains MySQL specific information through static final members 
 * 
 * @author Matt
 * @since  11-mrt-2005
 */

@DatabaseMetaPlugin( type="MSSQL", typeDescription="MS SQL Server" )
public class MSSQLServerDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
{
	public boolean supportsCatalogs() 
	{
		return false;
	}
	
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_JNDI };
	}
	
	public int getDefaultDatabasePort()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE) return 1433;
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
			return "jdbc:jtds:sqlserver://"+hostname+":"+port+"/"+databaseName;
		}
	}
	
	public String getSchemaTableCombination(String schema_name, String table_part)
	{
		// Something special for MSSQL
		//
		if (isUsingDoubleDecimalAsSchemaTableSeparator()) 
		{
			return schema_name+".."+table_part;
		}
		else
		{
			return schema_name+"."+table_part;
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
    
    public String getSQLQueryFields(String tableName)
    {
        return "SELECT TOP 1 * FROM "+tableName;
    }
    
    public String getSQLTableExists(String tablename)
    {
        return  getSQLQueryFields(tablename);
    }
    
    public String getSQLColumnExists(String columnname, String tablename)
    {
        return  getSQLQueryColumnFields(columnname, tablename);
    }
    public String getSQLQueryColumnFields(String columnname, String tableName)
    {
        return "SELECT TOP 1 " + columnname + " FROM "+tableName;
    }

    /**
     * @param tableNames The names of the tables to lock
     * @return The SQL command to lock database tables for write purposes.
     *         null is returned in case locking is not supported on the target database.
     *         null is the default value
     */
    public String getSQLLockTables(String tableNames[])
    {
        StringBuffer sql=new StringBuffer(128);
        for (int i=0;i<tableNames.length;i++)
        {
            sql.append("SELECT top 0 * FROM ").append(tableNames[i]).append(" WITH (TABLOCKX, HOLDLOCK);").append(Const.CR);
        }
        return sql.toString();
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
		case ValueMetaInterface.TYPE_DATE   : retval+="DATETIME"; break;
		case ValueMetaInterface.TYPE_BOOLEAN:
			if (supportsBooleanDataType()) {
				retval+="BIT"; 
			} else {
				retval+="CHAR(1)";
			}
			break;
		case ValueMetaInterface.TYPE_NUMBER :
		case ValueMetaInterface.TYPE_INTEGER: 
        case ValueMetaInterface.TYPE_BIGNUMBER: 
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
					retval+="BIGINT PRIMARY KEY";
				}
			} 
			else
			{
                if (precision==0)
                {
                    if (length>18)
                    {
                        retval+="DECIMAL("+length+",0)";
                    }
                    else
                    {
                        if (length>9)
                        {
                            retval+="BIGINT";
                        }
                        else
                        {
                            retval+="INT";
                        }
                    }
                }
                else
                {
                    if (precision>0)
                    {
                        if (length>0)
                        {
                            retval+="DECIMAL("+length+","+precision+")";
                        }
                    }
                    else
                    {
                        retval+="FLOAT(53)";
                    }
                }
			}
			break;
		case ValueMetaInterface.TYPE_STRING:
			if (length<8000)
			{
				//	Maybe use some default DB String length in case length<=0
				if (length>0)
				{
					retval+="VARCHAR("+length+")";	
				}
				else
				{
					retval+="VARCHAR(100)";
				} 
			}
			else
			{
				retval+="TEXT"; // Up to 2bilion characters.
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
	 * @see org.pentaho.di.core.database.DatabaseInterface#getSQLListOfProcedures()
	 */
	public String getSQLListOfProcedures()
	{
		return "select o.name from sysobjects o, sysusers u where  xtype in ( 'FN', 'P' ) and o.uid = u.uid order by o.name";
	}
	/* (non-Javadoc)
	 * @see org.pentaho.di.core.database.DatabaseInterface#getReservedWords()
	 */
	public String[] getReservedWords()
	{
		return new String[]
		{
			/* Transact-SQL Reference: Reserved Keywords
			 * Includes future keywords: could be reserved in future releases of SQL Server as new features are
			 * implemented.
			 * REMARK: When SET QUOTED_IDENTIFIER is ON (default), identifiers can be delimited by double quotation 
			 * marks, and literals must be delimited by single quotation marks. 
			 * When SET QUOTED_IDENTIFIER is OFF, identifiers cannot be quoted and 
			 * must follow all Transact-SQL rules for identifiers.
			 */
			"ABSOLUTE", "ACTION", "ADD", "ADMIN", "AFTER", "AGGREGATE", "ALIAS", "ALL", "ALLOCATE", "ALTER", "AND", 
			"ANY", "ARE", "ARRAY", "AS", "ASC", "ASSERTION", "AT", "AUTHORIZATION", 
			"BACKUP", "BEFORE", "BEGIN", "BETWEEN", "BINARY", "BIT", "BLOB", "BOOLEAN", "BOTH", "BREADTH", "BREAK", 
			"BROWSE", "BULK", "BY", 
			"CALL", "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", "CHAR", "CHARACTER", "CHECK", "CHECKPOINT", 
			"CLASS", "CLOB", "CLOSE", "CLUSTERED", "COALESCE", "COLLATE", "COLLATION", "COLUMN", "COMMIT", 
			"COMPLETION", "COMPUTE", "CONNECT", "CONNECTION", "CONSTRAINT", "CONSTRAINTS", "CONSTRUCTOR", 
			"CONTAINS", "CONTAINSTABLE", "CONTINUE", "CONVERT", "CORRESPONDING", "CREATE", "CROSS", "CUBE", 
			"CURRENT", "CURRENT_DATE", "CURRENT_PATH", "CURRENT_ROLE", "CURRENT_TIME", "CURRENT_TIMESTAMP", 
			"CURRENT_USER", "CURSOR", "CYCLE", 
			"DATA", "DATABASE", "DATE", "DAY", "DBCC", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", 
			"DEFERRABLE", "DEFERRED", "DELETE", "DENY", "DEPTH", "DEREF", "DESC", "DESCRIBE", "DESCRIPTOR", 
			"DESTROY", "DESTRUCTOR", "DETERMINISTIC", "DIAGNOSTICS", "DICTIONARY", "DISCONNECT", "DISK", "DISTINCT", 
			"DISTRIBUTED", "DOMAIN", "DOUBLE", "DROP", "DUMMY", "DUMP", "DYNAMIC", 
			"EACH", "ELSE", "END", "END-EXEC", "EQUALS", "ERRLVL", "ESCAPE", "EVERY", "EXCEPT", "EXCEPTION", "EXEC", 
			"EXECUTE", "EXISTS", "EXIT", "EXTERNAL", 
			"FALSE", "FETCH", "FILE", "FILLFACTOR", "FIRST", "FLOAT", "FOR", "FOREIGN", "FOUND", "FREE", "FREETEXT", 
			"FREETEXTTABLE", "FROM", "FULL", "FUNCTION", 
			"GENERAL", "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GROUP", "GROUPING", 
			"HAVING", "HOLDLOCK", "HOST", "HOUR", 
			"IDENTITY", "IDENTITY_INSERT", "IDENTITYCOL", "IF", "IGNORE", "IMMEDIATE", "IN", "INDEX", "INDICATOR", 
			"INITIALIZE", "INITIALLY", "INNER", "INOUT", "INPUT", "INSERT", "INT", "INTEGER", "INTERSECT", "INTERVAL", 
			"INTO", "IS", "ISOLATION", "ITERATE", 
			"JOIN", 
			"KEY", "KILL", 
			"LANGUAGE", "LARGE", "LAST", "LATERAL", "LEADING", "LEFT", "LESS", "LEVEL", "LIKE", "LIMIT", "LINENO", 
			"LOAD", "LOCAL", "LOCALTIME", "LOCALTIMESTAMP", "LOCATOR", 
			"MAP", "MATCH", "MINUTE", "MODIFIES", "MODIFY", "MODULE", "MONTH", 
			"NAMES", "NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NEW", "NEXT", "NO", "NOCHECK", "NONCLUSTERED", "NONE", 
			"NOT", "NULL", "NULLIF", "NUMERIC", 
			"OBJECT", "OF", "OFF", "OFFSETS", "OLD", "ON", "ONLY", "OPEN", "OPENDATASOURCE", "OPENQUERY", "OPENROWSET", 
			"OPENXML", "OPERATION", "OPTION", "OR", "ORDER", "ORDINALITY", "OUT", "OUTER", "OUTPUT", "OVER", 
			"PAD", "PARAMETER", "PARAMETERS", "PARTIAL", "PATH", "PERCENT", "PLAN", "POSTFIX", "PRECISION", "PREFIX", 
			"PREORDER", "PREPARE", "PRESERVE", "PRIMARY", "PRINT", "PRIOR", "PRIVILEGES", "PROC", "PROCEDURE", "PUBLIC", 
			"RAISERROR", "READ", "READS", "READTEXT", "REAL", "RECONFIGURE", "RECURSIVE", "REF", "REFERENCES", "REFERENCING", 
			"RELATIVE", "REPLICATION", "RESTORE", "RESTRICT", "RESULT", "RETURN", "RETURNS", "REVOKE", "RIGHT", "ROLE", 
			"ROLLBACK", "ROLLUP", "ROUTINE", "ROW", "ROWCOUNT", "ROWGUIDCOL", "ROWS", "RULE", 
			"SAVE", "SAVEPOINT", "SCHEMA", "SCOPE", "SCROLL", "SEARCH", "SECOND", "SECTION", "SELECT", "SEQUENCE", "SESSION", 
			"SESSION_USER", "SET", "SETS", "SETUSER", "SHUTDOWN", "SIZE", "SMALLINT", "SOME", "SPACE", "SPECIFIC", 
			"SPECIFICTYPE", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "START", "STATE", "STATEMENT", "STATIC", 
			"STATISTICS", "STRUCTURE", "SYSTEM_USER", 
			"TABLE", "TEMPORARY", "TERMINATE", "TEXTSIZE", "THAN", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", 
			"TIMEZONE_MINUTE", "TO", "TOP", "TRAILING", "TRAN", "TRANSACTION", "TRANSLATION", "TREAT", "TRIGGER", "TRUE", 
			"TRUNCATE", "TSEQUAL", 
			"UNDER", "UNION", "UNIQUE", "UNKNOWN", "UNNEST", "UPDATE", "UPDATETEXT", "USAGE", "USE", "USER", "USING", 
			"VALUE", "VALUES", "VARCHAR", "VARIABLE", "VARYING", "VIEW", 
			"WAITFOR", "WHEN", "WHENEVER", "WHERE", "WHILE", "WITH", "WITHOUT", "WORK", "WRITE", "WRITETEXT", 
			"YEAR", 
			"ZONE"				
        };
	}

    public String[] getUsedLibraries()
    {
        return new String[] { "jtds-1.2.jar" };
    }
    
    public String getExtraOptionsHelpText()
    {
        return "http://jtds.sourceforge.net/faq.html#urlFormat";
    }
    
    /**
     * Verifies on the specified database connection if an index exists on the fields with the specified name.
     * 
     * @param database a connected database
     * @param schemaName
     * @param tableName
     * @param idxFields
     * @return true if the index exists, false if it doesn't.
     * @throws KettleException
     */
	public boolean checkIndexExists(Database database, String schemaName, String tableName, String[] idx_fields) throws KettleDatabaseException  {
		
        String tablename = database.getDatabaseMeta().getQuotedSchemaTableCombination(schemaName, tableName);

		boolean exists[] = new boolean[idx_fields.length];
		for (int i=0;i<exists.length;i++) exists[i]=false;
		
		try
		{
			//
			// Get the info from the data dictionary...
			//
			StringBuffer sql = new StringBuffer(128);
			sql.append("select i.name table_name, c.name column_name ");
			sql.append("from     sysindexes i, sysindexkeys k, syscolumns c ");
			sql.append("where    i.name = '"+tablename+"' ");
			sql.append("AND      i.id = k.id ");
			sql.append("AND      i.id = c.id ");
			sql.append("AND      k.colid = c.colid ");
			
			ResultSet res = null;
			try 
			{
				res = database.openQuery(sql.toString());
				if (res!=null)
				{
					Object[] row = database.getRow(res);
					while (row!=null)
					{
						String column = database.getReturnRowMeta().getString(row, "column_name", "");
						int idx = Const.indexOfString(column, idx_fields);
						if (idx>=0) exists[idx]=true;
						
						row = database.getRow(res);
					}							
				}
				else
				{
					return false;
				}
			}
			finally
			{
				if ( res != null ) database.closeQuery(res);
			}
			
	        // See if all the fields are indexed...
	        boolean all=true;
	        for (int i=0;i<exists.length && all;i++) if (!exists[i]) all=false;
	        
			return all;
		}
		catch(Exception e)
		{
			throw new KettleDatabaseException("Unable to determine if indexes exists on table ["+tablename+"]", e);
		}
	}

	@Override
	public String getSQLListOfSchemas() {
		return "select name from sys.schemas";
	}
	
	@Override
	public boolean supportsSchemas() {
		return true;
	}
}
