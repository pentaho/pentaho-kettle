package org.pentaho.di.repository;

import org.pentaho.di.i18n.BaseMessages;

public class KettleRepositoryLostException extends RuntimeException {
  
  private static Class<?> PKG = KettleRepositoryLostException.class;

  public KettleRepositoryLostException() {
    super();
  }

  public KettleRepositoryLostException( String message ) {
    super( message );
  }

  public KettleRepositoryLostException( Throwable cause ) {
    super( cause );
  }

  public KettleRepositoryLostException( String message, Throwable cause ) {
    super( message, cause );
  }
  
  public static KettleRepositoryLostException lookupStackStrace( Throwable root ) {
    while( root != null ) {
      if( root instanceof KettleRepositoryLostException ) {
        return (KettleRepositoryLostException)root;
      } else {
        root = root.getCause();
      }
    }
    
    return null;
  }
  
  @Override
  public String getLocalizedMessage() {
    return BaseMessages.getString( PKG, "Repository.Lost.Error.Text" );
  }
}
