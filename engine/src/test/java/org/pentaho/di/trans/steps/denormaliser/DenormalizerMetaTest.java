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
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

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

}
