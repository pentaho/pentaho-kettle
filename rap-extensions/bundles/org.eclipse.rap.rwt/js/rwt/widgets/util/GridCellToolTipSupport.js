/*******************************************************************************
 * Copyright (c) 2009, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/


namespace( "rwt.widgets.util" );

rwt.widgets.util.GridCellToolTipSupport = {

  _cell : [ null, null, null ],
  _requestedCell : null,

  setEnabled : function( grid, value ) {
    if( value ) {
      grid.addEventListener( "renderCellToolTip", this._onRender, grid );
    } else {
      grid.removeEventListener( "renderCellToolTip", this._onRender, grid );
    }
  },

  // Called by server via Grid.js in response to _onRender -> _setCell
  showToolTip : function( text ) {
    if( this._isValidToolTip( text ) ) {
      var grid = this._cell[ 0 ];
      var item = rwt.remote.ObjectRegistry.getObject( this._cell[ 1 ] );
      if( grid.getRowContainer().getHoverItem() === item ) {
        grid.getRowContainer().setToolTipText( text );
        rwt.widgets.base.WidgetToolTip.getInstance().updateText();
        grid.getRowContainer().setToolTipText( "" );
      }
    }
  },

  getCurrentToolTipTargetBounds : function( row ) {
    var grid = this._cell[ 0 ];
    var container = grid.getRowContainer();
    var splitOffset = container.getSplitOffset ? container.getSplitOffset( this._cell[ 2 ] ) : 0;
    return {
      left : grid.getRenderConfig().itemLeft[ this._cell[ 2 ] ] - splitOffset,
      top : row.getTop(),
      height : row.getHeight(),
      width : grid.getRenderConfig().itemWidth[ this._cell[ 2 ] ]
    };
  },

  _onRender : function( row ) {
    var itemId = null;
    var columnIndex = -1;
    var item = this._rowContainer.findItemByRow( row );
    if( item ) {
      itemId = rwt.remote.ObjectRegistry.getId( this._rowContainer.getHoverItem() );
      var GridUtil = rwt.widgets.util.GridUtil;
      columnIndex = GridUtil.getColumnByPageX( this, rwt.event.MouseEvent.getPageX() );
    }
    rwt.widgets.util.GridCellToolTipSupport._setCell( this, itemId, columnIndex );
  },

  _setCell : function( grid, itemId, columnIndex ) {
    var cell = [ grid, itemId, columnIndex ];
    if( this._isValidCell( cell ) ) {
      this._cell = cell;
      var connection = rwt.remote.Connection.getInstance();
      connection.getRemoteObject( this._cell[ 0 ] ).call( "renderToolTipText", {
        "item" : this._cell[ 1 ],
        "column" : this._cell[ 2 ]
      } );
      this._requestedCell = this._cell;
    }
  },

  _isValidCell : function( cell ) {
    return    cell
           && cell[ 0 ] != null
           && cell[ 1 ] != null
           && cell[ 2 ] != -1;
  },

  _isValidToolTip : function( text ) {
    return    text
           && this._requestedCell
           && this._cell[ 0 ] === this._requestedCell[ 0 ]
           && this._cell[ 1 ] === this._requestedCell[ 1 ]
           && this._cell[ 2 ] === this._requestedCell[ 2 ];
  }

};
