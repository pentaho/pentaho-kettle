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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.tree.provider.DBConnectionFolderProvider;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class SpoonRefreshDbConnectionsSubtreeTest {

  private DBConnectionFolderProvider dbConnectionFolderProvider;
  private TreeNode treeNode;

  @Before
  public void setUp() throws Exception {
    GUIResource guiResource = mock( GUIResource.class );
    Spoon spoon = mock( Spoon.class );
    dbConnectionFolderProvider = new DBConnectionFolderProvider( guiResource, spoon );
    treeNode = new TreeNode();
  }

  private void callRefreshWith( AbstractMeta meta, String filter ) {
    dbConnectionFolderProvider.refresh( meta, treeNode, filter );
  }

  private void verifyNumberOfNodesCreated( int times ) {
    Assert.assertEquals( times, treeNode.getChildren().size() );
  }

  @Test
  public void noConnectionsExist() {
    AbstractMeta meta = mock( AbstractMeta.class );
    when( meta.getDatabases() ).thenReturn( Collections.<DatabaseMeta>emptyList() );

    callRefreshWith( meta, null );
    // one call - to create a parent tree node
    verifyNumberOfNodesCreated( 0 );
  }

  @Test
  public void severalConnectionsExist() {
    AbstractMeta meta = prepareMetaWithThreeDbs();

    callRefreshWith( meta, null );
    verifyNumberOfNodesCreated( 3 );
  }

  @Test
  public void onlyOneMatchesFiltering() {
    AbstractMeta meta = prepareMetaWithThreeDbs();

    callRefreshWith( meta, "2" );
    verifyNumberOfNodesCreated( 1 );
  }


  private static AbstractMeta prepareMetaWithThreeDbs() {
    AbstractMeta meta = mock( AbstractMeta.class );
    List<DatabaseMeta> dbs =
            asList( mockDatabaseMeta( "1" ), mockDatabaseMeta( "2" ), mockDatabaseMeta( "3" ) );
    when( meta.getDatabases() ).thenReturn( dbs );
    return meta;
  }

  private static DatabaseMeta mockDatabaseMeta( String name ) {
    DatabaseMeta mock = mock( DatabaseMeta.class );
    when( mock.getName() ).thenReturn( name );
    when( mock.getDisplayName() ).thenReturn( name );
    return mock;
  }
}
