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
package org.pentaho.di.core;

public enum ObjectLocationSpecificationMethod {
	FILENAME("filename", "Filename"),
	REPOSITORY_BY_NAME("rep_name", "Specify by name in repository"),
	REPOSITORY_BY_REFERENCE("rep_ref", "Specify by reference in repository");

	private String code;
	private String description;
	
	private ObjectLocationSpecificationMethod(String code, String description) {
		this.code = code;
		this.description = description;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String[] getDescriptions() {
		String[] desc = new String[values().length];
		for (int i=0;i<values().length;i++) {
			desc[i] = values()[i].getDescription();
		}
		return desc;
	}
	
	public static ObjectLocationSpecificationMethod getSpecificationMethodByCode(String code) {
		for (ObjectLocationSpecificationMethod method : values()) {
			if (method.getCode().equals(code)) {
				return method;
			}
		}
		return null;
	}
	
	public static ObjectLocationSpecificationMethod getSpecificationMethodByDescription(String description) {
		for (ObjectLocationSpecificationMethod method : values()) {
			if (method.getDescription().equals(description)) {
				return method;
			}
		}
		return null;
	}
}
