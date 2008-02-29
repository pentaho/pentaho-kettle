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
package org.pentaho.di.core.database.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.pentaho.di.core.database.Messages;

public class DatabaseUtil
{
	private static Map<String,DataSource> FoundDS = Collections.synchronizedMap(new HashMap<String,DataSource>());
	/**
	 * Since JNDI is supported different ways in different app servers, it's
	 * nearly impossible to have a ubiquitous way to look up a datasource. This
	 * method is intended to hide all the lookups that may be required to find a
	 * jndi name.
	 * 
	 * @param dsName
	 *            The Datasource name
	 * @return DataSource if there is one bound in JNDI
	 * @throws NamingException
	 */
	public static DataSource getDataSourceFromJndi(String dsName) throws NamingException
	{
		Object foundDs = FoundDS.get(dsName);
		if (foundDs != null)
		{
			return (DataSource) foundDs;
		}
		InitialContext ctx = new InitialContext();
		Object lkup = null;
		DataSource rtn = null;
		NamingException firstNe = null;
		// First, try what they ask for...
		try
		{
			lkup = ctx.lookup(dsName);
			if (lkup != null)
			{
				rtn = (DataSource) lkup;
				FoundDS.put(dsName, rtn);
				return rtn;
			}
		} catch (NamingException ignored)
		{
			firstNe = ignored;
		}
		try
		{
			// Needed this for Jboss
			lkup = ctx.lookup("java:" + dsName); //$NON-NLS-1$
			if (lkup != null)
			{
				rtn = (DataSource) lkup;
				FoundDS.put(dsName, rtn);
				return rtn;
			}
		} catch (NamingException ignored)
		{
		}
		try
		{
			// Tomcat
			lkup = ctx.lookup("java:comp/env/jdbc/" + dsName); //$NON-NLS-1$
			if (lkup != null)
			{
				rtn = (DataSource) lkup;
				FoundDS.put(dsName, rtn);
				return rtn;
			}
		} catch (NamingException ignored)
		{
		}
		try
		{
			// Others?
			lkup = ctx.lookup("jdbc/" + dsName); //$NON-NLS-1$
			if (lkup != null)
			{
				rtn = (DataSource) lkup;
				FoundDS.put(dsName, rtn);
				return rtn;
			}
		} catch (NamingException ignored)
		{
		}
		if (firstNe != null)
		{
			throw firstNe;
		}
		throw new NamingException(Messages.getString("DatabaseUtil.DSNotFound", dsName)); //$NON-NLS-1$
	}
}
