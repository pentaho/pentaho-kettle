/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 *     EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.widgets;

import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.theme.ThemeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.widgets.scrollbarkit.ScrollBarLCA;
import org.eclipse.swt.internal.widgets.scrollbarkit.ScrollBarThemeAdapter;

/**
 * Instances of this class are selectable user interface
 * objects that represent a range of positive, numeric values.
 * <p>
 * At any given moment, a given scroll bar will have a
 * single 'selection' that is considered to be its
 * value, which is constrained to be within the range of
 * values the scroll bar represents (that is, between its
 * <em>minimum</em> and <em>maximum</em> values).
 * </p><p>
 * Typically, scroll bars will be made up of five areas:
 * <ol>
 * <li>an arrow button for decrementing the value</li>
 * <li>a page decrement area for decrementing the value by a larger amount</li>
 * <li>a <em>thumb</em> for modifying the value by mouse dragging</li>
 * <li>a page increment area for incrementing the value by a larger amount</li>
 * <li>an arrow button for incrementing the value</li>
 * </ol>
 * Based on their style, scroll bars are either <code>HORIZONTAL</code>
 * (which have a left facing button for decrementing the value and a
 * right facing button for incrementing it) or <code>VERTICAL</code>
 * (which have an upward facing button for decrementing the value
 * and a downward facing buttons for incrementing it).
 * </p><p>
 * On some platforms, the size of the scroll bar's thumb can be
 * varied relative to the magnitude of the range of values it
 * represents (that is, relative to the difference between its
 * maximum and minimum values). Typically, this is used to
 * indicate some proportional value such as the ratio of the
 * visible area of a document to the total amount of space that
 * it would take to display it. SWT supports setting the thumb
 * size even if the underlying platform does not, but in this
 * case the appearance of the scroll bar will not change.
 * </p><p>
 * Scroll bars are created by specifying either <code>H_SCROLL</code>,
 * <code>V_SCROLL</code> or both when creating a <code>Scrollable</code>.
 * They are accessed from the <code>Scrollable</code> using
 * <code>getHorizontalBar</code> and <code>getVerticalBar</code>.
 * </p><p>
 * Note: Scroll bars are not Controls.  On some platforms, scroll bars
 * that appear as part of some standard controls such as a text or list
 * have no operating system resources and are not children of the control.
 * For this reason, scroll bars are treated specially.  To create a control
 * that looks like a scroll bar but has operating system resources, use
 * <code>Slider</code>.
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
 * <!--@see Slider-->
 * @see Scrollable
 * @see Scrollable#getHorizontalBar
 * @see Scrollable#getVerticalBar
 *
 * <p>(current) limitations:</p>
 * <ul>
 * <li>minimum, maximum, thumb, increment and pageIncrement properties are not
 * rendered (no corresponding client-side property)</li>
 * </ul>
 *
 * @since 1.0
 */
public class ScrollBar extends Widget {

  private final Scrollable parent;
  private int thumb;
  private int selection;
  private int minimum;
  private int maximum;

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
   * <p><strong>IMPORTANT:</strong> This method is <em>not</em> part of the RWT
   * public API. It is marked public only so that it can be shared
   * within the packages provided by RWT. It should never be accessed
   * from application code.
   * </p>
   *
   * @see SWT#HORIZONTAL
   * @see SWT#VERTICAL
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  ScrollBar( Scrollable parent, int style ) {
    super( parent, checkStyle( style ) );
    this.parent = parent;
  }

