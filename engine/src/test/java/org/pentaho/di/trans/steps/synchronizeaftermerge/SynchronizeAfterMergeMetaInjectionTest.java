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

package org.pentaho.di.trans.steps.synchronizeaftermerge;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class SynchronizeAfterMergeMetaInjectionTest extends BaseMetadataInjectionTest<SynchronizeAfterMergeMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    super.setup( new SynchronizeAfterMergeMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "SHEMA_NAME", new StringGetter() {
      @Override
      public String get() {
        return meta.getSchemaName();
      }
    } );
    check( "TABLE_NAME", new StringGetter() {
      @Override
      public String get() {
        return meta.getTableName();
      }
    } );
    check( "TABLE_FIELD", new StringGetter() {
      @Override
      public String get() {
        return meta.getKeyLookup()[0];
      }
    } );
    check( "STREAM_FIELD1", new StringGetter() {
      @Override
      public String get() {
        return meta.getKeyStream()[0];
      }
    } );
    check( "STREAM_FIELD2", new StringGetter() {
      @Override
      public String get() {
        return meta.getKeyStream2()[0];
      }
    } );
    check( "COMPARATOR", new StringGetter() {
      @Override
      public String get() {
        return meta.getKeyCondition()[0];
      }
    } );

    check( "UPDATE_TABLE_FIELD", new StringGetter() {
      @Override
      public String get() {
        return meta.getUpdateLookup()[0];
      }
    } );
    check( "STREAM_FIELD", new StringGetter() {
      @Override
      public String get() {
        return meta.getUpdateStream()[0];
      }
    } );
    check( "UPDATE", new BooleanGetter() {
      @Override
      public boolean get() {
        return meta.getUpdate()[0];
      }
    } );

    check( "COMMIT_SIZE", new StringGetter() {
      @Override
      public String get() {
        return meta.getCommitSize();
      }
    } );
    check( "TABLE_NAME_IN_FIELD", new BooleanGetter() {
      @Override
      public boolean get() {
        return meta.istablenameInField();
      }
    } );
    check( "TABLE_NAME_FIELD", new StringGetter() {
      @Override
      public String get() {
        return meta.gettablenameField();
      }
    } );
    check( "OPERATION_ORDER_FIELD", new StringGetter() {
      @Override
      public String get() {
        return meta.getOperationOrderField();
      }
    } );
    check( "USE_BATCH_UPDATE", new BooleanGetter() {
      @Override
      public boolean get() {
        return meta.useBatchUpdate();
      }
    } );
    check( "PERFORM_LOOKUP", new BooleanGetter() {
      @Override
      public boolean get() {
        return meta.isPerformLookup();
      }
    } );
    check( "ORDER_INSERT", new StringGetter() {
      @Override
      public String get() {
        return meta.getOrderInsert();
      }
    } );
    check( "ORDER_UPDATE", new StringGetter() {
      @Override
      public String get() {
        return meta.getOrderUpdate();
      }
    } );
    check( "ORDER_DELETE", new StringGetter() {
      @Override
      public String get() {
        return meta.getOrderDelete();
      }
    } );
    check( "CONNECTION_NAME", new StringGetter() {
      public String get() {
        return "My Connection";
      }
    }, "My Connection" );
  }

  @Test
  public void getXML() throws KettleException {
    skipProperties( "CONNECTION_NAME", "TABLE_NAME", "STREAM_FIELD2", "PERFORM_LOOKUP", "COMPARATOR",
        "OPERATION_ORDER_FIELD", "ORDER_DELETE", "SHEMA_NAME", "TABLE_NAME_IN_FIELD", "ORDER_UPDATE", "ORDER_INSERT",
        "USE_BATCH_UPDATE", "STREAM_FIELD", "TABLE_FIELD", "COMMIT_SIZE", "TABLE_NAME_FIELD" );
    meta.setDefault();
    check( "STREAM_FIELD1", new StringGetter() {
      @Override
      public String get() {
        return meta.getKeyStream()[0];
      }
    } );
    check( "UPDATE_TABLE_FIELD", new StringGetter() {
      @Override
      public String get() {
        return meta.getUpdateLookup()[0];
      }
    } );
    check( "UPDATE", new BooleanGetter() {
      @Override
      public boolean get() {
        return meta.getUpdate()[0];
      }
    } );

    meta.getXML();

    String[] actualKeyLookup = meta.getKeyLookup();
    assertNotNull( actualKeyLookup );
    assertEquals( 1, actualKeyLookup.length );

    String[] actualKeyCondition = meta.getKeyCondition();
    assertNotNull( actualKeyCondition );
    assertEquals( 1, actualKeyCondition.length );

    String[] actualKeyStream2 = meta.getKeyCondition();
    assertNotNull( actualKeyStream2 );
    assertEquals( 1, actualKeyStream2.length );

    String[] actualUpdateStream = meta.getUpdateStream();
    assertNotNull( actualUpdateStream );
    assertEquals( 1, actualUpdateStream.length );
  }

  private void skipProperties( String... propertyName ) {
    for ( String property : propertyName ) {
      skipPropertyTest( property );
    }
  }

}
