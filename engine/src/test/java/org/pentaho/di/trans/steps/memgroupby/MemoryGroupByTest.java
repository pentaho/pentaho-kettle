/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.memgroupby;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.reflect.Whitebox.setInternalState;

@RunWith( PowerMockRunner.class )
@PrepareForTest( RowDataUtil.class )
public class MemoryGroupByTest {

  @Before
  public void before() {
    mockStatic( RowDataUtil.class );
  }

  @Test
  public void handleLastOfGroup_PDI18198() throws KettleException {
    HashMap<MemoryGroupByData.HashEntry, Aggregate> map = new HashMap<>();

    Object[] o1 = new Object[] { "k1".getBytes() };
    Object[] o2 = new Object[] { "k2".getBytes() };
    Object[] o3 = new Object[] { "k3".getBytes() };

    MemoryGroupByData data = mock( MemoryGroupByData.class );

    doReturn( mock( MemoryGroupByData.HashEntry.class ) ).when( data ).getHashEntry( o1 );
    doReturn( mock( MemoryGroupByData.HashEntry.class ) ).when( data ).getHashEntry( o2 );
    doReturn( mock( MemoryGroupByData.HashEntry.class ) ).when( data ).getHashEntry( o3 );

    map.put( data.getHashEntry( o1 ), new Aggregate() );
    map.put( data.getHashEntry( o2 ), new Aggregate() );
    map.put( data.getHashEntry( o3 ), new Aggregate() );

    RowMetaInterface meta = mock( RowMetaInterface.class );
    doReturn( 0 ).when( meta ).size();

    RowMetaInterface agg = mock( RowMetaInterface.class );
    doReturn( 0 ).when( agg ).size();

    Object[] output = new Object[ 0 ];
    when( RowDataUtil.allocateRowData( 0 ) ).thenReturn( output );

    MemoryGroupBy groupby = mock( MemoryGroupBy.class );
    doCallRealMethod().when( groupby ).handleLastOfGroup();

    setInternalState( data, "map", map );
    setInternalState( data, "aggMeta", agg );
    setInternalState( data, "groupMeta", meta );
    setInternalState( data, "outputRowMeta", meta );
    setInternalState( groupby, "data", data );

    groupby.handleLastOfGroup();
    verify( groupby, times( 3 ) ).putRow( agg, output );
  }
}
