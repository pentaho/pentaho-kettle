/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH and others.
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

import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.rap.rwt.internal.theme.ThemeAdapter;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.graphics.FontUtil;
import org.eclipse.swt.internal.widgets.ITextAdapter;
import org.eclipse.swt.internal.widgets.ListModel;
import org.eclipse.swt.internal.widgets.combokit.ComboLCA;
import org.eclipse.swt.internal.widgets.combokit.ComboThemeAdapter;


/**
 * Instances of this class are controls that allow the user
 * to choose an item from a list of items, or optionally
 * enter a new value by typing it into an editable text
 * field. Often, <code>Combo</code>s are used in the same place
 * where a single selection <code>List</code> widget could
 * be used but space is limited. A <code>Combo</code> takes
 * less space than a <code>List</code> widget and shows
 * similar information.
 * <p>
 * Note: Since <code>Combo</code>s can contain both a list
 * and an editable text field, it is possible to confuse methods
 * which access one versus the other (compare for example,
 * <code>clearSelection()</code> and <code>deselectAll()</code>).
 * The API documentation is careful to indicate either "the
 * receiver's list" or the "the receiver's text field" to
 * distinguish between the two cases.
 * </p><p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to add children to it, or set a layout on it.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>DROP_DOWN, READ_ONLY<!--, SIMPLE --></dd>
 * <dt><b>Events:</b></dt>
 * <dd>DefaultSelection, Modify, Selection</dd>
 * </dl>
 * <p>
 * <!-- Note: Only one of the styles DROP_DOWN and SIMPLE may be specified. -->
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see List
 * @since 1.0
 */
public class Combo extends Composite {

  /**
   * The maximum number of characters that can be entered
   * into a text widget.
   * @since 1.3
   */
  public static final int LIMIT = Integer.MAX_VALUE;

  // Must be in sync with appearance "list-item"
  private static final int LIST_ITEM_PADDING = 3;

  private static final double LINE_HEIGHT_FACTOR = 1.4;

  private ITextAdapter textAdapter;
  private final ListModel model;
  private String text;
  private int textLimit;
  private int visibleCount;
  private final Point selection;
  private boolean dropped;

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
   * @see SWT#DROP_DOWN
   * @see SWT#READ_ONLY
   * @see SWT#SIMPLE
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public Combo( Composite parent, int style ) {
    super( parent, checkStyle( style ) );
    text = "";
    visibleCount = 5;
    dropped = false;
    textLimit = LIMIT;
    selection = new Point( 0, 0 );
    model = new ListModel( true );
  }

  @Override
  void initState() {
    removeState( /* CANVAS | */THEME_BACKGROUND );
  }

  /**
   * Returns the zero-relative index of the item which is currently
   * selected in the receiver's list, or -1 if no item is selected.
   *
   * @return the index of the selected item
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
   * Selects the item at the given zero-relative index in the receiver's
   * list.  If the item at the index was already selected, it remains
   * selected. Indices that are out of range are ignored.
   *
   * @param selectionIndex the index of the item to select
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void select( int selectionIndex ) {
    checkWidget();
    if( 0 <= selectionIndex && selectionIndex < getItemCount() ) {
      model.setSelection( selectionIndex );
      updateText();
    }
  }

  /**
   * Deselects the item at the given zero-relative index in the receiver's
   * list.  If the item at the index was already deselected, it remains
   * deselected. Indices that are out of range are ignored.
   *
   * @param index the index of the item to deselect
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void deselect( int index ) {
    checkWidget();
    if( index == model.getSelectionIndex() ) {
      model.setSelection( -1 );
    }
    updateText();
  }

  /**
   * Deselects all selected items in the receiver's list.
   * <p>
   * Note: To clear the selection in the receiver's text field,
   * use <code>clearSelection()</code>.
   * </p>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #clearSelection
   */
  public void deselectAll() {
    checkWidget();
    model.deselectAll();
    text = "";
    notifyListeners( SWT.Modify, new Event() );
  }

