/*******************************************************************************
 * Copyright (c) 2004, 2017 1&1 Internet AG, Germany, http://www.1und1.de,
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
 * This widget represents an image.
 *
 * @appearance image
 */
rwt.qx.Class.define("rwt.widgets.base.Image",
{
  extend : rwt.widgets.base.Terminator,




  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  /**
   * @param vSource {String} URL of the image
   * @param vWidth {Integer|String ? "auto"} definition of the width of the image
   * @param vHeight {Integer|String ? "auto"} definition of the height of the image
   */
  construct : function(vSource, vWidth, vHeight)
  {
    this.base(arguments);

    this._blank = rwt.remote.Connection.RESOURCE_PATH + "static/image/blank.gif";

    // Source
    if (vSource != null) {
      this.setSource(vSource);
    }

    // Dimensions
    if (vWidth != null) {
      this.setWidth(vWidth);
    } else {
      this.initWidth();
    }

    if (vHeight != null) {
      this.setHeight(vHeight);
    } else {
      this.initHeight();
    }

    // Property init
    this.initSelectable();
  },

  /*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

  properties :
  {
    /*
    ---------------------------------------------------------------------------
      REFINED PROPERTIES
    ---------------------------------------------------------------------------
    */

    allowStretchX :
    {
      refine : true,
      init : false
    },

    allowStretchY :
    {
      refine : true,
      init : false
    },

    selectable :
    {
      refine : true,
      init : false
    },

    width :
    {
      refine : true,
      init : "auto"
    },

    height :
    {
      refine : true,
      init : "auto"
    },

    appearance :
    {
      refine : true,
      init : "image"
    },





    /*
    ---------------------------------------------------------------------------
      OWN PROPERTIES
    ---------------------------------------------------------------------------
    */

    /** The source uri of the image. */
    source :
    {
      check : "String",
      apply : "_applySource",
      event : "changeSource",
      nullable : true,
      themeable : true
    },


    /** The assigned preloader instance of the image. */
    preloader :
    {
      check : "rwt.html.ImagePreloader",
      apply : "_applyPreloader",
      nullable : true
    },


    /**
     * The loading status.
     *
     *  True if the image is loaded correctly. False if no image is loaded
     *  or the one that should be loaded is currently loading or not available.
     */
    loaded :
    {
      check : "Boolean",
      init : false,
      apply : "_applyLoaded",
      event : "changeLoaded"
    },


    /** Should the image be maxified in it's own container? */
    resizeToInner :
    {
      check : "Boolean",
      init : false
    }
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
      EVENT MAPPERS
    ---------------------------------------------------------------------------
    */

    /**
     * Listener method of the "load" event - sets "loaded" property
     *
     * @type member
     * @return {void}
     */
    _onload : function() {
      this.setLoaded(true);
    },

    /*
    ---------------------------------------------------------------------------
      DISPLAYBLE HANDLING
    ---------------------------------------------------------------------------
    */

    /**
     * Registers an image at the image manager (rwt.html.ImageManager) and increases the
     * visible counter
     *
     * @type member
     * @return {void}
     */
    _beforeAppear : function()
    {
      var source = this.getSource();
      if (source)
      {
        rwt.html.ImageManager.getInstance().show(source);
        this._registeredAsVisible = true;
      }

      return this.base(arguments);
    },


    /**
     * Registers an image at the image manager (rwt.html.ImageManager) and reduces the
     * visible counter
     *
     * @type member
     * @return {void}
     */
    _beforeDisappear : function()
    {
      var source = this.getSource();
      if (source && this._registeredAsVisible)
      {
        rwt.html.ImageManager.getInstance().hide(source);
        delete this._registeredAsVisible;
      }

      return this.base(arguments);
    },




    /*
    ---------------------------------------------------------------------------
      MODIFIERS
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applySource : function(value, old)
    {
      var imageMgr = rwt.html.ImageManager.getInstance();

      if (old)
      {
        imageMgr.remove(old);

        if (this._registeredAsVisible)
        {
          imageMgr.hide(old);
          delete this._registeredAsVisible;
        }
      }

      if (value)
      {
        imageMgr.add(value);

        if (this.isSeeable())
        {
          this._registeredAsVisible = true;
          imageMgr.show(value);
        }
      }

      if (this.isCreated()) {
        this._connect();
      }
    },

    /**
     * Connects a callback method to the value manager to ensure
     * that changes to the source are handled by the image instance
     *
     * @type member
     * @return {void}
     */
    _connect : function() {
      this._syncSource( this.getSource() );
    },

    /**
     * Sets the preloader property (with creating a new instance)
     *
     * @param value {String} source of image instance
     * @return {void}
     */
    _syncSource : function(value)
    {
      if (value === null)
      {
        this.setPreloader(null);
      }
      else
      {
        var preloader = rwt.html.ImagePreloaderManager.getInstance().create(value);
        this.setPreloader(preloader);
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyPreloader : function(value, old)
    {
      if (old)
      {
        // remove event connection
        old.removeEventListener("load", this._onload, this);
      }

      if (value)
      {
        // Omit  here, otherwise the later setLoaded(true)
        // will not be executed (prevent recursion)
        this.setLoaded(false);

        if( !value.isErroneous() && value.isLoaded() ) {
          this.setLoaded(true);
        } else {
          value.addEventListener( "load", this._onload, this );
        }
      }
      else
      {
        this.setLoaded(false);
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyLoaded : function(value)
    {
      if (value && this.isCreated())
      {
        this._renderContent();
      }
      else if (!value)
      {
        this._invalidatePreferredInnerWidth();
        this._invalidatePreferredInnerHeight();
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyElement : function(value, old)
    {
      if (value)
      {
        if (!this._image)
        {
          try
          {
            this._createImageNode();
            this._image.style.border = "0 none";
            this._image.style.verticalAlign = "top";
            this._image.alt = "";
            this._image.title = "";
          }
          catch(ex) {
            throw new Error( "Failed while creating image #1 " + ex );
          }

          this._imageNodeCreated();
        }

        value.appendChild(this._image);
      }

      // call widget implmentation
      this.base(arguments, value, old);

      if (value && this.getSource()) {
        this._connect();
      }
    },

    // Create Image-Node
    // Webkit has problems with "new Image". Maybe related to "new Function" with
    // is also not working correctly.
    _createImageNode : rwt.util.Variant.select( "qx.client", {
      "webkit|blink" : function() {
        this._image = document.createElement("img");
      },
      "default" : function() {
        this._image = new Image();
      }
    } ),

    _imageNodeCreated : rwt.util.Variant.select( "qx.client", {
      "gecko|webkit|blink" : function() {
        this._styleEnabled();
      },
      "default": rwt.util.Functions.returnTrue
    } ),

    /*
    ---------------------------------------------------------------------------
      CLIENT OPTIMIZED MODIFIERS
    ---------------------------------------------------------------------------
    */

    /**
     * Internal method (called by the layout engine)
     * Applies the dimensions and then sets the source of the image instance
     *
     * @type member
     * @return {void}
     */
    _postApply : function()
    {
      this._postApplyDimensions();
      this._updateContent();
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     * @return {void}
     * @signature function(value, old)
     */
    _applyEnabled : function(value, old)
    {
      if (this._image) {
        this._styleEnabled();
      }

      return this.base(arguments, value, old);
    },


    /**
     * Updates the source of the image instance
     *
     * @type member
     * @return {void}
     * @signature function()
     */
    _updateContent : function() {
      var pl = this.getPreloader();
      this._image.src = pl && pl.isLoaded() ? pl.getSource() : this._blank;
    },


    /**
     * Reset the source of the image instance to a blank image
     *
     * @type member
     * @return {void}
     * @signature function()
     */
    _resetContent : function() {
      this._image.src = this._blank;
    },

    /**
     * Sets the style values for the states enabled/disabled
     *
     * @type member
     * @return {void}
     * @signature function()
     */
    _styleEnabled : function() {
      if (this._image)
      {
        var o = this.getEnabled()===false ? 0.3 : "";
        var s = this._image.style;

        s.opacity = s.KhtmlOpacity = s.MozOpacity = o;
      }
    },




    /*
    ---------------------------------------------------------------------------
      PREFERRED DIMENSIONS: INNER
    ---------------------------------------------------------------------------
    */

    /**
     * Returns width value of preloader or 0 (if preloader is not available)
     *
     * @type member
     * @return {Integer} Returns width value of preloader or 0 (if preloader is not available)
     */
    _computePreferredInnerWidth : function()
    {
      var preloader = this.getPreloader();
      return preloader ? preloader.getWidth() : 0;
    },


    /**
     * Returns height value of preloader or 0 (if preloader is not available)
     *
     * @type member
     * @return {Integer} Returns height value of preloader or 0 (if preloader is not available)
     */
    _computePreferredInnerHeight : function()
    {
      var preloader = this.getPreloader();
      return preloader ? preloader.getHeight() : 0;
    },




    /*
    ---------------------------------------------------------------------------
      APPLY
    ---------------------------------------------------------------------------
    */

    /**
     * Sets the style attributes for width and height
     *
     * @type member
     * @return {void}
     * @signature function()
     */
    _postApplyDimensions : function() {
      try
      {
        var vImageNode = this._image;

        if (this.getResizeToInner())
        {
          vImageNode.width = this.getInnerWidth();
          vImageNode.height = this.getInnerHeight();
        }
        else
        {
          vImageNode.width = this.getPreferredInnerWidth();
          vImageNode.height = this.getPreferredInnerHeight();
        }
      }
      catch(ex)
      {
        throw new Error( "postApplyDimensions failed " + ex );
      }
    },

    /*
    ---------------------------------------------------------------------------
      CHANGES IN DIMENSIONS
    ---------------------------------------------------------------------------
    */

    /**
     * Sets the width style attribute
     *
     * @type member
     * @param vNew {var} new inner width value
     * @param vOld {var} old inner width value
     * @return {void}
     * @signature function(vNew, vOld)
     */
    _changeInnerWidth : function(vNew) {
      if (this.getResizeToInner()) {
        this._image.width = vNew;
      }
    },


    /**
     * Sets the height style attribute
     *
     * @type member
     * @param vNew {var} new inner height value
     * @param vOld {var} old inner height value
     * @return {void}
     * @signature function(vNew, vOld)
     */
    _changeInnerHeight : function(vNew) {
      if (this.getResizeToInner()) {
        this._image.height = vNew;
      }
    }

  },





  /*
  *****************************************************************************
     DESTRUCTOR
  *****************************************************************************
  */

  destruct : function()
  {
    // Remove leaking filter attribute before leaving page
    if (this._image) {
      this._image.style.filter = "";
    }

    // Remove fields
    this._disposeFields("_image");
  }
});
