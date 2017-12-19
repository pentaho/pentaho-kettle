/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.core.parameters;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


public class NamedParamsDefaultTest {
  NamedParams namedParams;

  @Before
  public void setUp() throws Exception {
    namedParams = spy( new NamedParamsDefault() );
  }

  @Test
  public void testParameters() throws Exception {
    assertNull( namedParams.getParameterValue( "var1" ) );
    assertNull( namedParams.getParameterDefault( "var1" ) );
    assertNull( namedParams.getParameterDescription( "var1" ) );

    namedParams.setParameterValue( "var1", "y" );
    // Values for new parameters must be added by addParameterDefinition
    assertNull( namedParams.getParameterValue( "var1" ) );
    assertNull( namedParams.getParameterDefault( "var1" ) );
    assertNull( namedParams.getParameterDescription( "var1" ) );

    namedParams.addParameterDefinition( "var2", "z", "My Description" );
    assertEquals( "", namedParams.getParameterValue( "var2" ) );
    assertEquals( "z", namedParams.getParameterDefault( "var2" ) );
    assertEquals( "My Description", namedParams.getParameterDescription( "var2" ) );
    namedParams.setParameterValue( "var2", "y" );
    assertEquals( "y", namedParams.getParameterValue( "var2" ) );
    assertEquals( "z", namedParams.getParameterDefault( "var2" ) );

    // clearParameters() just clears their values, not their presence
    namedParams.clearParameters();
    assertEquals( "", namedParams.getParameterValue( "var2" ) );

    // eraseParameters() clears the list of parameters
    namedParams.eraseParameters();
    assertNull( namedParams.getParameterValue( "var1" ) );

    // Call activateParameters(), an empty method, for coverage
    namedParams.activateParameters();
  }

  @Test( expected = DuplicateParamException.class )
  public void testAddParameterDefinitionWithException() throws DuplicateParamException {
    namedParams.addParameterDefinition( "key", "value", "description" );
    namedParams.addParameterDefinition( "key", "value", "description" );
  }

  @Test
  public void testCopyParametersFromNullChecks() throws Exception {
    // Test null case
    namedParams.copyParametersFrom( null );

    NamedParams namedParams2 = new NamedParamsDefault();

    // Test internal params == null case
    ( (NamedParamsDefault) namedParams ).params = null;
    namedParams.copyParametersFrom( namedParams2 );
  }

  @Test
  public void testCopyParametersFrom() throws Exception {
    NamedParams namedParams2 = new NamedParamsDefault();
    namedParams2.addParameterDefinition( "key", "default value", "description" );
    namedParams2.setParameterValue( "key", "value" );
    assertNull( namedParams.getParameterValue( "key" ) );
    namedParams.copyParametersFrom( namedParams2 );
    assertEquals( "value", namedParams.getParameterValue( "key" ) );
  }

  @Test
  public void testCopyParametersFromWithException() throws Exception {
    NamedParams namedParams2 = mock( NamedParams.class );
    when( namedParams2.listParameters() ).thenReturn( new String[]{ "key" } );
    when( namedParams2.getParameterDescription( anyString() ) ).thenThrow( UnknownParamException.class );
    when( namedParams2.getParameterDefault( anyString() ) ).thenThrow( UnknownParamException.class );

    when( namedParams2.getParameterValue( anyString() ) ).thenThrow( UnknownParamException.class );
    doThrow( DuplicateParamException.class ).when( namedParams )
      .addParameterDefinition( anyString(), anyString(), anyString() );

    namedParams.copyParametersFrom( namedParams2 );
    // Normally the param's properties would be empty, but we're overriding the addParameterDefinition() to throw an
    // exception, so the param properties never get set and are thus null
    assertNull( namedParams.getParameterDescription( "key" ) );
    assertNull( namedParams.getParameterDefault( "key" ) );
    assertNull( namedParams.getParameterValue( "key" ) );
  }
}
