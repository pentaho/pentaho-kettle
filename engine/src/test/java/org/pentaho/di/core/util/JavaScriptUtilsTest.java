/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.pentaho.di.compatibility.Value;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.math.BigDecimal;
import java.util.Date;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * A set of tests for {@linkplain JavaScriptUtils} class.
 *
 * @author Andrey Khayrutdinov
 */
public class JavaScriptUtilsTest {

  private static final String UNDEFINED = Undefined.class.getName();
  private static final String JAVA_OBJECT = NativeJavaObject.class.getName();
  private static final String NATIVE_NUMBER = "org.mozilla.javascript.NativeNumber";

  private static final BigDecimal BIG_DECIMAL_ONE_DOT_ZERO = BigDecimal.valueOf( 1.0 );
  private static final Long LONG_ONE = 1L;
  private static final Double DOUBLE_ONE = 1.0;
  private static final double A_POSITIVE_DOUBLE = 90.01;
  private static final BigDecimal A_POSITIVE_BIG_DECIMAL = BigDecimal.valueOf( A_POSITIVE_DOUBLE );
  private static final double A_NEGATIVE_DOUBLE = -0.0102030405;
  private static final BigDecimal A_NEGATIVE_BIG_DECIMAL = BigDecimal.valueOf( A_NEGATIVE_DOUBLE );
  private static final double ALMOST_ONE_DOUBLE = 0.999999999999;
  private static final BigDecimal ALMOST_ONE_BIG_DECIMAL = BigDecimal.valueOf( ALMOST_ONE_DOUBLE );
  private static final double ALMOST_MINUS_ONE_DOUBLE = -1.00000000001;
  private static final BigDecimal ALMOST_MINUS_ONE_BIG_DECIMAL = BigDecimal.valueOf( ALMOST_MINUS_ONE_DOUBLE );
  private static final double ASSERT_DELTA = 1e-6;

  private static Context ctx;
  private static ScriptableObject scope;

  @BeforeClass
  public static void setUp() throws Exception {
    ctx = Context.enter();
    scope = ctx.initStandardObjects();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    scope = null;
    ctx = null;
    Context.exit();
  }


  private static Scriptable getIntValue() {
    Value value = new Value();
    value.setValue( 1 );
    return Context.toObject( value, scope );
  }

  private static Scriptable getDoubleValue() {
    Value value = new Value();
    value.setValue( 1.0 );
    return Context.toObject( value, scope );
  }

  // jsToNumber tests

  @Test
  public void jsToNumber_Undefined() throws Exception {
    assertNull( JavaScriptUtils.jsToNumber( null, UNDEFINED ) );
  }

  @Test
  public void jsToNumber_NativeJavaObject_Double() throws Exception {
    Scriptable value = getDoubleValue();
    Number number = JavaScriptUtils.jsToNumber( value, JAVA_OBJECT );
    assertEquals( DOUBLE_ONE, number );
  }

  @Test
  public void jsToNumber_NativeJavaObject_Int() throws Exception {
    Scriptable value = getIntValue();
    assertEquals( DOUBLE_ONE, JavaScriptUtils.jsToNumber( value, JAVA_OBJECT ) );
  }

  @Test
  public void jsToNumber_NativeNumber() throws Exception {
    Scriptable value = Context.toObject( 1.0, scope );
    assertEquals( DOUBLE_ONE, JavaScriptUtils.jsToNumber( value, NATIVE_NUMBER ) );
  }

  @Test
  public void jsToNumber_JavaNumber() throws Exception {
    Number number = JavaScriptUtils.jsToNumber( 1.0, Double.class.getName() );
    assertEquals( DOUBLE_ONE, number.doubleValue(), ASSERT_DELTA);
  }

  // jsToInteger tests

  @Test
  public void jsToInteger_Undefined() throws Exception {
    assertNull( JavaScriptUtils.jsToInteger( null, Undefined.class ) );
  }

