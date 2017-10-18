/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.multimerge;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

/**
 * @author Tatsiana_Kasiankova
 *
 */
public class MultiMergeJoinMetaTest implements InitializerInterface<StepMetaInterface> {
  LoadSaveTester loadSaveTester;
  Class<MultiMergeJoinMeta> testMetaClass = MultiMergeJoinMeta.class;
  private MultiMergeJoinMeta multiMergeMeta;

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( true );
    multiMergeMeta = new MultiMergeJoinMeta();
    List<String> attributes =
        Arrays.asList( "joinType", "keyFields", "inputSteps" );

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );


    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "keyFields", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "inputSteps", stringArrayLoadSaveValidator );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof MultiMergeJoinMeta ) {
      ( (MultiMergeJoinMeta) someMeta ).allocateKeys( 5 );
      ( (MultiMergeJoinMeta) someMeta ).allocateInputSteps( 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void testSetGetInputSteps() {
    assertNull( multiMergeMeta.getInputSteps() );
    String[] inputSteps = new String[] { "Step1", "Step2" };
    multiMergeMeta.setInputSteps( inputSteps );
    assertArrayEquals( inputSteps, multiMergeMeta.getInputSteps() );
  }

  @Test
  public void testGetXml() {
    String[] inputSteps = new String[] { "Step1", "Step2" };
    multiMergeMeta.setInputSteps( inputSteps );
    multiMergeMeta.setKeyFields( new String[] {"Key1", "Key2"} );
    String xml = multiMergeMeta.getXML();
    Assert.assertTrue( xml.contains( "step0" ) );
    Assert.assertTrue( xml.contains( "step1" ) );

  }

  @Test
  public void cloneTest() throws Exception {
    MultiMergeJoinMeta meta = new MultiMergeJoinMeta();
    meta.allocateKeys( 2 );
    meta.allocateInputSteps( 3 );
    meta.setKeyFields( new String[] { "key1", "key2" } );
    meta.setInputSteps( new String[] { "step1", "step2", "step3" } );
    // scalars should be cloned using super.clone() - makes sure they're calling super.clone()
    meta.setJoinType( "INNER" );
    MultiMergeJoinMeta aClone = (MultiMergeJoinMeta) meta.clone();
    Assert.assertFalse( aClone == meta );
    Assert.assertTrue( Arrays.equals( meta.getKeyFields(), aClone.getKeyFields() ) );
    Assert.assertTrue( Arrays.equals( meta.getInputSteps(), aClone.getInputSteps() ) );
    Assert.assertEquals( meta.getJoinType(), aClone.getJoinType() );
  }
}
