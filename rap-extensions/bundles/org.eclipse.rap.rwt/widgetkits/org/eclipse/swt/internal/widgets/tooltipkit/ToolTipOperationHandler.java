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
package org.eclipse.swt.internal.widgets.tooltipkit;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_SELECTION;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.WidgetOperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolTip;


public class ToolTipOperationHandler extends WidgetOperationHandler<ToolTip> {

  private static final String PROP_VISIBLE = "visible";

  public ToolTipOperationHandler( ToolTip toolTip ) {
    super( toolTip );
  }

  @Override
  public void handleSet( ToolTip toolTip, JsonObject properties ) {
    handleSetVisible( toolTip, properties );
  }

  @Override
  public void handleNotify( ToolTip toolTip, String eventName, JsonObject properties ) {
    if( EVENT_SELECTION.equals( eventName ) ) {
      handleNotifySelection( toolTip, properties );
    } else {
      super.handleNotify( toolTip, eventName, properties );
    }
  }

  /*
   * PROTOCOL SET visible
   *
   * @param visible (int) true if tooltip is visible, false otherwise
   */
  public void handleSetVisible( ToolTip toolTip, JsonObject properties ) {
    JsonValue value = properties.get( PROP_VISIBLE );
    if( value != null ) {
      toolTip.setVisible( value.asBoolean() );
    }
  }

  /*
   * PROTOCOL NOTIFY Selection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   */
  public void handleNotifySelection( ToolTip toolTip, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.Selection, properties );
    toolTip.notifyListeners( SWT.Selection, event );
  }

}
