/*******************************************************************************
 * Copyright (c) 2011, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.widgets;

import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.swt.widgets.Dialog;


/**
 * Utility class to work with non-blocking dialogs.
 *
 * @see Dialog
 * @since 2.0
 * @deprecated Use the methods on <code>Dialog</code> instead
 */
@Deprecated
public final class DialogUtil {

  /**
   * Opens the given <code>dialog</code> in a non-blocking way and brings it to the front of the
   * display. If given, the <code>dialogCallback</code> is notified when the dialog is closed.
   * <p>
   * Use this method instead of the <code>open()</code> method from the respective
   * <code>Dialog</code> implementation when running in <em>JEE_COMPATIBILITY</em> mode.
   * </p>
   *
   * @param dialog the dialog to open, must not be <code>null</code>.
   * @param dialogCallback the callback to be notified when the dialog was closed or
   *   <code>null</code> if no callback should be notified.
   *
   * @see Dialog
   * @see DialogCallback
   * @see org.eclipse.rap.rwt.application.Application.OperationMode
   * @deprecated Use <code>Dialog.open( DialogCallback )</code> instead
   */
  @Deprecated
  public static void open( Dialog dialog, DialogCallback dialogCallback ) {
    ParamCheck.notNull( dialog, "dialog" );
    dialog.open( dialogCallback );
  }

  private DialogUtil() {
    // prevent instantiation
  }

}