  @Test
  public void jsToInteger_NaturalNumbers() throws Exception {
    Number[] naturalNumbers = new Number[] { (byte) 1, (short) 1, 1, (long) 1 };

    for ( Number number : naturalNumbers ) {
      assertEquals( LONG_ONE, JavaScriptUtils.jsToInteger( number, number.getClass() ) );
    }
  }

  @Test
  public void jsToInteger_String() throws Exception {
    assertEquals( LONG_ONE, JavaScriptUtils.jsToInteger( "1", String.class ) );
  }

  @Test( expected = NumberFormatException.class )
  public void jsToInteger_String_Unparseable() throws Exception {
    JavaScriptUtils.jsToInteger( "q", String.class );
  }

  @Test
  public void jsToInteger_Double() throws Exception {
    assertEquals( LONG_ONE, JavaScriptUtils.jsToInteger( 1.0, Double.class ) );
  }

  @Test
  public void jsToInteger_NativeJavaObject_Int() throws Exception {
    Scriptable value = getIntValue();
    assertEquals( LONG_ONE, JavaScriptUtils.jsToInteger( value, NativeJavaObject.class ) );
  }

  @Test
  public void jsToInteger_NativeJavaObject_Double() throws Exception {
    Scriptable value = getDoubleValue();
    assertEquals( LONG_ONE, JavaScriptUtils.jsToInteger( value, NativeJavaObject.class ) );
  }

  @Test
  public void jsToInteger_Other_Int() throws Exception {
    assertEquals( LONG_ONE, JavaScriptUtils.jsToInteger( 1, getClass() ) );
  }

  @Test( expected = NumberFormatException.class )
  public void jsToInteger_Other_String() throws Exception {
    JavaScriptUtils.jsToInteger( "qwerty", getClass() );
  }

  // jsToString tests

  @Test
  public void jsToString_Undefined() throws Exception {
    assertEquals( "null", JavaScriptUtils.jsToString( null, UNDEFINED ) );
  }

  @Test
  public void jsToString_NativeJavaObject_Int() throws Exception {
    assertEquals( "1", JavaScriptUtils.jsToString( getIntValue(), JAVA_OBJECT ).trim() );
  }

  @Test
  public void jsToString_NativeJavaObject_Double() throws Exception {
    assertEquals( "1.0", JavaScriptUtils.jsToString( getDoubleValue(), JAVA_OBJECT ).trim() );
  }

  @Test
  public void jsToString_String() throws Exception {
    assertEquals( "qwerty", JavaScriptUtils.jsToString( "qwerty", String.class.getName() ) );
  }

  // jsToDate tests

  @Test
  public void jsToDate_Undefined() throws Exception {
    assertNull( JavaScriptUtils.jsToDate( null, UNDEFINED ) );
  }

  @Test
  public void jsToDate_NativeDate() throws Exception {
    Date date = new Date( 1 );
    Scriptable value = ctx.newObject( scope, "Date", new Object[] { date.getTime() } );
    assertEquals( date, JavaScriptUtils.jsToDate( value, "org.mozilla.javascript.NativeDate" ) );
  }

  @Test
  public void jsToDate_NativeJavaObject() throws Exception {
    Scriptable value = getIntValue();
    assertEquals( new Date( 1 ), JavaScriptUtils.jsToDate( value, JAVA_OBJECT ) );
  }

  @Test
  public void jsToDate_Double() throws Exception {
    assertEquals( new Date( 1 ), JavaScriptUtils.jsToDate( 1.0, Double.class.getName() ) );
  }

  @Test
  public void jsToDate_String() throws Exception {
    assertEquals( new Date( 1 ), JavaScriptUtils.jsToDate( "1.0", String.class.getName() ) );
  }

  @Test( expected = NumberFormatException.class )
  public void jsToDate_String_Unparseable() throws Exception {
    JavaScriptUtils.jsToDate( "qwerty", String.class.getName() );
  }

  // jsToBigNumber tests

  @Test
  public void jsToBigNumber_Undefined() throws Exception {
    assertNull( JavaScriptUtils.jsToBigNumber( null, UNDEFINED ) );
    Object objMock = mock( Object.class );
    assertNull( JavaScriptUtils.jsToBigNumber( objMock, UNDEFINED ) );
  }

