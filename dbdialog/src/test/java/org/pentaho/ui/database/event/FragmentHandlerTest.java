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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.dom.Document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FragmentHandler.
 */
public class FragmentHandlerTest {

  FragmentHandler fragmentHandler;
  Document document;
  XulDomContainer xulDomContainer;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    PluginRegistry.addPluginType( DatabasePluginType.getInstance() );
    PluginRegistry.init();
    KettleLogStore.init();
  }

  @Before
  public void setUp() throws Exception {
    fragmentHandler = new FragmentHandler();

    xulDomContainer = mock( XulDomContainer.class );

    document = mock( Document.class );
    when( xulDomContainer.getDocumentRoot() ).thenReturn( document );
    fragmentHandler.setXulDomContainer( xulDomContainer );
  }

  @Test
  public void testRefreshOptions() throws Exception {
    XulListbox connectionBox = mock( XulListbox.class );
    when( document.getElementById( "connection-type-list" ) ).thenReturn( connectionBox );
    when( connectionBox.getSelectedItem() ).thenReturn( "myDb" );
    XulListbox accessBox = mock( XulListbox.class );
    when( document.getElementById( "access-type-list" ) ).thenReturn( accessBox );
    when( accessBox.getSelectedItem() ).thenReturn( "Native" );
    DataHandler dataHandler = mock( DataHandler.class );
    when( xulDomContainer.getEventHandler( "dataHandler" ) ).thenReturn( dataHandler );
    DatabaseInterface dbInterface = mock( DatabaseInterface.class );
    when( dbInterface.getDefaultDatabasePort() ).thenReturn( 5309 );
    DataHandler.connectionMap.put( "myDb", dbInterface );

    XulComponent component = mock( XulComponent.class );
    XulComponent parent = mock( XulComponent.class );
    when( component.getParent() ).thenReturn( parent );
    when( document.getElementById( "database-options-box" ) ).thenReturn( component );
    XulDomContainer fragmentContainer = mock( XulDomContainer.class );
    Document mockDoc = mock( Document.class );
    XulComponent firstChild = mock( XulComponent.class );
    when( mockDoc.getFirstChild() ).thenReturn( firstChild );
    when( fragmentContainer.getDocumentRoot() ).thenReturn( mockDoc );
    when( xulDomContainer.loadFragment( anyString(), any( Object.class ) ) ).thenReturn( fragmentContainer );

    XulTextbox portBox = mock( XulTextbox.class );
    when( document.getElementById( "port-number-text" ) ).thenReturn( portBox );

    fragmentHandler.refreshOptions();

    // Iterate through the other database access types
    when( accessBox.getSelectedItem() ).thenReturn( "JNDI" );
    fragmentHandler.refreshOptions();
    when( accessBox.getSelectedItem() ).thenReturn( "ODBC" );
    fragmentHandler.refreshOptions();
    when( accessBox.getSelectedItem() ).thenReturn( "OCI" );
    fragmentHandler.refreshOptions();
    when( accessBox.getSelectedItem() ).thenReturn( "Plugin" );
    fragmentHandler.refreshOptions();
  }

  @Test
  public void testGetSetData() throws Exception {
    // This feature is basically disabled, get returns null and set does nothing
    assertNull( fragmentHandler.getData() );
    Object o = new Object();
    fragmentHandler.setData( o );
    assertNull( fragmentHandler.getData() );
  }

  @Test
  public void testLoadDatabaseOptionsFragment() throws Exception {
    XulComponent component = mock( XulComponent.class );
    XulComponent parent = mock( XulComponent.class );
    when( component.getParent() ).thenReturn( parent );
    when( document.getElementById( "database-options-box" ) ).thenReturn( component );
    XulDomContainer fragmentContainer = mock( XulDomContainer.class );
    Document mockDoc = mock( Document.class );
    XulComponent firstChild = mock( XulComponent.class );
    when( mockDoc.getFirstChild() ).thenReturn( firstChild );
    when( fragmentContainer.getDocumentRoot() ).thenReturn( mockDoc );
    when( xulDomContainer.loadFragment( anyString(), any( Object.class ) ) ).thenReturn( fragmentContainer );
    fragmentHandler.loadDatabaseOptionsFragment( "", new DataHandler() );
  }

  @Test( expected = XulException.class )
  public void testLoadDatabaseOptionsFragmentWithException() throws Exception {
    XulComponent component = mock( XulComponent.class );
    XulComponent parent = mock( XulComponent.class );
    when( component.getParent() ).thenReturn( parent );
    when( document.getElementById( "database-options-box" ) ).thenReturn( component );
    when( xulDomContainer.loadFragment( anyString(), any( Object.class ) ) ).thenThrow( new XulException() );
    fragmentHandler.loadDatabaseOptionsFragment( "", new DataHandler() );
  }

  @Test
  public void testGetFragment() throws Exception {
    DatabaseInterface dbInterface = mock( DatabaseInterface.class );
    assertEquals( "org/pentaho/ui/database/", fragmentHandler.getFragment( dbInterface, null, null, null ) );

    when( dbInterface.getXulOverlayFile() ).thenReturn( "overlay.xul" );
    // In real life the xul file should be available in the classpath as a resource, but during testing it won't be.
    // So instead of expecting FragmentHandler.packagePath + overlay.xul, it's just the package path
    assertEquals( "org/pentaho/ui/database/", fragmentHandler.getFragment( dbInterface, null, null, null ) );
  }

  @Test
  public void testShowMessage() throws Exception {
    XulMessageBox messageBox = mock( XulMessageBox.class );
    when( document.createElement( "messagebox" ) ).thenReturn( messageBox );
    fragmentHandler.showMessage( null );

    // Generate exception, should see a message in standard output
    when( document.createElement( "messagebox" ) ).thenThrow( new XulException() );
    fragmentHandler.showMessage( "" );
  }
}
