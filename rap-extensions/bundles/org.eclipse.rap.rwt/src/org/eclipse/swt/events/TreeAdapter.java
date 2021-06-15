/*******************************************************************************
 * Copyright (c) 2002, 2011 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.events;


/**
 * This adapter class provides default implementations for the methods described
 * by the <code>TreeListener</code> interface.
 * <p>
 * Classes that wish to deal with <code>TreeEvent</code>s can extend this
 * class and override only the methods which they are interested in.
 * </p>
 * 
 * @see TreeListener
 * @see TreeEvent
 */
public abstract class TreeAdapter implements TreeListener {

  /**
   * Sent when a tree branch is collapsed. The default behavior is to do
   * nothing.
   * 
   * @param e an event containing information about the tree operation
   */
  public void treeCollapsed( TreeEvent e ) {
  }

  /**
   * Sent when a tree branch is expanded. The default behavior is to do nothing.
   * 
   * @param e an event containing information about the tree operation
   */
  public void treeExpanded( TreeEvent e ) {
  }
}
