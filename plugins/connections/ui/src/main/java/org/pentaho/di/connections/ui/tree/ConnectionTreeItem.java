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


package org.pentaho.di.connections.ui.tree;

import org.pentaho.di.ui.core.widget.tree.LeveledTreeNode;

/**
 * Created by bmorrise on 2/4/19.
 */
public class ConnectionTreeItem {
  private String name;
  private LeveledTreeNode.LEVEL level;

  public ConnectionTreeItem( String name, LeveledTreeNode.LEVEL level ) {
    this.name = name;
    this.level = level;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public LeveledTreeNode.LEVEL getLevel() {
    return level;
  }

  public void setLevel( LeveledTreeNode.LEVEL level ) {
    this.level = level;
  }
}
