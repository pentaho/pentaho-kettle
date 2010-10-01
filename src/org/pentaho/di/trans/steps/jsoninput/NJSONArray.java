 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar HASSAN.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/


package org.pentaho.di.trans.steps.jsoninput;

import org.json.simple.JSONArray;
import org.pentaho.di.core.exception.KettleException;

public class NJSONArray {

	private JSONArray a;
	private boolean nullValue;
	
	public NJSONArray() throws KettleException {
		this.a=new JSONArray();
		setNull(false);
	}
	public NJSONArray(JSONArray ja) throws KettleException {
		this.a=ja;
		setNull(false);
	}
	public void setNull(boolean value) {
		this.nullValue=value;
	}
	public boolean isNull() {
		return this.nullValue;
	}
	
	public JSONArray getJSONArray() {
		return this.a;
	}
	
	@SuppressWarnings("unchecked")
	public void add(Object value) {
		this.a.add(value);
	}
	public int size() {
		return this.a.size();
	}
	
}
