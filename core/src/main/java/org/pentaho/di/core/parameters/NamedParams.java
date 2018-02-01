/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.parameters;

/**
 * Interface to implement named parameters.
 *
 * @author Sven Boden
 */
public interface NamedParams {
  /**
   * Add a parameter definition to this set.
   *
   * TODO: default, throw exception
   *
   * @param key
   *          Name of the parameter.
   * @param defValue
   *          default value.
   * @param description
   *          Description of the parameter.
   *
   * @throws DuplicateParamException
   *           Upon duplicate parameter definitions
   */
  void addParameterDefinition( String key, String defValue, String description ) throws DuplicateParamException;

  /**
   * Set the value of a parameter.
   *
   * @param key
   *          key to set value of
   * @param value
   *          value to set it to.
   *
   * @throws UnknownParamException
   *           Parameter 'key' is unknown.
   */
  void setParameterValue( String key, String value ) throws UnknownParamException;

  /**
   * Get the value of a parameter.
   *
   * @param key
   *          Key to get value for.
   *
   * @return value of parameter key.
   *
   * @throws UnknownParamException
   *           Parameter 'key' is unknown.
   */
  String getParameterValue( String key ) throws UnknownParamException;

  /**
   * Get the description of a parameter.
   *
   * @param key
   *          Key to get value for.
   *
   * @return description of parameter key.
   *
   * @throws UnknownParamException
   *           Parameter 'key' is unknown.
   */
  String getParameterDescription( String key ) throws UnknownParamException;

  /**
   * Get the default value of a parameter.
   *
   * @param key
   *          Key to get value for.
   *
   * @return default value for parameter key.
   *
   * @throws UnknownParamException
   *           Parameter 'key' is unknown.
   */
  String getParameterDefault( String key ) throws UnknownParamException;

  /**
   * List the parameters.
   *
   * @return Array of parameters.
   */
  String[] listParameters();

  /**
   * Clear the values.
   */
  void eraseParameters();

  /**
   * Copy params to these named parameters (clearing out first).
   *
   * @param params
   *          the parameters to copy from.
   */
  void copyParametersFrom( NamedParams params );

  /**
   * Merge the given named parameters with current ones.
   *
   * @param params
   *          the parameters to merge with.
   * @param replace
   *          replace if exists
   */
  default void mergeParametersWith( NamedParams params, boolean replace ) {
  }

  /**
   * Activate the currently set parameters
   */
  void activateParameters();

  /**
   * Clear all parameters
   */
  void clearParameters();
}
