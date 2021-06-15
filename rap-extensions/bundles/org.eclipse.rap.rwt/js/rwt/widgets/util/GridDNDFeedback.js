/*******************************************************************************
 * Copyright (c) 2009, 2017 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.util.GridDNDFeedback", {
  extend : rwt.qx.Object,

  construct : function( tree ) {
    this.base( arguments );
    this._tree = tree;
    this._feedback = null;
    this._currentRow = null;
    this._insertIndicator = null;
    this._expandTimer = null;
    this._scrollTimer = null;
  },

  destruct : function() {
    this._renderFeedback( this._currentRow, false );
    if( this._expandTimer != null ) {
      this._expandTimer.dispose();
      this._expandTimer = null;
    }
    if( this._scrollTimer != null ) {
      this._scrollTimer.dispose();
      this._scrollTimer = null;
    }
    this._tree = null;
    this._feedback = null;
    this._insertIndicator = null;
    this._currentRow = null;
  },

  members : {

    /////////
    // Public

    setFeedback : function( feedbackMap ) {
      this._renderFeedback( this._currentRow, false );
      this._feedback = feedbackMap;
      this._renderFeedback( this._currentRow, true );
    },

    renderFeedback : function( target ) {
      this._renderFeedback( this._currentRow, false );
      this._renderFeedback( target, true );
      this._currentRow = target;
    },

    isFeedbackNode : function( node ) {
      var result = false;
      if( this._insertIndicator != null && this._insertIndicator == node ) {
        result = true;
      }
      return result;
    },

    ////////////
    // Internals

    _renderFeedback : function( row, value ) {
      var item = this._tree._rowContainer.findItemByRow( row );
      if( this._feedback != null && row != null && item != null ) {
        if( this._feedback[ "FEEDBACK_SELECT" ] ) {
          this._renderFeedbackSelect( row, value );
        } else if( this._feedback[ "FEEDBACK_INSERT_BEFORE" ] ) {
          this._renderFeedbackBefore( row, value );
        } else if( this._feedback[ "FEEDBACK_INSERT_AFTER" ] ) {
          this._renderFeedbackAfter( row, value );
        }
        if( this._feedback[ "FEEDBACK_EXPAND" ] ) {
          this._renderFeedbackExpand( row, value );
        }
        if( this._feedback[ "FEEDBACK_SCROLL" ] ) {
          this._renderFeedbackScroll( row, value );
        }
      }
    },

    _renderFeedbackSelect : function( row, value ) {
      row.setState( "dnd_selected", value );
      var item = this._tree._rowContainer.findItemByRow( row );
      this._tree._rowContainer.renderItem( item );
    },

    _renderFeedbackBefore : function( row, value ) {
      if( value ) {
        // draw insert-indicator above row (1px heigher)
        var location = this._getRowLocation( row );
        location.x = location.x + row.getNextIndentOffset();
        location.y--;
        this._showInsertIndicator( location.x, location.y );
      } else {
        this._hideInsertIndicator();
      }
    },

    _renderFeedbackAfter : function( row, value ) {
      if( value ) {
        // draw insert-indicator below row (1px heigher)
        var location = this._getRowLocation( row );
        var height = row.getHeight();
        location.x = location.x + row.getNextIndentOffset();
        location.y = location.y + ( height - 1 );
        this._showInsertIndicator( location.x, location.y );
      } else {
        this._hideInsertIndicator();
      }
    },

    _renderFeedbackExpand : function( row, value ) {
      var item = this._tree._rowContainer.findItemByRow( row );
      if( item != null && item.hasChildren() ) {
        if( value && !item.isExpanded() ) {
          this._startExpandTimer();
        } else {
          this._stopExpandTimer();
        }
      }
    },

    _renderFeedbackScroll : function( row, value ) {
      if( value ) {
        this._startScrollTimer();
      } else {
        this._stopScrollTimer();
      }
    },

    _getRowLocation : function( row ) {
      var location = { x : 0, y : 0 };
      var node = row.$el.get( 0 );
      var treeNode = this._tree._getTargetNode();
      while( node != treeNode ) {
        location.x += node.offsetLeft;
        location.y += node.offsetTop;
        node = node.parentNode;
      }
      return location;
    },

    /////////
    // Helper

    _showInsertIndicator : function( x, y ) {
      if( this._insertIndicator == null ) {
        var div = document.createElement( "div" );
        div.style.position = "absolute";
        div.style.borderTopStyle = "solid";
        div.style.borderTopColor = "black";
        div.style.borderTopWidth = "2px";
        div.style.zIndex = 100000;
        div.style.height = "2px";
        this._insertIndicator = div;
      }
      var width = this._tree.getWidthValue() - ( x + 6 );
      this._insertIndicator.style.left = x + "px";
      this._insertIndicator.style.top = y + "px";
      this._insertIndicator.style.width = width + "px";
      var treeNode = this._tree._getTargetNode();
      treeNode.appendChild( this._insertIndicator );
    },

    _hideInsertIndicator : function() {
      var treeNode = this._tree._getTargetNode();
      treeNode.removeChild( this._insertIndicator );
    },

    _startExpandTimer : function() {
      if( this._expandTimer == null ) {
        this._expandTimer = new rwt.client.Timer( 750 );
        this._expandTimer.addEventListener( "interval",
                                            this._onExpandTimer,
                                            this );
      }
      this._expandTimer.setEnabled( true );
    },

    _stopExpandTimer : function() {
      if( this._expandTimer != null ) {
        this._expandTimer.stop();
      }
    },

    _onExpandTimer : function() {
      this._stopExpandTimer();
      var item = this._tree._rowContainer.findItemByRow( this._currentRow );
      item.setExpanded( true );
    },

    _startScrollTimer : function() {
      if( this._scrollTimer == null ) {
        this._scrollTimer = new rwt.client.Timer( 250 );
        this._scrollTimer.addEventListener( "interval", this._onScrollTimer, this );
      }
      this._scrollTimer.setEnabled( true );
    },

    _stopScrollTimer : function() {
      if( this._scrollTimer != null ) {
        this._scrollTimer.stop();
      }
    },

    _getScrollDirection : function( row ) {
      var result = 0;
      var rowIndex = this._tree._rowContainer.getRowIndex( row );
      if( rowIndex === 0 ) {
        result = -1;
      } else if( rowIndex >= this._tree._rowContainer.getRowCount() - 2 ) {
        result = 1;
      }
      return result;
    },

    _onScrollTimer : function() {
      this._stopScrollTimer();
      var offset = this._getScrollDirection( this._currentRow );
      if( offset !== 0 ) {
        var item = this._tree._rowContainer.findItemByRow( this._currentRow );
        if( item != null ) {
          var newItem = offset > 0 ? item.getNextItem() : item.getPreviousItem();
          if( newItem != null ) {
            var newTopIndex = this._tree._topItemIndex + offset;
            this._tree._setTopItemIndex( newTopIndex );
            var newRow = this._tree._rowContainer.findRowByItem( newItem );
            var oldRow = this._currentRow;
            var wrapper = function() {
              this._targetUpdateCheck( oldRow, newRow );
            };
            rwt.client.Timer.once( wrapper, this, 1 );
          }
        }
      }
    },

    _targetUpdateCheck : function( oldRow, newRow ) {
      if( !this.isDisposed() ) {
        // TODO [tb] : this is not a good way to check for scrolling, as the tree can theoretically
        //             scroll without changing the row order.
        if( newRow != this._currentRow && oldRow == this._currentRow ) {
          var dndSupport = rwt.remote.DNDSupport.getInstance();
          dndSupport.setCurrentTargetElement( newRow.$el.get( 0 ) );
        }
      }
    }

  }

} );

