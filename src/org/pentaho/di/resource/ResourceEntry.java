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

import org.pentaho.di.core.util.StringUtil;

public class ResourceEntry {
	public enum ResourceType { FILE, CONNECTION, SERVER, URL, DATABASENAME, ACTIONFILE, OTHER };
	
	private String resource;
	private ResourceType resourcetype;
	
	/**
	 * @param resource
	 * @param resourcetype
	 */
	public ResourceEntry(String resource, ResourceType resourcetype) {
		super();
		this.resource = resource;
		this.resourcetype = resourcetype;
	}

	/**
	 * @return the resource
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * @param resource the resource to set
	 */
	public void setResource(String resource) {
		this.resource = resource;
	}

	/**
	 * @return the resourcetype
	 */
	public ResourceType getResourcetype() {
		return resourcetype;
	}

	/**
	 * @param resourcetype the resourcetype to set
	 */
	public void setResourcetype(ResourceType resourcetype) {
		this.resourcetype = resourcetype;
	}
	
	public String toXml(int indentLevel) {
    StringBuffer buff = new StringBuffer(30);
    buff.append(StringUtil.getIndent(indentLevel)).append("<Resource type='").append(this.getResourcetype()). //$NON-NLS-1$
      append("'><![CDATA[").append(this.getResource()).append("]]>").append("</Resource>").append(StringUtil.CRLF);//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    return buff.toString();
  }
  
  
}
