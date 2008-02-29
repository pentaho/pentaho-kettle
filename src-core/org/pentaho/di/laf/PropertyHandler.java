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
package org.pentaho.di.laf;

public interface PropertyHandler extends Handler {
	/**
	 * load properties for the given properties file
	 * @param filename
	 * @return true if load was successful
	 */
	public boolean loadProps(String filename);
	
	/**
	 * check to see whether a property file exists within the classpath or filesystem
	 * @param filename
	 * @return true if resource exists
	 */
	public boolean exists(String filename);
	
	/**
	 * return the value of a given key from the properties list
	 * @param key
	 * @return null if the key is not found
	 */
	public String getProperty(String key);
	
	/**
	 * return the value of a given key from the properties list, returning
	 * the defValue string should the key not be found
	 * 
	 * @param key
	 * @param defValue
	 * @return a string representing either the value associated with the passed key or defValue 
	 * should that key not be found
	 */
	public String getProperty(String key, String defValue);
}
