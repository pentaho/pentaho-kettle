/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

(function(){

var $ = rwt.util.RWTQuery;

rwt.qx.Class.define( "rwt.widgets.ProgressBar", {
  extend : rwt.widgets.base.Parent,

  construct : function( vertical, indeterminate ) {
    this.base( arguments );
    this.$el = $( this );
    this._indet = indeterminate || false;
    this._vertical = vertical || false;
    this._minimum = 0;
    this._maximum = 100;
    this._selection = 0;
    this._createIndicator();
    this._createTimer();
    this.setOverflow( "hidden" );
    this.setState( "normal" );
    this.setAppearance( "progressbar" );
  },

  destruct : function() {
    if( this._timer != null ) {
      this._timer.dispose();
    }
    this._timer = null;
  },

  statics : {
    UNDETERMINED_SIZE : 40
  },

  properties : {

    indicatorColor : {
      nullable : true,
      init : null,
      apply : "_applyIndicatorColor",
      themeable : true
    },

    indicatorImage : {
      nullable : true,
      init : null,
      apply : "_applyIndicatorImage",
      themeable : true
    },

    indicatorGradient : {
      nullable : true,
      init : null,
      apply : "_applyIndicatorGradient",
      themeable : true
    },

    indicatorOpacity : {
      nullable : true,
      init : 1,
      apply : "_applyIndicatorOpacity",
      themeable : true
    }

  },

  members : {

    setMinimum : function( minimum ) {
      this._minimum = minimum;
      this.addToQueue( "indicatorSelection" );
      this.dispatchSimpleEvent( "minimumChanged" );
    },

    setMaximum : function( maximum ) {
      this._maximum = maximum;
      this.addToQueue( "indicatorSelection" );
      this.dispatchSimpleEvent( "maximumChanged" );
    },

    setSelection : function( selection ) {
      this._selection = selection;
      this.addToQueue( "indicatorSelection" );
      this.dispatchSimpleEvent( "selectionChanged", this );
      this.dispatchSimpleEvent( "updateToolTip", this );
    },

    getMinimum : function() {
      return this._minimum;
    },

    getMaximum : function() {
      return this._maximum;
    },

    getSelection : function() {
      return this._selection;
    },

    setState : function( state ) {
      if( state == "error" ) {
        this.removeState( "normal" );
        this.removeState( "paused" );
        this.addState( "error" );
      } else if( state == "paused" ) {
        this.removeState( "normal" );
        this.removeState( "error" );
        this.addState( "paused" );
      } else {
        this.removeState( "error" );
        this.removeState( "paused" );
        this.addState( "normal" );
      }
    },

    getToolTipTargetBounds : function() {
      if( this.isVertical() ) {
        return {
          "top" : this.getBoxHeight() - this._cachedBorderLeft - this._indicator.offsetHeight,
          "left" : 0,
          "width" : this.getBoxWidth(),
          "height" : 1
        };
      } else if( this.isIndeterminate() ) {
        return {
          "left" : 0,
          "top" : 0,
          "width" : this.getBoxWidth(),
          "height" : this.getBoxHeight()
        };
      } else {
        return {
          "left" : this._cachedBorderLeft + this._indicator.offsetWidth,
          "top" : 0,
          "width" : 1,
          "height" : this.getBoxHeight()
        };
      }
    },

    isIndeterminate : function() {
      return this._indet;
    },

    isHorizontal : function() {
      return !this._vertical;
    },

    isVertical : function() {
      return this._vertical;
    },

    _applyIndicatorColor : function( value ) {
      this.$indicator.css( "backgroundColor", value || "" );
    },

    _applyIndicatorImage : function( value ) {
      this.$indicator.css( "backgroundImage", value || "" );
    },

    _applyIndicatorGradient : function( value ) {
      this.$indicator.css( "backgroundGradient", value || "" );
    },

    _applyIndicatorOpacity : function( value ) {
      this.$indicator.css( "opacity", value || "" );
    },

    _onInterval : function() {
      if( this.isSeeable() ) {
        this._animPosition += 1;
        var max = this._vertical ? this.getHeight() : this.getWidth();
        if( this._animPosition > max ) { // we can reasonably ignore the border
          this._animPosition = rwt.widgets.ProgressBar.UNDETERMINED_SIZE * -1;
        }
        this._renderIndicatorSelection();
      }
    },

    _layoutPost : function( changes ) {
      if( changes.indicatorSelection || changes.initial ) {
        this._renderIndicatorSelection();
      }
    },

    _createIndicator : function() {
      this._indicator = document.createElement( "div" );
      this.$el.append( this._indicator );
      this.$indicator = $( this._indicator );
      this.$indicator.css( "position", "absolute" );
      if( this._vertical ) {
        this.$indicator.css( {
          "left" : 0,
          "top" : "auto",
          "width" : "100%"
        } );
      } else {
        this.$indicator.css( {
          "top" : 0 ,
          "height" : "100%",
          "bottom" : "auto"
        } );
      }
    },

    _createTimer : function() {
      if( this._indet ) {
        this._animPosition = rwt.widgets.ProgressBar.UNDETERMINED_SIZE * -1;
        this._timer = new rwt.client.Timer( 30 );
        this._timer.addEventListener( "interval", this._onInterval, this );
        this._timer.start();
      }
    },

    _renderIndicatorSelection : function() {
      var length = this._computeIndicatorCssLength();
      var offset = this._computeIndicatorCssOffset();
      if( this._vertical ) {
        this.$indicator.css( {
          "height" : length,
          "bottom" : offset
        } );
      } else {
        var props = {};
        props[ "width" ] = length;
        props[ this.getDirection() === "rtl" ? "right" : "left" ] = offset;
        this.$indicator.css( props );
      }
    },

    _computeIndicatorCssLength : function() {
      if( this.isIndeterminate() ) {
        return rwt.widgets.ProgressBar.UNDETERMINED_SIZE;
      }
      var selected = this._selection - this._minimum;
      var max = this._maximum - this._minimum;
      return Math.floor( 100 * selected / max ) + "%";
    },

    _computeIndicatorCssOffset : function() {
      return this.isIndeterminate() ? this._animPosition : 0;
    }

  }

} );

}());
