 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
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