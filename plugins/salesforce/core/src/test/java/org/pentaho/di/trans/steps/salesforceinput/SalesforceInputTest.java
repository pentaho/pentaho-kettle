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

import com.rometools.rome.io.impl.Base64;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.bind.XmlObject;
import com.sforce.ws.wsdl.Constants;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.salesforce.SalesforceConnection;

import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.util.Assert.assertNotNull;
import static org.pentaho.di.core.util.Assert.assertTrue;


public class SalesforceInputTest {

  public static final String VALUE = "value";
  SalesforceInput salesforceInput;

  SalesforceInputMeta meta;
  SalesforceInputData data;
  TransMeta transMeta;
  Trans trans;

  @Before
  public void setUp() {
    StepMeta stepMeta = new StepMeta();
    String name = "test";
    stepMeta.setName( name );
    StepDataInterface stepDataInterface = Mockito.mock( StepDataInterface.class );
    int copyNr = 0;
    TransMeta transMeta = Mockito.mock( TransMeta.class );
    Trans trans = Mockito.mock( Trans.class );
    Mockito.when( transMeta.findStep( Mockito.eq( name ) ) ).thenReturn( stepMeta );
    salesforceInput = new SalesforceInput( stepMeta, stepDataInterface, copyNr, transMeta, trans );

    meta = new SalesforceInputMeta();
    data = new SalesforceInputData();

    data.outputRowMeta = Mockito.mock( RowMeta.class );
    Mockito.when( data.outputRowMeta.getValueMeta( Mockito.eq( 0 ) ) ).thenReturn( new ValueMetaBinary() );

    data.convertRowMeta = Mockito.mock( RowMeta.class );
    Mockito.when( data.convertRowMeta.getValueMeta( Mockito.eq( 0 ) ) ).thenReturn( new ValueMetaString() );


  }

  @Test
  public void doConversions() throws Exception {


    Field metaField = salesforceInput.getClass().getDeclaredField( "meta" );
    metaField.setAccessible( true );
    metaField.set( salesforceInput, meta );

    Field dataField = salesforceInput.getClass().getDeclaredField( "data" );
    dataField.setAccessible( true );
    dataField.set( salesforceInput, data );

    Object[] outputRowData = new Object[ 1 ];
    byte[] binary = { 0, 1, 0, 1, 1, 1 };
    salesforceInput.doConversions( outputRowData, 0, new String( Base64.encode( binary ) ) );
    Assert.assertArrayEquals( binary, (byte[]) outputRowData[ 0 ] );

    binary = new byte[ 0 ];
    salesforceInput.doConversions( outputRowData, 0, new String( Base64.encode( binary ) ) );
    Assert.assertArrayEquals( binary, (byte[]) outputRowData[ 0 ] );
  }

  @Test
  public void testAddFieldsFromSOQLQuery() {

    final Set<String> fields = new LinkedHashSet<>();
    XmlObject testObject = createObject( "Field1", VALUE, ObjectType.XMLOBJECT );
    salesforceInput.addFields( "", fields, testObject );
    salesforceInput.addFields( "", fields, testObject );
    assertArrayEquals( "No duplicates", new String[] { "Field1" }, fields.toArray() );

    testObject = createObject( "Field2", VALUE, ObjectType.XMLOBJECT );
    salesforceInput.addFields( "", fields, testObject );
    assertArrayEquals( "Two fields", new String[] { "Field1", "Field2" }, fields.toArray() );
  }

  @Test
  public void testAddFields_nullIdNotAdded() {
    final Set<String> fields = new LinkedHashSet<>();
    XmlObject testObject = createObject( "Id", null, ObjectType.XMLOBJECT );
    salesforceInput.addFields( "", fields, testObject );
    assertArrayEquals( "Null Id field not added", new String[] {}, fields.toArray() );
  }

  @Test
  public void testAddFields_IdAdded() {
    final Set<String> fields = new LinkedHashSet<>();
    XmlObject testObject = createObject( "Id", VALUE, ObjectType.XMLOBJECT );
    salesforceInput.addFields( "", fields, testObject );
    assertArrayEquals( "Id field added", new String[] { "Id" }, fields.toArray() );
  }

