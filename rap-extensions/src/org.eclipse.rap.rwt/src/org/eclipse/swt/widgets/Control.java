/*******************************************************************************
 * Copyright (c) 2002, 2019 Innoopract Informationssysteme GmbH and others.
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

import static org.eclipse.swt.internal.widgets.MarkupUtil.checkMarkupPrecondition;
import static org.eclipse.swt.internal.widgets.MarkupUtil.isToolTipMarkupEnabledFor;
import static org.eclipse.swt.internal.widgets.MarkupUtil.MarkupTarget.TOOLTIP;
import static org.eclipse.swt.internal.widgets.MarkupValidator.isValidationDisabledFor;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.lifecycle.RemoteAdapter;
import org.eclipse.rap.rwt.internal.lifecycle.ReparentedControls;
import org.eclipse.rap.rwt.internal.theme.ThemeAdapter;
import org.eclipse.rap.rwt.internal.util.ActiveKeysUtil;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.rap.rwt.theme.ControlThemeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.GestureListener;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TouchListener;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.ControlRemoteAdapter;
import org.eclipse.swt.internal.widgets.IControlAdapter;
import org.eclipse.swt.internal.widgets.IDisplayAdapter;
import org.eclipse.swt.internal.widgets.MarkupValidator;


/**
 * Control is the abstract superclass of all windowed user interface classes.
 * <p>
 * <dl>
 * <dt><b>Styles:</b>
 * <dd>BORDER</dd>
 * <dd>LEFT_TO_RIGHT, RIGHT_TO_LEFT</dd>
 * <dt><b>Events:</b>
 * <dd>FocusIn, FocusOut, Help, KeyDown, KeyUp, MouseDoubleClick, MouseDown, <!-- MouseEnter, -->
 *     <!-- MouseExit, MouseHover, --> MouseUp, <!-- MouseMove,--> Move, <!-- Paint, --> Resize, Traverse,
 *     <!-- DragDetect, --> MenuDetect</dd>
 * </dl>
 * </p><p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the SWT implementation.
 * </p>
 *
 * @since 1.0
 */
public abstract class Control extends Widget implements Drawable {

  private final class ControlAdapter implements IControlAdapter {

    @Override
    public Shell getShell() {
      return internalGetShell();
    }

    @Override
    public int getTabIndex() {
      return tabIndex;
    }

    @Override
    public void setTabIndex( int index ) {
      if( takesFocus() ) {
        getRemoteAdapter().preserveTabIndex( tabIndex );
        tabIndex = index;
      }
    }

    private boolean takesFocus() {
      boolean result = ( getStyle() & SWT.NO_FOCUS ) == 0;
      result &= Control.this.getClass() != Composite.class;
      result &= Control.this.getClass() != ScrolledComposite.class;
      result &= Control.this.getClass() != SashForm.class;
      return result;
    }

    @Override
    public Font getUserFont() {
      return font;
    }

    @Override
    public Color getUserForeground() {
      return foreground;
    }

    @Override
    public Color getUserBackground() {
      return background;
    }

    @Override
    public Image getUserBackgroundImage() {
      return backgroundImage;
    }

    @Override
    public boolean getBackgroundTransparency() {
      return backgroundTransparency;
    }

    @Override
    public boolean isPacked() {
      return packed;
    }

    @Override
    public void clearPacked() {
      packed = false;
    }

    @Override
    /* provides direct access to the internal property in order to speed up preserving */
    public Rectangle getBounds() {
      return bounds;
    }

    @Override
    public void setForeground( Color color ) {
      foreground = color;
    }

    @Override
    public void setBackground( Color color ) {
      background = color;
    }

    @Override
    public void setVisible( boolean visible ) {
      internalSetVisible( visible );
    }

    @Override
    public void setEnabled( boolean enabled ) {
      internalSetEnabled( enabled );
    }

    @Override
    public void setToolTipText( String toolTipText ) {
      Control.this.toolTipText = toolTipText;
    }

    @Override
    public void setCursor( Cursor cursor ) {
      Control.this.cursor = cursor;
    }

  }

  private transient IControlAdapter controlAdapter;
  private Composite parent;
  private int tabIndex;
  private Rectangle bounds;
  private Object layoutData;
  private String toolTipText;
  private Menu menu;
  private Listener menuDisposeListener;
  private Color foreground;
  private Color background;
  private Image backgroundImage;
  private boolean backgroundTransparency;
  private Font font;
  private Cursor cursor;
  private BoxDimensions bufferedPadding;
  private transient Accessible accessible;
  private boolean packed;


  Control( Composite parent ) {
    // prevent instantiation from outside this package; only called by Shell
    // and its super-classes
    this.parent = parent;
    bounds = new Rectangle( 0, 0, 0, 0 );
    tabIndex = -1;
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
   *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *                </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   * </ul>
   *
   * @see SWT#BORDER
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public Control( Composite parent, int style ) {
    super( parent, style );
    this.parent = parent;
    controlAdapter = new ControlAdapter();
    bounds = new Rectangle( 0, 0, 0, 0 );
    tabIndex = -1;
    parent.addChild( this );
    createWidget();
  }

  void createWidget () {
    initState();
    checkOrientation( parent );
    checkMirrored();
    checkBackground();
    updateBackground();
  }

  void initState() {
    // by default let states empty
  }

  /**
   * Returns the receiver's parent, which must be a <code>Composite</code>
   * or null when the receiver is a shell that was created with null or
   * a display for a parent.
   *
   * @return the receiver's parent
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Composite getParent() {
    checkWidget();
    return parent;
  }

  Composite _getParent() {
    return parent;
  }

  /**
   * Returns the receiver's shell. For all controls other than
   * shells, this simply returns the control's nearest ancestor
   * shell. Shells return themselves, even if they are children
   * of other shells.
   *
   * @return the receiver's shell
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #getParent
   */
  public Shell getShell() {
    checkWidget();
    return internalGetShell();
  }

  Shell internalGetShell() {
    return parent.internalGetShell();
  }

  /**
   * Returns the receiver's monitor.
   *
   * @return the receiver's monitor
   *
   * @since 1.2
   */
  public Monitor getMonitor() {
    checkWidget();
    return display.getPrimaryMonitor();
  }

  //////////////
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
    if( hasState( HIDDEN ) != !visible ) {
      preserveState( HIDDEN );
      internalSetVisible( visible );
    }
  }

  private void internalSetVisible( boolean visible ) {
    if( hasState( HIDDEN ) != !visible ) {
      if( visible ) {
        notifyListeners( SWT.Show, null );
      }
      Control control = null;
      boolean fixFocus = false;
      if( !visible ) {
        control = display.getFocusControl();
        fixFocus = isFocusAncestor( control );
      }
      if( visible ) {
        removeState( HIDDEN );
      } else {
        addState( HIDDEN );
      }
      if( !visible ) {
        notifyListeners( SWT.Hide, null );
      }
      if( fixFocus ) {
        fixFocus( control );
      }
    }
  }

