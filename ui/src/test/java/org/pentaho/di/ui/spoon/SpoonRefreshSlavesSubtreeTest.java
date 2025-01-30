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


package org.pentaho.di.ui.spoon;

import org.junit.*;
import org.mockito.MockedStatic;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.cluster.SlaveServerManagementInterface;
import org.pentaho.di.cluster.SlaveServerManager;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.tree.provider.SlavesFolderProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class SpoonRefreshSlavesSubtreeTest {

  private SlavesFolderProvider slavesFolderProvider;
  private TreeNode treeNode;
  private Spoon mockSpoon;
  private GUIResource mockGuiResource;
  private DefaultBowl mockDefaultBowl;
  private MockedStatic<Spoon> spoonMockedStatic;
  private MockedStatic<GUIResource> guiResourceMockedStatic;
  private MockedStatic<DefaultBowl> defaultBowlMockedStatic;
  private SlaveServerManager mockSlaveServerManager;

  @Before
  public void setUp() throws Exception {
    mockGuiResource = mock( GUIResource.class );
    mockSpoon = mock( Spoon.class );
    mockDefaultBowl = mock( DefaultBowl.class );

    spoonMockedStatic = mockStatic( Spoon.class );
    guiResourceMockedStatic = mockStatic( GUIResource.class );
    defaultBowlMockedStatic = mockStatic( DefaultBowl.class );

    when( Spoon.getInstance() ).thenReturn( mockSpoon );
    when( GUIResource.getInstance() ).thenReturn( mockGuiResource );
    when( DefaultBowl.getInstance() ).thenReturn( mockDefaultBowl );

    mockSlaveServerManager = mock( SlaveServerManager.class );
    slavesFolderProvider = new SlavesFolderProvider( mockGuiResource );
    treeNode = new TreeNode();

    doReturn( DefaultBowl.getInstance() ).when( mockSpoon ).getBowl();
    doReturn( DefaultBowl.getInstance() ).when( mockSpoon ).getGlobalManagementBowl();
    when( mockDefaultBowl.getManager( SlaveServerManagementInterface.class ) ).thenReturn( mockSlaveServerManager );
  }

  @After
  public void tearDown() {
    spoonMockedStatic.close();
    guiResourceMockedStatic.close();
    defaultBowlMockedStatic.close();
  }

  private void callRefreshWith( AbstractMeta meta, String filter ) {
    if ( meta == null ) {
      slavesFolderProvider.refresh( Optional.empty(), treeNode, filter );
    } else {
      slavesFolderProvider.refresh( Optional.of( meta ), treeNode, filter );
    }
  }

  private void verifyNumberOfNodesCreated( int times ) {
    Assert.assertEquals( times, treeNode.getChildren().size() );
  }


  @Test
  public void noConnectionsExist() {
    callRefreshWith( null, null );
    verifyNumberOfNodesCreated( 0 );
  }

  @Test
  public void severalConnectionsExist() throws Exception {
    AbstractMeta meta = prepareMetaWithThreeSlaves();

    callRefreshWith( meta, null );
    verifyNumberOfNodesCreated( 3 );
  }

  @Test
  public void onlyOneMatchesFiltering() throws Exception {
    AbstractMeta meta = prepareMetaWithThreeSlaves();

    callRefreshWith( meta, "2" );
    verifyNumberOfNodesCreated( 1 );
  }


  private static AbstractMeta prepareMetaWithThreeSlaves() throws Exception {
    AbstractMeta meta = mock( AbstractMeta.class );
    List<SlaveServer> servers = Arrays.asList( mockServer( "1" ), mockServer( "2" ), mockServer( "3" ) );

    SlaveServerManagementInterface mocSlaveServerMgr = mock( SlaveServerManager.class );
    when( meta.getSlaveServerManagementInterface() ).thenReturn( mocSlaveServerMgr );
    when( mocSlaveServerMgr.getAll() ).thenReturn( servers );
    when( meta.getSlaveServers() ).thenReturn( servers );
    return meta;
  }

  private static SlaveServer mockServer( String name ) {
    return new SlaveServer( name, null, null, null, null );
  }

}
