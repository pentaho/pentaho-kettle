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
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.tree.TreeFolderProvider;

/**
 * Created by bmorrise on 7/9/18.
 */
public abstract class AutomaticTreeFolderProvider extends TreeFolderProvider {

  @Override
  public void checkUpdate( AbstractMeta meta, TreeNode treeNode, String filter ) {
    treeNode.removeAll();
    refresh( meta, treeNode, filter );
  }
}
