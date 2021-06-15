/*******************************************************************************
 * Copyright (c) 2002, 2018 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.ScrolledComposite", {

  extend : rwt.widgets.base.Scrollable,

  construct : function() {
    this.base( arguments, new rwt.widgets.base.Parent() );
    this.setScrollBarsVisible( false, false );
    this._clientArea.addEventListener( "mousewheel", this._onMouseWheel, this );
    this._clientArea.addEventListener( "keypress", this._onKeyPress, this );
    if( rwt.client.Client.supportsTouch() ) {
      this._clientArea.addEventListener( "mousedown", this._onTouch, this );
    }
    this.addEventListener( "userScroll", this._onUserScroll );
    this._content = null;
    this._requestTimerRunning = false;
    this._showFocusedControl = false;
    this._focusRoot = null;
    this.addEventListener( "changeParent", this._onChangeParent, this );
    this.setAppearance( "scrolledcomposite" );
    this.setEnableElementFocus( false );
  },

  members : {

    /////////
    // Public

    setShowFocusedControl : function( value ) {
      this._showFocusedControl = value;
    },

    setContent : function( widget ) {
      if( this._content != null ) {
        this._content.removeEventListener( "changeParent", this._onContentRemove, this );
        this._content.removeEventListener( "changeWidth", this._onContentResize, this );
        this._content.removeEventListener( "changeHeight", this._onContentResize, this );
        this._clientArea.remove( this._content );
      }
      this._content = widget;
      this._onContentResize();
      if( this._content != null ) {
        this._clientArea.add( this._content );
        this._content.addEventListener( "changeParent", this._onContentRemove, this );
        this._content.addEventListener( "changeWidth", this._onContentResize, this );
        this._content.addEventListener( "changeHeight", this._onContentResize, this );
      }
    },

    ///////////////
    // Eventhandler

    _onContentRemove : function() {
      this.setContent( null );
    },

    _onContentResize : function() {
      if(    this._content !== null
          && typeof this._content.getWidth() === "number"
          && typeof this._content.getHeight() === "number" )
      {
        var maxWidth = this._content.getWidth();
        var maxHeight = this._content.getHeight();
        this._horzScrollBar.setMaximum( maxWidth );
        this._vertScrollBar.setMaximum( maxHeight );
      }
    },

    _onChangeParent : function() {
      if( this._focusRoot != null ) {
        this._focusRoot.removeEventListener( "changeFocusedChild",
                                             this._onChangeFocusedChild,
                                             this );
      }
      this._focusRoot = this.getFocusRoot();
      if( this._focusRoot != null ) {
        this._focusRoot.addEventListener( "changeFocusedChild",
                                          this._onChangeFocusedChild,
                                          this );
      }
    },

    _onMouseWheel : function() {
      this.setBlockScrolling( false );
    },

    _onTouch : function() {
      this.setBlockScrolling( false );
    },

    _onKeyPress : function( evt ) {
      switch( evt.getKeyIdentifier() ) {
        case "Left":
        case "Up":
        case "Right":
        case "Down":
        case "PageUp":
        case "PageDown":
        case "End":
        case "Home":
          this.setBlockScrolling( false );
          evt.stopPropagation();
        break;
      }
    },

    _onUserScroll : function( horizontal ) {
      var server = rwt.remote.Connection.getInstance();
      var scrollbar = horizontal ? this._horzScrollBar : this._vertScrollBar;
      var remoteObject = server.getRemoteObject( this );
      var prop = horizontal ? "horizontalBar.selection" : "verticalBar.selection";
      remoteObject.set( prop, scrollbar.getValue() );
      if( scrollbar.getHasSelectionListener() ) {
        if( horizontal ) {
          server.onNextSend( this._sendHorizontalScrolled, this );
        } else {
          server.onNextSend( this._sendVerticalScrolled, this );
        }
        server.sendDelayed( 500 );
      }
    },

    _sendVerticalScrolled : function() {
      var server = rwt.remote.Connection.getInstance();
      server.getRemoteObject( this._vertScrollBar ).notify( "Selection" );
    },

    _sendHorizontalScrolled : function() {
      var server = rwt.remote.Connection.getInstance();
      server.getRemoteObject( this._horzScrollBar ).notify( "Selection" );
    },

    _onChangeFocusedChild : function( evt ) {
      var focusedChild = evt.getValue();
      var mouseFocus = rwt.widgets.util.FocusHandler.mouseFocus;
      // Workaround for bug 528481
      if( mouseFocus && focusedChild instanceof rwt.widgets.Button ) {
        this.setBlockScrolling( true );
      } else {
        this.setBlockScrolling( !this._showFocusedControl && focusedChild !== this );
      }
    }

  }

} );
