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
package org.pentaho.di.i18n;

import org.pentaho.di.laf.Handler;

/**
 * Standard Message handler that takes a root package, plus key and resolves that into one/more
 * resultant messages.  This Handler is used by all message types to enable flexible look and feel
 * as well as i18n to be implemented in variable ways.
 * 
 * @author dhushon
 *
 */
public interface MessageHandler extends Handler {

	/**
	 * get a key from the default (System global) bundle
	 * @param key
	 * @return
	 */
	public String getString(String key);
	
	/**
	 * get a key from the defined package bundle, by key
	 * @param packageName
	 * @param key
	 * @return
	 */
	public String getString(String packageName, String key);

	/**
	 * get a key from the defined package bundle, by key
	 * @param packageName
	 * @param key
	 * @param param1
	 * @return
	 */
	public String getString(String packageName, String key, String param1);

	/**
	 * get a key from the defined package bundle, by key
	 * @param packageName
	 * @param key
	 * @param param1
	 * @param param2
	 * @return
	 */
	public String getString(String packageName, String key, String param1, String param2);

	/**
	 * get a key from the defined package bundle, by key
	 * @param packageName
	 * @param key
	 * @param param1
	 * @param param2
	 * @param param3
	 * @return
	 */
	public String getString(String packageName, String key, String param1, String param2, String param3);

	/**
	 * get a key from the defined package bundle, by key
	 * @param packageName
	 * @param key
	 * @param param1
	 * @param param2
	 * @param param3
	 * @param param4
	 * @return
	 */
	public String getString(String packageName, String key, String param1, String param2, String param3, String param4);

	/**
	 * get a key from the defined package bundle, by key
	 * @param packageName
	 * @param key
	 * @param param1
	 * @param param2
	 * @param param3
	 * @param param4
	 * @param param5
	 * @return
	 */
	public String getString(String packageName, String key, String param1, String param2, String param3, String param4, String param5);

	/**
	 * get a key from the defined package bundle, by key
	 * @param packageName
	 * @param key
	 * @param param1
	 * @param param2
	 * @param param3
	 * @param param4
	 * @param param5
	 * @param param6
	 * @return
	 */
	public String getString(String packageName, String key, String param1, String param2, String param3, String param4, String param5, String param6);
}
