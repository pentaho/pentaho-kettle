/*******************************************************************************
 * Copyright (c) 2011, 2018 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.base.GridHeader", {

  extend : rwt.widgets.base.Parent,

  construct : function( argsMap ) {
    this.base( arguments );
    this.setOverflow( "hidden" );
    this.setZIndex( 2000 );
    this._fixedColumns = argsMap.splitContainer;
    this._scrollWidth = 0;
    this._scrollLeft = 0;
    this._footer = argsMap.footer ? true : false;
    this._baseAppearance = argsMap.appearance;
    this._config = argsMap.config;
    this._dummyColumn = this._createDummyColumn();
    this._currentDragColumn = null;
    this._feedbackLabel = null;
    this._labelToColumnMap = {};
    this._columnToLabelMap = {};
    // TODO [tb] : Find a cleaner solution to block drag-events
    var dragBlocker = function( event ) { event.stopPropagation(); };
    this.addEventListener( "dragstart", dragBlocker );
  },

  destruct : function() {
    this._dummyColumn = null;
  },

  events: {
    "columnLayoutChanged" : "rwt.event.Event"
  },

  members : {

    setScrollLeft : function( value ) {
      this._scrollLeft = value;
      if( this._fixedColumns && !rwt.widgets.base.Widget._inFlushGlobalQueues ) {
        for( var i = 0; i < this._children.length; i++ ) {
          var column = this._getColumnByLabel( this._children[ i ] );
          if( column && column.isFixed() ) {
            this._renderLabelLeft( this._children[ i ], column );
          }
        }
        if( !rwt.remote.EventUtil.getSuspended() ) {
          rwt.widgets.base.Widget.flushGlobalQueues();
        }
      }
      // NOTE [tb] : order is important to prevent flickering in IE
      if( this.isSeeable() ) {
        this.base( arguments, value );
      }
    },

    setScrollWidth : function( value ) {
      this._scrollWidth = value;
      if( this.getVisibility() ) {
        this._renderDummyColumn();
      }
    },

    _applyDirection : function( value ) {
      this.base( arguments, value );
      for( var i = 0; i < this._children.length; i++ ) {
        var column = this._getColumnByLabel( this._children[ i ] );
        this._children[ i ].setDirection( value );
        this._children[ i ].setHorizontalChildrenAlign( this._getAlignment( column ) );
      }
      this.getLayoutImpl().setMirror( value === "rtl" );
    },

    _onColumnDispose : function( event ) {
      var column = event.target;
      var label = this._getLabelByColumn( column );
      delete this._labelToColumnMap[ label.toHashCode() ];
      delete this._columnToLabelMap[ column.toHashCode() ];
      label.destroy();
    },

    renderColumns : function( columns ) {
      for( var key in columns ) {
        var column = columns[ key ];
        var label = this._getLabelByColumn( column );
        if( label ) {
          this._renderLabel( label, column );
        }
      }
      this._renderDummyColumn();
    },

    _renderLabel : function( label, column ) {
      this._renderLabelLeft( label, column );
      label.setCustomVariant( column.getCustomVariant() );
      label.setVisibility( column.getVisibility() && column.getWidth() > 0 );
      if( this._footer ) {
        this._renderFooterLabel( label, column );
      } else {
        this._renderHeaderLabel( label, column );
      }
      label.setHorizontalChildrenAlign( this._getAlignment( column ) );
      label.setWordWrap( column.getHeaderWordWrap() );
    },

    _renderFooterLabel : function( label, column ) {
      label.setWidth( column.getFooterWidth() );
      label.setText( column.getFooterText() );
      label.setImage( column.getFooterImage() );
      if( column.getFooterFont() !== null ) {
        label.setFont( column.getFooterFont() );
      } else {
        label.resetFont();
      }
      var onTop = column.isFixed() || column.getFooterSpan() > 1;
      label.setZIndex( onTop ? 1e7 : 1 );
    },

    _renderHeaderLabel : function( label, column ) {
      label.setWidth( column.getWidth() );
      label.setHoverEffect( column.getMoveable() );
      label.clearHtmlAttributes();
      label.setHtmlAttributes( column.getHtmlAttributes() );
      if( column.getFont() !== null ) {
        label.setFont( column.getFont() );
      } else {
        label.resetFont();
      }
      if( this._config.headerForeground != null ) {
        label.setTextColor( this._config.headerForeground );
      } else {
        label.resetTextColor();
      }
      if( this._config.headerBackground != null ) {
        label.setBackgroundGradient( null );
        label.setBackgroundColor( this._config.headerBackground );
      } else {
        label.resetBackgroundGradient();
        label.resetBackgroundColor();
      }
      label.setText( column.getText() );
      label.setImage( column.getImage() );
      label.setToolTipText( column.getToolTipText() );
      label.setSortIndicator( column.getSortDirection() );
      if( column.isGroup() && column.getShowChevron() ) {
        label.setChevron( column.isExpanded() ? "expanded" : "collapsed" );
      }
      this._renderLabelY( label, column );
      label.setZIndex( column.isFixed() ? 1e7 : 1 );
    },

    _renderLabelY : function( label, column ) {
      if( column.isGroup() ) {
        label.setTop( 0 );
        label.setHeight( column.getHeight() );
      } else if( column.getGroup() != null ) {
        var groupHeight = column.getGroup().getHeight();
        label.setTop( groupHeight );
        label.setHeight( this.getHeight() - groupHeight );
      } else {
        label.setTop( 0 );
        label.setHeight( "100%" );
      }
    },

    _renderLabelLeft : function( label, column ) {
      var offset = column.isFixed() ? this._adjustScrollLeft( this._scrollLeft ) : 0;
      label.setLeft( column.getLeft() + offset );
    },

    _onDummyRendered : function() {
      this.setScrollLeft( this._scrollLeft );
    },

    _flushChildrenQueue: function() {
      this.base( arguments );
      this.setScrollLeft( this._scrollLeft );
    },

    _fireUpdateEvent : function() {
      this.createDispatchEvent( "columnLayoutChanged" );
    },

    _renderDummyColumn : function() {
      var dummyLeft = this._getDummyColumnLeft();
      var totalWidth = Math.max( this._scrollWidth, this.getWidth() );
      var dummyWidth = Math.max( 0, totalWidth - dummyLeft );
      this._dummyColumn.setLeft( dummyLeft );
      this._dummyColumn.setWidth( dummyWidth );
      this._dummyColumn.setCustomVariant( this._config.variant );
      if( this._config.headerForeground != null ) {
        this._dummyColumn.setTextColor( this._config.headerForeground );
      } else {
        this._dummyColumn.resetTextColor();
      }
      if( this._config.headerBackground != null ) {
        this._dummyColumn.setBackgroundGradient( null );
        this._dummyColumn.setBackgroundColor( this._config.headerBackground );
      } else {
        this._dummyColumn.resetBackgroundGradient();
        this._dummyColumn.resetBackgroundColor();
      }
    },

    _getDummyColumnLeft : function() {
      var columns = this._labelToColumnMap;
      var result = 0;
      for( var key in columns ) {
        if( columns[ key ].getVisibility() ) {
          var left = columns[ key ].getLeft() + columns[ key ].getWidth();
          result = Math.max( result, left );
        }
      }
      return result;
    },

    _onLabelSelected : function( event ) {
      var column = this._getColumnByLabel( event.target );
      column.handleSelectionEvent( event );
    },

    _onLabelMoveStart : function( event ) {
      var column = this._getColumnByLabel( event.target );
      return !this._footer && column.getMoveable();
    },

    _onLabelMoveEnd : function( event ) {
      var column = this._getColumnByLabel( event.target );
      column.setLeft( event.position );
    },

    _onShowDragFeedback : function( event ) {
      var column = this._getColumnByLabel( event.target );
      var widget = this._getDragFeedback( column );
      widget.setLeft( event.position );
    },

    _onHideDragFeedback : function( event ) {
      var label = event.target;
      var column = this._getColumnByLabel( label );
      var widget = this._getDragFeedback( column );
      var left = label.getLeft();
      if( event.snap ) {
        rwt.animation.AnimationUtil.snapTo( widget, 250, left, label.getTop(), true );
      } else {
        widget.setDisplay( false );
      }
      this._currentDragColumn = null;
    },

    _onLabelResizeStart : function( event ) {
      var column = this._getColumnByLabel( event.target );
      return !this._footer && column.getResizeable();
    },

    _onLabelResizeEnd : function( event ) {
      var column = this._getColumnByLabel( event.target );
      column.setWidth( event.width );
    },

    _getColumnByLabel : function( label ) {
      return this._labelToColumnMap[ label.toHashCode() ];
    },

    _getLabelByColumn : function( column ) {
      var result = this._columnToLabelMap[ column.toHashCode() ];
      if( !result ) {
        if( column.getVisibility() ) {
          result = this._createLabel( column );
        } else {
          result = null;
        }
      }
      return result;
    },

    _getAlignment : function( column ) {
      var alignment = column ? column.getAlignment() : "left";
      if( this.getDirection() === "rtl" ) {
        if( alignment === "left" ) {
          return "right";
        } else if( alignment === "right" ) {
          return "left";
        }
      }
      return alignment;
    },

    _getDragFeedback : function( column ) {
      if( this._feedbackLabel === null ) {
        this._feedbackLabel = this._createFeedbackColumn();
      }
      if( this._currentDragColumn !== column ) {
        this._renderLabelY( this._feedbackLabel, column );
        this._feedbackLabel.setWidth( column.getWidth() );
        this._feedbackLabel.setCustomVariant( column.getCustomVariant() );
        this._feedbackLabel.setText( column.getText() );
        this._feedbackLabel.setImage( column.getImage() );
        this._feedbackLabel.setSortIndicator( column.getSortDirection() );
        this._feedbackLabel.setHorizontalChildrenAlign( this._getAlignment( column ) );
        this._feedbackLabel.setDisplay( true );
        this._feedbackLabel.dispatchSimpleEvent( "cancelAnimations" );
        this._currentDragColumn = column;
      }
      return this._feedbackLabel;
    },

    _createLabel : function( column ) {
      var label = new rwt.widgets.base.GridColumnLabel( this._baseAppearance );
      if( this._footer ) {
        label.addState( "footer" );
      } else if( column.getResizeable() ) {
        label.setResizeCursor( "col-resize" );
      }
      if( column.isGroup() ) {
        label.addState( "group" );
      }
      label.setTop( 0 );
      label.setHeight( "100%" );
      label.setDirection( this.getDirection() );
      this.add( label );
      this._labelToColumnMap[ label.toHashCode() ] = column;
      this._columnToLabelMap[ column.toHashCode() ] = label;
      label.addEventListener( "selected", this._onLabelSelected, this );
      label.addEventListener( "moveStart", this._onLabelMoveStart, this );
      label.addEventListener( "showDragFeedback", this._onShowDragFeedback, this );
      label.addEventListener( "hideDragFeedback", this._onHideDragFeedback, this );
      label.addEventListener( "moveEnd", this._onLabelMoveEnd, this );
      label.addEventListener( "resizeStart", this._onLabelResizeStart, this );
      label.addEventListener( "resizeEnd", this._onLabelResizeEnd, this );
      column.addEventListener( "dispose", this._onColumnDispose, this );
      return label;
    },

    _createDummyColumn : function() {
      var dummyColumn = new rwt.widgets.base.GridColumnLabel( this._baseAppearance );
      if( this._footer ) {
        dummyColumn.addState( "footer" );
      }
      dummyColumn.setTop( 0 );
      dummyColumn.setHeight( "100%" );
      dummyColumn.setDirection( this.getDirection() );
      dummyColumn.addState( "dummy" );
      dummyColumn.addEventListener( "appear", this._onDummyRendered, this );
      dummyColumn.setEnabled( false );
      this.add( dummyColumn );
      return dummyColumn;
    },

    _createFeedbackColumn : function() {
      var feedback = new rwt.widgets.base.GridColumnLabel( this._baseAppearance );
      feedback.addState( "moving" );
      feedback.setTop( 0 );
      feedback.setHeight( "100%" );
      feedback.setDirection( this.getDirection() );
      feedback.setEnabled( false );
      feedback.setZIndex( 1e8 );
      feedback.addState( "mouseover" ); // to make the label more visible, not ideal
      this.add( feedback );
      return feedback;
    },

    _adjustScrollLeft : function( scrollLeft ) {
      return rwt.widgets.base.Scrollable.adjustScrollLeft( this.getParent(), scrollLeft );
    }

  }

} );
