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
 *    Rüdiger Herrmann - bug 335112
 ******************************************************************************/
package org.eclipse.swt.widgets;

import org.eclipse.rap.rwt.internal.lifecycle.ProcessActionRunner;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.theme.CssBoxDimensions;
import org.eclipse.rap.rwt.internal.theme.ThemeAdapter;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.IDisplayAdapter;
import org.eclipse.swt.internal.widgets.IShellAdapter;
import org.eclipse.swt.internal.widgets.MenuHolder;
import org.eclipse.swt.internal.widgets.shellkit.ShellLCA;
import org.eclipse.swt.internal.widgets.shellkit.ShellThemeAdapter;

/**
 * Instances of this class represent the "windows"
 * which the desktop or "window manager" is managing.
 * Instances that do not have a parent (that is, they
 * are built using the constructor, which takes a
 * <code>Display</code> as the argument) are described
 * as <em>top level</em> shells. Instances that do have
 * a parent are described as <em>secondary</em> or
 * <em>dialog</em> shells.
 * <p>
 * Instances are always displayed in one of the maximized,
 * minimized or normal states:
 * <ul>
 * <li>
 * When an instance is marked as <em>maximized</em>, the
 * window manager will typically resize it to fill the
 * entire visible area of the display, and the instance
 * is usually put in a state where it can not be resized
 * (even if it has style <code>RESIZE</code>) until it is
 * no longer maximized.
 * </li><li>
 * When an instance is in the <em>normal</em> state (neither
 * maximized or minimized), its appearance is controlled by
 * the style constants which were specified when it was created
 * and the restrictions of the window manager (see below).
 * </li><li>
 * When an instance has been marked as <em>minimized</em>,
 * its contents (client area) will usually not be visible,
 * and depending on the window manager, it may be
 * "iconified" (that is, replaced on the desktop by a small
 * simplified representation of itself), relocated to a
 * distinguished area of the screen, or hidden. Combinations
 * of these changes are also possible.
 * </li>
 * </ul>
 * </p><p>
 * The <em>modality</em> of an instance may be specified using
 * style bits. The modality style bits are used to determine
 * whether input is blocked for other shells on the display.
 * The <code>PRIMARY_MODAL</code> style allows an instance to block
 * input to its parent. The <code>APPLICATION_MODAL</code> style
 * allows an instance to block input to every other shell in the
 * display. The <code>SYSTEM_MODAL</code> style allows an instance
 * to block input to all shells, including shells belonging to
 * different applications.
 * </p><p>
 * Note: The styles supported by this class are treated
 * as <em>HINT</em>s, since the window manager for the
 * desktop on which the instance is visible has ultimate
 * control over the appearance and behavior of decorations
 * and modality. For example, some window managers only
 * support resizable windows and will always assume the
 * RESIZE style, even if it is not set. In addition, if a
 * modality style is not supported, it is "upgraded" to a
 * more restrictive modality style that is supported. For
 * example, if <code>PRIMARY_MODAL</code> is not supported,
 * it would be upgraded to <code>APPLICATION_MODAL</code>.
 * A modality style may also be "downgraded" to a less
 * restrictive style. For example, most operating systems
 * no longer support <code>SYSTEM_MODAL</code> because
 * it can freeze up the desktop, so this is typically
 * downgraded to <code>APPLICATION_MODAL</code>.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BORDER, CLOSE, MIN, MAX, NO_TRIM, RESIZE, TITLE, ON_TOP, TOOL, SHEET</dd>
 * <dd>APPLICATION_MODAL, MODELESS, PRIMARY_MODAL, SYSTEM_MODAL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Activate, Close, Deactivate, Deiconify, Iconify</dd>
 * </dl>
 * Class <code>SWT</code> provides two "convenience constants"
 * for the most commonly required style combinations:
 * <dl>
 * <dt><code>SHELL_TRIM</code></dt>
 * <dd>
 * the result of combining the constants which are required
 * to produce a typical application top level shell: (that
 * is, <code>CLOSE | TITLE | MIN | MAX | RESIZE</code>)
 * </dd>
 * <dt><code>DIALOG_TRIM</code></dt>
 * <dd>
 * the result of combining the constants which are required
 * to produce a typical application dialog shell: (that
 * is, <code>TITLE | CLOSE | BORDER</code>)
 * </dd>
 * </dl>
 * </p>
 * <p>
 * Note: Only one of the styles APPLICATION_MODAL, MODELESS,
 * PRIMARY_MODAL and SYSTEM_MODAL may be specified.
 * </p><p>
 * IMPORTANT: This class is not intended to be subclassed.
 * </p>
 *
 * @see SWT
 */
