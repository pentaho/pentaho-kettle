/*******************************************************************************
 * Copyright (c) 2002, 2017 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.custom;

import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.rap.rwt.internal.theme.ThemeAdapter;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.custom.ICTabFolderAdapter;
import org.eclipse.swt.internal.custom.ctabfolderkit.CTabFolderLCA;
import org.eclipse.swt.internal.custom.ctabfolderkit.CTabFolderThemeAdapter;
import org.eclipse.swt.internal.events.EventTypes;
import org.eclipse.swt.internal.widgets.IItemHolderAdapter;
import org.eclipse.swt.internal.widgets.IWidgetGraphicsAdapter;
import org.eclipse.swt.internal.widgets.ItemHolder;
import org.eclipse.swt.internal.widgets.WidgetGraphicsAdapter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TypedListener;


/**
 * Instances of this class implement the notebook user interface
 * metaphor.  It allows the user to select a notebook page from
 * set of pages.
 * <p>
 * The item children that may be added to instances of this class
 * must be of type <code>CTabItem</code>.
 * <code>Control</code> children are created and then set into a
 * tab item using <code>CTabItem#setControl</code>.
 * </p><p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to set a layout on it.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p><p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>CLOSE, TOP, BOTTOM, FLAT, BORDER, SINGLE, MULTI</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * <dd>"CTabFolder2"</dd>
 * </dl>
 * </p>
 * <p>
 * Note: Only one of the styles TOP and BOTTOM
 * may be specified.
 * </p>
 * <hr/>
 * <p>Implementation Status: </p>
 * <p>Attributes, found in SWT, that are not supported</p>
 * <ul>
 * <li>simple (treated as <code>true</code>)</li>
 * </ul>
 *
 * @since 1.0
 */
public class CTabFolder extends Composite {

  // internal constants
  static final int DEFAULT_WIDTH = 64;
  static final int DEFAULT_HEIGHT = 64;
  static final int BUTTON_SIZE = 18;

  static final int SELECTION_FOREGROUND = SWT.COLOR_LIST_FOREGROUND;
  static final int SELECTION_BACKGROUND = SWT.COLOR_LIST_BACKGROUND;
  static final int BORDER1_COLOR = SWT.COLOR_WIDGET_NORMAL_SHADOW;
  static final int FOREGROUND = SWT.COLOR_WIDGET_FOREGROUND;
  static final int BACKGROUND = SWT.COLOR_WIDGET_BACKGROUND;
  static final int BUTTON_BORDER = SWT.COLOR_WIDGET_DARK_SHADOW;
  static final int BUTTON_FILL = SWT.COLOR_LIST_BACKGROUND;

  /**
   * marginWidth specifies the number of pixels of horizontal margin
   * that will be placed along the left and right edges of the form.
   *
   * The default value is 0.
   */
  public int marginWidth = 0;

  /**
   * marginHeight specifies the number of pixels of vertical margin
   * that will be placed along the top and bottom edges of the form.
   *
   * The default value is 0.
   */
  public int marginHeight = 0;

  private transient ICTabFolderAdapter tabFolderAdapter;
  private final IWidgetGraphicsAdapter selectionGraphicsAdapter;
  private final ItemHolder<CTabItem> itemHolder = new ItemHolder<>( CTabItem.class );
  private final ControlListener resizeListener;
  private FocusListener focusListener;
  private Menu showMenu;
  int selectedIndex = -1;
  private int firstIndex = -1; // index of the left most visible tab
  private boolean mru;
  private int[] priority = new int[ 0 ];
  final boolean showClose;
  boolean showUnselectedClose = true;
  boolean showUnselectedImage = true;
  boolean showMax;
  boolean showMin;
  private boolean inDispose;
  boolean minimized;
  boolean maximized;
  boolean onBottom;
  final boolean simple = true; // no curvy tab items supported (yet)
  boolean single;
  private final Rectangle maxRect = new Rectangle( 0, 0, 0, 0 );
  private final Rectangle minRect = new Rectangle( 0, 0, 0, 0 );
  // Chevron
  private final Rectangle chevronRect = new Rectangle( 0, 0, 0, 0 );
  private boolean showChevron;
  // Tab bar
  private int fixedTabHeight = SWT.DEFAULT;
  int tabHeight = 0;
  int minChars = 20;
  // TopRight control
  Control topRight;
  private int topRightAlignment = SWT.RIGHT;
  private final Rectangle topRightRect = new Rectangle( 0, 0, 0, 0 );
  // Client origin and border dimensions
  private int xClient;
  private int yClient;
  private final int highlight_margin;
  private int highlight_header;
  private int borderRight;
  private int borderLeft;
  private int borderBottom;
  private int borderTop;
  // keep track of size changes in order to redraw only affected area
  // on Resize
  //  Point oldSize;
  // Colors
  private Color selectionBackground = null;
  private Color selectionForeground = null;
  private Image selectionBgImage = null;

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
   * @param parent a widget which will be the parent of the new instance (cannot be null)
   * @param style the style of widget to construct
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   * </ul>
   *
   * @see SWT#TOP
   * @see SWT#BOTTOM
   * @see SWT#FLAT
   * @see SWT#BORDER
   * @see SWT#SINGLE
   * @see SWT#MULTI
   * @see #getStyle()
   */
  public CTabFolder( Composite parent, int style ) {
    super( parent, checkStyle( style ) );
    super.setLayout( new CTabFolderLayout() );
    onBottom = ( super.getStyle() & SWT.BOTTOM ) != 0;
    single = ( super.getStyle() & SWT.SINGLE ) != 0;
    showClose = ( super.getStyle() & SWT.CLOSE ) != 0;
    borderRight = ( style & SWT.BORDER ) != 0 ? 1 : 0;
    borderLeft = borderRight;
    borderTop = onBottom ? borderLeft : 0;
    borderBottom = onBottom ? 0 : borderLeft;
    highlight_header = ( style & SWT.FLAT ) != 0 ? 1 : 3;
    highlight_margin = ( style & SWT.FLAT ) != 0 ? 0 : 2;
    updateTabHeight( false );
    resizeListener = new ControlAdapter() {
      @Override
      public void controlResized( ControlEvent event ) {
        onResize();
      }
    };
    addControlListener( resizeListener );
    registerDisposeListener();
    selectionGraphicsAdapter = new WidgetGraphicsAdapter();
  }

  //////////////////
  // Item management

  /**
   * Return the tab items.
   *
   * @return the tab items
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   *  </ul>
   */
  public CTabItem[] getItems() {
    checkWidget();
    return itemHolder.getItems();
  }

  /**
   * Return the tab that is located at the specified index.
   *
   * @param index the index of the tab item
   * @return the item at the specified index
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_RANGE - if the index is out of range</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   * </ul>
   */
  public CTabItem getItem( int index ) {
    checkWidget();
    return itemHolder.getItem( index );
  }

  /**
   * Gets the item at a point in the widget.
   *
   * @param pt the point in coordinates relative to the CTabFolder
   * @return the item at a point or null
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   *  </ul>
   * @since 1.2
   */
  public CTabItem getItem( Point pt ) {
    // checkWidget();
    CTabItem result = null;
    Point size = getSize();
    boolean onChevron = showChevron && chevronRect.contains( pt );
    int itemCount = itemHolder.size();
    if( itemCount > 0 && size.x > borderLeft + borderRight && !onChevron ) {
      CTabItem[] items = itemHolder.getItems();
      for( int i = 0; result == null && i < priority.length; i++ ) {
        CTabItem item = items[ priority[ i ] ];
        Rectangle rect = item.getBounds();
        if( rect.contains( pt ) ) {
          result = item;
        }
      }
    }
    return result ;
  }

  /**
   * Return the number of tabs in the folder.
   *
   * @return the number of tabs in the folder
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   *  </ul>
   */
  public int getItemCount() {
    checkWidget();
    return itemHolder.size();
  }

  /**
   * Return the index of the specified tab or -1 if the tab is not
   * in the receiver.
   *
   * @param item the tab item for which the index is required
   *
   * @return the index of the specified tab item or -1
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   * </ul>
   */
  public int indexOf( CTabItem item ) {
    checkWidget();
    return itemHolder.indexOf( item );
  }

  ///////////////////////
  // Selection management

