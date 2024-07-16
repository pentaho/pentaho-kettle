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
    doReturn( DefaultBowl.getInstance() ).when( mockSpoon ).getBowl();
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
    DatabasesCollector dbCollector = new DatabasesCollector( prepareDbManager( null ), prepareMeta( null ), null );
    Assert.assertEquals( 0, dbCollector.getDatabaseNames().size() );
    callRefreshWith( null, null );
    // one call - to create a parent tree node
    verifyNumberOfNodesCreated( 0 );
  }

  @Test
  public void severalConnectionsExist() throws Exception {
    AbstractMeta meta = prepareMetaWithThreeDbs();
    DatabaseConnectionManager mgr = prepareDbManager( mockDatabaseMeta( "mysql" ), mockDatabaseMeta( "oracle" ) );
    DatabasesCollector dbCollector = new DatabasesCollector( mgr, meta, null );
    Assert.assertEquals( 5, dbCollector.getDatabaseNames().size() );
    callRefreshWith( meta, null );
    verifyNumberOfNodesCreated( 5 );
  }

  @Test
  public void onlyOneMatchesFiltering() throws Exception {
    AbstractMeta meta = prepareMetaWithThreeDbs();
    DatabasesCollector dbCollector = new DatabasesCollector( prepareDbManager( null ), meta, null );
    Assert.assertEquals( 3, dbCollector.getDatabaseNames().size() );
    callRefreshWith( meta, "2" );
    verifyNumberOfNodesCreated( 1 );
  }


  private static AbstractMeta prepareMetaWithThreeDbs() {
    AbstractMeta meta = mock( AbstractMeta.class );
    List<DatabaseMeta> dbs =
            asList( mockDatabaseMeta( "1" ), mockDatabaseMeta( "2" ), mockDatabaseMeta( "3" ) );
    when( meta.getLocalDbMetas() ).thenReturn( dbs );
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
    when( mockDbManager.getDatabases() ).thenReturn( dbs );
    return mockDbManager;
  }

  private static AbstractMeta prepareMeta( DatabaseMeta... metas ) {
    if ( metas == null ) {
      metas = new DatabaseMeta[ 0 ];
    }

    AbstractMeta meta = mock( AbstractMeta.class );
    List<DatabaseMeta> dbs = asList( metas );
    when( meta.getLocalDbMetas() ).thenReturn( dbs );
    return meta;
  }
}
