 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

 

package org.pentaho.di.core;

import org.pentaho.di.core.database.DatabaseMeta;


/**
 * This class contains all that is needed to execute an SQL statement in a database.
 * --> The Database connection
 * --> The SQL statement
 * 
 * @author Matt
 * @since 02-dec-2004
 *
 */
public class SQLStatement
{
	private String       stepname;
	private DatabaseMeta dbinfo;
	private String       sql;
	private String       error;
	
	/**
	 * Creates a new SQLStatement
	 * @param dbinfo The database connection
	 * @param sql The sql to execute on the connection
	 */
	public SQLStatement(String stepname, DatabaseMeta dbinfo, String sql)
	{
		this.stepname = stepname;
		this.dbinfo   = dbinfo;
		this.sql      = sql;
		this.error    = null;
	}

	/**
	 * Set the name of the step for which the SQL is intended
	 * @param stepname the name of the step for which the SQL is intended
	 */
	public void setStepname(String stepname)
	{
		this.stepname = stepname;
	}
	
	/**
	 * Return the name of the step for which the SQL is intended
	 * @return The name of the step for which the SQL is intended
	 */
	public String getStepname()
	{
		return stepname;
	}
	
	/**
	 * Sets the database connection for this SQL Statement
	 * @param dbinfo The databaseconnection
	 */
	public void setDatabase(DatabaseMeta dbinfo)
	{
		this.dbinfo = dbinfo;
	}
	
	/**
	 * Sets the SQL to execute for this SQL Statement.
	 * @param sql The sql to execute, without trailing ";" or anything else.
	 */
	public void setSQL(String sql)
	{
		this.sql = sql;
    }
	
	/**
	 * Get the database connection for this SQL Statement
	 * @return The database connection for this SQL Statement
	 */
	public DatabaseMeta getDatabase()
	{
		return dbinfo;
	}
	
	/**
	 * Get the SQL for this SQL Statement
	 * @return The SQL to execute for this SQL Statement
	 */
	public String getSQL()
	{
		return sql;
	}

	/**
	 * Sets the error that occurred when obtaining the SQL.
	 * @param error The error that occurred when obtaining the SQL.
	 */
	public void setError(String error)
	{
		this.error = error;
	}
	
	/**
	 * Get the error that occurred when obtaining the SQL.
	 * @return the error that occurred when obtaining the SQL.
	 */
	public String getError()
	{
		return error;
	}
	
	/**
	 * Checks whether or not an error occurred obtaining the SQL.
	 * @return true if an error is set, false if no error is set.
	 */
	public boolean hasError()
	{
		return error!=null;
	}
	
	/**
	 * Checks whether or not SQL statements are present
	 * @return true if changes are present, false if this is not the case.
	 */
	public boolean hasSQL()
	{
		return sql!=null && sql.length()>0;
	}
}