  @Test
  public void testAddFields_nullRelationalIdNotAdded() {
    final Set<String> fields = new LinkedHashSet<>();
    XmlObject complexObject = createObject( "Module2", null, ObjectType.SOBJECT );
    complexObject.addField( "Id", createObject( "Id", null, ObjectType.XMLOBJECT ) );
    salesforceInput.addFields( "", fields, complexObject );
    assertArrayEquals( "Relational null Id not added", new String[] {}, fields.toArray() );
  }

  @Test
  public void testAddFields_relationalIdAdded() {
    final Set<String> fields = new LinkedHashSet<>();
    XmlObject complexObject = createObject( "Module2", null, ObjectType.SOBJECT );
    complexObject.addField( "Id", createObject( "Id", VALUE, ObjectType.XMLOBJECT ) );
    complexObject.addField( "Name", createObject( "Name", VALUE, ObjectType.XMLOBJECT ) );
    salesforceInput.addFields( "", fields, complexObject );
    assertArrayEquals( "Relational fields added", new String[] { "Module2.Id", "Module2.Name" }, fields.toArray() );
  }

  @Test
  public void testGetFieldsActionWithException() {
    Map<String, String> queryParams = new HashMap<>();
    JSONObject response = salesforceInput.doAction( "getFields", meta, transMeta, trans, queryParams );
    assertEquals( StepInterface.FAILURE_STATUS, response.get( StepInterface.STATUS ) );
  }

  @Test
  public void testGetFieldsAction() {
    Map<String, String> queryParams = new HashMap<>();
    com.sforce.soap.partner.Field field = new com.sforce.soap.partner.Field();
    field.setName( "test" );
    field.setLength( 10 );
    field.setPrecision( 2 );
    com.sforce.soap.partner.Field field1 = new com.sforce.soap.partner.Field();
    field1.setName( "test1" );
    field1.setLength( 10 );
    field1.setPrecision( 2 );
    field1.setDefaultValue( field );
    com.sforce.soap.partner.Field[] fieldList = new com.sforce.soap.partner.Field[ 10 ];
    fieldList[ 0 ] = field;
    fieldList[ 1 ] = field1;

    try (
      MockedConstruction<SalesforceConnection> ignored = Mockito.mockConstruction( SalesforceConnection.class,
        ( mock, context ) -> {
          doNothing().when( mock ).connect();
          when( mock.getObjectFields( anyString() ) ).thenReturn( fieldList );
        } ) ) {
      meta.setSpecifyQuery( false );
      JSONObject response = salesforceInput.doAction( "getFields", meta, transMeta, trans, queryParams );
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
      meta.setSpecifyQuery( true );
      JSONObject response = salesforceInput.doAction( "getFields", meta, transMeta, trans, queryParams );
      assertNotNull( response );
    }

  }

  @Test
  public void testModulesAction() {
    Map<String, String> queryParams = new HashMap<>();
    JSONObject response = salesforceInput.modulesAction( queryParams );
    assertTrue( response.containsKey( "actionStatus" ) );
  }

  @Test
  public void test_testButtonAction() {
    Map<String, String> queryParams = new HashMap<>();
    JSONObject response = salesforceInput.testButtonAction( queryParams );
    assertTrue( response.containsKey( "connectionStatus" ) );
  }

  private XmlObject createObject( String fieldName, String value, ObjectType type ) {
    XmlObject result;
    switch ( type ) {
      case SOBJECT:
        result = new SObject();
        break;
      case XMLOBJECT:
        result = new XmlObject();
        break;
      default:
        throw new IllegalArgumentException();
    }
    result.setName( new QName( Constants.PARTNER_SOBJECT_NS, fieldName ) );
    result.setValue( value );
    return result;
  }

  private enum ObjectType {
    SOBJECT,
    XMLOBJECT
  }

}
