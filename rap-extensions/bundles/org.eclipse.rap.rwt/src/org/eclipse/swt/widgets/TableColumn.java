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

import static org.eclipse.swt.internal.widgets.MarkupUtil.checkMarkupPrecondition;
import static org.eclipse.swt.internal.widgets.MarkupUtil.isToolTipMarkupEnabledFor;
import static org.eclipse.swt.internal.widgets.MarkupUtil.MarkupTarget.TOOLTIP;
import static org.eclipse.swt.internal.widgets.MarkupValidator.isValidationDisabledFor;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.SerializableCompatibility;
import org.eclipse.swt.internal.widgets.IColumnAdapter;
import org.eclipse.swt.internal.widgets.MarkupValidator;
import org.eclipse.swt.internal.widgets.tablecolumnkit.TableColumnLCA;


/**
 * Instances of this class represent a column in a table widget.
 * <p><dl>
 * <dt><b>Styles:</b></dt>
 * <dd>LEFT, RIGHT, CENTER</dd>
 * <dt><b>Events:</b></dt>
 * <dd> Move, Resize, Selection</dd>
 * </dl>
 * </p><p>
 * Note: Only one of the styles LEFT, RIGHT and CENTER may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @since 1.0
 */
public class TableColumn extends Item {

  // TODO [rh] Fow now: keep in sync with settings in appearance theme, should
  //      be moved to theme adapter or similar
  private static final int SORT_INDICATOR_WIDTH = 10;
  private static final int SPACING = 2;

  private final Table parent;
  private final IColumnAdapter columnAdapter;
  private int width;
  private String toolTipText;
  private boolean resizable;
  private boolean moveable;
  private boolean packed;

  /**
   * Constructs a new instance of this class given its parent
   * (which must be a <code>Table</code>) and a style value
   * describing its behavior and appearance. The item is added
   * to the end of the items maintained by its parent.
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
   * @see SWT#LEFT
   * @see SWT#RIGHT
   * @see SWT#CENTER
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public TableColumn( Table parent, int style ) {
    this( parent, checkStyle( style ), checkNull( parent ).getColumnCount() );
  }

  /**
   * Constructs a new instance of this class given its parent
   * (which must be a <code>Table</code>), a style value
   * describing its behavior and appearance, and the index
   * at which to place it in the items maintained by its parent.
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
   * @param index the zero-relative index to store the receiver in its parent
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the parent (inclusive)</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   * </ul>
   *
   * @see SWT#LEFT
   * @see SWT#RIGHT
   * @see SWT#CENTER
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public TableColumn( Table parent, int style, int index ) {
    super( parent, checkStyle( style ) );
    this.parent = parent;
    resizable = true;
    columnAdapter = new ColumnAdapter();
    this.parent.createColumn( this, index );
  }

  /**
   * Returns the receiver's parent, which must be a <code>Table</code>.
   *
   * @return the receiver's parent
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Table getParent() {
    checkWidget();
    return parent;
  }

  /**
   * Gets the width of the receiver.
   *
   * @return the width
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getWidth() {
    checkWidget();
    return width;
  }

  /**
   * Sets the receiver's tool tip text to the argument, which
   * may be null indicating that no tool tip text should be shown.
   *
   * @param toolTipText the new tool tip text (or null)
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setToolTipText( String toolTipText ) {
    checkWidget();
    if(    toolTipText != null
        && isToolTipMarkupEnabledFor( this )
        && !isValidationDisabledFor( this ) )
    {
      MarkupValidator.getInstance().validate( toolTipText );
    }
    this.toolTipText = toolTipText;
  }

  /**
   * Returns the receiver's tool tip text, or null if it has
   * not been set.
   *
   * @return the receiver's tool tip text
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public String getToolTipText() {
    checkWidget();
    return toolTipText;
  }

  /**
   * Controls how text and images will be displayed in the receiver.
   * The argument should be one of <code>LEFT</code>, <code>RIGHT</code>
   * or <code>CENTER</code>.
   *
   * @param alignment the new alignment
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setAlignment( int alignment ) {
    checkWidget();
    if( ( alignment & ( SWT.LEFT | SWT.RIGHT | SWT.CENTER ) ) != 0 ) {
      style &= ~( SWT.LEFT | SWT.RIGHT | SWT.CENTER );
      style |= alignment & ( SWT.LEFT | SWT.RIGHT | SWT.CENTER );
    }
  }

  /**
   * Returns a value which describes the position of the
   * text or image in the receiver. The value will be one of
   * <code>LEFT</code>, <code>RIGHT</code> or <code>CENTER</code>.
   *
   * @return the alignment
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getAlignment() {
    checkWidget();
    int result = SWT.LEFT;
    if( ( style & SWT.CENTER ) != 0 ) {
      result = SWT.CENTER;
    }
    if( ( style & SWT.RIGHT ) != 0 ) {
      result = SWT.RIGHT;
    }
    return result;
  }

  /**
   * Sets the width of the receiver.
   *
   * @param width the new width
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setWidth( int width ) {
    checkWidget();
    if( width >= 0 ) {
      this.width = width;
      parent.updateScrollBars();
      notifyListeners( SWT.Resize, new Event() );
      processNextColumnsMoveEvent();
      packed = false;
    }
  }

  /**
   * Causes the receiver to be resized to its preferred size.
   * For a composite, this involves computing the preferred size
   * from its layout, if there is one.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void pack() {
    checkWidget();
    int width = getPreferredWidth();
    if( width != getWidth() ) {
      setWidth( width );
    }
    packed = true;
  }

  /**
   * Sets the moveable attribute.  A column that is
   * moveable can be reordered by the user by dragging
   * the header. A column that is not moveable cannot be
   * dragged by the user but may be reordered
   * by the programmer.
   *
   * @param moveable the moveable attribute
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see Table#setColumnOrder(int[])
   * @see Table#getColumnOrder()
   * @see TableColumn#getMoveable()
   * @see SWT#Move
   */
  public void setMoveable( boolean moveable ) {
    checkWidget();
    this.moveable = moveable;
  }

