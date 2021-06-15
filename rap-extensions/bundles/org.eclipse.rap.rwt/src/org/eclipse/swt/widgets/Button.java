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

import static org.eclipse.rap.rwt.internal.textsize.TextSizeUtil.getCharHeight;
import static org.eclipse.rap.rwt.internal.textsize.TextSizeUtil.textExtent;
import static org.eclipse.swt.internal.widgets.MarkupUtil.checkMarkupPrecondition;
import static org.eclipse.swt.internal.widgets.MarkupUtil.isMarkupEnabledFor;
import static org.eclipse.swt.internal.widgets.MarkupUtil.MarkupTarget.TEXT;
import static org.eclipse.swt.internal.widgets.MarkupValidator.isValidationDisabledFor;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.theme.Size;
import org.eclipse.rap.rwt.internal.theme.ThemeAdapter;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.MarkupValidator;
import org.eclipse.swt.internal.widgets.buttonkit.ButtonLCA;
import org.eclipse.swt.internal.widgets.buttonkit.ButtonThemeAdapter;


/**
 * Instances of this class represent a selectable user interface object that
 * issues notification when pressed and released.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd><!--ARROW, -->CHECK, PUSH, RADIO, TOGGLE, FLAT, WRAP</dd>
 * <dd><!--UP, DOWN, -->LEFT, RIGHT, CENTER</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles ARROW, CHECK, PUSH, RADIO, and TOGGLE
 * may be specified.
 * </p><p>
 * Note: Only one of the styles LEFT, RIGHT, and CENTER may be specified.
 * </p><p>
 * Note: Only one of the styles UP, DOWN, LEFT, and RIGHT may be specified
 * when the ARROW style is specified.
 * </p><p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the SWT implementation.
 * </p>
 * @since 1.0
 */
public class Button extends Control {

  private String text = "";
  private boolean selected;
  private boolean grayed;
  private Image image;
  private boolean isDefault;

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
   * <!--@see SWT#ARROW-->
   * @see SWT#CHECK
   * @see SWT#PUSH
   * @see SWT#RADIO
   * @see SWT#TOGGLE
   * @see SWT#FLAT
   * @see SWT#LEFT
   * @see SWT#RIGHT
   * @see SWT#CENTER
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  // TODO [rst] Remove comments from javadoc when fully implemented
  public Button( Composite parent, int style ) {
    super( parent, checkStyle( style ) );
  }

  @Override
  void initState() {
    if( ( style & ( SWT.PUSH | SWT.TOGGLE ) ) == 0 ) {
      addState( THEME_BACKGROUND );
    }
  }

  ////////////////
  // Getter/setter

  /**
   * Sets the receiver's text.
   * <p>
   * This method sets the button label.  The label may include
   * the mnemonic character but must not contain line delimiters.
   * </p>
   *
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
  public void setText( String text ) {
    checkWidget();
    if( text == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( ( style & SWT.ARROW ) == 0 ) {
      if( isMarkupEnabledFor( this ) && !isValidationDisabledFor( this ) ) {
        MarkupValidator.getInstance().validate( text );
      }
      this.text = text;
    }
  }

  /**
   * Returns the receiver's text, which will be an empty
   * string if it has never been set<!-- or if the receiver is
   * an <code>ARROW</code> button-->.
   *
   * @return the receiver's text
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  // TODO [rst] Remove comments from javadoc when fully implemented
  public String getText() {
    checkWidget();
    return text;
  }

  /**
   * Returns <code>true</code> if the receiver is selected,
   * and false otherwise.
   * <p>
   * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>,
   * it is selected when it is checked. When it is of type <code>TOGGLE</code>,
   * it is selected when it is pushed in. If the receiver is of any other type,
   * this method returns false.
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
    boolean result = false;
    if( ( style & ( SWT.CHECK | SWT.RADIO | SWT.TOGGLE ) ) != 0 ) {
      result = selected;
    }
    return result;
  }

  /**
   * Sets the selection state of the receiver, if it is of type <code>CHECK</code>,
   * <code>RADIO</code>, or <code>TOGGLE</code>.
   *
   * <p>
   * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>,
   * it is selected when it is checked. When it is of type <code>TOGGLE</code>,
   * it is selected when it is pushed in.
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
    if( ( style & ( SWT.CHECK | SWT.RADIO | SWT.TOGGLE ) ) != 0 ) {
      this.selected = selected;
    }
  }

  /**
   * Returns <code>true</code> if the receiver is grayed,
   * and false otherwise. When the widget does not have
   * the <code>CHECK</code> style, return false.
   *
   * @return the grayed state of the checkbox
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.2
   */
  public boolean getGrayed () {
    checkWidget();
    boolean result = false;
    if( ( style & SWT.CHECK ) != 0 ) {
      result = grayed;
    }
    return result;
  }

