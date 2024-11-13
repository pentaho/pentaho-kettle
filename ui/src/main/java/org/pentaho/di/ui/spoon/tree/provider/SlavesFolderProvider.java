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


package org.pentaho.di.ui.spoon.tree.provider;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.tree.TreeFolderProvider;

import java.util.List;

/**
 * Created by bmorrise on 6/28/18.
 */
public class SlavesFolderProvider extends TreeFolderProvider {

  private static Class<?> PKG = Spoon.class;
  public static final String STRING_SLAVES = BaseMessages.getString( PKG, "Spoon.STRING_SLAVES" );
  private GUIResource guiResource;

  public SlavesFolderProvider( GUIResource guiResource ) {
    this.guiResource = guiResource;
  }

  public SlavesFolderProvider() {
    this( GUIResource.getInstance() );
  }

  @Override
  public void refresh( AbstractMeta meta, TreeNode treeNode, String filter ) {
    List<SlaveServer> servers = meta.getSlaveServers();

    servers.sort( ( s1, s2 ) -> String.CASE_INSENSITIVE_ORDER.compare( s1.getName(), s2.getName() ) );

    for ( SlaveServer slaveServer : servers ) {
      if ( !filterMatch( slaveServer.getName(), filter ) ) {
        continue;
      }

      TreeNode childTreeNode = createTreeNode( treeNode, slaveServer.getName(), guiResource.getImageSlaveTree() );
      if ( slaveServer.isShared() ) {
        childTreeNode.setFont( guiResource.getFontBold() );
      }

    }
  }

  @Override
  public String getTitle() {
    return STRING_SLAVES;
  }

  @Override
  public Class getType() {
    return SlaveServer.class;
  }
}
