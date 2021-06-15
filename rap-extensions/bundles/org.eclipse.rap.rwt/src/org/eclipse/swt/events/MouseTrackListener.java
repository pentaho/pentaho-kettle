/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.events;

import org.eclipse.swt.internal.SWTEventListener;

/**
 * Classes which implement this interface provide methods that deal with the
 * events that are generated as the mouse pointer passes (or hovers) over
 * controls.
 * <p>
 * After creating an instance of a class that implements this interface it can
 * be added to a control using the <code>addMouseTrackListener</code> method and
 * removed using the <code>removeMouseTrackListener</code> method. When the
 * mouse pointer passes into or out of the area of the screen covered by a
 * control or pauses while over a control, the appropriate method will be
 * invoked.
 * </p>
 * 
 * @see MouseTrackAdapter
 * @see MouseEvent
 * @since 1.3
 */
public interface MouseTrackListener extends SWTEventListener {

  /**
   * Sent when the mouse pointer passes into the area of the screen covered by a
   * control.
   * 
   * @param e an event containing information about the mouse enter
   */
  public void mouseEnter( MouseEvent e );

  /**
   * Sent when the mouse pointer passes out of the area of the screen covered by
   * a control.
   * 
   * @param e an event containing information about the mouse exit
   */
  public void mouseExit( MouseEvent e );

  /**
   * Sent when the mouse pointer hovers (that is, stops moving for an (operating
   * system specified) period of time) over a control.
   * 
   * @param e an event containing information about the hover
   */
  public void mouseHover( MouseEvent e );
}