public class Shell extends Decorations {

  private static final BoxDimensions ZERO = new BoxDimensions( 0, 0, 0, 0 );
  private static final int MODE_NONE = 0;
  private static final int MODE_MAXIMIZED = 1;
  private static final int MODE_MINIMIZED = 2;
  private static final int MODE_FULLSCREEN = 4;

  private static final int INITIAL_SIZE_PERCENT = 60;
  private static final int MIN_WIDTH_LIMIT = 30;

  private class ShellAdapter implements IShellAdapter {

    @Override
    public Control getActiveControl() {
      return lastActive;
    }

    @Override
    public void setActiveControl( Control control ) {
      Shell.this.setActiveControl( control );
    }

    @Override
    public Rectangle getMenuBounds() {
      return Shell.this.getMenuBounds();
    }

    @Override
    public int getTopTrim() {
      return Shell.this.getTopTrim();
    }

    @Override
    public void setBounds( Rectangle bounds ) {
      Shell.this.setBounds( bounds, false );
    }

    @Override
    public ToolTip[] getToolTips() {
      return Shell.this.getToolTips();
    }

  }

  private Control lastActive;
  private transient IShellAdapter shellAdapter;
  private int alpha;
  private Rectangle savedBounds;
  private int mode;
  private boolean modified;
  private int minWidth;
  private int minHeight;
  private ToolTip[] toolTips;

  private Shell( Display display, Shell parent, int style ) {
    super( checkParent( parent ) );
    if( display != null ) {
      this.display = display;
    } else {
      this.display = Display.getCurrent();
      if( this.display == null ) {
        this.display = Display.getDefault();
      }
    }
    alpha = 0xFF;
    mode = MODE_NONE;
    this.style = checkStyle( style );
    addState( HIDDEN );
    minWidth = MIN_WIDTH_LIMIT;
    minHeight = getMinHeightLimit();
    this.display.addShell( this );
    reskinWidget();
    createWidget();
    setInitialSize();
  }


  /**
   * Constructs a new instance of this class. This is equivalent
   * to calling <code>Shell((Display) null)</code>.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   * </ul>
   *
   * @since 1.3
   */
  public Shell() {
    this( ( Display )null );
  }

  /**
   * Constructs a new instance of this class given only the style value
   * describing its behavior and appearance. This is equivalent to calling
   * <code>Shell((Display) null, style)</code>.
   * <p>
   * The style value is either one of the style constants defined in class
   * <code>SWT</code> which is applicable to instances of this class, or must
   * be built by <em>bitwise OR</em>'ing together (that is, using the
   * <code>int</code> "|" operator) two or more of those <code>SWT</code>
   * style constants. The class description lists the style constants that are
   * applicable to the class. Style bits are also inherited from superclasses.
   * </p>
   *
   * @param style the style of control to construct
   * @exception SWTException
   *                <ul>
   *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *                thread that created the parent</li>
   *                <li>ERROR_INVALID_SUBCLASS - if this class is not an
   *                allowed subclass</li>
   *                </ul>
   * @see SWT#BORDER
   * @see SWT#CLOSE
   * @see SWT#MIN
   * @see SWT#MAX
   * @see SWT#RESIZE
   * @see SWT#TITLE
   * @see SWT#NO_TRIM
   * @see SWT#SHELL_TRIM
   * @see SWT#DIALOG_TRIM
   * <!--@see SWT#MODELESS-->
   * <!--@see SWT#PRIMARY_MODAL-->
   * @see SWT#APPLICATION_MODAL
   * <!--@see SWT#SYSTEM_MODAL-->
   * @see SWT#SHEET
   */
  public Shell( int style ) {
    this( ( Display )null, style );
  }

  /**
   * Constructs a new instance of this class given only the display
   * to create it on. It is created with style <code>SWT.SHELL_TRIM</code>.
   * <p>
   * Note: Currently, null can be passed in for the display argument.
   * This has the effect of creating the shell on the currently active
   * display if there is one. If there is no current display, the
   * shell is created on a "default" display. <b>Passing in null as
   * the display argument is not considered to be good coding style,
   * and may not be supported in a future release of SWT.</b>
   * </p>
   *
   * @param display the display to create the shell on
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   * </ul>
   */
  public Shell( Display display ) {
    this( display, SWT.SHELL_TRIM );
  }

