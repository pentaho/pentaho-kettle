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


package org.pentaho.di.ui.core.widget.tree;

import java.util.Comparator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

public class LeveledTreeNode extends TreeNode {
  public enum LEVEL {
    DEFAULT( "Default" ),
    PROJECT( "Project" ),
    GLOBAL( "Global" ),
    LOCAL( "Local" );

    // will this need to be localized?
    private final String name;

    LEVEL( String name ) {
      this.name = name;
    }
    public String getName() {
      return name;
    }
  }

  private static final String NAME_KEY = "name";
  private static final String LEVEL_KEY = "level";

  public LeveledTreeNode( String name, LEVEL level, boolean overridden ) {
    setData( NAME_KEY, name );
    setData( LEVEL_KEY, level );
    setLabel( name + " [" + level.getName() + "]" );
    if ( overridden ) {
      setForeground( getDisabledColor() );
    }
  }

  // first DEFAULT items, then alphabetical, then by level
  public Comparator<TreeNode> COMPARATOR =
    Comparator.<TreeNode, LEVEL>comparing( t -> (LEVEL)t.getData().get( LEVEL_KEY ),
                                            ( o1, o2 ) -> {
    if ( o1 == LEVEL.DEFAULT ) {
      return -1;
    } else if ( o2 == LEVEL.DEFAULT ) {
      return 1;
    }
    return 0;
  } )

              .thenComparing( t -> (String)t.getData().get( NAME_KEY ),
                              String.CASE_INSENSITIVE_ORDER )
              .thenComparing( t -> (LEVEL)t.getData().get( LEVEL_KEY ) );
  @Override
  public int compareTo( TreeNode other ) {
    return COMPARATOR.compare( this, other );
  }

  private Color getDisabledColor() {
    Device device = Display.getCurrent();
    return new Color( device, 188, 188, 188 );
  }

  public static String getName( TreeItem treeItem ) {
    return (String) treeItem.getData( NAME_KEY );
  }

  public static LEVEL getLevel( TreeItem treeItem ) {
    return (LeveledTreeNode.LEVEL) treeItem.getData( LeveledTreeNode.LEVEL_KEY );
  }


}

