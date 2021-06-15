/*******************************************************************************
 * Copyright (c) 2002, 2014 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.util.FontSizeCalculation", {

  statics : {

    _wrapNode : null,
    _measureNode : null,
    _offset : rwt.client.Client.isZoomed() ? 1 : 0,

    measureItems : function( args ) {
      var items = args.items;
      var results = {};
      for( var i = 0; i < items.length; i++ ) {
        var item = items[ i ];
        var isMarkup = item[ 7 ];
        var size = this._measureItem( item, !isMarkup );
        var id = item[ 0 ];
        results[ id ] = size;
      }
      this._storeMeasurements( id, results );
    },

    _measureItem : function( item, escapeText ) {
      var text = escapeText ? this._escapeText( item[ 1 ] ) : item[ 1 ];
      var font = rwt.html.Font.fromArray( item.slice( 2, 6 ) );
      var fontProps = {};
      font.renderStyle( fontProps );
      var width = item[ 6 ] > 0 ? item[ 6 ] : null;
      return this.computeTextDimensions( text, fontProps, width );
    },

    computeTextDimensions : function( text, fontProps, wrapWidth ) {
      var textElement = this._getMeasureNode();
      var wrapElement = this._getWrapNode();
      var style = textElement.style;
      style.fontFamily = fontProps.fontFamily || "";
      style.fontSize = fontProps.fontSize || "";
      style.fontWeight = fontProps.fontWeight || "";
      style.fontStyle = fontProps.fontStyle || "";
      textElement.innerHTML = text;
      if( wrapWidth ) {
        wrapElement.style.width = wrapWidth + "px";
        wrapElement.style.whiteSpace = "normal";
        style.whiteSpace = "normal";
      } else {
        wrapElement.style.width = "auto";
        wrapElement.style.whiteSpace = "nowrap";
        style.whiteSpace = "nowrap";
      }
      return this._measureElement( textElement );
    },

    _measureElement : rwt.util.Variant.select( "qx.client", {
      "default" : function( element ) {
        var result;
        if( element.getBoundingClientRect ) {
          // See Bug 340841
          var bounds = element.getBoundingClientRect();
          // In FF 3.0.x getBoundingClientRect has no width/height properties
          if( bounds.width != null && bounds.height != null ) {
            result = [ Math.ceil( bounds.width ), Math.round( bounds.height ) ];
          } else {
            result = [ element.scrollWidth, element.scrollHeight ];
          }
        } else {
          result = [ element.scrollWidth, element.scrollHeight ];
        }
        return this._addOffset( result );
      },
      "trident" : function( element ) {
        var computed = window.getComputedStyle( element, null );
        var result = [
          Math.ceil( parseFloat( computed.width ) ), // Ensure Texts are not cut off or wrap
          Math.round( parseFloat( computed.height ) ) // Ensure vertical alignment looks right
        ];
        return this._addOffset( result );
      }
    } ),

    _addOffset : function( bounds ) {
      var x = bounds[ 0 ] > 0 ? bounds[ 0 ] + this._offset : 0;
      var y = bounds[ 1 ] > 0 ? bounds[ 1 ] + this._offset : 0;
      return [ x, y ];
    },

    _getMeasureNode : function() {
      var node = this._measureNode;
      if( !node ) {
        node = document.createElement( "div" );
        var style = node.style;
        style.width = style.height = "auto";
        style.visibility = "hidden";
        style.position = "absolute";
        style.margin = "0px";
        style.zIndex = "-1";
        this._getWrapNode().appendChild( node );
        this._measureNode = node;
      }
      return node;
    },

    _getWrapNode : function() {
      var node = this._wrapNode;
      if( !node ) {
        node = document.createElement( "div" );
        var style = node.style;
        style.width = style.height = "auto";
        style.visibility = "hidden";
        style.padding = "0px";
        style.position = "absolute";
        style.zIndex = "-1";
        document.body.appendChild( node );
        this._wrapNode = node;
      }
      return node;
    },

    _storeMeasurements : function( id, results ) {
      var remoteObject = rwt.remote.Connection.getInstance().getRemoteObject( this );
      remoteObject.call( "storeMeasurements", {
        "results" : results
      } );
    },

    _escapeText : function( text ) {
      var EncodingUtil = rwt.util.Encoding;
      var result = EncodingUtil.escapeText( text, true );
      result = EncodingUtil.replaceNewLines( result, "<br/>" );
      result = EncodingUtil.replaceWhiteSpaces( result );
      return result;
    }

  }
} );
