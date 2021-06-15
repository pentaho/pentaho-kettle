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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.widgets.sliderkit.SliderLCA;


/**
 * Instances of this class are selectable user interface
 * objects that represent a range of positive, numeric values.
 * <p>
 * At any given moment, a given slider will have a
 * single 'selection' that is considered to be its
 * value, which is constrained to be within the range of
 * values the slider represents (that is, between its
 * <em>minimum</em> and <em>maximum</em> values).
 * </p><p>
 * Typically, sliders will be made up of five areas:
 * <ol>
 * <li>an arrow button for decrementing the value</li>
 * <li>a page decrement area for decrementing the value by a larger amount</li>
 * <li>a <em>thumb</em> for modifying the value by mouse dragging</li>
 * <li>a page increment area for incrementing the value by a larger amount</li>
 * <li>an arrow button for incrementing the value</li>
 * </ol>
 * Based on their style, sliders are either <code>HORIZONTAL</code>
 * (which have a left facing button for decrementing the value and a
 * right facing button for incrementing it) or <code>VERTICAL</code>
 * (which have an upward facing button for decrementing the value
 * and a downward facing buttons for incrementing it).
 * </p><p>
 * On some platforms, the size of the slider's thumb can be
 * varied relative to the magnitude of the range of values it
 * represents (that is, relative to the difference between its
 * maximum and minimum values). Typically, this is used to
 * indicate some proportional value such as the ratio of the
 * visible area of a document to the total amount of space that
 * it would take to display it. SWT supports setting the thumb
 * size even if the underlying platform does not, but in this
 * case the appearance of the slider will not change.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>HORIZONTAL, VERTICAL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles HORIZONTAL and VERTICAL may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see ScrollBar
 * @since 1.2
 */
public class Slider extends Control {

  private final Point PREFERRED_SIZE = new Point( 170, 16 );

  private int increment;
  private int maximum;
  private int minimum;
  private int pageIncrement;
  private int selection;
  private int thumb;

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
   * @see SWT#HORIZONTAL
   * @see SWT#VERTICAL
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public Slider( Composite parent, int style ) {
    super( parent, checkStyle (style) );
    increment = 1;
    maximum = 100;
    minimum = 0;
    pageIncrement = 10;
    selection = 0;
    thumb = 10;
  }

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the user changes the receiver's value, by sending
   * it one of the messages defined in the <code>SelectionListener</code>
   * interface.
   * <p>
   * When <code>widgetSelected</code> is called, the event object detail field contains one of the following values:
   * <code>SWT.NONE</code> - for the end of a drag.
   * <code>SWT.DRAG</code>.
   * <code>SWT.HOME</code>.
   * <code>SWT.END</code>.
   * <code>SWT.ARROW_DOWN</code>.
   * <code>SWT.ARROW_UP</code>.
   * <code>SWT.PAGE_DOWN</code>.
   * <code>SWT.PAGE_UP</code>.
   * <code>widgetDefaultSelected</code> is not called.
   * </p>
   *
   * @param listener the listener which should be notified when the user changes the receiver's value
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
   * be notified when the user changes the receiver's value.
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

  static int checkStyle( int style ) {
    return checkBits( style, SWT.HORIZONTAL, SWT.VERTICAL, 0, 0, 0, 0 );
  }

