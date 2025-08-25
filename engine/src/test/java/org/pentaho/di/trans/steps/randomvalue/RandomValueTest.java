/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/
package org.pentaho.di.trans.steps.randomvalue;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class RandomValueTest {

  private RandomValue randomValue;
  private StepMockHelper<RandomValueMeta, RandomValueData> mockHelper;

  @Before
  public void setUp() {
    mockHelper = new StepMockHelper<>( "RandomValueTest", RandomValueMeta.class, RandomValueData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      mockHelper.logChannelInterface );
    when( mockHelper.trans.isRunning() ).thenReturn( true );
    randomValue = Mockito.spy(
      new RandomValue( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans ) );
  }

  @After
  public void tearDown() {
    mockHelper.cleanUp();
  }

  @Test
  public void testGetRandomFunctionTypesAction_returnsAllFunctionTypes() {
    JSONObject result = randomValue.getRandomFunctionTypesAction( Collections.emptyMap() );
    assertNotNull( result );
    assertTrue( result.containsKey( "randomFunctionTypes" ) );

    JSONArray array = (JSONArray) result.get( "randomFunctionTypes" );
    assertNotNull( array );

    // Should match the number of non-null functions in RandomValueMeta.functions
    long expectedCount = Arrays.stream( RandomValueMeta.functions )
      .filter( Objects::nonNull )
      .count();
    assertEquals( expectedCount, array.size() );

    int arrIdx = 0;
    for ( RandomValueMetaFunction func : RandomValueMeta.functions ) {
      if ( func == null ) {
        continue;
      }
      JSONObject obj = (JSONObject) array.get( arrIdx++ );
      assertEquals( func.getCode(), obj.get( "id" ) );
      assertEquals( func.getDescription(), obj.get( "name" ) );
    }
  }
}
