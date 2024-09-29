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


package org.pentaho.di.trans.steps.memgroupby;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Created by bmorrise on 2/11/16.
 */
@RunWith( MockitoJUnitRunner.class )
public class MemoryGroupByDataTest {
  private final MemoryGroupByData data = new MemoryGroupByData();

  @Mock private RowMetaInterface groupMeta;
  @Mock private ValueMetaInterface valueMeta;

  @Before public void setUp() throws Exception {
    data.groupMeta = groupMeta;
    when( groupMeta.size() ).thenReturn( 1 );
    when( groupMeta.getValueMeta( anyInt() ) ).thenReturn( valueMeta );
    when( valueMeta.convertToNormalStorageType( any() ) ).then( invocation -> {
      Object argument = invocation.getArguments()[0];
      return new String( (byte[]) argument );
    } );
  }

  @Test public void hashEntryTest() {
    HashMap<MemoryGroupByData.HashEntry, String> map = new HashMap<>();

    byte[] byteValue1 = "key".getBytes();
    Object[] groupData1 = new Object[1];
    groupData1[0] = byteValue1;

    MemoryGroupByData.HashEntry hashEntry1 = data.getHashEntry( groupData1 );
    map.put( hashEntry1, "value" );

    byte[] byteValue2 = "key".getBytes();
    Object[] groupData2 = new Object[1];
    groupData2[0] = byteValue2;

    MemoryGroupByData.HashEntry hashEntry2 = data.getHashEntry( groupData2 );

    String value = map.get( hashEntry2 );

    assertEquals( "value", value );
  }

}
