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

rwt.qx.Class.define( "rwt.widgets.Combo", {

  extend : rwt.widgets.base.HorizontalBoxLayout,

  construct : function( isCCombo ) {
    this.base( arguments );
    this._editable = true;
    this._userSelection = true;
    this._listMinWidth = -1;
    this._field = new rwt.widgets.base.BasicText();
    this._field.setTabIndex( null );
    this._field.setAllowStretchY( true );
    this._field.setTop( 0 );
    this._field.setWidth( "1*" );
    this._field.setHeight( "100%" );
    this.add( this._field );
    this._button = new rwt.widgets.base.BasicButton( "push", true );
    this._button.setTabIndex( null );
    this._button.setTop( 0 );
    this._button.setHeight( "100%" );
    this.add( this._button );
    this.setHideFocus( true );
    var appearance = isCCombo ? "ccombo" : "combo";
    this.setAppearance( appearance );
    this._field.setAppearance( appearance + "-field" );
    this._button.setAppearance( appearance + "-button" );
    this._registerListeners();
  },

  destruct : function() {
    this._list.destroy();
    this._disposeObjects( "_field", "_button" );
  },

  members : {

    setItems : function( items ) {
      this._listMinWidth = -1;
      this._userSelection = false;
      this._list.setItems( items );
      this._userSelection = true;
      this.dispatchSimpleEvent( "itemsChanged" );
      if( this._list.getVisible() ) {
        this._list.setMinWidth( this._getListMinWidth() );
      }
    },

    setVisibleItemCount : function( value ) {
      this._list.setVisibleItemCount( value );
    },

    select : function( index ) {
      this._userSelection = false;
      this._list.setSelectionIndex( index );
      this._userSelection = true;
    },

    setEditable : function( value ) {
      this._editable = value;
      this._field.setReadOnly( !value );
    },

    setListVisible : function( value ) {
      if( value ) {
        this._list.setMinWidth( this._getListMinWidth() );
      }
      this._list.setVisible( value );
    },

    setText : function( value ) {
      if( this._editable ) {
        this._field.setValue( value );
      }
    },

    setTextSelection : function( selection ) {
      this._field.setSelection( selection );
    },

    setTextLimit : function( value ) {
      this._field.setMaxLength( value );
    },

    addState : function( state ) {
      this.base( arguments, state );
      if( state === "rwt_FLAT" || state === "rwt_RIGHT_TO_LEFT" ) {
        this._field.addState( state );
        this._button.addState( state );
      }
    },

    removeState : function( state ) {
      this.base( arguments, state );
      if( state === "rwt_RIGHT_TO_LEFT" ) {
        this._field.removeState( state );
        this._button.removeState( state );
      }
    },

    _getSubWidgets : function() {
      return [ this._field, this._button, this._list ];
    },

    _applyDirection : function( value ) {
      this.base( arguments, value );
      this.setReverseChildrenOrder( value === "rtl" );
      this.setHorizontalChildrenAlign( value === "rtl" ? "right" : "left" );
      if( this._list ) {
        this._list.setDirection( value );
      }
    },

    _registerListeners : function() {
      this.addEventListener( "changeParent", this._onChangeParent, this );
      this.addEventListener( "mousedown", this._onMouseDown, this );
      this.addEventListener( "mousewheel", this._onMouseWheel, this );
      this.addEventListener( "keypress", this._onKeyPress, this );
      this._field.addEventListener( "input", this._onTextInput, this );
      this._field.addEventListener( "keypress", this._onTextKeyPress, this );
      this._field.addEventListener( "selectionChanged", this._onTextSelectionChange, this );
    },

    _onChangeParent : function() {
      // DropDown requires valid parent focus root when creating
      if( !this._list ) {
        var appearance = this.getAppearance() + "-list";
        this._list = new rwt.widgets.DropDown( { parent: this, appearance: appearance } );
        this._list.setSelectionWrapping( false );
        this._list.setDirection( this.getDirection() );
        var that = this;
        this._list.addListener( "Show", function( event ) {
          that._onListVisibleChanged( event );
        } );
        this._list.addListener( "Hide", function( event ) {
          that._onListVisibleChanged( event );
        } );
        this._list.addListener( "Selection", function( event ) {
          that._onListSelectionChanged( event );
        } );
      }
    },

    _applyFont : function( value, old ) {
      this.base( arguments, value, old );
      this._field.setFont( value );
    },

    _applyTextColor : function( value, old ) {
      this.base( arguments, value, old );
      this._field.setTextColor( value );
    },

    _applyCursor : function( value, old ) {
      this.base( arguments, value, old );
      this._field.setCursor( value );
      this._button.setCursor( value );
    },

    _ontabfocus : function() {
      this._showFocusIndicator();
      if( this._field.isCreated() ) {
        this._field.selectAll();
      }
    },

    _showFocusIndicator : function() {
      if( !this._editable ) {
        var cssSelector = ( this.getAppearance() === "combo" ? "" : "C" ) + "Combo-FocusIndicator";
        rwt.widgets.util.FocusIndicator.getInstance().show( this, cssSelector, null );
      }
    },

    _visualizeFocus : function() {
      if( this._field.isCreated() ) {
        this._field._visualizeFocus();
        this._field._renderSelection();
      }
      this.addState( "focused" );
    },

    _visualizeBlur : function() {
      if( this._field.isCreated() ) {
        // setting selection lenght to 0 needed for IE to deselect text
        this._field._setSelectionLength( 0 );
        this._field._visualizeBlur();
      }
      if( !this._editable ) {
        rwt.widgets.util.FocusIndicator.getInstance().hide( this );
      }
      this.removeState( "focused" );
    },

    _toggleListVisibility : function() {
      if( this._list.getVisible() ) {
        this._list.hide();
      } else {
        this._list.setMinWidth( this._getListMinWidth() );
        this._list.show();
      }
    },

    _getListMinWidth : function() {
      if( this._listMinWidth === -1 ) {
        var fontProps = {};
        this.getFont().renderStyle( fontProps );
        var calc = rwt.widgets.util.FontSizeCalculation;
        var items = this._list.getItems();
        for( var i = 0; i < this._list.getItemCount(); i++ ) {
          var text = this._escapeText( items[ i ] );
          var dimensions = calc.computeTextDimensions( text, fontProps );
          this._listMinWidth = Math.max( this._listMinWidth, dimensions[ 0 ] );
        }
      }
      return this._listMinWidth;
    },

    _escapeText : function( text ) {
      var Encoding = rwt.util.Encoding;
      var result = Encoding.escapeText( text, false );
      result = Encoding.replaceNewLines( result, "" );
      result = Encoding.replaceWhiteSpaces( result );
      return result;
    },

    _onMouseDown : function( event ) {
      var target = event.getTarget();
      if( event.isLeftButtonPressed() ) {
        if( target === this._field && this._field.getReadOnly() || target === this._button ) {
          this.setFocused( true );
          this._toggleListVisibility();
          event.preventDefault();
        }
      }
    },

    _onMouseWheel : function( event ) {
      if( !this._list.getVisible() && this.getFocused() && this._list.getItemCount() > 0 ) {
        this._scrollSelection( event.getWheelDelta() < 0 );
        event.preventDefault();
        event.stopPropagation();
      }
    },

    _onTextKeyPress : function( event ) {
      // Key press event propagation is disabled in BasicText.js. Redispatch the event.
      this.dispatchEvent( event );
    },

    _onKeyPress : function( event ) {
      var key = event.getKeyIdentifier();
      if( key === "Enter" ) {
        this._handleKeyEnter( event );
      } else if( key === "Up" || key === "Down" || key === "PageUp" || key === "PageDown" ) {
        this._handleKeyUpDown( event );
      }
      this._selectByFirstLetter( event );
      this._stopKeyEvent( event );
    },

    _handleKeyEnter : function( event ) {
      if( !this._list.getVisible() && event.getModifiers() === 0 ) {
        rwt.remote.EventUtil.notifyDefaultSelected( this );
      }
    },

    _handleKeyUpDown : function( event ) {
      if( event.isAltPressed() ) {
        this._toggleListVisibility();
      } else if( !this._list.getVisible() && this._list.getItemCount() > 0 ) {
        switch( event.getKeyIdentifier() ) {
          case "Up":
            this._scrollSelection( false );
          break;
          case "Down":
            this._scrollSelection( true );
          break;
          case "PageUp":
            this._scrollSelection( false, true );
          break;
          case "PageDown":
            this._scrollSelection( true, true );
          break;
        }
      }
    },

    _selectByFirstLetter : function( event ) {
      var charCode = event.getCharCode();
      if( charCode !== 0 && !event.isAltPressed() && !event.isCtrlPressed() ) {
        if( this._list.getVisible() || !this._editable ) {
          var startIndex = this._list.getSelectionIndex() + 1;
          var endIndex = this._list.getItemCount() - 1;
          var letter = String.fromCharCode( charCode );
          var selectionIndex = this._findIndexByFirstLetter( startIndex, endIndex, letter );
          if( selectionIndex === -1 ) {
            selectionIndex = this._findIndexByFirstLetter( 0, startIndex - 1, letter );
          }
          if( selectionIndex !== -1 ) {
            this._list.setSelectionIndex( selectionIndex );
          }
        }
      }
    },

    _findIndexByFirstLetter : function( startIndex, endIndex, letter ) {
      var items = this._list.getItems();
      for( var i = startIndex; i <= endIndex; i++ ) {
        if( items[ i ].slice( 0, 1 ).toLowerCase() === letter.toLowerCase() ) {
          return i;
        }
      }
      return -1;
    },

    _stopKeyEvent : function( event ) {
      switch( event.getKeyIdentifier() ) {
        case "Enter":
        case "Up":
        case "Down":
        case "PageUp":
        case "PageDown":
          event.preventDefault();
          event.stopPropagation();
        break;
      }
    },

    _scrollSelection : function( down, page ) {
      var index = this._list.getSelectionIndex();
      var itemCount = this._list.getItemCount();
      var visibleItems = this._list.getVisibleItemCount();
      if( index === -1 ) {
        index = 0;
      } else if( down ) {
        index = page ? index + visibleItems - 1 : index + 1;
        index = Math.min( itemCount - 1, index );
      } else {
        index = page ? index - visibleItems + 1 : index - 1;
        index = Math.max( 0, index );
      }
      if( index !== this._list.getSelectionIndex() ) {
        this._list.setSelectionIndex( index );
      }
    },

    _onTextInput : function() {
      if( this._editable ) {
        var remoteObject = rwt.remote.Connection.getInstance().getRemoteObject( this );
        remoteObject.set( "text", this._field.getComputedValue() );
        this._notifyModify( true );
        this._internalSelectionChanged = true;
        this._list.setSelectionIndex( -1 );
        this._internalSelectionChanged = false;
      }
    },

    _onListVisibleChanged : function() {
      var listVisible = this._list.getVisible();
      if( this._editable ) {
        this._field.setReadOnly( listVisible );
      }
      if( this.getFocused() ) {
        if( listVisible ) {
          this._field._visualizeBlur();
        } else if( this._editable ) {
          this._field._visualizeFocus();
        }
      }
      if( !rwt.remote.EventUtil.getSuspended() ) {
        var connection = rwt.remote.Connection.getInstance();
        connection.getRemoteObject( this ).set( "listVisible", listVisible );
      }
    },

    _onListSelectionChanged : function( event ) {
      if( !this._internalSelectionChanged ) {
        if( this._userSelection ) {
          this.setFocused( true );
        }
        this._field.setValue( event.index === -1 ? "" : event.text );
        if( this._field.isCreated() && this._userSelection ) {
          this._field.selectAll();
        }
        this._sendSelectionChanged( event );
        this.dispatchSimpleEvent( "selectionChanged" );
      }
    },

    _onTextSelectionChange : function() {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        var remoteObject = rwt.remote.Connection.getInstance().getRemoteObject( this );
        remoteObject.set( "selection", this._field.getSelection() );
      }
    },

    _sendSelectionChanged : function( event ) {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        var remoteObject = rwt.remote.Connection.getInstance().getRemoteObject( this );
        if( this._editable ) {
          remoteObject.set( "text", event.text );
        }
        remoteObject.set( "selectionIndex", event.index );
        rwt.remote.EventUtil.notifySelected( this );
        this._notifyModify();
      }
    },

    _notifyModify : function( delayed ) {
      var connection = rwt.remote.Connection.getInstance();
      if( connection.getRemoteObject( this ).isListening( "Modify" ) ) {
        connection.onNextSend( this._onSend, this );
        if( delayed ) {
          connection.sendDelayed( 500 );
        } else {
          connection.send();
        }
      }
    },

    _onSend : function() {
      if( !this.isDisposed() ) {
        rwt.remote.Connection.getInstance().getRemoteObject( this ).notify( "Modify", null, true );
      }
    },

    applyObjectId : function( id ) {
      this.base( arguments, id );
      this._list.applyObjectId( id + "-listbox" );
    }

  }

} );
