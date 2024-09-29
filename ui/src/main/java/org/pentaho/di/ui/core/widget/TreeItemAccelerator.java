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


package org.pentaho.di.ui.core.widget;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.ui.core.ConstUI;

/**
 * This class can be used to define accelerators (actions) to a tree item that just got created.
 *
 * @author Matt
 *
 */
public class TreeItemAccelerator {
  public static final void addDoubleClick( final TreeItem treeItem, final DoubleClickInterface doubleClick ) {
    final String[] path1 = ConstUI.getTreeStrings( treeItem );
    final Tree tree = treeItem.getParent();

    if ( doubleClick != null ) {
      final SelectionAdapter selectionAdapter = new SelectionAdapter() {
        public void widgetDefaultSelected( SelectionEvent selectionEvent ) {
          TreeItem[] items = tree.getSelection();
          for ( int i = 0; i < items.length; i++ ) {
            String[] path2 = ConstUI.getTreeStrings( items[i] );
            if ( equalPaths( path1, path2 ) ) {
              doubleClick.action( treeItem );
            }
          }
        }
      };
      tree.addSelectionListener( selectionAdapter );

      // Clean up when we do a refresh too.
      treeItem.addDisposeListener( new DisposeListener() {
        public void widgetDisposed( DisposeEvent disposeEvent ) {
          tree.removeSelectionListener( selectionAdapter );
        }
      } );
    }
  }

  public static final boolean equalPaths( String[] path1, String[] path2 ) {
    if ( path1 == null || path2 == null ) {
      return false;
    }
    if ( path1.length != path2.length ) {
      return false;
    }

    for ( int i = 0; i < path1.length; i++ ) {
      if ( !path1[i].equals( path2[i] ) ) {
        return false;
      }
    }
    return true;
  }
}
