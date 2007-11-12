/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.trans.steps.mapping;

public class MappingValueRename implements Cloneable {
	private String sourceValueName;
	private String targetValueName;

	/**
	 * @param sourceValueName
	 * @param targetValueName
	 */
	public MappingValueRename(String sourceValueName, String targetValueName) {
		super();
		this.sourceValueName = sourceValueName;
		this.targetValueName = targetValueName;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}
	
	@Override
	public String toString() {
		return sourceValueName+"-->"+targetValueName;
	}
	
	@Override
	public boolean equals(Object obj) {
		return sourceValueName.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return sourceValueName.hashCode();
	}
	
	/**
	 * @return the sourceValueName
	 */
	public String getSourceValueName() {
		return sourceValueName;
	}
	/**
	 * @param sourceValueName the sourceValueName to set
	 */
	public void setSourceValueName(String sourceValueName) {
		this.sourceValueName = sourceValueName;
	}
	/**
	 * @return the targetValueName
	 */
	public String getTargetValueName() {
		return targetValueName;
	}
	/**
	 * @param targetValueName the targetValueName to set
	 */
	public void setTargetValueName(String targetValueName) {
		this.targetValueName = targetValueName;
	}
}
