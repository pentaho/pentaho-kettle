/*******************************************************************************
 * Copyright (c) 2002, 2016 Innoopract Informationssysteme GmbH and others.
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

import static org.eclipse.rap.rwt.internal.service.ContextProvider.getApplicationContext;

import org.eclipse.rap.rwt.Adaptable;
import org.eclipse.rap.rwt.internal.lifecycle.SimpleLifeCycle;
import org.eclipse.rap.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.internal.SerializableCompatibility;

/**
 * This class is the abstract superclass of the classes
 * that represent the built in platform dialogs.
 * A <code>Dialog</code> typically contains other widgets
 * that are not accessible. A <code>Dialog</code> is not
 * a <code>Widget</code>.
 * <p>
 * This class can also be used as the abstract superclass
 * for user-designed dialogs. Such dialogs usually consist
 * of a Shell with child widgets.
 * <p>
 * Note: The <em>modality</em> styles supported by this class
 * are treated as <em>HINT</em>s, because not all are supported
 * by every subclass on every platform. If a modality style is
 * not supported, it is "upgraded" to a more restrictive modality
 * style that is supported.  For example, if <code>PRIMARY_MODAL</code>
 * is not supported by a particular dialog, it would be upgraded to
 * <code>APPLICATION_MODAL</code>. In addition, as is the case
 * for shells, the window manager for the desktop on which the
 * instance is visible has ultimate control over the appearance
 * and behavior of the instance, including its modality.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>APPLICATION_MODAL, PRIMARY_MODAL, SYSTEM_MODAL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles APPLICATION_MODAL, PRIMARY_MODAL,
 * and SYSTEM_MODAL may be specified.
 * </p>
 *
 * @see Shell
 */
public abstract class Dialog implements Adaptable, SerializableCompatibility {

  private static final int HORIZONTAL_DIALOG_UNIT_PER_CHAR = 4;

  final int style;
  final Shell parent;
  protected Shell shell;
  protected int returnCode;
  String title;

  /**
   * Constructs a new instance of this class given only its
   * parent.
   *
   * @param parent a shell which will be the parent of the new instance
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   * </ul>
   */
  public Dialog( Shell parent ) {
    this( parent, SWT.PRIMARY_MODAL );
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
   *
   * @param parent a shell which will be the parent of the new instance
   * @param style the style of dialog to construct
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   * </ul>
   *
   * <!--@see SWT#PRIMARY_MODAL-->
   * @see SWT#APPLICATION_MODAL
   * <!--@see SWT#SYSTEM_MODAL-->
   */
  public Dialog( Shell parent, int style ) {
    checkParent( parent );
    this.parent = parent;
    this.style = style;
    title = "";
  }

  /**
   * Returns the receiver's parent, which must be a <code>Shell</code>
   * or null.
   *
   * @return the receiver's parent
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Shell getParent() {
    return parent;
  }

  /**
   * Returns the receiver's style information.
   * <p>
   * Note that, the value which is returned by this method <em>may
   * not match</em> the value which was provided to the constructor
   * when the receiver was created.
   * </p>
   *
   * @return the style bits
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getStyle() {
    return style;
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
   */
  public String getText() {
    return title;
  }

  /**
   * Sets the receiver's text, which is the string that the
   * window manager will typically display as the receiver's
   * <em>title</em>, to the argument, which must not be null.
   *
   * @param string the new text
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the text is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setText( String string ) {
    if( string == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    title = string;
  }

  /**
   * Opens this dialog in a non-blocking way and brings it to the front of the display. If given,
   * the <code>dialogCallback</code> is notified when the dialog is closed.
   * <p>
   * Use this method instead of the <code>open()</code> method from the respective
   * <code>Dialog</code> implementation when running in <em>JEE_COMPATIBILTY</em> mode.
   * </p>
   *
   * @param dialogCallback the callback to be notified when the dialog was closed or
   *          <code>null</code> if no callback should be notified.
   * @see DialogCallback
   * @see org.eclipse.rap.rwt.application.Application.OperationMode
   * @rwtextension This method is not available in SWT.
   * @since 3.1
   */
  public void open( final DialogCallback dialogCallback ) {
    prepareOpen();
    returnCode = SWT.CANCEL;
    shell.open();
    shell.addShellListener( new ShellAdapter() {
      @Override
      public void shellClosed( ShellEvent event ) {
        if( dialogCallback != null ) {
          dialogCallback.dialogClosed( returnCode );
        }
      }
    } );
  }

  /**
   * Implementation of the <code>Adaptable</code> interface.
   * <p><strong>IMPORTANT:</strong> This method is <em>not</em> part of the RWT
   * public API. It is marked public only so that it can be shared
   * within the packages provided by RWT. It should never be accessed
   * from application code.
   * </p>
   */
  @Override
  public <T> T getAdapter( Class<T> adapter ) {
    return null;
  }

  protected void prepareOpen() {
  }

  protected void checkSubclass() {
    if( !Display.isValidClass( getClass() ) ) {
      SWT.error( SWT.ERROR_INVALID_SUBCLASS );
    }
  }

  protected void checkOperationMode() {
    if( getApplicationContext().getLifeCycleFactory().getLifeCycle() instanceof SimpleLifeCycle ) {
      throw new UnsupportedOperationException( "Method not supported in JEE_COMPATIBILITY mode." );
    }
  }

  protected void runEventLoop( Shell shell ) {
    shell.open();
    Display display = shell.getDisplay();
    while( !shell.isDisposed() ) {
      if( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
  }

  static int convertHorizontalDLUsToPixels( Control control, int dlus ) {
    Font dialogFont = control.getFont();
    float charWidth = TextSizeUtil.getAvgCharWidth( dialogFont );
    float width = charWidth * dlus + HORIZONTAL_DIALOG_UNIT_PER_CHAR / 2;
    return ( int )( width / HORIZONTAL_DIALOG_UNIT_PER_CHAR );
  }

  static int checkStyle( Shell parent, int style ) {
    int result = style;
    int mask = SWT.PRIMARY_MODAL | SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL;
    if( ( result & SWT.SHEET ) != 0 ) {
      result &= ~SWT.SHEET;
      if( ( result & mask ) == 0 ) {
        result |= parent == null ? SWT.APPLICATION_MODAL : SWT.PRIMARY_MODAL;
      }
    }
    if( ( result & mask ) == 0 ) {
      result |= SWT.APPLICATION_MODAL;
    }
    if( ( result & ( SWT.LEFT_TO_RIGHT ) ) == 0 ) {
      if( parent != null ) {
        if( ( parent.style & SWT.LEFT_TO_RIGHT ) != 0 ) {
          result |= SWT.LEFT_TO_RIGHT;
        }
      }
    }
    return result;
  }

  private static void checkParent( Shell parent ) {
    if( parent == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    parent.checkWidget();
  }

}
