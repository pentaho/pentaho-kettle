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
 * Global timer support.
 *
 * This class can be used to periodically fire an event. This event can be
 * used to simulate e.g. a background task. The static method
 * {@link #once} is a special case. It will call a function deferred after a
 * given timeout.
 */
rwt.qx.Class.define("rwt.client.Timer",
{
  extend : rwt.qx.Target,




  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  /**
   * @param interval {Number} initial interval in milliseconds of the timer.
   */
  construct : function(interval)
  {
    this.base(arguments);

    this.setEnabled(false);

    if (interval != null) {
      this.setInterval(interval);
    }

    this.__oninterval = rwt.util.Functions.bind(this._oninterval, this);

    //Event instance to dispatch when interval fires
    this.__event = new rwt.event.Event("interval");

  },

  /*
  *****************************************************************************
     STATICS
  *****************************************************************************
  */

  statics :
  {
    /**
     * Start a function after a given timeout.
     *
     * @type static
     * @param func {Function} Function to call
     * @param obj {Object} context (this), the function is called with
     * @param timeout {Number} Number of milliseconds to wait before the function is called.
     */
    once : function(func, obj, timeout)
    {
      // Create time instance
      var timer = new rwt.client.Timer(timeout);

      // Add event listener to interval
      timer.addEventListener("interval", function(e)
      {
        timer.dispose();
        func.call(obj, e);

        obj = null;
      },
      obj);

      // Directly start timer
      timer.start();
    }
  },




  /*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

  properties :
  {
    /**
     * With the enabled property the Timer can be started and suspended.
     * Setting it to "true" is equivalent to {@link #start}, setting it
     * to "false" is equivalent to {@link #stop}.
     */
    enabled :
    {
      init : true,
      check : "Boolean",
      apply : "_applyEnabled"
    },

    /**
     * Time in milliseconds between two callback calls.
     * This property can be set to modify the interval of
     * a running timer.
     */
    interval :
    {
      check : "Integer",
      init : 1000,
      apply : "_applyInterval"
    }
  },




  /*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */

  members :
  {
    __intervalHandler : null,




    /*
    ---------------------------------------------------------------------------
      APPLY ROUTINES
    ---------------------------------------------------------------------------
    */

    /**
     * Apply the interval of the timer.
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyInterval : function()
    {
      if (this.getEnabled()) {
        this.restart();
      }
    },


    /**
     * Apply the enabled state of the timer.
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyEnabled : function(value, old)
    {
      if (old)
      {
        window.clearInterval(this.__intervalHandler);
        this.__intervalHandler = null;
      }
      else if (value)
      {
        this.__intervalHandler = window.setInterval(this.__oninterval, this.getInterval());
      }
    },




    /*
    ---------------------------------------------------------------------------
      USER-ACCESS
    ---------------------------------------------------------------------------
    */

    /**
     * Start the timer
     *
     * @type member
     */
    start : function() {
      this.setEnabled(true);
    },


    /**
     * Start the timer with a given interval
     *
     * @type member
     * @param interval {Integer} Time in milliseconds between two callback calls.
     */
    startWith : function(interval)
    {
      this.setInterval(interval);
      this.start();
    },


    /**
     * Stop the timer.
     *
     * @type member
     */
    stop : function() {
      this.setEnabled(false);
    },


    /**
     * Restart the timer.
     * This makes it possible to change the interval of a running timer.
     *
     * @type member
     */
    restart : function()
    {
      this.stop();
      this.start();
    },


    /**
     * Restart the timer. with a given interval.
     *
     * @type member
     * @param interval {Integer} Time in milliseconds between two callback calls.
     */
    restartWith : function(interval)
    {
      this.stop();
      this.startWith(interval);
    },




    /*
    ---------------------------------------------------------------------------
      EVENT-MAPPER
    ---------------------------------------------------------------------------
    */

    /**
     * timer callback
     *
     * @type member
     */
    _oninterval : function() {
      try {
        if (this.getEnabled() && this.hasEventListeners("interval")) {
          this.dispatchEvent(this.__event, false);
        }
      } catch( ex ) {
        rwt.runtime.ErrorHandler.processJavaScriptError( ex );
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
    if (this.__intervalHandler) {
      window.clearInterval(this.__intervalHandler);
    }

    this._disposeFields("__intervalHandler", "__oninterval", "__event");
  }
});
