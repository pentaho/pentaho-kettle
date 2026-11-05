package org.pentaho.di.www.exception;

import org.pentaho.di.core.exception.KettleException;

public class ExecutionNotAllowedException extends KettleException {
  private static final long serialVersionUID = 1L;

  public ExecutionNotAllowedException() {
    super();
  }

  public ExecutionNotAllowedException( String message ) {
    super( message );
  }

  public ExecutionNotAllowedException( String message, Throwable cause ) {
    super( message, cause );
  }

  public ExecutionNotAllowedException( Throwable cause ) {
    super( cause );
  }
}
