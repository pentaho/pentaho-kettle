/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.ui.spoon;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.tree.provider.PartitionsFolderProvider;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Andrey Khayrutdinov
 */
public class SpoonRefreshPartitionsSubtreeTest {

  private PartitionsFolderProvider partitionsFolderProvider;
  private TreeNode treeNode;

  @Before
  public void setUp() throws Exception {
    GUIResource guiResource = mock( GUIResource.class );
    Spoon spoon = mock( Spoon.class );
    partitionsFolderProvider = new PartitionsFolderProvider( guiResource, spoon );
    treeNode = new TreeNode();
  }

  private void callRefreshWith( TransMeta meta, String filter ) {
    partitionsFolderProvider.refresh( meta, treeNode, filter );
  }

  private void verifyNumberOfNodesCreated( int times ) {
    Assert.assertEquals( times, treeNode.getChildren().size() );
  }

  @Test
  public void noPartitionsExist() {
    TransMeta meta = mock( TransMeta.class );
    when( meta.getPartitionSchemas() ).thenReturn( Collections.<PartitionSchema>emptyList() );

    callRefreshWith( meta, null );
    verifyNumberOfNodesCreated( 0 );
  }

  @Test
  public void severalPartitionsExist() {
    TransMeta meta = prepareMetaWithThreeSchemas();

    callRefreshWith( meta, null );
    verifyNumberOfNodesCreated( 3 );
  }

  @Test
  public void onlyOneMatchesFiltering() {
    TransMeta meta = prepareMetaWithThreeSchemas();

    callRefreshWith( meta, "2" );
    verifyNumberOfNodesCreated( 1 );
  }


  private static TransMeta prepareMetaWithThreeSchemas() {
    TransMeta meta = mock( TransMeta.class );
    List<PartitionSchema> schemas =
      asList( mockSchema( "1" ), mockSchema( "2" ), mockSchema( "3" ) );
    when( meta.getPartitionSchemas() ).thenReturn( schemas );
    return meta;
  }

  private static PartitionSchema mockSchema( String name ) {
    PartitionSchema schema = new PartitionSchema( name, Collections.<String>emptyList() );
    return schema;
  }
}
