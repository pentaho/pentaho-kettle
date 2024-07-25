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

package org.pentaho.di.trans.steps.salesforceinput;

import com.rometools.rome.io.impl.Base64;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.bind.XmlObject;
import com.sforce.ws.wsdl.Constants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;

import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;


public class SalesforceInputTest {

  public static final String VALUE = "value";
  SalesforceInput salesforceInput;

  SalesforceInputMeta meta;
  SalesforceInputData data;

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
  public void testAddFieldsFromSOQLQuery() throws Exception {


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
  public void testAddFields_nullIdNotAdded() throws Exception {
    final Set<String> fields = new LinkedHashSet<>();
    XmlObject testObject = createObject( "Id", null, ObjectType.XMLOBJECT );
    salesforceInput.addFields( "", fields, testObject );
    assertArrayEquals( "Null Id field not added", new String[] {}, fields.toArray() );
  }

  @Test
  public void testAddFields_IdAdded() throws Exception {
    final Set<String> fields = new LinkedHashSet<>();
    XmlObject testObject = createObject( "Id", VALUE, ObjectType.XMLOBJECT );
    salesforceInput.addFields( "", fields, testObject );
    assertArrayEquals( "Id field added", new String[] { "Id" }, fields.toArray() );
  }

  @Test
  public void testAddFields_nullRelationalIdNotAdded() throws Exception {
    final Set<String> fields = new LinkedHashSet<>();
    XmlObject complexObject = createObject( "Module2", null, ObjectType.SOBJECT );
    complexObject.addField( "Id", createObject( "Id", null, ObjectType.XMLOBJECT ) );
    salesforceInput.addFields( "", fields, complexObject );
    assertArrayEquals( "Relational null Id not added", new String[] {}, fields.toArray() );
  }

  @Test
  public void testAddFields_relationalIdAdded() throws Exception {
    final Set<String> fields = new LinkedHashSet<>();
    XmlObject complexObject = createObject( "Module2", null, ObjectType.SOBJECT );
    complexObject.addField( "Id", createObject( "Id", VALUE, ObjectType.XMLOBJECT ) );
    complexObject.addField( "Name", createObject( "Name", VALUE, ObjectType.XMLOBJECT ) );
    salesforceInput.addFields( "", fields, complexObject );
    assertArrayEquals( "Relational fields added", new String[] { "Module2.Id", "Module2.Name" }, fields.toArray() );
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
