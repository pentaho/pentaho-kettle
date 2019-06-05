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

package org.pentaho.di.trans.steps.blockuntilstepsfinish;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class BlockUntilStepsFinishMetaTest implements InitializerInterface<BlockUntilStepsFinishMeta> {
  LoadSaveTester<BlockUntilStepsFinishMeta> loadSaveTester;
  Class<BlockUntilStepsFinishMeta> testMetaClass = BlockUntilStepsFinishMeta.class;

  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    PluginRegistry.init( false );
    List<String> attributes =
      Arrays.asList( "stepName", "stepCopyNr" );

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "stepName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "stepCopyNr", stringArrayLoadSaveValidator );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
      new LoadSaveTester<BlockUntilStepsFinishMeta>( testMetaClass, attributes, new ArrayList<String>(),
        new ArrayList<String>(), getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( BlockUntilStepsFinishMeta someMeta ) {
    if ( someMeta instanceof BlockUntilStepsFinishMeta ) {
      ( (BlockUntilStepsFinishMeta) someMeta ).allocate( 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void cloneTest() throws Exception {
    BlockUntilStepsFinishMeta meta = new BlockUntilStepsFinishMeta();
    meta.allocate( 2 );
    meta.setStepName( new String[] { "step1", "step2" } );
    meta.setStepCopyNr( new String[] { "copy1", "copy2" } );
    BlockUntilStepsFinishMeta aClone = (BlockUntilStepsFinishMeta) meta.clone();
    assertFalse( aClone == meta );
    assertTrue( Arrays.equals( meta.getStepName(), aClone.getStepName() ) );
    assertTrue( Arrays.equals( meta.getStepCopyNr(), aClone.getStepCopyNr() ) );
    assertEquals( meta.getXML(), aClone.getXML() );
  }
}
