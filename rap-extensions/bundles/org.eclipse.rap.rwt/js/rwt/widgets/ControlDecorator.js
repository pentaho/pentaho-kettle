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

rwt.qx.Class.define( "rwt.widgets.ControlDecorator", {

  extend : rwt.widgets.base.Image,

  construct : function() {
    this.base( arguments );
    this.setZIndex( 1000 );
    this.setVisibility( false );
    this._showHover = true;
    this._text = null;
  },

  members : {

    setMarkupEnabled : function( value ) {
      this.setUserData( "toolTipMarkupEnabled", value );
    },

    setText : function( value ) {
      this._text = value;
      this._updateToolTip();
    },

    setShowHover : function( value ) {
      this._showHover = value;
      this._updateToolTip();
    },

    _updateToolTip : function() {
      var wm = rwt.remote.WidgetManager.getInstance();
      if( this._text === null || this._text === "" || !this._showHover ) {
        wm.setToolTip( this, null );
      } else {
        wm.setToolTip( this, this._text );
      }
    },

    setHasSelectionListener : function( value ) {
      var eventUtil = rwt.remote.EventUtil;
      if( value ) {
        this.addEventListener( "mousedown", eventUtil.widgetSelected, this );
      } else {
        this.removeEventListener( "mousedown", eventUtil.widgetSelected, this );
      }
    },

    setHasDefaultSelectionListener : function( value ) {
      var eventUtil = rwt.remote.EventUtil;
      if( value ) {
        this.addEventListener( "dblclick", eventUtil.widgetDefaultSelected, this );
      } else {
        this.removeEventListener( "dblclick", eventUtil.widgetDefaultSelected, this );
      }
    }

  }
} );
