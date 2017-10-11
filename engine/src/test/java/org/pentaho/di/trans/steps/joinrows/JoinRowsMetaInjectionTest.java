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
package org.pentaho.di.trans.steps.joinrows;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;

public class JoinRowsMetaInjectionTest extends BaseMetadataInjectionTest<JoinRowsMeta> {
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
