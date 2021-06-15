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

/**
 * Helper functions for functions.
 */
rwt.define( "rwt.util.Functions", {

  /**
   * Always returns true.
   */
  returnTrue : function() {
    return true;
  },

  /**
   * Always returns false.
   */
  returnFalse : function() {
    return false;
  },

  /**
   * Always returns null.
   */
  returnNull : function() {
    return null;
  },

  /**
   * Always returns 0.
   */
  returnZero : function() {
    return 0;
  },

  /**
   * Binds a function to a context object, i.e. `this` will be bound to the context object.
   */
  bind: function( fn, context ) {
    return function() {
      return fn.apply( context, arguments );
    };
  }

});
