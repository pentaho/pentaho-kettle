/*!
 * Copyright 2024 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.di.ui.spoon.tree;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.widget.tree.LeveledTreeNode;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.delegates.SpoonTreeLeveledSelection;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.components.XulMenuitem;

/**
 * This class provides the right click popup menu items for SlaveServers
 */
public class TreePopupMenuProvider {

  private static Class<?> PKG = Spoon.class;
  public void createSlaveServerMenuItems( XulDomContainer mainSpoonContainer, SpoonTreeLeveledSelection leveledSelection ) {
    Bowl currentBowl = Spoon.getInstance().getBowl();

    XulMenuitem moveProjectItem = (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById(  "slave-server-inst-move-project"  );
    XulMenuitem moveGlobalItem = (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById(  "slave-server-inst-move-global"  );
    if ( moveProjectItem != null && moveGlobalItem != null ) {
      if ( leveledSelection.getLevel() == LeveledTreeNode.LEVEL.PROJECT ) {
        moveGlobalItem.setVisible( true );
        moveProjectItem.setVisible( false );
      }

      if ( leveledSelection.getLevel() == LeveledTreeNode.LEVEL.GLOBAL ) {
        if ( currentBowl != DefaultBowl.getInstance() ) {
          moveProjectItem.setVisible( true );
          moveGlobalItem.setVisible( false );
        } else {
          moveProjectItem.setVisible( false );
          moveGlobalItem.setVisible( false );
        }
      }
      if ( leveledSelection.getLevel() == LeveledTreeNode.LEVEL.LOCAL ) {
        if ( currentBowl == DefaultBowl.getInstance() ) {
          moveGlobalItem.setVisible( true );
          moveProjectItem.setVisible( false );
        } else {
          moveGlobalItem.setVisible( true );
          moveProjectItem.setVisible( true );
        }
      }
    }
    // Copy
    XulMenuitem copyGlobalItem = (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById(  "slave-server-inst-copy-global"  );
    XulMenuitem copyProjectItem = (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById(  "slave-server-inst-copy-project"  );
    if ( copyGlobalItem != null && copyProjectItem != null ) {
      if ( leveledSelection.getLevel() == LeveledTreeNode.LEVEL.PROJECT ) {
        copyGlobalItem.setVisible( true );
        copyProjectItem.setVisible( false );
      }

      if ( leveledSelection.getLevel() == LeveledTreeNode.LEVEL.GLOBAL ) {
        if ( currentBowl != DefaultBowl.getInstance() ) {
          copyProjectItem.setVisible( true );
          copyGlobalItem.setVisible( false );
        } else {
          copyGlobalItem.setVisible( false );
          copyProjectItem.setVisible( false );
        }
      }
      if ( leveledSelection.getLevel() == LeveledTreeNode.LEVEL.LOCAL ) {
        if ( currentBowl == DefaultBowl.getInstance() ) {
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
    XulMenuitem dupItem = (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById( "slave-server-inst-duplicate" );
    if ( dupItem != null ) {
      if ( leveledSelection.getLevel() == LeveledTreeNode.LEVEL.PROJECT ) {
        dupItem.setVisible( true );
        dupItem.setLabel( BaseMessages.getString( PKG, "Spoon.Menu.Popup.SLAVE_SERVER.DuplicateInProject" ) );
      }

      if ( leveledSelection.getLevel() == LeveledTreeNode.LEVEL.GLOBAL ) {
        dupItem.setVisible( true );
        dupItem.setLabel( BaseMessages.getString( PKG, "Spoon.Menu.Popup.SLAVE_SERVER.DuplicateInGlobal" ) );
      }
      if ( leveledSelection.getLevel() == LeveledTreeNode.LEVEL.LOCAL ) {
        // If the connection is local, no need to show the menuitem
        dupItem.setVisible( false );
      }
    }
  }
}
