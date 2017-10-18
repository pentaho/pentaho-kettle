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

package org.pentaho.di.trans.steps.switchcase;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.core.row.value.ValueMetaBase;

public class SwitchCaseMetadataInjectionTest extends BaseMetadataInjectionTest<SwitchCaseMeta> {

  @Before
  public void setup() {
    super.setup( new SwitchCaseMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "FIELD_NAME", new StringGetter() {
      @Override
      public String get() {
        return meta.getFieldname();
      }
    } );
    String[] typeNames = ValueMetaBase.getAllTypes();
    checkStringToInt( "VALUE_TYPE", new IntGetter() {
      public int get() {
        return meta.getCaseValueType();
      }
    }, typeNames, getTypeCodes( typeNames ) );
    check( "VALUE_DECIMAL", new StringGetter() {
      @Override
      public String get() {
        return meta.getCaseValueDecimal();
      }
    } );
    check( "VALUE_GROUP", new StringGetter() {
      @Override
      public String get() {
        return meta.getCaseValueGroup();
      }
    } );
    check( "VALUE_FORMAT", new StringGetter() {
      @Override
      public String get() {
        return meta.getCaseValueFormat();
      }
    } );
    check( "CONTAINS", new BooleanGetter() {
      @Override
      public boolean get() {
        return meta.isContains();
      }
    } );
    check( "DEFAULT_TARGET_STEP_NAME", new StringGetter() {
      @Override
      public String get() {
        return meta.getDefaultTargetStepname();
      }
    } );
    check( "SWITCH_CASE_TARGET.CASE_VALUE", new StringGetter() {
      @Override
      public String get() {
        return meta.getCaseTargets().get( 0 ).caseValue;
      }
    } );
    check( "SWITCH_CASE_TARGET.CASE_TARGET_STEP_NAME", new StringGetter() {
      @Override
      public String get() {
        return meta.getCaseTargets().get( 0 ).caseTargetStepname;
      }
    } );
  }

}
