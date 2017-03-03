/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegates;
import org.pentaho.di.ui.spoon.delegates.SpoonSlaveDelegate;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

/**
 * @author Andrey Khayrutdinov
 */
public class SpoonRefreshSlavesSubtreeTest {

  private Spoon spoon;

  @Before
  public void setUp() throws Exception {
    spoon = mock( Spoon.class );
    spoon.delegates = mock( SpoonDelegates.class );
    spoon.delegates.slaves = new SpoonSlaveDelegate( spoon );

    TreeItem mockItem = mock( TreeItem.class );
    when( spoon.createTreeItem( any( TreeItem.class ), anyString(), any( Image.class ) ) ).thenReturn( mockItem );

    doCallRealMethod().when( spoon )
      .refreshSlavesSubtree( any( TreeItem.class ), any( AbstractMeta.class ), any( GUIResource.class ) );

  }


  private void callRefreshWith( AbstractMeta meta ) {
    spoon.refreshSlavesSubtree( mock( TreeItem.class ), meta, mock( GUIResource.class ) );
  }

  private void verifyNumberOfNodesCreated( int times ) {
    verify( spoon, times( times ) ).createTreeItem( any( TreeItem.class ), anyString(), any( Image.class ) );
  }


  @Test
  public void noConnectionsExist() {
    AbstractMeta meta = mock( AbstractMeta.class );
    when( meta.getSlaveServers() ).thenReturn( Collections.<SlaveServer>emptyList() );

    callRefreshWith( meta );
    verifyNumberOfNodesCreated( 1 );
  }

  @Test
  public void severalConnectionsExist() {
    when( spoon.filterMatch( anyString() ) ).thenReturn( true );
    AbstractMeta meta = prepareMetaWithThreeSlaves();

    callRefreshWith( meta );
    verifyNumberOfNodesCreated( 4 );
  }

  @Test
  public void onlyOneMatchesFiltering() {
    when( spoon.filterMatch( eq( "2" ) ) ).thenReturn( true );
    AbstractMeta meta = prepareMetaWithThreeSlaves();

    callRefreshWith( meta );
    verifyNumberOfNodesCreated( 2 );
  }


  private static AbstractMeta prepareMetaWithThreeSlaves() {
    AbstractMeta meta = mock( AbstractMeta.class );
    List<SlaveServer> servers =
      asList( mockServer( "1" ), mockServer( "2" ), mockServer( "3" ) );
    when( meta.getSlaveServers() ).thenReturn( servers );
    return meta;
  }

  private static SlaveServer mockServer( String name ) {
    return new SlaveServer( name, null, null, null, null );
  }

}
