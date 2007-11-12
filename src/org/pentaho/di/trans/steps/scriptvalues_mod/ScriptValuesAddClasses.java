 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 /**********************************************************************
 **                                                                   **
 ** This Script has been modified for higher performance              **
 ** and more functionality in December-2006,                          **
 ** by proconis GmbH / Germany                                        **
 **                                                                   ** 
 ** http://www.proconis.de                                            **
 ** info@proconis.de                                                  **
 **                                                                   **
 **********************************************************************/

package org.pentaho.di.trans.steps.scriptvalues_mod;

public class ScriptValuesAddClasses {
	
	private Class<?> addClass;
	private Object addObject;
	private String strJSName;
	
	// private String strJarFile;
	
	public ScriptValuesAddClasses(Class<?> addClass, Object addObject, String strJSName){
		super();
		this.addClass = addClass;
		this.addObject = addObject;
		this.strJSName = strJSName;
	}

	public Class<?> getAddClass(){
		return this.addClass;
	}
	
	public Object getAddObject(){
		return this.addObject;
	}
	
	public String getJSName(){
		return this.strJSName;
	}	
}