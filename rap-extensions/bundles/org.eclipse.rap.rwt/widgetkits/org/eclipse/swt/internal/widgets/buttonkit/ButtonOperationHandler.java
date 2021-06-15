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
package org.eclipse.swt.internal.widgets.buttonkit;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_DEFAULT_SELECTION;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_SELECTION;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.ControlOperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;


public class ButtonOperationHandler extends ControlOperationHandler<Button> {

  private static final String PROP_SELECTION = "selection";
  private static final String PROP_TEXT = "text";

  public ButtonOperationHandler( Button button ) {
    super( button );
  }

  @Override
  public void handleSet( Button button, JsonObject properties ) {
    super.handleSet( button, properties );
    handleSetSelection( button, properties );
    handleSetText( button, properties );
  }

  @Override
  public void handleNotify( Button button, String eventName, JsonObject properties ) {
    if( EVENT_SELECTION.equals( eventName ) ) {
      handleNotifySelection( button, properties );
    } else if( EVENT_DEFAULT_SELECTION.equals( eventName ) ) {
      handleNotifyDefaultSelection( button, properties );
    } else {
      super.handleNotify( button, eventName, properties );
    }
  }

  /*
   * PROTOCOL SET selection
   *
   * @param selection (boolean) true if the button was selected, otherwise false
   */
  public void handleSetSelection( Button button, JsonObject properties ) {
    JsonValue selection = properties.get( PROP_SELECTION );
    if( selection != null ) {
      button.setSelection( selection.asBoolean() );
    }
  }

  /*
   * PROTOCOL SET text
   *
   * @param text (String) the new button text
   */
  public void handleSetText( Button button, JsonObject properties ) {
    JsonValue text = properties.get( PROP_TEXT );
    if( text != null ) {
      button.setText( text.asString() );
    }
  }

  /*
   * PROTOCOL NOTIFY Selection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   */
  public void handleNotifySelection( Button button, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.Selection, properties );
    if( ( button.getStyle() & SWT.RADIO ) != 0 && !button.getSelection() ) {
      event.time = -1;
    }
    button.notifyListeners( SWT.Selection, event );
  }

  /*
   * PROTOCOL NOTIFY DefaultSelection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   */
  public void handleNotifyDefaultSelection( Button button, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.DefaultSelection, properties );
    button.notifyListeners( SWT.DefaultSelection, event );
  }

}
