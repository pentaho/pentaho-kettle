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

package org.pentaho.di.trans.steps.googleanalytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.model.GaData;

/**
 * @author Andrey Khayrutdinov
 */
public class GaInputStepTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void getNextDataEntry_WithPaging() throws Exception {
    final int recordsCount = 30;

    final String stepName = "GaInputStepTest";

    StepMeta stepMeta = new StepMeta( stepName, stepName, new GaInputStepMeta() );

    Trans trans = mock( Trans.class );

    TransMeta transMeta = mock( TransMeta.class );
    when( transMeta.findStep( stepName ) ).thenReturn( stepMeta );

    GaInputStepData data = new GaInputStepData();

    GaInputStep step = new GaInputStep( stepMeta, data, 0, transMeta, trans );

    FieldUtils.writeField( FieldUtils.getField( GaInputStep.class, "data", true ), step, data, true );

    Analytics.Data.Ga.Get mockQuery = prepareMockQuery( recordsCount );
    step = spy( step );
    doReturn( mockQuery ).when( step ).getQuery( any() );

    for ( int i = 0; i < recordsCount; i++ ) {
      List<String> next = step.getNextDataEntry();
      assertEquals( Integer.toString( i + 1 ), next.get( 0 ) );
    }
    assertNull( step.getNextDataEntry() );
  }

  private Analytics.Data.Ga.Get prepareMockQuery( int recordsCount ) throws Exception {
    final MockQueryAssistant assistant = new MockQueryAssistant( recordsCount );
    assistant.setLimit( 10 );

    Analytics.Data.Ga.Get get = mock( Analytics.Data.Ga.Get.class );
    when( get.setStartIndex( anyInt() ) ).thenAnswer( new Answer<Object>() {
      @Override
      public Object answer( InvocationOnMock invocation ) throws Throwable {
        assistant.setStartIndex( (Integer) invocation.getArguments()[ 0 ] );
        return null;
      }
    } );
    when( get.getStartIndex() ).thenAnswer( new Answer<Integer>() {
      @Override
      public Integer answer( InvocationOnMock invocation ) throws Throwable {
        return assistant.getStartIndex();
      }
    } );
    when( get.execute() ).thenAnswer( new Answer<GaData>() {
      @Override
      public GaData answer( InvocationOnMock invocation ) throws Throwable {
        return assistant.execute();
      }
    } );

    return get;
  }


  private static class MockQueryAssistant {
    private final int recordsCount;
    private Integer startIndex;
    private Integer limit;

    public MockQueryAssistant( int recordsCount ) {
      this.recordsCount = recordsCount;
    }

    public void setStartIndex( Integer startIndex ) {
      this.startIndex = startIndex;
    }

    public Integer getStartIndex() {
      return startIndex;
    }

    public void setLimit( Integer limit ) {
      this.limit = limit;
    }

    public GaData execute() {
      GaData result = new GaData();
      result.setTotalResults( recordsCount );
      result.setItemsPerPage( limit );

      List<List<String>> rows = new ArrayList<List<String>>();
      int start = ( startIndex == null ) ? 1 : startIndex;
      int end = Math.min( start + ( limit == null ? 1000 : limit ), recordsCount + 1 );
      while ( start < end ) {
        rows.add( Collections.singletonList( Integer.toString( start ) ) );
        start++;
      }

      result.setRows( rows );
      return result;
    }
  }
}
