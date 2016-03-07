/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.jdbc;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleSQLException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import static java.sql.Types.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.row.ValueMetaInterface.*;

public class ThinUtilTest {
  @SuppressWarnings( "deprecation" )
  @Test
  public void testFindClauseNullOrEmptyString() throws KettleSQLException {
    assertNull( ThinUtil.findClause( null, null ) );
    assertNull( ThinUtil.findClause( "", null ) );
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void testFindSelectFromNotFound() throws KettleSQLException {
    assertNull( ThinUtil.findClause( "Select * From Test", "WHERE" ) );
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void testFindSelectFromFound() throws KettleSQLException {
    assertEquals( "*", ThinUtil.findClause( "Select * From Test", "SELECT", "FROM" ) );
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void testFindClauseSkipsChars() throws KettleSQLException {
    assertNull( ThinUtil.findClause( "'Select' * From Test", "SELECT", "FROM" ) );
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void testAttemptDateValueExtraction() throws Exception {
    ValueMetaAndData timestamp = ThinUtil.attemptDateValueExtraction( "TIMESTAMP '2014-01-01 00:00:00'" );
    ValueMetaAndData date = ThinUtil.attemptDateValueExtraction( "DATE '2014-01-01'" );

    assertNotNull( timestamp );
    assertEquals( "2014-01-01 00:00:00", timestamp.toString() );

    assertNotNull( date );
    assertEquals( "2014-01-01", date.toString() );
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void testLikePatternMatching() {
    try {
      ThinUtil.like( "foo", null );
      fail( "Null pattern should not be allowed" );
    } catch ( IllegalArgumentException e ) {
      assertNotNull( e );
    }

    assertTrue( "Exact Matching", ThinUtil.like( "foobar", "foobar" ) );

    assertTrue( "_ Matching", ThinUtil.like( "foobar", "f__b_r" ) );
    assertTrue( "* Matching", ThinUtil.like( "foobar", "foo%" ) );

    assertTrue( "Regex Escaping", ThinUtil.like( "foo\\*?[]()bar", "%\\*?[]()%" ) );

    assertFalse( "False Match", ThinUtil.like( "foo", "bar" ) );
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void testGetValueMeta() throws SQLException {
    ValueMetaInterface testValue;
    String expectedName = "testName";
    testValue = ThinUtil.getValueMeta( expectedName, Types.BIGINT );
    assertEquals( expectedName, testValue.getName() );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, testValue.getType() );

    testValue = ThinUtil.getValueMeta( expectedName, Types.INTEGER );
    assertEquals( expectedName, testValue.getName() );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, testValue.getType() );

    testValue = ThinUtil.getValueMeta( expectedName, Types.SMALLINT );
    assertEquals( expectedName, testValue.getName() );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, testValue.getType() );

    testValue = ThinUtil.getValueMeta( expectedName, Types.CHAR );
    assertEquals( expectedName, testValue.getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, testValue.getType() );

    testValue = ThinUtil.getValueMeta( expectedName, Types.VARCHAR );
    assertEquals( expectedName, testValue.getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, testValue.getType() );

    testValue = ThinUtil.getValueMeta( expectedName, Types.CLOB );
    assertEquals( expectedName, testValue.getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, testValue.getType() );

    testValue = ThinUtil.getValueMeta( expectedName, Types.DATE );
    assertEquals( expectedName, testValue.getName() );
    assertEquals( ValueMetaInterface.TYPE_DATE, testValue.getType() );

    testValue = ThinUtil.getValueMeta( expectedName, Types.TIMESTAMP );
    assertEquals( expectedName, testValue.getName() );
    assertEquals( ValueMetaInterface.TYPE_DATE, testValue.getType() );

    testValue = ThinUtil.getValueMeta( expectedName, Types.TIME );
    assertEquals( expectedName, testValue.getName() );
    assertEquals( ValueMetaInterface.TYPE_DATE, testValue.getType() );

    testValue = ThinUtil.getValueMeta( expectedName, Types.DECIMAL );
    assertEquals( expectedName, testValue.getName() );
    assertEquals( ValueMetaInterface.TYPE_BIGNUMBER, testValue.getType() );

    testValue = ThinUtil.getValueMeta( expectedName, Types.DOUBLE );
    assertEquals( expectedName, testValue.getName() );
    assertEquals( ValueMetaInterface.TYPE_NUMBER, testValue.getType() );

    testValue = ThinUtil.getValueMeta( expectedName, Types.FLOAT );
    assertEquals( expectedName, testValue.getName() );
    assertEquals( ValueMetaInterface.TYPE_NUMBER, testValue.getType() );

    testValue = ThinUtil.getValueMeta( expectedName, Types.BOOLEAN );
    assertEquals( expectedName, testValue.getName() );
    assertEquals( ValueMetaInterface.TYPE_BOOLEAN, testValue.getType() );

    testValue = ThinUtil.getValueMeta( expectedName, Types.BIT );
    assertEquals( expectedName, testValue.getName() );
    assertEquals( ValueMetaInterface.TYPE_BOOLEAN, testValue.getType() );

    testValue = ThinUtil.getValueMeta( expectedName, Types.BINARY );
    assertEquals( expectedName, testValue.getName() );
    assertEquals( ValueMetaInterface.TYPE_BINARY, testValue.getType() );

    testValue = ThinUtil.getValueMeta( expectedName, Types.BLOB );
    assertEquals( expectedName, testValue.getName() );
    assertEquals( ValueMetaInterface.TYPE_BINARY, testValue.getType() );

    testValue = ThinUtil.getValueMeta( expectedName, Types.OTHER );
    assertEquals( expectedName, testValue.getName() );
    assertEquals( ValueMetaInterface.TYPE_NONE, testValue.getType() );

    testValue = ThinUtil.getValueMeta( expectedName, Types.NULL );
    assertEquals( expectedName, testValue.getName() );
    assertEquals( ValueMetaInterface.TYPE_NONE, testValue.getType() );

    try {
      ThinUtil.getValueMeta( expectedName, Integer.MIN_VALUE );
      fail();
    } catch ( SQLException expected ) {
      // Do nothing, there is no SQL Type for Integer.MIN_VALUE, an exception was thrown as expected.
    }
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void checkValueMetaTypeToSqlTypeConsistency() throws SQLException {
    class TypeMap {
      ValueMetaInterface valueMeta;
      int sqlType;
      String sqlDesc;
      TypeMap( int valueMetaType, int sqlType, String desc ) {
        this.valueMeta = mock( ValueMetaInterface.class );
        when( valueMeta.getType() ).thenReturn( valueMetaType );
        this.sqlType = sqlType;
        this.sqlDesc = desc;
      }
    }
    TypeMap[] typeMaps = new TypeMap[] {
      new TypeMap( TYPE_STRING,    VARCHAR,   "VARCHAR" ),
      new TypeMap( TYPE_DATE,      TIMESTAMP, "TIMESTAMP" ),
      new TypeMap( TYPE_INTEGER,   BIGINT,    "BIGINT" ),
      new TypeMap( TYPE_NUMBER,    DOUBLE,    "DOUBLE" ),
      new TypeMap( TYPE_BIGNUMBER, DECIMAL,   "DECIMAL" ),
      new TypeMap( TYPE_BOOLEAN,   BOOLEAN,   "BOOLEAN" ),
      new TypeMap( TYPE_BINARY,    BLOB,      "BLOB" ),
      new TypeMap( TYPE_NONE,      OTHER,     "OTHER" ),
    };
    for ( TypeMap map : typeMaps ) {
      assertEquals( map.sqlDesc, ThinUtil.getSqlTypeDesc( map.valueMeta ) );
      assertEquals( map.sqlType, ThinUtil.getSqlType( map.valueMeta ) );
      assertEquals( map.valueMeta.getType(), ThinUtil.getValueMeta( "test", map.sqlType ).getType() );
    }
  }


  @SuppressWarnings( "deprecation" )
  @Test
  public void testAttemptDateValueExtraction2() throws KettleValueException {
    ValueMetaAndData result = ThinUtil.attemptDateValueExtraction( "[2015/01/02 03:04:56.789]" );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_DATE, result.getValueMeta().getType() );
    assertEquals( "2015/01/02 03:04:56.789", result.getValueMeta().getString( result.getValueData() ) );

    //assertNull( ThinUtil.attemptDateValueExtraction( null ) );
    assertNull( ThinUtil.attemptDateValueExtraction( "" ) );
    assertNull( ThinUtil.attemptDateValueExtraction( "[]" ) );
    assertNull( ThinUtil.attemptDateValueExtraction( "[notadate]" ) );
    assertNull( ThinUtil.attemptDateValueExtraction( "2015/01/02 03:04:56.789" ) );
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void testAttemptIntegerValueExtraction() {
    ValueMetaAndData result = ThinUtil.attemptIntegerValueExtraction( "12345" );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, result.getValueMeta().getType() );
    assertEquals( 12345L, result.getValueData() );

    //assertNull( ThinUtil.attemptIntegerValueExtraction( null ) );
    assertNull( ThinUtil.attemptIntegerValueExtraction( "" ) );
    assertNull( ThinUtil.attemptIntegerValueExtraction( "123.45" ) );
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void testAttemptNumberValueExtraction() {
    ValueMetaAndData result = ThinUtil.attemptNumberValueExtraction( "12345.678" );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_NUMBER, result.getValueMeta().getType() );
    assertEquals( 12345.678, result.getValueData() );

    //assertNull( ThinUtil.attemptNumberValueExtraction( null ) );
    assertNull( ThinUtil.attemptNumberValueExtraction( "" ) );
    assertNull( ThinUtil.attemptNumberValueExtraction( "abcde" ) );
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void testAttemptBigNumberValueExtraction() {
    ValueMetaAndData result = ThinUtil.attemptBigNumberValueExtraction( "1234567890123456789.0987654321" );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_BIGNUMBER, result.getValueMeta().getType() );
    assertEquals( new BigDecimal( "1234567890123456789.0987654321" ), result.getValueData() );

    //assertNull( ThinUtil.attemptBigNumberValueExtraction( null ) );
    assertNull( ThinUtil.attemptBigNumberValueExtraction( "" ) );
    assertNull( ThinUtil.attemptBigNumberValueExtraction( "abcde" ) );
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void testAttemptStringValueExtraction() {
    ValueMetaAndData result = ThinUtil.attemptStringValueExtraction( "'testValue'" );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_STRING, result.getValueMeta().getType() );
    assertEquals( "testValue", result.getValueData() );

    result = ThinUtil.attemptStringValueExtraction( "'test\'\'Value'" );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_STRING, result.getValueMeta().getType() );
    assertEquals( "test\'Value", result.getValueData() );

    //assertNull( ThinUtil.attemptStringValueExtraction( null ) );
    assertNull( ThinUtil.attemptStringValueExtraction( "" ) );
    assertNull( ThinUtil.attemptStringValueExtraction( "abcde" ) );
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void testAttemptBooleanValueExtraction() {
    ValueMetaAndData result = ThinUtil.attemptBooleanValueExtraction( "TrUe" );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_BOOLEAN, result.getValueMeta().getType() );
    assertEquals( Boolean.TRUE, result.getValueData() );

    result = ThinUtil.attemptBooleanValueExtraction( "fAlSe" );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_BOOLEAN, result.getValueMeta().getType() );
    assertEquals( Boolean.FALSE, result.getValueData() );

    assertNull( ThinUtil.attemptBooleanValueExtraction( null ) );
    assertNull( ThinUtil.attemptBooleanValueExtraction( "" ) );
    assertNull( ThinUtil.attemptBooleanValueExtraction( "abcde" ) );
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void testStripQuotesIfNoWhitespace() {
    // map of tests to expected result
    Map<String, String> testStrings = new ImmutableMap.Builder<String, String>()
      .put( "\"quotedNospace\"",                   "quotedNospace" )
      .put( "unquoted",                            "unquoted" )
      .put( "\"quoted space\"",                    "\"quoted space\"" )
      .put( "\"field\" \"fieldAlias\"",            "\"field\" \"fieldAlias\"" )
      .put( "\"field with space\" \"fieldAlias\"", "\"field with space\" \"fieldAlias\"" )
      .put( "\"field\" AS \"field Alias\"",        "\"field\" AS \"field Alias\"" )
      .build();

    for ( String test : testStrings.keySet() ) {
      for ( char quote : new char[] { '\'', '"', '`' } ) {
        assertEquals(
          testStrings.get( test ).replace( '"', quote ),
          ThinUtil.stripQuotesIfNoWhitespace( test.replace( '"', quote ), quote ) );
      }
    }
  }
}
