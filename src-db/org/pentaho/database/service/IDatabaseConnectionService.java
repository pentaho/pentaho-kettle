package org.pentaho.database.service;

import java.util.List;

import org.pentaho.database.model.DatabaseConnectionPoolParameter;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;

public interface IDatabaseConnectionService {

  List<String> checkParameters(final IDatabaseConnection connection);

  String testConnection(final IDatabaseConnection connection);

  DatabaseConnectionPoolParameter[] getPoolingParameters();

  List<IDatabaseType> getDatabaseTypes();
  
}