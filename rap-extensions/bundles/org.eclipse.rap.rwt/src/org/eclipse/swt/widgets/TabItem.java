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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.MarkupValidator;
import org.eclipse.swt.internal.widgets.tabfolderkit.TabFolderThemeAdapter;
import org.eclipse.swt.internal.widgets.tabitemkit.TabItemLCA;


/**
 * Instances of this class represent a selectable user interface object
 * corresponding to a tab for a page in a tab folder.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 */
public class TabItem extends Item {

  // [if] This constants must be kept in sync with AppearancesBase.js
  private final static int TABS_SPACING = 1;
  private final static int IMAGE_TEXT_SPACING = 4;

  private final TabFolder parent;
  private Control control;
  private String toolTipText;

  /**
   * Constructs a new instance of this class given its parent
   * (which must be a <code>TabFolder</code>) and a style value
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
   * @see SWT
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public TabItem( TabFolder parent, int style ) {
    super( parent, checkStyle( style ) );
    this.parent = parent;
    parent.createItem( this, parent.getItemCount() );
  }

  /**
   * Constructs a new instance of this class given its parent
   * (which must be a <code>TabFolder</code>), a style value
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
   * @see SWT
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public TabItem( TabFolder parent, int style, int index ) {
    super( parent, checkStyle( style ) );
    this.parent = parent;
    parent.createItem( this, index );
  }

  /**
   * Returns the receiver's parent, which must be a <code>TabFolder</code>.
   *
   * @return the receiver's parent
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public TabFolder getParent() {
    checkWidget();
    return parent;
  }

  /**
   * Returns the control that is used to fill the client area of
   * the tab folder when the user selects the tab item.  If no
   * control has been set, return <code>null</code>.
   * <p>
   * @return the control
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Control getControl() {
    checkWidget();
    return control;
  }

  /**
   * Sets the control that is used to fill the client area of
   * the tab folder when the user selects the tab item.
   * <p>
   * @param control the new control (or null)
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the control has been disposed</li>
   *    <li>ERROR_INVALID_PARENT - if the control is not in the same widget tree</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setControl( Control control ) {
    checkWidget();
    if( control != null ) {
      if( control.isDisposed() ) {
        error( SWT.ERROR_INVALID_ARGUMENT );
      }
      if( control._getParent() != parent ) {
        error( SWT.ERROR_INVALID_PARENT );
      }
    }
    if( this.control != null && this.control.isDisposed() ) {
      this.control = null;
    }
    Control oldControl = this.control;
    Control newControl = control;
    this.control = control;
    int index = parent.indexOf( this );
    if( index != parent.getSelectionIndex() ) {
      if( newControl != null ) {
        newControl.setVisible( false );
      }
    } else {
      if( newControl != null ) {
        newControl.setBounds( parent.getClientArea() );
        newControl.setVisible( true );
      }
      if( oldControl != null ) {
        oldControl.setVisible( false );
      }
    }
  }

  /**
   * Returns a rectangle describing the receiver's size and location
   * relative to its parent.
   *
   * @return the receiver's bounding rectangle
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public Rectangle getBounds() {
    checkWidget();
    Rectangle result = new Rectangle( 0, 0, 0, 0 );
    int index = parent.indexOf( this );
    if( index != -1 ) {
      String text = getText();
      if( text != null ) {
        Point extent = TextSizeUtil.stringExtent( parent.getFont(), text );
        result.width = extent.x;
        result.height = extent.y;
      }
      Image image = getImage();
      if( image != null ) {
        Rectangle imageSize = image.getBounds();
        result.width += imageSize.width + IMAGE_TEXT_SPACING;
        result.height = Math.max( result.height, imageSize.height );
      }
      TabFolderThemeAdapter themeAdapter = parent.getThemeAdapter();
      BoxDimensions padding = themeAdapter.getItemPadding( this );
      BoxDimensions margin = themeAdapter.getItemMargin( this );
      BoxDimensions itemBorder = themeAdapter.getItemBorder( this );
      result.width += itemBorder.left + itemBorder.right + padding.left + padding.right;
      result.height += itemBorder.top + itemBorder.bottom + padding.top + padding.bottom;
      if( isBarTop() ) {
        result.y = margin.top;
      } else {
        BoxDimensions border = parent.getBorder();
        result.y = parent.getBounds().height - ( border.top + border.bottom ) - result.height;
        result.y -= margin.bottom;
      }
      if( index > 0 ) {
        TabItem prevItem = parent.getItem( index - 1 );
        Rectangle prevItemBounds = prevItem.getBounds();
        int selectionIndex = parent.getSelectionIndex();
        result.x = prevItemBounds.x + prevItemBounds.width + TABS_SPACING;
        if( index == selectionIndex || index - 1 == selectionIndex ) {
          result.x -= TABS_SPACING;
        }
      }
    }
    return result;
  }

  private boolean isBarTop() {
    return ( parent.getStyle() & SWT.BOTTOM ) == 0;
  }

  @Override
  public void setImage( Image image ) {
    checkWidget();
    int index = parent.indexOf( this );
    if( index > -1 ) {
      super.setImage( image );
    }
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
   *
   * @since 1.2
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
   *
   * @since 1.2
   */
  public String getToolTipText() {
    checkWidget();
    return toolTipText;
  }

  @Override
  public void setData( String key, Object value ) {
    handleBadge( key, value );
    if( !RWT.TOOLTIP_MARKUP_ENABLED.equals( key ) || !isToolTipMarkupEnabledFor( this ) ) {
      checkMarkupPrecondition( key, TOOLTIP, () -> toolTipText == null );
      super.setData( key, value );
    }
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == WidgetLCA.class ) {
      return ( T )TabItemLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  private void handleBadge( String key, Object value ) {
    if( RWT.BADGE.equals( key ) && value != null && !( value instanceof String ) ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
  }

  ///////////////////////////////////
  // Methods to dispose of the widget

  @Override
  void releaseParent() {
    super.releaseParent();
    parent.destroyItem( this );
  }

  //////////////////
  // Helping methods

  private static int checkStyle( int style ) {
    int result = SWT.NONE;
    if( style > 0 ) {
      result = style;
    }
    return result;
  }

}
