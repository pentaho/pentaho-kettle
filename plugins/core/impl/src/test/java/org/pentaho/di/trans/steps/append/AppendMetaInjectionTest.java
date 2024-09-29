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


package org.pentaho.di.trans.steps.append;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class AppendMetaInjectionTest extends BaseMetadataInjectionTest<AppendMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Before
  public void setup() {
    setup( new AppendMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "HEAD_STEP", new StringGetter() {
      public String get() {
        return meta.headStepname;
      }
    } );
    check( "TAIL_STEP", new StringGetter() {
      public String get() {
        return meta.tailStepname;
      }
    } );
  }
}
