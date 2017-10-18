/*
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 * **************************************************************************
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
 */

package org.pentaho.di.core.row.value;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;

import java.math.BigDecimal;


public class ValueMetaBaseLenientStringConversionTest {

  @Test
  public void testStrToIntLenient() throws Exception {
    System.setProperty( Const.KETTLE_LENIENT_STRING_TO_NUMBER_CONVERSION, "Y" );

    Object[] values = new Object[] {
      1L, "1",
      1L, "1b",
      1L, "1,5",
      1L, "1.5",
      10L, "10,000,000.25",
      10L, "10.000.000,25"
    };

    ValueMetaInteger meta = new ValueMetaInteger();
    for ( int i = 0; i < values.length; i += 2 ) {
      long expected = (Long) values[ i ];
      long actual = meta.convertStringToInteger( (String) values[ i + 1 ] );
      Assert.assertEquals( "Can't convert '" + values[ i + 1 ] + "' :", expected, actual );
    }
  }

  @Test
  public void testStrToIntStrict() throws Exception {
    System.setProperty( Const.KETTLE_LENIENT_STRING_TO_NUMBER_CONVERSION, "N" );

    String[] values = new String[] { "1a", "1,1", "100,000,3", "100.000,3" };

    ValueMetaInteger meta = new ValueMetaInteger();
    Long converted = null;
    Throwable exc = null;
    for ( String value : values ) {
      try {
        converted = meta.convertStringToInteger( value );
      } catch ( Exception e ) {
        exc = e;
      } finally {
        Assert.assertTrue( "Conversion of '" + value + "' didn't fail. Value is " + converted,
          exc instanceof KettleValueException );
        exc = null;
      }
    }
  }

  @Test
  public void testStrToBigNumberLenient() throws Exception {
    System.setProperty( Const.KETTLE_LENIENT_STRING_TO_NUMBER_CONVERSION, "Y" );

    Object[] values = new Object[] {
      1D, "1",
      1D, "1b",
      1D, "1,5",
      1.5D, "1.5",
      10D, "10,000,000.25",
      10D, "10.000.000,25"
    };

    ValueMetaBigNumber meta = new ValueMetaBigNumber();
    for ( int i = 0; i < values.length; i += 2 ) {
      Double expected = (Double) values[ i ];
      Double actual = meta.convertStringToBigNumber( (String) values[ i + 1 ] ).doubleValue();
      Assert.assertEquals( "Can't convert '" + values[ i + 1 ] + "' :", expected, actual );
    }
  }

  @Test
  public void testStrToBigNumberStrict() throws Exception {

    System.setProperty( Const.KETTLE_LENIENT_STRING_TO_NUMBER_CONVERSION, "N" );

    String[] values = new String[] { "1b", "1,5", "10,000,000.25" };

    ValueMetaBigNumber meta = new ValueMetaBigNumber();
    Throwable exc = null;
    BigDecimal converted = null;
    for ( String value : values ) {
      try {
        converted = meta.convertStringToBigNumber( value );
      } catch ( Exception e ) {
        exc = e;
      } finally {
        Assert.assertTrue( "Conversion of '" + value + "' didn't fail. Value is " + converted,
          exc instanceof KettleValueException );
        exc = null;
      }
    }
  }

}
