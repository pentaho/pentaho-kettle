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


import org.eclipse.swt.graphics.Rectangle;

/**
 * Instances of this class represent sources of touch input that generate <code>Touch</code> objects.
 * They also provide information about the input source, which is important for interpreting the
 * information in the <code>Touch</code> object.
 * <p>
 * Instances of this class can be marked as direct or indirect:
 * <ul>
 * <li>When an instance is <em>direct</em>, the touch source is a touch-sensitive digitizer surface such
 * as a tablet or a touch screen. There is a one-to-one mapping between a touch point and a location in
 * a window.
 * </li><li>
 * When an instance is <em>indirect</em> (more precisely, not direct), the touch source is a track pad or
 * other device that normally moves the cursor, but is also able to interpret multiple touches on its surface.
 * In this case there is not a one-to-one mapping between the location of the touch on the device and a
 * location on the display because the user can remove their finger or stylus and touch another part of
 * the device and resume what they were doing.
 * </li>
 * </ul>
 *
 * @see Touch
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 1.4
 */
public final class TouchSource {
  int /*long*/ handle;
  boolean direct;
  Rectangle bounds;

  /**
   * Constructs a new touch source from the given inputs.
   *
   * @param direct Is the touch source direct or indirect?
   * @param height height of the source in pixels.
   * @param width width of the source in pixels.
   */
  TouchSource( int /*long*/ handle, boolean direct, Rectangle bounds ) {
    this.handle = handle;
    this.direct = direct;
    this.bounds = bounds;
  }

  /**
   * Returns the type of touch input this source generates, <code>true</code> for direct or <code>false</code> for indirect.
   *
   * @return <code>true</code> if the input source is direct, or <code>false</code> otherwise
   */
  public boolean isDirect() {
    return direct;
  }

  /**
   * Returns the bounding rectangle of the device. For a direct source, this corresponds to the
   * bounds of the display device in pixels. For an indirect source, this contains the size of the
   * device in pixels.
   * <p>
   * Note that the x and y values may not necessarily be zero if the TouchSource is a direct source.
   * @return Rectangle The bounding rectangle of the input source.
   */
  public Rectangle getBounds() {
    return new Rectangle( bounds.x, bounds.y, bounds.width, bounds.height );
  }

  /**
   * Returns a string containing a concise, human-readable
   * description of the receiver.
   *
   * @return a string representation of the event
   */
  @Override
  public String toString() {
    return "TouchSource {handle=" + handle + " direct=" + direct + " bounds=" + bounds + "}";
  }

}