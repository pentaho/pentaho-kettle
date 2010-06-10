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


/**
 * This singleton keeps the location of a repository import.
 * 
 * NOT THREAD SAFE, ONLY ONE IMPORT AT A TIME PLEASE!!

 * @author matt
 *
 */
public class RepositoryImportLocation {

	private static RepositoryImportLocation location;
	
	private RepositoryDirectoryInterface repositoryDirectory;
	
	private RepositoryImportLocation() {
		repositoryDirectory = null;
	}
	
	/**
	 * Get the repository import location.
	 * WARNING: NOT THREAD SAFE, ONLY ONE IMPORT AT A TIME PLEASE!!
	 * 
	 * @return the import location in the repository in the form of a repository directory.
	 *         If no import location is set, null is returned.
	 */
	public static RepositoryDirectoryInterface getRepositoryImportLocation() {
		if (location==null) location = new RepositoryImportLocation();
		return location.repositoryDirectory;
	}
	
	/**
	 * Sets the repository import location.
	 * WARNING: NOT THREAD SAFE, ONLY ONE IMPORT AT A TIME PLEASE!!
	 * 
	 * ALSO MAKE SURE TO CLEAR THE IMPORT DIRECTORY AFTER IMPORT!!
	 * (sorry for shouting)
	 * 
	 * @param repositoryDirectory the import location in the repository in the form of a repository directory.
	 *    
	 */
	public static void setRepositoryImportLocation(RepositoryDirectoryInterface repositoryDirectory) {
		if (location==null) location = new RepositoryImportLocation();
		location.repositoryDirectory = repositoryDirectory;
	}
}
