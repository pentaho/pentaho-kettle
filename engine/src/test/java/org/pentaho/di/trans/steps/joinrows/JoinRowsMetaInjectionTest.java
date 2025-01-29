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

package org.pentaho.di.trans.steps.joinrows;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JoinRowsMetaInjectionTest extends BaseMetadataInjectionTest<JoinRowsMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Before
  public void setup() {
    setup( new JoinRowsMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "TEMP_DIR", new StringGetter() {
      public String get() {
        return meta.getDirectory();
      }
    } );
    check( "TEMP_FILE_PREFIX", new StringGetter() {
      public String get() {
        return meta.getPrefix();
      }
    } );
    check( "MAX_CACHE_SIZE", new IntGetter() {
      public int get() {
        return meta.getCacheSize();
      }
    } );
    check( "MAIN_STEP", new StringGetter() {
      public String get() {
        return meta.getMainStepname();
      }
    } );
    skipPropertyTest( "CONDITION" );
  }
}
