/*******************************************************************************
 * Copyright (c) 2008, 2016 Innoopract Informationssysteme GmbH and others.
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

import static org.eclipse.swt.internal.widgets.MarkupUtil.isMarkupEnabledFor;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ExpandAdapter;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.IExpandBarAdapter;
import org.eclipse.swt.internal.widgets.IItemHolderAdapter;
import org.eclipse.swt.internal.widgets.ItemHolder;
import org.eclipse.swt.internal.widgets.expandbarkit.ExpandBarLCA;


/**
 * Instances of this class support the layout of selectable expand bar items.
 * <p>
 * The item children that may be added to instances of this class must be of
 * type <code>ExpandItem</code>.
 * </p>
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>V_SCROLL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Expand, Collapse</dd>
 * </dl>
 * </p>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see ExpandItem
 * @see ExpandEvent
 * @see ExpandListener
 * @see ExpandAdapter
 * @since 1.2
 */
public class ExpandBar extends Composite {

  ExpandItem focusItem;
  int spacing;
  int allItemsHeight;
  private transient IExpandBarAdapter expandBarAdapter;
  private final ItemHolder<ExpandItem> itemHolder;
  private final ResizeListener resizeListener;

  /**
   * Constructs a new instance of this class given its parent and a style value
   * describing its behavior and appearance.
   * <p>
   * The style value is either one of the style constants defined in class
   * <code>SWT</code> which is applicable to instances of this class, or must
   * be built by <em>bitwise OR</em>'ing together (that is, using the
   * <code>int</code> "|" operator) two or more of those <code>SWT</code>
   * style constants. The class description lists the style constants that are
   * applicable to the class. Style bits are also inherited from superclasses.
   * </p>
   *
   * @param parent a composite control which will be the parent of the new
   *          instance (cannot be null)
   * @param style the style of control to construct
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the parent</li>
   *              <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
   *              subclass</li>
   *              </ul>
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public ExpandBar( Composite parent, int style ) {
    super( parent, checkStyle( style ) );
    spacing = 4;
    resizeListener = new ResizeListener();
    addControlListener( resizeListener );
    itemHolder = new ItemHolder<>( ExpandItem.class );
  }

  /**
   * Adds the listener to the collection of listeners who will be notified when
   * an item in the receiver is expanded or collapsed by sending it one of the
   * messages defined in the <code>ExpandListener</code> interface.
   *
   * @param listener the listener which should be notified
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @see ExpandListener
   * @see #removeExpandListener
   */
  public void addExpandListener( ExpandListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.Expand, typedListener );
    addListener( SWT.Collapse, typedListener );
  }

  /**
   * Removes the listener from the collection of listeners who will be notified
   * when items in the receiver are expanded or collapsed.
   *
   * @param listener the listener which should no longer be notified
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @see ExpandListener
   * @see #addExpandListener
   */
  public void removeExpandListener( ExpandListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    removeListener( SWT.Expand, listener );
    removeListener( SWT.Collapse, listener );
  }

  @Override
  public Point computeSize( int wHint, int hHint, boolean changed ) {
    checkWidget();
    int height = 0, width = 0;
    int itemCount = getItemCount();
    if( wHint == SWT.DEFAULT || hHint == SWT.DEFAULT ) {
      if( itemCount > 0 ) {
        height += spacing;
        for( int i = 0; i < itemCount; i++ ) {
          ExpandItem item = getItem( i );
          BoxDimensions itemBorder = item.getItemBorder();
          height += item.getHeaderHeight() + itemBorder.top + itemBorder.bottom;
          if( item.expanded ) {
            height += item.height;
          }
          height += spacing;
          int barPreferredWidth = item.getPreferredWidth() + 2 * spacing;
          width = Math.max( width, barPreferredWidth );
        }
      }
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
    width += border.left + border.right;
    height += border.top + border.bottom;
    if( ( style & SWT.V_SCROLL ) != 0 ) {
      width += getVerticalBar().getSize().x;
    }
    return new Point( width, height );
  }

  void createItem( ExpandItem item, int index ) {
    itemHolder.insert( item, index );
    if( focusItem == null ) {
      focusItem = item;
    }
    layoutItems( index );
  }

  void destroyItem( ExpandItem item ) {
    int index = 0;
    int itemCount = getItemCount();
    for( int i = 0; i < itemCount; i++ ) {
      if( getItem( i ) == item ) {
        index = i;
      }
    }
    if( index != itemCount ) {
      if( item == focusItem ) {
        int focusIndex = index > 0
                                  ? index - 1
                                  : 1;
        if( focusIndex < itemCount ) {
          focusItem = getItem( focusIndex );
        } else {
          focusItem = null;
        }
      }
      itemHolder.remove( item );
      layoutItems( index );
    }
  }

  @Override
  Control findBackgroundControl() {
    Control control = super.findBackgroundControl();
    if( !isAppThemed() ) {
      if( control == null ) {
        control = this;
      }
    }
    return control;
  }

  /**
   * Returns the item at the given, zero-relative index in the receiver. Throws
   * an exception if the index is out of range.
   *
   * @param index the index of the item to return
   * @return the item at the given index
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_INVALID_RANGE - if the index is not between 0 and
   *              the number of elements in the list minus 1 (inclusive)</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public ExpandItem getItem( int index ) {
    checkWidget();
    return itemHolder.getItem( index );
  }

  /**
   * Returns the number of items contained in the receiver.
   *
   * @return the number of items
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public int getItemCount() {
    checkWidget();
    return itemHolder.size();
  }

  /**
   * Returns an array of <code>ExpandItem</code>s which are the items in the
   * receiver.
   * <p>
   * Note: This is not the actual structure used by the receiver to maintain its
   * list of items, so modifying the array will not affect the receiver.
   * </p>
   *
   * @return the items in the receiver
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public ExpandItem[] getItems() {
    checkWidget();
    return itemHolder.getItems();
  }

  /**
   * Returns the receiver's spacing.
   *
   * @return the spacing
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public int getSpacing() {
    checkWidget();
    return spacing;
  }

  /**
   * Searches the receiver's list starting at the first item (index 0) until an
   * item is found that is equal to the argument, and returns the index of that
   * item. If no item is found, returns -1.
   *
   * @param item the search item
   * @return the index of the item
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the item is null</li>
   *              <li>ERROR_INVALID_ARGUMENT - if the item has been disposed</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public int indexOf( ExpandItem item ) {
    checkWidget();
    if( item == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( item.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    return itemHolder.indexOf( item );
  }

  void layoutItems( int index ) {
    if( !isInDispose() ) {
      int itemCount = getItemCount();
      if( index < itemCount ) {
        int y = spacing;
        for( int i = 0; i < index; i++ ) {
          ExpandItem item = getItem( i );
          if( item.expanded ) {
            y += item.height;
          }
          BoxDimensions itemBorder = item.getItemBorder();
          y += item.getHeaderHeight() + itemBorder.top + itemBorder.bottom + spacing;
        }
        for( int i = index; i < itemCount; i++ ) {
          ExpandItem item = getItem( i );
          item.setBounds( spacing, y, 0, 0, true, false );
          if( item.expanded ) {
            y += item.height;
          }
          BoxDimensions itemBorder = item.getItemBorder();
          y += item.getHeaderHeight() + itemBorder.top + itemBorder.bottom + spacing;
        }
      }
      // Calculate all items size
      if( itemCount > 0 ) {
        ExpandItem lastItem = getItem( itemCount - 1 );
        allItemsHeight = lastItem.y + lastItem.getBounds().height;
      }
      updateScrollBars();
      // Set items width based on scrollbar visibility
      Rectangle bounds = getBounds();
      BoxDimensions border = getBorder();
      int scrollBarWidth = getVScrollBarWidth();
      for( int i = 0; i < itemCount; i++ ) {
        ExpandItem item = getItem( i );
        Rectangle itemBounds = item.getBounds();
        if( isVScrollBarVisible() ) {
          if( this.getOrientation() == SWT.RIGHT_TO_LEFT ) {
            itemBounds.x = spacing + scrollBarWidth;
          }
          int width = bounds.width - scrollBarWidth - ( border.left + border.right ) - 2 * spacing;
          item.setBounds( itemBounds.x, itemBounds.y, width, item.height, true, true );
        } else {
          int width = bounds.width - ( border.left + border.right ) - 2 * spacing;
          item.setBounds( spacing, itemBounds.y, width, item.height, true, true );
        }
      }
    }
  }

  /**
   * Sets the receiver's spacing. Spacing specifies the number of pixels
   * allocated around each item.
   *
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setSpacing( int spacing ) {
    checkWidget();
    if( spacing >= 0 ) {
      if( spacing != this.spacing ) {
        this.spacing = spacing;
        layoutItems( 0 );
      }
    }
  }

  @Override
  public void setOrientation( int orientation ) {
    super.setOrientation( orientation );
    layoutItems( 0 );
  }

  @Override
  public void setData( String key, Object value ) {
    if( !RWT.MARKUP_ENABLED.equals( key ) || !isMarkupEnabledFor( this ) ) {
      super.setData( key, value );
    }
  }

  void showItem( ExpandItem item ) {
    Control control = item.control;
    if( control != null && !control.isDisposed() ) {
      control.setVisible( item.expanded );
    }
    int index = indexOf( item );
    layoutItems( index + 1 );
  }

  @Override
  protected void checkSubclass() {
    if( !isValidSubclass() ) {
      error( SWT.ERROR_INVALID_SUBCLASS );
    }
  }

  static int checkStyle( int style ) {
    int aStyle = style & ~SWT.H_SCROLL;
    return aStyle;
  }

  boolean isAppThemed() {
    return false;
  }

  private void updateScrollBars() {
    ScrollBar vScroll = getVerticalBar();
    if( vScroll != null ) {
      BoxDimensions border = getBorder();
      int availableHeight = getBounds().height - ( border.top + border.bottom ) - spacing;
      vScroll.setVisible( allItemsHeight > availableHeight );
    }
  }

  private boolean isVScrollBarVisible() {
    ScrollBar vScroll = getVerticalBar();
    return vScroll != null && vScroll.getVisible();
  }

  Rectangle getBottomSpacingBounds() {
    return new Rectangle( spacing, allItemsHeight, 10, spacing );
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == IItemHolderAdapter.class ) {
      return ( T )itemHolder;
    }
    if( adapter == IExpandBarAdapter.class ) {
      if( expandBarAdapter == null ) {
        expandBarAdapter = new ExpandBarAdapter();
      }
      return ( T )expandBarAdapter;
    }
    if( adapter == WidgetLCA.class ) {
      return ( T )ExpandBarLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  /////////////////////
  // Destroy expand bar

  @Override
  void releaseWidget() {
    if( resizeListener != null ) {
      removeControlListener( resizeListener );
    }
    super.releaseWidget();
  }

  @Override
  void releaseChildren() {
    Item[] expandItems = new ExpandItem[ getItemCount() ];
    System.arraycopy( getItems(), 0, expandItems, 0, getItems().length );
    for( int i = 0; i < expandItems.length; i++ ) {
      if( expandItems[ i ] != null ) {
        expandItems[ i ].dispose();
      }
    }
  }

  ////////////////////////////
  // Helping methods - various

  @Override
  int getVScrollBarWidth() {
    int result = 0;
    if( ( style & SWT.V_SCROLL ) != 0 ) {
      result = getVerticalBar().getSize().x;
    }
    return result;
  }

  ///////////////////
  // Skinning support

  @Override
  void reskinChildren( int flags ) {
    ExpandItem[] items = getItems();
    if( items != null ) {
      for( int i = 0; i < items.length; i++ ) {
        ExpandItem item = items[ i ];
        if( item != null ) {
          item.reskin( flags );
        }
      }
    }
    super.reskinChildren( flags );
  }

  private final class ExpandBarAdapter implements IExpandBarAdapter {

    @Override
    public Rectangle getBounds( ExpandItem item ) {
      return item.getBounds();
    }

    @Override
    public Rectangle getBottomSpacingBounds() {
      return ExpandBar.this.getBottomSpacingBounds();
    }

  }

  private final class ResizeListener extends ControlAdapter {

    @Override
    public void controlResized( ControlEvent event ) {
      layoutItems( 0 );
    }

  }

}
