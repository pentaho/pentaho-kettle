package org.pentaho.di.resource;

public interface ResourceHolderInterface {
  
  /**
   * @return The name of the holder of the resource
   */
  public String getName();
  /**
   * @return The description of the holder of the resource
   */
  public String getDescription();
  /**
   * @return The ID of the holder of the resource
   */
  public long getID();
  /**
   * @return The Type ID of the resource holder. The Type ID
   * is the system-defined type identifier (like TRANS or SORT).
   */
  public String getTypeId();
  
}
