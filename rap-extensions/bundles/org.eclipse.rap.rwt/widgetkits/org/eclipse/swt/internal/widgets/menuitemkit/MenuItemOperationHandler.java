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
package org.eclipse.swt.internal.widgets.menuitemkit;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_HELP;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_SELECTION;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.WidgetOperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;


public class MenuItemOperationHandler extends WidgetOperationHandler<MenuItem> {

  private static final String PROP_SELECTION = "selection";

  public MenuItemOperationHandler( MenuItem item ) {
    super( item );
  }

  @Override
  public void handleSet( MenuItem item, JsonObject properties ) {
    handleSetSelection( item, properties );
  }

  @Override
  public void handleNotify( MenuItem item, String eventName, JsonObject properties ) {
    if( EVENT_SELECTION.equals( eventName ) ) {
      handleNotifySelection( item, properties );
    } else if( EVENT_HELP.equals( eventName ) ) {
      handleNotifyHelp( item );
    } else {
      super.handleNotify( item, eventName, properties );
    }
  }

  /*
   * PROTOCOL SET selection
   *
   * @param selection (boolean) true if the item was selected, otherwise false
   */
  public void handleSetSelection( MenuItem item, JsonObject properties ) {
    JsonValue selection = properties.get( PROP_SELECTION );
    if( selection != null ) {
      item.setSelection( selection.asBoolean() );
    }
  }

  /*
   * PROTOCOL NOTIFY Selection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   */
  public void handleNotifySelection( MenuItem item, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.Selection, properties );
    if( ( item.getStyle() & SWT.RADIO ) != 0 && !item.getSelection() ) {
      event.time = -1;
    }
    item.notifyListeners( SWT.Selection, event );
  }

  /*
   * PROTOCOL NOTIFY Help
   */
  public void handleNotifyHelp( MenuItem item ) {
    item.notifyListeners( SWT.Help, new Event() );
  }

}
