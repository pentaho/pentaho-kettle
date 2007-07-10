package org.pentaho.di.core;

public interface ResourceUsageInterface {

  public enum  ResourceUsageType { FILERESOURCE, DATASOURCERESOURCE, ACTIONRESOURCE, URIRESOURCE, OTHERRESOURCE };  
  
  /**
   * @return The type of resource that the object is using
   */
  public ResourceUsageType getResourceUsageType();
  
  /**
   * Sets the resource usage type
   * @param value The type of the resource
   */
  public void setResourceUsageType(ResourceUsageType value);

  /**
   * @return Resource usage value
   */
  public String getResourceValue();

  /**
   * Sets resource that's being used by the object. Assumes that the resource
   * is representable as a string.
   * @param value The resource (as a string) that's being used
   */
  public void setResourceValue(String value);
  
  /**
   * @return The source object that's using the resource
   */
  public ResourceUsageSourceInterface getResourceUsageSource();

  /**
   * Sets the source object that's using the resource 
   * @param value
   */
  public void setResourceUsageSource(ResourceUsageSourceInterface value);
  
}
