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


package org.pentaho.di.ui.spoon.tree;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.widget.tree.LeveledTreeNode;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.delegates.SpoonTreeLeveledSelection;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.components.XulMenuitem;

/**
 * This class provides the right click popup menu items for Shared Objects. This relies on a naming convention in
 * menubar.xml
 */
public class TreePopupMenuProvider {

  private static Class<?> PKG = Spoon.class;

  public void createSharedObjectMenuItems( XulDomContainer mainSpoonContainer,
      SpoonTreeLeveledSelection leveledSelection, String menubarPrefix ) {
    Bowl currentBowl = Spoon.getInstance().getBowl();
    Bowl globalBowl = Spoon.getInstance().getGlobalManagementBowl();

    XulMenuitem moveProjectItem =
      (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById( menubarPrefix + "-inst-move-project"  );
    XulMenuitem moveGlobalItem =
      (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById( menubarPrefix + "-inst-move-global"  );

    if ( moveProjectItem != null && moveGlobalItem != null ) {
      if ( leveledSelection.getLevel() == LeveledTreeNode.LEVEL.PROJECT ) {
        moveGlobalItem.setVisible( true );
        moveProjectItem.setVisible( false );
      }

      if ( leveledSelection.getLevel() == LeveledTreeNode.LEVEL.GLOBAL ) {
        if ( currentBowl != globalBowl ) {
          moveProjectItem.setVisible( true );
          moveGlobalItem.setVisible( false );
        } else {
          moveProjectItem.setVisible( false );
          moveGlobalItem.setVisible( false );
        }
      }
      if ( leveledSelection.getLevel() == LeveledTreeNode.LEVEL.LOCAL ) {
        if ( currentBowl == globalBowl ) {
          moveGlobalItem.setVisible( true );
          moveProjectItem.setVisible( false );
        } else {
          moveGlobalItem.setVisible( true );
          moveProjectItem.setVisible( true );
        }
      }
    }
    // Copy
    XulMenuitem copyGlobalItem =
      (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById(  menubarPrefix + "-inst-copy-global"  );
    XulMenuitem copyProjectItem =
      (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById(  menubarPrefix + "-inst-copy-project"  );
    if ( copyGlobalItem != null && copyProjectItem != null ) {
      if ( leveledSelection.getLevel() == LeveledTreeNode.LEVEL.PROJECT ) {
        copyGlobalItem.setVisible( true );
        copyProjectItem.setVisible( false );
      }

      if ( leveledSelection.getLevel() == LeveledTreeNode.LEVEL.GLOBAL ) {
        if ( currentBowl != globalBowl ) {
          copyProjectItem.setVisible( true );
          copyGlobalItem.setVisible( false );
        } else {
          copyGlobalItem.setVisible( false );
          copyProjectItem.setVisible( false );
        }
      }
      if ( leveledSelection.getLevel() == LeveledTreeNode.LEVEL.LOCAL ) {
        if ( currentBowl == globalBowl ) {
          copyGlobalItem.setVisible( true );
          copyProjectItem.setVisible( false );
        } else {
          // If the connection is global, no need to show the menuitem
          copyGlobalItem.setVisible( true );
          copyProjectItem.setVisible( true );
        }
      }
    }
    // duplicate
    XulMenuitem dupGlobalItem =
      (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById( menubarPrefix + "-inst-duplicate-global" );
    XulMenuitem dupProjectItem =
      (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById( menubarPrefix + "-inst-duplicate-project" );
    if ( dupGlobalItem != null && dupProjectItem != null ) {
      if ( leveledSelection.getLevel() == LeveledTreeNode.LEVEL.PROJECT ) {
        dupGlobalItem.setVisible( false );
        dupProjectItem.setVisible( true );
      }

      if ( leveledSelection.getLevel() == LeveledTreeNode.LEVEL.GLOBAL ) {
        dupGlobalItem.setVisible( true );
        dupProjectItem.setVisible( false );
      }
    }
  }
}
