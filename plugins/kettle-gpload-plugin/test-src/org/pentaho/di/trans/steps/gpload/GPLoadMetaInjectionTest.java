/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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
import org.junit.Test;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class GPLoadMetaInjectionTest extends BaseMetadataInjectionTest<GPLoadMeta> {
  @Before
  public void setup() {
    setup( new GPLoadMeta() );
  }

  @Test
  public void test() throws Exception {

    check( "NULL_AS", new StringGetter() {
      public String get() {
        return meta.getNullAs();
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

    check( "LOCAL_HOST_PORT", new StringGetter() {
      public String get() {
        return meta.getLocalhostPort();
      }
    } );

    check( "ERROR_TABLE_NAME", new StringGetter() {
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

    check( "ERASE_FILES", new BooleanGetter() {
      public boolean get() {
        return meta.isEraseFiles();
      }
    } );

    check( "DELIMITER", new StringGetter() {
      public String get() {
        return meta.getDelimiter();
      }
    } );

    check( "UPDATE_CONDITION", new StringGetter() {
      public String get() {
        return meta.getUpdateCondition();
      }
    } );

    checkStringArray( new MetaValueGetter<String[]>() {
      public String[] get() {
        return meta.getFieldTable();
      }
    }, "FIELD_TABLE", "ft1", "ft2", "ft3" );

    checkStringArray( new MetaValueGetter<String[]>() {
      public String[] get() {
        return meta.getFieldStream();
      }
    }, "FIELD_STREAM", "fs1", "fs2", "fs3" );

    checkStringArray( new MetaValueGetter<String[]>() {
      public String[] get() {
        return meta.getDateMask();
      }
    }, "DATE_MASK", "dm1", "dm2", "dm3" );

    checkBooleanArray( new MetaValueGetter<boolean[]>() {
      public boolean[] get() {
        return meta.getMatchColumn();
      }
    }, "MATCH_COLUMN", true, false, true );

    checkBooleanArray( new MetaValueGetter<boolean[]>() {
      public boolean[] get() {
        return meta.getUpdateColumn();
      }
    }, "UPDATE_COLUMN", false, true, false );

    checkStringArray( new MetaValueGetter<String[]>() {
      public String[] get() {
        return meta.getLocalHosts();
      }
    }, "LOCAL_HOSTS", "lh1", "lh2", "lh3" );

  }

  interface MetaValueGetter<T> {
    T get();
  }

  private void checkStringArray( MetaValueGetter<String[]> metaValueGetter, String propertyName, String... testValues ) throws KettleException {
    ValueMetaInterface valueMetaInterface = new ValueMetaString( "f" );
    injector.setProperty( meta, propertyName, setValue( valueMetaInterface, (Object[]) testValues ), "f" );
    assertArrayEquals( testValues, metaValueGetter.get() );
    skipPropertyTest( propertyName );
  }

  private void checkBooleanArray( MetaValueGetter<boolean[]> metaValueGetter, String propertyName, boolean... testValues ) throws KettleException {
    ValueMetaInterface valueMetaInterface = new ValueMetaString( "f" );
    injector.setProperty( meta, propertyName, setValue( valueMetaInterface, testValues ), "f" );
    boolean[] apply = metaValueGetter.get();
    assertTrue( Arrays.equals( testValues, apply ) );
    skipPropertyTest( propertyName );
  }

  /*
  This overload is required as the base class (BaseMetadataInjectionTest) implementation of setValue uses varargs for the last argument, which doesn't play nicely with a boolean[]
   */
  protected List<RowMetaAndData> setValue( ValueMetaInterface valueMeta, boolean[] values ) {
    RowMeta rowsMeta = new RowMeta();
    rowsMeta.addValueMeta( valueMeta );
    List<RowMetaAndData> rows = new ArrayList<>();
    for ( Object v : values ) {
      rows.add( new RowMetaAndData( rowsMeta, v ) );
    }
    return rows;
  }

}
