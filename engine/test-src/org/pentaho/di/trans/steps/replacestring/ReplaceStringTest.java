/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.replacestring;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.mockito.Mockito;

/**
 * User: Dzmitry Stsiapanau Date: 1/31/14 Time: 11:19 AM
 */
public class ReplaceStringTest {
  private Object[] row = new Object[] { "some data", "another data" };

  private Object[] expectedRow = new Object[] { "some data", "1nother d1t1", "1no2her d121", null, null, null, null,
    null, null, null, null, null, null };

  private StepMockHelper<ReplaceStringMeta, ReplaceStringData> stepMockHelper;

  @Before
  public void setUp() throws Exception {
    stepMockHelper =
        new StepMockHelper<ReplaceStringMeta, ReplaceStringData>( "REPLACE STRING TEST", ReplaceStringMeta.class,
            ReplaceStringData.class );
    Mockito.when( stepMockHelper.logChannelInterfaceFactory.create( Mockito.any(), Mockito.any( LoggingObjectInterface.class ) ) ).thenReturn(
        stepMockHelper.logChannelInterface );
    Mockito.verify( stepMockHelper.logChannelInterface, Mockito.never() ).logError( Mockito.anyString() );
    Mockito.verify( stepMockHelper.logChannelInterface, Mockito.never() ).logError( Mockito.anyString(), Mockito.any( Object[].class ) );
    Mockito.verify( stepMockHelper.logChannelInterface, Mockito.never() ).logError( Mockito.anyString(), (Throwable) Mockito.anyObject() );
    Mockito.when( stepMockHelper.trans.isRunning() ).thenReturn( true );
  }

  @After
  public void tearDown() throws Exception {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testGetOneRow() throws Exception {
    ReplaceStringData data = new ReplaceStringData();

    ReplaceString replaceString =
        new ReplaceString( stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta, stepMockHelper.trans );
    RowMetaInterface inputRowMeta = new RowMeta();
    inputRowMeta.addValueMeta( 0, new ValueMetaString( "SomeDataMeta" ) );
    inputRowMeta.addValueMeta( 1, new ValueMetaString( "AnotherDataMeta" ) );
    replaceString.init( stepMockHelper.processRowsStepMetaInterface, data );
    replaceString.setInputRowMeta( inputRowMeta );
    data.outputRowMeta = inputRowMeta;
    data.outputRowMeta.addValueMeta( new ValueMetaString( "AnotherDataMeta" ) );
    data.inputFieldsNr = 2;
    data.numFields = 2;
    data.inStreamNrs = new int[] { 1, 1 };
    data.patterns = new Pattern[] { Pattern.compile( "a" ), Pattern.compile( "t" ) };
    data.replaceFieldIndex = new int[] { -1, -1 };
    data.outStreamNrs = new String[] { "", "1" };
    data.replaceByString = new String[] { "1", "2" };
    data.setEmptyString = new boolean[] { false, false };
    // when( inputRowMeta.size() ).thenReturn( 3 );
    // when( inputRowMeta.getString( anyObject(), 1 ) ).thenReturn((String) row[1]);

    Object[] output = replaceString.getOneRow( inputRowMeta, row );
    Assert.assertArrayEquals( "Output varies", expectedRow, output );
  }
}
