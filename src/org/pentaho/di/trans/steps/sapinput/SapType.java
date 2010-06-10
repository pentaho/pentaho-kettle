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
package org.pentaho.di.trans.steps.sapinput;

public enum SapType {
	Single("SINGLE", "Single"),
	Structure("STRUCTURE", "Structure"),
	Table("TABLE", "Table"),
	;
	
	private String code;
	private String description;
	
	private SapType(String code, String description) {
		this.code = code;
		this.description = description;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getDescription() {
		return description;
	}
	
	public static SapType findTypeForCode(String code) {
		for (SapType type : values()) {
			if (type.getCode().equalsIgnoreCase(code)) {
				return type;
			}
		}
		return null;
	}

	public static SapType findTypeForDescription(String description) {
		for (SapType type : values()) {
			if (type.getDescription().equalsIgnoreCase(description)) {
				return type;
			}
		}
		return null;
	}

	public static String[] getDescriptions() {
		String[] descriptions = new String[values().length];
		for (int i=0;i<values().length;i++) {
			descriptions[i] = values()[i].getDescription();
		}
		return descriptions;
	}
}
