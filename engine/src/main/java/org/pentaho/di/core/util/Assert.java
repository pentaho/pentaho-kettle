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

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

/**
 * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
 * @version $Revision
 *
 */
public final class Assert { // NOPMD

  private static final int INPUT_MAX_WIDTH = 30;

  /**
   * Avoid direct creation.
   */
  private Assert() {
    super();
  }

  /**
   * @param input
   *          input to test.
   * @param predicate
   *          predicate to apply.
   * @throws IllegalArgumentException
   *           if predicate rejected input.
   */
  public static void assertTrue( final Object input, final Predicate predicate ) throws IllegalArgumentException {
    if ( predicate.evaluate( input ) ) {
      return;
    }
    final StringBuilder builder = new StringBuilder();
    builder.append( "Predicate rejected input [predicate=" );
    builder.append( predicate );
    builder.append( ", input=" );
    builder.append( StringUtils.abbreviate( String.valueOf( input ), INPUT_MAX_WIDTH ) );
    builder.append( "]" );
    throw new IllegalArgumentException( builder.toString() );
  }

  /**
   * @param bool
   *          boolean to test.
   * @throws IllegalArgumentException
   *           if bool is false.
   */
  public static void assertTrue( final boolean bool ) throws IllegalArgumentException {
    assertTrue( bool, "Value cannot be false" );
  }

  /**
   * @param message
   *          message.
   * @param bool
   *          boolean to test.
   * @param args
   *          arguments to set, optional
   * @throws IllegalArgumentException
   *           if bool is false.
   */
  public static void assertTrue( final boolean bool, final String message, final Object... args ) throws IllegalArgumentException {
    if ( bool ) {
      return;
    }
    if ( args != null && args.length > 0 ) {
      throw new IllegalArgumentException( MessageFormat.format( message, args ) );
    }
    throw new IllegalArgumentException( message );
  }

  /**
   * @param bool
   *          boolean to test.
   * @throws IllegalArgumentException
   *           if bool is true.
   */
  public static void assertFalse( final boolean bool ) throws IllegalArgumentException {
    assertFalse( bool, "Value cannot be true" );
  }

  /**
   * @param bool
   *          boolean to test.
   * @param message
   *          message.
   * @param args
   *          optinal arguments.
   * @throws IllegalArgumentException
   *           if bool is true.
   */
  public static void assertFalse( final boolean bool, final String message, final Object... args ) throws IllegalArgumentException {
    if ( !bool ) {
      return;
    }
    if ( args != null && args.length > 0 ) {
      throw new IllegalArgumentException( MessageFormat.format( message, args ) );
    }
    throw new IllegalArgumentException( message );
  }

  /**
   * @param input
   *          input to test.
   * @param predicate
   *          predicate to apply.
   * @throws IllegalArgumentException
   *           if predicate didn't rejected input.
   */
  public static void assertFalse( final Object input, final Predicate predicate ) throws IllegalArgumentException {
    if ( !predicate.evaluate( input ) ) {
      return;
    }
    final StringBuilder builder = new StringBuilder();
    builder.append( "Predicate didn't rejected input [predicate=" );
    builder.append( predicate );
    builder.append( ", input=" );
    builder.append( StringUtils.abbreviate( String.valueOf( input ), INPUT_MAX_WIDTH ) );
    builder.append( "]" );
    throw new IllegalArgumentException( builder.toString() );
  }

  /**
   * @param collection
   *          collection to test.
   * @throws IllegalArgumentException
   *           if collection is null or empty.
   */
  public static void assertNotNullOrEmpty( final Collection<?> collection ) throws IllegalArgumentException {
    if ( collection == null || collection.isEmpty() ) {
      throw new IllegalArgumentException( "Collection cannot be null or empty" );
    }
  }

  /**
   * @param collection
   *          collection to test.
   * @param message
   *          the message.
   * @throws IllegalArgumentException
   *           if collection is null or empty.
   */
  public static void assertNotNullOrEmpty( final Collection<?> collection, final String message ) throws IllegalArgumentException {
    if ( collection == null || collection.isEmpty() ) {
      throw new IllegalArgumentException( message );
    }
  }

  /**
   * @param array
   *          collection to test.
   * @throws IllegalArgumentException
   *           if collection is null or empty.
   */
  public static void assertNotNullOrEmpty( final Object[] array ) throws IllegalArgumentException {
    if ( array == null || array.length == 0 ) {
      throw new IllegalArgumentException( "Array cannot be null or empty" );
    }
  }

  /**
   * @param array
   *          array to test.
   * @param message
   *          the message.
   * @throws IllegalArgumentException
   *           if collection is null or empty.
   */
  public static void assertNotNullOrEmpty( final Object[] array, final String message ) throws IllegalArgumentException {
    if ( array == null || array.length == 0 ) {
      throw new IllegalArgumentException( message );
    }
  }

