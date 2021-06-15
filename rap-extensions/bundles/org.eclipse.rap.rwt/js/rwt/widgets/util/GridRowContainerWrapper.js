/*******************************************************************************
 * Copyright (c) 2011, 2017 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

namespace( "rwt.widgets.util" );

rwt.widgets.util.GridRowContainerWrapper = function() {
  this._fixedColumns = 0;
  this._container = [];
  this._container[ 0 ] = new rwt.widgets.base.GridRowContainer();
  this._container[ 1 ] = new rwt.widgets.base.GridRowContainer();
  this._config = rwt.widgets.base.GridRowContainer.createRenderConfig();
  this._width = 0;
  this._splitOffset = 0;
  this._rowWidth = 0;
  this.addEventListener( "hoverItem", this._onHoverItem, this );
};

rwt.widgets.util.GridRowContainerWrapper.createInstance = function() {
  if( !this.prototype._protoInit ) {
    for( var i = 0; i < this._CONTAINER_DELEGATES.length; i++ ) {
      this._createContainerDelegater( this._CONTAINER_DELEGATES[ i ] );
    }
    for( var i = 0; i < this._CONTAINER_GETTER_DELEGATES.length; i++ ) {
      this._createContainerGetterDelegater( this._CONTAINER_GETTER_DELEGATES[ i ] );
    }
    this.prototype._protoInit = true;
  }
  return new rwt.widgets.util.GridRowContainerWrapper();
};

rwt.widgets.util.GridRowContainerWrapper._createContainerDelegater = function( funcName ) {
  this.prototype[ funcName ] = function() {
    this._container[ 0 ][ funcName ].apply( this._container[ 0 ], arguments );
    this._container[ 1 ][ funcName ].apply( this._container[ 1 ], arguments );
  };
};

rwt.widgets.util.GridRowContainerWrapper._createContainerGetterDelegater = function( funcName ) {
  this.prototype[ funcName ] = function() {
    return this._container[ 0 ][ funcName ].apply( this._container[ 0 ], arguments );
  };
};

rwt.widgets.util.GridRowContainerWrapper._CONTAINER_DELEGATES = [
  "setParent",
  "destroy",
  "addEventListener",
  "removeEventListener",
  "setSelectionProvider",
  "setHeight",
  "setTop",
  "setBackgroundColor",
  "setBackgroundImage",
  "setRowHeight",
  "setTopItem",
  "renderItem",
  "setToolTip",
  "renderItemQueue",
  "setBaseAppearance",
  "setCellToolTipsEnabled",
  "getRow",
  "setToolTipText",
  "setDirection"
];

rwt.widgets.util.GridRowContainerWrapper._CONTAINER_GETTER_DELEGATES = [
  "getTop",
  "getHeight",
  "getHoverItem",
  "getElement",
  "getChildrenLength",
  "getRowCount"
];

rwt.widgets.util.GridRowContainerWrapper.prototype = {

  _protoInit : false,

  ///////////////////
  // Wrapper-only API

  getSubContainer : function( pos ) {
    return this._container[ pos ] || null;
  },

  setFixedColumns : function( value ) {
    this._fixedColumns = value;
    this._updateConfig();
  },

  getFixedColumns : function() {
    return this._fixedColumns;
  },

  /////////////////////////////////////////////
  // New Implementation of TreeRowContainer API

  getRenderConfig : function() {
    return this._config;
  },

  setPostRenderFunction : function() {
    // TODO [tb] : Dummy!
  },

  setWidth : function( value ) {
    this._width = value;
    this._layoutX();
  },

  getWidth : function() {
    return this._width;
  },

  setRowWidth : function( value ) {
    this._rowWidth = value;
    this._layoutX();
  },

  setScrollLeft : function( value ) {
    this._container[ 1 ].setScrollLeft( value );
  },

  findItemByRow : function( row ) {
    var result = this._container[ 0 ].findItemByRow( row );
    if( result == null ) {
      result = this._container[ 1 ].findItemByRow( row );
    }
    return result;
  },

  findRowByElement : function( el ) {
    var result = this._container[ 0 ].findRowByElement( el );
    if( result == null ) {
      result = this._container[ 0 ].findRowByElement( el );
    }
    return result;
  },

  updateGridLines : function() {
    this._container[ 0 ].getRenderConfig().linesVisible = this._config.linesVisible;
    this._container[ 0 ].getRenderConfig().variant = this._config.variant;
    this._container[ 0 ].updateGridLines();
    this._container[ 1 ].getRenderConfig().linesVisible = this._config.linesVisible;
    this._container[ 1 ].getRenderConfig().variant = this._config.variant;
    this._container[ 1 ].updateGridLines();
  },

  renderAll : function() {
    this._updateConfig();
    this._container[ 0 ].renderAll();
    this._container[ 1 ].renderAll();
  },

  getSplitOffset : function( column ) {
    return column < this._fixedColumns ? 0 : this._splitOffset;
  },

  getRowIndex : function( row ) {
    var index = this._container[ 0 ].getRowIndex( row );
    if( index < 0 ) {
      index = this._container[ 1 ].getRowIndex( row );
    }
    return index;
  },

  _updateConfig : function() {
    var configLeft = this._container[ 0 ].getRenderConfig();
    var configRight = this._container[ 1 ].getRenderConfig();
    this._copyConfigToSubContainer( configLeft, configRight );
    configLeft.containerNumber = 0;
    configRight.containerNumber = 1;
    configRight.hasCheckBoxes = false;
    var cellOrder = this._config.cellOrder;
    var rightColumnsOffset = this._computeRightColumnsOffset( cellOrder );
    for( var i = 0; i < cellOrder.length; i++ ) {
      var column = cellOrder[ i ];
      if( i < this._fixedColumns ) {
        configRight.itemWidth[ column ] = 0;
      } else {
        configLeft.itemWidth[ column ] = 0;
        configRight.itemLeft[ column ] -= rightColumnsOffset;
        configRight.itemImageLeft[ column ] -= rightColumnsOffset;
        configRight.itemTextLeft[ column ] -= rightColumnsOffset;
      }
    }
    if( this._splitOffset !== rightColumnsOffset ) {
      this._splitOffset = rightColumnsOffset;
      this._layoutX();
    }
  },

  _copyConfigToSubContainer : function( configLeft, configRight ) {
    for( var key in this._config ) {
      if( this._config[ key ] instanceof Array ) {
        configLeft[ key ] = this._config[ key ].concat();
        configRight[ key ] = this._config[ key ].concat();
      } else {
        configLeft[ key ] = this._config[ key ];
        configRight[ key ] = this._config[ key ];
      }
    }
  },

  _computeRightColumnsOffset : function( cellOrder ) {
    var rightColumnsOffset = 0;
    if( cellOrder.length > this._fixedColumns ) {
      rightColumnsOffset = this._config.itemLeft[ cellOrder[ this._fixedColumns ] ];
    } else {
      rightColumnsOffset = this._width;
    }
    return rightColumnsOffset;
  },

  _layoutX : function() {
    var leftWidth = Math.min( this._splitOffset, this._width );
    this._container[ 0 ].setWidth( leftWidth );
    this._container[ 0 ].setRowWidth( leftWidth );
    this._container[ 1 ].setLeft( leftWidth );
    this._container[ 1 ].setWidth( this._width - leftWidth );
    this._container[ 1 ].setRowWidth( this._rowWidth - leftWidth );
  },

  _onHoverItem : function( item ) {
    for( var i = 0; i < this._container.length; i++ ) {
      this._container[ i ]._setHoverItem( item );
    }
  },

  findRowByItem : function( item, column ) {
    var pos = column < this._fixedColumns ? 0 : 1;
    return this._container[ pos ].findRowByItem( item );
  }

};
