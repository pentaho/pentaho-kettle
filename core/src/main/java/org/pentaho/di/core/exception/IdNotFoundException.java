/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core.exception;

import org.pentaho.di.repository.RepositoryObjectType;

/**
 * @author Yury Bakhmutski
 * @since 9-25-2015
 *
 */
public class IdNotFoundException extends KettleException {
  public static final long serialVersionUID = 3337875569693837831L;

  private String objectName;
  private String pathToObject;
  private RepositoryObjectType objectType;

  /**
   * Constructs a new throwable with null as its detail message.
   */
  public IdNotFoundException( String objectName, String pathToObject, RepositoryObjectType objectType ) {
    super();
    this.objectName = objectName;
    this.pathToObject = pathToObject;
    this.objectType = objectType;
  }

  /**
   * Constructs a new throwable with the specified detail message.
   *
   * @param message - the detail message. The detail message is saved for later retrieval by the getMessage() method.
   */
  public IdNotFoundException( String message, String objectName, String pathToObject,
      RepositoryObjectType objectType ) {
    super( message );
    this.objectName = objectName;
    this.pathToObject = pathToObject;
    this.objectType = objectType;
  }

  /**
   * Constructs a new throwable with the specified cause and a detail message of (cause==null ? null : cause.toString())
   * (which typically contains the class and detail message of cause).
   *
   * @param cause the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and
   *              indicates that the cause is nonexistent or unknown.)
   */
  public IdNotFoundException( Throwable cause, String objectName, String pathToObject,
      RepositoryObjectType objectType ) {
    super( cause );
    this.objectName = objectName;
    this.pathToObject = pathToObject;
    this.objectType = objectType;
  }

  /**
   * Constructs a new throwable with the specified detail message and cause.
   *
   * @param message the detail message (which is saved for later retrieval by the getMessage() method).
   * @param cause   the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and
   *                indicates that the cause is nonexistent or unknown.)
   */
  public IdNotFoundException( String message, Throwable cause, String objectName, String pathToObject,
      RepositoryObjectType objectType ) {
    super( message, cause );
    this.objectName = objectName;
    this.pathToObject = pathToObject;
    this.objectType = objectType;
  }

  public String getObjectName() {
    return objectName;
  }

  public String getPathToObject() {
    return pathToObject;
  }

  public RepositoryObjectType getObjectType() {
    return objectType;
  }
}
