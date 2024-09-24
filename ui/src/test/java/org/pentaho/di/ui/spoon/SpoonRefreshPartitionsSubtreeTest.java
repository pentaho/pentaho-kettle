/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