  /**
   * Sets the selection in the receiver's text field to the
   * range specified by the argument whose x coordinate is the
   * start of the selection and whose y coordinate is the end
   * of the selection.
   *
   * @param selection a point representing the new selection start and end
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public void setSelection( Point selection ) {
    checkWidget();
    if( selection == null ) {
      SWT.error ( SWT.ERROR_NULL_ARGUMENT );
    }
    int validatedStart = this.selection.x;
    int validatedEnd = this.selection.y;
    int start = selection.x;
    int end = selection.y;
    if( start >= 0 && end >= start ) {
      validatedStart = Math.min( start, text.length() );
      validatedEnd = Math.min( end, text.length() );
    } else if ( end >= 0 && start > end ) {
      validatedStart = Math.min( end, text.length() );
      validatedEnd = Math.min( start, text.length() );
    }
    this.selection.x = validatedStart;
    this.selection.y = validatedEnd;
  }

  /**
   * Returns a <code>Point</code> whose x coordinate is the
   * character position representing the start of the selection
   * in the receiver's text field, and whose y coordinate is the
   * character position representing the end of the selection.
   * An "empty" selection is indicated by the x and y coordinates
   * having the same value.
   * <p>
   * Indexing is zero based.  The range of a selection is from
   * 0..N where N is the number of characters in the widget.
   * </p>
   *
   * @return a point representing the selection start and end
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public Point getSelection() {
    checkWidget();
    return new Point( selection.x, selection.y );
  }

  /**
   * Sets the selection in the receiver's text field to an empty
   * selection starting just before the first character. If the
   * text field is editable, this has the effect of placing the
   * i-beam at the start of the text.
   * <p>
   * Note: To clear the selected items in the receiver's list,
   * use <code>deselectAll()</code>.
   * </p>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #deselectAll
   *
   * @since 1.3
   */
  public void clearSelection() {
    checkWidget();
    resetSelection();
  }

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
  }

  /**
   * Removes the item from the receiver's list at the given
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
    int selectionIndex = getSelectionIndex();
    if( selectionIndex == index ) {
      deselect( index );
    }
    model.remove( index );
  }

  /**
   * Removes the items from the receiver's list which are
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
    String[] items = model.getItems();
    int lastIndex = items.length - 1;
    if(    start < 0
        || start > lastIndex
        || end < 0
        || end > lastIndex
        || end < start )
    {
      error( SWT.ERROR_INVALID_RANGE );
    }
    int selectionIndex = getSelectionIndex();
    for( int i = start; i <= end; i++ ) {
      int index = indexOf( items[ i ] );
      if( selectionIndex == index ) {
        deselect( index );
      }
    }
    model.remove( start, end );
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
    int indexOfThisString = indexOf( string );
    int selectionIndex = getSelectionIndex();
    if( selectionIndex == indexOfThisString ) {
      deselect( indexOfThisString );
    }
    model.remove( string );
  }

  /**
   * Removes all of the items from the receiver's list and clear the
   * contents of receiver's text field.
   * <p>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void removeAll() {
    checkWidget();
    deselectAll();
    model.removeAll();
  }

  /**
   * Sets the text of the item in the receiver's list at the given
   * zero-relative index to the string argument. This is equivalent
   * to removing the old item at the index, and then adding the new
   * item at that index.
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
    model.setItem( index, string );
  }

  /**
   * Sets the receiver's list to be the given array of items.
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
  public void setItems( String... items ) {
    checkWidget();
    model.setItems( items );
  }

  /**
   * Returns the item at the given, zero-relative index in the
   * receiver's list. Throws an exception if the index is out
   * of range.
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
   * Returns a (possibly empty) array of <code>String</code>s which are
   * the items in the receiver's list.
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
   * Returns the number of items contained in the receiver's list.
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
   * Returns the height of the area which would be used to
   * display <em>one</em> of the items in the receiver's list.
   *
   * @return the height of one item
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public int getItemHeight() {
    checkWidget();
    int charHeight = TextSizeUtil.getCharHeight( getFont() );
    int padding = 2 * LIST_ITEM_PADDING;
    return charHeight + padding;
  }

  /**
   * Sets the number of items that are visible in the drop
   * down portion of the receiver's list.
   * <p>
   * Note: This operation is a hint and is not supported on
   * platforms that do not have this concept.
   * </p>
   *
   * @param count the new number of items to be visible
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setVisibleItemCount( int count ) {
    checkWidget();
    if( count >= 0 ) {
      visibleCount = count;
    }
  }

  /**
   * Gets the number of items that are visible in the drop
   * down portion of the receiver's list.
   * <p>
   * Note: This operation is a hint and is not supported on
   * platforms that do not have this concept.
   * </p>
   *
   * @return the number of items that are visible
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getVisibleItemCount () {
    checkWidget();
    return visibleCount ;
  }

  /**
   * Marks the receiver's list as visible if the argument is <code>true</code>,
   * and marks it invisible otherwise.
   * <p>
   * If one of the receiver's ancestors is not visible or some
   * other condition makes the receiver not visible, marking
   * it visible may not actually cause it to be displayed.
   * </p>
   *
   * @param visible the new visibility state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public void setListVisible( boolean visible ) {
    checkWidget();
    dropped = visible;
  }

  /**
   * Returns <code>true</code> if the receiver's list is visible,
   * and <code>false</code> otherwise.
   * <p>
   * If one of the receiver's ancestors is not visible or some
   * other condition makes the receiver not visible, this method
   * may still indicate that it is considered visible even though
   * it may not actually be showing.
   * </p>
   *
   * @return the receiver's list's visibility state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public boolean getListVisible() {
    checkWidget();
    return dropped;
  }

  /**
   * Searches the receiver's list starting at the first item
   * (index 0) until an item is found that is equal to the
   * argument, and returns the index of that item. If no item
   * is found, returns -1.
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
   * @param start the zero-relative index at which to begin the search
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
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    return model.indexOf( string, start );
  }

  /**
   * Returns a string containing a copy of the contents of the
   * receiver's text field, or an empty string if there are no
   * contents.
   *
   * @return the receiver's text
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public String getText() {
    checkWidget();
    String result = "";
    if( ( style & SWT.READ_ONLY ) != 0 ) {
      int idx = model.getSelectionIndex();
      if( idx != -1 ) {
        result = model.getItem( idx );
      }
    } else {
      result = text;
    }
    return result;
  }

  /**
   * Sets the contents of the receiver's text field to the given string.
   * <p>
   * Note: The text field in a <code>Combo</code> is typically only capable of
   * displaying a single line of text. Thus, setting the text to a string
   * containing line breaks or other special characters will probably cause it
   * to display incorrectly.
   * </p>
   *
   * @param string the new text
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the string is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setText( String string ) {
    checkWidget();
    if( string == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( ( style & SWT.READ_ONLY ) != 0 ) {
      int index = indexOf( string );
      if( index != -1 ) {
        select( index );
        selection.x = 0;
        selection.y = string.length();
      }
    } else if( internalSetText( string, true ) ) {
      resetSelection();
      notifyListeners( SWT.Modify, new Event() );
    }
  }

  /**
   * Returns the maximum number of characters that the receiver's
   * text field is capable of holding. If this has not been changed
   * by <code>setTextLimit()</code>, it will be the constant
   * <code>Combo.LIMIT</code>.
   *
   * @return the text limit
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #LIMIT
   * @since 1.3
   */
  public int getTextLimit() {
    checkWidget();
    return textLimit;
  }

  /**
   * Sets the maximum number of characters that the receiver's
   * text field is capable of holding to be the argument.
   * <p>
   * To reset this value to the default, use <code>setTextLimit(Combo.LIMIT)</code>.
   * Specifying a limit value larger than <code>Combo.LIMIT</code> sets the
   * receiver's limit to <code>Combo.LIMIT</code>.
   * </p>
   * @param limit new text limit
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_CANNOT_BE_ZERO - if the limit is zero</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #LIMIT
   * @since 1.3
   */
  public void setTextLimit( int limit ) {
    checkWidget();
    if( limit == 0 ) {
      error( SWT.ERROR_CANNOT_BE_ZERO );
    }
    if( limit > 0 ) {
      textLimit = limit;
    } else {
      textLimit = LIMIT;
    }
  }

  /**
   * Returns the height of the receivers's text field.
   *
   * @return the text height
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public int getTextHeight() {
    checkWidget();
    Font font = getFont();
    int fontSize = FontUtil.getData( font ).getHeight();
    return ( int )Math.floor( fontSize * LINE_HEIGHT_FACTOR );
  }

  // TODO [rst] Revise: In SWT, a width or height hint of 0 does not result in
  //      the DEFAULT_WIDTH/HEIGHT.
  @Override
  public Point computeSize( int wHint, int hHint, boolean changed )
  {
    checkWidget();
    int width = 0;
    int height = TextSizeUtil.getCharHeight( getFont() );
    if( wHint == SWT.DEFAULT || hHint == SWT.DEFAULT ) {
      String[] items = model.getItems();
      for( int i = 0; i < items.length; i++ ) {
        if( !"".equals( items[ i ] ) ) {
          Point extent = TextSizeUtil.stringExtent( getFont(), items[ i ] );
          width = Math.max( width, extent.x + 10 );
        }
      }
    }
    BoxDimensions fieldPadding = getFieldPadding();
    int buttonWidth = getButtonWidth();
    if( width != 0 ) {
      width += fieldPadding.left + fieldPadding.right + buttonWidth;
    }
    if( height != 0 ) {
      height += fieldPadding.top + fieldPadding.bottom;
      // TODO [rst] Workaround for two missing pixels (Ö, p are cut off), revise
      height += 2;
    }
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
    height += border.top + border.bottom;
    width += border.left + border.right;
    return new Point( width, height );
  }

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the receiver's selection changes, by sending
   * it one of the messages defined in the <code>SelectionListener</code>
   * interface.
   * <p>
   * <code>widgetSelected</code> is called when the combo's list selection changes.
   * <code>widgetDefaultSelected</code> is typically called when ENTER is pressed the combo's text area.
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

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the receiver's text is modified, by sending
   * it one of the messages defined in the <code>ModifyListener</code>
   * interface.
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
   * @see ModifyListener
   * @see #removeModifyListener
   */
  public void addModifyListener( ModifyListener listener ) {
    checkWidget();
    if( listener == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.Modify, typedListener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when the receiver's text is modified.
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
   * @see ModifyListener
   * @see #addModifyListener
   */
  public void removeModifyListener( ModifyListener listener ) {
    checkWidget();
    if( listener == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    removeListener( SWT.Modify, listener );
  }

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the receiver's text is verified, by sending
   * it one of the messages defined in the <code>VerifyListener</code>
   * interface.
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
   * @see VerifyListener
   * @see #removeVerifyListener
   */
  public void addVerifyListener( VerifyListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.Verify, typedListener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when the control is verified.
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
   * @see VerifyListener
   * @see #addVerifyListener
   */
  public void removeVerifyListener( VerifyListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    removeListener( SWT.Verify, listener );
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == ITextAdapter.class ) {
      if( textAdapter == null ) {
        textAdapter = new ITextAdapter() {
          @Override
          public void setText( String text ) {
            if( internalSetText( text, true ) ) {
              adjustSelection();
              notifyListeners( SWT.Modify, new Event() );
            }
          }
        };
      }
      return ( T )textAdapter;
    }
    if( adapter == WidgetLCA.class ) {
      return ( T )ComboLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  @Override
  boolean isTabGroup() {
    return true;
  }

  @Override
  String getNameText() {
    return getText();
  }

  private void updateText() {
    if( ( style & SWT.READ_ONLY ) == 0 ) {
      int selectionIndex = getSelectionIndex();
      String text = selectionIndex != -1 ? getItem( selectionIndex ) : "";
      if( internalSetText( text, false ) ) {
        adjustSelection();
        notifyListeners( SWT.Modify, new Event() );
      }
    } else {
      notifyListeners( SWT.Modify, new Event() );
    }
  }

  private boolean internalSetText( String text, boolean updateSelection ) {
    String verifiedText = verifyText( text, 0, this.text.length() );
    if( verifiedText != null ) {
      if( updateSelection ) {
        int index = -1;
        String[] items = model.getItems();
        for( int i = 0; index == -1 && i < items.length; i++ ) {
          if( verifiedText.equals( items[ i ] ) ) {
            index = i;
          }
        }
        model.setSelection( index );
      }
      if( verifiedText.length() > textLimit ) {
        this.text = verifiedText.substring( 0, textLimit );
      } else {
        this.text = verifiedText;
      }
    }
    return verifiedText != null;
  }

  private String verifyText( String text, int start, int end ) {
    Event event = new Event();
    event.text = text;
    event.start = start;
    event.end = end;
    notifyListeners( SWT.Verify, event );
    /*
     * It is possible (but unlikely), that application code could have disposed
     * the widget in the verify event. If this happens, answer null to cancel
     * the operation.
     */
    String result;
    if( event.doit && !isDisposed() ) {
      result = event.text;
    } else {
      return null;
    }
    return result;
  }

  private void resetSelection() {
    selection.x = 0;
    selection.y = 0;
  }

  private void adjustSelection() {
    selection.x = Math.min( selection.x, text.length() );
    selection.y = Math.min( selection.y, text.length() );
  }

  private BoxDimensions getFieldPadding() {
    return getThemeAdapter().getFieldPadding( this );
  }

  private int getButtonWidth() {
    return getThemeAdapter().getButtonWidth( this );
  }

  private ComboThemeAdapter getThemeAdapter() {
    return ( ComboThemeAdapter )getAdapter( ThemeAdapter.class );
  }

  private static int checkStyle( int style ) {
    int result = style;
    /*
     * Feature in Windows.  It is not possible to create
     * a combo box that has a border using Windows style
     * bits.  All combo boxes draw their own border and
     * do not use the standard Windows border styles.
     * Therefore, no matter what style bits are specified,
     * clear the BORDER bits so that the SWT style will
     * match the Windows widget.
     *
     * The Windows behavior is currently implemented on
     * all platforms.
     */
    result &= ~SWT.BORDER;

    /*
     * Even though it is legal to create this widget
     * with scroll bars, they serve no useful purpose
     * because they do not automatically scroll the
     * widget's client area.  The fix is to clear
     * the SWT style.
     */
    result &= ~( SWT.H_SCROLL | SWT.V_SCROLL );
    result = checkBits( result, SWT.DROP_DOWN, SWT.SIMPLE, 0, 0, 0, 0 );
    if( ( result & SWT.SIMPLE ) != 0 ) {
      return result & ~SWT.READ_ONLY;
    }
    return result;
  }
}
