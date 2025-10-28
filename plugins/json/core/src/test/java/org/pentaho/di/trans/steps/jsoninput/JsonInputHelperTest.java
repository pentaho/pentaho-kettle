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

package org.pentaho.di.trans.steps.jsoninput;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.jsoninput.json.node.Node;
import org.pentaho.di.trans.steps.jsoninput.json.node.ValueNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class JsonInputHelperTest {

  private JsonInputHelper helper;
  private TransMeta transMeta;
  private JsonInputMeta jsonInputMeta;
  private StepMeta stepMeta;
  private Map<String, String> queryParams;

  @Before
  public void setUp() {
    helper = new JsonInputHelper();
    transMeta = mock( TransMeta.class );
    jsonInputMeta = mock( JsonInputMeta.class );
    stepMeta = mock( StepMeta.class );
    queryParams = new HashMap<>();
    queryParams.put( "stepName", "step1" );
    when( transMeta.findStep( anyString() ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( jsonInputMeta );
  }


  @Test
  public void testConvertToJsonObject_withValueNode() {
    ValueNode valueNode = mock( ValueNode.class );
    when( valueNode.getType() ).thenReturn( "Value" );
    when( valueNode.getKey() ).thenReturn( "field" );
    when( valueNode.getValue() ).thenReturn( "abc" );
    JSONObject result = helper.convertToJsonObject( valueNode );
    assertEquals( "abc", result.get( "field" ) );
  }

  @Test
  public void testGetFilesAction_noFiles() {
    FileInputList fileInputList = mock( FileInputList.class );
    when( jsonInputMeta.getFiles( any(), eq( transMeta ) ) ).thenReturn( fileInputList );
    when( fileInputList.getFileStrings() ).thenReturn( new String[ 0 ] );
    JSONObject result = helper.getFilesAction( transMeta, queryParams );
    assertNotNull( result.get( "message" ) );
    JSONArray fileList = (JSONArray) result.get( "files" );
    assertEquals( 0, fileList.size() );
  }


  @Test
  public void testConvertToJsonObject_nullNode() {
    JSONObject result = helper.convertToJsonObject( null );
    assertTrue( result.isEmpty() );
  }


  @Test
  public void testHandleStepAction_unknownMethod() {
    JSONObject result = helper.handleStepAction( "unknown", transMeta, queryParams );
    assertEquals( JsonInputHelper.FAILURE_METHOD_NOT_FOUND_RESPONSE, result.get( JsonInputHelper.ACTION_STATUS ) );
  }

  @Test
  public void testGetFilesAction_withFilesPresent() {
    FileInputList fileInputList = mock( FileInputList.class );
    String[] files = new String[] { "a.json", "b.json" };
    when( jsonInputMeta.getFiles( any(), eq( transMeta ) ) ).thenReturn( fileInputList );
    when( fileInputList.getFileStrings() ).thenReturn( files );

    Map<String, String> localQueryParams = new HashMap<>();
    localQueryParams.put( "stepName", "step1" );

    JSONObject result = helper.getFilesAction( transMeta, localQueryParams );
    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    JSONArray fileList = (JSONArray) result.get( "files" );
    assertTrue( fileList.contains( "a.json" ) );
    assertTrue( fileList.contains( "b.json" ) );
  }

  @Test
  public void testHandleStepAction_getFilesDelegation() {
    FileInputList fileInputList = mock( FileInputList.class );
    when( jsonInputMeta.getFiles( any(), eq( transMeta ) ) ).thenReturn( fileInputList );
    when( fileInputList.getFileStrings() ).thenReturn( new String[] { "file.json" } );

    Map<String, String> localQueryParams = new HashMap<>();
    localQueryParams.put( "stepName", "step1" );
    JSONObject result = helper.handleStepAction( "getFiles", transMeta, localQueryParams );
    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
  }

  @Test
  public void testConvertToJsonObject_withObjectNode() {
    Node objectNode = mock( Node.class );
    when( objectNode.getType() ).thenReturn( "Object" );
    when( objectNode.getKey() ).thenReturn( "root" );
    when( objectNode.getChildren() ).thenReturn( Collections.emptyList() );
    JSONObject result = helper.convertToJsonObject( objectNode );
    assertTrue( result.containsKey( "root" ) );
  }

  @Test
  public void testConvertToJsonObject_withSimpleValueNode() {
    ValueNode valueNode = mock( ValueNode.class );
    when( valueNode.getType() ).thenReturn( "Value" );
    when( valueNode.getKey() ).thenReturn( "numberField" );
    when( valueNode.getValue() ).thenReturn( 123 );
    JSONObject result = helper.convertToJsonObject( valueNode );
    assertEquals( 123, result.get( "numberField" ) );
  }

  @Test
  public void testGetFilesAction_filesNull() {
    FileInputList fileInputList = mock( FileInputList.class );
    when( jsonInputMeta.getFiles( any(), eq( transMeta ) ) ).thenReturn( fileInputList );
    when( fileInputList.getFileStrings() ).thenReturn( null );

    Map<String, String> localQueryParams = new HashMap<>();
    localQueryParams.put( "stepName", "step1" );

    JSONObject result = helper.getFilesAction( transMeta, localQueryParams );

    assertNotNull( result.get( "message" ) );
    JSONArray fileList = (JSONArray) result.get( "files" );
    assertEquals( 0, fileList.size() );
  }

  @Test
  public void testConvertToJsonObject_valueNodeWithNullKey() {
    ValueNode valueNode = mock( ValueNode.class );
    when( valueNode.getType() ).thenReturn( "Value" );
    when( valueNode.getKey() ).thenReturn( null );
    when( valueNode.getValue() ).thenReturn( "val" );
    JSONObject result = helper.convertToJsonObject( valueNode );
    assertTrue( result.containsKey( null ) );
    assertEquals( "val", result.get( null ) );
  }

  @Test
  public void testConvertToJsonObject_arrayNodeWithNullKey() {
    Node arrayNode = mock( Node.class );
    when( arrayNode.getType() ).thenReturn( "Array" );
    when( arrayNode.getKey() ).thenReturn( null );
    when( arrayNode.getChildren() ).thenReturn( Collections.emptyList() );
    JSONObject result = helper.convertToJsonObject( arrayNode );
    assertTrue( result.containsKey( null ) );
  }


  @Test
  public void testConvertToJsonObject_valueNode() {
    Node objectNode = mock( Node.class );
    ValueNode valueNode = mock( ValueNode.class );

    when( objectNode.getType() ).thenReturn( "Object" );
    when( objectNode.getKey() ).thenReturn( "parent" );
    when( objectNode.getChildren() ).thenReturn( Collections.singletonList( valueNode ) );
    when( valueNode.getType() ).thenReturn( "Value" );
    when( valueNode.getKey() ).thenReturn( "child" );
    when( valueNode.getValue() ).thenReturn( "val" );

    JSONObject result = helper.convertToJsonObject( objectNode );
    assertTrue( result.containsKey( "parent" ) );
    Map<String, Object> parentMap = (Map<String, Object>) result.get( "parent" );
    assertEquals( "val", parentMap.get( "child" ) );
  }


  @Test
  public void testBuildErrorResponse() {
    JSONObject error = helper.buildErrorResponse( "labelKey", "messageKey" );
    assertEquals( StepInterface.FAILURE_RESPONSE, error.get( StepInterface.ACTION_STATUS ) );
    assertNotNull( error.get( "errorLabel" ) );
    assertNotNull( error.get( "errorMessage" ) );
  }

  @Test
  public void testConvertToJsonObject_objectNode() {
    Node objectNode = mock( Node.class );
    when( objectNode.getType() ).thenReturn( "Object" );
    when( objectNode.getKey() ).thenReturn( "root" );
    when( objectNode.getChildren() ).thenReturn( Collections.emptyList() );
    JSONObject result = helper.convertToJsonObject( objectNode );
    assertTrue( result.containsKey( "root" ) );
    // Additional assertion to differentiate from the other test
    Object rootValue = result.get( "root" );
    assertTrue( rootValue instanceof Map );
    assertTrue( ( (Map<?, ?>) rootValue ).isEmpty() );
  }

  @Test
  public void testConvertToJsonObject_arrayNode() {
    Node arrayNode = mock( Node.class );
    when( arrayNode.getType() ).thenReturn( "Array" );
    when( arrayNode.getKey() ).thenReturn( "arr" );
    when( arrayNode.getChildren() ).thenReturn( Collections.emptyList() );
    JSONObject result = helper.convertToJsonObject( arrayNode );
    assertTrue( result.containsKey( "arr" ) );
  }

  @Test
  public void testConvertToJsonObject_nestedObjectAndArray() {
    Node objectNode = mock( Node.class );
    Node arrayNode = mock( Node.class );
    ValueNode valueNode = mock( ValueNode.class );

    when( objectNode.getType() ).thenReturn( "Object" );
    when( objectNode.getKey() ).thenReturn( "root" );
    when( arrayNode.getType() ).thenReturn( "Array" );
    when( arrayNode.getKey() ).thenReturn( "arr" );
    when( valueNode.getType() ).thenReturn( "Value" );
    when( valueNode.getKey() ).thenReturn( "val" );
    when( valueNode.getValue() ).thenReturn( 123 );

    when( arrayNode.getChildren() ).thenReturn( Collections.singletonList( valueNode ) );
    when( objectNode.getChildren() ).thenReturn( Collections.singletonList( arrayNode ) );

    JSONObject result = helper.convertToJsonObject( objectNode );
    assertTrue( result.containsKey( "root" ) );
    Map<String, Object> rootMap = (Map<String, Object>) result.get( "root" );
    assertTrue( rootMap.containsKey( "arr" ) );
    JSONArray arr = (JSONArray) rootMap.get( "arr" );
    assertEquals( 123, arr.get( 0 ) );
  }

  @Test
  public void testGetFilesAction_withFiles() {
    FileInputList fileInputList = mock( FileInputList.class );
    String[] files = new String[] { "file1.json", "file2.json" };
    when( jsonInputMeta.getFiles( any(), eq( transMeta ) ) ).thenReturn( fileInputList );
    when( fileInputList.getFileStrings() ).thenReturn( files );

    JSONObject result = helper.getFilesAction( transMeta, queryParams );
    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    JSONArray fileList = (JSONArray) result.get( "files" );
    assertTrue( fileList.contains( "file1.json" ) );
    assertTrue( fileList.contains( "file2.json" ) );
  }

  @Test
  public void testConvertToJsonObject_arrayNodeWithValuesOnly() {
    Node arrayNode = mock( Node.class );
    ValueNode valueNode1 = mock( ValueNode.class );
    ValueNode valueNode2 = mock( ValueNode.class );

    when( arrayNode.getType() ).thenReturn( "Array" );
    when( arrayNode.getKey() ).thenReturn( "numbers" );
    when( valueNode1.getType() ).thenReturn( "Value" );
    when( valueNode1.getKey() ).thenReturn( "n1" );
    when( valueNode1.getValue() ).thenReturn( 1 );
    when( valueNode2.getType() ).thenReturn( "Value" );
    when( valueNode2.getKey() ).thenReturn( "n2" );
    when( valueNode2.getValue() ).thenReturn( 2 );
    when( arrayNode.getChildren() ).thenReturn( Arrays.asList( valueNode1, valueNode2 ) );

    JSONObject result = helper.convertToJsonObject( arrayNode );
    assertTrue( result.containsKey( "numbers" ) );
    JSONArray arr = (JSONArray) result.get( "numbers" );
    assertEquals( 1, arr.get( 0 ) );
    assertEquals( 2, arr.get( 1 ) );
  }

  @Test
  public void testSelectFieldsAction_noInputSpecified() {
    FileInputList fileInputList = mock( FileInputList.class );
    when( jsonInputMeta.getFiles( any(), eq( transMeta ) ) ).thenReturn( fileInputList );
    when( fileInputList.getFiles() ).thenReturn( Collections.emptyList() );
    JSONObject result = helper.selectFieldsAction( transMeta, queryParams );
    assertEquals( StepInterface.FAILURE_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    assertNotNull( result.get( "errorLabel" ) );
    assertNotNull( result.get( "errorMessage" ) );
  }


  @Test
  public void testSelectFieldsAction_getFilesReturnsNull() {
    when( jsonInputMeta.getFiles( any(), eq( transMeta ) ) ).thenReturn( null );
    JSONObject result = helper.selectFieldsAction( transMeta, queryParams );
    assertEquals( StepInterface.FAILURE_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
  }

  @Test
  public void testConvertToJsonObject_valueNodeNullKey() {
    ValueNode valueNode = mock( ValueNode.class );
    when( valueNode.getType() ).thenReturn( "Value" );
    when( valueNode.getKey() ).thenReturn( null );
    when( valueNode.getValue() ).thenReturn( "v" );
    JSONObject result = helper.convertToJsonObject( valueNode );
    assertTrue( result.containsKey( null ) );
    assertEquals( "v", result.get( null ) );
  }

  @Test
  public void testConvertToJsonObject_objectNodeWithMixedChildren() {
    Node objectNode = mock( Node.class );
    Node arrayNode = mock( Node.class );
    ValueNode valueNode = mock( ValueNode.class );

    when( objectNode.getType() ).thenReturn( "Object" );
    when( objectNode.getKey() ).thenReturn( "root" );
    when( arrayNode.getType() ).thenReturn( "Array" );
    when( arrayNode.getKey() ).thenReturn( "arr" );
    when( arrayNode.getChildren() ).thenReturn( Collections.emptyList() );
    when( valueNode.getType() ).thenReturn( "Value" );
    when( valueNode.getKey() ).thenReturn( "val" );
    when( valueNode.getValue() ).thenReturn( 42 );

    when( objectNode.getChildren() ).thenReturn( Arrays.asList( arrayNode, valueNode ) );

    JSONObject result = helper.convertToJsonObject( objectNode );
    assertTrue( result.containsKey( "root" ) );
    Object rootObj = result.get( "root" );
    assertTrue( rootObj instanceof java.util.Map );
    java.util.Map<?, ?> rootMap = (java.util.Map<?, ?>) rootObj;
    assertTrue( rootMap.containsKey( "arr" ) );
    assertTrue( rootMap.containsKey( "val" ) );
    assertEquals( 42, rootMap.get( "val" ) );
  }

  @Test
  public void testHandleStepAction_selectFieldsDelegation() {
    JsonInputHelper spyHelper = spy( new JsonInputHelper() );
    JSONObject expected = new JSONObject();
    doReturn( expected ).when( spyHelper ).selectFieldsAction( any(), any() );
    JSONObject result = spyHelper.handleStepAction( "selectFields", mock( TransMeta.class ), new java.util.HashMap<>() );
    assertSame( expected, result );
  }

  @Test
  public void testHandleStepAction_selectFieldsDelegation_nullQueryParams() {
    JsonInputHelper spyHelper = spy( new JsonInputHelper() );
    JSONObject expected = new JSONObject();
    doReturn( expected ).when( spyHelper ).selectFieldsAction( any(), eq( null ) );
    JSONObject result = spyHelper.handleStepAction( "selectFields", mock( TransMeta.class ), null );
    assertSame( expected, result );
  }

  @Test
  public void testBuildErrorResponse_customKeys() {
    String labelKey = "CustomLabel";
    String messageKey = "CustomMessage";
    JSONObject error = helper.buildErrorResponse( labelKey, messageKey );
    assertEquals( StepInterface.FAILURE_RESPONSE, error.get( StepInterface.ACTION_STATUS ) );
    assertNotNull( error.get( "errorLabel" ) );
    assertNotNull( error.get( "errorMessage" ) );
  }

  @Test
  public void testConvertToJsonObject_arrayNodeWithMixedChildren() {
    Node arrayNode = mock( Node.class );
    Node objectChild = mock( Node.class );
    ValueNode valueChild = mock( ValueNode.class );

    when( arrayNode.getType() ).thenReturn( "Array" );
    when( arrayNode.getKey() ).thenReturn( "arr" );
    when( objectChild.getType() ).thenReturn( "Object" );
    when( objectChild.getKey() ).thenReturn( "obj" );
    when( objectChild.getChildren() ).thenReturn( Collections.emptyList() );
    when( valueChild.getType() ).thenReturn( "Value" );
    when( valueChild.getKey() ).thenReturn( "val" );
    when( valueChild.getValue() ).thenReturn( "v" );
    when( arrayNode.getChildren() ).thenReturn( Arrays.asList( objectChild, valueChild ) );

    JSONObject result = helper.convertToJsonObject( arrayNode );
    assertTrue( result.containsKey( "arr" ) );
    JSONArray arr = (JSONArray) result.get( "arr" );
    assertTrue( arr.get( 0 ) instanceof Map );
    assertEquals( "v", arr.get( 1 ) );
  }

  @Test
  public void testConvertToJsonObject_deeplyNestedStructure() {
    Node rootObject = mock( Node.class );
    Node innerArray = mock( Node.class );
    Node innerObject = mock( Node.class );
    ValueNode valueNode = mock( ValueNode.class );

    when( rootObject.getType() ).thenReturn( "Object" );
    when( rootObject.getKey() ).thenReturn( "root" );
    when( innerArray.getType() ).thenReturn( "Array" );
    when( innerArray.getKey() ).thenReturn( "arr" );
    when( innerObject.getType() ).thenReturn( "Object" );
    when( innerObject.getKey() ).thenReturn( "obj" );
    when( valueNode.getType() ).thenReturn( "Value" );
    when( valueNode.getKey() ).thenReturn( "val" );
    when( valueNode.getValue() ).thenReturn( "deep" );

    when( innerObject.getChildren() ).thenReturn( Collections.singletonList( valueNode ) );
    when( innerArray.getChildren() ).thenReturn( Collections.singletonList( innerObject ) );
    when( rootObject.getChildren() ).thenReturn( Collections.singletonList( innerArray ) );

    JSONObject result = helper.convertToJsonObject( rootObject );
    assertTrue( result.containsKey( "root" ) );
    Map<?, ?> rootMap = (Map<?, ?>) result.get( "root" );
    assertTrue( rootMap.containsKey( "arr" ) );
    JSONArray arr = (JSONArray) rootMap.get( "arr" );
    Map<?, ?> obj = (Map<?, ?>) arr.get( 0 );
    assertEquals( "deep", obj.get( "val" ) );
  }

  @Test
  public void testBuildErrorResponse_emptyKeys() {
    JSONObject error = helper.buildErrorResponse( "", "" );
    assertEquals( StepInterface.FAILURE_RESPONSE, error.get( StepInterface.ACTION_STATUS ) );
    assertNotNull( error.get( "errorLabel" ) );
    assertNotNull( error.get( "errorMessage" ) );
  }

}
