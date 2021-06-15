/*******************************************************************************
 * Copyright (c) 2009, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.base.BasicButton", {

  extend : rwt.widgets.base.MultiCellWidget,

  construct : function( buttonType, noKeyControl ) {
    this.base( arguments, this._CELLORDER );
    this._selected = false;
    this._image = [ null, null, null ] ;
    this._hotImage = [ null, null, null ];
    this.addEventListener( "mouseover", this._onMouseOver );
    this.addEventListener( "mouseout", this._onMouseOut );
    this.addEventListener( "mousedown", this._onMouseDown );
    this.addEventListener( "mouseup", this._onMouseUp );
    if( !noKeyControl ) {
      this.addEventListener( "keydown", this._onKeyDown );
      this.addEventListener( "keyup", this._onKeyUp );
      this.addEventListener( "keypress", this._onKeyPress );
    }
    this.addState( buttonType );
    switch( buttonType ) {
      case "arrow":
      case "push":
        this._isSelectable = false;
        this._isDeselectable = false;
      break;
      case "toggle":
      case "check":
        this._isSelectable = true;
        this._isDeselectable = true;
      break;
      case "radio":
        this._isSelectable = true;
        this.setNoRadioGroup( false );
        rwt.widgets.util.RadioButtonUtil.registerExecute( this );
        rwt.widgets.util.RadioButtonUtil.registerKeypress( this );
      break;
    }

  },

  destruct : function() {
    if( this._animation != null ) {
      this._animation.dispose();
    }
    this._animation = null;
  },

  events: {
    "stateOverChanged" : "rwt.event.Event"
  },

  properties : {

    selectionIndicator : {
      apply : "_applySelectionIndicator",
      nullable : true,
      themeable : true
    },

    // TODO [tb] : non-ideal solution to provide theming support for image
    icon : {
      apply : "_applyIcon",
      nullable : true,
      themeable : true
    },

    animation : {
      check : "Object",
      nullable : false,
      init : null,
      apply : "_applyAnimation",
      themeable : true
    }

  },

  members : {
    _CELLORDER : [ "image", "image", "label" ],

    _applyIcon : function( newValue ) {
      this.setImage.apply( this, newValue );
    },

    setImage : function( value, width, height ) {
      this._image = [ value, width, height ];
      this._updateButtonImage();
    },

    getImage : function() {
      return this._image;
    },

    setHotImage : function( value, width, height ) {
      this._hotImage = [ value, width, height ];
      this._updateButtonImage();
    },

    _updateButtonImage : function() {
      var image =
          ( this._hotImage[ 0 ] != null && this.hasState( "over" ) )
        ? this._hotImage
        : this._image;
      var current = this.getCellContent( 1 );
      if( current != image[ 0 ] ) {
        this.setCellContent( 1, image[ 0 ] );
        this.setCellDimension( 1, image[ 1 ], image[ 2 ] );
      }
    },

    setText : function( value ) {
      this.setCellContent( 2, value );
    },

    _applySelectionIndicator : function( value ) {
      var url = value ? value[ 0 ] : null;
      var width = value ? value[ 1 ] : 0;
      var height = value ? value[ 2 ] : 0;
      this.setCellContent( 0, url );
      this.setCellDimension( 0, width, height );
    },

    setGrayed : function( value ) {
      this.toggleState( "grayed", value );
    },

    setNoRadioGroup : function( value ) {
      if( this.hasState( "radio") ) {
        this._noRadioGroup = value;
        this._isDeselectable = value;
      }
    },

    getNoRadioGroup : function() {
      return this._noRadioGroup;
    },

    execute : function() {
      this.base( arguments );
      if( this._isSelectable ) {
        this.setSelection( !( this._selected && this._isDeselectable ) );
      } else {
        this._notifySelected();
      }
    },

    setSelection : function( value ) {
      var wasSelected = this._selected;
      var selectionChanged = this._selected != value;
      if( selectionChanged ) {
        this._selected = value;
        this.toggleState( "selected", value );
        if( !rwt.remote.EventUtil.getSuspended() ) {
          var server = rwt.remote.Connection.getInstance();
          server.getRemoteObject( this ).set( "selection", this._selected );
        }
      }
      if( selectionChanged || wasSelected ) {
        this._notifySelected();
      }
    },

    _notifySelected : function() {
      // subclasses may overwrite
    },

    _onMouseOver : function( event ) {
      // [tb] Firefox can sometimes fire false "over" events.
      if ( event.getTarget() == this && !this.hasState( "over" ) ) {
        if( this.hasState( "abandoned" ) ) {
          this.removeState( "abandoned" );
          this.addState( "pressed" );
        }
        this.addState( "over" );
        this._updateButtonImage();
        this.createDispatchEvent( "stateOverChanged" );
      }
    },

    _onMouseOut : function( event ) {
      if ( event.getTarget() == this ) {
        this.removeState( "over" );
        this._updateButtonImage();
        if ( this.hasState( "pressed" ) ) {
          this.setCapture( true );
          this.removeState( "pressed" );
          this.addState( "abandoned" );
        }
        this.createDispatchEvent( "stateOverChanged" );
      }
    },

    _onMouseDown : function( event ) {
      if ( event.getTarget() == this && event.isLeftButtonPressed() ) {
        this.removeState( "abandoned" );
        this.addState( "pressed" );
      }
    },

    _onMouseUp : function() {
      this.setCapture( false );
      var hasPressed = this.hasState( "pressed" );
      var hasAbandoned = this.hasState( "abandoned" );
      if( hasPressed ) {
        this.removeState( "pressed" );
      }
      if ( hasAbandoned ) {
        this.removeState( "abandoned" );
      }
      if ( !hasAbandoned ) {
        this.addState( "over" );
        this._updateButtonImage();
        if ( hasPressed ) {
          this.execute();
        }
      }
    },

    _onKeyDown : function( event ) {
      switch( event.getKeyIdentifier() ) {
        case "Enter":
        case "Space":
          this.removeState( "abandoned" );
          this.addState( "pressed" );
          event.stopPropagation();
      }
    },

    _onKeyUp : function( event ) {
      switch( event.getKeyIdentifier() ) {
        case "Enter":
        case "Space":
          if ( this.hasState( "pressed" ) ) {
            this.removeState( "abandoned" );
            this.removeState( "pressed" );
            this.execute();
            event.preventDefault();
            event.stopPropagation();
          }
      }
    },

    _onKeyPress : function( event ) {
      switch( event.getKeyIdentifier() ) {
        case "Left":
        case "Up":
        case "Right":
        case "Down":
        case "PageUp":
        case "PageDown":
        case "End":
        case "Home":
        case "Enter":
        case "Space":
          event.preventDefault();
          event.stopPropagation();
      }
    },

    ////////////
    // Animation

    _applyAnimation : function( newValue ) {
      if( newValue[ "hoverIn" ] || newValue[ "hoverOut" ] ) {
        if( this._animation == null ) {
          this._animation = new rwt.animation.Animation();
          this._animation.addEventListener( "init",
                                            this._initAnimation,
                                            this );
        }
        this.addEventListener( "stateOverChanged",
                               this._animation.activateRendererOnce,
                               this._animation );
        this.addEventListener( "changeBackgroundGradient",
                               this._configureRenderer,
                               this );
        this._configureRenderer();
      } else if( this._animation != null ) {
        this.removeEventListener( "stateOverChanged",
                                  this._animation.activateRendererOnce,
                                  this._animation );
        this.removeEventListener( "changeBackgroundGradient",
                                  this._configureRenderer,
                                  this );
      }
    },

    _configureRenderer : function( event ) {
      if( !event || event.getValue() == null || event.getOldValue() == null ) {
        this._animation.skip();
        var renderer = this._animation.getDefaultRenderer( false );
        var renderType =   this.getBackgroundGradient() != null
                         ? "backgroundGradient"
                         : "backgroundColor";
        var animationType = rwt.animation.AnimationRenderer.ANIMATION_CHANGE;
        renderer.animate( this, renderType, animationType );
      }
    },

    _initAnimation : function() {
      if( this.hasState( "pressed" ) ) {
        this._animation.cancel();
      } else {
        var animation = this.getAnimation();
        if( this.hasState( "over" ) && animation[ "hoverIn" ] ) {
          this._animation.setProperties( animation[ "hoverIn" ] );
        } else if( !this.hasState( "over" ) && animation[ "hoverOut" ] ) {
          this._animation.setProperties( animation[ "hoverOut" ] );
        } else {
          this._animation.cancel();
        }
      }
    },

    _renderAppearance : function() {
      this.base( arguments );
      // TODO [tb] : Find a more elegant and generic way to do this.
      if( this._animation != null && !this._animation.isStarted() ) {
        this._animation.getDefaultRenderer().cancelActivateOnce();
      }
    }

  }
} );
