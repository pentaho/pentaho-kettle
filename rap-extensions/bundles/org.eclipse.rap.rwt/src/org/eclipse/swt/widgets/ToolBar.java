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
import org.eclipse.rap.rwt.internal.theme.ThemeAdapter;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.IItemHolderAdapter;
import org.eclipse.swt.internal.widgets.ItemHolder;
import org.eclipse.swt.internal.widgets.toolbarkit.ToolBarLCA;
import org.eclipse.swt.internal.widgets.toolbarkit.ToolBarThemeAdapter;

/**
 * Instances of this class support the layout of selectable
 * tool bar items.
 * <p>
 * The item children that may be added to instances of this class
 * must be of type <code>ToolItem</code>.
 * </p><p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to add <code>Control</code> children to it,
 * or set a layout on it.
 * </p><p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>FLAT, <!-- WRAP, --> RIGHT, HORIZONTAL, VERTICAL<!--, SHADOW_OUT--></dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles HORIZONTAL and VERTICAL may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * @since 1.0
 */
public class ToolBar extends Composite {

  private static final int DEFAULT_TOOLBAR_WIDTH = 24;
  private static final int DEFAULT_TOOLBAR_HEIGHT = 22;

  private final ItemHolder<ToolItem> itemHolder;

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
   * @see SWT#FLAT
   *<!-- @see SWT#WRAP -->
   *<!-- @see SWT#RIGHT -->
   * @see SWT#HORIZONTAL
   *<!-- @see SWT#SHADOW_OUT -->
   * @see SWT#VERTICAL
   * <!--@see Widget#checkSubclass()-->
   * @see Widget#getStyle()
   */
  public ToolBar( Composite parent, int style ) {
    super( parent, checkStyle( style ) );
    /*
     * Ensure that either of HORIZONTAL or VERTICAL is set. NOTE: HORIZONTAL and
     * VERTICAL have the same values as H_SCROLL and V_SCROLL so it is necessary
     * to first clear these bits to avoid scroll bars and then reset the bits
     * using the original style supplied by the programmer.
     */
    if( ( style & SWT.VERTICAL ) != 0 ) {
      this.style |= SWT.VERTICAL;
    } else {
      this.style |= SWT.HORIZONTAL;
    }
    itemHolder = new ItemHolder<>( ToolItem.class );
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == IItemHolderAdapter.class ) {
      return ( T )itemHolder;
    }
    if( adapter == WidgetLCA.class ) {
      return ( T )ToolBarLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  //////////////////
  // Item management

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
  public ToolItem getItem( int index ) {
    checkWidget();
    return itemHolder.getItem( index );
  }

  /**
   * Returns the item at the given point in the receiver
   * or null if no such item exists. The point is in the
   * coordinate system of the receiver.
   *
   * @param point the point used to locate the item
   * @return the item at the given point
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   * @since 1.3
   */
  public ToolItem getItem( Point point ) {
    checkWidget();
    if( point == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    ToolItem result = null;
    ToolItem[] items = getItems();
    for( int i = 0; result == null && i < items.length; i++ ) {
      Rectangle rect = items[ i ].getBounds();
      if( rect.contains( point ) ) {
        result = items[ i ];
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
   * Returns an array of <code>ToolItem</code>s which are the items
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
  public ToolItem[] getItems() {
    checkWidget();
    return itemHolder.getItems();
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
   *    <li>ERROR_NULL_ARGUMENT - if the tool item is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the tool item has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int indexOf( ToolItem item ) {
    checkWidget();
    if( item == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( item.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    return itemHolder.indexOf( item );
  }

  ////////////////////
  // Size computations

  @Override
  public Point computeSize( int wHint, int hHint, boolean changed ) {
    checkWidget();
    int width = 0;
    int height = 0;
    if( ( style & SWT.VERTICAL ) != 0 ) {
      for( int i = 0; i < itemHolder.size(); i++ ) {
        ToolItem item = itemHolder.getItem( i );
        Rectangle itemBounds = item.getBounds();
        width = Math.max( width, itemBounds.width );
        if( i == itemHolder.size() - 1 ) {
          height += itemBounds.height + itemBounds.y;
        }
      }
    } else {
      for( int i = 0; i < itemHolder.size(); i++ ) {
        ToolItem item = itemHolder.getItem( i );
        Rectangle itemBounds = item.getBounds();
        height = Math.max( height, itemBounds.height );
        if( i == itemHolder.size() - 1 ) {
          width += itemBounds.width + itemBounds.x;
        }

      }
    }
    BoxDimensions toolBarPadding = getToolBarPadding();
    width += toolBarPadding.left + toolBarPadding.right;
    height += toolBarPadding.top + toolBarPadding.bottom;
    if( width == 0 ) {
      width = DEFAULT_TOOLBAR_WIDTH;
    }
    if( height == 0 ) {
      height = DEFAULT_TOOLBAR_HEIGHT;
    }
    if( wHint != SWT.DEFAULT ) {
      width = wHint;
    }
    if( hHint != SWT.DEFAULT ) {
      height = hHint;
    }
    for( int i = 0; i < itemHolder.size(); i++ ) {
      ToolItem item = itemHolder.getItem( i );
      item.resizeControl();
    }
    Rectangle trim = computeTrim( 0, 0, width, height );
    width = trim.width;
    height = trim.height;
    return new Point( width, height );
  }

  BoxDimensions getToolBarPadding() {
    ToolBarThemeAdapter themeAdapter = ( ToolBarThemeAdapter )getAdapter( ThemeAdapter.class );
    return themeAdapter.getToolBarPadding( this );
  }

  /**
   * Returns the number of rows in the receiver. When
   * the receiver has the <code>WRAP</code> style, the
   * number of rows can be greater than one.  Otherwise,
   * the number of rows is always one.
   *
   * @return the number of items
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getRowCount() {
    checkWidget();
    // return 1 as long as we don't support the WRAP style bit
    return 1;
  }

  @Override
  public void setBounds( Rectangle bounds ) {
    Point oldSize = getSize();
    super.setBounds( bounds );
    if( !oldSize.equals( getSize() ) ) {
      layoutItems();
    }
  }

  @Override
  public void setFont( Font font ) {
    super.setFont( font );
    layoutItems();
  }

  ////////////////////////
  // Child control removal

  @Override
  void removeChild( Control control ) {
    super.removeChild( control );
    ToolItem[] items = itemHolder.getItems();
    for( int i = 0; i < items.length; i++ ) {
      ToolItem item = items[ i ];
      if( item != null && item.getControl() == control ) {
        item.setControl( null );
      }
    }
  }

  ////////////////////
  // Widget overrides

  @Override
  final void releaseChildren() {
    ToolItem[] toolItems = itemHolder.getItems();
    for( int i = 0; i < toolItems.length; i++ ) {
      toolItems[ i ].dispose();
    }
  }

  //////////////////
  // Helping methods

  private static int checkStyle( int style ) {
    /*
    * Even though it is legal to create this widget
    * with scroll bars, they serve no useful purpose
    * because they do not automatically scroll the
    * widget's client area.  The fix is to clear
    * the SWT style.
    */
    return style & ~( SWT.H_SCROLL | SWT.V_SCROLL );
  }

  void createItem( ToolItem item, int index ) {
    itemHolder.insert( item, index );
    layoutItems();
  }

  void destroyItem( ToolItem item ) {
    itemHolder.remove( item );
    layoutItems();
  }

  void layoutItems() {
    for( int i = 0; i < itemHolder.size(); i++ ) {
      ToolItem item = itemHolder.getItem( i );
      Rectangle ibounds = item.getBounds();
      Rectangle bounds = getBounds();
      boolean hasEnoughWidth = ibounds.x + ibounds.width <= bounds.width;
      boolean hasEnoughHeight = ibounds.y + ibounds.height <= bounds.height;
      item.setVisible( hasEnoughWidth && hasEnoughHeight );
      item.resizeControl();
    }
  }

  ///////////////////
  // Skinning support

  @Override
  void reskinChildren( int flags ) {
    ToolItem[] items = getItems();
    if( items != null ) {
      for( int i = 0; i < items.length; i++ ) {
        ToolItem item = items[ i ];
        if( item != null ) {
          item.reskin( flags );
        }
      }
    }
    super.reskinChildren( flags );
  }

}