  @Override
  public Point computeSize( int wHint, int hHint, boolean changed ) {
    checkWidget();
    int width, height;
    if( ( style & SWT.HORIZONTAL ) != 0 ) {
      width = PREFERRED_SIZE.x;
      height = PREFERRED_SIZE.y;
    } else {
      width = PREFERRED_SIZE.y;
      height = PREFERRED_SIZE.x;
    }
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

  @Override
  void createWidget() {
    super.createWidget();
    increment = 1;
    pageIncrement = 10;
    maximum = 100;
    thumb = 10;
  }

  /**
   * Returns the amount that the receiver's value will be
   * modified by when the up/down (or right/left) arrows
   * are pressed.
   *
   * @return the increment
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getIncrement() {
    checkWidget();
    return increment;
  }

  /**
   * Returns the maximum value which the receiver will allow.
   *
   * @return the maximum
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getMaximum() {
    checkWidget();
    return maximum;
  }

  /**
   * Returns the minimum value which the receiver will allow.
   *
   * @return the minimum
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getMinimum() {
    checkWidget();
    return minimum;
  }

  /**
   * Returns the amount that the receiver's value will be
   * modified by when the page increment/decrement areas
   * are selected.
   *
   * @return the page increment
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getPageIncrement() {
    checkWidget();
    return pageIncrement;
  }

  /**
   * Returns the 'selection', which is the receiver's value.
   *
   * @return the selection
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getSelection() {
    checkWidget();
    return selection;
  }

  /**
   * Returns the size of the receiver's thumb relative to the
   * difference between its maximum and minimum values.
   *
   * @return the thumb value
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getThumb() {
    checkWidget();
    return thumb;
  }

  /**
   * Sets the amount that the receiver's value will be
   * modified by when the up/down (or right/left) arrows
   * are pressed to the argument, which must be at least
   * one.
   *
   * @param value the new increment (must be greater than zero)
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setIncrement( int value ) {
    checkWidget();
    if( value >= 1 && value <= maximum - minimum ) {
      increment = value;
    }
  }

  /**
   * Sets the maximum. If this value is negative or less than or
   * equal to the minimum, the value is ignored. If necessary, first
   * the thumb and then the selection are adjusted to fit within the
   * new range.
   *
   * @param value the new maximum, which must be greater than the current minimum
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setMaximum( int value ) {
    checkWidget();
    if( 0 <= minimum && minimum < value ) {
      maximum = value;
      if( selection > maximum - thumb ) {
        selection = maximum - thumb;
      }
    }
    if( thumb >= maximum - minimum ) {
      thumb = maximum - minimum;
      selection = minimum;
    }
  }

  /**
   * Sets the minimum value. If this value is negative or greater
   * than or equal to the maximum, the value is ignored. If necessary,
   * first the thumb and then the selection are adjusted to fit within
   * the new range.
   *
   * @param value the new minimum
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setMinimum( int value ) {
    checkWidget();
    if( 0 <= value && value < maximum ) {
      minimum = value;
      if( selection < minimum ) {
        selection = minimum;
      }
    }
    if( thumb >= maximum - minimum ) {
      thumb = maximum - minimum;
      selection = minimum;
    }
  }

  /**
   * Sets the amount that the receiver's value will be
   * modified by when the page increment/decrement areas
   * are selected to the argument, which must be at least
   * one.
   *
   * @param value the page increment (must be greater than zero)
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setPageIncrement( int value ) {
    checkWidget();
    if( value >= 1 && value <= maximum - minimum ) {
      pageIncrement = value;
    }
  }

  /**
   * Sets the 'selection', which is the receiver's
   * value, to the argument which must be greater than or equal
   * to zero.
   *
   * @param value the new selection (must be zero or greater)
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setSelection( int value ) {
    checkWidget();
    if( value < minimum ) {
      selection = minimum;
    } else if ( value > maximum - thumb ) {
      selection = maximum - thumb;
    } else {
      selection = value;
    }
  }

  /**
   * Sets the size of the receiver's thumb relative to the
   * difference between its maximum and minimum values.  This new
   * value will be ignored if it is less than one, and will be
   * clamped if it exceeds the receiver's current range.
   *
   * @param value the new thumb value, which must be at least one and not
   * larger than the size of the current range
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setThumb( int value ) {
    checkWidget();
    if( value >= 1 ) {
      thumb = value;
    }
    if( value >= maximum - minimum ) {
      thumb = maximum - minimum;
      selection = minimum;
    }
  }

  /**
   * Sets the receiver's selection, minimum value, maximum
   * value, thumb, increment and page increment all at once.
   * <p>
   * Note: This is similar to setting the values individually
   * using the appropriate methods, but may be implemented in a
   * more efficient fashion on some platforms.
   * </p>
   *
   * @param selection the new selection value
   * @param minimum the new minimum value
   * @param maximum the new maximum value
   * @param thumb the new thumb value
   * @param increment the new increment value
   * @param pageIncrement the new pageIncrement value
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setValues( int selection,
                         int minimum,
                         int maximum,
                         int thumb,
                         int increment,
                         int pageIncrement )
  {
    checkWidget();
    if( selection >= minimum && selection <= maximum ) {
      this.selection = selection;
    }
    if( 0 <= minimum && minimum < maximum ) {
      this.minimum = minimum;
      if( selection < minimum ) {
        this.selection = minimum;
      }
    }
    if( 0 <= minimum && minimum < maximum ) {
      this.maximum = maximum;
      if( selection > maximum - thumb ) {
        this.selection = maximum - thumb;
      }
    }
    if( thumb >= 1 ) {
      this.thumb = thumb;
    }
    if( increment >= 1 && increment <= ( maximum - minimum ) ) {
      this.increment = increment;
    }
    if( pageIncrement >= 1 && pageIncrement <= ( maximum - minimum ) ) {
      this.pageIncrement = pageIncrement;
    }
    if( thumb >= maximum - minimum ) {
      this.thumb = maximum - minimum;
      this.selection = minimum;
    }
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == WidgetLCA.class ) {
      return ( T )SliderLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  @Override
  boolean isTabGroup() {
    return true;
  }

}
