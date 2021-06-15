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

import static org.eclipse.rap.rwt.internal.textsize.TextSizeUtil.textExtent;
import static org.eclipse.swt.internal.widgets.MarkupUtil.checkMarkupPrecondition;
import static org.eclipse.swt.internal.widgets.MarkupUtil.isMarkupEnabledFor;
import static org.eclipse.swt.internal.widgets.MarkupUtil.MarkupTarget.TEXT;
import static org.eclipse.swt.internal.widgets.MarkupValidator.isValidationDisabledFor;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.rap.rwt.internal.theme.ThemeAdapter;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.MarkupValidator;
import org.eclipse.swt.internal.widgets.labelkit.LabelLCA;
import org.eclipse.swt.internal.widgets.labelkit.LabelThemeAdapter;


/**
 * Instances of this class represent a non-selectable
 * user interface object that displays a string or image.
 * When SEPARATOR is specified, displays a single
 * vertical or horizontal line.
 * <p>
 * Shadow styles are hints and may not be honoured
 * by the platform.  To create a separator label
 * with the default shadow style for the platform,
 * do not specify a shadow style.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SEPARATOR, HORIZONTAL, VERTICAL</dd>
 * <dd>SHADOW_IN, SHADOW_OUT, SHADOW_NONE</dd>
 * <dd>CENTER, LEFT, RIGHT, WRAP</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * Note: Only one of SHADOW_IN, SHADOW_OUT and SHADOW_NONE may be specified.
 * SHADOW_NONE is a HINT. Only one of HORIZONTAL and VERTICAL may be specified.
 * Only one of CENTER, LEFT and RIGHT may be specified.
 * </p><p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the SWT implementation.
 * </p>
 *
 * <hr />
 * <strong>Note:</strong> Unlike in SWT, setting an image clears the text of the
 * label and vice versa. Thus, after calling <code>setText()</code>, the method
 * <code>getImage()</code> will return <code>null</code>, and after calling
 * <code>setImage()</code>, <code>getText</code> will return the empty string.
 * </p>
 * @since 1.0
 */
// TODO [rh] check what should happen with style == SEPARATOR and setForeground
public class Label extends Control {

  private String text = "";
  private Image image;

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
   * @see SWT#SEPARATOR
   * @see SWT#HORIZONTAL
   * @see SWT#VERTICAL
   * @see SWT#SHADOW_IN
   * @see SWT#SHADOW_OUT
   * @see SWT#SHADOW_NONE
   * @see SWT#CENTER
   * @see SWT#LEFT
   * @see SWT#RIGHT
   * @see SWT#WRAP
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public Label( Composite parent, int style ) {
    super( parent, checkStyle( style ) );
  }

  @Override
  void initState() {
    addState( THEME_BACKGROUND );
  }

