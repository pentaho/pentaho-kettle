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


package org.pentaho.di.ui.core.widget;

import java.util.Set;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

public class TreeUtil {
  public static final void setOptimalWidthOnColumns( Tree tree ) {
    int nrCols = tree.getColumnCount();
    int[] max = new int[nrCols];
    Image image = new Image( tree.getDisplay(), 10, 10 );
    GC gc = new GC( image );

    for ( int i = 0; i < max.length; i++ ) {
      TreeColumn treeColumn = tree.getColumn( i );
      Point point = gc.textExtent( treeColumn.getText() );
      max[i] = point.x;
    }

    getMaxWidths( tree.getItems(), max, gc );

    gc.dispose();
    image.dispose();

    for ( int i = 0; i < max.length; i++ ) {
      TreeColumn treeColumn = tree.getColumn( i );
      treeColumn.setWidth( max[i] + 30 );
    }
  }

  private static final void getMaxWidths( TreeItem[] items, int[] max, GC gc ) {
    for ( int i = 0; i < items.length; i++ ) {
      for ( int c = 0; c < max.length; c++ ) {
        String string = items[i].getText( c );
        Point point = gc.textExtent( string );
        if ( point.x > max[c] ) {
          max[c] = point.x;
        }
      }
      getMaxWidths( items[i].getItems(), max, gc );
    }
  }

  public static final TreeItem findTreeItem( Tree tree, String[] path ) {
    TreeItem[] items = tree.getItems();
    for ( int i = 0; i < items.length; i++ ) {
      TreeItem treeItem = findTreeItem( items[i], path, 0 );
      if ( treeItem != null ) {
        return treeItem;
      }
    }
    return null;
  }

  private static final TreeItem findTreeItem( TreeItem treeItem, String[] path, int level ) {
    if ( treeItem.getText().equals( path[level] ) ) {
      if ( level == path.length - 1 ) {
        return treeItem;
      }

      TreeItem[] items = treeItem.getItems();
      for ( int i = 0; i < items.length; i++ ) {
        TreeItem found = findTreeItem( items[i], path, level + 1 );
        if ( found != null ) {
          return found;
        }
      }
    }
    return null;
  }

  /**
   * Finds the first name of the form "baseName + ' ' + number" that is not in existingNames
   *
   *
   * @param baseName name to append to
   * @param existingNames set of existing names
   *
   * @return String new name
   */
  public static String findUniqueSuffix( String baseName, Set<String> existingNames ) {
    int nr = 1;
    String newName = baseName + " " + nr;
    while ( existingNames.contains( newName ) ) {
      nr++;
      newName = baseName + " " + nr;
    }
    return newName;
  }

}
