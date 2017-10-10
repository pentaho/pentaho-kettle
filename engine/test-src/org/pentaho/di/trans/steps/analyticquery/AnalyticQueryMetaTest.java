/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.analyticquery;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

public class AnalyticQueryMetaTest {

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes = Arrays.asList( "groupField", "aggregateField", "subjectField",
      "aggregateType", "valueField" );

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();

    Map<String, FieldLoadSaveValidator<?>> typeValidators = new HashMap<String, FieldLoadSaveValidator<?>>();
    Map<String, FieldLoadSaveValidator<?>> fieldValidators = new HashMap<String, FieldLoadSaveValidator<?>>();
    fieldValidators.put( "aggregateField", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 50 ) );
    fieldValidators.put( "subjectField", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 50 ) );
    fieldValidators.put( "aggregateType", new PrimitiveIntArrayLoadSaveValidator(
      new IntLoadSaveValidator( AnalyticQueryMeta.typeGroupCode.length ), 50 ) );
    fieldValidators.put( "valueField", new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator(), 50 ) );

    LoadSaveTester loadSaveTester =
      new LoadSaveTester( AnalyticQueryMeta.class, attributes, getterMap, setterMap, fieldValidators, typeValidators );
    loadSaveTester.testSerialization();
  }


  @Test
  public void testPDI16559() throws Exception {
    StepMockHelper<AnalyticQueryMeta, AnalyticQueryData> mockHelper =
            new StepMockHelper<AnalyticQueryMeta, AnalyticQueryData>( "analyticQuery", AnalyticQueryMeta.class, AnalyticQueryData.class );

    AnalyticQueryMeta analyticQuery = new AnalyticQueryMeta();
    analyticQuery.setGroupField( new String[] { "group1", "group2" } );
    analyticQuery.setSubjectField( new String[] { "field1", "field2", "field3", "field4", "field5" } );
    analyticQuery.setAggregateField( new String[] { "subj1", "subj2", "subj3" } );
    analyticQuery.setAggregateType( new int[] { 0, 1, 2, 3 } );
    analyticQuery.setValueField( new int[] { 0, 4, 8 } );

    try {
      String badXml = analyticQuery.getXML();
      Assert.fail( "Before calling afterInjectionSynchronization, should have thrown an ArrayIndexOOB" );
    } catch ( Exception expected ) {
      // Do Nothing
    }
    analyticQuery.afterInjectionSynchronization();
    //run without a exception
    String ktrXml = analyticQuery.getXML();

    int targetSz = analyticQuery.getSubjectField().length;

    Assert.assertEquals( targetSz, analyticQuery.getAggregateField().length );
    Assert.assertEquals( targetSz, analyticQuery.getAggregateType().length );
    Assert.assertEquals( targetSz, analyticQuery.getValueField().length );

  }
}
