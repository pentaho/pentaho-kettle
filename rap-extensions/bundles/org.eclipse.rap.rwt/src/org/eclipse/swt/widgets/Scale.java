/*******************************************************************************
 * Copyright (c) 2008, 2015 Innoopract Informationssysteme GmbH and others.
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
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.widgets.scalekit.ScaleLCA;


/**
 * Instances of the receiver represent a selectable user interface object that
 * present a range of continuous numeric values.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>HORIZONTAL, VERTICAL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles HORIZONTAL and VERTICAL may be specified.
 * </p>
 * <p>
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em> within the
 * SWT implementation.
 * </p>
 *
 * @since 1.2
 */
public class Scale extends Control {

  private final Point PREFERRED_SIZE = new Point( 160, 41 );

  private int increment;
  private int maximum;
  private int minimum;
  private int pageIncrement;
  private int selection;

  /**
   * Constructs a new instance of this class given its parent and a style value
   * describing its behavior and appearance.
   * <p>
   * The style value is either one of the style constants defined in class
   * <code>SWT</code> which is applicable to instances of this class, or must be
   * built by <em>bitwise OR</em>'ing together (that is, using the
   * <code>int</code> "|" operator) two or more of those <code>SWT</code> style
   * constants. The class description lists the style constants that are
   * applicable to the class. Style bits are also inherited from superclasses.
   * </p>
   *
   * @param parent a composite control which will be the parent of the new
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
   * @see SWT#HORIZONTAL
   * @see SWT#VERTICAL
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public Scale( Composite parent, int style ) {
    super( parent, checkStyle( style ) );
    maximum = 100;
    increment = 1;
    pageIncrement = 10;
  }

  /**
   * Adds the listener to the collection of listeners who will be notified when
   * the user changes the receiver's value, by sending it one of the messages
   * defined in the <code>SelectionListener</code> interface.
   * <p>
   * <code>widgetSelected</code> is called when the user changes the receiver's
   * value. <code>widgetDefaultSelected</code> is not called.
   * </p>
   *
   * @param listener the listener which should be notified
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @see SelectionListener
   * @see #removeSelectionListener
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
   * Removes the listener from the collection of listeners who will be notified
   * when the user changes the receiver's value.
   *
   * @param listener the listener which should no longer be notified
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
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
  public Point computeSize( int wHint, int hHint, boolean changed ) {
    checkWidget();
    BoxDimensions border = getBorder();
    int width = border.left + border.right;
    int height = border.top + border.bottom;
    if( ( style & SWT.HORIZONTAL ) != 0 ) {
      width += PREFERRED_SIZE.x;
      height += PREFERRED_SIZE.y;
    } else {
      width += PREFERRED_SIZE.y;
      height += PREFERRED_SIZE.x;
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
   * Returns the amount that the receiver's value will be modified by when the
   * up/down (or right/left) arrows are pressed.
   *
   * @return the increment
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public int getIncrement() {
    checkWidget();
    return increment;
  }

  /**
   * Returns the maximum value which the receiver will allow.
   *
   * @return the maximum
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public int getMaximum() {
    checkWidget();
    return maximum;
  }

  /**
   * Returns the minimum value which the receiver will allow.
   *
   * @return the minimum
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public int getMinimum() {
    checkWidget();
    return minimum;
  }

  /**
   * Returns the amount that the receiver's value will be modified by when the
   * page increment/decrement areas are selected.
   *
   * @return the page increment
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public int getPageIncrement() {
    checkWidget();
    return pageIncrement;
  }

  /**
   * Returns the 'selection', which is the receiver's position.
   *
   * @return the selection
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public int getSelection() {
    checkWidget();
    return selection;
  }

  /**
   * Sets the amount that the receiver's value will be modified by when the
   * up/down (or right/left) arrows are pressed to the argument, which must be
   * at least one.
   *
   * @param increment the new increment (must be greater than zero)
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setIncrement( int increment ) {
    checkWidget();
    if( increment >= 1 && increment <= maximum - minimum ) {
      this.increment = increment;
    }
  }

  /**
   * Sets the maximum value that the receiver will allow. This new value will be
   * ignored if it is not greater than the receiver's current minimum value. If
   * the new maximum is applied then the receiver's selection value will be
   * adjusted if necessary to fall within its new range.
   *
   * @param maximum the new maximum, which must be greater than the current
   *          minimum
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setMaximum( int maximum ) {
    checkWidget();
    if( 0 <= minimum && minimum < maximum ) {
      this.maximum = maximum;
      if( selection > this.maximum ) {
        selection = this.maximum;
      }
    }
  }

  /**
   * Sets the minimum value that the receiver will allow. This new value will be
   * ignored if it is negative or is not less than the receiver's current
   * maximum value. If the new minimum is applied then the receiver's selection
   * value will be adjusted if necessary to fall within its new range.
   *
   * @param minimum the new minimum, which must be nonnegative and less than the
   *          current maximum
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setMinimum( int minimum ) {
    checkWidget();
    if( 0 <= minimum && minimum < maximum ) {
      this.minimum = minimum;
      if( selection < this.minimum ) {
        selection = this.minimum;
      }
    }
  }

  /**
   * Sets the amount that the receiver's value will be modified by when the page
   * increment/decrement areas are selected to the argument, which must be at
   * least one.
   *
   * @param pageIncrement the page increment (must be greater than zero)
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setPageIncrement( int pageIncrement ) {
    checkWidget();
    if( pageIncrement >= 1 && pageIncrement <= maximum - minimum ) {
      this.pageIncrement = pageIncrement;
    }
  }

  /**
   * Sets the 'selection', which is the receiver's value, to the argument which
   * must be greater than or equal to zero.
   *
   * @param selection the new selection (must be zero or greater)
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setSelection( int selection ) {
    checkWidget();
    if( selection >= minimum && selection <= maximum ) {
      this.selection = selection;
    }
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == WidgetLCA.class ) {
      return ( T )ScaleLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  private static int checkStyle( int style ) {
    return checkBits( style, SWT.HORIZONTAL, SWT.VERTICAL, 0, 0, 0, 0 );
  }

  ///////////////////
  // Widget overrides

  @Override
  boolean isTabGroup() {
    return true;
  }

}
