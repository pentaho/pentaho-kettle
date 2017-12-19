/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.valuemapper;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;

public class ValueMapperMetaInjectionTest extends BaseMetadataInjectionTest<ValueMapperMeta> {
  @Before
  public void setup() {
    setup( new ValueMapperMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "FIELDNAME", new StringGetter() {
      public String get() {
        return meta.getFieldToUse();
      }
    } );
    check( "TARGET_FIELDNAME", new StringGetter() {
      public String get() {
        return meta.getTargetField();
      }
    } );
    check( "NON_MATCH_DEFAULT", new StringGetter() {
      public String get() {
        return meta.getNonMatchDefault();
      }
    } );
    check( "SOURCE", new StringGetter() {
      public String get() {
        return meta.getSourceValue()[0];
      }
    } );
    check( "TARGET", new StringGetter() {
      public String get() {
        return meta.getTargetValue()[0];
      }
    } );
  }
}
