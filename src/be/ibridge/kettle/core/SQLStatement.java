 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

 

package be.ibridge.kettle.core;

import be.ibridge.kettle.core.database.DatabaseMeta;

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
