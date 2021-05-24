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

import org.eclipse.swt.events.*;

/**
 * This event is sent by the StyledTextContent implementor when a change to
 * the text occurs.
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public class TextChangedEvent extends TypedEvent {

  static final long serialVersionUID = 3258696524257835065L;

  /**
   * Create the TextChangedEvent to be used by the StyledTextContent implementor.
   * <p>
   *
   * @param source the object that will be sending the TextChangedEvent,
   * 	cannot be null
   */
  public TextChangedEvent(StyledTextContent source) {
    super(source);
  }
}
