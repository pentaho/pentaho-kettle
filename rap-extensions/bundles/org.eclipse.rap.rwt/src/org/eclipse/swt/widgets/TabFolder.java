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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.IItemHolderAdapter;
import org.eclipse.swt.internal.widgets.ItemHolder;
import org.eclipse.swt.internal.widgets.tabfolderkit.TabFolderLCA;
import org.eclipse.swt.internal.widgets.tabfolderkit.TabFolderThemeAdapter;

/**
 * Instances of this class implement the notebook user interface
 * metaphor.  It allows the user to select a notebook page from
 * set of pages.
 * <p>
 * The item children that may be added to instances of this class
 * must be of type <code>TabItem</code>.
 * <code>Control</code> children are created and then set into a
 * tab item using <code>TabItem#setControl</code>.
 * </p><p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to set a layout on it.
 * </p><p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>TOP, BOTTOM</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles TOP and BOTTOM may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * @since 1.0
 */
public class TabFolder extends Composite {

  private static final TabItem[] EMPTY_TAB_ITEMS = new TabItem[ 0 ];
  private final ItemHolder<TabItem> itemHolder;
  private int selectionIndex;
  private final boolean onBottom;

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
   * @see SWT
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public TabFolder( Composite parent, int style ) {
    super( parent, checkStyle( style ) );
    itemHolder = new ItemHolder<>( TabItem.class );
    selectionIndex = -1;
    onBottom = ( super.getStyle() & SWT.BOTTOM ) != 0;
  }

