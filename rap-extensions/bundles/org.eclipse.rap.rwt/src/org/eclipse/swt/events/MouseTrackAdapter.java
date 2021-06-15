/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.events;

/**
 * This adapter class provides default implementations for the methods described
 * by the <code>MouseTrackListener</code> interface.
 * <p>
 * Classes that wish to deal with <code>MouseEvent</code>s which occur as the
 * mouse pointer passes (or hovers) over controls can extend this class and
 * override only the methods which they are interested in.
 * </p>
 * 
 * @see MouseTrackListener
 * @see MouseEvent
 * @since 1.3
 */
public abstract class MouseTrackAdapter implements MouseTrackListener {

  /**
   * Sent when the mouse pointer passes into the area of the screen covered by a
   * control. The default behavior is to do nothing.
   * 
   * @param e an event containing information about the mouse enter
   */
  public void mouseEnter( MouseEvent e ) {
  }

  /**
   * Sent when the mouse pointer passes out of the area of the screen covered by
   * a control. The default behavior is to do nothing.
   * 
   * @param e an event containing information about the mouse exit
   */
  public void mouseExit( MouseEvent e ) {
  }

  /**
   * Sent when the mouse pointer hovers (that is, stops moving for an (operating
   * system specified) period of time) over a control. The default behavior is
   * to do nothing.
   * 
   * @param e an event containing information about the hover
   */
  public void mouseHover( MouseEvent e ) {
  }
}