  /**
   * Set the selection to the tab at the specified index.
   *
   * @param index the index of the tab item to be selected
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setSelection( int index ) {
    checkWidget();
    if( index >= 0 && index <= itemHolder.size() - 1 ) {
      if( selectedIndex != index ) {
        int oldSelectionIndex = selectedIndex;
        selectedIndex = index;
        getItem( selectedIndex ).showing = false;
        Control control = getItem( selectedIndex ).getControl();
        // Adjust bounds of selected control and make it visible (if any)
        if( control != null && !control.isDisposed() ) {
          control.setBounds( getClientArea() );
          control.setVisible( true );
        }
        // Hide control of previous selection (if any)
        if( oldSelectionIndex >= 0 && oldSelectionIndex < getItemCount() ) {
          Control oldControl = getItem( oldSelectionIndex ).getControl();
          if( oldControl != null && !oldControl.isDisposed() ) {
            oldControl.setVisible( false );
          }
        }
      }
      showItem( getSelection() );
    }
  }

  /**
   * Return the index of the selected tab item, or -1 if there
   * is no selection.
   *
   * @return the index of the selected tab item or -1
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   *  </ul>
   */
  public int getSelectionIndex() {
    checkWidget();
    return selectedIndex;
  }

  /**
   * Set the selection to the tab at the specified item.
   *
   * @param item the tab item to be selected
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   * </ul>
   */
  public void setSelection( CTabItem item ) {
    checkWidget();
    if( item == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    int index = itemHolder.indexOf( item );
    setSelection( index );
  }

  /**
   * Return the selected tab item, or null if there is no selection.
   *
   * @return the selected tab item, or null if none has been selected
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   *  </ul>
   */
  public CTabItem getSelection() {
    checkWidget();
    CTabItem result = null;
    if( selectedIndex != -1 ) {
      result = itemHolder.getItem( selectedIndex );
    }
    return result;
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
   * @see CTabFolder#showItem(CTabItem)
   */
  public void showSelection () {
    checkWidget();
    if( selectedIndex != -1 ) {
      showItem( getSelection() );
    }
  }

  /**
   * Shows the item.  If the item is already showing in the receiver,
   * this method simply returns.  Otherwise, the items are scrolled until
   * the item is visible.
   *
   * @param item the item to be shown
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the item is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the item has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see CTabFolder#showSelection()
   */
  public void showItem( CTabItem item ) {
    checkWidget();
    if( item == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( item.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    int index = indexOf( item );
    if( index == -1 ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    int idx = -1;
    for( int i = 0; idx == -1 && i < priority.length; i++ ) {
      if( priority[ i ] == index ) {
        idx = i;
      }
    }
    if( mru ) {
      // move to front of mru order
      int[] newPriority = new int[ priority.length ];
      System.arraycopy( priority, 0, newPriority, 1, idx );
      System.arraycopy( priority,
        idx + 1,
        newPriority,
        idx + 1,
        priority.length - idx - 1 );
      newPriority[ 0 ] = index;
      priority = newPriority;
    }
    if( !item.isShowing() ) {
      updateItems( index );
    }
  }

  //////////////////////////////
  // Most recently used settings

  /**
   * When there is not enough horizontal space to show all the tabs,
   * by default, tabs are shown sequentially from left to right in
   * order of their index.  When the MRU visibility is turned on,
   * the tabs that are visible will be the tabs most recently selected.
   * Tabs will still maintain their left to right order based on index
   * but only the most recently selected tabs are visible.
   * <p>
   * For example, consider a CTabFolder that contains "Tab 1", "Tab 2",
   * "Tab 3" and "Tab 4" (in order by index).  The user selects
   * "Tab 1" and then "Tab 3".  If the CTabFolder is now
   * compressed so that only two tabs are visible, by default,
   * "Tab 2" and "Tab 3" will be shown ("Tab 3" since it is currently
   * selected and "Tab 2" because it is the previous item in index order).
   * If MRU visibility is enabled, the two visible tabs will be "Tab 1"
   * and "Tab 3" (in that order from left to right).</p>
   *
   * @param show the new visibility state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setMRUVisible( boolean show ) {
    checkWidget();
    if( mru != show ) {
      mru = show;
      if( !mru ) {
        int idx = firstIndex;
        int next = 0;
        for( int i = firstIndex; i < priority.length; i++ ) {
          priority[ next++ ] = i;
        }
        for( int i = 0; i < idx; i++ ) {
          priority[ next++ ] = i;
        }
        updateItems();
      }
    }
  }

  /**
   * Returns <code>true</code> if the receiver displays most
   * recently used tabs and <code>false</code> otherwise.
   * <p>
   * When there is not enough horizontal space to show all the tabs,
   * by default, tabs are shown sequentially from left to right in
   * order of their index.  When the MRU visibility is turned on,
   * the tabs that are visible will be the tabs most recently selected.
   * Tabs will still maintain their left to right order based on index
   * but only the most recently selected tabs are visible.
   * <p>
   * For example, consider a CTabFolder that contains "Tab 1", "Tab 2",
   * "Tab 3" and "Tab 4" (in order by index).  The user selects
   * "Tab 1" and then "Tab 3".  If the CTabFolder is now
   * compressed so that only two tabs are visible, by default,
   * "Tab 2" and "Tab 3" will be shown ("Tab 3" since it is currently
   * selected and "Tab 2" because it is the previous item in index order).
   * If MRU visibility is enabled, the two visible tabs will be "Tab 1"
   * and "Tab 3" (in that order from left to right).</p>
   *
   * @return the receiver's header's visibility state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public boolean getMRUVisible() {
    checkWidget();
    return mru;
  }


  ////////////////////////////////
  // Minimize / Maximize / Restore

  /**
   * Marks the receiver's maximize button as visible if the argument is <code>true</code>,
   * and marks it invisible otherwise.
   *
   * @param maximizeVisible the new visibility state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setMaximizeVisible( boolean maximizeVisible ) {
    checkWidget();
    if( showMax != maximizeVisible ) {
      showMax = maximizeVisible;
      updateItems();
    }
  }

  /**
   * Returns <code>true</code> if the maximize button
   * is visible.
   *
   * @return the visibility of the maximized button
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public boolean getMaximizeVisible() {
    checkWidget();
    return showMax;
  }

  /**
   * Marks the receiver's minimize button as visible if the argument is <code>true</code>,
   * and marks it invisible otherwise.
   *
   * @param minimizeVisible the new visibility state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setMinimizeVisible( boolean minimizeVisible ) {
    checkWidget();
    if( showMin != minimizeVisible ) {
      showMin = minimizeVisible;
      updateItems();
    }
  }

  /**
   * Returns <code>true</code> if the minimize button
   * is visible.
   *
   * @return the visibility of the minimized button
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public boolean getMinimizeVisible() {
    checkWidget();
    return showMin;
  }

  /**
   * Marks the receiver's minimize button as visible if the argument is <code>true</code>,
   * and marks it invisible otherwise.
   *
   * @param minimized the new visibility state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setMinimized( boolean minimized ) {
    checkWidget();
    if( this.minimized != minimized ) {
      if( minimized && maximized ) {
        setMaximized( false );
      }
      this.minimized = minimized;
    }
  }

  /**
   * Returns <code>true</code> if the receiver is minimized.
   *
   * @return the receiver's minimized state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public boolean getMinimized() {
    checkWidget();
    return minimized;
  }

  /**
   * Sets the maximized state of the receiver.
   *
   * @param maximized the new maximized state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setMaximized( boolean maximized ) {
    checkWidget();
    if( this.maximized != maximized ) {
      if( maximized && minimized ) {
        setMinimized( false );
      }
      this.maximized = maximized;
    }
  }

  /**
   * Returns <code>true</code> if the receiver is maximized.
   * <p>
   *
   * @return the receiver's maximized state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public boolean getMaximized() {
    checkWidget();
    return maximized;
  }

  //////////////////////////////////////
  // Appearance and dimension properties

  /**
   * Sets the layout which is associated with the receiver to be
   * the argument which may be null.
   * <p>
   * Note: No Layout can be set on this Control because it already
   * manages the size and position of its children.
   * </p>
   *
   * @param layout the receiver's new layout or null
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  @Override
  public void setLayout( Layout layout ) {
    checkWidget();
    // ignore - CTabFolder manages its own layout
  }

  /**
   * Specify a fixed height for the tab items.  If no height is specified,
   * the default height is the height of the text or the image, whichever
   * is greater. Specifying a height of -1 will revert to the default height.
   *
   * @param height the pixel value of the height or -1
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_INVALID_ARGUMENT - if called with a height of less than 0</li>
   * </ul>
   */
  public void setTabHeight( int height ) {
    checkWidget();
    if( height < -1 ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    fixedTabHeight = height;
    updateTabHeight( false );
  }

  /**
   * Returns the height of the tab
   *
   * @return the height of the tab
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   *  </ul>
   */
  public int getTabHeight() {
    checkWidget();
    int result;
    if( fixedTabHeight != SWT.DEFAULT ) {
      result = fixedTabHeight;
    } else {
      result = tabHeight - 1; // -1 for line drawn across top of tab
    }
    return result;
  }

  /**
   * Returns the number of characters that will
   * appear in a fully compressed tab.
   *
   * @return number of characters that will appear in a fully compressed tab
   */
  public int getMinimumCharacters() {
    checkWidget();
    return minChars;
  }

  /**
   * Sets the minimum number of characters that will
   * be displayed in a fully compressed tab.
   *
   * @param minimumCharacters the minimum number of characters that will be displayed in a fully compressed tab
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_INVALID_RANGE - if the count is less than zero</li>
   * </ul>
   */
  public void setMinimumCharacters( int minimumCharacters ) {
    checkWidget();
    if( minimumCharacters < 0 ) {
      SWT.error( SWT.ERROR_INVALID_RANGE );
    }
    if( minChars != minimumCharacters ) {
      minChars = minimumCharacters;
      updateItems();
    }
  }

  @Override
  public int getStyle() {
    checkWidget();
    int result = super.getStyle();
    result &= ~( SWT.TOP | SWT.BOTTOM );
    result |= onBottom ? SWT.BOTTOM : SWT.TOP;
    result &= ~( SWT.SINGLE | SWT.MULTI );
    result |= single ? SWT.SINGLE : SWT.MULTI;
    if( borderLeft != 0 ) {
      result |= SWT.BORDER;
    }
    return result;
  }

  /**
   * Returns <code>true</code> if the CTabFolder only displays the selected tab
   * and <code>false</code> if the CTabFolder displays multiple tabs.
   *
   * @return <code>true</code> if the CTabFolder only displays the selected tab and <code>false</code> if the CTabFolder displays multiple tabs
   */
  public boolean getSingle() {
    checkWidget();
    return single;
  }

  /**
   * Sets the number of tabs that the CTabFolder should display
   *
   * @param single <code>true</code> if only the selected tab should be displayed otherwise, multiple tabs will be shown.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setSingle( boolean single ) {
    checkWidget();
    if( this.single != single ) {
      this.single = single;
      updateItemsWithResizeEvent();
    }
  }

  /**
   * Returns the position of the tab.  Possible values are SWT.TOP or SWT.BOTTOM.
   *
   * @return the position of the tab
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   *  </ul>
   */
  public int getTabPosition() {
    checkWidget();
    return onBottom ? SWT.BOTTOM : SWT.TOP;
  }

  /**
   * Specify whether the tabs should appear along the top of the folder
   * or along the bottom of the folder.
   *
   * @param position <code>SWT.TOP</code> for tabs along the top or <code>SWT.BOTTOM</code> for tabs along the bottom
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the position value is not either SWT.TOP or SWT.BOTTOM</li>
   * </ul>
   */
  public void setTabPosition( int position ) {
    checkWidget();
    if( position != SWT.TOP && position != SWT.BOTTOM ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    if( onBottom != ( position == SWT.BOTTOM ) ) {
      onBottom = position == SWT.BOTTOM;
      borderTop = onBottom ? borderLeft : 0;
      borderBottom = onBottom ? 0 : borderRight;
      updateTabHeight( true );
      updateItemsWithResizeEvent();
    }
  }

  /**
   * Returns <code>true</code> if the receiver's border is visible.
   *
   * @return the receiver's border visibility state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public boolean getBorderVisible() {
    checkWidget();
    return borderLeft == 1;
  }

  /**
   * Toggle the visibility of the border
   *
   * @param show true if the border should be displayed
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setBorderVisible( boolean show ) {
    checkWidget();
    if( ( borderLeft != 1 ) != !show ) {
      borderLeft = borderRight = show ? 1 : 0;
      borderTop = onBottom ? borderLeft : 0;
      borderBottom = onBottom ? 0 : borderLeft;
      updateItemsWithResizeEvent();
    }
  }

  /**
   * Returns <code>true</code> if an image appears
   * in unselected tabs.
   *
   * @return <code>true</code> if an image appears in unselected tabs
   *
   * @since 1.3
   */
  public boolean getUnselectedImageVisible() {
    checkWidget();
    return showUnselectedImage;
  }

  /**
   * Specify whether the image appears on unselected tabs.
   *
   * @param visible <code>true</code> makes the image appear
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public void setUnselectedImageVisible( boolean visible ) {
    checkWidget();
    if( showUnselectedImage != visible ) {
      showUnselectedImage = visible;
      updateItems();
    }
  }

  /**
   * Returns <code>true</code> if the close button appears
   * when the user hovers over an unselected tabs.
   *
   * @return <code>true</code> if the close button appears on unselected tabs
   */
  public boolean getUnselectedCloseVisible() {
    checkWidget();
    return showUnselectedClose;
  }

  /**
   * Specify whether the close button appears
   * when the user hovers over an unselected tabs.
   *
   * @param visible <code>true</code> makes the close button appear
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setUnselectedCloseVisible( boolean visible ) {
    checkWidget();
    if( showUnselectedClose != visible ) {
      showUnselectedClose = visible;
      updateItems();
    }
  }

  @Override
  public Rectangle computeTrim( int x, int y, int width, int height ) {
    checkWidget();
    int trimX = x - marginWidth - highlight_margin - borderLeft;
    int trimWidth = width
      + borderLeft
      + borderRight
      + 2 * marginWidth
      + 2 * highlight_margin;
    int trimY;
    int trimHeight;
    if( minimized ) {
      trimY = onBottom
        ? y - borderTop
        : y - highlight_header - tabHeight - borderTop;
      trimHeight = borderTop + borderBottom + tabHeight + highlight_header;
    } else {
      trimY = onBottom
        ? y - marginHeight - highlight_margin - borderTop
        : y - marginHeight - highlight_header - tabHeight - borderTop;
      trimHeight = height
        + borderTop
        + borderBottom
        + 2 * marginHeight
        + tabHeight
        + highlight_header
        + highlight_margin;
    }
    return new Rectangle( trimX, trimY, trimWidth, trimHeight );
  }


  ///////////////////
  // Selection colors

  /**
   * Sets the receiver's selection background color to the color specified
   * by the argument, or to the default system color for the control
   * if the argument is null.
   *
   * @param color the new color (or null)
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setSelectionBackground( Color color ) {
    checkWidget();
    if( null != color && color.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    selectionBackground = color;
  }

  /**
   * Returns the receiver's selection background color.
   *
   * @return the selection background color of the receiver
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Color getSelectionBackground() {
    checkWidget();
    Color result = selectionBackground;
    if( result == null ) {
      result = getThemeAdapter().getSelectedBackground();
    }
    if( result == null ) {
      // Should never happen as the theming must prevent transparency for
      // this color
      throw new IllegalStateException( "Transparent selection background color" );
    }
    return result;
  }

  /**
   * Specify a gradient of colours to be draw in the background of the selected tab.
   * For example to draw a gradient that varies from dark blue to blue and then to
   * white, use the following call to setBackground:
   * <pre>
   *  cfolder.setBackground(new Color[]{display.getSystemColor(SWT.COLOR_DARK_BLUE),
   *                               display.getSystemColor(SWT.COLOR_BLUE),
   *                               display.getSystemColor(SWT.COLOR_WHITE),
   *                               display.getSystemColor(SWT.COLOR_WHITE)},
   *                   new int[] {25, 50, 100});
   * </pre>
   *
   * @param colors an array of Color that specifies the colors to appear in the gradient
   *               in order of appearance left to right.  The value <code>null</code> clears the
   *               background gradient.
   * @param percents an array of integers between 0 and 100 specifying the percent of the width
   *                 of the widget at which the color should change.  The size of the percents array must be one
   *                 less than the size of the colors array.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   *  </ul>
   *  @since 1.3
   */
  public void setSelectionBackground( Color[] colors, int[] percents ) {
    setSelectionBackground( colors, percents, false );
  }
  /**
   * Specify a gradient of colours to be draw in the background of the selected tab.
   * For example to draw a vertical gradient that varies from dark blue to blue and then to
   * white, use the following call to setBackground:
   * <pre>
   *  cfolder.setBackground(new Color[]{display.getSystemColor(SWT.COLOR_DARK_BLUE),
   *                               display.getSystemColor(SWT.COLOR_BLUE),
   *                               display.getSystemColor(SWT.COLOR_WHITE),
   *                               display.getSystemColor(SWT.COLOR_WHITE)},
   *                      new int[] {25, 50, 100}, true);
   * </pre>
   *
   * @param colors an array of Color that specifies the colors to appear in the gradient
   *               in order of appearance top to bottom.  The value <code>null</code> clears the
   *               background gradient.
   * @param percents an array of integers between 0 and 100 specifying the percent of the width
   *                 of the widget at which the color should change.  The size of the percents array must be one
   *                 less than the size of the colors array.
   *
   * @param vertical indicate the direction of the gradient.  True is vertical and false is horizontal.
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   *  </ul>
   *
   * @since 1.3
   */
  public void setSelectionBackground( Color[] colors, int[] percents, boolean vertical ) {
    checkWidget();
    if( colors != null ) {
      for( int i = 0; i < colors.length; i++ ) {
        if( colors[ i ] != null && colors[ i ].isDisposed() ) {
          SWT.error( SWT.ERROR_INVALID_ARGUMENT );
        }
      }
      // The colors array can optionally have an extra entry which describes the
      // highlight top color.
      // Thus its either one or two larger than the percents array
      if( percents == null
        || !( ( percents.length == colors.length - 1 )
        || ( percents.length == colors.length - 2 ) ) )
      {
        SWT.error(SWT.ERROR_INVALID_ARGUMENT);
      }
      for( int i = 0; i < percents.length; i++ ) {
        if( percents[ i ] < 0 || percents[ i ] > 100 ) {
          SWT.error( SWT.ERROR_INVALID_ARGUMENT );
        }
        if( i > 0 && percents[ i ] < percents[ i - 1 ] ) {
          SWT.error( SWT.ERROR_INVALID_ARGUMENT );
        }
      }
    }

    if( colors == null ) {
      selectionGraphicsAdapter.setBackgroundGradient( null, null, vertical );
      setSelectionBackground( ( Color )null );
    } else {
      int colorsLength = colors.length;
      if( percents.length == colors.length - 2 ) {
        colorsLength = colors.length - 1 ;
      }
      Color[] gradientColors = new Color[ colorsLength ];
      System.arraycopy( colors, 0, gradientColors, 0, colorsLength );
      int[] gradientPercents = new int[ gradientColors.length ];
      if( gradientColors.length > 0 ) {
        gradientPercents[ 0 ] = 0;
        for( int i = 1; i < gradientPercents.length; i++ ) {
          gradientPercents[ i ] = percents[ i - 1 ];
        }
        selectionGraphicsAdapter.setBackgroundGradient( gradientColors,
          gradientPercents,
          vertical );
        setSelectionBackground( gradientColors[ gradientColors.length - 1 ] );
      }
    }
  }

  /**
   * Set the image to be drawn in the background of the selected tab.  Image
   * is stretched or compressed to cover entire selection tab area.
   *
   * @param image the image to be drawn in the background
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   * @since 1.3
   */
  public void setSelectionBackground( Image image ) {
    checkWidget();
    selectionBgImage = image;
  }

  /**
   * Set the foreground color of the selected tab.
   *
   * @param color the color of the text displayed in the selected tab
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setSelectionForeground( Color color ) {
    checkWidget();
    selectionForeground = color;
  }

  /**
   * Returns the receiver's selection foreground color.
   *
   * @return the selection foreground color of the receiver
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Color getSelectionForeground() {
    checkWidget();
    Color result = selectionForeground;
    if( result == null ) {
      result = getThemeAdapter().getSelectedForeground();
    }
    if( result == null ) {
      // Should never happen as the theming must prevent transparency for
      // this color
      throw new IllegalStateException( "Transparent selection foreground color" );
    }
    return result;
  }

  //////////////////////////////////
  // Manipulation of topRight control

  /**
   * Set the control that appears in the top right corner of the tab folder.
   * Typically this is a close button or a composite with a Menu and close button.
   * The topRight control is optional.  Setting the top right control to null will
   * remove it from the tab folder.
   *
   * @param control the control to be displayed in the top right corner or null
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the control is not a child of this CTabFolder</li>
   * </ul>
   */
  public void setTopRight( Control control ) {
    checkWidget();
    setTopRight( control, SWT.RIGHT );
  }

  /**
   * Set the control that appears in the top right corner of the tab folder.
   * Typically this is a close button or a composite with a Menu and close button.
   * The topRight control is optional.  Setting the top right control to null
   * will remove it from the tab folder.
   * <p>
   * The alignment parameter sets the layout of the control in the tab area.
   * <code>SWT.RIGHT</code> will cause the control to be positioned on the far
   * right of the folder and it will have its default size.  <code>SWT.FILL</code>
   * will size the control to fill all the available space to the right of the
   * last tab.  If there is no available space, the control will not be visible.
   * </p>
   *
   * @param control the control to be displayed in the top right corner or null
   * @param alignment <code>SWT.RIGHT</code> or <code>SWT.FILL</code>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the control is not a child of this CTabFolder</li>
   * </ul>
   */
  public void setTopRight( Control control, int alignment ) {
    checkWidget();
    if( alignment != SWT.RIGHT && alignment != SWT.FILL ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    if( control != null && control.getParent() != this ) {
      SWT.error( SWT.ERROR_INVALID_PARENT );
    }
    if( topRight != control || topRightAlignment != alignment ) {
      topRight = control;
      topRightAlignment = alignment;
      if( updateItems() ) {
        redraw();
      }
    }
  }

  /**
   * Returns the control in the top right corner of the tab folder.
   * Typically this is a close button or a composite with a menu and close button.
   *
   * @return the control in the top right corner of the tab folder or null
   *
   * @exception  SWTException <ul>
   *      <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *      <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   *  </ul>
   */
  public Control getTopRight() {
    checkWidget();
    return topRight;
  }

  /**
   * Returns the alignment of the top right control.
   *
   * @return the alignment of the top right control which is either
   * <code>SWT.RIGHT</code> or <code>SWT.FILL</code>
   *
   * @exception  SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   *  </ul>
   *
   * @since 1.3
   */
  public int getTopRightAlignment() {
    checkWidget();
    return topRightAlignment;
  }

  ///////////////////////////
  // Adaptable implementation

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == IItemHolderAdapter.class ) {
      return ( T )itemHolder;
    }
    if( adapter == ICTabFolderAdapter.class ) {
      if( tabFolderAdapter == null ) {
        tabFolderAdapter = new CTabFolderAdapter();
      }
      return ( T )tabFolderAdapter;
    }
    if( adapter == WidgetLCA.class ) {
      return ( T )CTabFolderLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  ////////////////////
  // Control overrides

  @Override
  public void setFont( Font font ) {
    checkWidget();
    if( font != getFont() ) {
      super.setFont( font );
      if( !updateTabHeight( false ) ) {
        updateItems();
      }
    }
  }

  @Override
  public int getBorderWidth() {
    checkWidget();
    return 0;
  }

  //////////////////////
  // Composite overrides

  @Override
  public Rectangle getClientArea() {
    checkWidget();
    Rectangle result;
    if( minimized ) {
      result = new Rectangle( xClient, yClient, 0, 0 );
    } else {
      Point size = getSize();
      int width =   size.x
        - borderLeft
        - borderRight
        - 2 * marginWidth
        - 2 * highlight_margin;
      int height =   size.y
        - borderTop
        - borderBottom
        - 2 * marginHeight
        - highlight_margin
        - highlight_header;
      height -= tabHeight;
      result = new Rectangle( xClient, yClient, width, height );
    }
    return result;
  }

  ///////////////////////////////////////
  // Listener registration/deregistration

  /**
   * Adds the listener to receive events.
   * <p>
   *
   * @param listener the listener
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   * </ul>
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
   * Removes the listener.
   *
   * @param listener the listener
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   * </ul>
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
   *
   * Adds the listener to the collection of listeners who will
   * be notified when a tab item is closed, minimized, maximized,
   * restored, or to show the list of items that are not
   * currently visible.
   *
   * @param listener the listener which should be notified
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   * </ul>
   *
   * @see CTabFolder2Listener
   * @see #removeCTabFolder2Listener(CTabFolder2Listener)
   */
  public void addCTabFolder2Listener( CTabFolder2Listener listener ) {
    checkWidget();
    if( listener == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedCTabFolderListener typedListener = new TypedCTabFolderListener( listener );
    addListener( EventTypes.CTAB_FOLDER_MINIMIZE, typedListener );
    addListener( EventTypes.CTAB_FOLDER_MAXIMIZE, typedListener );
    addListener( EventTypes.CTAB_FOLDER_RESTORE, typedListener );
    addListener( EventTypes.CTAB_FOLDER_CLOSE, typedListener );
    addListener( EventTypes.CTAB_FOLDER_SHOW_LIST, typedListener );
  }

  /**
   * Removes the listener.
   *
   * @param listener the listener
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   * </ul>
   *
   * @see #addCTabFolder2Listener(CTabFolder2Listener)
   */
  public void removeCTabFolder2Listener( CTabFolder2Listener listener ) {
    checkWidget();
    if( listener == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    removeListener( EventTypes.CTAB_FOLDER_MINIMIZE, listener );
    removeListener( EventTypes.CTAB_FOLDER_MAXIMIZE, listener );
    removeListener( EventTypes.CTAB_FOLDER_RESTORE, listener );
    removeListener( EventTypes.CTAB_FOLDER_CLOSE, listener );
    removeListener( EventTypes.CTAB_FOLDER_SHOW_LIST, listener );
  }

  ///////////////////////////////////
  // Helping methods to arrange items

  boolean updateItems() {
    return updateItems(selectedIndex);
  }

  boolean updateItems(int showIndex) {
    CTabItem[] items = itemHolder.getItems();
    if (!single && !mru && showIndex != -1) {
      // make sure selected item will be showing
      int firstIndex = showIndex;
      if (priority[0] < showIndex) {
        int maxWidth = getRightItemEdge() - borderLeft;
        //        if (!simple) maxWidth -= curveWidth - 2*curveIndent;
        int width = 0;
        int[] widths = new int[items.length];
        //        GC gc = new GC(this);
        for (int i = priority[0]; i <= showIndex; i++) {
          //          widths[i] = items[i].preferredWidth(gc, i == selectedIndex, true);
          widths[i] = items[i].preferredWidth(i == selectedIndex, true);
          width += widths[i];
          if (width > maxWidth) {
            break;
          }
        }
        if (width > maxWidth) {
          width = 0;
          for (int i = showIndex; i >= 0; i--) {
            //            if (widths[i] == 0) widths[i] = items[i].preferredWidth(gc, i == selectedIndex, true);
            if (widths[i] == 0) {
              widths[i] = items[i].preferredWidth(i == selectedIndex, true);
            }
            width += widths[i];
            if (width > maxWidth) {
              break;
            }
            firstIndex = i;
          }
        } else {
          firstIndex = priority[0];
          for (int i = showIndex + 1; i < items.length; i++) {
            //            widths[i] = items[i].preferredWidth(gc, i == selectedIndex, true);
            widths[i] = items[i].preferredWidth(i == selectedIndex, true);
            width += widths[i];
            if (width >= maxWidth) {
              break;
            }
          }
          if (width < maxWidth) {
            for (int i = priority[0] - 1; i >= 0; i--) {
              //              if (widths[i] == 0) widths[i] = items[i].preferredWidth(gc, i == selectedIndex, true);
              if (widths[i] == 0) {
                widths[i] = items[i].preferredWidth(i == selectedIndex, true);
              }
              width += widths[i];
              if (width > maxWidth) {
                break;
              }
              firstIndex = i;
            }
          }
        }
        //        gc.dispose();
      }
      if (firstIndex != priority[0]) {
        int index = 0;
        for (int i = firstIndex; i < items.length; i++) {
          priority[index++] = i;
        }
        for (int i = 0; i < firstIndex; i++) {
          priority[index++] = i;
        }
      }
    }

    boolean oldShowChevron = showChevron;
    boolean changed = setItemSize();
    changed |= setItemLocation();
    setButtonBounds();
    changed |= showChevron != oldShowChevron;
    //    if (changed && getToolTipText() != null) {
    //      Point pt = getDisplay().getCursorLocation();
    //      pt = toControl(pt);
    //      _setToolTipText(pt.x, pt.y);
    //    }
    redraw();
    return changed;
  }

  boolean setItemLocation() {
    CTabItem[] items = itemHolder.getItems();
    boolean changed = false;
    if( items.length == 0 ) {
      return false;
    }
    Point size = getSize();
    int y = onBottom
      ? Math.max( borderBottom, size.y - borderBottom - tabHeight )
      : borderTop;
    if( single ) {
      int defaultX = getDisplay().getBounds().width + 10; // off screen
      for( int i = 0; i < items.length; i++ ) {
        CTabItem item = items[ i ];
        if( i == selectedIndex ) {
          firstIndex = selectedIndex;
          int oldX = item.x, oldY = item.y;
          item.x = borderLeft;
          item.y = y;
          item.showing = true;
          if( showClose || item.showClose ) {
            item.closeRect.x = borderLeft + getItemPaddingLeft( true );
            item.closeRect.y = onBottom
              ? size.y
              - borderBottom
              - tabHeight
              + ( tabHeight - BUTTON_SIZE ) / 2
              : borderTop + ( tabHeight - BUTTON_SIZE ) / 2;
          }
          if( item.x != oldX || item.y != oldY ) {
            changed = true;
          }
        } else {
          item.x = defaultX;
          item.showing = false;
        }
      }
    } else {
      int rightItemEdge = getRightItemEdge();
      int maxWidth = rightItemEdge - borderLeft;
      int width = 0;
      for( int i = 0; i < priority.length; i++ ) {
        CTabItem item = items[ priority[ i ] ];
        width += item.width;
        item.showing = i == 0 ? true : item.width > 0 && width <= maxWidth;
        //        if (!simple && priority[i] == selectedIndex) width += curveWidth - 2*curveIndent;
      }
      int x = 0;
      int defaultX = getDisplay().getBounds().width + 10; // off screen
      firstIndex = items.length - 1;
      for( int i = 0; i < items.length; i++ ) {
        CTabItem item = items[ i ];
        if( !item.showing ) {
          if( item.x != defaultX ) {
            changed = true;
          }
          item.x = defaultX;
        } else {
          firstIndex = Math.min( firstIndex, i );
          if( item.x != x || item.y != y ) {
            changed = true;
          }
          item.x = x;
          item.y = y;
          if( i == selectedIndex ) {
            int edge = Math.min( item.x + item.width, rightItemEdge );
            item.closeRect.x = edge - getItemPaddingRight( true ) - BUTTON_SIZE;
          } else {
            int rightPadding = getItemPaddingRight( false );
            item.closeRect.x = item.x + item.width - rightPadding - BUTTON_SIZE;
          }
          item.closeRect.y = onBottom
            ? size.y
            - borderBottom
            - tabHeight
            + ( tabHeight - BUTTON_SIZE ) / 2
            : borderTop + ( tabHeight - BUTTON_SIZE ) / 2;
          x = x + item.width;
          //          if (!simple && i == selectedIndex) x += curveWidth - 2*curveIndent;
        }
      }
    }
    return changed;
  }

  boolean setItemSize() {
    CTabItem[] items = itemHolder.getItems();
    boolean changed = false;
    if( isDisposed() ) {
      return changed;
    }
    Point size = getSize();
    if( size.x <= 0 || size.y <= 0 ) {
      return changed;
    }
    // Note [rst] We have to substract the border here because on the client,
    // the position 0,0 is not the origin of the border but the origin of the
    // area inside the border.
    xClient = borderLeft + marginWidth + highlight_margin;
    if( onBottom ) {
      yClient = borderTop + highlight_margin + marginHeight;
    } else {
      yClient = borderTop + tabHeight + highlight_header + marginHeight;
    }
    showChevron = false;
    if( single ) {
      showChevron = true;
      if( selectedIndex != -1 ) {
        CTabItem tab = items[ selectedIndex ];
        //        GC gc = new GC(this);
        //        int width = tab.preferredWidth(gc, true, false);
        //        gc.dispose();
        int width = tab.preferredWidth( true, false );
        width = Math.min( width, getRightItemEdge() - borderLeft );
        if( tab.height != tabHeight || tab.width != width ) {
          changed = true;
          tab.shortenedText = null;
          tab.shortenedTextWidth = 0;
          tab.height = tabHeight;
          tab.width = width;
          tab.closeRect.width = tab.closeRect.height = 0;
          if( showClose || tab.showClose ) {
            tab.closeRect.width = BUTTON_SIZE;
            tab.closeRect.height = BUTTON_SIZE;
          }
        }
      }
      return changed;
    }

    if (items.length == 0) {
      return changed;
    }

    int[] widths;
    //    GC gc = new GC(this);
    int tabAreaWidth = size.x - borderLeft - borderRight - 3;
    if (showMin) {
      tabAreaWidth -= BUTTON_SIZE;
    }
    if (showMax) {
      tabAreaWidth -= BUTTON_SIZE;
    }
    if (topRightAlignment == SWT.RIGHT && topRight != null) {
      Point rightSize = topRight.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
      tabAreaWidth -= rightSize.x + 3;
    }
    //    if (!simple) tabAreaWidth -= curveWidth - 2*curveIndent;
    tabAreaWidth = Math.max(0, tabAreaWidth);

    // First, try the minimum tab size at full compression.
    int minWidth = 0;
    int[] minWidths = new int[items.length];
    for (int i = 0; i < priority.length; i++) {
      int index = priority[i];
      //      minWidths[index] = items[index].preferredWidth(gc, index == selectedIndex, true);
      minWidths[index] = items[index].preferredWidth(index == selectedIndex, true);
      minWidth += minWidths[index];
      if (minWidth > tabAreaWidth) {
        break;
      }
    }
    if (minWidth > tabAreaWidth) {
      // full compression required and a chevron
      showChevron = items.length > 1;
      if (showChevron) {
        tabAreaWidth -= 3*BUTTON_SIZE/2;
      }
      widths = minWidths;
      int index = selectedIndex != -1 ? selectedIndex : 0;
      if (tabAreaWidth < widths[index]) {
        widths[index] = Math.max(0, tabAreaWidth);
      }
    } else {
      int maxWidth = 0;
      int[] maxWidths = new int[items.length];
      for (int i = 0; i < items.length; i++) {
        //        maxWidths[i] = items[i].preferredWidth(gc, i == selectedIndex, false);
        maxWidths[i] = items[i].preferredWidth(i == selectedIndex, false);
        maxWidth += maxWidths[i];
      }
      if (maxWidth <= tabAreaWidth) {
        // no compression required
        widths = maxWidths;
      } else {
        // determine compression for each item
        int extra = (tabAreaWidth - minWidth) / items.length;
        while (true) {
          int large = 0, totalWidth = 0;
          for (int i = 0 ; i < items.length; i++) {
            if (maxWidths[i] > minWidths[i] + extra) {
              totalWidth += minWidths[i] + extra;
              large++;
            } else {
              totalWidth += maxWidths[i];
            }
          }
          if (totalWidth >= tabAreaWidth) {
            extra--;
            break;
          }
          if (large == 0 || tabAreaWidth - totalWidth < large) {
            break;
          }
          extra++;
        }
        widths = new int[items.length];
        for (int i = 0; i < items.length; i++) {
          widths[i] = Math.min(maxWidths[i], minWidths[i] + extra);
        }
      }
    }
    //    gc.dispose();

    for (int i = 0; i < items.length; i++) {
      CTabItem tab = items[i];
      int width = widths[i];
      if (tab.height != tabHeight || tab.width != width) {
        changed = true;
        tab.shortenedText = null;
        tab.shortenedTextWidth = 0;
        tab.height = tabHeight;
        tab.width = width;
        tab.closeRect.width = tab.closeRect.height = 0;
        if (showClose || tab.showClose) {
          if (i == selectedIndex || showUnselectedClose) {
            tab.closeRect.width = BUTTON_SIZE;
            tab.closeRect.height = BUTTON_SIZE;
          }
        }
      }
    }
    return changed;
  }

  @SuppressWarnings("unused")
  void setButtonBounds() {
    CTabItem[] items = itemHolder.getItems();
    Point size = getSize();
    //    int oldX, oldY, oldWidth, oldHeight;
    // max button
    //    oldX = maxRect.x;
    //    oldY = maxRect.y;
    //    oldWidth = maxRect.width;
    //    oldHeight = maxRect.height;
    maxRect.x = maxRect.y = maxRect.width = maxRect.height = 0;
    if (showMax) {
      maxRect.x = size.x - borderRight - BUTTON_SIZE - 3;
      if (borderRight > 0) {
        maxRect.x += 1;
      }
      maxRect.y = onBottom ? size.y - borderBottom - tabHeight + (tabHeight - BUTTON_SIZE)/2: borderTop + (tabHeight - BUTTON_SIZE)/2;
      maxRect.width = BUTTON_SIZE;
      maxRect.height = BUTTON_SIZE;
    }
    //    if (oldX != maxRect.x || oldWidth != maxRect.width ||
    //        oldY != maxRect.y || oldHeight != maxRect.height) {
    //      int left = Math.min(oldX, maxRect.x);
    //      int right = Math.max(oldX + oldWidth, maxRect.x + maxRect.width);
    //      int top = onBottom ? size.y - borderBottom - tabHeight: borderTop + 1;
    //      redraw(left, top, right - left, tabHeight, false);
    //    }

    // min button
    //    oldX = minRect.x;
    //    oldY = minRect.y;
    //    oldWidth = minRect.width;
    //    oldHeight = minRect.height;
    minRect.x = minRect.y = minRect.width = minRect.height = 0;
    if (showMin) {
      minRect.x = size.x - borderRight - maxRect.width - BUTTON_SIZE - 3;
      if (borderRight > 0) {
        minRect.x += 1;
      }
      minRect.y = onBottom ? size.y - borderBottom - tabHeight + (tabHeight - BUTTON_SIZE)/2: borderTop + (tabHeight - BUTTON_SIZE)/2;
      minRect.width = BUTTON_SIZE;
      minRect.height = BUTTON_SIZE;
    }
    //    if (oldX != minRect.x || oldWidth != minRect.width ||
    //        oldY != minRect.y || oldHeight != minRect.height) {
    //      int left = Math.min(oldX, minRect.x);
    //      int right = Math.max(oldX + oldWidth, minRect.x + minRect.width);
    //      int top = onBottom ? size.y - borderBottom - tabHeight: borderTop + 1;
    //      redraw(left, top, right - left, tabHeight, false);
    //    }

    // top right control
    //    oldX = topRightRect.x;
    //    oldY = topRightRect.y;
    //    oldWidth = topRightRect.width;
    //    oldHeight = topRightRect.height;
    topRightRect.x = topRightRect.y = topRightRect.width = topRightRect.height = 0;
    if (topRight != null) {
      switch (topRightAlignment) {
        case SWT.FILL: {
          int rightEdge = size.x - borderRight - 3 - maxRect.width - minRect.width;
          if (!simple && borderRight > 0 && !showMax && !showMin) {
            rightEdge -= 2;
          }
          if (single) {
            if (items.length == 0 || selectedIndex == -1) {
              topRightRect.x = borderLeft + 3;
              topRightRect.width = rightEdge - topRightRect.x;
            } else {
              // fill size is 0 if item compressed
              CTabItem item = items[selectedIndex];
              if (item.x + item.width + 7 + 3*BUTTON_SIZE/2 >= rightEdge) {
                break;
              }
              topRightRect.x = item.x + item.width + 7 + 3*BUTTON_SIZE/2;
              topRightRect.width = rightEdge - topRightRect.x;
            }
          } else {
            // fill size is 0 if chevron showing
            if (showChevron) {
              break;
            }
            if (items.length == 0) {
              topRightRect.x = borderLeft + 3;
            } else {
              CTabItem item = items[items.length - 1];
              topRightRect.x = item.x + item.width;
              //              if (!simple && items.length - 1 == selectedIndex) topRightRect.x += curveWidth - curveIndent;
            }
            topRightRect.width = Math.max(0, rightEdge - topRightRect.x);
          }
          topRightRect.y = onBottom ? size.y - borderBottom - tabHeight: borderTop + 1;
          topRightRect.height = tabHeight - 1;
          break;
        }
        case SWT.RIGHT: {
          Point topRightSize = topRight.computeSize(SWT.DEFAULT, tabHeight, false);
          int rightEdge = size.x - borderRight - 3 - maxRect.width - minRect.width;
          if (!simple && borderRight > 0 && !showMax && !showMin) {
            rightEdge -= 2;
          }
          topRightRect.x = rightEdge - topRightSize.x;
          topRightRect.width = topRightSize.x;
          topRightRect.y = onBottom ? size.y - borderBottom - tabHeight: borderTop + 1;
          topRightRect.height = tabHeight - 1;
        }
      }
      topRight.setBounds(topRightRect);
    }
    //    if (oldX != topRightRect.x || oldWidth != topRightRect.width ||
    //      oldY != topRightRect.y || oldHeight != topRightRect.height) {
    //      int left = Math.min(oldX, topRightRect.x);
    //      int right = Math.max(oldX + oldWidth, topRightRect.x + topRightRect.width);
    //      int top = onBottom ? size.y - borderBottom - tabHeight : borderTop + 1;
    //      redraw(left, top, right - left, tabHeight, false);
    //    }

    // chevron button
    //    oldX = chevronRect.x;
    //    oldY = chevronRect.y;
    //    oldWidth = chevronRect.width;
    //    oldHeight = chevronRect.height;
    chevronRect.x = chevronRect.y = chevronRect.height = chevronRect.width = 0;
    if (single) {
      if (selectedIndex == -1 || items.length > 1) {
        chevronRect.width = 3*BUTTON_SIZE/2;
        chevronRect.height = BUTTON_SIZE;
        chevronRect.y = onBottom ? size.y - borderBottom - tabHeight + (tabHeight - chevronRect.height)/2 : borderTop + (tabHeight - chevronRect.height)/2;
        if (selectedIndex == -1) {
          chevronRect.x = size.x - borderRight - 3 - minRect.width - maxRect.width - topRightRect.width - chevronRect.width;
        } else {
          CTabItem item = items[selectedIndex];
          int w = size.x - borderRight - 3 - minRect.width - maxRect.width - chevronRect.width;
          if (topRightRect.width > 0) {
            w -= topRightRect.width + 3;
          }
          chevronRect.x = Math.min(item.x + item.width + 3, w);
        }
        if (borderRight > 0) {
          chevronRect.x += 1;
        }
      }
    } else {
      if (showChevron) {
        chevronRect.width = 3*BUTTON_SIZE/2;
        chevronRect.height = BUTTON_SIZE;
        int i = 0, lastIndex = -1;
        while (i < priority.length && items[priority[i]].showing) {
          lastIndex = Math.max(lastIndex, priority[i++]);
        }
        if (lastIndex == -1) {
          lastIndex = firstIndex;
        }
        CTabItem lastItem = items[lastIndex];
        int w = lastItem.x + lastItem.width + 3;
        //        if (!simple && lastIndex == selectedIndex) w += curveWidth - 2*curveIndent;
        chevronRect.x = Math.min(w, getRightItemEdge());
        chevronRect.y = onBottom ? size.y - borderBottom - tabHeight + (tabHeight - chevronRect.height)/2 : borderTop + (tabHeight - chevronRect.height)/2;
      }
    }
    //    if (oldX != chevronRect.x || oldWidth != chevronRect.width ||
    //        oldY != chevronRect.y || oldHeight != chevronRect.height) {
    //      int left = Math.min(oldX, chevronRect.x);
    //      int right = Math.max(oldX + oldWidth, chevronRect.x + chevronRect.width);
    //      int top = onBottom ? size.y - borderBottom - tabHeight: borderTop + 1;
    //      redraw(left, top, right - left, tabHeight, false);
    //    }
  }

  boolean updateTabHeight(boolean force){
    CTabItem[] items = itemHolder.getItems();
    int style = getStyle();
    if (fixedTabHeight == 0 && (style & SWT.FLAT) != 0 && (style & SWT.BORDER) == 0) {
      highlight_header = 0;
    }
    int oldHeight = tabHeight;
    if (fixedTabHeight != SWT.DEFAULT) {
      tabHeight = fixedTabHeight == 0 ? 0 : fixedTabHeight + 1; // +1 for line drawn across top of tab
    } else {
      int tempHeight = 0;
      //      GC gc = new GC(this);
      if (items.length == 0) {
        //        tempHeight = gc.textExtent("Default", CTabItem.FLAGS).y + CTabItem.TOP_MARGIN + CTabItem.BOTTOM_MARGIN; //$NON-NLS-1$
        BoxDimensions padding = getItemPadding( false );
        tempHeight = TextSizeUtil.getCharHeight( getFont() ) + padding.top + padding.bottom;
      } else {
        for (int i=0; i < items.length; i++) {
          //          tempHeight = Math.max(tempHeight, items[i].preferredHeight(gc));
          tempHeight = Math.max(tempHeight, items[i].preferredHeight(i==selectedIndex));
        }
      }
      //      gc.dispose();
      tabHeight =  tempHeight;
    }
    if (!force && tabHeight == oldHeight) {
      return false;
    }

    //    oldSize = null;
    //    if (onBottom) {
    //      int d = tabHeight - 12;
    //      curve = new int[]{0,13+d, 0,12+d, 2,12+d, 3,11+d, 5,11+d, 6,10+d, 7,10+d, 9,8+d, 10,8+d,
    //                    11,7+d, 11+d,7,
    //                12+d,6, 13+d,6, 15+d,4, 16+d,4, 17+d,3, 19+d,3, 20+d,2, 22+d,2, 23+d,1};
    //      curveWidth = 26+d;
    //      curveIndent = curveWidth/3;
    //    } else {
    //      int d = tabHeight - 12;
    //      curve = new int[]{0,0, 0,1, 2,1, 3,2, 5,2, 6,3, 7,3, 9,5, 10,5,
    //                    11,6, 11+d,6+d,
    //                    12+d,7+d, 13+d,7+d, 15+d,9+d, 16+d,9+d, 17+d,10+d, 19+d,10+d, 20+d,11+d, 22+d,11+d, 23+d,12+d};
    //      curveWidth = 26+d;
    //      curveIndent = curveWidth/3;
    //
    //      //this could be static but since values depend on curve, better to keep in one place
    //      topCurveHighlightStart = new int[] {
    //          0, 2,  1, 2,  2, 2,
    //          3, 3,  4, 3,  5, 3,
    //          6, 4,  7, 4,
    //          8, 5,
    //          9, 6, 10, 6};
    //
    //      //also, by adding in 'd' here we save some math cost when drawing the curve
    //      topCurveHighlightEnd = new int[] {
    //          10+d, 6+d,
    //          11+d, 7+d,
    //          12+d, 8+d,  13+d, 8+d,
    //          14+d, 9+d,
    //          15+d, 10+d,  16+d, 10+d,
    //          17+d, 11+d,  18+d, 11+d,  19+d, 11+d,
    //          20+d, 12+d,  21+d, 12+d,  22+d,  12+d };
    //    }

    notifyListeners(SWT.Resize, new Event());

    return true;
  }

  int getRightItemEdge (){
    int x = getSize().x - borderRight - 3;
    if (showMin) {
      x -= BUTTON_SIZE;
    }
    if (showMax) {
      x -= BUTTON_SIZE;
    }
    if (showChevron) {
      x -= 3*BUTTON_SIZE/2;
    }
    if (topRight != null && topRightAlignment != SWT.FILL) {
      Point rightSize = topRight.computeSize(SWT.DEFAULT, SWT.DEFAULT);
      x -= rightSize.x + 3;
    }
    return Math.max(0, x);
  }

  private void updateItemsWithResizeEvent() {
    Rectangle rectBefore = getClientArea();
    updateItems();
    Rectangle rectAfter = getClientArea();
    if( !rectBefore.equals( rectAfter ) ) {
      notifyListeners( SWT.Resize, new Event() );
    }
  }

  void onResize() {
    //  if (updateItems()) redrawTabs();
    updateItems();

    //    Point size = getSize();
    //    if (oldSize == null) {
    //    redraw();
    //    } else {
    //      if (onBottom && size.y != oldSize.y) {
    //      redraw();
    //      } else {
    //        int x1 = Math.min(size.x, oldSize.x);
    //        if (size.x != oldSize.x) x1 -= borderRight + highlight_margin + 2;
    //        if (!simple) x1 -= 5; // rounded top right corner
    //        int y1 = Math.min(size.y, oldSize.y);
    //        if (size.y != oldSize.y) y1 -= borderBottom + highlight_margin;
    //      int x2 = Math.max(size.x, oldSize.x);
    //      int y2 = Math.max(size.y, oldSize.y);
    //      redraw(0, y1, x2, y2 - y1, false);
    //      redraw(x1, 0, x2 - x1, y2, false);
    //      }
    //    }
    //    oldSize = size;

    // Differs from SWT code: adjust bounds of the selected items' control
    if( selectedIndex != -1 ) {
      CTabItem item = itemHolder.getItem( selectedIndex );
      Control control = item.getControl();
      if( control != null && !control.isDisposed() ) {
        control.setBounds( getClientArea() );
      }
    }
  }

  ///////////
  // Disposal

  void onDispose() {
    /*
     * Usually when an item is disposed of, destroyItem will change the size of
     * the items array, reset the bounds of all the tabs and manage the widget
     * associated with the tab. Since the whole folder is being disposed, this
     * is not necessary. For speed the inDispose flag is used to skip over this
     * part of the item dispose.
     */
    inDispose = true;
    removeControlListener( resizeListener );
    unregisterFocusListener();
    if( showMenu != null && !showMenu.isDisposed() ) {
      showMenu.dispose();
      showMenu = null;
    }
    while( itemHolder.size() > 0 ) {
      CTabItem item = itemHolder.getItem( 0 );
      item.dispose();
      itemHolder.remove( item );
    }
  }

  //////////////////
  // Helping methods

  private static int checkStyle( int style ) {
    int mask
      = SWT.CLOSE
      | SWT.TOP
      | SWT.BOTTOM
      | SWT.FLAT
      | SWT.LEFT_TO_RIGHT
      //      | SWT.RIGHT_TO_LEFT
      | SWT.SINGLE
      | SWT.MULTI;
    int result = style & mask;
    // TOP and BOTTOM are mutually exclusive, TOP is the default
    if( ( result & SWT.TOP ) != 0 ) {
      result = result & ~SWT.BOTTOM;
    }
    // SINGLE and MULTI are mutually exclusive, MULTI is the default
    if( ( result & SWT.MULTI ) != 0 ) {
      result = result & ~SWT.SINGLE;
    }
    return result;
  }

  private void registerDisposeListener() {
    addDisposeListener( new DisposeListener() {
      @Override
      public void widgetDisposed( DisposeEvent event ) {
        onDispose();
        CTabFolder.this.removeDisposeListener( this );
      }
    } );
  }

  private void registerFocusListener() {
    if( focusListener == null ) {
      focusListener = new FocusListener() {
        @Override
        public void focusGained( FocusEvent event ) {
          onFocus();
        }
        @Override
        public void focusLost( FocusEvent event ) {
          onFocus();
        }
      };
      addFocusListener( focusListener );
    }
  }

  private void onFocus() {
    if( selectedIndex < 0 ) {
      setSelection( 0, true );
    }
    unregisterFocusListener();
  }

  private void unregisterFocusListener() {
    if( focusListener != null ) {
      addFocusListener( focusListener );
      focusListener = null;
    }
  }

  private void showListMenu() {
    CTabItem[] items = getItems();
    if( items.length == 0 || !showChevron ) {
      return;
    }
    if( showMenu == null || showMenu.isDisposed() ) {
      showMenu = new Menu( this );
    } else {
      // TODO [rh] optimize: reuse existing menuItems if possible
      MenuItem[] menuItems = showMenu.getItems();
      for( int i = 0; i < menuItems.length; i++ ) {
        menuItems[ i ].dispose();
      }
    }
    showMenu.setOrientation( getOrientation() );
    final String id = "CTabFolder_showList_Index"; //$NON-NLS-1$
    for( int i = 0; i < items.length; i++ ) {
      CTabItem tab = items[ i ];
      if( !tab.showing ) {
        MenuItem item = new MenuItem( showMenu, SWT.NONE );
        item.setText( tab.getText() );
        item.setImage( tab.getImage() );
        item.setData( id, tab );
        item.addSelectionListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent event ) {
            MenuItem menuItem = ( MenuItem )event.getSource();
            int index = indexOf( ( CTabItem )menuItem.getData( id ) );
            setSelection( index, true );
          }
        } );
      }
    }
    // show menu if it contains any item
    if( showMenu.getItemCount() > 0 ) {
      int x = chevronRect.x;
      int y = chevronRect.y + chevronRect.height + 1;
      Point location = getDisplay().map( this, null, x, y );
      showMenu.setLocation( location.x, location.y );
      showMenu.setVisible( true );
    }
  }

  private void setSelection( int index, boolean notify ) {
    int oldSelectedIndex = selectedIndex;
    setSelection( index );
    if( notify && selectedIndex != oldSelectedIndex && selectedIndex != -1 ) {
      Event event = new Event();
      event.item = getSelection();
      notifyListeners( SWT.Selection, event );
    }
  }

  void createItem( CTabItem item, int index ) {
    itemHolder.insert( item, index );
    if( selectedIndex >= index ) {
      selectedIndex++;
    }
    int[] newPriority = new int[ priority.length + 1 ];
    int next = 0, priorityIndex = priority.length;
    for( int i = 0; i < priority.length; i++ ) {
      if( !mru && priority[ i ] == index ) {
        priorityIndex = next++;
      }
      newPriority[ next++ ] = priority[ i ] >= index
        ? priority[ i ] + 1
        : priority[ i ];
    }
    newPriority[ priorityIndex ] = index;
    priority = newPriority;
    updateItems();
    if (getItemCount() == 1) {
      registerFocusListener(); // RWT specific
      if (!updateTabHeight(false)) {
        updateItems();
      }
      redraw();
    } else {
      updateItems();
      //      redrawTabs();
      redraw();
    }

  }

  void destroyItem( CTabItem item ) {
    int index = indexOf( item );
    if( !inDispose && index != -1  ) {
      CTabItem[] items = getItems();
      if( items.length == 1 ) {
        itemHolder.remove( item );
        priority = new int[ 0 ];
        firstIndex = -1;
        selectedIndex = -1;
        Control control = item.getControl();
        if( control != null && !control.isDisposed() ) {
          control.setVisible( false );
        }
        setToolTipText( null );
        setButtonBounds();
      } else {
        itemHolder.remove( item );
        int[] newPriority = new int[ priority.length - 1 ];
        int next = 0;
        for( int i = 0; i < priority.length; i++ ) {
          if( priority[ i ] == index ) {
            continue;
          }
          newPriority[ next++ ] = priority[ i ] > index
            ? priority[ i ] - 1
            : priority[ i ];
        }
        priority = newPriority;
        // move the selection if this item is selected
        if( selectedIndex == index ) {
          Control control = item.getControl();
          selectedIndex = -1;
          int nextSelection = mru ? priority[ 0 ] : Math.max( 0, index - 1 );
          setSelection( nextSelection, true );
          if( control != null && !control.isDisposed() ) {
            control.setVisible( false );
          }
        } else if( selectedIndex > index ) {
          selectedIndex--;
        }
      }
      updateItems();
      if( getItemCount() == 0 ) {
        unregisterFocusListener();
      }
    }
  }


  public void setSimple( boolean simple ) {
    return;
  }

  //////////////////
  // Theming related

  int getItemPaddingLeft( boolean selected ) {
    return getItemPadding( selected ).left;
  }

  int getItemPaddingRight( boolean selected ) {
    return getItemPadding( selected ).right;
  }

  BoxDimensions getItemPadding( boolean selected ) {
    return getThemeAdapter().getItemPadding( selected );
  }

  int getItemSpacing( boolean selected ) {
    return getThemeAdapter().getItemSpacing( selected );
  }

  Font getItemFont( boolean selected ) {
    return getThemeAdapter().getItemFont( selected );
  }

  private CTabFolderThemeAdapter getThemeAdapter() {
    return ( CTabFolderThemeAdapter )getAdapter( ThemeAdapter.class );
  }

  ///////////////////
  // Skinning support

  @Override
  public void reskin( int flags ) {
    super.reskin( flags );
    CTabItem[] items = getItems();
    for( int i = 0; i < items.length; i++ ) {
      items[ i ].reskin( flags );
    }
  }

  ////////////////
  // Inner classes

  private final class CTabFolderAdapter implements ICTabFolderAdapter {

    @Override
    public Rectangle getChevronRect() {
      Rectangle rect = chevronRect;
      return new Rectangle( rect.x, rect.y, rect.width, rect.height );
    }

    @Override
    public boolean getChevronVisible() {
      return showChevron;
    }

    @Override
    public Rectangle getMinimizeRect() {
      Rectangle rect = minRect;
      return new Rectangle( rect.x, rect.y, rect.width, rect.height );
    }

    @Override
    public Rectangle getMaximizeRect() {
      Rectangle rect = maxRect;
      return new Rectangle( rect.x, rect.y, rect.width, rect.height );
    }

    @Override
    public void showListMenu() {
      CTabFolder.this.showListMenu();
    }

    @Override
    public boolean showItemImage( CTabItem item ) {
      return item.showImage();
    }

    @Override
    public boolean showItemClose( CTabItem item ) {
      return item.parent.showClose || item.showClose;
    }

    @Override
    public String getShortenedItemText( CTabItem item ) {
      return item.getShortenedText( item.getParent().getSelection() == item );
    }

    @Override
    public Color getUserSelectionForeground() {
      return selectionForeground;
    }

    @Override
    public Color getUserSelectionBackground() {
      return selectionBackground;
    }

    @Override
    public Image getUserSelectionBackgroundImage() {
      return selectionBgImage;
    }

    @Override
    public IWidgetGraphicsAdapter getUserSelectionBackgroundGradient() {
      return selectionGraphicsAdapter;
    }

    @Override
    public void doRedraw() {
      setButtonBounds();
    }

  }

}