package org.pentaho.database.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.database.dialect.IDatabaseDialect;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;

public class DatabaseDialectService {

  List<IDatabaseDialect> databaseDialects = new ArrayList<IDatabaseDialect>();
  Map<IDatabaseType, IDatabaseDialect> typeToDialectMap = new HashMap<IDatabaseType, IDatabaseDialect>();
  
  public void registerDatabaseDialect(IDatabaseDialect databaseDialect) {
    typeToDialectMap.put(databaseDialect.getDatabaseType(), databaseDialect);
    databaseDialects.add(databaseDialect);
  }
  
  public IDatabaseDialect getDialect(IDatabaseType databaseType) {
    return typeToDialectMap.get(databaseType);
  }
  
  public IDatabaseDialect getDialect(IDatabaseConnection connection) {
    return typeToDialectMap.get(connection.getDatabaseType());
  }
  
  public List<IDatabaseDialect> getDatabaseDialects() {
    return databaseDialects;
  }
}
