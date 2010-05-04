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

public interface RepositoryCapabilities {

	/**
	 * @return true if the repository supports users.
	 */
	public boolean supportsUsers();
	
	/**
	 * @return true if users can be managed in the repository
	 */
	public boolean managesUsers();
	
	/**
	 * @return true if this repository is read-only
	 */
	public boolean isReadOnly();

	/**
	 * @return true if the repository supports revisions.
	 */
	public boolean supportsRevisions();
	
	/**
	 * @return true if the repository supports storing metadata like names, descriptions, ... outside of the object definitions (XML)
	 */
	public boolean supportsMetadata();

	/**
	 * @return true if this repository supports file locking
	 */
	public boolean supportsLocking();
	
	/**
	 * @return true if the repository has a version registry
	 */
	public boolean hasVersionRegistry();
	
	/**
	 * @return true if the repository supports ACLs
	 */
	public boolean supportsAcls();
	
}