  /**
   * @param map
   *          collection to test.
   * @throws IllegalArgumentException
   *           if collection is null or empty.
   */
  public static void assertNotNullOrEmpty( final Map<?, ?> map ) throws IllegalArgumentException {
    if ( map == null || map.isEmpty() ) {
      throw new IllegalArgumentException( "Map cannot be null or empty" );
    }
  }

  /**
   * @param map
   *          map to test.
   * @param message
   *          the message.
   * @throws IllegalArgumentException
   *           if collection is null or empty.
   */
  public static void assertNotNullOrEmpty( final Map<?, ?> map, final String message ) throws IllegalArgumentException {
    if ( map == null || map.isEmpty() ) {
      throw new IllegalArgumentException( message );
    }
  }

  /**
   * @param input
   *          input to test.
   * @throws IllegalArgumentException
   *           if input is null or empty.
   */
  public static void assertNotEmpty( final String input ) throws IllegalArgumentException {
    if ( StringUtils.isEmpty( input ) ) {
      throw new IllegalArgumentException( "Input cannot be null or empty" );
    }
  }

  /**
   * @param input
   *          input to test.
   * @param message
   *          the message.
   * @throws IllegalArgumentException
   *           if input is null or empty.
   */
  public static void assertNotEmpty( final String input, final String message ) throws IllegalArgumentException {
    if ( StringUtils.isEmpty( input ) ) {
      throw new IllegalArgumentException( message );
    }
  }

  /**
   * @param input
   *          input to test.
   * @throws IllegalArgumentException
   *           if input is null or empty.
   */
  public static void assertNotBlank( final String input ) throws IllegalArgumentException {
    if ( StringUtils.isBlank( input ) ) {
      throw new IllegalArgumentException( "Input cannot be null or empty" );
    }
  }

  /**
   * @param input
   *          input to test.
   * @param message
   *          the message.
   * @throws IllegalArgumentException
   *           if input is null or empty.
   */
  public static void assertNotBlank( final String input, final String message ) throws IllegalArgumentException {
    if ( StringUtils.isBlank( input ) ) {
      throw new IllegalArgumentException( message );
    }
  }

  /**
   * @param input
   *          input to test.
   * @throws IllegalArgumentException
   *           if input is null.
   */
  public static void assertNotNull( final Object input ) throws IllegalArgumentException {
    if ( input == null ) {
      throw new IllegalArgumentException( "Input cannot be null" );
    }
  }

  /**
   * @param input
   *          input to test.
   * @param message
   *          the message.
   * @throws IllegalArgumentException
   *           if input is null.
   */
  public static void assertNotNull( final Object input, final String message ) throws IllegalArgumentException {
    if ( input == null ) {
      throw new IllegalArgumentException( message );
    }
  }

  /**
   * @param input
   *          input to test.
   * @throws IllegalArgumentException
   *           if input isn't null.
   */
  public static void assertNull( final Object input ) throws IllegalArgumentException {
    if ( input != null ) {
      throw new IllegalArgumentException( "Input must be null" );
    }
  }

  /**
   * @param input
   *          input to test.
   * @param message
   *          the message.
   * @throws IllegalArgumentException
   *           if input isn't null.
   */
  public static void assertNull( final Object input, final String message ) throws IllegalArgumentException {
    if ( input != null ) {
      throw new IllegalArgumentException( message );
    }
  }

  /**
   * @param value
   *          value to test.
   * @throws IllegalArgumentException
   *           if value is null or <= 0.
   */
  public static void assertGreaterZero( final Integer value ) throws IllegalArgumentException {
    assertGreaterZero( value, "Value must be greater than 0" );
  }

  /**
   * @param value
   *          value to test.
   * @throws IllegalArgumentException
   *           if value is null or <= 0.
   */
  public static void assertGreaterZero( final Double value ) throws IllegalArgumentException {
    assertGreaterZero( value, "Value must be greater than 0" );
  }

  /**
   * @param value
   *          value to test.
   * @param message
   *          the message.
   * @throws IllegalArgumentException
   *           if value is null or <= 0.
   */
  public static void assertGreaterZero( final Double value, final String message ) throws IllegalArgumentException {
    if ( value == null || value <= 0 ) {
      throw new IllegalArgumentException( message );
    }
  }

  /**
   * @param value
   *          value to test.
   * @param message
   *          the message.
   * @throws IllegalArgumentException
   *           if value is null or <= 0.
   */
  public static void assertGreaterZero( final Integer value, final String message ) throws IllegalArgumentException {
    if ( value == null || value <= 0 ) {
      throw new IllegalArgumentException( message );
    }
  }
}
