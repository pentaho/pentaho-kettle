/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.combinationlookup;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class CombinationLookupMetaInjectionTest extends BaseMetadataInjectionTest<CombinationLookupMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new CombinationLookupMeta() );
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
    check( "REPLACE_FIELDS", new BooleanGetter() {
      public boolean get() {
        return meta.replaceFields();
      }
    } );
    check( "KEY_FIELDS", new StringGetter() {
      public String get() {
        return meta.getKeyField()[ 0 ];
      }
    } );
    check( "KEY_LOOKUP", new StringGetter() {
      public String get() {
        return meta.getKeyLookup()[ 0 ];
      }
    } );
    check( "USE_HASH", new BooleanGetter() {
      public boolean get() {
        return meta.useHash();
      }
    } );
    check( "HASH_FIELD", new StringGetter() {
      public String get() {
        return meta.getHashField();
      }
    } );
    check( "TECHNICAL_KEY_FIELD", new StringGetter() {
      public String get() {
        return meta.getTechnicalKeyField();
      }
    } );
    check( "SEQUENCE_FROM", new StringGetter() {
      public String get() {
        return meta.getSequenceFrom();
      }
    } );
    check( "COMMIT_SIZE", new IntGetter() {
      public int get() {
        return meta.getCommitSize();
      }
    } );
    check( "PRELOAD_CACHE", new BooleanGetter() {
      public boolean get() {
        return meta.getPreloadCache();
      }
    } );
    check( "CACHE_SIZE", new IntGetter() {
      public int get() {
        return meta.getCacheSize();
      }
    } );
    check( "AUTO_INC", new BooleanGetter() {
      public boolean get() {
        return meta.isUseAutoinc();
      }
    } );
    check( "TECHNICAL_KEY_CREATION", new StringGetter() {
      public String get() {
        return meta.getTechKeyCreation();
      }
    } );
    check( "LAST_UPDATE_FIELD", new StringGetter() {
      public String get() {
        return meta.getLastUpdateField();
      }
    } );
    skipPropertyTest( "CONNECTIONNAME" );
  }
}
