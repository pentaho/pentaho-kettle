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


public class SapOutputField {
	private String sapFieldName;
	private SapType sapType;
	private String tableName;
	private String newName;
	private int targetType;
	
	/**
	 * @param sapFieldName
	 * @param sapType
	 * @param tableName
	 * @param newName
	 * @param targetType
	 */
	public SapOutputField(String sapFieldName, SapType sapType, String tableName, String newName, int targetType) {
		this.sapFieldName = sapFieldName;
		this.sapType = sapType;
		this.tableName = tableName;
		this.newName = newName;
		this.targetType = targetType;
	}

	/**
	 * @return the sapFieldName
	 */
	public String getSapFieldName() {
		return sapFieldName;
	}

	/**
	 * @param sapFieldName the sapFieldName to set
	 */
	public void setSapFieldName(String sapFieldName) {
		this.sapFieldName = sapFieldName;
	}

	/**
	 * @return the sapType
	 */
	public SapType getSapType() {
		return sapType;
	}

	/**
	 * @param sapType the sapType to set
	 */
	public void setSapType(SapType sapType) {
		this.sapType = sapType;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * @return the newName
	 */
	public String getNewName() {
		return newName;
	}

	/**
	 * @param newName the newName to set
	 */
	public void setNewName(String newName) {
		this.newName = newName;
	}

	/**
	 * @return the targetType
	 */
	public int getTargetType() {
		return targetType;
	}

	/**
	 * @param targetType the targetType to set
	 */
	public void setTargetType(int targetType) {
		this.targetType = targetType;
	}
}
