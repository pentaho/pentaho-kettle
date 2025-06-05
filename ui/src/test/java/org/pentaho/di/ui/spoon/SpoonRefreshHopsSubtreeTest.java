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
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.tree.provider.HopsFolderProvider;

import java.util.Optional;

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
    hopsFolderProvider.refresh( Optional.of( meta ), treeNode, filter );
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
