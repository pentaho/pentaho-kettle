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
package org.pentaho.di.trans.steps.mysqlbulkloader;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;

public class MySQLBulkLoaderMetaInjectionTest extends BaseMetadataInjectionTest<MySQLBulkLoaderMeta> {
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
