/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.sashkit;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_SELECTION;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.internal.protocol.ControlOperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Sash;


public class SashOperationHandler extends ControlOperationHandler<Sash> {

  public SashOperationHandler( Sash sash ) {
    super( sash );
  }

  @Override
  public void handleNotify( Sash sash, String eventName, JsonObject properties ) {
    if( EVENT_SELECTION.equals( eventName ) ) {
      handleNotifySelection( sash, properties );
    } else {
      super.handleNotify( sash, eventName, properties );
    }
  }

  /*
   * PROTOCOL NOTIFY Selection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   * @param detail (string) "drag" if sash is dragging
   * @param x (int) the sash x coordinate
   * @param y (int) the sash y coordinate
   * @param width (int) the sash width
   * @param height (int) the sash y height
   */
  public void handleNotifySelection( Sash sash, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.Selection, properties );
    sash.notifyListeners( SWT.Selection, event );
  }

}
