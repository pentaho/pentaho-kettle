package org.pentaho.di.ui.spoon;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.ui.core.gui.GUIResource;

import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class SpoonRefreshDbConnectionsSubtreeTest {

  private Spoon spoon;

  @Before
  public void setUp() throws Exception {
    spoon = mock( Spoon.class );

    TreeItem mockItem = mock( TreeItem.class );
    when( spoon.createTreeItem( any( TreeItem.class ), anyString(), any( Image.class ) ) ).thenReturn( mockItem );

    doCallRealMethod().when( spoon )
      .refreshDbConnectionsSubtree( any( TreeItem.class ), any( AbstractMeta.class ), any( GUIResource.class ) );
  }


  private void callRefreshWith( AbstractMeta meta ) {
    spoon.refreshDbConnectionsSubtree( mock( TreeItem.class ), meta, mock( GUIResource.class ) );
  }

  private void verifyNumberOfNodesCreated( int times ) {
    verify( spoon, times( times ) ).createTreeItem( any( TreeItem.class ), anyString(), any( Image.class ) );
  }


  @Test
  public void noConnectionsExist() {
    AbstractMeta meta = mock( AbstractMeta.class );
    when( meta.getDatabaseNames() ).thenReturn( new String[ 0 ] );

    callRefreshWith( meta );
    // one call - to create a parent tree node
    verifyNumberOfNodesCreated( 1 );
  }

  @Test
  public void severalConnectionsExist() {
    when( spoon.filterMatch( anyString() ) ).thenReturn( true );
    AbstractMeta meta = prepareMetaWithThreeDbs();

    callRefreshWith( meta );
    verifyNumberOfNodesCreated( 4 );
  }

  @Test
  public void onlyOneMatchesFiltering() {
    when( spoon.filterMatch( eq( "2" ) ) ).thenReturn( true );
    AbstractMeta meta = prepareMetaWithThreeDbs();

    callRefreshWith( meta );
    verifyNumberOfNodesCreated( 2 );
  }


  private static AbstractMeta prepareMetaWithThreeDbs() {
    AbstractMeta meta = mock( AbstractMeta.class );
    when( meta.getDatabaseNames() ).thenReturn( new String[] { "1", "2", "3" } );
    List<DatabaseMeta> dbs =
      asList( mockDatabaseMeta( "1" ), mockDatabaseMeta( "2" ), mockDatabaseMeta( "3" ) );
    when( meta.getDatabases() ).thenReturn( dbs );
    return meta;
  }

  private static DatabaseMeta mockDatabaseMeta( String name ) {
    DatabaseMeta mock = mock( DatabaseMeta.class );
    when( mock.getName() ).thenReturn( name );
    when( mock.getDisplayName() ).thenReturn( name );
    return mock;
  }
}
