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
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.spoon.Spoon;

/**
 * A TreeNode for the left-hand config tree that includes a Level for management. Levels represent where the config is
 * stored, and a priority for how the config is applied. This Level concept is used in the UI to abstract the equivalent
 * logic in back-end code.
 *
 */
public class LeveledTreeNode extends TreeNode {
  public enum LEVEL {
    /** The Default Level is for built-in objects that cannot be managed or deleted by Users. E.G. the "Pentaho
     *  Local" Run Configuration */
    DEFAULT( 1 ),
    /** The Project Level is for config objects that are contained in the current Project. */
    PROJECT( 2 ),
    /** The Global Level represents exactly one of the System (DefaultBowl) or Repository Levels, depending on whether
     *  we are connected to a Repository. */
    GLOBAL( 3 ),
    /** The File Level is for shared objects defined inside the xml of a Transformation or Job. This level is mostly
     *  deprecated. New Objects cannot be created at this Level. */
    FILE( 4 );


    // will this need to be localized?
    private final int displayOrder;

    LEVEL( int displayOrder  ) {
      this.displayOrder = displayOrder;
    }

    public int getDisplayOrder() {
      return displayOrder;
    }
  }

  private static Class<?> PKG = Spoon.class;  // for i18n purposes, needed by Translator2!!
  private static final String NAME_KEY = "name";
  private static final String LEVEL_KEY = "level";
  public static final String LEVEL_FILE_DISPLAY_NAME = BaseMessages.getString( PKG, "Spoon.Tree.Level.File.Name" );
  public static final String LEVEL_DEFAULT_DISPLAY_NAME = BaseMessages.getString( PKG, "Spoon.Tree.Level.Default.Name" );

  //public LeveledTreeNode( String name, LEVEL level, boolean overridden ) {
  public LeveledTreeNode( String name, LEVEL level, String levelDisplayName, boolean overridden ) {
    setData( NAME_KEY, name );
    setData( LEVEL_KEY, level );
    setLabel( name + " [" + levelDisplayName + "]" );
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
              .thenComparing( t ->((LEVEL) t.getData().get( LEVEL_KEY ) ).getDisplayOrder() );
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

