/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.regexeval;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaPluginType;
import org.pentaho.di.core.variables.VariableSpace;

public class RegexEvalMetaTest {
  RowMetaInterface mockInputRowMeta;
  VariableSpace mockVariableSpace;

  @BeforeClass
  public static void setupClass() throws KettleException {
    ValueMetaPluginType.getInstance().searchPlugins();
  }

  @Before
  public void setup() {
    mockInputRowMeta = mock( RowMetaInterface.class );
    mockVariableSpace = mock( VariableSpace.class );
  }

  @Test
  public void testGetFieldsReplacesResultFieldIfItExists() throws KettleStepException {
    RegexEvalMeta regexEvalMeta = new RegexEvalMeta();
    String name = "TEST_NAME";
    String resultField = "result";
    regexEvalMeta.setResultFieldName( resultField );
    when( mockInputRowMeta.indexOfValue( resultField ) ).thenReturn( 0 );
    ValueMetaInterface mockValueMeta = mock( ValueMetaInterface.class );
    String mockName = "MOCK_NAME";
    when( mockValueMeta.getName() ).thenReturn( mockName );
    when( mockInputRowMeta.getValueMeta( 0 ) ).thenReturn( mockValueMeta );
    regexEvalMeta.setReplacefields( true );
    regexEvalMeta.getFields( mockInputRowMeta, name, null, null, mockVariableSpace, null, null );
    ArgumentCaptor<ValueMetaInterface> captor = ArgumentCaptor.forClass( ValueMetaInterface.class );
    verify( mockInputRowMeta ).setValueMeta( eq( 0 ), captor.capture() );
    assertEquals( mockName, captor.getValue().getName() );
  }

  @Test
  public void testGetFieldsAddsResultFieldIfDoesntExist() throws KettleStepException {
    RegexEvalMeta regexEvalMeta = new RegexEvalMeta();
    String name = "TEST_NAME";
    String resultField = "result";
    regexEvalMeta.setResultFieldName( resultField );
    when( mockInputRowMeta.indexOfValue( resultField ) ).thenReturn( -1 );
    ValueMetaInterface mockValueMeta = mock( ValueMetaInterface.class );
    String mockName = "MOCK_NAME";
    when( mockVariableSpace.environmentSubstitute( resultField ) ).thenReturn( mockName );
    when( mockInputRowMeta.getValueMeta( 0 ) ).thenReturn( mockValueMeta );
    regexEvalMeta.setReplacefields( true );
    regexEvalMeta.getFields( mockInputRowMeta, name, null, null, mockVariableSpace, null, null );
    ArgumentCaptor<ValueMetaInterface> captor = ArgumentCaptor.forClass( ValueMetaInterface.class );
    verify( mockInputRowMeta ).addValueMeta( captor.capture() );
    assertEquals( mockName, captor.getValue().getName() );
  }

  @Test
  public void testGetFieldsReplacesFieldIfItExists() throws KettleStepException {
    RegexEvalMeta regexEvalMeta = new RegexEvalMeta();
    String name = "TEST_NAME";
    regexEvalMeta.allocate( 1 );
    String fieldName = "fieldname";
    //CHECKSTYLE:Indentation:OFF
    regexEvalMeta.getFieldName()[0] = fieldName;
    when( mockInputRowMeta.indexOfValue( fieldName ) ).thenReturn( 0 );
    ValueMetaInterface mockValueMeta = mock( ValueMetaInterface.class );
    String mockName = "MOCK_NAME";
    when( mockValueMeta.getName() ).thenReturn( mockName );
    when( mockInputRowMeta.getValueMeta( 0 ) ).thenReturn( mockValueMeta );
    regexEvalMeta.setReplacefields( true );
    regexEvalMeta.setAllowCaptureGroupsFlag( true );
    regexEvalMeta.getFields( mockInputRowMeta, name, null, null, mockVariableSpace, null, null );
    ArgumentCaptor<ValueMetaInterface> captor = ArgumentCaptor.forClass( ValueMetaInterface.class );
    verify( mockInputRowMeta ).setValueMeta( eq( 0 ), captor.capture() );
    assertEquals( mockName, captor.getValue().getName() );
  }

  @Test
  public void testGetFieldsAddsFieldIfDoesntExist() throws KettleStepException {
    RegexEvalMeta regexEvalMeta = new RegexEvalMeta();
    String name = "TEST_NAME";
    regexEvalMeta.allocate( 1 );
    String fieldName = "fieldname";
    regexEvalMeta.getFieldName()[0] = fieldName;
    when( mockInputRowMeta.indexOfValue( fieldName ) ).thenReturn( -1 );
    ValueMetaInterface mockValueMeta = mock( ValueMetaInterface.class );
    String mockName = "MOCK_NAME";
    when( mockVariableSpace.environmentSubstitute( fieldName ) ).thenReturn( mockName );
    when( mockInputRowMeta.getValueMeta( 0 ) ).thenReturn( mockValueMeta );
    regexEvalMeta.setReplacefields( true );
    regexEvalMeta.setAllowCaptureGroupsFlag( true );
    regexEvalMeta.getFields( mockInputRowMeta, name, null, null, mockVariableSpace, null, null );
    ArgumentCaptor<ValueMetaInterface> captor = ArgumentCaptor.forClass( ValueMetaInterface.class );
    verify( mockInputRowMeta ).addValueMeta( captor.capture() );
    assertEquals( fieldName, captor.getValue().getName() );
  }
}
