package org.pentaho.database.model;

import java.util.List;

public interface IDatabaseType {

  String getName();

  String getShortName();

  List<DatabaseAccessType> getSupportedAccessTypes();

  int getDefaultDatabasePort();

  String getExtraOptionsHelpUrl();

}