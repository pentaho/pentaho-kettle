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

package org.pentaho.di.trans.steps.memgroupby;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class MemoryGroupByMetaInjectionTest extends BaseMetadataInjectionTest<MemoryGroupByMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
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
