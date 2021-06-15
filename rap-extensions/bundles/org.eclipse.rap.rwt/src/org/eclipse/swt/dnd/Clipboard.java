/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.dnd;


import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

/**
 * The <code>Clipboard</code> provides a mechanism for transferring data from one
 * application to another or within an application.
 *
 * <p>IMPORTANT: This class is <em>not</em> intended to be subclassed.</p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#clipboard">Clipboard snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ClipboardExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public class Clipboard {

  private Display display;

  /**
   * Constructs a new instance of this class.  Creating an instance of a Clipboard
   * may cause system resources to be allocated depending on the platform.  It is therefore
   * mandatory that the Clipboard instance be disposed when no longer required.
   *
   * @param display the display on which to allocate the clipboard
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   * </ul>
   *
   * @see Clipboard#dispose
   * @see Clipboard#checkSubclass
   */
  public Clipboard(Display display) {
    checkSubclass ();
    if (display == null) {
      display = Display.getCurrent();
      if (display == null) {
        display = Display.getDefault();
      }
    }
    if (display.getThread() != Thread.currentThread()) {
      DND.error(SWT.ERROR_THREAD_INVALID_ACCESS);
    }
    this.display = display;
  }

  /**
   * Checks that this class can be subclassed.
   * <p>
   * The SWT class library is intended to be subclassed
   * only at specific, controlled points. This method enforces this
   * rule unless it is overridden.
   * </p><p>
   * <em>IMPORTANT:</em> By providing an implementation of this
   * method that allows a subclass of a class which does not
   * normally allow subclassing to be created, the implementer
   * agrees to be fully responsible for the fact that any such
   * subclass will likely fail between SWT releases and will be
   * strongly platform specific. No support is provided for
   * user-written classes which are implemented in this fashion.
   * </p><p>
   * The ability to subclass outside of the allowed SWT classes
   * is intended purely to enable those not on the SWT development
   * team to implement patches in order to get around specific
   * limitations in advance of when those limitations can be
   * addressed by the team. Subclassing should not be attempted
   * without an intimate and detailed understanding of the hierarchy.
   * </p>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   * </ul>
   */
  protected void checkSubclass () {
    String name = getClass().getName ();
    String validName = Clipboard.class.getName();
    if (!validName.equals(name)) {
      DND.error (SWT.ERROR_INVALID_SUBCLASS);
    }
  }

  /**
   * Throws an <code>SWTException</code> if the receiver can not
   * be accessed by the caller. This may include both checks on
   * the state of the receiver and more generally on the entire
   * execution context. This method <em>should</em> be called by
   * widget implementors to enforce the standard SWT invariants.
   * <p>
   * Currently, it is an error to invoke any method (other than
   * <code>isDisposed()</code>) on a widget that has had its
   * <code>dispose()</code> method called. It is also an error
   * to call widget methods from any thread that is different
   * from the thread that created the widget.
   * </p><p>
   * In future releases of SWT, there may be more or fewer error
   * checks and exceptions may be thrown for different reasons.
   * </p>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  protected void checkWidget () {
    Display display = this.display;
    if (display == null) DND.error (SWT.ERROR_WIDGET_DISPOSED);
    if (display.getThread() != Thread.currentThread ()) DND.error (SWT.ERROR_THREAD_INVALID_ACCESS);
    if (display.isDisposed()) DND.error(SWT.ERROR_WIDGET_DISPOSED);
  }

  /**
   * Disposes of the operating system resources associated with the clipboard.
   * The data will still be available on the system clipboard after the dispose
   * method is called.
   *
   * <p>NOTE: On some platforms the data will not be available once the application
   * has exited or the display has been disposed.</p>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   * </ul>
   */
  public void dispose () {
    if (isDisposed()) return;
    if (display.getThread() != Thread.currentThread()) DND.error(SWT.ERROR_THREAD_INVALID_ACCESS);
    display = null;
  }

  /**
   * Retrieve the data of the specified type currently available on the system
   * clipboard.  Refer to the specific subclass of <code>Transfer</code> to
   * determine the type of object returned.
   *
   * <p>The following snippet shows text and RTF text being retrieved from the
   * clipboard:</p>
   *
   *    <code><pre>
   *    Clipboard clipboard = new Clipboard(display);
   *    TextTransfer textTransfer = TextTransfer.getInstance();
   *    String textData = (String)clipboard.getContents(textTransfer);
   *    if (textData != null) System.out.println("Text is "+textData);
   *    RTFTransfer rtfTransfer = RTFTransfer.getInstance();
   *    String rtfData = (String)clipboard.getContents(rtfTransfer);
   *    if (rtfData != null) System.out.println("RTF Text is "+rtfData);
   *    clipboard.dispose();
   *    </code></pre>
   *
   * @param transfer the transfer agent for the type of data being requested
   * @return the data obtained from the clipboard or null if no data of this type is available
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if transfer is null</li>
   * </ul>
   *
   * @see Transfer
   */
  public Object getContents(Transfer transfer) {
    return getContents(transfer, DND.CLIPBOARD);
  }

  /**
   * Retrieve the data of the specified type currently available on the specified
   * clipboard.  Refer to the specific subclass of <code>Transfer</code> to
   * determine the type of object returned.
   *
   * <p>The following snippet shows text and RTF text being retrieved from the
   * clipboard:</p>
   *
   *    <code><pre>
   *    Clipboard clipboard = new Clipboard(display);
   *    TextTransfer textTransfer = TextTransfer.getInstance();
   *    String textData = (String)clipboard.getContents(textTransfer);
   *    if (textData != null) System.out.println("Text is "+textData);
   *    RTFTransfer rtfTransfer = RTFTransfer.getInstance();
   *    String rtfData = (String)clipboard.getContents(rtfTransfer, DND.CLIPBOARD);
   *    if (rtfData != null) System.out.println("RTF Text is "+rtfData);
   *    clipboard.dispose();
   *    </code></pre>
   *
   * <p>The clipboards value is either one of the clipboard constants defined in
   * class <code>DND</code>, or must be built by <em>bitwise OR</em>'ing together
   * (that is, using the <code>int</code> "|" operator) two or more
   * of those <code>DND</code> clipboard constants.</p>
   *
   * @param transfer the transfer agent for the type of data being requested
   * @param clipboards on which to look for data
   *
   * @return the data obtained from the clipboard or null if no data of this type is available
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if transfer is null</li>
   * </ul>
   *
   * @see Transfer
   * @see DND#CLIPBOARD
   * @see DND#SELECTION_CLIPBOARD
   *
   * @since 3.1
   */
  public Object getContents(Transfer transfer, int clipboards) {
    checkWidget();
    if (transfer == null) DND.error(SWT.ERROR_NULL_ARGUMENT);
    if (!(transfer instanceof TextTransfer)) return null;
    return display.getData("TextTransfer"); //$NON-NLS-1$
  }

  /**
   * Returns <code>true</code> if the clipboard has been disposed,
   * and <code>false</code> otherwise.
   * <p>
   * This method gets the dispose state for the clipboard.
   * When a clipboard has been disposed, it is an error to
   * invoke any other method using the clipboard.
   * </p>
   *
   * @return <code>true</code> when the widget is disposed and <code>false</code> otherwise
   *
   * @since 3.0
   */
  public boolean isDisposed () {
    return (display == null);
  }

  /**
   * Place data of the specified type on the system clipboard.  More than one type
   * of data can be placed on the system clipboard at the same time.  Setting the
   * data clears any previous data from the system clipboard, regardless of type.
   *
   * <p>NOTE: On some platforms, the data is immediately copied to the system
   * clipboard but on other platforms it is provided upon request.  As a result,
   * if the application modifies the data object it has set on the clipboard, that
   * modification may or may not be available when the data is subsequently
   * requested.</p>
   *
   * <p>The following snippet shows text and RTF text being set on the copy/paste
   * clipboard:
   * </p>
   *
   * <code><pre>
   *  Clipboard clipboard = new Clipboard(display);
   *  String textData = "Hello World";
   *  String rtfData = "{\\rtf1\\b\\i Hello World}";
   *  TextTransfer textTransfer = TextTransfer.getInstance();
   *  RTFTransfer rtfTransfer = RTFTransfer.getInstance();
   *  Transfer[] transfers = new Transfer[]{textTransfer, rtfTransfer};
   *  Object[] data = new Object[]{textData, rtfData};
   *  clipboard.setContents(data, transfers);
   *  clipboard.dispose();
   * </code></pre>
   *
   * @param data the data to be set in the clipboard
   * @param dataTypes the transfer agents that will convert the data to its
   * platform specific format; each entry in the data array must have a
   * corresponding dataType
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if data is null or datatypes is null
   *          or the length of data is not the same as the length of dataTypes</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *  @exception SWTError <ul>
   *    <li>ERROR_CANNOT_SET_CLIPBOARD - if the clipboard is locked or otherwise unavailable</li>
   * </ul>
   *
   * <p>NOTE: ERROR_CANNOT_SET_CLIPBOARD should be an SWTException, since it is a
   * recoverable error, but can not be changed due to backward compatibility.</p>
   */
  public void setContents(Object[] data, Transfer[] dataTypes) {
    setContents(data, dataTypes, DND.CLIPBOARD);
  }

  /**
   * Place data of the specified type on the specified clipboard.  More than one
   * type of data can be placed on the specified clipboard at the same time.
   * Setting the data clears any previous data from the specified
   * clipboard, regardless of type.
   *
   * <p>NOTE: On some platforms, the data is immediately copied to the specified
   * clipboard but on other platforms it is provided upon request.  As a result,
   * if the application modifies the data object it has set on the clipboard, that
   * modification may or may not be available when the data is subsequently
   * requested.</p>
   *
   * <p>The clipboards value is either one of the clipboard constants defined in
   * class <code>DND</code>, or must be built by <em>bitwise OR</em>'ing together
   * (that is, using the <code>int</code> "|" operator) two or more
   * of those <code>DND</code> clipboard constants.</p>
   *
   * <p>The following snippet shows text and RTF text being set on the copy/paste
   * clipboard:
   * </p>
   *
   * <code><pre>
   *  Clipboard clipboard = new Clipboard(display);
   *  String textData = "Hello World";
   *  String rtfData = "{\\rtf1\\b\\i Hello World}";
   *  TextTransfer textTransfer = TextTransfer.getInstance();
   *  RTFTransfer rtfTransfer = RTFTransfer.getInstance();
   *  Transfer[] transfers = new Transfer[]{textTransfer, rtfTransfer};
   *  Object[] data = new Object[]{textData, rtfData};
   *  clipboard.setContents(data, transfers, DND.CLIPBOARD);
   *  clipboard.dispose();
   * </code></pre>
   *
   * @param data the data to be set in the clipboard
   * @param dataTypes the transfer agents that will convert the data to its
   * platform specific format; each entry in the data array must have a
   * corresponding dataType
   * @param clipboards on which to set the data
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if data is null or datatypes is null
   *          or the length of data is not the same as the length of dataTypes</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *  @exception SWTError <ul>
   *    <li>ERROR_CANNOT_SET_CLIPBOARD - if the clipboard is locked or otherwise unavailable</li>
   * </ul>
   *
   * <p>NOTE: ERROR_CANNOT_SET_CLIPBOARD should be an SWTException, since it is a
   * recoverable error, but can not be changed due to backward compatibility.</p>
   *
   * @see DND#CLIPBOARD
   * @see DND#SELECTION_CLIPBOARD
   *
   * @since 3.1
   */
  public void setContents(Object[] data, Transfer[] dataTypes, int clipboards) {
    checkWidget();
    if (data == null || dataTypes == null || data.length != dataTypes.length) {
      DND.error(SWT.ERROR_INVALID_ARGUMENT);
    }

    for (int i = 0; i < dataTypes.length; i++) {
      if (dataTypes[i] instanceof TextTransfer && data[i] instanceof String){
        display.setData("TextTransfer", data[i]); //$NON-NLS-1$
        return;
      }
    }
  }

  /**
   * Returns an array of the data types currently available on the system
   * clipboard. Use with Transfer.isSupportedType.
   *
   * @return array of data types currently available on the system clipboard
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see Transfer#isSupportedType
   *
   * @since 3.0
   */
  public TransferData[] getAvailableTypes() {
    checkWidget();
    return new TransferData[0];
  }

  /**
   * Returns a platform specific list of the data types currently available on the
   * system clipboard.
   *
   * <p>Note: <code>getAvailableTypeNames</code> is a utility for writing a Transfer
   * sub-class.  It should NOT be used within an application because it provides
   * platform specific information.</p>
   *
   * @return a platform specific list of the data types currently available on the
   * system clipboard
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public String[] getAvailableTypeNames() {
    checkWidget();
    return new String[0];
  }
}
