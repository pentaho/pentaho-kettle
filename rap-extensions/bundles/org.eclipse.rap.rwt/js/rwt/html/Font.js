/*******************************************************************************
 * Copyright (c) 2004, 2013 1&1 Internet AG, Germany, http://www.1und1.de,
 *                          EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for the Eclipse Remote Application Platform
 ******************************************************************************/

/**
 * A wrapper for CSS font styles. Fond objects can be aplpied to widgets
 * or DOM elements.
 */
rwt.qx.Class.define( "rwt.html.Font", {

  extend : rwt.qx.Object,

  /**
   * @param size {String} The font size (Unit: pixel)
   * @param family {String[]} A sorted list of font families
   */
  construct : function( size, family ) {
    this.base( arguments );
    if( size !== undefined ) {
      this.setSize( size );
    }
    if( family !== undefined ) {
      this.setFamily( family );
    }
  },

  statics : {

    _fontPool : {},

    /**
     * Converts a typical CSS font definition string to an font object
     *
     * @type static
     * @param str {String} the CSS string
     * @return {rwt.html.Font} the created instance
     */
    fromString : function( str ) {
      var font = new rwt.html.Font();
      var parts = str.split( /\s+/ );
      var name = [];
      for( var i = 0; i < parts.length; i++ ) {
        var part = parts[ i ];
        switch( part ) {
          case "bold":
            font.setBold( true );
            break;
          case "italic":
            font.setItalic( true );
            break;
          case "underline":
            font.setDecoration( "underline" );
            break;
          default:
            var temp = parseInt( part, 10 );
            if( temp == part || rwt.util.Strings.contains( part, "px" ) ) {
              font.setSize(temp);
            } else {
              name.push(part);
            }
            break;
        }
      }
      if( name.length > 0 ) {
        font.setFamily( name );
      }
      return font;
    },

    fromArray : function( arr ) {
      var name = arr[ 0 ];
      var size = arr[ 1 ];
      var bold = arr[ 2 ];
      var italic = arr[ 3 ];
      var id = name + size + bold + italic;
      var font = this._fontPool[ id ]; // TODO [tb] : use pool for all static methods
      if( !font ) {
        font = new rwt.html.Font( size, name );
        font.setBold( bold );
        font.setItalic( italic );
        this._fontPool[ id ] = font;
      }
      return font;
    },

    /**
     * Converts a map property definition into a border object.
     *
     * @type static
     * @param config {Map} map of property values
     * @return {rwt.html.Font} the created instance
     */
    fromConfig : function( config ) {
      var font = new rwt.html.Font();
      font.set( config );
      return font;
    },

    /**
     * Removes all fond styles from this widget
     *
     * @param widget {rwt.widgets.base.Widget} widget to reset
     */
    reset : function( widget ) {
      widget.removeStyleProperty("fontFamily");
      widget.removeStyleProperty("fontSize");
      widget.removeStyleProperty("fontWeight");
      widget.removeStyleProperty("fontStyle");
      widget.removeStyleProperty("textDecoration");
    },

    /**
     * Removes all fond styles from this DOM element
     *
     * @param element {Element} DOM element to reset
     */
    resetElement : function( element ) {
      var style = element.style;
      style.fontFamily = "";
      style.fontSize = "";
      style.fontWeight = "";
      style.fontStyle = "";
      style.textDecoration = "";
    },

    /**
     * Reset a style map by setting the font attributes to empty.
     *
     * @param style {Map} The style map
     * @type static
     * @return {void}
     */
    resetStyle : function( style ) {
      style.fontFamily = "";
      style.fontSize = "";
      style.fontWeight = "";
      style.fontStyle = "";
      style.textDecoration = "";
    }
  },

  properties : {

    /** The font size (Unit: pixel) */
    size : {
      check : "Integer",
      nullable : true,
      apply : "_applySize"
    },

    /** A sorted list of font families */
    family : {
      check : "Array",
      nullable : true,
      apply : "_applyFamily"
    },

    /** Whether the font is bold */
    bold : {
      check : "Boolean",
      nullable : true,
      apply : "_applyBold"
    },

    /** Whether the font is italic */
    italic : {
      check : "Boolean",
      nullable : true,
      apply : "_applyItalic"
    },

    /** The text decoration for this font */
    decoration : {
      check : [ "underline", "line-through", "overline" ],
      nullable : true,
      apply : "_applyDecoration"
    }
  },

  members : {
    __size : null,
    __family : null,
    __bold : null,
    __italic : null,
    __decoration : null,

    _applySize : function(value) {
      this.__size = value === null ? null : value + "px";
    },

    _applyFamily : function( value ) {
      var family = "";
      for( var i = 0, l = value.length; i < l; i++ ) {
        if( value[ i ].indexOf( " " ) > 0 ) {
          family += '"' + value[ i ] + '"';
        } else {
          // in FireFox 2 and WebKit fonts like 'serif' or 'sans-serif' must
          // not be quoted!
          family += value[ i ];
        }
        if( i != l-1 ) {
          family += ",";
        }
      }
      this.__family = family;
    },

    _applyBold : function( value ) {
      this.__bold = value === null ? null : value ? "bold" : "normal";
    },

    _applyItalic : function( value ) {
      this.__italic = value === null ? null : value ? "italic" : "normal";
    },

    _applyDecoration : function( value ) {
      this.__decoration = value === null ? null : value;
    },

    /**
     * Apply the font to the given widget.
     *
     * @param widget {rwt.widgets.base.Widget} The widget to apply the font to
     */
    render : function( widget ) {
      widget.setStyleProperty( "fontFamily", this.__family );
      widget.setStyleProperty( "fontSize", this.__size );
      widget.setStyleProperty( "fontWeight", this.__bold );
      widget.setStyleProperty( "fontStyle", this.__italic );
      widget.setStyleProperty( "textDecoration", this.__decoration );
    },

    /**
     * Generate a style map with the current font attributes.
     *
     * @param style {Map} The style map
     * @type member
     * @return {void}
     */
    renderStyle : function( style ) {
      style.fontFamily = this.__family || "";
      style.fontSize = this.__size || "";
      style.fontWeight = this.__bold || "";
      style.fontStyle =  this.__italic || "";
      style.textDecoration = this.__decoration || "";
    },

    /**
     * Apply the font styles to the given DOM element.
     *
     * @param element {Element} The DOM element to apply the font to
     */
    renderElement : function( element ) {
      var style = element.style;
      style.fontFamily = this.__family || "";
      style.fontSize = this.__size || "";
      style.fontWeight = this.__bold || "";
      style.fontStyle =  this.__italic || "";
      style.textDecoration = this.__decoration || "";
    },

    toCss : function() {
      return ( this.getItalic() ? "italic " : "" ) +
             ( this.getBold() ? "bold " : "" ) +
             ( this.__size ? this.getSize() + "px " : "" ) +
             ( this.__family ? this.__family.replace(/\"/g, "'") : "" );
    }
  }
} );
