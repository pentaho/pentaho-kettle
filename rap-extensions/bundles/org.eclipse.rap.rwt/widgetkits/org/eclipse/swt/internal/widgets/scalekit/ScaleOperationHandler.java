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
package org.eclipse.swt.internal.widgets.scalekit;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_SELECTION;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.ControlOperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Scale;


public class ScaleOperationHandler extends ControlOperationHandler<Scale> {

  private static final String PROP_SELECTION = "selection";

  public ScaleOperationHandler( Scale scale ) {
    super( scale );
  }

  @Override
  public void handleSet( Scale scale, JsonObject properties ) {
    super.handleSet( scale, properties );
    handleSetSelection( scale, properties );
  }

  @Override
  public void handleNotify( Scale scale, String eventName, JsonObject properties ) {
    if( EVENT_SELECTION.equals( eventName ) ) {
      handleNotifySelection( scale, properties );
    } else {
      super.handleNotify( scale, eventName, properties );
    }
  }

  /*
   * PROTOCOL SET selection
   *
   * @param selection (int) the scale selection value
   */
  public void handleSetSelection( Scale scale, JsonObject properties ) {
    JsonValue value = properties.get( PROP_SELECTION );
    if( value != null ) {
      scale.setSelection( value.asInt() );
    }
  }

  /*
   * PROTOCOL NOTIFY Selection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   */
  public void handleNotifySelection( Scale scale, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.Selection, properties );
    scale.notifyListeners( SWT.Selection, event );
  }

}
