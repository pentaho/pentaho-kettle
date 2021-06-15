/*******************************************************************************
 * Copyright (c) 2004, 2016 1&1 Internet AG, Germany, http://www.1und1.de,
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
 * @appearance tab-view
 */
rwt.qx.Class.define("rwt.widgets.TabFolder", {

  extend : rwt.widgets.base.BoxLayout,

  construct : function() {
    this.base( arguments );
    this.addEventListener( "changeFocused", rwt.widgets.util.TabUtil.onTabFolderChangeFocused );
    this.addEventListener( "keypress", rwt.widgets.util.TabUtil.onTabFolderKeyPress );
    this._bar = new rwt.widgets.base.TabFolderBar();
    this._pane = new rwt.widgets.base.TabFolderPane();
    this.add( this._bar, this._pane );
    this.setEnableElementFocus( false );
  },

  properties : {

    appearance : {
      refine : true,
      init : "tab-view"
    },

    orientation : {
      refine : true,
      init : "vertical"
    },

    alignTabsToLeft : {
      check : "Boolean",
      init : true,
      apply : "_applyAlignTabsToLeft"
    },

    placeBarOnTop : {
      check : "Boolean",
      init : true,
      apply : "_applyPlaceBarOnTop"
    }

  },

  members : {

    getPane : function() {
      return this._pane;
    },

    getBar : function() {
      return this._bar;
    },

    _applyDirection : function( value ) {
      this.base( arguments, value );
      var isRTL = value === "rtl";
      this._bar.setReverseChildrenOrder( isRTL );
      this.setAlignTabsToLeft( !isRTL );
    },

    _applyAlignTabsToLeft : function( value ) {
      this._bar.setHorizontalChildrenAlign( value ? "left" : "right" );
      // force re-apply of states for all tabs
      this._bar._addChildrenToStateQueue();
    },

    _applyPlaceBarOnTop : function( value ) {
      // This does not work if we use flexible zones
      // this.setReverseChildrenOrder(!value);
      // move bar around
      if( value ) {
        this._bar.moveSelfToBegin();
      } else {
        this._bar.moveSelfToEnd();
      }
      // force re-apply of states for all tabs
      this._bar._addChildrenToStateQueue();
    }

  },

  destruct : function() {
    this._disposeObjects( "_bar", "_pane" );
  }

});
