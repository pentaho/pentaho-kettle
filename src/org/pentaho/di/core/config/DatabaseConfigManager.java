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
package org.pentaho.di.core.config;

import java.util.Collection;

import org.pentaho.di.core.annotations.Inject;
import org.pentaho.di.core.exception.KettleConfigException;

/**
 * A <code>ConfigManager</code> implementation that caters to loading configuration parameters from a database table.
 * 
 * @author Alex Silva
 *
 * @param <T>
 */
public class DatabaseConfigManager<T> extends BasicConfigManager<T>
{
	private String connectionURL;

	private String table;

	public String getConnectionURL()
	{
		return connectionURL;
	}

	@Inject
	public void setConnectionURL(String connectionURL)
	{
		this.connectionURL = connectionURL;
	}

	public String getTable()
	{
		return table;
	}

	@Inject
	public void setTable(String table)
	{
		this.table = table;
	}
	
	public Collection<T> load() throws KettleConfigException
	{
		//here we establish conn to the database and read config from table
		//but since we don't have any configurations coming from any table in the database, we'll implement this later.
		
		return null;
	}
	
	public <E> Collection<E> loadAs(Class<? extends E> type) throws KettleConfigException
	{
		return null;
	}
}
