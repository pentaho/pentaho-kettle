package org.pentaho.di.core;

public class DomainObjectCreationException extends Exception {
  private static final long serialVersionUID = -2151267602926525247L;

  public DomainObjectCreationException() {
    super();
  }

  public DomainObjectCreationException(final String message) {
    super(message);
  }

  public DomainObjectCreationException(final String message, final Throwable reas) {
    super(message, reas);
  }

  public DomainObjectCreationException(final Throwable reas) {
    super(reas);
  }
}
