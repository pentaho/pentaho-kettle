/*******************************************************************************
 * Copyright (c) 2002, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.custom;

import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;


/**
 * This event is sent when an event is generated in the <code>CTabFolder</code>.
 *
 * @since 1.0
 */
public class CTabFolderEvent extends TypedEvent {

  private static final long serialVersionUID = 1L;

  /**
   * The tab item for the operation.
   */
  public Widget item;

  /**
   * A flag indicating whether the operation should be allowed.
   * Setting this field to <code>false</code> will cancel the operation.
   * Applies to the close and showList events.
   */
  public boolean doit;

  /**
   * The widget-relative, x coordinate of the chevron button
   * at the time of the event.  Applies to the showList event.
   */
  public int x;

  /**
   * The widget-relative, y coordinate of the chevron button
   * at the time of the event.  Applies to the showList event.
   */
  public int y;

  /**
   * The width of the chevron button at the time of the event.
   * Applies to the showList event.
   */
  public int width;

  /**
   * The height of the chevron button at the time of the event.
   * Applies to the showList event.
   */
  public int height;

  CTabFolderEvent( Event event ) {
    super( event );
    x = event.x;
    y = event.y;
    width = event.width;
    height = event.height;
    item = event.item;
    doit = event.doit;
  }

  @Override
  public String toString() {
    String string = super.toString();
    return string.substring( 0, string.length() - 1 ) // remove trailing '}'
           + " item="
           + item
           + " doit="
           + doit
           + " x="
           + x
           + " y="
           + y
           + " width="
           + width
           + " height="
           + height
           + "}";
  }

}