  @Override
  void initState() {
    removeState( /* CANVAS | */ THEME_BACKGROUND );
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == IItemHolderAdapter.class ) {
      return ( T )itemHolder;
    }
    if( adapter == WidgetLCA.class ) {
      return ( T )TabFolderLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  //////////////////
  // Item management

  /**
   * Returns an array of <code>TabItem</code>s which are the items
   * in the receiver.
   * <p>
   * Note: This is not the actual structure used by the receiver
   * to maintain its list of items, so modifying the array will
   * not affect the receiver.
   * </p>
   *
   * @return the items in the receiver
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public TabItem[] getItems() {
    checkWidget();
    return itemHolder.getItems();
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
  public TabItem getItem( int index ) {
    checkWidget();
    return itemHolder.getItem( index );
  }

  /**
   * Returns the tab item at the given point in the receiver
   * or null if no such item exists. The point is in the
   * coordinate system of the receiver.
   *
   * @param point the point used to locate the item
   * @return the tab item at the given point, or null if the point is not in a tab item
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
  public TabItem getItem( Point point ) {
    checkWidget();
    if( point == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    TabItem result = null;
    for( int i = 0; i < getItemCount() && result == null; i++ ) {
      TabItem item = getItem( i );
      Rectangle itemBounds = item.getBounds();
      if( itemBounds.contains( point  ) ) {
        result = item;
      }
    }
    return result;
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
    return itemHolder.size();
  }

  /**
   * Searches the receiver's list starting at the first item
   * (index 0) until an item is found that is equal to the
   * argument, and returns the index of that item. If no item
   * is found, returns -1.
   *
   * @param item the search item
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
  public int indexOf( TabItem item ) {
    checkWidget();
    if( item == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( item.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    return itemHolder.indexOf( item );
  }

  /////////////////////
  // Seletion handling

  /**
   * Returns an array of <code>TabItem</code>s that are currently
   * selected in the receiver. An empty array indicates that no
   * items are selected.
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
  public TabItem[] getSelection() {
    checkWidget();
    TabItem[] result = EMPTY_TAB_ITEMS;
    if( getSelectionIndex() != -1 ) {
      TabItem selected = itemHolder.getItem( getSelectionIndex() );
      result = new TabItem[]{
        selected
      };
    }
    return result;
  }

  /**
   * Sets the receiver's selection to the given item.
   * The current selected is first cleared, then the new item is
   * selected.
   *
   * @param item the item to select
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the item is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setSelection( TabItem item ) {
    checkWidget();
    if( item == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    setSelection( new TabItem[]{ item } );
  }

  /**
   * Sets the receiver's selection to be the given array of items.
   * The current selected is first cleared, then the new items are
   * selected.
   *
   * @param items the array of items
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the items array is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setSelection( TabItem[] items ) {
    checkWidget();
    if( items == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( items.length == 0 ) {
      setSelection( -1, false );
    } else {
      for( int i = items.length - 1; i >= 0; --i ) {
        int index = indexOf( items[ i ] );
        if( index != -1 ) {
          setSelection( index, false );
        }
      }
    }
  }

  /**
   * Selects the item at the given zero-relative index in the receiver.
   * If the item at the index was already selected, it remains selected.
   * The current selection is first cleared, then the new items are
   * selected. Indices that are out of range are ignored.
   *
   * @param index the index of the item to select
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setSelection( int index ) {
    checkWidget ();
    if( index >= 0 && index < itemHolder.size() ) {
      setSelection( index, false );
    }
  }

  /**
   * Returns the zero-relative index of the item which is currently
   * selected in the receiver, or -1 if no item is selected.
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
    if( selectionIndex >= itemHolder.size() ) {
      selectionIndex = itemHolder.size() - 1;
    }
    return selectionIndex;
  }

  ///////////////////////////////
  // Layout and size computations

  @Override
  public void layout() {
    checkWidget();
    Control[] children = getChildren();
    for( int i = 0; i < children.length; i++ ) {
      children[ i ].setBounds( getClientArea() );
    }
  }

  @Override
  public Rectangle getClientArea() {
    checkWidget();
    Rectangle bounds = getBounds();
    BoxDimensions border = getBorder();
    int borderWidth = border.left + border.right;
    int borderHeight = border.top + border.bottom;
    BoxDimensions containerBorder = getContentContainerBorder();
    int containerBorderWidth = containerBorder.left + containerBorder.right;
    int containerBorderHeight = containerBorder.top + containerBorder.bottom;
    int tabBarHeight = getTabBarHeight();
    int x = border.left + containerBorder.left;
    int y = border.top + containerBorder.top + ( onBottom ? 0 : tabBarHeight );
    int width = bounds.width - borderWidth - containerBorderWidth;
    int height = bounds.height - tabBarHeight - borderHeight - containerBorderHeight;
    return new Rectangle( x, y, width, height );
  }

  @Override
  public Rectangle computeTrim( int x, int y, int width, int height ) {
    checkWidget();
    BoxDimensions border = getBorder();
    int borderWidth = border.left + border.right;
    int borderHeight = border.top + border.bottom;
    BoxDimensions containerBorder = getContentContainerBorder();
    int containerBorderWidth = containerBorder.left + containerBorder.right;
    int containerBorderHeight = containerBorder.top + containerBorder.bottom;
    int tabBarHeight = getTabBarHeight();
    int trimX = x - border.left - containerBorder.left;
    int trimY = y - border.top - containerBorder.top - ( onBottom ? 0 : tabBarHeight );
    int trimWidth = width + borderWidth + containerBorderWidth;
    int trimHeight = height + borderHeight + containerBorderHeight + tabBarHeight;
    return new Rectangle( trimX, trimY, trimWidth, trimHeight );
  }

  @Override
  public Point computeSize( int wHint, int hHint, boolean changed ) {
    checkWidget();
    Point itemsSize = new Point( 0, 0 );
    Point contentsSize = new Point( 0, 0 );
    TabItem[] items = getItems();
    // TODO: one item should be enough since layout already includes all items
    for( int i = 0; i < items.length; i++ ) {
      Rectangle thisItemBounds = items[ i ].getBounds();
      itemsSize.x += thisItemBounds.width;
      itemsSize.y = Math.max( itemsSize.y, thisItemBounds.height );
      Control control = items[ i ].getControl();
      if( control != null ) {
        Point thisSize = control.computeSize( SWT.DEFAULT, SWT.DEFAULT );
        contentsSize.x = Math.max( contentsSize.x, thisSize.x );
        contentsSize.y = Math.max( contentsSize.y, thisSize.y );
      }
    }
    int width = Math.max( itemsSize.x, contentsSize.x );
    int height = itemsSize.y + contentsSize.y;
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
    return new Point( width, height );
  }

  ///////////////////////////////////////
  // Listener registration/deregistration

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the receiver's selection changes, by sending
   * it one of the messages defined in the <code>SelectionListener</code>
   * interface.
   * <p>
   * When <code>widgetSelected</code> is called, the item field of the event object is valid.
   * <code>widgetDefaultSelected</code> is not called.
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
  void releaseChildren() {
    TabItem[] items = getItems();
    for( int i = 0; i < items.length; i++ ) {
      items[ i ].dispose();
    }
    super.releaseChildren();
  }

  void createItem( TabItem item, int index ) {
    itemHolder.insert( item, index );
    if( getItemCount() == 1 ) {
      setSelection( 0, true );
    } else if( index <= selectionIndex ) {
      setSelection( selectionIndex + 1, false );
    }
  }

  void destroyItem( TabItem item ) {
    int index = itemHolder.indexOf( item );
    int oldSelectionIndex = getSelectionIndex();
    if( index == oldSelectionIndex ) {
      setSelection( -1, false );
    }
    itemHolder.remove( item );
    if( itemHolder.size() > 0 && index <= oldSelectionIndex && !isInDispose() ) {
      boolean notifySelectionChanged = index == oldSelectionIndex;
      setSelection( Math.max( 0, oldSelectionIndex - 1 ), notifySelectionChanged );
    }
  }

  @Override
  void notifyResize( Point oldSize ) {
    super.notifyResize( oldSize );
    updateSelectedItemControl();
  }

  private void updateSelectedItemControl() {
    int index = getSelectionIndex();
    if( index != -1 ) {
      Control control = getItem( index ).getControl();
      if( control != null && !control.isDisposed() ) {
        control.setBounds( getClientArea() );
        control.setVisible( true );
      }
    }
  }

  private void setSelection( int index, boolean notify ) {
    int oldIndex = getSelectionIndex();
    if( oldIndex != index ) {
      if( oldIndex != -1 ) {
        TabItem item = itemHolder.getItem( oldIndex );
        Control control = item.getControl();
        if( control != null && !control.isDisposed() ) {
          control.setVisible( false );
        }
      }
      selectionIndex = index;
      if( index != -1 ) {
        updateSelectedItemControl();
        if( notify ) {
          Event event = new Event();
          event.item = itemHolder.getItem( index );
          notifyListeners( SWT.Selection, event );
        }
      }
    }
  }

  private static int checkStyle( int style ) {
    int result = checkBits( style, SWT.TOP, SWT.BOTTOM, 0, 0, 0, 0 );
    /*
    * Even though it is legal to create this widget
    * with scroll bars, they serve no useful purpose
    * because they do not automatically scroll the
    * widget's client area.  The fix is to clear
    * the SWT style.
    */
    return result & ~( SWT.H_SCROLL | SWT.V_SCROLL );
  }

