/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.insertupdate;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class InsertUpdateMetaInjectionTest extends BaseMetadataInjectionTest<InsertUpdateMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new InsertUpdateMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "SCHEMA_NAME", new StringGetter() {
      public String get() {
        return meta.getSchemaName();
      }
    } );
    check( "TABLE_NAME", new StringGetter() {
      public String get() {
        return meta.getTableName();
      }
    } );
    check( "COMMIT_SIZE", new StringGetter() {
      public String get() {
        return meta.getCommitSizeVar();
      }
    } );
    check( "DO_NOT", new BooleanGetter() {
      public boolean get() {
        return meta.isUpdateBypassed();
      }
    } );
    check( "KEY_STREAM", new StringGetter() {
      public String get() {
        return meta.getKeyFields()[ 0 ].getKeyStream();
      }
    } );
    check( "KEY_LOOKUP", new StringGetter() {
      public String get() {
        return meta.getKeyFields()[ 0 ].getKeyLookup();
      }
    } );
    check( "KEY_CONDITION", new StringGetter() {
      public String get() {
        return meta.getKeyFields()[ 0 ].getKeyCondition();
      }
    } );
    check( "KEY_STREAM2", new StringGetter() {
      public String get() {
        return meta.getKeyFields()[ 0 ].getKeyStream2();
      }
    } );
    check( "UPDATE_LOOKUP", new StringGetter() {
      public String get() {
        return meta.getUpdateFields()[ 0 ].getUpdateLookup();
      }
    } );
    check( "UPDATE_STREAM", new StringGetter() {
      public String get() {
        return meta.getUpdateFields()[ 0 ].getUpdateStream();
      }
    } );
    check( "UPDATE_FLAG", new BooleanGetter() {
      public boolean get() {
        return meta.getUpdateFields()[ 0 ].getUpdate();
      }
    } );
    skipPropertyTest( "CONNECTIONNAME" );
  }
}
