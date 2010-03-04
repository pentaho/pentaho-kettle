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

import org.pentaho.di.core.plugins.DatabaseMetaPlugin;


/**
 * Contains Database Connection information through static final members for a BMW Remedy Action Request System.
 * These connections are typically read-only ODBC only. 
 * 
 * @author Matt
 * @since  11-Sep-2007
 */

@DatabaseMetaPlugin( type="REMEDY-AR-SYSTEM", typeDescription="Remedy Action Request System" )
public class RemedyActionRequestSystemDatabaseMeta extends GenericDatabaseMeta implements DatabaseInterface
{
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_JNDI };
	}
	
	/**
	 * @see DatabaseInterface#getNotFoundTK(boolean)
	 */
	public int getNotFoundTK(boolean use_autoinc)
	{
		return super.getNotFoundTK(use_autoinc);
	}
	
	public String getDriverClass()
	{
		return "sun.jdbc.odbc.JdbcOdbcDriver"; // always ODBC!
	}
	
    public String getURL(String hostname, String port, String databaseName)
    {
    	return "jdbc:odbc:"+databaseName;
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
}
