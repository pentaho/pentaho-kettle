/*******************************************************************************
 * Copyright (c) 2009, 2018 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.Menu", {
  extend : rwt.widgets.base.Popup,
  include : rwt.animation.VisibilityAnimationMixin,

  construct : function() {
    this.base( arguments );
    this._openTimer = new rwt.client.Timer( 250 );
    this._closeTimer = new rwt.client.Timer( 250 );
    this._layout = new rwt.widgets.base.VerticalBoxLayout();
    this._preItem = null;
    this._menuLayoutScheduled = false;
    this._opener = null;
    this._mnemonics = false;
    this._hoverItem = null;
    this._openItem = null;
    this._itemsHiddenFlag = false;
    this.setAppearance( "menu" );
    this._maxCellWidths = [ null, null, null, null, null ];
    this._layout.set( {
      top : 0,
      right : 0,
      bottom : 0,
      left : 0,
      anonymous : true
    } );
    this.add( this._layout );
    this.addEventListener( "mousedown", this._onMouseDown );
    this.addEventListener( "mouseup", this._onMouseUp );
    this.addEventListener( "mouseout", this._onMouseOut );
    this.addEventListener( "mouseover", this._onMouseOver );
    this.addEventListener( "keypress", this._onKeyPress );
    this.addEventListener( "keydown", this._onKeyDown );
    this._openTimer.addEventListener( "interval", this._onOpenTimer, this );
    this._closeTimer.addEventListener( "interval", this._onCloseTimer, this );
    this.addToDocument();
  },

  destruct : function() {
    // needed if menu is disposed while scheduled to be shown (beforeAppear already called):
    rwt.widgets.util.MenuManager.getInstance().remove( this );
    this._makeInactive();
    this._disposeObjects( "_openTimer", "_closeTimer", "_preItem", "_animation" );
    this._disposeFields( "_layout", "_opener", "_hoverItem", "_openItem" );
  },

  statics : {

    menuDetectedByKey : function( evt ) {
      if( evt.getKeyIdentifier() === "Apps" ) {
        rwt.widgets.Menu.contextMenuHandler( evt );
      }
    },

    menuDetectedByMouse : function( evt ) {
      if( evt.getButton() === rwt.event.MouseEvent.C_BUTTON_RIGHT ) {
        rwt.widgets.Menu.contextMenuHandler( evt );
      }
    },

    contextMenuHandler : function( event ) {
      var control = rwt.widgets.util.WidgetUtil.getControl( event.getTarget() );
      var contextMenu = control ? control.getContextMenu() : null;
      if( contextMenu != null ) {
        event.stopPropagation();
        event.preventDefault();
        var pageX = rwt.event.MouseEvent.getPageX();
        var pageY = rwt.event.MouseEvent.getPageY();
        contextMenu.setLocation( pageX, pageY );
        contextMenu.setOpener( control );
        contextMenu.show();
      }
    },

    getAllowContextMenu : function( target, domTarget ) {
      var result = false;
      switch( target.classname ) {
        case "rwt.widgets.Label":
        case "rwt.widgets.Text":
        case "rwt.widgets.base.GridRowContainer":
        case "rwt.widgets.ListItem":
        case "rwt.widgets.base.BasicText":
          // NOTE: "enabled" can be "inherit", so it is not always a boolean
          if( target.getEnabled() !== false ) {
            if( rwt.widgets.Menu._hasNativeMenu( domTarget ) ) {
              var control = rwt.widgets.util.WidgetUtil.getControl( target );
              if( control !== null ) {
                result = control.getContextMenu() == null;
              } else {
                result = target.getContextMenu() == null;
              }
            }
          }
        break;
      }
      return result;
    },


    _hasNativeMenu : function( element ) {
      var result;
      var tagName = typeof element.tagName == "string" ? element.tagName.toLowerCase() : "";
      if( tagName === "a" ) {
        result = element.getAttribute( "href" ) && element.getAttribute( "target" ) !== "_rwt";
      } else {
        result = tagName === "input" || tagName === "textarea";
      }
      return result;
    }

  },

  members : {

    _minZIndex : 1e7,

    /////////
    // Opener

    setOpener : function( value ) {
      this._opener = value;
    },

    getOpener : function() {
      return this._opener;
    },

    setMnemonics : function( value ) {
      if( this._mnemonics !== value ) {
        this._mnemonics = value;
        var items = this._layout.getChildren();
        for( var i = 0; i < items.length; i++ ) {
          if( items[ i ].renderText ) {
            items[ i ].renderText();
          }
        }
      }
    },

    getMnemonics : function() {
      return this._mnemonics;
    },

    /////////
    // Layout

    addMenuItemAt : function( menuItem, index ) {
      // seperator does not have this function:
      if( menuItem.setParentMenu ) {
        // it is essential that this happens before the menuItem is added
        menuItem.setParentMenu( this );
      }
      var position = index;
      if( this._preItem && this._preItem !== menuItem ) {
        position++;
      }
      this._layout.addAt( menuItem, position );
    },

    scheduleMenuLayout : function() {
      if( this._menuLayoutScheduled !== true ) {
        this._menuLayoutScheduled = true;
        var children = this._layout.getChildren();
        var length = children.length;
        for( var i = 0; i < length; i++ ) {
          children[ i ]._invalidatePreferredInnerWidth();
          children[ i ].addToQueue( "layoutX" );
        }
        this.addToQueue( "menuLayout" );
      }
    },

    _layoutPost : function( changes ) {
      this.base( arguments, changes );
      if( changes.menuLayout ) {
        this._menuLayoutScheduled = false;
        if( this.isSeeable() ) {
          this._afterAppear(); // recomputes the location
        }
      }
    },

    getMaxCellWidth : function( cell ) {
      if( this._maxCellWidths[ cell ] == null ) {
        var max = 0;
        var children = this._layout.getChildren();
        var length = children.length;
        for( var i = 0; i < length; i++ ) {
          if( children[ i ].getPreferredCellWidth ) {
            max = Math.max( max, children[ i ].getPreferredCellWidth( cell ) );
          }
        }
        this._maxCellWidths[ cell ] = max;
      }
      if( cell === 0 && this._maxCellWidths[ 0 ] === 0 && this.getMaxCellWidth( 1 ) === 0 ) {
        this._maxCellWidths[ cell ] = 13;
      }
      return this._maxCellWidths[ cell ];
    },

    invalidateMaxCellWidth : function( cell ) {
      this._maxCellWidths[ cell ] = null;
    },

    invalidateAllMaxCellWidths : function() {
      for( var i = 0; i < 5; i++ ) {
        this._maxCellWidths[ i ] = null;
      }
    },

    // needed for the menu-manager:
    isSubElement : function( vElement, vButtonsOnly ) {
      var result = false;
      if (    ( vElement.getParent() === this._layout )
           || ( ( !vButtonsOnly ) && ( vElement === this ) ) ) {
        result = true;
      }
      if( !result ) {
        var a = this._layout.getChildren(), l=a.length;
        for ( var i = 0; i < l; i++ ) {
          if (    this.hasSubmenu( a[ i ] )
               && a[ i ].getMenu().isSubElement( vElement, vButtonsOnly ) )
          {
            result = true;
          }
        }
      }
      return result;
    },

    ////////
    // Hover

    setHoverItem : function( value, fromKeyEvent ) {
      var newHover = value ? value : this._openItem;
      if( this._hoverItem && this._hoverItem != newHover ) {
        this._hoverItem.removeState( "over" );
      }
      if( newHover ) {
        newHover.addState( "over" );
      }
      this._hoverItem = newHover;
      if( !fromKeyEvent ) {
        // handle open timer
        this._openTimer.setEnabled( false );
        if( this.hasSubmenu( newHover ) && ( this._openItem != newHover ) ) {
          this._openTimer.setEnabled( true );
        }
        // handle close timer
        if( this._openItem ) {
          if( this._openItem == newHover || newHover == null ) {
            this._closeTimer.setEnabled( false );
          } else if( newHover != null ) {
            this._closeTimer.setEnabled( true );
          }
        }
      }
      this.dispatchSimpleEvent( "changeHoverItem" );
    },

    getHoverItem : function() {
      return this._hoverItem;
    },

    hoverFirstItem : function( reversed ) {
      if( this._isDisplayable && !this._itemsHiddenFlag ) {
        this.setHoverItem( null, true );
        if( reversed ) {
          this._hoverPreviousItem();
        } else {
          this._hoverNextItem();
        }
      }
      this.toggleState( "hoverFristItem", !this._isDisplayable || this._itemsHiddenFlag );
    },

    _hoverNextItem : function() {
      // About _hoverNext/Previous:
      // the index used for the array of visible children can have
      // "-1" as a valid value (as returned by indexOf), meaning a position
      // between the last and the first item. This is value is needed when no
      // item is hovered or the index-position is wrapping around.
      var current;
      var next = null;
      var children = this._layout.getVisibleChildren();
      var index = children.indexOf( this._hoverItem );
      var startIndex = index;
      do {
        index++;
        if( index > children.length ) {
          index = -1;
        }
        current = index >= 0 ? children[ index ] : null;
        if(   current
           && current.isEnabled()
           && current.classname == "rwt.widgets.MenuItem" )
        {
          next = current;
        }
      } while( !next && ( index != startIndex ) );
      this.setHoverItem( next, true );
    },

    _hoverPreviousItem : function() {
      var current;
      var prev = null;
      var children = this._layout.getVisibleChildren();
      var index = children.indexOf( this._hoverItem );
      var startIndex = index;
      do {
        index--;
        if( index < -1 ) {
          index = children.length;
        }
        current = index >= 0 ? children[ index ] : null;
        if(   current
           && current.isEnabled()
           && current.classname == "rwt.widgets.MenuItem" )
        {
          prev = current;
        }
      } while( !prev && ( index != startIndex ) );
      this.setHoverItem( prev, true );
    },

    //////////////////
    // Pop-Up handling

    // overwritten:
    _makeActive : function() {
      this.setCapture( true );
    },

    // overwritten:
    _makeInactive : function() {
      this.setCapture( false );
    },

    _beforeAppear : function() {
      // original qooxdoo code: (1 line)
      rwt.widgets.base.Parent.prototype._beforeAppear.call( this );
      rwt.widgets.util.MenuManager.getInstance().add( this );
      this.bringToFront();
      this._makeActive();
      this._menuShown();
      rwt.widgets.util.MnemonicHandler.getInstance().deactivate();
    },

    _beforeDisappear : function() {
      // original qooxdoo code: (1 line)
      rwt.widgets.base.Parent.prototype._beforeDisappear.call( this );
      rwt.widgets.util.MenuManager.getInstance().remove( this );
      this._makeInactive();
      this.setOpenItem( null );
      this.setHoverItem( null );
      if( this._opener instanceof rwt.widgets.MenuItem ) {
        var parentMenu = this._opener.getParentMenu();
        if( parentMenu instanceof rwt.widgets.MenuBar ) {
          this._opener.removeState( "pressed" );
          if( parentMenu.getOpenItem() == this._opener ) {
            parentMenu.setOpenItem( null );
          }
        }
      }
      this._menuHidden();
    },

    //////////
    // Submenu

    hasSubmenu : function( item ) {
      return item && item.getMenu && item.getMenu();
    },

   _onOpenTimer : function() {
      this._openTimer.stop();
      this.setOpenItem( this._hoverItem );
      // fix for bug 299350
      this._closeTimer.stop();
    },

    _onCloseTimer : function() {
      this._closeTimer.stop();
      this.setOpenItem( null );
    },

    openByMnemonic : function( item ) {
      this.setOpenItem( item, true );
      this.setHoverItem( null, true );
    },

    setOpenItem : function( item, byMnemonic ) {
      if( this._openItem && this._openItem.getMenu() ) {
        this._openItem.setSubMenuOpen( false );
        var oldMenu = this._openItem.getMenu();
        oldMenu.hide();
        if( this.getVisibility() ) {
          this._makeActive();
        }
      }
      this._openItem = item;
      // in theory an item could have lost it's assigned menu (by eval-code)
      // since the timer has been started/the item opend, so check for it
      if( item && item.getMenu() && item.getEnabled() ) {
        var subMenu = item.getMenu();
        item.setSubMenuOpen( true );
        subMenu.setOpener( item );
        var itemNode = item.getElement();
        var thisNode = this.getElement();
        var thisNodeLeft = rwt.html.Location.getLeft( thisNode );
        // the position is relative to the document, therefore we need helper
        subMenu.setTop( rwt.html.Location.getTop( itemNode ) - 2 );
        if( this.getDirection() === "rtl" ) {
          subMenu.setRight( window.innerWidth - thisNodeLeft - 3 );
        } else {
          subMenu.setLeft( thisNodeLeft + thisNode.offsetWidth - 3 );
        }
        subMenu.setMnemonics( byMnemonic === true );
        subMenu.show();
        if( byMnemonic ) {
          subMenu.hoverFirstItem();
        }
      }
    },

    /////////////////
    // Event-handling

    _onMouseOut : function( event ) {
      var target = event.getOriginalTarget();
      if( this.contains( target ) ) {
        var related = event.getRelatedTarget();
        if( target === this || ( related !== this && !this.contains( related ) ) ) {
          this.setHoverItem( null );
        }
      } else {
        // This is a capture widget, re-dispatch on original
        target._dispatchEvent( event );
        event.stopPropagation();
      }
    },

    _onMouseOver : function( event ) {
      var target = event.getOriginalTarget();
      if( this.contains( target ) ) {
        if( target instanceof rwt.widgets.MenuItem ) {
          this.setHoverItem( target );
        }
        this._unhoverSubMenu();
      } else {
        // This is a capture widget, re-dispatch on original
        target._dispatchEvent( event );
        event.stopPropagation();
      }
    },

    _onMouseDown : function( event ) {
      this._unhoverSubMenu();
      var target = event.getOriginalTarget();
      if( this.contains( target ) ) {
        this.addState( "pressed" );
      } else {
        // This is a capture widget, re-dispatch on original
        rwt.event.EventHandlerUtil.handleFocusedChild( target );
        target._dispatchEvent( event );
        event.stopPropagation();
      }
    },

    _onMouseUp : function( event ) {
      var target = event.getOriginalTarget();
      if( this.contains( target ) ) {
        if( target instanceof rwt.widgets.MenuItem && this.hasState( "pressed" ) ) {
          target.execute();
        }
      } else {
        // This is a capture widget, re-dispatch on original
        target._dispatchEvent( event );
        event.stopPropagation();
      }
    },

    _unhoverSubMenu : function() {
      if( this._openItem ) {
        var subMenu = this._openItem.getMenu();
        subMenu.setOpenItem( null );
        subMenu.setHoverItem( null );
      }
    },

    _onKeyDown :function( event ) {
      if( this._mnemonics ) {
        var keyCode = event.getKeyCode();
        var isChar =    !isNaN( keyCode )
                     && rwt.event.EventHandlerUtil.isAlphaNumericKeyCode( keyCode );
        if( isChar ) {
          var event = {
            "type" : "trigger",
            "charCode" : keyCode,
            "success" : false
          };
          var items = this._layout.getChildren();
          for( var i = 0; i < items.length; i++ ) {
            if( items[ i ].handleMnemonic ) {
              items[ i ].handleMnemonic( event );
            }
          }
        }
      }
    },

    _onKeyPress : function( event ) {
      switch( event.getKeyIdentifier() ) {
        case "Up":
          this._handleKeyUp( event );
        break;
        case "Down":
          this._handleKeyDown( event );
        break;
        case "Left":
          this._handleKeyLeft( event );
        break;
        case "Right":
          this._handleKeyRight( event );
        break;
        case "Enter":
          this._handleKeyEnter( event );
        break;
      }
    },

    _handleKeyUp : function( event ) {
      if( this._openItem ) {
        this._openItem.getMenu()._hoverPreviousItem();
      } else {
        this._hoverPreviousItem();
      }
      event.preventDefault();
      event.stopPropagation();
    },

    _handleKeyDown : function( event ) {
      if( this._openItem ) {
        this._openItem.getMenu()._hoverNextItem();
      } else {
        this._hoverNextItem();
      }
      event.preventDefault();
      event.stopPropagation();
    },

    _handleKeyLeft : function( event ) {
      if( this._opener instanceof rwt.widgets.MenuItem ) {
        var parentMenu = this._opener.getParentMenu();
        if( parentMenu instanceof rwt.widgets.Menu ) {
          var hover = this._opener;
          parentMenu.setOpenItem( null );
          parentMenu.setHoverItem( hover, true );
          event.preventDefault();
          event.stopPropagation();
        }
      }
    },

    _handleKeyRight : function( event ) {
      if( this.hasSubmenu( this._hoverItem ) ) {
        this._onOpenTimer();
        this.setHoverItem( null, true );
        this._openItem.getMenu().hoverFirstItem();
        event.preventDefault();
        event.stopPropagation();
      }
    },

    _handleKeyEnter : function( event ) {
      if( this.hasSubmenu( this._hoverItem ) ) {
        this._onOpenTimer();
        this.setHoverItem( null, true );
        this._openItem.getMenu().hoverFirstItem();
      } else if( this._hoverItem ){
        this._hoverItem.execute();
        rwt.widgets.util.MenuManager.getInstance().update();
      }
      event.preventDefault();
      event.stopPropagation();
    },

    ////////////////
    // Client-Server

    _menuShown : function() {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        var remoteObject = rwt.remote.Connection.getInstance().getRemoteObject( this );
        if( remoteObject.isListening( "Show" ) ) {
          // create preliminary item
          if( this._preItem == null ) {
            this._preItem = new rwt.widgets.MenuItem( "push" );
            this._preItem.setText( "..." );
            this._preItem.setEnabled( false );
            this.addMenuItemAt( this._preItem, 0 );
          }
          // hide all but the preliminary item
          var items = this._layout.getChildren();
          for( var i = 0; i < items.length; i++ ) {
            var item = items[ i ];
            item.setDisplay( false );
          }
          this._preItem.setDisplay( true );
          this._itemsHiddenFlag = true;
          if( this.getWidth() < 60 ) {
            this.setWidth( 60 );
          }
          //this.setDisplay( true ); //wouldn't be called if display was false
          // send event
          remoteObject.notify( "Show" );
        } else {
          var display = this._layout.getChildren().length !== 0;
          //no items and no listener to add some:
          this.setDisplay( display );
        }
      }
    },

    _menuHidden : function() {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        rwt.remote.Connection.getInstance().getRemoteObject( this ).notify( "Hide" );
      }
    },

    unhideItems : function( reveal ) {
      if( reveal ) {
        var items = this._layout.getChildren();
        for( var i = 0; i < items.length; i++ ) {
          items[ i ].setDisplay( true );
        }
        if( this._preItem ) {
          this._preItem.setDisplay( false );
        }
        this._itemsHiddenFlag = false;
      } else {
        this.hide();
      }
    },

    // Called to open a popup menu from server side
    showMenu : function( menu, x, y ) {
      if( menu != null ) {
        menu._renderAppearance();
        if( menu.getDirection() === "rtl" ) {
          menu.setLeft( null );
          menu.setRight( window.innerWidth - x );
        } else {
          menu.setLeft( x );
          menu.setRight( null );
        }
        menu.setTop( y );
        menu.show();
      }
    },

    // needed for the menu-manager:
    isPressed : function() {
      return this.hasState( "pressed" );
    },

    hide : function() {
      this.base( arguments );
      this.removeState( "pressed" );
    }

  }

} );
