package org.pentaho.database.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.pentaho.database.model.IDatabaseType;

public class DatabaseTypeHelper {
  
  List<IDatabaseType> databaseTypes;
  List<String> databaseTypeNames = null;
  Map<String, IDatabaseType> databaseTypeByName = null;
  Map<String, IDatabaseType> databaseTypeByShortName = null;
  
// consider moving this to a separate service
//  Map<IDatabaseType, IDatabaseDialect> typeToDialectMap = new HashMap<IDatabaseType, IDatabaseDialect>();

//public void registerDatabaseType(IDatabaseType databaseType, Object /*IDatabaseDialect*/ databaseDialect) {
//databaseTypes.add(databaseType);
////typeToDialectMap.put(databaseType, databaseDialect);
//}

//public IDatabaseDialect getDialect(DatabaseConnection connection) {
//return typeToDialectMap.get(connection.getDatabaseType());
//}

  
//  private List<DatabaseAccessType> getList(DatabaseAccessType... accessTypes) {
//    ArrayList<DatabaseAccessType> list = new ArrayList<DatabaseAccessType>();
//    for (DatabaseAccessType accessType : accessTypes) {
//      list.add(accessType);
//    }
//    return list;
//  }
  
  public DatabaseTypeHelper(List<IDatabaseType> databaseTypes) {
    this.databaseTypes = databaseTypes;
  }
  
  private void init() {
    List<String> dbTypeNames = new ArrayList<String>();
    Map<String, IDatabaseType> dbTypeByName = new TreeMap<String, IDatabaseType>();
    Map<String, IDatabaseType> dbTypeByShortName = new TreeMap<String, IDatabaseType>();
    for (IDatabaseType dbtype : databaseTypes) {
      dbTypeNames.add(dbtype.getName());
      dbTypeByName.put(dbtype.getName(), dbtype);
      dbTypeByShortName.put(dbtype.getShortName(), dbtype);
    }
    databaseTypeNames = /*Collections.unmodifiableList(*/ dbTypeNames; //);
    databaseTypeByName = /*Collections.unmodifiableMap(*/ dbTypeByName; //);
    databaseTypeByShortName = /*Collections.unmodifiableMap(*/ dbTypeByShortName; //);
    
  }
  
  public List<String> getDatabaseTypeNames() {
    if (databaseTypeNames == null) {
      init();
    }
    return databaseTypeNames;
  }
  
  public IDatabaseType getDatabaseTypeByName(String name) {
    if (databaseTypeByName == null) {
      init();
    }
    return databaseTypeByName.get(name);
  }
  
  public IDatabaseType getDatabaseTypeByShortName(String name) {
    if (databaseTypeByShortName == null) {
      init();
    }
    return databaseTypeByShortName.get(name);
  }

}
