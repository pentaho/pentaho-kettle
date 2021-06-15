/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.util.TabUtil", {

  statics : {
    createTabItem : function( id, parentId, index ) {
      var widgetManager = rwt.remote.WidgetManager.getInstance();
      var tabFolder = widgetManager.findWidgetById( parentId );
      var tabItem = new rwt.widgets.TabItem();
      tabItem.setTabIndex( null );
      tabItem.setEnableElementFocus( false );
      tabItem.setDirection( tabFolder.getDirection() );
      tabItem.addEventListener( "changeFocused", rwt.widgets.util.TabUtil._onTabItemChangeFocus );
      tabItem.addEventListener( "changeChecked", rwt.widgets.util.TabUtil._onTabItemSelected );
      tabItem.addEventListener( "click", rwt.widgets.util.TabUtil._onTabItemClick );
      tabFolder.addEventListener( "changeDirection", function() {
        tabItem.setDirection( tabFolder.getDirection() );
      } );
      tabFolder.getBar().addAt( tabItem, index );
      var tabViewPage = new rwt.widgets.base.TabFolderPage( tabItem );
      tabFolder.getPane().add( tabViewPage );
      widgetManager.add( tabViewPage, id + "pg" );
      return tabItem;
    },

    releaseTabItem : function( tabItem ) {
      tabItem.removeEventListener( "changeFocused", rwt.widgets.util.TabUtil._onTabItemChangeFocus );
      tabItem.removeEventListener( "changeChecked", rwt.widgets.util.TabUtil._onTabItemSelected );
      tabItem.removeEventListener( "click", rwt.widgets.util.TabUtil._onTabItemClick );
      var widgetManager = rwt.remote.WidgetManager.getInstance();
      var itemId = widgetManager.findIdByWidget( tabItem );
      widgetManager.dispose( itemId + "pg" );
      widgetManager.dispose( itemId );
    },

    _onTabItemChangeFocus : function( evt ) {
      // Focus the tabFolder the item belongs to when the item is focused
      if( evt.getTarget().getFocused() ) {
        evt.getTarget().getParent().getParent().focus();
      }
    },

    _onTabItemClick : function( evt ) {
      // Focus the tabFolder the item belongs to when the item is clicked
      var folder = evt.getTarget().getParent().getParent();
      if( !folder.getFocused() ) {
        folder.focus();
      }
    },

    _onTabItemSelected : function( evt ) {
      var tab = evt.getTarget();
      if( !rwt.remote.EventUtil.getSuspended() && tab.getChecked() ) {
        var itemId = rwt.remote.WidgetManager.getInstance().findIdByWidget( tab );
        var folder = tab.getParent().getParent();
        var remoteObject = rwt.remote.Connection.getInstance().getRemoteObject( folder );
        remoteObject.set( "selection", itemId );
        rwt.remote.EventUtil.notifySelected( folder, { "item" : itemId } );
      }
    },

    onTabFolderKeyPress : function( evt ) {
      var folder = evt.getTarget();
      if( folder.classname == "rwt.widgets.TabFolder" ) {
        var manager = folder.getBar().getManager();
        var item = manager.getSelected();
        if( item != null ) {
          switch( evt.getKeyIdentifier() ) {
            case "Left":
              manager.selectPrevious( item );
              rwt.widgets.util.TabUtil.markTabItemFocused( folder, evt.getTarget() );
              evt.stopPropagation();
              break;
            case "Right":
              manager.selectNext( item );
              rwt.widgets.util.TabUtil.markTabItemFocused( folder, evt.getTarget() );
              evt.stopPropagation();
              break;
          }
        }
      }
    },

    onTabFolderChangeFocused : function( evt ) {
      var folder = evt.getTarget();
      var item = folder.getBar().getManager().getSelected();
      rwt.widgets.util.TabUtil.markTabItemFocused( folder, item );
    },

    markTabItemFocused : function( folder, item ) {
      var items = folder.getBar().getManager().getItems();
      for( var i = 0; i < items.length; i++ ) {
        items[i].removeState( "focused" );
      }
      // add state to the selected item if the tabFolder is focused
      if( item != null && folder.getFocused() ) {
        item.addState( "focused" );
      }
    }
  }
});
