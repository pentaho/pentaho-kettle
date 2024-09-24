/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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
