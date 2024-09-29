/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.univariatestats;

import org.apache.commons.io.IOUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.metastore.api.IMetaStore;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UnivariateStatsMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private final FieldLoadSaveValidator<UnivariateStatsMetaFunction> univariateFunctionFieldLoadSaveValidator =
    new FieldLoadSaveValidator<>() {
      final Random random = new Random();

      @Override
      public boolean validateTestObject( UnivariateStatsMetaFunction testObject, Object actual ) {
        return testObject.getXML().equals( ( (UnivariateStatsMetaFunction) actual ).getXML() );
      }

      @Override
      public UnivariateStatsMetaFunction getTestObject() {
        return new UnivariateStatsMetaFunction( UUID.randomUUID().toString(), random.nextBoolean(), random
          .nextBoolean(), random.nextBoolean(), random.nextBoolean(), random.nextBoolean(), random.nextBoolean(),
          random.nextDouble(), random.nextBoolean() );
      }
    };
  private final ArrayLoadSaveValidator<UnivariateStatsMetaFunction> univariateFunctionArrayFieldLoadSaveValidator =
    new ArrayLoadSaveValidator<>( univariateFunctionFieldLoadSaveValidator );

  @Test
  public void testGetAndSetSetInputFieldMetaFunctions() {
    UnivariateStatsMetaFunction[] stats = new UnivariateStatsMetaFunction[3];
    UnivariateStatsMeta meta = new UnivariateStatsMeta();
    meta.setInputFieldMetaFunctions( stats );
    assertSame( stats, meta.getInputFieldMetaFunctions() );
  }

  @Test
  public void testAllocateAndGetNumFieldsToProcess() {
    UnivariateStatsMeta meta = new UnivariateStatsMeta();
    meta.allocate( 13 );
    assertEquals( 13, meta.getNumFieldsToProcess() );
  }

  @Test
  public void testLegacyLoadXml() throws IOException, KettleXMLException {
    String legacyXml =
        IOUtils.toString( UnivariateStatsMetaTest.class.getClassLoader().getResourceAsStream(
            "org/pentaho/di/trans/steps/univariatestats/legacyUnivariateStatsMetaTest.xml" ) );
    IMetaStore mockMetaStore = mock( IMetaStore.class );
    UnivariateStatsMeta meta = new UnivariateStatsMeta();
    meta.loadXML( XMLHandler.loadXMLString( legacyXml ).getFirstChild(), new ArrayList<>(), mockMetaStore );
    assertEquals( 2, meta.getNumFieldsToProcess() );
    UnivariateStatsMetaFunction first = meta.getInputFieldMetaFunctions()[0];
    assertEquals( "a", first.getSourceFieldName() );
    assertTrue( first.getCalcN() );
    assertTrue( first.getCalcMean() );
    assertTrue( first.getCalcStdDev() );
    assertTrue( first.getCalcMin() );
    assertTrue( first.getCalcMax() );
    assertTrue( first.getCalcMedian() );
    assertEquals( .5, first.getCalcPercentile(), 0 );
    assertTrue( first.getInterpolatePercentile() );
    UnivariateStatsMetaFunction second = meta.getInputFieldMetaFunctions()[1];
    assertEquals( "b", second.getSourceFieldName() );
    assertFalse( second.getCalcN() );
    assertFalse( second.getCalcMean() );
    assertFalse( second.getCalcStdDev() );
    assertFalse( second.getCalcMin() );
    assertFalse( second.getCalcMax() );
    assertFalse( second.getCalcMedian() );
    assertEquals( -1.0, second.getCalcPercentile(), 0 );
    assertFalse( second.getInterpolatePercentile() );
  }

  @Test
  public void loadSaveRoundTripTest() throws KettleException {
    List<String> attributes = List.of( "inputFieldMetaFunctions" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorTypeMap =
      new HashMap<>();

    fieldLoadSaveValidatorTypeMap.put( UnivariateStatsMetaFunction[].class.getCanonicalName(),
        univariateFunctionArrayFieldLoadSaveValidator );

    LoadSaveTester loadSaveTester =
        new LoadSaveTester( UnivariateStatsMeta.class, attributes, new HashMap<String, String>(),
            new HashMap<String, String>(), new HashMap<String, FieldLoadSaveValidator<?>>(),
            fieldLoadSaveValidatorTypeMap );

    loadSaveTester.testSerialization();
  }

  private void assertContains( Map<String, Integer> map, String key, Integer value ) {
    assertTrue( "Expected map to contain " + key, map.containsKey( key ) );
    assertSame( "Expected key of " + key + " to be of type " + ValueMetaBase.getTypeDesc( value ), map.get( key ),
      value );
  }

  @Test
  public void testGetFields() throws KettleStepException {
    UnivariateStatsMeta meta = new UnivariateStatsMeta();
    UnivariateStatsMetaFunction[] functions = univariateFunctionArrayFieldLoadSaveValidator.getTestObject();
    meta.setInputFieldMetaFunctions( functions );
    RowMetaInterface mockRowMetaInterface = mock( RowMetaInterface.class );
    final AtomicBoolean clearCalled = new AtomicBoolean( false );
    final List<ValueMetaInterface> valueMetaInterfaces = new ArrayList<>();
    doAnswer( (Answer<Void>) invocation -> {
      clearCalled.set( true );
      return null;
    } ).when( mockRowMetaInterface ).clear();
    doAnswer( (Answer<Void>) invocation -> {
      if ( !clearCalled.get() ) {
        throw new RuntimeException( "Clear not called before adding value metas" );
      }
      valueMetaInterfaces.add( (ValueMetaInterface) invocation.getArguments()[0] );
      return null;
    } ).when( mockRowMetaInterface ).addValueMeta( any( ValueMetaInterface.class ) );
    meta.getFields( mockRowMetaInterface, null, null, null, null, null, null );
    Map<String, Integer> valueMetas = new HashMap<>();
    for ( ValueMetaInterface vmi : valueMetaInterfaces ) {
      valueMetas.put( vmi.getName(), vmi.getType() );
    }
    for ( UnivariateStatsMetaFunction function : functions ) {
      if ( function.getCalcN() ) {
        assertContains( valueMetas, function.getSourceFieldName() + "(N)", ValueMetaInterface.TYPE_NUMBER );
      }
      if ( function.getCalcMean() ) {
        assertContains( valueMetas, function.getSourceFieldName() + "(mean)", ValueMetaInterface.TYPE_NUMBER );
      }
      if ( function.getCalcStdDev() ) {
        assertContains( valueMetas, function.getSourceFieldName() + "(stdDev)", ValueMetaInterface.TYPE_NUMBER );
      }
      if ( function.getCalcMin() ) {
        assertContains( valueMetas, function.getSourceFieldName() + "(min)", ValueMetaInterface.TYPE_NUMBER );
      }
      if ( function.getCalcMax() ) {
        assertContains( valueMetas, function.getSourceFieldName() + "(max)", ValueMetaInterface.TYPE_NUMBER );
      }
      if ( function.getCalcMedian() ) {
        assertContains( valueMetas, function.getSourceFieldName() + "(median)", ValueMetaInterface.TYPE_NUMBER );
      }
      if ( function.getCalcPercentile() >= 0 ) {
        NumberFormat pF = NumberFormat.getInstance();
        pF.setMaximumFractionDigits( 2 );
        String res = pF.format( function.getCalcPercentile() * 100 );
        assertContains( valueMetas, function.getSourceFieldName() + "(" + res + "th percentile)",
            ValueMetaInterface.TYPE_NUMBER );
      }
    }
  }

  @Test
  public void testCheckNullPrev() {
    UnivariateStatsMeta meta = new UnivariateStatsMeta();
    List<CheckResultInterface> remarks = new ArrayList<>();
    meta.check( remarks, null, null, null, new String[0], null, null, null, null, null );
    assertEquals( 2, remarks.size() );
    assertEquals( "Not receiving any fields from previous steps!", remarks.get( 0 ).getText() );
  }

  @Test
  public void testCheckEmptyPrev() {
    UnivariateStatsMeta meta = new UnivariateStatsMeta();
    RowMetaInterface mockRowMetaInterface = mock( RowMetaInterface.class );
    when( mockRowMetaInterface.size() ).thenReturn( 0 );
    List<CheckResultInterface> remarks = new ArrayList<>();
    meta.check( remarks, null, null, mockRowMetaInterface, new String[0], null, null, null, null, null );
    assertEquals( 2, remarks.size() );
    assertEquals( "Not receiving any fields from previous steps!", remarks.get( 0 ).getText() );
  }

  @Test
  public void testCheckGoodPrev() {
    UnivariateStatsMeta meta = new UnivariateStatsMeta();
    RowMetaInterface mockRowMetaInterface = mock( RowMetaInterface.class );
    when( mockRowMetaInterface.size() ).thenReturn( 500 );
    List<CheckResultInterface> remarks = new ArrayList<>();
    meta.check( remarks, null, null, mockRowMetaInterface, new String[0], null, null, null, null, null );
    assertEquals( 2, remarks.size() );
    assertEquals( "Step is connected to previous one, receiving " + 500 + " fields", remarks.get( 0 ).getText() );
  }

  @Test
  public void testCheckWithInput() {
    UnivariateStatsMeta meta = new UnivariateStatsMeta();
    List<CheckResultInterface> remarks = new ArrayList<>();
    meta.check( remarks, null, null, null, new String[1], null, null, null, null, null );
    assertEquals( 2, remarks.size() );
    assertEquals( "Step is receiving info from other steps.", remarks.get( 1 ).getText() );
  }

  @Test
  public void testCheckWithoutInput() {
    UnivariateStatsMeta meta = new UnivariateStatsMeta();
    List<CheckResultInterface> remarks = new ArrayList<>();
    meta.check( remarks, null, null, null, new String[0], null, null, null, null, null );
    assertEquals( 2, remarks.size() );
    assertEquals( "No input received from other steps!", remarks.get( 1 ).getText() );
  }

  @Test
  public void testGetStep() {
    StepMeta mockStepMeta = mock( StepMeta.class );
    when( mockStepMeta.getName() ).thenReturn( "testName" );
    StepDataInterface mockStepDataInterface = mock( StepDataInterface.class );
    int cnr = 10;
    TransMeta mockTransMeta = mock( TransMeta.class );
    Trans mockTrans = mock( Trans.class );
    when( mockTransMeta.findStep( "testName" ) ).thenReturn( mockStepMeta );
    StepInterface step =
        new UnivariateStatsMeta().getStep( mockStepMeta, mockStepDataInterface, cnr, mockTransMeta, mockTrans );
    assertTrue( "Expected Step to be instanceof " + UnivariateStats.class, step instanceof UnivariateStats );
  }

  @Test
  public void testGetStepData() {
    assertTrue( "Expected StepData to be instanceof " + UnivariateStatsData.class, new UnivariateStatsMeta()
        .getStepData() instanceof UnivariateStatsData );
  }
}
