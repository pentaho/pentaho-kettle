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

package org.pentaho.di.core.variables;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.version.BuildVersion;


/**
 * This class is an implementation of VariableSpace
 * 
 * @author Sven Boden
 */
public class Variables implements VariableSpace 
{
    private Map<String, String> properties;
    private VariableSpace parent;
    private Map<String, String> injection;
    private boolean initialized;
    
    public Variables()
    {
        properties  = new Hashtable<String,String>();
        parent      = null;
        injection   = null;
        initialized = false;
        
        // The Kettle version
        properties.put(Const.INTERNAL_VARIABLE_KETTLE_VERSION, Const.VERSION);

        // The Kettle build version
        properties.put(Const.INTERNAL_VARIABLE_KETTLE_BUILD_VERSION, BuildVersion.getInstance().getRevision() );

        // The Kettle build date
        properties.put(Const.INTERNAL_VARIABLE_KETTLE_BUILD_DATE, BuildVersion.getInstance().getBuildDate() );
    }

	public void copyVariablesFrom(VariableSpace space) {
		if ( space != null && this != space )
		{
			// If space is not null and this variable is not already
			// the same object as the argument.
			String[] variableNames = space.listVariables();
			for ( int idx = 0; idx < variableNames.length; idx++ )
			{
				properties.put(variableNames[idx], space.getVariable(variableNames[idx]));
			}		
		}
	}

	public VariableSpace getParentVariableSpace() {
		return parent;
	}
	
	public void setParentVariableSpace(VariableSpace parent) {
		this.parent = parent;
	}

	public String getVariable(String variableName, String defaultValue) {
        String var = properties.get(variableName);
        if (var==null) return defaultValue;
        return var;
	}

	public String getVariable(String variableName) {
		return properties.get(variableName);
	}
	
	public boolean getBooleanValueOfVariable(String variableName, boolean defaultValue) {
		if (!Const.isEmpty(variableName))
		{
			String value = environmentSubstitute(variableName);
			if (!Const.isEmpty(value))
			{
				return ValueMeta.convertStringToBoolean(value);
			}
		}
		return defaultValue;
	}

	public void initializeVariablesFrom(VariableSpace parent) {
	   this.parent = parent;
	   
	   // Add all the system properties...
	   for (Object key : System.getProperties().keySet()) {
		   properties.put((String)key, System.getProperties().getProperty((String)key));
	   }
	   
	   if ( parent != null )
	   {
           copyVariablesFrom(parent);
	   }
	   if ( injection != null )
	   {
	       properties.putAll(injection);
	       injection = null;
	   }
	   initialized = true;
	}

	public String[] listVariables() {
		List<String> list = new ArrayList<String>();
		for ( String name : properties.keySet() )
		{
			list.add(name);
		}
		return (String[])list.toArray(new String[list.size()]);
	}

	public void setVariable(String variableName, String variableValue) 
	{
        if (variableValue!=null)
        {
            properties.put(variableName, variableValue);
        }
        else
        {
            properties.remove(variableName);
        }
    }
	
	public String environmentSubstitute(String aString)
	{
        if (aString==null || aString.length()==0) return aString;
        
        return StringUtil.environmentSubstitute(aString, properties);
	}
	
	public String[] environmentSubstitute(String string[])
	{
		String retval[] = new String[string.length];
		for (int i = 0; i < string.length; i++)
		{
			retval[i] = environmentSubstitute(string[i]);
		}
		return retval;
	}	

	public void shareVariablesWith(VariableSpace space) {
	    // not implemented in here... done by pointing to the same VariableSpace
		// implementation
	}

	public void injectVariables(Map<String, String> prop) {
		if ( initialized )
		{
		    // variables are already initialized
       	    if ( prop != null )
			{
			     properties.putAll(prop);
			     injection = null;			     
			}
		}
		else
		{
			// We have our own personal copy, so changes afterwards
			// to the input properties don't affect us.
			injection = new Hashtable<String, String>();
			injection.putAll(prop);
		}	
	}
	
	/**
	 * Get a default variable space as a placeholder. Everytime you 
	 * will get a new instance.
	 * 
	 * @return a default variable space.
	 */
	synchronized public static VariableSpace getADefaultVariableSpace()
	{
	    VariableSpace space = new Variables();
	    
	    space.initializeVariablesFrom(null);
	    
	    return space;
	}	
}