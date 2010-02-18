package org.pentaho.di.ui.repository.repositoryexplorer;

public class ControllerInitializationException extends Exception {
  private static final long serialVersionUID = -2151267602926525247L;

  public ControllerInitializationException() {
    super();
  }

  public ControllerInitializationException(final String message) {
    super(message);
  }

  public ControllerInitializationException(final String message, final Throwable reas) {
    super(message, reas);
  }

  public ControllerInitializationException(final Throwable reas) {
    super(reas);
  }
}
