/*******************************************************************************
 * Copyright (c) 2004, 2016 1&1 Internet AG, Germany, http://www.1und1.de,
 *                          EclipseSource, and others.
 *
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
 * This is the basic widget of all qooxdoo applications.
 *
 * rwt.widgets.base.ClientDocument is the parent of all children inside your application. It
 * also handles their resizing and focus navigation.
 *
 * @appearance client-document
 */
rwt.qx.Class.define( "rwt.widgets.base.ClientDocument", {

  extend : rwt.widgets.base.Parent,

  statics : {

    getInstance : function() {
      return rwt.runtime.Singletons.get( rwt.widgets.base.ClientDocument );
    }

  },

  construct : function() {
    this.base( arguments );
    this._window = window;
    this._document = window.document;
    // init element
    this.setElement( this._document.body );
    this.getElement().setAttribute( "spellcheck", "false" );
    // reset absolute position
    this._document.body.style.position = "";
    // cache current size
    this._cachedInnerWidth = this._document.body.offsetWidth;
    this._cachedInnerHeight = this._document.body.offsetHeight;
    // add resize handler
    this.addEventListener( "windowresize", this._onwindowresize );
    // dialog support
    this._modalWidgets = [];
    // enable as focus root behavior
    this.activateFocusRoot();
    // initialize properties
    this.initSelectable();
    // register as current focus root
    rwt.event.EventHandler.setFocusRoot( this );
    // Gecko-specific settings
    if( rwt.client.Client.isGecko() ) {
      // Fix for bug 193703:
      this.getElement().style.position = "absolute";
      this.setSelectable( true );
    }
    var resetScrollPosition = rwt.util.Functions.bind( this._resetScrollPosition, this );
    this._document.documentElement.onscroll = resetScrollPosition;
    this.getElement().onscroll = resetScrollPosition;
  },

  properties : {

    appearance : {
      refine : true,
      init : "client-document"
    },

    enableElementFocus : {
      refine : true,
      init : false
    },

    enabled : {
      refine : true,
      init : true
    },

    selectable : {
      refine : true,
      init : false
    },

    hideFocus : {
      refine : true,
      init : true
    },

    /**
     * Sets the global cursor style
     *
     * The name of the cursor to show when the mouse pointer is over the widget.
     * This is any valid CSS2 cursor name defined by W3C.
     *
     * The following values are possible:
     * - default
     * - crosshair
     * - pointer (hand is the ie name and will mapped to pointer in non-ie)
     * - move
     * - n-resize
     * - ne-resize
     * - e-resize
     * - se-resize
     * - s-resize
     * - sw-resize
     * - w-resize
     * - nw-resize
     * - text
     * - wait
     * - help
     * - url([file]) = self defined cursor, file should be an ANI- or CUR-type
     */
    globalCursor : {
      check : "String",
      nullable : true,
      themeable : true,
      apply : "_applyGlobalCursor",
      event : "changeGlobalCursor"
    }
  },

  members : {

    // ------------------------------------------------------------------------
    // OVERWRITE WIDGET FUNCTIONS/PROPERTIES
    // ------------------------------------------------------------------------

    _applyParent : rwt.util.Functions.returnTrue,

    _applyOverflow : function( value ) {
      var property = "overflow";
      if( value === "scrollX" ) {
        property = "overflowX";
      } else if( value === "scrollY" ) {
        property = "overflowY";
      }
      this._document.documentElement.style[ property ] = value === "hidden" ? value : "auto";
      this.getElement().style[ property ] = value === "hidden" ? value : "auto";
    },

    getTopLevelWidget : function() {
      return this;
    },

    getWindowElement : function() {
      return this._window;
    },

    getDocumentElement : function() {
      return this._document;
    },

    getParent : rwt.util.Functions.returnNull,

    getToolTip : rwt.util.Functions.returnNull,

    isSeeable : rwt.util.Functions.returnTrue,

    _isDisplayable : true,

    _hasParent : false,

    _initialLayoutDone : true,

    _isInDom : true,

    // ------------------------------------------------------------------------
    // BLOCKER AND DIALOG SUPPORT
    // ------------------------------------------------------------------------

    /**
     * Returns the blocker widget if already created; otherwise create it first
     *
     * @return {ClientDocumentBlocker} the blocker widget.
     */
    _getBlocker : function() {
      if( !this._blocker ) {
        // Create blocker instance
        this._blocker = new rwt.widgets.base.ClientDocumentBlocker();
        this._blocker.setStyleProperty( "position", "fixed" );
        // Add blocker to client document
        this.add( this._blocker );
      }
      return this._blocker;
    },

    block : function( vActiveChild ) {
      this._getBlocker().show();
      if( rwt.qx.Class.isDefined( "rwt.widgets.base.Window" )
          && vActiveChild instanceof rwt.widgets.base.Window )
      {
        this._modalWidgets.push( vActiveChild );
        var vOrigIndex = vActiveChild.getZIndex();
        this._getBlocker().setZIndex( vOrigIndex );
        vActiveChild.setZIndex( vOrigIndex + 1 );
      }
    },

    release : function( vActiveChild ) {
      if( vActiveChild ) {
        rwt.util.Arrays.remove( this._modalWidgets, vActiveChild );
      }
      var l = this._modalWidgets.length;
      if( l === 0 ) {
        this._getBlocker().hide();
      } else {
        var oldActiveChild = this._modalWidgets[ l - 1 ];
        var old = oldActiveChild.getZIndex();
        this._getBlocker().setZIndex( old );
        oldActiveChild.setZIndex( old + 1 );
      }
    },

    _resetScrollPosition : function() {
      var overflow = this.getOverflow();
      if( overflow !== "scroll" && overflow !== "scrollX" ) {
        document.documentElement.scrollLeft = 0;
        document.body.scrollLeft = 0;
      }
      if( overflow !== "scroll" && overflow !== "scrollY" ) {
        document.documentElement.scrollTop = 0;
        document.body.scrollTop = 0;
      }
    },

    // CSS API

    createStyleElement : function( vCssText ) {
      return rwt.html.StyleSheet.createElement( vCssText );
    },

    addCssRule : function( vSheet, vSelector, vStyle ) {
      return rwt.html.StyleSheet.addRule( vSheet, vSelector, vStyle );
    },

    removeCssRule : function( vSheet, vSelector ) {
      return rwt.html.StyleSheet.removeRule( vSheet, vSelector );
    },

    removeAllCssRules : function( vSheet ) {
      return rwt.html.StyleSheet.removeAllRules( vSheet );
    },

    // ------------------------------------------------------------------------
    // GLOBAL CURSOR SUPPORT
    // ------------------------------------------------------------------------

    _applyGlobalCursor : function( value ) {
      if( !this._globalCursorStyleSheet ) {
        this._globalCursorStyleSheet = this.createStyleElement();
      }
      this.removeCssRule( this._globalCursorStyleSheet, "*" );
      if( value ) {
        this.addCssRule( this._globalCursorStyleSheet, "*", "cursor:" + value + " !important" );
      }
    },

    // ------------------------------------------------------------------------
    // WINDOW RESIZE HANDLING
    // ------------------------------------------------------------------------

    _onwindowresize : function() {
      rwt.widgets.util.PopupManager.getInstance().update();
      this._recomputeInnerWidth();
      this._recomputeInnerHeight();
      rwt.widgets.base.Widget.flushGlobalQueues();
    },

    _computeInnerWidth : function() {
      return this._document.body.offsetWidth;
    },

    _computeInnerHeight : function() {
      return this._document.body.offsetHeight;
    }

  },

  defer : function() {
    // CSS fix
    var boxSizingAttr = rwt.client.Client.getEngineBoxSizingAttributes();
    var borderBoxCss = boxSizingAttr.join( ":border-box;" ) + ":border-box;";
    var contentBoxCss = boxSizingAttr.join( ":content-box;" ) + ":content-box;";
    rwt.html.StyleSheet.createElement(
      "html,body { margin:0;border:0;padding:0; } " +
      "html { border:0 none; } " +
      "*{" + borderBoxCss +"} " +
      "img{" + contentBoxCss + "}"
    );
    rwt.html.StyleSheet.createElement( "html,body{width:100%;height:100%;overflow:hidden;}" );
    rwt.widgets.base.ClientDocument.BOXSIZING = "border-box";
  },

  destruct : function() {
    this._disposeObjects( "_blocker" );
    this._disposeFields( "_window", "_document", "_modalWidgets", "_globalCursorStyleSheet" );
  }

} );
