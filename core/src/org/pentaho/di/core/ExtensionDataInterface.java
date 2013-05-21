package org.pentaho.di.core;

import java.util.Map;

/**
 * This interface gives you access to a map which is associated with an object to store the state of non-standard objects.
 * It can be used by plugins to store state-information details in transformations, jobs, steps, job-entries... at runtime.
 * 
 * @author matt
 *
 */
public interface ExtensionDataInterface {
  public Map<String, Object> getExtensionDataMap();
}
