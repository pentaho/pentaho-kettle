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


package org.pentaho.di.ui.spoon.delegates;
import org.pentaho.di.ui.core.widget.tree.LeveledTreeNode;
import java.util.Objects;

/**
 * Data class that holds information about a selected node in the tree for specific types of objects.
 *
 */
public class SpoonTreeLeveledSelection {

  private final String type;
  private final String name;
  private final LeveledTreeNode.LEVEL level;

  public SpoonTreeLeveledSelection( String type, String name, LeveledTreeNode.LEVEL level ) {
    this.type = Objects.requireNonNull( type );
    this.name = Objects.requireNonNull( name );
    this.level = Objects.requireNonNull( level );
  }


  public String getType() {
    return this.type;
  }

  public String getName() {
    return this.name;
  }

  public LeveledTreeNode.LEVEL getLevel() {
    return this.level;
  }

}
