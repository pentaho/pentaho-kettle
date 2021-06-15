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
import org.eclipse.rap.rwt.internal.theme.Size;
import org.eclipse.rap.rwt.internal.theme.ThemeAdapter;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.IToolItemAdapter;
import org.eclipse.swt.internal.widgets.MarkupValidator;
import org.eclipse.swt.internal.widgets.toolbarkit.ToolBarThemeAdapter;
import org.eclipse.swt.internal.widgets.toolitemkit.ToolItemLCA;


/**
 * Instances of this class represent a selectable user interface object
 * that represents a button in a tool bar.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>PUSH, CHECK, RADIO, SEPARATOR, DROP_DOWN</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles CHECK, PUSH, RADIO, SEPARATOR and DROP_DOWN
 * may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 */
public class ToolItem extends Item {

  private static final int DEFAULT_WIDTH = 24;
  private static final int DEFAULT_HEIGHT = 22;

  private final ToolBar parent;
  private boolean selected;
  private Control control;
  private int width;
  private boolean computedWidth;
  private String toolTipText;
  private boolean visible;
  private Image disabledImage;
  private Image hotImage;
  private transient IToolItemAdapter toolItemAdapter;


  /**
   * Constructs a new instance of this class given its parent
   * (which must be a <code>ToolBar</code>) and a style value
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
   * @see SWT#PUSH
   * @see SWT#CHECK
   * @see SWT#RADIO
   * @see SWT#SEPARATOR
   * @see SWT#DROP_DOWN
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public ToolItem( ToolBar parent, int style ) {
    this( checkNull( parent ), checkStyle( style ), parent.getItemCount() );
  }

  /**
   * Constructs a new instance of this class given its parent
   * (which must be a <code>ToolBar</code>), a style value
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
   * @see SWT#PUSH
   * @see SWT#CHECK
   * @see SWT#RADIO
   * @see SWT#SEPARATOR
   * @see SWT#DROP_DOWN
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public ToolItem( ToolBar parent, int style, int index ) {
    super( parent, checkStyle( style ) );
    this.parent = parent;
    visible = true;
    computedWidth = true;
    parent.createItem( this, index );
    computeInitialWidth();
  }

  /**
   * Returns the receiver's parent, which must be a <code>ToolBar</code>.
   *
   * @return the receiver's parent
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public ToolBar getParent() {
    checkWidget();
    return parent;
  }

  ////////////////////////////////////////
  // Displayed content (text, image, etc.)

  /**
   * Sets the receiver's text. The string may include
   * the mnemonic character.
   * </p>
   * <p>
   * Mnemonics are indicated by an '&amp;' that causes the next
   * character to be the mnemonic.  When the user presses a
   * key sequence that matches the mnemonic, a selection
   * event occurs. On most platforms, the mnemonic appears
   * underlined but may be emphasised in a platform specific
   * manner.  The mnemonic indicator character '&amp;' can be
   * escaped by doubling it in the string, causing a single
   * '&amp;' to be displayed.
   * </p>
   *
   * @param text the new text
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the text is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  @Override
  public void setText( String text ) {
    checkWidget();
    if( text == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( ( style & SWT.SEPARATOR ) == 0 ) {
      super.setText( text );
      parent.layoutItems();
    }
  }

  @Override
  public void setImage( Image image ) {
    checkWidget();
    if( ( style & SWT.SEPARATOR ) == 0 ) {
      super.setImage( image );
      parent.layoutItems();
    }
  }

  /**
   * Sets the receiver's disabled image to the argument, which may be
   * null indicating that no disabled image should be displayed.
   * <p>
   * The disabled image is displayed when the receiver is disabled.
   * </p>
   *
   * @param image the disabled image to display on the receiver (may be null)
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.2
   */
  public void setDisabledImage( Image image ) {
    checkWidget();
    if( ( style & SWT.SEPARATOR ) == 0 ) {
      disabledImage = image;
      parent.layoutItems();
    }
  }

  /**
   * Returns the receiver's disabled image if it has one, or null
   * if it does not.
   * <p>
   * The disabled image is displayed when the receiver is disabled.
   * </p>
   *
   * @return the receiver's disabled image
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   * @since 1.2
   */
  public Image getDisabledImage() {
    checkWidget();
    return disabledImage;
  }

