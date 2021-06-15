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

import static org.eclipse.swt.internal.widgets.MarkupUtil.isMarkupEnabledFor;
import static org.eclipse.swt.internal.widgets.MarkupValidator.isValidationDisabledFor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.SerializableCompatibility;
import org.eclipse.swt.internal.widgets.IItemHolderAdapter;
import org.eclipse.swt.internal.widgets.ITreeItemAdapter;
import org.eclipse.swt.internal.widgets.IWidgetColorAdapter;
import org.eclipse.swt.internal.widgets.IWidgetFontAdapter;
import org.eclipse.swt.internal.widgets.MarkupValidator;
import org.eclipse.swt.internal.widgets.treeitemkit.TreeItemLCA;


/**
 * Instances of this class represent a selectable user interface object that
 * represents a hierarchy of tree items in a tree widget.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @since 1.0
 */
public class TreeItem extends Item {

  private final TreeItem parentItem;
  final Tree parent;
  TreeItem[] items;
  int itemCount;
  private transient ITreeItemAdapter treeItemAdapter;
  int index;
  private Data[] data;
  private Font font;
  private boolean expanded;
  private boolean checked;
  private Color background;
  private Color foreground;
  private boolean grayed;
  int depth;
  private boolean cached;
  private int flatIndex;

  /**
   * Constructs a new instance of this class given its parent (which must be a
   * <code>Tree</code> or a <code>TreeItem</code>) and a style value describing
   * its behavior and appearance. The item is added to the end of the items
   * maintained by its parent.
   * <p>
   * The style value is either one of the style constants defined in class
   * <code>SWT</code> which is applicable to instances of this class, or must be
   * built by <em>bitwise OR</em>'ing together (that is, using the
   * <code>int</code> "|" operator) two or more of those <code>SWT</code> style
   * constants. The class description lists the style constants that are
   * applicable to the class. Style bits are also inherited from superclasses.
   * </p>
   *
   * @param parent a tree control which will be the parent of the new instance
   *          (cannot be null)
   * @param style the style of control to construct
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the parent</li>
   *              <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
   *              subclass</li>
   *              </ul>
   * @see SWT
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public TreeItem( Tree parent, int style ) {
    this( parent, null, style, parent == null ? 0 : parent.getItemCount(), true );
  }

  /**
   * Constructs a new instance of this class given its parent (which must be a
   * <code>Tree</code> or a <code>TreeItem</code>), a style value describing its
   * behavior and appearance, and the index at which to place it in the items
   * maintained by its parent.
   * <p>
   * The style value is either one of the style constants defined in class
   * <code>SWT</code> which is applicable to instances of this class, or must be
   * built by <em>bitwise OR</em>'ing together (that is, using the
   * <code>int</code> "|" operator) two or more of those <code>SWT</code> style
   * constants. The class description lists the style constants that are
   * applicable to the class. Style bits are also inherited from superclasses.
   * </p>
   *
   * @param parent a tree control which will be the parent of the new instance
   *          (cannot be null)
   * @param style the style of control to construct
   * @param index the zero-relative index to store the receiver in its parent
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *              <li>ERROR_INVALID_RANGE - if the index is not between 0 and
   *              the number of elements in the parent (inclusive)</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the parent</li>
   *              <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
   *              subclass</li>
   *              </ul>
   * @see SWT
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public TreeItem( Tree parent, int style, int index ) {
    this( parent, null, style, index, true );
  }

  /**
   * Constructs a new instance of this class given its parent (which must be a
   * <code>Tree</code> or a <code>TreeItem</code>) and a style value describing
   * its behavior and appearance. The item is added to the end of the items
   * maintained by its parent.
   * <p>
   * The style value is either one of the style constants defined in class
   * <code>SWT</code> which is applicable to instances of this class, or must be
   * built by <em>bitwise OR</em>'ing together (that is, using the
   * <code>int</code> "|" operator) two or more of those <code>SWT</code> style
   * constants. The class description lists the style constants that are
   * applicable to the class. Style bits are also inherited from superclasses.
   * </p>
   *
   * @param parentItem a tree control which will be the parent of the new
   *          instance (cannot be null)
   * @param style the style of control to construct
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the parent</li>
   *              <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
   *              subclass</li>
   *              </ul>
   * @see SWT
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public TreeItem( TreeItem parentItem, int style ) {
    this( parentItem == null ? null : parentItem.parent,
          parentItem,
          style,
          parentItem == null ? 0 : parentItem.itemCount, true );
  }

  /**
   * Constructs a new instance of this class given its parent (which must be a
   * <code>Tree</code> or a <code>TreeItem</code>), a style value describing its
   * behavior and appearance, and the index at which to place it in the items
   * maintained by its parent.
   * <p>
   * The style value is either one of the style constants defined in class
   * <code>SWT</code> which is applicable to instances of this class, or must be
   * built by <em>bitwise OR</em>'ing together (that is, using the
   * <code>int</code> "|" operator) two or more of those <code>SWT</code> style
   * constants. The class description lists the style constants that are
   * applicable to the class. Style bits are also inherited from superclasses.
   * </p>
   *
   * @param parentItem a tree control which will be the parent of the new
   *          instance (cannot be null)
   * @param style the style of control to construct
   * @param index the zero-relative index to store the receiver in its parent
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *              <li>ERROR_INVALID_RANGE - if the index is not between 0 and
   *              the number of elements in the parent (inclusive)</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the parent</li>
   *              <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
   *              subclass</li>
   *              </ul>
   * @see SWT
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public TreeItem( TreeItem parentItem, int style, int index ) {
    this( parentItem == null ? null : parentItem.parent, parentItem, style, index, true );
  }

  TreeItem( Tree parent, TreeItem parentItem, int style, int index, boolean create ) {
    super( parent, style );
    this.parent = parent;
    this.parentItem = parentItem;
    this.index = index;
    if( parentItem != null ) {
      depth = parentItem.depth + 1;
    }
    parent.invalidateFlatIndex();
    setEmpty();
    if( create ) {
      int numberOfItems;
      if( parentItem != null ) {
        numberOfItems = parentItem.itemCount;
      } else {
        // If there is no parent item, get the next index of the tree
        numberOfItems = parent.getItemCount();
      }
      // check range
      if( index < 0 || index > numberOfItems ) {
        error( SWT.ERROR_INVALID_RANGE );
      }
      if( parentItem != null ) {
        parentItem.createItem( this, index );
      } else {
        parent.createItem( this, index );
      }
      parent.updateScrollBars();
    }
  }

  private void setEmpty() {
    items = new TreeItem[ 4 ];
  }

  private void createItem( TreeItem item, int index ) {
    if( itemCount == items.length ) {
      /*
       * Grow the array faster when redraw is off or the table is not visible.
       * When the table is painted, the items array is resized to be smaller to
       * reduce memory usage.
       */
      boolean small = /* drawCount == 0 && */isVisible();
      int length = small ? items.length + 4 : Math.max( 4, items.length * 3 / 2 );
      TreeItem[] newItems = new TreeItem[ length ];
      System.arraycopy( items, 0, newItems, 0, items.length );
      items = newItems;
    }
    System.arraycopy( items, index, items, index + 1, itemCount - index );
    items[ index ] = item;
    itemCount++;
    adjustItemIndices( index );
  }

  private void destroyItem( int index ) {
    itemCount--;
    if( itemCount == 0 ) {
      setEmpty();
    } else {
      System.arraycopy( items, index + 1, items, index, itemCount - index );
      items[ itemCount ] = null;
    }
    adjustItemIndices( index );
  }

  private void adjustItemIndices( int start ) {
    for( int i = start; i < itemCount; i++ ) {
      if( items[ i ] != null ) {
        items[ i ].index = i;
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == IItemHolderAdapter.class ) {
      return ( T )new CompositeItemHolder();
    }
    if(    adapter == IWidgetFontAdapter.class
        || adapter == IWidgetColorAdapter.class
        || adapter == ITreeItemAdapter.class )
    {
      if( treeItemAdapter == null ) {
        treeItemAdapter = new TreeItemAdapter();
      }
      return ( T )treeItemAdapter;
    }
    if( adapter == WidgetLCA.class ) {
      return ( T )TreeItemLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  //////////////////////////
  // Parent/child relations

  /**
   * Returns the receiver's parent, which must be a <code>Tree</code>.
   *
   * @return the receiver's parent
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public Tree getParent() {
    checkWidget();
    return parent;
  }

  /**
   * Returns the receiver's parent item, which must be a <code>TreeItem</code>
   * or null when the receiver is a root.
   *
   * @return the receiver's parent item
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public TreeItem getParentItem() {
    checkWidget();
    return parentItem;
  }

  /////////////////
  // Getter/Setter

  /**
   * Sets the expanded state of the receiver.
   * <p>
   *
   * @param expanded the new expanded state
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setExpanded( boolean expanded ) {
    checkWidget();
    if( this.expanded != expanded && ( !expanded || itemCount > 0 ) ) {
      this.expanded = expanded;
      if( !expanded ) {
        updateSelection();
      }
      markCached();
      parent.invalidateFlatIndex();
      parent.updateScrollBars();
      parent.updateAllItems();
    }
  }

  /**
   * Returns <code>true</code> if the receiver is expanded, and false otherwise.
   * <p>
   *
   * @return the expanded state
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public boolean getExpanded() {
    checkWidget();
    return expanded;
  }

  /**
   * Returns a rectangle describing the receiver's size and location relative to
   * its parent.
   *
   * @return the receiver's bounding rectangle
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public Rectangle getBounds() {
    return getBounds( 0 );
  }

  /**
   * Returns a rectangle describing the receiver's size and location relative to
   * its parent at a column in the tree.
   *
   * @param index the index that specifies the column
   * @return the receiver's bounding column rectangle
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public Rectangle getBounds( int index ) {
    checkWidget();
    if( !parent.checkData( this, this.index ) ) {
      error( SWT.ERROR_WIDGET_DISPOSED );
    }
    Rectangle result = new Rectangle( 0, 0, 0, 0 );
    if( isVisible() && isValidColumn( index ) ) {
      int left = parent.getVisualCellLeft( this, index );
      int width = parent.getVisualCellWidth( this, index );
      result = new Rectangle( left, getItemTop(), width, parent.getItemHeight() );
    }
    return result;
  }

  /**
   * Returns the background color at the given column index in the receiver.
   *
   * @param index the column index
   * @return the background color
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public Color getBackground( int index ) {
    checkWidget();
    if( !parent.checkData( this, this.index ) ) {
      error( SWT.ERROR_WIDGET_DISPOSED );
    }
    Color result;
    if( hasData( index ) && data[ index ].background != null ) {
      result = data[ index ].background;
    } else if( background == null ) {
      result = parent.getBackground();
    } else {
      result = background;
    }
    return result;
  }

  /**
   * Returns the font that the receiver will use to paint textual information
   * for the specified cell in this item.
   *
   * @param index the column index
   * @return the receiver's font
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public Font getFont( int index ) {
    checkWidget();
    if( !parent.checkData( this, this.index ) ) {
      error( SWT.ERROR_WIDGET_DISPOSED );
    }
    Font result;
    if( hasData( index ) && data[ index ].font != null ) {
      result = data[ index ].font;
    } else if( font == null ) {
      result = parent.getFont();
    } else {
      result = font;
    }
    return result;
  }

  /**
   * Returns the foreground color at the given column index in the receiver.
   *
   * @param index the column index
   * @return the foreground color
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public Color getForeground( int index ) {
    checkWidget();
    if( !parent.checkData( this, this.index ) ) {
      error( SWT.ERROR_WIDGET_DISPOSED );
    }
    Color result;
    if( hasData( index ) && data[ index ].foreground != null ) {
      result = data[ index ].foreground;
    } else if( foreground == null ) {
      result = parent.getForeground();
    } else {
      result = foreground;
    }
    return result;
  }

  /**
   * Sets the background color at the given column index in the receiver to the
   * color specified by the argument, or to the default system color for the
   * item if the argument is null.
   *
   * @param index the column index
   * @param color the new color (or null)
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed
   *              </li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setBackground( int index, Color color ) {
    checkWidget();
    if( color != null && color.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    int count = Math.max( 1, parent.getColumnCount() );
    if( index >= 0 && index < count ) {
      ensureData( index, count );
      if( !equals( data[ index ].background, color ) ) {
        data[ index ].background = color;
        markCached();
        parent.redraw();
      }
    }
  }

  /**
   * Sets the font that the receiver will use to paint textual information for
   * the specified cell in this item to the font specified by the argument, or
   * to the default font for that kind of control if the argument is null.
   *
   * @param index the column index
   * @param font the new font (or null)
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed
   *              </li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setFont( int index, Font font ) {
    checkWidget();
    if( font != null && font.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    int count = Math.max( 1, parent.getColumnCount() );
    if( index >= 0 && index < count ) {
      ensureData( index, count );
      if( !equals( font, data[ index ].font ) ) {
        data[ index ].font = font;
        data[ index ].preferredWidthBuffer = Data.UNKNOWN_WIDTH;
        markCached();
        parent.redraw();
      }
    }
  }

  /**
   * Sets the foreground color at the given column index in the receiver to the
   * color specified by the argument, or to the default system color for the
   * item if the argument is null.
   *
   * @param index the column index
   * @param color the new color (or null)
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed
   *              </li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setForeground( int index, Color color ) {
    checkWidget();
    if( color != null && color.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    int count = Math.max( 1, parent.getColumnCount() );
    if( index >= 0 && index < count ) {
      ensureData( index, count );
      if( !equals( data[ index ].foreground, color ) ) {
        data[ index ].foreground = color;
        markCached();
        parent.redraw();
      }
    }
  }

  /**
   * Sets the font that the receiver will use to paint textual information for
   * this item to the font specified by the argument, or to the default font for
   * that kind of control if the argument is null.
   *
   * @param font the new font (or null)
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed
   *              </li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setFont( Font font ) {
    checkWidget();
    if( font != null && font.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    if( !equals( this.font, font ) ) {
      this.font = font;
      markCached();
      if( parent.getColumnCount() == 0 ) {
        parent.updateScrollBars();
      }
      parent.redraw();
    }
  }

  /**
   * Returns the font that the receiver will use to paint textual information
   * for this item.
   *
   * @return the receiver's font
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public Font getFont() {
    checkWidget();
    if( !parent.checkData( this, index ) ) {
      error( SWT.ERROR_WIDGET_DISPOSED );
    }
    Font result;
    if( font == null ) {
      result = parent.getFont();
    } else {
      result = font;
    }
    return result;
  }

  /**
   * Sets the receiver's background color to the color specified by the
   * argument, or to the default system color for the item if the argument is
   * null.
   *
   * @param value the new color (or null)
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed
   *              </li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setBackground( Color value ) {
    checkWidget();
    if( value != null && value.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    if( !equals( background, value ) ) {
      background = value;
      markCached();
    }
  }

  /**
   * Returns the receiver's background color.
   *
   * @return the background color
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public Color getBackground() {
    checkWidget();
    if( !parent.checkData( this, index ) ) {
      error( SWT.ERROR_WIDGET_DISPOSED );
    }
    Color result;
    if( background == null ) {
      result = parent.getBackground();
    } else {
      result = background;
    }
    return result;
  }

  /**
   * Returns the foreground color that the receiver will use to draw.
   *
   * @return the receiver's foreground color
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public Color getForeground() {
    checkWidget();
    if( !parent.checkData( this, index ) ) {
      error( SWT.ERROR_WIDGET_DISPOSED );
    }
    Color result;
    if( foreground == null ) {
      result = parent.getForeground();
    } else {
      result = foreground;
    }
    return result;
  }

  /**
   * Sets the receiver's foreground color to the color specified by the
   * argument, or to the default system color for the item if the argument is
   * null.
   *
   * @param value the new color (or null)
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed
   *              </li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setForeground( Color value ) {
    checkWidget();
    if( value != null && value.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    if( !equals( foreground, value ) ) {
      foreground = value;
      markCached();
    }
  }

  /**
   * Sets the checked state of the receiver.
   * <p>
   *
   * @param checked the new checked state
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setChecked( boolean checked ) {
    checkWidget();
    if( ( parent.getStyle() & SWT.CHECK ) != 0 ) {
      if( this.checked != checked ) {
        this.checked = checked;
        markCached();
      }
    }
  }

  /**
   * Returns <code>true</code> if the receiver is checked, and false otherwise.
   * When the parent does not have the <code>CHECK style, return false.
   * <p>
   *
   * @return the checked state
   * @exception SWTException <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver
   *              has been disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if
   *              not called from the thread that created the receiver</li>
   *              </ul>
   */
  public boolean getChecked() {
    checkWidget();
    if( !parent.checkData( this, index ) ) {
      error( SWT.ERROR_WIDGET_DISPOSED );
    }
    return checked;
  }

  /**
   * Sets the grayed state of the checkbox for this item. This state change only
   * applies if the Tree was created with the SWT.CHECK style.
   *
   * @param grayed the new grayed state of the checkbox
   * @exception SWTException <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver
   *              has been disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if
   *              not called from the thread that created the receiver</li>
   *              </ul>
   */
  public void setGrayed( boolean grayed ) {
    checkWidget();
    if( ( parent.getStyle() & SWT.CHECK ) != 0 ) {
      if( this.grayed != grayed ) {
        this.grayed = grayed;
        markCached();
      }
    }
  }

  /**
   * Returns <code>true</code> if the receiver is grayed, and false otherwise.
   * When the parent does not have the <code>CHECK style, return false.
   * <p>
   *
   * @return the grayed state of the checkbox
   * @exception SWTException <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver
   *              has been disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if
   *              not called from the thread that created the receiver</li>
   *              </ul>
   */
  public boolean getGrayed() {
    checkWidget();
    if( !parent.checkData( this, index ) ) {
      error( SWT.ERROR_WIDGET_DISPOSED );
    }
    return grayed;
  }

  /**
   * Returns the receiver's text, which will be an empty string if it has never
   * been set.
   *
   * @return the receiver's text
   * @exception SWTException <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver
   *              has been disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if
   *              not called from the thread that created the receiver</li>
   *              </ul>
   */
  @Override
  public String getText() {
    checkWidget();
    return getText( 0 );
  }

  /**
   * Returns the text stored at the given column index in the receiver, or empty
   * string if the text has not been set.
   *
   * @param index the column index
   * @return the text stored at the given column index in the receiver
   * @exception SWTException <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver
   *              has been disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if
   *              not called from the thread that created the receiver</li>
   *              </ul>
   */
  public String getText( int index ) {
    checkWidget();
    if( !parent.checkData( this, this.index ) ) {
      error( SWT.ERROR_WIDGET_DISPOSED );
    }
    return getTextWithoutMaterialize( index );
  }

  String getTextWithoutMaterialize( int index ) {
    String result = "";
    if( hasData( index ) ) {
      result = data[ index ].text;
    }
    return result;
  }

  /**
   * Returns a rectangle describing the size and location
   * relative to its parent of the text at a column in the
   * tree.
   *
   * @param index the index that specifies the column
   * @return the receiver's bounding text rectangle
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public Rectangle getTextBounds( int index ) {
    checkWidget();
    if( !parent.checkData( this, this.index ) ) {
      error( SWT.ERROR_WIDGET_DISPOSED );
    }
    Rectangle result = new Rectangle( 0, 0, 0, 0 );
    if( isVisible() && isValidColumn( index ) ) {
      result.x = parent.getVisualTextLeft( this, index );
      result.y = getItemTop();
      result.width = parent.getVisualTextWidth( this, index );
      result.height = parent.getItemHeight();
    }
    return result;
  }


  /**
   * Sets the text for multiple columns in the tree.
   *
   * @param value the array of new strings
   * @exception IllegalArgumentException <ul> <li>ERROR_NULL_ARGUMENT - if the
   *              text is null</li> </ul>
   * @exception SWTException <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver
   *              has been disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if
   *              not called from the thread that created the receiver</li>
   *              </ul>
   */
  public void setText( String[] value ) {
    checkWidget();
    if( value == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    for( int i = 0; i < value.length; i++ ) {
      if( value[ i ] != null ) {
        setText( i, value[ i ] );
      }
    }
  }

  /**
   * Sets the receiver's text.
   *
   * @param text the new text
   * @exception IllegalArgumentException <ul> <li>ERROR_NULL_ARGUMENT - if the
   *              text is null</li> </ul>
   * @exception SWTException <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver
   *              has been disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if
   *              not called from the thread that created the receiver</li>
   *              </ul>
   */
  @Override
  public void setText( String text ) {
    checkWidget();
    setText( 0, text );
  }

  /**
   * Sets the receiver's text at a column
   *
   * @param index the column index
   * @param text the new text
   * @exception IllegalArgumentException <ul> <li>ERROR_NULL_ARGUMENT - if the
   *              text is null</li> </ul>
   * @exception SWTException <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver
   *              has been disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if
   *              not called from the thread that created the receiver</li>
   *              </ul>
   */
  public void setText( int index, String text ) {
    checkWidget();
    if( text == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( isMarkupEnabledFor( parent ) && !isValidationDisabledFor( parent ) ) {
      MarkupValidator.getInstance().validate( text );
    }
    int count = Math.max( 1, parent.getColumnCount() );
    if( index >= 0 && index < count ) {
      ensureData( index, count );
      if( !text.equals( data[ index ].text ) ) {
        data[ index ].text = text;
        data[ index ].preferredWidthBuffer = Data.UNKNOWN_WIDTH;
        markCached();
        if( parent.getColumnCount() == 0 ) {
          parent.updateScrollBars();
        }
        parent.redraw();
      }
    }
  }

  /**
   * Returns the receiver's image if it has one, or null
   * if it does not.
   *
   * @return the receiver's image
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   * @since 1.4
   */
  @Override
  public Image getImage() {
    checkWidget();
    return getImage( 0 );
  }

  /**
   * Returns the image stored at the given column index in the receiver, or null
   * if the image has not been set or if the column does not exist.
   *
   * @param index the column index
   * @return the image stored at the given column index in the receiver
   * @exception SWTException <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver
   *              has been disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if
   *              not called from the thread that created the receiver</li>
   *              </ul>
   */
  public Image getImage( int index ) {
    checkWidget();
    if( !parent.checkData( this, this.index ) ) {
      error( SWT.ERROR_WIDGET_DISPOSED );
    }
    Image result = null;
    if( hasData( index ) ) {
      result = data[ index ].image;
    }
    return result;
  }

  /**
   * Returns a rectangle describing the size and location relative to its parent
   * of an image at a column in the tree.
   *
   * @param index the index that specifies the column
   * @return the receiver's bounding image rectangle
   * @exception SWTException <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver
   *              has been disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if
   *              not called from the thread that created the receiver</li>
   *              </ul>
   */
  public Rectangle getImageBounds( int index ) {
    checkWidget();
    if( !parent.checkData( this, this.index ) ) {
      error( SWT.ERROR_WIDGET_DISPOSED );
    }
    Rectangle result = null;
    int validColumnCount = Math.max( 1, parent.columnHolder.size() );
    if( ( 0 <= index && index < validColumnCount ) ) {
      result = new Rectangle( 0, 0, 0, 0 );
      Point size = parent.getItemImageSize( index );
      result.width = size.x;
      result.height = size.y;
      result.x = parent.getVisualCellLeft( this, index );
      // Note: The left cell-padding is visually ignored for the tree-column
      if( !parent.isTreeColumn( index ) ) {
        result.x += parent.getCellPadding().left;
      }
      // SWT behavior on windows gives the correct y value
      // On Gtk the y value is always the same (eg. 1)
      // we emulate the default windows behavior here
      result.y = getItemTop();
    } else {
      result = new Rectangle( 0, 0, 0, 0 );
    }
    return result;
  }

  void clear() {
    data = null;
    checked = false;
    grayed = false;
    foreground = null;
    background = null;
    font = null;
    clearCached();
    parent.updateScrollBars();
  }

  /**
   * Clears the item at the given zero-relative index in the receiver. The text,
   * icon and other attributes of the item are set to the default value. If the
   * tree was created with the <code>SWT.VIRTUAL</code> style, these attributes
   * are requested again as needed.
   *
   * @param index the index of the item to clear
   * @param recursive <code>true</code> if all child items of the indexed item
   *          should be cleared recursively, and <code>false</code> otherwise
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_INVALID_RANGE - if the index is not between 0 and
   *              the number of elements in the list minus 1 (inclusive)</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @see SWT#VIRTUAL
   * @see SWT#SetData
   */
  public void clear( int index, boolean recursive ) {
    checkWidget();
    if( index < 0 || index >= itemCount ) {
      error( SWT.ERROR_INVALID_RANGE );
    }
    TreeItem item = items[ index ];
    if( item != null ) {
      item.clear();
      if( recursive ) {
        item.clearAll( true, false );
      }
      if( parent.isVirtual() ) {
        parent.redraw();
      }
    }
  }

  @Override
  public void setImage( Image image ) {
    checkWidget();
    setImage( 0, image );
  }

  /**
   * Sets the receiver's image at a column.
   *
   * @param index the column index
   * @param image the new image
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_INVALID_ARGUMENT - if the image has been disposed
   *              </li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setImage( int index, Image image ) {
    checkWidget();
    if( image != null && image.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    int count = Math.max( 1, parent.getColumnCount() );
    if( index >= 0 && index < count ) {
      ensureData( index, count );
      if( !equals( data[ index ].image, image ) ) {
        parent.updateColumnImageCount( index, data[ index ].image, image );
        data[ index ].image = image;
        data[ index ].preferredWidthBuffer = Data.UNKNOWN_WIDTH;
        parent.updateItemImageSize( image );
        markCached();
        if( parent.getColumnCount() == 0 ) {
          parent.updateScrollBars();
        }
        parent.redraw();
      }
    }
  }

  /**
   * Sets the image for multiple columns in the tree.
   *
   * @param value the array of new images
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the array of images is null</li>
   *              <li>ERROR_INVALID_ARGUMENT - if one of the images has been
   *              disposed</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setImage( Image[] value ) {
    checkWidget();
    if( value == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    for( int i = 0; i < value.length; i++ ) {
      if( value[ i ] != null && value[ i ].isDisposed() ) {
        error( SWT.ERROR_INVALID_ARGUMENT );
      }
    }
    for( int i = 0; i < value.length; i++ ) {
      if( value[ i ] != null ) {
        setImage( i, value[ i ] );
      }
    }
  }

  /**
   * Clears all the items in the receiver. The text, icon and other attributes
   * of the items are set to their default values. If the tree was created with
   * the <code>SWT.VIRTUAL</code> style, these attributes are requested again as
   * needed.
   *
   * @param recursive <code>true</code> if all child items should be cleared
   *          recursively, and <code>false</code> otherwise
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @see SWT#VIRTUAL
   * @see SWT#SetData
   */
  public void clearAll( boolean recursive ) {
    clearAll( recursive, true );
  }

  void clearAll( boolean recursive, boolean doVisualUpdate ) {
    checkWidget();
    for( int i = 0; i < itemCount; i++ ) {
      TreeItem item = items[ i ];
      if( item != null ) {
        item.clear();
        if( recursive ) {
          item.clearAll( true, false );
        }
      }
    }
    if( parent.isVirtual() && doVisualUpdate ) {
      parent.redraw();
    }
  }

  ////////////////////////////////////////
  // Methods to maintain (sub-) TreeItems

  /**
   * Returns a (possibly empty) array of <code>TreeItem</code>s which are the
   * direct item children of the receiver.
   * <p>
   * Note: This is not the actual structure used by the receiver to maintain its
   * list of items, so modifying the array will not affect the receiver.
   * </p>
   *
   * @return the receiver's items
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public TreeItem[] getItems() {
    checkWidget();
    TreeItem[] result = new TreeItem[ itemCount ];
    if( parent.isVirtual() ) {
      for( int i = 0; i < itemCount; i++ ) {
        result[ i ] = _getItem( i );
      }
    } else {
      System.arraycopy( items, 0, result, 0, itemCount );
    }
    return result;
  }

  TreeItem _getItem( int index ) {
    if( parent.isVirtual() && items[ index ] == null ) {
      items[ index ] = new TreeItem( parent, this, SWT.NONE, index, false );
    }
    return items[ index ];
  }

  TreeItem[] getCreatedItems() {
    TreeItem[] result;
    if( parent.isVirtual() ) {
      int count = 0;
      for( int i = 0; i < itemCount; i++ ) {
        if( items[ i ] != null ) {
          count++;
        }
      }
      result = new TreeItem[ count ];
      count = 0;
      for( int i = 0; i < itemCount; i++ ) {
        if( items[ i ] != null ) {
          result[ count ] = items[ i ];
          count++;
        }
      }
    } else {
      result = new TreeItem[ itemCount ];
      System.arraycopy( items, 0, result, 0, itemCount );
    }
    return result;
  }

  /**
   * Returns the item at the given, zero-relative index in the receiver. Throws
   * an exception if the index is out of range.
   *
   * @param index the index of the item to return
   * @return the item at the given index
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_INVALID_RANGE - if the index is not between 0 and
   *              the number of elements in the list minus 1 (inclusive)</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public TreeItem getItem( int index ) {
    checkWidget();
    if( !parent.checkData( this, this.index ) ) {
      error( SWT.ERROR_WIDGET_DISPOSED );
    }
    if( index < 0 || index >= itemCount ) {
      SWT.error( SWT.ERROR_INVALID_RANGE );
    }
    return _getItem( index );
  }

  /**
   * Returns the number of items contained in the receiver that are direct item
   * children of the receiver.
   *
   * @return the number of items
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public int getItemCount() {
    checkWidget();
    if( !parent.checkData( this, index ) ) {
      error( SWT.ERROR_WIDGET_DISPOSED );
    }
    return itemCount;
  }

  /**
   * Searches the receiver's list starting at the first item (index 0) until an
   * item is found that is equal to the argument, and returns the index of that
   * item. If no item is found, returns -1.
   *
   * @param item the search item
   * @return the index of the item
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the tool item is null</li>
   *              <li>ERROR_INVALID_ARGUMENT - if the tool item has been
   *              disposed</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public int indexOf( TreeItem item ) {
    checkWidget();
    if( item == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( item.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    return item.parentItem == this ? item.index : -1;
  }

  /**
   * Removes all of the items from the receiver.
   * <p>
   *
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void removeAll() {
    checkWidget();
    for( int i = itemCount - 1; i >= 0; i-- ) {
      if( items[ i ] != null ) {
        items[ i ].dispose();
      } else {
        itemCount--;
      }
    }
    setEmpty();
  }

  /**
   * Sets the number of child items contained in the receiver.
   *
   * @param count the number of items
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setItemCount( int count ) {
    checkWidget();
    int oldItemCount = itemCount;
    int newItemCount = Math.max( 0, count );
    if( newItemCount != oldItemCount ) {
      int index = oldItemCount - 1;
      while( index >= newItemCount ) {
        TreeItem item = items[ index ];
        if( item != null && !item.isDisposed() ) {
          item.dispose();
        }
        index--;
      }
      int length = Math.max( 4, ( newItemCount + 3 ) / 4 * 4 );
      TreeItem[] newItems = new TreeItem[ length ];
      System.arraycopy( items, 0, newItems, 0, Math.min( newItemCount, itemCount ) );
      items = newItems;
      if( !parent.isVirtual() ) {
        for( int i = oldItemCount; i < newItemCount; i++ ) {
          new TreeItem( this, SWT.NONE, i );
        }
      }
      itemCount = newItemCount;
      parent.invalidateFlatIndex();
      parent.updateScrollBars();
      parent.redraw();
    }
  }

  /////////////////////////////////
  // Methods to dispose of the item

  @Override
  final void releaseChildren() {
    for( int i = items.length - 1; i >= 0; i-- ) {
      if( items[ i ] != null ) {
        items[ i ].dispose();
      }
    }
  }

  @Override
  final void releaseParent() {
    if( parentItem != null ) {
      parentItem.destroyItem( index );
    } else {
      parent.destroyItem( index );
    }
    if( !parent.isInDispose() ) {
      parent.invalidateFlatIndex();
      parent.removeFromSelection( this );
      parent.updateScrollBars();
    }
    super.releaseParent();
  }

  //////////////////
  // helping methods

  private boolean isValidColumn( int index ) {
    int columnCount = parent.getColumnCount();
    return ( columnCount == 0 && index == 0 ) || ( index >= 0 && index < columnCount );
  }

  private boolean isVisible() {
    return getParentItem() == null || getParentItem().getExpanded();
  }

  int getItemTop() {
    int headerHeight = parent.getHeaderHeight();
    int itemHeight = parent.getItemHeight();
    return headerHeight + ( getFlatIndex() - parent.getTopItemIndex() ) * itemHeight;
  }

  int getFlatIndex() {
    if( !parent.isFlatIndexValid ) {
      parent.updateAllItems();
    }
    return flatIndex;
  }

  void setFlatIndex( int flatIndex ) {
    this.flatIndex = flatIndex;
  }

  boolean hasPreferredWidthBuffer( int index ) {
    return getPreferredWidthBuffer( index ) != Data.UNKNOWN_WIDTH;
  }

  int getPreferredWidthBuffer( int index ) {
    int result = Data.UNKNOWN_WIDTH;
    if( hasData( index ) ) {
      result = data[ index ].preferredWidthBuffer;
    }
    return result;
  }

  void setPreferredWidthBuffer( int index, int preferredWidthBuffer ) {
    int count = Math.max( 1, parent.getColumnCount() );
    ensureData( index, count );
    data[ index ].preferredWidthBuffer = preferredWidthBuffer;
  }

  void clearPreferredWidthBuffers( boolean recursive ) {
    int count = Math.max( 1, parent.getColumnCount() );
    for( int i = 0; i < count; i++ ) {
      if( hasData( i ) ) {
        data[ i ].preferredWidthBuffer = Data.UNKNOWN_WIDTH;
      }
    }
    if( recursive && expanded ) {
      for( int i = 0; i < itemCount; i++ ) {
        TreeItem item = items[ i ];
        if( item != null ) {
          item.clearPreferredWidthBuffers( recursive );
        }
      }
    }
  }

  int getInnerHeight() {
    int innerHeight = itemCount * parent.getItemHeight();
    for( int i = 0; i < itemCount; i++ ) {
      TreeItem item = items[ i ];
      if( item != null && item.getExpanded() ) {
        innerHeight += item.getInnerHeight();
      }
    }
    return innerHeight;
  }

  void markCached() {
    if( parent.isVirtual() ) {
      cached = true;
    }
  }

  private void clearCached() {
    if( parent.isVirtual() ) {
      cached = false;
    }
  }

  boolean isCached() {
    return parent.isVirtual() ? cached : true;
  }

  private static boolean equals( Object object1, Object object2 ) {
    boolean result;
    if( object1 == object2 ) {
      result = true;
    } else if( object1 == null ) {
      result = false;
    } else {
      result = object1.equals( object2 );
    }
    return result;
  }

  ////////////////////////////////////////
  // Manage item data (texts, images, etc)

  private void ensureData( int index, int columnCount ) {
    if( data == null ) {
      data = new Data[ columnCount ];
    } else if( data.length < columnCount ) {
      Data[] newData = new Data[ columnCount ];
      System.arraycopy( data, 0, newData, 0, data.length );
      data = newData;
    }
    if( data[ index ] == null ) {
      data[ index ] = new Data();
    }
  }

  private boolean hasData( int index ) {
    return data != null && index >= 0 && index < data.length && data[ index ] != null;
  }

  final void shiftData( int index ) {
    if( data != null && data.length > index && parent.getColumnCount() > 1 ) {
      Data[] newData = new Data[ data.length + 1 ];
      System.arraycopy( data, 0, newData, 0, index );
      int offSet = data.length - index;
      System.arraycopy( data, index, newData, index + 1, offSet );
      data = newData;
    }
    for( int i = 0; i < itemCount; i++ ) {
      if( items[ i ] != null ) {
        items[ i ].shiftData( index );
      }
    }
  }

  final void removeData( int index ) {
    if( data != null && data.length > index && parent.getColumnCount() > 1 ) {
      Data[] newData = new Data[ data.length - 1 ];
      System.arraycopy( data, 0, newData, 0, index );
      int offSet = data.length - index - 1;
      System.arraycopy( data, index + 1, newData, index, offSet );
      data = newData;
    }
    for( int i = 0; i < itemCount; i++ ) {
      if( items[ i ] != null ) {
        items[ i ].removeData( index );
      }
    }
  }

  private void updateSelection() {
    TreeItem[] selection = parent.getSelection();
    List<TreeItem> selectedItems = new ArrayList<>( Arrays.asList( selection ) );
    if( deselectChildren( selectedItems ) ) {
      if( ( parent.getStyle() & SWT.SINGLE ) != 0 ) {
        selectedItems.add( this );
      }
      parent.setSelection( selectedItems.toArray( new TreeItem[ 0 ] ) );
      Event event = new Event();
      event.item = this;
      parent.notifyListeners( SWT.Selection, event );
    }
  }

  boolean deselectChildren( List<TreeItem> selectedItems ) {
    boolean result = false;
    for( int i = 0; i < itemCount; i++ ) {
      TreeItem item = items[ i ];
      if( item != null ) {
        if( selectedItems.contains( item ) ) {
          selectedItems.remove( item );
          result = true;
        }
        if( item.deselectChildren( selectedItems ) ) {
          result = true;
        }
      }
    }
    return result;
  }

  ////////////////
  // Inner classes

  private final class TreeItemAdapter
    implements ITreeItemAdapter, IWidgetFontAdapter, IWidgetColorAdapter
  {

    @Override
    public boolean isParentDisposed() {
      Widget itemParent = parentItem == null ? parent : parentItem;
      return itemParent.isDisposed();
    }

    @Override
    public Color getUserBackground() {
      return background;
    }

    @Override
    public Color getUserForeground() {
      return foreground;
    }

    @Override
    public Font getUserFont() {
      return font;
    }

    @Override
    public String[] getTexts() {
      int columnCount = Math.max( 1, getParent().getColumnCount() );
      String[] result = null;
      if( data != null ) {
        for( int i = 0; i < data.length; i++ ) {
          String text = data[ i ] == null ? "" : data[ i ].text;
          if( !"".equals( text ) ) {
            if( result == null ) {
              result = new String[ columnCount ];
              Arrays.fill( result, "" );
            }
            result[ i ] = text;
          }
        }
      }
      return result;
    }

    @Override
    public Image[] getImages() {
      int columnCount = Math.max( 1, getParent().getColumnCount() );
      Image[] result = null;
      if( data != null ) {
        for( int i = 0; i < data.length; i++ ) {
          Image image = data[ i ] == null ? null : data[ i ].image;
          if( image != null ) {
            if( result == null ) {
              result = new Image[ columnCount ];
            }
            result[ i ] = image;
          }
        }
      }
      return result;
    }

    @Override
    public Color[] getCellBackgrounds() {
      int columnCount = Math.max( 1, getParent().getColumnCount() );
      Color[] result = null;
      if( data != null ) {
        for( int i = 0; i < data.length; i++ ) {
          Color background = data[ i ] == null ? null : data[ i ].background;
          if( background != null ) {
            if( result == null ) {
              result = new Color[ columnCount ];
            }
            result[ i ] = background;
          }
        }
      }
      return result;
    }

    @Override
    public Color[] getCellForegrounds() {
      int columnCount = Math.max( 1, getParent().getColumnCount() );
      Color[] result = null;
      if( data != null ) {
        for( int i = 0; i < data.length; i++ ) {
          Color foreground = data[ i ] == null ? null : data[ i ].foreground;
          if( foreground != null ) {
            if( result == null ) {
              result = new Color[ columnCount ];
            }
            result[ i ] = foreground;
          }
        }
      }
      return result;
    }

    @Override
    public Font[] getCellFonts() {
      int columnCount = Math.max( 1, getParent().getColumnCount() );
      Font[] result = null;
      if( data != null ) {
        for( int i = 0; i < data.length; i++ ) {
          Font font = data[ i ] == null ? null : data[ i ].font;
          if( font != null ) {
            if( result == null ) {
              result = new Font[ columnCount ];
            }
            result[ i ] = font;
          }
        }
      }
      return result;
    }

  }

  private final class CompositeItemHolder implements IItemHolderAdapter<Item> {

    @Override
    public void add( Item item ) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void insert( Item item, int index ) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void remove( Item item ) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Item[] getItems() {
      TreeItem[] items = getCreatedItems();
      Item[] result = new Item[ items.length ];
      System.arraycopy( items, 0, result, 0, items.length );
      return result;
    }

  }

  private static final class Data implements SerializableCompatibility {
    static final int UNKNOWN_WIDTH = -1;
    String text = "";
    // Note [fappel]: Yourkit analysis with the UI workbench testsuite showed an extensive
    //                appearance of preferred width calculations. Buffering the preferred width
    //                speeds up the suite on my machine to 1/4th of the time needed without buffering.
    int preferredWidthBuffer = UNKNOWN_WIDTH;
    Image image;
    Font font;
    Color background;
    Color foreground;
  }

}
