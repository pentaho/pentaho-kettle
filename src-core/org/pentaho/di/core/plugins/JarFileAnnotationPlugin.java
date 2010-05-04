/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.core.plugins;

import java.net.URL;

public class JarFileAnnotationPlugin {
	private URL			jarFile;
	private URL pluginFolder;
	private String className;
	/**
	 * @param jarFile
	 * @param classFile
	 * @param annotation
	 */
	public JarFileAnnotationPlugin(String className, URL jarFile, URL pluginFolder) {
	  this.className = className;
		this.jarFile = jarFile;
		this.pluginFolder = pluginFolder;
	}

	@Override
	public String toString() {
		return jarFile.toString();
	}
	
	/**
	 * @return the jarFile
	 */
	public URL getJarFile() {
		return jarFile;
	}

	/**
	 * @param jarFile
	 *            the jarFile to set
	 */
	public void setJarFile(URL jarFile) {
		this.jarFile = jarFile;
	}

  public URL getPluginFolder() {
    return pluginFolder;
  }

  public void setPluginFolder(URL pluginFolder) {
    this.pluginFolder = pluginFolder;
  }
	
  public String getClassName(){
    return className;
  }

}
