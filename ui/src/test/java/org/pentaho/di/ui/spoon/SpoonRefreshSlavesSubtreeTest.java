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
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.tree.provider.SlavesFolderProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class SpoonRefreshSlavesSubtreeTest {

  private SlavesFolderProvider slavesFolderProvider;
  private TreeNode treeNode;

  @Before
  public void setUp() throws Exception {
    GUIResource guiResource = mock( GUIResource.class );
    slavesFolderProvider = new SlavesFolderProvider( guiResource );
    treeNode = new TreeNode();
  }

  private void callRefreshWith( AbstractMeta meta, String filter ) {
    slavesFolderProvider.refresh( meta, treeNode, filter );
  }

  private void verifyNumberOfNodesCreated( int times ) {
    Assert.assertEquals( times, treeNode.getChildren().size() );
  }


  @Test
  public void noConnectionsExist() {
    AbstractMeta meta = mock( AbstractMeta.class );
    when( meta.getSlaveServers() ).thenReturn( Collections.<SlaveServer>emptyList() );

    callRefreshWith( meta, null );
    verifyNumberOfNodesCreated( 0 );
  }

  @Test
  public void severalConnectionsExist() {
    AbstractMeta meta = prepareMetaWithThreeSlaves();

    callRefreshWith( meta, null );
    verifyNumberOfNodesCreated( 3 );
  }

  @Test
  public void onlyOneMatchesFiltering() {
    AbstractMeta meta = prepareMetaWithThreeSlaves();

    callRefreshWith( meta, "2" );
    verifyNumberOfNodesCreated( 1 );
  }


  private static AbstractMeta prepareMetaWithThreeSlaves() {
    AbstractMeta meta = mock( AbstractMeta.class );
    List<SlaveServer> servers = Arrays.asList( mockServer( "1" ), mockServer( "2" ), mockServer( "3" ) );
    when( meta.getSlaveServers() ).thenReturn( servers );
    return meta;
  }

  private static SlaveServer mockServer( String name ) {
    return new SlaveServer( name, null, null, null, null );
  }

}
