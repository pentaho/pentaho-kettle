/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.trans.steps.switchcase;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class SwitchCaseMetadataInjectionTest extends BaseMetadataInjectionTest<SwitchCaseMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

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
