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
package org.eclipse.swt.internal.internal.widgets.controldecoratorkit;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_DEFAULT_SELECTION;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_SELECTION;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.internal.protocol.WidgetOperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.widgets.ControlDecorator;
import org.eclipse.swt.widgets.Event;


public class ControlDecoratorOperationHandler extends WidgetOperationHandler<ControlDecorator> {

  public ControlDecoratorOperationHandler( ControlDecorator decorator ) {
    super( decorator );
  }

  @Override
  public void handleNotify( ControlDecorator decorator, String eventName, JsonObject properties ) {
    if( EVENT_SELECTION.equals( eventName ) ) {
      handleNotifySelection( decorator, properties );
    } else if( EVENT_DEFAULT_SELECTION.equals( eventName ) ) {
      handleNotifyDefaultSelection( decorator, properties );
    } else {
      super.handleNotify( decorator, eventName, properties );
    }
  }

  /*
   * PROTOCOL NOTIFY Selection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   */
  public void handleNotifySelection( ControlDecorator decorator, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.Selection, properties );
    decorator.notifyListeners( SWT.Selection, event );
  }

  /*
   * PROTOCOL NOTIFY DefaultSelection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   */
  public void handleNotifyDefaultSelection( ControlDecorator decorator, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.DefaultSelection, properties );
    decorator.notifyListeners( SWT.DefaultSelection, event );
  }

}
