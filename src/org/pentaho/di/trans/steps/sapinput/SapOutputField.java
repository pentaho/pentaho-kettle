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
