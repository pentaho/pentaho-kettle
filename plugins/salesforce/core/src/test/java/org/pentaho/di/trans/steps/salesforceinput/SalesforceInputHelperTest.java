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

package org.pentaho.di.trans.steps.salesforceinput;

import com.sforce.soap.partner.FieldType;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.bind.XmlObject;
import com.sforce.ws.wsdl.Constants;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.steps.salesforce.SalesforceConnection;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.util.Assert.assertNotNull;
import static org.pentaho.di.core.util.Assert.assertTrue;
import static org.pentaho.di.trans.step.StepHelperInterface.ACTION_STATUS;
import static org.pentaho.di.trans.steps.salesforceinput.SalesforceInputHelper.VALUE_STRING;

public class SalesforceInputHelperTest {

  public static final String VALUE = "value";
  SalesforceInputHelper salesforceInputHelper;
  SalesforceInputMeta meta;
  TransMeta transMeta;

  @Before
  public void setUp() {
    meta = mock( SalesforceInputMeta.class );
    salesforceInputHelper = new SalesforceInputHelper( meta );
    transMeta = mock( TransMeta.class );

    KettleLogStore.init();
  }

  @Test
  public void testHandleStepAction_ModulesMethod() {
    Map<String, String> queryParams = new HashMap<>();
    salesforceInputHelper.handleStepAction( "modules", transMeta, queryParams );
    assertTrue( queryParams.containsKey( "moduleFlag" ) );
    assertEquals( "true", queryParams.get( "moduleFlag" ) );
  }

  @Test
  public void testHandleStepAction_GetFieldsMethod() {
    Map<String, String> queryParams = new HashMap<>();
    JSONObject response = salesforceInputHelper.handleStepAction( "getFields", transMeta, queryParams );
    assertNotNull( response );
    assertEquals( StepInterface.FAILURE_STATUS, response.get( StepInterface.STATUS ) );
  }

  @Test
  public void testHandleStepAction_OtherMethod() {
    Map<String, String> queryParams = new HashMap<>();
    JSONObject response = salesforceInputHelper.handleStepAction( "otherMethod", transMeta, queryParams );
    assertNotNull( response );
  }

  @Test
  public void testHandleStepAction_ExceptionHandling() {
    SalesforceInputHelper helperSpy = Mockito.spy( salesforceInputHelper );
    Mockito.doThrow( new RuntimeException( "Test Exception" ) )
        .when( helperSpy ).getFieldsAction( Mockito.any() );

    Map<String, String> queryParams = new HashMap<>();
    JSONObject response = helperSpy.handleStepAction( "getFields", transMeta, queryParams );
    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
  }

  @Test
  public void testAddFieldsFromSOQLQuery() {
    final Set<String> fields = new LinkedHashSet<>();
    XmlObject testObject = createObject( "Field1", VALUE, ObjectType.XMLOBJECT );
    salesforceInputHelper.addFields( "", fields, testObject );
    salesforceInputHelper.addFields( "", fields, testObject );
    assertArrayEquals( "No duplicates", new String[] { "Field1" }, fields.toArray() );

    testObject = createObject( "Field2", VALUE, ObjectType.XMLOBJECT );
    salesforceInputHelper.addFields( "", fields, testObject );
    assertArrayEquals( "Two fields", new String[] { "Field1", "Field2" }, fields.toArray() );
  }

  @Test
  public void testAddFields_nullIdNotAdded() {
    final Set<String> fields = new LinkedHashSet<>();
    XmlObject testObject = createObject( "Id", null, ObjectType.XMLOBJECT );
    salesforceInputHelper.addFields( "", fields, testObject );
    assertArrayEquals( "Null Id field not added", new String[] {}, fields.toArray() );
  }

  @Test
  public void testAddFields_IdAdded() {
    final Set<String> fields = new LinkedHashSet<>();
    XmlObject testObject = createObject( "Id", VALUE, ObjectType.XMLOBJECT );
    salesforceInputHelper.addFields( "", fields, testObject );
    assertArrayEquals( "Id field added", new String[] { "Id" }, fields.toArray() );
  }

  @Test
  public void testAddFields_nullRelationalIdNotAdded() {
    final Set<String> fields = new LinkedHashSet<>();
    XmlObject complexObject = createObject( "Module2", null, ObjectType.SOBJECT );
    complexObject.addField( "Id", createObject( "Id", null, ObjectType.XMLOBJECT ) );
    salesforceInputHelper.addFields( "", fields, complexObject );
    assertArrayEquals( "Relational null Id not added", new String[] {}, fields.toArray() );
  }

