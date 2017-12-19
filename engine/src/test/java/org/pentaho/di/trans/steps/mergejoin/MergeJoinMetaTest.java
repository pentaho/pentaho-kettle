/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016 - 2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.mergejoin;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidatorFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class MergeJoinMetaTest {

  LoadSaveTester loadSaveTester;

  public MergeJoinMetaTest() {
    //SwitchCaseMeta bean-like attributes
    List<String> attributes = Arrays.asList( "joinType", "keyFields1", "keyFields2" );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "joinType", "getJoinType" );
    getterMap.put( "keyFields1", "getKeyFields1" );
    getterMap.put( "keyFields2", "getKeyFields2" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "joinType", "setJoinType" );
    setterMap.put( "keyFields1", "setKeyFields1" );
    setterMap.put( "keyFields2", "setKeyFields2" );

    this.loadSaveTester = new LoadSaveTester( MergeJoinMeta.class,
      attributes,
      getterMap, setterMap,
      attrValidatorMap, typeValidatorMap );

    FieldLoadSaveValidatorFactory validatorFactory = loadSaveTester.getFieldLoadSaveValidatorFactory();

    FieldLoadSaveValidator<MergeJoinMeta> targetValidator = new FieldLoadSaveValidator<MergeJoinMeta>() {

      @Override
      public MergeJoinMeta getTestObject() {
        return new MergeJoinMeta() {
          {
            setJoinType( join_types[0] );
            setKeyFields1( new String[]{ "field1", "field2" } );
            setKeyFields2( new String[]{ "field1", "field3" } );
          }
        };
      }

      @Override
      public boolean validateTestObject( MergeJoinMeta testObject, Object actual ) {
        return testObject.getJoinType().equals( ( (MergeJoinMeta) actual ).getJoinType() )
          && Arrays.equals( testObject.getKeyFields1(), ( (MergeJoinMeta) actual ).getKeyFields1() )
          && Arrays.equals( testObject.getKeyFields2(), ( (MergeJoinMeta) actual ).getKeyFields2() );
      }
    };

    validatorFactory.registerValidator( validatorFactory.getName( MergeJoinMeta.class ), targetValidator );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void testGetFieldsEmptyInput() throws Exception {
    RowMeta outputRowMeta = new RowMeta();
    MergeJoinMeta meta = new MergeJoinMeta();

    RowMeta inputRow1 = new RowMeta();
    ValueMetaInteger field1_row1 = new ValueMetaInteger( "field1" );
    field1_row1.setOrigin( "inputStep1" );
    inputRow1.addValueMeta( field1_row1 );
    ValueMetaString field2_row1 = new ValueMetaString( "field2" );
    field2_row1.setOrigin( "inputStep1" );
    inputRow1.addValueMeta( field2_row1 );

    RowMeta inputRow2 = new RowMeta();
    ValueMetaString field1_row2 = new ValueMetaString( "field1" );
    field1_row2.setOrigin( "inputStep2" );
    inputRow2.addValueMeta( field1_row2 );
    ValueMetaString field3_row2 = new ValueMetaString( "field3" );
    field3_row2.setOrigin( "inputStep2" );
    inputRow2.addValueMeta( field3_row2 );

    StepMeta stepMeta = new StepMeta( "Merge", meta );

    meta.getFields( outputRowMeta, "Merge Join",
      new RowMetaInterface[]{ inputRow1, inputRow2 }, stepMeta, new Variables(), null, null );

    assertNotNull( outputRowMeta );
    assertFalse( outputRowMeta.isEmpty() );
    assertEquals( 4, outputRowMeta.size() );
    List<ValueMetaInterface> vmi = outputRowMeta.getValueMetaList();
    assertNotNull( vmi );
    // Proceed in order
    ValueMetaInterface field1 = outputRowMeta.getValueMeta( 0 );
    assertNotNull( field1 );
    assertEquals( "field1", field1.getName() );
    assertTrue( field1 instanceof ValueMetaInteger );
    assertEquals( "inputStep1", field1.getOrigin() );

    ValueMetaInterface field2 = outputRowMeta.getValueMeta( 1 );
    assertNotNull( field2 );
    assertEquals( "field2", field2.getName() );
    assertTrue( field2 instanceof ValueMetaString );
    assertEquals( "inputStep1", field2.getOrigin() );

    ValueMetaInterface field1_1 = outputRowMeta.getValueMeta( 2 );
    assertNotNull( field1_1 );
    assertEquals( "field1_1", field1_1.getName() );
    assertTrue( field1_1 instanceof ValueMetaString );
    assertEquals( "Merge Join", field1_1.getOrigin() );

    ValueMetaInterface field3 = outputRowMeta.getValueMeta( 3 );
    assertNotNull( field3 );
    assertEquals( "field3", field3.getName() );
    assertTrue( field3 instanceof ValueMetaString );
    assertEquals( "inputStep2", field3.getOrigin() );

  }

  @Test
  public void cloneTest() throws Exception {
    MergeJoinMeta meta = new MergeJoinMeta();
    meta.allocate( 2, 3 );
    meta.setKeyFields1( new String[] { "kf1-1", "kf1-2" } );
    meta.setKeyFields2( new String[] { "kf2-1", "kf2-2", "kf2-3" } );
    // scalars should be cloned using super.clone() - makes sure they're calling super.clone()
    meta.setJoinType( "INNER" );
    MergeJoinMeta aClone = (MergeJoinMeta) meta.clone();
    assertFalse( aClone == meta ); // Not same object returned by clone
    assertTrue( Arrays.equals( meta.getKeyFields1(), aClone.getKeyFields1() ) );
    assertTrue( Arrays.equals( meta.getKeyFields2(), aClone.getKeyFields2() ) );
    assertEquals( meta.getJoinType(), aClone.getJoinType() );

    assertNotNull( aClone.getStepIOMeta() );
    assertFalse( meta.getStepIOMeta() == aClone.getStepIOMeta() );
    List<StreamInterface> infoStreams = meta.getStepIOMeta().getInfoStreams();
    List<StreamInterface> cloneInfoStreams = aClone.getStepIOMeta().getInfoStreams();
    assertFalse( infoStreams == cloneInfoStreams );
    int streamSize = infoStreams.size();
    assertTrue( streamSize == cloneInfoStreams.size() );
    for ( int i = 0; i < streamSize; i++ ) {
      assertFalse( infoStreams.get( i ) == cloneInfoStreams.get( i ) );
    }
  }
}
