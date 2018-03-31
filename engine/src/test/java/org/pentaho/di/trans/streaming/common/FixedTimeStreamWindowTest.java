/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.streaming.common;

import io.reactivex.Observable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.SubtransExecutor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class FixedTimeStreamWindowTest {
  @Mock SubtransExecutor subtransExecutor;

  @Test
  public void emptyResultShouldNotThrowException() throws KettleException {
    when( subtransExecutor.execute( any()  ) ).thenReturn( Optional.empty() );
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "field" ) );
    FixedTimeStreamWindow<List> window =
      new FixedTimeStreamWindow<>( subtransExecutor, rowMeta, 0, 2 );
    window.buffer( Observable.fromIterable( singletonList( asList( "v1", "v2" ) ) ) ).forEach( result -> { } );
  }

  @Test
  public void resultsComeBackToParent() throws KettleException {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "field" ) );
    Result mockResult = new Result();
    mockResult.setRows( Arrays.asList( new RowMetaAndData( rowMeta, "queen" ), new RowMetaAndData( rowMeta, "king" ) ) );
    when( subtransExecutor.execute( any()  ) ).thenReturn( Optional.of( mockResult ) );
    FixedTimeStreamWindow<List> window =
      new FixedTimeStreamWindow<>( subtransExecutor, rowMeta, 0, 2 );
    window.buffer( Observable.fromIterable( singletonList( asList( "v1", "v2" ) ) ) )
      .forEach( result -> assertEquals( mockResult, result ) );
  }
}
