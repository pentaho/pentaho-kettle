/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.databaselookup;

import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.TimedRow;
import org.pentaho.di.core.row.RowMeta;
import java.util.LinkedHashMap;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;


public class DefaultCacheTest {

  @Test
  public void storeRowInCacheTest() {

    DatabaseLookupData databaseLookupData = mock( DatabaseLookupData.class );
    DatabaseLookupMeta databaseLookupMeta = mock( DatabaseLookupMeta.class );
    DefaultCache defaultCache = new DefaultCache( databaseLookupData, 10 );
    RowMeta rowMeta = mock( RowMeta.class );
    //Several inserts with the same key and different values
    defaultCache.storeRowInCache( databaseLookupMeta, rowMeta, new Object[]{1}, new Object[]{ 100 } );
    defaultCache.storeRowInCache( databaseLookupMeta, rowMeta, new Object[]{1}, new Object[]{ 200 } );
    defaultCache.storeRowInCache( databaseLookupMeta, rowMeta, new Object[]{1}, new Object[]{ 300 } );
    LinkedHashMap<RowMetaAndData, TimedRow> map = (LinkedHashMap<RowMetaAndData, TimedRow>) Whitebox.getInternalState( defaultCache, "map" );
    //Only one entry expected
    assertEquals( 1, map.size() );
    //The value expected is the first one inserted
    assertEquals( 100, map.get( new RowMetaAndData( rowMeta, new Object[]{1} ) ).getRow()[0] );
  }
}
