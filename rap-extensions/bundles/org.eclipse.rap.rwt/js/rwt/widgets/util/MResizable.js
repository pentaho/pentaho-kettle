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
 * Provides resizing behavior to any widget.
 */
rwt.qx.Mixin.define("rwt.widgets.util.MResizable",
{
  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  construct : function()
  {
    this._frame = new rwt.widgets.base.Terminator();
    this._frame.setAppearance("resizer-frame");
    this.addEventListener("mousedown", this._onmousedown);
    this.addEventListener("mouseup", this._onmouseup);
    this.addEventListener("mousemove", this._onmousemove);
  },





  /*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

  properties :
  {
    /** It is resizable in the left direction. */
    resizableWest :
    {
      check : "Boolean",
      init : true,
      apply : "_applyResizable"
    },


    /** It is resizable in the top direction. */
    resizableNorth :
    {
      check : "Boolean",
      init : true,
      apply : "_applyResizable"
    },


    /** It is resizable in the right direction. */
    resizableEast :
    {
      check : "Boolean",
      init : true,
      apply : "_applyResizable"
    },


    /** It is resizable in the bottom direction. */
    resizableSouth :
    {
      check : "Boolean",
      init : true,
      apply : "_applyResizable"
    },


    /** If the window is resizable */
    resizable :
    {
      group : [ "resizableNorth", "resizableEast", "resizableSouth", "resizableWest" ],
      mode  : "shorthand"
    },


    /** The resize method to use */
    resizeMethod :
    {
      init : "frame",
      check : [ "opaque", "lazyopaque", "frame", "translucent" ],
      event : "changeResizeMethod"
    }
  },






  /*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */

  members :
  {
    /**
     * Adjust so that it returns a boolean instead of an array.
     *
     * @type member
     * @return {Boolean} TODOC
     */
    isResizable : function() {
      return this.getResizableWest() || this.getResizableEast() || this.getResizableNorth() || this.getResizableSouth();
    },


    /**
     * Adjust so that it returns a boolean instead of an array.
     * Wrapper around isResizable. Please use isResizable instead.
     *
     * @type member
     * @return {Boolean} TODOC
     */
    getResizable : function() {
      return this.isResizable();
    },



    _applyResizable : function() {
      // placeholder
    },


    /**
     * TODOC
     *
     * @type member
     * @param e {Event} TODOC
     * @return {void}
     */
    _onmousedown : function(e)
    {
      if (this._resizeNorth || this._resizeSouth || this._resizeWest || this._resizeEast)
      {
        // enable capturing
        this.setCapture(true);

        // activate global cursor
        this.getTopLevelWidget().setGlobalCursor(this.getCursor());

        // caching element
        var el = this.getElement();

        // measuring and caching of values for resize session
        var pa = this._getResizeParent();
        var pl = pa.getElement();

        // compute locations
        var paLoc = rwt.html.Location.get(pl, "scroll");
        var elLoc = rwt.html.Location.get(el);

        // handle frame and translucently
        switch(this.getResizeMethod())
        {
          case "translucent":
            this.setOpacity(0.5);
            break;

          case "frame":
            var f = this._frame;

            if (f.getParent() != pa)
            {
              f.setParent(pa);
              rwt.widgets.base.Widget.flushGlobalQueues();
            }

            f._renderRuntimeLeft(elLoc.left - paLoc.left);
            f._renderRuntimeTop(elLoc.top - paLoc.top);

            f._renderRuntimeWidth(el.offsetWidth);
            f._renderRuntimeHeight(el.offsetHeight);

            f.setZIndex(this.getZIndex() + 1);

            break;
        }

        // create resize session
        var s = this._resizeSession = {};
        var minRef = this._getMinSizeReference();

        if (this._resizeWest)
        {
          s.boxWidth = el.offsetWidth;
          s.boxRight = elLoc.right;
        }

        if (this._resizeWest || this._resizeEast)
        {
          s.boxLeft = elLoc.left;

          s.parentContentLeft = paLoc.left;
          s.parentContentRight = paLoc.right;

          s.minWidth = minRef.getMinWidthValue();
          s.maxWidth = minRef.getMaxWidthValue();
        }

        if (this._resizeNorth)
        {
          s.boxHeight = el.offsetHeight;
          s.boxBottom = elLoc.bottom;
        }

        if (this._resizeNorth || this._resizeSouth)
        {
          s.boxTop = elLoc.top;

          s.parentContentTop = paLoc.top;
          s.parentContentBottom = paLoc.bottom;

          s.minHeight = minRef.getMinHeightValue();
          s.maxHeight = minRef.getMaxHeightValue();
        }
      }
      else
      {
        // cleanup resize session
        delete this._resizeSession;
      }

      // stop event
      e.stopPropagation();
    },


    /**
     * TODOC
     *
     * @type member
     * @param e {Event} TODOC
     * @return {void}
     */
    _onmouseup : function(e)
    {
      var s = this._resizeSession;

      if (s)
      {
        // disable capturing
        this.setCapture(false);

        // deactivate global cursor
        this.getTopLevelWidget().setGlobalCursor(null);

        // sync sizes to frame
        if(    this.getResizeMethod() === "lazyopaque"
            || ( this.getResizeMethod() === "frame" && this._frame && this._frame.getParent() )
         ) {
          if (s.lastLeft != null) {
            this.setLeft(s.lastLeft);
          }

          if (s.lastTop != null) {
            this.setTop(s.lastTop);
          }

          if (s.lastWidth != null)
          {
            this._changeWidth(s.lastWidth);
          }

          if (s.lastHeight != null)
          {
            this._changeHeight(s.lastHeight);
          }

          if (this.getResizeMethod() == "frame") {
            this._frame.setParent(null);
          }
        } else if( this.getResizeMethod() === "translucent" ) {
          this.setOpacity(null);
        }
        delete this._resizeSession;
      }

      // stop event
      e.stopPropagation();
    },


    /**
     * TODOC
     *
     * @type member
     * @param p {var} TODOC
     * @param e {Event} TODOC
     * @return {var} TODOC
     */
    _near : function(p, e) {
      return e > (p - 5) && e < (p + 5);
    },


    /**
     * TODOC
     *
     * @type member
     * @param e {Event} TODOC
     * @return {void}
     */
    _onmousemove : function(e)
    {
      if (this._disableResize) {
        return;
      }

      var s = this._resizeSession;

      if (s)
      {
        if (this._resizeWest)
        {
          s.lastWidth = rwt.util.Numbers.limit(s.boxWidth + s.boxLeft - Math.max(e.getPageX(), s.parentContentLeft), s.minWidth, s.maxWidth);
          s.lastLeft = s.boxRight - s.lastWidth - s.parentContentLeft;
        }
        else if (this._resizeEast)
        {
          s.lastWidth = rwt.util.Numbers.limit(Math.min(e.getPageX(), s.parentContentRight) - s.boxLeft, s.minWidth, s.maxWidth);
        }

        if (this._resizeNorth)
        {
          s.lastHeight = rwt.util.Numbers.limit(s.boxHeight + s.boxTop - Math.max(e.getPageY(), s.parentContentTop), s.minHeight, s.maxHeight);
          s.lastTop = s.boxBottom - s.lastHeight - s.parentContentTop;
        }
        else if (this._resizeSouth)
        {
          s.lastHeight = rwt.util.Numbers.limit(Math.min(e.getPageY(), s.parentContentBottom) - s.boxTop, s.minHeight, s.maxHeight);
        }

        switch(this.getResizeMethod())
        {
          case "opaque":
          case "translucent":
            if (this._resizeWest || this._resizeEast)
            {
              this.setWidth(s.lastWidth);

              if (this._resizeWest) {
                this.setLeft(s.lastLeft);
              }
            }

            if (this._resizeNorth || this._resizeSouth)
            {
              this.setHeight(s.lastHeight);

              if (this._resizeNorth) {
                this.setTop(s.lastTop);
              }
            }

            break;

          default:
            var o = this.getResizeMethod() == "frame" ? this._frame : this;

            if (this._resizeWest || this._resizeEast)
            {
              o._renderRuntimeWidth(s.lastWidth);

              if (this._resizeWest) {
                o._renderRuntimeLeft(s.lastLeft);
              }
            }

            if (this._resizeNorth || this._resizeSouth)
            {
              o._renderRuntimeHeight(s.lastHeight);

              if (this._resizeNorth) {
                o._renderRuntimeTop(s.lastTop);
              }
            }
        }
      }
      else
      {
        var resizeMode = "";
        var el = this.getElement();

        this._resizeNorth = this._resizeSouth = this._resizeWest = this._resizeEast = false;

        var elLoc = rwt.html.Location.get(el);

        if (this._near(elLoc.top, e.getPageY()))
        {
          if (this.getResizableNorth())
          {
            resizeMode = "n";
            this._resizeNorth = true;
          }
        }
        else if (this._near(elLoc.bottom, e.getPageY()))
        {
          if (this.getResizableSouth())
          {
            resizeMode = "s";
            this._resizeSouth = true;
          }
        }

        if (this._near(elLoc.left, e.getPageX()))
        {
          if (this.getResizableWest())
          {
            resizeMode += "w";
            this._resizeWest = true;
          }
        }
        else if (this._near(elLoc.right, e.getPageX()))
        {
          if (this.getResizableEast())
          {
            resizeMode += "e";
            this._resizeEast = true;
          }
        }

        if (this._resizeNorth || this._resizeSouth || this._resizeWest || this._resizeEast) {
          this.setCursor(resizeMode + "-resize");
        } else {
          this.resetCursor();
        }
      }

      // stop event
      e.stopPropagation();
    }
  },





  /*
  *****************************************************************************
     DESTRUCTOR
  *****************************************************************************
  */

  destruct : function() {
    this._disposeObjects("_frame");
  }
});
