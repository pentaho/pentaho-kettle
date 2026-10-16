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


package org.pentaho.di.trans.steps.execsqlrow;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExecSQLRowMetaInjectionTest extends BaseMetadataInjectionTest<ExecSQLRowMeta> {

  @Before
  public void setup() {
    setup( new ExecSQLRowMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "SQL_FIELD_NAME", new StringGetter() {
      @Override
      public String get() {
        return meta.getSqlFieldName();
      }
    } );
    check( "COMMIT_SIZE", new IntGetter() {
      @Override
      public int get() {
        return meta.getCommitSize();
      }
    } );
    check( "READ_SQL_FROM_FILE", new BooleanGetter() {
      @Override
      public boolean get() {
        return meta.isSqlFromfile();
      }
    } );
    check( "SEND_SINGLE_STATEMENT", new BooleanGetter() {
      @Override
      public boolean get() {
        return meta.IsSendOneStatement();
      }
    } );
    check( "UPDATE_STATS", new StringGetter() {
      @Override
      public String get() {
        return meta.getUpdateField();
      }
    } );
    check( "INSERT_STATS", new StringGetter() {
      @Override
      public String get() {
        return meta.getInsertField();
      }
    } );
    check( "DELETE_STATS", new StringGetter() {
      @Override
      public String get() {
        return meta.getDeleteField();
      }
    } );
    check( "READ_STATS", new StringGetter() {
      @Override
      public String get() {
        return meta.getReadField();
      }
    } );

    skipPropertyTest( "CONNECTION_NAME" );

    List<DatabaseMeta> databasesList = new ArrayList<>();
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    when( dbMeta.getName() ).thenReturn( "testDBMeta" );
    databasesList.add( dbMeta );
    meta.setDatabasesList( databasesList );
    ValueMetaInterface valueMeta = new ValueMetaString( "DBMETA" );
    injector.setProperty( meta, "CONNECTION_NAME", setValue( valueMeta, "testDBMeta" ), "DBMETA" );
    assertEquals( "testDBMeta", meta.getDatabaseMeta().getName() );
  }
}
