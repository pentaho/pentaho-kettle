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

package org.pentaho.di.core.row.value;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;

import junit.framework.Assert;

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

    Assert.assertEquals( BASE_VALUE, meta.getNativeDataType( BASE_VALUE ) );
    Assert.assertEquals( TEST_VALUE, meta.getNativeDataType( TEST_VALUE ) );
    Assert.assertEquals( null, meta.getNativeDataType( null ) );
    Assert.assertEquals( "1", meta.getNativeDataType( 1 ) );
    Assert.assertEquals( "1.0", meta.getNativeDataType( 1.0 ) );

    Date d = ( new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" ) ).parse( "2012-11-10 09:08:07.654" );
    Assert.assertEquals( d.toString(), meta.getNativeDataType( d ) );

    Timestamp ts = Timestamp.valueOf( "2012-11-10 09:08:07.654321" );
    Assert.assertEquals( "2012-11-10 09:08:07.654321", meta.getNativeDataType( ts ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
    Assert.assertEquals( "", meta.getNativeDataType( "" ) );
    Assert.assertEquals( "1", meta.getNativeDataType( "1" ) );
    Assert.assertEquals( "    ", meta.getNativeDataType( "    " ) );
    Assert.assertEquals( "  1  ", meta.getNativeDataType( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_LEFT );
    Assert.assertEquals( "", meta.getNativeDataType( "" ) );
    Assert.assertEquals( "1", meta.getNativeDataType( "1" ) );
    Assert.assertEquals( "", meta.getNativeDataType( "    " ) );
    Assert.assertEquals( "1  ", meta.getNativeDataType( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_RIGHT );
    Assert.assertEquals( "", meta.getNativeDataType( "" ) );
    Assert.assertEquals( "1", meta.getNativeDataType( "1" ) );
    Assert.assertEquals( "", meta.getNativeDataType( "    " ) );
    Assert.assertEquals( "  1", meta.getNativeDataType( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_BOTH );
    Assert.assertEquals( "", meta.getNativeDataType( "" ) );
    Assert.assertEquals( "1", meta.getNativeDataType( "1" ) );
    Assert.assertEquals( "", meta.getNativeDataType( "    " ) );
    Assert.assertEquals( "1", meta.getNativeDataType( "  1  " ) );
  }

  @Test
  public void testGetNativeData_emptyIsNull() throws Exception {
    meta.setNullsAndEmptyAreDifferent( false );

    Assert.assertEquals( BASE_VALUE, meta.getNativeDataType( BASE_VALUE ) );
    Assert.assertEquals( TEST_VALUE, meta.getNativeDataType( TEST_VALUE ) );
    Assert.assertEquals( null, meta.getNativeDataType( null ) );
    Assert.assertEquals( "1", meta.getNativeDataType( 1 ) );
    Assert.assertEquals( "1.0", meta.getNativeDataType( 1.0 ) );

    Date d = ( new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" ) ).parse( "2012-11-10 09:08:07.654" );
    Assert.assertEquals( d.toString(), meta.getNativeDataType( d ) );

    Timestamp ts = Timestamp.valueOf( "2012-11-10 09:08:07.654321" );
    Assert.assertEquals( "2012-11-10 09:08:07.654321", meta.getNativeDataType( ts ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
    // Assert.assertEquals( null, meta.getNativeDataType( "" ) ); //TODO: is it correct?
    Assert.assertEquals( "", meta.getNativeDataType( "" ) ); // TODO: is it correct?
    Assert.assertEquals( "1", meta.getNativeDataType( "1" ) );
    Assert.assertEquals( "    ", meta.getNativeDataType( "    " ) );
    Assert.assertEquals( "  1  ", meta.getNativeDataType( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_LEFT );
    // Assert.assertEquals( null, meta.getNativeDataType( "" ) ); //TODO: is it correct?
    Assert.assertEquals( "", meta.getNativeDataType( "" ) ); // TODO: is it correct?
    Assert.assertEquals( "1", meta.getNativeDataType( "1" ) );
    // Assert.assertEquals( null, meta.getNativeDataType( "    " ) ); //TODO: is it correct?
    Assert.assertEquals( "", meta.getNativeDataType( "    " ) ); // TODO: is it correct?
    Assert.assertEquals( "1  ", meta.getNativeDataType( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_RIGHT );
    // Assert.assertEquals( null, meta.getNativeDataType( "" ) ); //TODO: is it correct?
    Assert.assertEquals( "", meta.getNativeDataType( "" ) ); // TODO: is it correct?
    Assert.assertEquals( "1", meta.getNativeDataType( "1" ) );
    // Assert.assertEquals( null, meta.getNativeDataType( "    " ) ); //TODO: is it correct?
    Assert.assertEquals( "", meta.getNativeDataType( "    " ) ); // TODO: is it correct?
    Assert.assertEquals( "  1", meta.getNativeDataType( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_BOTH );
    // Assert.assertEquals( null, meta.getNativeDataType( "" ) ); //TODO: is it correct?
    Assert.assertEquals( "", meta.getNativeDataType( "" ) ); // TODO: is it correct?
    Assert.assertEquals( "1", meta.getNativeDataType( "1" ) );
    // Assert.assertEquals( null, meta.getNativeDataType( "    " ) ); //TODO: is it correct?
    Assert.assertEquals( "", meta.getNativeDataType( "    " ) ); // TODO: is it correct?
    Assert.assertEquals( "1", meta.getNativeDataType( "  1  " ) );
  }

  @Test
  public void testIsNull_emptyIsNotNull() throws KettleValueException {
    meta.setNullsAndEmptyAreDifferent( true );

    Assert.assertEquals( true, meta.isNull( null ) );
    Assert.assertEquals( false, meta.isNull( "" ) );

    Assert.assertEquals( false, meta.isNull( "1" ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
    Assert.assertEquals( false, meta.isNull( "    " ) );
    Assert.assertEquals( false, meta.isNull( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_LEFT );
    Assert.assertEquals( false, meta.isNull( "    " ) );
    Assert.assertEquals( false, meta.isNull( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_RIGHT );
    Assert.assertEquals( false, meta.isNull( "    " ) );
    Assert.assertEquals( false, meta.isNull( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_BOTH );
    Assert.assertEquals( false, meta.isNull( "    " ) );
    Assert.assertEquals( false, meta.isNull( "  1  " ) );
  }

  @Test
  public void testIsNull_emptyIsNull() throws KettleValueException {
    meta.setNullsAndEmptyAreDifferent( false );

    Assert.assertEquals( true, meta.isNull( null ) );
    Assert.assertEquals( true, meta.isNull( "" ) );

    Assert.assertEquals( false, meta.isNull( "1" ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
    Assert.assertEquals( false, meta.isNull( "    " ) );
    Assert.assertEquals( false, meta.isNull( meta.getString( "    " ) ) );

    Assert.assertEquals( false, meta.isNull( "  1  " ) );
    Assert.assertEquals( false, meta.isNull( meta.getString( "  1  " ) ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_LEFT );
    // Assert.assertEquals( true, meta.isNull( "    " ) ); //TODO: is it correct?
    Assert.assertEquals( false, meta.isNull( "    " ) ); // TODO: is it correct?
    Assert.assertEquals( true, meta.isNull( meta.getString( "    " ) ) );

    Assert.assertEquals( false, meta.isNull( "  1  " ) );
    Assert.assertEquals( false, meta.isNull( meta.getString( "  1  " ) ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_RIGHT );
    // Assert.assertEquals( true, meta.isNull( "    " ) ); //TODO: is it correct?
    Assert.assertEquals( false, meta.isNull( "    " ) ); // TODO: is it correct?
    Assert.assertEquals( true, meta.isNull( meta.getString( "    " ) ) );

    Assert.assertEquals( false, meta.isNull( "  1  " ) );
    Assert.assertEquals( false, meta.isNull( meta.getString( "  1  " ) ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_BOTH );
    // Assert.assertEquals( true, meta.isNull( "    " ) ); //TODO: is it correct?
    Assert.assertEquals( false, meta.isNull( "    " ) ); // TODO: is it correct?
    Assert.assertEquals( true, meta.isNull( meta.getString( "    " ) ) );

    Assert.assertEquals( false, meta.isNull( "  1  " ) );
    Assert.assertEquals( false, meta.isNull( meta.getString( "  1  " ) ) );
  }

  @Test
  public void testGetString_emptyIsNotNull() throws KettleValueException {
    meta.setNullsAndEmptyAreDifferent( true );

    Assert.assertEquals( null, meta.getString( null ) );
    Assert.assertEquals( "", meta.getString( "" ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
    Assert.assertEquals( "    ", meta.getString( "    " ) );
    Assert.assertEquals( "  1  ", meta.getString( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_LEFT );
    Assert.assertEquals( "", meta.getString( "    " ) );
    Assert.assertEquals( "1  ", meta.getString( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_RIGHT );
    Assert.assertEquals( "", meta.getString( "    " ) );
    Assert.assertEquals( "  1", meta.getString( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_BOTH );
    Assert.assertEquals( "", meta.getString( "    " ) );
    Assert.assertEquals( "1", meta.getString( "  1  " ) );
  }

  @Test
  public void testGetString_emptyIsNull() throws KettleValueException {
    meta.setNullsAndEmptyAreDifferent( false );

    Assert.assertEquals( null, meta.getString( null ) );
    //Assert.assertEquals( null, meta.getString( "" ) ); // TODO: is it correct?
    Assert.assertEquals( "", meta.getString( "" ) ); // TODO: is it correct?

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
    Assert.assertEquals( "    ", meta.getString( "    " ) );
    Assert.assertEquals( "  1  ", meta.getString( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_LEFT );
    // Assert.assertEquals( null, meta.getString( "    " ) ); // TODO: is it correct?
    Assert.assertEquals( "", meta.getString( "    " ) ); // TODO: is it correct?
    Assert.assertEquals( "1  ", meta.getString( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_RIGHT );
    // Assert.assertEquals( null, meta.getString( "    " ) ); // TODO: is it correct?
    Assert.assertEquals( "", meta.getString( "    " ) ); // TODO: is it correct?
    Assert.assertEquals( "  1", meta.getString( "  1  " ) );

    meta.setTrimType( ValueMetaInterface.TRIM_TYPE_BOTH );
    // Assert.assertEquals( null, meta.getString( "    " ) ); // TODO: is it correct?
    Assert.assertEquals( "", meta.getString( "    " ) ); // TODO: is it correct?
    Assert.assertEquals( "1", meta.getString( "  1  " ) );
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
      Assert.assertEquals( msg, expected, actual );
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
