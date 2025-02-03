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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.ClusterSchemaManagementInterface;
import org.pentaho.di.cluster.ClusterSchemaManager;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.tree.provider.ClustersFolderProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

/**
 * @author Andrey Khayrutdinov
 */
public class SpoonRefreshClustersSubtreeTest {

  private ClustersFolderProvider clustersFolderProvider;
  private TreeNode treeNode;
  private Spoon mockSpoon;
  private GUIResource mockGuiResource;
  private DefaultBowl mockDefaultBowl;
  private MockedStatic<Spoon> spoonMockedStatic;
  private MockedStatic<GUIResource> guiResourceMockedStatic;
  private MockedStatic<DefaultBowl> defaultBowlMockedStatic;
  private ClusterSchemaManager mockClusterSchemaManager;

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
    doReturn( DefaultBowl.getInstance() ).when( mockSpoon ).getBowl();
    doReturn( DefaultBowl.getInstance() ).when( mockSpoon ).getGlobalManagementBowl();
    mockClusterSchemaManager = mock( ClusterSchemaManager.class );
    when( mockDefaultBowl.getManager( ClusterSchemaManagementInterface.class ) ).thenReturn( mockClusterSchemaManager );

    clustersFolderProvider = new ClustersFolderProvider( mockGuiResource );
    treeNode = new TreeNode();
  }

  @After
  public void tearDown() {
    spoonMockedStatic.close();
    guiResourceMockedStatic.close();
    defaultBowlMockedStatic.close();
  }

  private void callRefreshWith( TransMeta meta, String filter ) {
    clustersFolderProvider.refresh( Optional.of( meta ), treeNode, filter );
  }

  private void verifyNumberOfNodesCreated( int times ) {
    Assert.assertEquals( times, treeNode.getChildren().size() );
  }

  @Test
  public void noClusters() {
    TransMeta meta = mock( TransMeta.class );
    when( meta.getClusterSchemas() ).thenReturn( Collections.<ClusterSchema>emptyList() );

    when( meta.getSharedObjectManager( ClusterSchemaManagementInterface.class ) ).thenReturn( mockClusterSchemaManager );

    callRefreshWith( meta, null );
    verifyNumberOfNodesCreated( 0 );
  }

  @Test
  public void severalClustersExist() throws Exception {
    TransMeta meta = prepareMeta();

    callRefreshWith( meta, null );
    verifyNumberOfNodesCreated( 3 );
  }

  @Test
  public void onlyOneMatchesFiltering() throws Exception {
    TransMeta meta = prepareMeta();

    callRefreshWith( meta, "2" );
    verifyNumberOfNodesCreated( 1 );
  }


  private TransMeta prepareMeta() throws Exception {
    TransMeta meta = mock( TransMeta.class );
    ClusterSchemaManager mockCSM = mock( ClusterSchemaManager.class );

    when( meta.getSharedObjectManager( ClusterSchemaManagementInterface.class ) ).thenReturn( mockCSM );
    List<ClusterSchema> schemas = Arrays.asList( createSchema( "1" ), createSchema( "2" ), createSchema( "3" ) );
    when( meta.getClusterSchemas() ).thenReturn( schemas );
    when( mockCSM.getAll() ).thenReturn( schemas );
    return meta;
  }

  private static ClusterSchema createSchema( String name ) {
    return new ClusterSchema( name, Collections.<SlaveServer>emptyList() );
  }
}
