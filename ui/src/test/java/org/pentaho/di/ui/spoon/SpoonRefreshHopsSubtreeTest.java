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
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.tree.provider.HopsFolderProvider;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class SpoonRefreshHopsSubtreeTest {

  private HopsFolderProvider hopsFolderProvider;
  private TreeNode treeNode;

  @Before
  public void setUp() throws Exception {
    GUIResource guiResource = mock( GUIResource.class );
    hopsFolderProvider = new HopsFolderProvider( guiResource );
    treeNode = new TreeNode();
  }

  private void callRefreshWith( TransMeta meta, String filter ) {
    hopsFolderProvider.refresh( meta, treeNode, filter );
  }

  private void verifyNumberOfNodesCreated( int times ) {
    Assert.assertEquals( times, treeNode.getChildren().size() );
  }

  @Test
  public void noHopsExist() {
    TransMeta meta = mock( TransMeta.class );
    when( meta.nrTransHops() ).thenReturn( 0 );

    callRefreshWith( meta, null );
    verifyNumberOfNodesCreated( 0 );
  }

  @Test
  public void severalHopsExist() {
    TransMeta meta = prepareMetaWithThreeHops();

    callRefreshWith( meta, null );
    verifyNumberOfNodesCreated( 3 );
  }

  @Test
  public void onlyOneMatchesFiltering() {
    TransMeta meta = prepareMetaWithThreeHops();

    callRefreshWith( meta, "2" );
    verifyNumberOfNodesCreated( 1 );
  }

  private static TransMeta prepareMetaWithThreeHops() {
    TransMeta meta = mock( TransMeta.class );
    when( meta.nrTransHops() ).thenReturn( 3 );

    for ( int i = 0; i < meta.nrTransHops(); i++ ) {
      TransHopMeta hopMeta = mockHopMeta( i );
      when( meta.getTransHop( eq( i ) ) ).thenReturn( hopMeta );
    }
    return meta;
  }

  private static TransHopMeta mockHopMeta( int number ) {
    TransHopMeta hopMeta = new TransHopMeta( null, null, false );
    hopMeta = spy( hopMeta );
    doReturn( Integer.toString( number ) ).when( hopMeta ).toString();
    return hopMeta;
  }
}
