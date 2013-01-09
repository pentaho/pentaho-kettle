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
