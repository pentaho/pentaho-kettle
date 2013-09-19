package org.pentaho.di.core.database;

import org.pentaho.di.core.exception.KettleDatabaseException;

public interface DatabaseTransactionListener {
  public void commit() throws KettleDatabaseException;
  
  public void rollback() throws KettleDatabaseException;
}
