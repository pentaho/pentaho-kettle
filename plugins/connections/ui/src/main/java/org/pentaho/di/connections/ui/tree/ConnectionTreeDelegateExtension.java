/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.ui.tree;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.ui.spoon.TreeSelection;
import org.pentaho.di.ui.spoon.delegates.SpoonTreeDelegateExtension;
import org.pentaho.di.connections.vfs.VFSConnectionDetails;

import java.util.List;

@ExtensionPoint( id = "VFSConnectionTreeDelegateExtension", description = "",
  extensionPointId = "SpoonTreeDelegateExtension" )
public class ConnectionTreeDelegateExtension implements ExtensionPointInterface {

  public ConnectionTreeDelegateExtension() {

  }

  @Override public void callExtensionPoint( LogChannelInterface log, Object extension ) throws KettleException {
    SpoonTreeDelegateExtension treeDelExt = (SpoonTreeDelegateExtension) extension;

    int caseNumber = treeDelExt.getCaseNumber();
    AbstractMeta meta = treeDelExt.getTransMeta();
    String[] path = treeDelExt.getPath();
    List<TreeSelection> objects = treeDelExt.getObjects();

    TreeSelection object = null;

    if ( path[ 2 ].equals( ConnectionFolderProvider.STRING_VFS_CONNECTIONS ) ) {
      switch ( caseNumber ) {
        case 3:
          object = new TreeSelection( path[ 2 ], VFSConnectionDetails.class, meta );
          break;
        case 4:
          try {
            final String name = path[ 3 ];
            object = new TreeSelection( name, new ConnectionTreeItem( name ), meta );
          } catch ( Exception e ) {
            // Do Nothing
          }
          break;
      }
    }

    if ( object != null ) {
      objects.add( object );
    }
  }
}

