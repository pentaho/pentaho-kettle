/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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
  }
}
