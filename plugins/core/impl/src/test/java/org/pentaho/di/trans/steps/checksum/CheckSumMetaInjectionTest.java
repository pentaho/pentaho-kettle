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


package org.pentaho.di.trans.steps.checksum;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class CheckSumMetaInjectionTest extends BaseMetadataInjectionTest<CheckSumMeta> {

  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new CheckSumMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "RESULT_FIELD", new StringGetter() {
      public String get() {
        return meta.getResultFieldName();
      }
    } );
    check( "TYPE", new StringGetter() {
      public String get() {
        return meta.getCheckSumType();
      }
    } );
    check( "COMPATIBILITY_MODE", new BooleanGetter() {
      public boolean get() {
        return meta.isCompatibilityMode();
      }
    } );
    check( "OLD_CHECKSUM_BEHAVIOR", new BooleanGetter() {
      public boolean get() {
        return meta.isOldChecksumBehaviour();
      }
    } );
    check( "RESULT_TYPE", new IntGetter() {
      public int get() {
        return meta.getResultType();
      }
    } );
    check( "FIELD_NAME", new StringGetter() {
      public String get() {
        return meta.getFieldName()[ 0 ];
      }
    } );
    check( "FIELD_SEPARATOR_STRING", new StringGetter() {
      public String get() {
        return meta.getFieldSeparatorString();
      }
    } );

    check( "EVALUATION_METHOD", new IntGetter() {
      public int get() {
        return meta.getEvaluationMethod();
      }
    } );
  }
}
