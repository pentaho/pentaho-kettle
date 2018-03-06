/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.selectvalues;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;

public class SelectValuesMetaInjectionTest extends BaseMetadataInjectionTest<SelectValuesMeta> {
  @Before
  public void setup() {
    setup( new SelectValuesMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "SELECT_UNSPECIFIED", new BooleanGetter() {
      public boolean get() {
        return meta.isSelectingAndSortingUnspecifiedFields();
      }
    } );
    check( "FIELD_NAME", new StringGetter() {
      public String get() {
        return meta.getSelectFields()[0].getName();
      }
    } );
    check( "FIELD_RENAME", new StringGetter() {
      public String get() {
        return meta.getSelectFields()[0].getRename();
      }
    } );
    check( "FIELD_LENGTH", new IntGetter() {
      public int get() {
        return meta.getSelectFields()[0].getLength();
      }
    } );
    check( "FIELD_PRECISION", new IntGetter() {
      public int get() {
        return meta.getSelectFields()[0].getPrecision();
      }
    } );
    check( "REMOVE_NAME", new StringGetter() {
      public String get() {
        return meta.getDeleteName()[0];
      }
    } );
    check( "META_NAME", new StringGetter() {
      public String get() {
        return meta.getMeta()[0].getName();
      }
    } );
    check( "META_RENAME", new StringGetter() {
      public String get() {
        return meta.getMeta()[0].getRename();
      }
    } );
    check( "META_LENGTH", new IntGetter() {
      public int get() {
        return meta.getMeta()[0].getLength();
      }
    } );
    check( "META_PRECISION", new IntGetter() {
      public int get() {
        return meta.getMeta()[0].getPrecision();
      }
    } );
    check( "META_CONVERSION_MASK", new StringGetter() {
      public String get() {
        return meta.getMeta()[0].getConversionMask();
      }
    } );
    check( "META_DATE_FORMAT_LENIENT", new BooleanGetter() {
      public boolean get() {
        return meta.getMeta()[0].isDateFormatLenient();
      }
    } );
    check( "META_DATE_FORMAT_LOCALE", new StringGetter() {
      public String get() {
        return meta.getMeta()[0].getDateFormatLocale();
      }
    } );
    check( "META_DATE_FORMAT_TIMEZONE", new StringGetter() {
      public String get() {
        return meta.getMeta()[0].getDateFormatTimeZone();
      }
    } );
    check( "META_LENIENT_STRING_TO_NUMBER", new BooleanGetter() {
      public boolean get() {
        return meta.getMeta()[0].isLenientStringToNumber();
      }
    } );
    check( "META_DECIMAL", new StringGetter() {
      public String get() {
        return meta.getMeta()[0].getDecimalSymbol();
      }
    } );
    check( "META_GROUPING", new StringGetter() {
      public String get() {
        return meta.getMeta()[0].getGroupingSymbol();
      }
    } );
    check( "META_CURRENCY", new StringGetter() {
      public String get() {
        return meta.getMeta()[0].getCurrencySymbol();
      }
    } );
    check( "META_ENCODING", new StringGetter() {
      public String get() {
        return meta.getMeta()[0].getEncoding();
      }
    } );

    ValueMetaInterface mftt = new ValueMetaString( "f" );
    injector.setProperty( meta, "META_STORAGE_TYPE", setValue( mftt, "normal" ), "f" );
    assertEquals( 0, meta.getMeta()[0].getStorageType() );
    injector.setProperty( meta, "META_STORAGE_TYPE", setValue( mftt, "binary-string" ), "f" );
    assertEquals( 1, meta.getMeta()[0].getStorageType() );
    injector.setProperty( meta, "META_STORAGE_TYPE", setValue( mftt, "indexed" ), "f" );
    assertEquals( 2, meta.getMeta()[0].getStorageType() );
    skipPropertyTest( "META_STORAGE_TYPE" );

    // TODO check field type plugins
    skipPropertyTest( "META_TYPE" );
  }

  //PDI-16932 test default values length and precision after injection
  @Test
  public void testDefaultValue() throws Exception {
    ValueMetaInterface valueMeta = new ValueMetaString( "f" );
    injector.setProperty( meta, "FIELD_NAME", setValue( valueMeta, "testValue" ), "f" );
    nonTestedProperties.clear(); // we don't need to test other properties
    assertEquals( SelectValuesMeta.UNDEFINED, meta.getSelectFields()[ 0 ].getLength() );
    assertEquals( SelectValuesMeta.UNDEFINED, meta.getSelectFields()[ 0 ].getPrecision() );
  }
}
