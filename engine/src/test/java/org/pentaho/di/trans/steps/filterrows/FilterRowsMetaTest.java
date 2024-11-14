/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.trans.steps.filterrows;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.pentaho.di.core.Condition;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ConditionLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class FilterRowsMetaTest {
  LoadSaveTester loadSaveTester;
  Class<FilterRowsMeta> testMetaClass = FilterRowsMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "condition", "send_true_to", "send_false_to" );

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "condition", new ConditionLoadSaveValidator() );
    attrValidatorMap.put( "trueStepName", new StringLoadSaveValidator() );
    attrValidatorMap.put( "falseStepname", new StringLoadSaveValidator() );

    getterMap.put( "send_true_to", "getTrueStepname" );
    setterMap.put( "send_true_to", "setTrueStepname" );
    getterMap.put( "send_false_to", "getFalseStepname" );
    setterMap.put( "send_false_to", "setFalseStepname" );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void testClone() {
    FilterRowsMeta filterRowsMeta = new FilterRowsMeta();
    filterRowsMeta.setCondition( new Condition() );
    filterRowsMeta.setTrueStepname( "true" );
    filterRowsMeta.setFalseStepname( "false" );

    FilterRowsMeta clone = (FilterRowsMeta) filterRowsMeta.clone();
    assertNotNull( clone.getCondition() );
    assertEquals( "true", clone.getTrueStepname() );
    assertEquals( "false", clone.getFalseStepname() );
  }

  @Test
  public void modifiedTarget() throws Exception {
    FilterRowsMeta filterRowsMeta = new FilterRowsMeta();
    StepMeta trueOutput = new StepMeta( "true", new DummyTransMeta() );
    StepMeta falseOutput = new StepMeta( "false", new DummyTransMeta() );

    filterRowsMeta.setCondition( new Condition() );
    filterRowsMeta.setTrueStepname( trueOutput.getName() );
    filterRowsMeta.setFalseStepname( falseOutput.getName() );
    filterRowsMeta.searchInfoAndTargetSteps( ImmutableList.of( trueOutput, falseOutput ) );

    trueOutput.setName( "true renamed" );
    falseOutput.setName( "false renamed" );

    assertEquals( "true renamed", filterRowsMeta.getTrueStepname() );
    assertEquals( "false renamed", filterRowsMeta.getFalseStepname() );
  }
}