  /**
   * Constructs a new instance of this class given the display
   * to create it on and a style value describing its behavior
   * and appearance.
   * <p>
   * The style value is either one of the style constants defined in
   * class <code>SWT</code> which is applicable to instances of this
   * class, or must be built by <em>bitwise OR</em>'ing together
   * (that is, using the <code>int</code> "|" operator) two or more
   * of those <code>SWT</code> style constants. The class description
   * lists the style constants that are applicable to the class.
   * Style bits are also inherited from superclasses.
   * </p><p>
   * Note: Currently, null can be passed in for the display argument.
   * This has the effect of creating the shell on the currently active
   * display if there is one. If there is no current display, the
   * shell is created on a "default" display. <b>Passing in null as
   * the display argument is not considered to be good coding style,
   * and may not be supported in a future release of SWT.</b>
   * </p>
   *
   * @param display the display to create the shell on
   * @param style the style of control to construct
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   * </ul>
   *
   * @see SWT#BORDER
   * @see SWT#CLOSE
   * @see SWT#MIN
   * @see SWT#MAX
   * @see SWT#RESIZE
   * @see SWT#TITLE
   * @see SWT#NO_TRIM
   * @see SWT#SHELL_TRIM
   * @see SWT#DIALOG_TRIM
   * @see SWT#MODELESS
   * @see SWT#PRIMARY_MODAL
   * @see SWT#APPLICATION_MODAL
   * @see SWT#SYSTEM_MODAL
   * @see SWT#SHEET
   */
  public Shell( Display display, int style ) {
    this( display, null, style );
  }

