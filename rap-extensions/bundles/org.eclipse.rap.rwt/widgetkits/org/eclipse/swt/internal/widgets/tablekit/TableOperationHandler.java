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
package org.eclipse.swt.internal.widgets.tablekit;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_DEFAULT_SELECTION;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_ITEM;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_SELECTION;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_SET_DATA;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.find;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.ControlOperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.widgets.CellToolTipUtil;
import org.eclipse.swt.internal.widgets.ICellToolTipAdapter;
import org.eclipse.swt.internal.widgets.ICellToolTipProvider;
import org.eclipse.swt.internal.widgets.ITableAdapter;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;


public class TableOperationHandler extends ControlOperationHandler<Table> {

  private static final String PROP_SELECTION = "selection";
  private static final String PROP_SCROLL_LEFT = "scrollLeft";
  private static final String PROP_TOP_ITEM_INDEX = "topItemIndex";
  private static final String PROP_FOCUS_ITEM = "focusItem";
  private static final String METHOD_RENDER_TOOLTIP_TEXT = "renderToolTipText";

  public TableOperationHandler( Table table ) {
    super( table );
  }

  @Override
  public void handleSet( Table table, JsonObject properties ) {
    super.handleSet( table, properties );
    handleSetSelection( table, properties );
    handleSetScrollLeft( table, properties );
    handleSetTopItemIndex( table, properties );
    handleSetFocusItem( table, properties );
  }

  @Override
  public void handleCall( Table table, String method, JsonObject properties ) {
    if( METHOD_RENDER_TOOLTIP_TEXT.equals( method ) ) {
      handleCallRenderToolTipText( table, properties );
    }
  }

  @Override
  public void handleNotify( Table table, String eventName, JsonObject properties ) {
    if( EVENT_SELECTION.equals( eventName ) ) {
      handleNotifySelection( table, properties );
    } else if( EVENT_DEFAULT_SELECTION.equals( eventName ) ) {
      handleNotifyDefaultSelection( table, properties );
    } else if( EVENT_SET_DATA.equals( eventName ) ) {
      handleNotifySetData();
    } else {
      super.handleNotify( table, eventName, properties );
    }
  }

  /*
   * PROTOCOL SET selection
   *
   * @param selection ([string]) array with ids of selected items
   */
  public void handleSetSelection( Table table, JsonObject properties ) {
    JsonValue values = properties.get( PROP_SELECTION );
    if( values != null ) {
      JsonArray itemIds = values.asArray();
      int[] newSelection = new int[ itemIds.size() ];
      for( int i = 0; i < newSelection.length; i++ ) {
        String itemId = itemIds.get( i ).asString();
        TableItem item = getItem( table, itemId );
        if( item != null ) {
          newSelection[ i ] = table.indexOf( item );
        } else {
          newSelection[ i ] = -1;
        }
      }
      table.deselectAll();
      table.select( newSelection );
    }
  }

  /*
   * PROTOCOL SET scrollLeft
   *
   * @param scrollLeft (int) left scroll offset in pixels
   */
  public void handleSetScrollLeft( Table table, JsonObject properties ) {
    JsonValue value = properties.get( PROP_SCROLL_LEFT );
    if( value != null ) {
      int scrollLeft = value.asInt();
      getTableAdapter( table ).setLeftOffset( scrollLeft );
      setScrollBarSelection( table.getHorizontalBar(), scrollLeft );
    }
  }

  /*
   * PROTOCOL SET topItemIndex
   *
   * @param topItemIndex (int) visual index of the item, which is on the top of the table
   */
  public void handleSetTopItemIndex( Table table, JsonObject properties ) {
    JsonValue value = properties.get( PROP_TOP_ITEM_INDEX );
    if( value != null ) {
      int topItemIndex = value.asInt();
      table.setTopIndex( topItemIndex );
      int scrollTop = topItemIndex * table.getItemHeight();
      setScrollBarSelection( table.getVerticalBar(), scrollTop );
    }
  }

  /*
   * PROTOCOL SET focusItem
   *
   * @param focusItem (string) id of focus item
   */
  public void handleSetFocusItem( Table table, JsonObject properties ) {
    JsonValue value = properties.get( PROP_FOCUS_ITEM );
    if( value != null ) {
      TableItem item = getItem( table, value.asString() );
      if( item != null ) {
        getTableAdapter( table ).setFocusIndex( table.indexOf( item ) );
      }
    }
  }

  /*
   * PROTOCOL CALL renderToolTipText
   *
   * @param item (string) id of the hovered item
   * @param column (int) column index of the hovered cell
   */
  public void handleCallRenderToolTipText( Table table, JsonObject properties ) {
    ICellToolTipAdapter adapter = CellToolTipUtil.getAdapter( table );
    ICellToolTipProvider provider = adapter.getCellToolTipProvider();
    if( provider != null ) {
      TableItem item = getItem( table, properties.get( "item" ).asString() );
      int columnIndex = properties.get( "column" ).asInt();
      if( item != null && ( columnIndex == 0 || columnIndex < table.getColumnCount() ) ) {
        provider.getToolTipText( item, columnIndex );
      }
    }
  }

  /*
   * PROTOCOL NOTIFY Selection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   * @param detail (string) "check" if checkbox is selected, "hyperlink" if RWT hyperlink is
   *        selected
   * @param item (string) id of selected item
   * @param text (string) the value of href attribute or content of the selected RWT hyperlink
   */
  public void handleNotifySelection( Table table, JsonObject properties ) {
    TableItem item = getItem( table, properties.get( EVENT_PARAM_ITEM ).asString() );
    if( item != null ) {
      Event event = createSelectionEvent( SWT.Selection, properties );
      event.item = item;
      table.notifyListeners( SWT.Selection, event );
    }
  }

  /*
   * PROTOCOL NOTIFY DefaultSelection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   * @param detail (string) "check" is checkbox is selected
   * @param item (string) id of selected item
   */
  public void handleNotifyDefaultSelection( Table table, JsonObject properties ) {
    TableItem item = getItem( table, properties.get( EVENT_PARAM_ITEM ).asString() );
    if( item == null ) {
      item = getFocusItem( table );
    }
    Event event = createSelectionEvent( SWT.DefaultSelection, properties );
    event.item = item;
    table.notifyListeners( SWT.DefaultSelection, event );
  }

  /*
   * PROTOCOL NOTIFY SetData
   * ignored, SetData event is fired when set topItemIndex
   */
  public void handleNotifySetData() {
  }

  private static TableItem getItem( Table table, String itemId ) {
    TableItem item;
    String[] idParts = itemId.split( "#" );
    if( idParts.length == 2 ) {
      int index = Integer.parseInt( idParts[ 1 ] );
      item = table.getItem( index );
    } else {
      item = ( TableItem )find( table, itemId );
    }
    return item;
  }

  private static void setScrollBarSelection( ScrollBar scrollBar, int selection ) {
    if( scrollBar != null ) {
      scrollBar.setSelection( selection );
    }
  }

  private static TableItem getFocusItem( Table table ) {
    TableItem result = null;
    int focusIndex = getTableAdapter( table ).getFocusIndex();
    if( focusIndex != -1 ) {
      result = table.getItem( focusIndex );
    }
    return result;
  }

  @Override
  protected boolean allowMouseEvent( Table table, int x, int y ) {
    return super.allowMouseEvent( table, x, y ) && y >= table.getHeaderHeight();
  }

  private static ITableAdapter getTableAdapter( Table table ) {
    return table.getAdapter( ITableAdapter.class );
  }

}
