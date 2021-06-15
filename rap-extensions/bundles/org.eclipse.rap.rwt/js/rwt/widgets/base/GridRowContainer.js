/*******************************************************************************
 * Copyright (c) 2011, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

(function( $ ){

rwt.qx.Class.define( "rwt.widgets.base.GridRowContainer", {

  extend : rwt.widgets.base.VerticalBoxLayout,

  construct : function() {
    this.base( arguments );
    this.$rows = $( "<div>" ).css( fillLayout ).appendTo( this );
    this.$el = $( this );
    this.setOverflow( "hidden" );
    this._scrollLeft = 0;
    this._rowHeight = 16;
    this._rowWidth = 0;
    this._gridLines = { "horizontal" :  null, "vertical" : null };
    this._baseAppearance = null;
    this._topItem = null;
    this._renderTime = null;
    this._topItemIndex = 0;
    this._items = [];
    this._asyncQueue = {};
    this._asyncTimer = new rwt.client.Timer( 0 );
    this._asyncTimer.addEventListener( "interval", this._onAsyncTimer, this );
    this._hoverItem = null;
    this._hoverTargetType = [];
    this._config = rwt.widgets.base.GridRowContainer.createRenderConfig();
    this.addEventListener( "elementOver", this._onElementOver, this );
    this.addEventListener( "elementOut", this._onElementOut, this );
  },

  destruct : function() {
    while( this.getRowCount() > 0 ) {
      this.getRow( 0 ).dispose();
    }
    this._gridLines = null;
    this._topItem = null;
    this._renderTime = null;
    this._items = null;
    this._hoverItem = null;
    this._hoverTargetType = null;
    this._asyncTimer.dispose();
    this._asyncTimer = null;
  },

  statics : {

    createRenderConfig : function() {
      return {
        "textColor" : null,
        "font" : null,
        "enabled" : true,
        "seeable" : false,
        "focused" : false,
        "linesVisible" : false,
        "fullSelection" : false,
        "hideSelection" : false,
        "alwaysHideSelection" : false,
        "autoHeight" : false,
        "variant" : null,
        "selectionPadding" : null,
        "indentionWidth" : 0,
        "hasCheckBoxes" : false,
        "checkBoxLeft" : null,
        "checkBoxWidth" : null,
        "columnCount" : 0,
        "cellOrder" : [ 0 ],
        "treeColumn" : 0,
        "alignment" : [],
        "wordWrap" : [],
        "itemLeft" : [],
        "itemWidth" : [],
        "itemImageLeft" : [],
        "itemImageWidth" : [],
        "itemTextLeft" : [],
        "itemTextWidth" : [],
        "itemCellCheck" : [],
        "itemCellCheckLeft" : [],
        "itemCellCheckWidth" : [],
        "vBarWidth" : 0
      };
    }

  },

  members : {

    /////////////
    // Public API

    setTopItem : function( item, index, render ) {
      this._topItem = item;
      if( render ) {
        var delta = index - this._topItemIndex;
        this._topItemIndex = index;
        var forwards = delta > 0;
        delta = Math.abs( delta );
        if( delta >= this.getRowCount() ) {
          this._renderAll( true );
        } else {
          var numberOfShiftingRows = this.getRowCount() - delta;
          var updateFromRow = forwards ? numberOfShiftingRows : 0;
          var newFirstRow = this.getRow( forwards ? delta : numberOfShiftingRows );
          this._sortRows( newFirstRow, forwards );
          this._updateRows( updateFromRow, delta, true );
        }
        this._checkHoverItem();
      } else {
        this._topItemIndex = index;
      }
    },

    /**
     * Returns a map with values for treeRow configuration. (see _createRenderConfig).
     * Will not be changed by GridRow or GridRowContainer. When changing it, renderAll must
     * be called for them take effect.
     */
    getRenderConfig : function() {
      return this._config;
    },

    /**
     * Calls this function with an item as the parameter. Expects a boolean as return value.
     */
    setSelectionProvider : function( func, context ) {
      this._selectionProvider = [ func, context ];
    },

    setBaseAppearance : function( value ) {
      this._baseAppearance = value;
    },

    // TODO [tb] : the rest of the setters could be refactored to "update" functions using _config.

    setRowWidth : function( width ) {
      this._rowWidth = width;
      this.$rows.css( "width", width );
      this._forEachRow( function( row ) {
        row.setWidth( width );
      } );
    },

    setRowHeight : function( height ) {
      this._rowHeight = height;
      this._forEachRow( function( row ) {
        row.setHeight( height );
      } );
      this._updateRowCount();
    },

    setCellToolTipsEnabled : function( value ) {
      this._cellToolTipsEnabled = value;
      if( value ) {
       // an empty string allows ToolTipManager to bind WidgetToolTip to GridRowContainer, but instead
       // of actually appearing WidgetToolTip will call requestToolTipText
        this.setToolTipText( "" );
      } else {
        this.resetToolTipText();
      }
    },

    getCellToolTipsEnabled : function() {
      return this._cellToolTipsEnabled;
    },

    requestToolTipText : function() {
      // This event is handled by GridCellToolTipSupport:
      this.dispatchSimpleEvent( "renderCellToolTip", this._hoverRow, true );
    },

    getToolTipTargetBounds : function() {
      return rwt.widgets.util.GridCellToolTipSupport.getCurrentToolTipTargetBounds( this._hoverRow );
    },

    updateGridLines : function() {
      this._gridLines = {
        horizontal :  this._getGridLine( "horizontal" ),
        vertical : this._getGridLine( "vertical" )
      };
      this._forEachRow( function( row ) {
        row.setGridLines( this._gridLines );
        row.setState( "linesvisible", this._config.linesVisible );
      } );
    },

    _getGridLine : function( line ) {
      if( !this._config.linesVisible ) {
        return null;
      }
      var states = { rowTempalte : this._config.rowTemplate != null };
      states[ line ] = true;
      if( this._config.variant != null ) {
        states[ this._config.variant ] = true;
      }
      var tv = new rwt.theme.ThemeValues( states );
      var gridColor = tv.getCssColor( rwt.util.Strings.toFirstUp( this._baseAppearance ) + "-GridLine", "color" );
      gridColor = gridColor == "undefined" ? "transparent" : gridColor;
      tv.dispose();
      return gridColor;
    },

    getRowCount : function() {
      return this.$rows.prop( "childElementCount" );
    },

    getRow : function( index ) {
      return this.$rows.prop( "children" )[ index ][ "row" ];
    },

    renderAll : function() {
      this._renderAll( false );
    },

    renderItemQueue : function( queue ) {
      for( var key in queue ) {
        var item = queue[ key ];
        var index = this._items.indexOf( item );
        if( index !== -1 ) {
          this._renderRow( this.getRow( index ), item );
        }
      }
    },

    renderItem : function( item ) {
      if( this._isCreated && item != null ) {
        var row = this.findRowByItem( item );
        if( row!= null ) {
          this._renderRow( row, item );
        }
      }
    },

    renderRowHeight : function() {
      this._forEachRow( function( row ) {
        var item = this.findItemByRow( row );
        if( item != null ) {
          row.renderHeight( item, this._config );
        }
      } );
    },

    setScrollLeft : function( value ) {
      this._scrollLeft = value;
      if( this.isSeeable() ) {
        this.base( arguments, value );
      }
    },

    findRowByElement : function( target ) {
      while( target && !target.row && !target.rwtWidget ) {
        target = target.parentElement;
      }
      return target ? target.row : null;
    },

    findItemByRow : function( targetRow ) {
      var index = -1;
      var rowCount = this.getRowCount();
      for( var i = 0; i < rowCount && index === -1; i++ ) {
        if( this.getRow( i ) === targetRow ) {
          index = i;
        }
      }
      return index !== -1 ? this._items[ index ] : null;
    },

    findRowByItem : function( targetItem ) {
      var index = this._items.indexOf( targetItem );
      return index !== -1 ? this.getRow( index ) : null;
    },

    getRowIndex : function( row ) {
      return this._items.indexOf( this.findItemByRow( row ) );
    },

    getHoverItem : function() {
      return this._hoverItem;
    },

    ///////////
    // Internal

    _getRowAppearance : function() {
      return this._baseAppearance + "-row";
    },

    ////////////
    // Internals

    _renderAll : function( contentOnly ) {
      this._updateRows( 0, this.getRowCount(), contentOnly );
    },

    _updateRowCount : function() {
      var rowsNeeded = Math.round( ( this.getHeight() / this._rowHeight ) + 0.5 );
      while( this.getRowCount() < rowsNeeded ) {
        var row = new rwt.widgets.base.GridRow();
        row.setMirror( this.getDirection() === "rtl" );
        row.setAppearance( this._getRowAppearance() );
        row.setGridLines( this._gridLines );
        row.$el.css( {
          "zIndex": 0,
          "width": this._rowWidth,
          "height": this._rowHeight
        } );
        row.setState( "linesvisible", this._config.linesVisible );
        this.$rows.append( row.$el );
      }
      while( this.getRowCount() > rowsNeeded ) {
        this.getRow( this.getRowCount() - 1 ).dispose();
      }
      this._items.length = this.getRowCount();
      this._updateRowsEvenState();
    },

    _updateRowsEvenState: function() {
      this._forEachRow( function( row, i ) {
        row.updateEvenState( this._topItemIndex + i );
      } );
    },

    _updateRows : function( from, delta, contentOnly ) {
      this._updateRowsEvenState();
      var item = this._topItem;
      var to = from + delta;
      var rowIndex = 0;
      while( item != null && rowIndex < this.getRowCount() ) {
        this._items[ rowIndex ] = item;
        if( rowIndex >= from && rowIndex <= to ) {
          this._renderRow( this.getRow( rowIndex ), item, contentOnly );
        }
        item = item.getNextItem();
        rowIndex++;
      }
      for( var i = rowIndex; i < this.getRowCount(); i++ ) {
        this._renderRow( this.getRow( i ), null, contentOnly );
        this._items[ i ] = null;
      }
    },

    _renderRow : function( row, item, contentOnly ) {
       row.renderItem( item,
                       this._config,
                       this._isSelected( item ),
                       this._getHoverElement( item ),
                       contentOnly );
      if( item ) {
        this.dispatchSimpleEvent( "rowRendered", row );
      }
    },

    _sortRows : function( newFirstRow, forwards ) {
      var lastRowIndex = this.getRowCount() - 1;
      while( this.getRow( 0 ) !== newFirstRow ) {
        if( forwards ) {
          this.$rows.append( this.getRow( 0 ).$el );
          this._items.push( this._items.shift() );
        } else {
          this.$rows.prepend( this.getRow( lastRowIndex ).$el );
          this._items.unshift( this._items.pop() );
        }
      }
    },

    _checkHoverItem : function() {
      var x = rwt.event.MouseEvent.getClientX();
      var y = rwt.event.MouseEvent.getClientY();
      var element = rwt.event.EventHandlerUtil.getElementAt( x, y );
      var row = this.findRowByElement( element );
      if( this._hoverRow !== row ) {
        this._onRowOver( row, element );
      }
    },

    _onElementOver : function( event ) {
      var element = event.getDomTarget();
      this._onRowOver( this.findRowByElement( element ), element );
    },

    _onElementOut : function( event ) {
      var target = event.getDomEvent();
      var related = rwt.event.EventHandlerUtil.getRelatedTargetObjectFromEvent( target );
      if( !this.findRowByElement( related ) ) {
        this._hoverTargetType = [];
        this._setHoverItem( null, null );
      }
    },

    _onRowOver : function( row, element ) {
      if( row ) {
        var item = this.findItemByRow( row );
        var targetTypes = row.identify( element );
        if( item !== this._hoverItem || this._hoverTargetType[ 0 ] !== targetTypes[ 0 ] ) {
          this._hoverTargetType = targetTypes;
          this._setHoverItem( item, row );
        }
      }
    },

    _setHoverItem : function( item, row ) {
      var oldItem = this._hoverItem;
      this._hoverItem = item;
      this._hoverRow = row || ( item ? this.findRowByItem( item ) : null );
      if( oldItem !== item ) {
        this.dispatchSimpleEvent( "hoverItem", item );
        this.dispatchSimpleEvent( "updateToolTip", this );
        this._renderAsync( oldItem );
      }
      this._renderAsync( item );
    },

    _getHoverElement : function( item ) {
      var result = null;
      if( this._hoverItem === item ) {
        result = this._hoverTargetType;
      }
      return result;
    },

    _renderAsync : function( item ) {
      // async rendering needed in some cases where webkit (and possibly other browser) get confused
      // when changing dom-elements in "mouseover" events
      if( item !== null ) {
        this._asyncQueue[ item.toHashCode() ] = item;
        this._asyncTimer.start();
      }
    },

    _onAsyncTimer : function() {
      this._asyncTimer.stop();
      this.renderItemQueue( this._asyncQueue );
      this._asyncQueue = {};
    },

    _isSelected : function( item ) {
      return this._selectionProvider[ 0 ].call( this._selectionProvider[ 1 ], item );
    },

    _forEachRow : function( fn ) {
      var rowCount = this.getRowCount();
      for( var i = 0; i < rowCount; i++ ) {
        fn.call( this, this.getRow( i ), i );
      }
    },

    //////////////
    // Overwritten

    _applyHeight : function( value, oldValue ) {
      this.base( arguments, value, oldValue );
      this._updateRowCount();
    },

    _applyDirection : function( value ) {
      this.base( arguments, value );
      var isRTL = this.getDirection() === "rtl";
      this.$rows.css( {
        "left" : isRTL ? "" : 0,
        "right" : isRTL ? 0 : ""
      } );
      this._forEachRow( function( row ) {
        row.setMirror( isRTL );
      } );
    },

    _afterAppear : function() {
      this.base( arguments );
      this.setScrollLeft( this._scrollLeft );
    },

    supportsDrop : function() {
      return true;
    }

  }
} );

var fillLayout = {
  "position" : "absolute",
  "left" : 0,
  "top" : 0,
  "height" : "100%"
};

}( rwt.util._RWTQuery ));
