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


package org.pentaho.di.trans.steps.update;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class UpdateMetaInjectionTest extends BaseMetadataInjectionTest<UpdateMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Before
  public void setup() {
    setup( new UpdateMeta() );
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
    check( "BATCH_UPDATE", new BooleanGetter() {
      public boolean get() {
        return meta.useBatchUpdate();
      }
    } );
    check( "SKIP_LOOKUP", new BooleanGetter() {
      public boolean get() {
        return meta.isSkipLookup();
      }
    } );
    check( "IGNORE_LOOKUP_FAILURE", new BooleanGetter() {
      public boolean get() {
        return meta.isErrorIgnored();
      }
    } );
    check( "FLAG_FIELD", new StringGetter() {
      public String get() {
        return meta.getIgnoreFlagField();
      }
    } );
    check( "KEY_STREAM", new StringGetter() {
      public String get() {
        return meta.getKeyStream()[0];
      }
    } );
    check( "KEY_LOOKUP", new StringGetter() {
      public String get() {
        return meta.getKeyLookup()[0];
      }
    } );
    check( "KEY_CONDITION", new StringGetter() {
      public String get() {
        return meta.getKeyCondition()[0];
      }
    } );
    check( "KEY_STREAM2", new StringGetter() {
      public String get() {
        return meta.getKeyStream2()[0];
      }
    } );
    check( "UPDATE_LOOKUP", new StringGetter() {
      public String get() {
        return meta.getUpdateLookup()[0];
      }
    } );
    check( "UPDATE_STREAM", new StringGetter() {
      public String get() {
        return meta.getUpdateStream()[0];
      }
    } );
    skipPropertyTest( "CONNECTIONNAME" );
  }
}
