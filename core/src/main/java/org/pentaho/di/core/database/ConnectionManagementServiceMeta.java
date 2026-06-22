package org.pentaho.di.core.database;

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.ValueMetaInterface;

import org.apache.commons.lang3.NotImplementedException;

/**
 * Acts as a placeholder for the Connection Management Service connection, which is not a real database, but needs to be
 * acting as one in the internal flow.
 * <p>
 * Internally contains an instance of a real database and proxies all calls to it after it's data is fetched from the
 * connection management service.
 */
public class ConnectionManagementServiceMeta extends BaseDatabaseMeta implements DatabaseInterface {

  private static final String NOT_IMPLEMENTED_MESSAGE =
    "ConnectionManagementServiceMeta is a placeholder and should not be used directly.";

  @Override
  public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean useAutoinc,
                                    boolean addFieldName, boolean addCr ) {
    throw new NotImplementedException( NOT_IMPLEMENTED_MESSAGE );
  }

  @Override
  public int[] getAccessTypeList() {
    return new int[ 0 ];
  }

  @Override
  public String getDriverClass() {
    throw new NotImplementedException( NOT_IMPLEMENTED_MESSAGE );
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) throws KettleDatabaseException {
    throw new NotImplementedException( NOT_IMPLEMENTED_MESSAGE );
  }

  @Override
  public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc, String pk,
                                       boolean semicolon ) {
    throw new NotImplementedException( NOT_IMPLEMENTED_MESSAGE );
  }

  @Override
  public String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
                                          String pk, boolean semicolon ) {
    throw new NotImplementedException( NOT_IMPLEMENTED_MESSAGE );
  }

  @Override
  public String[] getUsedLibraries() {
    throw new NotImplementedException( NOT_IMPLEMENTED_MESSAGE );
  }
}
