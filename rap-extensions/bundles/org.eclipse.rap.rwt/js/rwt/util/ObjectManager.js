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

/** This class allows basic managment of assigned objects. */
rwt.qx.Class.define("rwt.util.ObjectManager",
{
  extend : rwt.qx.Target,




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
    /*
    ---------------------------------------------------------------------------
      USER API
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @param vObject {var} TODOC
     * @return {void | Boolean} TODOC
     */
    add : function(vObject)
    {
      if (this.getDisposed()) {
        return;
      }

      this._objects[vObject.toHashCode()] = vObject;
    },


    /**
     * TODOC
     *
     * @type member
     * @param vObject {var} TODOC
     * @return {void | Boolean} TODOC
     */
    remove : function(vObject)
    {
      if (this.getDisposed()) {
        return false;
      }

      delete this._objects[vObject.toHashCode()];
    },


    /**
     * TODOC
     *
     * @type member
     * @param vObject {var} TODOC
     * @return {var} TODOC
     */
    has : function(vObject) {
      return this._objects[vObject.toHashCode()] != null;
    },


    /**
     * TODOC
     *
     * @type member
     * @param vObject {var} TODOC
     * @return {var} TODOC
     */
    get : function(vObject) {
      return this._objects[vObject.toHashCode()];
    },


    /**
     * TODOC
     *
     * @type member
     * @return {var} TODOC
     */
    getAll : function() {
      return this._objects;
    }

  },




  /*
  *****************************************************************************
     DESTRUCTOR
  *****************************************************************************
  */

  destruct : function() {
    this._disposeObjectDeep("_objects");
  }
});
