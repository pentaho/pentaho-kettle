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

import org.pentaho.di.core.exception.KettleConfigException;

/**
 * A base class for <code>ConfigManager</code> to derive from.
 * 
 * @author Alex Silva
 *
 * @param <T>
 */
public abstract class BasicConfigManager<T> implements ConfigManager<T>
{
	protected String id;

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.config.ConfigParameters#getId()
	 */
	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}
	
	@SuppressWarnings("unchecked")
	public <E> Collection<E> loadAs(Class<? extends E> type) throws KettleConfigException
	{
		Collection<T> coll = load();
		
		if (coll.isEmpty())
			return (Collection<E>)coll;
		
		for (T obj:coll)
		{
			if (obj.getClass().isAssignableFrom(type))
				return (Collection<E>)coll;
			
			break;
		}
		
		throw new KettleConfigException(type + " is not a valid class type for the configurations elements loaded!");
	}

	
}
