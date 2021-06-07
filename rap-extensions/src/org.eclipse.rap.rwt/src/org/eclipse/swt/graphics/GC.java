/*******************************************************************************
 * Copyright (c) 2010, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.graphics;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Control;


/**
 * Class <code>GC</code> is where all of the drawing capabilities that are
 * supported by SWT are located. Instances are used to draw on a
 * <code>Control</code>.
 * <!--
 * Class <code>GC</code> is where all of the drawing capabilities that are
 * supported by SWT are located. Instances are used to draw on either an
 * <code>Image</code>, a <code>Control</code>, or directly on a <code>Display</code>.
 * -->
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>LEFT_TO_RIGHT <!--, RIGHT_TO_LEFT --></dd>
 * </dl>
 *
 * <p>
 * The SWT drawing coordinate system is the two-dimensional space with the origin
 * (0,0) at the top left corner of the drawing area and with (x,y) values increasing
 * to the right and downward respectively.
 * </p>
 *
 * <!--
 * <p>
 * The result of drawing on an image that was created with an indexed
 * palette using a color that is not in the palette is platform specific.
 * Some platforms will match to the nearest color while other will draw
 * the color itself. This happens because the allocated image might use
 * a direct palette on platforms that do not support indexed palette.
 * </p>
 * -->
 *
 * <p>
 * Application code must explicitly invoke the <code>GC.dispose()</code>
 * method to release the operating system resources managed by each instance
 * when those instances are no longer required. <!-- This is <em>particularly</em>
 * important on Windows95 and Windows98 where the operating system has a limited
 * number of device contexts available. -->
 * </p>
 *
 * <!--
 * <p>
 * Note: Only one of LEFT_TO_RIGHT and RIGHT_TO_LEFT may be specified.
 * </p>
 * -->
 *
 * @see org.eclipse.swt.events.PaintEvent
 * <!--
 * @see <a href="http://www.eclipse.org/swt/snippets/#gc">GC snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Examples: GraphicsExample, PaintExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * -->
 * @since 1.3
 */
public class GC extends Resource {

  private final GCDelegate delegate;
  private boolean advanced;
  private int antialias;
  private int textAntialias;

  /**
   * Constructs a new instance of this class which has been
   * configured to draw on the specified drawable. Sets the
   * foreground color, background color and font in the GC
   * to match those in the drawable.
   * <p>
   * You must dispose the graphics context when it is no longer required.
   * </p>
   * @param drawable the drawable to draw on
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the drawable is null</li>
   *    <li>ERROR_NULL_ARGUMENT - if there is no current device</li>
   *    <li>ERROR_INVALID_ARGUMENT
   *          - if the drawable is an image that is not a bitmap or an icon
   *          - if the drawable is an image or printer that is already selected
   *            into another graphics context</li>
   * </ul>
   * @exception SWTError <ul>
   *    <li>ERROR_NO_HANDLES if a handle could not be obtained for GC creation</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS if not called from the thread that created the drawable</li>
   * </ul>
   */
  public GC( Drawable drawable ) {
    this( drawable, SWT.NONE );
  }