  /**
   * Sets the receiver's hot image to the argument, which may be
   * null indicating that no hot image should be displayed.
   * <p>
   * The hot image is displayed when the mouse enters the receiver.
   * </p>
   *
   * @param image the hot image to display on the receiver (may be null)
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   * @since 1.2
   */
  public void setHotImage( Image image ) {
    checkWidget();
    if( ( style & SWT.SEPARATOR ) == 0 ) {
      hotImage = image;
      parent.layoutItems();
    }
  }

  /**
   * Returns the receiver's hot image if it has one, or null
   * if it does not.
   * <p>
   * The hot image is displayed when the mouse enters the receiver.
   * </p>
   *
   * @return the receiver's hot image
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   * @since 1.2
   */
  public Image getHotImage() {
    checkWidget();
    return hotImage;
  }

  /**
   * Sets the control that is used to fill the bounds of
   * the item when the item is a <code>SEPARATOR</code>.
   *
   * @param control the new control
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
        SWT.error( SWT.ERROR_INVALID_ARGUMENT );
      }
      if( control.getParent() != parent ) {
        SWT.error( SWT.ERROR_INVALID_PARENT );
      }
    }
    if( ( style & SWT.SEPARATOR ) != 0 ) {
      if( this.control != null && !this.control.isDisposed() ) {
        this.control.setVisible( false );
      }
      this.control = control;
      if( this.control != null ) {
        this.control.setVisible( true );
      }
      resizeControl();
    }
  }

  /**
   * Returns the control that is used to fill the bounds of
   * the item when the item is a <code>SEPARATOR</code>.
   *
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
   * Returns the receiver's tool tip text, or null if it has not been set.
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

  @Override
  public void setData( String key, Object value ) {
    if( RWT.BADGE.equals( key ) && ( style & SWT.PUSH ) == 0 ) {
      return;
    } else if( RWT.TOOLTIP_MARKUP_ENABLED.equals( key ) && isToolTipMarkupEnabledFor( this ) ) {
      return;
    }
    checkMarkupPrecondition( key, TOOLTIP, () -> toolTipText == null );
    super.setData( key, value );
  }

  ///////////
  // Enabled

  /**
   * Enables the receiver if the argument is <code>true</code>,
   * and disables it otherwise.
   * <p>
   * A disabled control is typically
   * not selectable from the user interface and draws with an
   * inactive or "grayed" look.
   * </p>
   *
   * @param enabled the new enabled state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setEnabled( boolean enabled ) {
    checkWidget();
    if( enabled ) {
      removeState( DISABLED );
    } else {
      addState( DISABLED );
    }
  }

  /**
   * Returns <code>true</code> if the receiver is enabled, and
   * <code>false</code> otherwise. A disabled control is typically
   * not selectable from the user interface and draws with an
   * inactive or "grayed" look.
   *
   * @return the receiver's enabled state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #isEnabled
   */
  public boolean getEnabled() {
    checkWidget();
    return !hasState( DISABLED );
  }

  /**
   * Returns <code>true</code> if the receiver is enabled and all
   * of the receiver's ancestors are enabled, and <code>false</code>
   * otherwise. A disabled control is typically not selectable from the
   * user interface and draws with an inactive or "grayed" look.
   *
   * @return the receiver's enabled state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #getEnabled
   */
  public boolean isEnabled() {
    checkWidget();
    return getEnabled() && parent.isEnabled();
  }

  /////////////
  // Dimensions

