/*******************************************************************************
 * Copyright (c) 2013, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.treecolumnkit;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_DEFAULT_SELECTION;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_SELECTION;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getAdapter;

import java.util.Arrays;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.internal.lifecycle.ProcessActionRunner;
import org.eclipse.rap.rwt.internal.protocol.WidgetOperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.widgets.ITreeAdapter;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;


public class TreeColumnOperationHandler extends WidgetOperationHandler<TreeColumn> {

  private static final String METHOD_MOVE = "move";
  private static final String METHOD_RESIZE = "resize";
  private static final String PROP_LEFT = "left";
  private static final String PROP_WIDTH = "width";

  public TreeColumnOperationHandler( TreeColumn column ) {
    super( column );
  }

  @Override
  public void handleCall( TreeColumn column, String method, JsonObject properties ) {
    if( METHOD_MOVE.equals( method ) ) {
      handleCallMove( column, properties );
    } else if( METHOD_RESIZE.equals( method ) ) {
      handleCallResize( column, properties );
    }
  }

  @Override
  public void handleNotify( TreeColumn column, String eventName, JsonObject properties ) {
    if( EVENT_SELECTION.equals( eventName ) ) {
      handleNotifySelection( column, properties );
    } else if( EVENT_DEFAULT_SELECTION.equals( eventName ) ) {
      handleNotifyDefaultSelection( column, properties );
    } else {
      super.handleNotify( column, eventName, properties );
    }
  }

  /*
   * PROTOCOL CALL move
   *
   * @param left (int) the left position of the column
   */
  public void handleCallMove( final TreeColumn column, JsonObject properties ) {
    final int newLeft = properties.get( PROP_LEFT ).asInt();
    ProcessActionRunner.add( new Runnable() {
      @Override
      public void run() {
        moveColumn( column, newLeft );
      }
    } );
  }

  /*
   * PROTOCOL CALL resize
   *
   * @param width (int) the width of the column
   */
  public void handleCallResize( final TreeColumn column, JsonObject properties ) {
    final int width = properties.get( PROP_WIDTH ).asInt();
    ProcessActionRunner.add( new Runnable() {
      @Override
      public void run() {
        column.setWidth( width );
      }
    } );
  }

  /*
   * PROTOCOL NOTIFY Selection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   */
  public void handleNotifySelection( TreeColumn column, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.Selection, properties );
    column.notifyListeners( SWT.Selection, event );
  }

  /*
   * PROTOCOL NOTIFY DefaultSelection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   */
  public void handleNotifyDefaultSelection( TreeColumn column, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.DefaultSelection, properties );
    column.notifyListeners( SWT.DefaultSelection, event );
  }

  static void moveColumn( TreeColumn column, int newLeft ) {
    Tree tree = column.getParent();
    int targetColumn = findMoveTarget( tree, newLeft );
    int[] columnOrder = tree.getColumnOrder();
    int index = tree.indexOf( column );
    int orderIndex = arrayIndexOf( columnOrder, index );
    columnOrder = arrayRemove( columnOrder, orderIndex );
    if( orderIndex < targetColumn ) {
      targetColumn--;
    }
    if( isFixed( column ) || isFixed( tree.getColumn( targetColumn ) ) ) {
      targetColumn = tree.indexOf( column );
    }
    columnOrder = arrayInsert( columnOrder, targetColumn, index );
    if( Arrays.equals( columnOrder, tree.getColumnOrder() ) ) {
      // TODO [rh] HACK mark left as changed
      TreeColumn[] columns = tree.getColumns();
      for( int i = 0; i < columns.length; i++ ) {
        getAdapter( columns[ i ] ).preserve( PROP_LEFT, null );
      }
    } else {
      tree.setColumnOrder( columnOrder );
      // [if] HACK mark left as changed - see bug 336340
      getAdapter( column ).preserve( PROP_LEFT, null );
    }
  }

  /* (intentionally non-JavaDoc'ed)
   * Returns the index in the columnOrder array at which the moved column
   * should be inserted (moving remaining columns to the right). A return
   * value of columnCount indicates that the moved column should be inserted
   * after the right-most column.
   */
  private static int findMoveTarget( Tree tree, int newLeft ) {
    int result = -1;
    TreeColumn[] columns = tree.getColumns();
    int[] columnOrder = tree.getColumnOrder();
    if( newLeft < 0 ) {
      result = 0;
    } else {
      for( int i = 0; result == -1 && i < columns.length; i++ ) {
        TreeColumn column = columns[ columnOrder [ i ] ];
        int left = getLeft( column );
        int width = column.getWidth();
        if( isFixed( column ) ) {
          left += getLeftOffset( column );
        }
        if( newLeft >= left && newLeft <= left + width ) {
          result = i;
          if( newLeft >= left + width / 2 && result < columns.length && !isFixed( column ) ) {
            result++;
          }
        }
      }
    }
    // Column was moved right of the right-most column
    if( result == -1 ) {
      result = columns.length;
    }
    return result;
  }

  private static boolean isFixed( TreeColumn column ) {
    return getTreeAdapter( column ).isFixedColumn( column );
  }

  private static int getLeft( TreeColumn column ) {
    return getTreeAdapter( column ).getColumnLeft( column );
  }

  private static int getLeftOffset( TreeColumn column ) {
    return getTreeAdapter( column ).getScrollLeft();
  }

  private static ITreeAdapter getTreeAdapter( TreeColumn column ) {
    return column.getParent().getAdapter( ITreeAdapter.class );
  }

  private static int arrayIndexOf( int[] array, int value ) {
    int result = -1;
    for( int i = 0; result == -1 && i < array.length; i++ ) {
      if( array[ i ] == value ) {
        result = i;
      }
    }
    return result;
  }

  private static int[] arrayRemove( int[] array, int index ) {
    int length = array.length;
    int[] result = new int[ length - 1 ];
    System.arraycopy( array, 0, result, 0, index );
    if( index < length - 1 ) {
      System.arraycopy( array, index + 1, result, index, length - index - 1 );
    }
    return result;
  }

  private static int[] arrayInsert( int[] array, int index, int value ) {
    int length = array.length;
    int[] result = new int[ length + 1 ];
    System.arraycopy( array, 0, result, 0, length );
    System.arraycopy( result, index, result, index + 1, length - index );
    result[ index ] = value;
    return result;
  }

}
