/*******************************************************************************
 * Copyright (c) 2011, 2019 Rüdiger Herrmann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rüdiger Herrmann - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.widgets;

import static org.eclipse.swt.internal.widgets.MarkupUtil.checkMarkupPrecondition;
import static org.eclipse.swt.internal.widgets.MarkupUtil.isMarkupEnabledFor;
import static org.eclipse.swt.internal.widgets.MarkupUtil.MarkupTarget.TEXT;
import static org.eclipse.swt.internal.widgets.MarkupValidator.isValidationDisabledFor;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.widgets.IToolTipAdapter;
import org.eclipse.swt.internal.widgets.MarkupValidator;
import org.eclipse.swt.internal.widgets.tooltipkit.ToolTipLCA;


/**
 * Instances of this class represent popup windows that are used
 * to inform or warn the user.
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BALLOON, ICON_ERROR, ICON_INFORMATION, ICON_WARNING</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * </p><p>
 * Note: Only one of the styles ICON_ERROR, ICON_INFORMATION,
 * and ICON_WARNING may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#tooltips">Tool Tips snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 1.4
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ToolTip extends Widget {

  private final Shell parent;
  private boolean autoHide;
  private boolean visible;
  private String text;
  private String message;
  private int x;
  private int y;
  private transient IToolTipAdapter toolTipAdapter;

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
   * @see SWT#BALLOON
   * @see SWT#ICON_ERROR
   * @see SWT#ICON_INFORMATION
   * @see SWT#ICON_WARNING
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public ToolTip( Shell parent, int style ) {
    super( parent, checkStyle( style ) );
    this.parent = parent;
    autoHide = true;
    text = "";
    message = "";
    Point cursorLocation = display.getCursorLocation();
    x = cursorLocation.x;
    y = cursorLocation.y;
    this.parent.createToolTip( this );
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == IToolTipAdapter.class ) {
      if( toolTipAdapter == null ) {
        toolTipAdapter = new IToolTipAdapter() {
          @Override
          public Point getLocation() {
            return new Point( x, y );
          }
        };
      }
      return ( T )toolTipAdapter;
    } else if( adapter == WidgetLCA.class ) {
      return ( T )ToolTipLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  /**
   * Returns the receiver's parent, which must be a <code>Shell</code>.
   *
   * @return the receiver's parent
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Shell getParent() {
    checkWidget();
    return parent;
  }

  /**
   * Returns <code>true</code> if the receiver is automatically
   * hidden by the platform, and <code>false</code> otherwise.
   *
   * @return the receiver's auto hide state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   */
  public boolean getAutoHide() {
    checkWidget();
    return autoHide;
  }

  /**
   * Makes the receiver hide automatically when <code>true</code>,
   * and remain visible when <code>false</code>.
   *
   * @param autoHide the auto hide state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #getVisible
   * @see #setVisible
   */
  public void setAutoHide( boolean autoHide ) {
    checkWidget();
    this.autoHide = autoHide;
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
  public boolean isVisible() {
    checkWidget();
    return visible;
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
    return visible;
  }

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
    this.visible = visible;
  }

  /**
   * Returns the receiver's text, which will be an empty
   * string if it has never been set.
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
   * Sets the receiver's text.
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
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( isMarkupEnabledFor( this ) && !isValidationDisabledFor( this ) ) {
      MarkupValidator.getInstance().validate( text );
    }
    this.text = text;
  }

  /**
   * Returns the receiver's message, which will be an empty
   * string if it has never been set.
   *
   * @return the receiver's message
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public String getMessage() {
    checkWidget();
    return message;
  }

  /**
   * Sets the receiver's message.
   *
   * @param message the new message
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the text is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setMessage( String message ) {
    checkWidget();
    if( message == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( isMarkupEnabledFor( this ) && !isValidationDisabledFor( this ) ) {
      MarkupValidator.getInstance().validate( message );
    }
    this.message = message;
  }

  /**
   * Sets the location of the receiver, which must be a tooltip,
   * to the point specified by the arguments which are relative
   * to the display.
   * <p>
   * Note that this is different from most widgets where the
   * location of the widget is relative to the parent.
   * </p>
   *
   * @param x the new x coordinate for the receiver
   * @param y the new y coordinate for the receiver
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setLocation( int x, int y ) {
    checkWidget();
    this.x = x;
    this.y = y;
  }

  /**
   * Sets the location of the receiver, which must be a tooltip,
   * to the point specified by the argument which is relative
   * to the display.
   * <p>
   * Note that this is different from most widgets where the
   * location of the widget is relative to the parent.
   * </p><p>
   * Note that the platform window manager ultimately has control
   * over the location of tooltips.
   * </p>
   *
   * @param location the new location for the receiver
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setLocation( Point location ) {
    checkWidget();
    if( location == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    setLocation( location.x, location.y );
  }

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the receiver is selected by the user, by sending
   * it one of the messages defined in the <code>SelectionListener</code>
   * interface.
   * <p>
   * <code>widgetSelected</code> is called when the receiver is selected.
   * <code>widgetDefaultSelected</code> is not called.
   * </p>
   *
   * @param listener the listener which should be notified when the receiver is selected by the user
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
   * be notified when the receiver is selected by the user.
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
    if( !RWT.MARKUP_ENABLED.equals( key ) || !isMarkupEnabledFor( this ) ) {
      checkMarkupPrecondition( key, TEXT, () -> text.isEmpty() );
      super.setData( key, value );
    }
  }

  @Override
  String getNameText() {
    return text;
  }

  @Override
  void releaseParent() {
    super.releaseParent();
    parent.destroyToolTip( this );
  }

  private static int checkStyle( int style ) {
    int result;
    int mask = SWT.ICON_INFORMATION | SWT.ICON_WARNING | SWT.ICON_ERROR;
    if( ( style & mask ) == 0 ) {
      result = style;
    } else {
      result = checkBits( style,
                          SWT.ICON_INFORMATION,
                          SWT.ICON_WARNING,
                          SWT.ICON_ERROR,
                          0,
                          0,
                          0 );
    }
    return result;
  }
}
