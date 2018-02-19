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
package org.pentaho.di.trans.steps.streamlookup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class StreamLookupMetaTest implements InitializerInterface<StepMetaInterface> {
  LoadSaveTester loadSaveTester;
  Class<StreamLookupMeta> testMetaClass = StreamLookupMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "inputSorted", "memoryPreservationActive", "usingSortedList", "usingIntegerPair", "keystream",
            "keylookup", "value", "valueName", "valueDefault", "valueDefaultType" );

    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "keystream", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "keylookup", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "value", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "valueName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "valueDefault", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "valueDefaultType", new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator( 7 ), 5 ) );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            new HashMap<String, String>(), new HashMap<String, String>(), attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof StreamLookupMeta ) {
      ( (StreamLookupMeta) someMeta ).allocate( 5, 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void testCloneInfoSteps() {
    StreamLookupMeta meta = new StreamLookupMeta();
    meta.setDefault();

    final String stepName = UUID.randomUUID().toString();
    StepMeta infoStep = mock( StepMeta.class );
    when( infoStep.getName() ).thenReturn( stepName );
    meta.getStepIOMeta().getInfoStreams().get( 0 ).setStepMeta( infoStep );

    StreamLookupMeta cloned = (StreamLookupMeta) meta.clone();
    assertEquals( stepName, cloned.getStepIOMeta().getInfoStreams().get( 0 ).getStepname() );
    assertNotSame( meta.getStepIOMeta().getInfoStreams().get( 0 ),
      cloned.getStepIOMeta().getInfoStreams().get( 0 ) );
  }

  //PDI-16110
  @Test
  public void testGetXML() {
    StreamLookupMeta streamLookupMeta = new StreamLookupMeta();
    streamLookupMeta.setKeystream( new String[] { "testKeyStreamValue" } );
    streamLookupMeta.setKeylookup( new String[] { "testKeyLookupValue" } );
    streamLookupMeta.setValue( new String[] { "testValue" } );
    streamLookupMeta.setValueName( new String[] {} );
    streamLookupMeta.setValueDefault( new String[] {} );
    streamLookupMeta.setValueDefaultType( new int[] {} );

    //run without exception
    streamLookupMeta.afterInjectionSynchronization();
    streamLookupMeta.getXML();

    Assert.assertEquals( streamLookupMeta.getKeystream().length, streamLookupMeta.getValueName().length );
    Assert.assertEquals( streamLookupMeta.getKeystream().length, streamLookupMeta.getValueDefault().length );
    Assert.assertEquals( streamLookupMeta.getKeystream().length, streamLookupMeta.getValueDefaultType().length );
  }
}
