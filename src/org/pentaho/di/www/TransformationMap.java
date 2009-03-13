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

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
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
	private Map<String, Trans> transformationMap;
    private Map<String, TransConfiguration> configurationMap;
    private Map<String, Appender> loggingMap;
    
    private Map<String, List<SocketPortAllocation>> hostServerSocketPortsMap; 
    
    /**
     * @param parentThreadName
     * @deprecated The parent thread name is no longer used.
     */
    public TransformationMap(String parentThreadName)
    {
    	this();
    }
    
    public TransformationMap()
    {
        transformationMap = new Hashtable<String, Trans>();
        configurationMap  = new Hashtable<String, TransConfiguration>();
        loggingMap        = new Hashtable<String, Appender>();
        
        hostServerSocketPortsMap = new Hashtable<String, List<SocketPortAllocation>>();
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

    /**
     * This is the meat of the whole problem.  We'll allocate a port for a given slave, transformation and step copy, always on the same host.
     * Algorithm:
     * 1) Search for the right map in the hostPortMap
     * 
     * @param portRangeStart the start of the port range as described in the used cluster schema
     * @param hostname the hostname to allocate this address for
     * @param transformationName
     * @param sourceStepName
     * @param sourceStepCopy
     * @return
     */
    public synchronized SocketPortAllocation allocateServerSocketPort(int portRangeStart, String hostname, String transformationName, String sourceSlaveName, String sourceStepName, String sourceStepCopy, String targetSlaveName, String targetStepName, String targetStepCopy) {
    	
    	// Look up the sockets list for the given host
    	//
    	List<SocketPortAllocation> serverSocketPortsMap = hostServerSocketPortsMap.get(hostname);
    	if (serverSocketPortsMap==null) {
    		serverSocketPortsMap = new ArrayList<SocketPortAllocation>();
    		hostServerSocketPortsMap.put(hostname, serverSocketPortsMap);
    	} 

    	// Find the socket port allocation in the list...
    	//
    	SocketPortAllocation socketPortAllocation = null;
    	int maxPort=portRangeStart-1;
    	for (int index = 0; index < serverSocketPortsMap.size() ; index++) {
    		SocketPortAllocation spa = serverSocketPortsMap.get(index);
    		if (spa.getPort()>maxPort) {
    			maxPort=spa.getPort();
    		}

    		if (spa.getSourceSlaveName().equalsIgnoreCase(sourceSlaveName) && 
	    			spa.getTargetSlaveName().equalsIgnoreCase(targetSlaveName) &&
	    			spa.getTransformationName().equalsIgnoreCase(transformationName) &&
	    			spa.getSourceStepName().equalsIgnoreCase(sourceStepName) &&
	    			spa.getSourceStepCopy().equalsIgnoreCase(sourceStepCopy) &&
	    			spa.getTargetStepName().equalsIgnoreCase(targetStepName) &&
	    			spa.getTargetStepCopy().equalsIgnoreCase(targetStepCopy)
	    		) {
	    			// This is the port we want, return it.  Make sure it's allocated.
	    			//
    				spa.setAllocated(true);
	    			socketPortAllocation = spa;
	    			break;
	    	}
    	
    		if (!spa.isAllocated()) {
    			// This is not an allocated port.
    			// So we can basically use this port slot to put our own allocation in it.
    			//
    			// However, that is ONLY possible if the port belongs to the same slave server couple.
    			// Otherwise, we keep on searching.
    			//
    			if (spa.getSourceSlaveName().equalsIgnoreCase(sourceSlaveName) && spa.getTargetSlaveName().equalsIgnoreCase(targetSlaveName)) {
	    			socketPortAllocation = new SocketPortAllocation(spa.getPort(), new Date(), transformationName, sourceSlaveName, sourceStepName, sourceStepCopy, targetSlaveName, targetStepName, targetStepCopy);
	    			serverSocketPortsMap.set(index, socketPortAllocation);
	    			break;
    			}
    		}
    	}
    	
    	if (socketPortAllocation==null) {
    		// Allocate a new port and add it to the back of the list
    		// Normally this list should stay sorted on port number this way
    		//
    		socketPortAllocation = new SocketPortAllocation(maxPort+1, new Date(), transformationName, sourceSlaveName, sourceStepName, sourceStepCopy, targetSlaveName, targetStepName, targetStepCopy);
    		serverSocketPortsMap.add(socketPortAllocation);
    	}
    	
    	// DEBUG : Do a verification on the content of the list.
    	// If we find a port twice in the list, complain!
    	//
    	for (int i = 0; i < serverSocketPortsMap.size() ; i++) {
    		for (int j = 0; j < serverSocketPortsMap.size() ; j++) {
    			if (i!=j) {
    				SocketPortAllocation one = serverSocketPortsMap.get(i);
    				SocketPortAllocation two = serverSocketPortsMap.get(j);
    				if (one.getPort() == two.getPort()) {
    					System.out.println("WTF!!! Identical ports discovered in the ports list.");
    				}
    			}
    		}
    	}
    	    	
    	// give back the good news too...
    	//
    	return socketPortAllocation;
    }
    
    /**
     * Deallocate all the ports for the given transformation name, accross all hosts. 
     * @param transName the transformation name to release
     */
    public void deallocateServerSocketPorts(String transName) {
    	for (String hostname : hostServerSocketPortsMap.keySet()) {
    		for (SocketPortAllocation spa : hostServerSocketPortsMap.get(hostname)) {
    			if (spa.getTransformationName().equalsIgnoreCase(transName)) {
    				spa.setAllocated(false);
    			}
    		}
    	}
    }

	public void deallocateServerSocketPort(int port, String hostname) {
    	// Look up the sockets list for the given host
    	//
    	List<SocketPortAllocation> serverSocketPortsMap = hostServerSocketPortsMap.get(hostname);
    	if (serverSocketPortsMap==null) {
    		return; // nothing to deallocate
    	}
    	
    	// Find the socket port allocation in the list...
    	//
    	for (SocketPortAllocation spa : new ArrayList<SocketPortAllocation>(serverSocketPortsMap)) {
    		
    		if (spa.getPort()==port) {
    			spa.setAllocated(false);
    			return;
    		}
    	}
	}
}
