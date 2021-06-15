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
 * The image preloader can be used to fill the browser cache with images,
 * which are needed later. Once all images are pre loaded a "complete"
 * event is fired.
 */
rwt.qx.Class.define("rwt.html.ImagePreloaderSystem",
{
  extend : rwt.qx.Target,



  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  /**
   * If the callback is provided the preloading starts automatically and the callback
   * is called on completion of the pre loading. Otherwhise the pre loading has to be
   * started manually using {@link #start}.
   *
   * @param vPreloadList {String[]} list of image URLs to preload
   * @param vCallBack {Function} callback function. This function gets called after the
   *    preloading is completed
   * @param vCallBackScope {Object?window} scope for the callback function
   */
  construct : function(vPreloadList, vCallBack, vCallBackScope)
  {
    this.base(arguments);

    // internally use a map for the image sources
    if( vPreloadList instanceof Array ) {
      this._list = rwt.util.Objects.fromArray( vPreloadList );
    } else {
      this._list = vPreloadList;
    }

    // Create timer
    this._timer = new rwt.client.Timer( 3000 );
    this._timer.addEventListener("interval", this.__oninterval, this);

    // If we use the compact syntax, automatically add an event listeners and start the loading process
    if (vCallBack) {
      this.addEventListener("completed", vCallBack, vCallBackScope || null);
    }
  },



  /*
  *****************************************************************************
     EVENTS
  *****************************************************************************
  */

  events:
  {
    /** Fired after the pre loading of the images is complete */
    "completed" : "rwt.event.Event"
  },




  /*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */

  members :
  {
    _stopped : false,




    /*
    ---------------------------------------------------------------------------
      USER ACCESS
    ---------------------------------------------------------------------------
    */

    /**
     * Start the preloading
     *
     * @type member
     * @return {void}
     */
    start : function()
    {
      if (rwt.util.Objects.isEmpty(this._list))
      {
        this.createDispatchEvent("completed");
        return;
      }

      for (var vSource in this._list)
      {
        var vPreloader = rwt.html.ImagePreloaderManager.getInstance().create( vSource );

        if (vPreloader.isErroneous() || vPreloader.isLoaded()) {
          delete this._list[vSource];
        }
        else
        {
          vPreloader._origSource = vSource;

          vPreloader.addEventListener("load", this.__onload, this);
          vPreloader.addEventListener("error", this.__onerror, this);
        }
      }

      // Initial check
      this._check();
    },




    /*
    ---------------------------------------------------------------------------
      EVENT LISTENERS
    ---------------------------------------------------------------------------
    */

    /**
     * Load event handler
     *
     * @type member
     * @param e {Event} Event object
     */
    __onload : function(e)
    {
      if (this.getDisposed()) {
        return;
      }

      delete this._list[e.getTarget()._origSource];
      this._check();
    },


    /**
     * Error handler
     *
     * @type member
     * @param e {Event} Event object
     */
    __onerror : function(e)
    {
      if (this.getDisposed()) {
        return;
      }

      delete this._list[e.getTarget()._origSource];
      this._check();
    },


    /**
     * Timer interval handler
     *
     * @type member
     * @param e {Event} Event object
     */
    __oninterval : function()
    {
      this._stopped = true;
      this._timer.stop();

      this.createDispatchEvent("completed");
    },




    /*
    ---------------------------------------------------------------------------
      CHECK
    ---------------------------------------------------------------------------
    */

    /**
     * Checks whether the pre loading is complete and dispatches the "complete" event.
     *
     * @type member
     */
    _check : function()
    {
      if (this._stopped) {
        return;
      }

      if (rwt.util.Objects.isEmpty(this._list))
      {
        this._timer.stop();
        this.createDispatchEvent("completed");
      }
      else
      {
        // Restart timer for timeout
        this._timer.restart();
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
    if (this._timer)
    {
      this._timer.removeEventListener("interval", this.__oninterval, this);
      this._disposeObjects("_timer");
    }

    this._disposeFields("_list");
  }
});
