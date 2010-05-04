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

public enum RepositoryObjectType {

	TRANSFORMATION("transformation", ".ktr"), 
	JOB("job", ".kjb"),
	DATABASE("database", ".kdb"),
	SLAVE_SERVER("slave server", ".ksl"),
	CLUSTER_SCHEMA("cluster schema", ".kcs"),
	PARTITION_SCHEMA("partition schema", ".kps"),
	STEP("step", ".kst"),
	JOB_ENTRY("job entry", ".kje"),

	// non-standard, Kettle database repository only!
	//
	// USER("user", ".usr"),

	; 
	
	private String	typeDescription;
	private String  extension;
	
	private RepositoryObjectType(String typeDescription, String extension) {
		this.typeDescription = typeDescription;
		this.extension = extension;
	}
	
	public String toString() {
		return typeDescription;
	}
	
	public String getTypeDescription() {
		return typeDescription;
	}
	
	public String getExtension() {
		return extension;
	}
}
