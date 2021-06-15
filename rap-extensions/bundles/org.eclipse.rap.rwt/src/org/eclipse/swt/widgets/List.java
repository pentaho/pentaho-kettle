/*******************************************************************************
 * Copyright (c) 2002, 2019 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.widgets;

import static org.eclipse.rap.rwt.internal.textsize.TextSizeUtil.stringExtent;
import static org.eclipse.swt.internal.widgets.MarkupUtil.checkMarkupPrecondition;
import static org.eclipse.swt.internal.widgets.MarkupUtil.isMarkupEnabledFor;
import static org.eclipse.swt.internal.widgets.MarkupUtil.MarkupTarget.TEXT;
import static org.eclipse.swt.internal.widgets.MarkupValidator.isValidationDisabledFor;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.rap.rwt.internal.theme.ThemeAdapter;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.widgets.IListAdapter;
import org.eclipse.swt.internal.widgets.ListModel;
import org.eclipse.swt.internal.widgets.MarkupValidator;
import org.eclipse.swt.internal.widgets.listkit.ListLCA;
import org.eclipse.swt.internal.widgets.listkit.ListThemeAdapter;


/**
 * Instances of this class represent a selectable user interface
 * object that displays a list of strings and issues notification
 * when a string is selected.  A list may be single or multi select.
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SINGLE, MULTI</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection, DefaultSelection</dd>
 * </dl>
 * <p>
 * Note: Only one of SINGLE and MULTI may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * @since 1.0
 */
public class List extends Scrollable {

  private final class ResizeListener extends ControlAdapter {
    @Override
    public void controlResized( ControlEvent event ) {
      updateScrollBars();
    }
  }

  private final ListModel model;
  private int focusIndex;
  private transient IListAdapter listAdapter;
  private final ResizeListener resizeListener;
  private int topIndex;
  private boolean hasVScrollBar;
  private boolean hasHScrollBar;
  private BoxDimensions bufferedItemPadding;
  private int customItemHeight;

  /**
   * Constructs a new instance of this class given its parent
   * and a style value describing its behavior and appearance.
   * <p>
   * The style value is either one of the style constants defined in
   * class <code>SWT</code> which is applicable to instances of this
   * class, or must be built by <em>bitwise OR</em>'ing together
   * (that is, using the <code>int</code> "|" operator) two or more
   * of those <code>SWT</code> style constants. The class description
   * lists the style constants that are applicable to the class.
   * Style bits are also inherited from superclasses.
   * </p>
   *
   * @param parent a composite control which will be the parent of the new instance (cannot be null)
   * @param style the style of control to construct
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   * </ul>
   *
   * @see SWT#SINGLE
   * @see SWT#MULTI
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public List( Composite parent, int style ) {
    super( parent, checkStyle( style ) );
    model = new ListModel( ( style & SWT.SINGLE ) != 0 );
    focusIndex = -1;
    customItemHeight = -1;
    resizeListener = new ResizeListener();
    addControlListener( resizeListener );
  }

  /////////////////////
  // Adaptable override

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == IListAdapter.class ) {
      if( listAdapter == null ) {
        listAdapter = new IListAdapter() {
          @Override
          public void setFocusIndex( int focusIndex ) {
            List.this.setFocusIndex( focusIndex );
          }

          @Override
          public Point getItemDimensions() {
            return List.this.getItemDimensions();
          }
        };
      }
      return ( T )listAdapter;
    }
    if( adapter == WidgetLCA.class ) {
      return ( T )ListLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  ///////////////////////////////
  // Methods to get/set selection

  /**
   * Returns an array of <code>String</code>s that are currently
   * selected in the receiver.  The order of the items is unspecified.
   * An empty array indicates that no items are selected.
   * <p>
   * Note: This is not the actual structure used by the receiver
   * to maintain its selection, so modifying the array will
   * not affect the receiver.
   * </p>
   * @return an array representing the selection
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public String[] getSelection() {
    checkWidget();
    int[] selectionIndices = model.getSelectionIndices();
    String[] result = new String[ selectionIndices.length ];
    for( int i = 0; i < result.length; i++ ) {
      result[ i ] = model.getItem( selectionIndices[ i ] );
    }
    return result;
  }

  /**
   * Returns the zero-relative index of the item which is currently
   * selected in the receiver, or -1 if no item is selected.
   *
   * @return the index of the selected item or -1
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getSelectionIndex() {
    checkWidget();
    return model.getSelectionIndex();
  }

  /**
   * Returns the zero-relative indices of the items which are currently
   * selected in the receiver.  The order of the indices is unspecified.
   * The array is empty if no items are selected.
   * <p>
   * Note: This is not the actual structure used by the receiver
   * to maintain its selection, so modifying the array will
   * not affect the receiver.
   * </p>
   * @return the array of indices of the selected items
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int[] getSelectionIndices() {
    checkWidget();
    return model.getSelectionIndices();
  }

  /**
   * Returns the number of selected items contained in the receiver.
   *
   * @return the number of selected items
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getSelectionCount() {
    checkWidget();
    return model.getSelectionCount();
  }

  /**
   * Selects the item at the given zero-relative index in the receiver.
   * If the item at the index was already selected, it remains selected.
   * The current selection is first cleared, then the new item is selected.
   * Indices that are out of range are ignored.
   *
   * @param selection the index of the item to select
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   * @see List#deselectAll()
   * @see List#select(int)
   */
  // TODO [rh] selection is not scrolled into view (see List.js)
  public void setSelection( int selection ) {
    checkWidget();
    model.setSelection( selection );
    updateFocusIndexAfterSelectionChange();
  }

