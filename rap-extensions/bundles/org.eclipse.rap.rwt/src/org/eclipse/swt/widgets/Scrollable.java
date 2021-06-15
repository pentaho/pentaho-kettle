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

import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Rectangle;

/**
 * This class is the abstract superclass of all classes which
 * represent controls that have standard scroll bars.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>H_SCROLL, V_SCROLL</dd>
 * <dt><b>Events:</b>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the SWT implementation.
 * </p>
 * @since 1.0
 */
public abstract class Scrollable extends Control {

  ScrollBar verticalBar;
  ScrollBar horizontalBar;

  Scrollable( Composite parent ) {
    // prevent instantiation from outside this package
    super( parent );
  }

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
   * @see SWT#H_SCROLL
   * @see SWT#V_SCROLL
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public Scrollable( Composite parent, int style ) {
    super( parent, style );
    createScrollBars();
  }

  private void createScrollBars() {
    if( ( style & SWT.H_SCROLL ) != 0 ) {
      horizontalBar = new ScrollBar( this, SWT.H_SCROLL );
      horizontalBar.setVisible( false );
    }
    if( ( style & SWT.V_SCROLL ) != 0 ) {
      verticalBar = new ScrollBar( this, SWT.V_SCROLL );
      verticalBar.setVisible( false );
    }
  }

  /**
   * Returns a rectangle which describes the area of the
   * receiver which is capable of displaying data (that is,
   * not covered by the "trimmings").
   *
   * @return the client area
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #computeTrim
   */
  public Rectangle getClientArea() {
    checkWidget();
    Rectangle bounds = getBounds();
    BoxDimensions border = getBorder();
    BoxDimensions padding = getPadding();
    int width = bounds.width
                - ( border.left + border.right )
                - ( padding.left + padding.right )
                - getVScrollBarWidth();
    int height = bounds.height
                 - ( border.top + border.bottom )
                 - ( padding.top + padding.bottom )
                 - getHScrollBarHeight();
    return new Rectangle( padding.left, padding.top, Math.max( 0, width ), Math.max( 0, height ) );
  }

  /**
   * Given a desired <em>client area</em> for the receiver
   * (as described by the arguments), returns the bounding
   * rectangle which would be required to produce that client
   * area.
   * <p>
   * In other words, it returns a rectangle such that, if the
   * receiver's bounds were set to that rectangle, the area
   * of the receiver which is capable of displaying data
   * (that is, not covered by the "trimmings") would be the
   * rectangle described by the arguments (relative to the
   * receiver's parent).
   * </p>
   *
   * @param x the desired x coordinate of the client area
   * @param y the desired y coordinate of the client area
   * @param width the desired width of the client area
   * @param height the desired height of the client area
   * @return the required bounds to produce the given client area
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #getClientArea
   */
  public Rectangle computeTrim( int x, int y, int width, int height ) {
    checkWidget();
    BoxDimensions border = getBorder();
    BoxDimensions padding = getPadding();
    int newWidth = width + border.left + border.right + padding.left + padding.right;
    if( verticalBar != null ) {
      newWidth += verticalBar.getSize().x;
    }
    int newHeight = height + border.top + border.bottom + padding.top + padding.bottom;
    if( horizontalBar != null ) {
      newHeight += horizontalBar.getSize().y;
    }
    int newX = x - border.left - padding.left;
    int newY = y - border.top - padding.top;
    return new Rectangle( newX, newY, newWidth, newHeight );
  }

  /**
   * Returns the receiver's horizontal scroll bar if it has
   * one, and null if it does not.
   *
   * @return the horizontal scroll bar (or null)
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.4
   */
  public ScrollBar getHorizontalBar() {
    checkWidget();
    return horizontalBar;
  }

  /**
   * Returns the receiver's vertical scroll bar if it has
   * one, and null if it does not.
   *
   * @return the vertical scroll bar (or null)
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.4
   */
  public ScrollBar getVerticalBar() {
    checkWidget();
    return verticalBar;
  }

  @Override
  void releaseChildren() {
    super.releaseChildren();
    if( verticalBar != null ) {
      verticalBar.dispose();
    }
    if( horizontalBar != null ) {
      horizontalBar.dispose();
    }
  }

  int getVScrollBarWidth() {
    int result = 0;
    if( verticalBar != null && verticalBar.getVisible() ) {
      result = verticalBar.getSize().x;
    }
    return result;
  }

  int getHScrollBarHeight() {
    int result = 0;
    if( horizontalBar != null && horizontalBar.getVisible() ) {
      result = horizontalBar.getSize().y;
    }
    return result;
  }
}
