/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * A TreeEditor is a manager for a Control that appears above a cell in a Tree
 * and tracks with the moving and resizing of that cell. It can be used to
 * display a text widget above a cell in a Tree so that the user can edit the
 * contents of that cell. It can also be used to display a button that can
 * launch a dialog for modifying the contents of the associated cell.
 * <p>
 * Here is an example of using a TreeEditor: <code><pre>
 * 	final Tree tree = new Tree(shell, SWT.BORDER);
 * 	for (int i = 0; i &lt; 3; i++) {
 * 		TreeItem item = new TreeItem(tree, SWT.NONE);
 * 		item.setText("item " + i);
 * 		for (int j = 0; j &lt; 3; j++) {
 * 			TreeItem subItem = new TreeItem(item, SWT.NONE);
 * 			subItem.setText("item " + i + " " + j);
 * 		}
 * 	}
 *
 * 	final TreeEditor editor = new TreeEditor(tree);
 * 	//The editor must have the same size as the cell and must
 * 	//not be any smaller than 50 pixels.
 * 	editor.horizontalAlignment = SWT.LEFT;
 * 	editor.grabHorizontal = true;
 * 	editor.minimumWidth = 50;
 *
 * 	tree.addSelectionListener(new SelectionAdapter() {
 * 		public void widgetSelected(SelectionEvent e) {
 * 			// Clean up any previous editor control
 * 			Control oldEditor = editor.getEditor();
 * 			if (oldEditor != null) oldEditor.dispose();
 *
 * 			// Identify the selected row
 * 			TreeItem item = (TreeItem)e.item;
 * 			if (item == null) return;
 *
 * 			// The control that will be the editor must be a child of the Tree
 * 			Text newEditor = new Text(tree, SWT.NONE);
 * 			newEditor.setText(item.getText());
 * 			newEditor.addModifyListener(new ModifyListener() {
 * 				public void modifyText(ModifyEvent e) {
 * 					Text text = (Text)editor.getEditor();
 * 					editor.getItem().setText(text.getText());
 * 				}
 * 			});
 * 			newEditor.selectAll();
 * 			newEditor.setFocus();
 * 			editor.setEditor(newEditor, item);
 * 		}
 * 	});
 * </pre></code>
 *
 * @since 1.0
 */
public class TreeEditor extends ControlEditor {

  Tree tree;
  TreeItem item;
  int column = 0;
  ControlListener columnListener;
  TreeListener treeListener;
  Runnable timer;
  // [if] Reduce TIMEOUT in order to execute the layout timer in the same request (see bug 364802)
  static final int TIMEOUT = 0; // 1500;

  /**
   * Creates a TreeEditor for the specified Tree.
   *
   * @param tree the Tree Control above which this editor will be displayed
   */
  public TreeEditor( Tree tree ) {
    super( tree );
    this.tree = tree;
    columnListener = new ControlListener() {

      public void controlMoved( ControlEvent e ) {
        layout();
      }

      public void controlResized( ControlEvent e ) {
        layout();
      }
    };
    timer = new SerializableRunnable() {

      public void run() {
        layout();
      }
    };
    treeListener = new TreeListener() {

      final Runnable runnable = new SerializableRunnable() {

        public void run() {
          if( editor == null || editor.isDisposed() )
            return;
          if( TreeEditor.this.tree.isDisposed() )
            return;
          layout();
          editor.setVisible( true );
        }
      };

      public void treeCollapsed( TreeEvent e ) {
        if( editor == null || editor.isDisposed() )
          return;
        editor.setVisible( false );
        e.widget.getDisplay().asyncExec( runnable );
      }

      public void treeExpanded( TreeEvent e ) {
        if( editor == null || editor.isDisposed() )
          return;
        editor.setVisible( false );
        e.widget.getDisplay().asyncExec( runnable );
      }
    };
    tree.addTreeListener( treeListener );
    // To be consistent with older versions of SWT, grabVertical defaults to
    // true
    grabVertical = true;
  }

  Rectangle computeBounds() {
    if( item == null || column == -1 || item.isDisposed() )
      return new Rectangle( 0, 0, 0, 0 );
    Rectangle cell = item.getBounds( column );
    Rectangle rect = item.getImageBounds( column );
    // [if] Fix cell editor position when cell padding is set on the tree.
    // See TableEditor and bug 269065
    if( rect.width > 0 ) {
      cell.width -= rect.width + rect.x - cell.x;
      cell.x = rect.x + rect.width;
    }
    Rectangle area = tree.getClientArea();
    if( cell.x < area.x + area.width ) {
      if( cell.x + cell.width > area.x + area.width ) {
        cell.width = area.x + area.width - cell.x;
      }
    }
    Rectangle editorRect = new Rectangle( cell.x,
                                          cell.y,
                                          minimumWidth,
                                          minimumHeight );
    if( grabHorizontal ) {
      if( tree.getColumnCount() == 0 ) {
        // Bounds of tree item only include the text area - stretch out to
        // include
        // entire client area
        cell.width = area.x + area.width - cell.x;
      }
      editorRect.width = Math.max( cell.width, minimumWidth );
    }
    if( grabVertical ) {
      editorRect.height = Math.max( cell.height, minimumHeight );
    }
    if( horizontalAlignment == SWT.RIGHT ) {
      editorRect.x += cell.width - editorRect.width;
    } else if( horizontalAlignment == SWT.LEFT ) {
      // do nothing - cell.x is the right answer
    } else { // default is CENTER
      editorRect.x += ( cell.width - editorRect.width ) / 2;
    }
    // don't let the editor overlap with the +/- of the tree
    editorRect.x = Math.max( cell.x, editorRect.x );
    if( verticalAlignment == SWT.BOTTOM ) {
      editorRect.y += cell.height - editorRect.height;
    } else if( verticalAlignment == SWT.TOP ) {
      // do nothing - cell.y is the right answer
    } else { // default is CENTER
      editorRect.y += ( cell.height - editorRect.height ) / 2;
    }
    return editorRect;
  }

