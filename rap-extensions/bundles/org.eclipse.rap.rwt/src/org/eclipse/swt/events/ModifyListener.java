/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.events;

import org.eclipse.swt.internal.SWTEventListener;

/**
 * Classes which implement this interface provide a method that deals with the
 * events that are generated when text is modified.
 * <p>
 * After creating an instance of a class that implements this interface it can
 * be added to a text widget using the <code>addModifyListener</code> method
 * and removed using the <code>removeModifyListener</code> method. When the
 * text is modified, the modifyText method will be invoked.
 * </p>
 * 
 * @see ModifyEvent
 */
// TODO [rh] Not yet implemented on: Combo, Spinner, CCombo, (StyledText)
public interface ModifyListener extends SWTEventListener {

  /**
   * Sent when the text is modified.
   * 
   * @param event an event containing information about the modify
   */
  public void modifyText( ModifyEvent event );
}
