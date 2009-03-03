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
package org.pentaho.di.resource;

public class ResourceDefinition {
	private String filename;
	private String content;
	
	private String origin;
	
	/**
	 * @param filename
	 * @param content
	 */
	public ResourceDefinition(String filename, String content) {
		super();
		this.filename = filename;
		this.content = content;
	}
	
	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}
	
	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}
	
	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the origin of the resource as entered by the user. (optional)
	 */
	public String getOrigin() {
		return origin;
	}

	/**
	 * @param origin the origin of the resource as entered by the user. (optional)
	 */
	public void setOrigin(String origin) {
		this.origin = origin;
	}
}
