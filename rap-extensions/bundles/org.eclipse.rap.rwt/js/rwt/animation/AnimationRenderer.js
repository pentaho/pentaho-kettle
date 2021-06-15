/*******************************************************************************
 * Copyright (c) 2010, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.qx.Class.define( "rwt.animation.AnimationRenderer", {

  extend : rwt.qx.Object,

  construct : function( animation ) {
    // Animation is responsible for the dispose:
    this._autoDispose = false;
    this.base( arguments );
    this._animation = animation;
    this._animation._addRenderer( this );
    // Simple use:
    this._converterFunction = null;
    this._renderFunction = null;
    this._context = null;
    this._startValue = null;
    this._endValue = null;
    this._lastValue = null;
    this._setupFunction = null;
    this._cloneFrom = null;
    this._active = true;
    this._activeOnce = false;
    // Widget integration:
    this._invisibilityGetter = rwt.util.Functions.returnZero;
    this._fullVisibilityValue = null;
    this._autoStartEnabled = true;
    this._renderType = null;
    this._renderAdapter = null;
    this._animationType = 0;
    this._autoCheck = true;
  },

  destruct : function() {
    this.clearAnimation();
    this._animation._removeRenderer( this );
    this._animation = null;
    this._startValue = null;
    this._endValue = null;
    this._invisibilityGetter = null;
    this._lastValue = null;
    this._setupFunction = null;
    this._converterFunction = null;
    this._renderFunction = null;
    this._context = null;
    this._cloneFrom = null;
  },

  members : {

    //////////////////////////
    // Public API - simple use

    // Converts transitionValue (usually between 0 and 1) to the render-value.
    setConverter : function( type ) {
      if( typeof type == "string" ) {
        this._converterFunction = rwt.animation.AnimationRenderer.converter[ type ];
      } else {
        this._converterFunction = type;
      }
    },

    // Sets the function that is called with the value to render.
    setRenderFunction : function( func, context ) {
      if( this._renderType == null ) {
        this._renderFunction = func;
        this._context = context;
      }
    },

    renderValue : function( value ) {
      this._renderFunction.call( this._context, value );
      this._lastValue = value;
    },

    setStartValue : function( value ) {
      this._startValue = value;
    },

    setEndValue : function( value ) {
      this._endValue = value;
    },

    // The setup-function is called (if set) directly before the first frame of
    // an Animation is rendered. It is the last chance to set startValue,
    // endValue, renderFunction and converter before they are used.
    // The first parameter is the "config" value from Animation.start().
    // The second paramter will be the animationRenderer.
    setSetupFunction : function( func ) {
      this._setupFunction = func;
    },

    // Use lastValue from this renderer as transition-value.
    setCloneFrom : function( renderer ) {
      this._cloneFrom = renderer;
    },

    getAnimation : function() {
      return this._animation;
    },

    getContext : function() {
      return this._context;
    },

    getStartValue : function() {
      return this._startValue;
    },

    getEndValue : function() {
      return this._endValue;
    },

    // Returns the value that was last rendered.
    getLastValue : function() {
      return this._lastValue;
    },

    // Set to false to disable calls to setupFunction and renderFunction.
    // Also disables widget-integration.
    setActive : function( value ) {
      if( this._active !== value ) {
        if( this._animation.isRunning() ) {
          throw "AnimationRenderer: Can not change \"active\" while running!";
        }
        this._active = value;
        if( this._renderType !== null ) {
          this._handleAnimationType();
        }
      }
    },

    isActive : function() {
      return this._active;
    },

    // Sets active to false as soon as animation is finished
    activateOnce : function() {
      if( !this._activeOnce ) {
        this.setActive( true );
        this._activeOnce = true;
      }
    },

    cancelActivateOnce : function() {
      if( this._activeOnce ) {
        this._activeOnce = false;
        this.setActive( false );
      }
    },

    //////////////////////////////////
    // internals - called by Animation

    _setup : function( config ) {
      if( this._active ) {
        if( this._context instanceof rwt.widgets.base.Widget && this._context._isCreated !== true ) {
          if( this._context._isInGlobalElementQueue ) {
            rwt.widgets.base.Widget.flushGlobalQueues();
          } else {
            throw new Error( "AnimationRenderer setup failed: Widget not ready." );
          }
        }
        if( this._setupFunction != null ) {
          this._setupFunction.call( this._context, config, this );
        }
        this._startValue = this._prepareValue( this._startValue );
        this._endValue = this._prepareValue( this._endValue );
        if( this._renderFunction == null || this._converterFunction == null ) {
          throw new Error( "renderFunction or converterFunction missing" );
        }
      }
    },

    _render : function( transitionValue ) {
      if( this._active ) {
        var convertValue = this._cloneFrom != null
                           ? this._cloneFrom.getLastValue()
                           : transitionValue;
        try {
          var value = this._converterFunction( convertValue, this._startValue, this._endValue );
          this.renderValue( value );
        } catch( e ) {
          throw "AnimationRenderer failed: " + ( e.message ? e.message : e );
        }
      }
    },

    _finish : function( config ) {
      if( this._active && config == "disappear" ) {
        this._updateWidgetVisibility();
        this._forceWidgetRenderer();
      }
      this.cancelActivateOnce();
    },

    _prepareValue : function( value ) {
      var result = value;
      switch( this._renderType ) {
        case "backgroundColor":
          if( typeof value == "string" ) {
            if( value == "transparent" || value === "" || value.slice( 0, 4 ) === "rgba" ) {
              result = null;
            } else {
              result = rwt.util.Colors.stringToRgb( value );
            }
          }
        break;
        case "backgroundGradient":
          if( value ) {
            var result = [];
            for( var i = 0; i < value.length; i++ ) {
              result[ i ] = [
                value[ i ][ 0 ],
                rwt.util.Colors.stringToRgb( value[ i ][ 1 ] )
              ];
            }
          }
        break;
        case "opacity":
          result = ( value == null || value > 1 || value < 0 ) ? 1 : value;
        break;
        default:
          result = value != null ? value : 0;
        break;
      }
      return result;
    },

    ///////////////////////////
    // API - Widget integration

    // The RenderType can currently be:  "height", "opacity", "backgroundColor",
    // "backgroundGradient". The AnimationTypes are defined in the statics.
    animate : function( widget, renderType, animationType ) {
      if(    this._context != widget
          || this._renderType != renderType
          || this._animationType != animationType )
      {
        this.clearAnimation();
        this._context = widget;
        this._renderAdapter = widget.getAdapter( rwt.widgets.util.WidgetRenderAdapter );
        this._renderType = renderType;
        this._animationType = animationType;
        this._renderFunction = this._renderAdapter.getOriginalRenderer( this._renderType );
        var map = rwt.animation.AnimationRenderer.converterByRenderType;
        this.setConverter( map[ this._renderType ] );
        this._handleAnimationType();
      }
    },

    clearAnimation : function() {
      if( this._renderType !== null ) {
        this._animationType = 0;
        this._handleAnimationType();
        this._renderType = null;
        this.setRenderFunction( null, null );
        this._renderAdapter = null;
      }
    },

    isAnimated : function( type ) {
      var result = false;
      if( this._animationType > 0 && this._active ) {
        var animated = type & this._animationType;
        if( typeof type === "undefined" || animated !== 0 ) {
          result = true;
        }
      }
      return result;
    },

    setInvisibilityGetter : function( value ) {
      this._invisibilityGetter = value;
    },

    // default is true
    setAutoStart : function( value ) {
      this._autoStartEnabled = value;
    },

    // Prevent autoStart if startValue/endValue are invalid. If set to false,
    // the values can be set before or in the setupFunction is called.
    // Default is true.
    setAutoCheck : function( value ) {
      this._autoCheck = value;
    },

    // Return the actual, planned or last known value from the widget.
    getValueFromWidget : function() {
      var result = null;
      switch( this._renderType ) {
        case "opacity":
          result = this._context.getOpacity();
        break;
        case "height":
          if( this._context.isCreated() && this._context._style.height ) {
            result = parseInt( this._context._style.height, 10 );
          } else {
            result = this._context.getHeightValue();
            this._context._computedHeightValue = null;
            this._context._invalidatePreferredInnerHeight();
            this._context._invalidatePreferredBoxHeight();
          }
        break;
        case "top":
          if( this._context.isCreated() && this._context._style.top ) {
            result = parseInt( this._context._style.top, 10 );
          } else {
            result = this._context.getTopValue();
          }
        break;
        case "left":
          if( this._context.isCreated() && this._context._style.left ) {
            result = parseInt( this._context._style.left, 10 );
          } else {
            result = this._context.getLeftValue();
          }
        break;
        case "backgroundColor":
          var bg = "backgroundColor";
          var context = this._context;
          if( context.getGfxProperty && context.getGfxProperty( bg ) ) {
            result = context.getGfxProperty( bg );
          } else {
            result = rwt.html.Style.getBackgroundColor( context );
          }
        break;
        case "backgroundGradient":
          var context = this._context;
          // NOTE : this is not necessarily the actually last rendered value, but converting from
          //        css3-syntax to rwt-gradient would be overkill. It shouldn't matter.
          result = context.getBackgroundGradient();
        break;
        default:
          throw "getValueFromWidget: " + this._renderType + " not supported!";
      }
      return result;
    },

    // Are current values valid for animation (after using prepareValue)
    // Assumes that the given values ARE valid as a property of the renderType.
    checkValues : function() {
      var result;
      switch( this._renderType ) {
        case "backgroundGradient":
        case "backgroundColor":
          result = this._startValue != null && this._endValue != null;
        break;
        default:
          result = true;
        break;
      }
      // NOTE: Does not compare objects, i.e. gradients:
      return result && this._startValue != this._endValue;
    },

    //////////////////////////////////
    // Widget integration - internals

    _handleAnimationType : function() {
      if( this._animation.isRunning() ) {
        throw "AnimationRenderer: Can not change animation while running!";
      }
      // Note: Conventional event-handler would not be able to prevent the
      // actual rendering, therefore the functions are overwritten instead.
      if( this.isAnimated() ) {
        this._attachToApplyVisibility( true );
        this._attachToWidgetRenderer( true );
      } else {
        this._attachToApplyVisibility( false );
        this._attachToWidgetRenderer( false );
      }
    },

    _attachToApplyVisibility : function( value ) {
      if( value ) {
        this._renderAdapter.addRenderListener( "visibility", this._onVisibilityChange, this );
        this._context.addEventListener( "create", this._onCreate, this );
      } else {
        this._renderAdapter.removeRenderListener( "visibility", this._onVisibilityChange, this );
        this._context.removeEventListener( "create", this._onCreate, this );
      }
    },

    _attachToWidgetRenderer : function( value ) {
      if( value ) {
        this._renderAdapter.addRenderListener( this._renderType, this._onOriginalRenderer, this );
      } else {
        this._renderAdapter.removeRenderListener( this._renderType, this._onOriginalRenderer, this );
      }

    },

    //////////////////////////////////////
    // Widget integration - event handlers

    _onVisibilityChange : function( originalArgs ) {
      var value = originalArgs[ 0 ];
      var allow = false;
      if( value ) {
        allow = this._onBeforeAppear();
      } else {
        if( !this._context.isCreated() ) {
          this._animation.cancel(); // scheduled appear animation
        }
        if( this._context.isSeeable() ) {
          allow = this._onBeforeDisappear();
        } else {
          allow = true;
        }
      }
      return allow;
    },


    _onCreate : function() {
      if( this._context.isDisplayable() ) {
       this._onBeforeAppear();
      }
    },

    _onBeforeAppear : function() {
      if( this._context.isCreated() ) {
        this._animation.skip();
      } else {
        this._animation.cancel();
      }
      var typeAppear = rwt.animation.AnimationRenderer.ANIMATION_APPEAR;
      if( this._context.isCreated() && this.isAnimated( typeAppear ) ) {
        this.setEndValue( this.getValueFromWidget() );
        if( this._invisibilityGetter != null ) {
          this.setStartValue( this._invisibilityGetter( this._context ) );
          this._render( 0 );
        }
        this._autoStart( typeAppear );
      }
      return true;
    },

    _onBeforeDisappear : function() {
      this._animation.skip();
      var typeDisappear = rwt.animation.AnimationRenderer.ANIMATION_DISAPPEAR;
      var result = !this.isAnimated( typeDisappear );
      if( !result ) {
        if( this._invisibilityGetter !== null ) {
          this.setEndValue( this._invisibilityGetter( this._context ) );
        }
        this.setStartValue( this.getValueFromWidget() );
        this._autoStart( typeDisappear );
      }
      return result;
    },

    _onOriginalRenderer : function( originalArgs ) {
      var value = originalArgs[ 0 ];
      var oldValue = originalArgs[ 1 ];
      var result = false;
      if( this._animation.isStarted() ) {
        var config = this._animation.getConfig();
        var endValue = this._endValue;
        if( config == "change" || config == "appear" ) {
          this.setEndValue( value );
        }
        if( endValue != this._endValue ) {
          if( this._animation.isRunning() ) {
            this.setStartValue( this.getLastValue() );
          }
          if( !this.checkValues() || !this._animation.restart() ) {
            this._animation.cancel();
            this.cancelActivateOnce();
            result = true;
          }
        }
      } else {
        var typeChange = rwt.animation.AnimationRenderer.ANIMATION_CHANGE;
        if( this.isAnimated( typeChange ) && this._context.isSeeable() ) {
          this.setStartValue(   typeof oldValue !== "undefined"
                              ? oldValue
                              : this.getValueFromWidget() );
          this.setEndValue( value );
          if( !this._autoStart( typeChange ) && this._autoStartEnabled ) {
            result = true;
          }
        } else {
          result = true;
        }
      }
      return result;
    },

    //////////////////////////////
    // Widget integration - helper

    // Forces the widget to call the renderer, may be asynchronous due to flush.
    _forceWidgetRenderer : function() {
      var applyName = rwt.animation.AnimationRenderer.applyFunctionNames[ this._renderType ];
      this._context[ applyName ]( this._context.get( this._renderType ) );
    },

    _autoStart : function( type ) {
      var result = false;
      if(    this._autoStartEnabled
          && this.isAnimated( type )
          && ( this._autoCheck ? this.checkValues() : true ) )
      {
        result = this._animation.start( this._typeToConfig( type ) );
      } else {
        this.cancelActivateOnce();
      }
      return result;
    },

    _typeToConfig : function( type ) {
      var result = null;
      switch( type ) {
        case rwt.animation.AnimationRenderer.ANIMATION_APPEAR:
          result = "appear";
        break;
        case rwt.animation.AnimationRenderer.ANIMATION_DISAPPEAR:
          result = "disappear";
        break;
        case rwt.animation.AnimationRenderer.ANIMATION_CHANGE:
          result = "change";
        break;
      }
      return result;
    },

    // calls the original "_applyVisibility".
    _updateWidgetVisibility : function() {
      var value = this._context.getVisibility();
      this._renderAdapter.forceRender( "visibility", value );
    }

  },

  statics : {

    ANIMATION_APPEAR : 1,
    ANIMATION_DISAPPEAR : 2,
    ANIMATION_CHANGE : 4,

    applyFunctionNames : {
      "height" : "_applyHeight",
      "opacity" : "_applyOpacity",
      "backgroundColor" : "_applyBackgroundColor",
      "backgroundGradient" : "_applyBackgroundGradient",
      "top" : "_applyTop",
      "left" : "_applyLeft"
    },

    converterByRenderType : {
      "height" : "numericPositiveRound",
      "top" : "numericRound",
      "left" : "numericRound",
      "opacity" : "factor",
      "backgroundColor" : "color",
      "backgroundGradient" : "gradient"
    },

    converter : {

      // Converter working without startValue/EndValue

      none : function( value ) {
        return value;
      },

      round : Math.round,

      positive : function( value ) {
        return Math.max( 0, value );
      },

      // Converter needing valid startValue/EndValue

      numeric : function( value, startValue, endValue ) {
        return startValue + ( endValue - startValue ) * value;
      },

      numericRound : function( value, startValue, endValue ) {
        var result = startValue + ( endValue - startValue ) * value;
        return Math.round( result );
      },

      numericPositive : function( value, startValue, endValue ) {
        var diff = endValue - startValue;
        return Math.max( 0, startValue + diff * value );
      },

      numericPositiveRound : function( value, startValue, endValue ) {
        var diff = endValue - startValue;
        var result = Math.max( 0, startValue + diff * value );
        return Math.round( result );
      },

      factor : function( value, startValue, endValue ) {
        var result = startValue + ( endValue - startValue ) * value;
        return Math.max( 0, Math.min( result, 1) );
      },

      color : function( value, startValue, endValue ) {
        var result = [];
        var part;
        var partDiff;
        for( var i = 0; i < 3; i++ ) {
          partDiff = endValue[ i ] - startValue[ i ];
          part = Math.round( startValue[ i ] + partDiff * value );
          result[ i ] = Math.max( 0, Math.min( part, 255 ) );
        }
        return rwt.util.Colors.rgbToRgbString( result );
      },

      // Assumes that the number of colors are identical
      gradient : function( value, startValue, endValue ) {
        var convertColor = rwt.animation.AnimationRenderer.converter.color;
        var convertFactor = rwt.animation.AnimationRenderer.converter.factor;
        var result = [];
        var length = Math.min( endValue.length, startValue.length );
        for( var i = 0; i < length; i++ ) {
          result[ i ] = [
            convertFactor( value, startValue[ i ][ 0 ], endValue[ i ][ 0 ] ),
            convertColor( value, startValue[ i ][ 1 ], endValue[ i ][ 1 ] )
          ];
        }
        return result;
      }

    }//converter

  }

} );
