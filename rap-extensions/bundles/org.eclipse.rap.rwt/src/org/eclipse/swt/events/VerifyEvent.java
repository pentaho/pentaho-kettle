/*******************************************************************************
 * Copyright (c) 2002, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 *     EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.events;

import org.eclipse.swt.widgets.Event;


/**
 * Instances of this class are sent as a result of
 * text being modified.
 *
 * @see VerifyListener
 */
public final class VerifyEvent extends KeyEvent {

  private static final long serialVersionUID = 1L;

  /**
   * the new text that will be inserted.
   * Setting this field will change the text that is about to
   * be inserted or deleted.
   */
  public String text;

  /**
   * the range of text being modified.
   * Setting these fields has no effect.
   */
  public int start, end;


  /**
   * Constructs a new instance of this class based on the
   * information in the given untyped event.
   *
   * @param event the untyped event containing the information
   */
  public VerifyEvent( Event event ) {
    super( event );
    this.start = event.start;
    this.end = event.end;
    this.text = event.text;
  }

  /**
   * Returns a string containing a concise, human-readable
   * description of the receiver.
   *
   * @return a string representation of the event
   */
  @Override
  public String toString() {
    String string = super.toString();
    return string.substring( 0, string.length() - 1 ) // remove trailing '}'
      // differs from SWT: no doit in superclass, thus add explicitly
      + " doit=" + doit
      + " start=" + start
      + " end=" + end
      + " text=" + text
      + "}";
  }
}
