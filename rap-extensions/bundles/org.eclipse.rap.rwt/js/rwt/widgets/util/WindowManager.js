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

/** This singleton manages rwt.widgets.base.Windows */
rwt.qx.Class.define("rwt.widgets.util.WindowManager",
{
  extend : rwt.util.ObjectManager,





  /*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

  properties :
  {
    /** This property holds the current active window */
    activeWindow :
    {
      check : "Object",
      nullable : true,
      apply : "_applyActiveWindow"
    }
  },




  /*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */

  members :
  {

    blockActiveState : false,

    /*
    ---------------------------------------------------------------------------
      APPLY ROUTINES
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyActiveWindow : function(value, old)
    {
      rwt.widgets.util.PopupManager.getInstance().update();

      if (old) {
        old.setActive(false);
      }

      if (value) {
        value.setActive(true);
      }

      if (old && old.getModal()) {
        old.getTopLevelWidget().release(old);
      }

      if (value && value.getModal()) {
        value.getTopLevelWidget().block(value);
      }
    },




    /*
    ---------------------------------------------------------------------------
      UTILITIES
    ---------------------------------------------------------------------------
    */

    /**
     * Updates all registered window instances
     *
     * @type member
     * @return {void}
     */
    update : function()
    {
      var vWindow;
      var vAll = this.getAll();

      for (var vHashCode in vAll)
      {
        vWindow = vAll[vHashCode];

        if (!vWindow.getAutoHide()) {
          continue;
        }

        vWindow.hide();
      }
    },




    /*
    ---------------------------------------------------------------------------
      MANAGER INTERFACE
    ---------------------------------------------------------------------------
    */

    /**
     * Compares two windows (used as sort method in {@link #remove}).
     * Sorts the windows by checking which of the given windows is active.
     * If none of those two are active the zIndex are subtracted from each
     * other to determine the sort order.
     *
     * @type member
     * @param w1 {rwt.widgets.base.Window} first window to compare
     * @param w2 {rwt.widgets.base.Window} second window to compare
     * @return {int | var} 1 for first window active, -1 for second window active
     * and the subtraction of the zIndex if none of the two are active.
     */
    compareWindows : function(w1, w2)
    {
      switch(w1.getWindowManager().getActiveWindow())
      {
        case w1:
          return 1;

        case w2:
          return -1;
      }

      return w1.getZIndex() - w2.getZIndex();
    },


    /**
     * Adds a {@link rwt.widgets.base.Window} instance to the manager and
     * sets it as active window.
     *
     * @type member
     * @param vWindow {rwt.widgets.base.Window} window instance to add
     * @return {void}
     */
    add : function(vWindow)
    {
      this.base(arguments, vWindow);

      this.setActiveWindow(vWindow);
    },


    /**
     * Removes a {@link rwt.widgets.base.Window} instance from the manager.
     * If the current active window is the one which should be removed the
     * existing windows are compared to determine the new active window
     * (using the {@link #compareWindows} method).
     *
     * @type member
     * @param vWindow {rwt.widgets.base.Window} window instance
     * @return {void}
     */
    remove : function( vWindow ) {
      this.base( arguments, vWindow );
      if( this.getActiveWindow() == vWindow ) {
        var a = [];
        for( var i in this._objects ) {
          a.push( this._objects[ i ] );
        }
        var l = a.length;
        if( l === 0 ) {
          this.setActiveWindow( null );
        } else if ( l === 1 ) {
          this.setActiveWindow( a[ 0 ] );
        } else if ( l > 1 ) {
          a.sort( this.compareWindows );
          this.setActiveWindow( a[ l - 1 ] );
        }
      }
    }

  }
});
