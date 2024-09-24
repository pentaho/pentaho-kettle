/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.denormaliser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

import org.junit.Assert;

public class DenormalizerMetaTest implements InitializerInterface<StepMetaInterface> {
  LoadSaveTester loadSaveTester;
  Class<DenormaliserMeta> testMetaClass = DenormaliserMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "groupField", "keyField", "denormaliserTargetField" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      //CHECKSTYLE IGNORE EmptyBlock FOR NEXT 3 LINES
      {
        // put( "fieldName", "getFieldName" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      //CHECKSTYLE IGNORE EmptyBlock FOR NEXT 3 LINES
      {
        // put( "fieldName", "setFieldName" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "groupField", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "denormaliserTargetField",
        new ArrayLoadSaveValidator<DenormaliserTargetField>( new DenormaliserTargetFieldLoadSaveValidator(), 5 ) );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof DenormaliserMeta ) {
      ( (DenormaliserMeta) someMeta ).allocate( 5, 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  public class DenormaliserTargetFieldLoadSaveValidator implements FieldLoadSaveValidator<DenormaliserTargetField> {
    final Random rand = new Random();
    @Override
    public DenormaliserTargetField getTestObject() {
      DenormaliserTargetField rtn = new DenormaliserTargetField();
      rtn.setFieldName( UUID.randomUUID().toString() );
      rtn.setKeyValue( UUID.randomUUID().toString() );
      rtn.setTargetCurrencySymbol( UUID.randomUUID().toString() );
      rtn.setTargetGroupingSymbol( UUID.randomUUID().toString() );
      rtn.setTargetName( UUID.randomUUID().toString() );
      rtn.setTargetType( rand.nextInt( 7 ) );
      rtn.setTargetPrecision( rand.nextInt( 9 ) );
      rtn.setTargetNullString( UUID.randomUUID().toString() );
      rtn.setTargetLength( rand.nextInt( 50 ) );
      rtn.setTargetDecimalSymbol( UUID.randomUUID().toString() );
      rtn.setTargetAggregationType( rand.nextInt( DenormaliserTargetField.typeAggrDesc.length ) );
      return rtn;
    }

    @Override
    public boolean validateTestObject( DenormaliserTargetField testObject, Object actual ) {
      if ( !( actual instanceof DenormaliserTargetField ) ) {
        return false;
      }
      DenormaliserTargetField another = (DenormaliserTargetField) actual;
      return new EqualsBuilder()
          .append( testObject.getFieldName(), another.getFieldName() )
          .append( testObject.getKeyValue(), another.getKeyValue() )
          .append( testObject.getTargetName(), another.getTargetName() )
          .append( testObject.getTargetType(), another.getTargetType() )
          .append( testObject.getTargetLength(), another.getTargetLength() )
          .append( testObject.getTargetPrecision(), another.getTargetPrecision() )
          .append( testObject.getTargetCurrencySymbol(), another.getTargetCurrencySymbol() )
          .append( testObject.getTargetDecimalSymbol(), another.getTargetDecimalSymbol() )
          .append( testObject.getTargetGroupingSymbol(), another.getTargetGroupingSymbol() )
          .append( testObject.getTargetNullString(), another.getTargetNullString() )
          .append( testObject.getTargetFormat(), another.getTargetFormat() )
          .append( testObject.getTargetAggregationType(), another.getTargetAggregationType() )
          .isEquals();
    }
  }

  @Test
  public void testGetFields() throws KettleStepException {

    final String PERSON = "Person";
    final String YEAR = "Year";
    final String SALARY = "Salary";
    final String Y2018 = "2018";
    final String Y2019 = "2019";
    final int TARGET_LENGTH = 10;
    final int TARGET_PRECISION = 2;
    final String TARGET_FORMAT = "0.00";
    final String TARGET_GROUP = ",";
    final String TARGET_DECIMAL = ".";
    final String TARGET_CURRENCY = "$";

    RowMetaInterface inputRow = new RowMeta();
    inputRow.addValueMeta( new ValueMetaString( PERSON ) );
    inputRow.addValueMeta( new ValueMetaInteger( YEAR ) );
    inputRow.addValueMeta( new ValueMetaNumber( SALARY ) );

    DenormaliserTargetField targetField2018 = new DenormaliserTargetField();
    targetField2018.setTargetName( Y2018 );
    targetField2018.setFieldName( SALARY );
    targetField2018.setTargetType( ValueMetaInterface.TYPE_BIGNUMBER );
    targetField2018.setTargetLength( TARGET_LENGTH );
    targetField2018.setTargetPrecision( TARGET_PRECISION );
    targetField2018.setTargetFormat( TARGET_FORMAT );
    targetField2018.setTargetDecimalSymbol( TARGET_DECIMAL );
    targetField2018.setTargetGroupingSymbol( TARGET_GROUP );
    targetField2018.setTargetCurrencySymbol( TARGET_CURRENCY );

    DenormaliserTargetField targetField2019 = new DenormaliserTargetField();
    targetField2019.setFieldName( SALARY );
    targetField2019.setTargetName( Y2019 );
    targetField2019.setTargetType( ValueMetaInterface.TYPE_BIGNUMBER );
    targetField2019.setTargetLength( TARGET_LENGTH );
    targetField2019.setTargetPrecision( TARGET_PRECISION );
    targetField2019.setTargetFormat( TARGET_FORMAT );
    targetField2019.setTargetDecimalSymbol( TARGET_DECIMAL );
    targetField2019.setTargetGroupingSymbol( TARGET_GROUP );
    targetField2019.setTargetCurrencySymbol( TARGET_CURRENCY );

    DenormaliserMeta meta = new DenormaliserMeta();
    meta.setDefault();
    meta.setKeyField( YEAR );
    meta.setGroupField( new String[] { PERSON } );
    meta.setDenormaliserTargetField( new DenormaliserTargetField[] { targetField2018, targetField2019 } );

    meta.getFields( inputRow, "Denormaliser", null, null, new Variables(), null, null );

    Assert.assertEquals( 3, inputRow.size() );

    Assert.assertEquals( PERSON, inputRow.getValueMeta( 0 ).getName() );
    Assert.assertEquals( Y2018, inputRow.getValueMeta( 1 ).getName() );
    Assert.assertEquals( Y2019, inputRow.getValueMeta( 2 ).getName() );

    for ( int i = 1; i <= 2; i++ ) {
      Assert.assertEquals( TARGET_LENGTH, inputRow.getValueMeta( i ).getLength() );
      Assert.assertEquals( TARGET_PRECISION, inputRow.getValueMeta( i ).getPrecision() );
      Assert.assertEquals( TARGET_FORMAT, inputRow.getValueMeta( i ).getFormatMask() );
      Assert.assertEquals( TARGET_DECIMAL, inputRow.getValueMeta( i ).getDecimalSymbol() );
      Assert.assertEquals( TARGET_GROUP, inputRow.getValueMeta( i ).getGroupingSymbol() );
      Assert.assertEquals( TARGET_CURRENCY, inputRow.getValueMeta( i ).getCurrencySymbol() );
    }

  }

}
