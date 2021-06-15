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

/** This singleton manage stuff around image handling. */
rwt.qx.Class.define( "rwt.html.ImageManager", {

  extend : rwt.qx.Target,

  statics : {

    getInstance : function() {
      return rwt.runtime.Singletons.get( rwt.html.ImageManager );
    }

  },

  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  construct : function()
  {
    this.base(arguments);

    this.__visible = {};
    this.__all = {};
  },





  /*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */

  members :
  {
    /**
     * Register an image.
     * Gives the application the possibility to preload images.
     *
     * @type member
     * @param source {String} The incoming (unresolved) URL.
     * @return {void}
     */
    add : function(source)
    {
      var data = this.__all;

      if (data[source] === undefined) {
        data[source] = 1;
      } else {
        data[source]++;
      }
    },


    /**
     * Register an image.
     * Gives the application the possibility to preload images.
     *
     * @type member
     * @param source {String} The incoming (unresolved) URL.
     * @return {void}
     */
    remove : function(source)
    {
      var data = this.__all;

      if (data[source] !== undefined) {
        data[source]--;
      }

      if (data[source] <= 0) {
        delete data[source];
      }
    },


    /**
     * Register an visible image.
     * Gives the application the possibility to preload visible images.
     *
     * @type member
     * @param source {String} The incoming (unresolved) URL.
     * @return {void}
     */
    show : function(source)
    {
      var data = this.__visible;
      if (data[source] === undefined) {
        data[source] = 1;
      } else {
        data[source]++;
      }
    },


    /**
     * Register an image and reduce the visible counter
     * Warning: Only use after using show() before
     *
     * @type member
     * @param source {String} The incoming (unresolved) URL.
     * @return {void}
     */
    hide : function(source)
    {
      var data = this.__visible;

      if (data[source] !== undefined) {
        data[source]--;
      }

      if (data[source]<=0) {
        delete data[source];
      }
    },


    /**
     * Returns a map with the sources of all visible images
     *
     * @type member
     * @return {Map} Map with sources of all visible images
     */
    getVisibleImages : function()
    {
      var visible = this.__visible;
      var list = {};

      for (var source in visible)
      {
        if (visible[source] > 0) {
          list[source] = true;
        }
      }

      return list;
    },


    /**
     * Returns a map with the sources of all hidden images
     *
     * @type member
     * @return {Map} Map with sources of all hidden images
     */
    getHiddenImages : function()
    {
      var visible = this.__visible;
      var all = this.__all;
      var list = {};

      for (var source in all)
      {
        if (visible[source] === undefined) {
          list[source] = true;
        }
      }

      return list;
    }
  },





  /*
  *****************************************************************************
     DESTRUCTOR
  *****************************************************************************
  */

  destruct : function() {
    this._disposeFields("__all", "__visible");
  }
});
