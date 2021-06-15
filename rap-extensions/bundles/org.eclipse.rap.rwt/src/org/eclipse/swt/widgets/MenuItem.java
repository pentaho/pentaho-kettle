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
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.widgets.IItemHolderAdapter;
import org.eclipse.swt.internal.widgets.menuitemkit.MenuItemLCA;


/**
 * Instances of this class represent a selectable user interface object
 * that issues notification when pressed and released.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>CHECK, CASCADE, PUSH, RADIO, SEPARATOR</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Arm, Help, Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles CHECK, CASCADE, PUSH, RADIO and SEPARATOR
 * may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 */
public class MenuItem extends Item {

  private final Menu parent;
  private Menu menu;
  private DisposeListener menuDisposeListener;
  private boolean selection;
  private int userId;
  private AcceleratorBinding acceleratorBinding;

  /**
   * Constructs a new instance of this class given its parent
   * (which must be a <code>Menu</code>) and a style value
   * describing its behavior and appearance. The item is added
   * to the end of the items maintained by its parent.
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
   * @param parent a menu control which will be the parent of the new instance (cannot be null)
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
   * @see SWT#CHECK
   * @see SWT#CASCADE
   * @see SWT#PUSH
   * @see SWT#RADIO
   * @see SWT#SEPARATOR
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public MenuItem( Menu parent, int style ) {
    super( parent, checkStyle( style ) );
    this.parent = parent;
    parent.getAdapter( IItemHolderAdapter.class ).add( this );
  }

  /**
   * Constructs a new instance of this class given its parent
   * (which must be a <code>Menu</code>), a style value
   * describing its behavior and appearance, and the index
   * at which to place it in the items maintained by its parent.
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
   * @param parent a menu control which will be the parent of the new instance (cannot be null)
   * @param style the style of control to construct
   * @param index the zero-relative index to store the receiver in its parent
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the parent (inclusive)</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   * </ul>
   *
   * @see SWT#CHECK
   * @see SWT#CASCADE
   * @see SWT#PUSH
   * @see SWT#RADIO
   * @see SWT#SEPARATOR
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public MenuItem( Menu parent, int style, int index ) {
    super( parent, checkStyle( style ) );
    this.parent = parent;
    parent.getAdapter( IItemHolderAdapter.class ).insert( this, index );
  }

  /**
   * Returns the receiver's parent, which must be a <code>Menu</code>.
   *
   * @return the receiver's parent
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Menu getParent() {
    checkWidget();
    return parent;
  }

  /**
   * Sets the receiver's pull down menu to the argument.
   * Only <code>CASCADE</code> menu items can have a
   * pull down menu. The sequence of key strokes, button presses
   * and/or button releases that are used to request a pull down
   * menu is platform specific.
   * <p>
   * Note: Disposing of a menu item that has a pull down menu
   * will dispose of the menu.  To avoid this behavior, set the
   * menu to null before the menu item is disposed.
   * </p>
   *
   * @param menu the new pull down menu
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_MENU_NOT_DROP_DOWN - if the menu is not a drop down menu</li>
   *    <li>ERROR_MENUITEM_NOT_CASCADE - if the menu item is not a <code>CASCADE</code></li>
   *    <li>ERROR_INVALID_ARGUMENT - if the menu has been disposed</li>
   *    <li>ERROR_INVALID_PARENT - if the menu is not in the same widget tree</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setMenu( Menu menu ) {
    checkWidget();
    if( this.menu != menu ) {
      if( ( style & SWT.CASCADE ) == 0 ) {
        SWT.error( SWT.ERROR_MENUITEM_NOT_CASCADE );
      }
      if( menu != null ) {
        if( menu.isDisposed() ) {
          SWT.error( SWT.ERROR_INVALID_ARGUMENT );
        }
        if( ( menu.getStyle() & SWT.DROP_DOWN ) == 0 ) {
          SWT.error( SWT.ERROR_MENU_NOT_DROP_DOWN );
        }
        if( menu.getParent() != getParent().getParent() ) {
          SWT.error( SWT.ERROR_INVALID_PARENT );
        }
      }
      removeMenuDisposeListener();
      /* Assign the new menu */
      if( this.menu != null ) {
        this.menu.cascade = null;
      }
      this.menu = menu;
      if( menu != null ) {
        menu.cascade = this;
      }
      addMenuDisposeListener();
    }
  }

  /**
   * Returns the receiver's cascade menu if it has one or null
   * if it does not. Only <code>CASCADE</code> menu items can have
   * a pull down menu. The sequence of key strokes, button presses
   * and/or button releases that are used to request a pull down
   * menu is platform specific.
   *
   * @return the receiver's menu
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Menu getMenu() {
    checkWidget();
    return menu;
  }

  /**
   * Sets the identifier associated with the receiver to the argument.
   *
   * @param id the new identifier. This must be a non-negative value. System-defined identifiers are negative values.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_INVALID_ARGUMENT - if called with an negative-valued argument.</li>
   * </ul>
   *
   * @since 1.4
   */
  public void setID( int id ) {
    checkWidget();
    if( id < 0 ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    userId = id;
  }

  /**
   * Gets the identifier associated with the receiver.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.4
   */
  public int getID() {
    checkWidget();
    return userId;
  }

  /**
   * Sets the image the receiver will display to the argument.
   * <p>
   * Note: This operation is a hint and is not supported on
   * platforms that do not have this concept (for example, Windows NT).
   * </p>
   *
   * @param image the image to display
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  @Override
  public void setImage( Image image ) {
    checkWidget();
    if( ( style & SWT.SEPARATOR ) == 0 ) {
      super.setImage( image );
    }
  }

  /**
   * Sets the widget accelerator.  An accelerator is the bit-wise
   * OR of zero or more modifier masks and a key. Examples:
   * <code>SWT.MOD1 | SWT.MOD2 | 'T', SWT.MOD3 | SWT.F2</code>.
   * <code>SWT.CONTROL | SWT.SHIFT | 'T', SWT.ALT | SWT.F2</code>.
   * The default value is zero, indicating that the menu item does
   * not have an accelerator.
   *
   * @param accelerator an integer that is the bit-wise OR of masks and a key
   *
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 2.1
   */
  public void setAccelerator( int accelerator ) {
    checkWidget();
    if( accelerator != 0 ) {
      if( acceleratorBinding == null ) {
        acceleratorBinding = new AcceleratorBinding( this );
      }
      acceleratorBinding.setAccelerator( accelerator );
    } else if( acceleratorBinding != null ) {
      acceleratorBinding.release();
      acceleratorBinding = null;
    }
  }

  /**
   * Returns the widget accelerator.  An accelerator is the bit-wise
   * OR of zero or more modifier masks and a key. Examples:
   * <code>SWT.CONTROL | SWT.SHIFT | 'T', SWT.ALT | SWT.F2</code>.
   * The default value is zero, indicating that the menu item does
   * not have an accelerator.
   *
   * @return the accelerator or 0
   *
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 2.1
   */
  public int getAccelerator() {
    checkWidget();
    return acceleratorBinding != null ? acceleratorBinding.getAccelerator() : 0;
  }

  //////////
  // Enabled

  /**
   * Enables the receiver if the argument is <code>true</code>,
   * and disables it otherwise. A disabled menu item is typically
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
    removeState( DISABLED );
    if( !enabled ) {
      addState( DISABLED );
    }
  }

  /**
   * Returns <code>true</code> if the receiver is enabled, and
   * <code>false</code> otherwise. A disabled menu item is typically
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
   * otherwise. A disabled menu item is typically not selectable from the
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
    return getEnabled() && parent.isEnabled();
  }

  ////////////
  // Selection

  /**
   * Returns <code>true</code> if the receiver is selected,
   * and false otherwise.
   * <p>
   * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>,
   * it is selected when it is checked.
   *
   * @return the selection state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public boolean getSelection() {
    checkWidget();
    return selection;
  }

  /**
   * Sets the selection state of the receiver.
   * <p>
   * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>,
   * it is selected when it is checked.
   *
   * @param selection the new selection state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setSelection( boolean selection ) {
    checkWidget();
    if( ( style & ( SWT.CHECK | SWT.RADIO ) ) != 0 ) {
      this.selection = selection;
    }
  }

  ///////////////////////
  // Listener maintenance

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the menu item is selected, by sending
   * it one of the messages defined in the <code>SelectionListener</code>
   * interface.
   * <p>
   * When <code>widgetSelected</code> is called, the stateMask field of the event object is valid.
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
   * be notified when the control is selected.
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

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the help events are generated for the control, by sending
   * it one of the messages defined in the <code>HelpListener</code>
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
   * be notified when the arm events are generated for the control, by sending
   * it one of the messages defined in the <code>ArmListener</code>
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
   * @see ArmListener
   * @see #removeArmListener
   * @since 1.3
   */
  public void addArmListener( ArmListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.Arm, typedListener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when the arm events are generated for the control.
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
   * @see ArmListener
   * @see #addArmListener
   * @since 1.3
   */
  public void removeArmListener( ArmListener listener ) {
    checkWidget();
    removeListener( SWT.Arm, listener );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == WidgetLCA.class ) {
      return ( T )MenuItemLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  //////////////////
  // Item overrides

  @Override
  void releaseWidget() {
    super.releaseWidget();
    if( acceleratorBinding != null ) {
      acceleratorBinding.release();
      acceleratorBinding = null;
    }
  }

  @Override
  final void releaseChildren() {
    if( menu != null ) {
      removeMenuDisposeListener();
      menu.dispose();
      menu = null;
    }
  }

  @Override
  final void releaseParent() {
    super.releaseParent();
    parent.getAdapter( IItemHolderAdapter.class ).remove( this );
  }

  @Override
  String getNameText() {
    String result;
    if( ( style & SWT.SEPARATOR ) != 0 ) {
      result = "|";
    } else {
      result = super.getNameText();
    }
    return result;
  }

  ///////////////////////////////////////////////////////
  // Helping methods to observe the disposal of the menu

  private void addMenuDisposeListener() {
    if( menu != null ) {
      if( menuDisposeListener == null ) {
        menuDisposeListener = new DisposeListener() {
          @Override
          public void widgetDisposed( DisposeEvent event ) {
            menu = null;
          }
        };
      }
      menu.addDisposeListener( menuDisposeListener );
    }
  }

  private void removeMenuDisposeListener() {
    if( menu != null ) {
      menu.removeDisposeListener( menuDisposeListener );
    }
  }

  void handleAcceleratorActivation() {
    if( ( style & SWT.CHECK ) != 0 ) {
      selection = !selection;
    } else if ( ( style & SWT.RADIO ) != 0 ) {
      deselectOtherRadios();
      selection = true;
    }
    notifyListeners( SWT.Selection, new Event() );
  }

  private void deselectOtherRadios() {
    for( MenuItem item : parent.getItems() ) {
      if( item != this && ( item.getStyle() & SWT.RADIO ) != 0 && item.getSelection() ) {
        item.setSelection( false );
        item.notifyListeners( SWT.Selection, new Event() );
      }
    }
  }

  ///////////////////////////////////////
  // Helping methods to verify arguments

  private static int checkStyle( int style ) {
    return checkBits( style,
                      SWT.PUSH,
                      SWT.CHECK,
                      SWT.RADIO,
                      SWT.SEPARATOR,
                      SWT.CASCADE,
                      0 );
  }

  ///////////////////
  // Skinning support

  @Override
  void reskinChildren( int flags ) {
    if( menu != null ) {
      menu.reskin( flags );
    }
    super.reskinChildren( flags );
  }

}
