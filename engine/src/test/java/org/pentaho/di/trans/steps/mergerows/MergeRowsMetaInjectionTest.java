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


package org.pentaho.di.trans.steps.mergerows;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class MergeRowsMetaInjectionTest extends BaseMetadataInjectionTest<MergeRowsMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Before
  public void setup() {
    setup( new MergeRowsMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "FLAG_FIELD", new StringGetter() {
      public String get() {
        return meta.getFlagField();
      }
    } );
    check( "KEY_FIELDS", new StringGetter() {
      public String get() {
        return meta.getKeyFields()[0];
      }
    } );
    check( "VALUE_FIELDS", new StringGetter() {
      public String get() {
        return meta.getValueFields()[0];
      }
    } );
  }
}
