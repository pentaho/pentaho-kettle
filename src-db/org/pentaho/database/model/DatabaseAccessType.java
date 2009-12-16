package org.pentaho.database.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum DatabaseAccessType {
  NATIVE("Native (JDBC)"),
  ODBC("ODBC"),
  OCI("OCI"),
  PLUGIN("Plugin specific access method"),
  JNDI("JNDI"),
  CUSTOM("Custom");

  private String name;
  
  private static Map<String, DatabaseAccessType> typeByName = null; 
  
  private DatabaseAccessType(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public static DatabaseAccessType getAccessTypeByName(String name) {
    if (typeByName == null) {
      typeByName = new HashMap<String, DatabaseAccessType>();
      for (DatabaseAccessType type : values()) {
        typeByName.put(type.getName(), type);
      }
    }
    return typeByName.get(name);
  }
  
  public static List<DatabaseAccessType> getList(DatabaseAccessType... accessTypes) {
    ArrayList<DatabaseAccessType> list = new ArrayList<DatabaseAccessType>();
    for (DatabaseAccessType accessType : accessTypes) {
      list.add(accessType);
    }
    return list;
  }
}
