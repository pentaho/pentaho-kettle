package org.pentaho.di.core.auth.core;

import org.pentaho.di.core.exception.KettleException;

public class AuthenticationFactoryException extends KettleException {
  private static final long serialVersionUID = -7649037092966810244L;

  public AuthenticationFactoryException( String message ) {
    super( message );
  }

  public AuthenticationFactoryException( String message, Throwable cause ) {
    super( message, cause );
  }

  public AuthenticationFactoryException( Throwable cause ) {
    super( cause );
  }
}
