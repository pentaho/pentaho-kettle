package org.pentaho.di.core.database;

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.ValueMetaInterface;

public class ConcreteBaseDatabaseMeta extends BaseDatabaseMeta {

  @Override
  public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean use_autoinc,
      boolean add_fieldname, boolean add_cr ) {
    return null;
  }

  @Override
  public String getDriverClass() {
    return null;
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) throws KettleDatabaseException {
    return null;
  }

  @Override
  public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
      String pk, boolean semicolon ) {
    return null;
  }

  @Override
  public String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
      String pk, boolean semicolon ) {
    return null;
  }

  @Override
  public String[] getUsedLibraries() {
    return null;
  }

  @Override
  public int[] getAccessTypeList() {
    return null;
  }

}
