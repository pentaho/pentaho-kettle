/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.ui.spoon.tree.provider;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.tree.TreeFolderProvider;

/**
 * Created by bmorrise on 6/28/18.
 */
public class ClustersFolderProvider extends TreeFolderProvider {

  private static Class<?> PKG = Spoon.class;
  public static final String STRING_CLUSTERS = BaseMessages.getString( PKG, "Spoon.STRING_CLUSTERS" );

  private GUIResource guiResource;

  public ClustersFolderProvider( GUIResource guiResource ) {
    this.guiResource = guiResource;
  }

  public ClustersFolderProvider() {
    this( GUIResource.getInstance() );
  }

  @Override
  public void refresh( AbstractMeta meta, TreeNode treeNode, String filter ) {
    TransMeta transMeta = (TransMeta) meta;
    for ( ClusterSchema clusterSchema : transMeta.getClusterSchemas() ) {
      if ( !filterMatch( clusterSchema.getName(), filter ) ) {
        continue;
      }
      TreeNode childTreeNode = createTreeNode( treeNode, clusterSchema.toString(), guiResource.getImageClusterMedium() );
      if ( clusterSchema.isShared() ) {
        childTreeNode.setFont( GUIResource.getInstance().getFontBold() );
      }
    }
  }

  @Override
  public String getTitle() {
    return STRING_CLUSTERS;
  }

  @Override
  public Class getType() {
    return ClusterSchema.class;
  }
}
