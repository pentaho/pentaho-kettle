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
 * This singleton manages multiple instances of rwt.widgets.base.Iframe.
 * <p>
 * The problem: When dragging over an iframe then all mouse events will be
 * passed to the document of the iframe, not the main document.
 * <p>
 * The solution: In order to be able to track mouse events over iframes, this
 * manager will block all iframes during a drag with a glasspane.
 */
rwt.qx.Class.define( "rwt.widgets.util.IframeManager", {

  extend : rwt.util.ObjectManager,

  statics : {

    getInstance : function() {
      return rwt.runtime.Singletons.get( rwt.widgets.util.IframeManager );
    }

  },

  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  construct : function() {
    this.base(arguments);

    this._blocked = {};
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
      METHODS
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @param evt {Event} TODOC
     * @return {void}
     */
    handleMouseDown : function()
    {
      var iframeMap = this._blockData = rwt.util.Objects.copy(this.getAll());
      for (var key in iframeMap) {
        iframeMap[key].block();
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @param evt {Event} TODOC
     * @return {void}
     */
    handleMouseUp : function()
    {
      var iframeMap = this._blockData;
      for (var key in iframeMap) {
        iframeMap[key].release();
      }
    }
  },


  /*
    ---------------------------------------------------------------------------
      DESTRUCTOR
    ---------------------------------------------------------------------------
  */
  destruct : function()
  {
    this._disposeFields("_blocked", "_blockData");
  }
});
