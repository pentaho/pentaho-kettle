/*******************************************************************************
 * Copyright (c) 2004, 2018 1&1 Internet AG, Germany, http://www.1und1.de,
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

namespace( "rwt.util" );

(function() {

/*
 * Basic color keywords as defined in CSS 3
 * See http://www.w3.org/TR/css3-color/#html4
 */
var NAMED = {
  black : [0, 0, 0],
  silver : [192, 192, 192],
  gray : [128, 128, 128],
  white : [255, 255, 255],
  maroon : [128, 0, 0],
  red : [255, 0, 0],
  purple : [128, 0, 128],
  fuchsia : [255, 0, 255],
  green : [0, 128, 0],
  lime : [0, 255, 0],
  olive : [128, 128, 0],
  yellow : [255, 255, 0],
  navy : [0, 0, 128],
  blue : [0, 0, 255],
  teal : [0, 128, 128],
  aqua : [0, 255, 255],
  transparent : [-1, -1, -1]
};

var REGEXP = {
  hex3 : /^#([0-9a-fA-F]{1})([0-9a-fA-F]{1})([0-9a-fA-F]{1})$/,
  hex6 : /^#([0-9a-fA-F]{1})([0-9a-fA-F]{1})([0-9a-fA-F]{1})([0-9a-fA-F]{1})([0-9a-fA-F]{1})([0-9a-fA-F]{1})$/,
  rgb : /^rgb\(\s*([0-9]{1,3}\.{0,1}[0-9]*)\s*,\s*([0-9]{1,3}\.{0,1}[0-9]*)\s*,\s*([0-9]{1,3}\.{0,1}[0-9]*)\s*\)$/,
  rgba : /^rgba\(\s*([0-9]{1,3}\.{0,1}[0-9]*)\s*,\s*([0-9]{1,3}\.{0,1}[0-9]*)\s*,\s*([0-9]{1,3}\.{0,1}[0-9]*)\s*,\s*([0-9]{1,3}\.{0,1}[0-9]*)\s*\)$/
};

/**
 * Methods to convert colors between different color spaces.
 */
rwt.util.Colors = {

  /**
   * Detects if a string is a valid color.
   */
  isValid : function( str ) {
    return isNamedColor( str ) || isHex3String( str ) || isHex6String( str ) || isRgbString( str ) || isRgbaString( str );
  },

  /**
   * Converts a string to an RGB array. Supports named colors, rgb(), #xxx, and #xxxxxx.
   */
  stringToRgb : function( str ) {
    if( isNamedColor( str ) ) {
      return NAMED[str];
    } else if( isRgbString( str ) ) {
      return rgbStringToRgb();
    } else if( isRgbaString( str ) ) {
      return rgbaStringToRgba();
    } else if( isHex3String( str ) ) {
      return hex3StringToRgb();
    } else if( isHex6String( str ) ) {
      return hex6StringToRgb();
    }
    throw new Error( "Could not parse color: " + str );
  },

  /**
   * Converts an RGB array to a string of the form "rgb(r, g, b)".
   */
  rgbToRgbString : function( rgb ) {
    return "rgb(" + rgb[0] + "," + rgb[1] + "," + rgb[2] + ")";
  },

  rgbaToRgbaString : function( rgba ) {
    var alpha = ( rgba[3] || 255 ) / 255;
    return "rgba(" + rgba[0] + "," + rgba[1] + "," + rgba[2] + "," + alpha + ")";
  },

  /**
   * Converts a RGB array to a string of the form "xxxxxx".
   */
  rgbToHexString : function( rgb ) {
    return hexstr( rgb[0] ) + hexstr( rgb[1] ) + hexstr( rgb[2] );
  }

};

function hexstr( number ) {
  var hexstr = number.toString( 16 ).toLowerCase();
  return hexstr.length == 1 ? "0" + hexstr : hexstr;
}

function isNamedColor( value ) {
  return NAMED[value] !== undefined;
}

function isHex3String( str ) {
  return REGEXP.hex3.test( str );
}

function isHex6String( str ) {
  return REGEXP.hex6.test( str );
}

function isRgbString( str ) {
  return REGEXP.rgb.test( str );
}

function isRgbaString( str ) {
  return REGEXP.rgba.test( str );
}

function hex3StringToRgb() {
  var r = parseInt( RegExp.$1, 16 ) * 17;
  var g = parseInt( RegExp.$2, 16 ) * 17;
  var b = parseInt( RegExp.$3, 16 ) * 17;
  return [r, g, b];
}

function hex6StringToRgb() {
  var r = (parseInt( RegExp.$1, 16 ) * 16) + parseInt( RegExp.$2, 16 );
  var g = (parseInt( RegExp.$3, 16 ) * 16) + parseInt( RegExp.$4, 16 );
  var b = (parseInt( RegExp.$5, 16 ) * 16) + parseInt( RegExp.$6, 16 );
  return [r, g, b];
}

function rgbStringToRgb() {
  var r = parseInt( RegExp.$1, 10 );
  var g = parseInt( RegExp.$2, 10 );
  var b = parseInt( RegExp.$3, 10 );
  return [r, g, b];
}

function rgbaStringToRgba() {
  var r = parseInt( RegExp.$1, 10 );
  var g = parseInt( RegExp.$2, 10 );
  var b = parseInt( RegExp.$3, 10 );
  var a = Math.round( parseFloat( RegExp.$4 ) * 255 );
  return [r, g, b, a ];
}

})();
