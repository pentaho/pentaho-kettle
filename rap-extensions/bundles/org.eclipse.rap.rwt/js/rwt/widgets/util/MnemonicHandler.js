/*******************************************************************************
 * Copyright (c) 2013, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

(function() {

var EventHandlerUtil = rwt.event.EventHandlerUtil;
var WidgetUtil = rwt.widgets.util.WidgetUtil;

rwt.qx.Class.define( "rwt.widgets.util.MnemonicHandler", {

  extend : rwt.qx.Object,

  statics : {

    getInstance : function() {
      return rwt.runtime.Singletons.get( rwt.widgets.util.MnemonicHandler );
    }

  },

  construct : function() {
    this.base( arguments );
    this._map = {};
    this._activator = null;
    this._active = null;
    this._activeMenu = null;
    this._allowMenuActivate = false;
  },

  members : {

    add : function( widget, listener ) {
      var shell = WidgetUtil.getShell( widget );
      if( shell != null ) {  // TODO [tb] : this is for MenuBar items, handle them like Menu items
        this._registerFocusRoot( shell );
        var handlers = this._map[ shell.toHashCode() ];
        if( widget instanceof rwt.widgets.MenuItem ) {
          handlers.menu[ widget.toHashCode() ] = [ widget, listener ];
        } else {
          handlers.controls[ widget.toHashCode() ] = [ widget, listener ];
        }
      }
    },

    remove : function( widget ) {
      // NOTE: The shell may be gone if the widget is in dispose, therefore we have to search:
      var hash = widget.toHashCode();
      if( widget instanceof rwt.widgets.MenuItem ) {
        for( var key in this._map ) {
          if( this._map[ key ].menu[ widget.toHashCode() ] ) {
            delete this._map[ key ].menu[ hash ];
            return;
          }
        }
      } else {
        for( var key in this._map ) {
          if( this._map[ key ].controls[ widget.toHashCode() ] ) {
            delete this._map[ key ].controls[ hash ];
            return;
          }
        }
      }
    },

    setActivator : function( str ) {
      if( str ) {
        this._activator = {};
        this._activator.ctrlKey = str.indexOf( "CTRL" ) !== -1;
        this._activator.altKey = str.indexOf( "ALT" ) !== -1;
        this._activator.shiftKey = str.indexOf( "SHIFT" ) !== -1;
      } else {
        this._activator = null;
      }
    },

    isActive : function() {
      return this._active != null;
    },

    handleKeyEvent : function( eventType, keyCode, charCode, domEvent ) {
      var result = false;
      this._checkActiveMenu();
      // Some browser fire multiple keydown for modifier keys:
      if( eventType !== "keydown" || rwt.event.EventHandlerUtil.isFirstKeyDown( keyCode ) ) {
        if( this._isActivation( eventType, keyCode, charCode, domEvent ) ) {
          this.activate();
        } else if( this._isDeactivation( eventType, keyCode, charCode, domEvent ) ) {
          this.deactivate( this._allowMenuActivate || this._activeMenu );
        } else if( this._isTrigger( eventType, keyCode, charCode, domEvent ) ) {
          result = this.trigger( keyCode );
          this._allowMenuActivate = false;
        } else if( this.isActive() && !this._isActivatorCombo( domEvent ) ) {
          this._allowMenuActivate = false;
        }
      }
      return result;
    },

    activate : function() {
      if( this._noMenuOpen() ) {
        var root = rwt.widgets.base.Window.getDefaultWindowManager().getActiveWindow();
        if( root == null ) {
          root = rwt.widgets.base.ClientDocument.getInstance();
        }
        this._active = root.toHashCode();
        this._allowMenuActivate = true;
        this._fire( { "type" : "show" } );
      }
    },

    deactivate : function( allowMenuToggle ) {
      if( this._activeMenu && allowMenuToggle ) {
        this._activeMenu.setActive( false );
        this._activeMenu = null;
      }
      if( this._active ) {
        this._fire( { "type" : "hide" } );
        if( allowMenuToggle ) {
          this._activateMenuBar();
        }
        this._active = null;
      }
    },

    trigger : function( charCode ) {
      var event = {
        "type" : "trigger",
        "charCode" : charCode,
        "success" : false
      };
      this._fire( event, true );
      return event.success;
    },

    _registerFocusRoot : function( root ) {
      if( !this._map[ root.toHashCode() ] ) {
        this._map[ root.toHashCode() ] = { "menu" : {}, "controls" : {} };
        root.addEventListener( "dispose", function() {
          this.deactivate();
          delete this._map[ root.toHashCode() ];
        }, this );
        root.addEventListener( "changeActive", this._onRootChangeActive, this );
      }
    },

    _onRootChangeActive : function( event ) {
      if( event.getValue() === false ) {
        this.deactivate();
      }
    },

    _fire : function( event, onlyVisible ) {
      if( this._map[ this._active ] ) {
        this._doFire( event, onlyVisible, this._map[ this._active ].controls );
        if( !event.success ) {
          this._doFire( event, onlyVisible, this._map[ this._active ].menu );
        }
      }
    },

    _doFire : function( event, onlyVisible, handlers ) {
      for( var key in handlers ) {
        var entry = handlers[ key ];
        if( ( !onlyVisible || entry[ 0 ].isSeeable() ) && entry[ 0 ].getEnabled() ) {
          try{
            entry[ 1 ].call( entry[ 0 ], event );
            if( event.success ) {
              break;
            }
          } catch( ex ) {
            var msg = "Could not handle mnemonic " + event.type + ". ";
            if( entry[ 0 ].isDisposed() ) {
              msg +=  entry[ 0 ].classname + " is disposed. ";
            }
            msg += ex.message;
            throw new Error( msg );
          }
        }
      }
    },

    _activateMenuBar : function() {
      // Accepted limitation: MenuBars without mnemonics can not be activated by mnemonic keys
      if( this._map[ this._active ] ) {
        var items = this._map[ this._active ].menu;
        for( var key in items ) {
          var bar = items[ key ][ 0 ].getParentMenu();
          bar.setMnemonics( true );
          bar.setActive( true );
          this._activeMenu = bar;
          break;
        }
      }
    },

    _checkActiveMenu : function() {
      if( this._activeMenu && !this._activeMenu.getActive() ) {
        this._activeMenu = null;
      }
    },

    /////////
    // Helper

    _isActivation : function( eventType, keyCode, charCode, domEvent ) {
      return    this._activator
             && this._active == null && this._activeMenu == null
             && eventType === "keydown"
             && EventHandlerUtil.isModifier( keyCode )
             && this._isActivatorCombo( domEvent );
    },

    _isDeactivation : function( eventType, keyCode, charCode, domEvent ) {
      return    this._isGlobalDeactivation( eventType, keyCode, charCode, domEvent )
             || this._isMenuDeactivation( eventType, keyCode, charCode, domEvent );
    },

    _isMenuDeactivation : function( eventType, keyCode, charCode, domEvent ) {
      return    this._activator != null
             && this._activeMenu != null
             && EventHandlerUtil.isModifier( keyCode )
             && this._isActivatorCombo( domEvent );
    },

    _isGlobalDeactivation : function( eventType, keyCode, charCode, domEvent ) {
      return    this._activator != null
             && this._active != null
             && eventType != "keypress"
             && !this._isActivatorCombo( domEvent );
    },

    _isActivatorCombo : function( domEvent ) {
      return    this._activator.ctrlKey === domEvent.ctrlKey
             && this._activator.altKey === domEvent.altKey
             && this._activator.shiftKey === domEvent.shiftKey;
    },

    _isTrigger : function( eventType, keyCode ) {
      var isChar = !isNaN( keyCode ) && rwt.event.EventHandlerUtil.isAlphaNumericKeyCode( keyCode );
      return this._active != null && eventType === "keydown" && isChar;
     },

     _noMenuOpen : function() {
       return rwt.util.Objects.isEmpty( rwt.widgets.util.MenuManager.getInstance().getAll() );
     }

  }

} );

}() );
