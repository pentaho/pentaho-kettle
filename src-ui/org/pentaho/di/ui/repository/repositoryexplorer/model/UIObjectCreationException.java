package org.pentaho.di.ui.repository.repositoryexplorer.model;

public class UIObjectCreationException extends Exception {
  private static final long serialVersionUID = -2151267602926525247L;

  public UIObjectCreationException() {
    super();
  }

  public UIObjectCreationException(final String message) {
    super(message);
  }

  public UIObjectCreationException(final String message, final Throwable reas) {
    super(message, reas);
  }

  public UIObjectCreationException(final Throwable reas) {
    super(reas);
  }
}
