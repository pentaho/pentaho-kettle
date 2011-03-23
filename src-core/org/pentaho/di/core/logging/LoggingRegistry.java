/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.core.logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.EnvUtil;

/**
 * This singleton class contains the logging registry.
 * It's a hash-map containing the hierarchy for a certain UUID that is generated when a job, job-entry, transformation or step is being created.
 * With it, you can see for each log record to which step-mapping-xform-entry-job-job hiearchy it belongs.
 * 
 * @author matt
 *
 */
public class LoggingRegistry {
	
	private static LoggingRegistry registry;
	
	private Map<String,LoggingObjectInterface> map;
	
	private Date lastModificationTime;
	
	private int maxSize;
	
	private LoggingRegistry() {
		map = new ConcurrentHashMap<String, LoggingObjectInterface>();	
		lastModificationTime = new Date();
		maxSize = Const.toInt(EnvUtil.getSystemProperty(Const.KETTLE_MAX_LOGGING_REGISTRY_SIZE), 1000);
	}
	
	public static LoggingRegistry getInstance() {
		if (registry!=null) {
			return registry;
		}
		registry = new LoggingRegistry();
		return registry;
	}
	
	/**
	 * This methods registers a new logging source, stores it in the registry.
	 * 
	 * @param object The logging source
	 * @param int The logging level for this logging source
	 * @return a new ID (UUID)
	 */
	public String registerLoggingSource(Object object) {
		
		// Extract the core logging information from the object itself, including the hierarchy.
		//
		LoggingObject loggingSource = new LoggingObject(object);
		
		// First do a sanity check to see if the object is not already present in the registry
		// This will prevent excessive memory leakage in the registry map too... 
		//
		LoggingObjectInterface found = findExistingLoggingSource(loggingSource);
		if (found!=null && found.getParent()!=null) {
		  // Return the previous log channel ID
		  //
			return found.getLogChannelId();
		}

		// Nothing was found, generate a new ID and store it in the registry...
		//
		String logChannelId = UUID.randomUUID().toString();
		loggingSource.setLogChannelId(logChannelId);

		map.put(logChannelId, loggingSource);
		lastModificationTime = new Date();
		loggingSource.setRegistrationDate(lastModificationTime);
		
		// Validate that we're not leaking references.  If the size of the map becomes too large we opt to remove the oldest...
		//
		if (maxSize>0 && map.size()>maxSize) {
		  
		  // First we'll try to find a similar entry.
		  // For example we might be adding the same entries all the time to the registry when a job is in a loop.
		  //
		  LoggingObjectInterface similar = findFirstSimilarLoggingObject(loggingSource);
		  if (similar!=null) {
		    map.remove(similar.getLogChannelId());
		  }
		  
		  // If this didn't work we retry when it gets out of hand...
		  //
		  if (map.size()>maxSize+250) {
  		  // Remove 250 and trim it back to maxSize
  		  //
  		  List<LoggingObjectInterface> all = new ArrayList<LoggingObjectInterface>(map.values());
  		  Collections.sort(all, new Comparator<LoggingObjectInterface>() {
  		    @Override
  		    public int compare(LoggingObjectInterface o1, LoggingObjectInterface o2) {
  		      if (o1==null && o2!=null) return -1;
            if (o1!=null && o2==null) return 1;
            if (o1==null && o2==null) return 0;
  		      return o1.getRegistrationDate().compareTo(o2.getRegistrationDate());
  		    }
        });
  		  
  		  // Remove 250 entries...
  		  //
  		  for (int i=0;i<250;i++) {
  		    LoggingObjectInterface toRemove = all.get(i);
  		    map.remove(toRemove.getLogChannelId());
  		  }
		  }
		}
		
		return logChannelId;
	}
	
	private LoggingObjectInterface findFirstSimilarLoggingObject(LoggingObject src) {

	  for (LoggingObjectInterface obj : map.values()) {
	    boolean sameName =   obj.getObjectName()!=null && src.getObjectName()!=null && obj.getObjectName().equals( src.getObjectName() );
	    if (sameName) {
	      return obj;
	    }
	  }
	  return null;
  }

  /**
	 * See if the registry already contains the specified logging object.  If so, return the one in the registry.
	 * You can use this to verify existence prior to assigning a new channel ID.
	 * @param loggingObject The logging object to verify
	 * @return the existing object or null if none is present.
	 */
	public LoggingObjectInterface findExistingLoggingSource(LoggingObjectInterface loggingObject) {
		LoggingObjectInterface found = null;
		for (LoggingObjectInterface verify : map.values()) {

			if (loggingObject.equals(verify)) {
				found = verify;
				break;
			}
		}
		return found;
	}

	/**
	 * Get the logging source object for a certain logging id
	 * @param logChannelId the logging channel id to look for
	 * @return the logging source of null if nothing was found
	 */
	public LoggingObjectInterface getLoggingObject(String logChannelId) {
		return map.get(logChannelId);
	}
	
	public Map<String, LoggingObjectInterface> getMap() {
		return map;
	}

	/**
	 * In a situation where you have a job or transformation, you want to get a list of ALL the children where the parent is the channel ID.
	 * The parent log channel ID is added
	 * 
	 * @param parentLogChannelId The parent log channel ID
	 * @return the list of child channel ID
	 */
	public List<String> getLogChannelChildren(String parentLogChannelId) {
		if (parentLogChannelId==null) {
			return null;
		}
		List<String> list = new ArrayList<String>();
		getLogChannelChildren(list, parentLogChannelId);
		if (!list.contains(parentLogChannelId)) {
			list.add(parentLogChannelId);
		}
		return list;
	}

	/**
	 * In a situation where you have a job or transformation, you want to get a list of ALL the children where the parent is the channel ID.
	 * 
	 * @param parentLogChannelId The parent log channel ID
	 * @return the list of child channel ID
	 */
	private List<String> getLogChannelChildren(List<String> children, String parentLogChannelId) {
		
		List<String> list = new ArrayList<String>();
		
		

		// Get all the direct children of this parent
		//
		Iterator<LoggingObjectInterface> mapIterator = map.values().iterator();
		while(mapIterator.hasNext()) {
			LoggingObjectInterface loggingObject = mapIterator.next();
			if (loggingObject.getLogChannelId().equals(parentLogChannelId)) {
				continue; // not this one!
			}
			
			LoggingObjectInterface parent = loggingObject.getParent(); 
			if (parent!=null && parent.getLogChannelId()!=null) {
				// object has a parent, this is a candidate
				//
				if (parent.getLogChannelId().equals(parentLogChannelId)) {
					String childId = loggingObject.getLogChannelId();
					
					// We don't really want duplicates...
					//
					if (!list.contains(childId)) {
						list.add(childId);
					}
				}
			}
		}
		
		// Now for all these children, get the children too...
		//
		
		for (String childId : new ArrayList<String>(list)) {
			getLogChannelChildren(list, childId);
		}
		
		// Add all the entries in list to children, again, avoid duplicates
		//
		Iterator<String> listIterator = list.iterator();
		while(listIterator.hasNext()) {
			String id = listIterator.next();
			if (!children.contains(id)) {
				children.add(id);
			}
		}

		return children;
	}
	
	public Date getLastModificationTime() {
		return lastModificationTime;
	}
}
