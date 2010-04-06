package org.pentaho.di.ui.spoon;

public class InstanceCreationException extends Exception {
  private static final long serialVersionUID = -2151267602926525247L;

  public InstanceCreationException() {
    super();
  }

  public InstanceCreationException(final String message) {
    super(message);
  }

  public InstanceCreationException(final String message, final Throwable reas) {
    super(message, reas);
  }

  public InstanceCreationException(final Throwable reas) {
    super(reas);
  }
}