  /**
   * Returns a rectangle describing the receiver's size and location
   * relative to its parent.
   *
   * @return the receiver's bounding rectangle
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   *    created the receiver</li>
   * </ul>
   */
  public Rectangle getBounds() {
    checkWidget();
    Rectangle clientArea = parent.getClientArea();
    int left = clientArea.x;
    int top = clientArea.y;
    int width = getWidth();
    int height = getHeight();
    int index = parent.indexOf( this );
    BoxDimensions toolBarPadding = parent.getToolBarPadding();
    if( ( parent.style & SWT.VERTICAL ) != 0 ) {
      if( index > 0 ) {
        Rectangle upperSiblingBounds = parent.getItem( index - 1 ).getBounds();
        top += upperSiblingBounds.y + upperSiblingBounds.height;
        top += getToolBarSpacing();
      } else {
        top += toolBarPadding.top;
      }
      int innerParentWidth = parent.getSize().x - toolBarPadding.left - toolBarPadding.right;
      left += toolBarPadding.left + innerParentWidth / 2 - width / 2;
      left = Math.max( left, 0 );
    } else {
      if( index > 0 ) {
        Rectangle leftSiblingBounds = parent.getItem( index - 1 ).getBounds();
        left += leftSiblingBounds.x + leftSiblingBounds.width;
        left += getToolBarSpacing();
      } else {
        left += toolBarPadding.left;
      }
      BoxDimensions border = parent.getBorder();
      int innerParentHeight = parent.getSize().y
                            - ( toolBarPadding.top + toolBarPadding.bottom )
                            - ( border.top + border.bottom );
      top += toolBarPadding.top + innerParentHeight / 2 - height / 2;
      top = Math.max( top, 0 );
    }
    return new Rectangle( left, top, width, height );
  }

   // TODO [tb] : if needed, cache dimensions to optimize performance
   private int getHeight() {
     int height;
     if(    ( parent.style & SWT.VERTICAL ) != 0
         && ( style & SWT.SEPARATOR ) != 0
         && getControl() == null )
     {
       height = getSeparatorWidth();
     } else {
       height = 0;
       ToolItem[] siblings = getParent().getItems();
       for( int i = 0; i < siblings.length; i++ ) {
         height = Math.max(  height, siblings[ i ].getPreferredHeight() );
       }
     }
     return height;
   }

   /**
    * Gets the width of the receiver.
    *
    * @return the width
    *
    * @exception SWTException <ul>
    *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
    *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
    *    created the receiver</li>
    * </ul>
    */
   public int getWidth() {
     checkWidget();
     int result;
     boolean isVertical = ( parent.style & SWT.VERTICAL ) != 0;
     if( ( style & SWT.SEPARATOR ) != 0 && ( !isVertical || !computedWidth ) ) {
       result = width;
     } else {
       if( isVertical ) {
         result = 0;
         ToolItem[] siblings = getParent().getItems();
         for( int i = 0; i < siblings.length; i++ ) {
           if( ( siblings[ i ].style & SWT.SEPARATOR ) == 0 ) {
             result = Math.max(  result, siblings[ i ].getPreferredWidth() );
           }
         }
       } else {
         result = getPreferredWidth();
       }
     }
     return result;
   }

  int getPreferredHeight() {
    int result = 0;
    if( ( style & SWT.SEPARATOR ) == 0 ) {
      boolean styleRight = ( parent.style & SWT.RIGHT ) != 0;
      boolean hasImage = image != null;
      boolean hasText = !"".equals( text );
      if( hasImage ) {
        result += image.getBounds().height;
      }
      if( hasText ) {
        int charHeight = TextSizeUtil.getCharHeight( parent.getFont() );
        result = styleRight ? Math.max( result, charHeight ) : result + charHeight;
      }
      if( !styleRight && hasText && hasImage ) {
        result += getSpacing();
      }
      result += getFrameHeight();
    }
    return Math.max( result, DEFAULT_HEIGHT );
  }

  int getPreferredWidth() {
    int result = 0;
    boolean styleRight = ( parent.style & SWT.RIGHT ) != 0;
    boolean hasImage = image != null;
    boolean hasText = !"".equals( text );
    if( hasImage ) {
      result += image.getBounds().width;
    }
    if( hasText ) {
      int textWidth = TextSizeUtil.stringExtent( parent.getFont(), text ).x;
      result = styleRight ? result + textWidth : Math.max( result, textWidth );
    }
    if( styleRight && hasText && hasImage ) {
      result += getSpacing();
    }
    if( ( style & SWT.DROP_DOWN ) != 0 ) {
      result += getSpacing() * 2;
      result += 1; // the separator-line
      result += getDropDownImageSize().width;
    }
    result += getFrameWidth();
    return result;
  }

