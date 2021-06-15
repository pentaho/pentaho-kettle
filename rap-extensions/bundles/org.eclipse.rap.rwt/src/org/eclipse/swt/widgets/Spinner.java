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

import java.text.NumberFormat;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.rap.rwt.internal.theme.ThemeAdapter;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.spinnerkit.SpinnerLCA;
import org.eclipse.swt.internal.widgets.spinnerkit.SpinnerThemeAdapter;


/**
 * Instances of this class are selectable user interface
 * objects that allow the user to enter and modify numeric
 * values.
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>READ_ONLY, WRAP</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection, Modify</dd>
 * </dl>
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * @since 1.0
 */
public class Spinner extends Composite {

  /**
   * the <!-- operating system --> limit for the number of characters
   * that the text field in an instance of this class can hold
   *
   * @since 1.3
   */
  public static final int LIMIT = Integer.MAX_VALUE;

  private static final int UP_DOWN_MIN_HEIGHT = 18;

  private int minimum;
  private int maximum;
  private int digits;
  private int increment;
  private int pageIncrement;
  private int selection;
  private int textLimit;

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
   * @see SWT#READ_ONLY
   * @see SWT#WRAP
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public Spinner( Composite parent, int style ) {
    super( parent, checkStyle( style ) );
    minimum = 0;
    maximum = 100;
    digits = 0;
    increment = 1;
    pageIncrement = 10;
    selection = 0;
    textLimit = LIMIT;
  }

  @Override
  void initState() {
    removeState( /* CANVAS | */ THEME_BACKGROUND );
  }

  /**
   * Returns the number of decimal places used by the receiver.
   *
   * @return the digits
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getDigits() {
    checkWidget();
    return digits;
  }

  /**
   * Sets the number of decimal places used by the receiver.
   * <p>
   * The digit setting is used to allow for floating point values in the receiver.
   * For example, to set the selection to a floating point value of 1.37 call setDigits() with
   * a value of 2 and setSelection() with a value of 137. Similarly, if getDigits() has a value
   * of 2 and getSelection() returns 137 this should be interpreted as 1.37. This applies to all
   * numeric APIs.
   * </p>
   *
   * @param value the new digits (must be greater than or equal to zero)
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the value is less than zero</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public void setDigits( int value ) {
    checkWidget();
    if( value < 0 ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    digits = value;
  }

  /////////////////////////////////////////
  // Methods to control range and increment

  /**
   * Returns the amount that the receiver's value will be
   * modified by when the up/down arrows are pressed.
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
   * Sets the amount that the receiver's value will be
   * modified by when the up/down arrows are pressed to
   * the argument, which must be at least one.
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
    if( value >= 1 ) {
      increment = value;
    }
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
   * Sets the minimum value that the receiver will allow.  This new
   * value will be ignored if it is greater than the receiver's
   * current maximum value.  If the new minimum is applied then the receiver's
   * selection value will be adjusted if necessary to fall within its new range.
   *
   * @param value the new minimum, which must be less than or equals to the current maximum
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setMinimum( int value ) {
    checkWidget();
    if( value <= maximum ) {
      minimum = value;
      if( selection < minimum ) {
        selection = minimum;
      }
    }
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
   * Sets the maximum value that the receiver will allow.  This new
   * value will be ignored if it is less than the receiver's current
   * minimum value.  If the new maximum is applied then the receiver's
   * selection value will be adjusted if necessary to fall within its new range.
   *
   * @param value the new maximum, which must be greater than or equal to the current minimum
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setMaximum( int value ) {
    checkWidget();
    if( value >= minimum ) {
      maximum = value;
      if( selection > maximum ) {
        selection = maximum;
      }
    }
  }

  /**
   * Returns the amount that the receiver's position will be
   * modified by when the page up/down keys are pressed.
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
   * Sets the amount that the receiver's position will be
   * modified by when the page up/down keys are pressed
   * to the argument, which must be at least one.
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
    if( value >= 1 ) {
      pageIncrement = value;
    }
  }

  /**
   * Returns the <em>selection</em>, which is the receiver's position.
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
   * Sets the <em>selection</em>, which is the receiver's
   * position, to the argument. If the argument is not within
   * the range specified by minimum and maximum, it will be
   * adjusted to fall within this range.
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
    selection = Math.min( Math.max( minimum, value ), maximum );
    notifyListeners( SWT.Modify, new Event() );
  }

  /**
   * Sets the receiver's selection, minimum value, maximum
   * value, digits, increment and page increment all at once.
   * <p>
   * Note: This is similar to setting the values individually
   * using the appropriate methods, but may be implemented in a
   * more efficient fashion on some platforms.
   * </p>
   *
   * @param selection the new selection value
   * @param minimum the new minimum value
   * @param maximum the new maximum value
   * @param digits the new digits value
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
                         int digits,
                         int increment,
                         int pageIncrement )
  {
    checkWidget();
    if( maximum >= minimum && digits >= 0 && increment >= 1 && pageIncrement >= 1 ) {
      this.minimum = minimum;
      this.maximum = maximum;
      this.digits = digits;
      this.increment = increment;
      this.pageIncrement = pageIncrement;
      setSelection( selection );
    }
  }

  /**
   * Returns a string containing a copy of the contents of the
   * receiver's text field, or an empty string if there are no
   * contents.
   *
   * @return the receiver's text
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public String getText() {
    checkWidget();
    String result = String.valueOf( selection );
    if( digits > 0 ) {
      NumberFormat nf = NumberFormat.getInstance( RWT.getLocale() );
      nf.setMinimumFractionDigits( digits );
      nf.setMaximumFractionDigits( digits );
      result = nf.format( selection / Math.pow( 10, digits ) );
    }
    return result;
  }

  /**
   * Sets the maximum number of characters that the receiver
   * is capable of holding to be the argument.
   * <p>
   * Instead of trying to set the text limit to zero, consider
   * creating a read-only text widget.
   * </p><p>
   * To reset this value to the default, use <code>setTextLimit(Text.LIMIT)</code>.
   * Specifying a limit value larger than <code>Text.LIMIT</code> sets the
   * receiver's limit to <code>Text.LIMIT</code>.
   * </p>
   *
   * @param textLimit new text limit
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_CANNOT_BE_ZERO - if the limit is zero</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #LIMIT
   * @since 1.3
   */
  public void setTextLimit( int textLimit ) {
    checkWidget();
    if( textLimit == 0 ) {
      error( SWT.ERROR_CANNOT_BE_ZERO );
    }
    // Note that we mimic here the behavior of SWT Text with style MULTI on
    // Windows. In SWT, other operating systems and/or style flags behave
    // different.
    this.textLimit = textLimit;
  }

