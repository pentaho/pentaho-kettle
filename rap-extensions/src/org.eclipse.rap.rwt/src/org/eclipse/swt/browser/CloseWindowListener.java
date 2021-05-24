/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.browser;

import org.eclipse.swt.internal.SWTEventListener;

/**
 * This listener interface may be implemented in order to receive
 * a {@link WindowEvent} notification when a {@link Browser} is
 * about to be closed and when its host window should be closed
 * by the application.
 *
 * @see OpenWindowListener
 *
 * @since 3.0
 */
public interface CloseWindowListener extends SWTEventListener {

  /**
   * This method is called when the window hosting a {@link Browser} should be closed.
   * Application would typically close the {@link org.eclipse.swt.widgets.Shell} that
   * hosts the <code>Browser</code>. The <code>Browser</code> is disposed after this
   * notification.
   *
   * <p>The following fields in the <code>WindowEvent</code> apply:
   * <ul>
   * <li>(in) widget the <code>Browser</code> that is going to be disposed
   * </ul></p>
   *
   * @param event the <code>WindowEvent</code> that specifies the <code>Browser</code>
   * that is going to be disposed
   *
   * @see org.eclipse.swt.widgets.Shell#close()
   *
   * @since 3.0
   */
  public void close(WindowEvent event);
}
