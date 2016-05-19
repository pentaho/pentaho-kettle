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

package org.pentaho.di.trans.steps.getvariable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransformationTestCase;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class GetVariableMetaTest extends TransformationTestCase {

  public GetVariableMetaTest() throws KettleException {
    super();
  }

  @Test
  public void testClone1() {
    GetVariableMeta gvm = new GetVariableMeta();
    gvm.allocate( 1 );
    String[] fieldName = { "Test" };
    int[] fieldType = { ValueMetaFactory.getIdForValueMeta( "Number" ) };
    String[] varString = { "${testVariable}" };
    int[] fieldLength = { 1 };
    int[] fieldPrecision = { 2 };
    String[] currency = { "$" };
    String[] decimal = { "." };
    String[] group = { "TestGroup" };
    int[] trimType = { ValueMetaString.getTrimTypeByDesc( "none" ) };

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

  @Test
  public void testLoadSave() throws KettleException {
    List<String> attributes = Arrays.asList( "FieldName", "VariableString", "FieldType", "FieldFormat", "Currency",
      "Decimal", "Group", "FieldLength", "FieldPrecision", "TrimType" );

    int copies = new Random().nextInt( 20 ) + 1;
    FieldLoadSaveValidator<?> stringArrayValidator =
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), copies );
    FieldLoadSaveValidator<?> intArrayValidator =
      new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator(), copies );
    FieldLoadSaveValidator<?> fieldTypeArrayValidator = new PrimitiveIntArrayLoadSaveValidator(
      new IntLoadSaveValidator( ValueMetaFactory.getAllValueMetaNames().length ), copies );
    FieldLoadSaveValidator<?> trimTypeArrayValidator = new PrimitiveIntArrayLoadSaveValidator(
        new IntLoadSaveValidator( ValueMetaString.trimTypeCode.length ), copies );

    Map<String, FieldLoadSaveValidator<?>> attributeMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attributeMap.put( "FieldName", stringArrayValidator );
    attributeMap.put( "VariableString", stringArrayValidator );
    attributeMap.put( "FieldType", fieldTypeArrayValidator );
    attributeMap.put( "FieldFormat", stringArrayValidator );
    attributeMap.put( "Currency", stringArrayValidator );
    attributeMap.put( "Decimal", stringArrayValidator );
    attributeMap.put( "Group", stringArrayValidator );
    attributeMap.put( "FieldLength", intArrayValidator );
    attributeMap.put( "FieldPrecision", intArrayValidator );
    attributeMap.put( "TrimType", trimTypeArrayValidator );

    LoadSaveTester loadSaveTester = new LoadSaveTester( GetVariableMeta.class, attributes,
      new HashMap<String, String>(), new HashMap<String, String>(), attributeMap,
      new HashMap<String, FieldLoadSaveValidator<?>>() );

    loadSaveTester.testSerialization();
  }
}
