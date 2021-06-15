/*******************************************************************************
 * Copyright (c) 2009, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.Button", {

  extend : rwt.widgets.base.BasicButton,

  construct : function( buttonType ) {
    this.base( arguments, buttonType );
    this._alignment = buttonType === "arrow" ? "up" : "center";
    switch( buttonType ) {
     case "arrow":
       this.addState( "rwt_UP" );
       this.setAppearance( "push-button" );
     break;
     case "push":
     case "toggle":
       this.setAppearance( "push-button" );
     break;
     case "check":
       this.setAppearance( "check-box" );
     break;
     case "radio":
       this.setAppearance( "radio-button" );
    }
    this.initTabIndex();
    this._rawText = null;
    this._mnemonicIndex = null;
    this._markupEnabled = false;
  },

  destruct : function() {
    this.setMnemonicIndex( null );
  },

  properties : {

    tabIndex : {
      refine : true,
      init : 1
    }

  },

  members : {

    setMarkupEnabled : function( value ) {
      this._markupEnabled = value;
    },

    setText : function( value ) {
      this._rawText = value;
      this._mnemonicIndex = null;
      this._applyText( false );
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

    setAlignment : function( value ) {
      if( this.hasState( "rwt_ARROW" ) ) {
        this.removeState( "rwt_" + this._alignment.toUpperCase() );
        this.addState( "rwt_" + value.toUpperCase() );
      } else {
        this.setHorizontalChildrenAlign( value );
      }
      this._alignment = value;
    },

    setWrap : function( value ) {
      if( value ) {
        this.setFlexibleCell( 2 );
        this.setWordWrap( true );
      }
    },

    getToolTipTargetBounds : function() {
      if( this.getSelectionIndicator() ) {
        var styleTop = parseInt( this.getCellNode( 0 ).style.top, 10 );
        return  {
          "left" : this._cachedBorderLeft + this.getPaddingLeft(),
          "top" : this._cachedBorderTop + styleTop,
          "width" : this.getSelectionIndicator()[ 1 ],
          "height" : this.getSelectionIndicator()[ 2 ]
        };
      } else {
        return  {
          "left" : 0,
          "top" : 0,
          "width" : this.getBoxWidth(),
          "height" : this.getBoxHeight()
        };
      }
    },

    computeBadgePosition : function() {
      return [ -3, -5, "auto", "auto" ];
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
            this.setFocused( true );
            this.execute();
            event.success = true;
          }
        break;
      }
    },

    _applyText : function( mnemonic ) {
      var EncodingUtil = rwt.util.Encoding;
      if( this._rawText ) {
        var text = this._rawText;
        if( !this._markupEnabled ) {
          var mnemonicIndex = mnemonic ? this._mnemonicIndex : undefined;
          text = EncodingUtil.escapeText( this._rawText, mnemonicIndex );
          text = EncodingUtil.replaceNewLines( text, "<br/>" );
        }
        this.setCellContent( 2, text );
      } else {
        this.setCellContent( 2, null );
      }
    },

    //overwritten:
    _applyEnabled : function( value ) {
      this.base( arguments, value );
      if( this._mnemonicIndex !== null && !value ) {
        this._applyText( false );
      }
    },

    //overwritten:
    _afterRenderLayout : function() {
      if( this.getFocused() ) {
         this._showFocusIndicator();
      }
    },

    _ontabfocus : function() {
      this._showFocusIndicator();
    },

    _showFocusIndicator : function() {
      if( !rwt.widgets.util.FocusHandler.mouseFocus ) {
        var focusIndicator = rwt.widgets.util.FocusIndicator.getInstance();
        var node = this.getCellNode( 2 ) != null ? this.getCellNode( 2 ) : this.getCellNode( 1 );
        focusIndicator.show( this, "Button-FocusIndicator", node );
      }
    },

    _visualizeBlur : function() {
      this.base( arguments );
      rwt.widgets.util.FocusIndicator.getInstance().hide( this );
    },

    // overwritten:
    _notifySelected : function() {
      rwt.remote.EventUtil.notifySelected( this );
    }

  }
} );
