/*******************************************************************************
 * Copyright (c) 2002, 2012 Innoopract Informationssysteme GmbH.
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

import org.eclipse.swt.internal.SWTEventObject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;


/**
 * This is the super class for all typed event classes provided
 * by SWT. Typed events contain particular information which is
 * applicable to the event occurrence.
 *
 * @see org.eclipse.swt.widgets.Event
 */
public class TypedEvent extends SWTEventObject {
  private static final long serialVersionUID = 1L;

  /**
   * the display where the event occurred
   *
   * @since 1.2
   */
  public Display display;

  /**
   * the widget that issued the event
   */
  public Widget widget;

  /**
   * the time that the event occurred.
   * 
   * NOTE: This field is an unsigned integer and should
   * be AND'ed with 0xFFFFFFFFL so that it can be treated
   * as a signed long.
   * 
   * @since 2.0
   */ 
  public int time;
  
  /**
   * a field for application use
   */
  public Object data;

  /**
   * Constructs a new instance of this class.
   *
   * @param source the object that fired the event
   *
   * @since 1.3
   */
  public TypedEvent( Object source ) {
    super( source );
  }

  /**
   * Constructs a new instance of this class based on the
   * information in the argument.
   *
   * @param event the low level event to initialize the receiver with
   */
  public TypedEvent( Event event ) {
    super( event.widget );
    this.display = event.display;
    this.widget = event.widget;
    this.time = event.time;
    this.data = event.data;
  }

  // this implementation is extended by subclasses
  @Override
  public String toString() {
    return getName() + "{" + widget + " time=" + time + " data=" + data + "}";
  }

  private String getName() {
    String result = getClass().getName();
    int index = result.lastIndexOf( '.' );
    if( index != -1 ) {
      result = result.substring( index + 1, result.length() );
    }
    return result;
  }

}