  private BoxDimensions getContentContainerBorder() {
    return getThemeAdapter().getContentContainerBorder( this );
  }

  private int getTabBarHeight() {
    int result = 0;
    TabFolderThemeAdapter themeAdapter = getThemeAdapter();
    int textHeight = TextSizeUtil.getCharHeight( getFont() );
    for( TabItem item : getItems() ) {
      Image image = item.getImage();
      int imageHeight = image == null ? 0 : image.getBounds().height;
      BoxDimensions itemPadding = themeAdapter.getItemPadding( item );
      int paddingHeight = itemPadding.top + itemPadding.bottom;
      BoxDimensions itemMargin = themeAdapter.getItemMargin( item );
      int marginHeight = itemMargin.top + itemMargin.bottom;
      BoxDimensions itemBorder = themeAdapter.getItemBorder( item );
      int borderHeight = itemBorder.top + itemBorder.bottom;
      int itemHeight = Math.max( textHeight, imageHeight )
                     + paddingHeight
                     + marginHeight
                     + borderHeight;
      result = Math.max( result, itemHeight );
    }
    return result;
  }

  TabFolderThemeAdapter getThemeAdapter() {
    return ( TabFolderThemeAdapter )getAdapter( ThemeAdapter.class );
  }

  @Override
  void reskinChildren( int flags ) {
    TabItem[] items = getItems();
    if( items != null ) {
      for( int i = 0; i < items.length; i++ ) {
        TabItem item = items[ i ];
        if( item != null ) {
          item.reskin( flags );
        }
      }
    }
    super.reskinChildren( flags );
  }

}
