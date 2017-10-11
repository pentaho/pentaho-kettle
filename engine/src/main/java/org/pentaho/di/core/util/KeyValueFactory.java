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

package org.pentaho.di.core.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 *
 * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
 *
 * @param <T>
 *          type of key value.
 */
public class KeyValueFactory<T> {

  /**
   * Default instance for ...
   */
  public static final KeyValueFactory<String> STRING = new KeyValueFactory<String>( "" );

  /**
   * Default instance for ...
   */
  public static final KeyValueFactory<Integer> INTEGER = new KeyValueFactory<Integer>( 0 );

  /**
   * Default instance for ...
   */
  public static final KeyValueFactory<Integer> INTEGER_ONE = new KeyValueFactory<Integer>( 1 );

  /**
   * Default instance for ...
   */
  public static final KeyValueFactory<Boolean> BOOLEAN = new KeyValueFactory<Boolean>( Boolean.FALSE );

  /**
   * Default instance for ...
   */
  public static final KeyValueFactory<Boolean> BOOLEAN_TRUE = new KeyValueFactory<Boolean>( Boolean.TRUE );

  /**
   * Default instance for ...
   */
  public static final KeyValueFactory<Float> FLOAT = new KeyValueFactory<Float>( 0.0f );

  /**
   * Default instance for ...
   */
  public static final KeyValueFactory<Float> FLOAT_ONE = new KeyValueFactory<Float>( 1.0f );

  /**
   * Default instance for ...
   */
  public static final KeyValueFactory<Double> DOUBLE = new KeyValueFactory<Double>( 0.0 );

  /**
   * Default instance for ...
   */
  public static final KeyValueFactory<Double> DOUBLE_ONE = new KeyValueFactory<Double>( 1.0 );

  /**
   * Default instance for ...
   */
  public static final KeyValueFactory<Long> LONG = new KeyValueFactory<Long>( 0L );

  /**
   * Default instance for ...
   */
  public static final KeyValueFactory<Long> LONG_ONE = new KeyValueFactory<Long>( 1L );

  private final T defaultValue;

  /**
   * Constructor.
   *
   * @param defaultValue
   *          default value to set.
   */
  public KeyValueFactory( final T defaultValue ) {
    this.defaultValue = defaultValue;
  }

  /**
   * @return the defaultValue
   */
  public T getDefaultValue() {
    return this.defaultValue;
  }

  /**
   * @param key
   *          key to set.
   * @return new key value initialized with default value.
   * @throws IllegalArgumentException
   *           if key is blank.
   */
  public KeyValue<T> create( final String key ) throws IllegalArgumentException {
    return new KeyValue<T>( key, this.defaultValue );
  }

  /**
   * @param keys
   *          keys to use.
   * @return new instances.
   * @throws IllegalArgumentException
   *           if one key is blank.
   */
  public List<KeyValue<T>> createAll( final String... keys ) throws IllegalArgumentException {
    final List<KeyValue<T>> instances = new ArrayList<KeyValue<T>>();
    for ( String key : keys ) {
      instances.add( create( key ) );
    }
    return instances;
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder( this, ToStringStyle.SHORT_PREFIX_STYLE );
    builder.append( "defaultValue", this.defaultValue );
    return builder.toString();
  }

}
