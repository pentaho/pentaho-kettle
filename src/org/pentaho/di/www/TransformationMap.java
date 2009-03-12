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
	private static final int	PORT_RECYCLE_PERIOD_IN_HOURSE	= 12;
	
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
    	// Remove ports older than 12 hours
    	//
    	int removedPort=-1;
    	SocketPortAllocation socketPortAllocation = null;
    	for (SocketPortAllocation spa : new ArrayList<SocketPortAllocation>(serverSocketPortsMap)) {
    		
    		if (spa.getSourceSlaveName().equalsIgnoreCase(sourceSlaveName) && 
    			spa.getTargetSlaveName().equalsIgnoreCase(targetSlaveName) &&
    			spa.getTransformationName().equalsIgnoreCase(transformationName) &&
    			spa.getSourceStepName().equalsIgnoreCase(sourceStepName) &&
    			spa.getSourceStepCopy().equalsIgnoreCase(sourceStepCopy) &&
    			spa.getTargetStepName().equalsIgnoreCase(targetStepName) &&
    			spa.getTargetStepCopy().equalsIgnoreCase(targetStepCopy)
    		) {
    			long elapsed = spa.getLastRequested().getTime() - System.currentTimeMillis();
    			int hours = (int) (elapsed/(1000*60*60));

    			// If the port was de-allocated OR 
    			// if it was more than 12 hours ago that this port was allocated, remove it from the list
    			//
    			if (!spa.isAllocated() || hours<PORT_RECYCLE_PERIOD_IN_HOURSE) {
	    			socketPortAllocation = spa;
	    			break;
    			} else {
    				serverSocketPortsMap.remove(spa); // remove the port, it's too old.
    				removedPort=spa.getPort(); // just in case...
    			}
    		}
    	}
    	
    	if (socketPortAllocation!=null) {
    		socketPortAllocation.setLastRequested(new Date());
    	} else {
    		boolean allocated = false;
    		int allocatedPort=-1;

    		// Allocate an available port for the host...
    		//
    		
    		// If we just removed a port, it's easy... 
    		//
    		if (removedPort>0) {
    			allocatedPort=removedPort;
    			allocated=true;
    		} else {
	    		
	    		for (int p=portRangeStart;!allocated;p++) {
	    			boolean portFound=false;
		    		for (SocketPortAllocation portAllocation : serverSocketPortsMap) {
		    			if (portAllocation.getPort()==p) {
		    				portFound=true; // sorry, "p" is taken.
		    			}
		    		}
		    		if (!portFound) {
		    			allocated=true;
		    			allocatedPort=p;
		    		}
	    		}
    		}
    		
    		// Now that we have a new port, remember it the next time around...
    		//
    		socketPortAllocation = new SocketPortAllocation(allocatedPort, new Date(), transformationName, sourceSlaveName, sourceStepName, sourceStepCopy, targetSlaveName, targetStepName, targetStepCopy);
    		serverSocketPortsMap.add(socketPortAllocation);
    	}
    	    	
    	// give back the good news too...
    	//
    	return socketPortAllocation;
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
    			serverSocketPortsMap.remove(spa);
    			return;
    		}
    	}
	}
}
