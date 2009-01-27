/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Sven 
 * Boden.  The Initial Developer is Sven Boden.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.core.parameters;


/**
 * Interface to implement named parameters.
 * 
 * @author Sven Boden 
 */
public interface NamedParams
{
	/**
	 * Add a parameter definition to this set.
	 * 
	 * TODO: default, throw exception
	 * 
	 * @param key Name of the parameter.
	 * @param description Description of the parameter. 
	 */
	void addParameterDefinition(String key, String description);
	    
	/**
	 * Set the value of a parameter.
	 * 
	 * @param key key to set value of
	 * @param value value to set it to.
	 */
    void setParameterValue(String key, String value);
    
	/**
	 * Get the value of a parameter.
	 * 
	 * @param key Key to get value for.
     *
	 * @return null when not defined.
	 */
	String getParameterValue(String key);

	/**
	 * Get the description of a parameter.
	 * 
	 * @param key Key to get value for.
     *
	 * @return null when not defined.
	 */
	String getParameterDescription(String key);
	
    /**
     * List the parameters.
     * 
     * @return Array of parameters.
     */
    String[] listParameters();
    
    /**
     * Clear the values.
     */
    void clearValues();
    
    /**
     * Copy params to these named parameters (clearing out first).
     * 
     * @param params the parameters to copy from.
     */
    void copyParametersFrom(NamedParams params);
    
    /**
     * Activate the currently set parameters
     */
    void activateParameters();
}