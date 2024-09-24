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

package org.pentaho.di.trans.steps.rules;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

public class RulesExecutorDataTest {

  private RulesExecutorData data;

  @Before
  public void beforeTest() {
    // init
    data = spy( new RulesExecutorData() );
    ValueMetaInterface c1 = mock( ValueMetaInterface.class );
    when( c1.getName() ).thenReturn( "c1" );
    ValueMetaInterface c2 = mock( ValueMetaInterface.class );
    when( c2.getName() ).thenReturn( "c2" );
    List<ValueMetaInterface> initColumns = Arrays.asList( c1, c2 );

    Rules.Column fc1 = new Rules.Column();
    fc1.setName( "c1" );
    List<Rules.Column> fetchedColumns = new ArrayList<Rules.Column>( Arrays.asList( fc1 ) );
    List<Rules.Column> fetchedColumnsSpy = spy( fetchedColumns );

    StatefulKnowledgeSession session = mock( StatefulKnowledgeSession.class );
    doReturn( session ).when( data ).initNewKnowledgeSession();
    doReturn( fetchedColumnsSpy ).when( data ).fetchColumns( session );

    RowMetaInterface rowMeta = mock( RowMetaInterface.class );
    when( rowMeta.getValueMetaList() ).thenReturn( initColumns );
    data.initializeColumns( rowMeta );
  }

  @Test
  public void testLoadRow() throws Exception {

    // test
    data.loadRow( new Object[] { "1", "2" } );
    data.execute();
    data.loadRow( new Object[] { "3", "4" } );

    // verify
    assertEquals( null, data.fetchResult( "c1" ) );
    assertEquals( null, data.fetchResult( "c2" ) );
  }
}
