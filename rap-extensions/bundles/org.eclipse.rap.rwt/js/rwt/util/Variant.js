/*******************************************************************************
 * Copyright (c) 2004, 2014 1&1 Internet AG, Germany, http://www.1und1.de,
 *                          EclipseSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for the Eclipse Remote Application Platform
 ******************************************************************************/

/*global qxvariants:false*/

/**
 * Manage variants of source code. May it be for different debug options,
 * browsers or other environment flags.
 *
 * Variants enable the selection and removal of code from the build version.
 * A variant consists of a collection of states from which exactly one is active
 * at load time of the framework. The global map <code>qxvariants</code> can be
 * used to select a variant before the Framework is loades.
 *
 * Depending on the selected variant a specific code
 * path can be choosen using the <code>select</code> method. The generator is
 * able to set a variant and remove all code paths which are
 * not selected by the variant.
 *
 * Variants are used to implement browser optimized builds and to remove
 * debugging code from the build version. It is very similar to conditional
 * compilation in C/C++.
 */
rwt.util.Variant = {

  /** {Map} stored variants */
  __variants : {},

  /**
   * Import settings from global qxvariants into current environment
   */
  __init : function() {
    if( window.qxvariants ) {
      for( var name in qxvariants ) {
        this.__variants[ name ] = qxvariants[ name ];
      }
      window.qxvariants = undefined;
      try {
        delete window.qxvariants;
      } catch( ex ) {
      }
    }
  },

  /**
   * Define a variant
   *
   * @param name {String} unique name for the variant
   * @param value {String} value for the variant
   */
  define : function( name, value ) {
    if( name in this.__variants ) {
      throw new Error( "Variant already defined: '" + name + "'" );
    }
    this.__variants[ name ] = value;
  },

  /**
   * Get the current value of a variant.
   *
   * @param name {String} name of the variant
   * @return {String} current value of the variant
   */
  get : function( name ) {
    var data = this.__variants[ name ];
    if( data === undefined ) {
      throw new Error( 'Undefined variant "' + name + '"' );
    }
    return data;
  },

  /**
   * Select a function depending on the value of the variant.
   *
   * Example:
   *
   * <pre class='javascript'>
   * var f = rwt.util.Variant.select( "qx.client", {
   *   "gecko": function() { ... },
   *   "trident|webkit": function() { ... },
   *   "default": function() { ... }
   * });
   * </pre>
   *
   * Depending on the value of the <code>"qx.client"</code> variant this will select the
   * corresponding function. The first case is selected if the variant is "gecko", the second
   * is selected if the variant is "trident" or "webkit" and the third function is selected if
   * none of the other names match the variant.
   *
   * @param name {String} name of the variant. To enable the generator to optimize
   *   this selection, the name must be a string literal.
   * @param functions {Map} map with variant names as names and functions as values.
   * @return {Function} The selected function from the map.
   */
  select : function( name, functions ) {
    if( typeof this.__variants[ name ] === "undefined" ) {
      throw new Error( "Variant not defined: '" + name + "'" );
    }
    for( var variant in functions ) {
      if( this.isSet( name, variant ) ) {
        return functions[ variant ];
      }
    }
    if( functions[ "default" ] !== undefined ) {
      return functions[ "default" ];
    }
    throw new Error( "No match for variant: '" + name + "'" );
  },

  /**
   * Check whether a variant is set to a given value. To enable the generator to optimize
   * this selection, both parameters must be string literals.
   *
   * This method is meant to be used in if statements to select code paths. If the condition of
   * an if statement is only this method, the generator is able to optimize the if
   * statement.
   *
   * Example:
   *
   * <pre class='javascript'>
   * if (rwt.util.Variant.isSet("qx.client", "trident")) {
   *   // some Internet Explorer specific code
   * } else if(rwt.util.Variant.isSet("qx.client", "webkit")){
   *   // Opera specific code
   * } else {
   *   // common code for all other browsers
   * }
   * </pre>
   *
   * @param name {String} name of the variant
   * @param variants {String} value to check for. Several values can be "or"-combined by
   *   separating them with a "|" character. A value of "trident|webkit" would for example
   *   check if the variant is set to "trident" or "webkit"
   * @return {Boolean} whether the variant is set to the given value
   */
  isSet : function( name, variants ) {
    // fast path
    var actual = this.get( name );
    if( variants.indexOf( "|" ) < 0 ) {
      return actual === variants;
    }
    var nameParts = variants.split( "|" );
    for( var i = 0, l = nameParts.length; i < l; i++ ) {
      if( actual === nameParts[ i ] ) {
        return true;
      }
    }
    return false;
  }

};

rwt.util.Variant.define( "qx.debug", "off" );
rwt.util.Variant.__init();
