/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.combinationlookup;

import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;

import static org.junit.Assert.*;

public class CombinationLookupMetaTest {
  @Test
  public void testProvidesModelerMeta() throws Exception {

    final RowMeta rowMeta = Mockito.mock( RowMeta.class );
    final CombinationLookupMeta combinationLookupMeta = new CombinationLookupMeta() {
      @Override Database createDatabaseObject() {
        return Mockito.mock( Database.class );
      }

      @Override protected RowMetaInterface getDatabaseTableFields( Database db, String schemaName, String tableName )
        throws KettleDatabaseException {
        assertEquals( "aSchema", schemaName );
        assertEquals( "aDimTable", tableName );
        return rowMeta;
      }
    };
    combinationLookupMeta.setKeyLookup( new String[] { "f1", "f2", "f3" } );
    combinationLookupMeta.setKeyField( new String[] { "s4", "s5", "s6" } );
    combinationLookupMeta.setSchemaName( "aSchema" );
    combinationLookupMeta.setTablename( "aDimTable" );

    final CombinationLookupData dimensionLookupData = new CombinationLookupData();
    assertEquals( rowMeta, combinationLookupMeta.getRowMeta( dimensionLookupData ) );
    assertEquals( 3, combinationLookupMeta.getDatabaseFields().size() );
    assertEquals( "f1", combinationLookupMeta.getDatabaseFields().get( 0 ) );
    assertEquals( "f2", combinationLookupMeta.getDatabaseFields().get( 1 ) );
    assertEquals( "f3", combinationLookupMeta.getDatabaseFields().get( 2 ) );
    assertEquals( 3, combinationLookupMeta.getStreamFields().size() );
    assertEquals( "s4", combinationLookupMeta.getStreamFields().get( 0 ) );
    assertEquals( "s5", combinationLookupMeta.getStreamFields().get( 1 ) );
    assertEquals( "s6", combinationLookupMeta.getStreamFields().get( 2 ) );
  }
}
