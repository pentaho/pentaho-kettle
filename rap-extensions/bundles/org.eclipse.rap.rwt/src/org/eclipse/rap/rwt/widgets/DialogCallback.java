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

import org.eclipse.swt.internal.SerializableCompatibility;
import org.eclipse.swt.widgets.Dialog;


/**
 * This callback interface is used to inform application code that a dialog was closed.
 *
 * @since 2.0
 * @see Dialog#open(DialogCallback)
 */
public interface DialogCallback extends SerializableCompatibility {

  /**
   * This method is called after a dialog was closed. The meaning of the <code>returnCode</code>
   * is defined by the respective <code>Dialog</code> implementation but usually indicates how the
   * dialog was left. For example, pressing the 'OK' button would lead to the
   * <code>returnCode</code> {@link org.eclipse.swt.SWT#OK SWT.OK}.
   *
   * @param returnCode {@link org.eclipse.swt.SWT#CANCEL SWT.CANCEL} if the dialog was closed with
   *   the shells' close button, a dialog-specific return code otherwise.
   * @see org.eclipse.swt.SWT SWT
   * @see org.eclipse.swt.widgets.Dialog Dialog
   * @see org.eclipse.swt.widgets.Dialog#open(DialogCallback) Dialog#open(DialogCallback)
   */
  void dialogClosed( int returnCode );

}