  /**
   * Sets the receiver's text.
   * <p>
   * This method sets the widget label.  The label may include
   * the mnemonic character and line delimiters.
   * </p>
   * <p>
   * Mnemonics are indicated by an '&amp;' that causes the next
   * character to be the mnemonic.  When the user presses a
   * key sequence that matches the mnemonic, focus is assigned
   * to the control that follows the label. On most platforms,
   * the mnemonic appears underlined but may be emphasised in a
   * platform specific manner.  The mnemonic indicator character
   * '&amp;' can be escaped by doubling it in the string, causing
   * a single '&amp;' to be displayed.
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
    if( ( style & SWT.SEPARATOR ) == 0 ) {
      if( isMarkupEnabledFor( this ) && !isValidationDisabledFor( this ) ) {
        MarkupValidator.getInstance().validate( text );
      }
      this.text = text;
      image = null;
    }
  }

  /**
   * Returns the receiver's text, which will be an empty
   * string if it has never been set or if the receiver is
   * a <code>SEPARATOR</code> label.
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
    return text;
  }

  /**
   * Sets the receiver's image to the argument, which may be
   * null indicating that no image should be displayed.
   *
   * @param image the image to display on the receiver (may be null)
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  // TODO [rst] Clarify or remove this comment:
  // TODO: The LCA does not yet handle images. So, setting an image currently
  public void setImage( Image image ) {
    checkWidget();
    if( ( style & SWT.SEPARATOR ) == 0 ) {
      this.image = image;
      text = "";
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
   * Controls how text and images will be displayed in the receiver.
   * The argument should be one of <code>LEFT</code>, <code>RIGHT</code>
   * or <code>CENTER</code>.  If the receiver is a <code>SEPARATOR</code>
   * label, the argument is ignored and the alignment is not changed.
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
    if(    ( style & SWT.SEPARATOR ) == 0
        && ( alignment & ( SWT.LEFT | SWT.RIGHT | SWT.CENTER ) ) != 0 )
    {
      style &= ~( SWT.LEFT | SWT.RIGHT | SWT.CENTER );
      style |= alignment & ( SWT.LEFT | SWT.RIGHT | SWT.CENTER );
    }
  }

  /**
   * Returns a value which describes the position of the
   * text or image in the receiver. The value will be one of
   * <code>LEFT</code>, <code>RIGHT</code> or <code>CENTER</code>
   * unless the receiver is a <code>SEPARATOR</code> label, in
   * which case, <code>NONE</code> is returned.
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
    int result;
    if( ( style & SWT.SEPARATOR ) != 0 ) {
      result = 0;
    } else if( ( style & SWT.LEFT ) != 0 ) {
      result = SWT.LEFT;
    } else if( ( style & SWT.CENTER ) != 0 ) {
      result = SWT.CENTER;
    } else if( ( style & SWT.RIGHT ) != 0 ) {
      result = SWT.RIGHT;
    } else {
      result = SWT.LEFT;
    }
    return result;
  }

  @Override
  public Point computeSize( int wHint, int hHint, boolean changed ) {
    checkWidget();
    int width = 0;
    int height = 0;
    if( ( style & SWT.SEPARATOR ) != 0 ) {
      int lineWidth = getSeparatorLineWidth();
      if( ( style & SWT.HORIZONTAL ) != 0 ) {
        width = DEFAULT_WIDTH;
        height = lineWidth;
      } else {
        width = lineWidth;
        height = DEFAULT_HEIGHT;
      }
    } else if( image != null ) {
      Rectangle rect = image.getBounds();
      width = rect.width;
      height = rect.height;
    } else if( text.length() > 0 ) {
      int wrapWidth = 0;
      if( ( style & SWT.WRAP ) != 0 && wHint != SWT.DEFAULT ) {
        wrapWidth = wHint;
      }
      Point extent = textExtent( getFont(), text, wrapWidth, isMarkupEnabledFor( this ) );
      width = extent.x;
      height = extent.y + 2;
    } else {
      height = TextSizeUtil.getCharHeight( getFont() );
    }
    if( wHint != SWT.DEFAULT ) {
      width = wHint;
    }
    if( hHint != SWT.DEFAULT ) {
      height = hHint;
    }
    BoxDimensions border = getBorder();
    BoxDimensions padding = getPadding();
    width += border.left + border.right + padding.left + padding.right;
    height += border.top + border.bottom + padding.top + padding.bottom;
    return new Point( width, height );
  }

  @Override
  String getNameText() {
    return getText();
  }

  @Override
  public void setData( String key, Object value ) {
    if( !RWT.MARKUP_ENABLED.equals( key ) || !isMarkupEnabledFor( this ) ) {
      checkMarkupPrecondition( key, TEXT, () -> text.isEmpty() );
      super.setData( key, value );
    }
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == WidgetLCA.class ) {
      return ( T )LabelLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  private static int checkStyle( int style ) {
    int result = style;
    result |= SWT.NO_FOCUS;
    if( ( style & SWT.SEPARATOR ) != 0 ) {
      result = checkBits( result, SWT.VERTICAL, SWT.HORIZONTAL, 0, 0, 0, 0 );
      result = checkBits ( result,
                           SWT.SHADOW_OUT,
                           SWT.SHADOW_IN,
                           SWT.SHADOW_NONE,
                           0,
                           0,
                           0 );
    }
    result = checkBits( result, SWT.LEFT, SWT.CENTER, SWT.RIGHT, 0, 0, 0 );
    return result;
  }

  private int getSeparatorLineWidth() {
    LabelThemeAdapter themeAdapter = ( LabelThemeAdapter )getAdapter( ThemeAdapter.class );
    return themeAdapter.getSeparatorLineWidth( this );
  }

}
