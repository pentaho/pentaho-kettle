/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

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
