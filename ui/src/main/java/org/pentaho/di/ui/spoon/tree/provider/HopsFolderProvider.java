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
