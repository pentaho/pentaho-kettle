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
* Unless required by applicable law or agreed to in writpackage engine;

public class MemoryGroupByMetaInjectionTest {

}
ing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
******************************************************************************/
package org.pentaho.di.trans.steps.memgroupby;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;

public class MemoryGroupByMetaInjectionTest extends BaseMetadataInjectionTest<MemoryGroupByMeta> {
  @Before
  public void setup() {
    setup( new MemoryGroupByMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "GROUPFIELD", new StringGetter() {
      @Override
      public String get() {
        return meta.getGroupField()[0];
      }
    } );
    check( "AGGREGATEFIELD", new StringGetter() {
      @Override
      public String get() {
        return meta.getAggregateField()[0];
      }
    } );
    check( "SUBJECTFIELD", new StringGetter() {
      @Override
      public String get() {
        return meta.getSubjectField()[0];
      }
    } );
    check( "AGGREGATETYPE", new IntGetter() {
      @Override
      public int get() {
        return meta.getAggregateType()[0];
      }
    } );
    check( "VALUEFIELD", new StringGetter() {
      @Override
      public String get() {
        return meta.getValueField()[0];
      }
    } );
    check( "ALWAYSGIVINGBACKONEROW", new BooleanGetter() {
        @Override
        public boolean get() {
          return meta.isAlwaysGivingBackOneRow();
        }
      } );
  }
}