  /**
   * Removes all associations between the TreeEditor and the row in the tree.
   * The tree and the editor Control are <b>not</b> disposed.
   */
  public void dispose() {
    if( tree != null && !tree.isDisposed() ) {
      if( this.column > -1 && this.column < tree.getColumnCount() ) {
        TreeColumn treeColumn = tree.getColumn( this.column );
        treeColumn.removeControlListener( columnListener );
      }
      if( treeListener != null )
        tree.removeTreeListener( treeListener );
    }
    columnListener = null;
    treeListener = null;
    tree = null;
    item = null;
    column = 0;
    timer = null;
    super.dispose();
  }

  /**
   * Returns the zero based index of the column of the cell being tracked by
   * this editor.
   *
   * @return the zero based index of the column of the cell being tracked by
   *         this editor
   */
  public int getColumn() {
    return column;
  }

  /**
   * Returns the TreeItem for the row of the cell being tracked by this editor.
   *
   * @return the TreeItem for the row of the cell being tracked by this editor
   */
  public TreeItem getItem() {
    return item;
  }

  void resize() {
    layout();
    /*
     * On some platforms, the table scrolls when an item that is partially
     * visible at the bottom of the table is selected. Ensure that the correct
     * row is edited by laying out one more time in a timerExec().
     */
    if (tree != null) {
      Display display = tree.getDisplay();
      display.timerExec(-1, timer);
      display.timerExec(TIMEOUT, timer);
    }
  }

  /**
   * Sets the zero based index of the column of the cell being tracked by this
   * editor.
   *
   * @param column the zero based index of the column of the cell being tracked
   *          by this editor
   */
  public void setColumn( int column ) {
    int columnCount = tree.getColumnCount();
    // Separately handle the case where the tree has no TreeColumns.
    // In this situation, there is a single default column.
    if( columnCount == 0 ) {
      this.column = ( column == 0 ) ? 0 : -1;
      resize();
      return;
    }
    if( this.column > -1 && this.column < columnCount ) {
      TreeColumn treeColumn = tree.getColumn( this.column );
      treeColumn.removeControlListener( columnListener );
      this.column = -1;
    }
    if( column < 0 || column >= tree.getColumnCount() )
      return;
    this.column = column;
    TreeColumn treeColumn = tree.getColumn( this.column );
    treeColumn.addControlListener( columnListener );
    resize();
  }

  public void setItem( TreeItem item ) {
    this.item = item;
    resize();
  }

  /**
   * Specify the Control that is to be displayed and the cell in the tree that
   * it is to be positioned above.
   * <p>
   * Note: The Control provided as the editor <b>must</b> be created with its
   * parent being the Tree control specified in the TreeEditor constructor.
   *
   * @param editor the Control that is displayed above the cell being edited
   * @param item the TreeItem for the row of the cell being tracked by this
   *          editor
   * @param column the zero based index of the column of the cell being tracked
   *          by this editor
   */
  public void setEditor( Control editor, TreeItem item, int column ) {
    setItem( item );
    setColumn( column );
    setEditor( editor );
  }

  public void setEditor( Control editor ) {
    super.setEditor( editor );
    resize();
  }

  /**
   * Specify the Control that is to be displayed and the cell in the tree that
   * it is to be positioned above.
   * <p>
   * Note: The Control provided as the editor <b>must</b> be created with its
   * parent being the Tree control specified in the TreeEditor constructor.
   *
   * @param editor the Control that is displayed above the cell being edited
   * @param item the TreeItem for the row of the cell being tracked by this
   *          editor
   */
  public void setEditor( Control editor, TreeItem item ) {
    setItem( item );
    setEditor( editor );
  }

  public void layout() {
    if( tree == null || tree.isDisposed() )
      return;
    if( item == null || item.isDisposed() )
      return;
    int columnCount = tree.getColumnCount();
    if( columnCount == 0 && column != 0 )
      return;
    if( columnCount > 0 && ( column < 0 || column >= columnCount ) )
      return;
    super.layout();
  }
}
