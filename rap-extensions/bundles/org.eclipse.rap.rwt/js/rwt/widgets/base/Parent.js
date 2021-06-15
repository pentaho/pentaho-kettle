/*******************************************************************************
 * Copyright (c) 2004, 2015 1&1 Internet AG, Germany, http://www.1und1.de,
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
 * Abstract base class for all widget which have child widgets (e.g. layout manager)
 *
 * Don't instantiate this class directly.
 */
rwt.qx.Class.define("rwt.widgets.base.Parent",
{
  extend : rwt.widgets.base.Widget,




  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  construct : function()
  {
    this.base(arguments);

    // Contains all children
    this._children = [];

    // Create instanceof layout implementation
    this._layoutImpl = this._createLayoutImpl();
  },




  /*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

  properties :
  {
    /** Individual focus handler for all child elements. */
    focusHandler :
    {
      check : "rwt.widgets.util.FocusHandler",
      apply : "_applyFocusHandler",
      nullable : true
    },

    /** The current active child. */
    activeChild :
    {
      check : "rwt.widgets.base.Widget",
      event : "changeActiveChild",
      nullable : true
    },

    /** The current focused child. */
    focusedChild :
    {
      check : "rwt.widgets.base.Widget",
      apply : "_applyFocusedChild",
      event : "changeFocusedChild",
      nullable : true
    },

    /** all visible child widgets */
    visibleChildren :
    {
      _cached      : true,
      defaultValue : null
    }
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
      FOCUS HANDLING
    ---------------------------------------------------------------------------
    */

    /**
     * Wether the widget has its own focus handler or uses one of its parent's
     * focus handler.
     *
     * @type member
     * @return {Boolean} whether the widget has its own focus handler
     */
    isFocusRoot : function() {
      return this.getFocusHandler() != null;
    },


    /**
     * Return the responsible focus handler
     *
     * @type member
     * @return {rwt.widgets.util.FocusHandler} TODOC
     */
    getFocusRoot : function()
    {
      if (this.isFocusRoot()) {
        return this;
      }

      if (this._hasParent) {
        return this.getParent().getFocusRoot();
      }

      return null;
    },


    /**
     * Let the widget use its own focus handler
     *
     * @type member
     * @return {void}
     */
    activateFocusRoot : function()
    {
      if (this._focusHandler) {
        return;
      }

      this._focusHandler = new rwt.widgets.util.FocusHandler(this);
      this.setFocusHandler(this._focusHandler);
    },


    /**
     * Delegate keyevent to the focus handler
     *
     * @type member
     * @param e {Event} TODOC
     * @return {void}
     */
    _onfocuskeyevent : function(e) {
      this.getFocusHandler()._onkeyevent(this, e);
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyFocusHandler : function(value)
    {
      if (value)
      {
        // Add Key Handler
        this.addEventListener("keypress", this._onfocuskeyevent);

        // Activate focus handling (but keep already configured tabIndex)
        if (this.getTabIndex() < 1) {
          this.setTabIndex(1);
        }

        // But hide the focus outline
        this.setHideFocus(true);

        // Make myself the default
        this.setActiveChild(this);
      }
      else
      {
        // Remove Key Handler
        this.removeEventListener("keydown", this._onfocuskeyevent);
        this.removeEventListener("keypress", this._onfocuskeyevent);

        // Deactivate focus handling
        this.setTabIndex(null);

        // Don't hide focus outline
        this.setHideFocus(false);
      }
    },

    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyFocusedChild : function(value, old)
    {
      var vFocusValid = value != null;
      var vBlurValid = old != null;

      if (rwt.qx.Class.isDefined("rwt.widgets.util.PopupManager") && vFocusValid)
      {
        var vMgr = rwt.widgets.util.PopupManager.getInstance();

        if (vMgr) {
          vMgr.update(value);
        }
      }

      if (vBlurValid)
      {
        // Dispatch FocusOut
        if (old.hasEventListeners("focusout"))
        {
          var vEventObject = new rwt.event.FocusEvent("focusout", old);

          if (vFocusValid) {
            vEventObject.setRelatedTarget(value);
          }

          old.dispatchEvent(vEventObject);
          vEventObject.dispose();
        }
      }

      if (vFocusValid)
      {
        if (value.hasEventListeners("focusin"))
        {
          // Dispatch FocusIn
          var vEventObject = new rwt.event.FocusEvent("focusin", value);

          if (vBlurValid) {
            vEventObject.setRelatedTarget(old);
          }

          value.dispatchEvent(vEventObject);
          vEventObject.dispose();
        }
      }

      if (vBlurValid)
      {
        if (this.getActiveChild() == old && !vFocusValid) {
          this.setActiveChild(null);
        }

        old.setFocused(false);

        // Dispatch Blur
        var vEventObject = new rwt.event.FocusEvent("blur", old);

        if (vFocusValid) {
          vEventObject.setRelatedTarget(value);
        }

        old.dispatchEvent(vEventObject);

        var vMgr = rwt.widgets.util.ToolTipManager.getInstance();

        if (vMgr) {
          vMgr.handleBlur(vEventObject);
        }

        vEventObject.dispose();
      }

      if (vFocusValid)
      {
        this.setActiveChild(value);
        value.setFocused(true);
        rwt.event.EventHandler.setFocusRoot(this);

        // Dispatch Focus
        var vEventObject = new rwt.event.FocusEvent("focus", value);

        if (vBlurValid) {
          vEventObject.setRelatedTarget(old);
        }

        value.dispatchEvent(vEventObject);

        var vMgr = rwt.widgets.util.ToolTipManager.getInstance();

        if (vMgr) {
          vMgr.handleFocus(vEventObject);
        }

        vEventObject.dispose();
      }
    },

    _applyWidth : function( newValue, oldValue ) {
      this.base( arguments, newValue, oldValue );
      this._layoutX();
    },

    _applyHeight : function( newValue, oldValue ) {
      this.base( arguments, newValue, oldValue );
      this._layoutY();
    },

    _applyBorder : function( newValue, oldValue ) {
      this.base( arguments, newValue, oldValue );
      this._layoutX();
      this._layoutY();
    },

    _applyDirection : function( value ) {
      this.base( arguments, value );
      this._layoutX();
    },

    _layoutX : function() {},

    _layoutY : function() {},


    /*
    ---------------------------------------------------------------------------
      LAYOUT IMPLEMENTATION
    ---------------------------------------------------------------------------
    */

    _layoutImpl : null,


    /**
     * abstract method. Create layout implementation.
     *
     * This method must be overwritten by all subclasses
     *
     * return
     *
     * @type member
     * @return {rwt.widgets.base.BoxLayout} TODOC
     */
    _createLayoutImpl : function() {
      return new rwt.widgets.util.CanvasLayoutImpl( this);
    },


    /**
     * Return the layout implementation.
     *
     * return {rwt.widgets.util.LayoutImpl}
     *
     * @type member
     * @return {var} TODOC
     */
    getLayoutImpl : function() {
      return this._layoutImpl;
    },




    /*
    ---------------------------------------------------------------------------
      CHILDREN MANAGMENT: MANAGE ALL
    ---------------------------------------------------------------------------
    */

    /**
     * Return the array of all children
     *
     * @type member
     * @return {rwt.widgets.base.Widget[]} all children
     */
    getChildren : function() {
      return this._children;
    },


    /**
     * Get number of children
     *
     * @type member
     * @return {Integer} number of children
     */
    getChildrenLength : function() {
      return this.getChildren().length;
    },


    /**
     * Check if the widget has children
     *
     * @type member
     * @return {Boolean} whether the widget has children
     */
    hasChildren : function() {
      return this.getChildrenLength() > 0;
    },


    /**
     * Check if there are any children inside
     *
     * @type member
     * @return {Boolean} whether the number of children is 0
     */
    isEmpty : function() {
      return this.getChildrenLength() === 0;
    },


    /**
     * Get the index of a child widget.
     *
     * @type member
     * @param vChild {rwt.widgets.base.Widget} Child widget to get the index for
     * @return {Integer} index of the child widget
     */
    indexOf : function(vChild) {
      return this.getChildren().indexOf(vChild);
    },


    /**
     * Check if the given widget is a child
     *
     * @type member
     * @param vWidget {rwt.widgets.base.Widget} The widget which should be checked.
     * @return {Boolean | var} TODOC
     */
    contains : function(vWidget)
    {
      switch(vWidget)
      {
        case null:
          return false;

        case this:
          return true;

        default:
          // try the next parent of the widget (recursive until found)
          return this.contains(vWidget.getParent());
      }
    },




    /*
    ---------------------------------------------------------------------------
      CHILDREN MANAGMENT: MANAGE VISIBLE ONES

      uses a cached private property
    ---------------------------------------------------------------------------
    */

    /**
     * Return the array of all visible children
     * (which are configured as visible=true)
     *
     * @type member
     * @return {rwt.widgets.base.Widget[]} all visible children
     */
    _computeVisibleChildren : function()
    {
      var vVisible = [];
      var vChildren = this.getChildren();

      if (! vChildren)
      {
        return 0;
      }

      var vLength = vChildren.length;

      for (var i=0; i<vLength; i++)
      {
        var vChild = vChildren[i];

        if (vChild._isDisplayable ) {
          vVisible.push(vChild);
        }
      }

      return vVisible;
    },


    /**
     * Get number of visible children
     *
     * @type member
     * @return {Integer} number of visible children
     */
    getVisibleChildrenLength : function() {
      return this.getVisibleChildren().length;
    },


    /**
     * Check if the widget has any visible children
     *
     * @type member
     * @return {Boolean} whether the widget has any visible children
     */
    hasVisibleChildren : function() {
      return this.getVisibleChildrenLength() > 0;
    },


    /**
     * Check whether there are any visible children inside
     *
     * @type member
     * @return {Boolean} whether there are any visible children inside
     */
    isVisibleEmpty : function() {
      return this.getVisibleChildrenLength() === 0;
    },




    /*
    ---------------------------------------------------------------------------
      CHILDREN MANAGMENT: ADD
    ---------------------------------------------------------------------------
    */

    /**
     * Add/Append another widget. Allows to add multiple at
     *  one, a parameter could be a widget.
     *
     * @type member
     * @param varargs {rwt.widgets.base.Widget} variable number of widgets to add
     * @return {Parent} This widget.
     * @throws TODOC
     */
    add : function()
    {
      var vWidget;

      for (var i=0, l=arguments.length; i<l; i++)
      {
        vWidget = arguments[i];
        vWidget.setParent( this );
      }

      return this;
    },


    /**
     * Add a child widget at the specified index
     *
     * @type member
     * @param vChild {widget} widget to add
     * @param vIndex {Integer} Index, at which the widget will be inserted
     */
    addAt : function(vChild, vIndex)
    {
      if (vIndex == null || vIndex < 0) {
        throw new Error("Not a valid index for addAt(): " + vIndex);
      }

      if (vChild.getParent() == this)
      {
        var vChildren = this.getChildren();
        var vOldIndex = vChildren.indexOf(vChild);

        if (vOldIndex != vIndex)
        {
          if (vOldIndex != -1) {
            rwt.util.Arrays.removeAt(vChildren, vOldIndex);
          }

          rwt.util.Arrays.insertAt(vChildren, vChild, vIndex);

          if (this._initialLayoutDone)
          {
            this._invalidateVisibleChildren();
            this.getLayoutImpl().updateChildrenOnMoveChild(vChild, vIndex, vOldIndex);
          }
        }
      }
      else
      {
        vChild._insertIndex = vIndex;
        vChild.setParent(this);
      }
    },


    /**
     * Add a child widget as the first widget
     *
     * @type member
     * @param vChild {widget} widget to add
     */
    addAtBegin : function(vChild) {
      return this.addAt(vChild, 0);
    },


    /**
     * Add a child widget as the last widget
     *
     * @type member
     * @param vChild {widget} widget to add
     */
    addAtEnd : function(vChild)
    {
      // we need to fix here, when the child is already inside myself, but
      // want to change its position
      var vLength = this.getChildrenLength();
      return this.addAt(vChild, vChild.getParent() == this ? vLength - 1 : vLength);
    },


    /**
     * Add a widget before another already inserted widget
     *
     * @type member
     * @param vChild {var} widget to add
     * @param vBefore {var} widget before the new widget will be inserted.
     */
    addBefore : function(vChild, vBefore)
    {
      var vChildren = this.getChildren();
      var vTargetIndex = vChildren.indexOf(vBefore);

      if (vTargetIndex == -1) {
        throw new Error("Child to add before: " + vBefore + " is not inside this parent.");
      }

      var vSourceIndex = vChildren.indexOf(vChild);

      if (vSourceIndex == -1 || vSourceIndex > vTargetIndex) {
        vTargetIndex++;
      }

      return this.addAt(vChild, Math.max(0, vTargetIndex - 1));
    },


    /**
     * Add a widget after another already inserted widget
     *
     * @type member
     * @param vChild {var} widget to add
     * @param vAfter {var} widgert, after which the new widget will be inserted
     */
    addAfter : function(vChild, vAfter)
    {
      var vChildren = this.getChildren();
      var vTargetIndex = vChildren.indexOf(vAfter);

      if (vTargetIndex == -1) {
        throw new Error("Child to add after: " + vAfter + " is not inside this parent.");
      }

      var vSourceIndex = vChildren.indexOf(vChild);

      if (vSourceIndex != -1 && vSourceIndex < vTargetIndex) {
        vTargetIndex--;
      }

      return this.addAt(vChild, Math.min(vChildren.length, vTargetIndex + 1));
    },




    /*
    ---------------------------------------------------------------------------
      CHILDREN MANAGMENT: REMOVE
    ---------------------------------------------------------------------------
    */

    /**
     * Remove one or multiple childrens.
     *
     * @type member
     * @param varargs {rwt.widgets.base.Widget} variable number of widgets to remove
     */
    remove : function( /*children*/ )
    {
      var vWidget;

      for (var i=0, l=arguments.length; i<l; i++)
      {
        vWidget = arguments[i];

        if (!(vWidget instanceof rwt.widgets.base.Parent) && !(vWidget instanceof rwt.widgets.base.Terminator)) {
          throw new Error("Invalid Widget: " + vWidget);
        } else if (vWidget.getParent() == this) {
          vWidget.setParent(null);
        }
      }
    },


    /**
     * Remove the widget at the specified index.
     *
     * @type member
     * @param vIndex {Integer} Index of the widget to remove.
     */
    removeAt : function(vIndex)
    {
      var vChild = this.getChildren()[vIndex];

      if (vChild)
      {
        delete vChild._insertIndex;

        vChild.setParent(null);
      }
    },


    /**
     * Remove all children.
     *
     * @type member
     */
    removeAll : function()
    {
      var cs = this.getChildren();
      var co = cs[0];

      while (co)
      {
        this.remove(co);
        co = cs[0];
      }
    },




    /*
    ---------------------------------------------------------------------------
      CHILDREN MANAGMENT: FIRST CHILD
    ---------------------------------------------------------------------------
    */

    /**
     * Get the first child
     *
     * @type member
     * @return {Widget|null} First child widget (null if this widget does not have any children)
     */
    getFirstChild : function() {
      return rwt.util.Arrays.getFirst(this.getChildren()) || null;
    },


    /**
     * Get the first visible child
     *
     * @type member
     * @return {Widget|null} First visible child widget (null if this widget does
     *     not have any visible children)
     */
    getFirstVisibleChild : function() {
      return rwt.util.Arrays.getFirst(this.getVisibleChildren()) || null;
    },


    /**
     * Get the first active child
     *
     * @type member
     * @param vIgnoreClasses {Class[]} array of classes which should be ignored
     * @return {Widget|null} First active child widget (null if this widget does
     *     not have any active children)
     */
    getFirstActiveChild : function(vIgnoreClasses) {
      return rwt.widgets.base.Widget.getActiveSiblingHelper(null, this, 1, vIgnoreClasses, "first") || null;
    },




    /*
    ---------------------------------------------------------------------------
      CHILDREN MANAGMENT: LAST CHILD
    ---------------------------------------------------------------------------
    */

    /**
     * Get the last child
     *
     * @type member
     * @return {Widget|null} Last child widget (null if this widget does
     *     not have any children)
     */
    getLastChild : function() {
      return rwt.util.Arrays.getLast(this.getChildren()) || null;
    },


    /**
     * Get the last visible child
     *
     * @type member
     * @return {Widget|null} Last visible child widget (null if this widget does
     *     not have any visible children)
     */
    getLastVisibleChild : function() {
      return rwt.util.Arrays.getLast(this.getVisibleChildren()) || null;
    },


    /**
     * Get the last active child
     *
     * @type member
     * @param vIgnoreClasses {Class[]} array of classes which should be ignored
     * @return {Widget|null} Last active child widget (null if this widget does
     *     not have any active children)
     */
    getLastActiveChild : function(vIgnoreClasses) {
      return rwt.widgets.base.Widget.getActiveSiblingHelper(null, this, -1, vIgnoreClasses, "last") || null;
    },




    /*
    ---------------------------------------------------------------------------
      CHILDREN MANAGMENT: LOOP UTILS
    ---------------------------------------------------------------------------
    */

    /**
     * Call a callbach function for each child widget. The callback has the following signature:
     * <code>function(childWidget, widgetIndex)</code>. The first parameter is the child widget
     * and the second the index of the child widget in its parent.
     *
     * @type member
     * @param vFunc {Function} callback function. Signature: <code>function(childWidget, widgetIndex)</code>
     */
    forEachChild : function(vFunc)
    {
      var ch = this.getChildren(), chc, i = -1;

      if (! ch) {
        return;
      }

      chc = ch[++i];
      while( chc ) {
        vFunc.call(chc, i);
        chc = ch[++i];
      }
    },


    /**
     * Call a callbach function for each visible child widget. The callback has the following signature:
     * <code>function(childWidget, widgetIndex)</code>. The first parameter is the child widget
     * and the second the index of the child widget in its parent.
     *
     * @type member
     * @param vFunc {Function} callback function. Signature: <code>function(childWidget, widgetIndex)</code>
     */
    forEachVisibleChild : function(vFunc)
    {
      var ch = this.getVisibleChildren(), chc, i = -1;

      if (! ch) {
        return;
      }

      chc = ch[++i];
      while( chc ) {
        vFunc.call(chc, i);
        chc = ch[++i];
      }
    },

    _markInDispose : function() {
      this._isInGlobalDisposeQueue = true;
      if( this.forEachChild ) {
        this.forEachChild( arguments.callee );
      }
    },

    // overridden
    _fireBeforeDispose : function() {
      this.base( arguments );
      if( this.forEachChild ) {
        this.forEachChild( arguments.callee );
      }
    },

    /*
    ---------------------------------------------------------------------------
      APPEAR/DISAPPEAR MESSAGES FOR CHILDREN
    ---------------------------------------------------------------------------
    */

    // overridden
    _beforeAppear : function()
    {
      this.base(arguments);

      this.forEachVisibleChild(function()
      {
        if (this.isAppearRelevant()) {
          this._beforeAppear();
        }
      });
    },


    // overridden
    _afterAppear : function()
    {
      this.base(arguments);

      this.forEachVisibleChild(function()
      {
        if (this.isAppearRelevant()) {
          this._afterAppear();
        }
      });
    },

    // overridden
    _beforeDisappear : function()
    {
      this.base(arguments);

      this.forEachVisibleChild(function()
      {
        if (this.isAppearRelevant()) {
          this._beforeDisappear();
        }
      });
    },


    // overridden
    _afterDisappear : function()
    {
      this.base(arguments);

      this.forEachVisibleChild(function()
      {
        if (this.isAppearRelevant()) {
          this._afterDisappear();
        }
      });
    },


    // overridden
    _afterInsertDom : function() {
      this.base( arguments );
      // NOTE: a "visible" Child is actually every "displayable" child here
      this.forEachVisibleChild( function() {
        // Check if the nodes are actually connected - See Bug 359665
        if(    this.getElement()
            && this.getElement().parentNode === this.getParent()._getTargetNode() )
        {
          this._afterInsertDom();
        }
      } );
    },



    // overridden
    _afterRemoveDom : function() {
      this.base( arguments );
      this.forEachVisibleChild( function() {
        if( this.isInDom() ) {
          this._afterRemoveDom();
        }
      } );
    },



    /*
    ---------------------------------------------------------------------------
      DISPLAYBLE HANDLING
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @param vDisplayable {var} TODOC
     * @param vParent {var} TODOC
     * @param vHint {var} TODOC
     * @return {void}
     */
    _handleDisplayableCustom : function()
    {
      this.forEachChild(function() {
        this._handleDisplayable();
      });
    },




    /*
    ---------------------------------------------------------------------------
      STATE QUEUE
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @return {void}
     */
    _addChildrenToStateQueue : function()
    {
      this.forEachVisibleChild(function() {
        this.addToStateQueue();
      });
    },


    // overridden
    recursiveAddToStateQueue : function()
    {
      this.addToStateQueue();

      this.forEachVisibleChild(function() {
        this.recursiveAddToStateQueue();
      });
    },


    // overridden
    _recursiveAppearanceThemeUpdate : function(vNewAppearanceTheme, vOldAppearanceTheme)
    {
      this.base(arguments, vNewAppearanceTheme, vOldAppearanceTheme);

      this.forEachVisibleChild(function() {
        this._recursiveAppearanceThemeUpdate(vNewAppearanceTheme, vOldAppearanceTheme);
      });
    },




    /*
    ---------------------------------------------------------------------------
      CHILDREN QUEUE
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @param vChild {var} TODOC
     * @return {void}
     */
    _addChildToChildrenQueue : function(vChild)
    {
      if (!vChild._isInParentChildrenQueue && vChild._isDisplayable)
      {
        rwt.widgets.base.Widget.addToGlobalLayoutQueue(this);

        if (!this._childrenQueue) {
          this._childrenQueue = {};
        }

        this._childrenQueue[vChild.toHashCode()] = vChild;
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @param vChild {var} TODOC
     * @return {void}
     */
    _removeChildFromChildrenQueue : function(vChild)
    {
      if (this._childrenQueue && vChild._isInParentChildrenQueue)
      {
        delete this._childrenQueue[vChild.toHashCode()];

        if (rwt.util.Objects.isEmpty(this._childrenQueue))
        {
          this._childrenQueue = {};
          rwt.widgets.base.Widget.removeFromGlobalLayoutQueue(this);
        }
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @return {void}
     */
    _flushChildrenQueue : function()
    {
      if (!rwt.util.Objects.isEmpty(this._childrenQueue))
      {
        this.getLayoutImpl().flushChildrenQueue(this._childrenQueue);
        delete this._childrenQueue;
      }
    },




    /*
    ---------------------------------------------------------------------------
      LAYOUT QUEUE
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @param p {var} TODOC
     * @return {void}
     */
    _addChildrenToLayoutQueue : function(p)
    {
      this.forEachChild(function() {
        this.addToLayoutChanges(p);
      });
    },


    /**
     * TODOC
     *
     * @type member
     * @param vChild {var} TODOC
     * @return {void}
     */
    _layoutChild : function(vChild)
    {
      if (!vChild._isDisplayable)
      {
        // this.warn("Want to render an invisible child: " + vChild + " -> omitting!");
        return;
      }

      // APPLY LAYOUT
      var vChanges = vChild._layoutChanges;

      if (vChild.renderBorder && vChanges.border ) {
        vChild.renderBorder( vChanges );
      }

      if (vChild.renderPadding)
      {
        if (vChanges.paddingLeft || vChanges.paddingRight || vChanges.paddingTop || vChanges.paddingBottom) {
          vChild.renderPadding(vChanges);
        }
      }

      // WRAP TO LAYOUT ENGINE
      this.getLayoutImpl().layoutChild(vChild, vChanges);

      // POST LAYOUT
      vChild._layoutPost(vChanges);

      // DISPLAY DOM NODE
      // insert dom node (if initial flag enabled)
      if (vChanges.initial)
      {
        vChild._initialLayoutDone = true;
        rwt.widgets.base.Widget.addToGlobalDisplayQueue(vChild);
      }

      // CLEANUP
      vChild._layoutChanges = {};

      delete vChild._isInParentLayoutQueue;
      delete this._childrenQueue[vChild.toHashCode()];
    },

    _layoutPost : function( changes ) {
      this.createDispatchDataEvent( "flush", changes );
    },



    /*
    ---------------------------------------------------------------------------
      DIMENSION CACHE
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @return {var} TODOC
     */
    _computePreferredInnerWidth : function() {
      return this.getLayoutImpl().computeChildrenNeededWidth();
    },


    /**
     * TODOC
     *
     * @type member
     * @return {var} TODOC
     */
    _computePreferredInnerHeight : function() {
      return this.getLayoutImpl().computeChildrenNeededHeight();
    },


    /**
     * TODOC
     *
     * @type member
     * @param vNew {var} TODOC
     * @param vOld {var} TODOC
     * @return {void}
     */
    _changeInnerWidth : function()
    {
      var vLayout = this.getLayoutImpl();

      if (vLayout.invalidateChildrenFlexWidth) {
        vLayout.invalidateChildrenFlexWidth();
      }

      this.forEachVisibleChild(function()
      {
        if (vLayout.updateChildOnInnerWidthChange(this) && this._recomputeBoxWidth())
        {
          this._recomputeOuterWidth();
          this._recomputeInnerWidth();
        }
      });
    },


    /**
     * TODOC
     *
     * @type member
     * @param vNew {var} TODOC
     * @param vOld {var} TODOC
     * @return {void}
     */
    _changeInnerHeight : function()
    {
      var vLayout = this.getLayoutImpl();

      if (vLayout.invalidateChildrenFlexHeight) {
        vLayout.invalidateChildrenFlexHeight();
      }

      this.forEachVisibleChild(function()
      {
        if (vLayout.updateChildOnInnerHeightChange(this) && this._recomputeBoxHeight())
        {
          this._recomputeOuterHeight();
          this._recomputeInnerHeight();
        }
      });
    },


    /**
     * TODOC
     *
     * @type member
     * @param vChild {var} TODOC
     * @return {var} TODOC
     */
    getInnerWidthForChild : function() {
      return this.getInnerWidth();
    },


    /**
     * TODOC
     *
     * @type member
     * @param vChild {var} TODOC
     * @return {var} TODOC
     */
    getInnerHeightForChild : function() {
      return this.getInnerHeight();
    },






    /*
    ---------------------------------------------------------------------------
      REMAPPING
    ---------------------------------------------------------------------------
    */

    _remappingChildTable : [ "add", "remove", "addAt", "addAtBegin", "addAtEnd", "removeAt", "addBefore", "addAfter", "removeAll" ],

    _remapStart : "return this._remappingChildTarget.",
    _remapStop : ".apply(this._remappingChildTarget, arguments)",


    /**
     * TODOC
     *
     * @type member
     * @param vTarget {var} TODOC
     * @return {void}
     */
    remapChildrenHandlingTo : function(vTarget)
    {
      var t = this._remappingChildTable;

      this._remappingChildTarget = vTarget;

      for (var i=0, l=t.length, s; i<l; i++)
      {
        s = t[i];
        this[s] = new Function(rwt.widgets.base.Parent.prototype._remapStart + s + rwt.widgets.base.Parent.prototype._remapStop);
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
    this._disposeObjectDeep("_children", 1);
    this._disposeObjects("_layoutImpl", "_focusHandler");
    this._disposeFields("_childrenQueue", "_childrenQueue", "_remappingChildTable",
      "_remappingChildTarget", "_cachedVisibleChildren");
  }
});