  /**
   * Selects the items at the given zero-relative indices in the receiver.
   * The current selection is cleared before the new items are selected.
   * <p>
   * Indices that are out of range and duplicate indices are ignored.
   * If the receiver is single-select and multiple indices are specified,
   * then all indices are ignored.
   *
   * @param selection the indices of the items to select
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the array of indices is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see List#deselectAll()
   * @see List#select(int[])
   */
  public void setSelection( int[] selection ) {
    checkWidget();
    model.setSelection( selection );
    updateFocusIndexAfterSelectionChange();
  }

  /**
   * Selects the items in the range specified by the given zero-relative
   * indices in the receiver. The range of indices is inclusive.
   * The current selection is cleared before the new items are selected.
   * <p>
   * Indices that are out of range are ignored and no items will be selected
   * if start is greater than end.
   * If the receiver is single-select and there is more than one item in the
   * given range, then all indices are ignored.
   *
   * @param start the start index of the items to select
   * @param end the end index of the items to select
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see List#deselectAll()
   * @see List#select(int,int)
   */
  public void setSelection( int start, int end ) {
    checkWidget();
    model.setSelection( start, end );
    updateFocusIndexAfterSelectionChange();
  }

  /**
   * Sets the receiver's selection to be the given array of items.
   * The current selection is cleared before the new items are selected.
   * <p>
   * Items that are not in the receiver are ignored.
   * If the receiver is single-select and multiple items are specified,
   * then all items are ignored.
   *
   * @param selection the array of items
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the array of items is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see List#deselectAll()
   * @see List#select(int[])
   * @see List#setSelection(int[])
   */
  public void setSelection( String[] selection ) {
    checkWidget();
    model.setSelection( selection );
    updateFocusIndexAfterSelectionChange();
  }

