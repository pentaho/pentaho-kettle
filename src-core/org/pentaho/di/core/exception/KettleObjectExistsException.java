package org.pentaho.di.core.exception;


public class KettleObjectExistsException extends KettleException {
  public static final long serialVersionUID = 0x1L;

  /**
   * Constructs a new throwable with null as its detail message.
   */
  public KettleObjectExistsException() {
    super();
  }

  /**
   * Constructs a new throwable with the specified detail message.
   * @param message - the detail message. The detail message is saved for later retrieval by the getMessage() method.
   */
  public KettleObjectExistsException(String message) {
    super(message);
  }

  /**
   * Constructs a new throwable with the specified cause and a detail message of (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause).
   * @param cause the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
   */
  public KettleObjectExistsException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new throwable with the specified detail message and cause.
   * @param message the detail message (which is saved for later retrieval by the getMessage() method).
   * @param cause the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
   */
  public KettleObjectExistsException(String message, Throwable cause) {
    super(message, cause);
  }
}
