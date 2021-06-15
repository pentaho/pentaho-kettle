/*******************************************************************************
 * Copyright (c) 2007, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

/**
 * Store for theme values that cannot be kept in a qooxdoo theme. The store is
 * filled from the server at startup.
 */
rwt.qx.Class.define( "rwt.theme.ThemeStore", {

  extend : rwt.qx.Object,

  statics : {

    getInstance : function() {
      return rwt.runtime.Singletons.get( rwt.theme.ThemeStore );
    }

  },

  construct : function() {
    this._values = {
      dimensions : {},
      boxdims : {},
      images : {},
      gradients : {},
      fonts : {},
      colors : {},
      borders : {},
      cursors : {},
      animations : {},
      shadows : {}
    };
    this._cssValues = {};
    this._statesMap = {
      "*" : {
        "hover" : "over"
      },
      "DateTime-Calendar-Day" : {
        "unfocused" : "parent_unfocused"
      },
      "List-Item" : {
        "unfocused" : "parent_unfocused"
      },
      "Text" : {
        "read-only" : "readonly"
      },
      "TreeItem" : {
        "unfocused" : "parent_unfocused"
      },
      "Tree-RowOverlay" : {
        "unfocused" : "parent_unfocused"
      },
      "TreeColumn" : {
        "hover" : "mouseover"
      },
      "Shell" : {
        "inactive" : "!active"
      },
      "Shell-Titlebar" : {
        "inactive" : "!active"
      },
      "Shell-MinButton" : {
        "inactive" : "!active"
      },
      "Shell-MaxButton" : {
        "inactive" : "!active"
      },
      "Shell-CloseButton" : {
        "inactive" : "!active"
      },
      "TableColumn" : {
        "hover" : "mouseover"
      },
      "TableItem" : {
        "unfocused" : "parent_unfocused"
      },
      "Table-RowOverlay" : {
        "unfocused" : "parent_unfocused"
      },
      "TabItem" : {
        "selected" : "checked",
        "first" : "firstChild",
        "last" : "lastChild",
        "bottom" : "barBottom"
      }
    };
    this._namedColors = {};
  },

  members : {

    setCurrentTheme : function( themeId ) {
      this._currentTheme = themeId;
    },

    getCurrentTheme : function() {
      return this._currentTheme;
    },

    setFallbackTheme : function( themeId ) {
      this.fallbackTheme = themeId;
    },

    getFallbackTheme : function() {
      return this.fallbackTheme;
    },

    /////////////
    // Server API

    defineValues : function( values ) {
      for( var type in this._values ) {
        if( type in values ) {
          for( var key in values[ type ] ) {
            if( !( key in this._values[ type ] ) ) {
              this._values[ type ][ key ] = values[ type ][ key ];
            }
          }
        }
      }
    },

    setThemeCssValues : function( theme, values, isFallback ) {
      this._cssValues[ theme ] = values;
      if( isFallback ) {
        this.fallbackTheme = theme;
      }
      this._fillNamedColors( theme );
    },

    /////////////
    // Client API

    getColor : function( element, states, property, theme ) {
      var key = this._getCssValue( element, states, property, theme );
      var result = this._values.colors[ key ];
      if( result instanceof Array ) {
        result = "#" + rwt.util.Colors.rgbToHexString( result );
      }
      return result;
    },

    getAlpha : function( element, states, property, theme ) {
      var key = this._getCssValue( element, states, property, theme );
      var color = this._values.colors[ key ];
      var result = 1;
      if( color instanceof Array ) {
        result = color[ 3 ];
      } else if( color === "transparent" ) {
        result = 0;
      }
      return result;
    },

    getNamedColor : function( name ) {
      var result = this._namedColors[ name ];
      return result ? result : name;
    },

    getDimension : function( element, states, property, theme ) {
      var key = this._getCssValue( element, states, property, theme );
      return this._values.dimensions[ key ];
    },

    getBoxDimensions : function( element, states, property, theme ) {
      var key = this._getCssValue( element, states, property, theme );
      return this._values.boxdims[ key ];
    },

    getFloat : function( element, states, property, theme ) {
      return parseFloat( this._getCssValue( element, states, property, theme ) );
    },

    getIdentifier : function( element, states, property, theme ) {
      return this._getCssValue( element, states, property, theme );
    },

    getImage : function( element, states, property, theme ) {
      var result;
      var key = this._getCssValue( element, states, property, theme );
      var imageArray = this._values.images[ key ];
      if( imageArray != null ) {
        // TODO [rh] remove hard-coded path (first segment is defined by
        //      resource-manager)
        result = "rwt-resources/themes/images/" + key;
      } else {
        // TODO [rst] Handle null values - currently, both null and the string
        // "undefined" lead to a js error for icon property
        result = rwt.theme.ThemeValues.NONE_IMAGE;
      }
      return result;
    },

    getSizedImage : function( element, states, property, theme ) {
      var key = this._getCssValue(  element, states, property, theme );
      var imageArray = this._values.images[ key ];
      var result;
      if( imageArray != null ) {
        // TODO [tb] : Revise hardcoded path
        result = [ "rwt-resources/themes/images/" + key ].concat( imageArray );
      } else {
        result = rwt.theme.ThemeValues.NONE_IMAGE_SIZED;
      }
      return result;
    },

    getCursor : function( element, states, property, theme ) {
      var key = this._getCssValue( element, states, property, theme );
      var result = this._values.cursors[ key ];
      if( result === null ) {
        result = "rwt-resources/themes/cursors/" + key;
      }
      return result;
    },

    getAnimation : function( element, states, property, theme ) {
      var key = this._getCssValue( element, states, property, theme );
      return this._values.animations[ key ];
    },

    getFont : function( element, states, property, theme ) {
      var key = this._getCssValue( element, states, property, theme );
      var value = this._values.fonts[ key ];
      if( !( value instanceof rwt.html.Font ) ) {
        var font = new rwt.html.Font();
        font.setSize( value.size );
        font.setFamily( value.family );
        font.setBold( value.bold );
        font.setItalic( value.italic );
        this._values.fonts[ key ] = font;
      }
      return this._values.fonts[ key ];
    },

    getBorder : function( element, states, property, theme ) {
      var key = this._createCompleteBorderKey( element, states, theme );
      var radiiKey = this._getCssValue( element, states, "border-radius", theme );
      var radii = this._values.boxdims[ radiiKey ];
      var rounded = radii != null && ( radii.join( "" ) !== "0000" );
      if( rounded ) {
        key += "#" + radiiKey;
      }
      var border = this._values.borders[ key ];
      if( !border || !( border instanceof rwt.html.Border ) ) {
        var top = this.getBorderEdge( element, states, "border-top", theme );
        var right = this.getBorderEdge( element, states, "border-right", theme );
        var bottom = this.getBorderEdge( element, states, "border-bottom", theme );
        var left = this.getBorderEdge( element, states, "border-left", theme );
        var widths
          = [ top.getWidthTop(), right.getWidthTop(), bottom.getWidthTop(), left.getWidthTop() ];
        var colors
          = [ top.getColorTop(), right.getColorTop(), bottom.getColorTop(), left.getColorTop() ];
        var styles
          = [ top.getStyleTop(), right.getStyleTop(), bottom.getStyleTop(), left.getStyleTop() ];
        border = new rwt.html.Border( widths, styles, colors, rounded ? radii : undefined );
        this._values.borders[ key ] = border;
      }
      return border;
    },

    getBorderEdge : function( element, states, property, theme ) {
      var border;
      var key = this._getCssValue( element, states, property, theme );
      var value = this._values.borders[ key ];
      var resolved = value instanceof rwt.html.Border;
      var radiiKey = this._getCssValue( element, states, "border-radius", theme );
      var radii = this._values.boxdims[ radiiKey ];
      if( radii != null && ( radii.join( "" ) !== "0000" ) ) {
        var roundedBorderKey = key + "#" + radiiKey;
        border = this._values.borders[ roundedBorderKey ];
        if( !border ) {
          var width = resolved ? value.getWidthTop() : value.width;
          var style = resolved ? value.getStyleTop() : value.style;
          var color = resolved ? value.getColorTop() : value.color;
          border = new rwt.html.Border( width, style, color, radii );
          this._values.borders[ roundedBorderKey ] = border;
        }
      }
      if( !border ) {
        if( resolved ) {
          border = value;
        } else {
          border = new rwt.html.Border( value.width, value.style, value.color );
          this._values.borders[ key ] = border;
        }
      }
      return border;
    },

    getNamedBorder : function( name ) {
      var key = "_" + name;
      var result = this._values.borders[ key ];
      if( !result ) {
        var borderDef = rwt.theme.BorderDefinitions.getDefinition( name );
        if( borderDef ) {
          var color = this._resolveNamedColors( borderDef.color );
          var innerColor = this._resolveNamedColors( borderDef.innerColor );
          result = new rwt.html.Border( borderDef.width, "complex", color, innerColor );
          this._values.borders[ key ] = result;
        } else {
          result = null;
        }
      }
      return result;
    },

    getShadow : function( element, states, property, theme ) {
      var key = this._getCssValue( element, states, property, theme );
      return this._values.shadows[ key ];
    },

    getGradient : function( element, states, property, theme ) {
      var result = null;
      var key = this._getCssValue( element, states, property, theme );
      var value = this._values.gradients[ key ];
      if( value ) {
        // TODO [if] remove this check when values are rendered only once
        if( value.colors && value.percents ) {
          var gradient = [];
          for( var i = 0; i < value.colors.length; i++ ) {
            gradient[ i ] = [ value.percents[ i ] / 100, value.colors[ i ] ];
          }
          gradient.horizontal = !value.vertical;
          this._values.gradients[ key ] = gradient;
        }
        result = this._values.gradients[ key ];
      }
      return result;
    },

    ////////////
    // Internals

    _getCssValue : function( element, states, property, theme ) {
      var result;
      if( theme == null ) {
        theme = this._currentTheme;
      }
      if(    this._cssValues[ theme ] !== undefined
          && this._cssValues[ theme ][ element ] !== undefined
          && this._cssValues[ theme ][ element ][ property ] !== undefined )
      {
        var values = this._cssValues[ theme ][ element ][ property ];
        for( var i = 0; i < values.length; i++ ) {
          if( this._matches( states, element, values[ i ][ 0 ] ) ) {
            result = values[ i ][ 1 ];
            break;
          }
        }
      }
      if( result === undefined && theme != this.fallbackTheme ) {
        result = this._getCssValue( element, states, property, this.fallbackTheme );
      }
      return result;
    },

    _matches : function( states, element, constraints ) {
      var result = true;
      for( var i = 0; i < constraints.length && result; i++ ) {
        var cond = constraints[ i ];
        if( cond.length > 0 ) {
          var c = cond.charAt( 0 );
          if( c == "." ) {
            result = "variant_" + cond.substr( 1 ) in states;
          } else if( c == ":" ) {
            var state = this._translateState( cond.substr( 1 ), element );
            if( state.charAt( 0 ) == "!" ) {
              result = ! ( state.substr( 1 ) in states );
            } else {
              result = state in states;
            }
          } else if( c == "[" ) {
            result = "rwt_" + cond.substr( 1 ) in states;
          }
        }
      }
      return result;
    },

    _translateState : function( state, element ) {
      var result = state;
      if( element in this._statesMap && state in this._statesMap[ element ] ) {
        result = this._statesMap[ element ][ state ];
      } else if( state in this._statesMap[ "*" ] ) {
        result = this._statesMap[ "*" ][ state ];
      }
      return result;
    },

    _resolveNamedColors : function( colorArr ) {
      var result = null;
      if( colorArr ) {
        result = [];
        for( var i = 0; i < colorArr.length; i++ ) {
          result[ i ] = this.getNamedColor( colorArr[ i ] );
        }
      }
      return result;
    },

    // Fills a map with named colors necessary for border-definitions
    _fillNamedColors : function( theme ) {
      this._namedColors[ "darkshadow" ]
        = this.getColor( "Display", {}, "rwt-darkshadow-color", theme );
      this._namedColors[ "highlight" ]
        = this.getColor( "Display", {}, "rwt-highlight-color", theme );
      this._namedColors[ "lightshadow" ]
        = this.getColor( "Display", {}, "rwt-lightshadow-color", theme );
      this._namedColors[ "shadow" ]
        = this.getColor( "Display", {}, "rwt-shadow-color", theme );
      this._namedColors[ "thinborder" ]
        = this.getColor( "Display", {}, "rwt-thinborder-color", theme );
      // TODO [rst] eliminate these properties
      this._namedColors[ "selection-marker" ]
        = this.getColor( "Display", {}, "rwt-selectionmarker-color", theme );
      this._namedColors[ "background" ]
        = this.getColor( "*", {}, "background-color", theme );
      this._namedColors[ "foreground" ]
        = this.getColor( "*", {}, "color", theme );
      this._namedColors[ "info.foreground" ]
        = this.getColor( "Widget-ToolTip", {}, "color", theme );
    },

    _createCompleteBorderKey : function( element, states, theme ) {
      var topKey = this._getCssValue( element, states, "border-top", theme );
      var rightKey = this._getCssValue( element, states, "border-right", theme );
      var bottomKey = this._getCssValue( element, states, "border-bottom", theme );
      var leftKey = this._getCssValue( element, states, "border-left", theme );
      if( topKey === rightKey && topKey === bottomKey && topKey === leftKey ) {
        return topKey;
      }
      return topKey + "#" + rightKey + "#" + bottomKey + "#" + leftKey;
    }

  }

} );
