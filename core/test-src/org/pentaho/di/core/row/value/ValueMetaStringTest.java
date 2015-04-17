/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

public class ValueMetaStringTest {
  private static final String BASE_VALUE = "Some text";
  private static final String TEST_VALUE = "Some text";
  private static final boolean ValueMetaBase_EMPTY_STRING_AND_NULL_ARE_DIFFERENT =
      ValueMetaBase.EMPTY_STRING_AND_NULL_ARE_DIFFERENT;
  private static final boolean ValueMeta_EMPTY_STRING_AND_NULL_ARE_DIFFERENT =
      ValueMeta.EMPTY_STRING_AND_NULL_ARE_DIFFERENT;

  @AfterClass
  public static void afterClass() throws NoSuchFieldException, SecurityException, IllegalArgumentException,
    IllegalAccessException {
    resetEmptyStringIsNotNull();
  }

  @Test
  public void testGetNativeData_emptyIsNotNull() throws KettleValueException, ParseException, NoSuchFieldException,
    SecurityException, IllegalArgumentException, IllegalAccessException {
    ensureEmptyStringIsNotNull( true );

    ValueMetaString meta = new ValueMetaString( BASE_VALUE );
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
  public void testGetNativeData_emptyIsNull() throws KettleValueException, ParseException, NoSuchFieldException,
    SecurityException, IllegalArgumentException, IllegalAccessException {
    ensureEmptyStringIsNotNull( false );

    ValueMetaString meta = new ValueMetaString( BASE_VALUE );
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
    ensureEmptyStringIsNotNull( true );

    ValueMetaString meta = new ValueMetaString( BASE_VALUE );
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
    ensureEmptyStringIsNotNull( false );

    ValueMetaString meta = new ValueMetaString( BASE_VALUE );
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
    ensureEmptyStringIsNotNull( true );

    ValueMetaString meta = new ValueMetaString( BASE_VALUE );
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
    ensureEmptyStringIsNotNull( false );

    ValueMetaString meta = new ValueMetaString( BASE_VALUE );
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
    ensureEmptyStringIsNotNull( true );

    ValueMetaString meta = new ValueMetaString( BASE_VALUE );

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
    ensureEmptyStringIsNotNull( false );

    ValueMetaString meta = new ValueMetaString( BASE_VALUE );

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

  private static void ensureBooleanStaticFieldVal( Field f, boolean newValue ) throws NoSuchFieldException,
    SecurityException, IllegalArgumentException, IllegalAccessException {
    boolean value = f.getBoolean( null );
    if ( value != newValue ) {
      final boolean fieldAccessibleBak = f.isAccessible();
      Field.setAccessible( new Field[] { f }, true );

      Field modifiersField = Field.class.getDeclaredField( "modifiers" );
      final int modifiersBak = f.getModifiers();
      final int modifiersNew = modifiersBak & ~Modifier.FINAL;
      final boolean modifAccessibleBak = modifiersField.isAccessible();
      if ( modifiersBak != modifiersNew ) {
        if ( !modifAccessibleBak ) {
          modifiersField.setAccessible( true );
        }
        modifiersField.setInt( f, modifiersNew );
      }

      f.setBoolean( null, newValue );

      if ( modifiersBak != modifiersNew ) {
        modifiersField.setInt( f, modifiersBak );
        if ( !modifAccessibleBak ) {
          modifiersField.setAccessible( modifAccessibleBak );
        }
      }
      if ( !fieldAccessibleBak ) {
        Field.setAccessible( new Field[] { f }, fieldAccessibleBak );
      }
    }
  }

  private void ensureEmptyStringIsNotNull( boolean newValue ) {
    try {
      ensureBooleanStaticFieldVal( ValueMetaBase.class.getField( "EMPTY_STRING_AND_NULL_ARE_DIFFERENT" ), newValue );
      ensureBooleanStaticFieldVal( ValueMeta.class.getField( "EMPTY_STRING_AND_NULL_ARE_DIFFERENT" ), newValue );
    } catch ( NoSuchFieldException e ) {
      throw new RuntimeException( e );
    } catch ( SecurityException e ) {
      throw new RuntimeException( e );
    } catch ( IllegalArgumentException e ) {
      throw new RuntimeException( e );
    } catch ( IllegalAccessException e ) {
      throw new RuntimeException( e );
    }
    Assert.assertEquals( "ValueMetaBase.EMPTY_STRING_AND_NULL_ARE_DIFFERENT", newValue,
        ValueMetaBase.EMPTY_STRING_AND_NULL_ARE_DIFFERENT );
    Assert.assertEquals( "ValueMeta.EMPTY_STRING_AND_NULL_ARE_DIFFERENT", newValue,
        ValueMeta.EMPTY_STRING_AND_NULL_ARE_DIFFERENT );
  }

  private static void resetEmptyStringIsNotNull() throws NoSuchFieldException, IllegalAccessException {
    ensureBooleanStaticFieldVal( ValueMetaBase.class.getField( "EMPTY_STRING_AND_NULL_ARE_DIFFERENT" ),
        ValueMetaBase_EMPTY_STRING_AND_NULL_ARE_DIFFERENT );
    ensureBooleanStaticFieldVal( ValueMeta.class.getField( "EMPTY_STRING_AND_NULL_ARE_DIFFERENT" ),
        ValueMeta_EMPTY_STRING_AND_NULL_ARE_DIFFERENT );
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
}
