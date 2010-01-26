package org.pentaho.di.ui.repository.repositoryexplorer;

public class AccessDeniedException extends Exception {
  private static final long serialVersionUID = -2151267602926525247L;

  public AccessDeniedException() {
    super();
  }

  public AccessDeniedException(final String message) {
    super(message);
  }

  public AccessDeniedException(final String message, final Throwable reas) {
    super(message, reas);
  }

  public AccessDeniedException(final Throwable reas) {
    super(reas);
  }
}