  /**
   * Selects the item at the given zero-relative index in the receiver's
   * list.  If the item at the index was already selected, it remains
   * selected. Indices that are out of range are ignored.
   *
   * @param index the index of the item to select
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void select( int index ) {
    checkWidget();
    if( ( style & SWT.SINGLE ) != 0 ) {
      if( index >= 0 && index < model.getItemCount() ) {
        model.setSelection( index );
      }
    } else {
      model.addSelection( index );
    }
  }

  /**
   * Selects the items at the given zero-relative indices in the receiver.
   * The current selection is not cleared before the new items are selected.
   * <p>
   * If the item at a given index is not selected, it is selected.
   * If the item at a given index was already selected, it remains selected.
   * Indices that are out of range and duplicate indices are ignored.
   * If the receiver is single-select and multiple indices are specified,
   * then all indices are ignored.
   *
   * @param indices the array of indices for the items to select
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the array of indices is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see List#setSelection(int[])
   */
  public void select( int[] indices ) {
    checkWidget();
    if( indices == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    int length = indices.length;
    if( length != 0 && ( ( style & SWT.SINGLE ) == 0 || length <= 1 ) ) {
      int i = 0;
      while( i < length ) {
        int index = indices[ i ];
        model.addSelection( index );
        i++;
      }
    }
  }

  /**
   * Selects the items in the range specified by the given zero-relative
   * indices in the receiver. The range of indices is inclusive.
   * The current selection is not cleared before the new items are selected.
   * <p>
   * If an item in the given range is not selected, it is selected.
   * If an item in the given range was already selected, it remains selected.
   * Indices that are out of range are ignored and no items will be selected
   * if start is greater than end.
   * If the receiver is single-select and there is more than one item in the
   * given range, then all indices are ignored.
   *
   * @param start the start of the range
   * @param end the end of the range
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see List#setSelection(int,int)
   */
  public void select( int start, int end ) {
    checkWidget();
    if( end >= 0 && start <= end && ( ( style & SWT.SINGLE ) == 0 || start == end ) ) {
      int count = model.getItemCount();
      if( count != 0 && start < count ) {
        int startIndex = Math.max( 0, start );
        int endIndex = Math.min( end, count - 1 );
        if( ( style & SWT.SINGLE ) != 0 ) {
          model.setSelection( startIndex );
        } else {
          for( int i = startIndex; i <= endIndex; i++ ) {
            model.addSelection( i );
          }
        }
      }
    }
  }

  /**
   * Selects all of the items in the receiver.
   * <p>
   * If the receiver is single-select, do nothing.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void selectAll() {
    checkWidget();
    model.selectAll();
    updateFocusIndexAfterSelectionChange();
  }

  /**
   * Deselects all selected items in the receiver.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void deselectAll() {
    checkWidget();
    model.deselectAll();
    updateFocusIndexAfterSelectionChange();
  }

  /**
   * Deselects the item at the given zero-relative index in the receiver.
   * If the item at the index was already deselected, it remains
   * deselected. Indices that are out of range are ignored.
   *
   * @param index the index of the item to deselect
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public void deselect( int index ) {
    checkWidget();
    removeFromSelection( index );
  }

  /**
   * Deselects the items at the given zero-relative indices in the receiver.
   * If the item at the given zero-relative index in the receiver
   * is selected, it is deselected.  If the item at the index
   * was not selected, it remains deselected.  The range of the
   * indices is inclusive. Indices that are out of range are ignored.
   *
   * @param start the start index of the items to deselect
   * @param end the end index of the items to deselect
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public void deselect( int start, int end ) {
    checkWidget();
    if( start == 0 && end == model.getItemCount() - 1 ) {
      deselectAll();
    } else {
      int actualStart = Math.max( 0, start );
      for( int i = actualStart; i <= end; i++ ) {
        removeFromSelection( i );
      }
    }
  }

  /**
   * Deselects the items at the given zero-relative indices in the receiver.
   * If the item at the given zero-relative index in the receiver
   * is selected, it is deselected.  If the item at the index
   * was not selected, it remains deselected. Indices that are out
   * of range and duplicate indices are ignored.
   *
   * @param indices the array of indices for the items to deselect
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the set of indices is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public void deselect( int [] indices ) {
    checkWidget();
    if( indices == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    for( int i = 0; i < indices.length; i++ ) {
      removeFromSelection( indices[ i ] );
    }
  }

  private void removeFromSelection( int index ) {
    if( index >= 0 && index < model.getItemCount() ) {
      boolean found = false;
      int selection[] = model.getSelectionIndices();
      for( int i = 0; !found && i < selection.length; i++ ) {
        if( index == selection[ i ] ) {
          int length = selection.length;
          int[] newSel = new int[ length - 1 ];
          System.arraycopy( selection, 0, newSel, 0, i );
          if( i < length - 1 ) {
            System.arraycopy( selection, i + 1, newSel, i, length - i - 1 );
          }
          selection = newSel;
          model.setSelection( selection );
          found = true;
        }
      }
    }
  }

  /**
   * Returns <code>true</code> if the item is selected,
   * and <code>false</code> otherwise.  Indices out of
   * range are ignored.
   *
   * @param index the index of the item
   * @return the visibility state of the item at the index
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public boolean isSelected( int index ) {
    checkWidget();
    boolean result;
    if( ( style & SWT.SINGLE ) != 0 ) {
      result = index == getSelectionIndex();
    } else {
      int[] selectionIndices = getSelectionIndices();
      result = false;
      for( int i = 0; !result && i < selectionIndices.length; i++ ) {
        if( index == selectionIndices[ i ] ) {
          result = true;
        }
      }
    }
    return result;
  }

  /**
   * Sets the zero-relative index of the item which is currently
   * at the top of the receiver. This index can change when items
   * are scrolled or new items are added and removed.
   *
   * @param topIndex the index of the top item
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public void setTopIndex( int topIndex ) {
    checkWidget();
    int count = model.getItemCount();
    if( this.topIndex != topIndex && topIndex >= 0 && topIndex < count ) {
      this.topIndex = topIndex;
    }
  }

  /**
   * Returns the zero-relative index of the item which is currently
   * at the top of the receiver. This index can change when items are
   * scrolled or new items are added or removed.
   *
   * @return the index of the top item
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public int getTopIndex() {
    checkWidget();
    return topIndex;
  }

  /**
   * Shows the selection.  If the selection is already showing in the receiver,
   * this method simply returns.  Otherwise, the items are scrolled until
   * the selection is visible.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public void showSelection() {
    checkWidget();
    int index = getSelectionIndex();
    if( index != -1 ) {
      int itemCount = getVisibleItemCount();
      if( index < topIndex ) {
        // Show item as top item
        setTopIndex( index );
      } else if( itemCount > 0 && index >= topIndex + itemCount ) {
        // Show item as last item
        setTopIndex( index - itemCount + 1 );
      }
    }
  }

  /**
   * Returns the zero-relative index of the item which currently
   * has the focus in the receiver, or -1 if no item has focus.
   *
   * @return the index of the selected item
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getFocusIndex() {
    checkWidget();
    return focusIndex;
  }

  ////////////////////////////////
  // Methods to maintain the items

  /**
   * Adds the argument to the end of the receiver's list.
   *
   * @param string the new item
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #add(String,int)
   */
  public void add( String string ) {
    checkWidget();
    model.add( string );
    updateFocusIndexAfterItemChange();
    updateScrollBars();
  }

  /**
   * Adds the argument to the receiver's list at the given
   * zero-relative index.
   * <p>
   * Note: To add an item at the end of the list, use the
   * result of calling <code>getItemCount()</code> as the
   * index or use <code>add(String)</code>.
   * </p>
   *
   * @param string the new item
   * @param index the index for the item
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
   *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list (inclusive)</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #add(String)
   */
  public void add( String string, int index ) {
    checkWidget();
    model.add( string, index );
    updateFocusIndexAfterItemChange();
    updateScrollBars();
  }

  /**
   * Removes the item from the receiver at the given
   * zero-relative index.
   *
   * @param index the index for the item
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void remove( int index ) {
    checkWidget();
    model.remove( index );
    updateFocusIndexAfterItemChange();
    adjustTopIndex();
    updateScrollBars();
  }

  /**
   * Removes the items from the receiver which are
   * between the given zero-relative start and end
   * indices (inclusive).
   *
   * @param start the start of the range
   * @param end the end of the range
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_RANGE - if either the start or end are not between 0 and the number of elements in the list minus 1 (inclusive)</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void remove( int start, int end ) {
    checkWidget();
    model.remove( start, end );
    updateFocusIndexAfterItemChange();
    adjustTopIndex();
    updateScrollBars();
  }

  /**
   * Removes the items from the receiver at the given
   * zero-relative indices.
   *
   * @param indices the array of indices of the items
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
   *    <li>ERROR_NULL_ARGUMENT - if the indices array is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void remove( int[] indices ) {
    checkWidget();
    model.remove( indices );
    updateFocusIndexAfterItemChange();
    adjustTopIndex();
    updateScrollBars();
  }

  /**
   * Searches the receiver's list starting at the first item
   * until an item is found that is equal to the argument,
   * and removes that item from the list.
   *
   * @param string the item to remove
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the string is not found in the list</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void remove( String string ) {
    checkWidget();
    model.remove( string );
    updateFocusIndexAfterItemChange();
    adjustTopIndex();
    updateScrollBars();
  }

  /**
   * Removes all of the items from the receiver.
   * <p>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void removeAll() {
    checkWidget();
    model.removeAll();
    updateFocusIndexAfterItemChange();
    adjustTopIndex();
    updateScrollBars();
  }

  /**
   * Sets the text of the item in the receiver's list at the given
   * zero-relative index to the string argument. This is equivalent
   * to <code>remove</code>'ing the old item at the index, and then
   * <code>add</code>'ing the new item at that index.
   *
   * @param index the index for the item
   * @param string the new text for the item
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
   *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setItem( int index, String string ) {
    checkWidget();
    validateMarkup( new String[] { string } );
    model.setItem( index, string );
    updateScrollBars();
  }

  /**
   * Sets the receiver's items to be the given array of items.
   *
   * @param items the array of items
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the items array is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if an item in the items array is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setItems( String[] items ) {
    checkWidget();
    validateMarkup( items );
    model.setItems( items );
    updateScrollBars();
  }

  /**
   * Returns the item at the given, zero-relative index in the
   * receiver. Throws an exception if the index is out of range.
   *
   * @param index the index of the item to return
   * @return the item at the given index
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public String getItem( int index ) {
    checkWidget();
    return model.getItem( index );
  }

  /**
   * Returns the number of items contained in the receiver.
   *
   * @return the number of items
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getItemCount() {
    checkWidget();
    return model.getItemCount();
  }

  /**
   * Returns a (possibly empty) array of <code>String</code>s which
   * are the items in the receiver.
   * <p>
   * Note: This is not the actual structure used by the receiver
   * to maintain its list of items, so modifying the array will
   * not affect the receiver.
   * </p>
   *
   * @return the items in the receiver's list
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public String[] getItems() {
    checkWidget();
    return model.getItems();
  }

  /**
   * Gets the index of an item.
   * <p>
   * The list is searched starting at 0 until an
   * item is found that is equal to the search item.
   * If no item is found, -1 is returned.  Indexing
   * is zero based.
   *
   * @param string the search item
   * @return the index of the item
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int indexOf( String string ) {
    checkWidget();
    return indexOf( string, 0 );
  }

  /**
   * Searches the receiver's list starting at the given,
   * zero-relative index until an item is found that is equal
   * to the argument, and returns the index of that item. If
   * no item is found or the starting index is out of range,
   * returns -1.
   *
   * @param string the search item
   * @param start the zero-relative index at which to start the search
   * @return the index of the item
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int indexOf( String string, int start ) {
    checkWidget();
    if( string == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    return model.indexOf( string, start );
  }

  /**
   * Returns the height of the area which would be used to
   * display <em>one</em> of the items in the list.
   *
   * @return the height of one item
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getItemHeight() {
    checkWidget();
    int result = customItemHeight;
    if( result == -1 ) {
      BoxDimensions itemPadding = getItemPadding();
      result = TextSizeUtil.getCharHeight( getFont() ) + itemPadding.top + itemPadding.bottom;
    }
    return result;
  }

  /////////////////////////////////////////
  // Listener registration/de-registration

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the receiver's selection changes, by sending
   * it one of the messages defined in the <code>SelectionListener</code>
   * interface.
   * <p>
   * <code>widgetSelected</code> is called when the selection changes.
   * <code>widgetDefaultSelected</code> is typically called when an item is double-clicked.
   * </p>
   *
   * @param listener the listener which should be notified
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see SelectionListener
   * @see #removeSelectionListener
   * @see SelectionEvent
   */
  public void addSelectionListener( SelectionListener listener ) {
    checkWidget();
    if( listener == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.Selection, typedListener );
    addListener( SWT.DefaultSelection, typedListener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when the receiver's selection changes.
   *
   * @param listener the listener which should no longer be notified
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see SelectionListener
   * @see #addSelectionListener
   */
  public void removeSelectionListener( SelectionListener listener ) {
    checkWidget();
    if( listener == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    removeListener( SWT.Selection, listener );
    removeListener( SWT.DefaultSelection, listener );
  }

  @Override
  public void setFont( Font font ) {
    super.setFont( font );
    updateScrollBars();
  }

  @Override
  boolean isTabGroup() {
    return true;
  }

  @Override
  public void setData( String key, Object value ) {
    if( RWT.CUSTOM_ITEM_HEIGHT.equals( key ) ) {
      setCustomItemHeight( value );
    }
    if( !RWT.MARKUP_ENABLED.equals( key ) || !isMarkupEnabledFor( this ) ) {
      checkMarkupPrecondition( key, TEXT, () -> model.getItemCount() == 0 );
      super.setData( key, value );
    }
  }

  /////////////////////////////////////////
  // Widget dimensions

  @Override
  public Point computeSize( int wHint, int hHint, boolean changed ) {
    checkWidget ();
    int width = getMaxItemWidth();
    int height = getItemHeight() * getItemCount();
    if( width == 0 ) {
      width = DEFAULT_WIDTH;
    }
    if( height == 0 ) {
      height = DEFAULT_HEIGHT;
    }
    if( wHint != SWT.DEFAULT ) {
      width = wHint;
    }
    if( hHint != SWT.DEFAULT ) {
      height = hHint;
    }
    BoxDimensions border = getBorder();
    width += border.left + border.right;
    height += border.top + border.bottom;
    if( ( style & SWT.V_SCROLL ) != 0 ) {
      width += getVerticalBar().getSize().x;
    }
    if( ( style & SWT.H_SCROLL ) != 0 ) {
      height += getHorizontalBar().getSize().y;
    }
    return new Point( width, height );
  }

  /////////////////////////////////
  // Helping methods for focusIndex

  private void setFocusIndex( int focusIndex ) {
    int count = model.getItemCount();
    if( focusIndex == -1 || ( focusIndex >= 0 && focusIndex < count ) ) {
      this.focusIndex = focusIndex;
    }
  }

  private void updateFocusIndexAfterSelectionChange() {
    focusIndex = -1;
    if( model.getItemCount() > 0 ) {
      if( model.getSelectionIndex() == -1 ) {
        focusIndex = 0;
      } else {
        focusIndex = model.getSelectionIndices()[ 0 ];
      }
    }
  }

  private void updateFocusIndexAfterItemChange() {
    if( model.getItemCount() == 0 ) {
      focusIndex = -1;
    } else if( model.getSelectionIndex() == -1 ){
      focusIndex = model.getItemCount() - 1;
    }
  }

  //////////////////
  // Helping methods

  private static int checkStyle( int style ) {
    return checkBits( style, SWT.SINGLE, SWT.MULTI, 0, 0, 0, 0 );
  }

  private int getItemWidth( String item ) {
    Point extent = stringExtent( getFont(), item, isMarkupEnabledFor( this ) );
    BoxDimensions itemPadding = getItemPadding();
    return extent.x + itemPadding.left + itemPadding.right;
  }

  private int getMaxItemWidth() {
    int result = 0;
    String[] items = getItems();
    for( int i = 0; i < items.length; i++ ) {
      int itemWidth = getItemWidth( items[ i ] );
      result = Math.max( result, itemWidth );
    }
    return result;
  }

  private void adjustTopIndex() {
    int count = model.getItemCount();
    if( count == 0 ) {
      topIndex = 0;
    } else if( topIndex >= count - 1 ) {
      topIndex = count - 1;
    }
  }

  final int getVisibleItemCount() {
    int clientHeight = getBounds().height;
    if( ( style & SWT.H_SCROLL ) != 0 ) {
      clientHeight -= getHorizontalBar().getSize().y;
    }
    int result = 0;
    if( clientHeight >= 0 ) {
      int itemHeight = getItemHeight();
      result = clientHeight / itemHeight;
    }
    return result;
  }

  Point getItemDimensions() {
    int width = 0;
    int height = 0;
    if( getItemCount() > 0 ) {
      int availableWidth = getClientArea().width;
      if( ( style & SWT.H_SCROLL ) == 0 && isMarkupEnabledFor( this ) ) {
        width = availableWidth;
      } else {
        width = Math.max( getMaxItemWidth(), availableWidth );
      }
      height = getItemHeight();
    }
    return new Point( width, height );
  }

  private BoxDimensions getItemPadding() {
    if( bufferedItemPadding == null ) {
      ListThemeAdapter themeAdapter = ( ListThemeAdapter )getAdapter( ThemeAdapter.class );
      bufferedItemPadding = themeAdapter.getItemPadding( this );
    }
    return bufferedItemPadding;
  }

  private void setCustomItemHeight( Object value ) {
    if( value == null ) {
      customItemHeight = -1;
    } else {
      if( !( value instanceof Integer ) ) {
        error( SWT.ERROR_INVALID_ARGUMENT );
      }
      int itemHeight = ( ( Integer )value ).intValue();
      if( itemHeight < 0 ) {
        error( SWT.ERROR_INVALID_RANGE );
      }
      customItemHeight = itemHeight;
    }
  }

  private void validateMarkup( String[] items ) {
    if( items != null && isMarkupEnabledFor( this ) && !isValidationDisabledFor( this ) ) {
      for( int i = 0; i < items.length; i++ ) {
        if( items[ i ] != null ) {
          MarkupValidator.getInstance().validate( items[ i ] );
        }
      }
    }
  }

  ///////////////////////////////////////
  // Helping methods - dynamic scrollbars

  boolean hasVScrollBar() {
    return ( style & SWT.V_SCROLL ) != 0 && hasVScrollBar;
  }

  boolean hasHScrollBar() {
    return ( style & SWT.H_SCROLL ) != 0 && hasHScrollBar;
  }

  @Override
  int getVScrollBarWidth() {
    int result = 0;
    if( hasVScrollBar() ) {
      result = getVerticalBar().getSize().x;
    }
    return result;
  }

  @Override
  int getHScrollBarHeight() {
    int result = 0;
    if( hasHScrollBar() ) {
      result = getHorizontalBar().getSize().y;
    }
    return result;
  }

  boolean needsVScrollBar() {
    int availableHeight = getClientArea().height;
    int height = getItemCount() * getItemHeight();
    return height > availableHeight;
  }

  boolean needsHScrollBar() {
    boolean result = false;
    if( ( style & SWT.H_SCROLL ) != 0 ) {
      int availableWidth = getClientArea().width;
      int width = getMaxItemWidth();
      result = width > availableWidth;
    }
    return result;
  }

  void updateScrollBars() {
    hasVScrollBar = false;
    hasHScrollBar = needsHScrollBar();
    if( needsVScrollBar() ) {
      hasVScrollBar = true;
      hasHScrollBar = needsHScrollBar();
    }
    ScrollBar hScroll = getHorizontalBar();
    if( hScroll != null ) {
      hScroll.setVisible( hasHScrollBar );
    }
    ScrollBar vScroll = getVerticalBar();
    if( vScroll != null ) {
      vScroll.setVisible( hasVScrollBar );
    }
  }
}
