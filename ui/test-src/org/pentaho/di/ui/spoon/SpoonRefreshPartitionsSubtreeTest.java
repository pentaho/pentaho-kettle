package org.pentaho.di.ui.spoon;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.gui.GUIResource;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

/**
 * @author Andrey Khayrutdinov
 */
public class SpoonRefreshPartitionsSubtreeTest {

  private Spoon spoon;

  @Before
  public void setUp() throws Exception {
    spoon = mock( Spoon.class );

    TreeItem mockItem = mock( TreeItem.class );
    when( spoon.createTreeItem( any( TreeItem.class ), anyString(), any( Image.class ) ) ).thenReturn( mockItem );

    doCallRealMethod().when( spoon )
      .refreshPartitionsSubtree( any( TreeItem.class ), any( TransMeta.class ), any( GUIResource.class ) );
  }


  private void callRefreshWith( TransMeta meta ) {
    spoon.refreshPartitionsSubtree( mock( TreeItem.class ), meta, mock( GUIResource.class ) );
  }

  private void verifyNumberOfNodesCreated( int times ) {
    verify( spoon, times( times ) ).createTreeItem( any( TreeItem.class ), anyString(), any( Image.class ) );
  }

  @Test
  public void noPartitionsExist() {
    TransMeta meta = mock( TransMeta.class );
    when( meta.getPartitionSchemas() ).thenReturn( Collections.<PartitionSchema>emptyList() );

    callRefreshWith( meta );
    verifyNumberOfNodesCreated( 1 );
  }

  @Test
  public void severalPartitionsExist() {
    when( spoon.filterMatch( anyString() ) ).thenReturn( true );
    TransMeta meta = prepareMetaWithThreeSchemas();

    callRefreshWith( meta );
    verifyNumberOfNodesCreated( 4 );
  }

  @Test
  public void onlyOneMatchesFiltering() {
    when( spoon.filterMatch( eq( "2" ) ) ).thenReturn( true );
    TransMeta meta = prepareMetaWithThreeSchemas();

    callRefreshWith( meta );
    verifyNumberOfNodesCreated( 2 );
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
