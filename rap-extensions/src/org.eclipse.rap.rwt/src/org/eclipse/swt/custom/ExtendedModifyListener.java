/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.custom;

import org.eclipse.swt.internal.SWTEventListener;

/**
 * Classes which implement this interface provide a method
 * that deals with the event that is generated when text
 * is modified.
 *
 * @see ExtendedModifyEvent
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public interface ExtendedModifyListener extends SWTEventListener {

  /**
   * This method is called after a text change occurs.
   * <p>
   * The following event fields are used:<ul>
   * <li>event.start the start offset of the new text (input)</li>
   * <li>event.length the length of the new text (input)</li>
   * <li>event.replacedText the replaced text (input)</li>
   * </ul>
   *
   * @param event the given event
   * @see ExtendedModifyEvent
   */
  public void modifyText(ExtendedModifyEvent event);
}


