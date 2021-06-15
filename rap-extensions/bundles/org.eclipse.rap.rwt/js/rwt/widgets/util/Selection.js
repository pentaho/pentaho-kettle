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
 * Helper for rwt.widgets.util.SelectionManager, contains data for selections
 */
rwt.qx.Class.define( "rwt.widgets.util.Selection", {

  extend : rwt.qx.Object,

  construct : function() {
    this.base( arguments );
    this.__storage = [];
  },

  members : {

    /**
     * Add an item to the selection
     *
     * @type member
     * @param item {var} item to add
     * @return {void}
     */
    add : function( item ) {
      var index = this.__storage.indexOf( item );
      if( index === -1 ) {
        this.__storage.push( item );
      }
    },

    /**
     * Remove an item from the selection
     *
     * @type member
     * @param item {var} item to remove
     * @return {void}
     */
    remove : function( item ) {
      var index = this.__storage.indexOf( item );
      if( index !== -1 ) {
        this.__storage.splice( index, 1 );
      }
    },

    /**
     * Remove all items from the selection
     *
     * @type member
     * @return {void}
     */
    removeAll : function() {
      this.__storage = [];
    },

    /**
     * Check whether the selection contains a given item
     *
     * @type member
     * @param item {var} item to check for
     * @return {Boolean} whether the selection contains the item
     */
    contains : function( item ) {
      return this.__storage.indexOf( item ) !== -1;
    },

    /**
     * Convert selection to an array
     *
     * @type member
     * @return {Array} array representation of the selection
     */
    toArray : function() {
      return this.__storage.slice( 0 );
    },

    /**
     * Return first element of the Selection
     *
     * @type member
     * @return {var} first item of the selection
     */
    getFirst : function() {
      return this.__storage.length > 0 ? this.__storage[ 0 ] : null;
    },

    /**
     * Get a string representation of the Selection. The return value can be used to compare selections.
     *
     * @type member
     * @return {String} string representation of the Selection
     */
    getChangeValue : function() {
      var sb = [];
      for( var i = 0; i < this.__storage.length; i++ ) {
        sb.push( this.__storage[ i ].toHashCode() );
      }
      sb.sort();
      return sb.join( ";" );
    },

    /**
     * Whether the selection is empty
     *
     * @type member
     * @return {Boolean} whether the selection is empty
     */
    isEmpty : function() {
      return this.__storage.length === 0;
    }

  },

  destruct : function() {
    this._disposeFields( "__storage" );
  }

} );
