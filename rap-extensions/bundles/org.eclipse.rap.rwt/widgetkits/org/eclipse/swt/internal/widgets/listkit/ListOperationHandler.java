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
package org.eclipse.swt.internal.widgets.listkit;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_DEFAULT_SELECTION;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_SELECTION;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.ControlOperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.widgets.IListAdapter;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;


public class ListOperationHandler extends ControlOperationHandler<List> {

  private static final String PROP_TOP_INDEX = "topIndex";
  private static final String PROP_SELECTION = "selection";
  private static final String PROP_FOCUS_INDEX = "focusIndex";

  public ListOperationHandler( List list ) {
    super( list );
  }

  @Override
  public void handleSet( List list, JsonObject properties ) {
    super.handleSet( list, properties );
    handleSetTopIndex( list, properties );
    handleSetSelection( list, properties );
    handleSetFocusIndex( list, properties );
  }

  @Override
  public void handleNotify( List list, String eventName, JsonObject properties ) {
    if( EVENT_SELECTION.equals( eventName ) ) {
      handleNotifySelection( list, properties );
    } else if( EVENT_DEFAULT_SELECTION.equals( eventName ) ) {
      handleNotifyDefaultSelection( list, properties );
    } else {
      super.handleNotify( list, eventName, properties );
    }
  }

  /*
   * PROTOCOL SET topIndex
   *
   * @param topIndex (int) the index of the item, which is on the top of the list
   */
  public void handleSetTopIndex( List list, JsonObject properties ) {
    JsonValue value = properties.get( PROP_TOP_INDEX );
    if( value != null ) {
      list.setTopIndex( value.asInt() );
    }
  }

  /*
   * PROTOCOL SET selection
   *
   * @param selection ([int]) array with indices of selected items
   */
  public void handleSetSelection( List list, JsonObject properties ) {
    JsonValue value = properties.get( PROP_SELECTION );
    if( value != null ) {
      JsonArray arrayValue = value.asArray();
      int[] selection = new int[ arrayValue.size() ];
      for( int i = 0; i < selection.length; i++ ) {
        selection[ i ] = arrayValue.get( i ).asInt();
      }
      list.setSelection( selection );
    }
  }

  /*
   * PROTOCOL SET focusIndex
   *
   * @param focusIndex (int) the index of the item, which has focus
   */
  public void handleSetFocusIndex( List list, JsonObject properties ) {
    JsonValue value = properties.get( PROP_FOCUS_INDEX );
    if( value != null ) {
      list.getAdapter( IListAdapter.class ).setFocusIndex( value.asInt() );
    }
  }

  /*
   * PROTOCOL NOTIFY Selection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   */
  public void handleNotifySelection( List list, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.Selection, properties );
    list.notifyListeners( SWT.Selection, event );
  }

  /*
   * PROTOCOL NOTIFY DefaultSelection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   */
  public void handleNotifyDefaultSelection( List list, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.DefaultSelection, properties );
    list.notifyListeners( SWT.DefaultSelection, event );
  }

}
