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

package org.pentaho.di.core.injection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Ignore;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.injection.bean.BeanInjectionInfo;
import org.pentaho.di.core.injection.bean.BeanInjector;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;

/**
 * Base class for test metadata injection.
 */
@Ignore
public abstract class BaseMetadataInjectionTest<T> {
  protected BeanInjectionInfo info;
  protected BeanInjector injector;
  protected T meta;
  protected Set<String> nonTestedProperties;

  protected void setup( T meta ) {
    KettleLogStore.init();
    this.meta = meta;
    info = new BeanInjectionInfo( meta.getClass() );
    injector = new BeanInjector( info );
    nonTestedProperties = new HashSet<>( info.getProperties().keySet() );
  }

  @After
  public void after() {
    assertTrue( "Some properties where not tested: " + nonTestedProperties, nonTestedProperties.isEmpty() );
  }

  protected List<RowMetaAndData> setValue( ValueMetaInterface valueMeta, Object... values ) {
    RowMeta rowsMeta = new RowMeta();
    rowsMeta.addValueMeta( valueMeta );
    List<RowMetaAndData> rows = new ArrayList<>();
    for ( Object v : values ) {
      rows.add( new RowMetaAndData( rowsMeta, v ) );
    }
    return rows;
  }

  protected void skipPropertyTest( String propertyName ) {
    nonTestedProperties.remove( propertyName );
  }

  /**
   * Check boolean property.
   */
  protected void check( String propertyName, BooleanGetter getter ) throws KettleException {
    ValueMetaInterface valueMetaString = new ValueMetaString( "f" );

    injector.setProperty( meta, propertyName, setValue( valueMetaString, "Y" ), "f" );
    assertEquals( true, getter.get() );

    injector.setProperty( meta, propertyName, setValue( valueMetaString, "N" ), "f" );
    assertEquals( false, getter.get() );

    ValueMetaInterface valueMetaBoolean = new ValueMetaBoolean( "f" );

    injector.setProperty( meta, propertyName, setValue( valueMetaBoolean, true ), "f" );
    assertEquals( true, getter.get() );

    injector.setProperty( meta, propertyName, setValue( valueMetaBoolean, false ), "f" );
    assertEquals( false, getter.get() );

    skipPropertyTest( propertyName );
  }

  /**
   * Check string property.
   */
  protected void check( String propertyName, StringGetter getter, String... values ) throws KettleException {
    ValueMetaInterface valueMeta = new ValueMetaString( "f" );

    if ( values.length == 0 ) {
      values = new String[] { "v", "v2", null };
    }

    String correctValue = null;
    for ( String v : values ) {
      injector.setProperty( meta, propertyName, setValue( valueMeta, v ), "f" );
      if ( v != null ) {
        // only not-null values injected
        correctValue = v;
      }
      assertEquals( correctValue, getter.get() );
    }

    skipPropertyTest( propertyName );
  }

  /**
   * Check enum property.
   */
  protected void check( String propertyName, EnumGetter getter, Class<?> enumType ) throws KettleException {
    ValueMetaInterface valueMeta = new ValueMetaString( "f" );

    Object[] values = enumType.getEnumConstants();

    for ( Object v : values ) {
      injector.setProperty( meta, propertyName, setValue( valueMeta, v ), "f" );
      assertEquals( v, getter.get() );
    }

    try {
      injector.setProperty( meta, propertyName, setValue( valueMeta, "###" ), "f" );
      fail( "Should be passed to enum" );
    } catch ( KettleException ex ) {
    }

    skipPropertyTest( propertyName );
  }

  /**
   * Check int property.
   */
  protected void check( String propertyName, IntGetter getter ) throws KettleException {
    ValueMetaInterface valueMetaString = new ValueMetaString( "f" );

    injector.setProperty( meta, propertyName, setValue( valueMetaString, "1" ), "f" );
    assertEquals( 1, getter.get() );

    injector.setProperty( meta, propertyName, setValue( valueMetaString, "45" ), "f" );
    assertEquals( 45, getter.get() );

    ValueMetaInterface valueMetaInteger = new ValueMetaInteger( "f" );

    injector.setProperty( meta, propertyName, setValue( valueMetaInteger, 1234L ), "f" );
    assertEquals( 1234, getter.get() );

    injector.setProperty( meta, propertyName, setValue( valueMetaInteger, (long) Integer.MAX_VALUE ), "f" );
    assertEquals( Integer.MAX_VALUE, getter.get() );

    skipPropertyTest( propertyName );
  }

  /**
   * Check string-to-int property.
   */
  protected void checkStringToInt( String propertyName, IntGetter getter, String[] codes, int[] ids )
    throws KettleException {
    if ( codes.length != ids.length ) {
      throw new RuntimeException( "Wrong codes/ids sizes" );
    }
    ValueMetaInterface valueMetaString = new ValueMetaString( "f" );

    for ( int i = 0; i < codes.length; i++ ) {
      injector.setProperty( meta, propertyName, setValue( valueMetaString, codes[i] ), "f" );
      assertEquals( ids[i], getter.get() );
    }

    skipPropertyTest( propertyName );
  }

  /**
   * Check long property.
   */
  protected void check( String propertyName, LongGetter getter ) throws KettleException {
    ValueMetaInterface valueMetaString = new ValueMetaString( "f" );

    injector.setProperty( meta, propertyName, setValue( valueMetaString, "1" ), "f" );
    assertEquals( 1, getter.get() );

    injector.setProperty( meta, propertyName, setValue( valueMetaString, "45" ), "f" );
    assertEquals( 45, getter.get() );

    ValueMetaInterface valueMetaInteger = new ValueMetaInteger( "f" );

    injector.setProperty( meta, propertyName, setValue( valueMetaInteger, 1234L ), "f" );
    assertEquals( 1234, getter.get() );

    injector.setProperty( meta, propertyName, setValue( valueMetaInteger, Long.MAX_VALUE ), "f" );
    assertEquals( Long.MAX_VALUE, getter.get() );

    skipPropertyTest( propertyName );
  }

  public static int[] getTypeCodes( String[] typeNames ) {
    int[] typeCodes = new int[typeNames.length];
    for ( int i = 0; i < typeNames.length; i++ ) {
      typeCodes[i] = ValueMetaBase.getType( typeNames[i] );
    }
    return typeCodes;
  }

  public interface BooleanGetter {
    boolean get();
  }

  public interface StringGetter {
    String get();
  }

  public interface EnumGetter {
    Enum<?> get();
  }

  public interface IntGetter {
    int get();
  }

  public interface LongGetter {
    long get();
  }
}
