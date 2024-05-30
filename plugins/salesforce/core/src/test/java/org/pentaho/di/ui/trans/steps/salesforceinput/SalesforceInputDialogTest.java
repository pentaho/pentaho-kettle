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
package org.pentaho.di.ui.trans.steps.salesforceinput;

import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.bind.XmlObject;
import com.sforce.ws.wsdl.Constants;
import org.apache.commons.lang.reflect.FieldUtils;
import org.eclipse.swt.widgets.Shell;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.salesforceinput.SalesforceInputMeta;
import org.pentaho.di.ui.core.PropsUI;

import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doNothing;


public class SalesforceInputDialogTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  public static final String VALUE = "value";
  private static boolean changedPropsUi;
  private SalesforceInputDialog dialog;

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  @BeforeClass
  public static void hackPropsUi() throws Exception {
    Field props = getPropsField();
    if ( props == null ) {
      throw new IllegalStateException( "Cannot find 'props' field in " + Props.class.getName() );
    }
    Object value = FieldUtils.readStaticField( props, true );
    if ( value == null ) {
      PropsUI mock = mock( PropsUI.class );
      FieldUtils.writeStaticField( props, mock, true );
      changedPropsUi = true;
    } else {
      changedPropsUi = false;
    }
  }

  @AfterClass
  public static void restoreNullInPropsUi() throws Exception {
    if ( changedPropsUi ) {
      Field props = getPropsField();
      FieldUtils.writeStaticField( props, null, true );
    }
  }

  private static Field getPropsField() {
    return FieldUtils.getDeclaredField( Props.class, "props", true );
  }

  @Before
  public void setUp() {
    dialog = spy( new SalesforceInputDialog( mock( Shell.class ), new SalesforceInputMeta(), mock( TransMeta.class ), "SalesforceInputDialogTest" ) );
    doNothing().when( dialog ).addFieldToTable( any(), any(), anyBoolean(), any(), any(), any() );
  }

  @Test
  public void testAddFieldsFromSOQLQuery() throws Exception {
    final Set<String> fields = new LinkedHashSet<>();
    XmlObject testObject = createObject( "Field1", VALUE, ObjectType.XMLOBJECT );
    dialog.addFields( "", fields, testObject );
    dialog.addFields( "", fields, testObject );
    assertArrayEquals( "No duplicates", new String[]{"Field1"}, fields.toArray() );

    testObject = createObject( "Field2", VALUE, ObjectType.XMLOBJECT );
    dialog.addFields( "", fields, testObject );
    assertArrayEquals( "Two fields", new String[]{"Field1", "Field2"}, fields.toArray() );
  }

  @Test
  public void testAddFields_nullIdNotAdded() throws Exception {
    final Set<String> fields = new LinkedHashSet<>();
    XmlObject testObject = createObject( "Id", null, ObjectType.XMLOBJECT );
    dialog.addFields( "", fields, testObject );
    assertArrayEquals( "Null Id field not added", new String[]{}, fields.toArray() );
  }

  @Test
  public void testAddFields_IdAdded() throws Exception {
    final Set<String> fields = new LinkedHashSet<>();
    XmlObject testObject = createObject( "Id", VALUE, ObjectType.XMLOBJECT );
    dialog.addFields( "", fields, testObject );
    assertArrayEquals( "Id field added", new String[]{"Id"}, fields.toArray() );
  }

  @Test
  public void testAddFields_nullRelationalIdNotAdded() throws Exception {
    final Set<String> fields = new LinkedHashSet<>();
    XmlObject complexObject = createObject( "Module2", null, ObjectType.SOBJECT );
    complexObject.addField( "Id", createObject( "Id", null, ObjectType.XMLOBJECT ) );
    dialog.addFields( "", fields, complexObject );
    assertArrayEquals( "Relational null Id not added", new String[]{}, fields.toArray() );
  }

  @Test
  public void testAddFields_relationalIdAdded() throws Exception {
    final Set<String> fields = new LinkedHashSet<>();
    XmlObject complexObject = createObject( "Module2", null, ObjectType.SOBJECT );
    complexObject.addField( "Id", createObject( "Id", VALUE, ObjectType.XMLOBJECT ) );
    complexObject.addField( "Name", createObject( "Name", VALUE, ObjectType.XMLOBJECT ) );
    dialog.addFields( "", fields, complexObject );
    assertArrayEquals( "Relational fields added", new String[]{"Module2.Id", "Module2.Name"}, fields.toArray() );
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
