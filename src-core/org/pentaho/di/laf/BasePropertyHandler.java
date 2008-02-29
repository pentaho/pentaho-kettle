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

/**
 * This is a static accessor for the dynamic property loader and should be used by all classes
 * requiring access to property files.  The static accessor provides a notification from the LAFFactory
 * when the concrete handler is changed at runtime should the LAF be changed.
 * 
 * @author dhushon
 *
 */
public class BasePropertyHandler implements LAFChangeListener<PropertyHandler> {
	
	static BasePropertyHandler instance = null;
	protected PropertyHandler handler = null;
	Class<PropertyHandler> clazz = PropertyHandler.class;
	
	static {
		getInstance();
	}
	
	private BasePropertyHandler() {
		init();
	}
	
	private void init() {
		//counting on LAFFactory to return a class conforming to @see MessageHandler
		handler = (PropertyHandler)LAFFactory.getHandler(clazz);
	}
	
	public static BasePropertyHandler getInstance() {
		if (instance == null) {
			instance = new BasePropertyHandler();
		}
		return instance;
	}
	
	protected PropertyHandler getHandler() {
		return handler;
	}
	
	protected static PropertyHandler getInstanceHandler() {
		return getInstance().getHandler();
	}
	
	/**
	 * return the value of a given key from the properties list
	 * @param key
	 * @return null if the key is not found
	 */
	public static String getProperty(String key) {
		return getInstanceHandler().getProperty(key);
	}
	
	/**
	 * return the value of a given key from the properties list, returning
	 * the defValue string should the key not be found
	 * 
	 * @param key
	 * @param defValue
	 * @return a string representing either the value associated with the passed key or defValue 
	 * should that key not be found
	 */
	public static String getProperty(String key, String defValue) {
		return getInstanceHandler().getProperty(key, defValue);
	}

	public void notify(PropertyHandler changedObject) {
		handler = changedObject;
	}

}
