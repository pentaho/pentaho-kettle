package org.pentaho.di.core.logging;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
	
	private LoggingRegistry() {
		map = new ConcurrentHashMap<String, LoggingObjectInterface>();	
		lastModificationTime = new Date();
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
		if (found!=null) {
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
		return logChannelId;
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
			if (parent!=null) {
				// object has a parent, this is a candidate
				//
				if (parent.getLogChannelId()==null) {
					System.out.println("!!!!!!!!!OOOPS!!!!!!!!!");
				}
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
