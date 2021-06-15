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
 * The Terminator is the base class for all widgets, which don't have child
 * widgets.
 *
 * If used directly it represents an rectangular area, which can be positioned
 * and sized using the layout managers.
 */
rwt.qx.Class.define( "rwt.widgets.base.Terminator", {

  extend : rwt.widgets.base.Widget,

  members : {

    _applyTextColor : function() {
      this._renderTextColor();
    },

    _renderAppearance : function() {
      this.base( arguments );
      this._renderTextColor();
    },

    _renderTextColor : function() {
      var color = this.getEnabled() ? this.getTextColor() : this._getDisabledColor();
      if( this.getStyleProperty( "color" ) !== color ) {
        if( color ) {
          this.setStyleProperty( "color", color );
        } else {
          this.removeStyleProperty( "color" );
        }
      }
    },

    _getDisabledColor : function() {
      var result = this.__theme$textColor;
      var parent = this.getParent();
      while( !result && parent !== null ) {
        result = parent.__theme$textColor;
        parent = parent.getParent();
      }
      return result;
    },

    renderPadding : function( changes ) {
      if( changes.paddingLeft ) {
        this._renderRuntimePaddingLeft( this.getPaddingLeft() );
      }

      if( changes.paddingRight ) {
        this._renderRuntimePaddingRight( this.getPaddingRight() );
      }

      if( changes.paddingTop ) {
        this._renderRuntimePaddingTop( this.getPaddingTop() );
      }

      if( changes.paddingBottom ) {
        this._renderRuntimePaddingBottom( this.getPaddingBottom() );
      }
    },

    _renderContent : function() {
      // Small optimization: Only add innerPreferred jobs
      // if we don't have a static width
      if( this._computedWidthTypePixel ) {
        this._cachedPreferredInnerWidth = null;
      } else {
        this._invalidatePreferredInnerWidth();
      }

      // Small optimization: Only add innerPreferred jobs
      // if we don't have a static height
      if( this._computedHeightTypePixel ) {
        this._cachedPreferredInnerHeight = null;
      } else {
        this._invalidatePreferredInnerHeight();
      }

      // add load job
      if( this._initialLayoutDone ) {
        this.addToJobQueue( "load" );
      }
    },

    _layoutPost : function( changes ) {
      if( changes.initial || changes.load || changes.width || changes.height ) {
        this._postApply();
      }
      this.createDispatchDataEvent( "flush", changes ); // TODO [tb] : use simple event
    },

    _postApply : rwt.util.Functions.returnTrue,

    _computeBoxWidthFallback : function() {
      return this.getPreferredBoxWidth();
    },

    _computeBoxHeightFallback : function() {
      return this.getPreferredBoxHeight();
    },

    /**
     * Returns the preferred inner width of the widget. This value is used
     * by the layout managers to calculate the actual size of the widget.
     *
     * @return {Integer} the preffered inner width.
     * @signature function()
     */
    _computePreferredInnerWidth : rwt.util.Functions.returnZero,

    /**
     * Returns the preferred inner height of the widget. This value is used
     * by the layout managers to calculate the actual size of the widget.
     *
     * @return {Integer} the preffered inner height.
     * @signature function()
     */
    _computePreferredInnerHeight : rwt.util.Functions.returnZero,

    _isWidthEssential : function() {
      if( !this._computedLeftTypeNull && !this._computedRightTypeNull ) {
        return true;
      }

      if( !this._computedWidthTypeNull && !this._computedWidthTypeAuto ) {
        return true;
      }

      if( !this._computedMinWidthTypeNull && !this._computedMinWidthTypeAuto ) {
        return true;
      }

      if( !this._computedMaxWidthTypeNull && !this._computedMaxWidthTypeAuto ) {
        return true;
      }

      if( this._targetNode ) {
        return true;
      }

      return false;
    },

    _isHeightEssential : function() {
      if( !this._computedTopTypeNull && !this._computedBottomTypeNull ) {
        return true;
      }

      if( !this._computedHeightTypeNull && !this._computedHeightTypeAuto ) {
        return true;
      }

      if( !this._computedMinHeightTypeNull && !this._computedMinHeightTypeAuto ) {
        return true;
      }

      if( !this._computedMaxHeightTypeNull && !this._computedMaxHeightTypeAuto ) {
        return true;
      }

      if( this._targetNode ) {
        return true;
      }

      return false;
    }

  }

} );
