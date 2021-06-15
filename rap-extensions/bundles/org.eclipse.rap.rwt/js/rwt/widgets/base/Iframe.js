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
 * Container widget for internal frames (iframes).
 *
 * An iframe can display any HTML page inside the widget.
 *
 * @appearance iframe
 */
rwt.qx.Class.define("rwt.widgets.base.Iframe",
{
  extend : rwt.widgets.base.Terminator,




  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  /**
   * @param vSource {String?null} URL of the HTML page displayed in the iframe.
   */
  construct : function(vSource)
  {
    this.base(arguments);

    this.initSelectable();
    this.initTabIndex();
    this.initScrolling();

    if (vSource != null) {
      this.setSource(vSource);
    }
  },


  /*
  *****************************************************************************
     EVENTS
  *****************************************************************************
  */

  events:
  {
    /**
     * The "load" event is fired after the iframe content has successfully been loaded.
     */
    "load" : "rwt.event.Event"
  },





  /*
  *****************************************************************************
     STATICS
  *****************************************************************************
  */

  statics :
  {
    load : function( obj ) {
      try {
        if( !obj ) {
          throw new Error("Could not find iframe which was loaded [A]!");
        }
        if( obj.currentTarget ) {
          obj = obj.currentTarget;
        }
        // Find iframe instance and call onload
        if( obj._QxIframe ) {
          obj._QxIframe._onload();
        } else if( obj.parentNode ) { // Check for parentNode necessary, see Bug 346064
          throw new Error("Could not find iframe which was loaded [B]!");
        }
      } catch( ex ) {
        rwt.runtime.ErrorHandler.processJavaScriptError( ex );
      }
    },

    _useAlternateLayouting : function() {
      return rwt.client.Client.isMobileSafari();
    }

  },




  /*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

  properties :
  {
    tabIndex :
    {
      refine : true,
      init : 0
    },

    selectable :
    {
      refine : true,
      init : false
    },

    appearance :
    {
      refine : true,
      init : "iframe"
    },

    /**
     * Source URL of the iframe.
     */
    source :
    {
      check : "String",
      apply : "_applySource",
      event : "changeSource",
      nullable : true
    },

    /**
     * Name of the iframe.
     */
    frameName :
    {
      check : "String",
      init : "",
      apply : "_applyFrameName"
    },


    /** Whether the iframe's content pane should have scroll bars */
    scrolling :
    {
      check : ["yes", "no", "auto"],
      init  : "auto",
      apply : "_applyScrolling"
    }
  },




  /*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */

  members :
  {
    /**
     * Get the DOM element of the iframe.
     *
     * @type member
     * @return {Element} The DOM element of the iframe.
     */
    getIframeNode : function() {
      return this._iframeNode;
    },


    /**
     * Change the DOM element of the iframe.
     *
     * @type member
     * @param vIframeNode {Element} The new DOM element of the iframe.
     */
    setIframeNode : function(vIframeNode) {
      /*jshint boss: true */
      return this._iframeNode = vIframeNode;
    },


    /**
     * TODOC
     *
     * @type member
     * @return {var} TODOC
     */
    getBlockerNode : function() {
      return this._blockerNode;
    },


    /**
     * TODOC
     *
     * @type member
     * @param vBlockerNode {var} TODOC
     * @return {var} TODOC
     */
    setBlockerNode : function(vBlockerNode) {
      /*jshint boss: true */
      return this._blockerNode = vBlockerNode;
    },




    /*
    ---------------------------------------------------------------------------
      WINDOW & DOCUMENT ACCESS
    ---------------------------------------------------------------------------
    */

    /**
     * Get the DOM window object of the iframe.
     *
     * @type member
     * @return {DOMWindow} The DOM window object of the iframe.
     */
    getContentWindow : function()
    {
      if (this.isCreated()) {
        return rwt.html.Iframes.getWindow(this.getIframeNode());
      } else {
        return null;
      }
    },


    /**
     * Get the DOM document object of the iframe.
     *
     * @type member
     * @return {DOMDocument} The DOM document object of the iframe.
     */
    getContentDocument : function()
    {
      if (this.isCreated()) {
        return rwt.html.Iframes.getDocument(this.getIframeNode());
      } else {
        return null;
      }
    },

    /**
     * @signature function()
     */
    isLoaded : function() {
      return this._isLoaded;
    },

    /*
    ---------------------------------------------------------------------------
      METHODS
    ---------------------------------------------------------------------------
    */

    /**
     * Reload the contents of the iframe.
     *
     * @type member
     */
    reload : function()
    {
      if (this.isCreated() && this.getContentWindow())
      {
        this._isLoaded = false;

        var currentSource = this.queryCurrentUrl() || this.getSource();

        try
        {
          /*
          Some gecko users might have an exception here:
            Exception... "Component returned failure code: 0x805e000a
            [nsIDOMLocation.replace]"  nsresult: "0x805e000a (<unknown>)"
          */
          try
          {
            this.getContentWindow().location.replace(currentSource);
          }
          catch(ex)
          {
            this.getIframeNode().src = currentSource;
          }
        }
        catch(ex) {
          throw new Error( "Iframe source could not be set! This may be related to AdBlock Plus Firefox Extension." );
        }
      }
    },


    /**
     * Returns the current (served) URL inside the iframe
     *
     * @return {String} Returns the location href or null (if a query is not possible/allowed)
     */
    queryCurrentUrl : function()
    {
      var doc = this.getContentDocument();

      try
      {
        if (doc && doc.location) {
          return doc.location.href;
        }
      }
      catch(ex) {}

      return null;
    },


    /**
     * Cover the iframe with a transparent blocker div element. This prevents
     * mouse or key events to be handled by the iframe. To release the blocker
     * use {@link #release}.
     *
     * @type member
     */
    block : function()
    {
      if (this._blockerNode &&
         (!this._blockerNode.parentElement ||
         (rwt.client.Client.isGecko() && !this._blockerNode.parentNode))) {
        this._getBlockerParent().appendChild(this._blockerNode);
      }
    },


    /**
     * Release the blocker set by {@link #block}.
     *
     * @type member
     */
    release : function()
    {
      if (this._blockerNode &&
         (this._blockerNode.parentElement ||
         (rwt.client.Client.isGecko() && this._blockerNode.parentNode))) {
        this._getBlockerParent().removeChild(this._blockerNode);
      }
    },


    /**
     * Get the parent element of the blocker node. Respects extended border
     * elements.
     *
     * @return {Element} the blocker's parent element
     */
    _getBlockerParent : function()
    {
      var el = this.getElement();
      if (this._innerStyle) {
        return el.firstChild;
      } else {
        return el;
      }
    },



    /*
    ---------------------------------------------------------------------------
      ELEMENT HELPER
    ---------------------------------------------------------------------------
    */

    /**
     * Creates an template iframe element and sets all required html and style properties.
     *
     * @type static
     * @param vFrameName {String} Name of the iframe.
     */
    _generateIframeElement : function()
    {
      var frameEl = this._createIframeNode( this.getFrameName() );

      frameEl._QxIframe = this;

      frameEl.frameBorder = "0";
      frameEl.frameSpacing = "0";

      frameEl.marginWidth = "0";
      frameEl.marginHeight = "0";

      if( !rwt.widgets.base.Iframe._useAlternateLayouting() ) {
        frameEl.width = "100%";
        frameEl.height = "100%";
      }

      frameEl.hspace = "0";
      frameEl.vspace = "0";

      frameEl.border = "0";
      frameEl.unselectable = "on";
      frameEl.allowTransparency = "true";

      frameEl.style.position = "absolute";
      frameEl.style.top = 0;
      frameEl.style.left = 0;

      return frameEl;
    },

    _createIframeNode : function( frameName ) {
      var frameEl = rwt.widgets.base.Iframe._element = document.createElement("iframe");
      frameEl.onload = rwt.widgets.base.Iframe.load;
      if (frameName) {
        frameEl.name = frameName;
      }
      return frameEl;
    },


    /**
     * TODOC
     *
     * @type static
     * @return {void}
     */
    _generateBlockerElement : function()
    {
      var blockerEl = rwt.widgets.base.Iframe._blocker = document.createElement("div");
      var blockerStyle = blockerEl.style;

      blockerStyle.position = "absolute";
      blockerStyle.top = 0;
      blockerStyle.left = 0;
      blockerStyle.width = "100%";
      blockerStyle.height = "100%";
      blockerStyle.zIndex = 1;
      // IE 9/10 needs a background image in order to capture mouse events
      // 441842: [Browser] Disabled state does not work in IE10
      // https://bugs.eclipse.org/bugs/show_bug.cgi?id=441842
      if( rwt.client.Client.isTrident() && rwt.client.Client.getVersion() < 11 ) {
        var blank = rwt.remote.Connection.RESOURCE_PATH + "static/image/blank.gif";
        blockerStyle.backgroundImage = "url(" + blank + ")";
      }

      return blockerEl;
    },






    /*
    ---------------------------------------------------------------------------
      APPLY ROUTINES
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyElement : function(value, old)
    {
      var iframeNode = this.setIframeNode(this._generateIframeElement());
      this.setBlockerNode(this._generateBlockerElement());

      this._syncSource();
      this._syncScrolling();

      value.appendChild(iframeNode);

      this.base(arguments, value, old);
    },


    /**
     * TODOC
     *
     * @type member
     * @return {void}
     */
    _beforeAppear : function()
    {
      this.base(arguments);

      // register to iframe manager as active widget
      rwt.widgets.util.IframeManager.getInstance().add(this);
    },


    /**
     * TODOC
     *
     * @type member
     * @return {void}
     */
    _beforeDisappear : function()
    {
      this.base(arguments);

      // deregister from iframe manager
      rwt.widgets.util.IframeManager.getInstance().remove(this);
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applySource : function()
    {
      if (this.isCreated()) {
        this._syncSource();
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @return {void}
     */
    _syncSource : function()
    {
      var currentSource = this.getSource();

      if (currentSource == null || currentSource === "") {
        currentSource = rwt.remote.Connection.RESOURCE_PATH + "static/html/blank.html";
      }

      this._isLoaded = false;

      try
      {
        // the guru says ...
        // it is better to use 'replace' than 'src'-attribute, since 'replace' does not interfer
        // with the history (which is taken care of by the history manager), but there
        // has to be a loaded document
        if (this.getContentWindow())
        {
          /*
          Some gecko users might have an exception here:
            Exception... "Component returned failure code: 0x805e000a
            [nsIDOMLocation.replace]"  nsresult: "0x805e000a (<unknown>)"
          */
          try
          {
            this.getContentWindow().location.replace(currentSource);
          }
          catch(ex)
          {
            this.getIframeNode().src = currentSource;
          }
        }
        else
        {
          this.getIframeNode().src = currentSource;
        }
      }
      catch(ex) {
        throw new Error( "Iframe source could not be set! This may be related to AdBlock Plus Firefox Extension." );
      }
    },


    // property apply
    _applyScrolling : function()
    {
      if (this.isCreated()) {
        this._syncScrolling();
      }
    },


    /**
     * Sync scrolling property to the iframe DOM node.
     *
     * @type member
     */
    _syncScrolling : function() {
      this.getIframeNode().setAttribute("scrolling", this.getScrolling());
    },


    _applyFrameName : function()
    {
      if (this.isCreated()) {
        throw new Error("Not allowed to set frame name after it has been created");
      }
    },




    /*
    ---------------------------------------------------------------------------
      EVENT HANDLER
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @return {void}
     */
    _onload : function()
    {
      if (!this._isLoaded)
      {
        this._isLoaded = true;
        this.createDispatchEvent("load");
      }
    },


    /*
    ---------------------------------------------------------------------------
      LOAD STATUS
    ---------------------------------------------------------------------------
    */

    _isLoaded : false
  },


  defer : function( statics, members ) {
    if( rwt.widgets.base.Iframe._useAlternateLayouting() ) {
      var originalRenderWidth = members._renderRuntimeWidth;
      var originalRenderHeight = members._renderRuntimeHeight;
      var originalResetWidth = members._resetRuntimeWidth;
      var originalResetHeight = members._resetRuntimeHeight;
      members._renderRuntimeWidth = function( value ) {
        originalRenderWidth.call( this, value );
        this._iframeNode.style.minWidth = value + "px";
        this._iframeNode.style.maxWidth = value + "px";
      };
      members._renderRuntimeHeight = function( value ) {
        originalRenderHeight.call( this, value );
        this._iframeNode.style.minHeight = value + "px";
        this._iframeNode.style.maxHeight = value + "px";
      };
      members._resetRuntimeWidth = function( value ) {
        originalResetWidth.call( this, value );
        this._iframeNode.style.minWidth = "";
        this._iframeNode.style.maxWidth = "";
      };
      members._resetRuntimeHeight = function( value ) {
        originalResetHeight.call( this, value );
        this._iframeNode.style.minHeight = "";
        this._iframeNode.style.maxHeight = "";
      };
    }
  },

  /*
  *****************************************************************************
     DESTRUCTOR
  *****************************************************************************
  */

  destruct : function()
  {
    if (this._iframeNode)
    {
      this._iframeNode._QxIframe = null;
      this._iframeNode.onload = null;
      this._iframeNode = null;
      this._getTargetNode().innerHTML = "";
    }
    this._disposeFields("__onload", "_blockerNode");
  }
});
