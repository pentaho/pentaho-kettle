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

package org.pentaho.di.trans.steps.switchcase;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidatorFactory;
import org.pentaho.di.trans.steps.loadsave.validator.ListLoadSaveValidator;

/**
 * @author nhudak
 */
public class SwitchCaseMetaTest {

  LoadSaveTester<SwitchCaseMeta> loadSaveTester;

  public SwitchCaseMetaTest() {
    //SwitchCaseMeta bean-like attributes
    List<String> attributes = Arrays.asList(
      "fieldname",
      "isContains",
      "caseValueFormat", "caseValueDecimal", /* "caseValueType",*/"caseValueGroup",
      "defaultTargetStepname",
      "caseTargets" );

    //Non-standard getters & setters
    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "isContains", "isContains" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "isContains", "setContains" );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap =
      new HashMap<String, FieldLoadSaveValidator<?>>();

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap =
      new HashMap<String, FieldLoadSaveValidator<?>>();

    this.loadSaveTester = new LoadSaveTester<>( SwitchCaseMeta.class,
      attributes,
      getterMap, setterMap,
      attrValidatorMap, typeValidatorMap );

    FieldLoadSaveValidatorFactory validatorFactory = loadSaveTester.getFieldLoadSaveValidatorFactory();

    FieldLoadSaveValidator<SwitchCaseTarget> targetValidator = new FieldLoadSaveValidator<SwitchCaseTarget>() {
      private final StepMetaInterface targetStepInterface = new DummyTransMeta();

      @Override
      public SwitchCaseTarget getTestObject() {
        return new SwitchCaseTarget() {
          {
            caseValue = UUID.randomUUID().toString();
            caseTargetStepname = UUID.randomUUID().toString();
            caseTargetStep = new StepMeta( caseTargetStepname, targetStepInterface );
          }
        };
      }

      @Override
      public boolean validateTestObject( SwitchCaseTarget testObject, Object actual ) {
        return testObject.caseValue.equals( ( (SwitchCaseTarget) actual ).caseValue )
          && testObject.caseTargetStepname.equals( ( (SwitchCaseTarget) actual ).caseTargetStepname );
      }
    };

    validatorFactory.registerValidator( validatorFactory.getName( SwitchCaseTarget.class ), targetValidator );
    validatorFactory.registerValidator( validatorFactory.getName( List.class, SwitchCaseTarget.class ),
      new ListLoadSaveValidator<SwitchCaseTarget>( targetValidator ) );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  // Note - cloneTest removed because it's now covered by the load/save tester


  @Test
  public void testsearchInfoAndTargetStepsTwice() {
    StepMetaInterface defStep = new DummyTransMeta();
    StepMeta stepMeta = new StepMeta( "id", "default", defStep );

    SwitchCaseMeta meta = new SwitchCaseMeta();
    meta.allocate();
    meta.setDefaultTargetStepname( stepMeta.getName() );
    meta.searchInfoAndTargetSteps( Collections.singletonList( stepMeta ) );
    // would throw npe
    meta.searchInfoAndTargetSteps( Collections.singletonList( stepMeta ) );
  }
}
