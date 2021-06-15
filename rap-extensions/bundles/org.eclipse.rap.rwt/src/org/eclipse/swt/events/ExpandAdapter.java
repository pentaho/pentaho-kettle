/*******************************************************************************
 * Copyright (c) 2008, 2011 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.events;

/**
 * This adapter class provides default implementations for the methods described
 * by the <code>ExpandListener</code> interface.
 * <p>
 * Classes that wish to deal with <code>ExpandEvent</code>s can extend this
 * class and override only the methods which they are interested in.
 * </p>
 *
 * @see ExpandListener
 * @see ExpandEvent
 * @since 1.2
 */
public abstract class ExpandAdapter implements ExpandListener {

  /**
   * Sent when an item is collapsed. The default behavior is to do nothing.
   *
   * @param e an event containing information about the operation
   */
  public void itemCollapsed( ExpandEvent e ) {
  }

  /**
   * Sent when an item is expanded. The default behavior is to do nothing.
   *
   * @param e an event containing information about the operation
   */
  public void itemExpanded( ExpandEvent e ) {
  }
}
