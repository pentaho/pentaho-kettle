/*******************************************************************************
 * Copyright (c) 2002, 2016 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.CTabFolder", {

  extend : rwt.widgets.base.Parent,

  construct : function() {
    this.base( arguments );
    this.setTabIndex( 1 );
    this.setHideFocus( true );
    this.setAppearance( "ctabfolder" );
    this.setOverflow( "hidden" );
    this.setEnableElementFocus( false );
    this._tabPosition = "top";
    this._tabHeight = 0;
    this._selectionForeground = null;
    this._selectionBackground = null;
    this._selectionBackgroundImage = null;
    this._selectionBackgroundGradient = null;
    this._chevron = null;
    this._chevronBounds = [ 0, 0, 0, 0 ];
    this._chevronMenu = null;
    this._unselectedCloseVisible = true;
    // Minimize/maximize buttons, initially non-existing
    this._minMaxState = "normal";  // valid states: min, max, normal
    this._maxButton = null;
    this._maxButtonBounds = [ 0, 0, 0, 0 ];
    this._minButton = null;
    this._minButtonBounds = [ 0, 0, 0, 0 ];
    this._body = new rwt.widgets.base.Parent();
    this._body.addState( "barTop" );
    this._body.setAppearance( "ctabfolder-body" );
    this.add( this._body );
    this._separator = new rwt.widgets.base.Parent();
    this._separator.setAppearance( "ctabfolder-separator" );
    this.add( this._separator );
    this._frame = new rwt.widgets.base.Parent();
    this._frame.setAppearance( "ctabfolder-frame" );
    this.add( this._frame );
    this._frameBorder = new rwt.html.Border( 2, "solid", "black" );

    // Create horizontal line that separates the button bar from the rest of
    // the client area
    // Add resize listeners to update selection border (this._highlightXXX)
    this.addEventListener( "changeWidth", this._updateLayout, this );
    this.addEventListener( "changeHeight", this._updateLayout, this );
    // Add keypress listener to select items with left/right keys
    this.addEventListener( "keypress", this._onKeyPress, this );
    this.addEventListener( "contextmenu", this._onContextMenu, this );
  },

  destruct : function() {
    // use hideMin/MaxButton to dispose of toolTips
    this.hideMinButton();
    this.hideMaxButton();
    this.removeEventListener( "changeWidth", this._updateLayout, this );
    this.removeEventListener( "changeHeight", this._updateLayout, this );
    this.removeEventListener( "keypress", this._onKeyPress, this );
    this.removeEventListener( "contextmenu", this._onContextMenu, this );
    this._disposeObjects( "_frame", "_separator" );
    this._frameBorder.dispose();
  },

  statics : {
    BUTTON_SIZE : 18,

    MIN_TOOLTIP : "Minimize",
    MAX_TOOLTIP : "Maximize",
    RESTORE_TOOLTIP : "Restore",
    CHEVRON_TOOLTIP : "Show List",
    CLOSE_TOOLTIP : "Close",

    setToolTipTexts : function( min, max, restore, chevron, close ) {
      rwt.widgets.CTabFolder.MIN_TOOLTIP = min;
      rwt.widgets.CTabFolder.MAX_TOOLTIP = max;
      rwt.widgets.CTabFolder.RESTORE_TOOLTIP = restore;
      rwt.widgets.CTabFolder.CHEVRON_TOOLTIP = chevron;
      rwt.widgets.CTabFolder.CLOSE_TOOLTIP = close;
    }
  },

  members : {

    addState : function( state ) {
      this.base( arguments, state );
      if( state.substr( 0, 8 ) == "variant_" || state.substr( 0, 4 ) == "rwt_" ) {
        this._body.addState( state );
        this._frame.addState( state );
        this._mapItems( function( item ) {
          item.addState( state );
        } );
      }
    },

    removeState : function( state ) {
      this.base( arguments, state );
      if( state.substr( 0, 8 ) == "variant_" || state.substr( 0, 4 ) == "rwt_" ) {
        this._body.removeState( state );
        this._frame.removeState( state );
        this._mapItems( function( item ) {
          item.removeState( state );
        } );
      }
    },

    /* valid values: "top", "bottom" */
    setTabPosition : function( tabPosition ) {
      this._tabPosition = tabPosition;
      this._mapItems( function( item ) {
        item.setTabPosition( tabPosition );
      } );
      this._body.toggleState( "barTop", tabPosition == "top" );
      this._updateLayout();
    },

    /* returns one of: "top", "bottom" */
    getTabPosition : function() {
      return this._tabPosition;
    },

    setTabHeight : function( tabHeight ) {
      this._tabHeight = tabHeight;
      var buttonTop = this._getButtonTop();
      if( this._minButton != null ) {
        this._minButton.setTop( buttonTop );
      }
      if( this._maxButton != null ) {
        this._maxButton.setTop( buttonTop );
      }
      if( this._chevron != null ) {
        this._chevron.setTop( buttonTop );
      }
      this._updateLayout();
    },

    _applyDirection : function( value ) {
      this.base( arguments, value );
      this.getLayoutImpl().setMirror( value === "rtl" );
      if( this._chevron != null ) {
        this._chevron.setDirection( value );
      }
      this._mapItems( function( item ) {
        item.setDirection( value );
      } );
    },

    _applyTextColor : function( value, old ) {
      this.base( arguments, value, old );
      this._mapItems( function( item ) {
        item.updateForeground();
      } );
    },

    _applyBackgroundColor : function( value ) {
      this._body.setBackgroundColor( value );
    },

    _applyBackgroundImage : function( value ) {
      this._body.setUserData( "backgroundImageSize", this.getUserData( "backgroundImageSize" ) );
      this._body.setBackgroundImage( value );
    },

    // transparent not supported, null resets color
    setSelectionForeground : function( color ) {
      this._selectionForeground = color;
      this._mapItems( function( item ) {
        item.updateForeground();
      } );
    },

    // transparent not supported, null resets color
    setSelectionBackground : function( color ) {
      this._selectionBackground = color;
      this._mapItems( function( item ) {
        item.updateBackground();
      } );
      if( color != null && !this.hasState( "rwt_FLAT" ) ) {
        this._frame.setBorder( null );
        this._frameBorder.dispose();
        this._frameBorder = new rwt.html.Border( 2, "solid", color );
        this._frame.setBorder( this._frameBorder );
      } else {
        this._frame.resetBorder();
      }
    },

    setSelectionBackgroundImage : function( image ) {
      this._selectionBackgroundImage = image;
      this._mapItems( function( item ) {
        item.updateBackgroundImage();
      } );
    },

    setSelectionBackgroundGradient : function( gradient ) {
      this._selectionBackgroundGradient = gradient;
      this._mapItems( function( item ) {
        item.updateBackgroundGradient();
      } );
    },

    setUnselectedCloseVisible : function( value ) {
      this._unselectedCloseVisible = value;
      this._mapItems( function( item ) {
        item.updateCloseButton();
      } );
    },

    setBorderVisible : function( visible ) {
      this.toggleState( "rwt_BORDER", visible );
      this._updateLayout();
    },

    getSelectionForeground : function() {
      return this._selectionForeground;
    },

    getSelectionBackground : function() {
      return this._selectionBackground;
    },

    getSelectionBackgroundImage : function() {
      return this._selectionBackgroundImage;
    },

    getSelectionBackgroundGradient : function() {
      return this._selectionBackgroundGradient;
    },

    getUnselectedCloseVisible : function() {
      return this._unselectedCloseVisible;
    },

    _mapItems : function( func ) {
      var children = this.getChildren();
      for( var i = 0; i < children.length; i++ ) {
        if( children[ i ].classname === "rwt.widgets.CTabItem" ) {
          func( children[ i ] );
        }
      }
    },

    _getButtonTop : function() {
      return ( this._tabHeight / 2 ) - ( rwt.widgets.CTabFolder.BUTTON_SIZE / 2 );
    },

    setChevronBounds : function( left, top, width, height ) {
      this._chevronBounds = [ left, top, width, height ];
      if( this._chevron != null ) {
        this._chevron.setSpace( left, width, top, height );
      }
    },

    showChevron : function() {
      if( this._chevron == null ) {
        // Create chevron button
        this._chevron = new rwt.widgets.base.BasicButton( "push", true );
        this._chevron.setAppearance( "ctabfolder-drop-down-button" );
        this._chevron.addEventListener( "execute", this._onChevronExecute, this );
        this._chevron.setDirection( this.getDirection() );
        var wm = rwt.remote.WidgetManager.getInstance();
        wm.setToolTip( this._chevron, rwt.widgets.CTabFolder.CHEVRON_TOOLTIP );
        this.add( this._chevron );
      }
      this._chevron.setLeft( this._chevronBounds[ 0 ] );
      this._chevron.setTop( this._chevronBounds[ 1 ] );
      this._chevron.setWidth( this._chevronBounds[ 2 ] );
      this._chevron.setHeight( this._chevronBounds[ 3 ] );
    },

    hideChevron : function() {
      if( this._chevron != null ) {
        var wm = rwt.remote.WidgetManager.getInstance();
        wm.setToolTip( this._chevron, null );
        this._chevron.removeEventListener( "execute", this._onChevronExecute, this );
        this.remove( this._chevron );
        this._chevron.dispose();
        this._chevron = null;
      }
    },

    setMinMaxState : function( state ) {
      this._minMaxState = state;
      var minIcon = [ null, 10, 10 ];
      var maxIcon = [ null, 10, 10 ];
      var minToolTip = "";
      var maxToolTip = "";
      var path = rwt.remote.Connection.RESOURCE_PATH + "widget/rap/ctabfolder/";
      switch( state ) {
        case "min":
          minIcon[ 0 ] = path + "restore.gif";
          maxIcon[ 0 ] = path + "maximize.gif";
          minToolTip = rwt.widgets.CTabFolder.RESTORE_TOOLTIP;
          maxToolTip = rwt.widgets.CTabFolder.MAX_TOOLTIP;
          break;
        case "max":
          minIcon[ 0 ] = path + "minimize.gif";
          maxIcon[ 0 ] = path + "restore.gif";
          minToolTip = rwt.widgets.CTabFolder.MIN_TOOLTIP;
          maxToolTip = rwt.widgets.CTabFolder.RESTORE_TOOLTIP;
          break;
        case "normal":
          minIcon[ 0 ] = path + "minimize.gif";
          maxIcon[ 0 ] = path + "maximize.gif";
          minToolTip = rwt.widgets.CTabFolder.MIN_TOOLTIP;
          maxToolTip = rwt.widgets.CTabFolder.MAX_TOOLTIP;
          break;
      }
      var wm = rwt.remote.WidgetManager.getInstance();
      if( this._minButton != null ) {
        this._minButton.setIcon( minIcon );
        wm.setToolTip( this._minButton, minToolTip );
      }
      if( this._maxButton != null ) {
        this._maxButton.setIcon( maxIcon );
        wm.setToolTip( this._maxButton, maxToolTip );
      }
    },

    setMaxButtonBounds : function( left, top, width, height ) {
      this._maxButtonBounds = [ left, top, width, height ];
      if( this._maxButton != null ) {
        this._maxButton.setSpace( left, width, top, height );
      }
    },

    showMaxButton : function() {
      if( this._maxButton == null ) {
        this._maxButton = new rwt.widgets.base.BasicButton( "push", true );
        this._maxButton.setAppearance( "ctabfolder-button" );
        this.setMinMaxState( this._minMaxState );  // initializes the icon according to current state
        // [if] "mousedown" is used instead of "execute" because of the bug 247672
        this._maxButton.addEventListener( "mousedown", this._onMinMaxExecute, this );
        this.add( this._maxButton );
      }
      this._maxButton.setLeft( this._maxButtonBounds[ 0 ] );
      this._maxButton.setTop( this._maxButtonBounds[ 1 ] );
      this._maxButton.setWidth( this._maxButtonBounds[ 2 ] );
      this._maxButton.setHeight( this._maxButtonBounds[ 3 ] );
    },

    hideMaxButton : function() {
      if( this._maxButton != null ) {
        this._maxButton.removeEventListener( "mousedown", this._onMinMaxExecute, this );
        var wm = rwt.remote.WidgetManager.getInstance();
        wm.setToolTip( this._maxButton, null );
        this.remove( this._maxButton );
        this._maxButton.dispose();
        this._maxButton = null;
      }
    },

    setMinButtonBounds : function( left, top, width, height ) {
      this._minButtonBounds = [ left, top, width, height ];
      if( this._minButton != null ) {
        this._minButton.setSpace( left, width, top, height );
      }
    },

    showMinButton : function() {
      if( this._minButton == null ) {
        this._minButton = new rwt.widgets.base.BasicButton( "push", true );
        this._minButton.setAppearance( "ctabfolder-button" );
        this.setMinMaxState( this._minMaxState );  // initializes the icon according to current state
        // [if] "mousedown" is used instead of "execute" because of the bug 247672
        this._minButton.addEventListener( "mousedown", this._onMinMaxExecute, this );
        this.add( this._minButton );
      }
      this._minButton.setLeft( this._minButtonBounds[ 0 ] );
      this._minButton.setTop( this._minButtonBounds[ 1 ] );
      this._minButton.setWidth( this._minButtonBounds[ 2 ] );
      this._minButton.setHeight( this._minButtonBounds[ 3 ] );
    },

    hideMinButton : function() {
      if( this._minButton != null ) {
        this._minButton.removeEventListener( "mousedown", this._onMinMaxExecute, this );
        var wm = rwt.remote.WidgetManager.getInstance();
        wm.setToolTip( this._minButton, null );
        this.remove( this._minButton );
        this._minButton.dispose();
        this._minButton = null;
      }
    },

    deselectAll : function() {
      this._mapItems( function( item ) {
        item.setSelected( false );
      } );
    },

    _updateLayout : function() {
      // TODO [rst] take actual border width into account
      var borderWidth = this.hasState( "rwt_BORDER" ) ? 1 : 0;
      var sepBorderWidth = 1;
      var width = this.getWidth() - borderWidth * 2;
      var tabHeight = this._tabHeight + 1;

      this._body.setLeft( 0 );
      this._body.setTop( 0 );
      this._body.setWidth( this.getWidth() );
      this._body.setHeight( this.getHeight() );

      this._separator.setLeft( borderWidth );
      this._separator.setWidth( width );
      this._separator.setHeight( 10 );

      this._frame.setLeft( borderWidth );
      this._frame.setWidth( width );
      this._frame.setHeight( this.getHeight() - borderWidth - sepBorderWidth - tabHeight );

      if( this._tabPosition == "top" ) {
        this._separator.setTop( tabHeight );
        this._frame.setTop( tabHeight + 1 );
      } else { // tabPosition == "bottom"
        this._separator.setTop( this.getHeight() - tabHeight - 1 );
        this._frame.setTop( borderWidth );
      }
    },

    _onChevronExecute : function() {
      if( this._chevronMenu == null || !this._chevronMenu.isSeeable() ) {
        if( !rwt.remote.EventUtil.getSuspended() ) {
          var server = rwt.remote.Connection.getInstance();
          server.getRemoteObject( this ).notify( "Folder", { "detail" : "showList" } );
        }
      }
    },

    _onMinMaxExecute : function( evt ) {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        var detail;
        if ( evt.getTarget() == this._minButton ) {
          // Minimize button was pressed
          if( this._minMaxState == "min" ) {
            this.setMinMaxState( "normal" );
            detail = "restore";
          } else {
            this.setMinMaxState( "min" );
            detail = "minimize";
          }
        } else {
          // Maximize button was pressed
          if( this._minMaxState == "normal" || this._minMaxState == "min" ) {
            this.setMinMaxState( "max" );
            detail = "maximize";
          } else {
            this.setMinMaxState( "normal" );
            detail = "restore";
          }
        }
        var remoteObject = rwt.remote.Connection.getInstance().getRemoteObject( this );
        remoteObject.set( "minimized", this._minMaxState == "min" );
        remoteObject.set( "maximized", this._minMaxState == "max" );
        remoteObject.notify( "Folder", { "detail" : detail } );
      }
    },

    _onKeyPress : function( evt ) {
      switch( evt.getKeyIdentifier() ) {
        case "Left":
          // TODO [rh] implementation missing: select tab item to the left
          evt.stopPropagation();
          break;
        case "Right":
          // TODO [rh] implementation missing: select tab item to the right
          evt.stopPropagation();
          break;
      }
    },

    _onContextMenu : function( evt ) {
      var menu = this.getContextMenu();
      if( menu != null ) {
        menu.setLocation( evt.getPageX(), evt.getPageY() );
        menu.setOpener( this );
        menu.show();
        evt.stopPropagation();
      }
    },

    _notifySelection : function( item ) {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        if( !item.isSelected() ) {
          this.deselectAll();
          item.setSelected( true );
          var itemId = rwt.remote.ObjectRegistry.getId( item );
          rwt.remote.Connection.getInstance().getRemoteObject( this ).set( "selection", itemId );
          rwt.remote.EventUtil.notifySelected( this, { "item" : itemId } );
        }
      }
    },

    _notifyDefaultSelection : function( item ) {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        var itemId = rwt.remote.ObjectRegistry.getId( item );
        rwt.remote.EventUtil.notifyDefaultSelected( this, { "item" : itemId } );
      }
    }
  }
} );
