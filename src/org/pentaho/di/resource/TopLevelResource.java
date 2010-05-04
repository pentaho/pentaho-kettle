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
package org.pentaho.di.resource;

/**
 * This describes the top level resource after an export
 * 
 * @author matt
 *
 */
public class TopLevelResource {
	private String baseResourceName;
	private String archiveName;
	private String resourceName;
	
	/**
	 * @param baseResourceName
	 * @param archiveName
	 * @param resourceName
	 */
	public TopLevelResource(String baseResourceName, String archiveName, String resourceName) {
		this.baseResourceName = baseResourceName;
		this.archiveName = archiveName;
		this.resourceName = resourceName;
	}
	/**
	 * @return the baseResourceName
	 */
	public String getBaseResourceName() {
		return baseResourceName;
	}
	/**
	 * @param baseResourceName the baseResourceName to set
	 */
	public void setBaseResourceName(String baseResourceName) {
		this.baseResourceName = baseResourceName;
	}
	/**
	 * @return the archiveName
	 */
	public String getArchiveName() {
		return archiveName;
	}
	/**
	 * @param archiveName the archiveName to set
	 */
	public void setArchiveName(String archiveName) {
		this.archiveName = archiveName;
	}
	/**
	 * @return the resourceName
	 */
	public String getResourceName() {
		return resourceName;
	}
	/**
	 * @param resourceName the resourceName to set
	 */
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	
	
}
