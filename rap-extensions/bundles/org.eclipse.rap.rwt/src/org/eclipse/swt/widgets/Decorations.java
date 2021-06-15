/*******************************************************************************
 * Copyright (c) 2007, 2015 Innoopract Informationssysteme GmbH and others.
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

import org.eclipse.rap.rwt.internal.lifecycle.ProcessActionRunner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.widgets.MenuHolder;


/**
 * <p>This class was introduced to be API compatible with SWT and does only
 * provide those methods that are absolutely necessary to serve this purpose.
 * </p>
 */
public class Decorations extends Canvas {

  private Menu menuBar;
  private MenuHolder menuHolder;
  private DisposeListener menuBarDisposeListener;
  private Image image;
  private Image[] images;
  private String text;
  private Button defaultButton;
  private Button saveDefault;
  private Listener defaultButtonFocusListener;
  private Control savedFocus;

  Decorations( Composite parent ) {
    // prevent instantiation from outside this package
    super( parent );
    images = new Image[0];
    text = "";
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == MenuHolder.class ) {
      if( menuHolder == null ) {
        menuHolder = new MenuHolder();
      }
      return ( T )menuHolder;
    }
    return super.getAdapter( adapter );
  }

  /**
   * Sets the receiver's images to the argument, which may
   * be an empty array. Images are typically displayed by the
   * window manager when the instance is marked as iconified,
   * and may also be displayed somewhere in the trim when the
   * instance is in normal or maximized states. Depending where
   * the icon is displayed, the platform chooses the icon with
   * the "best" attributes. It is expected that the array will
   * contain the same icon rendered at different sizes, with
   * different depth and transparency attributes.
   *
   * @param images the new image array
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the array of images is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if one of the images is null or has been
   *                                 disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   *                                      created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public void setImages( Image[] images ) {
    checkWidget();
    if( images == null ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    for( int i = 0; i < images.length; i++ ) {
      if( images[i] == null ) {
        error( SWT.ERROR_INVALID_ARGUMENT );
      }
    }
    this.images = images;
  }

  /**
   * Returns the receiver's images if they had previously been
   * set using <code>setImages()</code>. Images are typically
   * displayed by the window manager when the instance is
   * marked as iconified, and may also be displayed somewhere
   * in the trim when the instance is in normal or maximized
   * states. Depending where the icon is displayed, the platform
   * chooses the icon with the "best" attributes.  It is expected
   * that the array will contain the same icon rendered at different
   * sizes, with different depth and transparency attributes.
   *
   * <p>
   * Note: This method will return an empty array if called before
   * <code>setImages()</code> is called. It does not provide
   * access to a window manager provided, "default" image
   * even if one exists.
   * </p>
   *
   * @return the images
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   *                                      created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public Image[] getImages() {
    checkWidget();
    return images;
  }

  /**
   * Sets the receiver's image to the argument, which may
   * be null. The image is typically displayed by the window
   * manager when the instance is marked as iconified, and
   * may also be displayed somewhere in the trim when the
   * instance is in normal or maximized states.
   *
   * @param image the new image (or null)
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setImage( Image image ) {
    checkWidget();
    this.image = image;
  }

  /**
   * Returns the receiver's image if it had previously been
   * set using <code>setImage()</code>. The image is typically
   * displayed by the window manager when the instance is
   * marked as iconified, and may also be displayed somewhere
   * in the trim when the instance is in normal or maximized
   * states.
   * <p>
   * Note: This method will return null if called before
   * <code>setImage()</code> is called. It does not provide
   * access to a window manager provided, "default" image
   * even if one exists.
   * </p>
   *
   * @return the image
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Image getImage() {
    checkWidget();
    return image;
  }

  /**
   * Sets the receiver's text, which is the string that the
   * window manager will typically display as the receiver's
   * <em>title</em>, to the argument, which must not be null.
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
   * @since 2.2
   */
  public void setText( String text ) {
    checkWidget();
    if( text == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    this.text = text;
  }

  /**
   * Returns the receiver's text, which is the string that the
   * window manager will typically display as the receiver's
   * <em>title</em>. If the text has not previously been set,
   * returns an empty string.
   *
   * @return the text
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   * @since 2.2
   */
  public String getText() {
    checkWidget();
    return text;
  }

  /**
   * Sets the receiver's menu bar to the argument, which
   * may be null.
   *
   * @param menuBar the new menu bar
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the menu has been disposed</li>
   *    <li>ERROR_INVALID_PARENT - if the menu is not in the same widget tree</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setMenuBar( Menu menuBar ) {
    checkWidget();
    if( this.menuBar != menuBar ) {
      if( menuBar != null ) {
        if( menuBar.isDisposed() ) {
          SWT.error( SWT.ERROR_INVALID_ARGUMENT );
        }
        if( menuBar.getParent() != this ) {
          SWT.error( SWT.ERROR_INVALID_PARENT );
        }
        if( ( menuBar.getStyle() & SWT.BAR ) == 0 ) {
          SWT.error( SWT.ERROR_MENU_NOT_BAR );
        }
      }
      removeMenuBarDisposeListener();
      this.menuBar = menuBar;
      addMenuBarDisposeListener();
    }
  }

  /**
   * Returns the receiver's menu bar if one had previously
   * been set, otherwise returns null.
   *
   * @return the menu bar or null
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Menu getMenuBar() {
    checkWidget();
    return menuBar;
  }

  @Override
  public boolean isReparentable() {
    checkWidget();
    return false;
  }

  /**
   * If the argument is not null, sets the receiver's default
   * button to the argument, and if the argument is null, sets
   * the receiver's default button to the first button which
   * was set as the receiver's default button (called the
   * <em>saved default button</em>). If no default button had
   * previously been set, or the saved default button was
   * disposed, the receiver's default button will be set to
   * null.
   * <p>
   * The default button is the button that is selected when
   * the receiver is active and the user presses ENTER.
   * </p>
   *
   * @param button the new default button
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the button has been disposed</li>
   *    <li>ERROR_INVALID_PARENT - if the control is not in the same widget tree</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   * @since 2.2
   */
  public void setDefaultButton( Button button ) {
    checkWidget();
    if( button != null ) {
      if( button.isDisposed() ) {
        error( SWT.ERROR_INVALID_ARGUMENT );
      }
      if( button.getShell() != this ) {
        error( SWT.ERROR_INVALID_PARENT );
      }
    }
    setDefaultButton( button, true );
  }

  /**
   * Returns the receiver's default button if one had
   * previously been set, otherwise returns null.
   *
   * @return the default button or null
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see Shell#setDefaultButton(Button)
   * @since 2.2
   */
  public Button getDefaultButton() {
    checkWidget();
    Button result = null;
    if( defaultButton != null && !defaultButton.isDisposed() ) {
      result = defaultButton;
    }
    return result;
  }

  void updateDefaultButton( final Control focusControl, final boolean set ) {
    if( isPushButton( focusControl ) ) {
      ProcessActionRunner.add( new Runnable() {
        @Override
        public void run() {
          Button defaultButton = ( Button )focusControl;
          updateDefaultButtonFocusListener( defaultButton, set );
          setDefaultButton( set ? defaultButton : null, false );
        }
      } );
    }
  }

  void setDefaultButton( Button button, boolean save ) {
    if( button == null ) {
      if( defaultButton == saveDefault ) {
        if( save ) {
          saveDefault = null;
        }
        return;
      }
    } else {
      if( ( button.getStyle() & SWT.PUSH ) == 0 ) {
        return;
      }
      if( button == defaultButton ) {
        if( save ) {
          saveDefault = defaultButton;
        }
        return;
      }
    }
    if( defaultButton != null && !defaultButton.isDisposed() ) {
      defaultButton.setDefault( false );
    }
    defaultButton = button;
    if( defaultButton == null ) {
      defaultButton = saveDefault;
    }
    if( defaultButton != null && !defaultButton.isDisposed() ) {
      defaultButton.setDefault( true );
    }
    if( save ) {
      saveDefault = defaultButton;
    }
    if( saveDefault != null && saveDefault.isDisposed() ) {
      saveDefault = null;
    }
  }

  final void setSavedFocus( Control control ) {
    savedFocus = control;
  }

  final Control getSavedFocus() {
    return savedFocus;
  }

  final void saveFocus() {
    Control control = display.getFocusControl();
    if( control != null && control != this && this == control.getShell() ) {
      setSavedFocus( control );
    }
  }

  final boolean restoreFocus() {
    if( savedFocus != null && savedFocus.isDisposed() ) {
      savedFocus = null;
    }
    boolean result = false;
    if( savedFocus != null && savedFocus.setSavedFocus() ) {
      result = true;
    }
    return result;
  }

  @Override
  String getNameText() {
    return getText();
  }

  static int checkStyle( int style ) {
    int result = style;
    if( ( result & SWT.NO_TRIM ) != 0 ) {
      int trim = ( SWT.CLOSE
                 | SWT.TITLE
                 | SWT.MIN
                 | SWT.MAX
                 | SWT.RESIZE
                 | SWT.BORDER );
      result &= ~trim;
    }
    if( ( result & ( /* SWT.MENU | */ SWT.MIN | SWT.MAX | SWT.CLOSE ) ) != 0 ) {
      result |= SWT.TITLE;
    }
    if( ( result & ( SWT.MIN | SWT.MAX ) ) != 0 ) {
      result |= SWT.CLOSE;
    }
    return result;
  }

  @Override
  final void releaseWidget() {
    removeMenuBarDisposeListener();
    super.releaseWidget();
  }

  @Override
  Decorations menuShell() {
    return this;
  }

  void fixDecorations( Decorations newDecorations, Control control ) {
    if( newDecorations != this ) {
      if( control == savedFocus ) {
        savedFocus = null;
      }
      if( control == defaultButton ) {
        defaultButton = null;
      }
      if( control == saveDefault ) {
        saveDefault = null;
      }
    }
  }

  private void addMenuBarDisposeListener() {
    if( menuBar != null ) {
      if( menuBarDisposeListener == null ) {
        menuBarDisposeListener = new DisposeListener() {
          @Override
          public void widgetDisposed( DisposeEvent event ) {
            Decorations.this.menuBar = null;
          }
        };
      }
      menuBar.addDisposeListener( menuBarDisposeListener );
    }
  }

  private void removeMenuBarDisposeListener() {
    if( menuBar != null ) {
      menuBar.removeDisposeListener( menuBarDisposeListener );
    }
  }

  private static boolean isPushButton( Control control ) {
    return control instanceof Button && ( control.style & SWT.PUSH ) != 0 ;
  }

  private void updateDefaultButtonFocusListener( Button defaultButton, boolean set ) {
    if( !defaultButton.isDisposed() ) {
      if( set ) {
        defaultButton.addListener( SWT.FocusOut, getDefaultButtonFocusListener() );
      } else {
        defaultButton.removeListener( SWT.FocusOut, getDefaultButtonFocusListener() );
      }
    }
  }

  private Listener getDefaultButtonFocusListener() {
    if( defaultButtonFocusListener == null ) {
      defaultButtonFocusListener = new Listener() {
        @Override
        public void handleEvent( Event event ) {
          // dummy listener - see bug 419920
        }
      };
    }
    return defaultButtonFocusListener;
  }

  ///////////////////
  // Skinning support

  @Override
  void reskinChildren( int flags ) {
    if( menuBar != null ) {
      menuBar.reskin( flags );
    }
    for( Menu menu : this.getAdapter( MenuHolder.class ) ) {
      if( menu != null ) {
        menu.reskin( flags );
      }
    }
    super.reskinChildren( flags );
  }

}
