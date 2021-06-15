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

rwt.qx.Class.define("rwt.widgets.base.BoxLayout",
{
  extend : rwt.widgets.base.Parent,




  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  /**
   * @param orientation {String} Initial value for {@link #orientation}.
   */
  construct : function(orientation)
  {
    this.base(arguments);

    // apply orientation
    if (orientation != null) {
      this.setOrientation(orientation);
    } else {
      this.initOrientation();
    }
  },




  /*
  *****************************************************************************
     STATICS
  *****************************************************************************
  */

  statics : {
    STR_REVERSED : "-reversed"
  },




  /*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

  properties :
  {
    /** The orientation of the layout control. */
    orientation :
    {
      check : [ "horizontal", "vertical" ],
      init : "horizontal",
      apply : "_applyOrientation",
      event : "changeOrientation"
    },


    /** The spacing between childrens. Could be any positive integer value. */
    spacing :
    {
      check : "Integer",
      init : 0,
      themeable : true,
      apply : "_applySpacing",
      event : "changeSpacing"
    },


    /** The horizontal align of the children. Allowed values are: "left", "center" and "right" */
    horizontalChildrenAlign :
    {
      check : [ "left", "center", "right" ],
      init : "left",
      themeable : true,
      apply : "_applyHorizontalChildrenAlign"
    },


    /** The vertical align of the children. Allowed values are: "top", "middle" and "bottom" */
    verticalChildrenAlign :
    {
      check : [ "top", "middle", "bottom" ],
      init : "top",
      themeable : true,
      apply : "_applyVerticalChildrenAlign"
    },


    /** Should the children be layouted in reverse order? */
    reverseChildrenOrder :
    {
      check : "Boolean",
      init : false,
      apply : "_applyReverseChildrenOrder"
    },


    /**
     * Should the widgets be stretched to the available width (orientation==vertical) or height (orientation==horizontal)?
     *  This only applies if the child has not configured a own value for this axis.
     */
    stretchChildrenOrthogonalAxis :
    {
      check : "Boolean",
      init : true,
      apply : "_applyStretchChildrenOrthogonalAxis"
    },


    /**
     * If there are min/max values in combination with flex try to optimize placement.
     *  This is more complex and produces more time for the layouter but sometimes this feature is needed.
     */
    useAdvancedFlexAllocation :
    {
      check : "Boolean",
      init : false,
      apply : "_applyUseAdvancedFlexAllocation"
    },




    /*
    ---------------------------------------------------------------------------
      ACCUMULATED CHILDREN WIDTH/HEIGHT
    --------------------------------------------------------------------------------

      Needed for center/middle and right/bottom alignment

    ---------------------------------------------------------------------------
    */

    accumulatedChildrenOuterWidth :
    {
      _cached      : true,
      defaultValue : null
    },

    accumulatedChildrenOuterHeight :
    {
      _cached      : true,
      defaultValue : null
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
      INIT LAYOUT IMPL
    ---------------------------------------------------------------------------
    */

    /**
     * This creates an new instance of the layout impl this widget uses
     *
     * @type member
     * @return {rwt.widgets.base.BoxLayout} TODOC
     */
    _createLayoutImpl : function() {
      return this.getOrientation() == "vertical" ? new rwt.widgets.util.VerticalBoxLayoutImpl(this) : new rwt.widgets.util.HorizontalBoxLayoutImpl(this);
    },




    /*
    ---------------------------------------------------------------------------
      HELPERS
    ---------------------------------------------------------------------------
    */

    _layoutHorizontal : false,
    _layoutVertical : false,
    _layoutMode : "left",


    /**
     * TODOC
     *
     * @type member
     * @return {var} TODOC
     */
    isHorizontal : function() {
      return this._layoutHorizontal;
    },


    /**
     * TODOC
     *
     * @type member
     * @return {var} TODOC
     */
    isVertical : function() {
      return this._layoutVertical;
    },


    /**
     * TODOC
     *
     * @type member
     * @return {var} TODOC
     */
    getLayoutMode : function()
    {
      if (this._layoutMode == null) {
        this._updateLayoutMode();
      }

      return this._layoutMode;
    },


    /**
     * TODOC
     *
     * @type member
     * @return {void}
     */
    _updateLayoutMode : function()
    {
      this._layoutMode = this._layoutVertical ? this.getVerticalChildrenAlign() : this.getHorizontalChildrenAlign();

      if (this.getReverseChildrenOrder()) {
        this._layoutMode += rwt.widgets.base.BoxLayout.STR_REVERSED;
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @return {void}
     */
    _invalidateLayoutMode : function() {
      this._layoutMode = null;
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
     * @return {var} TODOC
     */
    _applyOrientation : function(value, old)
    {
      // update fast access variables
      this._layoutHorizontal = value == "horizontal";
      this._layoutVertical = value == "vertical";

      // Layout Implementation
      if (this._layoutImpl)
      {
        this._layoutImpl.dispose();
        this._layoutImpl = null;
      }

      if (value) {
        this._layoutImpl = this._createLayoutImpl();
      }

      // call layout helper
      this._doLayoutOrder(value, old);

      this.addToQueueRuntime("orientation");
    },

    _applySpacing : function()
    {
      this._doLayout();
      this.addToQueueRuntime("spacing");
    },

    _applyHorizontalChildrenAlign : function()
    {
      this._doLayoutOrder();
      this.addToQueueRuntime("horizontalChildrenAlign");
    },

    _applyVerticalChildrenAlign : function()
    {
      this._doLayoutOrder();
      this.addToQueueRuntime("verticalChildrenAlign");
    },

    _applyReverseChildrenOrder : function()
    {
      this._doLayoutOrder();
      this.addToQueueRuntime("reverseChildrenOrder");
    },

    _applyStretchChildrenOrthogonalAxis : function() {
      this.addToQueueRuntime("stretchChildrenOrthogonalAxis");
    },

    _applyUseAdvancedFlexAllocation : function() {
      this.addToQueueRuntime("useAdvancedFlexAllocation");
    },



    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     * @return {var} TODOC
     */
    _doLayoutOrder : function()
    {
      // update layout mode
      this._invalidateLayoutMode();

      // call doLayout
      this._doLayout();
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _doLayout : function()
    {
      // invalidate inner preferred dimensions
      this._invalidatePreferredInnerDimensions();

      // accumulated width needs to be invalidated
      this._invalidateAccumulatedChildrenOuterWidth();
      this._invalidateAccumulatedChildrenOuterHeight();
    },


    /**
     * TODOC
     *
     * @type member
     * @return {String} TODOC
     */
    _computeAccumulatedChildrenOuterWidth : function() {
      var children = this.getVisibleChildren();
      var spacing = this.getSpacing();
      var result = -spacing;
      var i = 0;
      var child = children[ i ];
      while( child != null ) {
        result += child.getOuterWidth() + spacing;
        i++;
        child = children[ i ];
      }
      return result;
    },


    /**
     * TODOC
     *
     * @type member
     * @return {String} TODOC
     */
    _computeAccumulatedChildrenOuterHeight : function()
    {
      var ch = this.getVisibleChildren(), chc, i = -1, sp = this.getSpacing(), s = -sp;

      chc = ch[++i];
      while( chc ) {
        s += chc.getOuterHeight() + sp;
        chc = ch[++i];
      }

      return s;
    },




    /*
    ---------------------------------------------------------------------------
      STRETCHING SUPPORT
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @return {void}
     */
    _recomputeChildrenStretchingX : function()
    {
      var ch = this.getVisibleChildren(), chc, i = -1;

      chc = ch[++i];
      while( chc ) {
        if (chc._recomputeStretchingX() && chc._recomputeBoxWidth()) {
          chc._recomputeOuterWidth();
        }
        chc = ch[++i];
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @return {void}
     */
    _recomputeChildrenStretchingY : function()
    {
      var ch = this.getVisibleChildren(), chc, i = -1;

      chc = ch[++i];
      while( chc ) {
        if (chc._recomputeStretchingY() && chc._recomputeBoxHeight()) {
          chc._recomputeOuterHeight();
        }
        chc = ch[++i];
      }
    }
  }
});
