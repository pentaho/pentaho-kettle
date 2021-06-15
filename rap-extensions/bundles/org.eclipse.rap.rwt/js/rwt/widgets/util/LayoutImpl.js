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
 * Abstact base class of all layout implementations
 *
 * @param vWidget {rwt.widgets.base.Parent} reference to the associated widget
 */
rwt.qx.Class.define("rwt.widgets.util.LayoutImpl",
{
  extend : rwt.qx.Object,




  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  construct : function(vWidget)
  {
    this.base(arguments);

    this._widget = vWidget;
  },




  /*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */

  members :
  {
    /**
     * Returns the associated widget
     *
     * @type member
     * @return {rwt.widgets.base.Parent} reference to the associated widget
     */
    getWidget : function() {
      return this._widget;
    },

    /*
      Global Structure:
      [01] COMPUTE BOX DIMENSIONS FOR AN INDIVIDUAL CHILD
      [02] COMPUTE NEEDED DIMENSIONS FOR AN INDIVIDUAL CHILD
      [03] COMPUTE NEEDED DIMENSIONS FOR ALL CHILDREN
      [04] UPDATE LAYOUT WHEN A CHILD CHANGES ITS OUTER DIMENSIONS
      [05] UPDATE CHILD ON INNER DIMENSION CHANGES OF LAYOUT
      [06] UPDATE LAYOUT ON JOB QUEUE FLUSH
      [07] UPDATE CHILDREN ON JOB QUEUE FLUSH
      [08] CHILDREN ADD/REMOVE/MOVE HANDLING
      [09] FLUSH LAYOUT QUEUES OF CHILDREN
      [10] LAYOUT CHILD
    */

    /*
    ---------------------------------------------------------------------------
      [01] COMPUTE BOX DIMENSIONS FOR AN INDIVIDUAL CHILD
    ---------------------------------------------------------------------------
    */

    /**
     * Compute and return the box width of the given child
     *
     * @type member
     * @param vChild {rwt.widgets.base.Widget} TODOC
     * @return {Integer} box width of the given child
     */
    computeChildBoxWidth : function(vChild) {
      return vChild.getWidthValue() || vChild._computeBoxWidthFallback();
    },


    /**
     * Compute and return the box height of the given child
     *
     * @type member
     * @param vChild {rwt.widgets.base.Widget} TODOC
     * @return {Integer} box height of the given child
     */
    computeChildBoxHeight : function(vChild) {
      return vChild.getHeightValue() || vChild._computeBoxHeightFallback();
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
     * @param vChild {rwt.widgets.base.Widget} TODOC
     * @return {Integer} needed width
     */
    computeChildNeededWidth : function(vChild)
    {
      // omit ultra long lines, these two variables only needed once
      // here, but this enhance the readability of the code :)
      var vMinBox = vChild._computedMinWidthTypePercent ? null : vChild.getMinWidthValue();
      var vMaxBox = vChild._computedMaxWidthTypePercent ? null : vChild.getMaxWidthValue();

      var vBox = (vChild._computedWidthTypePercent || vChild._computedWidthTypeFlex ? null : vChild.getWidthValue()) || vChild.getPreferredBoxWidth() || 0;

      return rwt.util.Numbers.limit(vBox, vMinBox, vMaxBox) + vChild.getMarginLeft() + vChild.getMarginRight();
    },


    /**
     * Compute and return the needed height of the given child
     *
     * @type member
     * @param vChild {rwt.widgets.base.Widget} TODOC
     * @return {Integer} needed height
     */
    computeChildNeededHeight : function(vChild)
    {
      // omit ultra long lines, these two variables only needed once
      // here, but this enhance the readability of the code :)
      var vMinBox = vChild._computedMinHeightTypePercent ? null : vChild.getMinHeightValue();
      var vMaxBox = vChild._computedMaxHeightTypePercent ? null : vChild.getMaxHeightValue();

      var vBox = (vChild._computedHeightTypePercent || vChild._computedHeightTypeFlex ? null : vChild.getHeightValue()) || vChild.getPreferredBoxHeight() || 0;

      return rwt.util.Numbers.limit(vBox, vMinBox, vMaxBox) + vChild.getMarginTop() + vChild.getMarginBottom();
    },




    /*
    ---------------------------------------------------------------------------
      [03] COMPUTE NEEDED DIMENSIONS FOR ALL CHILDREN
    ---------------------------------------------------------------------------
    */

    /**
     * Calculate the maximum needed width of all children
     *
     * @type member
     * @return {Integer} maximum needed width of all children
     */
    computeChildrenNeededWidth_max : function()
    {
      for (var i=0, ch=this.getWidget().getVisibleChildren(), chl=ch.length, maxv=0; i<chl; i++) {
        maxv = Math.max(maxv, ch[i].getNeededWidth());
      }

      return maxv;
    },


    /**
     * Calculate the maximum needed height of all children
     *
     * @type member
     * @return {Integer} maximum needed height of all children
     */
    computeChildrenNeededHeight_max : function()
    {
      for (var i=0, ch=this.getWidget().getVisibleChildren(), chl=ch.length, maxv=0; i<chl; i++) {
        maxv = Math.max(maxv, ch[i].getNeededHeight());
      }

      return maxv;
    },


    /**
     * Compute and return the width needed by all children of this widget
     *
     * @type member
     * @return {Integer} TODOC
     */
    computeChildrenNeededWidth_sum : function()
    {
      for (var i=0, ch=this.getWidget().getVisibleChildren(), chl=ch.length, sumv=0; i<chl; i++) {
        sumv += ch[i].getNeededWidth();
      }

      return sumv;
    },


    /**
     * Compute and return the height needed by all children of this widget
     *
     * @type member
     * @return {Integer} height needed by all children of this widget
     */
    computeChildrenNeededHeight_sum : function()
    {
      for (var i=0, ch=this.getWidget().getVisibleChildren(), chl=ch.length, sumv=0; i<chl; i++) {
        sumv += ch[i].getNeededHeight();
      }

      return sumv;
    },


    /**
     * Compute and return the width needed by all children of this widget
     *
     * @return {Integer} width needed by all children of this widget
     */
    computeChildrenNeededWidth : null,  // alias set in defer


    /**
     * Compute and return the height needed by all children of this widget
     *
     * @return {Integer} height needed by all children of this widget
     */
    computeChildrenNeededHeight : null,  // alias set in defer




    /*
    ---------------------------------------------------------------------------
      [04] UPDATE LAYOUT WHEN A CHILD CHANGES ITS OUTER DIMENSIONS
    ---------------------------------------------------------------------------
    */

    /**
     * Things to do and layout when any of the childs changes its outer width.
     * Needed by layouts where the children depend on each other, like flow or box layouts.
     *
     * Subclasses might implement this method
     *
     * @type member
     * @param vChild {rwt.widgets.base.Widget} changed child widget
     * @return {void}
     */
    updateSelfOnChildOuterWidthChange : function() {},


    /**
     * Things to do and layout when any of the childs changes its outer height.
     * Needed by layouts where the children depend on each other, like flow or box layouts.
     *
     * Subclasses might implement this method
     *
     * @type member
     * @param vChild {rwt.widgets.base.Widget} changed child widget
     * @return {void}
     */
    updateSelfOnChildOuterHeightChange : function() {},




    /*
    ---------------------------------------------------------------------------
      [05] UPDATE CHILD ON INNER DIMENSION CHANGES OF LAYOUT
    ---------------------------------------------------------------------------
    */

    /**
     * Actions that should be done if the inner width of the layout widget has changed.
     * Normally this includes updates to percent values and ranges.
     *
     * Subclasses might implement this method
     *
     * @type member
     * @param vChild {rwt.widgets.base.Widget} changed child widget
     * @return {boolean}
     */
    updateChildOnInnerWidthChange : function() {},


    /**
     * Actions that should be done if the inner height of the layout widget has changed.
     * Normally this includes updates to percent values and ranges.
     *
     * Subclasses might implement this method
     *
     * @type member
     * @param vChild {rwt.widgets.base.Widget} changed child widget
     * @return {void}
     */
    updateChildOnInnerHeightChange : function() {},




    /*
    ---------------------------------------------------------------------------
      [06] UPDATE LAYOUT ON JOB QUEUE FLUSH
    ---------------------------------------------------------------------------
    */

    /**
     * Invalidate and recompute cached data according to job queue.
     * This is executed at the beginning of the job queue handling.
     *
     * Subclasses might implement this method
     *
     * @type member
     * @param vJobQueue {Object} TODOC
     * @return {void}
     */
    updateSelfOnJobQueueFlush : function() {},




    /*
    ---------------------------------------------------------------------------
      [07] UPDATE CHILDREN ON JOB QUEUE FLUSH
    ---------------------------------------------------------------------------
    */

    /**
     * Updates children on job queue flush.
     * This is executed at the end of the job queue handling.
     *
     * Subclasses might implement this method
     *
     * @type member
     * @param vJobQueue {Object} TODOC
     * @return {boolean}
     */
    updateChildrenOnJobQueueFlush : function() {},




    /*
    ---------------------------------------------------------------------------
      [08] CHILDREN ADD/REMOVE/MOVE HANDLING
    ---------------------------------------------------------------------------
    */

    /**
     * Add child to current layout. Rarely needed by some layout implementations.
     *
     * Subclasses might implement this method
     *
     * @type member
     * @param vChild {rwt.widgets.base.Widget} newly added child
     * @param vIndex {Integer} index of the child
     * @return {void}
     */
    updateChildrenOnAddChild : function() {},


    /**
     * Remove child from current layout.
     *  Needed by layouts where the children depend on each other, like flow or box layouts.
     *
     *  Subclasses might implement this method
     *
     * @type member
     * @param vChild {rwt.widgets.base.Widget} newly added child
     * @param vIndex {Integer} index of the child
     * @return {void}
     */
    updateChildrenOnRemoveChild : function() {},


    /**
     * Move child within its parent to a new position.
     *  Needed by layouts where the children depend on each other, like flow or box layouts.
     *
     * Subclasses might implement this method
     *
     * @type member
     * @param vChild {rwt.widgets.base.Widget} newly added child
     * @param vIndex {Integer} new index of the child
     * @param vOldIndex {Integer} old index of the child
     * @return {void}
     */
    updateChildrenOnMoveChild : function() {},




    /*
    ---------------------------------------------------------------------------
      [09] FLUSH LAYOUT QUEUES OF CHILDREN
    ---------------------------------------------------------------------------
    */

    /**
     * Has full control of the order in which the registered
     * (or non-registered) children should be layouted.
     *
     * @type member
     * @param vChildrenQueue {Object} TODOC
     * @return {void}
     */
    flushChildrenQueue : function(vChildrenQueue)
    {
      var vWidget = this.getWidget();

      for (var vHashCode in vChildrenQueue) {
        vWidget._layoutChild(vChildrenQueue[vHashCode]);
      }
    },




    /*
    ---------------------------------------------------------------------------
      [10] LAYOUT CHILD
    ---------------------------------------------------------------------------
    */

    /**
     * Called from rwt.widgets.base.Parent. Its task is to apply the layout
     * (excluding border and padding) to the child.
     *
     * @type member
     * @param vChild {rwt.widgets.base.Widget} child to layout
     * @param vJobs {Set} layout changes to perform
     * @return {void}
     */
    layoutChild : function() {},


    /**
     * Apply min-/max-width to the child. Direct usage of stylesheet properties.
     * This is only possible in modern capable clients (i.e. excluding all current
     *  versions of Internet Explorer)
     *
     * @type member
     * @param vChild {rwt.widgets.base.Widget} child to layout
     * @param vJobs {Set} layout changes to perform
     * @return {void}
     * @signature function(vChild, vJobs)
     */
    layoutChild_sizeLimitX : function( vChild, vJobs ) {
      if( vJobs.minWidth ) {
        if( vChild._computedMinWidthTypeNull ) {
          vChild._resetRuntimeMinWidth();
        } else {
          vChild._renderRuntimeMinWidth( vChild.getMinWidthValue() );
        }
      } else if( vJobs.initial && !vChild._computedMinWidthTypeNull ) {
        vChild._renderRuntimeMinWidth( vChild.getMinWidthValue() );
      }
      if( vJobs.maxWidth ) {
        if( vChild._computedMaxWidthTypeNull ) {
          vChild._resetRuntimeMaxWidth();
        } else {
          vChild._renderRuntimeMaxWidth( vChild.getMaxWidthValue() );
        }
      } else if( vJobs.initial && !vChild._computedMaxWidthTypeNull ) {
        vChild._renderRuntimeMaxWidth( vChild.getMaxWidthValue() );
      }
    },


    /**
     * Apply min-/max-height to the child. Direct usage of stylesheet properties.
     * This is only possible in modern capable clients (i.e. excluding all current
     *  versions of Internet Explorer)
     *
     * @type member
     * @param vChild {rwt.widgets.base.Widget} child to layout
     * @param vJobs {Set} layout changes to perform
     * @return {void}
     * @signature function(vChild, vJobs)
     */
    layoutChild_sizeLimitY : function( vChild, vJobs ) {
      if( vJobs.minHeight ) {
        if( vChild._computedMinHeightTypeNull ) {
          vChild._resetRuntimeMinHeight();
        } else {
          vChild._renderRuntimeMinHeight( vChild.getMinHeightValue() );
        }
      } else if( vJobs.initial && !vChild._computedMinHeightTypeNull ) {
        vChild._renderRuntimeMinHeight( vChild.getMinHeightValue() );
      }
      if( vJobs.maxHeight ) {
        if( vChild._computedMaxHeightTypeNull ) {
          vChild._resetRuntimeMaxHeight();
        } else {
          vChild._renderRuntimeMaxHeight( vChild.getMaxHeightValue() );
        }
      } else if( vJobs.initial && !vChild._computedMaxHeightTypeNull ) {
        vChild._renderRuntimeMaxHeight( vChild.getMaxHeightValue() );
      }
    },

    /**
     * Apply the X margin values as pure stylesheet equivalent.
     *
     * @type member
     * @param vChild {rwt.widgets.base.Widget} child to layout
     * @param vJobs {Set} layout changes to perform
     * @return {void}
     */
    layoutChild_marginX : function( vChild, vJobs ) {
      if( vJobs.marginLeft || vJobs.initial ) {
        var vValueLeft = vChild.getMarginLeft();
        if( vValueLeft != null ) {
          vChild._renderRuntimeMarginLeft( vValueLeft );
        } else {
          vChild._resetRuntimeMarginLeft();
        }
      }
      if( vJobs.marginRight || vJobs.initial ) {
        var vValueRight = vChild.getMarginRight();
        if( vValueRight != null ) {
          vChild._renderRuntimeMarginRight( vValueRight );
        } else {
          vChild._resetRuntimeMarginRight();
        }
      }
    },


    /**
     * Apply the Y margin values as pure stylesheet equivalent.
     *
     * @type member
     * @param vChild {rwt.widgets.base.Widget} child to layout
     * @param vJobs {Set} layout changes to perform
     * @return {void}
     */
    layoutChild_marginY : function( vChild, vJobs ) {
      if( vJobs.marginTop || vJobs.initial ) {
        var vValueTop = vChild.getMarginTop();
        if( vValueTop != null ) {
          vChild._renderRuntimeMarginTop( vValueTop );
        } else {
          vChild._resetRuntimeMarginTop();
        }
      }
      if( vJobs.marginBottom || vJobs.initial ) {
        var vValueBottom = vChild.getMarginBottom();
        if( vValueBottom != null ) {
          vChild._renderRuntimeMarginBottom( vValueBottom );
        } else {
          vChild._resetRuntimeMarginBottom();
        }
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @param vChild {var} TODOC
     * @param vJobs {var} TODOC
     * @return {var} TODOC
     */
    layoutChild_sizeX_essentialWrapper : function(vChild, vJobs) {
      return vChild._isWidthEssential() ? this.layoutChild_sizeX(vChild, vJobs) : vChild._resetRuntimeWidth();
    },


    /**
     * TODOC
     *
     * @type member
     * @param vChild {var} TODOC
     * @param vJobs {var} TODOC
     * @return {var} TODOC
     */
    layoutChild_sizeY_essentialWrapper : function(vChild, vJobs) {
      return vChild._isHeightEssential() ? this.layoutChild_sizeY(vChild, vJobs) : vChild._resetRuntimeHeight();
    }
  },




  /*
  *****************************************************************************
     DEFER
  *****************************************************************************
  */

  defer : function(statics, members)
  {
    members.computeChildrenNeededWidth = members.computeChildrenNeededWidth_max;
    members.computeChildrenNeededHeight = members.computeChildrenNeededHeight_max;
  },




  /*
  *****************************************************************************
     DESTRUCTOR
  *****************************************************************************
  */

  destruct : function() {
    this._disposeFields("_widget");
  }
});