  /**
   * Returns <code>true</code> if the receiver is visible and all
   * ancestors up to and including the receiver's nearest ancestor
   * shell are visible. Otherwise, <code>false</code> is returned.
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
   *                </ul>
   */
  public boolean getVisible() {
    checkWidget();
    return !hasState( HIDDEN );
  }

  //////////////
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
   *                </ul>
   */
  public void setEnabled( boolean enabled ) {
    checkWidget();
    if( hasState( DISABLED ) != !enabled ) {
      preserveState( DISABLED );
      internalSetEnabled( enabled );
    }
  }

  private void internalSetEnabled( boolean enabled ) {
    if( hasState( DISABLED ) != !enabled ) {
      /*
       * Feature in Windows.  If the receiver has focus, disabling
       * the receiver causes no window to have focus.  The fix is
       * to assign focus to the first ancestor window that takes
       * focus.  If no window will take focus, set focus to the
       * desktop.
       */
      Control control = null;
      boolean fixFocus = false;
      if( !enabled ) {
        control = display.getFocusControl();
        fixFocus = isFocusAncestor( control );
      }
      if( enabled ) {
        removeState( DISABLED );
      } else {
        addState( DISABLED );
      }
      if( fixFocus ) {
        fixFocus( control );
      }
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
   *                </ul>
   *
   * @see #isEnabled
   */
  public boolean getEnabled() {
    checkWidget();
    return !hasState( DISABLED );
  }

  /**
   * Returns <code>true</code> if the receiver is enabled and all
   * ancestors up to and including the receiver's nearest ancestor
   * shell are enabled.  Otherwise, <code>false</code> is returned.
   * A disabled control is typically not selectable from the user
   * interface and draws with an inactive or "grayed" look.
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

  /////////
  // Colors

  /**
   * Sets the receiver's background color to the color specified
   * by the argument, or to the default system color for the control
   * if the argument is null.
   *
   * @param color the new color (or null)
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setBackground( Color color ) {
    checkWidget();
    if( color != null && color.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    getRemoteAdapter().preserveBackground( background, backgroundTransparency );
    background = color;
    updateBackground();
  }

  /**
   * Returns the receiver's background color.
   *
   * @return the background color
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Color getBackground() {
    checkWidget();
    Control control = findBackgroundControl();
    if( control == null ) {
      control = this;
    }
    Color result = control.background;
    if( result == null ) {
      result = getControlThemeAdapter().getBackground( control );
    }
    Shell shell = control.getShell();
    control = control.parent;
    while( result == null && control != null ) {
      result = control.getBackground();
      control = control == shell ? null : control.parent;
    }
    if( result == null ) {
      // Should never happen as the theming must prevent transparency for
      // shell background colors
      throw new IllegalStateException( "Transparent shell background color" );
    }
    return result;
  }

  /**
   * Sets the receiver's background image to the image specified
   * by the argument, or to the default system color for the control
   * if the argument is null.  The background image is tiled to fill
   * the available space.
   * <p>
   * Note: This operation is a hint and may be overridden by the platform.
   * For example, on Windows the background of a Button cannot be changed.
   * </p>
   * @param image the new image (or null)
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
   *    <!-- <li>ERROR_INVALID_ARGUMENT - if the argument is not a bitmap</li>-->
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.1
   */
  public void setBackgroundImage( Image image ) {
    checkWidget();
    if( image != null && image.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    if( backgroundImage != image ) {
      getRemoteAdapter().preserveBackgroundImage( backgroundImage );
      backgroundImage = image;
    }
  }

  /**
   * Returns the receiver's background image.
   *
   * @return the background image
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.1
   */
  public Image getBackgroundImage() {
    checkWidget();
    Control control = findBackgroundControl();
    if( control == null ) {
      control = this;
    }
    return control.backgroundImage;
  }

  /**
   * Sets the receiver's foreground color to the color specified
   * by the argument, or to the default system color for the control
   * if the argument is null.
   *
   * @param color the new color (or null)
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
   *                </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   */
  public void setForeground( Color color ) {
    checkWidget();
    if( color != null && color.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    getRemoteAdapter().preserveForeground( foreground );
    foreground = color;
  }

  /**
   * Returns the foreground color that the receiver will use to draw.
   *
   * @return the receiver's foreground color
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   */
  public Color getForeground() {
    checkWidget();
    Color result = foreground;
    if( result == null ) {
      result = getControlThemeAdapter().getForeground( this );
    }
    if( result == null ) {
      // Should never happen as the theming must prevent transparency for
      // foreground colors
      throw new IllegalStateException( "Transparent foreground color" );
    }
    return result;
  }

  void updateBackgroundMode() {
    boolean oldState = hasState( PARENT_BACKGROUND );
    checkBackground();
    if( oldState != hasState( PARENT_BACKGROUND ) ) {
      updateBackground();
    }
  }

  /*
   * Checks whether parent background should be applied to this control and and
   * sets PARENT_BACKGROUND state if so.
   */
  // verbatim copy of SWT 3.7.0 GTK
  void checkBackground () {
    Shell shell = getShell ();
    if (this == shell) {
      return;
    }
    removeState( PARENT_BACKGROUND );
    Composite composite = parent;
    do {
      int mode = composite.backgroundMode;
      if (mode != SWT.INHERIT_NONE) {
        if (mode == SWT.INHERIT_DEFAULT) {
          Control control = this;
          do {
            if( !control.hasState( THEME_BACKGROUND ) ) {
              return;
            }
            control = control.parent;
          } while (control != composite);
        }
        addState( PARENT_BACKGROUND );
        return;
      }
      if (composite == shell) {
        break;
      }
      composite = composite._getParent();
    } while (true);
  }

  /**
   * Applies the background according to PARENT_BACKGROUND state.
   */
  private void updateBackground() {
    getRemoteAdapter().preserveBackground( background, backgroundTransparency );
    backgroundTransparency =    background == null
      && backgroundImage == null
      && hasState( PARENT_BACKGROUND );
  }

  Control findBackgroundControl() {
    Control result = null;
    if( background != null || backgroundImage != null ) {
      result = this;
    } else if( hasState( PARENT_BACKGROUND ) ) {
      result = parent.findBackgroundControl();
    }
    return result;
  }

  void checkMirrored() {
    if( ( style & SWT.RIGHT_TO_LEFT ) != 0 ) {
      style |= SWT.MIRRORED;
    }
  }

  /////////
  // Fonts

  /**
   * Sets the font that the receiver will use to paint textual information
   * to the font specified by the argument, or to the default font for that
   * kind of control if the argument is null.
   *
   * @param font the new font (or null)
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
   *                </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   */
  public void setFont( Font font ) {
    checkWidget();
    if( font != null && font.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    getRemoteAdapter().preserveFont( this.font );
    this.font = font;
  }

  /**
   * Returns the font that the receiver will use to paint textual information.
   *
   * @return the receiver's font
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   */
  public Font getFont() {
    checkWidget();
    Font result = font;
    if( result == null ) {
      result = getControlThemeAdapter().getFont( this );
    }
    return result;
  }

  /////////
  // Cursors

  /**
   * Sets the receiver's cursor to the cursor specified by the
   * argument, or to the default cursor for that kind of control
   * if the argument is null.
   * <p>
   * When the mouse pointer passes over a control its appearance
   * is changed to match the control's cursor.
   * </p>
   *
   * @param cursor the new cursor (or null)
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.2
   */
  public void setCursor( Cursor cursor ) {
    checkWidget();
    if( cursor != null && cursor.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    getRemoteAdapter().preserveCursor( this.cursor );
    this.cursor = cursor;
  }

  /**
   * Returns the receiver's cursor, or null if it has not been set.
   * <p>
   * When the mouse pointer passes over a control its appearance
   * is changed to match the control's cursor.
   * </p>
   *
   * @return the receiver's cursor or <code>null</code>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.2
   */
  public Cursor getCursor() {
    checkWidget();
    return cursor;
  }

  //////////////////
  // Focus handling

  /**
   * Causes the receiver to have the <em>keyboard focus</em>,
   * such that all keyboard events will be delivered to it.  Focus
   * reassignment will respect applicable platform constraints.
   *
   * @return <code>true</code> if the control got focus, and <code>false</code> if it was unable to.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   *
   * @see #forceFocus
   */
  public boolean setFocus() {
    checkWidget();
    boolean result = false;
    if( ( style & SWT.NO_FOCUS ) == 0 ) {
      result = forceFocus();
    }
    return result;
  }

  /**
   * Forces the receiver to have the <em>keyboard focus</em>, causing
   * all keyboard events to be delivered to it.
   *
   * @return <code>true</code> if the control got focus, and <code>false</code> if it was unable to.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   *
   * @see #setFocus
   */
  public boolean forceFocus() {
    checkWidget();
    // if (display.focusEvent == SWT.FocusOut) return false;
    Shell shell = getShell(); // was: Decorations shell = menuShell();
    shell.setSavedFocus( this );
    if( !isEnabled() || !isVisible() || !isActive() ) {
      return false;
    }
    if( isFocusControl() ) {
      return true;
    }
    shell.setSavedFocus( null );
    setFocusControl( this ); // was: OS.SetFocus( handle );
    if( isDisposed() ) {
      return false;
    }
    shell.setSavedFocus( this );
    return isFocusControl();
  }

  /**
   * Returns <code>true</code> if the receiver has the user-interface
   * focus, and <code>false</code> otherwise.
   *
   * @return the receiver's focus state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   */
  public boolean isFocusControl() {
    checkWidget();
    return this == getDisplay().getFocusControl();
  }

  boolean setSavedFocus() {
    return forceFocus();
  }

  ///////////////////////////////////////////////////////////////////////
  // Methods to manipulate, transform and query the controls' dimensions

  /**
   * Returns a rectangle describing the receiver's size and location
   * relative to its parent (or its display if its parent is null),
   * unless the receiver is a shell. In this case, the location is
   * relative to the display.
   *
   * @return the receiver's bounding rectangle
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   */
  public Rectangle getBounds() {
    checkWidget();
    return new Rectangle( bounds.x, bounds.y, bounds.width, bounds.height );
  }

  /**
   * Sets the receiver's size and location to the rectangular
   * area specified by the argument. The <code>x</code> and
   * <code>y</code> fields of the rectangle are relative to
   * the receiver's parent (or its display if its parent is null).
   * <p>
   * Note: Attempting to set the width or height of the
   * receiver to a negative number will cause that
   * value to be set to zero instead.
   * </p>
   *
   * @param bounds the new bounds for the receiver
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   */
  public void setBounds( Rectangle bounds ) {
    checkWidget();
    if( bounds == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    setBounds( bounds, true );
  }

  /**
   * Sets the receiver's size and location to the rectangular
   * area specified by the arguments. The <code>x</code> and
   * <code>y</code> arguments are relative to the receiver's
   * parent (or its display if its parent is null), unless
   * the receiver is a shell. In this case, the <code>x</code>
   * and <code>y</code> arguments are relative to the display.
   * <p>
   * Note: Attempting to set the width or height of the
   * receiver to a negative number will cause that
   * value to be set to zero instead.
   * </p>
   *
   * @param x the new x coordinate for the receiver
   * @param y the new y coordinate for the receiver
   * @param width the new width for the receiver
   * @param height the new height for the receiver
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   */
  public void setBounds( int x, int y, int width, int height ) {
    setBounds( new Rectangle( x, y, width, height ) );
  }

  /**
   * Sets the receiver's location to the point specified by
   * the arguments which are relative to the receiver's
   * parent (or its display if its parent is null), unless
   * the receiver is a shell. In this case, the point is
   * relative to the display.
   *
   * @param location the new location for the receiver
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   */
  public void setLocation( Point location ) {
    if( location == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    Rectangle newBounds = new Rectangle( location.x,
      location.y,
      bounds.width,
      bounds.height );
    setBounds( newBounds );
  }

  /**
   * Sets the receiver's location to the point specified by
   * the arguments which are relative to the receiver's
   * parent (or its display if its parent is null), unless
   * the receiver is a shell. In this case, the point is
   * relative to the display.
   *
   * @param x the new x coordinate for the receiver
   * @param y the new y coordinate for the receiver
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   */
  public void setLocation( int x, int y ) {
    setLocation( new Point( x, y ) );
  }

  /**
   * Returns a point describing the receiver's location relative
   * to its parent (or its display if its parent is null), unless
   * the receiver is a shell. In this case, the point is
   * relative to the display.
   *
   * @return the receiver's location
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   */
  public Point getLocation() {
    checkWidget();
    return new Point( bounds.x, bounds.y );
  }

  /**
   * Sets the receiver's size to the point specified by the argument.
   * <p>
   * Note: Attempting to set the width or height of the
   * receiver to a negative number will cause them to be
   * set to zero instead.
   * </p>
   *
   * @param size the new size for the receiver
   *
   * @exception IllegalArgumentException <ul>
   *                <li>ERROR_NULL_ARGUMENT - if the point is null</li>
   *                </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   */
  public void setSize( Point size ) {
    if( size == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    setBounds( new Rectangle( bounds.x, bounds.y, size.x, size.y ) );
  }

  /**
   * Sets the receiver's size to the point specified by the arguments.
   * <p>
   * Note: Attempting to set the width or height of the
   * receiver to a negative number will cause that
   * value to be set to zero instead.
   * </p>
   *
   * @param width the new width for the receiver
   * @param height the new height for the receiver
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   */
  public void setSize( int width, int height ) {
    setSize( new Point( width, height ) );
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
   *                </ul>
   */
  public Point getSize() {
    checkWidget();
    return new Point( bounds.width, bounds.height );
  }

  /**
   * Returns the preferred size of the receiver.
   * <p>
   * The <em>preferred size</em> of a control is the size that it would
   * best be displayed at. The width hint and height hint arguments
   * allow the caller to ask a control questions such as "Given a particular
   * width, how high does the control need to be to show all of the contents?"
   * To indicate that the caller does not wish to constrain a particular
   * dimension, the constant <code>SWT.DEFAULT</code> is passed for the hint.
   * </p>
   *
   * @param wHint the width hint (can be <code>SWT.DEFAULT</code>)
   * @param hHint the height hint (can be <code>SWT.DEFAULT</code>)
   * @return the preferred size of the control
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   *
   * @see Layout
   * @see #getBorderWidth
   * @see #getBounds
   * @see #getSize
   * @see #pack(boolean)
   * @see "computeTrim, getClientArea for controls that implement them"
   */
  public Point computeSize( int wHint, int hHint ) {
    return computeSize( wHint, hHint, true );
  }

  /**
   * Returns the preferred size of the receiver.
   * <p>
   * The <em>preferred size</em> of a control is the size that it would
   * best be displayed at. The width hint and height hint arguments
   * allow the caller to ask a control questions such as "Given a particular
   * width, how high does the control need to be to show all of the contents?"
   * To indicate that the caller does not wish to constrain a particular
   * dimension, the constant <code>SWT.DEFAULT</code> is passed for the hint.
   * </p><p>
   * If the changed flag is <code>true</code>, it indicates that the receiver's
   * <em>contents</em> have changed, therefore any caches that a layout manager
   * containing the control may have been keeping need to be flushed. When the
   * control is resized, the changed flag will be <code>false</code>, so layout
   * manager caches can be retained.
   * </p>
   *
   * @param wHint the width hint (can be <code>SWT.DEFAULT</code>)
   * @param hHint the height hint (can be <code>SWT.DEFAULT</code>)
   * @param changed <code>true</code> if the control's contents have changed, and <code>false</code> otherwise
   * @return the preferred size of the control.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   *
   * @see Layout
   * @see #getBorderWidth
   * @see #getBounds
   * @see #getSize
   * @see #pack(boolean)
   * @see "computeTrim, getClientArea for controls that implement them"
   */
  public Point computeSize( int wHint, int hHint, boolean changed ) {
    checkWidget();
    int width = DEFAULT_WIDTH;
    int height = DEFAULT_HEIGHT;
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

  /**
   * Causes the receiver to be resized to its preferred size.
   * For a composite, this involves computing the preferred size
   * from its layout, if there is one.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   *
   * @see #computeSize(int, int, boolean)
   */
  public void pack() {
    checkWidget();
    pack( true );
  }

  /**
   * Causes the receiver to be resized to its preferred size.
   * For a composite, this involves computing the preferred size
   * from its layout, if there is one.
   * <p>
   * If the changed flag is <code>true</code>, it indicates that the receiver's
   * <em>contents</em> have changed, therefore any caches that a layout manager
   * containing the control may have been keeping need to be flushed. When the
   * control is resized, the changed flag will be <code>false</code>, so layout
   * manager caches can be retained.
   * </p>
   *
   * @param changed whether or not the receiver's contents have changed
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   *
   * @see #computeSize(int, int, boolean)
   */
  public void pack( boolean changed ) {
    checkWidget();
    setSize( computeSize( SWT.DEFAULT, SWT.DEFAULT, changed ) );
    packed = true;
  }

  /**
   * Returns the receiver's border width.
   * Note: When the theming defines different border widths for the four edges, this method returns
   * the maximum border width.
   *
   * @return the border width
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   */
  public int getBorderWidth() {
    checkWidget();
    BoxDimensions border = getBorder();
    int max1 = Math.max( border.left, border.right );
    int max2 = Math.max( border.top, border.bottom );
    return Math.max( max1, max2 );
  }

  BoxDimensions getBorder() {
    return getControlThemeAdapter().getBorder( this );
  }

  BoxDimensions getPadding() {
    if( bufferedPadding == null ) {
      bufferedPadding = getControlThemeAdapter().getPadding( this );
    }
    return bufferedPadding;
  }

  private ControlThemeAdapter getControlThemeAdapter() {
    return getAdapter( ControlThemeAdapter.class );
  }

  /**
   * Returns a point which is the result of converting the
   * argument, which is specified in display relative coordinates,
   * to coordinates relative to the receiver.
   * <p>
   * @param x the x coordinate to be translated
   * @param y the y coordinate to be translated
   * @return the translated coordinates
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   */
  public Point toControl( int x, int y ) {
    checkWidget();
    return getDisplay().map( null, this, x, y );
  }

  /**
   * Returns a point which is the result of converting the
   * argument, which is specified in display relative coordinates,
   * to coordinates relative to the receiver.
   * <p>
   * @param point the point to be translated (must not be null)
   * @return the translated coordinates
   *
   * @exception IllegalArgumentException <ul>
   *                <li>ERROR_NULL_ARGUMENT - if the point is null</li>
   *                </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   */
  public Point toControl( Point point ) {
    checkWidget();
    if( point == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    return toControl( point.x, point.y );
  }

  /**
   * Returns a point which is the result of converting the
   * argument, which is specified in coordinates relative to
   * the receiver, to display relative coordinates.
   * <p>
   * @param x the x coordinate to be translated
   * @param y the y coordinate to be translated
   * @return the translated coordinates
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Point toDisplay( int x, int y ) {
    checkWidget();
    return getDisplay().map( this, null, x, y );
  }

  /**
   * Returns a point which is the result of converting the
   * argument, which is specified in coordinates relative to
   * the receiver, to display relative coordinates.
   * <p>
   * @param point the point to be translated (must not be null)
   * @return the translated coordinates
   *
   * @exception IllegalArgumentException <ul>
   *                <li>ERROR_NULL_ARGUMENT - if the point is null</li>
   *                </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   */
  public Point toDisplay( Point point ) {
    checkWidget();
    if( point == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    return toDisplay( point.x, point.y );
  }

  ///////////////////////////
  // Layout related methods

  /**
   * Returns layout data which is associated with the receiver.
   *
   * @return the receiver's layout data
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   */
  public Object getLayoutData() {
    checkWidget();
    return layoutData;
  }

  /**
   * Sets the layout data associated with the receiver to the argument.
   *
   * @param layoutData the new layout data for the receiver.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   */
  public void setLayoutData( Object layoutData ) {
    checkWidget();
    this.layoutData = layoutData;
  }

  @SuppressWarnings( "unused" )
  void markLayout( boolean changed, boolean all ) {
    /* Do nothing */
  }

  @SuppressWarnings( "unused" )
  void updateLayout( boolean resize, boolean all ) {
    /* Do nothing */
  }

  /////////////////////
  // ToolTip operations

  /**
   * Sets the receiver's tool tip text to the argument, which
   * may be null indicating that no tool tip text should be shown.
   *
   * @param toolTipText the new tool tip text (or null)
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setToolTipText( String toolTipText ) {
    checkWidget();
    if(    toolTipText != null
      && isToolTipMarkupEnabledFor( this )
      && !isValidationDisabledFor( this ) )
    {
      MarkupValidator.getInstance().validate( toolTipText );
    }
    getRemoteAdapter().preserveToolTipText( this.toolTipText );
    this.toolTipText = toolTipText;
  }

  /**
   * Returns the receiver's tool tip text, or null if it has
   * not been set.
   *
   * @return the receiver's tool tip text
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public String getToolTipText() {
    checkWidget();
    return toolTipText;
  }

  ///////////////////
  // Menu operations

  /**
   * Sets the receiver's pop up menu to the argument.
   * All controls may optionally have a pop up
   * menu that is displayed when the user requests one for
   * the control. The sequence of key strokes, button presses
   * and/or button releases that are used to request a pop up
   * menu is platform specific.
   * <p>
   * Note: Disposing of a control that has a pop up menu will
   * dispose of the menu.  To avoid this behavior, set the
   * menu to null before the control is disposed.
   * </p>
   *
   * @param menu the new pop up menu
   *
   * @exception IllegalArgumentException <ul>
   *                <li>ERROR_MENU_NOT_POP_UP - the menu is not a pop up menu</li>
   *    <li>ERROR_INVALID_PARENT - if the menu is not in the same widget tree</li>
   *                <li>ERROR_INVALID_ARGUMENT - if the menu has been disposed</li>
   *                </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   */
  public void setMenu( Menu menu ) {
    checkWidget();
    if( this.menu != menu ) {
      if( menu != null ) {
        if( menu.isDisposed() ) {
          SWT.error( SWT.ERROR_INVALID_ARGUMENT );
        }
        if( ( menu.getStyle() & SWT.POP_UP ) == 0 ) {
          SWT.error( SWT.ERROR_MENU_NOT_POP_UP );
        }
        if( menu.getParent() != getShell() ) {
          SWT.error( SWT.ERROR_INVALID_PARENT );
        }
      }
      removeMenuDisposeListener();
      _setMenu( menu );
      addMenuDisposeListener();
    }
  }

  /**
   * Returns the receiver's pop up menu if it has one, or null
   * if it does not. All controls may optionally have a pop up
   * menu that is displayed when the user requests one for
   * the control. The sequence of key strokes, button presses
   * and/or button releases that are used to request a pop up
   * menu is platform specific.
   *
   * @return the receiver's menu
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   */
  public Menu getMenu() {
    checkWidget();
    return menu;
  }

  ///////////
  // Z-Order

  /**
   * Moves the receiver above the specified control in the
   * drawing order. If the argument is null, then the receiver
   * is moved to the top of the drawing order. The control at
   * the top of the drawing order will not be covered by other
   * controls even if they occupy intersecting areas.
   *
   * @param control the sibling control (or null)
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the control has been disposed</li>
   *                </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   *
   * @see Control#moveBelow
   * @see Composite#getChildren
   */
  public void moveAbove( Control control ) {
    checkWidget();
    if( control != null && control.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    if( this instanceof Shell ) {
      // TODO: add support for Shell reordering
    } else if( control == null || control.parent == parent && control != this ) {
      parent.moveAbove( this, control );
    }
  }

  /**
   * Moves the receiver below the specified control in the
   * drawing order. If the argument is null, then the receiver
   * is moved to the bottom of the drawing order. The control at
   * the bottom of the drawing order will be covered by all other
   * controls which occupy intersecting areas.
   *
   * @param control the sibling control (or null)
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the control has been disposed</li>
   *                </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   *
   * @see Control#moveAbove
   * @see Composite#getChildren
   */
  public void moveBelow( Control control ) {
    checkWidget();
    if( control != null && control.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    if( this instanceof Shell ) {
      // TODO: add support for Shell reordering
    } else if( control == null || control.parent == parent && control != this ) {
      parent.moveBelow( this, control );
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    T result = null;
    if( adapter == IControlAdapter.class ) {
      if( controlAdapter == null ) {
        controlAdapter = new ControlAdapter();
      }
      result = ( T )controlAdapter;
    } else if( adapter == ControlThemeAdapter.class ) {
      result = ( T )super.getAdapter( ThemeAdapter.class );
    } else {
      result = super.getAdapter( adapter );
    }
    return result;
  }

  @Override
  RemoteAdapter createRemoteAdapter( Widget parent, String id ) {
    ControlRemoteAdapter remoteAdapter = new ControlRemoteAdapter( id );
    remoteAdapter.setParent( parent );
    return remoteAdapter;
  }

  //////////////////////////////////
  // Methods to add/remove listener

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the control is moved or resized, by sending
   * it one of the messages defined in the <code>ControlListener</code>
   * interface.
   *
   * @param listener the listener which should be notified
   *
   * @exception IllegalArgumentException <ul>
   *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   *                </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   *
   * @see ControlListener
   * @see #removeControlListener
   */
  public void addControlListener( ControlListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.Move, typedListener );
    addListener( SWT.Resize, typedListener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when the control is moved or resized.
   *
   * @param listener the listener which should no longer be notified
   *
   * @exception IllegalArgumentException <ul>
   *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   *                </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   *
   * @see ControlListener
   * @see #addControlListener
   */
  public void removeControlListener( ControlListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    removeListener( SWT.Move, listener );
    removeListener( SWT.Resize, listener );
  }

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when mouse buttons are pressed and released, by sending
   * it one of the messages defined in the <code>MouseListener</code>
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
   * @see MouseListener
   * @see #removeMouseListener
   *
   * @since 1.1
   */
  public void addMouseListener( MouseListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.MouseDown, typedListener );
    addListener( SWT.MouseUp, typedListener );
    addListener( SWT.MouseDoubleClick, typedListener );
  }

  /**
   * Empty stub for the sake of single sourcing
   * @param listener
   */
  public void addMouseTrackListener ( MouseTrackListener listener ) {
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when mouse buttons are pressed and released.
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
   * @see MouseListener
   * @see #addMouseListener
   *
   * @since 1.1
   */
  public void removeMouseListener( MouseListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    removeListener( SWT.MouseDown, listener );
    removeListener( SWT.MouseUp, listener );
    removeListener( SWT.MouseDoubleClick, listener );
  }

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when keys are pressed and released on the system keyboard, by sending
   * it one of the messages defined in the <code>KeyListener</code>
   * interface.
   * <!--
   * TODO [rh] investigate whether this statements is true in RWT as well
   * <p>
   * When a key listener is added to a control, the control
   * will take part in widget traversal.  By default, all
   * traversal keys (such as the tab key and so on) are
   * delivered to the control.  In order for a control to take
   * part in traversal, it should listen for traversal events.
   * Otherwise, the user can traverse into a control but not
   * out.  Note that native controls such as table and tree
   * implement key traversal in the operating system.  It is
   * not necessary to add traversal listeners for these controls,
   * unless you want to override the default traversal.
   * </p>
   * -->
   * <!-- RAP specific -->
   * <p>
   * <strong>Note:</strong> the key events in RWT are not meant for
   * general purpose.
   * </p>
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
   * @see KeyListener
   * @see #removeKeyListener
   *
   * @since 1.2
   */
  public void addKeyListener( KeyListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.KeyUp, typedListener );
    addListener( SWT.KeyDown, typedListener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when keys are pressed and released on the system keyboard.
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
   * @see KeyListener
   * @see #addKeyListener
   *
   * @since 1.2
   */
  public void removeKeyListener( KeyListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    removeListener( SWT.KeyUp, listener );
    removeListener( SWT.KeyDown, listener );
  }

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when traversal events occur, by sending it
   * one of the messages defined in the <code>TraverseListener</code>
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
   * @see TraverseListener
   * @see #removeTraverseListener
   *
   * @since 1.2
   */
  public void addTraverseListener( TraverseListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.Traverse, typedListener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when traversal events occur.
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
   * @see TraverseListener
   * @see #addTraverseListener
   *
   * @since 1.2
   */
  public void removeTraverseListener( TraverseListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    removeListener( SWT.Traverse, listener );
  }

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the control gains or loses focus, by sending
   * it one of the messages defined in the <code>FocusListener</code>
   * interface.
   *
   * @param listener the listener which should be notified
   *
   * @exception IllegalArgumentException <ul>
   *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   *                </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   *
   * @see FocusListener
   * @see #removeFocusListener
   */
  public void addFocusListener( FocusListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.FocusIn, typedListener );
    addListener( SWT.FocusOut, typedListener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when the control gains or loses focus.
   *
   * @param listener the listener which should no longer be notified
   *
   * @exception IllegalArgumentException <ul>
   *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   *                </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   *
   * @see FocusListener
   * @see #addFocusListener
   */
  public void removeFocusListener( FocusListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    removeListener( SWT.FocusIn, listener );
    removeListener( SWT.FocusOut, listener );
  }

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when help events are generated for the control,
   * by sending it one of the messages defined in the
   * <code>HelpListener</code> interface.
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
   * @see HelpListener
   * @see #removeHelpListener
   * @since 1.3
   */
  public void addHelpListener( HelpListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.Help, typedListener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when the help events are generated for the control.
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
   * @see HelpListener
   * @see #addHelpListener
   * @since 1.3
   */
  public void removeHelpListener( HelpListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    removeListener( SWT.Help, listener );
  }

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when a drag gesture occurs, by sending it
   * one of the messages defined in the <code>DragDetectListener</code>
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
   * @see DragDetectListener
   * @see #removeDragDetectListener
   *
   * @since 1.3
   */
  public void addDragDetectListener( DragDetectListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.DragDetect, typedListener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when a drag gesture occurs.
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
   * @see DragDetectListener
   * @see #addDragDetectListener
   *
   * @since 1.3
   */
  public void removeDragDetectListener( DragDetectListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    removeListener( SWT.DragDetect, listener );
  }

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the platform-specific context menu trigger
   * has occurred, by sending it one of the messages defined in
   * the <code>MenuDetectListener</code> interface.
   *
   * @param listener the listener which should be notified
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   *                                      created the receiver</li>
   * </ul>
   *
   * @see MenuDetectListener
   * @see #removeMenuDetectListener
   *
   * @since 1.3
   */
  public void addMenuDetectListener( MenuDetectListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.MenuDetect, typedListener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when the platform-specific context menu trigger has
   * occurred.
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
   * @see MenuDetectListener
   * @see #addMenuDetectListener
   *
   * @since 1.3
   */
  public void removeMenuDetectListener( MenuDetectListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    removeListener( SWT.MenuDetect, listener );
  }

  /**
   * Requests that this control and all of its ancestors be repositioned
   * their layouts at the earliest opportunity. This should be invoked after
   * modifying the control in order to inform any dependent layouts of
   * the change.
   * <p>
   * The control will not be repositioned synchronously. This method is
   * fast-running and only marks the control for future participation in
   * a deferred layout.
   * <p>
   * Invoking this method multiple times before the layout occurs is an
   * inexpensive no-op.
   *
   * @since 3.1
   */
  public void requestLayout() {
    getShell().layout( new Control[] { this }, SWT.DEFER );
  }

  ////////////////
  // drawing (Note that we can't really force a redraw. This is just a
  // fake for event notifications that come on OS systems with redraws)

  /**
   * If the argument is <code>false</code>, causes subsequent drawing
   * operations in the receiver to be ignored. No drawing of any kind
   * can occur in the receiver until the flag is set to true.
   * Graphics operations that occurred while the flag was
   * <code>false</code> are lost. When the flag is set to <code>true</code>,
   * the entire widget is marked as needing to be redrawn.  Nested calls
   * to this method are stacked.
   * <p>
   * Note: This operation is a hint and may not be supported on some
   * platforms or for some widgets.
   * </p>
   * <p>
   * Note: With RAP we can't really force a redraw. This is just a
   *       fake to enable event notifications that come on OS systems
   *       with redraws.
   * </p>
   *
   * @param redraw the new redraw state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setRedraw( boolean redraw ) {
    checkWidget();
    internalSetRedraw( redraw );
  }

  /**
   * Causes the entire bounds of the receiver to be marked
   * as needing to be redrawn.
   *
   * <p>
   * Note: With RAP we can't really force a redraw. This is just a
   *       fake to enable event notifications that come on OS systems
   *       with redraws.
   * </p>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void redraw() {
    checkWidget();
    internalSetRedraw( true );
  }

  /**
   * Causes the rectangular area of the receiver specified by
   * the arguments to be marked as needing to be redrawn.
   * The next time a paint request is processed, that area of
   * the receiver will be painted, including the background.
   * If the <code>all</code> flag is <code>true</code>, any
   * children of the receiver which intersect with the specified
   * area will also paint their intersecting areas. If the
   * <code>all</code> flag is <code>false</code>, the children
   * will not be painted.
   *
   * @param x the x coordinate of the area to draw
   * @param y the y coordinate of the area to draw
   * @param width the width of the area to draw
   * @param height the height of the area to draw
   * @param all <code>true</code> if children should redraw, and <code>false</code> otherwise
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #update()
   * @see PaintListener
   * @see SWT#NO_BACKGROUND
   * @see SWT#NO_REDRAW_RESIZE
   * @see SWT#NO_MERGE_PAINTS
   * @see SWT#DOUBLE_BUFFERED
   * @since 1.3
   */
  //  * @see SWT#Paint
  public void redraw( int x, int y, int width, int height, boolean all ) {
    checkWidget();
    if( width > 0 && height > 0 ) {
      internalSetRedraw( true, x, y, width, height );
    }
  }

  void internalSetRedraw( boolean redraw ) {
    display.redrawControl( this, redraw );
  }

  @SuppressWarnings( "unused" )
  void internalSetRedraw( boolean redraw, int x, int y, int width, int height ) {
    display.redrawControl( this, redraw );
  }

  /**
   * Forces all outstanding paint requests for the widget
   * to be processed before this method returns. If there
   * are no outstanding paint request, this method does
   * nothing.
   * <p>
   * Note: This method does not cause a redraw.
   * </p>
   *
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public void update() {
    checkWidget();
  }

  /**
   * Changes the parent of the widget to be the one provided if
   * the underlying operating system supports this feature.
   * Returns <code>true</code> if the parent is successfully changed.
   *
   * @param parent the new parent for the control.
   * @return <code>true</code> if the parent is changed and <code>false</code> otherwise.
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
   *    <li>ERROR_NULL_ARGUMENT - if the parent is <code>null</code></li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *  </ul>
   *
   *  @since 1.3
   */
  public boolean setParent( Composite parent ) {
    checkWidget();
    if( parent == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( parent.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    if( !isReparentable() ) {
      return false;
    }
    if( this.parent != parent ) {
      releaseParent();
      Shell newShell = parent.getShell();
      Shell oldShell = getShell();
      Decorations newDecorations = parent.menuShell();
      Decorations oldDecorations = menuShell();
      if( oldShell != newShell || oldDecorations != newDecorations ) {
        fixChildren( newShell, oldShell, newDecorations, oldDecorations );
      }
      getRemoteAdapter().preserveParent( this.parent );
      ReparentedControls.add( this );
      this.parent = parent;
      getRemoteAdapter().setParent( parent );
      parent.addChild( this );
    }
    return true;
  }

  /**
   * Returns <code>true</code> if the underlying operating
   * system supports this reparenting, otherwise <code>false</code>
   *
   * @return <code>true</code> if the widget can be reparented, otherwise <code>false</code>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public boolean isReparentable() {
    checkWidget();
    return true;
  }

  /**
   * Sets the orientation of the receiver, which must be one
   * of the constants <code>SWT.LEFT_TO_RIGHT</code> or <code>SWT.RIGHT_TO_LEFT</code>.
   *
   * @param orientation new orientation style
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.4
   */
  public void setOrientation( int orientation ) {
    checkWidget();
    int flags = SWT.RIGHT_TO_LEFT | SWT.LEFT_TO_RIGHT;
    if( ( orientation & flags ) == 0 || ( orientation & flags ) == flags ) {
      return;
    }
    getRemoteAdapter().preserveOrientation( style & flags );
    style &= ~SWT.MIRRORED;
    style &= ~flags;
    style |= orientation & flags;
    updateOrientation();
    checkMirrored();
  }

  /**
   * Returns the orientation of the receiver, which will be one of the
   * constants <code>SWT.LEFT_TO_RIGHT</code> or <code>SWT.RIGHT_TO_LEFT</code>.
   *
   * @return the orientation style
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.4
   */
  public int getOrientation() {
    checkWidget();
    return style & ( SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT );
  }

  void updateOrientation() {
    // subclasses may override
  }

  ////////////////
  // Accessibility

  /**
   * Returns the accessible object for the receiver.
   * <p>
   * If this is the first time this object is requested,
   * then the object is created and returned. The object
   * returned by getAccessible() does not need to be disposed.
   * </p>
   *
   * @return the accessible object
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see Accessible#addAccessibleListener
   * @see Accessible#addAccessibleControlListener
   *
   * @since 1.4
   */
  public Accessible getAccessible() {
    checkWidget();
    if( accessible == null ) {
      accessible = Accessible.internal_new_Accessible( this );
    }
    return accessible;
  }

  ////////////////////
  // Touch and Gesture

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when gesture events are generated for the control,
   * by sending it one of the messages defined in the
   * <code>GestureListener</code> interface.
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
   * @see GestureListener
   * @see #removeGestureListener
   *
   * @since 1.4
   */
  public void addGestureListener( GestureListener listener ) {
    checkWidget();
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when gesture events are generated for the control.
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
   * @see GestureListener
   * @see #addGestureListener
   *
   * @since 1.4
   */
  public void removeGestureListener( GestureListener listener ) {
    checkWidget();
  }

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when touch events occur, by sending it
   * one of the messages defined in the <code>TouchListener</code>
   * interface.
   * <p>
   * NOTE: You must also call <code>setTouchEnabled</code> to notify the
   * windowing toolkit that you want touch events to be generated.
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
   * @see TouchListener
   * @see #removeTouchListener
   *
   * @since 1.4
   */
  public void addTouchListener( TouchListener listener ) {
    checkWidget();
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when touch events occur.
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
   * @see TouchListener
   * @see #addTouchListener
   *
   * @since 1.4
   */
  public void removeTouchListener( TouchListener listener ) {
    checkWidget();
  }

  /**
   * Sets whether the receiver should accept touch events. By default, a Control does not accept
   * touch events. No error or exception is thrown if the underlying hardware does not support touch
   * input.
   *
   * @param enabled the new touch-enabled state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *
   * @since 1.4
   */
  public void setTouchEnabled( boolean enabled ) {
    checkWidget();
  }

  /**
   * Returns <code>true</code> if this control is receiving OS-level touch events,
   * otherwise <code>false</code>
   * <p>
   * Note that this method will return false if the current platform does not support touch-based
   * input. If this method does return true, gesture events will not be sent to the control.
   *
   * @return <code>true</code> if the widget is currently receiving touch events; <code>false</code>
   * otherwise.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.4
   */
  public boolean getTouchEnabled() {
    checkWidget();
    return false;
  }

  @Override
  public void setData( String key, Object value ) {
    if( RWT.TOOLTIP_MARKUP_ENABLED.equals( key ) && isToolTipMarkupEnabledFor( this ) ) {
      // MARKUP_ENABLED cannot be changed once it is set
      return;
    }
    if( RWT.ACTIVE_KEYS.equals( key ) ) {
      if( value != null && !( value instanceof String[] ) ) {
        String mesg = "Illegal value for RWT.ACTIVE_KEYS in data, must be a string array";
        throw new IllegalArgumentException( mesg );
      }
      ActiveKeysUtil.preserveActiveKeys( this );
    }
    if( RWT.CANCEL_KEYS.equals( key ) ) {
      if( value != null && !( value instanceof String[] ) ) {
        String mesg = "Illegal value for RWT.CANCEL_KEYS in data, must be a string array";
        throw new IllegalArgumentException( mesg );
      }
      ActiveKeysUtil.preserveCancelKeys( this );
    }
    if( RWT.CUSTOM_VARIANT.equals( key ) ) {
      bufferedPadding = null;
    }
    checkMarkupPrecondition( key, TOOLTIP, () -> toolTipText == null );
    super.setData( key, value );
  }

  ////////////
  // Disposal

  @Override
  void releaseParent() {
    if( parent != null ) {
      parent.removeChild( this );
    }
  }

  @Override
  void releaseWidget() {
    if( menu != null ) {
      removeMenuDisposeListener();
      menu.dispose();
      menu = null;
    }
    Shell shell = internalGetShell();
    if( display.getFocusControl() == this ) {
      Control focusControl = parent;
      while( focusControl != null && focusControl.isInDispose() ) {
        focusControl = focusControl.getParent();
      }
      if( focusControl != null && focusControl.internalGetShell() != shell ) {
        focusControl = null;
      }
      setFocusControl( focusControl );
    }
    if( shell.getSavedFocus() == this ) {
      shell.setSavedFocus( null );
    }
    internalSetRedraw( false );
    if( accessible != null ) {
      accessible.internal_dispose_Accessible();
    }
    accessible = null;
    super.releaseWidget();
  }

  /////////////
  // Tab order

  boolean isTabGroup() {
    Control[] tabList = parent._getTabList();
    if( tabList != null ) {
      for( int i = 0; i < tabList.length; i++ ) {
        if( tabList[ i ] == this ) {
          return true;
        }
      }
    }
    return false;
  }

  ////////////////////////////////
  // Helping methods for setBounds

  void setBounds( Rectangle bounds, boolean updateMode ) {
    Point oldLocation = getLocation();
    Point oldSize = getSize();
    _setBounds( new Rectangle( bounds.x, bounds.y, bounds.width, bounds.height ) );
    if( updateMode ) {
      updateMode();
    }
    clearPacked( oldSize );
    notifyMove( oldLocation );
    notifyResize( oldSize );
  }

  void _setBounds( Rectangle rectangle ) {
    getRemoteAdapter().preserveBounds( bounds );
    bounds = rectangle;
    bounds.width = Math.max( 0, bounds.width );
    bounds.height = Math.max( 0, bounds.height );
  }

  private void _setMenu( Menu menu ) {
    getRemoteAdapter().preserveMenu( this.menu );
    this.menu = menu;
  }

  void updateMode() {
    // subclasses may override
  }

  private void clearPacked( Point oldSize ) {
    if( !oldSize.equals( getSize() ) ) {
      packed = false;
    }
  }

  void notifyResize( Point oldSize ) {
    if( !oldSize.equals( getSize() ) ) {
      notifyListeners( SWT.Resize, new Event() );
    }
  }

  void notifyMove( Point oldLocation ) {
    if( !oldLocation.equals( getLocation() ) ) {
      notifyListeners( SWT.Move, new Event() );
    }
  }

  ////////////////////////
  // Focus helping methods

  private void setFocusControl( Control control ) {
    if( control != null ) {
      display.setActiveShell( control.getShell() );
    }
    // focus
    IDisplayAdapter displayAdapter = display.getAdapter( IDisplayAdapter.class );
    displayAdapter.setFocusControl( control, true );
    // active
    if( control != null ) {
      Shell shell = control.getShell();
      shell.setActiveControl( control );
    }
  }

  Control[] getPath() {
    int count = 0;
    Shell shell = getShell();
    Control control = this;
    while( control != shell ) {
      count++;
      control = control.parent;
    }
    control = this;
    Control[] result = new Control[ count ];
    while( control != shell ) {
      result[ --count ] = control;
      control = control.parent;
    }
    return result;
  }

  // Copied from SWT/win32 as is
  @SuppressWarnings("all")
  boolean isFocusAncestor (Control control) {
    while (control != null && control != this && !(control instanceof Shell)) {
      control = control.parent;
    }
    return control == this;
  }

  // Copied from SWT/win32 as is
  void fixFocus ( Control focusControl) {
    Shell shell = getShell ();
    Control control = this;
    while (control != shell && (control = control.parent) != null) {
      if (control.setFixedFocus ()) {
        return;
      }
    }
    shell.setSavedFocus (focusControl);
    //    OS.SetFocus (0);
    // Replacement for OS.setFocus( 0 )
    IDisplayAdapter displayAdapter = display.getAdapter( IDisplayAdapter.class );
    displayAdapter.setFocusControl( null, true );
  }

  // Copied from SWT/win32 as is
  boolean setFixedFocus () {
    if ((style & SWT.NO_FOCUS) != 0) {
      return false;
    }
    return forceFocus ();
  }

  boolean isActive() {
    Shell shell = getShell();
    boolean result = shell.getEnabled();
    Shell[] allShells = getDisplay().getShells();
    int bits = SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL | SWT.PRIMARY_MODAL;
    int shellIndex = allShells.length;
    for( int i = 0; i < allShells.length && result; i++ ) {
      if( allShells[ i ] == shell ) {
        shellIndex = i;
      }
      if( ( allShells[ i ].style & bits ) != 0 && shellIndex < i ) {
        result = false;
      }
    }
    return result;
  }

  Decorations menuShell() {
    return parent.menuShell();
  }

  void fixChildren( Shell newShell,
                    Shell oldShell,
                    Decorations newDecorations,
                    Decorations oldDecorations )
  {
    oldShell.fixShell( newShell, this );
    oldDecorations.fixDecorations( newDecorations, this );
  }

  private void preserveState( int flag ) {
    if( ( flag & DISABLED ) != 0 ) {
      getRemoteAdapter().preserveEnabled( !hasState( DISABLED ) );
    }
    if( ( flag & HIDDEN ) != 0 ) {
      getRemoteAdapter().preserveVisible( !hasState( HIDDEN ) );
    }
  }

  private void addMenuDisposeListener() {
    if( menu != null ) {
      if( menuDisposeListener == null ) {
        menuDisposeListener = new Listener() {
          @Override
          public void handleEvent( Event event ) {
            _setMenu( null );
          }
        };
      }
      menu.addListener( SWT.Dispose, menuDisposeListener );
    }
  }

  private void removeMenuDisposeListener() {
    if( menu != null ) {
      menu.removeListener( SWT.Dispose, menuDisposeListener );
    }
  }

  private ControlRemoteAdapter getRemoteAdapter() {
    return ( ControlRemoteAdapter )getAdapter( RemoteAdapter.class );
  }

  public void addPaintListener( PaintListener listener ) {  }
  public void removePaintListener( PaintListener listener ) {  }
  public void addMouseMoveListener( MouseMoveListener listener ) {  }
  public void removeMouseMoveListener( MouseMoveListener listener ) {  }
}