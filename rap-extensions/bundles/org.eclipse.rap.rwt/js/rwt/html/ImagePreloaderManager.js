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

/** This singleton manage all rwt.html.ImagePreloader instances. */
rwt.qx.Class.define( "rwt.html.ImagePreloaderManager", {

  extend : rwt.qx.Object,

  statics : {

    getInstance : function() {
      return rwt.runtime.Singletons.get( rwt.html.ImagePreloaderManager );
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

    this._objects = {};
  },




  /*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */

  members :
  {
    /**
     * Adds a rwt.html.ImagePreloader instance to the manager
     *
     * @type member
     * @param vObject {Preloader} rwt.html.ImagePreloader instance
     * @return {void}
     */
    add : function(vObject) {
      this._objects[vObject.getUri()] = vObject;
    },


    /**
     * Removes a rwt.html.ImagePreloader instance from the manager
     *
     * @type member
     * @param vObject {Preloader} rwt.html.ImagePreloader instance
     * @return {void}
     */
    remove : function(vObject) {
      delete this._objects[vObject.getUri()];
    },


    /**
     * Returns whether an image preloader instance with the given source is registered
     *
     * @type member
     * @param vSource {String} Source of preloader image instance
     * @return {Boolean} whether an image preloader instance has given source
     */
    has : function(vSource) {
      return this._objects[vSource] != null;
    },


    /**
     * Return image preloader instance with given source
     *
     * @type member
     * @param vSource {String} Source of preloader image instance
     * @return {Preloader} rwt.html.ImagePreloader instance
     */
    get : function(vSource) {
      return this._objects[vSource];
    },


    /**
     * Create new qx.io.image.preloader instance with given source
     *
     * @type member
     * @param vSource {String} Source of preloader image instance
     * @return {Preloader} new rwt.html.ImagePreloader instance
     */
    create : function(vSource)
    {
      if (this._objects[vSource]) {
        return this._objects[vSource];
      }

      return new rwt.html.ImagePreloader(vSource);
    }
  },




  /*
  *****************************************************************************
     DESTRUCTOR
  *****************************************************************************
  */

  destruct : function() {
    this._disposeFields("_objects");
  }
});