  /**
   * Returns the maximum number of characters that the receiver is capable of holding.
   * <p>
   * If this has not been changed by <code>setTextLimit()</code>,
   * it will be the constant <code>Text.LIMIT</code>.
   * </p>
   *
   * @return the text limit
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #LIMIT
   * @since 1.3
   */
  public int getTextLimit() {
    checkWidget ();
    return textLimit;
  }

  ///////////////////
  // Size calculation

  @Override
  public Point computeSize( int wHint, int hHint, boolean changed ) {
    checkWidget();
    int width = 0;
    int height = 0;
    if( wHint == SWT.DEFAULT || hHint == SWT.DEFAULT ) {
      int maxValue = Math.max( Math.abs( maximum ), Math.abs( minimum ) );
      String string = String.valueOf( maxValue );
      if( digits > 0 ) {
        StringBuilder buffer = new StringBuilder();
        buffer.append( string );
        buffer.append( "," );
        int count = digits - string.length();
        while( count >= 0 ) {
          buffer.append( "0" );
          count--;
        }
        string = buffer.toString();
      }
      if( minimum < 0 || maximum < 0 ) {
        string += "-";
      }
      Point textSize = TextSizeUtil.stringExtent( getFont(), string );
      BoxDimensions padding = getFieldPadding();
      int buttonWidth = getButtonWidth();
      width = textSize.x + buttonWidth + padding.left + padding.right;
      height = textSize.y + padding.top + padding.bottom;
    }
    if( width == 0 ) {
      width = DEFAULT_WIDTH;
    }
    if( height == 0 ) {
      height = DEFAULT_HEIGHT;
    }
    if( wHint != SWT.DEFAULT ) {
      width = wHint;
    }
    if( hHint != SWT.DEFAULT ) {
      height = hHint;
    }
    Rectangle trim = computeTrim( 0, 0, width, height );
    if( hHint == SWT.DEFAULT ) {
      BoxDimensions border = getBorder();
      int upDownHeight = UP_DOWN_MIN_HEIGHT + border.top + border.bottom;
      trim.height = Math.max( trim.height, upDownHeight );
    }
    return new Point( trim.width, trim.height );
  }

  @Override
  public Rectangle computeTrim( int x, int y, int width, int height ) {
    checkWidget();
    Rectangle result = new Rectangle( x, y, width, height );
    if( ( style & SWT.BORDER ) != 0 ) {
      BoxDimensions border = getBorder();
      result.x -= border.left;
      result.y -= border.top;
      result.width += border.left + border.right;
      result.height += border.top + border.bottom;
    }
    int buttonWidth = getButtonWidth();
    result.width += buttonWidth;
    return result;
  }

  /////////////////////////////////////////////
  // Event listener registration/deregistration

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the receiver's text is modified, by sending
   * it one of the messages defined in the <code>ModifyListener</code>
   * interface.
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
   * @see ModifyListener
   * @see #removeModifyListener
   */
  public void addModifyListener( ModifyListener listener ) {
    checkWidget();
    if( listener == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.Modify, typedListener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when the receiver's text is modified.
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
   * @see ModifyListener
   * @see #addModifyListener
   */
  public void removeModifyListener( ModifyListener listener ) {
    checkWidget();
    if( listener == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    removeListener( SWT.Modify, listener );
  }

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the control is selected by the user, by sending
   * it one of the messages defined in the <code>SelectionListener</code>
   * interface.
   * <p>
   * <code>widgetSelected</code> is not called for texts.
   * <code>widgetDefaultSelected</code> is typically called when ENTER is pressed in a single-line text.
   * </p>
   *
   * @param listener the listener which should be notified when the control is selected by the user
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
   *
   * @since 1.2
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
   * be notified when the control is selected by the user.
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
   *
   * @since 1.2
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
      return ( T )SpinnerLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  //////////////////
  // Helping methods

  private BoxDimensions getFieldPadding() {
    return getThemeAdapter().getFieldPadding( this );
  }

  private int getButtonWidth() {
    return getThemeAdapter().getButtonWidth( this );
  }

  private SpinnerThemeAdapter getThemeAdapter() {
    return ( SpinnerThemeAdapter )getAdapter( ThemeAdapter.class );
  }

  private static int checkStyle( int style ) {
    /*
    * Even though it is legal to create this widget
    * with scroll bars, they serve no useful purpose
    * because they do not automatically scroll the
    * widget's client area.  The fix is to clear
    * the SWT style.
    */
    return style & ~( SWT.H_SCROLL | SWT.V_SCROLL );
  }
}
