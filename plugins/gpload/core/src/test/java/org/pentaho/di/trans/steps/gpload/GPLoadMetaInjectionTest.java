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

package org.pentaho.di.trans.steps.gpload;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class GPLoadMetaInjectionTest extends BaseMetadataInjectionTest<GPLoadMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Before
  public void setup() {
    setup( new GPLoadMeta() );
  }

  @Test
  public void test() throws Exception {

    check( "LOCALHOST_NAME", new StringGetter() {
      public String get() {
        return meta.getLocalHosts()[0];
      }
    } );
    check( "PORT", new StringGetter() {
      public String get() {
        return meta.getLocalhostPort();
      }
    } );
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
    check( "ERROR_TABLE", new StringGetter() {
      public String get() {
        return meta.getErrorTableName();
      }
    } );
    check( "GPLOAD_PATH", new StringGetter() {
      public String get() {
        return meta.getGploadPath();
      }
    } );
    check( "CONTROL_FILE", new StringGetter() {
      public String get() {
        return meta.getControlFile();
      }
    } );
    check( "DATA_FILE", new StringGetter() {
      public String get() {
        return meta.getDataFile();
      }
    } );
    check( "LOG_FILE", new StringGetter() {
      public String get() {
        return meta.getLogFile();
      }
    } );
    check( "NULL_AS", new StringGetter() {
      public String get() {
        return meta.getNullAs();
      }
    } );
    check( "FIELD_TABLE", new StringGetter() {
      public String get() {
        return meta.getFieldTable()[0];
      }
    } );
    check( "FIELD_STREAM", new StringGetter() {
      public String get() {
        return meta.getFieldStream()[0];
      }
    } );
    check( "FIELD_MATCH", new BooleanGetter() {
      public boolean get() {
        return meta.getMatchColumn()[0];
      }
    } );
    check( "FIELD_UPDATE", new BooleanGetter() {
      public boolean get() {
        return meta.getUpdateColumn()[0];
      }
    } );
    check( "FIELD_DATEMASK", new StringGetter() {
      public String get() {
        return meta.getDateMask()[0];
      }
    } );
    check( "MAX_ERRORS", new StringGetter() {
      public String get() {
        return meta.getMaxErrors();
      }
    } );
    check( "LOAD_METHOD", new StringGetter() {
      public String get() {
        return meta.getLoadMethod();
      }
    } );
    check( "LOAD_ACTION", new StringGetter() {
      public String get() {
        return meta.getLoadAction();
      }
    } );
    check( "ENCODING", new StringGetter() {
      public String get() {
        return meta.getEncoding();
      }
    } );
    check( "ERASE_FILE", new BooleanGetter() {
      public boolean get() {
        return meta.isEraseFiles();
      }
    } );
    check( "ENCLOSURE_NUMBERS", new BooleanGetter() {
      public boolean get() {
        return meta.getEncloseNumbers();
      }
    } );
    check( "DELIMITER", new StringGetter() {
      public String get() {
        return meta.getDelimiter();
      }
    } );
    check( "UPDATE_CONDITIONS", new StringGetter() {
      public String get() {
        return meta.getUpdateCondition();
      }
    } );
  }
}
