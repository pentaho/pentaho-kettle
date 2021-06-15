/*******************************************************************************
 * Copyright (c) 2009, 2017 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

(function($){

rwt.qx.Class.define( "rwt.widgets.ToolItem", {

  extend : rwt.widgets.base.BasicButton,

  construct : function( itemType, vertical ) {
    this.base( arguments, itemType );
    this._isDropDown = false;
    this.setAppearance( "toolbar-button" );
    if( itemType == "dropDown" ) {
      this._isDropDown = true;
      this._isSelectable = false;
      this._isDeselectable = false;
      this._sendEvent = true;
      this.$dropDownArrow = $( "<div>" ).css({
        position: "absolute"
      });
      this.$separator = $( "<div>" ).css({
        position: "absolute",
        top: 0,
        right: 0,
        left: "auto",
        width: 1,
        height: "100%"
      });
    }
    if( vertical ) {
      this.addState( "rwt_VERTICAL" );
    }
    this._rawText = null;
    this._mnemonicIndex = null;
    this.removeEventListener( "keydown", this._onKeyDown );
    this.removeEventListener( "keyup", this._onKeyUp );
    this.addEventListener( "changeParent", this._onChangeParent, this );
  },

  destruct : function() {
    this.setMnemonicIndex( null );
  },

  properties : {

    dropDownArrow : {
      apply : "_applyDropDownArrow",
      nullable : true,
      themeable : true
    },

    separatorBorder : {
      nullable : true,
      init : null,
      apply : "_applySeparatorBorder",
      themeable : true
    }

  },

  members : {

    // overwritten:
    _onKeyPress : function( event ) {
      // give to toolBar for keyboard control (left/right keys):
      this.getParent().dispatchEvent( event );
      this.base( arguments, event );
    },

    setText : function( value ) {
      this._rawText = value;
      this._mnemonicIndex = null;
      this._applyText( false );
      this.dispatchSimpleEvent( "changeText" );
    },

    computeBadgePosition : function() {
      return [ 3, 3, "auto", "auto" ];
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
      if( this._rawText ) {
        var mnemonicIndex = mnemonic ? this._mnemonicIndex : undefined;
        var text = rwt.util.Encoding.escapeText( this._rawText, mnemonicIndex );
        this.setCellContent( 2, text );
      } else {
        this.setCellContent( 2, null );
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
            this.execute();
            event.success = true;
          }
        break;
      }
    },

    ////////////////////
    // Dropdown-support

    // overwritten:
    _onMouseDown : function( event ) {
      if ( event.getTarget() == this && event.isLeftButtonPressed() ) {
        this.removeState( "abandoned" );
        if( this._isDropDownClick( event ) ) {
          this._onDropDownClick();
        } else {
          this.addState( "pressed" );
        }
      }
    },

    _isDropDownClick : function( event ) {
      if( this.$separator ) {
        if( this.getDirection() === "rtl" ) {
          return event.getClientX() < this.$separator.offset().left;
        }
        return event.getClientX() > this.$separator.offset().left;
      }
      return false;
    },

    _onDropDownClick : function() {
      if( this._sendEvent ) {
        rwt.remote.EventUtil.notifySelected( this, 0, 0, 0, 0, "arrow" );
      }
      this.dispatchSimpleEvent( "dropDownClicked" );
    },

    _applyDropDownArrow : function( value ) {
      if( this._isDropDown ) {
        var rtl = this.getDirection() === "rtl";
        var url = value ? value[ 0 ] : null;
        var width = value ? value[ 1 ] : 0;
        var height = value ? value[ 2 ] : 0;
        this.$dropDownArrow.css({
          backgroundImage: url,
          width: width,
          height: height,
          right: rtl ? "" : this.getSpacing(),
          left: rtl ? this.getSpacing() : ""
        });
        var separatorPos = width + this.getSpacing() * 2;
        this.$separator.css({
          right: rtl ? "" : separatorPos,
          left: rtl ? separatorPos : ""
        });
      }
    },

    _applySeparatorBorder : function() {
      if( this._isDropDown ) {
        var border = this.getSeparatorBorder();
        this.$separator.css({
          borderLeftWidth: ( border.getWidthLeft() || 0 ) + "px",
          borderLeftStyle: border.getStyleLeft() || "none",
          borderLeftColor: border.getColorLeft() || ""
        });
      }
    },

    _createSubElements: function() {
      this.base( arguments );
      if( this._isDropDown ) {
        this.$dropDownArrow.appendTo( this );
        this.$separator.appendTo( this );
      }
    },

    // overwritten:
    _afterRenderLayout : function( changes ) {
      if( this._isDropDown && changes.layoutY ) {
        var innerHeight = this.getInnerHeight();
        var imageHeight = ( this.getDropDownArrow() || [ null, 0, 0 ] )[ 2 ];
        this.$dropDownArrow.css("top", Math.round( this.getPaddingTop() + innerHeight / 2 - imageHeight / 2) );
      }
    },

    // overwritten:
    _getAvailableInnerWidth : function() {
      if( !this._isDropDown ) {
        return this.getInnerWidth();
      }
      var imageWidth = ( this.getDropDownArrow() || [ null, 0, 0 ] )[ 1 ];
      return Math.max( 0, this.getInnerWidth() - imageWidth - 1 - this.getSpacing() * 2 );
    },

    // overwritten:
    _notifySelected : function() {
      rwt.remote.EventUtil.notifySelected( this );
    },

    _onChangeParent : function( event ) {
      var oldParent = event.getOldValue();
      var newParent = event.getValue();
      if( oldParent ) {
        this._toggleFirstLastState( oldParent );
      } else {
        this._toggleFirstLastState( newParent );
      }
      if( newParent ) {
        this.setDirection( newParent.getDirection() );
        this.toggleState( "rwt_RIGHT", newParent.hasState( "rwt_RIGHT" ) );
        this.toggleState( "rwt_FLAT", newParent.hasState( "rwt_FLAT" ) );
      }
    },

    _toggleFirstLastState : function( parent ) {
      var first;
      var last;
      parent.forEachVisibleChild( function() {
        if( this instanceof rwt.widgets.ToolItem ) {
          this.toggleState( "first", false );
          this.toggleState( "last", false );
          if( !first ) {
            first = this;
          }
          last = this;
        }
      } );
      if( first ) {
        first.toggleState( "first", true );
        last.toggleState( "last", true );
      }
    }

  }

} );

}( rwt.util._RWTQuery ));
