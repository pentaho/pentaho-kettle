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

/**
 * Helper functions for arrays.
 */
rwt.util.Arrays = {

  /**
   * Convert an arguments object into an array.
   */
  fromArguments : function( args ) {
    return Array.prototype.slice.call( args, 0 );
  },

  /**
   * Expand shorthand definition to a four element array.
   * Used for box properties such as padding and margin.
   */
  fromShortHand : function( input ) {
    var len = input.length;
    if( len === 0 || len > 4 ) {
      throw new Error( "Invalid number of arguments!" );
    }
    var result = rwt.util.Arrays.copy( input );
    if( len === 1 ) {
      result[1] = result[2] = result[3] = result[0];
    } else if( len === 2 ) {
      result[2] = result[0];
      result[3] = result[1];
    } else if( len === 3 ) {
      result[3] = result[1];
    }
    return result;
  },

  /**
   * Checks whether the array contains the given element.
   */
  contains : function( arr, obj ) {
    return arr.indexOf( obj ) != -1;
  },

  /**
   * Creates a copy of the given array.
   */
  copy : function( arr ) {
    return arr.concat();
  },

  /**
   * Return the first element of the given array.
   */
  getFirst : function( arr ) {
    return arr[0];
  },

  /**
   * Returns the last element of the given array.
   */
  getLast : function( arr ) {
    return arr[arr.length - 1];
  },

  /**
   * Insert an element into the array at the given position.
   */
  insertAt : function( arr, obj, i ) {
    arr.splice( i, 0, obj );
    return arr;
  },

  /**
   * Remove the element at the given index from the array. Returns the removed element.
   */
  removeAt : function( arr, i ) {
    return arr.splice( i, 1 )[0];
  },

  /**
   * Removes an element from an array. Returns the removed element.
   */
  remove : function( arr, obj ) {
    var i = arr.indexOf( obj );
    if( i != -1 ) {
      arr.splice( i, 1 );
      return obj;
    }
  }

};
