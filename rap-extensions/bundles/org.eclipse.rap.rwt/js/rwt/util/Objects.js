/*******************************************************************************
 * Copyright (c) 2004, 2014 1&1 Internet AG, Germany, http://www.1und1.de,
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

var shadowsKeys = !( { toString: null } ).propertyIsEnumerable( "toString" );

var shadowedKeys = [
  "isPrototypeOf",
  "hasOwnProperty",
  "toLocaleString",
  "toString",
  "valueOf",
  "constructor"
];

/**
 * Helper functions for objects.
 */
rwt.util.Objects = {

  /**
   * Returns a copy of an object.
   */
  copy : function( source ) {
    var clone = {};
    for( var key in source ) {
      clone[key] = source[key];
    }
    return clone;
  },

  /**
   * Convert an array into a object.
   *
   * All elements of the array become keys of the returned object by
   * calling "toString" on the array elements. The values of the
   * object are set to "true"
   */
  fromArray: function( array ) {
    var obj = {};
    for( var i = 0, l = array.length; i < l; i++ ) {
      obj[array[i].toString()] = true;
    }
    return obj;
  },

  /**
   * Checks if the given object contains any keys.
   */
  isEmpty : function( object ) {
    /*jshint unused:false */
    for( var key in object ) {
      return false;
    }
    return true;
  },

  /**
   * Retrieves all keys of an object and its prototype chain.
   */
  getKeys : function( object ) {
    var arr = [];
    for( var key in object ) {
      arr.push( key );
    }
    // Old IEs don't include "shadowed" keys in a for-each loop even if they are defined directly
    // in the object.
    if( shadowsKeys ) {
      for( var i = 0, l = shadowedKeys.length; i < l; i++ ) {
        if( object.hasOwnProperty( shadowedKeys[i] ) ) {
          arr.push( shadowedKeys[i] );
        }
      }
    }
    return arr;
  },

  /**
   * Retrieves all values of an object and its prototype chain.
   */
  getValues : function( object ) {
    var arr = [];
    for( var key in object ) {
      arr.push( object[key] );
    }
    return arr;
  },

  /**
   * Inserts all keys of the source object into the target object. The target object is modified.
   * Returns the modified target object.
   */
  mergeWith : function( target, source, overwrite ) {
    if( overwrite === undefined ) {
      overwrite = true;
    }
    for( var key in source ) {
      if( overwrite || target[key] === undefined ) {
        target[key] = source[key];
      }
    }
    return target;
  }

};

})();
