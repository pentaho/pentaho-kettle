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
package org.pentaho.di.repository;

import java.util.List;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.w3c.dom.Node;

public interface RepositoryMeta {
	
	public static final String XML_TAG = "repository";

	public abstract String getDialogClassName();

	public abstract String getRevisionBrowserDialogClassName();

	public abstract void loadXML(Node repnode, List<DatabaseMeta> databases) throws KettleException;

	public abstract String getXML();

	/**
	 * @return the id
	 */
	public abstract String getId();

	/**
	 * @param id
	 *            the id to set
	 */
	public abstract void setId(String id);

	/**
	 * @return the name
	 */
	public abstract String getName();

	/**
	 * @param name
	 *            the name to set
	 */
	public abstract void setName(String name);

	/**
	 * @return the description
	 */
	public abstract String getDescription();

	/**
	 * @param description
	 *            the description to set
	 */
	public abstract void setDescription(String description);
	
	
	/**
	 * Describes the capabilities of the repository 
	 * @return The repository capabilities object
	 */
	public RepositoryCapabilities getRepositoryCapabilities();

	public RepositoryMeta clone();

}