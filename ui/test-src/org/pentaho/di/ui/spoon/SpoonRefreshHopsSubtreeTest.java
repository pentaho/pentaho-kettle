package org.pentaho.di.ui.spoon;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.gui.GUIResource;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

/**
 * @author Andrey Khayrutdinov
 */
public class SpoonRefreshHopsSubtreeTest {

  private Spoon spoon;

  @Before
  public void setUp() throws Exception {
    spoon = mock( Spoon.class );

    TreeItem mockItem = mock( TreeItem.class );
    when( spoon.createTreeItem( any( TreeItem.class ), anyString(), any( Image.class ) ) ).thenReturn( mockItem );

    doCallRealMethod().when( spoon )
      .refreshHopsSubtree( any( TreeItem.class ), any( TransMeta.class ), any( GUIResource.class ) );
  }


  private void callRefreshWith( TransMeta meta ) {
    spoon.refreshHopsSubtree( mock( TreeItem.class ), meta, mock( GUIResource.class ) );
  }

  private void verifyNumberOfNodesCreated( int times ) {
    verify( spoon, times( times ) ).createTreeItem( any( TreeItem.class ), anyString(), any( Image.class ) );
  }


  @Test
  public void noHopsExist() {
    TransMeta meta = mock( TransMeta.class );
    when( meta.nrTransHops() ).thenReturn( 0 );

    callRefreshWith( meta );
    verifyNumberOfNodesCreated( 1 );
  }

  @Test
  public void severalHopsExist() {
    when( spoon.filterMatch( anyString() ) ).thenReturn( true );
    TransMeta meta = prepareMetaWithThreeHops();

    callRefreshWith( meta );
    verifyNumberOfNodesCreated( 4 );
  }

  @Test
  public void onlyOneMatchesFiltering() {
    when( spoon.filterMatch( eq( "2" ) ) ).thenReturn( true );
    TransMeta meta = prepareMetaWithThreeHops();

    callRefreshWith( meta );
    verifyNumberOfNodesCreated( 2 );
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
