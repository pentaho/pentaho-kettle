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
