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

( function() {

var Animation = rwt.animation.Animation;
var AnimationRenderer = rwt.animation.AnimationRenderer;
var EventHandlerUtil = rwt.event.EventHandlerUtil;

rwt.qx.Mixin.define( "rwt.animation.VisibilityAnimationMixin", {

  properties : {

    animation : {
      check : "Object",
      nullable : false,
      init : null,
      apply : "_applyAnimation",
      themeable : true
    }

  },

  construct : function() {
    this.hide(); // forces _applyVisibility to be called on show() - not a good practice
    this.addEventListener( "changeVisibility", this._blockUserEvents, this );
    this.addEventListener( "destroy", this._onDestroyAnim, this );
    if( this instanceof rwt.widgets.Composite ) {
      this.show();
    }
  },

  destruct : function() {
    if( this._appearAnimation != null ) {
      this._appearAnimation.dispose();
    }
    this._appearAnimation = null;
    if( this._disappearAnimation != null ) {
      this._disappearAnimation.dispose();
    }
    this._disappearAnimation = null;
  },

  members : {
    _appearAnimation : null, // Declaration in constructor would be too late (mixin)
    _disappearAnimation : null,
    _animateDestroy : false,

    _applyAnimation : function( newValue ) {
      this._configureAppearAnimation( newValue );
      this._configureDisappearAnimation( newValue );
    },

    ////////////////////
    // Appear animations

    _configureAppearAnimation : function( config ) {
      if( this._appearAnimation !== null ) {
        this._appearAnimation.getDefaultRenderer().setActive( false );
      }
      for( var type in config ) {
        switch( type ) {
          case "fadeIn":
            this._configureFadeIn( config[ type ] );
          break;
          case "slideIn":
            this._configureSlideIn( config[ type ] );
          break;
          case "flyInTop":
          case "flyInLeft":
          case "flyInRight":
          case "flyInBottom":
            this._configureFlyIn( config[ type ], type );
          break;
        }
      }
    },

    _configureFadeIn : function( props ) {
      var animation = this._getAppearAnimation();
      animation.setProperties( props );
      animation.getDefaultRenderer().animate( this, "opacity", AnimationRenderer.ANIMATION_APPEAR );
    },

    _configureSlideIn : function( props ) {
      var animation = this._getAppearAnimation();
      animation.setProperties( props );
      var renderer = animation.getDefaultRenderer();
      var animationType = AnimationRenderer.ANIMATION_APPEAR | AnimationRenderer.ANIMATION_CHANGE;
      renderer.animate( this, "height", animationType );
      animation.addEventListener( "init", this._initSlideAnimation, this );
      animation.addEventListener( "cancel", this._finishSlideAnimation, this );
    },

    _configureFlyIn : function( props, type ) {
      var animation = this._getAppearAnimation();
      animation.setProperties( props );
      var renderer = animation.getDefaultRenderer();
      var animationType = AnimationRenderer.ANIMATION_APPEAR;
      switch( type ) {
        case "flyInTop":
          renderer.animate( this, "top", animationType );
          renderer.setInvisibilityGetter( rwt.animation.VisibilityAnimationMixin.hideTop );
        break;
        case "flyInBottom":
          renderer.animate( this, "top", animationType );
          renderer.setInvisibilityGetter( rwt.animation.VisibilityAnimationMixin.hideBottom );
        break;
        case "flyInLeft":
          renderer.animate( this, "left", animationType );
          renderer.setInvisibilityGetter( rwt.animation.VisibilityAnimationMixin.hideLeft );
        break;
        case "flyInRight":
          renderer.animate( this, "left", animationType );
          renderer.setInvisibilityGetter( rwt.animation.VisibilityAnimationMixin.hideRight );
        break;
      }
    },

    _getAppearAnimation : function() {
      if( this._appearAnimation === null ) {
        this._appearAnimation = new Animation();
      }
      this._appearAnimation.getDefaultRenderer().setActive( true );
      return this._appearAnimation;
    },

    ///////////////////////
    // Disappear Animations

    _onDestroyAnim : function() {
      var result = true;
      if( this._animateDestroy ) {
        this._markInDispose();
        this.hide();
        if( this._disappearAnimation.isStarted() ) {
          result = false;
          this._disappearAnimation.addEventListener( "cancel", this._finishDestroyAnimation, this );
        } else {
          this._animateDestroy = false;
          delete this._isInGlobalDisposeQueue;
        }
      }
      return result;
    },

    _finishDestroyAnimation : function() {
      this._animateDestroy = false;
      delete this._isInGlobalDisposeQueue;
      this.destroy();
    },

    _configureDisappearAnimation : function( config ) {
      if( this._disappearAnimation !== null ) {
        this._disappearAnimation.getDefaultRenderer().setActive( false );
        this._animateDestroy = false;
      }
      for( var type in config ) {
        switch( type ) {
          case "fadeOut":
            this._configureFadeOut( config[ type ] );
          break;
          case "slideOut":
            this._configureSlideOut( config[ type ] );
          break;
          case "flyOutTop":
          case "flyOutLeft":
          case "flyOutRight":
          case "flyOutBottom":
            this._configureFlyOut( config[ type ], type );
          break;
        }
      }
    },

    _configureFadeOut : function( props ) {
      var animation = this._getDisappearAnimation();
      var renderer = animation.getDefaultRenderer();
      renderer.animate( this, "opacity", AnimationRenderer.ANIMATION_DISAPPEAR );
      animation.setProperties( props );
    },

    _configureSlideOut : function( props ) {
      var animation = this._getDisappearAnimation();
      var renderer = animation.getDefaultRenderer();
      renderer.animate( this, "height", AnimationRenderer.ANIMATION_DISAPPEAR );
      animation.addEventListener( "init", this._initSlideAnimation, this );
      animation.addEventListener( "cancel", this._finishSlideAnimation, this );
      animation.setProperties( props );
    },

    _configureFlyOut : function( props, type ) {
      var animation = this._getDisappearAnimation();
      animation.setProperties( props );
      var renderer = animation.getDefaultRenderer();
      var animationType = AnimationRenderer.ANIMATION_DISAPPEAR;
      switch( type ) {
        case "flyOutTop":
          renderer.animate( this, "top", animationType );
          renderer.setInvisibilityGetter( rwt.animation.VisibilityAnimationMixin.hideTop );
        break;
        case "flyOutBottom":
          renderer.animate( this, "top", animationType );
          renderer.setInvisibilityGetter( rwt.animation.VisibilityAnimationMixin.hideBottom );
        break;
        case "flyOutLeft":
          renderer.animate( this, "left", animationType );
          renderer.setInvisibilityGetter( rwt.animation.VisibilityAnimationMixin.hideLeft );
        break;
        case "flyOutRight":
          renderer.animate( this, "left", animationType );
          renderer.setInvisibilityGetter( rwt.animation.VisibilityAnimationMixin.hideRight );
        break;
      }
    },

    _getDisappearAnimation : function() {
      if( this._disappearAnimation === null ) {
        this._disappearAnimation = new Animation();
        if( this instanceof rwt.widgets.Shell ) {
          this._disappearAnimation.addEventListener( "init", this._lockActiveState, this );
          this._disappearAnimation.addEventListener( "cancel", this._unlockActiveState, this );
        }
      }
      this._disappearAnimation.getDefaultRenderer().setActive( true );
      this._animateDestroy = true;
      return this._disappearAnimation;
    },

    /////////
    // helper

    _initSlideAnimation : function() {
      this.setContainerOverflow( false );
    },

    _finishSlideAnimation : function() {
      // TODO : could container overflow just be generally false, or use _applyHeight instead?
      this.setContainerOverflow( true );
    },

    _blockUserEvents : function( changeEvent ) {
      var element = this.getElement();
      if( element ) {
        EventHandlerUtil.blockUserDomEvents( element, !changeEvent.getValue() );
      }
    },

    _lockActiveState : function() {
      //this._setActiveState = rwt.util.Functions.returnNull;
      this.getWindowManager().blockActiveState = true;
    },

    _unlockActiveState : function() {
      var manager = rwt.widgets.base.Window.getDefaultWindowManager();
      manager.blockActiveState = false;
      if( !this.isDisposed() && !this._isInGlobalDisposeQueue ) {
        //delete this._setActiveState;
        this._setActiveState( this.getActive() );
      }
      var active = manager.getActiveWindow();
      if( active && active !== this ) {
        active._setActiveState( true );
      }
    }

 },

 statics : {

   hideTop : function( widget ) {
     return parseInt( widget.getHeightValue(), 10 ) * -1;
   },

   hideBottom : function( widget ) {
     return widget.getParent().getInnerHeight();
   },

   hideLeft : function( widget ) {
     return parseInt( widget.getWidthValue(), 10 ) * -1;
   },

   hideRight : function( widget ) {
     return widget.getParent().getInnerWidth();
   }

 }

} );

}());
