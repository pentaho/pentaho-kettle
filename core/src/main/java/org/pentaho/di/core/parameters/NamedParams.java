/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
