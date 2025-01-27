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
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.partition.PartitionSchemaManagementInterface;
import org.pentaho.di.partition.PartitionSchemaManager;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.tree.provider.PartitionsFolderProvider;

import java.util.Collections;
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
public class SpoonRefreshPartitionsSubtreeTest {

  private PartitionsFolderProvider partitionsFolderProvider;
  private TreeNode treeNode;
  private Spoon mockSpoon;
  private GUIResource mockGuiResource;
  private DefaultBowl mockDefaultBowl;
  private MockedStatic<Spoon> spoonMockedStatic;
  private MockedStatic<GUIResource> guiResourceMockedStatic;
  private MockedStatic<DefaultBowl> defaultBowlMockedStatic;
  private PartitionSchemaManager mockPartitionSchemaManager;

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

    mockPartitionSchemaManager = mock( PartitionSchemaManager.class );
    partitionsFolderProvider = new PartitionsFolderProvider( mockGuiResource, mockSpoon );
    treeNode = new TreeNode();

    doReturn( DefaultBowl.getInstance() ).when( mockSpoon ).getBowl();
    doReturn( DefaultBowl.getInstance() ).when( mockSpoon ).getGlobalManagementBowl();
    when( mockDefaultBowl.getManager( PartitionSchemaManagementInterface.class ) ).thenReturn( mockPartitionSchemaManager );
  }
  @After
  public void tearDown() {
    spoonMockedStatic.close();
    guiResourceMockedStatic.close();
    defaultBowlMockedStatic.close();
  }


  private void callRefreshWith( AbstractMeta meta, String filter ) {
    if ( meta == null ) {
      partitionsFolderProvider.refresh( Optional.empty(), treeNode, filter );
    } else {
      partitionsFolderProvider.refresh( Optional.of( meta ), treeNode, filter );
    }
  }

  private void verifyNumberOfNodesCreated( int times ) {
    Assert.assertEquals( times, treeNode.getChildren().size() );
  }

  @Test
  public void noPartitionsExist() {
    //TransMeta meta = mock( TransMeta.class );
    //when( meta.getPartitionSchemas() ).thenReturn( Collections.<PartitionSchema>emptyList() );

    callRefreshWith( null, null );
    verifyNumberOfNodesCreated( 0 );
  }

  @Test
  public void severalPartitionsExist() throws Exception {
    TransMeta meta = prepareMetaWithThreeSchemas();

    callRefreshWith( meta, null );
    verifyNumberOfNodesCreated( 3 );
  }

  @Test
  public void onlyOneMatchesFiltering() throws Exception {
    TransMeta meta = prepareMetaWithThreeSchemas();

    callRefreshWith( meta, "2" );
    verifyNumberOfNodesCreated( 1 );
  }


  private static TransMeta prepareMetaWithThreeSchemas() throws Exception {
    TransMeta meta = mock( TransMeta.class );
    List<PartitionSchema> schemas =
      asList( mockSchema( "1" ), mockSchema( "2" ), mockSchema( "3" ) );
    PartitionSchemaManagementInterface mocPartitionSchemaMgr = mock( PartitionSchemaManager.class );
    when( meta.getSharedObjectManager( PartitionSchemaManagementInterface.class ) ).thenReturn( mocPartitionSchemaMgr );
    when( mocPartitionSchemaMgr.getAll() ).thenReturn( schemas );
    when( meta.getPartitionSchemas() ).thenReturn( schemas );
    return meta;
  }

  private static PartitionSchema mockSchema( String name ) {
    PartitionSchema schema = new PartitionSchema( name, Collections.<String>emptyList() );
    return schema;
  }
}
