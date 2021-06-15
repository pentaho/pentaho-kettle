/*******************************************************************************
 * Copyright (c) 2004, 2013 1&1 Internet AG, Germany, http://www.1und1.de,
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

/** The event object for drag and drop sessions */
rwt.qx.Class.define("rwt.event.DragEvent",
{
  extend : rwt.event.MouseEvent,




  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  construct : function(vType, vMouseEvent, vTarget, vRelatedTarget)
  {
    this._mouseEvent = vMouseEvent;

    var vOriginalTarget = null;

    switch(vType)
    {
      case "dragstart":
      case "dragover":
        vOriginalTarget = vMouseEvent.getOriginalTarget();
    }

    this.base(arguments, vType, vMouseEvent.getDomEvent(), vTarget.getElement(), vTarget, vOriginalTarget, vRelatedTarget);
  },




  /*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */

  members :
  {
    /*
    ---------------------------------------------------------------------------
      UTILITIY
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @return {var} TODOC
     */
    getMouseEvent : function() {
      return this._mouseEvent;
    },




    /*
    ---------------------------------------------------------------------------
      APPLICATION CONNECTION
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @return {void}
     * @throws TODOC
     */
    startDrag : function()
    {
      if (this.getType() != "dragstart") {
        throw new Error("rwt.event.DragEvent startDrag can only be called during the dragstart event: " + this.getType());
      }

      this.stopPropagation();
      rwt.event.DragAndDropHandler.getInstance().startDrag();
    },




    /*
    ---------------------------------------------------------------------------
      DATA SUPPORT
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @param sType {String} TODOC
     * @param oData {Object} TODOC
     * @return {void}
     */
    addData : function(sType, oData) {
      rwt.event.DragAndDropHandler.getInstance().addData(sType, oData);
    },


    /**
     * TODOC
     *
     * @type member
     * @param sType {String} TODOC
     * @return {var} TODOC
     */
    getData : function(sType) {
      return rwt.event.DragAndDropHandler.getInstance().getData(sType);
    },


    /**
     * TODOC
     *
     * @type member
     * @return {void}
     */
    clearData : function() {
      rwt.event.DragAndDropHandler.getInstance().clearData();
    },


    /**
     * TODOC
     *
     * @type member
     * @return {var} TODOC
     */
    getDropDataTypes : function() {
      return rwt.event.DragAndDropHandler.getInstance().getDropDataTypes();
    },




    /*
    ---------------------------------------------------------------------------
      ACTION SUPPORT
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @param sAction {String} TODOC
     * @return {void}
     */
    addAction : function(sAction) {
      rwt.event.DragAndDropHandler.getInstance().addAction(sAction);
    },


    /**
     * TODOC
     *
     * @type member
     * @param sAction {String} TODOC
     * @return {void}
     */
    removeAction : function(sAction) {
      rwt.event.DragAndDropHandler.getInstance().removeAction(sAction);
    },


    /**
     * TODOC
     *
     * @type member
     * @return {var} TODOC
     */
    getAction : function() {
      return rwt.event.DragAndDropHandler.getInstance().getCurrentAction();
    },


    /**
     * TODOC
     *
     * @type member
     * @return {void}
     */
    clearActions : function() {
      rwt.event.DragAndDropHandler.getInstance().clearActions();
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
    setFeedbackWidget : function(widget, deltaX, deltaY, autoDisposeWidget) {
      rwt.event.DragAndDropHandler.getInstance().setFeedbackWidget(widget, deltaX, deltaY, autoDisposeWidget);
    },




    /*
    ---------------------------------------------------------------------------
      CURSPOR POSITIONING SUPPORT
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
    setCursorPosition : function(deltaX, deltaY) {
      rwt.event.DragAndDropHandler.getInstance().setCursorPosition(deltaX, deltaY);
    }
  },




  /*
  *****************************************************************************
     DESTRUCTOR
  *****************************************************************************
  */

  destruct : function() {
    this._disposeFields("_mouseEvent");
  }
});
