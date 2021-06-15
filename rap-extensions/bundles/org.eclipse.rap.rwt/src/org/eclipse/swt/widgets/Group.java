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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.groupkit.GroupLCA;
import org.eclipse.swt.internal.widgets.groupkit.GroupThemeAdapter;


/**
 * Instances of this class provide an etched border
 * with an optional title.
 * <p>
 * Shadow styles are hints and may not be honoured
 * by the platform.  To create a group with the
 * default shadow style for the platform, do not
 * specify a shadow style.</p>
 * <p>The various SHADOW_* styles are not yet implemented.</p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SHADOW_ETCHED_IN, SHADOW_ETCHED_OUT, SHADOW_IN, SHADOW_OUT, SHADOW_NONE</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * Note: Only one of the above styles may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * <hr/>
 * Note: The styles <code>SHADOW_XXX</code> are not yet implemented in RWT.
 * @since 1.0
 */
public class Group extends Composite {

  private String text;

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
   * <p>The various SHADOW_* styles are not yet implemented.</p>
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
   * <!--@see SWT#SHADOW_ETCHED_IN-->
   * <!--@see SWT#SHADOW_ETCHED_OUT-->
   * @see SWT#SHADOW_IN
   * @see SWT#SHADOW_OUT
   * @see SWT#SHADOW_NONE
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  // TODO: [bm] implement shadow styles for Group
  public Group( Composite parent, int style ) {
    super( parent, checkStyle( style ) );
    text = "";
  }

  /**
   * Sets the receiver's text, which is the string that will
   * be displayed as the receiver's <em>title</em>, to the argument,
   * which may not be null. The string may include the mnemonic character.
   * </p>
   * Mnemonics are indicated by an '&amp;' that causes the next
   * character to be the mnemonic.  When the user presses a
   * key sequence that matches the mnemonic, focus is assigned
   * to the first child of the group. On most platforms, the
   * mnemonic appears underlined but may be emphasised in a
   * platform specific manner.  The mnemonic indicator character
   * '&amp;' can be escaped by doubling it in the string, causing
   * a single '&amp;' to be displayed.
   * </p>
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
    this.text = text;
  }

  /**
   * Returns the receiver's text, which is the string that the
   * is used as the <em>title</em>. If the text has not previously
   * been set, returns an empty string.
   *
   * @return the text
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

  @Override
  public Rectangle getClientArea() {
    checkWidget();
    Rectangle bounds = getBounds();
    BoxDimensions trimmings = getThemeAdapter().getTrimmingSize( this );
    int trimmingsWidth = trimmings.left + trimmings.right;
    int trimmingsHeight = trimmings.top + trimmings.bottom;
    BoxDimensions border = getBorder();
    int borderWidth = border.left + border.right;
    int borderHeight = border.top + border.bottom;
    int width = Math.max( 0, bounds.width - trimmingsWidth - borderWidth );
    int height = Math.max( 0, bounds.height - trimmingsHeight - borderHeight );
    return new Rectangle( trimmings.left, trimmings.top, width, height );
  }

  @Override
  public Rectangle computeTrim( int x, int y, int width, int height ) {
    BoxDimensions trimmings = getThemeAdapter().getTrimmingSize( this );
    int trimmingsWidth = trimmings.left + trimmings.right;
    int trimmingsHeight = trimmings.top + trimmings.bottom;
    BoxDimensions border = getBorder();
    int borderWidth = border.left + border.right;
    int borderHeight = border.top + border.bottom;
    return super.computeTrim( x - trimmings.left - border.left,
                              y - trimmings.top - border.top,
                              width + trimmingsWidth + borderWidth,
                              height + trimmingsHeight + borderHeight );
  }

  @Override
  public Point computeSize( int wHint, int hHint, boolean changed ) {
    checkWidget();
    Point result = super.computeSize( wHint, hHint, changed );
    int length = text.length();
    if( length != 0 ) {
      Font font = getFont();
      Point stringExtent = TextSizeUtil.stringExtent( font, text );
      BoxDimensions headTrimmings = getThemeAdapter().getHeaderTrimmingSize( this );
      int headerWidth = stringExtent.x + headTrimmings.left + headTrimmings.right;
      result.x = Math.max( result.x, headerWidth );
    }
    return result;
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == WidgetLCA.class ) {
      return ( T )GroupLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  private GroupThemeAdapter getThemeAdapter() {
    return ( GroupThemeAdapter )getAdapter( ThemeAdapter.class );
  }

  //////////////////
  // Helping methods

  @Override
  String getNameText() {
    return getText();
  }

  private static int checkStyle( int style ) {
    int result = style | SWT.NO_FOCUS;
    /*
     * Even though it is legal to create this widget with scroll bars, they
     * serve no useful purpose because they do not automatically scroll the
     * widget's client area. The fix is to clear the SWT style.
     */
    return result & ~( SWT.H_SCROLL | SWT.V_SCROLL );
  }
}
