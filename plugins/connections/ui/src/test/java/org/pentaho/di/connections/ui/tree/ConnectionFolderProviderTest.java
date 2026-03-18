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


package org.pentaho.di.connections.ui.tree;

import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.Collections;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;


public class ConnectionFolderProviderTest {
  private ConnectionFolderProvider connectionFolderProvider;
  private Spoon mockSpoon;
  private GUIResource mockGuiResource;
  private Bowl mockManagementBowl;
  private ConnectionManager mockConnectionManager;

  private MockedStatic<Spoon> spoonMockedStatic;
  private MockedStatic<GUIResource> guiResourceMockedStatic;

  @Before
  public void setup() throws Exception {
    mockSpoon = mock( Spoon.class );
    mockGuiResource = mock( GUIResource.class );
    mockManagementBowl = mock( Bowl.class );
    mockConnectionManager = mock( ConnectionManager.class );

    spoonMockedStatic = mockStatic( Spoon.class );
    guiResourceMockedStatic = mockStatic( GUIResource.class );
    when( Spoon.getInstance() ).thenReturn( mockSpoon );
    when( GUIResource.getInstance() ).thenReturn( mockGuiResource );

    doReturn( mockManagementBowl ).when( mockSpoon ).getManagementBowl();
    doReturn( mockManagementBowl ).when( mockSpoon ).getGlobalManagementBowl();
    doReturn( "Global" ).when( mockManagementBowl ).getLevelDisplayName();
    doReturn( mockConnectionManager ).when( mockManagementBowl ).getManager( ConnectionManager.class );
    doReturn( Collections.emptyList() ).when( mockConnectionManager ).getNames();
  }

  @After
  public void tearDown() {
    spoonMockedStatic.close();
    guiResourceMockedStatic.close();
  }

  @Test
  public void refresh_AddsGlobalConnection_WhenAllowed() throws Exception {
    connectionFolderProvider = new ConnectionFolderProvider();
    TreeNode parentTreeNode = new TreeNode();
    String connectionName = "global-connection";

    mockGlobalConnection( connectionName );
    doReturn( true ).when( mockSpoon ).isAllowedManageGlobalVFS();

    assertFalse( parentTreeNode.hasChildren() );

    connectionFolderProvider.refresh( Optional.empty(), parentTreeNode, null );

    assertTrue( parentTreeNode.hasChildren() );
    assertTrue( parentTreeNode.getChildren().stream().anyMatch(
      tn -> tn.getLabel() != null && tn.getLabel().startsWith( connectionName + " [" ) ) );
  }

  @Test
  public void refresh_SkipsGlobalConnection_WhenNotAllowed() throws Exception {
    connectionFolderProvider = new ConnectionFolderProvider();
    TreeNode parentTreeNode = new TreeNode();

    mockGlobalConnection( "global-connection" );
    doReturn( false ).when( mockSpoon ).isAllowedManageGlobalVFS();

    assertFalse( parentTreeNode.hasChildren() );

    connectionFolderProvider.refresh( Optional.empty(), parentTreeNode, null );

    assertFalse( parentTreeNode.hasChildren() );
  }

  @Test
  public void create_AddsConnectionsRootNode_WhenNotAllowed() throws Exception {
    connectionFolderProvider = new ConnectionFolderProvider();
    TreeNode parentTreeNode = new TreeNode();

    mockGlobalConnection( "global-connection" );
    doReturn( false ).when( mockSpoon ).isAllowedManageGlobalVFS();

    assertFalse( parentTreeNode.hasChildren() );

    connectionFolderProvider.create( Optional.empty(), parentTreeNode );

    assertTrue( parentTreeNode.hasChildren() );
    assertTrue( parentTreeNode.getChildren().stream().anyMatch(
      tn -> connectionFolderProvider.getTitle().equals( tn.getLabel() ) ) );
  }

  private void mockGlobalConnection( String connectionName ) throws Exception {
    ConnectionDetails connectionDetails = mock( ConnectionDetails.class );

    doReturn( Collections.singletonList( connectionName ) ).when( mockConnectionManager ).getNames();
    doReturn( connectionDetails ).when( mockConnectionManager ).getConnectionDetails( connectionName );
    doReturn( connectionName ).when( connectionDetails ).getName();
  }
}
