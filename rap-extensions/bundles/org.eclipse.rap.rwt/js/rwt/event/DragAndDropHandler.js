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
 * This manager (singleton) manage all drag and drop handling of a rwt.qx.Init instance.
 */
rwt.qx.Class.define( "rwt.event.DragAndDropHandler", {

  extend : rwt.util.ObjectManager,

  statics : {

    getInstance : function() {
      return rwt.runtime.Singletons.get( rwt.event.DragAndDropHandler );
    }

  },

  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  construct : function()
  {
    this.base(arguments);

    this.__data = {};
    this.__actions = {};
    this.__cursors = {};

    var vCursor, vAction;
    var vActions = [ "move", "copy", "alias", "nodrop" ];

    for (var i=0, l=vActions.length; i<l; i++)
    {
      vAction = vActions[i];
      vCursor = this.__cursors[vAction] = new rwt.widgets.base.Image();
      vCursor.setAppearance("cursor-dnd-" + vAction);
      vCursor.setZIndex(1e8);
    }
  },




  /*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

  properties :
  {
    sourceWidget :
    {
      check : "rwt.widgets.base.Widget",
      nullable : true
    },

    destinationWidget :
    {
      check : "rwt.widgets.base.Widget",
      nullable : true,
      apply : "_applyDestinationWidget"
    },

    currentAction :
    {
      check : "String",
      nullable : true,
      event : "changeCurrentAction"
    },


    /**
     * The default delta x of the cursor feedback.
     *
     * @see #setCursorPosition
     */
    defaultCursorDeltaX :
    {
      check : "Integer",
      init : 5
    },


    /**
     * The default delta y of the cursor feedback.
     *
     * @see #setCursorPosition
     */
    defaultCursorDeltaY :
    {
      check : "Integer",
      init : 15
    }
  },




  /*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */

  members :
  {
    __lastDestinationEvent : null,




    /*
    ---------------------------------------------------------------------------
      COMMON MODIFIER
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyDestinationWidget : function(value)
    {
      if (value)
      {
        value.dispatchEvent(new rwt.event.DragEvent("dragdrop", this.__lastDestinationEvent, value, this.getSourceWidget()));
        this.__lastDestinationEvent = null;
      }
    },




    /*
    ---------------------------------------------------------------------------
      DATA HANDLING
    ---------------------------------------------------------------------------
    */

    /**
     * Add data of mimetype.
     *
     * #param vMimeType[String]: A valid mimetype
     * #param vData[Any]: Any value for the mimetype
     *
     * @type member
     * @param vMimeType {var} TODOC
     * @param vData {var} TODOC
     * @return {void}
     */
    addData : function(vMimeType, vData) {
      this.__data[vMimeType] = vData;
    },


    /**
     * TODOC
     *
     * @type member
     * @param vMimeType {var} TODOC
     * @return {var} TODOC
     */
    getData : function(vMimeType) {
      return this.__data[vMimeType];
    },


    /**
     * TODOC
     *
     * @type member
     * @return {void}
     */
    clearData : function() {
      this.__data = {};
    },




    /*
    ---------------------------------------------------------------------------
      MIME TYPE HANDLING
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @return {var} TODOC
     */
    getDropDataTypes : function()
    {
      var vDestination = this.getDestinationWidget();
      var vDropTypes = [];

      // If there is not any destination, simple return
      if (!vDestination) {
        return vDropTypes;
      }

      // Search for matching mimetypes
      var vDropDataTypes = vDestination.getDropDataTypes();

      for (var i=0, l=vDropDataTypes.length; i<l; i++)
      {
        if (vDropDataTypes[i] in this.__data) {
          vDropTypes.push(vDropDataTypes[i]);
        }
      }

      return vDropTypes;
    },

    /**
     * @signature function(e)
     */
    getDropTarget : rwt.util.Variant.select("qx.client",
    {
      // This hack is no longer needed for Firefox 2.0
      // We should verify, which Firefox version needed this hack.
      /*
      "gecko" : function(e)
      {
        var vCurrent = e.getTarget();

        // work around gecko bug (all other browsers are correct)
        // clicking on a free space and drag prohibit the get of
        // a valid event target. The target is always the element
        // which was the one with the mousedown event before.
        if (vCurrent == this.__dragCache.sourceWidget) {
          vCurrent = rwt.event.EventHandlerUtil.getTargetObject(rwt.html.ElementFromPoint.getElementFromPoint(e.getPageX(), e.getPageY()));
        } else {
          vCurrent = rwt.event.EventHandlerUtil.getTargetObject(null, vCurrent);
        }

        while (vCurrent != null)
        {
          if (!vCurrent.supportsDrop(this.__dragCache)) {
            return null;
          }

          if (this.supportsDrop(vCurrent)) {
            return vCurrent;
          }

          vCurrent = vCurrent.getParent();
        }

        return null;
      },
      */

      "default" : function(e)
      {
        var vCurrent = e.getTarget();

        while (vCurrent != null)
        {
          if (!vCurrent.supportsDrop(this.__dragCache)) {
            return null;
          }

          if (this.supportsDrop(vCurrent)) {
            return vCurrent;
          }

          vCurrent = vCurrent.getParent();
        }

        return null;
      }
    }),





    /*
    ---------------------------------------------------------------------------
      START DRAG
    ---------------------------------------------------------------------------
    */

    /**
     * This needed be called from any "dragstart" event to really start drag session.
     *
     * @type member
     * @return {void}
     * @throws TODOC
     */
    startDrag : function()
    {
      if (!this.__dragCache) {
        throw new Error("Invalid usage of startDrag. Missing dragInfo!");
      }

      // Update status flag
      this.__dragCache.dragHandlerActive = true;

      // Internal storage of source widget
      this.setSourceWidget(this.__dragCache.sourceWidget);

      // Add feedback widget
      if (this.__feedbackWidget)
      {
        this.__feedbackWidget.setVisibility(false);

        var doc = rwt.widgets.base.ClientDocument.getInstance();
        doc.add(this.__feedbackWidget);
        this.__feedbackWidget.setZIndex(1e8);
      }
    },




    /*
    ---------------------------------------------------------------------------
      FIRE IMPLEMENTATION FOR USER EVENTS
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @param fromWidget {var} TODOC
     * @param toWidget {var} TODOC
     * @param e {Event} TODOC
     * @return {void}
     */
    _fireUserEvents : function(fromWidget, toWidget, e)
    {
      if (fromWidget && fromWidget != toWidget && fromWidget.hasEventListeners("dragout")) {
        fromWidget.dispatchEvent(new rwt.event.DragEvent("dragout", e, fromWidget, toWidget), true);
      }

      if (toWidget)
      {
        if (fromWidget != toWidget && toWidget.hasEventListeners("dragover")) {
          toWidget.dispatchEvent(new rwt.event.DragEvent("dragover", e, toWidget, fromWidget), true);
        }

        if (toWidget.hasEventListeners("dragmove")) {
          toWidget.dispatchEvent(new rwt.event.DragEvent("dragmove", e, toWidget, null), true);
        }
      }
    },




    /*
    ---------------------------------------------------------------------------
      HANDLER FOR MOUSE EVENTS
    ---------------------------------------------------------------------------
    */

    /**
     * This wraps the mouse events to custom handlers.
     *
     * @type member
     * @param e {Event} TODOC
     * @return {var} TODOC
     */
    handleMouseEvent : function(e)
    {
      switch(e.getType())
      {
        case "mousedown":
          return this._handleMouseDown(e);

        case "mouseup":
          return this._handleMouseUp(e);

        case "mousemove":
          return this._handleMouseMove(e);
      }
    },


    /**
     * This starts the core drag and drop session.
     *
     * To really get drag and drop working you need to define
     * a function which you attach to "dragstart"-event, which
     * invokes at least this.startDrag()
     *
     * @type member
     * @param e {Event} TODOC
     * @return {void}
     */
    _handleMouseDown : function(e)
    {
      if (e.getDefaultPrevented() || !e.isLeftButtonPressed()) {
        return;
      }

      if(this.__dragCache == null)
      {
        // Store initial dragCache
        this.__dragCache =
        {
          startScreenX      : e.getScreenX(),
          startScreenY      : e.getScreenY(),
          pageX             : e.getPageX(),
          pageY             : e.getPageY(),
          sourceWidget      : e.getTarget(),
          sourceElement     : e.getDomTarget(),
          sourceTopLevel    : e.getTarget().getTopLevelWidget(),
          dragHandlerActive : false,
          hasFiredDragStart : false
        };
      }
    },


    /**
     * Handler for mouse move events
     *
     * @type member
     * @param e {Event} TODOC
     * @return {void}
     */
    _handleMouseMove : function(e)
    {
      // Return if dragCache was not filled before
      if (!this.__dragCache) {
        return;
      }

      /*
        Default handling if drag handler is activated
      */

      if (this.__dragCache.dragHandlerActive)
      {
        // Update page coordinates
        this.__dragCache.pageX = e.getPageX();
        this.__dragCache.pageY = e.getPageY();

        // Get current target
        var currentDropTarget = this.getDropTarget(e);

        // Update action
        this.setCurrentAction(currentDropTarget ? this._evalNewAction(e.isShiftPressed(), e.isCtrlPressed(), e.isAltPressed()) : null);

        // Fire user events
        this._fireUserEvents(this.__dragCache.currentDropWidget, currentDropTarget, e);

        // Store current widget
        this.__dragCache.currentDropWidget = currentDropTarget;

        // Update cursor icon
        this._renderCursor();

        // Update user feedback
        this._renderFeedbackWidget();
      }

      /*
        Initial activation and fire of dragstart
      */

      else if (!this.__dragCache.hasFiredDragStart)
      {
        if (Math.abs(e.getScreenX() - this.__dragCache.startScreenX) > 5 || Math.abs(e.getScreenY() - this.__dragCache.startScreenY) > 5)
        {
          // Fire dragstart event to finally allow the above if to handle next events
          this.__dragCache.sourceWidget.dispatchEvent(new rwt.event.DragEvent("dragstart", e, this.__dragCache.sourceWidget), true);

          // Update status flag
          this.__dragCache.hasFiredDragStart = true;

          // Look if handler become active
          if (this.__dragCache.dragHandlerActive)
          {
            // Fire first user events
            var currentDropWidget = this.getDropTarget( e );
            this._fireUserEvents( null, currentDropWidget, e);

            // Update status flags
            this.__dragCache.currentDropWidget = currentDropWidget;

            // Activate capture for clientDocument
            rwt.widgets.base.ClientDocument.getInstance().setCapture(true);
          }
        }
      }
    },


    /**
     * Handle mouse up event. Normally this finalize the drag and drop event.
     *
     * @type member
     * @param e {Event} TODOC
     * @return {void}
     */
    _handleMouseUp : function(e)
    {
      // Return if dragCache was not filled before
      if (!this.__dragCache) {
        return;
      }

      if (this.__dragCache.dragHandlerActive) {
        this._endDrag(this.getDropTarget(e), e);
      }
      else
      {
        // Clear drag cache
        this.__dragCache = null;
      }
    },




    /*
    ---------------------------------------------------------------------------
      HANDLER FOR KEY EVENTS
    ---------------------------------------------------------------------------
    */

    /**
     * This wraps the key events to custom handlers.
     *
     * @type member
     * @param e {Event} TODOC
     * @return {void}
     */
    handleKeyEvent : function(e)
    {
      if (!this.__dragCache) {
        return;
      }

      switch(e.getType())
      {
        case "keydown":
          this._handleKeyDown(e);
          return;

        case "keyup":
          this._handleKeyUp(e);
          return;
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @param e {Event} TODOC
     * @return {void}
     */
    _handleKeyDown : function(e)
    {
      // Stop Drag on Escape
      if (e.getKeyIdentifier() == "Escape") {
        this.cancelDrag(e);
      }

      // Update cursor and action on press of modifier keys
      else if (this.getCurrentAction() != null)
      {
        // TODO this doesn't work in WebKit because WebKit doesn't fire keyevents for modifier keys
        switch(e.getKeyIdentifier())
        {
          case "Shift":
          case "Control":
          case "Alt":
            this.setAction(this._evalNewAction(e.isShiftPressed(), e.isCtrlPressed(), e.isAltPressed()));
            this._renderCursor();

            e.preventDefault();
        }
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @param e {Event} TODOC
     * @return {void}
     */
    _handleKeyUp : function(e)
    {
      // TODO this doesn't work in WebKit because WebKit doesn't fire keyevents for modifier keys
      var bShiftPressed = e.getKeyIdentifier() == "Shift";
      var bCtrlPressed = e.getKeyIdentifier() == "Control";
      var bAltPressed = e.getKeyIdentifier() == "Alt";

      if (bShiftPressed || bCtrlPressed || bAltPressed)
      {
        if (this.getCurrentAction() != null)
        {
          this.setAction(this._evalNewAction(!bShiftPressed && e.isShiftPressed(), !bCtrlPressed && e.isCtrlPressed(), !bAltPressed && e.isAltPressed()));
          this._renderCursor();

          e.preventDefault();
        }
      }
    },




    /*
    ---------------------------------------------------------------------------
      IMPLEMENTATION OF DRAG&DROP SESSION FINALISATION
    ---------------------------------------------------------------------------
    */

    /**
     * Cancel current drag and drop session
     *
     * @type member
     * @param e {Event} TODOC
     * @return {void}
     */
    cancelDrag : function(e)
    {
      // Return if dragCache was not filled before
      if (!this.__dragCache) {
        return;
      }

      if (this.__dragCache.dragHandlerActive) {
        this._endDrag(null, e);
      }
      else
      {
        // Clear drag cache
        this.__dragCache = null;
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @return {void}
     */
    globalCancelDrag : function()
    {
      if (this.__dragCache && this.__dragCache.dragHandlerActive) {
        this._endDragCore();
      }
    },


    /**
     * This will be called to the end of each drag and drop session
     *
     * @type member
     * @param currentDestinationWidget {var} TODOC
     * @param e {Event} TODOC
     * @return {void}
     */
    _endDrag : function(currentDestinationWidget, e)
    {
      // Use given destination widget
      if (currentDestinationWidget)
      {
        this.__lastDestinationEvent = e;
        this.setDestinationWidget(currentDestinationWidget);
      }

      // Dispatch dragend event
      this.getSourceWidget().dispatchEvent(new rwt.event.DragEvent("dragend", e, this.getSourceWidget(), currentDestinationWidget), true);

      // Fire dragout event
      // RAP : We create this event on the server-side:
      //this._fireUserEvents(this.__dragCache && this.__dragCache.currentDropWidget, null, e);

      // Call helper
      this._endDragCore();
    },


    /**
     * TODOC
     *
     * @type member
     * @return {void}
     */
    _endDragCore : function()
    {
      // Cleanup feedback widget
      if (this.__feedbackWidget)
      {
        var doc = rwt.widgets.base.ClientDocument.getInstance();
        doc.remove(this.__feedbackWidget);

        if (this.__feedbackAutoDispose) {
          this.__feedbackWidget.destroy();
        }

        this.__feedbackWidget = null;
      }

      // Remove cursor
      var oldCursor = this.__cursor;

      if (oldCursor)
      {
        oldCursor._style.display = "none";
        this.__cursor = null;
      }

      this._cursorDeltaX = null;
      this._cursorDeltaY = null;

      // Reset drag cache for next drag and drop session
      if (this.__dragCache)
      {
        this.__dragCache.currentDropWidget = null;
        this.__dragCache = null;
      }

      // Deactivate capture for clientDocument
      rwt.widgets.base.ClientDocument.getInstance().setCapture(false);

      // Cleanup data and actions
      this.clearData();
      this.clearActions();

      // Cleanup widgets
      this.setSourceWidget(null);
      this.setDestinationWidget(null);
    },




    /*
    ---------------------------------------------------------------------------
      IMPLEMENTATION OF CURSOR UPDATES
    ---------------------------------------------------------------------------
    */

    /**
     * Sets the position of the cursor feedback (the icon showing whether dropping
     * is allowed at the current position and which action a drop will do).
     *
     * @type member
     * @param deltaX {int} The number of pixels the top-left corner of the
     *          cursor feedback should be away from the mouse cursor in x direction.
     * @param deltaY {int} The number of pixels the top-left corner of the
     *          cursor feedback should be away from the mouse cursor in y direction.
     * @return {void}
     */
    setCursorPosition : function(deltaX, deltaY)
    {
      this._cursorDeltaX = deltaX;
      this._cursorDeltaY = deltaY;
    },


    /**
     * Select and setup the current used cursor
     *
     * @type member
     * @return {void}
     */
    _renderCursor : function()
    {
      var vNewCursor;
      var vOldCursor = this.__cursor;

      switch(this.getCurrentAction())
      {
        case "move":
          vNewCursor = this.__cursors.move;
          break;

        case "copy":
          vNewCursor = this.__cursors.copy;
          break;

        case "alias":
          vNewCursor = this.__cursors.alias;
          break;

        default:
          vNewCursor = this.__cursors.nodrop;
      }

      // Hide old cursor
      if (vNewCursor != vOldCursor && vOldCursor != null) {
        vOldCursor._style.display = "none";
      }

      // Ensure that the cursor is created
      if (!vNewCursor._initialLayoutDone)
      {
        rwt.widgets.base.ClientDocument.getInstance().add(vNewCursor);
        rwt.widgets.base.Widget.flushGlobalQueues();
      }

      // Apply position with runtime style (fastest qooxdoo method)
      vNewCursor._renderRuntimeLeft(this.__dragCache.pageX + ((this._cursorDeltaX != null) ? this._cursorDeltaX : this.getDefaultCursorDeltaX()));
      vNewCursor._renderRuntimeTop(this.__dragCache.pageY + ((this._cursorDeltaY != null) ? this._cursorDeltaY : this.getDefaultCursorDeltaY()));

      // Finally show new cursor
      if (vNewCursor != vOldCursor) {
        vNewCursor._style.display = "";
      }

      // Store new cursor
      this.__cursor = vNewCursor;
    },




    /*
    ---------------------------------------------------------------------------
      IMPLEMENTATION OF DROP TARGET VALIDATION
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @param vWidget {var} TODOC
     */
    supportsDrop : function(vWidget)
    {
      var vTypes = vWidget.getDropDataTypes();

      if (!vTypes) {
        return false;
      }

      for (var i=0; i<vTypes.length; i++)
      {
        if (vTypes[i] in this.__data) {
          return true;
        }
      }

      return false;
    },




    /*
    ---------------------------------------------------------------------------
      ACTION HANDLING
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @param vAction {var} TODOC
     * @param vForce {var} TODOC
     * @return {void}
     */
    addAction : function(vAction, vForce)
    {
      this.__actions[vAction] = true;

      // Defaults to first added action
      if (vForce || this.getCurrentAction() == null) {
        this.setCurrentAction(vAction);
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @return {void}
     */
    clearActions : function()
    {
      this.__actions = {};
      this.setCurrentAction(null);
    },


    /**
     * TODOC
     *
     * @type member
     * @param vAction {var} TODOC
     * @return {void}
     */
    removeAction : function(vAction)
    {
      delete this.__actions[vAction];

      // Reset current action on remove
      if (this.getCurrentAction() == vAction) {
        this.setCurrentAction(null);
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @param vAction {var} TODOC
     * @return {void}
     */
    setAction : function(vAction)
    {
      if (vAction != null && !(vAction in this.__actions)) {
        this.addAction(vAction, true);
      } else {
        this.setCurrentAction(vAction);
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @param vKeyShift {var} TODOC
     * @param vKeyCtrl {var} TODOC
     * @param vKeyAlt {var} TODOC
     * @return {var | null} TODOC
     */
    _evalNewAction : function(vKeyShift, vKeyCtrl, vKeyAlt)
    {
      if (vKeyShift && vKeyCtrl && "alias" in this.__actions) {
        return "alias";
      } else if (vKeyShift && vKeyAlt && "copy" in this.__actions) {
        return "copy";
      } else if (vKeyShift && "move" in this.__actions) {
        return "move";
      } else if (vKeyAlt && "alias" in this.__actions) {
        return "alias";
      } else if (vKeyCtrl && "copy" in this.__actions) {
        return "copy";
      }
      else
      {
        // Return the first action found
        for (var vAction in this.__actions) {
          return vAction;
        }
      }

      return null;
    },




    /*
    ---------------------------------------------------------------------------
      USER FEEDBACK SUPPORT
    ---------------------------------------------------------------------------
    */

    /**
     * Sets the widget to show as feedback for the user. This widget should
     * represent the object(s) the user is dragging.
     *
     * @type member
     * @param widget {rwt.widgets.base.Widget} the feedback widget.
     * @param deltaX {int ? 10} the number of pixels the top-left corner of the widget
     *          should be away from the mouse cursor in x direction.
     * @param deltaY {int ? 10} the number of pixels the top-left corner of the widget
     *          should be away from the mouse cursor in y direction.
     * @param autoDisposeWidget {Boolean} whether the widget should be disposed when
     *          dragging is finished or cancelled.
     * @return {void}
     */
    setFeedbackWidget : function(widget, deltaX, deltaY, autoDisposeWidget)
    {
      this.__feedbackWidget = widget;
      this.__feedbackDeltaX = (deltaX != null) ? deltaX : 10;
      this.__feedbackDeltaY = (deltaY != null) ? deltaY : 10;
      this.__feedbackAutoDispose = autoDisposeWidget ? true : false;
    },


    /**
     * Renders the user feedback widget at the correct location.
     *
     * @type member
     * @return {void}
     */
    _renderFeedbackWidget : function()
    {
      if (this.__feedbackWidget)
      {
        this.__feedbackWidget.setVisibility(true);

        // Apply position with runtime style (fastest qooxdoo method)
        this.__feedbackWidget._renderRuntimeLeft(this.__dragCache.pageX + this.__feedbackDeltaX);
        this.__feedbackWidget._renderRuntimeTop(this.__dragCache.pageY + this.__feedbackDeltaY);
      }
    }
  },




  /*
  *****************************************************************************
     DESTRUCTOR
  *****************************************************************************
  */

  destruct : function()
  {
    this._disposeObjectDeep("__cursors", 1);
    this._disposeObjects("__feedbackWidget");
    this._disposeFields("__dragCache", "__data", "__actions", "__lastDestinationEvent");
  }
});
