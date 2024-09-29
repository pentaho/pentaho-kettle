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
