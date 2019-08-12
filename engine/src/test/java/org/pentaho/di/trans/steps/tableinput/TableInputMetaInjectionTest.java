/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.tableinput;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class TableInputMetaInjectionTest extends BaseMetadataInjectionTest<TableInputMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new TableInputMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "SQL", new StringGetter() {
      public String get() {
        return meta.getSQL();
      }
    } );
    check( "LIMIT", new StringGetter() {
      public String get() {
        return meta.getRowLimit();
      }
    } );
    check( "EXECUTE_FOR_EACH_ROW", new BooleanGetter() {
      public boolean get() {
        return meta.isExecuteEachInputRow();
      }
    } );
    check( "REPLACE_VARIABLES", new BooleanGetter() {
      public boolean get() {
        return meta.isVariableReplacementActive();
      }
    } );
    check( "LAZY_CONVERSION", new BooleanGetter() {
      public boolean get() {
        return meta.isLazyConversionActive();
      }
    } );
    check( "CACHED_ROW_META", new BooleanGetter() {
      public boolean get() {
        return meta.isCachedRowMetaActive();
      }
    } );
    skipPropertyTest( "CONNECTIONNAME" );
  }
}
