/*******************************************************************************
 * Copyright (c) 2010, 2015 Innoopract Informationssysteme GmbH and others.
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

(function(){

rwt.widgets.util.Template = function( cells ) {
  this._cells = cells;
  this._cellRenderer = [];
  this._parseCells();
};

rwt.widgets.util.Template.prototype = {

  /**
   * @param {Object} options The options object.
   *
   * @param {HTMLElement} options.element The element in which to render the cells. The element
   * may have other children that will be ignored, but no assumptions should be made about the
   * amount or order of the cell elements added by this template.
   *
   * @param {int} options.zIndexOffset The z-index of the first cell.
   *
   * @returns {object} the container reference
   */
  _createContainer : function( options ) {
    if( !options.element || typeof options.element.nodeName !== "string" ) {
      throw new Error( "Not a valid target for TemplateContainer:" + options.element );
    }
    if( typeof options.zIndexOffset !== "number" ) {
      throw new Error( "Not a valid z-index:" + options.zIndexOffset );
    }
    return {
      "element" : options.element,
      "template" : this,
      "zIndexOffset" : options.zIndexOffset,
      "cellElements" : [],
      "cellCache" : []
    };
  },

  _getCellElement : function( container, cell ) {
    return container.cellElements[ cell ] || null;
  },

  _getCellByElement : function( container, element ) {
    return container.cellElements.indexOf( element );
  },

  /**
   * @param {Object} options The options object.
   * @param {Object} options.container Reference to a container created by this template
   * @param {Object} options.item The item to be rendered. Must implement API of GridItem.
   * @param {boolean} options.enabled Items are renderd as disabled unless this is true
   * @param {boolean} options.seeable Must indicate whether the container element is already in DOM
   * @param {boolean} options.markupEnabled Set to true to prevent escaping
   * @param {int[]} options.bounds The bounds in pixel and order of left, top, width, height
   */
  _render : function( options ) {
    if( !options.container || options.container.template !== this ) {
      throw new Error( "No valid TemplateContainer: " + options.container );
    }
    this._createElements( options );
    this._renderAllBounds( options );
    this._renderAllStyles( options );
    this._renderAllContent( options );
    this._updateCache( options );
  },

  getCellCount : function() {
    return this._cells.length;
  },

  getCellType : function( cell ) {
    return this._cells[ cell ].type;
  },

  getCellData : function( cell ) {
    return this._cells[ cell ];
  },

  isCellSelectable : function( cell ) {
    return this._cells[ cell ].selectable === true;
  },

  hasContent : function( item, cell ) {
    switch( this._getContentType( cell ) ) {
      case "text":
        return this._hasText( item, cell );
      case "image":
        return this._hasImage( item, cell );
      default:
        return false;
    }
  },

  getCellName : function( cell ) {
    return this._cells[ cell ].name || null;
  },

  getCellContent : function( item, cell, cellRenderOptions ) {
    if( !item ) {
      return null;
    }
    switch( this._getContentType( cell ) ) {
      case "text":
        return this._getText( item, cell, cellRenderOptions || {} );
      case "image":
        return this._getImage( item, cell, cellRenderOptions || {} );
      default:
        return null;
    }
  },

  _getText : function( item, cell, cellRenderOptions ) {
    if( this._isBound( cell ) ) {
      return item.getText( this._getIndex( cell ), cellRenderOptions.escaped );
    } else {
      return this._cells[ cell ].text || "";
    }
  },

  _getImage : function( item, cell ) {
    if( this._isBound( cell ) ) {
      return item.getImage( this._getIndex( cell ) );
    } else {
      return this._cells[ cell ].image || null;
    }
  },

  getCellForeground : function( item, cell ) {
    var result = null;
    if( item ) {
      if( this._isBound( cell ) ) {
        result = item.getCellForeground( this._getIndex( cell ) );
      }
      if( ( result === null || result === "" ) && this._cells[ cell ].foreground ) {
        result = this._cells[ cell ].foreground;
      }
    }
    return result;
  },

  getCellBackground : function( item, cell ) {
    var result = null;
    if( item ) {
      if( this._isBound( cell ) ) {
        result = item.getCellBackground( this._getIndex( cell ) );
      }
      if( ( result === null || result === "" ) && this._cells[ cell ].background ) {
        result = this._cells[ cell ].background;
      }
    }
    return result;
  },

  getCellFont : function( item, cell ){
    var result = null;
    if( item ) {
      if( this._isBound( cell ) ) {
        result = item.getCellFont( this._getIndex( cell ) );
      }
      if( ( result === null || result === "" ) && this._cells[ cell ].font ) {
        result = this._cells[ cell ].font;
      }
    }
    return result;
  },

  _createElements : function( options ) { // TODO [tb] : do during renderContent
    var elements = options.container.cellElements;
    var item = options.item;
    for( var i = 0; i < this._cells.length; i++ ) {
      if(    !elements[ i ]
          && this._cellRenderer[ i ]
          && ( this.hasContent( item, i ) || ( this.getCellBackground( item, i ) != null ) )
      ) {
        var element = this._cellRenderer[ i ].createElement( this._cells[ i ] );
        element.style.zIndex = options.container.zIndexOffset + i;
        options.container.element.appendChild( element );
        options.container.cellElements[ i ] = element;
        options.container.cellCache[ i ] = { "initialized" : false };
      }
    }
  },

  _renderAllContent : function( options ) {
    var cellRenderOptions = {
      "markupEnabled" : options.markupEnabled,
      "enabled" : options.enabled,
      "seeable" : options.seeable
    };
    var container = options.container;
    for( var i = 0; i < this._cells.length; i++ ) {
      var element = container.cellElements[ i ];
      if( element ) {
        var renderer = rwt.widgets.util.CellRendererRegistry.getInstance().getAll();
        var cellRenderer = renderer[ this._cells[ i ].type ];
        cellRenderOptions.width = container.cellCache[ i ].width;
        cellRenderOptions.height = container.cellCache[ i ].height;
        var renderContent = cellRenderer.renderContent;
        renderContent( element,
                       this.getCellContent( options.item, i, cellRenderOptions ),
                       this._cells[ i ],
                       cellRenderOptions );
      }
    }
  },

  _renderAllStyles : function( options ) {
    for( var i = 0; i < this._cells.length; i++ ) {
      var element = options.container.cellElements[ i ];
      if( element ) {
        var cache = options.container.cellCache[ i ];
        var background = this.getCellBackground( options.item, i );
        var foreground = this.getCellForeground( options.item, i );
        var font = this.getCellFont( options.item, i );
        if( cache.font !== font ) {
          cache.font = font;
          this._renderFont( element, font );
        }
        if( cache.color !== foreground ) {
          cache.color = foreground;
          this._renderForeground( element, foreground );
        }
        if( cache.backgroundColor !== background ) {
          cache.backgroundColor = background;
          this._renderBackground( element, background );
        }
      }
    }
  },

  // TODO [tb] : optimize to render only flexi content or if bounds changed or if new
  _renderAllBounds : function( options ) {
    var container = options.container;
    var boundsChanged = container.lastBounds !== options.bounds.join( "," );
    for( var i = 0; i < this._cells.length; i++ ) {
      var element = options.container.cellElements[ i ];
      if( element && ( boundsChanged || !container.cellCache[ i ].initialized ) ) {
        var cache = container.cellCache[ i ];
        element.style.left = this._getCellLeft( options, i ) + "px";
        element.style.top = this._getCellTop( options, i ) + "px";
        var width = this._getCellWidth( options, i );
        var height = this._getCellHeight( options, i );
        element.style.width = width + "px";
        element.style.height = height + "px";
        cache.width = width;
        cache.height = height;
      }
    }
  },

  _renderBackground : function( element, color ) {
    rwt.html.Style.setBackgroundColor( element, color );
  },

  _renderForeground : function( element, color ) {
    element.style.color = color || "inherit";
  },

  _renderFont : function( element, font ) {
    if( font ) {
      element.style.font = font;
    } else {
      rwt.html.Font.resetElement( element );
    }
  },

  _updateCache : function( options ) {
    var container = options.container;
    for( var i = 0; i < this._cells.length; i++ ) {
      if( container.cellCache[ i ] ) {
        container.cellCache[ i ].initialized = true;
      }
    }
    container.lastBounds = options.bounds.join( "," );
    container.lastItem = options.item;
  },

  /**
   * The type of content "text/image" the renderer expects
   */
  _getContentType : function( cell ) {
    var cellRenderer = this._cellRenderer[ cell ];
    return cellRenderer ? cellRenderer.contentType : null;
  },

  _getCellWidth : function( options, cell ) {
    if( this._cells[ cell ].width !== undefined ) {
      return this._cells[ cell ].width;
    }
    return   options.bounds[ 2 ]
           - ( this._getCellLeft( options, cell ) - options.bounds[ 0 ] )
           - this._getCellRight( options, cell );
  },

  _getCellHeight : function( options, cell ) {
    if( this._cells[ cell ].height !== undefined ) {
      return this._cells[ cell ].height;
    }
    return   options.bounds[ 3 ]
           - ( this._getCellTop( options, cell ) - options.bounds[ 1 ] )
           - this._getCellBottom( options, cell );
  },

  _getCellLeft : function( options, cell ) {
    var cellData = this._cells[ cell ];
    if( cellData.left !== undefined ) {
      return   options.bounds[ 0 ]
             + Math.round( cellData.left[ 0 ] * options.bounds[ 2 ] / 100 )
             + cellData.left[ 1 ];
    }
    var right = this._getCellRight( options, cell );
    return options.bounds[ 0 ] + options.bounds[ 2 ] - cellData.width - right;
  },

  _getCellTop : function( options, cell ) {
    var cellData = this._cells[ cell ];
    if( cellData.top !== undefined ) {
      return   options.bounds[ 1 ]
             + Math.round( cellData.top[ 0 ] * options.bounds[ 3 ] / 100 )
             + cellData.top[ 1 ];
    }
    var bottom = this._getCellBottom( options, cell );
    return options.bounds[ 1 ] + options.bounds[ 3 ] - cellData.height - bottom;
  },

  _getCellRight : function( options, cell ) {
    var cellData = this._cells[ cell ];
    if( cellData.right !== undefined ) {
      return Math.round( cellData.right[ 0 ] * options.bounds[ 2 ] / 100 ) + cellData.right[ 1 ];
    }
    return 0;
  },

  _getCellBottom : function( options, cell ) {
    var cellData = this._cells[ cell ];
    if( cellData.bottom !== undefined ) {
      return Math.round( cellData.bottom[ 0 ] * options.bounds[ 3 ] / 100 ) + cellData.bottom[ 1 ];
    }
    return 0;
  },

  _hasText : function( item, cell ) {
    if( !item ) {
      return false;
    } else if( this._isBound( cell ) ) {
      return item.hasText( this._getIndex( cell ) );
    } else {
      return this._cells[ cell ].text != null;
    }
  },

  _hasImage : function( item, cell ) {
    if( !item ) {
      return false;
    } else if( this._isBound( cell ) ) {
      return item.getImage( this._getIndex( cell ) ) !== null;
    } else {
      return this._cells[ cell ].image != null;
    }
  },

  _isBound : function( cell ) {
    return typeof this._cells[ cell ].bindingIndex === "number";
  },

  _getIndex : function( cell ) {
    return this._cells[ cell ].bindingIndex;
  },

  _parseCells : function() {
    var renderer = rwt.widgets.util.CellRendererRegistry.getInstance().getAll();
    for( var i = 0; i < this._cells.length; i++ ) {
      this._cellRenderer[ i ] = renderer[ this._cells[ i ].type ];
      if( this._cells[ i ].font ) {
        var font = this._cells[ i ].font;
        this._cells[ i ].font = rwt.html.Font.fromArray( font ).toCss();
      }
      if( this._cells[ i ].foreground ) {
        var foreground = this._cells[ i ].foreground;
        this._cells[ i ].foreground = rwt.util.Colors.rgbToRgbString( foreground );
      }
      if( this._cells[ i ].background ) {
        var background = this._cells[ i ].background;
        this._cells[ i ].background = rwt.util.Colors.rgbToRgbString( background );
      }
    }
  }

};

}());

