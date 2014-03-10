/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.tableinput;

import org.junit.Test;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: Dzmitry Stsiapanau Date: 2/4/14 Time: 5:47 PM
 */
public class TableInputMetaTest {

  public class TableInputMetaHandler extends TableInputMeta {
    public Database database = mock( Database.class );

    @Override
    protected Database getDatabase() {
      return database;
    }
  }

  @Test
  public void testGetFields() throws Exception {
    TableInputMetaHandler meta = new TableInputMetaHandler();
    meta.setLazyConversionActive( true );
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    meta.setDatabaseMeta( dbMeta );
    Database mockDB = meta.getDatabase();
    when( mockDB.getQueryFields( anyString(), anyBoolean() ) ).thenReturn( createMockFields() );

    RowMetaInterface expectedRowMeta = new RowMeta();
    ValueMetaInterface valueMeta = new ValueMeta( "field1", ValueMeta.TYPE_STRING );
    valueMeta.setStorageMetadata( new ValueMeta( "field1", ValueMeta.TYPE_STRING ) );
    valueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
    expectedRowMeta.addValueMeta( valueMeta );

    VariableSpace space = mock( VariableSpace.class );
    RowMetaInterface rowMetaInterface = new RowMeta();
    meta.getFields( rowMetaInterface, "TABLE_INPUT_META", null, null, space, null, null );

    assertEquals( expectedRowMeta.toString(), rowMetaInterface.toString() );
  }

  private RowMetaInterface createMockFields() {
    RowMetaInterface rowMetaInterface = new RowMeta();
    ValueMetaInterface valueMeta = new ValueMeta( "field1", ValueMeta.TYPE_STRING );
    rowMetaInterface.addValueMeta( valueMeta );
    return rowMetaInterface;
  }
}
