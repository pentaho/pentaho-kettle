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

public enum RepositoryOperation {

	READ_TRANSFORMATION("Read transformation"),
	MODIFY_TRANSFORMATION("Modify transformation"),
	DELETE_TRANSFORMATION("Delete transformation"),
	EXECUTE_TRANSFORMATION("Execute transformation"),
	LOCK_TRANSFORMATION("Lock transformation"),
	
	READ_JOB("Read job"),
	MODIFY_JOB("Modify job"),
	DELETE_JOB("Delete job"),
	EXECUTE_JOB("Execute job"),
	LOCK_JOB("Lock job"),
	
	MODIFY_DATABASE("Modify database connection"),
	DELETE_DATABASE("Delete database connection"),
	EXPLORE_DATABASE("Explore database connection"),
	
	MODIFY_SLAVE_SERVER("Modify slave server"),
	DELETE_SLAVE_SERVER("Delete slave server"),

	MODIFY_CLUSTER_SCHEMA("Modify cluster schema"),
	DELETE_CLUSTER_SCHEMA("Delete cluster schema"),

	MODIFY_PARTITION_SCHEMA("Modify partition schema"),
	DELETE_PARTITION_SCHEMA("Delete partition schema"),
	
	CREATE_DIRECTORY("Create directory"),
	RENAME_DIRECTORY("Rename directory"),
	DELETE_DIRECTORY("Delete directory"),

	;

	private final String description;
	
	RepositoryOperation(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String toString() {
		return description;
	}
}
