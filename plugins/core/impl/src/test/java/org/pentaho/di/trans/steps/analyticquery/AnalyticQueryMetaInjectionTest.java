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


package org.pentaho.di.trans.steps.analyticquery;


import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class AnalyticQueryMetaInjectionTest extends BaseMetadataInjectionTest<AnalyticQueryMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Before
  public void setup() {
    setup( new AnalyticQueryMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "GROUP_FIELDS", new StringGetter() {
      public String get() {
        return meta.getGroupField()[ 0 ];
      }
    } );
    check( "OUTPUT.AGGREGATE_FIELD", new StringGetter() {
      public String get() {
        return meta.getAggregateField()[ 0 ];
      }
    } );
    check( "OUTPUT.SUBJECT_FIELD", new StringGetter() {
      public String get() {
        return meta.getSubjectField()[ 0 ];
      }
    } );
    check( "OUTPUT.AGGREGATE_TYPE", new IntGetter() {
      public int get() {
        return meta.getAggregateType()[ 0 ];
      }
    } );
    check( "OUTPUT.VALUE_FIELD", new IntGetter() {
      public int get() {
        return meta.getValueField()[ 0 ];
      }
    } );
  }
}