  @Test
  public void testAddFields_relationalIdAdded() {
    final Set<String> fields = new LinkedHashSet<>();
    XmlObject complexObject = createObject( "Module2", null, ObjectType.SOBJECT );
    complexObject.addField( "Id", createObject( "Id", VALUE, ObjectType.XMLOBJECT ) );
    complexObject.addField( "Name", createObject( "Name", VALUE, ObjectType.XMLOBJECT ) );
    salesforceInputHelper.addFields( "", fields, complexObject );
    assertArrayEquals( "Relational fields added", new String[] { "Module2.Id", "Module2.Name" }, fields.toArray() );
  }

  @Test
  public void testGetFieldsActionWithException() {
    Map<String, String> queryParams = new HashMap<>();
    JSONObject response = salesforceInputHelper.handleStepAction( "getFields", transMeta, queryParams );
    assertEquals( StepInterface.FAILURE_STATUS, response.get( StepInterface.STATUS ) );
  }

  @Test
  public void testGetFieldsAction() {
    Map<String, String> queryParams = new HashMap<>();
    com.sforce.soap.partner.Field field = new com.sforce.soap.partner.Field();
    field.setName( "test" );
    field.setLength( 10 );
    field.setPrecision( 2 );
    field.setType( FieldType.valueOf( VALUE_STRING ) );
    com.sforce.soap.partner.Field field1 = new com.sforce.soap.partner.Field();
    field1.setName( "test1" );
    field1.setLength( 10 );
    field1.setPrecision( 2 );
    field1.setDefaultValue( field );
    field1.setType( FieldType.valueOf( VALUE_STRING ) );
    com.sforce.soap.partner.Field[] fieldList = new com.sforce.soap.partner.Field[ 2 ];
    fieldList[ 0 ] = field;
    fieldList[ 1 ] = field1;

    try (
        MockedConstruction<SalesforceConnection> ignored = Mockito.mockConstruction( SalesforceConnection.class,
            ( mock, context ) -> {
              doNothing().when( mock ).connect();
              when( mock.getObjectFields( meta.getModule() ) ).thenReturn( fieldList );
            } ) ) {
      when( meta.isSpecifyQuery() ).thenReturn( false );
      JSONObject response = salesforceInputHelper.handleStepAction( "getFields", transMeta, queryParams );
      assertNotNull( response );
    }
  }

  @Test
  public void testGetFieldsActionWithSpecifiedQueryTrue() {
    Map<String, String> queryParams = new HashMap<>();
    XmlObject testObject = createObject( "Field1", VALUE, ObjectType.XMLOBJECT );
    XmlObject xmlObject = new XmlObject();
    xmlObject.setField( "Field1", testObject );
    xmlObject.setName( new QName( Constants.PARTNER_SOBJECT_NS, "test" ) );
    XmlObject[] fields = new XmlObject[ 10 ];
    fields[ 0 ] = testObject;
    fields[ 1 ] = xmlObject;
    try (
        MockedConstruction<SalesforceConnection> ignored = Mockito.mockConstruction( SalesforceConnection.class,
            ( mock, context ) -> {
              doNothing().when( mock ).connect();
              when( mock.getElements() ).thenReturn( fields );
            } ) ) {
      when( meta.isSpecifyQuery() ).thenReturn( true );
      JSONObject response = salesforceInputHelper.handleStepAction( "getFields", transMeta, queryParams );
      assertNotNull( response );
    }

  }

  @Test
  public void testModulesAction() {
    Map<String, String> queryParams = new HashMap<>();
    JSONObject response = salesforceInputHelper.modulesAction( transMeta, queryParams );
    assertTrue( response.containsKey( "actionStatus" ) );
  }

  @Test
  public void test_testButtonAction() {
    JSONObject response = salesforceInputHelper.testButtonAction( transMeta );
    assertTrue( response.containsKey( "connectionStatus" ) );
  }

  private XmlObject createObject( String fieldName, String value, ObjectType type ) {
    XmlObject result = switch ( type ) {
      case SOBJECT -> new SObject();
      case XMLOBJECT -> new XmlObject();
      default -> throw new IllegalArgumentException();
    };

    result.setName( new QName( Constants.PARTNER_SOBJECT_NS, fieldName ) );
    result.setValue( value );
    return result;
  }

  private enum ObjectType {
    SOBJECT,
    XMLOBJECT
  }
}
