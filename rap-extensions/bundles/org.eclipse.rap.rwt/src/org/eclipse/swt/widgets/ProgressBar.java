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
import org.eclipse.rap.rwt.internal.theme.ThemeAdapter;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.widgets.progressbarkit.ProgressBarLCA;
import org.eclipse.swt.internal.widgets.progressbarkit.ProgressBarThemeAdapter;

/**
 * Instances of the receiver represent is an unselectable user interface object
 * that is used to display progress, typically in the form of a bar.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SMOOTH, HORIZONTAL, VERTICAL, INDETERMINATE</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles HORIZONTAL and VERTICAL may be specified.
 * </p>
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em> within the
 * SWT implementation.
 * </p>
 * @since 1.0
 */
public class ProgressBar extends Control {

  private int minimum;
  private int selection;
  private int maximum;
  private int state;

  /**
   * Constructs a new instance of this class given its parent and a style value
   * describing its behavior and appearance.
   * <p>
   * The style value is either one of the style constants defined in class
   * <code>SWT</code> which is applicable to instances of this class, or must
   * be built by <em>bitwise OR</em>'ing together (that is, using the
   * <code>int</code> "|" operator) two or more of those <code>SWT</code>
   * style constants. The class description lists the style constants that are
   * applicable to the class. Style bits are also inherited from superclasses.
   * </p>
   *
   * @param parent a composite control which will be the parent of the new
   *            instance (cannot be null)
   * @param style the style of control to construct
   * @exception IllegalArgumentException
   *                <ul>
   *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *                </ul>
   * @exception SWTException
   *                <ul>
   *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *                thread that created the parent</li>
   *                <li>ERROR_INVALID_SUBCLASS - if this class is not an
   *                allowed subclass</li>
   *                </ul>
   * @see SWT#SMOOTH
   * @see SWT#HORIZONTAL
   * @see SWT#VERTICAL
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public ProgressBar( Composite parent, int style ) {
    super( parent, checkStyle( style ) );
    maximum = 100;
    state = SWT.NORMAL;
  }

  static int checkStyle( int style ) {
    int currStyle = style | SWT.NO_FOCUS;
    return checkBits( currStyle, SWT.HORIZONTAL, SWT.VERTICAL, 0, 0, 0, 0 );
  }

  @Override
  public Point computeSize( int wHint, int hHint, boolean changed ) {
    checkWidget();
    BoxDimensions border = getBorder();
    int barWidth = getProgressBarWidth();
    int width = border.left + border.right;
    int height = border.top + border.bottom;
    if( ( style & SWT.HORIZONTAL ) != 0 ) {
      width += barWidth * 10;
      height += barWidth;
    } else {
      width += barWidth;
      height += barWidth * 10;
    }
     if( wHint != SWT.DEFAULT ) {
       width = wHint + border.left + border.right;
     }
     if( hHint != SWT.DEFAULT ) {
       height = hHint + border.top + border.bottom;
     }
    return new Point( width, height );
  }

  /**
   * Returns the maximum value which the receiver will allow.
   *
   * @return the maximum
   * @exception SWTException
   *                <ul>
   *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
   *                disposed</li>
   *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *                thread that created the receiver</li>
   *                </ul>
   */
  public int getMaximum() {
    checkWidget();
    return maximum;
  }

  /**
   * Returns the minimum value which the receiver will allow.
   *
   * @return the minimum
   * @exception SWTException
   *                <ul>
   *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
   *                disposed</li>
   *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *                thread that created the receiver</li>
   *                </ul>
   */
  public int getMinimum() {
    checkWidget();
    return minimum;
  }

  /**
   * Returns the single 'selection' that is the receiver's position.
   *
   * @return the selection
   * @exception SWTException
   *                <ul>
   *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
   *                disposed</li>
   *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *                thread that created the receiver</li>
   *                </ul>
   */
  public int getSelection() {
    checkWidget();
    return selection;
  }

  /**
   * Sets the maximum value that the receiver will allow. This new value will be
   * ignored if it is not greater than the receiver's current minimum value. If
   * the new maximum is applied then the receiver's selection value will be
   * adjusted if necessary to fall within its new range.
   *
   * @param value the new maximum, which must be greater than the current
   *            minimum
   * @exception SWTException
   *                <ul>
   *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
   *                disposed</li>
   *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *                thread that created the receiver</li>
   *                </ul>
   */
  public void setMaximum( int value ) {
    checkWidget();
    if( value > getMinimum() ) {
      maximum = value;
      if( selection > maximum ) {
        selection = maximum;
      }
    }
  }

  /**
   * Sets the minimum value that the receiver will allow. This new value will be
   * ignored if it is negative or is not less than the receiver's current
   * maximum value. If the new minimum is applied then the receiver's selection
   * value will be adjusted if necessary to fall within its new range.
   *
   * @param value the new minimum, which must be nonnegative and less than the
   *            current maximum
   * @exception SWTException
   *                <ul>
   *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
   *                disposed</li>
   *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *                thread that created the receiver</li>
   *                </ul>
   */
  public void setMinimum( int value ) {
    checkWidget();
    if( value > 0 && value < getMaximum() ) {
      minimum = value;
      if( minimum > selection ) {
        selection = minimum;
      }
    }
  }

  /**
   * Sets the single 'selection' that is the receiver's position to the argument
   * which must be greater than or equal to zero.
   *
   * @param value the new selection (must be zero or greater)
   * @exception SWTException
   *                <ul>
   *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
   *                disposed</li>
   *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *                thread that created the receiver</li>
   *                </ul>
   */
  public void setSelection( int value ) {
    checkWidget();
    if( value < minimum ) {
      selection = minimum;
    } else if( value > maximum ) {
      selection = maximum;
    } else {
      selection = value;
    }
  }

  /**
   * Sets the state of the receiver. The state must be one of these values:
   * <ul>
   * <li>{@link SWT#NORMAL}</li>
   * <li>{@link SWT#ERROR}</li>
   * <li>{@link SWT#PAUSED}</li>
   * </ul>
   *
   * @param state the new state
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.3
   */
  public void setState( int state ) {
    checkWidget();
    if( state == SWT.NORMAL || state == SWT.PAUSED || state == SWT.ERROR ) {
      this.state = state;
    }
  }

  /**
   * Returns the state of the receiver. The value will be one of:
   * <ul>
   * <li>{@link SWT#NORMAL}</li>
   * <li>{@link SWT#ERROR}</li>
   * <li>{@link SWT#PAUSED}</li>
   * </ul>
   *
   * @return the state
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.3
   */
  public int getState() {
    checkWidget();
    return state;
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == WidgetLCA.class ) {
      return ( T )ProgressBarLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  private int getProgressBarWidth() {
    ThemeAdapter themeAdapter = getAdapter( ThemeAdapter.class );
    return ( ( ProgressBarThemeAdapter )themeAdapter ).getProgressBarWidth( this );
  }

}
