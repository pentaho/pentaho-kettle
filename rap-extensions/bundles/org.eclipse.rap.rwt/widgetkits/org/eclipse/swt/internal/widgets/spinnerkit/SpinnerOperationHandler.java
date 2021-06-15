/*******************************************************************************
 * Copyright (c) 2013, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.spinnerkit;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_DEFAULT_SELECTION;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_MODIFY;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_SELECTION;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.ControlOperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Spinner;


public class SpinnerOperationHandler extends ControlOperationHandler<Spinner> {

  private static final String PROP_SELECTION = "selection";

  public SpinnerOperationHandler( Spinner spinner ) {
    super( spinner );
  }

  @Override
  public void handleSet( Spinner spinner, JsonObject properties ) {
    super.handleSet( spinner, properties );
    handleSetSelection( spinner, properties );
  }

  @Override
  public void handleNotify( Spinner spinner, String eventName, JsonObject properties ) {
    if( EVENT_SELECTION.equals( eventName ) ) {
      handleNotifySelection( spinner, properties );
    } else if( EVENT_DEFAULT_SELECTION.equals( eventName ) ) {
      handleNotifyDefaultSelection( spinner, properties );
    } else if( EVENT_MODIFY.equals( eventName ) ) {
      handleNotifyModify();
    } else {
      super.handleNotify( spinner, eventName, properties );
    }
  }

  /*
   * PROTOCOL SET selection
   *
   * @param selection (int) the spinner selection value
   */
  public void handleSetSelection( Spinner spinner, JsonObject properties ) {
    JsonValue value = properties.get( PROP_SELECTION );
    if( value != null ) {
      spinner.setSelection( value.asInt() );
    }
  }

  /*
   * PROTOCOL NOTIFY Selection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   */
  public void handleNotifySelection( Spinner spinner, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.Selection, properties );
    spinner.notifyListeners( SWT.Selection, event );
  }

  /*
   * PROTOCOL NOTIFY DefaultSelection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   */
  public void handleNotifyDefaultSelection( Spinner spinner, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.DefaultSelection, properties );
    spinner.notifyListeners( SWT.DefaultSelection, event );
  }

  /*
   * PROTOCOL NOTIFY Modify
   * ignored, Modify event is fired when set selection
   */
  public void handleNotifyModify() {
  }

}
