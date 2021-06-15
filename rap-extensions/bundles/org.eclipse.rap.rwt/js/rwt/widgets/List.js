/*******************************************************************************
 * Copyright: 2004, 2018 1&1 Internet AG, Germany, http://www.1und1.de,
 *                       and EclipseSource
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for the Eclipse Remote Application Platform
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.List", {

  extend : rwt.widgets.base.Scrollable,

  construct : function( multiSelection ) {
    this.base( arguments, new rwt.widgets.base.VerticalBoxLayout() );
    this.setAppearance( "list" );
    this.setScrollBarsVisible( false, false );
    this.setEnableElementFocus( false );
    this._manager = new rwt.widgets.util.SelectionManager( this._clientArea );
    this._manager.setMultiSelection( multiSelection );
    this._manager.setDragSelection( false );
    this._manager.addEventListener( "changeLeadItem", this._onChangeLeadItem, this );
    this._manager.addEventListener( "changeSelection", this._onSelectionChange, this );
    this.addEventListener( "focus", this._onFocusChange, this );
    this.addEventListener( "blur", this._onFocusChange, this );
    this.addEventListener( "mouseover", this._onMouseOver, this );
    this.addEventListener( "mousedown", this._onMouseDown, this );
    this.addEventListener( "mouseup", this._onMouseUp, this );
    this.addEventListener( "click", this._onClick, this );
    this.addEventListener( "dblclick", this._onDblClick, this );
    this.addEventListener( "keypress", this._onKeyPress, this );
    this.addEventListener( "appear", this._onAppear, this );
    this.addEventListener( "userScroll", this._onUserScroll );
    this.initOverflow();
    this.initTabIndex();
    this._pressedString = "";
    this._lastKeyPress = 0;
    this._itemWidth = 0;
    this._itemHeight = 0;
    this._topIndex = 0;
    this._markupEnabled = false;
  },

  destruct : function() {
    this._disposeObjects( "_manager" );
  },

  members : {

    setMarkupEnabled : function( value ) {
      this._markupEnabled = value;
    },

    getSelectedItem : function() {
      return this._manager.getSelectedItems()[0] || null;
    },

    getSelectedItems : function() {
      return this._manager.getSelectedItems();
    },

    _onAppear : function() {
      // [ad] Fix for Bug 277678
      // when #showSelection() is called for invisible widget
      this._applyTopIndex( this._topIndex );
    },

    setTopIndex : function( value ) {
      this._topIndex = value;
      this._applyTopIndex( value );
    },

    _applyTopIndex : function( newIndex ) {
      var items = this._manager.getItems();
      if( items.length > 0 && items[ 0 ].isCreated() ) {
        if( this._itemHeight > 0 ) {
          this.setVBarSelection( newIndex * this._itemHeight );
        }
      }
    },

    _getTopIndex : function() {
      var topIndex = 0;
      var scrollTop = this._clientArea.getScrollTop();
      var items = this._manager.getItems();
      if( items.length > 0 ) {
        var itemHeight = this._manager.getItemHeight( items[ 0 ] );
        if( itemHeight > 0 ) {
          topIndex = Math.round( scrollTop / itemHeight );
        }
      }
      return topIndex;
    },

    _onChangeLeadItem : function() {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        var focusIndex = this._clientArea.indexOf( this._manager.getLeadItem() );
        rwt.remote.Connection.getInstance().getRemoteObject( this ).set( "focusIndex", focusIndex );
      }
    },

    _onSelectionChange : function() {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        this._sendSelectionChange();
        rwt.remote.EventUtil.notifySelected( this );
      }
      this._updateSelectedItemState();
    },

    _sendSelectionChange : function() {
      var selection = [];
      var selectedItems = this._manager.getSelectedItems();
      for( var i = 0; i < selectedItems.length; i++ ) {
        var index = this._clientArea.indexOf( selectedItems[ i ] );
        selection.push( index );
      }
      rwt.remote.Connection.getInstance().getRemoteObject( this ).set( "selection", selection );
    },

    _onFocusChange : function() {
      this._updateSelectedItemState();
    },

    _updateSelectedItemState : function() {
      var selectedItems = this._manager.getSelectedItems();
      for( var i = 0; i < selectedItems.length; i++ ) {
        selectedItems[ i ].toggleState( "parent_unfocused", !this.getFocused() );
      }
    },

    _onUserScroll : function() {
      this._topIndex = this._isCreated ? this._getTopIndex() : 0;
      rwt.remote.Connection.getInstance().getRemoteObject( this ).set( "topIndex", this._topIndex );
    },

    _onDblClick : function() {
      rwt.remote.EventUtil.notifyDefaultSelected( this );
    },

    _onMouseOver : function( event ) {
      var item = this.getListItemTarget( event.getTarget() );
      if( item ) {
        this._manager.handleMouseOver( item, event );
      }
    },

    _onMouseDown : function( event ) {
      if( !this._checkAndProcessHyperlink( event ) ) {
        var item = this.getListItemTarget( event.getTarget() );
        if( item ) {
          this._manager.handleMouseDown( item, event );
        }
      }
    },

    _onMouseUp : function( event ) {
      if( !this._checkAndProcessHyperlink( event ) ) {
        var item = this.getListItemTarget( event.getTarget() );
        if( item ) {
          this._manager.handleMouseUp( item, event );
        }
      }
    },

    _onClick : function( event ) {
      if( !this._checkAndProcessHyperlink( event ) ) {
        var item = this.getListItemTarget( event.getTarget() );
        if( item ) {
          this._manager.handleClick( item, event );
        }
      }
    },

    getListItemTarget : function( item ) {
      while( item != null && item.getParent() != this._clientArea ) {
        item = item.getParent();
      }
      return item;
    },

    _onKeyPress : function( event ) {
      this._manager.handleKeyPress( event );
      // Fix for bug# 288344
      if( !event.isAltPressed() && !event.isCtrlPressed() ) {
        if( event.getCharCode() !== 0 ) {
          // Reset string after a second of non pressed key
          if( ( ( new Date() ).valueOf() - this._lastKeyPress ) > 1000 ) {
            this._pressedString = "";
          }
          // Combine keys the user pressed to a string
          this._pressedString += String.fromCharCode( event.getCharCode() );
          // Find matching item
          var matchedItem = this.findString( this._pressedString, null );
          if( matchedItem ) {
            var oldVal = this._manager._getChangeValue();
            // Temporary disable change event
            var oldFireChange = this._manager.getFireChange();
            this._manager.setFireChange( false );
            // Reset current selection
            this._manager._deselectAll();
            // Update manager
            this._manager.setItemSelected( matchedItem, true );
            this._manager.setAnchorItem( matchedItem );
            this._manager.setLeadItem( matchedItem );
            // Scroll to matched item
            matchedItem.scrollIntoView();
            // Recover event status
            this._manager.setFireChange( oldFireChange );
            // Dispatch event if there were any changes
            if( oldFireChange && this._manager._hasChanged( oldVal ) ) {
              this._manager._dispatchChange();
            }
          }
          // Store timestamp
          this._lastKeyPress = ( new Date() ).valueOf();
          event.preventDefault();
        }
      }
    },

    findString : function( text, startIndex ) {
      return this._findItem( text, startIndex || 0 );
    },

    _findItem : function( userValue, startIndex ) {
      var allItems = this.getItems();
      // If no startIndex given try to get it by current selection
      if( startIndex == null ) {
        startIndex = allItems.indexOf( this.getSelectedItem() );
        if (startIndex == -1) {
          startIndex = 0;
        }
      }
      // Mode #1: Find all items after the startIndex
      for( var i = startIndex; i < allItems.length; i++ ) {
        if( allItems[ i ].matchesString( userValue ) ) {
          return allItems[i];
        }
      }
      // Mode #2: Find all items before the startIndex
      for( var i = 0; i < startIndex; i++ ) {
        if( allItems[ i ].matchesString( userValue ) ) {
          return allItems[i];
        }
      }
      return null;
    },

    setItems : function( value ) {
      var items = this._escapeItems( value );
      // preserve selection and focused item
      var oldLeadItem = this._manager.getLeadItem();
      var oldAnchorItem = this._manager.getAnchorItem();
      var oldSelection = this._manager.getSelectedItems();
      // exchange/add/remove items
      var oldItems = this.getItems();
      for( var i = 0; i < items.length; i++ ) {
        if( i < oldItems.length ) {
          oldItems[ i ].setLabel( items[ i ] );
        } else {
          var item = new rwt.widgets.ListItem();
          item.setDirection( this.getDirection() );
          item.addEventListener( "mouseover", this._onListItemMouseOver, this );
          item.addEventListener( "mouseout", this._onListItemMouseOut, this );
          // prevent items from being drawn outside the list
          this._renderItemDimension( item );
          item.setTabIndex( null );
          item.setLabel( items[ i ] );
          if( i % 2 === 0 ) {
            item.addState( "even" );
          }
          if( this._customVariant !== null ) {
            item.addState( this._customVariant );
          }
          this._clientArea.add( item );
        }
      }
      var child = null;
      while( this._clientArea.getChildrenLength() > items.length ) {
        child = this._clientArea.getLastChild();
        child.removeEventListener( "mouseover", this._onListItemMouseOver, this );
        child.removeEventListener( "mouseout", this._onListItemMouseOut, this );
        // [if] Workaround for bug:
        // 278361: [Combo] Overlays text after changing items
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=278361
        // Items are not removed from DOM if the _isDisplayable property is false.
        child._isDisplayable = true;
        child.destroy();
      }
      // restore previous selection and focusItem
      this._manager.setSelectedItems( oldSelection );
      this._manager.setLeadItem( oldLeadItem );
      if( this._manager.getMultiSelection() ) {
        this._manager.setAnchorItem( oldAnchorItem );
      }
      this._updateScrollDimension();
      this._applyTopIndex( this._topIndex );
    },

    _escapeItems : function( items ) {
      var result = items;
      if( !this._markupEnabled ) {
        var EncodingUtil = rwt.util.Encoding;
        for( var i = 0; i < result.length; i++ ) {
          result[ i ] = EncodingUtil.replaceNewLines( result[ i ], " " );
          result[ i ] = EncodingUtil.escapeText( result[ i ], false );
          result[ i ] = EncodingUtil.replaceWhiteSpaces( result[ i ] );
        }
      }
      return result;
    },

    getItems : function() {
      return this._manager.getItems();
    },

    getItemsCount : function() {
      return this.getItems().length;
    },

    getItemIndex : function( item ) {
      return this._clientArea.indexOf( item );
    },

    /**
     * Sets the single selection for the List to the item specified by the given
     * itemIndex (-1 to clear selection).
     */
    selectItem : function( itemIndex ) {
      if( itemIndex == -1 ) {
        this._manager.deselectAll();
      } else {
        var item = this.getItems()[ itemIndex ];
        this._manager.setSelectedItem( item );
        // avoid warning message. scrollIntoView works only for visible widgets
        // the assumtion is that if 'this' is visible, the item to scroll into
        // view is also visible
        if ( this._clientArea.isCreated() && this._clientArea.isDisplayable() ) {
          this._manager.scrollItemIntoView( item );
        }
      }
    },

    /**
     * Sets the multi selection for the List to the items specified by the given
     * itemIndices array (empty array to clear selection).
     */
    selectItems : function( itemIndices ) {
      this._manager.deselectAll();
      for( var i = 0; i < itemIndices.length; i++ ) {
        var item = this.getItems()[ itemIndices[ i ] ];
        this._manager.setItemSelected( item, true );
      }
    },

    /**
     * Sets the focused item the List to the item specified by the given
     * itemIndex (-1 for no focused item).
     */
    focusItem : function( itemIndex ) {
      if( itemIndex == -1 ) {
        this._manager.setLeadItem( null );
      } else {
        var items = this.getItems();
        this._manager.setLeadItem( items[ itemIndex ] );
      }
    },

    selectAll : function() {
      if( this._manager.getMultiSelection() === true ) {
        this._manager.selectAll();
      }
    },

    setItemDimensions : function( width, height ) {
      this._itemWidth = width;
      this._itemHeight = height;
      var items = this.getItems();
      for( var i = 0; i < items.length; i++ ) {
        this._renderItemDimension( items[ i ] );
      }
      this._vertScrollBar.setIncrement( height );
      this._updateScrollDimension();
      this._applyTopIndex( this._topIndex );
    },

    _updateScrollDimension : function() {
      var itemCount = this.getItems().length;
      this._internalChangeFlag = true;
      this._horzScrollBar.setMaximum( this._itemWidth );
      this._vertScrollBar.setMaximum( this._itemHeight * itemCount );
      this._internalChangeFlag = false;
    },

    setCustomVariant : function( value ) {
      if( this._customVariant !== null ) {
        var oldState = this._customVariant;
        this._clientArea.forEachChild( function() {
          this.removeState( oldState );
        } );
      }
      this._clientArea.forEachChild( function() {
        this.addState( value );
      } );
      this.base( arguments, value );
    },

    _renderItemDimension : function( item ) {
      item.setWidth( this._itemWidth );
      item.setHeight( this._itemHeight );
    },

    _onListItemMouseOver : function( evt ) {
      evt.getTarget().addState( "over" );
    },

    _onListItemMouseOut : function( evt ) {
      evt.getTarget().removeState( "over" );
    },

    _checkAndProcessHyperlink : function( event ) {
      var hyperlink = null;
      var target = event.getOriginalTarget();
      if( this._markupEnabled && target instanceof rwt.widgets.ListItem ) {
        hyperlink = this._findHyperlink( event );
        if( hyperlink !== null && this._isRWTHyperlink( hyperlink ) ) {
          event.setDefaultPrevented( true );
          if( event.getType() === "click" ) {
            this._activateHyperlink( hyperlink );
          }
        }
      }
      return hyperlink !== null;
    },

    _activateHyperlink : function( hyperlink ) {
      var text = hyperlink.getAttribute( "href" );
      if( !text ) {
        text = hyperlink.innerHTML;
      }
      var properties = {
        "detail" : "hyperlink",
        "text" : text
      };
      rwt.remote.EventUtil.notifySelected( this, properties );
    },

    _findHyperlink : function( event ) {
      var targetNode = event.getDomTarget();
      var tagName = targetNode.tagName.toLowerCase();
      while( tagName !== 'a' && tagName !== 'div' ) {
        targetNode = targetNode.parentNode;
        tagName = targetNode.tagName.toLowerCase();
      }
      return tagName === 'a' ? targetNode : null;
    },

    _isRWTHyperlink : function( hyperlink ) {
      return hyperlink.getAttribute( "target" ) === "_rwt";
    },

    _applyDirection : function( value ) {
      this.base( arguments, value );
      this._clientArea.setHorizontalChildrenAlign( value === "rtl" ? "right" : "left" );
      this._clientArea.forEachChild( function() {
        this.setDirection( value );
      } );
    }

  }

} );
