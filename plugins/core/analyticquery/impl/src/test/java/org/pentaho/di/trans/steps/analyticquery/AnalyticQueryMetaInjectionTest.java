/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.analyticquery;


import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;

public class AnalyticQueryMetaInjectionTest extends BaseMetadataInjectionTest<AnalyticQueryMeta> {
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
