/*******************************************************************************
 * Copyright (c) 2002, 2016 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.Group", {

  extend : rwt.widgets.base.Parent,

  construct : function() {
    this.base( arguments );
    this._frame = new rwt.widgets.base.Parent();
    this._frame.setAppearance( "group-box-frame" );
    this.add( this._frame );
    this._legend = new rwt.widgets.base.MultiCellWidget( [ "label" ] );
    this._legend.setAppearance( "group-box-legend" );
    this.add( this._legend );
    this.setOverflow( "hidden" );
    this.setEnableElementFocus( false );
    var themeValues = new rwt.theme.ThemeValues( {} );
    this._themeBackgroundColor = themeValues.getCssColor( "Group-Label", "background-color" );
    themeValues.dispose();
    this._legend.addEventListener( "mouseover", this._onMouseOver, this );
    this._legend.addEventListener( "mouseout", this._onMouseOut, this );
    this._legend.setTop( 0 );
    this._legend.setLeft( 0 );
    // Disable scrolling (see bug 345903)
    rwt.widgets.base.Widget.disableScrolling( this );
  },

  destruct : function() {
    this._disposeObjects( "_legend", "_frame" );
    this.setMnemonicIndex( null );
  },

  properties : {

    appearance : {
      refine : true,
      init : "group-box"
    }

  },

  members : {

    setText : function( value ) {
      this._rawText = value;
      this._mnemonicIndex = null;
      this._applyText( false );
    },

    setMnemonicIndex : function( value ) {
      this._mnemonicIndex = value;
      var mnemonicHandler = rwt.widgets.util.MnemonicHandler.getInstance();
      if( ( typeof value === "number" ) && ( value >= 0 ) ) {
        mnemonicHandler.add( this, this._onMnemonic );
      } else {
        mnemonicHandler.remove( this );
      }
    },

    getMnemonicIndex : function() {
      return this._mnemonicIndex;
    },

    _onMnemonic : function( event ) {
      switch( event.type ) {
        case "show":
          this._applyText( true );
        break;
        case "hide":
          this._applyText( false );
        break;
        case "trigger":
          var charCode = this._rawText.toUpperCase().charCodeAt( this._mnemonicIndex );
          if( event.charCode === charCode ) {
            var widget = this._findFirstFocusableChild( this );
            if( widget != null ) {
              widget.focus();
            }
            event.success = true;
          }
        break;
      }
    },

    _applyText : function( mnemonic ) {
      var EncodingUtil = rwt.util.Encoding;
      if( this._rawText ) {
        var mnemonicIndex = mnemonic ? this._mnemonicIndex : undefined;
        var text = EncodingUtil.escapeText( this._rawText, mnemonicIndex );
        if( this.hasState( "rwt_WRAP" ) ) {
          text = EncodingUtil.replaceNewLines( text, "<br/>" );
        }
        this._setLegend( text );
      } else {
        this._setLegend( null );
      }
    },

    _getSubWidgets : function() {
      return [ this._legend, this._frame ];
    },

    _applyBackgroundColor : function( value, old ) {
      this.base( arguments, value, old );
      if( this._themeBackgroundColor === "undefined" ) {
        this._legend.setBackgroundColor( value );
      }
    },

    _applyFont : function( value, old ) {
      this.base( arguments, value, old );
      this._legend.setFont( value );
    },

    _onMouseOver : function() {
      this._legend.addState( "over" );
      this._frame.addState( "over" );
    },

    _onMouseOut : function() {
      this._legend.removeState( "over" );
      this._frame.removeState( "over" );
    },

    _applyDirection : function( value ) {
      this.base( arguments, value );
      this.getLayoutImpl().setMirror( value === "rtl" );
    },

    _layoutPost : function( changes ) {
      this.base( arguments, changes );
      this._frame._layoutPost( changes );
    },

    //////////////////
    // apply subelement IDs

    applyObjectId : function( id ) {
      this.base( arguments, id );
      this._legend.applyObjectId( id + "-label" );
    },

    _setLegend : function( text ) {
      if( text !== "" && text !== null ) {
        this._legend.setCellContent( 0, text );
        this._legend.setDisplay( true );
      } else {
        this._legend.setDisplay( false );
      }
    },

    _findFirstFocusableChild : function( parent ) {
      var ObjectRegistry = rwt.remote.ObjectRegistry;
      var WidgetUtil = rwt.widgets.util.WidgetUtil;
      var result = null;
      var ids = WidgetUtil.getChildIds( parent );
      for( var i = 0; i < ids.length && result === null; i++ ) {
        var child = ObjectRegistry.getObject( ids[ i ] );
        if( WidgetUtil.getChildIds( child ) ) {
          result = this._findFirstFocusableChild( child );
        } else if( child.isSeeable() && child.isEnabled() ) {
          result = child;
        }
      }
      return result;
    }

  }

} );
