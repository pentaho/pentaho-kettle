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
package org.pentaho.di.www;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Appender;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;


/**
 * This is a map between the transformation name and the (running/waiting/finished) transformation.
 * 
 * @author Matt
 *
 */
public class TransformationMap
{
	public int SERVER_SOCKET_PORT_START = 40000; 
		
    private Map<String, Trans> transformationMap;
    private Map<String, TransConfiguration> configurationMap;
    private Map<String, Appender> loggingMap;
    
    private String parentThreadName;
    
    private Map<String, Integer> serverSocketPorts; 
    
    public TransformationMap(String parentThreadName)
    {
        this.parentThreadName = parentThreadName;
        
        transformationMap = new Hashtable<String, Trans>();
        configurationMap  = new Hashtable<String, TransConfiguration>();
        loggingMap        = new Hashtable<String, Appender>();
        
        serverSocketPorts = new Hashtable<String, Integer>();
    }
    
    public synchronized void addTransformation(String transformationName, Trans trans, TransConfiguration transConfiguration)
    {
        transformationMap.put(transformationName, trans);
        configurationMap.put(transformationName, transConfiguration);
    }
    
    public synchronized Trans getTransformation(String transformationName)
    {
        return transformationMap.get(transformationName);
    }
    
    public synchronized TransConfiguration getConfiguration(String transformationName)
    {
        return configurationMap.get(transformationName);
    }

    public synchronized void removeTransformation(String transformationName)
    {
        transformationMap.remove(transformationName);
        configurationMap.remove(transformationName);
        
        // Remove the ports too...
        for (String key : serverSocketPorts.keySet()) {
        	if (key.startsWith(transformationName + " - ")) {
        		serverSocketPorts.remove(key);
        	}
        }
    }
    
    public synchronized Appender getAppender(String transformationName)
    {
        return loggingMap.get(transformationName);
    }
    
    public synchronized void addAppender(String transformationName, Appender appender)
    {
        loggingMap.put(transformationName, appender);
    }

    public synchronized void removeAppender(String transformationName)
    {
        loggingMap.remove(transformationName);
    }
    
    public String[] getTransformationNames()
    {
        Set<String> keySet = transformationMap.keySet();
        return keySet.toArray(new String[keySet.size()]);
    }

    /**
     * @return the parentThreadName
     */
    public String getParentThreadName()
    {
        return parentThreadName;
    }

    /**
     * @return the configurationMap
     */
    public Map<String, TransConfiguration> getConfigurationMap()
    {
        return configurationMap;
    }

    /**
     * @param configurationMap the configurationMap to set
     */
    public void setConfigurationMap(Map<String, TransConfiguration> configurationMap)
    {
        this.configurationMap = configurationMap;
    }
    
    private String createServerSocketPortKey(String transformationName, String stepName, String stepCopy)
    {
    	return transformationName + " - " + stepName + " - " + stepCopy;
    }
    
    public synchronized int getServerSocketPort(String transformationName, String stepName, String stepCopy) {
    	String key = createServerSocketPortKey(transformationName, stepName, stepCopy);
    	Integer port = serverSocketPorts.get(key);
    	if (port!=null) return port;
    	
    	// See if there are used ports on this slave server...
    	int maxPort = SERVER_SOCKET_PORT_START-1;
    	for (Integer slaveStepPort : serverSocketPorts.values()) {
    		if (slaveStepPort>maxPort) maxPort=slaveStepPort;
    	}
		// Increment the port..
		port=maxPort+1;
    	
    	// Store in the map
    	serverSocketPorts.put(key, port);
    	
    	// give back the good news too...
    	return port;
    }
}
