package org.pentaho.di.engine.api;

public class RowException extends Exception {

  public RowException( String msg ) {
    super( msg );
  }

  public RowException( String msg, Throwable inner ) {
    super( msg, inner );
  }

}
