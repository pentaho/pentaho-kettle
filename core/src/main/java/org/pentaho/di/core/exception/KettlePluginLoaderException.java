/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.exception;

/**
 * This Exception is throws when an error occurs loading plugins.
 *
 * @author Matt
 * @since 9-12-2004
 *
 */
public class KettlePluginLoaderException extends KettleException {
  public static final long serialVersionUID = 0x8D8EA0264F7A1C16L;

  private String pluginId;

  /**
   * Constructs a new throwable with null as its detail message.
   *
   * @param pluginId
   *          The missing plugin id
   */
  public KettlePluginLoaderException( String pluginId ) {
    super();
    this.pluginId = pluginId;
  }

  /**
   * Constructs a new throwable with the specified detail message.
   *
   * @param pluginId
   *          The missing plugin id
   * @param message
   *          - the detail message. The detail message is saved for later retrieval by the getMessage() method.
   */
  public KettlePluginLoaderException( String pluginId, String message ) {
    super( message );
    this.pluginId = pluginId;
  }

  /**
   * Constructs a new throwable with the specified cause and a detail message of (cause==null ? null : cause.toString())
   * (which typically contains the class and detail message of cause).
   *
   * @param pluginId
   *          The missing plugin id
   * @param cause
   *          the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and
   *          indicates that the cause is nonexistent or unknown.)
   */
  public KettlePluginLoaderException( String pluginId, Throwable cause ) {
    super( cause );
    this.pluginId = pluginId;
  }

  /**
   * Constructs a new throwable with the specified detail message and cause.
   *
   * @param pluginId
   *          The missing plugin id
   * @param message
   *          the detail message (which is saved for later retrieval by the getMessage() method).
   * @param cause
   *          the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and
   *          indicates that the cause is nonexistent or unknown.)
   */
  public KettlePluginLoaderException( String pluginId, String message, Throwable cause ) {
    super( message, cause );
    this.pluginId = pluginId;
  }

  /**
   * @return The ID of the missing plugin that caused this exception
   */
  public String getPluginId() {
    return pluginId;
  }

  /**
   * @param pluginId
   *          The ID of the missing plugin that caused this exception
   */
  public void setPluginId( String pluginId ) {
    this.pluginId = pluginId;
  }

}