  /**
   * Gets the moveable attribute. A column that is
   * not moveable cannot be reordered by the user
   * by dragging the header but may be reordered
   * by the programmer.
   *
   * @return the moveable attribute
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see Table#getColumnOrder()
   * @see Table#setColumnOrder(int[])
   * @see TableColumn#setMoveable(boolean)
   * @see SWT#Move
   */
  public boolean getMoveable() {
    checkWidget();
    return moveable;
  }

  /**
   * Sets the resizable attribute.  A column that is
   * resizable can be resized by the user dragging the
   * edge of the header.  A column that is not resizable
   * cannot be dragged by the user but may be resized
   * by the programmer.
   *
   * @param resizable the resize attribute
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setResizable( boolean resizable ) {
    checkWidget();
    this.resizable = resizable;
  }

  /**
   * Gets the resizable attribute. A column that is
   * not resizable cannot be dragged by the user but
   * may be resized by the programmer.
   *
   * @return the resizable attribute
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public boolean getResizable() {
    checkWidget();
    return resizable;
  }

  ///////////////////////////////////////
  // Listener registration/deregistration

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the control is moved or resized, by sending
   * it one of the messages defined in the <code>ControlListener</code>
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
   * @see ControlListener
   * @see #removeControlListener
   */
  public void addControlListener( ControlListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.Move, typedListener );
    addListener( SWT.Resize, typedListener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when the control is moved or resized.
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
   * @see ControlListener
   * @see #addControlListener
   */
  public void removeControlListener( ControlListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    removeListener( SWT.Move, listener );
    removeListener( SWT.Resize, listener );
  }

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the control is selected, by sending
   * it one of the messages defined in the <code>SelectionListener</code>
   * interface.
   * <p>
   * <code>widgetSelected</code> is called when the column header is selected.
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
   * be notified when the control is selected.
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
  public void setData( String key, Object value ) {
    if( !RWT.TOOLTIP_MARKUP_ENABLED.equals( key ) || !isToolTipMarkupEnabledFor( this ) ) {
      checkMarkupPrecondition( key, TOOLTIP, () -> toolTipText == null );
      super.setData( key, value );
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == IColumnAdapter.class ) {
      return ( T )columnAdapter;
    }
    if( adapter == WidgetLCA.class ) {
      return ( T )TableColumnLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  ////////////////////
  // Widget dimensions

  final int getPreferredWidth () {
    // Compute width from the column itself
    int result = 0;
    Font font = parent.getHeaderFont();
    if( text.length() > 0 ) {
      if( text.indexOf( '\n' ) != -1 ) {
        result = TextSizeUtil.textExtent( font, text, 0 ).x;
      } else {
        result = TextSizeUtil.stringExtent( font, text ).x;
      }
    }
    Image image = getImage();
    if( image != null ) {
      result += image.getBounds().width + SPACING;
    }
    if( parent.getSortColumn() == this && parent.getSortDirection() != SWT.NONE ) {
      result += SORT_INDICATOR_WIDTH + SPACING;
    }
    BoxDimensions headerPadding = parent.getThemeAdapter().getHeaderPadding( parent );
    result += headerPadding.left + headerPadding.right;
    // Mimic Windows behaviour that forces first item to resolve
    if( parent.getItemCount() > 0 && parent.getCachedItems().length == 0 ) {
      parent.checkData( parent.getItem( 0 ), 0 );
    }
    // Extend computed width if there are wider items
    int columnIndex = parent.indexOf( this );
    int itemsPreferredWidth = parent.getItemsPreferredWidth( columnIndex );
    // Add 1px for the right column border
    return Math.max( result, itemsPreferredWidth ) + 1;
  }

  ////////////////////////////
  // Widget and Item overrides

  @Override
  void releaseParent() {
    super.releaseParent();
    parent.destroyColumn( this );
  }

  @Override
  String getNameText() {
    return getText();
  }

  //////////////
  // Left offset

  final int getLeft() {
    int result = 0;
    TableColumn[] columns = parent.getColumns();
    int[] columnOrder = parent.getColumnOrder();
    int orderedIndex = -1;
    for( int i = 0; orderedIndex == -1 && i < columnOrder.length; i++ ) {
      if( columnOrder[ i ] == parent.indexOf( this ) ) {
        orderedIndex = i;
      }
    }
    for( int i = 0; i < orderedIndex; i++ ) {
      result += columns[ columnOrder[ i ] ].getWidth();
    }
    return result;
  }

  //////////////////
  // Helping methods

  private static int checkStyle( int style ) {
    return checkBits( style, SWT.LEFT, SWT.CENTER, SWT.RIGHT, 0, 0, 0 );
  }

  private static Table checkNull( Table table ) {
    if( table == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    return table;
  }

  private void processNextColumnsMoveEvent() {
    int[] columnsOrder = parent.getColumnOrder();
    boolean found = false;
    for( int i = 0; i < columnsOrder.length; i++ ) {
      TableColumn column = parent.getColumn( columnsOrder[ i ] );
      if( column == this ) {
        found = true;
      } else if( found ) {
        column.notifyListeners( SWT.Move, new Event() );
      }
    }
  }

  ////////////////
  // Inner classes

  private final class ColumnAdapter implements IColumnAdapter, SerializableCompatibility {

    @Override
    public boolean isPacked() {
      return packed;
    }

    @Override
    public void clearPacked() {
      packed = false;
    }

  }
}
