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
 * Interface that defines the contract for loading configuration parameters for Kettle.
 * The parameter T refers to 
 * @see BasicConfigManager
 * @see DatabaseConfigManager
 * @see DigesterConfigManager
 * 
 *  
 * @author Alex Silva
 *
 * @param <T> The type of the configuration object(s) that will be managed(loaded) by this implementation.
 */
public interface ConfigManager<T>
{

	/**
	 * 
	 * @return The unique id for this manager
	 */
	public abstract String getId();
	
	/**
	 * Allows callers to set the id for this manager
	 * @param id
	 */
	public abstract void setId(String id);
	
	/**
	 * Loads the configuration parameters.
	 * @return A collection containing the parameters
	 * @throws KettleConfigException If an error occurs during load.
	 */
	public Collection<T> load() throws KettleConfigException;
	
	/**
	 * Similar to load(), but allows callers to dynamically cast the underlying collection to a specific type.
	 * This is useful for callers who are instantiating this class dynamically and don't know the expected return type
	 * until load is called.
	 * @param <E> The type of the objects managed by this implementation.
	 * @param type A class representing this type.
	 * @return A collection of the parameters.
	 * @throws KettleConfigException If a loading problem occurs.  Implementations are also required to throw this exception if
	 * <E> cannot be cast to <T>
	 */
	public <E> Collection<E> loadAs(Class<? extends E> type) throws KettleConfigException;

}