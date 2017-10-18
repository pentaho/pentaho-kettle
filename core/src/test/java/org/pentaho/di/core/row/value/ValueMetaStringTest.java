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

package org.pentaho.di.core.row.value;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;

import junit.framework.Assert;

import static org.junit.Assert.assertEquals;

public class ValueMetaStringTest {
  private static final String BASE_VALUE = "Some text";
  private static final String TEST_VALUE = "Some text";

  private ConfigurableMeta meta;

  @Before
  public void setUp() {
    meta = new ConfigurableMeta( BASE_VALUE );
  }

  @After
  public void tearDown() {
    meta = null;
  }

  @Test
  public void testGetNativeData_emptyIsNotNull() throws Exception {
    meta.setNullsAndEmptyAreDifferent( true );

    assertEquals( BASE_VALUE, meta.getNativeDataType( BASE_VALUE ) );
    assertEquals( TEST_VALUE, meta.getNativeDataType( TEST_VALUE ) );
    assertEquals( null, meta.getNativeDataType( null ) );
    assertEquals( "1", meta.getNativeDataType( 1 ) );
    assertEquals( "1.0", meta.getNativeDataType( 1.0 ) );

    Date d = ( new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" ) ).parse( "2012-11-10 09:08:07.654" );
    assertEquals( d.toString(), meta.getNativeDataType( d ) );

    Timestamp ts = Timestamp.valueOf( "2012-11-10 09:08:07.654321" );
    assertEquals( "2012-11-10 09:08:07.654321", meta.getNativeDataType( ts ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
    assertEquals( "", meta.getNativeDataType( "" ) );
    assertEquals( "1", meta.getNativeDataType( "1" ) );
    assertEquals( "    ", meta.getNativeDataType( "    " ) );
    assertEquals( "  1  ", meta.getNativeDataType( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_LEFT );
    assertEquals( "", meta.getNativeDataType( "" ) );
    assertEquals( "1", meta.getNativeDataType( "1" ) );
    assertEquals( "", meta.getNativeDataType( "    " ) );
    assertEquals( "1  ", meta.getNativeDataType( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_RIGHT );
    assertEquals( "", meta.getNativeDataType( "" ) );
    assertEquals( "1", meta.getNativeDataType( "1" ) );
    assertEquals( "", meta.getNativeDataType( "    " ) );
    assertEquals( "  1", meta.getNativeDataType( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_BOTH );
    assertEquals( "", meta.getNativeDataType( "" ) );
    assertEquals( "1", meta.getNativeDataType( "1" ) );
    assertEquals( "", meta.getNativeDataType( "    " ) );
    assertEquals( "1", meta.getNativeDataType( "  1  " ) );
  }

  @Test
  public void testGetNativeData_emptyIsNull() throws Exception {
    meta.setNullsAndEmptyAreDifferent( false );

    assertEquals( BASE_VALUE, meta.getNativeDataType( BASE_VALUE ) );
    assertEquals( TEST_VALUE, meta.getNativeDataType( TEST_VALUE ) );
    assertEquals( null, meta.getNativeDataType( null ) );
    assertEquals( "1", meta.getNativeDataType( 1 ) );
    assertEquals( "1.0", meta.getNativeDataType( 1.0 ) );

    Date d = ( new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" ) ).parse( "2012-11-10 09:08:07.654" );
    assertEquals( d.toString(), meta.getNativeDataType( d ) );

    Timestamp ts = Timestamp.valueOf( "2012-11-10 09:08:07.654321" );
    assertEquals( "2012-11-10 09:08:07.654321", meta.getNativeDataType( ts ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
    // assertEquals( null, meta.getNativeDataType( "" ) ); //TODO: is it correct?
    assertEquals( "", meta.getNativeDataType( "" ) ); // TODO: is it correct?
    assertEquals( "1", meta.getNativeDataType( "1" ) );
    assertEquals( "    ", meta.getNativeDataType( "    " ) );
    assertEquals( "  1  ", meta.getNativeDataType( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_LEFT );
    // assertEquals( null, meta.getNativeDataType( "" ) ); //TODO: is it correct?
    assertEquals( "", meta.getNativeDataType( "" ) ); // TODO: is it correct?
    assertEquals( "1", meta.getNativeDataType( "1" ) );
    // assertEquals( null, meta.getNativeDataType( "    " ) ); //TODO: is it correct?
    assertEquals( "", meta.getNativeDataType( "    " ) ); // TODO: is it correct?
    assertEquals( "1  ", meta.getNativeDataType( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_RIGHT );
    // assertEquals( null, meta.getNativeDataType( "" ) ); //TODO: is it correct?
    assertEquals( "", meta.getNativeDataType( "" ) ); // TODO: is it correct?
    assertEquals( "1", meta.getNativeDataType( "1" ) );
    // assertEquals( null, meta.getNativeDataType( "    " ) ); //TODO: is it correct?
    assertEquals( "", meta.getNativeDataType( "    " ) ); // TODO: is it correct?
    assertEquals( "  1", meta.getNativeDataType( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_BOTH );
    // assertEquals( null, meta.getNativeDataType( "" ) ); //TODO: is it correct?
    assertEquals( "", meta.getNativeDataType( "" ) ); // TODO: is it correct?
    assertEquals( "1", meta.getNativeDataType( "1" ) );
    // assertEquals( null, meta.getNativeDataType( "    " ) ); //TODO: is it correct?
    assertEquals( "", meta.getNativeDataType( "    " ) ); // TODO: is it correct?
    assertEquals( "1", meta.getNativeDataType( "  1  " ) );
  }

  @Test
  public void testIsNull_emptyIsNotNull() throws KettleValueException {
    meta.setNullsAndEmptyAreDifferent( true );

    assertEquals( true, meta.isNull( null ) );
    assertEquals( false, meta.isNull( "" ) );

    assertEquals( false, meta.isNull( "1" ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
    assertEquals( false, meta.isNull( "    " ) );
    assertEquals( false, meta.isNull( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_LEFT );
    assertEquals( false, meta.isNull( "    " ) );
    assertEquals( false, meta.isNull( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_RIGHT );
    assertEquals( false, meta.isNull( "    " ) );
    assertEquals( false, meta.isNull( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_BOTH );
    assertEquals( false, meta.isNull( "    " ) );
    assertEquals( false, meta.isNull( "  1  " ) );
  }

  @Test
  public void testIsNull_emptyIsNull() throws KettleValueException {
    meta.setNullsAndEmptyAreDifferent( false );

    assertEquals( true, meta.isNull( null ) );
    assertEquals( true, meta.isNull( "" ) );

    assertEquals( false, meta.isNull( "1" ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
    assertEquals( false, meta.isNull( "    " ) );
    assertEquals( false, meta.isNull( meta.getString( "    " ) ) );

    assertEquals( false, meta.isNull( "  1  " ) );
    assertEquals( false, meta.isNull( meta.getString( "  1  " ) ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_LEFT );
    // assertEquals( true, meta.isNull( "    " ) ); //TODO: is it correct?
    assertEquals( false, meta.isNull( "    " ) ); // TODO: is it correct?
    assertEquals( true, meta.isNull( meta.getString( "    " ) ) );

    assertEquals( false, meta.isNull( "  1  " ) );
    assertEquals( false, meta.isNull( meta.getString( "  1  " ) ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_RIGHT );
    // assertEquals( true, meta.isNull( "    " ) ); //TODO: is it correct?
    assertEquals( false, meta.isNull( "    " ) ); // TODO: is it correct?
    assertEquals( true, meta.isNull( meta.getString( "    " ) ) );

    assertEquals( false, meta.isNull( "  1  " ) );
    assertEquals( false, meta.isNull( meta.getString( "  1  " ) ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_BOTH );
    // assertEquals( true, meta.isNull( "    " ) ); //TODO: is it correct?
    assertEquals( false, meta.isNull( "    " ) ); // TODO: is it correct?
    assertEquals( true, meta.isNull( meta.getString( "    " ) ) );

    assertEquals( false, meta.isNull( "  1  " ) );
    assertEquals( false, meta.isNull( meta.getString( "  1  " ) ) );
  }

  @Test
  public void testGetString_emptyIsNotNull() throws KettleValueException {
    meta.setNullsAndEmptyAreDifferent( true );

    assertEquals( null, meta.getString( null ) );
    assertEquals( "", meta.getString( "" ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
    assertEquals( "    ", meta.getString( "    " ) );
    assertEquals( "  1  ", meta.getString( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_LEFT );
    assertEquals( "", meta.getString( "    " ) );
    assertEquals( "1  ", meta.getString( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_RIGHT );
    assertEquals( "", meta.getString( "    " ) );
    assertEquals( "  1", meta.getString( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_BOTH );
    assertEquals( "", meta.getString( "    " ) );
    assertEquals( "1", meta.getString( "  1  " ) );
  }

  @Test
  public void testGetString_emptyIsNull() throws KettleValueException {
    meta.setNullsAndEmptyAreDifferent( false );

    assertEquals( null, meta.getString( null ) );
    //assertEquals( null, meta.getString( "" ) ); // TODO: is it correct?
    assertEquals( "", meta.getString( "" ) ); // TODO: is it correct?

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
    assertEquals( "    ", meta.getString( "    " ) );
    assertEquals( "  1  ", meta.getString( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_LEFT );
    // assertEquals( null, meta.getString( "    " ) ); // TODO: is it correct?
    assertEquals( "", meta.getString( "    " ) ); // TODO: is it correct?
    assertEquals( "1  ", meta.getString( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_RIGHT );
    // assertEquals( null, meta.getString( "    " ) ); // TODO: is it correct?
    assertEquals( "", meta.getString( "    " ) ); // TODO: is it correct?
    assertEquals( "  1", meta.getString( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_BOTH );
    // assertEquals( null, meta.getString( "    " ) ); // TODO: is it correct?
    assertEquals( "", meta.getString( "    " ) ); // TODO: is it correct?
    assertEquals( "1", meta.getString( "  1  " ) );
  }

  @Test
  public void testCompare_emptyIsNotNull() throws KettleValueException {
    meta.setNullsAndEmptyAreDifferent( true );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );

    assertSignum( 0, meta.compare( null, null ) ); // null == null
    assertSignum( -1, meta.compare( null, "" ) ); // null < ""
    assertSignum( -1, meta.compare( null, " " ) ); // null < " "
    assertSignum( -1, meta.compare( null, " 1" ) ); // null < " 1"
    assertSignum( -1, meta.compare( null, " 1 " ) ); // null < " 1 "
    assertSignum( -1, meta.compare( null, "1" ) ); // null < "1"
    assertSignum( -1, meta.compare( null, "1 " ) ); // null < "1 "

    assertSignum( 1, meta.compare( "", null ) ); // "" > null
    assertSignum( 0, meta.compare( "", "" ) ); // "" == ""
    assertSignum( -1, meta.compare( "", " " ) ); // "" < " "
    assertSignum( -1, meta.compare( "", " 1" ) ); // "" < " 1"
    assertSignum( -1, meta.compare( "", " 1 " ) ); // "" < " 1 "
    assertSignum( -1, meta.compare( "", "1" ) ); // "" < "1"
    assertSignum( -1, meta.compare( "", "1 " ) ); // "" < "1 "

    assertSignum( 1, meta.compare( " ", null ) ); // " " > null
    assertSignum( 1, meta.compare( " ", "" ) ); // " " > ""
    assertSignum( 0, meta.compare( " ", " " ) ); // " " == " "
    assertSignum( -1, meta.compare( " ", " 1" ) ); // " " < " 1"
    assertSignum( -1, meta.compare( " ", " 1 " ) ); // " " < " 1 "
    assertSignum( -1, meta.compare( " ", "1" ) ); // " " < "1"
    assertSignum( -1, meta.compare( " ", "1 " ) ); // " " < "1 "

    assertSignum( 1, meta.compare( " 1", null ) ); // " 1" > null
    assertSignum( 1, meta.compare( " 1", "" ) ); // " 1" > ""
    assertSignum( 1, meta.compare( " 1", " " ) ); // " 1" > " "
    assertSignum( 0, meta.compare( " 1", " 1" ) ); // " 1" == " 1"
    assertSignum( -1, meta.compare( " 1", " 1 " ) ); // " 1" < " 1 "
    assertSignum( -1, meta.compare( " 1", "1" ) ); // " 1" < "1"
    assertSignum( -1, meta.compare( " 1", "1 " ) ); // " 1" < "1 "

    assertSignum( 1, meta.compare( " 1 ", null ) ); // " 1 " > null
    assertSignum( 1, meta.compare( " 1 ", "" ) ); // " 1 " > ""
    assertSignum( 1, meta.compare( " 1 ", " " ) ); // " 1 " > " "
    assertSignum( 1, meta.compare( " 1 ", " 1" ) ); // " 1 " > " 1"
    assertSignum( 0, meta.compare( " 1 ", " 1 " ) ); // " 1 " == " 1 "
    assertSignum( -1, meta.compare( " 1 ", "1" ) ); // " 1 " < "1"
    assertSignum( -1, meta.compare( " 1 ", "1 " ) ); // " 1 " < "1 "

    assertSignum( 1, meta.compare( "1", null ) ); // "1" > null
    assertSignum( 1, meta.compare( "1", "" ) ); // "1" > ""
    assertSignum( 1, meta.compare( "1", " " ) ); // "1" > " "
    assertSignum( 1, meta.compare( "1", " 1" ) ); // "1" > " 1"
    assertSignum( 1, meta.compare( "1", " 1 " ) ); // "1" > " 1 "
    assertSignum( 0, meta.compare( "1", "1" ) ); // "1" == "1"
    assertSignum( -1, meta.compare( "1", "1 " ) ); // "1" < "1 "

    assertSignum( 1, meta.compare( "1 ", null ) ); // "1 " > null
    assertSignum( 1, meta.compare( "1 ", "" ) ); // "1 " > ""
    assertSignum( 1, meta.compare( "1 ", " " ) ); // "1 " > " "
    assertSignum( 1, meta.compare( "1 ", " 1" ) ); // "1 " > " 1"
    assertSignum( 1, meta.compare( "1 ", " 1 " ) ); // "1 " > " 1 "
    assertSignum( 1, meta.compare( "1 ", "1" ) ); // "1 " > "1"
    assertSignum( 0, meta.compare( "1 ", "1 " ) ); // "1 " == "1 "

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_LEFT );

    assertSignum( 0, meta.compare( null, null ) ); // null == null
    assertSignum( -1, meta.compare( null, "" ) ); // null < ""
    assertSignum( -1, meta.compare( null, " " ) ); // null < ""
    assertSignum( -1, meta.compare( null, " 1" ) ); // null < "1"
    assertSignum( -1, meta.compare( null, " 1 " ) ); // null < "1 "
    assertSignum( -1, meta.compare( null, "1" ) ); // null < "1"
    assertSignum( -1, meta.compare( null, "1 " ) ); // null < "1 "

    assertSignum( 1, meta.compare( "", null ) ); // "" > null
    assertSignum( 0, meta.compare( "", "" ) ); // "" == ""
    assertSignum( 0, meta.compare( "", " " ) ); // "" == ""
    assertSignum( -1, meta.compare( "", " 1" ) ); // "" < "1"
    assertSignum( -1, meta.compare( "", " 1 " ) ); // "" < "1 "
    assertSignum( -1, meta.compare( "", "1" ) ); // "" < "1"
    assertSignum( -1, meta.compare( "", "1 " ) ); // "" < "1 "

    assertSignum( 1, meta.compare( " ", null ) ); // "" > null
    assertSignum( 0, meta.compare( " ", "" ) ); // "" == ""
    assertSignum( 0, meta.compare( " ", " " ) ); // "" == ""
    assertSignum( -1, meta.compare( " ", " 1" ) ); // "" < "1"
    assertSignum( -1, meta.compare( " ", " 1 " ) ); // "" < "1 "
    assertSignum( -1, meta.compare( " ", "1" ) ); // "" < "1"
    assertSignum( -1, meta.compare( " ", "1 " ) ); // "" < "1 "

    assertSignum( 1, meta.compare( " 1", null ) ); // "1" > null
    assertSignum( 1, meta.compare( " 1", "" ) ); // "1" > ""
    assertSignum( 1, meta.compare( " 1", " " ) ); // "1" > ""
    assertSignum( 0, meta.compare( " 1", " 1" ) ); // "1" == "1"
    assertSignum( -1, meta.compare( " 1", " 1 " ) ); // "1" < "1 "
    assertSignum( 0, meta.compare( " 1", "1" ) ); // "1" == "1"
    assertSignum( -1, meta.compare( " 1", "1 " ) ); // "1" < "1 "

    assertSignum( 1, meta.compare( " 1 ", null ) ); // "1 " > null
    assertSignum( 1, meta.compare( " 1 ", "" ) ); // "1 " > ""
    assertSignum( 1, meta.compare( " 1 ", " " ) ); // "1 " > ""
    assertSignum( 1, meta.compare( " 1 ", " 1" ) ); // "1 " > "1"
    assertSignum( 0, meta.compare( " 1 ", " 1 " ) ); // "1 " == "1 "
    assertSignum( 1, meta.compare( " 1 ", "1" ) ); // "1 " > "1"
    assertSignum( 0, meta.compare( " 1 ", "1 " ) ); // "1 " == "1 "

    assertSignum( 1, meta.compare( "1", null ) ); // "1" > null
    assertSignum( 1, meta.compare( "1", "" ) ); // "1" > ""
    assertSignum( 1, meta.compare( "1", " " ) ); // "1" > ""
    assertSignum( 0, meta.compare( "1", " 1" ) ); // "1" == "1"
    assertSignum( -1, meta.compare( "1", " 1 " ) ); // "1" < "1 "
    assertSignum( 0, meta.compare( "1", "1" ) ); // "1" == "1"
    assertSignum( -1, meta.compare( "1", "1 " ) ); // "1" < "1 "

    assertSignum( 1, meta.compare( "1 ", null ) ); // "1 " > null
    assertSignum( 1, meta.compare( "1 ", "" ) ); // "1 " > ""
    assertSignum( 1, meta.compare( "1 ", " " ) ); // "1 " > ""
    assertSignum( 1, meta.compare( "1 ", " 1" ) ); // "1 " > "1"
    assertSignum( 0, meta.compare( "1 ", " 1 " ) ); // "1 " == "1 "
    assertSignum( 1, meta.compare( "1 ", "1" ) ); // "1 " > "1"
    assertSignum( 0, meta.compare( "1 ", "1 " ) ); // "1 " == "1 "

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_RIGHT );

    assertSignum( 0, meta.compare( null, null ) ); // null == null
    assertSignum( -1, meta.compare( null, "" ) ); // null < ""
    assertSignum( -1, meta.compare( null, " " ) ); // null < ""
    assertSignum( -1, meta.compare( null, " 1" ) ); // null < " 1"
    assertSignum( -1, meta.compare( null, " 1 " ) ); // null < " 1"
    assertSignum( -1, meta.compare( null, "1" ) ); // null < "1"
    assertSignum( -1, meta.compare( null, "1 " ) ); // null < "1"

    assertSignum( 1, meta.compare( "", null ) ); // "" > null
    assertSignum( 0, meta.compare( "", "" ) ); // "" == ""
    assertSignum( 0, meta.compare( "", " " ) ); // "" == ""
    assertSignum( -1, meta.compare( "", " 1" ) ); // "" < " 1"
    assertSignum( -1, meta.compare( "", " 1 " ) ); // "" < " 1"
    assertSignum( -1, meta.compare( "", "1" ) ); // "" < "1"
    assertSignum( -1, meta.compare( "", "1 " ) ); // "" < "1"

    assertSignum( 1, meta.compare( " ", null ) ); // "" > null
    assertSignum( 0, meta.compare( " ", "" ) ); // "" == ""
    assertSignum( 0, meta.compare( " ", " " ) ); // "" == ""
    assertSignum( -1, meta.compare( " ", " 1" ) ); // "" < " 1"
    assertSignum( -1, meta.compare( " ", " 1 " ) ); // "" < " 1"
    assertSignum( -1, meta.compare( " ", "1" ) ); // "" < "1"
    assertSignum( -1, meta.compare( " ", "1 " ) ); // "" < "1"

    assertSignum( 1, meta.compare( " 1", null ) ); // " 1" > null
    assertSignum( 1, meta.compare( " 1", "" ) ); // " 1" > ""
    assertSignum( 1, meta.compare( " 1", " " ) ); // " 1" > ""
    assertSignum( 0, meta.compare( " 1", " 1" ) ); // " 1" == " 1"
    assertSignum( 0, meta.compare( " 1", " 1 " ) ); // " 1" == " 1"
    assertSignum( -1, meta.compare( " 1", "1" ) ); // " 1" < "1"
    assertSignum( -1, meta.compare( " 1", "1 " ) ); // " 1" < "1"

    assertSignum( 1, meta.compare( " 1 ", null ) ); // " 1" > null
    assertSignum( 1, meta.compare( " 1 ", "" ) ); // " 1" > ""
    assertSignum( 1, meta.compare( " 1 ", " " ) ); // " 1" > ""
    assertSignum( 0, meta.compare( " 1 ", " 1" ) ); // " 1" == " 1"
    assertSignum( 0, meta.compare( " 1 ", " 1 " ) ); // " 1" == " 1"
    assertSignum( -1, meta.compare( " 1 ", "1" ) ); // " 1" < "1"
    assertSignum( -1, meta.compare( " 1 ", "1 " ) ); // " 1" < "1"

    assertSignum( 1, meta.compare( "1", null ) ); // "1" > null
    assertSignum( 1, meta.compare( "1", "" ) ); // "1" > ""
    assertSignum( 1, meta.compare( "1", " " ) ); // "1" > ""
    assertSignum( 1, meta.compare( "1", " 1" ) ); // "1" > " 1"
    assertSignum( 1, meta.compare( "1", " 1 " ) ); // "1" > " 1"
    assertSignum( 0, meta.compare( "1", "1" ) ); // "1" == "1"
    assertSignum( 0, meta.compare( "1", "1 " ) ); // "1" == "1"

    assertSignum( 1, meta.compare( "1 ", null ) ); // "1" > null
    assertSignum( 1, meta.compare( "1 ", "" ) ); // "1" > ""
    assertSignum( 1, meta.compare( "1 ", " " ) ); // "1" > ""
    assertSignum( 1, meta.compare( "1 ", " 1" ) ); // "1" > " 1"
    assertSignum( 1, meta.compare( "1 ", " 1 " ) ); // "1" > " 1"
    assertSignum( 0, meta.compare( "1 ", "1" ) ); // "1" == "1"
    assertSignum( 0, meta.compare( "1 ", "1 " ) ); // "1" == "1"

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_BOTH );

    assertSignum( 0, meta.compare( null, null ) ); // null == null
    assertSignum( -1, meta.compare( null, "" ) ); // null < ""
    assertSignum( -1, meta.compare( null, " " ) ); // null < ""
    assertSignum( -1, meta.compare( null, " 1" ) ); // null < "1"
    assertSignum( -1, meta.compare( null, " 1 " ) ); // null < "1"
    assertSignum( -1, meta.compare( null, "1" ) ); // null < "1"
    assertSignum( -1, meta.compare( null, "1 " ) ); // null < "1"

    assertSignum( 1, meta.compare( "", null ) ); // "" > null
    assertSignum( 0, meta.compare( "", "" ) ); // "" == ""
    assertSignum( 0, meta.compare( "", " " ) ); // "" == ""
    assertSignum( -1, meta.compare( "", " 1" ) ); // "" < "1"
    assertSignum( -1, meta.compare( "", " 1 " ) ); // "" < "1"
    assertSignum( -1, meta.compare( "", "1" ) ); // "" < "1"
    assertSignum( -1, meta.compare( "", "1 " ) ); // "" < "1"

    assertSignum( 1, meta.compare( " ", null ) ); // "" > null
    assertSignum( 0, meta.compare( " ", "" ) ); // "" == ""
    assertSignum( 0, meta.compare( " ", " " ) ); // "" == ""
    assertSignum( -1, meta.compare( " ", " 1" ) ); // "" < "1"
    assertSignum( -1, meta.compare( " ", " 1 " ) ); // "" < "1"
    assertSignum( -1, meta.compare( " ", "1" ) ); // "" < "1"
    assertSignum( -1, meta.compare( " ", "1 " ) ); // "" < "1"

    assertSignum( 1, meta.compare( " 1", null ) ); // "1" > null
    assertSignum( 1, meta.compare( " 1", "" ) ); // "1" > ""
    assertSignum( 1, meta.compare( " 1", " " ) ); // "1" > ""
    assertSignum( 0, meta.compare( " 1", " 1" ) ); // "1" == "1"
    assertSignum( 0, meta.compare( " 1", " 1 " ) ); // "1" == "1"
    assertSignum( 0, meta.compare( " 1", "1" ) ); // "1" == "1"
    assertSignum( 0, meta.compare( " 1", "1 " ) ); // "1" == "1"

    assertSignum( 1, meta.compare( " 1 ", null ) ); // "1" > null
    assertSignum( 1, meta.compare( " 1 ", "" ) ); // "1" > ""
    assertSignum( 1, meta.compare( " 1 ", " " ) ); // "1" > ""
    assertSignum( 0, meta.compare( " 1 ", " 1" ) ); // "1" == "1"
    assertSignum( 0, meta.compare( " 1 ", " 1 " ) ); // "1" == "1"
    assertSignum( 0, meta.compare( " 1 ", "1" ) ); // "1" == "1"
    assertSignum( 0, meta.compare( " 1 ", "1 " ) ); // "1" == "1"

    assertSignum( 1, meta.compare( "1", null ) ); // "1" > null
    assertSignum( 1, meta.compare( "1", "" ) ); // "1" > ""
    assertSignum( 1, meta.compare( "1", " " ) ); // "1" > ""
    assertSignum( 0, meta.compare( "1", " 1" ) ); // "1" == "1"
    assertSignum( 0, meta.compare( "1", " 1 " ) ); // "1" == "1"
    assertSignum( 0, meta.compare( "1", "1" ) ); // "1" == "1"
    assertSignum( 0, meta.compare( "1", "1 " ) ); // "1" == "1"

    assertSignum( 1, meta.compare( "1 ", null ) ); // "1" > null
    assertSignum( 1, meta.compare( "1 ", "" ) ); // "1" > ""
    assertSignum( 1, meta.compare( "1 ", " " ) ); // "1" > ""
    assertSignum( 0, meta.compare( "1 ", " 1" ) ); // "1" == "1"
    assertSignum( 0, meta.compare( "1 ", " 1 " ) ); // "1" == "1"
    assertSignum( 0, meta.compare( "1 ", "1" ) ); // "1" == "1"
    assertSignum( 0, meta.compare( "1 ", "1 " ) ); // "1" == "1"
  }

  @Test
  public void testCompare_emptyIsNull() throws KettleValueException {
    meta.setNullsAndEmptyAreDifferent( false );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );

    assertSignum( 0, meta.compare( null, null ) ); // null == null
    assertSignum( 0, meta.compare( null, "" ) ); // null == null
    assertSignum( -1, meta.compare( null, " " ) ); // null < " "
    assertSignum( -1, meta.compare( null, " 1" ) ); // null < " 1"
    assertSignum( -1, meta.compare( null, " 1 " ) ); // null < " 1 "
    assertSignum( -1, meta.compare( null, "1" ) ); // null < "1"
    assertSignum( -1, meta.compare( null, "1 " ) ); // null < "1 "

    assertSignum( 0, meta.compare( "", null ) ); // null > null
    assertSignum( 0, meta.compare( "", "" ) ); // null == null
    assertSignum( -1, meta.compare( "", " " ) ); // null < " "
    assertSignum( -1, meta.compare( "", " 1" ) ); // null < " 1"
    assertSignum( -1, meta.compare( "", " 1 " ) ); // null < " 1 "
    assertSignum( -1, meta.compare( "", "1" ) ); // null < "1"
    assertSignum( -1, meta.compare( "", "1 " ) ); // null < "1 "

    assertSignum( 1, meta.compare( " ", null ) ); // " " > null
    assertSignum( 1, meta.compare( " ", "" ) ); // " " > null
    assertSignum( 0, meta.compare( " ", " " ) ); // " " == " "
    assertSignum( -1, meta.compare( " ", " 1" ) ); // " " < " 1"
    assertSignum( -1, meta.compare( " ", " 1 " ) ); // " " < " 1 "
    assertSignum( -1, meta.compare( " ", "1" ) ); // " " < "1"
    assertSignum( -1, meta.compare( " ", "1 " ) ); // " " < "1 "

    assertSignum( 1, meta.compare( " 1", null ) ); // " 1" > null
    assertSignum( 1, meta.compare( " 1", "" ) ); // " 1" > null
    assertSignum( 1, meta.compare( " 1", " " ) ); // " 1" > " "
    assertSignum( 0, meta.compare( " 1", " 1" ) ); // " 1" == " 1"
    assertSignum( -1, meta.compare( " 1", " 1 " ) ); // " 1" < " 1 "
    assertSignum( -1, meta.compare( " 1", "1" ) ); // " 1" < "1"
    assertSignum( -1, meta.compare( " 1", "1 " ) ); // " 1" < "1 "

    assertSignum( 1, meta.compare( " 1 ", null ) ); // " 1 " > null
    assertSignum( 1, meta.compare( " 1 ", "" ) ); // " 1 " > null
    assertSignum( 1, meta.compare( " 1 ", " " ) ); // " 1 " > " "
    assertSignum( 1, meta.compare( " 1 ", " 1" ) ); // " 1 " > " 1"
    assertSignum( 0, meta.compare( " 1 ", " 1 " ) ); // " 1 " == " 1 "
    assertSignum( -1, meta.compare( " 1 ", "1" ) ); // " 1 " < "1"
    assertSignum( -1, meta.compare( " 1 ", "1 " ) ); // " 1 " < "1 "

    assertSignum( 1, meta.compare( "1", null ) ); // "1" > null
    assertSignum( 1, meta.compare( "1", "" ) ); // "1" > null
    assertSignum( 1, meta.compare( "1", " " ) ); // "1" > " "
    assertSignum( 1, meta.compare( "1", " 1" ) ); // "1" > " 1"
    assertSignum( 1, meta.compare( "1", " 1 " ) ); // "1" > " 1 "
    assertSignum( 0, meta.compare( "1", "1" ) ); // "1" == "1"
    assertSignum( -1, meta.compare( "1", "1 " ) ); // "1" < "1 "

    assertSignum( 1, meta.compare( "1 ", null ) ); // "1 " > null
    assertSignum( 1, meta.compare( "1 ", "" ) ); // "1 " > null
    assertSignum( 1, meta.compare( "1 ", " " ) ); // "1 " > " "
    assertSignum( 1, meta.compare( "1 ", " 1" ) ); // "1 " > " 1"
    assertSignum( 1, meta.compare( "1 ", " 1 " ) ); // "1 " > " 1 "
    assertSignum( 1, meta.compare( "1 ", "1" ) ); // "1 " > "1"
    assertSignum( 0, meta.compare( "1 ", "1 " ) ); // "1 " == "1 "

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_LEFT );

    assertSignum( 0, meta.compare( null, null ) ); // null == null
    assertSignum( 0, meta.compare( null, "" ) ); // null < null
    // assertSignum( 0, meta.compare( null, " " ) ); // null == null //TODO: Is it correct?
    assertSignum( -1, meta.compare( null, " " ) ); // null < null //TODO: Is it correct?
    assertSignum( -1, meta.compare( null, " 1" ) ); // null < "1"
    assertSignum( -1, meta.compare( null, " 1 " ) ); // null < "1 "
    assertSignum( -1, meta.compare( null, "1" ) ); // null < "1"
    assertSignum( -1, meta.compare( null, "1 " ) ); // null < "1 "

    assertSignum( 0, meta.compare( "", null ) ); // null == null
    assertSignum( 0, meta.compare( "", "" ) ); // null == null
    // assertSignum( 0, meta.compare( "", " " ) ); // null == null //TODO: Is it correct?
    assertSignum( -1, meta.compare( "", " " ) ); // null < null //TODO: Is it correct?
    assertSignum( -1, meta.compare( "", " 1" ) ); // null < "1"
    assertSignum( -1, meta.compare( "", " 1 " ) ); // null < "1 "
    assertSignum( -1, meta.compare( "", "1" ) ); // null < "1"
    assertSignum( -1, meta.compare( "", "1 " ) ); // null < "1 "

    assertSignum( 1, meta.compare( " ", null ) ); // null > null
    // assertSignum( 0, meta.compare( " ", "" ) ); // null == null //TODO: Is it correct?
    assertSignum( 1, meta.compare( " ", "" ) ); // null > null //TODO: Is it correct?
    assertSignum( 0, meta.compare( " ", " " ) ); // null == null
    assertSignum( -1, meta.compare( " ", " 1" ) ); // null < "1"
    assertSignum( -1, meta.compare( " ", " 1 " ) ); // null < "1 "
    assertSignum( -1, meta.compare( " ", "1" ) ); // null < "1"
    assertSignum( -1, meta.compare( " ", "1 " ) ); // null < "1 "

    assertSignum( 1, meta.compare( " 1", null ) ); // "1" > null
    assertSignum( 1, meta.compare( " 1", "" ) ); // "1" > null
    assertSignum( 1, meta.compare( " 1", " " ) ); // "1" > null
    assertSignum( 0, meta.compare( " 1", " 1" ) ); // "1" == "1"
    assertSignum( -1, meta.compare( " 1", " 1 " ) ); // "1" < "1 "
    assertSignum( 0, meta.compare( " 1", "1" ) ); // "1" == "1"
    assertSignum( -1, meta.compare( " 1", "1 " ) ); // "1" < "1 "

    assertSignum( 1, meta.compare( " 1 ", null ) ); // "1 " > null
    assertSignum( 1, meta.compare( " 1 ", "" ) ); // "1 " > null
    assertSignum( 1, meta.compare( " 1 ", " " ) ); // "1 " > null
    assertSignum( 1, meta.compare( " 1 ", " 1" ) ); // "1 " > "1"
    assertSignum( 0, meta.compare( " 1 ", " 1 " ) ); // "1 " == "1 "
    assertSignum( 1, meta.compare( " 1 ", "1" ) ); // "1 " > "1"
    assertSignum( 0, meta.compare( " 1 ", "1 " ) ); // "1 " == "1 "

    assertSignum( 1, meta.compare( "1", null ) ); // "1" > null
    assertSignum( 1, meta.compare( "1", "" ) ); // "1" > null
    assertSignum( 1, meta.compare( "1", " " ) ); // "1" > null
    assertSignum( 0, meta.compare( "1", " 1" ) ); // "1" == "1"
    assertSignum( -1, meta.compare( "1", " 1 " ) ); // "1" < "1 "
    assertSignum( 0, meta.compare( "1", "1" ) ); // "1" == "1"
    assertSignum( -1, meta.compare( "1", "1 " ) ); // "1" < "1 "

    assertSignum( 1, meta.compare( "1 ", null ) ); // "1 " > null
    assertSignum( 1, meta.compare( "1 ", "" ) ); // "1 " > null
    assertSignum( 1, meta.compare( "1 ", " " ) ); // "1 " > null
    assertSignum( 1, meta.compare( "1 ", " 1" ) ); // "1 " > "1"
    assertSignum( 0, meta.compare( "1 ", " 1 " ) ); // "1 " == "1 "
    assertSignum( 1, meta.compare( "1 ", "1" ) ); // "1 " > "1"
    assertSignum( 0, meta.compare( "1 ", "1 " ) ); // "1 " == "1 "

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_RIGHT );

    assertSignum( 0, meta.compare( null, null ) ); // null == null
    assertSignum( 0, meta.compare( null, "" ) ); // null == null
    // assertSignum( 0, meta.compare( null, " " ) ); // null == null //TODO: Is it correct?
    assertSignum( -1, meta.compare( null, " " ) ); // null < null //TODO: Is it correct?
    assertSignum( -1, meta.compare( null, " 1" ) ); // null < " 1"
    assertSignum( -1, meta.compare( null, " 1 " ) ); // null < " 1"
    assertSignum( -1, meta.compare( null, "1" ) ); // null < "1"
    assertSignum( -1, meta.compare( null, "1 " ) ); // null < "1"

    assertSignum( 0, meta.compare( "", null ) ); // null == null
    assertSignum( 0, meta.compare( "", "" ) ); // null == null
    // assertSignum( 0, meta.compare( "", " " ) ); // null == null //TODO: Is it correct?
    assertSignum( -1, meta.compare( "", " " ) ); // null < null //TODO: Is it correct?
    assertSignum( -1, meta.compare( "", " 1" ) ); // null < " 1"
    assertSignum( -1, meta.compare( "", " 1 " ) ); // null < " 1"
    assertSignum( -1, meta.compare( "", "1" ) ); // null < "1"
    assertSignum( -1, meta.compare( "", "1 " ) ); // null < "1"

    // assertSignum( 0, meta.compare( " ", null ) ); // null == null //TODO: Is it correct?
    assertSignum( 1, meta.compare( " ", null ) ); // null > null //TODO: Is it correct?
    // assertSignum( 0, meta.compare( " ", "" ) ); // null == null //TODO: Is it correct?
    assertSignum( 1, meta.compare( " ", "" ) ); // null > null //TODO: Is it correct?
    assertSignum( 0, meta.compare( " ", " " ) ); // null == null
    assertSignum( -1, meta.compare( " ", " 1" ) ); // null < " 1"
    assertSignum( -1, meta.compare( " ", " 1 " ) ); // null < " 1"
    assertSignum( -1, meta.compare( " ", "1" ) ); // null < "1"
    assertSignum( -1, meta.compare( " ", "1 " ) ); // null < "1"

    assertSignum( 1, meta.compare( " 1", null ) ); // " 1" > null
    assertSignum( 1, meta.compare( " 1", "" ) ); // " 1" > null
    assertSignum( 1, meta.compare( " 1", " " ) ); // " 1" > null
    assertSignum( 0, meta.compare( " 1", " 1" ) ); // " 1" == " 1"
    assertSignum( 0, meta.compare( " 1", " 1 " ) ); // " 1" == " 1"
    assertSignum( -1, meta.compare( " 1", "1" ) ); // " 1" < "1"
    assertSignum( -1, meta.compare( " 1", "1 " ) ); // " 1" < "1"

    assertSignum( 1, meta.compare( " 1 ", null ) ); // " 1" > null
    assertSignum( 1, meta.compare( " 1 ", "" ) ); // " 1" > null
    assertSignum( 1, meta.compare( " 1 ", " " ) ); // " 1" > null
    assertSignum( 0, meta.compare( " 1 ", " 1" ) ); // " 1" == " 1"
    assertSignum( 0, meta.compare( " 1 ", " 1 " ) ); // " 1" == " 1"
    assertSignum( -1, meta.compare( " 1 ", "1" ) ); // " 1" < "1"
    assertSignum( -1, meta.compare( " 1 ", "1 " ) ); // " 1" < "1"

    assertSignum( 1, meta.compare( "1", null ) ); // "1" > null
    assertSignum( 1, meta.compare( "1", "" ) ); // "1" > null
    assertSignum( 1, meta.compare( "1", " " ) ); // "1" > null
    assertSignum( 1, meta.compare( "1", " 1" ) ); // "1" > " 1"
    assertSignum( 1, meta.compare( "1", " 1 " ) ); // "1" > " 1"
    assertSignum( 0, meta.compare( "1", "1" ) ); // "1" == "1"
    assertSignum( 0, meta.compare( "1", "1 " ) ); // "1" == "1"

    assertSignum( 1, meta.compare( "1 ", null ) ); // "1" > null
    assertSignum( 1, meta.compare( "1 ", "" ) ); // "1" > null
    assertSignum( 1, meta.compare( "1 ", " " ) ); // "1" > null
    assertSignum( 1, meta.compare( "1 ", " 1" ) ); // "1" > " 1"
    assertSignum( 1, meta.compare( "1 ", " 1 " ) ); // "1" > " 1"
    assertSignum( 0, meta.compare( "1 ", "1" ) ); // "1" == "1"
    assertSignum( 0, meta.compare( "1 ", "1 " ) ); // "1" == "1"

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_BOTH );

    assertSignum( 0, meta.compare( null, null ) ); // null == null
    assertSignum( 0, meta.compare( null, "" ) ); // null == null
    // assertSignum( 0, meta.compare( null, " " ) ); // null == null //TODO: Is it correct?
    assertSignum( -1, meta.compare( null, " " ) ); // null < null //TODO: Is it correct?
    assertSignum( -1, meta.compare( null, " 1" ) ); // null < "1"
    assertSignum( -1, meta.compare( null, " 1 " ) ); // null < "1"
    assertSignum( -1, meta.compare( null, "1" ) ); // null < "1"
    assertSignum( -1, meta.compare( null, "1 " ) ); // null < "1"

    assertSignum( 0, meta.compare( "", null ) ); // null == null
    assertSignum( 0, meta.compare( "", "" ) ); // null == null
    // assertSignum( 0, meta.compare( "", " " ) ); // null == null //TODO: Is it correct?
    assertSignum( -1, meta.compare( "", " " ) ); // null < null //TODO: Is it correct?
    assertSignum( -1, meta.compare( "", " 1" ) ); // null < "1"
    assertSignum( -1, meta.compare( "", " 1 " ) ); // null < "1"
    assertSignum( -1, meta.compare( "", "1" ) ); // null < "1"
    assertSignum( -1, meta.compare( "", "1 " ) ); // null < "1"

    // assertSignum( 0, meta.compare( " ", null ) ); // null == null //TODO: Is it correct?
    assertSignum( 1, meta.compare( " ", null ) ); // null > null //TODO: Is it correct?
    // assertSignum( 0, meta.compare( " ", "" ) ); // null == null //TODO: Is it correct?
    assertSignum( 1, meta.compare( " ", "" ) ); // null > null //TODO: Is it correct?
    assertSignum( 0, meta.compare( " ", " " ) ); // null == null
    assertSignum( -1, meta.compare( " ", " 1" ) ); // null < "1"
    assertSignum( -1, meta.compare( " ", " 1 " ) ); // null < "1"
    assertSignum( -1, meta.compare( " ", "1" ) ); // null < "1"
    assertSignum( -1, meta.compare( " ", "1 " ) ); // null < "1"

    assertSignum( 1, meta.compare( " 1", null ) ); // "1" > null
    assertSignum( 1, meta.compare( " 1", "" ) ); // "1" > null
    assertSignum( 1, meta.compare( " 1", " " ) ); // "1" > null
    assertSignum( 0, meta.compare( " 1", " 1" ) ); // "1" == "1"
    assertSignum( 0, meta.compare( " 1", " 1 " ) ); // "1" == "1"
    assertSignum( 0, meta.compare( " 1", "1" ) ); // "1" == "1"
    assertSignum( 0, meta.compare( " 1", "1 " ) ); // "1" == "1"

    assertSignum( 1, meta.compare( " 1 ", null ) ); // "1" > null
    assertSignum( 1, meta.compare( " 1 ", "" ) ); // "1" > null
    assertSignum( 1, meta.compare( " 1 ", " " ) ); // "1" > null
    assertSignum( 0, meta.compare( " 1 ", " 1" ) ); // "1" == "1"
    assertSignum( 0, meta.compare( " 1 ", " 1 " ) ); // "1" == "1"
    assertSignum( 0, meta.compare( " 1 ", "1" ) ); // "1" == "1"
    assertSignum( 0, meta.compare( " 1 ", "1 " ) ); // "1" == "1"

    assertSignum( 1, meta.compare( "1", null ) ); // "1" > null
    assertSignum( 1, meta.compare( "1", "" ) ); // "1" > null
    assertSignum( 1, meta.compare( "1", " " ) ); // "1" > null
    assertSignum( 0, meta.compare( "1", " 1" ) ); // "1" == "1"
    assertSignum( 0, meta.compare( "1", " 1 " ) ); // "1" == "1"
    assertSignum( 0, meta.compare( "1", "1" ) ); // "1" == "1"
    assertSignum( 0, meta.compare( "1", "1 " ) ); // "1" == "1"

    assertSignum( 1, meta.compare( "1 ", null ) ); // "1" > null
    assertSignum( 1, meta.compare( "1 ", "" ) ); // "1" > null
    assertSignum( 1, meta.compare( "1 ", " " ) ); // "1" > null
    assertSignum( 0, meta.compare( "1 ", " 1" ) ); // "1" == "1"
    assertSignum( 0, meta.compare( "1 ", " 1 " ) ); // "1" == "1"
    assertSignum( 0, meta.compare( "1 ", "1" ) ); // "1" == "1"
    assertSignum( 0, meta.compare( "1 ", "1 " ) ); // "1" == "1"


  }

  @Test
  public void testCompare_collatorEnabled() throws KettleValueException {
    ValueMetaString meta = new ValueMetaString( BASE_VALUE );
    meta.setCollatorDisabled( false );
    meta.setCollatorLocale( Locale.FRENCH );

    meta.setCollatorStrength( 3 );
    assertSignum( -1, meta.compare( "E", "F" ) );
    assertSignum( -1, meta.compare( "e", "\u00e9" ) );
    assertSignum( -1, meta.compare( "e", "E" ) );
    assertSignum( -1, meta.compare( "\u0001", "\u0002" ) );
    assertSignum( 0, meta.compare( "e", "e" ) );

    meta.setCollatorStrength( 2 );
    assertSignum( -1, meta.compare( "E", "F" ) );
    assertSignum( -1, meta.compare( "e", "\u00e9" ) );
    assertSignum( -1, meta.compare( "e", "E" ) );
    assertSignum( 0, meta.compare( "\u0001", "\u0002" ) );
    assertSignum( 0, meta.compare( "e", "e" ) );

    meta.setCollatorStrength( 1 );
    assertSignum( -1, meta.compare( "E", "F" ) );
    assertSignum( -1, meta.compare( "e", "\u00e9" ) );
    assertSignum( 0, meta.compare( "e", "E" ) );
    assertSignum( 0, meta.compare( "\u0001", "\u0002" ) );
    assertSignum( 0, meta.compare( "e", "e" ) );

    meta.setCollatorStrength( 0 );
    assertSignum( -1, meta.compare( "E", "F" ) );
    assertSignum( 0, meta.compare( "e", "\u00e9" ) );
    assertSignum( 0, meta.compare( "e", "E" ) );
    assertSignum( 0, meta.compare( "\u0001", "\u0002" ) );
    assertSignum( 0, meta.compare( "e", "e" ) );
  }

  @Test
  public void testGetIntegerWithoutConversionMask() throws KettleValueException, ParseException {
    String value = "100.56";
    ValueMetaInterface stringValueMeta = new ValueMetaString( "test" );

    Long expected = 100L;
    Long result = stringValueMeta.getInteger( value );
    assertEquals( expected, result );
  }

  @Test
  public void testGetNumberWithoutConversionMask() throws KettleValueException, ParseException {
    String value = "100.56";
    ValueMetaInterface stringValueMeta = new ValueMetaString( "test" );

    Double expected = 100.56D;
    Double result = stringValueMeta.getNumber( value );
    assertEquals( expected, result );
  }

  @Test
  public void testGetBigNumberWithoutConversionMask() throws KettleValueException, ParseException {
    String value = "100.5";
    ValueMetaInterface stringValueMeta = new ValueMetaString( "test" );

    BigDecimal expected = new BigDecimal( 100.5 );
    BigDecimal result = stringValueMeta.getBigNumber( value );
    assertEquals( expected, result );
  }

  @Test
  public void testGetDateWithoutConversionMask() throws KettleValueException, ParseException {
    Calendar date = new GregorianCalendar( 2017, 9, 20 ); // month 9 = Oct
    String value = "2017/10/20 00:00:00.000";
    ValueMetaInterface stringValueMeta = new ValueMetaString( "test" );

    Date expected = Date.from( date.toInstant() );
    Date result = stringValueMeta.getDate( value );
    assertEquals( expected, result );
  }

  private static void assertSignum( int expected, int actual ) {
    assertSignum( "", expected, actual );
  }

  private static void assertSignum( String msg, int expected, int actual ) {
    if ( expected < 0 ) {
      if ( actual >= 0 ) {
        Assert.failNotEquals( msg, "(<0)", actual );
      }
    } else if ( expected > 0 ) {
      if ( actual <= 0 ) {
        Assert.failNotEquals( msg, "(>0)", actual );
      }
    } else {
      assertEquals( msg, expected, actual );
    }
  }

  @SuppressWarnings( "deprecation" )
  private static class ConfigurableMeta extends ValueMetaString {
    private boolean nullsAndEmptyAreDifferent;

    public ConfigurableMeta( String name ) {
      super( name );
    }

    public void setNullsAndEmptyAreDifferent( boolean nullsAndEmptyAreDifferent ) {
      this.nullsAndEmptyAreDifferent = nullsAndEmptyAreDifferent;
    }

    @Override
    public boolean isNull( Object data ) throws KettleValueException {
      return super.isNull( data, nullsAndEmptyAreDifferent );
    }

    @Override
    protected String convertBinaryStringToString( byte[] binary ) throws KettleValueException {
      return super.convertBinaryStringToString( binary, nullsAndEmptyAreDifferent );
    }
  }
}
