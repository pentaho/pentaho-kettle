/*******************************************************************************
 * Copyright: 2004, 2015 1&1 Internet AG, Germany, http://www.1und1.de,
 *                       and EclipseSource
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for the Eclipse Remote Application Platform
 ******************************************************************************/

rwt.qx.Class.define("rwt.widgets.util.CanvasLayoutImpl",
{
  extend : rwt.widgets.util.LayoutImpl,




  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  construct : function(vWidget) {
    this.base(arguments, vWidget);
    this._mirror = false;
  },




  /*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */

  members :
  {

    setMirror : function( value ) {
      this._mirror = value;
      this.getWidget()._addChildrenToLayoutQueue( "left" );
      this.getWidget()._addChildrenToLayoutQueue( "right" );
    },

    /*
    ---------------------------------------------------------------------------
      [01] COMPUTE BOX DIMENSIONS FOR AN INDIVIDUAL CHILD
    ---------------------------------------------------------------------------
    */

    /** Compute and return the box width of the given child */
    /**
     * Global Structure:
     *  [01] COMPUTE BOX DIMENSIONS FOR AN INDIVIDUAL CHILD
     *  [02] COMPUTE NEEDED DIMENSIONS FOR AN INDIVIDUAL CHILD
     *  [03] COMPUTE NEEDED DIMENSIONS FOR ALL CHILDREN
     *  [04] UPDATE LAYOUT WHEN A CHILD CHANGES ITS OUTER DIMENSIONS
     *  [05] UPDATE CHILD ON INNER DIMENSION CHANGES OF LAYOUT
     *  [06] UPDATE LAYOUT ON JOB QUEUE FLUSH
     *  [07] UPDATE CHILDREN ON JOB QUEUE FLUSH
     *  [08] CHILDREN ADD/REMOVE/MOVE HANDLING
     *  [09] FLUSH LAYOUT QUEUES OF CHILDREN
     *  [10] LAYOUT CHILD
     *
     *  Inherits from rwt.widgets.util.LayoutImpl:
     *  [03] COMPUTE NEEDED DIMENSIONS FOR ALL CHILDREN
     *  [04] UPDATE LAYOUT WHEN A CHILD CHANGES ITS OUTER DIMENSIONS
     *  [06] UPDATE LAYOUT ON JOB QUEUE FLUSH
     *  [07] UPDATE CHILDREN ON JOB QUEUE FLUSH
     *  [08] CHILDREN ADD/REMOVE/MOVE HANDLING
     *  [09] FLUSH LAYOUT QUEUES OF CHILDREN
     *
     * @type member
     * @param vChild {var} TODOC
     * @return {var} TODOC
     */
    computeChildBoxWidth : function(vChild)
    {
      var vValue = null;

      if (vChild._computedLeftTypeNull || vChild._computedRightTypeNull) {
        vValue = vChild.getWidthValue();
      } else if (vChild._hasParent) {
        vValue = this.getWidget().getInnerWidth() - vChild.getLeftValue() - vChild.getRightValue();
      }

      return vValue !== null ? vValue : vChild._computeBoxWidthFallback();
    },


    /**
     * Compute and return the box height of the given child
     *
     * @type member
     * @param vChild {var} TODOC
     * @return {var} TODOC
     */
    computeChildBoxHeight : function(vChild)
    {
      var vValue = null;

      if (vChild._computedTopTypeNull || vChild._computedBottomTypeNull) {
        vValue = vChild.getHeightValue();
      } else if (vChild._hasParent) {
        vValue = this.getWidget().getInnerHeight() - vChild.getTopValue() - vChild.getBottomValue();
      }

      return vValue !== null ? vValue : vChild._computeBoxWidthFallback();
    },




    /*
    ---------------------------------------------------------------------------
      [02] COMPUTE NEEDED DIMENSIONS FOR AN INDIVIDUAL CHILD
    ---------------------------------------------------------------------------
    */

    /**
     * Compute and return the needed width of the given child
     *
     * @type member
     * @param vChild {var} TODOC
     * @return {var} TODOC
     */
    computeChildNeededWidth : function(vChild)
    {
      var vLeft = vChild._computedLeftTypePercent ? null : vChild.getLeftValue();
      var vRight = vChild._computedRightTypePercent ? null : vChild.getRightValue();
      var vMinBox = vChild._computedMinWidthTypePercent ? null : vChild.getMinWidthValue();
      var vMaxBox = vChild._computedMaxWidthTypePercent ? null : vChild.getMaxWidthValue();

      if (vLeft != null && vRight != null) {
        var vBox = vChild.getPreferredBoxWidth() || 0;
      } else {
        var vBox = (vChild._computedWidthTypePercent ? null : vChild.getWidthValue()) || vChild.getPreferredBoxWidth() || 0;
      }

      return rwt.util.Numbers.limit(vBox, vMinBox, vMaxBox) + vLeft + vRight + vChild.getMarginLeft() + vChild.getMarginRight();
    },


    /**
     * Compute and return the needed height of the given child
     *
     * @type member
     * @param vChild {var} TODOC
     * @return {var} TODOC
     */
    computeChildNeededHeight : function(vChild)
    {
      var vTop = vChild._computedTopTypePercent ? null : vChild.getTopValue();
      var vBottom = vChild._computedBottomTypePercent ? null : vChild.getBottomValue();
      var vMinBox = vChild._computedMinHeightTypePercent ? null : vChild.getMinHeightValue();
      var vMaxBox = vChild._computedMaxHeightTypePercent ? null : vChild.getMaxHeightValue();

      if (vTop != null && vBottom != null) {
        var vBox = vChild.getPreferredBoxHeight() || 0;
      } else {
        var vBox = (vChild._computedHeightTypePercent ? null : vChild.getHeightValue()) || vChild.getPreferredBoxHeight() || 0;
      }

      return rwt.util.Numbers.limit(vBox, vMinBox, vMaxBox) + vTop + vBottom + vChild.getMarginTop() + vChild.getMarginBottom();
    },




    /*
    ---------------------------------------------------------------------------
      [05] UPDATE CHILD ON INNER DIMENSION CHANGES OF LAYOUT
    ---------------------------------------------------------------------------
    */

    /**
     * Actions that should be done if the inner width of the widget was changed.
     *  Normally this includes update to percent values and ranges.
     *
     * @type member
     * @param vChild {var} TODOC
     * @return {var} TODOC
     */
    updateChildOnInnerWidthChange : function(vChild)
    {
      // this makes sure that both functions get executed before return
      var vUpdatePercent = vChild._recomputePercentX();
      var vUpdateRange = vChild._recomputeRangeX();

      return vUpdatePercent || vUpdateRange;
    },


    /**
     * Actions that should be done if the inner height of the widget was changed.
     *  Normally this includes update to percent values and ranges.
     *
     * @type member
     * @param vChild {var} TODOC
     * @return {var} TODOC
     */
    updateChildOnInnerHeightChange : function(vChild)
    {
      // this makes sure that both functions get executed before return
      var vUpdatePercent = vChild._recomputePercentY();
      var vUpdateRange = vChild._recomputeRangeY();

      return vUpdatePercent || vUpdateRange;
    },




    /*
    ---------------------------------------------------------------------------
      [10] LAYOUT CHILD
    ---------------------------------------------------------------------------
    */

    /**
     * This is called from rwt.widgets.base.Widget and  it's task is to apply the layout
     *  (excluding border and padding) to the child.
     *
     * @type member
     * @param vChild {var} TODOC
     * @param vJobs {var} TODOC
     * @return {void}
     */
    layoutChild : function(vChild, vJobs)
    {
      this.layoutChild_sizeX_essentialWrapper(vChild, vJobs);
      this.layoutChild_sizeY_essentialWrapper(vChild, vJobs);

      this.layoutChild_sizeLimitX(vChild, vJobs);
      this.layoutChild_sizeLimitY(vChild, vJobs);

      this.layoutChild_locationX(vChild, vJobs);
      this.layoutChild_locationY(vChild, vJobs);

      this.layoutChild_marginX(vChild, vJobs);
      this.layoutChild_marginY(vChild, vJobs);
    },


    /**
     * TODOC
     *
     * @type member
     * @param vChild {var} TODOC
     * @param vJobs {var} TODOC
     * @return {void}
     * @signature function(vChild, vJobs)
     */
    layoutChild_sizeX : rwt.util.Variant.select("qx.client",
    {
      "trident|webkit|blink" : function(vChild, vJobs)
      {
        if (vJobs.initial || vJobs.width || vJobs.minWidth || vJobs.maxWidth || vJobs.left || vJobs.right)
        {
          if (vChild._computedMinWidthTypeNull && vChild._computedWidthTypeNull && vChild._computedMaxWidthTypeNull && !(!vChild._computedLeftTypeNull && !vChild._computedRightTypeNull)) {
            vChild._resetRuntimeWidth();
          } else {
            vChild._renderRuntimeWidth(vChild.getBoxWidth());
          }
        }
      },

      "default" : function( vChild, vJobs ) {
        if( vJobs.initial || vJobs.width ) {
          if( vChild._computedWidthTypeNull ) {
            vChild._resetRuntimeWidth();
          } else  {
            vChild._renderRuntimeWidth( vChild.getWidthValue() );
          }
        }
      }
    }),


    /**
     * TODOC
     *
     * @type member
     * @param vChild {var} TODOC
     * @param vJobs {var} TODOC
     * @return {void}
     * @signature function(vChild, vJobs)
     */
    layoutChild_sizeY : rwt.util.Variant.select("qx.client",
    {
      "trident|webkit|blink" : function(vChild, vJobs)
      {
        if (vJobs.initial || vJobs.height || vJobs.minHeight || vJobs.maxHeight || vJobs.top || vJobs.bottom)
        {
          if (vChild._computedMinHeightTypeNull && vChild._computedHeightTypeNull && vChild._computedMaxHeightTypeNull && !(!vChild._computedTopTypeNull && !vChild._computedBottomTypeNull)) {
            vChild._resetRuntimeHeight();
          } else {
            vChild._renderRuntimeHeight(vChild.getBoxHeight());
          }
        }
      },

      "default" : function( vChild, vJobs ) {
        if( vJobs.initial || vJobs.height ) {
          if( vChild._computedHeightTypeNull ) {
            vChild._resetRuntimeHeight();
          } else  {
            vChild._renderRuntimeHeight( vChild.getHeightValue() );
          }
        }
      }
    }),


    /**
     * TODOC
     *
     * @type member
     * @param vChild {var} TODOC
     * @param vJobs {var} TODOC
     * @return {void}
     */
    layoutChild_locationX : function( vChild, vJobs ) {
      if( this._mirror ) {
        this.layoutChild_locationX_mirror( vChild, vJobs );
      } else {
        this.layoutChild_locationX_nomirror( vChild, vJobs );
      }
    },

    layoutChild_locationX_nomirror : function( vChild, vJobs ) {
      var vWidget = this.getWidget();
      if( vJobs.initial || vJobs.left || vJobs.parentPaddingLeft ) {
        if( vChild._computedLeftTypeNull ) {
          if( vChild._computedRightTypeNull && vWidget.getPaddingLeft() > 0 ) {
            vChild._renderRuntimeLeft( vWidget.getPaddingLeft() );
          } else {
            vChild._resetRuntimeLeft();
          }
        } else {
          vChild._renderRuntimeLeft( vChild.getLeftValue() + vWidget.getPaddingLeft() );
        }
      }
      if( vJobs.initial || vJobs.right || vJobs.parentPaddingRight ) {
        if( vChild._computedRightTypeNull ) {
          if( vChild._computedLeftTypeNull && vWidget.getPaddingRight() > 0 ) {
            vChild._renderRuntimeRight( vWidget.getPaddingRight() );
          } else {
            vChild._resetRuntimeRight();
          }
        } else {
          vChild._renderRuntimeRight( vChild.getRightValue() + vWidget.getPaddingRight() );
        }
      }
    },

    layoutChild_locationX_mirror : function( vChild, vJobs ) {
      var vWidget = this.getWidget();
      if( vJobs.initial || vJobs.left || vJobs.parentPaddingLeft ) {
        if( vChild._computedLeftTypeNull ) {
          if( vChild._computedRightTypeNull && vWidget.getPaddingLeft() > 0 ) {
            vChild._renderRuntimeRight( vWidget.getPaddingLeft() );
          } else {
            vChild._resetRuntimeRight();
          }
        } else {
          vChild._renderRuntimeRight( vChild.getLeftValue() + vWidget.getPaddingLeft() );
        }
      }
      if( vJobs.initial || vJobs.right || vJobs.parentPaddingRight ) {
        if( vChild._computedRightTypeNull ) {
          if( vChild._computedLeftTypeNull && vWidget.getPaddingRight() > 0 ) {
            vChild._renderRuntimeLeft( vWidget.getPaddingRight() );
          } else {
            vChild._resetRuntimeLeft();
          }
        } else {
          vChild._renderRuntimeLeft( vChild.getRightValue() + vWidget.getPaddingRight() );
        }
      }
    },

    /**
     * TODOC
     *
     * @type member
     * @param vChild {var} TODOC
     * @param vJobs {var} TODOC
     * @return {void}
     */
    layoutChild_locationY : function( vChild, vJobs ) {
      var vWidget = this.getWidget();
      if( vJobs.initial || vJobs.top || vJobs.parentPaddingTop ) {
        if( vChild._computedTopTypeNull ) {
          if( vChild._computedBottomTypeNull && vWidget.getPaddingTop() > 0 ) {
            vChild._renderRuntimeTop( vWidget.getPaddingTop() );
          } else {
            vChild._resetRuntimeTop();
          }
        } else {
          vChild._renderRuntimeTop( vChild.getTopValue() + vWidget.getPaddingTop() );
        }
      }
      if( vJobs.initial || vJobs.bottom || vJobs.parentPaddingBottom ) {
        if( vChild._computedBottomTypeNull ) {
          if( vChild._computedTopTypeNull && vWidget.getPaddingBottom() > 0 ) {
            vChild._renderRuntimeBottom( vWidget.getPaddingBottom() );
          } else {
            vChild._resetRuntimeBottom();
          }
        } else {
          vChild._renderRuntimeBottom( vChild.getBottomValue() + vWidget.getPaddingBottom() );
        }
      }
    }

  }
} );
