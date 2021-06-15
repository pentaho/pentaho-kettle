/*******************************************************************************
 * Copyright (c) 2004, 2019 1&1 Internet AG, Germany, http://www.1und1.de,
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
 * @state active
 * @state maximized This state is active if the window is maximized
 *
 * @appearance window The main window object
 * @appearance window-resize-frame {rwt.widgets.base.Terminator}
 * @appearance window-captionbar-icon {rwt.widgets.base.Image}
 * @appearance window-captionbar-title {rwt.widgets.base.Label} The label of the caption bar
 * @appearance window-captionbar-minimize-button {rwt.widgets.base.BasicButton}
 * @appearance window-captionbar-restore-button {rwt.widgets.base.BasicButton}
 * @appearance window-captionbar-maximize-button {rwt.widgets.base.BasicButton}
 * @appearance window-captionbar-close-button {rwt.widgets.base.BasicButton}
 * @appearance window-statusbar {rwt.widgets.base.HorizontalBoxLayout}
 * @appearance window-statusbar-text {rwt.widgets.base.Label}
 *
 * @appearance window-captionbar {rwt.widgets.base.HorizontalBoxLayout}
 * @state active {window-captionbar}
 */
rwt.qx.Class.define("rwt.widgets.base.Window",
{
  extend : rwt.widgets.base.ResizablePopup,



  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  construct : function(vCaption, vIcon, vWindowManager)
  {
    this.base(arguments);

    // ************************************************************************
    //   MANAGER
    // ************************************************************************
    // Init Window Manager
    this.setWindowManager(vWindowManager || rwt.widgets.base.Window.getDefaultWindowManager());

    // ************************************************************************
    //   LAYOUT
    // ************************************************************************
    var l = this._layout = new rwt.widgets.base.VerticalBoxLayout();
    l.setEdge(0);
    this.add(l);

    // ************************************************************************
    //   CAPTIONBAR
    // ************************************************************************
    var cb = this._captionBar = new rwt.widgets.base.HorizontalBoxLayout();
    cb.setAppearance("window-captionbar");
    cb.setHeight("auto");
    cb.setOverflow("hidden");
    l.add(cb);

    // ************************************************************************
    //   CAPTIONICON
    // ************************************************************************
    var ci = this._captionIcon = new rwt.widgets.base.Image();
    ci.setAppearance("window-captionbar-icon");
    cb.add(ci);

    // ************************************************************************
    //   CAPTIONTITLE
    // ************************************************************************
    var ct = this._captionTitle = new rwt.widgets.base.Label(vCaption);
    ct.setAppearance("window-captionbar-title");
    ct.setSelectable(false);
    cb.add(ct);

    // ************************************************************************
    //   CAPTIONFLEX
    // ************************************************************************
    var cf = this._captionFlex = new rwt.widgets.base.HorizontalSpacer();
    cb.add(cf);

    // ************************************************************************
    //   CAPTIONBUTTONS: MINIMIZE
    // ************************************************************************
    var bm = this._minimizeButton = new rwt.widgets.base.BasicButton( "push", true );

    bm.setAppearance("window-captionbar-minimize-button");
    bm.setTabIndex(null);

    bm.addEventListener("execute", this._onminimizebuttonclick, this);
    bm.addEventListener("mousedown", this._onbuttonmousedown, this);

    cb.add(bm);

    // ************************************************************************
    //   CAPTIONBUTTONS: RESTORE
    // ************************************************************************
    var br = this._restoreButton = new rwt.widgets.base.BasicButton( "push", true );

    br.setAppearance("window-captionbar-restore-button");
    br.setTabIndex(null);

    br.addEventListener("execute", this._onrestorebuttonclick, this);
    br.addEventListener("mousedown", this._onbuttonmousedown, this);

    // don't add initially
    // cb.add(br);
    // ************************************************************************
    //   CAPTIONBUTTONS: MAXIMIZE
    // ************************************************************************
    var bx = this._maximizeButton = new rwt.widgets.base.BasicButton( "push", true );

    bx.setAppearance("window-captionbar-maximize-button");
    bx.setTabIndex(null);

    bx.addEventListener("execute", this._onmaximizebuttonclick, this);
    bx.addEventListener("mousedown", this._onbuttonmousedown, this);

    cb.add(bx);

    // ************************************************************************
    //   CAPTIONBUTTONS: CLOSE
    // ************************************************************************
    var bc = this._closeButton = new rwt.widgets.base.BasicButton( "push", true );

    bc.setAppearance("window-captionbar-close-button");
    bc.setTabIndex(null);

    bc.addEventListener("execute", this._onclosebuttonclick, this);
    bc.addEventListener("mousedown", this._onbuttonmousedown, this);

    cb.add(bc);

    // ************************************************************************
    //   PANE
    // ************************************************************************
    var p = this._pane = new rwt.widgets.base.Parent();
    p.setHeight("1*");
    p.setOverflow("hidden");
    l.add(p);

    // ************************************************************************
    //   STATUSBAR
    // ************************************************************************
    var sb = this._statusBar = new rwt.widgets.base.HorizontalBoxLayout();
    sb.setAppearance("window-statusbar");
    sb.setHeight("auto");

    // ************************************************************************
    //   STATUSTEXT
    // ************************************************************************
    var st = this._statusText = new rwt.widgets.base.Label("Ready");
    st.setAppearance("window-statusbar-text");
    st.setSelectable(false);
    sb.add(st);

    // ************************************************************************
    //   INIT
    // ************************************************************************
    if (vCaption != null) {
      this.setCaption(vCaption);
    }

    if (vIcon != null) {
      this.setIcon(vIcon);
    }

    // ************************************************************************
    //   FUNCTIONAL
    // ************************************************************************
    this.setAutoHide(false);

    // ************************************************************************
    //   EVENTS: WINDOW
    // ************************************************************************
    this.addEventListener("mousedown", this._onwindowmousedown);
    this.addEventListener("click", this._onwindowclick);

    // ************************************************************************
    //   EVENTS: CAPTIONBAR
    // ************************************************************************
    cb.addEventListener("mousedown", this._oncaptionmousedown, this);
    cb.addEventListener("mouseup", this._oncaptionmouseup, this);
    cb.addEventListener("mousemove", this._oncaptionmousemove, this);
    cb.addEventListener("dblclick", this._oncaptiondblblick, this);

    // ************************************************************************
    //   REMAPPING
    // ************************************************************************
    this.remapChildrenHandlingTo(this._pane);
  },




  /*
  *****************************************************************************
     STATICS
  *****************************************************************************
  */

  statics :
  {
    /*
    ---------------------------------------------------------------------------
      MANAGER HANDLING
    ---------------------------------------------------------------------------
    */

    /**
     * Returns the default window manager. If no exists a new instance of
     * the manager is created.
     *
     * @type static
     * @return {rwt.widgets.util.WindowManager} window manager instance
     */
    getDefaultWindowManager : function()
    {
      if (!rwt.widgets.base.Window._defaultWindowManager) {
        rwt.widgets.base.Window._defaultWindowManager = new rwt.widgets.util.WindowManager();
      }

      return rwt.widgets.base.Window._defaultWindowManager;
    }
  },




  /*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

  properties :
  {
    /** Appearance of the widget */
    appearance :
    {
      refine : true,
      init : "window"
    },


    /** The windowManager to use for. */
    windowManager :
    {
      check : "rwt.widgets.util.WindowManager",
      event : "changeWindowManager"
    },


    /**
     * If the window is active, only one window in a single rwt.widgets.util.WindowManager could
     *  have set this to true at the same time.
     */
    active :
    {
      check : "Boolean",
      init : false,
      apply : "_applyActive",
      event : "changeActive"
    },


    /** Should be window be modal (this disable minimize and maximize buttons) */
    modal :
    {
      check : "Boolean",
      init : false,
      apply : "_applyModal",
      event : "changeModal"
    },


    /** The current mode (minimized or maximized) of the window instance
     * <b>Attention:</b> if the window instance is neither maximized nor minimized this
     * property will return <code>null</code>
     */
    mode :
    {
      check : [ "minimized", "maximized" ],
      init : null,
      nullable: true,
      apply : "_applyMode",
      event : "changeMode"
    },


    /** The opener (button) of the window */
    opener :
    {
      check : "rwt.widgets.base.Widget"
    },


    /** The text of the caption */
    caption :
    {
      apply : "_applyCaption",
      event : "changeCaption",
      dispose : true
    },


    /** The icon of the caption */
    icon :
    {
      check : "String",
      nullable : true,
      apply : "_applyIcon",
      event : "changeIcon"
    },


    /** The text of the statusbar */
    status :
    {
      check : "String",
      init : "Ready",
      apply : "_applyStatus",
      event :"changeStatus"
    },


    /** Should the close button be shown */
    showClose :
    {
      check : "Boolean",
      init : true,
      apply : "_applyShowClose"
    },


    /** Should the maximize button be shown */
    showMaximize :
    {
      check : "Boolean",
      init : true,
      apply : "_applyShowMaximize"
    },


    /** Should the minimize button be shown */
    showMinimize :
    {
      check : "Boolean",
      init : true,
      apply : "_applyShowMinimize"
    },


    /** Should the statusbar be shown */
    showStatusbar :
    {
      check : "Boolean",
      init : false,
      apply : "_applyShowStatusbar"
    },


    /** Should the user have the ability to close the window */
    allowClose :
    {
      check : "Boolean",
      init : true,
      apply : "_applyAllowClose"
    },


    /** Should the user have the ability to maximize the window */
    allowMaximize :
    {
      check : "Boolean",
      init : true,
      apply : "_applyAllowMaximize"
    },


    /** Should the user have the ability to minimize the window */
    allowMinimize :
    {
      check : "Boolean",
      init : true,
      apply : "_applyAllowMinimize"
    },


    /** If the text (in the captionbar) should be visible */
    showCaption :
    {
      check : "Boolean",
      init : true,
      apply : "_applyShowCaption"
    },


    /** If the icon (in the captionbar) should be visible */
    showIcon :
    {
      check : "Boolean",
      init : true,
      apply : "_applyShowIcon"
    },


    /** If the window is moveable */
    moveable :
    {
      check : "Boolean",
      init : true,
      event : "changeMoveable"
    },


    /** The move method to use */
    moveMethod :
    {
      check : [ "opaque", "frame", "translucent" ],
      init : "opaque",
      event : "changeMoveMethod"
    }
  },




  /*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */

  members :
  {

    _isFocusRoot : true,

    /*
    ---------------------------------------------------------------------------
      UTILITIES
    ---------------------------------------------------------------------------
    */

    /**
     * Accessor method for the pane sub widget
     *
     * @type member
     * @return {rwt.widgets.base.Parent} pane sub widget
     */
    getPane : function() {
      return this._pane;
    },


    /**
     * Accessor method for the captionbar sub widget
     *
     * @type member
     * @return {rwt.widgets.base.HorizontalBoxLayout} captionbar sub widget
     */
    getCaptionBar : function() {
      return this._captionBar;
    },


    /**
     * Accessor method for the statusbar sub widget
     *
     * @type member
     * @return {rwt.widgets.base.HorizontalBoxLayout} statusbar sub widget
     */
    getStatusBar : function() {
      return this._statusBar;
    },


    /**
     * Closes the current window instance.
     * Technically calls the {@link rwt.widgets.base.Widget#hide} method.
     *
     * @type member
     * @return {void}
     */
    close : function() {
      this.hide();
    },


    /**
     * Opens the window.<br/>
     * Sets the opener property (if available) and centers
     * the window if the property {@link #centered} is enabled.
     *
     * @type member
     * @param vOpener {Object} Opener widget
     * @return {void}
     */
    open : function(vOpener)
    {
      if (vOpener != null) {
        this.setOpener(vOpener);
      }

      if (this.getCentered()) {
        this.centerToBrowser();
      }

      this.show();
    },


    /**
     * Set the focus on the window.<br/>
     * Setting the {@link #active} property to <code>true</code>
     *
     * @type member
     * @return {void}
     */
    focus : function() {
      this.setActive(true);
    },


    /**
     * Release the focus on the window.<br/>
     * Setting the {@link #active} property to <code>false</code>
     *
     * @type member
     * @return {void}
     */
    blur : function() {
      this.setActive(false);
    },


    /**
     * Maximize the window by setting the property {@link mode} to <code>maximized</code>
     *
     * @type member
     * @return {void}
     */
    maximize : function() {
      this.setMode("maximized");
    },


    /**
     * Maximize the window by setting the property {@link mode} to <code>minimized</code>
     *
     * @type member
     * @return {void}
     */
    minimize : function() {
      this.setMode("minimized");
    },


    /**
     * Maximize the window by setting the property {@link mode} to <code>null</code>
     *
     * @type member
     * @return {void}
     */
    restore : function() {
      this.setMode(null);
    },




    /*
    ---------------------------------------------------------------------------
      APPEAR/DISAPPEAR
    ---------------------------------------------------------------------------
    */

    /**
     * Executes routines to ensure the window is displayed correctly and gains control.<br/>
     * Hides all open popups, sets the focus root to the current window, adds
     * the current window to the window manager and calls {@link rwt.widgets.base.Popup#_makeActive}.
     *
     * @type member
     * @return {void}
     */
    _beforeAppear : function()
    {
      // Intentionally bypass superclass and call super.super._beforeAppear
      rwt.widgets.base.Parent.prototype._beforeAppear.call(this);

      // Hide popups
      rwt.widgets.util.PopupManager.getInstance().update();

      // Configure the focus root to be the current opened window
      rwt.event.EventHandler.setFocusRoot(this);

      this.getWindowManager().add(this);
      this._makeActive();
    },


    /**
     * Executes routines to ensure the window releases all control.<br/>
     * Resets the focus root, release the capturing on any contained widget,
     * deregisters from the window manager and calls {@link rwt.widgets.base.Popup#_makeInactive}.
     *
     * @type member
     * @return {void}
     */
    _beforeDisappear : function()
    {
      // Intentionally bypass superclass and call super.super._beforeDisappear
      rwt.widgets.base.Parent.prototype._beforeDisappear.call(this);

      // Reset focus root
      var vFocusRoot = rwt.event.EventHandler.getFocusRoot();

      if (vFocusRoot == this || this.contains(vFocusRoot)) {
        rwt.event.EventHandler.setFocusRoot(null);
      }

      // Be sure to disable any capturing inside invisible parts
      // Is this to much overhead?
      // Are there any other working solutions?
      var vWidget = rwt.event.EventHandler.getCaptureWidget();

      if (vWidget && this.contains(vWidget)) {
        vWidget.setCapture(false);
      }

      this.getWindowManager().remove(this);
      this._makeInactive();
    },




    /*
    ---------------------------------------------------------------------------
      ZIndex Positioning
    ---------------------------------------------------------------------------
    */

    _minZIndex : 1e5,


    /**
     * Gets all registered window instances (sorted by the zIndex) and resets
     * the zIndex on all instances.
     *
     * @type member
     * @return {void}
     */
    _sendTo : function()
    {
      var zIndexCompare = function(a, b) {
        return a.getZIndex() - b.getZIndex();
      };
      var vAll = rwt.util.Objects.getValues(this.getWindowManager().getAll()).sort(zIndexCompare);
      var vLength = vAll.length;
      var vIndex = this._minZIndex;

      for (var i=0; i<vLength; i++) {
        vAll[i].setZIndex(vIndex++);
      }
    },




    /*
    ---------------------------------------------------------------------------
      MODIFIERS
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyActive : function(value, old)
    {
      if (old)
      {
        if (this.getFocused()) {
          this.setFocused(false);
        }
        if (this.getWindowManager().getActiveWindow() == this) {
          this.getWindowManager().setActiveWindow(null);
        }
        this._setActiveState( false );

      }
      else
      {
        // Switch focus
        // Also do this if gets inactive as this moved the focus outline
        // away from any focused child.
        if (!this.getFocusedChild()) {
          this.setFocused(true);
        }
        this._setActiveState( true );
        this.getWindowManager().setActiveWindow(this);
        this.bringToFront();

      }
    },

    _setActiveState : function( value ) {
      if( !this.getWindowManager().blockActiveState ) {
        this.toggleState( "active", value );
        this._captionBar.toggleState( "active", value );
        this._minimizeButton.toggleState( "active", value );
        this._restoreButton.toggleState( "active", value );
        this._maximizeButton.toggleState( "active", value );
        this._closeButton.toggleState( "active", value );
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyModal : function( value ) {
      // Inform blocker
      if( this._initialLayoutDone && this.getVisibility() && this.getDisplay() ) {
        var vTop = this.getTopLevelWidget();
        if( value ) {
          vTop.block( this );
        } else {
          vTop.release( this );
        }
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     * @return {var} TODOC
     */
    _applyAllowClose : function() {
      this._closeButtonManager();
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     * @return {var} TODOC
     */
    _applyAllowMaximize : function() {
      this._maximizeButtonManager();
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     * @return {var} TODOC
     */
    _applyAllowMinimize : function() {
      this._minimizeButtonManager();
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyMode : function(value, old)
    {
      switch(value)
      {
        case "minimized":
          this._disableResize = true;
          this._minimize();
          break;

        case "maximized":
          this._disableResize = true;
          if( old === "minimized" ) {
            this._restoreFromMinimized();
          } else {
            this._maximize();
          }
          break;

        default:
          delete this._disableResize;
          switch(old)
          {
            case "maximized":
              this._restoreFromMaximized();
              break;

            case "minimized":
              this._restoreFromMinimized();
              break;
          }
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyShowCaption : function(value)
    {
      if (value) {
        this._captionBar.addAt(this._captionTitle, this.getShowIcon() ? 1 : 0);
      } else {
        this._captionBar.remove(this._captionTitle);
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyShowIcon : function(value)
    {
      if (value) {
        this._captionBar.addAtBegin(this._captionIcon);
      } else {
        this._captionBar.remove(this._captionIcon);
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyShowStatusbar : function(value)
    {
      if (value) {
        this._layout.addAtEnd(this._statusBar);
      } else {
        this._layout.remove(this._statusBar);
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyShowClose : function(value)
    {
      if (value) {
        this._captionBar.addAtEnd(this._closeButton);
      } else {
        this._captionBar.remove(this._closeButton);
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyShowMaximize : function(value)
    {
      if (value)
      {
        var t = this.getMode() == "maximized" ? this._restoreButton : this._maximizeButton;

        if (this.getShowMinimize()) {
          this._captionBar.addAfter(t, this._minimizeButton);
        } else {
          this._captionBar.addAfter(t, this._captionFlex);
        }
      }
      else
      {
        this._captionBar.remove(this._maximizeButton);
        this._captionBar.remove(this._restoreButton);
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyShowMinimize : function(value)
    {
      if (value) {
        this._captionBar.addAfter(this._minimizeButton, this._captionFlex);
      } else {
        this._captionBar.remove(this._minimizeButton);
      }
    },


    /**
     * Enables/disables the minimize button in order of the {@link #allowMinimize} property
     *
     * @type member
     */
    _minimizeButtonManager : function() {
      if( this.getAllowMinimize() === false ) {
        this._minimizeButton.setEnabled( false );
      } else {
        this._minimizeButton.resetEnabled();
      }
    },


    /**
     * Enables/disables the close button in order of the {@link #allowClose} property
     *
     * @type member
     */
    _closeButtonManager : function() {
      if( this.getAllowClose() === false ) {
        this._closeButton.setEnabled( false );
      } else {
        this._closeButton.resetEnabled();
      }
    },


    /**
     * Disables the maximize and restore buttons when the window instance is already maximized,
     * otherwise the {@link #enabled} property of both buttons get resetted.
     *
     * @type member
     */
    _maximizeButtonManager : function() {
      var b = this.getAllowMaximize() && this.getResizable() && this._computedMaxWidthTypeNull && this._computedMaxHeightTypeNull;
      if( this._maximizeButton ) {
        if( b === false ) {
          this._maximizeButton.setEnabled( false );
        } else {
          this._maximizeButton.resetEnabled();
        }
      }
      if( this._restoreButton ) {
        if( b === false ) {
          this._restoreButton.setEnabled( false );
        } else {
          this._restoreButton.resetEnabled();
        }
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyStatus : function(value) {
      this._statusText.setText(value);
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     * @return {void} TODOC
     */
    _applyMaxWidth : function(value)
    {
      this.base(arguments, value);
      this._maximizeButtonManager();
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     * @return {void} TODOC
     */
    _applyMaxHeight : function(value)
    {
      this.base(arguments, value);
      this._maximizeButtonManager();
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     * @return {var} TODOC
     */
    _applyResizable : function() {
      this._maximizeButtonManager();
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyCaption : function(value) {
      this._captionTitle.setText(value);
    },

    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyIcon : function(value) {
      this._captionIcon.setSource(value);
    },




    /*
    ---------------------------------------------------------------------------
      STATE LAYOUT IMPLEMENTATION
    ---------------------------------------------------------------------------
    */

    /**
     * Minimizes the window. Technically this methods calls the {@link rwt.widgets.base.Widget#blur}
     * and the {@link rwt.widgets.base.Widget#hide} methods.
     *
     * @type member
     * @return {void}
     */
    _minimize : function()
    {
      this.blur();
      this.hide();
    },


    /**
     * Restores the window from maximized mode.<br/>
     * Restores the previous dimension and location, removes the
     * state <code>maximized</code> and replaces the restore button
     * with the maximize button.
     *
     * @type member
     * @return {void}
     */
    _restoreFromMaximized : function()
    {
      // restore previous dimension and location
      this.setLeft(this._previousLeft ? this._previousLeft : null);
      this.setWidth(this._previousWidth ? this._previousWidth : null);
      this.setRight(this._previousRight ? this._previousRight : null);

      this.setTop(this._previousTop ? this._previousTop : null);
      this.setHeight(this._previousHeight ? this._previousHeight : null);
      this.setBottom(this._previousBottom ? this._previousBottom : null);

      // update state
      this.removeState("maximized");

      // toggle button
      if (this.getShowMaximize())
      {
        var cb = this._captionBar;
        var v = cb.indexOf(this._restoreButton);

        cb.remove(this._restoreButton);
        cb.addAt(this._maximizeButton, v);
      }

      // finally focus the window
      this.focus();
    },


    /**
     * Restores the window from minimized mode.<br/>
     * Reset the window mode to maximized if the window
     * has the state maximized and call {@link rwt.widgets.base.Widget#show} and
     * {@link rwt.widgets.base.Widget#focus}
     *
     * @type member
     * @return {void}
     */
    _restoreFromMinimized : function()
    {
      if (this.hasState("maximized")) {
        this.setMode("maximized");
      }

      this.show();
      this.focus();
    },


    /**
     * Maximizes the window.<br/>
     * Stores the current dimension and location and setups up
     * the new ones. Adds the state <code>maximized</code> and toggles
     * the buttons in the caption bar.
     *
     * @type member
     * @return {void}
     */
    _maximize : function()
    {
      if (this.hasState("maximized")) {
        return;
      }

      // store current dimension and location
      this._previousLeft = this.getLeft();
      this._previousWidth = this.getWidth();
      this._previousRight = this.getRight();
      this._previousTop = this.getTop();
      this._previousHeight = this.getHeight();
      this._previousBottom = this.getBottom();

      // setup new dimension and location
      this.setLeft( 0 );
      this.setTop( 0 );
      this.setWidth( "100%" );
      this.setHeight( "100%" );

      // update state
      this.addState("maximized");

      // toggle button
      if (this.getShowMaximize())
      {
        var cb = this._captionBar;
        var v = cb.indexOf(this._maximizeButton);

        cb.remove(this._maximizeButton);
        cb.addAt(this._restoreButton, v);
      }

      // finally focus the window
      this.focus();
    },




    /*
    ---------------------------------------------------------------------------
      EVENTS: WINDOW
    ---------------------------------------------------------------------------
    */


    /**
     * Stops every mouse click on the window by calling {@link rwt.event.Event#stopPropagation}
     *
     * @type member
     * @param e {rwt.event.MouseEvent} mouse click event
     * @return {void}
     */
    _onwindowclick : function(e)
    {
      // stop event
      e.stopPropagation();
    },


    /**
     * Focuses the window instance.
     *
     * @type member
     * @param e {rwt.event.MouseEvent} mouse down event
     * @return {void}
     */
    _onwindowmousedown : function() {
      try {
        this.focus();
      } catch( ex ) {
        rwt.runtime.ErrorHandler.processJavaScriptError( ex );
      }
    },



    /*
    ---------------------------------------------------------------------------
      EVENTS: BUTTONS
    ---------------------------------------------------------------------------
    */

    /**
     * Stops every mouse down event on each button in the captionbar
     * by calling {@link rwt.event.Event#stopPropagation}
     *
     * @type member
     * @param e {rwt.event.MouseEvent} mouse down event
     * @return {void}
     */
    _onbuttonmousedown : function(e) {
      e.stopPropagation();
    },


    /**
     * Minmizes the window, removes all states from the minimize button and
     * stops the further propagation of the event (calling {@link rwt.event.Event#stopPropagation}).
     *
     * @type member
     * @param e {rwt.event.MouseEvent} mouse click event
     * @return {void}
     */
    _onminimizebuttonclick : function(e)
    {
      this.minimize();

      // we need to be sure that the button gets the right states after clicking
      // because the button will move and does not get the mouseup event anymore
      this._minimizeButton.removeState("pressed");
      this._minimizeButton.removeState("abandoned");
      this._minimizeButton.removeState("over");

      e.stopPropagation();
    },


    /**
     * Restores the window, removes all states from the restore button and
     * stops the further propagation of the event (calling {@link rwt.event.Event#stopPropagation}).
     *
     * @type member
     * @param e {rwt.event.MouseEvent} mouse click event
     * @return {void}
     */
    _onrestorebuttonclick : function(e)
    {
      this.restore();

      // we need to be sure that the button gets the right states after clicking
      // because the button will move and does not get the mouseup event anymore
      this._restoreButton.removeState("pressed");
      this._restoreButton.removeState("abandoned");
      this._restoreButton.removeState("over");

      e.stopPropagation();
    },


    /**
     * Maximizes the window, removes all states from the maximize button and
     * stops the further propagation of the event (calling {@link rwt.event.Event#stopPropagation}).
     *
     * @type member
     * @param e {rwt.event.MouseEvent} mouse click event
     * @return {void}
     */
    _onmaximizebuttonclick : function(e)
    {
      this.maximize();

      // we need to be sure that the button gets the right states after clicking
      // because the button will move and does not get the mouseup event anymore
      this._maximizeButton.removeState("pressed");
      this._maximizeButton.removeState("abandoned");
      this._maximizeButton.removeState("over");

      e.stopPropagation();
    },


    /**
     * Closes the window, removes all states from the close button and
     * stops the further propagation of the event (calling {@link rwt.event.Event#stopPropagation}).
     *
     * @type member
     * @param e {rwt.event.MouseEvent} mouse click event
     * @return {void}
     */
    _onclosebuttonclick : function(e)
    {
      this.close();

      // we need to be sure that the button gets the right states after clicking
      // because the button will move and does not get the mouseup event anymore
      this._closeButton.removeState("pressed");
      this._closeButton.removeState("abandoned");
      this._closeButton.removeState("over");

      e.stopPropagation();
    },

    /*
    ---------------------------------------------------------------------------
      EVENTS: CAPTIONBAR
    ---------------------------------------------------------------------------
    */

    /**
     * Enables the capturing of the caption bar and prepares the drag session and the
     * appearance (translucent, frame or opaque) for the moving of the window.
     *
     * @type member
     * @param e {rwt.event.MouseEvent} mouse down event
     * @return {void}
     */
    _oncaptionmousedown : function(e)
    {
      if (!e.isLeftButtonPressed() || !this.getMoveable() || this.getMode() != null) {
        return;
      }

      // enable capturing
      this._captionBar.setCapture(true);

      // element cache
      var el = this.getElement();

      // measuring and caching of values for drag session
      var pa = this.getParent();
      var pl = pa.getElement();

      // compute locations
      var paLoc = rwt.html.Location.get(pl, "scroll");
      var elLoc = rwt.html.Location.get(el);
      var scrollX = rwt.html.Viewport.getScrollLeft();
      var scrollY = rwt.html.Viewport.getScrollTop();

      this._dragSession =
      {
        offsetX                   : e.getPageX() - elLoc.left + paLoc.left + scrollX,
        offsetY                   : e.getPageY() - elLoc.top + paLoc.top + scrollY,
        parentAvailableAreaLeft   : paLoc.left + scrollX + 5,
        parentAvailableAreaTop    : paLoc.top + scrollY + 5,
        parentAvailableAreaRight  : paLoc.right + scrollX - 5,
        parentAvailableAreaBottom : paLoc.bottom + scrollY - 5
      };
      // handle frame and translucently
      switch(this.getMoveMethod())
      {
        case "translucent":
          this.setOpacity(0.5);
          break;

        case "frame":
          var f = this._frame;

          if (f.getParent() != this.getParent())
          {
            f.setParent(this.getParent());

            // This flush is required to get the element node, needed by
            // the code below and the other event handlers
            rwt.widgets.base.Widget.flushGlobalQueues();
          }

          f._renderRuntimeLeft(elLoc.left - paLoc.left);
          f._renderRuntimeTop(elLoc.top - paLoc.top);

          f._renderRuntimeWidth(el.offsetWidth);
          f._renderRuntimeHeight(el.offsetHeight);

          f.setZIndex(this.getZIndex() + 1);

          break;
      }
    },


    /**
     * Disables the capturing of the caption bar and moves the window
     * to the last position of the drag session. Also restores the appearance
     * of the window.
     *
     * @type member
     * @param e {rwt.event.MouseEvent} mouse up event
     * @return {void}
     */
    _oncaptionmouseup : function()
    {
      var s = this._dragSession;

      if (!s) {
        return;
      }

      // disable capturing
      this._captionBar.setCapture(false);

      // move window to last position
      if (s.lastX != null) {
        this.setLeft(s.lastX);
      }

      if (s.lastY != null) {
        this.setTop(s.lastY);
      }

      // handle frame and translucently
      switch(this.getMoveMethod())
      {
        case "translucent":
          this.setOpacity(null);
          break;

        case "frame":
          this._frame.setParent(null);
          break;
      }

      // cleanup session
      delete this._dragSession;
    },


    /**
     * Does the moving of the window by rendering the position
     * of the window (or frame) at runtime using direct dom methods.
     *
     * @type member
     * @param e {rwt.event.Event} mouse move event
     * @return {void}
     */
    _oncaptionmousemove : function(e)
    {
      var s = this._dragSession;

      // pre check for active session and capturing
      if (!s || !this._captionBar.getCapture()) {
        return;
      }

      var x = this._appModal ? e.getClientX() : e.getPageX();
      var y = this._appModal ? e.getClientY() : e.getPageY();

      // pre check if we go out of the available area
      if (!rwt.util.Numbers.isBetween(x, s.parentAvailableAreaLeft, s.parentAvailableAreaRight) || !rwt.util.Numbers.isBetween(y, s.parentAvailableAreaTop, s.parentAvailableAreaBottom)) {
        return;
      }

      // use the fast and direct dom methods
      var o = this.getMoveMethod() == "frame" ? this._frame : this;

      o._renderRuntimeLeft(s.lastX = x - s.offsetX);
      o._renderRuntimeTop(s.lastY = y - s.offsetY);
    },


    /**
     * Maximizes the window or restores it if it is already
     * maximized.
     *
     * @type member
     * @param e {rwt.event.MouseEvent} double click event
     * @return {void}
     */
    _oncaptiondblblick : function()
    {
      if (!this._maximizeButton.getEnabled()) {
        return;
      }

      return this.getMode() == "maximized" ? this.restore() : this.maximize();
    }
  },




  /*
  *****************************************************************************
     DESTRUCTOR
  *****************************************************************************
  */

  destruct : function()
  {
    this._disposeObjects("_layout", "_captionBar", "_captionIcon",
      "_captionTitle", "_captionFlex", "_closeButton", "_minimizeButton",
      "_maximizeButton", "_restoreButton", "_pane", "_statusBar", "_statusText");
  }
});
