/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.browser;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

/**
 * A <code>WindowEvent</code> is sent by a {@link Browser} when
 * a new window needs to be created or when an existing window needs to be
 * closed. This notification occurs when a javascript command such as
 * <code>window.open</code> or <code>window.close</code> gets executed by
 * a <code>Browser</code>.
 *
 * <p>
 * The following example shows how <code>WindowEvent</code>'s are typically
 * handled.
 *
 * <code><pre>
 *	public static void main(String[] args) {
 *		Display display = new Display();
 *		Shell shell = new Shell(display);
 *		shell.setText("Main Window");
 *		shell.setLayout(new FillLayout());
 *		Browser browser = new Browser(shell, SWT.NONE);
 *		initialize(display, browser);
 *		shell.open();
 *		browser.setUrl("http://www.eclipse.org");
 *		while (!shell.isDisposed()) {
 *			if (!display.readAndDispatch())
 *				display.sleep();
 *		}
 *		display.dispose();
 *	}
 *
 *	static void initialize(final Display display, Browser browser) {
 *		browser.addOpenWindowListener(new OpenWindowListener() {
 *			public void open(WindowEvent event) {
 *				// Certain platforms can provide a default full browser.
 *				// simply return in that case if the application prefers
 *				// the default full browser to the embedded one set below.
 *				if (!event.required) return;
 *
 *				// Embed the new window
 *				Shell shell = new Shell(display);
 *				shell.setText("New Window");
 *				shell.setLayout(new FillLayout());
 *				Browser browser = new Browser(shell, SWT.NONE);
 *				initialize(display, browser);
 *				event.browser = browser;
 *			}
 *		});
 *		browser.addVisibilityWindowListener(new VisibilityWindowListener() {
 *			public void hide(WindowEvent event) {
 *				Browser browser = (Browser)event.widget;
 *				Shell shell = browser.getShell();
 *				shell.setVisible(false);
 *			}
 *			public void show(WindowEvent event) {
 *				Browser browser = (Browser)event.widget;
 *				Shell shell = browser.getShell();
 *				if (event.location != null) shell.setLocation(event.location);
 *				if (event.size != null) {
 *					Point size = event.size;
 *					shell.setSize(shell.computeSize(size.x, size.y));
 *				}
 *				if (event.addressBar || event.menuBar || event.statusBar || event.toolBar) {
 *					// Create widgets for the address bar, menu bar, status bar and/or tool bar
 *					// leave enough space in the Shell to accommodate a Browser of the size
 *					// given by event.size
 *				}
 *				shell.open();
 *			}
 *		});
 *		browser.addCloseWindowListener(new CloseWindowListener() {
 *			public void close(WindowEvent event) {
 *				Browser browser = (Browser)event.widget;
 *				Shell shell = browser.getShell();
 *				shell.close();
 *			}
 *		});
 *	}
 * </pre></code>
 *
 * The following notifications are emitted when the user selects a hyperlink that targets a new window
 * or as the result of a javascript that executes window.open.
 *
 * <p>Main Browser
 * <ul>
 *    <li>User selects a link that opens in a new window or javascript requests a new window</li>
 *    <li>OpenWindowListener.open() notified</li>
 *    <ul>
 *    		<li>Application creates a new Shell and a second Browser inside that Shell</li>
 *    		<li>Application registers WindowListener's on that second Browser, such as VisibilityWindowListener</li>
 *	    	<li>Application returns the second Browser as the host for the new window content</li>
 *    </ul>
 * </ul>
 *
 * <p>Second Browser
 * <ul>
 *    <li>VisibilityWindowListener.show() notified</li>
 *    <ul>
 *    		<li>Application sets navigation tool bar, status bar, menu bar and Shell size
 *    		<li>Application makes the Shell hosting the second Browser visible
 *    		<li>User now sees the new window
 *    </ul>
 * </ul>
 *
 * @see CloseWindowListener
 * @see OpenWindowListener
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.0
 */
public class WindowEvent extends TypedEvent {

  /**
   * Specifies whether the platform requires the user to provide a
   * <code>Browser</code> to handle the new window.
   *
   * @since 3.1
   */
  public boolean required;


  /**
   * <code>Browser</code> provided by the application.
   */
  public Browser browser;

  /**
   * Requested location for the <code>Shell</code> hosting the <code>Browser</code>.
   * It is <code>null</code> if no location has been requested.
   */
  public Point location;

  /**
   * Requested <code>Browser</code> size. The client area of the <code>Shell</code>
   * hosting the <code>Browser</code> should be large enough to accommodate that size.
   * It is <code>null</code> if no size has been requested.
   */
  public Point size;

  /**
   * Specifies whether the <code>Shell</code> hosting the <code>Browser</code> should
   * display an address bar.
   *
   * @since 3.1
   */
  public boolean addressBar;

  /**
   * Specifies whether the <code>Shell</code> hosting the <code>Browser</code> should
   * display a menu bar.  Note that this is always <code>true</code> on OS X.
   *
   * @since 3.1
   */
  public boolean menuBar;

  /**
   * Specifies whether the <code>Shell</code> hosting the <code>Browser</code> should
   * display a status bar.
   *
   * @since 3.1
   */
  public boolean statusBar;

  /**
   * Specifies whether the <code>Shell</code> hosting the <code>Browser</code> should
   * display a tool bar.
   *
   * @since 3.1
   */
  public boolean toolBar;

  static final long serialVersionUID = 3617851997387174969L;

  /**
   * Constructs a new instance of this class.
   *
   * @param widget the widget that fired the event
   *
   * @since 3.5
   */
  public WindowEvent(Widget widget) {
    super(widget);
  }

  /**
   * Returns a string containing a concise, human-readable
   * description of the receiver.
   *
   * @return a string representation of the event
   */
  @Override
  public String toString() {
    String string = super.toString ();
    return string.substring (0, string.length() - 1) // remove trailing '}'
      + " required=" + required
      + " browser=" + browser
      + " location=" + location
      + " size=" + size
      + " addressBar=" + addressBar
      + " menuBar=" + menuBar
      + " statusBar=" + statusBar
      + " toolBar=" + toolBar
      + "}";
  }
}