  private int getFrameWidth() {
    BoxDimensions border = getBorder();
    BoxDimensions padding = getPadding();
    return padding.left + padding.right + border.left + border.right;
  }

  private int getFrameHeight() {
    BoxDimensions border = getBorder();
    BoxDimensions padding = getPadding();
    return padding.top + padding.bottom + border.top + border.bottom;
  }

  private BoxDimensions getBorder() {
    return getToolBarThemeAdapter().getItemBorder( this );
  }

  private Size getDropDownImageSize() {
    return getToolBarThemeAdapter().getDropDownImageSize( parent );
  }

  private BoxDimensions getPadding() {
    return getToolBarThemeAdapter().getItemPadding( this );
  }

  private int getToolBarSpacing() {
    return getToolBarThemeAdapter().getToolBarSpacing( parent );
  }

  private int getSpacing() {
    return getToolBarThemeAdapter().getItemSpacing( this );
  }

  int getSeparatorWidth() {
    return getToolBarThemeAdapter().getSeparatorWidth( parent );
  }

  private ToolBarThemeAdapter getToolBarThemeAdapter() {
    return ( ToolBarThemeAdapter )parent.getAdapter( ThemeAdapter.class );
  }

  /**
   * Sets the width of the receiver, for <code>SEPARATOR</code> ToolItems.
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
    if( ( style & SWT.SEPARATOR ) != 0 && width >= 0 ) {
      computedWidth = false;
      this.width = width;
      parent.layoutItems();
    }
  }

  ////////////
  // Selection

  /**
   * Returns <code>true</code> if the receiver is selected,
   * and false otherwise.
   * <p>
   * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>,
   * it is selected when it is checked (which some platforms draw as a
   * pushed in button). If the receiver is of any other type, this method
   * returns false.
   * </p>
   *
   * @return the selection state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public boolean getSelection() {
    checkWidget();
    boolean result = selected;
    if( ( style & ( SWT.CHECK | SWT.RADIO ) ) == 0 ) {
      result = false;
    }
    return result;
  }

  /**
   * Sets the selection state of the receiver.
   * <p>
   * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>,
   * it is selected when it is checked (which some platforms draw as a
   * pushed in button).
   * </p>
   *
   * @param selected the new selection state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setSelection( boolean selected ) {
    checkWidget();
    if( ( style & ( SWT.CHECK | SWT.RADIO ) ) != 0 ) {
      this.selected = selected;
    }
  }

  ///////////////////////////////////////////
  // Listener registration and deregistration

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the control is selected, by sending
   * it one of the messages defined in the <code>SelectionListener</code>
   * interface.
   * <p>
   * When <code>widgetSelected</code> is called when the mouse is over the arrow portion of a drop-down tool,
   * the event object detail field contains the value <code>SWT.ARROW</code>.
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

  ///////////////////////////////////
  // Methods to dispose of the widget

  @Override
  void releaseParent() {
    super.releaseParent();
    parent.destroyItem( this );
  }

  //////////////////
  // Helping methods

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    if ( adapter == IToolItemAdapter.class ) {
      if( toolItemAdapter == null ) {
        toolItemAdapter = new IToolItemAdapter() {
          @Override
          public boolean getVisible() {
            return visible;
          }
        };
      }
      return ( T )toolItemAdapter;
    }
    if( adapter == WidgetLCA.class ) {
      return ( T )ToolItemLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  void resizeControl() {
    if( control != null && !control.isDisposed() ) {
      control.setBounds( getBounds() );
    }
  }

  private void computeInitialWidth() {
    if( ( style & SWT.SEPARATOR ) != 0 ) {
      width = getSeparatorWidth();
    } else {
      width = DEFAULT_WIDTH;
      if( ( style & SWT.DROP_DOWN ) != 0 ) {
        width += getDropDownImageSize().width;
      }
    }
  }

  private static ToolBar checkNull( ToolBar parent ) {
    if( parent == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    return parent;
  }

  private static int checkStyle( int style ) {
    return checkBits( style,
                      SWT.PUSH,
                      SWT.CHECK,
                      SWT.RADIO,
                      SWT.SEPARATOR,
                      SWT.DROP_DOWN,
                      0 );
  }

  void setVisible( boolean visible ) {
    this.visible = visible;
  }

}
