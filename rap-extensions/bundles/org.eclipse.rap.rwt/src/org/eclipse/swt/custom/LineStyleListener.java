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
 * that can provide the style information for a line that
 * is to be drawn.
 *
 * @see LineStyleEvent
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public interface LineStyleListener extends SWTEventListener {

  /**
   * This method is called when a line is about to be drawn in order to get the
   * line's style information.
   * <p>
   * The following event fields are used:<ul>
   * <li>event.lineOffset line start offset (input)</li>
   * <li>event.lineText line text (input)</li>
   * <li>event.styles array of StyleRanges, need to be in order (output)</li>
   * </ul>
   *
   * @param event the given event
   * @see LineStyleEvent
   */
  public void lineGetStyle(LineStyleEvent event);
}
