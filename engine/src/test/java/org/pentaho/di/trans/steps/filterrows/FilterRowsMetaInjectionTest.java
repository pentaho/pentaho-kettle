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

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class FilterRowsMetaInjectionTest extends BaseMetadataInjectionTest<FilterRowsMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new FilterRowsMeta() );
  }

  @Test
  public void test() throws Exception {

    check( "SEND_FALSE_STEP", new StringGetter() {
      public String get() {
        return meta.getFalseStepname();
      }
    } );

    check( "SEND_TRUE_STEP", new StringGetter() {
      public String get() {
        return meta.getTrueStepname();
      }
    } );

    check( "CONDITION", new StringGetter() {
      public String get() {
        return meta.getConditionXML();
      }
    }, new Condition().getXML() );
  }
}
