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

package org.pentaho.di.trans.steps.mysqlbulkloader;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class MySQLBulkLoaderMetaInjectionTest extends BaseMetadataInjectionTest<MySQLBulkLoaderMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Before
  public void setup() {
    setup( new MySQLBulkLoaderMeta() );
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
    check( "FIFO_FILE", new StringGetter() {
      public String get() {
        return meta.getFifoFileName();
      }
    } );
    check( "ENCODING", new StringGetter() {
      public String get() {
        return meta.getEncoding();
      }
    } );
    check( "USE_REPLACE_CLAUSE", new BooleanGetter() {
      public boolean get() {
        return meta.isReplacingData();
      }
    } );
    check( "USE_IGNORE_CLAUSE", new BooleanGetter() {
      public boolean get() {
        return meta.isIgnoringErrors();
      }
    } );
    check( "LOCAL_FILE", new BooleanGetter() {
      public boolean get() {
        return meta.isLocalFile();
      }
    } );
    check( "DELIMITER", new StringGetter() {
      public String get() {
        return meta.getDelimiter();
      }
    } );
    check( "ENCLOSURE", new StringGetter() {
      public String get() {
        return meta.getEnclosure();
      }
    } );
    check( "ESCAPE_CHAR", new StringGetter() {
      public String get() {
        return meta.getEscapeChar();
      }
    } );
    check( "BULK_SIZE", new StringGetter() {
      public String get() {
        return meta.getBulkSize();
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

    ValueMetaInterface mftt = new ValueMetaString( "f" );
    injector.setProperty( meta, "FIELD_FORMAT", setValue( mftt, "OK" ), "f" );
    assertEquals( 0, meta.getFieldFormatType()[0] );
    injector.setProperty( meta, "FIELD_FORMAT", setValue( mftt, "DATE" ), "f" );
    assertEquals( 1, meta.getFieldFormatType()[0] );
    injector.setProperty( meta, "FIELD_FORMAT", setValue( mftt, "TIMESTAMP" ), "f" );
    assertEquals( 2, meta.getFieldFormatType()[0] );
    injector.setProperty( meta, "FIELD_FORMAT", setValue( mftt, "NUMBER" ), "f" );
    assertEquals( 3, meta.getFieldFormatType()[0] );
    injector.setProperty( meta, "FIELD_FORMAT", setValue( mftt, "STRING_ESC" ), "f" );
    assertEquals( 4, meta.getFieldFormatType()[0] );
    skipPropertyTest( "FIELD_FORMAT" );
  }
}
