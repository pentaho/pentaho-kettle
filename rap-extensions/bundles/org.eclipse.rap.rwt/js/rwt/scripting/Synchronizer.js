/*******************************************************************************
 * Copyright (c) 2012, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

(function(){

rwt.define( "rwt.scripting", {} );

// TODO [rst] Define directly using rwt.define, remove surrounding function scope
rwt.scripting.Synchronizer = function( widget ) {
  widget.addEventListener( "changeBackgroundColor", this._onChangeBackgroundColor, this );
  widget.addEventListener( "changeTextColor", this._onChangeTextColor, this );
  widget.addEventListener( "changeVisibility", this._onChangeVisibility, this );
  widget.addEventListener( "changeEnabled", this._onChangeEnabled, this );
  widget.addEventListener( "changeToolTipText", this._onChangeToolTipText, this );
  widget.addEventListener( "changeCursor", this._onChangeCursor, this );
  switch( widget.classname ) {
    case "rwt.widgets.Button":
      widget.addEventListener( "changeText", this._onChangeButtonText, this );
    break;
    case "rwt.widgets.ProgressBar":
      widget.addEventListener( "selectionChanged", this._onChangeProgressBarSelection, this );
    break;
    case "rwt.widgets.Label":
      widget.addEventListener( "changeText", this._onChangeLabelText, this );
    break;
  }

};

rwt.scripting.Synchronizer._ENABLE_KEY = "rwt.scripting.Synchronizer.ENABLED";

rwt.scripting.Synchronizer.enable = function( widget ) {
  widget.setUserData( this._ENABLE_KEY, true );
};

rwt.scripting.Synchronizer.disable = function( widget ) {
  widget.setUserData( this._ENABLE_KEY, false );
};

rwt.scripting.Synchronizer.prototype = { // TODO : use the getter defined by the proxy?

  _onChangeBackgroundColor : function( event ) {
    var widget = event.getTarget();
    var color = widget.__user$backgroundColor;
    this._sync( widget, "background", this._convertColor( color ) );
  },

  _onChangeTextColor : function( event ) {
    var widget = event.getTarget();
    var color = widget.__user$textColor;
    this._sync( widget, "foreground", this._convertColor( color ) );
  },

  _onChangeVisibility : function( event ) {
    var widget = event.getTarget();
    this._sync( widget, "visibility", widget.getVisibility() );
  },

  _onChangeEnabled : function( event ) {
    var widget = event.getTarget();
    this._sync( widget, "enabled", widget.getEnabled() );
  },

  _onChangeCursor : function( event ) {
    var widget = event.getTarget();
    this._sync( widget, "cursor", widget.__user$cursor || null );
  },

  _onChangeToolTipText : function( event ) {
    var widget = event.getTarget();
    this._sync( widget, "toolTip", widget.getToolTipText() );
  },

  // These events are fired by "dispatchSimpleEvent" in the widgets type handler or setter:

  _onChangeButtonText : function( widget ) {
    this._sync( widget, "text", widget.getCellContent( 2 ) );
  },

  _onChangeLabelText : function( widget ) {
    this._sync( widget, "text", widget.getCellContent( 1 ) );
  },

  _onChangeProgressBarSelection : function( widget ) {
    this._sync( widget, "selection", widget._selection );
  },

  /////////
  // helper

  _sync : function( widget, property, value ) {
    // TODO : use eventUtil.getSuspended instead, catches changes made during response
    if( widget.getUserData( rwt.scripting.Synchronizer._ENABLE_KEY ) ) {
      rap.getRemoteObject( widget ).set( property, value );
    }
  },

  _convertColor : function( color ) {
    var result = null;
    if( color != null ) {
      result = rwt.util.Colors.stringToRgb( color );
    }
    return result;
  }

};


}());