  /**
   * Sets the grayed state of the receiver.  This state change
   * only applies if the control was created with the SWT.CHECK
   * style.
   *
   * @param grayed the new grayed state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.2
   */
  public void setGrayed( boolean grayed ) {
    checkWidget();
    if( ( style & SWT.CHECK ) != 0 ) {
      this.grayed = grayed;
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
   */
  public Image getImage() {
    checkWidget();
    return image;
  }

  /**
   * Sets the receiver's image to the argument, which may be
   * <code>null</code> indicating that no image should be displayed.
   *
   * @param image the image to display on the receiver (may be <code>null</code>)
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setImage( Image image ) {
    checkWidget();
    if( image != null && image.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    if( ( style & SWT.ARROW ) == 0 ) {
      this.image = image;
    }
  }

  /**
   * Returns a value which describes the position of the
   * text or image in the receiver. The value will be one of
   * <code>LEFT</code>, <code>RIGHT</code> or <code>CENTER</code>
   * unless the receiver is an <code>ARROW</code> button, in
   * which case, the alignment will indicate the direction of
   * the arrow (one of <code>LEFT</code>, <code>RIGHT</code>,
   * <code>UP</code> or <code>DOWN</code>)
   *
   * @return the alignment
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  // TODO [rst] Remove comments from javadoc when fully implemented
  public int getAlignment() {
    checkWidget();
    int result;
    if( ( style & SWT.ARROW ) != 0 ) {
      if( ( style & SWT.UP ) != 0 ) {
        result = SWT.UP;
      } else if( ( style & SWT.DOWN ) != 0 ) {
        result = SWT.DOWN;
      } else if( ( style & SWT.LEFT ) != 0 ) {
        result = SWT.LEFT;
      } else if( ( style & SWT.RIGHT ) != 0 ) {
        result = SWT.RIGHT;
      } else {
        result = SWT.UP;
      }
    } else {
      if( ( style & SWT.LEFT ) != 0 ) {
        result = SWT.LEFT;
      } else if( ( style & SWT.CENTER ) != 0 ) {
        result = SWT.CENTER;
      } else if( ( style & SWT.RIGHT ) != 0 ) {
        result = SWT.RIGHT;
      } else {
        result = SWT.LEFT;
      }
    }
    return result;
  }

  /**
   * Controls how text, images and arrows will be displayed
   * in the receiver. The argument should be one of
   * <code>LEFT</code>, <code>RIGHT</code> or <code>CENTER</code>
   * <!--
   * unless the receiver is an <code>ARROW</code> button, in
   * which case, the argument indicates the direction of
   * the arrow (one of <code>LEFT</code>, <code>RIGHT</code>,
   * <code>UP</code> or <code>DOWN</code>)
   * -->.
   *
   * @param alignment the new alignment
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
   // TODO [rst] Remove comments from javadoc when fully implemented
  public void setAlignment( int alignment ) {
    checkWidget();
    if( ( style & SWT.ARROW ) != 0 ) {
      if( ( style & ( SWT.UP | SWT.DOWN | SWT.LEFT | SWT.RIGHT ) ) != 0 ) {
        style &= ~( SWT.UP | SWT.DOWN | SWT.LEFT | SWT.RIGHT );
        style |= alignment & ( SWT.UP | SWT.DOWN | SWT.LEFT | SWT.RIGHT );
      }
    } else if( ( alignment & ( SWT.LEFT | SWT.RIGHT | SWT.CENTER ) ) != 0 ) {
      style &= ~( SWT.LEFT | SWT.RIGHT | SWT.CENTER );
      style |= alignment & ( SWT.LEFT | SWT.RIGHT | SWT.CENTER );
    }
  }

  @Override
  public Point computeSize( int wHint, int hHint, boolean changed ) {
    checkWidget();
    int width = 0;
    int height = 0;
    ButtonThemeAdapter themeAdapter = getThemeAdapter();
    BoxDimensions padding = themeAdapter.getPadding( this );
    if( ( style & SWT.ARROW ) != 0 ) {
      Size arrowSize = themeAdapter.getArrowSize( this );
      width = arrowSize.width;
      height = arrowSize.height;
    } else {
      boolean hasImage = image != null;
      boolean hasText = text.length() > 0;
      Font font = getFont();
      if( ( style & ( SWT.CHECK | SWT.RADIO ) ) != 0 ) {
        Size checkSize = themeAdapter.getCheckSize( this );
        width += checkSize.width;
        if( hasText || hasImage ) {
          width += themeAdapter.getCheckSpacing( this );
        }
        height = Math.max( height, checkSize.height );
      }
      if( hasImage ) {
        Rectangle imageBounds = image.getBounds();
        width += imageBounds.width;
        height = Math.max( height, imageBounds.height );
      }
      if( hasText && hasImage ) {
        width += themeAdapter.getSpacing( this );
      }
      if( hasText ) {
        Point extent;
        boolean markupEnabled = isMarkupEnabledFor( this );
        int wrapWidth = SWT.DEFAULT;
        if( ( style & SWT.WRAP ) != 0 && wHint != SWT.DEFAULT ) {
          wrapWidth = wHint - width - ( padding.left + padding.right );
        }
        extent = textExtent( font, text, wrapWidth, markupEnabled );
        width += extent.x;
        height = Math.max( height, extent.y );
      }
      if( height == 0 ) {
        height = getCharHeight( font );
      }
    }
    width += padding.left + padding.right;
    height += padding.top + padding.bottom;
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

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the control is selected, by sending
   * it one of the messages defined in the <code>SelectionListener</code>
   * interface.
   * <p>
   * <code>widgetSelected</code> is called when the control is selected.
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
    if( RWT.BADGE.equals( key ) && ( style & SWT.PUSH ) == 0 ) {
      return;
    } else if( RWT.MARKUP_ENABLED.equals( key ) && isMarkupEnabledFor( this ) ) {
      return;
    }
    checkMarkupPrecondition( key, TEXT, () -> text.isEmpty() );
    super.setData( key, value );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == WidgetLCA.class ) {
      return ( T )ButtonLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  void setDefault( boolean isDefault ) {
    this.isDefault = isDefault;
  }

  boolean getDefault() {
    return isDefault;
  }

  @Override
  boolean isTabGroup() {
    return true;
  }

  @Override
  String getNameText() {
    return getText();
  }

  private static int checkStyle( int style ) {
    int result = checkBits( style,
                            SWT.PUSH,
                            SWT.ARROW,
                            SWT.CHECK,
                            SWT.RADIO,
                            SWT.TOGGLE,
                            0 );
    if( ( result & ( SWT.PUSH | SWT.TOGGLE ) ) != 0 ) {
      result = checkBits( result, SWT.CENTER, SWT.LEFT, SWT.RIGHT, 0, 0, 0 );
    } else if( ( result & ( SWT.CHECK | SWT.RADIO ) ) != 0 ) {
      result = checkBits( result, SWT.LEFT, SWT.RIGHT, SWT.CENTER, 0, 0, 0 );
    } else if( ( result & SWT.ARROW ) != 0 ) {
      result |= SWT.NO_FOCUS;
      result = checkBits( result, SWT.UP, SWT.DOWN, SWT.LEFT, SWT.RIGHT, 0, 0 );
    }
    return result;
  }

  private ButtonThemeAdapter getThemeAdapter() {
    return ( ButtonThemeAdapter )getAdapter( ThemeAdapter.class );
  }

}
