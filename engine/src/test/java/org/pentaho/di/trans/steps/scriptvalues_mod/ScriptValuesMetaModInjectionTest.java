/*
/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.scriptvalues_mod;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import static org.junit.Assert.assertEquals;

public class ScriptValuesMetaModInjectionTest extends BaseMetadataInjectionTest<ScriptValuesMetaMod> {

  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new ScriptValuesMetaMod() );
  }

  @Test
  public void test() throws Exception {
    check( "COMPATIBILITY_MODE", new BooleanGetter() {
      public boolean get() {
        return meta.isCompatible();
      }
    } );
    check( "OPTIMIZATION_LEVEL", new StringGetter() {
      public String get() {
        return meta.getOptimizationLevel();
      }
    } );
    check( "FIELD_NAME", new StringGetter() {
      public String get() {
        return meta.getFieldname()[ 0 ];
      }
    } );
    check( "FIELD_RENAME_TO", new StringGetter() {
      public String get() {
        return meta.getRename()[ 0 ];
      }
    } );
    check( "FIELD_REPLACE", new BooleanGetter() {
      public boolean get() {
        return meta.getReplace()[ 0 ];
      }
    } );
    check( "SCRIPT_NAME", new StringGetter() {
      public String get() {
        return meta.getJSScripts()[ 0 ].getScriptName();
      }
    } );
    check( "SCRIPT", new StringGetter() {
      public String get() {
        return meta.getJSScripts()[ 0 ].getScript();
      }
    } );

    // field type requires special handling, since it's stored as an array of ints, but injected as strings
    skipPropertyTest( "FIELD_TYPE" );
    ValueMetaInterface mftt = new ValueMetaString( "f" );
    injector.setProperty( meta, "FIELD_TYPE", setValue( mftt, "String" ), "f" );
    assertEquals( ValueMetaInterface.TYPE_STRING, meta.getType()[ 0 ] );
    // reset the types array, so we can set it again
    meta.setType( new int[] {} );
    injector.setProperty( meta, "FIELD_TYPE", setValue( mftt, "Integer" ), "f" );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, meta.getType()[ 0 ] );

    // length and precision fields also need special handing, to ensure that we get -1 when the injected string value
    // is empty or null
    skipPropertyTest( "FIELD_LENGTH" );
    injector.setProperty( meta, "FIELD_LENGTH", setValue( new ValueMetaString( "" ), "" ), "" );
    assertEquals( -1, meta.getLength()[ 0 ] );
    injector.setProperty( meta, "FIELD_LENGTH", setValue( new ValueMetaString( " " ), " " ), " " );
    assertEquals( -1, meta.getLength()[ 0 ] );
    injector.setProperty( meta, "FIELD_LENGTH", setValue( new ValueMetaString( null ), null ), null );
    assertEquals( -1, meta.getLength()[ 0 ] );
    injector.setProperty( meta, "FIELD_LENGTH", setValue( new ValueMetaString( "5" ), "5" ), "5" );
    assertEquals( 5, meta.getLength()[ 0 ] );
    injector.setProperty( meta, "FIELD_LENGTH", setValue( new ValueMetaInteger( "5" ), new Long( 5 ) ), "5" );
    assertEquals( 5, meta.getLength()[ 0 ] );
    injector.setProperty( meta, "FIELD_LENGTH", setValue( new ValueMetaInteger( "5" ), (Long) null ), "5" );
    assertEquals( -1, meta.getLength()[ 0 ] );
    injector.setProperty( meta, "FIELD_LENGTH", setValue( new ValueMetaNumber( "5" ), new Double( 5 ) ), "5" );
    assertEquals( 5, meta.getLength()[ 0 ] );
    injector.setProperty( meta, "FIELD_LENGTH", setValue( new ValueMetaInteger( "5" ), (Double) null ), "5" );
    assertEquals( -1, meta.getLength()[ 0 ] );

    skipPropertyTest( "FIELD_PRECISION" );
    injector.setProperty( meta, "FIELD_PRECISION", setValue( new ValueMetaString( "" ), "" ), "" );
    assertEquals( -1, meta.getPrecision()[ 0 ] );
    injector.setProperty( meta, "FIELD_PRECISION", setValue( new ValueMetaString( " " ), " " ), " " );
    assertEquals( -1, meta.getPrecision()[ 0 ] );
    injector.setProperty( meta, "FIELD_PRECISION", setValue( new ValueMetaString( null ), null ), null );
    assertEquals( -1, meta.getPrecision()[ 0 ] );
    injector.setProperty( meta, "FIELD_PRECISION", setValue( new ValueMetaString( "5" ), "5" ), "5" );
    assertEquals( 5, meta.getPrecision()[ 0 ] );
    injector.setProperty( meta, "FIELD_PRECISION", setValue( new ValueMetaInteger( "5" ), new Long( 5 ) ), "5" );
    assertEquals( 5, meta.getPrecision()[ 0 ] );
    injector.setProperty( meta, "FIELD_PRECISION", setValue( new ValueMetaInteger( "5" ), (Long) null ), "5" );
    assertEquals( -1, meta.getPrecision()[ 0 ] );
    injector.setProperty( meta, "FIELD_PRECISION", setValue( new ValueMetaNumber( "5" ), new Double( 5 ) ), "5" );
    assertEquals( 5, meta.getPrecision()[ 0 ] );
    injector.setProperty( meta, "FIELD_PRECISION", setValue( new ValueMetaInteger( "5" ), (Double) null ), "5" );
    assertEquals( -1, meta.getPrecision()[ 0 ] );
  }
}
