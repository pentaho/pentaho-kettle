/*******************************************************************************
 * Copyright (c) 2009, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

(function($){

var $labelTemplate = $( "<div>" ).css( {
  position : "absolute",
  overflow : "hidden",
  whiteSpace : "nowrap",
  textDecoration : "inherit"
} );

var $imageTemplate = $( "<div>" ).css( {
  position : "absolute",
  backgroundRepeat : "no-repeat"
} );

rwt.qx.Class.define( "rwt.widgets.base.MultiCellWidget", {

  extend : rwt.widgets.base.Terminator,

  /**
   * param cells: an array of cell types to define widget structure.
   *              Valid types are "image" and "label".
   *              Examples:
   *                [ "image" ]
   *                [ "image", "image", "label", "image" ]
   */
  construct : function( cells ) {
    this.base( arguments );
    this.$el = $( this );
    // cellData for a single cell is:
    // [ type, content, width, height, computedWidth, computedHeight, visible ]
    this.__cellData = null;
    this.$cells = null;
    this.__cellCount = null;
    this.__styleRegExp = /([a-z])([A-Z])/g;
    this.__createCellData( cells );
    this.__paddingCache = [ 0, 0, 0, 0 ];
    this.__fontCache = {};
    this.__colorCache = "";
    this._flexibleCell = -1;
    this._expandFlexCell = false;
    this.initWidth();
    this.initHeight();
    this.addToQueue( "createContent" );
    this.setOverflow( "hidden" );
    this.initSelectable();
    this.initCursor();
    this.initTextColor();
    this.initHorizontalChildrenAlign();
  },

  destruct : function() {
    this._disposeObjectDeep( "__cellData", 0 );
    this._disposeObjectDeep( "$cells", 0 );
    this._disposeObjectDeep( "__paddingCache", 0 );
    this._disposeObjectDeep( "_fontCache", 0 );
  },

  properties : {

    spacing : {
      check : "Integer",
      init : 4,
      themeable : true,
      apply : "_applySpacing",
      event : "changeSpacing"
    },

    vertical : {
      init : false,
      apply : "_applyVertical",
      themeable : true
    },

    horizontalChildrenAlign : {
      check : [ "left", "center", "right" ],
      init : "center",
      themeable : true,
      apply : "_applyHorizontalChildrenAlign"
    },

    verticalChildrenAlign : {
      check : [ "top", "middle", "bottom" ],
      init : "middle",
      themeable : true,
      apply : "_applyVerticalChildrenAlign"
    },

    textOverflow : {
      check : [ "clip", "ellipsis" ],
      init : "clip",
      themeable : true,
      apply : "_applyTextOverflow"
    },

    wordWrap : {
      check : "Boolean",
      init: false,
      apply : "_applyWordWrap"
    },

    /////////////////////////////////
    // refined properties from Widget

    selectable : {
      refine : true,
      init : false
    },

    textColor : {
      refine : true,
      init : "#000000"
    },

    cursor : {
      refine : true,
      init : "default"
    },

    allowStretchX : {
      refine : true,
      init : false
    },

    allowStretchY : {
      refine : true,
      init : false
    },

    appearance : {
      refine : true,
      init : "atom"
    },

    width : {
      refine : true,
      init : "auto"
    },

    height : {
      refine : true,
      init : "auto"
    }

  },

  members : {

    // TODO [tb] : clean up api (private/public, order)

    ///////////////////////
    // LAYOUT : public api

    /**
     * This is either the URL (image) or the text (label)
     */
    setCellContent : function( cell, value ) {
      this.__updateComputedCellDimension( cell );
      if( this._cellHasContent( cell ) != ( value != null ) ) {
        this._invalidatePreferredInnerWidth();
        this.addToQueue( "createContent" );
      } else {
        this.addToQueue( "updateContent" );
      }
      this.__cellData[ cell ][ 1 ] = value;
    },

    /**
     * The dimensions for the cell. Is mandatory for images (or 0x0 will
     * be assumed), optional for labels. Set a dimension to "null" to use the
     * computed value.
     */
    setCellDimension : function( cell, width, height ) {
      this.setCellWidth( cell, width );
      this.setCellHeight( cell, height );
    },

    /**
     * Setting visibility for a cell to false causes the element to have display:none,
     * but still to be created and layouted.
     */
    setCellVisible : function( cell, value ) {
      this.__cellData[ cell ][ 6 ] = value;
      if( this.$cells[ cell ] ) {
        this.$cells[ cell ].css( "display", value ? "" : "none" );
      }
    },

    isCellVisible : function( cell ) {
      return this.__cellData[ cell ][ 6 ];
    },

    getCellNode : function( cell ) {
      return this.$cells[ cell ] ? this.$cells[ cell ].get( 0 ) : null;
    },

    getCellContent : function( cell ) {
      return this.__cellData[ cell ][ 1 ];
    },

    setCellWidth : function( cell, width ) {
      if( this._getCellWidth( cell ) !== width ) {
        this._setCellWidth( cell, width );
        this._invalidatePreferredInnerWidth();
        this._scheduleLayoutX();
      }
    },

    setCellHeight : function( cell, height ) {
      this._setCellHeight( cell, height );
      this._invalidatePreferredInnerWidth();
      this._invalidatePreferredInnerHeight();
      this._scheduleLayoutY();
    },

    setFlexibleCell : function( value ) {
      this._flexibleCell = value;
    },

    getFlexibleCell : function() {
      return this._flexibleCell;
    },

    expandFlexCell : function( expand ) {
      if( typeof expand === "boolean" ) {
        this._expandFlexCell = expand;
      }
      return this._expandFlexCell;
    },

    // NOTE : Only needed by Tests
    getCellDimension : function( cell ) {
      var width = this.getCellWidth( cell );
      var height = this.getCellHeight( cell );
      return [ width, height ];
    },

    /**
     * Returns the user-set value for width if it exists, else the computed
     */
    getCellWidth : function( cell, ignoreFlexible ) {
      var cellEntry = this.__cellData[ cell ];
      var isFlexible = this._flexibleCell === cell && ignoreFlexible !== true;
      var width = ( cellEntry[ 2 ] != null ? cellEntry[ 2 ] : cellEntry[ 4 ] );
      if( width == null || ( isFlexible && cellEntry[ 3 ] === null ) ) {
        var computed = this.__computeCellDimension( cellEntry );
        width = computed[ 0 ];
      }
      if( isFlexible ) {
        width = this._adjustCellWidth( cell, width );
      }
      return width;
    },

    /**
     * Returns the user-set value for height if it exists, else the computed
     */
    getCellHeight : function( cell, ignoreFlexible ) {
      var cellEntry = this.__cellData[ cell ];
      var isFlexible = this._flexibleCell === cell && ignoreFlexible !== true;
      var height = ( cellEntry[ 3 ] != null ? cellEntry[ 3 ] : cellEntry[ 5 ] );
      if( height == null || ( isFlexible && cellEntry[ 3 ] === null ) ) {
        var wrapWidth = isFlexible && this.getWordWrap() ? this.getCellWidth( cell ) : null;
        var computed = this.__computeCellDimension( cellEntry, wrapWidth );
        height = computed[ 1 ];
      }
      if( isFlexible ) {
        height = this._adjustCellHeight( cell, height );
      }
      return height;
    },

    /*
    ---------------------------------------------------------------------------
      DOM/HTML
    ---------------------------------------------------------------------------
    */

    _applyElement : function( value, old ) {
      this.base( arguments, value, old );
      if( value ) {
        this._createSubElements();
        var font = this.getFont();
        if( font ) {
          font.render( this );
        } else {
          rwt.html.Font.reset( this );
        }
      }
    },

    _applyDirection : function( value ) {
      this.base( arguments, value );
      this.addToQueue( "layoutX" );
    },

    _createSubElements : function() {
      for( var i = 0; i < this.__cellCount; i++ ) {
        var hasContent = this._cellHasContent( i );
        var hasNode = this.getCellNode( i ) != null;
        if( !hasNode && hasContent ) {
          if( this._isTextCell( i ) ) {
            this.__setCellNode( i, $labelTemplate.clone() );
          } else if( this._isImageCell( i ) ) {
            this.__setCellNode( i, $imageTemplate.clone() );
          }
        } else if( hasNode && !hasContent ) {
          this.$cells[ i ].css( "display", "none" );
        } else if( hasNode && hasContent && this.isCellVisible( i ) ) {
          this.$cells[ i ].css( "display", "" );
        }
      }
      if( !this.getEnabled() ) {
        this._applyEnabled( false );
      }
      if( this.getTextOverflow() !== "clip" ) {
        this._applyTextOverflow( this.getTextOverflow() );
      }
      if( this.getWordWrap() ) {
        this._applyWordWrap( true );
      }
    },

    /*
    ---------------------------------------------------------------------------
      LAYOUT : _apply methods
    ---------------------------------------------------------------------------
    */

    _applyVertical : function() {
      this._scheduleLayoutX();
      this._scheduleLayoutY();
    },

    _applySpacing : function() {
      this._invalidatePreferredInnerWidth();
      this._scheduleLayoutX();
    },

    _applyHorizontalChildrenAlign : function( value ) {
      this._scheduleLayoutX();
      this.setStyleProperty( "textAlign", value );
    },

    _applyVerticalChildrenAlign : function() {
      this._scheduleLayoutY();
    },

    _applyPaddingTop : function( value ) {
      this.addToLayoutChanges( "paddingTop" );
      this.__paddingCache[ 0 ] = value;
      this._invalidateFrameHeight();
    },

    _applyPaddingRight : function( value ) {
      this.addToLayoutChanges( "paddingRight" );
      this.__paddingCache[ 1 ] = value;
      this._invalidateFrameWidth();
    },

    _applyPaddingBottom : function( value ) {
      this.addToLayoutChanges( "paddingBottom" );
      this.__paddingCache[ 2 ] = value;
      this._invalidateFrameHeight();
    },

    _applyPaddingLeft : function( value ) {
      this.addToLayoutChanges( "paddingLeft" );
      this.__paddingCache[ 3 ] = value;
      this._invalidateFrameWidth();
    },

    _applyEnabled : function( value, old ) {
      this.base( arguments, value, old );
      this._styleAllImagesEnabled();
    },

    _applyTextOverflow : function( value ) {
      for( var i = 0; i < this.__cellCount; i++ ) {
        if( this._isTextCell( i ) && this.$cells[ i ] ) {
          this.$cells[ i ].css( "textOverflow", value === "clip" ? "" : value );
        }
      }
    },

    _applyWordWrap : function( value  ) {
      if( this._flexibleCell !== -1 && this.$cells[ this._flexibleCell ] ) {
        this.$cells[ this._flexibleCell ].css( "whiteSpace", value ? "" : "nowrap" );
      }
    },

    /*
    ---------------------------------------------------------------------------
      LAYOUT : internals
    ---------------------------------------------------------------------------
    */

    _scheduleLayoutX : function() {
      this.addToQueue( "layoutX" );
      this._afterScheduleLayoutX();
    },

    _scheduleLayoutY : function() {
      this.addToQueue( "layoutY" );
      this._afterScheduleLayoutY();
    },

    _afterScheduleLayoutX : rwt.util.Functions.returnTrue,

    _afterScheduleLayoutY : rwt.util.Functions.returnTrue,

    _beforeComputeInnerWidth : rwt.util.Functions.returnTrue,

    _beforeComputeInnerHeight : rwt.util.Functions.returnTrue,

    _beforeRenderLayout : rwt.util.Functions.returnTrue,

    _afterRenderLayout : rwt.util.Functions.returnTrue,

    _cellHasContent : function( cell ) {
      var content = this.__cellData[ cell ][ 1 ];
      return content != null;
    },

    _isImageCell : function( cell ) {
      var type = this.__cellData[ cell ][ 0 ];
      return type == "image";
    },

    _isTextCell : function( cell ) {
      var type = this.__cellData[ cell ][ 0 ];
      return type == "label";
    },

    _setCellWidth : function( cell, width ) {
      this.__cellData[ cell ][ 2 ] = width;
    },

    _getCellWidth : function( cell ) {
      return this.__cellData[ cell ][ 2 ];
    },

    _setCellHeight : function( cell, height ) {
      this.__cellData[ cell ][ 3 ] = height;
    },

    __setCellNode : function( cell, $node ) {
      this.$cells[ cell ] = $node;
      if( $node !== null && !this.isCellVisible( cell ) ) {
        $node.css( "display", "none" );
      }
      if( $node !== null ) {
        this.$el.append( $node );
      }
    },

    __cellHasNode : function( cell ) {
      return this.$cells[ cell ] != null;
    },

    __createCellData : function( cells ) {
      var data = [];
      var nodes = [];
      this.__cellCount = cells.length;
      for( var i = 0; i < this.__cellCount; i++ ) {
        nodes[ i ] = null;
        data[ i ] = [ cells[ i ], null, null, null, null, null, true ];
      }
      this.$cells = nodes;
      this.__cellData = data;
    },

    __updateComputedCellDimension : function( cell ) {
      var cellEntry = this.__cellData[ cell ];
      cellEntry[ 4 ] = null; //delete computedWidth
      cellEntry[ 5 ] = null; //delete computedHeight
      if( cellEntry[ 2 ] == null ) { //uses computed width
        this._invalidatePreferredInnerWidth();
        this._scheduleLayoutX();
      }
      if( cellEntry[ 3 ] == null ) { //usses computedheight
        this._invalidatePreferredInnerHeight();
        this._scheduleLayoutY();
      }
    },

    // TODO: calculate in actual cell node directly when possible
    __computeCellDimension : function( cellEntry, wrapWidth ) {
      var dimension;
      if( cellEntry[ 0 ] == "label" && cellEntry[ 1 ] != null ) {
        var calc = rwt.widgets.util.FontSizeCalculation;
        dimension = calc.computeTextDimensions( cellEntry[ 1 ], this.__fontCache, wrapWidth );
      } else {
        dimension = [ 0, 0 ];
      }
      cellEntry[ 4 ] = dimension[ 0 ];
      cellEntry[ 5 ] = dimension[ 1 ];
      return dimension;
    },

    _isWidthEssential : rwt.util.Functions.returnTrue,
    _isHeightEssential : rwt.util.Functions.returnTrue,

    _computePreferredInnerWidth : function() {
      if( this.getVertical() ) {
        return this._getContentSizeOrthogonalDir();
      }
      return this._getContentSizeFlowDir( "ignoreFlexible" );
    },

    _computePreferredInnerHeight : function() {
      if( this.getVertical() ) {
        return this._getContentSizeFlowDir( "ignoreFlexible" );
      }
      return this._getContentSizeOrthogonalDir();
    },

    _adjustCellWidth : function( cell, preferredCellWidth ) {
      // NOTE: Will assume current width as valid, not to be used for widget size calculation
      var inner = this._getAvailableInnerWidth();
      var contentWidth = this._getContentSizeFlowDir( "skipFlexible" );
      var maxCellWidth = Math.max( 0, inner - contentWidth );
      var result;
      if( preferredCellWidth > maxCellWidth || this._expandFlexCell ) {
        result = maxCellWidth;
      } else {
        result = preferredCellWidth;
      }
      return result;
    },

    _getContentSizeFlowDir : function( hint ) {
      var vertical = this.getVertical();
      if( vertical ) {
        this._beforeComputeInnerHeight();
      } else {
        this._beforeComputeInnerWidth();
      }
      var ignoreFlexible = hint === "ignoreFlexible";
      var skipFlexible = hint === "skipFlexible";
      var visibleCells = 0;
      var result = 0;
      for( var i = 0; i < this.__cellCount; i++ ) {
        if( this.cellIsDisplayable( i ) ) {
          visibleCells++;
          if( !skipFlexible || ( i !== this._flexibleCell ) ) {
            result += vertical ? this.getCellHeight( i, ignoreFlexible ) : this.getCellWidth( i, ignoreFlexible );
          }
        }
      }
      result += Math.max( 0, visibleCells - 1 ) * this.getSpacing();
      return result;
    },

    _getContentSizeOrthogonalDir : function() {
      var vertical = this.getVertical();
      if( vertical ) {
        this._beforeComputeInnerWidth();
      } else {
        this._beforeComputeInnerHeight();
      }
      var result = 0;
      for( var i = 0; i < this.__cellCount; i++ ) {
        result = Math.max( result, vertical ? this.getCellWidth( i, true ) : this.getCellHeight( i, true ) );
      }
      return result;
    },

    _adjustCellHeight : function( cell, preferredCellHeight ) {
      var inner = this.getInnerHeight();
      var result;
      if( preferredCellHeight > inner ) {
        result = inner;
      } else {
        result = preferredCellHeight;
      }
      return result;
    },

    cellIsDisplayable : function( cell ) {
      return this._flexibleCell === cell || ( this.getCellWidth( cell ) > 0 );
    },

    renderPadding : function() { },

    _layoutPost : function( changes ) {
      if( changes.createContent ){
        this._createSubElements();
      }
      if( changes.updateContent || changes.createContent ) {
        this._updateAllImages();
        this._updateAllLabels();
      }
      changes.layoutX = changes.width || changes.layoutX || changes.frameWidth || changes.initial;
      changes.layoutY =    changes.height
                        || changes.layoutY
                        || changes.frameHeight
                        || changes.initial
                        || ( changes.layoutX && this._flexibleCell != -1 );
      this._beforeRenderLayout( changes );
      var vertical = this.getVertical();
      if( ( changes.layoutY && vertical ) || ( changes.layoutX && !vertical ) ) {
        this._renderLayoutFlowDir();
      }
      if( ( changes.layoutX && vertical ) || ( changes.layoutY && !vertical ) ) {
        this._renderLayoutOrthDir();
      }
      this._afterRenderLayout( changes );
      this.base( arguments, changes );
    },

    // NOTE: Currently no support for top/bottom alignment or flex-cells in vertical mode (we don't need it)
    _renderLayoutFlowDir : function() {
      var space = this.getSpacing();
      var vertical = this.getVertical();
      var offset = this._getFirstCellOffset( vertical );
      for( var i = 0; i < this.__cellCount; i++ ) {
        if( this.cellIsDisplayable( i ) ) {
          var size = vertical ? this.getCellHeight( i ) : this.getCellWidth( i );
          if( this._cellHasContent( i ) ) {
            if( this.getDirection() === "rtl" ) {
              this.$cells[ i ].css( vertical ? "top" : "right", offset );
            } else {
              this.$cells[ i ].css( vertical ? "top" : "left", offset );
            }
            this.$cells[ i ].css( vertical ? "height" : "width", Math.max( 0, size ) );
          }
          offset += ( size + space );
        }
      }
    },

    _getFirstCellOffset : function( vertical ) {
      var align = vertical ? this.getVerticalChildrenAlign() : this.getHorizontalChildrenAlign();
      var content = this._getContentSizeFlowDir();
      var inner = vertical ? this.getInnerHeight() : this._getAvailableInnerWidth();
      switch( align ) {
        case "left":
          return this.__paddingCache[ 3 ];
        case "center":
          return Math.round( this.__paddingCache[ 3 ] + inner / 2 - content / 2 );
        case "middle":
          return Math.round( this.__paddingCache[ 0 ] + inner / 2 - content / 2 );
        case "right":
          return this.__paddingCache[ 3 ] + inner - content;
      }
    },

    _renderLayoutOrthDir : function() {
      var vertical = this.getVertical();
      for( var i = 0; i < this.__cellCount; i++ ) {
        if( this._cellHasContent( i ) ) {
          this._renderCellLayoutOrthDir( i, vertical );
        }
      }
    },

    // NOTE: Currently no support for left/right alignment in vertical mode (we don't need it)
    _renderCellLayoutOrthDir : function( cell, vertical ) {
      var align = vertical ? this.getHorizontalChildrenAlign() : this.getVerticalChildrenAlign();
      var pad = this.__paddingCache;
      var inner = vertical ? this._getAvailableInnerWidth() : this.getInnerHeight();
      var cellSize = vertical ? this.getCellWidth( cell ) : this.getCellHeight( cell );
      var offset = null;
      switch( align ) {
        case "top":
          offset = pad[ 0 ];
        break;
        case "middle":
          offset = Math.round( pad[ 0 ] + inner * 0.5 - cellSize * 0.5 );
        break;
        case "center":
          offset = Math.round( pad[ 3 ] + inner * 0.5 - cellSize * 0.5 );
        break;
        case "bottom":
          offset = pad[ 0 ] + inner - cellSize;
        break;
      }
      if( this.getDirection() === "rtl" ) {
        this.$cells[ cell ].css( vertical ? "right" : "top", offset );
      } else {
        this.$cells[ cell ].css( vertical ? "left" : "top", offset );
      }
      this.$cells[ cell ].css( vertical ? "width" : "height", cellSize );
    },

    _getAvailableInnerWidth : function() {
      return this.getInnerWidth();
    },

    /*
    ---------------------------------------------------------------------------
      IMAGE
    ---------------------------------------------------------------------------
    */

    _updateAllImages : function() {
      for( var i = 0; i < this.__cellCount; i++ ) {
        if( this._isImageCell( i ) && this._cellHasContent( i ) ) {
          this._updateImage( i );
        }
      }
    },

    _styleAllImagesEnabled : function() {
      for( var i = 0; i < this.__cellCount; i++ ) {
        if( this._isImageCell( i ) && this.__cellHasNode( i ) ) {
          this._updateImage( i );
        }
      }
    },

    _updateImage : function( cell ) {
      this.$cells[ cell ].css( {
        opacity : this.getEnabled() ? 1 : 0.3,
        backgroundImage : this.getCellContent( cell )
      } );
    },

    /*
    ---------------------------------------------------------------------------
      LABEL
    ---------------------------------------------------------------------------
    */

    _applyFont : function( value ) {
      this._styleFont( value );
    },

    _styleFont : function( font ) {
      if( font ) {
        font.render( this );
        font.renderStyle( this.__fontCache );
      } else {
        rwt.html.Font.reset( this );
        rwt.html.Font.resetStyle( this.__fontCache );
      }
      for( var i = 0; i < this.__cellCount; i++ ) {
        if( this._isTextCell( i ) && this._cellHasContent( i ) ) {
          this.__updateComputedCellDimension( i );
        }
      }
    },

    _updateAllLabels : function() {
      for( var i = 0; i < this.__cellCount; i++ ) {
        if( this._isTextCell( i ) && this._cellHasContent( i ) ) {
          this._updateLabel( i );
        }
      }
    },

    _updateLabel : function( cell ) {
      this.$cells[ cell ].html( this.getCellContent( cell ) );
    }

  }

} );

}( rwt.util._RWTQuery ));
