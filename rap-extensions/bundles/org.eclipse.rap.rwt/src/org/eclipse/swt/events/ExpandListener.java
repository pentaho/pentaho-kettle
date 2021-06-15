/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
 * Classes which implement this interface provide methods that deal with the
 * expanding and collapsing of <code>ExpandItem</code>s.
 * <p>
 * After creating an instance of a class that implements this interface it can
 * be added to a <code>ExpandBar</code> control using the
 * <code>addExpandListener</code> method and removed using the
 * <code>removeExpandListener</code> method. When a item of the
 * <code>ExpandBar</code> is expanded or collapsed, the appropriate method
 * will be invoked.
 * </p>
 *
 * @see ExpandAdapter
 * @see ExpandEvent
 * @since 1.2
 */
public interface ExpandListener extends SWTEventListener {

  /**
   * Sent when an item is collapsed.
   *
   * @param e an event containing information about the operation
   */
  public void itemCollapsed( ExpandEvent e );

  /**
   * Sent when an item is expanded.
   *
   * @param e an event containing information about the operation
   */
  public void itemExpanded( ExpandEvent e );
}
