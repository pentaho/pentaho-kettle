/*******************************************************************************
 * Copyright (c) 2010, 2011 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;


/**
 * Instances of this class represent the system tray that is part of the task
 * bar status area on some operating systems.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see Display#getSystemTray
 * @since 1.4
 */
public class Tray extends Widget {

  /**
   * Returns the item at the given, zero-relative index in the receiver. Throws
   * an exception if the index is out of range.
   *
   * @param index the index of the item to return
   * @return the item at the given index
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_INVALID_RANGE - if the index is not between 0 and
   *              the number of elements in the list minus 1 (inclusive)</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.4
   */
  public TrayItem getItem( int index ) {
    checkWidget();
    if( index < 0 || index >= getItemCount() ) {
      SWT.error( SWT.ERROR_INVALID_RANGE );
    }
    return null;
  }

  /**
   * Returns the number of items contained in the receiver.
   *
   * @return the number of items
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.4
   */
  public int getItemCount() {
    checkWidget();
    return 0;
  }

  /**
   * Returns an array of <code>TrayItem</code>s which are the items in the
   * receiver.
   * <p>
   * Note: This is not the actual structure used by the receiver to maintain its
   * list of items, so modifying the array will not affect the receiver.
   * </p>
   *
   * @return the items in the receiver
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.4
   */
  public TrayItem[] getItems() {
    checkWidget();
    return new TrayItem[ 0 ];
  }
  
}
