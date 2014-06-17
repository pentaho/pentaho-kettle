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
