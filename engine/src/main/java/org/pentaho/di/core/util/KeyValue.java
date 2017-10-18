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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
 *
 * @param <T>
 *          type of value
 */
public class KeyValue<T> implements Serializable {

  /**
   * The default true values.
   */
  public static final List<String> DEFAULT_TRUE_VALUES = Arrays.asList( new String[] { "j", "y", "on", "true" } );

  /**
   * Valid key characters.
   */
  public static final String VALID_KEY_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789_-";

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = -6847244072467344205L;

  private final String key;

  private T value; // NOPMD

  /**
   * Constructor. Key will be converted to lower case.
   *
   * @param key
   *          key to set.
   * @param value
   *          value to set, may be null.
   * @throws IllegalArgumentException
   *           if key is invalid.
   */
  public KeyValue( final String key, final T value ) throws IllegalArgumentException {
    final String keyToSet = StringUtils.lowerCase( key );
    assertKey( keyToSet );
    this.key = keyToSet;
    this.value = value;
  }

  /**
   * Constructor. Key will be converted to lower case. Value is null.
   *
   * @param key
   *          key to set.
   * @throws IllegalArgumentException
   *           if key is invalid.
   */
  public KeyValue( final String key ) throws IllegalArgumentException {
    this( key, null );
  }

  /**
   * @param lowerKey
   *          key to test.
   * @throws IllegalArgumentException
   *           if key is invalid.
   */
  public static final void assertKey( final String lowerKey ) throws IllegalArgumentException {
    Assert.assertNotEmpty( lowerKey, "Key cannot be null or empty" );
    if ( !StringUtils.containsOnly( lowerKey, VALID_KEY_CHARS ) ) {
      throw new IllegalArgumentException( "Key contains invalid characters [validKeyCharacters="
        + VALID_KEY_CHARS + "]" );
    }
    if ( lowerKey.charAt( 0 ) == '-' ) {
      throw new IllegalArgumentException( "Key must not start with '-'" );
    }
    if ( lowerKey.endsWith( "-" ) ) {
      throw new IllegalArgumentException( "Key must not end with '-'" );
    }
    if ( "_".equals( lowerKey ) ) {
      throw new IllegalArgumentException( "Key must not be  '_'" );
    }
  }

  /**
   * @return the key, never null.
   */
  public String getKey() {
    return this.key;
  }

  /**
   * @return the value
   */
  public T getValue() {
    return this.value;
  }

  /**
   * @param value
   *          the value to set
   */
  public void setValue( final T value ) {
    this.value = value;
  }

  /**
   * @param newValue
   *          value to set.
   * @return this.
   */
  public KeyValue<T> value( final T newValue ) {
    this.value = newValue;
    return this;
  }

  /**
   * @return value.
   */
  public T value() {
    return this.value;
  }

  /**
   * @param trueValues
   *          string true values, case is ignored.
   * @return boolean value, null if value is null.
   */
  public Boolean booleanValue( final String... trueValues ) {
    return this.booleanValue( Arrays.asList( trueValues ) );
  }

  /**
   * @param trueValues
   *          string true values, case is ignored.
   * @return boolean value, null if value is null.
   */
  public Boolean booleanValue( final List<String> trueValues ) {
    return this.booleanValue( trueValues, true );
  }

  /**
   * @param trueValues
   *          string true values.
   * @param ignoreCase
   *          ignore case?
   * @return boolean value, null if value is null.
   */
  public Boolean booleanValue( final List<String> trueValues, final boolean ignoreCase ) {
    if ( this.value == null ) {
      return null;
    }
    if ( this.value instanceof Boolean ) {
      return (Boolean) this.value;
    }
    final String stringValue = this.stringValue();
    if ( ignoreCase ) {
      return trueValues.contains( StringUtils.lowerCase( stringValue ) );
    }
    return trueValues.contains( stringValue );
  }

  /**
   * Uses DEFAULT_TRUE_VALUES, ignore case.
   *
   * @return boolean value or null if value is null.
   */
  public Boolean booleanValue() {
    return this.booleanValue( DEFAULT_TRUE_VALUES, true );
  }

  /**
   * @param defaultValue
   *          the default value
   * @return boolean value or default value if value is null.
   */
  public Boolean booleanValue( final Boolean defaultValue ) {
    final Boolean returnValue = this.booleanValue();
    if ( returnValue == null ) {
      return defaultValue;
    }
    return returnValue;
  }

