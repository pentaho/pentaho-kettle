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

package org.pentaho.di.trans.steps.elasticsearchbulk;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class ElasticSearchBulkMetaInjectionTest extends BaseMetadataInjectionTest<ElasticSearchBulkMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new ElasticSearchBulkMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "INDEX_NAME", new StringGetter() {
      public String get() {
        return meta.getIndex();
      }
    } );
    check( "INDEX_TYPE", new StringGetter() {
      public String get() {
        return meta.getType();
      }
    } );
    check( "JSON_INPUT", new BooleanGetter() {
      public boolean get() {
        return meta.isJsonInsert();
      }
    } );
    check( "JSON_FIELD", new StringGetter() {
      public String get() {
        return meta.getJsonField();
      }
    } );
    check( "ID_FIELD", new StringGetter() {
      public String get() {
        return meta.getIdInField();
      }
    } );
    check( "OVERWRITE_IF_EXIST", new BooleanGetter() {
      public boolean get() {
        return meta.isOverWriteIfSameId();
      }
    } );
    check( "ID_OUTPUT_FIELD", new StringGetter() {
      public String get() {
        return meta.getIdOutField();
      }
    } );
    check( "USE_OUTPUT", new BooleanGetter() {
      public boolean get() {
        return meta.isUseOutput();
      }
    } );
    check( "STOP_ON_ERROR", new BooleanGetter() {
      public boolean get() {
        return meta.isStopOnError();
      }
    } );
    check( "BATCH_SIZE", new StringGetter() {
      public String get() {
        return meta.getBatchSize();
      }
    } );
    check( "TIMEOUT_VALUE", new StringGetter() {
      public String get() {
        return meta.getTimeOut();
      }
    } );
    check( "TIMEOUT_UNIT", new EnumGetter() {
      public TimeUnit get() {
        return meta.getTimeoutUnit();
      }
    }, TimeUnit.class );
    check( "SERVER.ADDRESS", new StringGetter() {
      public String get() {
        return meta.servers.get( 0 ).address;
      }
    } );
    check( "SERVER.PORT", new IntGetter() {
      public int get() {
        return meta.servers.get( 0 ).port;
      }
    } );
    check( "FIELD.NAME", new StringGetter() {
      public String get() {
        return meta.fields.get( 0 ).name;
      }
    } );
    check( "FIELD.TARGET_NAME", new StringGetter() {
      public String get() {
        return meta.fields.get( 0 ).targetName;
      }
    } );
    check( "SETTING.NAME", new StringGetter() {
      public String get() {
        return meta.settings.get( 0 ).setting;
      }
    } );
    check( "SETTING.VALUE", new StringGetter() {
      public String get() {
        return meta.settings.get( 0 ).value;
      }
    } );
  }
}
