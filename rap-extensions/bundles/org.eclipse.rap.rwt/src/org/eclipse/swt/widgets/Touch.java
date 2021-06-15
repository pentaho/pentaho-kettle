/*******************************************************************************
 * Copyright (c) 2010, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.widgets;

/**
 * Instances of this class are created in response to a
 * touch-based input device being touched. They are found
 * in the <code>touches</code> field of an Event or TouchEvent.
 *
 * @see org.eclipse.swt.events.TouchEvent
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 1.4
 */
public final class Touch {

  /**
   * The unique identity of the touch. Use this value to track changes to a touch
   * during the touch's life. Two touches may have the same identity even if they
   * come from different sources.
   */
	public long id;

  /**
   * The object representing the input source that generated the touch.
   */
	public TouchSource source;

  /**
   * The state of this touch at the time it was generated. If this field is 0
   * then the finger is still touching the device but has not moved
   * since the last <code>TouchEvent</code> was generated.
   *
   * @see org.eclipse.swt.SWT#TOUCHSTATE_DOWN
   * @see org.eclipse.swt.SWT#TOUCHSTATE_MOVE
   * @see org.eclipse.swt.SWT#TOUCHSTATE_UP
   */
	public int state;

  /**
   * A flag indicating that the touch is the first touch from a previous
   * state of no touch points. Once designated as such, the touch remains
   * the primary touch until all fingers are removed from the device.
   */
	public boolean primary;

	/**
	 * The X location of the touch in TouchSource coordinates
	 */
	public int x;

	/**
	 * The Y location of the touch in TouchSource coordinates
	 */
	public int y;

    /**
     * Constructs a new touch state from the given inputs.
     *
     * @param identity Identity of the touch
     * @param source Object representing the device that generated the touch
     * @param state One of the state constants representing the state of this touch
     * @param primary Whether or not the touch is the primary touch
     * @param x X location of the touch in screen coordinates
     * @param y Y location of the touch in screen coordinates
     */
    Touch (long identity, TouchSource source, int state, boolean primary, int x, int y) {
    	this.id = identity;
    	this.source = source;
    	this.state = state;
    	this.primary = primary;
    	this.x = x;
    	this.y = y;
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the event
     */
    @Override
    public String toString() {
    	return "Touch {id=" + id
    	+ " source=" + source
    	+ " state=" + state
    	+ " primary=" + primary
    	+ " x=" + x
    	+ " y=" + y
    	+ "}";
    }

}
