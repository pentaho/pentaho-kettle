package org.pentaho.di.connections.ui.tree;

import org.pentaho.di.connections.vfs.VFSConnectionDetails;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.ui.core.widget.tree.LeveledTreeNode;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TreeSelection;
import org.pentaho.di.vfs.connections.ui.dialog.ConnectionDelegate;

import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class ConnectionPopupMenuExtensionTest {

  @Test
  public void rootPopupMenu_DoesNotAddNew_WhenNotAllowedInGlobalContext() throws Exception {
    Spoon spoon = mock( Spoon.class );
    Tree tree = mock( Tree.class );
    Bowl bowl = mock( Bowl.class );
    ConnectionDelegate connectionDelegate = mock( ConnectionDelegate.class );

    TreeSelection[] treeSelections = new TreeSelection[] { new TreeSelection( "connections", VFSConnectionDetails.class ) };

    when( spoon.getTreeObjects( tree ) ).thenReturn( treeSelections );
    when( spoon.isAllowedManageGlobalVFS() ).thenReturn( false );
    when( spoon.getManagementBowl() ).thenReturn( bowl );
    when( spoon.getGlobalManagementBowl() ).thenReturn( bowl );

    try ( MockedStatic<Spoon> spoonMockedStatic = mockStatic( Spoon.class );
          MockedStatic<ConnectionDelegate> connectionDelegateMockedStatic = mockStatic( ConnectionDelegate.class );
          MockedConstruction<Menu> ignoredMenuConstruction = mockConstruction( Menu.class );
          MockedConstruction<MenuItem> menuItemConstruction = mockConstruction( MenuItem.class ) ) {

      spoonMockedStatic.when( Spoon::getInstance ).thenReturn( spoon );
      connectionDelegateMockedStatic.when( ConnectionDelegate::getInstance ).thenReturn( connectionDelegate );

      ConnectionPopupMenuExtension extension = new ConnectionPopupMenuExtension();
      extension.callExtensionPoint( null, tree );

      assertEquals( 0, menuItemConstruction.constructed().size() );
    }
  }

  @Test
  public void rootPopupMenu_AddsNew_WhenInProjectContextWithoutGlobalPermission() throws Exception {
    Spoon spoon = mock( Spoon.class );
    Tree tree = mock( Tree.class );
    Bowl managementBowl = mock( Bowl.class );
    Bowl globalBowl = mock( Bowl.class );
    ConnectionDelegate connectionDelegate = mock( ConnectionDelegate.class );

    TreeSelection[] treeSelections = new TreeSelection[] { new TreeSelection( "connections", VFSConnectionDetails.class ) };

    when( spoon.getTreeObjects( tree ) ).thenReturn( treeSelections );
    when( spoon.isAllowedManageGlobalVFS() ).thenReturn( false );
    when( spoon.getManagementBowl() ).thenReturn( managementBowl );
    when( spoon.getGlobalManagementBowl() ).thenReturn( globalBowl );

    try ( MockedStatic<Spoon> spoonMockedStatic = mockStatic( Spoon.class );
          MockedStatic<ConnectionDelegate> connectionDelegateMockedStatic = mockStatic( ConnectionDelegate.class );
          MockedConstruction<Menu> ignoredMenuConstruction = mockConstruction( Menu.class );
          MockedConstruction<MenuItem> menuItemConstruction = mockConstruction( MenuItem.class ) ) {

      spoonMockedStatic.when( Spoon::getInstance ).thenReturn( spoon );
      connectionDelegateMockedStatic.when( ConnectionDelegate::getInstance ).thenReturn( connectionDelegate );

      ConnectionPopupMenuExtension extension = new ConnectionPopupMenuExtension();
      extension.callExtensionPoint( null, tree );

      assertEquals( 1, menuItemConstruction.constructed().size() );
    }
  }

  @Test
  public void itemPopupMenu_ProjectLevel_DoesNotAddMoveCopy_WhenNotAllowed() throws Exception {
    Spoon spoon = mock( Spoon.class );
    Tree tree = mock( Tree.class );
    ConnectionDelegate connectionDelegate = mock( ConnectionDelegate.class );

    ConnectionTreeItem treeItem = new ConnectionTreeItem( "project-conn", LeveledTreeNode.LEVEL.PROJECT );
    TreeSelection[] treeSelections = new TreeSelection[] { new TreeSelection( "project-conn", treeItem ) };

    when( spoon.getTreeObjects( tree ) ).thenReturn( treeSelections );
    when( spoon.isAllowedManageGlobalVFS() ).thenReturn( false );

    try ( MockedStatic<Spoon> spoonMockedStatic = mockStatic( Spoon.class );
          MockedStatic<ConnectionDelegate> connectionDelegateMockedStatic = mockStatic( ConnectionDelegate.class );
          MockedConstruction<Menu> menuConstruction = mockConstruction( Menu.class,
            ( menuMock, context ) -> when( menuMock.getItems() ).thenReturn( new MenuItem[0] ) );
          MockedConstruction<MenuItem> menuItemConstruction = mockConstruction( MenuItem.class ) ) {

      spoonMockedStatic.when( Spoon::getInstance ).thenReturn( spoon );
      connectionDelegateMockedStatic.when( ConnectionDelegate::getInstance ).thenReturn( connectionDelegate );

      ConnectionPopupMenuExtension extension = new ConnectionPopupMenuExtension();
      extension.callExtensionPoint( null, tree );

      assertEquals( 3, menuItemConstruction.constructed().size() );
      assertEquals( 1, menuConstruction.constructed().size() );
    }
  }

  @Test
  public void itemPopupMenu_ProjectLevel_AddsMoveCopy_WhenAllowed() throws Exception {
    Spoon spoon = mock( Spoon.class );
    Tree tree = mock( Tree.class );
    Bowl globalBowl = mock( Bowl.class );
    ConnectionDelegate connectionDelegate = mock( ConnectionDelegate.class );

    ConnectionTreeItem treeItem = new ConnectionTreeItem( "project-conn", LeveledTreeNode.LEVEL.PROJECT );
    TreeSelection[] treeSelections = new TreeSelection[] { new TreeSelection( "project-conn", treeItem ) };

    when( spoon.getTreeObjects( tree ) ).thenReturn( treeSelections );
    when( spoon.isAllowedManageGlobalVFS() ).thenReturn( true );
    when( spoon.getGlobalManagementBowl() ).thenReturn( globalBowl );
    when( globalBowl.getLevelDisplayName() ).thenReturn( "Global" );

    try ( MockedStatic<Spoon> spoonMockedStatic = mockStatic( Spoon.class );
          MockedStatic<ConnectionDelegate> connectionDelegateMockedStatic = mockStatic( ConnectionDelegate.class );
          MockedConstruction<Menu> menuConstruction = mockConstruction( Menu.class,
            ( menuMock, context ) -> when( menuMock.getItems() ).thenReturn( new MenuItem[0] ) );
          MockedConstruction<MenuItem> menuItemConstruction = mockConstruction( MenuItem.class ) ) {

      spoonMockedStatic.when( Spoon::getInstance ).thenReturn( spoon );
      connectionDelegateMockedStatic.when( ConnectionDelegate::getInstance ).thenReturn( connectionDelegate );

      ConnectionPopupMenuExtension extension = new ConnectionPopupMenuExtension();
      extension.callExtensionPoint( null, tree );

      assertEquals( 5, menuItemConstruction.constructed().size() );
      assertEquals( 1, menuConstruction.constructed().size() );
    }
  }
}
