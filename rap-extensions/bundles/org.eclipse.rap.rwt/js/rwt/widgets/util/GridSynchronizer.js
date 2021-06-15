/*******************************************************************************
 * Copyright (c) 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

namespace( "rwt.widgets.util" );

rwt.widgets.util.GridSynchronizer = function( grid ) {
  this._grid = grid;
  this._grid.addEventListener( "selectionChanged", this._onSelectionChanged, this );
  this._grid.addEventListener( "focusItemChanged", this._onFocusItemChanged, this );
  this._grid.addEventListener( "topItemChanged", this._onTopItemChanged, this );
  this._grid.addEventListener( "scrollLeftChanged", this._onScrollLeftChanged, this );
  this._grid.getRootItem().addEventListener( "update", this._onItemUpdate, this );
};

rwt.widgets.util.GridSynchronizer.prototype = {

  _onSelectionChanged : function( event ) {
    if( !rwt.remote.EventUtil.getSuspended() ) {
      var item = event.item;
      var type = event.type;
      var connection = rwt.remote.Connection.getInstance();
      if( type === "selection" ) {
        connection.getRemoteObject( this._grid ).set( "selection", this._getSelectionList() );
      } else if( type === "check" ) {
        connection.getRemoteObject( item ).set( "checked", item.isChecked() );
      } else if( type === "cellCheck" ) {
        connection.getRemoteObject( item ).set( "cellChecked", this._getCellChecked( item ) );
      }
      this._notifySelectionChanged( event );
    }
  },

  _notifySelectionChanged : function( event ) {
    var detail;
    var type = event.type;
    if( type === "check" || type === "cellCheck") {
      detail = "check";
    } else if( type === "cell" ) {
      detail = "cell";
    } else if( type === "hyperlink" ) {
      detail = "hyperlink";
    }
    var properties = {
      "item" : this._getItemId( event.item ),
      "detail" : detail,
      "index" : event.index,
      "text" : event.text
    };
    if( type === "defaultSelection" ) {
      rwt.remote.EventUtil.notifyDefaultSelected( this._grid, properties );
    } else {
      rwt.remote.EventUtil.notifySelected( this._grid, properties );
    }
  },

  _onFocusItemChanged : function() {
    if( !rwt.remote.EventUtil.getSuspended() ) {
      var connection = rwt.remote.Connection.getInstance();
      var itemId = this._getItemId( this._grid.getFocusItem() );
      connection.getRemoteObject( this._grid ).set( "focusItem", itemId );
    }
  },

  _onTopItemChanged : function() {
    if( !rwt.remote.EventUtil.getSuspended() ) {
      var vScroll = this._grid.getVerticalBar();
      var connection = rwt.remote.Connection.getInstance();
      var remoteObject = connection.getRemoteObject( this._grid );
      remoteObject.set( "topItemIndex", this._grid.getTopItemIndex() );
      if( vScroll.getHasSelectionListener() ) {
        connection.onNextSend( function() {
          connection.getRemoteObject( vScroll ).notify( "Selection" );
        }, this );
      }
      if( remoteObject.isListening( "SetData" ) ) {
        connection.onNextSend( function() {
          remoteObject.notify( "SetData" );
        }, this );
      }
      if( vScroll.getHasSelectionListener() || remoteObject.isListening( "SetData" ) ) {
        connection.sendDelayed( 400 );
      }
    }
  },

  _onScrollLeftChanged : function() {
    // TODO [tb] : There should be a check for suspended,
    // but currently this is needed to sync the value with the
    // server when the scrollbars are hidden by the server.
    var hScroll = this._grid.getHorizontalBar();
    var connection = rwt.remote.Connection.getInstance();
    var remoteObject = connection.getRemoteObject( this._grid );
    remoteObject.set( "scrollLeft", hScroll.getValue() );
    if( hScroll.getHasSelectionListener() ) {
      connection.onNextSend( function() {
        connection.getRemoteObject( hScroll ).notify( "Selection" );
      }, this );
      connection.sendDelayed( 400 );
    }
  },

  _onItemUpdate : function( event ) {
    if( !rwt.remote.EventUtil.getSuspended() || event.rendering ) {
      var connection = rwt.remote.Connection.getInstance();
      if( event.msg === "height" ) {
        connection.getRemoteObject( event.target ).set( "height", event.target.getOwnHeight() );
      } else if( event.msg === "expanded" || event.msg === "collapsed" ) {
        var expanded = event.msg === "expanded";
        connection.getRemoteObject( event.target ).set( "expanded", expanded );
        connection.getRemoteObject( this._grid ).notify( expanded ? "Expand" : "Collapse", {
          "item" : this._getItemId( event.target )
        } );
      }
    }
  },

  _getSelectionList : function() {
    var result = [];
    var selection = this._grid.getSelection();
    for( var i = 0; i < selection.length; i++ ) {
      result.push( this._getItemId( selection[ i ] ) );
    }
    return result;
  },

  _getCellChecked : function( item ) {
    var cellChecked = item.getCellChecked();
    var result = [];
    for( var i = 0; i < this._grid.getRenderConfig().columnCount; i++ ) {
      result[ i ] = cellChecked[ i ] === true;
    }
    return result;
  },

  _getItemId : function( item ) {
    var ObjectRegistry = rwt.remote.ObjectRegistry;
    var result;
    if( item.isCached() ) {
      result = ObjectRegistry.getId( item );
    } else {
      var parent = item.getParent();
      if( parent.isRootItem() ) {
        result = ObjectRegistry.getId( this._grid );
      } else {
        result = ObjectRegistry.getId( parent );
      }
      result += "#" + parent.indexOf( item );
    }
    return result;
  }

};
