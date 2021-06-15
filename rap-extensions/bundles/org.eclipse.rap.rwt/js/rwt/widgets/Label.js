/*******************************************************************************
 * Copyright (c) 2013, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.Label", {

  extend : rwt.widgets.base.MultiCellWidget,

  include : rwt.widgets.util.OverStateMixin,

  construct : function( styles ) {
    this.base( arguments, this._CELLORDER );
    this.setVerticalChildrenAlign( "top" );
    this.setAlignment( "left" );
    this.setAppearance( "label-wrapper" );
    if( styles.WRAP ) {
      this.setFlexibleCell( 1 );
      this.setWordWrap( true );
    }
    this._markupEnabled = styles.MARKUP_ENABLED === true;
    this._rawText = null;
    this._mnemonicIndex = null;
  },

  destruct : function() {
    this.setMnemonicIndex( null );
  },

  members : {

    _CELLORDER : [ "image", "label" ],

    setAlignment : function( value ) {
      this.setHorizontalChildrenAlign( value );
    },

    setImage : function( image ) {
      if( image ) {
        this.setCellContent( 0, image[ 0 ] );
        this.setCellDimension( 0, image[ 1 ], image[ 2 ] );
      } else {
        this.setCellContent( 0, null );
        this.setCellDimension( 0, 0, 0 );
      }
    },

    setText : function( value ) {
      this._rawText = value;
      this._applyText( false );
      this._mnemonicIndex = null;
      this.dispatchSimpleEvent( "changeText", this ); // used by Synchronizer.js
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

    _applyText : function( mnemonic ) {
      var text = this._rawText;
      if( text ) {
        if( !this._markupEnabled ) {
          var EncodingUtil = rwt.util.Encoding;
          var mnemonicIndex = mnemonic ? this._mnemonicIndex : undefined;
          // Order is important here: escapeText, replace line breaks
          text = EncodingUtil.escapeText( this._rawText, mnemonicIndex );
          text = EncodingUtil.replaceNewLines( text, "<br/>" );
          text = EncodingUtil.replaceWhiteSpaces( text ); // fixes bug 192634
        }
        this.setCellContent( 1, text );
      } else {
        this.setCellContent( 1, null );
      }
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
            var focusWidget = this.getNextSibling();
            if( focusWidget && focusWidget.getVisibility() && focusWidget.getEnabled() ) {
              focusWidget.focus();
            }
            event.success = true;
          }
        break;
      }
    },

    setTopMargin : function( value ) {
      this.setPaddingTop( value );
    },

    setLeftMargin : function( value ) {
      this.setPaddingLeft( value );
    },

    setRightMargin : function( value ) {
      this.setPaddingRight( value );
    },

    setBottomMargin : function( value ) {
      this.setPaddingBottom( value );
    }

  }

} );
