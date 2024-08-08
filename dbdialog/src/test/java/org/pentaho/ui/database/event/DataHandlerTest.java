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
package org.pentaho.ui.database.event;

import java.util.Properties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.pentaho.di.core.database.BaseDatabaseMeta;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.ui.util.Launch;
import org.pentaho.ui.util.Launch.Status;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.XulCheckbox;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.containers.XulRoot;
import org.pentaho.ui.xul.dom.Document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for DataHandler.
 */
public class DataHandlerTest {

  DataHandler dataHandler;
  Document document;
  XulDomContainer xulDomContainer;
  XulListbox accessBox;
  XulListbox connectionBox;
  XulTextbox connectionNameBox;
  XulDeck dialogDeck;
  XulListbox deckOptionsBox;
  XulTextbox hostNameBox;
  XulTextbox databaseNameBox;
  XulTextbox portNumberBox;
  XulTextbox userNameBox;
  XulTextbox passwordBox;
  XulTextbox serverInstanceBox;
  XulTextbox webappName;
  XulMessageBox messageBox;
  XulRoot generalDatasourceWindow;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    PluginRegistry.addPluginType( DatabasePluginType.getInstance() );
    PluginRegistry.init();
    KettleLogStore.init();
  }

  @Before
  public void setUp() throws Exception {
    dataHandler = new DataHandler();
    // avoid actually opening browser windows during test
    Launch noLaunch = mock( Launch.class );
    when( noLaunch.openURL( anyString() ) ).thenReturn( Status.Success );
    dataHandler.launch = noLaunch;
    xulDomContainer = mock( XulDomContainer.class );

    document = mock( Document.class );
    XulComponent rootElement = mock( XulComponent.class );
    when( document.getRootElement() ).thenReturn( rootElement );

    // Mock the UI components

    accessBox = mock( XulListbox.class );
    when( document.getElementById( "access-type-list" ) ).thenReturn( accessBox );
    connectionBox = mock( XulListbox.class );
    when( document.getElementById( "connection-type-list" ) ).thenReturn( connectionBox );
    connectionNameBox = mock( XulTextbox.class );
    when( document.getElementById( "connection-name-text" ) ).thenReturn( connectionNameBox );
    dialogDeck = mock( XulDeck.class );
    when( document.getElementById( "dialog-panel-deck" ) ).thenReturn( dialogDeck );
    deckOptionsBox = mock( XulListbox.class );
    when( document.getElementById( "deck-options-list" ) ).thenReturn( deckOptionsBox );
    hostNameBox = mock( XulTextbox.class );
    when( document.getElementById( "server-host-name-text" ) ).thenReturn( hostNameBox );
    databaseNameBox = mock( XulTextbox.class );
    when( document.getElementById( "database-name-text" ) ).thenReturn( databaseNameBox );
    portNumberBox = mock( XulTextbox.class );
    when( document.getElementById( "port-number-text" ) ).thenReturn( portNumberBox );
    userNameBox = mock( XulTextbox.class );
    when( document.getElementById( "username-text" ) ).thenReturn( userNameBox );
    passwordBox = mock( XulTextbox.class );
    when( document.getElementById( "password-text" ) ).thenReturn( passwordBox );
    serverInstanceBox = mock( XulTextbox.class );
    when( document.getElementById( "instance-text" ) ).thenReturn( serverInstanceBox );
    when( serverInstanceBox.getValue() ).thenReturn( "instance" );
    when( serverInstanceBox.getAttributeValue( "shouldDisablePortIfPopulated" ) ).thenReturn( "true" );
    webappName = mock( XulTextbox.class );
    when( document.getElementById( "web-application-name-text" ) ).thenReturn( webappName );
    when( webappName.getValue() ).thenReturn( "webappName" );

    messageBox = mock( XulMessageBox.class );
    when( document.createElement( "messagebox" ) ).thenReturn( messageBox );
    when( xulDomContainer.getDocumentRoot() ).thenReturn( document );

    generalDatasourceWindow = mock( XulRoot.class );
    when( generalDatasourceWindow.getRootObject() ).thenReturn( mock( XulComponent.class ) );
    when( document.getElementById( "general-datasource-window" ) ).thenReturn( generalDatasourceWindow );
    dataHandler.setXulDomContainer( xulDomContainer );
  }

  @Test
  public void testLoadConnectionData() throws Exception {

    DatabaseInterface dbInterface = mock( DatabaseInterface.class );
    when( dbInterface.getDefaultDatabasePort() ).thenReturn( 5309 );
    DataHandler.connectionMap.put( "myDb", dbInterface );
    dataHandler.loadConnectionData();

    // Should immediately return if called again since the connectionBox will have been loaded
    dataHandler.loadConnectionData();
  }

  @Test
  public void testLoadConnectionDataWithSelectedItem() throws Exception {

    DatabaseInterface dbInterface = mock( DatabaseInterface.class );
    when( dbInterface.getDefaultDatabasePort() ).thenReturn( 5309 );
    when( connectionBox.getSelectedItem() ).thenReturn( "myDb" );
    dataHandler.loadConnectionData();
  }

  @Test
  public void testLoadAccessData() throws Exception {
    when( accessBox.getSelectedItem() ).thenReturn( "Native" );

    DatabaseInterface dbInterface = mock( DatabaseInterface.class );
    when( dbInterface.getDefaultDatabasePort() ).thenReturn( 5309 );
    DataHandler.connectionMap.put( "myDb", dbInterface );
    dataHandler.loadAccessData();

    // Should immediately return if called again since the connectionBox will have been loaded
    dataHandler.loadAccessData();
  }

  @Test
  public void testLoadAccessDataWithSelectedItem() throws Exception {

    DatabaseInterface dbInterface = mock( DatabaseInterface.class );
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    when( dbInterface.getAccessTypeList() ).thenReturn(
      new int[]{ DatabaseMeta.TYPE_ACCESS_NATIVE } );
    when( dbInterface.getDefaultDatabasePort() ).thenReturn( 5309 );
    when( connectionBox.getSelectedItem() ).thenReturn( "myDb" );
    DataHandler.connectionMap.put( "myDb", dbInterface );
    dataHandler.cache = databaseMeta;
    dataHandler.getData();
    dataHandler.loadAccessData();
  }

  @Test
  public void testEditOptions() throws Exception {

  }

  @Test
  public void testClearOptionsData() throws Exception {

  }

  @Test( expected = RuntimeException.class )
  public void testGetOptionHelpNoDatabase() throws Exception {
    when( accessBox.getSelectedItem() ).thenReturn( "JNDI" );
    when( connectionBox.getSelectedItem() ).thenReturn( "MyDB" );
    dataHandler.getOptionHelp();
  }

  @Test
  public void testGetOptionHelp() throws Exception {
    when( accessBox.getSelectedItem() ).thenReturn( "JNDI" );
    when( connectionBox.getSelectedItem() ).thenReturn( "PostgreSQL" );
    dataHandler.getOptionHelp();
  }

  @Test
  public void testSetDeckChildIndex() throws Exception {

  }

  @Test
  public void testOnPoolingCheck() throws Exception {

  }

  @Test
  public void testOnClusterCheck() throws Exception {

  }

  @Test
  public void testGetSetData() throws Exception {
    Object data = dataHandler.getData();
    assertNotNull( data );
    assertTrue( data instanceof DatabaseMeta );
    DatabaseMeta initialDbMeta = (DatabaseMeta) data;

    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    when( dbMeta.getAccessType() ).thenReturn( DatabaseMeta.TYPE_ACCESS_JNDI );
    Properties props = new Properties();
    props.put( BaseDatabaseMeta.ATTRIBUTE_PREFIX_EXTRA_OPTION + "KettleThin.webappname", "foo" );
    when( dbMeta.getAttributes() ).thenReturn( props );

    when( accessBox.getSelectedItem() ).thenReturn( "JNDI" );
    when( deckOptionsBox.getSelectedIndex() ).thenReturn( -1 );
    dataHandler.setData( dbMeta );
    assertEquals( dbMeta, dataHandler.getData() );
    assertNotSame( initialDbMeta, dataHandler.getData() );
    assertFalse( props.containsKey( BaseDatabaseMeta.ATTRIBUTE_PREFIX_EXTRA_OPTION + "KettleThin.webappname" ) );
    verify( dbMeta ).setDBName( "foo" );

    dataHandler.setData( null );
    assertEquals( dbMeta, dataHandler.getData() );
  }

  @Test
  public void testPushPopCache() throws Exception {
    dataHandler.getData();
    dataHandler.pushCache();
    dataHandler.popCache();
    verify( webappName ).setValue( "pentaho" );
  }

  @Test
  public void testPushCacheUpdatesDatabaseInterface() throws Exception {
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    when( connectionBox.getSelectedItem() ).thenReturn( "test" );
    dataHandler.cache = databaseMeta;
    dataHandler.getControls();
    dataHandler.getData();
    dataHandler.pushCache();
    verify( databaseMeta ).setDatabaseType( "test" );
  }

  @Test
  public void testOnCancel() throws Exception {

  }

  @Test
  public void testOnOK() throws Exception {

  }

  @Test
  public void testTestDatabaseConnection() throws Exception {

  }

  @Test
  public void testGetInfo() throws Exception {

  }

  @Test
  public void testRestoreDefaults() throws Exception {

  }

  @Test
  public void testPoolingRowChange() throws Exception {

  }

  @Test
  public void testGetControls() throws Exception {
    dataHandler.getControls();
    assertNotNull( dataHandler.hostNameBox );
    assertNotNull( dataHandler.portNumberBox );
    assertNotNull( dataHandler.userNameBox );
    assertNotNull( dataHandler.passwordBox );
  }

  @Test
  public void testDisablePortIfInstancePopulated() throws Exception {
    dataHandler.getControls();
    dataHandler.disablePortIfInstancePopulated();
    // Because portNumberBox is a mock, the setDisabled() will not persist, so the above call is for branch coverage

    when( serverInstanceBox.getValue() ).thenReturn( null );
    dataHandler.disablePortIfInstancePopulated();
    assertFalse( dataHandler.portNumberBox.isDisabled() );
  }

  @Test
  public void testShowMessage() throws Exception {
    dataHandler.showMessage( "MyMessage", false );
    dataHandler.showMessage( "MyMessage", true );
    when( document.createElement( "messagebox" ) ).thenThrow( new XulException() );
    dataHandler.showMessage( "MyMessage", false );
  }

  @Test
  public void testHandleUseSecurityCheckbox() throws Exception {
    dataHandler.handleUseSecurityCheckbox();
    // Now add the widget
    XulCheckbox useIntegratedSecurityCheck = mock( XulCheckbox.class );
    when( useIntegratedSecurityCheck.isChecked() ).thenReturn( false );
    when( document.getElementById( "use-integrated-security-check" ) ).thenReturn( useIntegratedSecurityCheck );
    dataHandler.getControls();
    dataHandler.handleUseSecurityCheckbox();
    when( useIntegratedSecurityCheck.isChecked() ).thenReturn( true );
    dataHandler.handleUseSecurityCheckbox();
  }

  @Test
  public void testDatabaseTypeListener() throws Exception {
    DataHandler.DatabaseTypeListener listener = spy( new DataHandler.DatabaseTypeListener( PluginRegistry.getInstance() ) {

      @Override
      public void databaseTypeAdded( String pluginName, DatabaseInterface databaseInterface ) {

      }

      @Override
      public void databaseTypeRemoved( String pluginName ) {

      }
    } );

    assertNotNull( listener );

    PluginInterface pluginInterface = mock( PluginInterface.class );
    when( pluginInterface.getName() ).thenReturn( "Oracle" );
    doReturn( DatabaseInterface.class ).when( pluginInterface ).getMainType();
    when( pluginInterface.getIds() ).thenReturn( new String[] { "oracle" } );
    doReturn( DatabasePluginType.class ).when( pluginInterface ).getPluginType();

    listener.pluginAdded( pluginInterface );
    // The test can't load the plugin, so databaseTypeAdded never gets called. Perhaps register a mock plugin
    verify( listener, never() ).databaseTypeAdded( eq( "Oracle" ), any( DatabaseInterface.class ) );
    listener.pluginRemoved( pluginInterface );
    verify( listener, times( 1 ) ).databaseTypeRemoved( "Oracle" );

    // Changed calls removed then added
    listener.pluginChanged( pluginInterface );
    verify( listener, times( 2 ) ).databaseTypeRemoved( "Oracle" );
    verify( listener, never() ).databaseTypeAdded( eq( "Oracle" ), any( DatabaseInterface.class ) );
  }
}
