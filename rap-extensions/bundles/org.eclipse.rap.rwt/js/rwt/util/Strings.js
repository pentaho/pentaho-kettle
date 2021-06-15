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
 * Helper functions for strings.
 */
rwt.util.Strings = {

  /**
   * Checks whether the string contains a given substring.
   */
  contains : function( str, substr ) {
    return str.indexOf( substr ) !== -1;
  },

  /**
   * Returns a string with the first character converted to upper case.
   */
  toFirstUp : function( str ) {
    return str.charAt( 0 ).toUpperCase() + str.substr( 1 );
  }

};
