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

package org.pentaho.di.repository;

import org.pentaho.di.core.exception.KettleException;

/**
 * This interface allows you to pass a simple interface to an object to allow it to store or load itself from or to any
 * type of repository in a generic fashion.
 *
 * @author matt
 *
 */
public interface RepositoryAttributeInterface {

  /**
   * Set a String attribute
   *
   * @param code
   * @param value
   * @throws KettleException
   */
  public void setAttribute( String code, String value ) throws KettleException;

  /**
   * Get a string attribute. If the attribute is not found, return null
   *
   * @param code
   * @return
   * @throws KettleException
   */
  public String getAttributeString( String code ) throws KettleException;

  /**
   * Set a boolean attribute
   *
   * @param code
   * @param value
   * @throws KettleException
   */
  public void setAttribute( String code, boolean value ) throws KettleException;

  /**
   * Get a boolean attribute, if the attribute is not found, return false;
   *
   * @param code
   * @return
   * @throws KettleException
   */
  public boolean getAttributeBoolean( String code ) throws KettleException;

  /**
   * Set an integer attribute
   *
   * @param code
   * @param value
   * @throws KettleException
   */
  public void setAttribute( String code, long value ) throws KettleException;

  /**
   * Get an integer attribute. If the attribute is not found, return 0;
   *
   * @param code
   * @return
   * @throws KettleException
   */
  public long getAttributeInteger( String code ) throws KettleException;
}
