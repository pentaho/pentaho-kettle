/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.row;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaPluginType;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.xml.XMLHandler;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.w3c.dom.Node;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
public class ValueMetaAndDataTest {

  private PluginRegistry pluginRegistry;

  @Before
  public void before() {
    pluginRegistry = Mockito.mock( PluginRegistry.class );
  }

  @Test
  public void testConstructors() throws KettleValueException {
    ValueMetaAndData result;

    result = new ValueMetaAndData( new ValueMetaString( "ValueStringName" ), "testValue1" );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_STRING, result.getValueMeta().getType() );
    assertEquals( "ValueStringName", result.getValueMeta().getName() );
    assertEquals( "testValue1", result.getValueData() );

    result = new ValueMetaAndData( "StringName", "testValue2" );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_STRING, result.getValueMeta().getType() );
    assertEquals( "StringName", result.getValueMeta().getName() );
    assertEquals( "testValue2", result.getValueData() );

    result = new ValueMetaAndData( "NumberName", Double.valueOf( "123.45" ) );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_NUMBER, result.getValueMeta().getType() );
    assertEquals( "NumberName", result.getValueMeta().getName() );
    assertEquals( Double.valueOf( "123.45" ), result.getValueData() );

    result = new ValueMetaAndData( "IntegerName", 234L );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, result.getValueMeta().getType() );
    assertEquals( "IntegerName", result.getValueMeta().getName() );
    assertEquals( 234L, result.getValueData() );

    Date testDate = Calendar.getInstance().getTime();
    result = new ValueMetaAndData( "DateName", testDate );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_DATE, result.getValueMeta().getType() );
    assertEquals( "DateName", result.getValueMeta().getName() );
    assertEquals( testDate, result.getValueData() );

    result = new ValueMetaAndData( "BigNumberName", new BigDecimal( "123456789.987654321" ) );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_BIGNUMBER, result.getValueMeta().getType() );
    assertEquals( "BigNumberName", result.getValueMeta().getName() );
    assertEquals( new BigDecimal( "123456789.987654321" ), result.getValueData() );

    result = new ValueMetaAndData( "BooleanName", Boolean.TRUE );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_BOOLEAN, result.getValueMeta().getType() );
    assertEquals( "BooleanName", result.getValueMeta().getName() );
    assertEquals( Boolean.TRUE, result.getValueData() );

    byte[] testBytes = new byte[ 50 ];
    new Random().nextBytes( testBytes );
    result = new ValueMetaAndData( "BinaryName", testBytes );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_BINARY, result.getValueMeta().getType() );
    assertEquals( "BinaryName", result.getValueMeta().getName() );
    assertArrayEquals( testBytes, (byte[]) result.getValueData() );

    result = new ValueMetaAndData( "SerializableName", new StringBuilder( "serializable test" ) );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_SERIALIZABLE, result.getValueMeta().getType() );
    assertEquals( "SerializableName", result.getValueMeta().getName() );
    assertTrue( result.getValueData() instanceof StringBuilder );
    assertEquals( "serializable test", result.getValueData().toString() );

  }

  @Test
  @PrepareForTest( { EnvUtil.class } )
  public void testLoadXML() throws ParseException, KettleXMLException {
    PowerMockito.mockStatic( EnvUtil.class );
    Mockito.when( EnvUtil.getSystemProperty( Const.KETTLE_DEFAULT_DATE_FORMAT ) )
      .thenReturn( "yyyy-MM-dd HH:mm:ss.SSS" );
    ValueMetaAndData valueMetaAndData = new ValueMetaAndData( Mockito.mock( ValueMetaInterface.class ), new Object() );
    List<PluginInterface> pluginTypeList = new ArrayList<>();
    PluginInterface plugin = Mockito.mock( PluginInterface.class );
    Mockito.when( plugin.getName() ).thenReturn( "3" );
    String[] ids = { "3" };
    Mockito.when( plugin.getIds() ).thenReturn( ids );
    pluginTypeList.add( plugin );
    Mockito.when( pluginRegistry.getPlugins( ValueMetaPluginType.class ) ).thenReturn( pluginTypeList );
    ValueMetaFactory.pluginRegistry = pluginRegistry;

    String testData = "2010/01/01 00:00:00.000";
    Node node = XMLHandler.loadXMLString(
      "<value>\n"
        + "    <name/>\n"
        + "    <type>3</type>\n"
        + "    <text>" + testData + "</text>\n"
        + "    <length>-1</length>\n"
        + "    <precision>-1</precision>\n"
        + "    <isnull>N</isnull>\n"
        + "    <mask/>\n"
        + "</value>", "value" );

    valueMetaAndData.loadXML( node );
    Assert.assertEquals( valueMetaAndData.getValueData(),
      new SimpleDateFormat( ValueMetaBase.COMPATIBLE_DATE_FORMAT_PATTERN ).parse( testData ) );
  }
}
