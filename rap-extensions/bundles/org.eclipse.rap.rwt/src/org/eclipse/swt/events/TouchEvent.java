/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.events;


import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Touch;
/**
 * Instances of this class are sent in response to
 * a touch-based input source being touched.
 *
 * @see TouchListener
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * 
 * @since 1.4
 */
public class TouchEvent extends TypedEvent {
  
  /**
   * The set of touches representing the state of all contacts with touch input
   * device at the time the event was generated.
   * 
   * @see org.eclipse.swt.widgets.Touch
   */
  public Touch[] touches;

  /**
   * The state of the keyboard modifier keys and mouse masks 
   * at the time the event was generated.
   * 
   * @see org.eclipse.swt.SWT#MODIFIER_MASK
   * @see org.eclipse.swt.SWT#BUTTON_MASK
   */
  public int stateMask;
  
  /**
   * The widget-relative x coordinate of the pointer
   * at the time the touch occurred.
   */
  public int x;
  
  /**
   * The widget-relative y coordinate of the pointer
   * at the time the touch occurred.
   */ 
  public int y;
  
  static final long serialVersionUID = -8348741538373572182L;
  
  /**
   * Constructs a new instance of this class based on the
   * information in the given untyped event.
   *
   * @param e the untyped event containing the information
   */
  public TouchEvent( Event e ) {
    super( e );
// [if] Event touches field is missing
//    this.touches = e.touches;
    this.stateMask = e.stateMask;
    this.x = e.x;
    this.y = e.y;
  }
  
  /**
   * Returns a string containing a concise, human-readable
   * description of the receiver.
   *
   * @return a string representation of the event
   */
  public String toString() {
    String string = super.toString();
    string = string.substring( 0, string.length() - 1 ); // remove trailing '}'
    string += " stateMask=" + stateMask
           + " x=" + x
           + " y=" + y;
    if( touches != null ) {
      for( int i = 0; i < touches.length; i++ ) {
        string += "\n     " + touches[ i ].toString();
      }
      string += "\n";
    }
    string += "}";
    return string;
  }
}
