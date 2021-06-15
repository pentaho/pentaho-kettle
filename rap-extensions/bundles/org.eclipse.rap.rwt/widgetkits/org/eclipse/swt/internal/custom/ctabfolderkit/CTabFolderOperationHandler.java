/*******************************************************************************
 * Copyright (c) 2013, 2017 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.custom.ctabfolderkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.find;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_DEFAULT_SELECTION;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_FOLDER;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_FOLDER_DETAIL_CLOSE;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_FOLDER_DETAIL_MAXIMIZE;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_FOLDER_DETAIL_MINIMIZE;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_FOLDER_DETAIL_RESTORE;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_DETAIL;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_ITEM;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_SELECTION;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.lifecycle.ProcessActionRunner;
import org.eclipse.rap.rwt.internal.protocol.ClientMessageConst;
import org.eclipse.rap.rwt.internal.protocol.ControlOperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.custom.ICTabFolderAdapter;
import org.eclipse.swt.internal.events.EventTypes;
import org.eclipse.swt.widgets.Event;


public class CTabFolderOperationHandler extends ControlOperationHandler<CTabFolder> {

  private static final String PROP_MINIMIZED = "minimized";
  private static final String PROP_MAXIMIZED = "maximized";
  private static final String PROP_SELECTION = "selection";

  public CTabFolderOperationHandler( CTabFolder folder ) {
    super( folder );
  }

  @Override
  public void handleSet( CTabFolder folder, JsonObject properties ) {
    super.handleSet( folder, properties );
    handleSetMinimized( folder, properties );
    handleSetMaximized( folder, properties );
    handleSetSelection( folder, properties );
  }

  @Override
  public void handleNotify( CTabFolder folder, String eventName, JsonObject properties ) {
    if( EVENT_SELECTION.equals( eventName ) ) {
      handleNotifySelection( folder, properties );
    } else if( EVENT_DEFAULT_SELECTION.equals( eventName ) ) {
      handleNotifyDefaultSelection( folder, properties );
    } else if( EVENT_FOLDER.equals( eventName ) ) {
      handleNotifyFolder( folder, properties );
    } else {
      super.handleNotify( folder, eventName, properties );
    }
  }

  /*
   * PROTOCOL SET minimized
   *
   * @param minimized (boolean) true if the folder was minimized, otherwise false
   */
  public void handleSetMinimized( CTabFolder folder, JsonObject properties ) {
    JsonValue value = properties.get( PROP_MINIMIZED );
    if( value != null ) {
      folder.setMinimized( value.asBoolean() );
    }
  }

  /*
   * PROTOCOL SET maximized
   *
   * @param maximized (boolean) true if the folder was maximized, otherwise false
   */
  public void handleSetMaximized( CTabFolder folder, JsonObject properties ) {
    JsonValue value = properties.get( PROP_MAXIMIZED );
    if( value != null ) {
      folder.setMaximized( value.asBoolean() );
    }
  }

  /*
   * PROTOCOL SET selection
   *
   * @param selection (string) the id of selected item
   */
  public void handleSetSelection( final CTabFolder folder, JsonObject properties ) {
    JsonValue value = properties.get( PROP_SELECTION );
    if( value != null ) {
      final CTabItem item = getItem( folder, value.asString() );
      // TODO [rh] it's a hack: necessary because folder.setSelection changes
      //      the visibility of tabItem.control; but preserveValues stores
      //      the already changed visibility and thus no visibility property is rendered
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
  public void handleNotifySelection( CTabFolder folder, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.Selection, properties );
    event.item = getItem( folder, properties.get( EVENT_PARAM_ITEM ).asString() );
    folder.notifyListeners( SWT.Selection, event );
  }

  /*
   * PROTOCOL NOTIFY DefaultSelection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   * @param item (string) id of selected item
   */
  public void handleNotifyDefaultSelection( CTabFolder folder, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.DefaultSelection, properties );
    event.item = getItem( folder, properties.get( EVENT_PARAM_ITEM ).asString() );
    folder.notifyListeners( SWT.DefaultSelection, event );
  }

  /*
   * PROTOCOL NOTIFY Folder
   *
   * @param detail (string) "minimize", "maximize", "restore", "close" or "showList"
   * @param item (string) id of selected item
   */
  public void handleNotifyFolder( CTabFolder folder, JsonObject properties ) {
    JsonValue value = properties.get( EVENT_PARAM_DETAIL );
    if( value != null ) {
      String detail = value.asString();
      if( EVENT_FOLDER_DETAIL_MINIMIZE.equals( detail ) ) {
        folder.notifyListeners( EventTypes.CTAB_FOLDER_MINIMIZE, new Event() );
      } else if( EVENT_FOLDER_DETAIL_MAXIMIZE.equals( detail ) ) {
        folder.notifyListeners( EventTypes.CTAB_FOLDER_MAXIMIZE, new Event() );
      } else if( EVENT_FOLDER_DETAIL_RESTORE.equals( detail ) ) {
        folder.notifyListeners( EventTypes.CTAB_FOLDER_RESTORE, new Event() );
      } else if( EVENT_FOLDER_DETAIL_CLOSE.equals( detail ) ) {
        CTabItem item = getItem( folder, properties.get( EVENT_PARAM_ITEM ).asString() );
        notifyCloseListeners( folder, item );
      } else if( ClientMessageConst.EVENT_FOLDER_DETAIL_SHOW_LIST.equals( detail ) ) {
        notifyShowListListeners( folder );
      }
    }
  }

  private static void notifyCloseListeners( final CTabFolder folder, final CTabItem item ) {
    if( item != null ) {
      ProcessActionRunner.add( new Runnable() {
        @Override
        public void run() {
          if( !item.isDisposed() ) {
            boolean doit = sendCloseEvent( folder, item );
            if( doit ) {
              item.dispose();
            }
          }
        }
      } );
    }
  }

  private static boolean sendCloseEvent( CTabFolder folder, CTabItem item ) {
    Event event = new Event();
    event.item = item;
    event.doit = true;
    folder.notifyListeners( EventTypes.CTAB_FOLDER_CLOSE, event );
    return event.doit;
  }

  private static void notifyShowListListeners( final CTabFolder folder ) {
    ProcessActionRunner.add( new Runnable() {
      @Override
      public void run() {
        boolean doit = sendShowListEvent( folder );
        if( doit ) {
          getCTabFolderAdapter( folder ).showListMenu();
        }
      }
    } );
  }

  private static boolean sendShowListEvent( CTabFolder folder ) {
    Event event = new Event();
    Rectangle chevronRect = getChevronBounds( folder );
    event.x = chevronRect.x;
    event.y = chevronRect.y;
    event.height = chevronRect.height;
    event.width = chevronRect.width;
    event.doit = true;
    folder.notifyListeners( EventTypes.CTAB_FOLDER_SHOW_LIST, event );
    return event.doit;
  }

  private static Rectangle getChevronBounds( CTabFolder folder ) {
    return getCTabFolderAdapter( folder ).getChevronRect();
  }

  private static CTabItem getItem( CTabFolder folder, String itemId ) {
    return ( CTabItem )find( folder, itemId );
  }

  private static ICTabFolderAdapter getCTabFolderAdapter( CTabFolder folder ) {
    return folder.getAdapter( ICTabFolderAdapter.class );
  }

}
