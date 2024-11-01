/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.HashMap;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
  public void testInitializeVariablesFrom() {
    final Variables variablesMock = mock( Variables.class );
    doCallRealMethod().when( variablesMock ).initializeVariablesFrom( eq( null ) );

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
    int expectedNumOfProperties = System.getProperties().size() - 1;
    verify( variablesMock, times( expectedNumOfProperties ) ).getProperties();
    verify( propertiesMock, times( expectedNumOfProperties ) ).put( anyString(), anyString() );
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
  private Callable<Boolean> newCallable() {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        for ( int i = 0; i < 300; i++ ) {
          String key = "key" + i;
          variables.setVariable( key, "value" );
          assertEquals( "value", variables.environmentSubstitute( "${" + key + "}" ) );
        }
        return true;
      }
    };
  }

  @Test
  public void testFieldSubstitution() throws KettleValueException {
    Object[] rowData = new Object[]{ "DataOne", "DataTwo" };
    RowMeta rm = new RowMeta();
    rm.addValueMeta( new ValueMetaString( "FieldOne" ) );
    rm.addValueMeta( new ValueMetaString( "FieldTwo" ) );

    Variables vars = new Variables();
    assertNull( vars.fieldSubstitute( null, rm, rowData ) );
    assertEquals( "", vars.fieldSubstitute( "", rm, rowData ) );
    assertEquals( "DataOne", vars.fieldSubstitute( "?{FieldOne}", rm, rowData ) );
    assertEquals( "TheDataOne", vars.fieldSubstitute( "The?{FieldOne}", rm, rowData ) );
  }

  @Test
  public void testEnvironmentSubstitute() {
    Variables vars = new Variables();
    vars.setVariable( "VarOne", "DataOne" );
    vars.setVariable( "VarTwo", "DataTwo" );

    assertNull( vars.environmentSubstitute( (String) null ) );
    assertEquals( "", vars.environmentSubstitute( "" ) );
    assertEquals( "DataTwo", vars.environmentSubstitute( "${VarTwo}" ) );
    assertEquals( "DataTwoEnd", vars.environmentSubstitute( "${VarTwo}End" ) );

    assertEquals( 0, vars.environmentSubstitute( new String[0] ).length );
    assertArrayEquals( new String[]{ "DataOne", "TheDataOne" },
      vars.environmentSubstitute( new String[]{ "${VarOne}", "The${VarOne}" } ) );
  }

  @Test
  public void testInjection() {
    String systemPropertyName = this.getClass().getName() + "testInjection";
    String sysValue = "from system";
    System.getProperties().setProperty( systemPropertyName, sysValue );

    Variables vars1 = new Variables();
    Variables vars2 = new Variables();
    // only initialize vars1
    vars1.initializeVariablesFrom( null );
    assertEquals( sysValue, vars1.environmentSubstitute( "${" + systemPropertyName + "}" ) );

    Map<String, String> injectionMap = new HashMap<>();
    injectionMap.put( "VarOne", "DataOne" );
    String injectionValue = "from injection";
    injectionMap.put( systemPropertyName, injectionValue );

    vars1.injectVariables( injectionMap );
    vars2.injectVariables( injectionMap );

    vars2.initializeVariablesFrom( null );

    // make sure values survive regardless of injection/initialize order
    // these were initialized, then injected
    assertEquals( "DataOne", vars1.environmentSubstitute( "${VarOne}" ) );
    assertEquals( injectionValue, vars1.environmentSubstitute( "${" + systemPropertyName + "}" ) );

    // injected then initialized
    assertEquals( "DataOne", vars2.environmentSubstitute( "${VarOne}" ) );
    assertEquals( injectionValue, vars2.environmentSubstitute( "${" + systemPropertyName + "}" ) );

    // initialized, injected, initialized
    vars1.initializeVariablesFrom( null );
    assertEquals( "DataOne", vars1.environmentSubstitute( "${VarOne}" ) );
    assertEquals( injectionValue, vars1.environmentSubstitute( "${" + systemPropertyName + "}" ) );
  }

  @Test
  public void testParent() {
    String systemPropertyName = this.getClass().getName() + "testInjection";
    String sysValue = "from system";
    System.getProperties().setProperty( systemPropertyName, sysValue );

    Variables vars1 = new Variables();
    Variables vars2 = new Variables();
    // only initialize vars1
    vars1.initializeVariablesFrom( null );
    assertEquals( sysValue, vars1.environmentSubstitute( "${" + systemPropertyName + "}" ) );

    Variables parentVars = new Variables();
    parentVars.setVariable( "VarOne", "DataOne" );
    String parentValue = "from parent";
    parentVars.setVariable( systemPropertyName, parentValue );

    vars1.initializeVariablesFrom( parentVars );
    vars2.initializeVariablesFrom( parentVars );

    // make sure values survive regardless of being re-initialized
    assertEquals( "DataOne", vars1.environmentSubstitute( "${VarOne}" ) );
    assertEquals( parentValue, vars1.environmentSubstitute( "${" + systemPropertyName + "}" ) );

    assertEquals( "DataOne", vars2.environmentSubstitute( "${VarOne}" ) );
    assertEquals( parentValue, vars2.environmentSubstitute( "${" + systemPropertyName + "}" ) );

    vars2.initializeVariablesFrom( null );

    assertEquals( "DataOne", vars2.environmentSubstitute( "${VarOne}" ) );
    assertEquals( parentValue, vars2.environmentSubstitute( "${" + systemPropertyName + "}" ) );
  }

}
