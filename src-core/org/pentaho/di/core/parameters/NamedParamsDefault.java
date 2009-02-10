 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Sven 
 * Boden.  The Initial Developer is Sven Boden.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.core.parameters;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class is an implementation of NamedParams.
 * 
 * @author Sven Boden
 */
public class NamedParamsDefault implements NamedParams 
{
	/**
	 * Map to store named parameters in.
	 */
	private Map<String, OneNamedParam> params = new HashMap<String, OneNamedParam>();
	
	/**
	 * Target class for the parameter keys.
	 */
	class OneNamedParam {
		public String key;
		public String description;
		public String defaultValue;    // not used for the moment.
		public String value;
	}
	
	/**
	 * Default constructor.
	 */
	public NamedParamsDefault() {	
	}
	
	public void addParameterDefinition(String key, String description) {
		OneNamedParam oneParam = new OneNamedParam();
		
		oneParam.key = key;
		oneParam.description = description;
		oneParam.value = "";
		
		params.put(key, oneParam);		
	}

	public String getParameterDescription(String key) {
		String description = null;
		
		OneNamedParam theParam = params.get(key);
		if ( theParam != null )  {
			description = theParam.description;
		}
		
		return description;
	}

	public String getParameterValue(String key) {
		String value = null;
		
		OneNamedParam theParam = params.get(key);
		if ( theParam != null )  {
			value = theParam.value;
		}
		
		return value;
	}

	public String[] listParameters() {
		Set<String> keySet = params.keySet();
		
		String[] paramArray = keySet.toArray(new String[0]);
	    Arrays.sort(paramArray);
	    
	    return paramArray;
	}

	public void setParameterValue(String key, String value) {
		OneNamedParam theParam = params.get(key);
		if ( theParam != null )  {
			theParam.value = value;
		}		
	}

	public void clearValues() {
		params.clear();
	}

//	public void clearValues() {	
//		String[] keys = listParameters();
//		for ( int idx = 0; idx < keys.length; idx++)  {
//			OneNamedParam theParam = params.get(keys[idx]);
//			if ( theParam != null )  {
//				theParam.value = "";
//			}			
//		}
//	}

	public void activateParameters() {
		// Do nothing here.
	}

	public void copyParametersFrom(NamedParams aParam) {
		if ( params != null )  {
			params.clear();
			String[] keys = aParam.listParameters();
			for ( int idx = 0; idx < keys.length; idx++)  {
				String desc  = aParam.getParameterDescription(keys[idx]);
				String value = aParam.getParameterValue(keys[idx]);
				
				addParameterDefinition(keys[idx], desc);
				setParameterValue(keys[idx], value);
			}
		}
	}
}