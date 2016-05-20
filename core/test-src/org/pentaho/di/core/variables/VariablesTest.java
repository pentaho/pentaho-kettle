/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.variables;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Variables tests.
 *
 * @author Yury Bakhmutski
 * @see Variables
 */
public class VariablesTest {

  private Variables variables = new Variables();

  /**
   * Test for PDI-12893 issue.  Checks if an ConcurrentModificationException while iterating over the System properties
   * is occurred.
   */
  @Test
  public void testinItializeVariablesFrom() {
    final Variables variablesMock = mock( Variables.class );
    doCallRealMethod().when( variablesMock ).initializeVariablesFrom( any( VariableSpace.class ) );

    @SuppressWarnings( "unchecked" )
    final Map<String, String> propertiesMock = mock( Map.class );
    when( variablesMock.getProperties() ).thenReturn( propertiesMock );

    doAnswer( new Answer<Map<String, String>>() {
      final String keyStub = "key";

      @Override
      public Map<String, String> answer( InvocationOnMock invocation ) throws Throwable {
        if ( System.getProperty( keyStub ) == null ) {
          modifySystemproperties();
        }

        if ( invocation.getArguments()[ 1 ] != null ) {
          propertiesMock.put( (String) invocation.getArguments()[ 0 ], System.getProperties().getProperty(
            (String) invocation.getArguments()[ 1 ] ) );
        }
        return propertiesMock;
      }
    } ).when( propertiesMock ).put( anyString(), anyString() );

    variablesMock.initializeVariablesFrom( null );
  }

  private void modifySystemproperties() {
    final String keyStub = "key";
    final String valueStub = "value";

    Thread thread = new Thread( new Runnable() {
      @Override
      public void run() {
        System.setProperty( keyStub, valueStub );
      }
    } );
    thread.start();
  }

  /**
   * Spawns 20 threads that modify variables to test concurrent modification error fix.
   *
   * @throws Exception
   */
  @Test
  public void testConcurrentModification() throws Exception {

    int threads = 20;
    List<Callable<Boolean>> callables = new ArrayList<Callable<Boolean>>();
    for ( int i = 0; i < threads; i++ ) {
      callables.add( newCallable() );
    }

    // Assert threads ran successfully.
    for ( Future<Boolean> result : Executors.newFixedThreadPool( 5 ).invokeAll( callables ) ) {
      assertTrue( result.get() );
    }
  }

  // Note:  Not using lambda so this can be ported to older version compatible with 1.7
  private Callable newCallable() {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        for ( int i = 0; i < 300; i++ ) {
          String key = "key" + i;
          variables.setVariable( key, "value" );
          assertEquals( variables.environmentSubstitute( "${" + key + "}" ), "value" );
        }
        return true;
      }
    };
  }

}
