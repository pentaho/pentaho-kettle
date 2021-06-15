/*******************************************************************************
 * Copyright (c) 2014, 2017 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

namespace( "rwt.widgets.util" );

rwt.widgets.util.GridRowLayout = function( gridConfig, item ) {
  this._gridConfig = gridConfig;
  this._item = item;
  this._computeIndention();
  if( !this._gridConfig.rowTemplate ) {
    this._computeCellPadding();
    this._computeAllCells();
    if( this._gridConfig.hasCheckBoxes ) {
      this._computeCheckBox();
    }
  }
};

rwt.widgets.util.GridRowLayout.prototype._computeIndention = function() {
  var treeColumn = this._gridConfig.treeColumn;
  if( this._item && typeof treeColumn === "number" && treeColumn > -1 ) {
    this.indention = this._gridConfig.indentionWidth * ( this._item.getLevel() + 1 );
  } else {
    this.indention = 0;
  }
};

rwt.widgets.util.GridRowLayout.prototype._computeCellPadding = function() {
  var manager = rwt.theme.AppearanceManager.getInstance();
  var states = {};
  if( this._gridConfig.variant ) {
    states[ this._gridConfig.variant ] = true;
  }
  this.cellPadding = manager.styleFrom( this._gridConfig.baseAppearance + "-cell", states ).padding;
};

rwt.widgets.util.GridRowLayout.prototype._computeAllCells = function() {
  this._initCells();
  var span = -1;
  for( var i = 0; i < this._gridConfig.cellOrder.length; i++ ) {
    if( span === -1 ) {
      span = this._item ? this._item.getColumnSpan( this._gridConfig.cellOrder[ i ] ) : 0;
      this._computeCell( i, span );
    } else {
      this.cellWidth[ this._gridConfig.cellOrder[ i ] ] = 0;
    }
    span--;
  }
};

rwt.widgets.util.GridRowLayout.prototype._initCells = function() {
  this.cellLeft = new Array( this._gridConfig.itemLeft.length );
  this.cellWidth = new Array( this._gridConfig.itemWidth.length );
  this.cellImageLeft = new Array( this._gridConfig.itemImageLeft.length );
  this.cellImageWidth = new Array( this._gridConfig.itemImageWidth.length );
  this.cellTextLeft = new Array( this._gridConfig.itemTextLeft.length );
  this.cellTextWidth = new Array( this._gridConfig.itemTextWidth.length );
  this.cellCheckLeft = new Array( this._gridConfig.itemCellCheckLeft.length );
  this.cellCheckWidth = new Array( this._gridConfig.itemCellCheckWidth.length );
};

rwt.widgets.util.GridRowLayout.prototype._computeCell = function( cellPos, span ) {
  var cell = this._gridConfig.cellOrder[ cellPos ];
  this.cellLeft[ cell ] = this._gridConfig.itemLeft[ cell ];
  this.cellWidth[ cell ] = this._gridConfig.itemWidth[ cell ];
  this.cellImageLeft[ cell ] = this._gridConfig.itemImageLeft[ cell ];
  this.cellImageWidth[ cell ] = this._gridConfig.itemImageWidth[ cell ];
  this.cellTextLeft[ cell ] = this._gridConfig.itemTextLeft[ cell ];
  this.cellTextWidth[ cell ] = this._gridConfig.itemTextWidth[ cell ];
  this.cellCheckLeft[ cell ] = this._gridConfig.itemCellCheckLeft[ cell ];
  this.cellCheckWidth[ cell ] = this._gridConfig.itemCellCheckWidth[ cell ];
  if( span > 0 ) {
    this._expandCell( cellPos, span );
  }
  if( this._gridConfig.treeColumn === cell ) {
    this._indentCellContent( cell );
  }
};

rwt.widgets.util.GridRowLayout.prototype._expandCell = function( cellPos, span ) {
  var maxCell = this._gridConfig.cellOrder.length - 1;
  var cell = this._gridConfig.cellOrder[ cellPos ];
  var firstCell = this._gridConfig.cellOrder[ cellPos ];
  var lastCell = this._gridConfig.cellOrder[ Math.min( cellPos + span, maxCell ) ];
  var lastCellEnd = this._gridConfig.itemLeft[ lastCell ] + this._gridConfig.itemWidth[ lastCell ];
  this.cellWidth[ cell ] = lastCellEnd - this._gridConfig.itemLeft[ firstCell ];
  this.cellTextWidth[ cell ] =   lastCellEnd
                               - this._gridConfig.itemTextLeft[ firstCell ]
                               - this.cellPadding[ 1 ];
};

rwt.widgets.util.GridRowLayout.prototype._indentCellContent = function( cell ) {
  var cellEnd = this.cellLeft[ cell ] + this.cellWidth[ cell ];
  var imageLeft = this.cellImageLeft[ cell ] += this.indention;
  var textLeft = this.cellTextLeft[ cell ] += this.indention;
  var checkLeft = this.cellCheckLeft[ cell ] += this.indention;
  if( imageLeft + this.cellImageWidth[ cell ] > cellEnd ) {
    this.cellImageWidth[ cell ] = Math.max( 0, cellEnd - imageLeft );
  }
  if( textLeft + this.cellTextWidth[ cell ] > cellEnd ) {
    this.cellTextWidth[ cell ] = Math.max( 0, cellEnd - textLeft );
  }
  if( checkLeft + this.cellCheckWidth[ cell ] > cellEnd ) {
    this.cellCheckWidth[ cell ] = Math.max( 0, cellEnd - checkLeft );
  }
};

rwt.widgets.util.GridRowLayout.prototype._computeCheckBox = function() {
  this.checkBoxLeft = this._gridConfig.checkBoxLeft + this.indention;
  this.checkBoxWidth = this._gridConfig.checkBoxWidth;
  var cell = this._gridConfig.cellOrder[ 0 ];
  var cellEnd = this.cellLeft[ cell ] + this.cellWidth[ cell ];
  if( this.checkBoxLeft + this.checkBoxWidth > cellEnd ) {
    this.checkBoxWidth = Math.max( 0, cellEnd - this.checkBoxLeft );
  }
};
