/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.repository.pur.model;

import java.util.Date;

import org.pentaho.di.repository.ObjectId;

public class RepositoryLock implements java.io.Serializable {

  private static final long serialVersionUID = -5107186539466626808L; /* EESOURCE: UPDATE SERIALVERUID */
  private ObjectId objectId;
  private String message;
  private String login;
  private String username;
  private Date lockDate;

  /**
   * Create a new repository lock object for the current date/time.
   *
   * @param objectId
   * @param login
   * @param message
   * @param username
   */
  public RepositoryLock( ObjectId objectId, String message, String login, String username ) {
    this( objectId, message, login, username, new Date() ); // now
  }

  /**
   * Create a new repository lock object.
   *
   * @param objectId
   * @param message
   * @param username
   * @param lockDate
   */
  public RepositoryLock( ObjectId objectId, String message, String login, String username, Date lockDate ) {
    this.objectId = objectId;
    this.message = message;
    this.login = login;
    this.username = username;
    this.lockDate = lockDate;
  }

  /**
   * @return the objectId
   */
  public ObjectId getObjectId() {
    return objectId;
  }

  /**
   * @param objectId
   *     the objectId to set
   */
  public void setObjectId( ObjectId objectId ) {
    this.objectId = objectId;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * @param message
   *     the message to set
   */
  public void setMessage( String message ) {
    this.message = message;
  }

  /**
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * @param username
   *     the username to set
   */
  public void setUsername( String username ) {
    this.username = username;
  }

  /**
   * @return the lockDate
   */
  public Date getLockDate() {
    return lockDate;
  }

  /**
   * @param lockDate
   *     the lockDate to set
   */
  public void setLockDate( Date lockDate ) {
    this.lockDate = lockDate;
  }

  /**
   * @return the login
   */
  public String getLogin() {
    return login;
  }

  /**
   * @param login
   *     the login to set
   */
  public void setLogin( String login ) {
    this.login = login;
  }

  @Override public String toString() {
    return getUsername();
  }
}