  @Test
  public void jsToBigNumber_NativeNumber() throws Exception {
    Scriptable value = Context.toObject( A_POSITIVE_DOUBLE, scope );
    assertEquals( A_POSITIVE_BIG_DECIMAL, JavaScriptUtils.jsToBigNumber( value, NATIVE_NUMBER ) );

    value = Context.toObject( A_NEGATIVE_DOUBLE, scope );
    assertEquals( A_NEGATIVE_BIG_DECIMAL, JavaScriptUtils.jsToBigNumber( value, NATIVE_NUMBER ) );

    value = Context.toObject( ALMOST_ONE_DOUBLE, scope );
    assertEquals( ALMOST_ONE_BIG_DECIMAL, JavaScriptUtils.jsToBigNumber( value, NATIVE_NUMBER ) );

    value = Context.toObject( ALMOST_MINUS_ONE_DOUBLE, scope );
    assertEquals( ALMOST_MINUS_ONE_BIG_DECIMAL, JavaScriptUtils.jsToBigNumber( value, NATIVE_NUMBER ) );
  }

  @Test
  public void jsToBigNumber_NativeJavaObject_Int() throws Exception {
    assertEquals( BigDecimal.ONE, JavaScriptUtils.jsToBigNumber( getIntValue(), JAVA_OBJECT ) );
  }

  @Test
  public void jsToBigNumber_NativeJavaObject_Double() throws Exception {
    assertEquals( BIG_DECIMAL_ONE_DOT_ZERO, JavaScriptUtils.jsToBigNumber( getDoubleValue(), JAVA_OBJECT ) );
  }

  @Test
  public void jsToBigNumber_NativeJavaObject_BigDecimal() throws Exception {
    Value value = new Value();
    value.setValue( BigDecimal.ONE );
    Scriptable object = Context.toObject( value, scope );
    assertEquals( BigDecimal.ONE, JavaScriptUtils.jsToBigNumber( object, JAVA_OBJECT ) );
  }

  @Test
  public void jsToBigNumber_NaturalNumbers() throws Exception {
    Number[] naturalNumbers = new Number[] { (byte) 1, (short) 1, 1, (long) 1 };

    for ( Number number : naturalNumbers ) {
      assertEquals( BigDecimal.ONE, JavaScriptUtils.jsToBigNumber( number, number.getClass().getName() ) );
    }
  }

  @Test
  public void jsToBigNumber_Double() throws Exception {
    assertEquals( BIG_DECIMAL_ONE_DOT_ZERO, JavaScriptUtils.jsToBigNumber( 1.0, Double.class.getName() ) );
  }

  @Test
  public void jsToBigNumber_String() throws Exception {
    assertEquals( BigDecimal.ONE, JavaScriptUtils.jsToBigNumber( "1", String.class.getName() ) );
  }

  @Test( expected = RuntimeException.class )
  public void jsToBigNumber_UnknownClass() throws Exception {
    JavaScriptUtils.jsToBigNumber( "1", "qwerty" );
  }

  // convertFromJs tests

  @Test( expected = RuntimeException.class )
  public void convertFromJs_TypeNone() throws Exception {
    JavaScriptUtils.convertFromJs( null, ValueMetaInterface.TYPE_NONE, "qwerty" );
  }

  @Test
  public void convertFromJs_TypeBoolean() throws Exception {
    Object o = new Object();
    Object o2 = JavaScriptUtils.convertFromJs( o, ValueMetaInterface.TYPE_BOOLEAN, "qwerty" );
    assertEquals( o, o2 );
  }

  @Test
  public void convertFromJs_TypeBinary() throws Exception {
    byte[] bytes = new byte[] { 0, 1 };
    Object converted = JavaScriptUtils.convertFromJs( bytes, ValueMetaInterface.TYPE_BINARY, "qwerty" );
    assertThat( converted, is( instanceOf( byte[].class ) ) );
    assertArrayEquals( bytes, (byte[]) converted );
  }
}
