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
 * This listener interface may be implemented in order to receive a
 * {@link ProgressEvent} notification when a {@link Browser} makes a progress in
 * loading the current URL or when the current URL has been loaded.
 *
 * @see Browser#addProgressListener(ProgressListener)
 * @see Browser#removeProgressListener(ProgressListener)
 * @see Browser#getUrl()
 * @since 1.4
 */
public interface ProgressListener extends SWTEventListener {

  /**
   * This method is called when a progress is made during the loading of the
   * current location.
   * <p>
   * <p>
   * The following fields in the <code>ProgressEvent</code> apply:
   * <ul>
   * <li>(in) current the progress for the location currently being loaded
   * <li>(in) total the maximum progress for the location currently being loaded
   * <li>(in) widget the <code>Browser</code> whose current URL is being loaded
   * </ul>
   *
   * @param event the <code>ProgressEvent</code> related to the loading of the
   *          current location of a <code>Browser</code>
   */
  public void changed( ProgressEvent event );

  /**
   * This method is called when the current location has been completely loaded.
   * <p>
   * <p>
   * The following fields in the <code>ProgressEvent</code> apply:
   * <ul>
   * <li>(in) widget the <code>Browser</code> whose current URL has been loaded
   * </ul>
   *
   * @param event the <code>ProgressEvent</code> related to the
   *          <code>Browser</code> that has loaded its current URL.
   */
  public void completed( ProgressEvent event );
}
