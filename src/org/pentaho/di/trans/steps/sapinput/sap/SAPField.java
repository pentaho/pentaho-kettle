/* Copyright (c) 2010 Aschauer EDV GmbH.  All rights reserved. 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * This software was developed by Aschauer EDV GmbH and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 * 
 * Please contact Aschauer EDV GmbH www.aschauer-edv.at if you need additional
 * information or have any questions.
 * 
 * @author  Robert Wintner robert.wintner@aschauer-edv.at
 * @since   PDI 4.0
 */

package org.pentaho.di.trans.steps.sapinput.sap;

public class SAPField {

	private String name;
	private String table;
	private String type;
	private String typePentaho;
	private String typeSAP;
	private Object value;
	private String description;
	private String defaultvalue;

	public SAPField(String name, String table, String type) {
		super();
		this.name = name;
		this.table = table;
		this.type = type;
	}

	public SAPField(String name, String table, String type, Object value) {
		super();
		this.name = name;
		this.table = table;
		this.type = type;
		this.value = value;
	}

	@Override
	public String toString() {
		return "SAPField [name=" + name + ", table=" + table + ", type=" + type
				+ ", typePentaho=" + typePentaho + ", typeSAP=" + typeSAP
				+ ", value=" + value + ", defaultvalue=" + defaultvalue
				+ ", description=" + description + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getTypePentaho() {
		return typePentaho;
	}

	public void setTypePentaho(String typepentaho) {
		this.typePentaho = typepentaho;
	}

	public String getTypeSAP() {
		return typeSAP;
	}

	public void setTypeSAP(String typesap) {
		this.typeSAP = typesap;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDefaultvalue() {
		return defaultvalue;
	}

	public void setDefaultvalue(String defaultvalue) {
		this.defaultvalue = defaultvalue;
	}

}
