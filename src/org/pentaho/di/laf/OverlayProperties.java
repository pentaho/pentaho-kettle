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

import java.util.Properties;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class OverlayProperties extends Properties implements PropertyHandler {

	private static final long serialVersionUID = 1L;
	private String name = null;
	
	public OverlayProperties(String file) throws IOException{
		load(file);
	}
	
	public boolean exists(String filename) {
		try {
			return (getURL(filename)!=null);
		} catch (MalformedURLException e) {
			return false;
		} 
	}
	
	public boolean loadProps(String filename) {
		try {
			return load(filename);
		} catch (IOException e) {
			return false;
		}
	}
	
	private URL getURL(String filename) throws MalformedURLException {
		URL url;
		File file = new File(filename);
		if (file.exists()) {
			url = file.toURI().toURL();
		} else {
			ClassLoader classLoader = getClass().getClassLoader();
			url = classLoader.getResource(filename);
		}
		return url;
	}
	
	/**
	 * cleanse and reload the property file
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public boolean load(String filename) throws IOException {
			URL url = getURL(filename);
			if (url == null) return false;
			clear();
			load(url.openStream());
		return true;
	}
	
	public String getName() {
		return name;
	}
}