  /**
   * Constructs a new instance of this class given only its
   * parent. It is created with style <code>SWT.DIALOG_TRIM</code>.
   * <p>
   * Note: Currently, null can be passed in for the parent.
   * This has the effect of creating the shell on the currently active
   * display if there is one. If there is no current display, the
   * shell is created on a "default" display. <b>Passing in null as
   * the parent is not considered to be good coding style,
   * and may not be supported in a future release of SWT.</b>
   * </p>
   *
   * @param parent a shell which will be the parent of the new instance
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the parent is disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   * </ul>
   */
  public Shell( Shell parent ) {
    this( parent, SWT.DIALOG_TRIM );
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
   * </p><p>
   * Note: Currently, null can be passed in for the parent.
   * This has the effect of creating the shell on the currently active
   * display if there is one. If there is no current display, the
   * shell is created on a "default" display. <b>Passing in null as
   * the parent is not considered to be good coding style,
   * and may not be supported in a future release of SWT.</b>
   * </p>
   *
   * @param parent a shell which will be the parent of the new instance
   * @param style the style of control to construct
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the parent is disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   * </ul>
   *
   * @see SWT#BORDER
   * @see SWT#CLOSE
   * @see SWT#MIN
   * @see SWT#MAX
   * @see SWT#RESIZE
   * @see SWT#TITLE
   * @see SWT#NO_TRIM
   * @see SWT#SHELL_TRIM
   * @see SWT#DIALOG_TRIM
   * @see SWT#ON_TOP
   * @see SWT#TOOL
   * @see SWT#SHEET
   * <!--@see SWT#MODELESS-->
   * <!--@see SWT#PRIMARY_MODAL-->
   * @see SWT#APPLICATION_MODAL
   * <!--@see SWT#SYSTEM_MODAL-->
   */
  public Shell( Shell parent, int style ) {
    this( parent != null ? parent.display : null, parent, style );
  }

  @Override
  Shell internalGetShell() {
    return this;
  }

  /**
   * Returns an array containing all shells which are
   * descendents of the receiver.
   * <p>
   * @return the dialog shells
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Shell[] getShells() {
    checkWidget();
    return internalGetShells();
  }

  private Shell[] internalGetShells() {
    int count = 0;
    Shell[] shells = display.getShells();
    for( int i = 0; i < shells.length; i++ ) {
      Control shell = shells[ i ];
      do {
        shell = shell._getParent();
      } while( shell != null && shell != this );
      if( shell == this ) {
        count++;
      }
    }
    int index = 0;
    Shell[] result = new Shell[ count ];
    for( int i = 0; i < shells.length; i++ ) {
      Control shell = shells[ i ];
      do {
        shell = shell._getParent();
      } while( shell != null && shell != this );
      if( shell == this ) {
        result[ index++ ] = shells[ i ];
      }
    }
    return result;
  }

  /**
   * If the receiver is visible, moves it to the top of the
   * drawing order for the display on which it was created
   * (so that all other shells on that display, which are not
   * the receiver's children will be drawn behind it) and asks
   * the window manager to make the shell active
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see Control#moveAbove
   * @see Control#setFocus
   * @see Control#setVisible
   * @see Display#getActiveShell
   * @see Decorations#setDefaultButton(Button)
   * @see Shell#open
   * @see Shell#setActive
   */
  public void setActive() {
    checkWidget();
    if( isVisible() ) {
      display.setActiveShell( this );
    }
  }

  /**
   * If the receiver is visible, moves it to the top of the drawing order for
   * the display on which it was created (so that all other shells on that
   * display, which are not the receiver's children will be drawn behind it) and
   * forces the window manager to make the shell active.
   *
   * @exception SWTException <ul>
   *   <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *   <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   *   created the receiver</li>
   * </ul>
   * @since 1.2
   * @see Control#moveAbove
   * @see Control#setFocus
   * @see Control#setVisible
   * @see Display#getActiveShell
   * @see Decorations#setDefaultButton(Button)
   * @see Shell#open
   * @see Shell#setActive
   */
  public void forceActive() {
    checkWidget();
    setActive();
  }

  /////////////////////
  // Shell measurements

  // TODO [rst] Move to class Decorations, as soon as it exists
  @Override
  public Rectangle getClientArea() {
    checkWidget();
    Rectangle bounds = getBounds();
    BoxDimensions padding = getPadding();
    BoxDimensions titleBarMargin = getTitleBarMargin();
    int hTopTrim;
    hTopTrim = titleBarMargin.top + titleBarMargin.bottom;
    hTopTrim += getTitleBarHeight();
    hTopTrim += getMenuBarHeight();
    BoxDimensions border = getBorder();
    int padingWidth = padding.left + padding.right;
    int paddingHeight = padding.top + padding.bottom;
    int borderWidth = border.left + border.right;
    int borderHeight = border.top + border.bottom;
    return new Rectangle( padding.left,
                          hTopTrim + padding.top,
                          bounds.width - padingWidth - borderWidth,
                          bounds.height - hTopTrim - paddingHeight - borderHeight );
  }

  // TODO [rst] Move to class Decorations, as soon as it exists
  @Override
  public Rectangle computeTrim( int x, int y, int width, int height ) {
    checkWidget();
    int hTopTrim = getTopTrim();
    BoxDimensions padding = getPadding();
    BoxDimensions border = getBorder();
    int paddingWidth = padding.left + padding.right;
    int paddingHeight = padding.top + padding.bottom;
    int borderWidth = border.left + border.right;
    int borderHeight = border.top + border.bottom;
    return new Rectangle( x - padding.left - border.left,
                          y - hTopTrim - padding.top - border.top,
                          width + paddingWidth + borderWidth,
                          height + hTopTrim + paddingHeight + borderHeight );
  }

  private void setInitialSize() {
    int width = display.getBounds().width * INITIAL_SIZE_PERCENT / 100;
    int height = display.getBounds().height * INITIAL_SIZE_PERCENT / 100;
    _setBounds( new Rectangle( 0, 0, width, height ) );
  }

  private int getMinHeightLimit() {
    BoxDimensions border = getBorder();
    BoxDimensions titleBarMargin = getTitleBarMargin();
    int titleBarHeight = getTitleBarHeight();
    return titleBarMargin.top + titleBarMargin.bottom + titleBarHeight + border.top + border.bottom;
  }

  private Rectangle getMenuBounds() {
    Rectangle result = null;
    if( getMenuBar() == null ) {
      result = new Rectangle( 0, 0, 0, 0 );
    } else {
      Rectangle bounds = getBounds();
      int hTop = ( style & SWT.TITLE ) != 0 ? 1 : 0;
      hTop += getTitleBarHeight();
      BoxDimensions padding = getPadding();
      BoxDimensions border = getBorder();
      int paddingWidth = padding.left + padding.right;
      int borderWidth = border.left + border.right;
      result = new Rectangle( padding.left,
                              hTop + padding.top,
                              bounds.width - paddingWidth - borderWidth,
                              getMenuBarHeight() );
    }
    return result;
  }

  @Override
  public int getBorderWidth() {
    return getFullScreen() ? 0 : super.getBorderWidth();
  }

  @Override
  BoxDimensions getBorder() {
    return getFullScreen() ? ZERO : super.getBorder();
  }

  private int getTopTrim() {
    BoxDimensions titleBarMargin = getTitleBarMargin();
    return titleBarMargin.top + titleBarMargin.bottom + getTitleBarHeight() + getMenuBarHeight();
  }

  private int getTitleBarHeight() {
    int result = 0;
    if( !getFullScreen() ) {
      result = getThemeAdapter().getTitleBarHeight( this );
    }
    return result;
  }

  private BoxDimensions getTitleBarMargin() {
    if( !getFullScreen() ) {
      return getThemeAdapter().getTitleBarMargin( this );
    }
    return CssBoxDimensions.ZERO.dimensions;
  }

  private int getMenuBarHeight() {
    return getThemeAdapter().getMenuBarHeight( this );
  }

  private ShellThemeAdapter getThemeAdapter() {
    return ( ShellThemeAdapter )getAdapter( ThemeAdapter.class );
  }

  @Override
  Composite findDeferredControl() {
    return layoutCount > 0 ? this : null;
  }

  @Override
  void updateMode() {
    mode &= ~MODE_MAXIMIZED;
    mode &= ~MODE_MINIMIZED;
    mode &= ~MODE_FULLSCREEN;
  }

  /////////////////////
  // Adaptable override

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == IShellAdapter.class ) {
      if( shellAdapter == null ) {
        shellAdapter = new ShellAdapter();
      }
      return ( T )shellAdapter;
    }
    if( adapter == WidgetLCA.class ) {
      return ( T )ShellLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  /////////////
  // Enablement

  @Override
  public void setEnabled( boolean enabled ) {
    checkWidget();
    if( getEnabled() != enabled ) {
      super.setEnabled( enabled );
      if( enabled ) {
        if( !restoreFocus() ) {
          traverseGroup( true );
        }
      }
    }
  }

  @Override
  public boolean isEnabled() {
    checkWidget();
    return getEnabled();
  }

  /////////////
  // Visibility

  @Override
  public boolean isVisible() {
    checkWidget();
    return getVisible();
  }

  @Override
  public void setVisible( boolean visible ) {
    checkWidget();
    boolean wasVisible = getVisible();
    super.setVisible( visible );
    // Emulate OS behavior: in SWT, a layout is triggered during
    // Shell#setVisible(true)
    if( visible && !wasVisible && !isDisposed() ) {
      changed( getChildren() );
      layout( true, true );
    }
  }

  /**
   * Moves the receiver to the top of the drawing order for
   * the display on which it was created (so that all other
   * shells on that display, which are not the receiver's
   * children will be drawn behind it), marks it visible,
   * sets the focus and asks the window manager to make the
   * shell active.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see Control#moveAbove
   * @see Control#setFocus
   * @see Control#setVisible
   * @see Display#getActiveShell
   * @see Decorations#setDefaultButton(Button)
   * @see Shell#setActive
   * @see Shell#forceActive
   */
  public void open() {
    checkWidget();
    // Order of setActiveShell/bringToTop/setVisible is crucial
    display.setActiveShell( this );
    bringToTop();
    setVisible( true );
    if( !restoreFocus() && !traverseGroup( true ) ) {
      setFocus();
    }
  }

  /**
   * Requests that the window manager close the receiver in
   * the same way it would be closed when the user clicks on
   * the "close box" or performs some other platform specific
   * key or mouse combination that indicates the window
   * should be removed.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see SWT#Close
   * @see Shell#dispose()
   */
  public void close() {
    checkWidget();
    ProcessActionRunner.add( new Runnable() {
      @Override
      public void run() {
        Event event = new Event();
        notifyListeners( SWT.Close, event );
        if( event.doit ) {
          Shell.this.dispose();
        }
      }
    } );
  }

  /**
   * Sets the receiver's alpha value.
   * <p>
   * This operation <!-- requires the operating system's advanced
   * widgets subsystem which --> may not be available on some
   * platforms.
   * </p>
   * @param alpha the alpha value
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.1
   */
  public void setAlpha( int alpha ) {
    checkWidget();
    this.alpha = alpha & 0xFF;
  }

  /**
   * Returns the receiver's alpha value.
   *
   * @return the alpha value
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.1
   */
  public int getAlpha() {
    checkWidget();
    return alpha;
  }

  /**
   * Sets the receiver's modified state as specified by the argument.
   *
   * @param modified the new modified state for the receiver
   *
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public void setModified ( boolean modified ) {
    checkWidget();
    this.modified = modified;
  }

  /**
   * Gets the receiver's modified state.
   *
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public boolean getModified () {
    checkWidget();
    return modified;
  }

  /**
   * Sets the receiver's minimum size to the size specified by the arguments.
   * If the new minimum size is larger than the current size of the receiver,
   * the receiver is resized to the new minimum size.
   *
   * @param width the new minimum width for the receiver
   * @param height the new minimum height for the receiver
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public void setMinimumSize( int width, int height ) {
    checkWidget();
    minWidth = Math.max( MIN_WIDTH_LIMIT, width );
    minHeight = Math.max( getMinHeightLimit(), height );
    Point size = getSize();
    int newWidth = Math.max( size.x, minWidth );
    int newHeight = Math.max( size.y, minHeight );
    if( newWidth != size.x || newHeight != size.y ) {
      setSize( newWidth, newHeight );
    }
  }

  /**
   * Sets the receiver's minimum size to the size specified by the argument.
   * If the new minimum size is larger than the current size of the receiver,
   * the receiver is resized to the new minimum size.
   *
   * @param size the new minimum size for the receiver
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public void setMinimumSize( Point size ) {
    checkWidget();
    if( size == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    setMinimumSize( size.x, size.y );
  }

  /**
   * Returns a point describing the minimum receiver's size. The
   * x coordinate of the result is the minimum width of the receiver.
   * The y coordinate of the result is the minimum height of the
   * receiver.
   *
   * @return the receiver's size
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public Point getMinimumSize() {
    checkWidget();
    return new Point( minWidth,  minHeight );
  }

  @Override
  public void setBounds( Rectangle bounds ) {
    int newWidth = Math.max( bounds.width, minWidth );
    int newHeight = Math.max( bounds.height, minHeight );
    super.setBounds( new Rectangle( bounds.x, bounds.y, newWidth, newHeight ) );
  }

  /**
   * Returns the instance of the ToolBar object representing the tool bar that can appear on the
   * trim of the shell. This will return <code>null</code> if the platform does not support tool
   * bars that not part of the content area of the shell, or if the style of the shell does not
   * support a tool bar.
   * <p>
   *
   * @return a ToolBar object representing the window's tool bar or null.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.4
   */
  public ToolBar getToolBar() {
    checkWidget();
    return null;
  }

  // ///////////////////////////////////////////////
  // Event listener registration and deregistration

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when operations are performed on the receiver,
   * by sending the listener one of the messages defined in the
   * <code>ShellListener</code> interface.
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
   * @see ShellListener
   * @see #removeShellListener
   */
  public void addShellListener( ShellListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.Close, typedListener );
    addListener( SWT.Activate, typedListener );
    addListener( SWT.Deactivate, typedListener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when operations are performed on the receiver.
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
   * @see ShellListener
   * @see #addShellListener
   */
  public void removeShellListener( ShellListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    removeListener( SWT.Close, listener );
    removeListener( SWT.Activate, listener );
    removeListener( SWT.Deactivate, listener );
  }

  ///////////
  // Disposal

  @Override
  void releaseChildren() {
    super.releaseChildren();
    Shell[] dialogShells = internalGetShells();
    for( int i = 0; i < dialogShells.length; i++ ) {
      dialogShells[ i ].dispose();
    }
    Menu[] menus = getAdapter( MenuHolder.class ).getMenus();
    for( Menu menu : menus ) {
      menu.dispose();
    }
    if( toolTips != null ) {
      for( int i = 0; i < toolTips.length; i++ ) {
        if( toolTips[ i ] != null ) {
          toolTips[ i ].dispose();
        }
      }
    }
  }

  @Override
  void releaseParent() {
    // Do not call super.releaseParent()
    // This method would try to remove a child-shell from its ControlHolder
    // but shells are currently not added to the ControlHolder of its parent
    display.removeShell( this );
  }

  ////////////////////////////////////////////////////////////
  // Methods to maintain activeControl and send ActivateEvents

  void setActiveControl( Control activateControl ) {
    Control control = activateControl;
    if( control != null && control.isDisposed() ) {
      control = null;
    }
    if( lastActive != null && lastActive.isDisposed() ) {
      lastActive = null;
    }
    if( lastActive != control ) {
      // Compute the list of controls to be activated and deactivated by finding
      // the first common parent control.
      Control[] activate = ( control == null ) ? new Control[ 0 ] : control.getPath();
      Control[] deactivate = lastActive == null ? new Control[ 0 ] : lastActive.getPath();
      lastActive = control;

      int index = 0;
      int length = Math.min( activate.length, deactivate.length );
      while( index < length && activate[ index ] == deactivate[ index ] ) {
        index++;
      }
      // It is possible (but unlikely), that application code could have
      // destroyed some of the widgets. If this happens, keep processing those
      // widgets that are not disposed.
      for( int i = deactivate.length - 1; i >= index; --i ) {
        if( !deactivate[ i ].isDisposed() ) {
          deactivate[ i ].notifyListeners( SWT.Deactivate, new Event() );
        }
      }
      for( int i = activate.length - 1; i >= index; --i ) {
        if( !activate[ i ].isDisposed() ) {
          activate[ i ].notifyListeners( SWT.Activate, new Event() );
        }
      }
    }
  }

  private void bringToTop() {
    Object adapter = display.getAdapter( IDisplayAdapter.class );
    IDisplayAdapter displayAdapter = ( IDisplayAdapter )adapter;
    displayAdapter.setFocusControl( this, true );
    // When a Shell is opened client-side the widget that is currently focused
    // loses its focus. This is unwanted in the case that the request that
    // opened the Shell sets the focus to some widget after opening the Shell.
    // The fix is to force the DisplayLCA to issue JavaScript that sets the
    // focus on the server-side focused widget.
    displayAdapter.invalidateFocus();
  }

  ////////////////
  // Tab traversal

  @SuppressWarnings( "unused" )
  private boolean traverseGroup( boolean next ) {
    // TODO [rh] fake implementation
    boolean result = false;
    if( getChildren().length > 0 ) {
      result = getChildren()[ 0 ].forceFocus();
    }
    return result;
  }

  //////////////////////
  // minimize / maximize

  // TODO [rst] Move these methods to class Decorations when implemented

  /**
   * Sets the minimized stated of the receiver.
   * If the argument is <code>true</code> causes the receiver
   * to switch to the minimized state, and if the argument is
   * <code>false</code> and the receiver was previously minimized,
   * causes the receiver to switch back to either the maximized
   * or normal states.
   * <!--
   * <p>
   * Note: The result of intermixing calls to <code>setMaximized(true)</code>
   * and <code>setMinimized(true)</code> will vary by platform. Typically,
   * the behavior will match the platform user's expectations, but not
   * always. This should be avoided if possible.
   * </p>
   * -->
   * @param minimized the new maximized state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #setMaximized
   */
  public void setMinimized( boolean minimized ) {
    checkWidget();
    if( minimized ) {
      mode |= MODE_MINIMIZED;
    } else {
      if( ( mode & MODE_MINIMIZED ) != 0 ) {
        setActive();
      }
      mode &= ~MODE_MINIMIZED;
    }
  }

  /**
   * Sets the maximized state of the receiver.
   * If the argument is <code>true</code> causes the receiver
   * to switch to the maximized state, and if the argument is
   * <code>false</code> and the receiver was previously maximized,
   * causes the receiver to switch back to either the minimized
   * or normal states.
   * <!--<p>
   * Note: The result of intermixing calls to <code>setMaximized(true)</code>
   * and <code>setMinimized(true)</code> will vary by platform. Typically,
   * the behavior will match the platform user's expectations, but not
   * always. This should be avoided if possible.
   * </p>
   * -->
   * @param maximized the new maximized state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #setMinimized
   */
  public void setMaximized( boolean maximized ) {
    checkWidget();
    if( ( mode & MODE_FULLSCREEN ) == 0 ) {
      if( maximized ) {
        if( ( mode & MODE_MAXIMIZED ) == 0 ) {
          setActive();
          savedBounds = getBounds();
          setBounds( display.getBounds(), false );
        }
        mode |= MODE_MAXIMIZED;
        mode &= ~MODE_MINIMIZED;
      } else {
        if( ( mode & MODE_MAXIMIZED ) != 0 ) {
          setBounds( savedBounds, false );
        }
        mode &= ~MODE_MAXIMIZED;
      }
    }
  }

  /**
   * Returns <code>true</code> if the receiver is currently
   * minimized, and false otherwise.
   * <p>
   *
   * @return the minimized state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #setMinimized
   */
  public boolean getMinimized() {
    checkWidget();
    return ( mode & MODE_MINIMIZED ) != 0;
  }

  /**
   * Returns <code>true</code> if the receiver is currently
   * maximized, and false otherwise.
   * <p>
   *
   * @return the maximized state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #setMaximized
   */
  public boolean getMaximized() {
    checkWidget();
    return    ( mode & MODE_FULLSCREEN ) == 0
           && ( mode & MODE_MINIMIZED ) == 0
           && ( mode & MODE_MAXIMIZED ) != 0;
  }

  /**
   * Sets the full screen state of the receiver.
   * If the argument is <code>true</code> causes the receiver
   * to switch to the full screen state, and if the argument is
   * <code>false</code> and the receiver was previously switched
   * into full screen state, causes the receiver to switch back
   * to either the maximized or normal states.
   * <p>
   * Note: The result of intermixing calls to <code>setFullScreen(true)</code>,
   * <code>setMaximized(true)</code> and <code>setMinimized(true)</code> will
   * vary by platform. Typically, the behavior will match the platform user's
   * expectations, but not always. This should be avoided if possible.
   * </p>
   *
   * @param fullScreen the new fullscreen state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public void setFullScreen( boolean fullScreen ) {
    checkWidget();
    if( ( ( mode & MODE_FULLSCREEN ) != 0 ) != fullScreen ) {
      if( fullScreen ) {
        setActive();
        if( ( mode & MODE_MAXIMIZED ) == 0 ) {
          savedBounds = getBounds();
        }
        setBounds( display.getBounds(), false );
        mode |= MODE_FULLSCREEN;
        mode &= ~MODE_MINIMIZED;
      } else {
        if( ( mode & MODE_MAXIMIZED ) == 0 ) {
          setBounds( savedBounds, false );
        }
        mode &= ~MODE_FULLSCREEN;
      }
      layout();
    }
  }

  /**
   * Returns <code>true</code> if the receiver is currently
   * in fullscreen state, and false otherwise.
   * <p>
   *
   * @return the fullscreen state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public boolean getFullScreen() {
    checkWidget();
    return ( mode & MODE_FULLSCREEN ) != 0;
  }

  void fixShell( Shell newShell, Control control ) {
    if( newShell != this && control == lastActive ) {
      setActiveControl( null );
    }
  }

  ///////////////////
  // ToolTips support

  void createToolTip( ToolTip toolTip ) {
    int id = 0;
    if( toolTips == null ) {
      toolTips = new ToolTip[ 4 ];
    }
    while( id < toolTips.length && toolTips[ id ] != null ) {
      id++;
    }
    if( id == toolTips.length ) {
      ToolTip[] newToolTips = new ToolTip[ toolTips.length + 4 ];
      System.arraycopy( toolTips, 0, newToolTips, 0, toolTips.length );
      toolTips = newToolTips;
    }
    toolTips[ id ] = toolTip;
  }

  void destroyToolTip( ToolTip toolTip ) {
    boolean found = false;
    for( int i = 0; !found && i < toolTips.length; i++ ) {
      if( toolTips[ i ] == toolTip ) {
        toolTips[ i ] = null;
        found = true;
      }
    }
  }

  private ToolTip[] getToolTips() {
    ToolTip[] result;
    if( toolTips == null ) {
      result = new ToolTip[ 0 ];
    } else {
      int count = 0;
      for( int i = 0; i < toolTips.length; i++ ) {
        if( toolTips[ i ] != null ) {
          count++;
        }
      }
      result = new ToolTip[ count ];
      int index = 0;
      for( int i = 0; i < toolTips.length; i++ ) {
        if( toolTips[ i ] != null ) {
          result[ index ] = toolTips[ i ];
          index++;
        }
      }
    }
    return result;
  }

  private static Shell checkParent( Shell parent ) {
    if( parent != null && parent.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    return parent;
  }

  ///////////////////
  // Skinning support

  @Override
  void reskinChildren( int flags ) {
    Shell[] shells = getShells();
    for( int i = 0; i < shells.length; i++ ) {
      Shell shell = shells[ i ];
      if( shell != null ) {
        shell.reskin( flags );
      }
    }
    super.reskinChildren( flags );
  }
}