  /**
   * @return string value or null if value is null.
   */
  public String stringValue() {
    if ( this.value == null ) {
      return null;
    }
    if ( this.value instanceof String ) {
      return (String) this.value;
    }
    return String.valueOf( this.value );
  }

  /**
   * @param defaultValue
   *          the default value.
   * @return string value or default value if value is null.
   */
  public String stringValue( final String defaultValue ) {
    final String returnValue = this.stringValue();
    if ( returnValue == null ) {
      return defaultValue;
    }
    return returnValue;
  }

  /**
   * @param defaultValue
   *          the default value.
   * @return string value or default value if value is blank.
   */
  public String stringValueDefaultIfBlank( final String defaultValue ) {
    final String returnValue = this.stringValue();
    if ( StringUtils.isBlank( returnValue ) ) {
      return defaultValue;
    }
    return returnValue;
  }

  /**
   * @return integer value or null if value is null.
   * @throws NumberFormatException
   *           if string value of value cannot be converted to Integer
   */
  public Integer integerValue() throws NumberFormatException {
    if ( this.value == null ) {
      return null;
    }
    if ( this.value instanceof Integer ) {
      return (Integer) this.value;
    }
    return Integer.valueOf( String.valueOf( this.value ) );
  }

  /**
   * @param defaultValue
   *          the default value.
   * @return integer value or default value if value is null or cannot be converted to integer.
   */
  public Integer integerValue( final Integer defaultValue ) {
    if ( this.value == null ) {
      return defaultValue;
    }
    try {
      return this.integerValue();
    } catch ( NumberFormatException e ) {
      return defaultValue;
    }
  }

  /**
   * @return long value or null if value is null.
   * @throws NumberFormatException
   *           if string value of value cannot be converted to Long
   */
  public Long longValue() throws NumberFormatException {
    if ( this.value == null ) {
      return null;
    }
    if ( this.value instanceof Long ) {
      return (Long) this.value;
    }
    return Long.valueOf( String.valueOf( this.value ) );
  }

  /**
   * @param defaultValue
   *          the default value.
   * @return long value or default value if value is null or cannot be converted to long.
   */
  public Long longValue( final Long defaultValue ) {
    if ( this.value == null ) {
      return defaultValue;
    }
    try {
      return this.longValue();
    } catch ( NumberFormatException e ) {
      return defaultValue;
    }
  }

  /**
   * @return double value or null if value is null.
   * @throws NumberFormatException
   *           if string value of value cannot be converted to Double
   */
  public Double doubleValue() throws NumberFormatException {
    if ( this.value == null ) {
      return null;
    }
    if ( this.value instanceof Double ) {
      return (Double) this.value;
    }
    return Double.valueOf( String.valueOf( this.value ) );
  }

  /**
   * @param defaultValue
   *          the default value.
   * @return double value or default value if value is null or cannot be converted to double.
   */
  public Double doubleValue( final Double defaultValue ) {
    if ( this.value == null ) {
      return defaultValue;
    }
    try {
      return this.doubleValue();
    } catch ( NumberFormatException e ) {
      return defaultValue;
    }
  }

  /**
   * @return float value or null if value is null.
   * @throws NumberFormatException
   *           if string value of value cannot be converted to Float
   */
  public Float floatValue() throws NumberFormatException {
    if ( this.value == null ) {
      return null;
    }
    if ( this.value instanceof Float ) {
      return (Float) this.value;
    }
    return Float.valueOf( String.valueOf( this.value ) );
  }

  /**
   * @param defaultValue
   *          the default value.
   * @return float value or default value if value is null or cannot be converted to float.
   */
  public Float floatValue( final Float defaultValue ) {
    if ( this.value == null ) {
      return defaultValue;
    }
    try {
      return this.floatValue();
    } catch ( NumberFormatException e ) {
      return defaultValue;
    }
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append( KeyValue.class.getSimpleName() );
    builder.append( '(' );
    builder.append( this.key );
    if ( this.value == null ) {
      builder.append( "<null>)" );
    } else {
      builder.append( "=[" );
      builder.append( this.value );
      builder.append( "])" );
    }
    return builder.toString();
  }

}
