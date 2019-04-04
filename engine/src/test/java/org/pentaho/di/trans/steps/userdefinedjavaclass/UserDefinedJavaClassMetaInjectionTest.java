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

package org.pentaho.di.trans.steps.userdefinedjavaclass;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;


public class UserDefinedJavaClassMetaInjectionTest  extends BaseMetadataInjectionTest<UserDefinedJavaClassMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new UserDefinedJavaClassMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "CLEAR_RESULT_FIELDS", () -> meta.isClearingResultFields() );
    check( "TARGET_DESCRIPTION", () -> meta.getTargetStepDefinitions().get( 0 ).description );
    check( "TARGET_STEP_NAME", () -> meta.getTargetStepDefinitions().get( 0 ).stepName );
    check( "TARGET_TAG", () -> meta.getTargetStepDefinitions().get( 0 ).tag );
    check( "INFO_DESCRIPTION", () -> meta.getInfoStepDefinitions().get( 0 ).description );
    check( "INFO_STEP_NAME", () -> meta.getInfoStepDefinitions().get( 0 ).stepName );
    check( "INFO_TAG", () -> meta.getInfoStepDefinitions().get( 0 ).tag );
    check( "DESCRIPTION", () -> meta.getUsageParameters().get( 0 ).description );
    check( "VALUE", () -> meta.getUsageParameters().get( 0 ).value );
    check( "TAG", () -> meta.getUsageParameters().get( 0 ).tag );

    ArrayList<UserDefinedJavaClassMeta.FieldInfo> fieldList = new ArrayList<>();
    fieldList.add( new UserDefinedJavaClassMeta.FieldInfo( null, ValueMetaInterface.TYPE_STRING, -1, -1 ) );
    meta.replaceFields( fieldList );
    check( "FIELD_NAME", () -> meta.getFieldInfo().get( 0 ).name );
    check( "FIELD_LENGTH", () -> meta.getFieldInfo().get( 0 ).length );
    check( "FIELD_PRECISION", () -> meta.getFieldInfo().get( 0 ).precision );

    skipPropertyTest( "FIELD_TYPE" );
    ValueMetaInterface mftt = new ValueMetaString( "f" );
    injector.setProperty( meta, "FIELD_TYPE", setValue( mftt, "String" ), "f" );
    assertEquals( ValueMetaInterface.TYPE_STRING, meta.getFieldInfo().get( 0 ).type );
    // reset the field info array, so we can set it again
    meta.setFieldInfo( new ArrayList<>() );
    injector.setProperty( meta, "FIELD_TYPE", setValue( mftt, "Integer" ), "f" );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, meta.getFieldInfo().get( 0 ).type );

    injector.setProperty( meta, "FIELD_LENGTH", setValue( new ValueMetaString( "" ), "" ), "" );
    assertEquals( -1, meta.getFieldInfo().get( 0 ).length );
    injector.setProperty( meta, "FIELD_LENGTH", setValue( new ValueMetaString( " " ), " " ), " " );
    assertEquals( -1, meta.getFieldInfo().get( 0 ).length );
    injector.setProperty( meta, "FIELD_LENGTH", setValue( new ValueMetaString( "5" ), "5" ), "5" );
    assertEquals( 5, meta.getFieldInfo().get( 0 ).length );
    injector.setProperty( meta, "FIELD_LENGTH", setValue( new ValueMetaInteger( "4" ), new Long( 4 ) ), "4" );
    assertEquals( 4, meta.getFieldInfo().get( 0 ).length );

    injector.setProperty( meta, "FIELD_PRECISION", setValue( new ValueMetaString( "" ), "" ), "" );
    assertEquals( -1, meta.getFieldInfo().get( 0 ).precision );
    injector.setProperty( meta, "FIELD_PRECISION", setValue( new ValueMetaString( " " ), " " ), " " );
    assertEquals( -1, meta.getFieldInfo().get( 0 ).precision );
    injector.setProperty( meta, "FIELD_PRECISION", setValue( new ValueMetaString( "5" ), "5" ), "5" );
    assertEquals( 5, meta.getFieldInfo().get( 0 ).precision );
    injector.setProperty( meta, "FIELD_PRECISION", setValue( new ValueMetaInteger( "4" ), new Long( 4 ) ), "4" );
    assertEquals( 4, meta.getFieldInfo().get( 0 ).precision );

    ArrayList<UserDefinedJavaClassDef> definitions = new ArrayList<>();
    definitions.add( new UserDefinedJavaClassDef( UserDefinedJavaClassDef.ClassType.NORMAL_CLASS, "Test", "" ) );
    meta.replaceDefinitions( definitions );
    check( "CLASS_NAME", () -> meta.getDefinitions().get( 0 ).getClassName() );
    check( "CLASS_SOURCE", () -> meta.getDefinitions().get( 0 ).getSource() );
  }

}
