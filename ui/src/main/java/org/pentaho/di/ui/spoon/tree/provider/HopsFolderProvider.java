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

import org.eclipse.swt.graphics.Image;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.Spoon;

/**
 * Created by bmorrise on 6/28/18.
 */
public class HopsFolderProvider extends AutomaticTreeFolderProvider {

  private static Class<?> PKG = Spoon.class;
  public static final String STRING_HOPS = BaseMessages.getString( PKG, "Spoon.STRING_HOPS" );

  private GUIResource guiResource;

  public HopsFolderProvider( GUIResource guiResource ) {
    this.guiResource = guiResource;
  }

  public HopsFolderProvider() {
    this( GUIResource.getInstance() );
  }

  @Override
  public void refresh( AbstractMeta meta, TreeNode treeNode, String filter ) {

    TransMeta transMeta = (TransMeta) meta;
    // Put the steps below it.
    for ( int i = 0; i < transMeta.nrTransHops(); i++ ) {
      TransHopMeta hopMeta = transMeta.getTransHop( i );

      if ( !filterMatch( hopMeta.toString(), filter ) ) {
        continue;
      }

      Image icon = hopMeta.isEnabled() ? guiResource.getImageHopTree() : guiResource.getImageDisabledHopTree();
      createTreeNode( treeNode, hopMeta.toString(), icon );
    }
  }

  @Override
  public String getTitle() {
    return STRING_HOPS;
  }

  @Override
  public Class getType() {
    return TransHopMeta.class;
  }
}
