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
package org.eclipse.swt.internal.widgets.treekit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.find;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_COLLAPSE;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_DEFAULT_SELECTION;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_EXPAND;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_ITEM;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_SELECTION;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_SET_DATA;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.ControlOperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.widgets.CellToolTipUtil;
import org.eclipse.swt.internal.widgets.ICellToolTipAdapter;
import org.eclipse.swt.internal.widgets.ICellToolTipProvider;
import org.eclipse.swt.internal.widgets.ITreeAdapter;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;


public class TreeOperationHandler extends ControlOperationHandler<Tree> {

  private static final String PROP_SELECTION = "selection";
  private static final String PROP_SCROLL_LEFT = "scrollLeft";
  private static final String PROP_TOP_ITEM_INDEX = "topItemIndex";
  private static final String METHOD_RENDER_TOOLTIP_TEXT = "renderToolTipText";

  public TreeOperationHandler( Tree tree ) {
    super( tree );
  }

  @Override
  public void handleSet( Tree tree, JsonObject properties ) {
    super.handleSet( tree, properties );
    handleSetSelection( tree, properties );
    handleSetScrollLeft( tree, properties );
    handleSetTopItemIndex( tree, properties );
  }

  @Override
  public void handleCall( Tree tree, String method, JsonObject properties ) {
    if( METHOD_RENDER_TOOLTIP_TEXT.equals( method ) ) {
      handleCallRenderToolTipText( tree, properties );
    }
  }

  @Override
  public void handleNotify( Tree tree, String eventName, JsonObject properties ) {
    if( EVENT_SELECTION.equals( eventName ) ) {
      handleNotifySelection( tree, properties );
    } else if( EVENT_DEFAULT_SELECTION.equals( eventName ) ) {
      handleNotifyDefaultSelection( tree, properties );
    } else if( EVENT_EXPAND.equals( eventName ) ) {
      handleNotifyExpand( tree, properties );
    } else if( EVENT_COLLAPSE.equals( eventName ) ) {
      handleNotifyCollapse( tree, properties );
    } else if( EVENT_SET_DATA.equals( eventName ) ) {
      handleNotifySetData();
    } else {
      super.handleNotify( tree, eventName, properties );
    }
  }

  /*
   * PROTOCOL SET selection
   *
   * @param selection ([string]) array with ids of selected items
   */
  public void handleSetSelection( Tree tree, JsonObject properties ) {
    JsonValue values = properties.get( PROP_SELECTION );
    if( values != null ) {
      JsonArray itemIds = values.asArray();
      TreeItem[] selectedItems = new TreeItem[ itemIds.size() ];
      boolean validItemFound = false;
      for( int i = 0; i < itemIds.size(); i++ ) {
        selectedItems[ i ] = getItem( tree, itemIds.get( i ).asString() );
        if( selectedItems[ i ] != null ) {
          validItemFound = true;
        }
      }
      if( !validItemFound ) {
        selectedItems = new TreeItem[ 0 ];
      }
      tree.setSelection( selectedItems );
    }
  }

  /*
   * PROTOCOL SET scrollLeft
   *
   * @param scrollLeft (int) left scroll offset in pixels
   */
  public void handleSetScrollLeft( Tree tree, JsonObject properties ) {
    JsonValue value = properties.get( PROP_SCROLL_LEFT );
    if( value != null ) {
      int scrollLeft = value.asInt();
      getTreeAdapter( tree ).setScrollLeft( scrollLeft );
      setScrollBarSelection( tree.getHorizontalBar(), scrollLeft );
    }
  }

  /*
   * PROTOCOL SET topItemIndex
   *
   * @param topItemIndex (int) visual index of the item, which is on the top of the tree
   */
  public void handleSetTopItemIndex( Tree tree, JsonObject properties ) {
    JsonValue value = properties.get( PROP_TOP_ITEM_INDEX );
    if( value != null ) {
      int topItemIndex = value.asInt();
      getTreeAdapter( tree ).setTopItemIndex( topItemIndex );
      int scrollTop = topItemIndex * tree.getItemHeight();
      setScrollBarSelection( tree.getVerticalBar(), scrollTop );
    }
  }

  /*
   * PROTOCOL CALL renderToolTipText
   *
   * @param item (string) id of the hovered item
   * @param column (int) column index of the hovered cell
   */
  public void handleCallRenderToolTipText( Tree tree, JsonObject properties ) {
    ICellToolTipAdapter adapter = CellToolTipUtil.getAdapter( tree );
    ICellToolTipProvider provider = adapter.getCellToolTipProvider();
    if( provider != null ) {
      TreeItem item = getItem( tree, properties.get( "item" ).asString() );
      int columnIndex = properties.get( "column" ).asInt();
      if( item != null && ( columnIndex == 0 || columnIndex < tree.getColumnCount() ) ) {
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
  public void handleNotifySelection( Tree tree, JsonObject properties ) {
    TreeItem item = getItem( tree, properties.get( EVENT_PARAM_ITEM ).asString() );
    if( item != null ) {
      Event event = createSelectionEvent( SWT.Selection, properties );
      event.item = item;
      tree.notifyListeners( SWT.Selection, event );
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
  public void handleNotifyDefaultSelection( Tree tree, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.DefaultSelection, properties );
    event.item = getItem( tree, properties.get( EVENT_PARAM_ITEM ).asString() );
    tree.notifyListeners( SWT.DefaultSelection, event );
  }

  /*
   * PROTOCOL NOTIFY Expand
   *
   * @param item (string) id of expanded item
   */
  public void handleNotifyExpand( Tree tree, JsonObject properties ) {
    TreeItem item = getItem( tree, properties.get( EVENT_PARAM_ITEM ).asString() );
    if( item != null ) {
      Event event = new Event();
      event.item = item;
      tree.notifyListeners( SWT.Expand, event );
    }
  }

  /*
   * PROTOCOL NOTIFY Collapse
   *
   * @param item (string) id of collapsed item
   */
  public void handleNotifyCollapse( Tree tree, JsonObject properties ) {
    TreeItem item = getItem( tree, properties.get( EVENT_PARAM_ITEM ).asString() );
    if( item != null ) {
      Event event = new Event();
      event.item = item;
      tree.notifyListeners( SWT.Collapse, event );
    }
  }

  /*
   * PROTOCOL NOTIFY SetData
   * ignored, SetData event is fired when set topItemIndex
   */
  public void handleNotifySetData() {
  }

  private static TreeItem getItem( Tree tree, String itemId ) {
    TreeItem item = null;
    String[] idParts = itemId.split( "#" );
    if( idParts.length == 2 ) {
      Widget parent = find( tree, idParts[ 0 ] );
      if( parent != null ) {
        int itemIndex = Integer.parseInt( idParts[ 1 ] );
        if( getId( tree ).equals( idParts[ 0 ] ) ) {
          item = tree.getItem( itemIndex );
        } else {
          item = ( ( TreeItem )parent ).getItem( itemIndex );
        }
      }
    } else {
      item = ( TreeItem )find( tree, itemId );
    }
    return item;
  }

  private static void setScrollBarSelection( ScrollBar scrollBar, int selection ) {
    if( scrollBar != null ) {
      scrollBar.setSelection( selection );
    }
  }

  @Override
  protected boolean allowMouseEvent( Tree tree, int x, int y ) {
    return super.allowMouseEvent( tree, x, y ) && y >= tree.getHeaderHeight();
  }

  private static ITreeAdapter getTreeAdapter( Tree tree ) {
    return tree.getAdapter( ITreeAdapter.class );
  }

}