  /**
   * Returns the receiver's parent, which must be a Scrollable.
   *
   * @return the receiver's parent
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Scrollable getParent() {
    checkWidget();
    return parent;
  }

  /////////////
  // Visibility

  /**
   * Marks the receiver as visible if the argument is <code>true</code>,
   * and marks it invisible otherwise.
   * <p>
   * If one of the receiver's ancestors is not visible or some
   * other condition makes the receiver not visible, marking
   * it visible may not actually cause it to be displayed.
   * </p>
   *
   * @param visible the new visibility state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setVisible( boolean visible ) {
    checkWidget();
    boolean isVisible = !hasState( HIDDEN );
    if( isVisible != visible ) {
      if( visible ) {
        removeState( HIDDEN );
      } else {
        addState( HIDDEN );
      }
    }
  }

  /**
   * Returns <code>true</code> if the receiver is visible, and
   * <code>false</code> otherwise.
   * <p>
   * If one of the receiver's ancestors is not visible or some
   * other condition makes the receiver not visible, this method
   * may still indicate that it is considered visible even though
   * it may not actually be showing.
   * </p>
   *
   * @return the receiver's visibility state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public boolean getVisible() {
    checkWidget();
    return !hasState( HIDDEN );
  }

  /**
   * Returns <code>true</code> if the receiver is visible and all
   * of the receiver's ancestors are visible and <code>false</code>
   * otherwise.
   *
   * @return the receiver's visibility state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #getVisible
   */
  public boolean isVisible() {
    checkWidget();
    return getVisible() && parent.isVisible();
  }

  /////////////
  // Enablement

  /**
   * Enables the receiver if the argument is <code>true</code>,
   * and disables it otherwise. A disabled control is typically
   * not selectable from the user interface and draws with an
   * inactive or "grayed" look.
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

  /**
   * Returns a point describing the receiver's size. The
   * x coordinate of the result is the width of the receiver.
   * The y coordinate of the result is the height of the
   * receiver.
   *
   * @return the receiver's size
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Point getSize() {
    Point result = parent.getSize();
    if( ( style & SWT.HORIZONTAL ) != 0 ) {
      result.y = getScrollBarWidth();
    } else {
      result.x = getScrollBarWidth();
    }
    return result;
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
   *
   * @see ScrollBar
   */
  public int getThumb() {
    checkWidget();
    return thumb;
  }

  /**
   * Sets the size of the receiver's thumb relative to the
   * difference between its maximum and minimum values.  This new
   * value will be ignored if it is less than one, and will be
   * clamped if it exceeds the receiver's current range.
   *
   * @param thumb the new thumb value, which must be at least one and not
   * larger than the size of the current range
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setThumb( int thumb ) {
    checkWidget();
    this.thumb = thumb;
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

  public void setIncrement( int value ) { }

  /**
   * Sets the maximum. If this value is negative or less than or
   * equal to the minimum, the value is ignored. If necessary, first
   * the thumb and then the selection are adjusted to fit within the
   * new range.
   *
   * @param maximum the new maximum
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setMaximum( int maximum ) {
    checkWidget();
    this.maximum = maximum;
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
   * Sets the minimum value. If this value is negative or greater
   * than or equal to the maximum, the value is ignored. If necessary,
   * first the thumb and then the selection are adjusted to fit within
   * the new range.
   *
   * @param minimum the new minimum
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setMinimum( int minimum ) {
    checkWidget();
    this.minimum = minimum;
  }

  public void setPageIncrement( int value ) { }

  /**
   * Sets the single <em>selection</em> that is the receiver's
   * value to the argument which must be greater than or equal
   * to zero.
   *
   * @param selection the new selection (must be zero or greater)
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setSelection( int selection ) {
    checkWidget();
    if( this.selection != selection ) {
      this.selection = selection;
    }
  }

  /**
   * Returns the single 'selection' that is the receiver's value.
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

  ////////////////////
  // SelectionListener

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the receiver's value changes, by sending
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
   * be notified when the receiver's value changes.
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
  @SuppressWarnings( "unchecked" )
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == WidgetLCA.class ) {
      return ( T )ScrollBarLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  @Override
  void releaseParent() {
    super.releaseParent();
    if( parent.horizontalBar == this ) {
      parent.horizontalBar = null;
    }
    if( parent.verticalBar == this ) {
      parent.verticalBar = null;
    }
  }

  //////////////////
  // Helping methods

  private static int checkStyle( int style ) {
    return checkBits( style, SWT.HORIZONTAL, SWT.VERTICAL, 0, 0, 0, 0 );
  }

  private int getScrollBarWidth() {
    ScrollBarThemeAdapter themeAdapter = ( ScrollBarThemeAdapter )getAdapter( ThemeAdapter.class );
    return themeAdapter.getScrollBarWidth( this );
  }

}