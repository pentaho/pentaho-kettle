package org.pentaho.di.connections.ui.tree;

import org.pentaho.di.connections.vfs.VFSConnectionDetails;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.ui.spoon.SelectionTreeExtension;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.vfs.connections.ui.dialog.ConnectionDelegate;

import org.junit.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConnectionViewTreeExtensionTest {

  @Test
  public void createNew_DoesNotOpenDialog_WhenNotAllowedInGlobalContext() throws Exception {
    Spoon spoon = mock( Spoon.class );
    Bowl bowl = mock( Bowl.class );
    ConnectionDelegate connectionDelegate = mock( ConnectionDelegate.class );

    when( spoon.isAllowedManageGlobalVFS() ).thenReturn( false );
    when( spoon.getManagementBowl() ).thenReturn( bowl );
    when( spoon.getGlobalManagementBowl() ).thenReturn( bowl );

    SelectionTreeExtension selection =
      new SelectionTreeExtension( null, VFSConnectionDetails.class, Spoon.CREATE_NEW_SELECTION_EXTENSION );

    try ( MockedStatic<Spoon> spoonMockedStatic = mockStatic( Spoon.class );
          MockedStatic<ConnectionDelegate> connectionDelegateMockedStatic = mockStatic( ConnectionDelegate.class ) ) {

      spoonMockedStatic.when( Spoon::getInstance ).thenReturn( spoon );
      connectionDelegateMockedStatic.when( ConnectionDelegate::getInstance ).thenReturn( connectionDelegate );

      ConnectionViewTreeExtension extension = new ConnectionViewTreeExtension();
      extension.callExtensionPoint( null, selection );

      verify( connectionDelegate, never() ).openDialog();
    }
  }

  @Test
  public void createNew_OpensDialog_WhenAllowedInGlobalContext() throws Exception {
    Spoon spoon = mock( Spoon.class );
    Bowl bowl = mock( Bowl.class );
    ConnectionDelegate connectionDelegate = mock( ConnectionDelegate.class );

    when( spoon.isAllowedManageGlobalVFS() ).thenReturn( true );
    when( spoon.getManagementBowl() ).thenReturn( bowl );
    when( spoon.getGlobalManagementBowl() ).thenReturn( bowl );

    SelectionTreeExtension selection =
      new SelectionTreeExtension( null, VFSConnectionDetails.class, Spoon.CREATE_NEW_SELECTION_EXTENSION );

    try ( MockedStatic<Spoon> spoonMockedStatic = mockStatic( Spoon.class );
          MockedStatic<ConnectionDelegate> connectionDelegateMockedStatic = mockStatic( ConnectionDelegate.class ) ) {

      spoonMockedStatic.when( Spoon::getInstance ).thenReturn( spoon );
      connectionDelegateMockedStatic.when( ConnectionDelegate::getInstance ).thenReturn( connectionDelegate );

      ConnectionViewTreeExtension extension = new ConnectionViewTreeExtension();
      extension.callExtensionPoint( null, selection );

      verify( connectionDelegate ).openDialog();
    }
  }

  @Test
  public void createNew_OpensDialog_WhenInProjectContextWithoutGlobalPermission() throws Exception {
    Spoon spoon = mock( Spoon.class );
    Bowl managementBowl = mock( Bowl.class );
    Bowl globalBowl = mock( Bowl.class );
    ConnectionDelegate connectionDelegate = mock( ConnectionDelegate.class );

    when( spoon.isAllowedManageGlobalVFS() ).thenReturn( false );
    when( spoon.getManagementBowl() ).thenReturn( managementBowl );
    when( spoon.getGlobalManagementBowl() ).thenReturn( globalBowl );

    SelectionTreeExtension selection =
      new SelectionTreeExtension( null, VFSConnectionDetails.class, Spoon.CREATE_NEW_SELECTION_EXTENSION );

    try ( MockedStatic<Spoon> spoonMockedStatic = mockStatic( Spoon.class );
          MockedStatic<ConnectionDelegate> connectionDelegateMockedStatic = mockStatic( ConnectionDelegate.class ) ) {

      spoonMockedStatic.when( Spoon::getInstance ).thenReturn( spoon );
      connectionDelegateMockedStatic.when( ConnectionDelegate::getInstance ).thenReturn( connectionDelegate );

      ConnectionViewTreeExtension extension = new ConnectionViewTreeExtension();
      extension.callExtensionPoint( null, selection );

      verify( connectionDelegate ).openDialog();
    }
  }
}
