/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.getvariable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransformationTestCase;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.NonZeroIntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class GetVariableMetaTest extends TransformationTestCase {

  LoadSaveTester loadSaveTester;

  @Before
  public void setUp() throws Exception {
    List<String> attributes =
        Arrays.asList( "fieldName", "variableString", "fieldFormat", "fieldType", "fieldLength", "fieldPrecision", "currency", "decimal", "group", "trimType" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "fieldName", "getFieldName" );
        put( "variableString", "getVariableString" );
        put( "fieldFormat", "getFieldFormat" );
        put( "fieldType", "getFieldType" );
        put( "fieldLength", "getFieldLength" );
        put( "fieldPrecision", "getFieldPrecision" );
        put( "currency", "getCurrency" );
        put( "decimal", "getDecimal" );
        put( "group", "getGroup" );
        put( "trimType", "getTrimType" );
      }
    };

    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "fieldName", "setFieldName" );
        put( "variableString", "setVariableString" );
        put( "fieldFormat", "setFieldFormat" );
        put( "fieldType", "setFieldType" );
        put( "fieldLength", "setFieldLength" );
        put( "fieldPrecision", "setFieldPrecision" );
        put( "currency", "setCurrency" );
        put( "decimal", "setDecimal" );
        put( "group", "setGroup" );
        put( "trimType", "setTrimType" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 3 );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "fieldName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "variableString", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fieldFormat", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "currency", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "decimal", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "group", stringArrayLoadSaveValidator );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    typeValidatorMap.put( int[].class.getCanonicalName(), new PrimitiveIntArrayLoadSaveValidator(
        new NonZeroIntLoadSaveValidator( 4 ), 3 ) );

    loadSaveTester =
        new LoadSaveTester( GetVariableMeta.class, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testLoadSaveXML() throws KettleException {
    loadSaveTester.testXmlRoundTrip();
  }

  @Test
  public void testLoadSaveRepo() throws KettleException {
    loadSaveTester.testRepoRoundTrip();
  }

  public GetVariableMetaTest() throws KettleException {
    super();
  }

  @Test
  public void testClone1() {
    GetVariableMeta gvm = new GetVariableMeta();
    gvm.allocate( 1 );
    String[] fieldName = { "Test" };
    int[] fieldType = { ValueMeta.getType( "Number" ) };
    String[] varString = { "${testVariable}" };
    int[] fieldLength = { 1 };
    int[] fieldPrecision = { 2 };
    String[] currency = { "$" };
    String[] decimal = { "." };
    String[] group = { "TestGroup" };
    int[] trimType = { ValueMeta.getTrimTypeByDesc( "none" ) };

    gvm.setFieldName( fieldName );
    gvm.setFieldType( fieldType );
    gvm.setVariableString( varString );
    gvm.setFieldLength( fieldLength );
    gvm.setFieldPrecision( fieldPrecision );
    gvm.setCurrency( currency );
    gvm.setDecimal( decimal );
    gvm.setGroup( group );
    gvm.setTrimType( trimType );

    GetVariableMeta clone = (GetVariableMeta) gvm.clone();
    assertEquals( clone.getFieldName()[0], "Test" );
    assertEquals( clone.getFieldType()[0], ValueMetaInterface.TYPE_NUMBER );
    assertEquals( clone.getVariableString()[0], "${testVariable}" );
    assertEquals( clone.getFieldLength()[0], 1 );
    assertEquals( clone.getFieldPrecision()[0], 2 );
    assertEquals( clone.getCurrency()[0], "$" );
    assertEquals( clone.getDecimal()[0], "." );
    assertEquals( clone.getGroup()[0], "TestGroup" );
    assertEquals( clone.getTrimType()[0], ValueMetaInterface.TRIM_TYPE_NONE );
  }

  @Test
  public void testGetVariableMeta1() {
    GetVariableMeta gvm = new GetVariableMeta();
    assertNotNull( gvm );
    assertNull( gvm.getFieldName() );
    assertNull( gvm.getVariableString() );
    assertNull( gvm.getFieldFormat() );
    assertNull( gvm.getFieldType() );
    assertNull( gvm.getFieldLength() );
    assertNull( gvm.getFieldPrecision() );
    assertNull( gvm.getCurrency() );
    assertNull( gvm.getDecimal() );
    assertNull( gvm.getGroup() );
    assertNull( gvm.getTrimType() );
  }

  @Test
  public void testAllocate1() {
    GetVariableMeta gvm = new GetVariableMeta();
    gvm.allocate( 1 );
    assertNotNull( gvm.getFieldName() );
    assertNotNull( gvm.getVariableString() );
    assertNotNull( gvm.getFieldFormat() );
    assertNotNull( gvm.getFieldType() );
    assertNotNull( gvm.getFieldLength() );
    assertNotNull( gvm.getFieldPrecision() );
    assertNotNull( gvm.getCurrency() );
    assertNotNull( gvm.getDecimal() );
    assertNotNull( gvm.getGroup() );
    assertNotNull( gvm.getTrimType() );
  }

  public class FieldTypeLoadSaveValidator implements FieldLoadSaveValidator<Integer> {
    final Random rand = new Random();
    @Override
    public Integer getTestObject() {
      return rand.nextInt( 10 );
    }
    @Override
    public boolean validateTestObject( Integer testObject, Object actual ) {
      if ( !( actual instanceof Integer ) ) {
        return false;
      }
      Integer actualInt = (Integer) actual;
      return actualInt.equals( testObject );
    }
  }
}
