package org.pentaho.di.trans;


public enum ServiceCacheMethod {

  None("No caching"), LocalMemory("Cache in local memory"),
  ;

  private String description;
  
  private ServiceCacheMethod(String description) {
    this.description = description;
  }
  
  public String getDescription() {
    return description;
  }
  
  public static ServiceCacheMethod getMethodByName(String string) {
    for (ServiceCacheMethod method : values()) {
      if (method.name().equalsIgnoreCase(string)) return method;
    }
    return None;
  }
  
  public static ServiceCacheMethod getMethodByDescription(String description) {
    for (ServiceCacheMethod method : values()) {
      if (method.getDescription().equalsIgnoreCase(description)) return method;
    }
    return None;
  }
  
  public static String[] getDescriptions() {
    String[] strings = new String[values().length];
    for (int i=0;i<values().length;i++) {
      strings[i] = values()[i].getDescription();
    }
    return strings;
  }
}
