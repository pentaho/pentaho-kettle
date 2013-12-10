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

package org.pentaho.di.trans.steps.fuzzymatch;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

/**
 * User: Dzmitry Stsiapanau Date: 10/16/13 Time: 6:23 PM
 */
public class FuzzyMatchTest {
  @InjectMocks
  private FuzzyMatchHandler fuzzyMatch;
  private StepMockHelper<FuzzyMatchMeta, FuzzyMatchData> mockHelper;

  private Object[] row = new Object[] { "Catrine" };
  private Object[] row2 = new Object[] { "John" };
  private Object[] row3 = new Object[] { "Catriny" };
  private List<Object[]> rows = new ArrayList<Object[]>();
  private List<Object[]> lookupRows = new ArrayList<Object[]>();
  {
    rows.add( row );
    lookupRows.add( row2 );
    lookupRows.add( row3 );
  }

  private class FuzzyMatchHandler extends FuzzyMatch {
    private Object[] resultRow = null;

    public FuzzyMatchHandler( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
        Trans trans ) {
      super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
    }

    @Override
    public void putRow( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
      resultRow = row;
    }
  }

  @Before
  public void setUp() throws Exception {
    mockHelper =
        new StepMockHelper<FuzzyMatchMeta, FuzzyMatchData>( "Fuzzy Match", FuzzyMatchMeta.class, FuzzyMatchData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        mockHelper.logChannelInterface );
    when( mockHelper.trans.isRunning() ).thenReturn( true );
  }

  @After
  public void tearDown() throws Exception {
    mockHelper.cleanUp();
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testProcessRow() throws Exception {
    fuzzyMatch =
        new FuzzyMatchHandler(
            mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans );
    fuzzyMatch.init( mockHelper.initStepMetaInterface, mockHelper.initStepDataInterface );
    fuzzyMatch.getInputRowSets().add( mockHelper.getMockInputRowSet( rows ) );
    fuzzyMatch.getInputRowSets().add( mockHelper.getMockInputRowSet( lookupRows ) );

    when( mockHelper.processRowsStepMetaInterface.getAlgorithmType() ).thenReturn( 8 );
    mockHelper.processRowsStepDataInterface.look = mock( HashSet.class );
    when( mockHelper.processRowsStepDataInterface.look.iterator() ).thenReturn( lookupRows.iterator() );

    fuzzyMatch.processRow( mockHelper.processRowsStepMetaInterface, mockHelper.processRowsStepDataInterface );
    Assert.assertEquals( fuzzyMatch.resultRow[0], row3[0] );
  }
}
