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
 * Helper functions for numbers.
 */
rwt.util.Numbers = {

  /**
   * Checks whether a value is a valid number. NaN is not considered valid.
   */
  isNumber : function( value ) {
    return typeof value === "number" && !isNaN( value );
  },

  /**
   * Check whether the number is between a given range.
   */
  isBetween : function( nr, vmin, vmax) {
    return nr > vmin && nr < vmax;
  },

  /**
   * Limit the number to the given range.
   */
  limit : function( n, vmin, vmax ) {
    if( typeof vmax === "number" && n > vmax ) {
      return vmax;
    }
    if( typeof vmin === "number" && n < vmin ) {
      return vmin;
    }
    return n;
  }

};