  /**
   * Constructs a new instance of this class which has been
   * configured to draw on the specified drawable. Sets the
   * foreground color, background color and font in the GC
   * to match those in the drawable.
   * <p>
   * You must dispose the graphics context when it is no longer required.
   * </p>
   *
   * @param drawable the drawable to draw on
   * @param style the style of GC to construct
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the drawable is null</li>
   *    <li>ERROR_NULL_ARGUMENT - if there is no current device</li>
   *    <li>ERROR_INVALID_ARGUMENT
   *          - if the drawable is an image that is not a bitmap or an icon
   *          - if the drawable is an image or printer that is already selected
   *            into another graphics context</li>
   * </ul>
   * @exception SWTError <ul>
   *    <li>ERROR_NO_HANDLES if a handle could not be obtained for GC creation</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS if not called from the thread that created the drawable</li>
   * </ul>
   */
  public GC( Drawable drawable, int style ) {
    super( determineDevice( drawable ) );
    if( drawable == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    delegate = determineDelegate( drawable );
    antialias = SWT.DEFAULT;
    textAntialias = SWT.DEFAULT;
  }

  /**
   * Sets the font which will be used by the receiver
   * to draw and measure text to the argument. If the
   * argument is null, then a default font appropriate
   * for the platform will be used instead.
   *
   * @param font the new font for the receiver, or null to indicate a default font
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the font has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void setFont( Font font ) {
    checkDisposed();
    if( font != null && font.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    Font newFont = font != null ? font : delegate.getDefaultFont();
    delegate.setFont( newFont );
  }

  /**
   * Returns the font currently being used by the receiver
   * to draw and measure text.
   *
   * @return the receiver's font
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public Font getFont() {
    checkDisposed();
    return delegate.getFont();
  }

  /**
   * Returns the width of the specified character in the font
   * selected into the receiver.
   * <p>
   * The width is defined as the space taken up by the actual
   * character, not including the leading and tailing whitespace
   * or overhang.
   * </p>
   *
   * @param ch the character to measure
   * @return the width of the character
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public int getCharWidth( char ch ) {
    checkDisposed();
    return delegate.stringExtent( Character.toString( ch ) ).x;
  }

  /**
   * Returns the extent of the given string. No tab
   * expansion or carriage return processing will be performed.
   * <p>
   * The <em>extent</em> of a string is the width and height of
   * the rectangular area it would cover if drawn in a particular
   * font (in this case, the current font in the receiver).
   * </p>
   *
   * @param string the string to measure
   * @return a point containing the extent of the string
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public Point stringExtent( String string ) {
    checkDisposed();
    if( string == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    return delegate.stringExtent( string );
  }

  /**
   * Returns the extent of the given string. Tab expansion and
   * carriage return processing are performed.
   * <p>
   * The <em>extent</em> of a string is the width and height of
   * the rectangular area it would cover if drawn in a particular
   * font (in this case, the current font in the receiver).
   * </p>
   *
   * @param string the string to measure
   * @return a point containing the extent of the string
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public Point textExtent( String string ) {
    checkDisposed();
    if( string == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    return delegate.textExtent( string, 0 );
  }

  /**
   * Returns a FontMetrics which contains information
   * about the font currently being used by the receiver
   * to draw and measure text.
   *
   * @return font metrics for the receiver's font
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public FontMetrics getFontMetrics() {
    checkDisposed();
    return new FontMetrics( delegate.getFont() );
  }

  /**
   * Sets the background color. The background color is used
   * for fill operations and as the background color when text
   * is drawn.
   *
   * @param color the new background color for the receiver
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the color is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the color has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void setBackground( Color color ) {
    checkDisposed();
    if( color == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( color.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    delegate.setBackground( color );
  }

  /**
   * Returns the background color.
   *
   * @return the receiver's background color
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public Color getBackground() {
    checkDisposed();
    return delegate.getBackground();
  }

  /**
   * Sets the foreground color. The foreground color is used
   * for drawing operations including when text is drawn.
   *
   * @param color the new foreground color for the receiver
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the color is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the color has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void setForeground( Color color ) {
    checkDisposed();
    if( color == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( color.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    delegate.setForeground( color );
  }

  /**
   * Returns the receiver's foreground color.
   *
   * @return the color used for drawing foreground things
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public Color getForeground() {
    checkDisposed();
    return delegate.getForeground();
  }

  /**
   * Sets the area of the receiver which can be changed
   * by drawing operations to the rectangular area specified
   * by the argument.  Specifying <code>null</code> for the
   * rectangle reverts the receiver's clipping area to its
   * original value.
   *
   * @param rect the clipping rectangle or <code>null</code>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   * @since 3.0
   */
  public void setClipping( Rectangle rect ) {
    checkDisposed();
    if( rect == null ) {
      delegate.setClipping( ( Rectangle )null );
    } else {
      setClipping( rect.x, rect.y, rect.width, rect.height );
    }
  }

  /**
   * Sets the area of the receiver which can be changed
   * by drawing operations to the rectangular area specified
   * by the arguments.
   *
   * @param x the x coordinate of the clipping rectangle
   * @param y the y coordinate of the clipping rectangle
   * @param width the width of the clipping rectangle
   * @param height the height of the clipping rectangle
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   * @since 3.0
   */
  public void setClipping( int x, int y, int width, int height ) {
    checkDisposed();
    delegate.setClipping( new Rectangle( x, y, width, height ) );
  }

  /**
   * Sets the area of the receiver which can be changed
   * by drawing operations to the path specified
   * by the argument.
   * <p>
   * This operation requires the operating system's advanced
   * graphics subsystem which may not be available on some
   * platforms.
   * </p>
   *
   * @param path the clipping path.
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the path has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
   * </ul>
   *
   * @see Path
   * @see #getAdvanced
   * @see #setAdvanced
   *
   * @since 3.0
   */
  public void setClipping( Path path ) {
    checkDisposed();
    if( path != null && path.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    delegate.setClipping( path );
  }

  /**
   * Returns the bounding rectangle of the receiver's clipping
   * region. If no clipping region is set, the return value
   * will be a rectangle which covers the entire bounds of the
   * object the receiver is drawing on.
   *
   * @return the bounding rectangle of the clipping region
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public Rectangle getClipping() {
    checkDisposed();
    return delegate.getClipping();
  }

  /**
   * Sets the receiver's alpha value which must be
   * between 0 (transparent) and 255 (opaque).
   * <p>
   * This operation requires the operating system's advanced
   * graphics subsystem which may not be available on some
   * platforms.
   * </p>
   * @param alpha the alpha value
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
   * </ul>
   */
  public void setAlpha( int alpha ) {
    checkDisposed();
    if( alpha >= 0 && alpha <= 255 && delegate.getAlpha() != alpha ) {
      delegate.setAlpha( alpha );
      advanced = true;
    }
  }

  /**
   * Returns the receiver's alpha value. The alpha value
   * is between 0 (transparent) and 255 (opaque).
   *
   * @return the alpha value
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public int getAlpha() {
    checkDisposed();
    return delegate.getAlpha();
  }

  /**
   * Sets the width that will be used when drawing lines
   * for all of the figure drawing operations (that is,
   * <code>drawLine</code>, <code>drawRectangle</code>,
   * <code>drawPolyline</code>, and so forth.
   *
   * @param lineWidth the width of a line
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void setLineWidth( int lineWidth ) {
    checkDisposed();
    if( delegate.getLineWidth() != lineWidth ) {
      delegate.setLineWidth( lineWidth );
    }
  }

  /**
   * Returns the width that will be used when drawing lines
   * for all of the figure drawing operations (that is,
   * <code>drawLine</code>, <code>drawRectangle</code>,
   * <code>drawPolyline</code>, and so forth.
   *
   * @return the receiver's line width
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public int getLineWidth() {
    checkDisposed();
    return delegate.getLineWidth();
  }

  /**
   * Sets the receiver's line cap style to the argument, which must be one
   * of the constants <code>SWT.CAP_FLAT</code>, <code>SWT.CAP_ROUND</code>,
   * or <code>SWT.CAP_SQUARE</code>.
   *
   * @param lineCap the cap style to be used for drawing lines
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the style is not valid</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void setLineCap( int lineCap ) {
    checkDisposed();
    if( delegate.getLineCap() != lineCap ) {
      switch( lineCap ) {
        case SWT.CAP_ROUND:
        case SWT.CAP_FLAT:
        case SWT.CAP_SQUARE:
          break;
        default:
          SWT.error( SWT.ERROR_INVALID_ARGUMENT );
      }
      delegate.setLineCap( lineCap );
    }
  }

  /**
   * Returns the receiver's line cap style, which will be one
   * of the constants <code>SWT.CAP_FLAT</code>, <code>SWT.CAP_ROUND</code>,
   * or <code>SWT.CAP_SQUARE</code>.
   *
   * @return the cap style used for drawing lines
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public int getLineCap() {
    checkDisposed();
    return delegate.getLineCap();
  }

  /**
   * Sets the receiver's line join style to the argument, which must be one
   * of the constants <code>SWT.JOIN_MITER</code>, <code>SWT.JOIN_ROUND</code>,
   * or <code>SWT.JOIN_BEVEL</code>.
   *
   * @param lineJoin the join style to be used for drawing lines
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the style is not valid</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void setLineJoin( int lineJoin ) {
    checkDisposed();
    if( delegate.getLineJoin() != lineJoin ) {
      switch( lineJoin ) {
        case SWT.JOIN_MITER:
        case SWT.JOIN_ROUND:
        case SWT.JOIN_BEVEL:
          break;
        default:
          SWT.error( SWT.ERROR_INVALID_ARGUMENT );
      }
      delegate.setLineJoin( lineJoin );
    }
  }

  /**
   * Returns the receiver's line join style, which will be one
   * of the constants <code>SWT.JOIN_MITER</code>, <code>SWT.JOIN_ROUND</code>,
   * or <code>SWT.JOIN_BEVEL</code>.
   *
   * @return the join style used for drawing lines
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public int getLineJoin() {
    checkDisposed();
    return delegate.getLineJoin();
  }

  /**
   * Sets the receiver's line attributes.
   * <p>
   * This operation requires the operating system's advanced
   * graphics subsystem which may not be available on some
   * platforms.
   * </p>
   * @param attributes the line attributes
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the attributes is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if any of the line attributes is not valid</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
   * </ul>
   *
   * @see LineAttributes
   */
  public void setLineAttributes( LineAttributes attributes ) {
    checkDisposed();
    if( attributes == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    setLineWidth( ( int )attributes.width );
    setLineCap( attributes.cap );
    setLineJoin( attributes.join );
    advanced = true;
  }

  /**
   * Returns the receiver's line attributes.
   *
   * @return the line attributes used for drawing lines
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public LineAttributes getLineAttributes() {
    checkDisposed();
    int lineWidth = delegate.getLineWidth();
    int lineCap = delegate.getLineCap();
    int lineJoin = delegate.getLineJoin();
    return new LineAttributes( lineWidth, lineCap, lineJoin );
  }

  /**
   * Sets the receiver to always use the operating system's advanced graphics
   * subsystem for all graphics operations if the argument is <code>true</code>.
   * If the argument is <code>false</code>, the advanced graphics subsystem is
   * no longer used, advanced graphics state is cleared and the normal graphics
   * subsystem is used from now on.
   * <p>
   * Normally, the advanced graphics subsystem is invoked automatically when
   * any one of the alpha, antialias, patterns, interpolation, paths, clipping
   * or transformation operations in the receiver is requested.  When the receiver
   * is switched into advanced mode, the advanced graphics subsystem performs both
   * advanced and normal graphics operations.  Because the two subsystems are
   * different, their output may differ.  Switching to advanced graphics before
   * any graphics operations are performed ensures that the output is consistent.
   * </p><p>
   * Advanced graphics may not be installed for the operating system.  In this
   * case, this operation does nothing.  Some operating system have only one
   * graphics subsystem, so switching from normal to advanced graphics does
   * nothing.  However, switching from advanced to normal graphics will always
   * clear the advanced graphics state, even for operating systems that have
   * only one graphics subsystem.
   * </p>
   *
   * @param advanced the new advanced graphics state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see #setAlpha
   * @see #setAntialias
   * @see #setClipping(Path)
   * @see #setLineAttributes
   * @see #setTextAntialias
   * @see #setTransform
   * @see #getAdvanced
   *
   * @since 1.4
   */
  //  * @see #setInterpolation
  //  * @see #setForegroundPattern
  //  * @see #setBackgroundPattern
  public void setAdvanced( boolean advanced ) {
    checkDisposed();
    this.advanced = advanced;
    if( !advanced ) {
      delegate.setAlpha( 255 );
      antialias = SWT.DEFAULT;
      textAntialias = SWT.DEFAULT;
    }
  }

  /**
   * Sets the receiver's anti-aliasing value to the parameter,
   * which must be one of <code>SWT.DEFAULT</code>, <code>SWT.OFF</code>
   * or <code>SWT.ON</code>. Note that this controls anti-aliasing for all
   * <em>non-text drawing</em> operations.
   * <p>
   * This operation requires the operating system's advanced
   * graphics subsystem which may not be available on some
   * platforms.
   * </p>
   *
   * @param antialias the anti-aliasing setting
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the parameter is not one of <code>SWT.DEFAULT</code>,
   *                                 <code>SWT.OFF</code> or <code>SWT.ON</code></li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
   * </ul>
   *
   * @see #getAdvanced
   * @see #setAdvanced
   * @see #setTextAntialias
   *
   * @since 1.5
   */
  public void setAntialias( int antialias ) {
    if( antialias != SWT.DEFAULT && antialias != SWT.ON && antialias != SWT.OFF ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    this.antialias = antialias;
    advanced = true;
  }

  /**
   * Returns the receiver's anti-aliasing setting value, which will be
   * one of <code>SWT.DEFAULT</code>, <code>SWT.OFF</code> or
   * <code>SWT.ON</code>. Note that this controls anti-aliasing for all
   * <em>non-text drawing</em> operations.
   *
   * @return the anti-aliasing setting
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see #getTextAntialias
   *
   * @since 1.5
   */
  public int getAntialias() {
    return antialias;
  }

  /**
   * Sets the receiver's text anti-aliasing value to the parameter,
   * which must be one of <code>SWT.DEFAULT</code>, <code>SWT.OFF</code>
   * or <code>SWT.ON</code>. Note that this controls anti-aliasing only
   * for all <em>text drawing</em> operations.
   * <p>
   * This operation requires the operating system's advanced
   * graphics subsystem which may not be available on some
   * platforms.
   * </p>
   *
   * @param antialias the anti-aliasing setting
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the parameter is not one of <code>SWT.DEFAULT</code>,
   *                                 <code>SWT.OFF</code> or <code>SWT.ON</code></li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
   * </ul>
   *
   * @see #getAdvanced
   * @see #setAdvanced
   * @see #setAntialias
   *
   * @since 1.5
   */
  public void setTextAntialias( int antialias ) {
    if( antialias != SWT.DEFAULT && antialias != SWT.ON && antialias != SWT.OFF ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    textAntialias = antialias;
    advanced = true;
  }

  /**
   * Returns the receiver's text drawing anti-aliasing setting value,
   * which will be one of <code>SWT.DEFAULT</code>, <code>SWT.OFF</code> or
   * <code>SWT.ON</code>. Note that this controls anti-aliasing
   * <em>only</em> for text drawing operations.
   *
   * @return the anti-aliasing setting
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see #getAntialias
   *
   * @since 1.5
   */
  public int getTextAntialias() {
    return textAntialias;
  }

  /**
   * Returns <code>true</code> if receiver is using the operating system's
   * advanced graphics subsystem.  Otherwise, <code>false</code> is returned
   * to indicate that normal graphics are in use.
   * <p>
   * Advanced graphics may not be installed for the operating system.  In this
   * case, <code>false</code> is always returned.  Some operating system have
   * only one graphics subsystem.  If this subsystem supports advanced graphics,
   * then <code>true</code> is always returned.  If any graphics operation such
   * as alpha, antialias, patterns, interpolation, paths, clipping or transformation
   * has caused the receiver to switch from regular to advanced graphics mode,
   * <code>true</code> is returned.  If the receiver has been explicitly switched
   * to advanced mode and this mode is supported, <code>true</code> is returned.
   * </p>
   *
   * @return the advanced value
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see #setAdvanced
   *
   * @since 1.4
   */
  public boolean getAdvanced() {
    checkDisposed();
    return advanced;
  }

  /**
   * Sets the transform that is currently being used by the receiver. If
   * the argument is <code>null</code>, the current transform is set to
   * the identity transform.
   * <p>
   * This operation requires the operating system's advanced
   * graphics subsystem which may not be available on some
   * platforms.
   * </p>
   *
   * @param transform the transform to set
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the parameter has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
   * </ul>
   *
   * @see Transform
   * @see #getAdvanced
   * @see #setAdvanced
   *
   * @since 3.1
   */
  public void setTransform( Transform transform ) {
    checkDisposed();
    if( transform != null && transform.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    float[] elements = new float[] { 1, 0, 0, 1, 0, 0 };
    if( transform == null ) {
      delegate.setTransform( elements );
    } else {
      transform.getElements( elements );
      delegate.setTransform( elements );
    }
  }

  /**
   * Sets the parameter to the transform that is currently being
   * used by the receiver.
   *
   * @param transform the destination to copy the transform into
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the parameter is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the parameter has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see Transform
   *
   * @since 3.1
   */
  public void getTransform( Transform transform ) {
    checkDisposed();
    if( transform == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( transform.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    float[] elements = delegate.getTransform();
    transform.setElements( elements[ 0 ],
      elements[ 1 ],
      elements[ 2 ],
      elements[ 3 ],
      elements[ 4 ],
      elements[ 5 ] );
  }

  /**
   * Draws a line, using the foreground color, between the points
   * (<code>x1</code>, <code>y1</code>) and (<code>x2</code>, <code>y2</code>).
   *
   * @param x1 the first point's x coordinate
   * @param y1 the first point's y coordinate
   * @param x2 the second point's x coordinate
   * @param y2 the second point's y coordinate
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void drawLine( int x1, int y1, int x2, int y2 ) {
    checkDisposed();
    delegate.drawLine( x1, y1, x2, y2 );
  }

  /**
   * Draws the outline of the specified rectangle, using the receiver's
   * foreground color. The left and right edges of the rectangle are at
   * <code>rect.x</code> and <code>rect.x + rect.width</code>. The top
   * and bottom edges are at <code>rect.y</code> and
   * <code>rect.y + rect.height</code>.
   *
   * @param rect the rectangle to draw
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the rectangle is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void drawRectangle( Rectangle rect ) {
    if( rect == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    drawRectangle( rect.x, rect.y, rect.width, rect.height );
  }

  /**
   * Draws the outline of the rectangle specified by the arguments,
   * using the receiver's foreground color. The left and right edges
   * of the rectangle are at <code>x</code> and <code>x + width</code>.
   * The top and bottom edges are at <code>y</code> and <code>y + height</code>.
   *
   * @param x the x coordinate of the rectangle to be drawn
   * @param y the y coordinate of the rectangle to be drawn
   * @param width the width of the rectangle to be drawn
   * @param height the height of the rectangle to be drawn
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void drawRectangle( int x, int y, int width, int height ) {
    checkDisposed();
    drawRectangle( x, y, width, height, 0, 0, false );
  }

  /**
   * Draws a rectangle, based on the specified arguments, which has
   * the appearance of the platform's <em>focus rectangle</em> if the
   * platform supports such a notion, and otherwise draws a simple
   * rectangle in the receiver's foreground color.
   *
   * @param x the x coordinate of the rectangle
   * @param y the y coordinate of the rectangle
   * @param width the width of the rectangle
   * @param height the height of the rectangle
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see #drawRectangle(int, int, int, int)
   */
  public void drawFocus( int x, int y, int width, int height ) {
    drawRectangle( x, y, width, height );
  }

  /**
   * Fills the interior of the specified rectangle, using the receiver's
   * background color.
   *
   * @param rect the rectangle to be filled
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the rectangle is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see #drawRectangle(int, int, int, int)
   */
  public void fillRectangle( Rectangle rect ) {
    if( rect == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    fillRectangle( rect.x, rect.y, rect.width, rect.height );
  }

  /**
   * Fills the interior of the rectangle specified by the arguments,
   * using the receiver's background color.
   *
   * @param x the x coordinate of the rectangle to be filled
   * @param y the y coordinate of the rectangle to be filled
   * @param width the width of the rectangle to be filled
   * @param height the height of the rectangle to be filled
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see #drawRectangle(int, int, int, int)
   */
  public void fillRectangle( int x, int y, int width, int height ) {
    checkDisposed();
    drawRectangle( x, y, width, height, 0, 0, true );
  }

  /**
   * Fills the interior of the specified rectangle with a gradient
   * sweeping from left to right or top to bottom progressing
   * from the receiver's foreground color to its background color.
   *
   * @param x the x coordinate of the rectangle to be filled
   * @param y the y coordinate of the rectangle to be filled
   * @param width the width of the rectangle to be filled, may be negative
   *        (inverts direction of gradient if horizontal)
   * @param height the height of the rectangle to be filled, may be negative
   *        (inverts direction of gradient if vertical)
   * @param vertical if true sweeps from top to bottom, else
   *        sweeps from left to right
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see #drawRectangle(int, int, int, int)
   */
  public void fillGradientRectangle( int x, int y, int width, int height, boolean vertical ) {
    checkDisposed();
    if( width != 0 && height != 0 ) {
      if( delegate.getBackground().equals( delegate.getForeground() ) ) {
        fillRectangle( x, y, width, height );
      } else {
        fillGradientRect( x, y, width, height, vertical );
      }
    }
  }

  /**
   * Draws the outline of the round-cornered rectangle specified by
   * the arguments, using the receiver's foreground color. The left and
   * right edges of the rectangle are at <code>x</code> and <code>x + width</code>.
   * The top and bottom edges are at <code>y</code> and <code>y + height</code>.
   * The <em>roundness</em> of the corners is specified by the
   * <code>arcWidth</code> and <code>arcHeight</code> arguments, which
   * are respectively the width and height of the ellipse used to draw
   * the corners.
   *
   * @param x the x coordinate of the rectangle to be drawn
   * @param y the y coordinate of the rectangle to be drawn
   * @param width the width of the rectangle to be drawn
   * @param height the height of the rectangle to be drawn
   * @param arcWidth the width of the arc
   * @param arcHeight the height of the arc
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void drawRoundRectangle( int x, int y, int width, int height, int arcWidth, int arcHeight )
  {
    checkDisposed();
    drawRectangle( x, y, width, height, arcWidth, arcHeight, false );
  }

  /**
   * Fills the interior of the round-cornered rectangle specified by
   * the arguments, using the receiver's background color.
   *
   * @param x the x coordinate of the rectangle to be filled
   * @param y the y coordinate of the rectangle to be filled
   * @param width the width of the rectangle to be filled
   * @param height the height of the rectangle to be filled
   * @param arcWidth the width of the arc
   * @param arcHeight the height of the arc
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see #drawRoundRectangle
   */
  public void fillRoundRectangle( int x, int y, int width, int height, int arcWidth, int arcHeight )
  {
    checkDisposed();
    drawRectangle( x, y, width, height, arcWidth, arcHeight, true );
  }

  /**
   * Draws the outline of an oval, using the foreground color,
   * within the specified rectangular area.
   * <p>
   * The result is a circle or ellipse that fits within the
   * rectangle specified by the <code>x</code>, <code>y</code>,
   * <code>width</code>, and <code>height</code> arguments.
   * </p><p>
   * The oval covers an area that is <code>width + 1</code>
   * pixels wide and <code>height + 1</code> pixels tall.
   * </p>
   *
   * @param x the x coordinate of the upper left corner of the oval to be drawn
   * @param y the y coordinate of the upper left corner of the oval to be drawn
   * @param width the width of the oval to be drawn
   * @param height the height of the oval to be drawn
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void drawOval( int x, int y, int width, int height ) {
    checkDisposed();
    drawArc( x, y, width, height, 0, 360, false );
  }

  /**
   * Fills the interior of an oval, within the specified
   * rectangular area, with the receiver's background
   * color.
   *
   * @param x the x coordinate of the upper left corner of the oval to be filled
   * @param y the y coordinate of the upper left corner of the oval to be filled
   * @param width the width of the oval to be filled
   * @param height the height of the oval to be filled
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see #drawOval
   */
  public void fillOval( int x, int y, int width, int height ) {
    checkDisposed();
    drawArc( x, y, width, height, 0, 360, true );
  }

  /**
   * Draws the outline of a circular or elliptical arc
   * within the specified rectangular area.
   * <p>
   * The resulting arc begins at <code>startAngle</code> and extends
   * for <code>arcAngle</code> degrees, using the current color.
   * Angles are interpreted such that 0 degrees is at the 3 o'clock
   * position. A positive value indicates a counter-clockwise rotation
   * while a negative value indicates a clockwise rotation.
   * </p><p>
   * The center of the arc is the center of the rectangle whose origin
   * is (<code>x</code>, <code>y</code>) and whose size is specified by the
   * <code>width</code> and <code>height</code> arguments.
   * </p><p>
   * The resulting arc covers an area <code>width + 1</code> pixels wide
   * by <code>height + 1</code> pixels tall.
   * </p>
   *
   * @param x the x coordinate of the upper-left corner of the arc to be drawn
   * @param y the y coordinate of the upper-left corner of the arc to be drawn
   * @param width the width of the arc to be drawn
   * @param height the height of the arc to be drawn
   * @param startAngle the beginning angle
   * @param arcAngle the angular extent of the arc, relative to the start angle
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void drawArc( int x, int y, int width, int height, int startAngle, int arcAngle ) {
    checkDisposed();
    drawArc( x, y, width, height, startAngle, arcAngle, false );
  }

  /**
   * Fills the interior of a circular or elliptical arc within
   * the specified rectangular area, with the receiver's background
   * color.
   * <p>
   * The resulting arc begins at <code>startAngle</code> and extends
   * for <code>arcAngle</code> degrees, using the current color.
   * Angles are interpreted such that 0 degrees is at the 3 o'clock
   * position. A positive value indicates a counter-clockwise rotation
   * while a negative value indicates a clockwise rotation.
   * </p><p>
   * The center of the arc is the center of the rectangle whose origin
   * is (<code>x</code>, <code>y</code>) and whose size is specified by the
   * <code>width</code> and <code>height</code> arguments.
   * </p><p>
   * The resulting arc covers an area <code>width + 1</code> pixels wide
   * by <code>height + 1</code> pixels tall.
   * </p>
   *
   * @param x the x coordinate of the upper-left corner of the arc to be filled
   * @param y the y coordinate of the upper-left corner of the arc to be filled
   * @param width the width of the arc to be filled
   * @param height the height of the arc to be filled
   * @param startAngle the beginning angle
   * @param arcAngle the angular extent of the arc, relative to the start angle
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see #drawArc
   */
  public void fillArc( int x, int y, int width, int height, int startAngle, int arcAngle ) {
    checkDisposed();
    drawArc( x, y, width, height, startAngle, arcAngle, true );
  }

  /**
   * Draws the closed polygon which is defined by the specified array
   * of integer coordinates, using the receiver's foreground color. The array
   * contains alternating x and y values which are considered to represent
   * points which are the vertices of the polygon. Lines are drawn between
   * each consecutive pair, and between the first pair and last pair in the
   * array.
   *
   * @param pointArray an array of alternating x and y values which are the vertices of the polygon
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT if pointArray is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void drawPolygon( int[] pointArray ) {
    checkDisposed();
    if( pointArray == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    delegate.drawPolyline( pointArray, true, false );
  }

  /**
   * Fills the interior of the closed polygon which is defined by the
   * specified array of integer coordinates, using the receiver's
   * background color. The array contains alternating x and y values
   * which are considered to represent points which are the vertices of
   * the polygon. Lines are drawn between each consecutive pair, and
   * between the first pair and last pair in the array.
   *
   * @param pointArray an array of alternating x and y values which are the vertices of the polygon
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT if pointArray is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see #drawPolygon
   */
  public void fillPolygon( int[] pointArray ) {
    checkDisposed();
    if( pointArray == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    delegate.drawPolyline( pointArray, true, true );
  }

  /**
   * Draws the polyline which is defined by the specified array
   * of integer coordinates, using the receiver's foreground color. The array
   * contains alternating x and y values which are considered to represent
   * points which are the corners of the polyline. Lines are drawn between
   * each consecutive pair, but not between the first pair and last pair in
   * the array.
   *
   * @param pointArray an array of alternating x and y values which are the corners of the polyline
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the point array is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void drawPolyline( int[] pointArray ) {
    checkDisposed();
    if( pointArray == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    delegate.drawPolyline( pointArray, false, false );
  }

  /**
   * Draws a pixel, using the foreground color, at the specified
   * point (<code>x</code>, <code>y</code>).
   * <p>
   * Note that the receiver's line attributes do not affect this
   * operation.
   * </p>
   *
   * @param x the point's x coordinate
   * @param y the point's y coordinate
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void drawPoint( int x, int y ) {
    checkDisposed();
    delegate.drawPoint( x, y );
  }

  /**
   * Draws the given image in the receiver at the specified
   * coordinates.
   *
   * @param image the image to draw
   * @param x the x coordinate of where to draw
   * @param y the y coordinate of where to draw
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the image is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the given coordinates are outside the bounds of the image</li>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   * @exception SWTError <ul>
   *    <li>ERROR_NO_HANDLES - if no handles are available to perform the operation</li>
   * </ul>
   */
  public void drawImage( Image image, int x, int y) {
    checkDisposed();
    if( image == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( image.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    Rectangle src = new Rectangle( 0, 0, -1, -1 );
    Rectangle dest = new Rectangle( x, y, -1, -1 );
    delegate.drawImage( image, src, dest, true );
  }

  /**
   * Copies a rectangular area from the source image into a (potentially
   * different sized) rectangular area in the receiver. If the source
   * and destination areas are of differing sizes, then the source
   * area will be stretched or shrunk to fit the destination area
   * as it is copied. The copy fails if any part of the source rectangle
   * lies outside the bounds of the source image, or if any of the width
   * or height arguments are negative.
   *
   * @param image the source image
   * @param srcX the x coordinate in the source image to copy from
   * @param srcY the y coordinate in the source image to copy from
   * @param srcWidth the width in pixels to copy from the source
   * @param srcHeight the height in pixels to copy from the source
   * @param destX the x coordinate in the destination to copy to
   * @param destY the y coordinate in the destination to copy to
   * @param destWidth the width in pixels of the destination rectangle
   * @param destHeight the height in pixels of the destination rectangle
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the image is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
   *    <li>ERROR_INVALID_ARGUMENT - if any of the width or height arguments are negative.
   *    <li>ERROR_INVALID_ARGUMENT - if the source rectangle is not contained within the bounds of the source image</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   * @exception SWTError <ul>
   *    <li>ERROR_NO_HANDLES - if no handles are available to perform the operation</li>
   * </ul>
   */
  public void drawImage( Image image,
                         int srcX,
                         int srcY,
                         int srcWidth,
                         int srcHeight,
                         int destX,
                         int destY,
                         int destWidth,
                         int destHeight )
  {
    checkDisposed();
    if( srcWidth != 0 && srcHeight != 0 && destWidth != 0 && destHeight != 0 ) {
      if(    srcX < 0
        || srcY < 0
        || srcWidth < 0
        || srcHeight < 0
        || destWidth < 0
        || destHeight < 0 )
      {
        SWT.error( SWT.ERROR_INVALID_ARGUMENT );
      }
      if( image == null ) {
        SWT.error( SWT.ERROR_NULL_ARGUMENT );
      }
      if( image.isDisposed() ) {
        SWT.error( SWT.ERROR_INVALID_ARGUMENT );
      }
      int imgWidth = image.getBounds().width;
      int imgHeight = image.getBounds().height;
      if( srcX + srcWidth > imgWidth || srcY + srcHeight > imgHeight ) {
        SWT.error( SWT.ERROR_INVALID_ARGUMENT );
      }
      Rectangle src = new Rectangle( srcX, srcY, srcWidth, srcHeight );
      Rectangle dest = new Rectangle( destX, destY, destWidth, destHeight );
      delegate.drawImage( image, src, dest, false );
    }
  }

  /**
   * Draws the given string, using the receiver's current font and
   * foreground color. No tab expansion or carriage return processing
   * will be performed. The background of the rectangular area where
   * the string is being drawn will be filled with the receiver's
   * background color.
   *
   * @param string the string to be drawn
   * @param x the x coordinate of the top left corner of the rectangular area where the string is to be drawn
   * @param y the y coordinate of the top left corner of the rectangular area where the string is to be drawn
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void drawString( String string, int x, int y ) {
    drawString( string, x, y, false );
  }

  /**
   * Draws the given string, using the receiver's current font and
   * foreground color. No tab expansion or carriage return processing
   * will be performed. If <code>isTransparent</code> is <code>true</code>,
   * then the background of the rectangular area where the string is being
   * drawn will not be modified, otherwise it will be filled with the
   * receiver's background color.
   *
   * @param string the string to be drawn
   * @param x the x coordinate of the top left corner of the rectangular area where the string is to be drawn
   * @param y the y coordinate of the top left corner of the rectangular area where the string is to be drawn
   * @param isTransparent if <code>true</code> the background will be transparent, otherwise it will be opaque
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void drawString( String string, int x, int y, boolean isTransparent ) {
    int flags = isTransparent ? SWT.DRAW_TRANSPARENT : SWT.NONE;
    drawText( string, x, y, flags );
  }

  /**
   * Draws the given string, using the receiver's current font and
   * foreground color. Tab expansion and carriage return processing
   * are performed. The background of the rectangular area where
   * the text is being drawn will be filled with the receiver's
   * background color.
   *
   * @param string the string to be drawn
   * @param x the x coordinate of the top left corner of the rectangular area where the text is to be drawn
   * @param y the y coordinate of the top left corner of the rectangular area where the text is to be drawn
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void drawText( String string, int x, int y ) {
    drawText( string, x, y, SWT.DRAW_DELIMITER | SWT.DRAW_TAB );
  }

  /**
   * Draws the given string, using the receiver's current font and
   * foreground color. Tab expansion and carriage return processing
   * are performed. If <code>isTransparent</code> is <code>true</code>,
   * then the background of the rectangular area where the text is being
   * drawn will not be modified, otherwise it will be filled with the
   * receiver's background color.
   *
   * @param string the string to be drawn
   * @param x the x coordinate of the top left corner of the rectangular area where the text is to be drawn
   * @param y the y coordinate of the top left corner of the rectangular area where the text is to be drawn
   * @param isTransparent if <code>true</code> the background will be transparent, otherwise it will be opaque
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void drawText( String string, int x, int y, boolean isTransparent ) {
    int flags = SWT.DRAW_DELIMITER | SWT.DRAW_TAB;
    if( isTransparent ) {
      flags |= SWT.DRAW_TRANSPARENT;
    }
    drawText( string, x, y, flags );
  }

  /**
   * Draws the given string, using the receiver's current font and
   * foreground color. Tab expansion, line delimiter and mnemonic
   * processing are performed according to the specified flags. If
   * <code>flags</code> includes <code>DRAW_TRANSPARENT</code>,
   * then the background of the rectangular area where the text is being
   * drawn will not be modified, otherwise it will be filled with the
   * receiver's background color.
   * <p>
   * The parameter <code>flags</code> may be a combination of:
   * <dl>
   * <dt><b>DRAW_DELIMITER</b></dt>
   * <dd>draw multiple lines</dd>
   * <dt><b>DRAW_TAB</b></dt>
   * <dd>expand tabs</dd>
   * <dt><b>DRAW_MNEMONIC</b></dt>
   * <dd>underline the mnemonic character</dd>
   * <dt><b>DRAW_TRANSPARENT</b></dt>
   * <dd>transparent background</dd>
   * </dl>
   * </p>
   *
   * @param string the string to be drawn
   * @param x the x coordinate of the top left corner of the rectangular area where the text is to be drawn
   * @param y the y coordinate of the top left corner of the rectangular area where the text is to be drawn
   * @param flags the flags specifying how to process the text
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void drawText( String string, int x, int y, int flags ) {
    checkDisposed();
    if( string == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( string.length() != 0 ) {
      delegate.drawText( string, x, y, flags );
    }
  }

  /**
   * Draws the path described by the parameter.
   * <p>
   * This operation requires the operating system's advanced
   * graphics subsystem which may not be available on some
   * platforms.
   * </p>
   *
   * @param path the path to draw
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the parameter is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the parameter has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
   * </ul>
   *
   * @see Path
   *
   * @since 2.1
   */
  public void drawPath( Path path ) {
    checkDisposed();
    if( path == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( path.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    delegate.drawPath( path, false );
  }

  /**
   * Fills the path described by the parameter.
   * <p>
   * This operation requires the operating system's advanced
   * graphics subsystem which may not be available on some
   * platforms.
   * </p>
   *
   * @param path the path to fill
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the parameter is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the parameter has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
   * </ul>
   *
   * @see Path
   *
   * @since 2.1
   */
  public void fillPath( Path path ) {
    checkDisposed();
    if( path == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( path.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    delegate.drawPath( path, true );
  }

  /**
   * Returns the receiver's style information.
   * <p>
   * Note that the value which is returned by this method <em>may
   * not match</em> the value which was provided to the constructor
   * when the receiver was created. This can occur when the underlying
   * operating system does not support a particular combination of
   * requested styles.
   * </p>
   *
   * @return the style bits
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public int getStyle() {
    checkDisposed();
    return SWT.LEFT_TO_RIGHT;
  }

  GCDelegate getGCDelegate() {
    return delegate;
  }

  static Rectangle checkBounds( int x, int y, int width, int height ) {
    Rectangle result = new Rectangle( x, y, width, height );
    if( width < 0 ) {
      result.x = x + width;
      result.width = -width;
    }
    if( height < 0 ) {
      result.y = y + height;
      result.height = -height;
    }
    return result;
  }

  private void checkDisposed() {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
  }

  private void drawArc( int x,
                        int y,
                        int width,
                        int height,
                        int startAngle,
                        int arcAngle,
                        boolean fill )
  {
    Rectangle bounds = checkBounds( x, y, width, height );
    if( bounds.width != 0 && bounds.height != 0 && arcAngle != 0 ) {
      delegate.drawArc( bounds, startAngle, arcAngle, fill );
    }
  }

  private void drawRectangle( int x,
                              int y,
                              int width,
                              int height,
                              int arcWidth,
                              int arcHeight,
                              boolean fill )
  {
    Rectangle bounds = checkBounds( x, y, width, height );
    if( bounds.width != 0 && bounds.height != 0 ) {
      if( arcWidth == 0 || arcHeight == 0 ) {
        delegate.drawRectangle( bounds, fill );
      } else {
        int absArcWidth = Math.abs( arcWidth );
        int absArcHeight = Math.abs( arcHeight );
        delegate.drawRoundRectangle( bounds, absArcWidth, absArcHeight, fill );
      }
    }
  }

  private void fillGradientRect( int x, int y, int width, int height, boolean vertical ) {
    Rectangle bounds = new Rectangle( x, y, width, height );
    delegate.fillGradientRectangle( bounds, vertical );
  }

  private static GCDelegate determineDelegate( Drawable drawable ) {
    GCDelegate result = null;
    // Assume that Drawable is either a Control or a Device
    if( drawable instanceof Control ) {
      result = new ControlGC( ( Control )drawable );
    } else if( drawable instanceof Device ) {
      result = new DeviceGC( ( Device )drawable );
    }
    return result;
  }

  private static Device determineDevice( Drawable drawable ) {
    Device result = null;
    if( drawable instanceof Control ) {
      result = ( ( Control )drawable ).getDisplay();
    } else if( drawable instanceof Device ) {
      result = ( Device )drawable;
    }
    return result;
  }

  @SuppressWarnings( "unused" )
  void writeObject( ObjectOutputStream stream ) throws IOException {
    throw new NotSerializableException( getClass().getName() );
  }

  public void setLineStyle( int lineStyle ) {
    return;
  }
}