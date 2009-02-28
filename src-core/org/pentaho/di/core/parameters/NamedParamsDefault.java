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
		/**
		 * key of this parameter
		 */
		public String key;
		
		/**
		 * Description of the parameter
		 */
		public String description;
		
		/**
		 * Default value for this parameter
		 */
		public String defaultValue;
		
		/**
		 * Actual value of the parameter.
		 */
		public String value;
	}
	
	/**
	 * Default constructor.
	 */
	public NamedParamsDefault() {	
	}
	
	public void addParameterDefinition(String key, String defValue, String description) throws DuplicateParamException {
		
		if ( params.get(key) == null )  {
			OneNamedParam oneParam = new OneNamedParam();
		
			oneParam.key = key;
			oneParam.defaultValue = defValue;
			oneParam.description = description;
			oneParam.value = "";
		
			params.put(key, oneParam);
		}
		else  {
			throw new DuplicateParamException("Duplicate parameter '" + key + "' detected.");
		}	
	}

	public String getParameterDescription(String key) throws UnknownParamException {
		String description = null;
		
		OneNamedParam theParam = params.get(key);
		if ( theParam != null )  {
			description = theParam.description;
		}
		
		return description;
	}

	public String getParameterValue(String key) throws UnknownParamException {
		String value = null;
		
		OneNamedParam theParam = params.get(key);
		if ( theParam != null )  {
			value = theParam.value;
		}
		
		return value;
	}
	
	public String getParameterDefault(String key) throws UnknownParamException {
		String value = null;
		
		OneNamedParam theParam = params.get(key);
		if ( theParam != null )  {
			value = theParam.defaultValue;
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

	public void eraseParameters() {
		params.clear();
	}

	public void clearParameters() {	
		String[] keys = listParameters();
		for ( int idx = 0; idx < keys.length; idx++)  {
			OneNamedParam theParam = params.get(keys[idx]);
			if ( theParam != null )  {
				theParam.value = "";
			}			
		}
	}

	public void activateParameters() {
		// Do nothing here.
	}

	public void copyParametersFrom(NamedParams aParam) {
		if ( params != null )  {
			params.clear();
			String[] keys = aParam.listParameters();
			for ( int idx = 0; idx < keys.length; idx++)  {
				String desc;
				try {
					desc = aParam.getParameterDescription(keys[idx]);
				} catch (UnknownParamException e) {
					desc = "";
				}
				String defValue;
				try {
					defValue = aParam.getParameterDefault(keys[idx]);
				} catch (UnknownParamException e) {
					defValue = "";
				}
				String value;
				try {
					value = aParam.getParameterValue(keys[idx]);
				} catch (UnknownParamException e) {
					value = "";
				}
				
				try {
					addParameterDefinition(keys[idx], defValue, desc);
				} catch (DuplicateParamException e) {
					// Do nothing, just overwrite.
				}
				setParameterValue(keys[idx], value);
			}
		}
	}
}