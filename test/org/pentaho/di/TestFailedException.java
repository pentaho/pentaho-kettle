package org.pentaho.di;

public class TestFailedException extends Exception {

  private static final long serialVersionUID = 8585395841938180974L;

  TestFailedException(String message) {
    super(message);
  }
}
