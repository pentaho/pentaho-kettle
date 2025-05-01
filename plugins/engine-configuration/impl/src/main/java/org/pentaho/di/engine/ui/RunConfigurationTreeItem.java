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


package org.pentaho.di.engine.ui;

import org.pentaho.di.ui.core.widget.tree.LeveledTreeNode;

/**
 * Tuple of Name and Level for a selected RunConfiguration in the tree.
 */
public class RunConfigurationTreeItem {
  private String name;
  private LeveledTreeNode.LEVEL level;

  public RunConfigurationTreeItem( String name, LeveledTreeNode.LEVEL level ) {
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
