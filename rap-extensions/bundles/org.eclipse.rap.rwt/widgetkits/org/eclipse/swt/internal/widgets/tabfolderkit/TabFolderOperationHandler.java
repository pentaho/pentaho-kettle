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
package org.eclipse.swt.internal.widgets.tabfolderkit;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_ITEM;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_SELECTION;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.find;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.lifecycle.ProcessActionRunner;
import org.eclipse.rap.rwt.internal.protocol.ControlOperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;


public class TabFolderOperationHandler extends ControlOperationHandler<TabFolder> {

  private static final String PROP_SELECTION = "selection";

  public TabFolderOperationHandler( TabFolder folder ) {
    super( folder );
  }

  @Override
  public void handleSet( TabFolder folder, JsonObject properties ) {
    super.handleSet( folder, properties );
    handleSetSelection( folder, properties );
  }

  @Override
  public void handleNotify( TabFolder folder, String eventName, JsonObject properties ) {
    if( EVENT_SELECTION.equals( eventName ) ) {
      handleNotifySelection( folder, properties );
    } else {
      super.handleNotify( folder, eventName, properties );
    }
  }

  /*
   * PROTOCOL SET selection
   *
   * @param selection (string) the id of selected item
   */
  public void handleSetSelection( final TabFolder folder, JsonObject properties ) {
    JsonValue value = properties.get( PROP_SELECTION );
    if( value != null ) {
      final TabItem item = getItem( folder, value.asString() );
      ProcessActionRunner.add( new Runnable() {
        @Override
        public void run() {
          folder.setSelection( item );
          preserveProperty( folder, PROP_SELECTION, getId( item ) );
        }
      } );
    }
  }

  /*
   * PROTOCOL NOTIFY Selection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   * @param item (string) id of selected item
   */
  public void handleNotifySelection( TabFolder folder, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.Selection, properties );
    event.item = getItem( folder, properties.get( EVENT_PARAM_ITEM ).asString() );
    folder.notifyListeners( SWT.Selection, event );
  }

  private static TabItem getItem( TabFolder folder, String itemId ) {
    return ( TabItem )find( folder, itemId );
  }

}
