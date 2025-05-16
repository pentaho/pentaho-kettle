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
import org.mockito.MockedStatic;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.shared.DatabaseConnectionManager;
import org.pentaho.di.shared.DatabaseManagementInterface;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.tree.provider.DBConnectionFolderProvider;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class SpoonRefreshDbConnectionsSubtreeTest {

  private DBConnectionFolderProvider dbConnectionFolderProvider;
  private TreeNode treeNode;
  private Spoon mockSpoon;
  private GUIResource mockGuiResource;
  private DatabaseConnectionManager mockDbManager;
  private DefaultBowl mockDefaultBowl;
  private MockedStatic<Spoon> spoonMockedStatic;
  private MockedStatic<GUIResource> guiResourceMockedStatic;
  private MockedStatic<DefaultBowl> defaultBowlMockedStatic;

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

    mockDbManager = mock( DatabaseConnectionManager.class );
    dbConnectionFolderProvider = new DBConnectionFolderProvider( mockGuiResource, mockSpoon );

    treeNode = new TreeNode();
    doReturn( DefaultBowl.getInstance() ).when( mockSpoon ).getManagementBowl();
    doReturn( DefaultBowl.getInstance() ).when( mockSpoon ).getGlobalManagementBowl();
    when( mockDefaultBowl.getManager( DatabaseManagementInterface.class ) ).thenReturn( mockDbManager );
  }

  @After
  public void tearDown() {
    spoonMockedStatic.close();
    guiResourceMockedStatic.close();
    defaultBowlMockedStatic.close();
  }

  private void callRefreshWith( AbstractMeta meta, String filter ) throws Exception {
    if ( meta == null ) {
      dbConnectionFolderProvider.refresh( Optional.empty(), treeNode, filter );
    } else {
      dbConnectionFolderProvider.refresh( Optional.of( meta ), treeNode, filter );
    }
  }

  private void verifyNumberOfNodesCreated( int times ) {
    Assert.assertEquals( times, treeNode.getChildren().size() );
  }

  @Test
  public void noConnectionsExist() throws Exception {
    DatabasesCollector dbCollector = new DatabasesCollector( prepareDbManager( null ), null );
    Assert.assertEquals( 0, dbCollector.getDatabaseNames().size() );

    dbCollector = new DatabasesCollector( prepareMeta( null ).getDatabaseManagementInterface(), null );
    Assert.assertEquals( 0, dbCollector.getDatabaseNames().size() );

    callRefreshWith( null, null );
    // one call - to create a parent tree node
    verifyNumberOfNodesCreated( 0 );
  }

  @Test
  public void severalConnectionsExist() throws Exception {
    DatabaseConnectionManager mgr = prepareDbManager( mockDatabaseMeta( "mysql" ), mockDatabaseMeta( "oracle" ) );
    DatabasesCollector dbCollector = new DatabasesCollector( mgr, null );
    Assert.assertEquals( 2, dbCollector.getDatabaseNames().size() );

    AbstractMeta meta = prepareMetaWithThreeDbs();
    dbCollector = new DatabasesCollector( meta.getDatabaseManagementInterface(), null );
    Assert.assertEquals( 3, dbCollector.getDatabaseNames().size() );

    callRefreshWith( meta, null );
    verifyNumberOfNodesCreated( 5 );
  }

  @Test
  public void onlyOneMatchesFiltering() throws Exception {
    AbstractMeta meta = prepareMetaWithThreeDbs();
    DatabasesCollector dbCollector = new DatabasesCollector( meta.getDatabaseManagementInterface(), null );
    Assert.assertEquals( 3, dbCollector.getDatabaseNames().size() );
    callRefreshWith( meta, "2" );
    verifyNumberOfNodesCreated( 1 );
  }


  private static AbstractMeta prepareMetaWithThreeDbs() throws Exception {
    AbstractMeta meta = mock( AbstractMeta.class );
    List<DatabaseMeta> dbs =
            asList( mockDatabaseMeta( "1" ), mockDatabaseMeta( "2" ), mockDatabaseMeta( "3" ) );

    DatabaseManagementInterface dbMgr = mock( DatabaseManagementInterface.class );
    when( dbMgr.getAll() ).thenReturn( dbs );
    when( meta.getDatabaseManagementInterface() ).thenReturn( dbMgr );
    return meta;
  }

  private static DatabaseMeta mockDatabaseMeta( String name ) {
    DatabaseMeta mock = mock( DatabaseMeta.class );
    when( mock.getName() ).thenReturn( name );
    when( mock.getDisplayName() ).thenReturn( name );
    return mock;
  }
  private DatabaseConnectionManager prepareDbManager( DatabaseMeta... metas ) throws KettleException {
    if ( metas == null ) {
      metas = new DatabaseMeta[ 0 ];
    }

    List<DatabaseMeta> dbs = asList( metas );
    when( mockDbManager.getAll() ).thenReturn( dbs );
    return mockDbManager;
  }

  private static AbstractMeta prepareMeta( DatabaseMeta... metas ) throws Exception {
    if ( metas == null ) {
      metas = new DatabaseMeta[ 0 ];
    }

    AbstractMeta meta = mock( AbstractMeta.class );
    List<DatabaseMeta> dbs = asList( metas );
    DatabaseManagementInterface dbMgr = mock( DatabaseManagementInterface.class );
    when( dbMgr.getAll() ).thenReturn( dbs );
    when( meta.getDatabaseManagementInterface() ).thenReturn( dbMgr );
    return meta;
  }
}
