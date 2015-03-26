package org.pentaho.di.ui.spoon;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.ui.core.gui.GUIResource;

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
