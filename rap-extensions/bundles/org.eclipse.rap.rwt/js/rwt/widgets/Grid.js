/*******************************************************************************
 * Copyright (c) 2010, 2019 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.Grid", {

  extend : rwt.widgets.base.Parent,

  construct : function( argsMap ) {
    this.base( arguments );
    this._rootItem = new rwt.widgets.GridItem();
    this._hasMultiSelection = false;
    this._leadItem = null;
    this._topItemIndex = 0;
    this._topItem = null;
    this._selection = [];
    this._focusItem = null;
    this._renderQueue = {};
    this._resizeLine = null;
    this._selectionTimestamp = null;
    this._selectionOffsetX = null;
    this._delayedSelection = false;
    this._sortDirection = null;
    this._sortColumn = null;
    this._hasFixedColumns = false;
    this._headerHeight = 0;
    this._footerHeight = 0;
    this._itemHeight = 16;
    this._rowContainer = rwt.widgets.util.GridUtil.createTreeRowContainer( argsMap );
    this._columns = {};
    this._horzScrollBar = new rwt.widgets.base.ScrollBar( true );
    this._vertScrollBar = new rwt.widgets.base.ScrollBar( false );
    this._vertScrollBar.setAutoThumbSize( false );
    this._header = null;
    this._footer = null;
    this.add( this._rowContainer );
    this.add( this._horzScrollBar );
    this.add( this._vertScrollBar );
    this._config = this._rowContainer.getRenderConfig();
    this.setCursor( "default" );
    this.setOverflow( "hidden" );
    this.setEnableElementFocus( false );
    rwt.widgets.base.Widget.disableScrolling( this ); // see bugs 279460 and 364739
    rwt.widgets.util.ScrollBarsActivator.install( this );
    this._configureScrollBars();
    this._registerListeners();
    this._parseArgsMap( argsMap );
  },

  destruct : function() {
    this._rootItem.removeEventListener( "update", this._onItemUpdate, this );
    this._rootItem.dispose();
    this._rootItem = null;
    this._rowContainer = null;
    this._header = null;
    this._footer = null;
    this._horzScrollBar = null;
    this._vertScrollBar = null;
    this._leadItem = null;
    this._focusItem = null;
    this._sortColumn = null;
    this._resizeLine = null;
  },

  members : {

    /////////////////////////////////
    // Contructor & Subwidget helpers

    _createHeader : function() {
      this._header = new rwt.widgets.base.GridHeader( {
        "appearance" : this.getAppearance(),
        "splitContainer" : this._hasFixedColumns,
        "config" : this._config
      } );
      this.add( this._header );
      this._header.addEventListener( "showResizeLine", this._onShowResizeLine, this );
      this._header.addEventListener( "hideResizeLine", this._onHideResizeLine, this );
      this._header.setTop( 0 );
      this._header.setScrollLeft( this._adjustScrollLeft( this._horzScrollBar.getValue() ) );
      this._header.setDirection( this.getDirection() );
      this._scheduleColumnUpdate();
    },

    _createFooter : function() {
      this._footer = new rwt.widgets.base.GridHeader( {
        "appearance" : this.getAppearance(),
        "splitContainer" : this._hasFixedColumns,
        "footer" : true,
        "config" : this._config
      } );
      this.add( this._footer );
      this._footer.setScrollLeft( this._adjustScrollLeft( this._horzScrollBar.getValue() ) );
      this._footer.setDirection( this.getDirection() );
      this._scheduleColumnUpdate();
    },

    _configureScrollBars : function() {
      var dragBlocker = function( event ) { event.stopPropagation(); };
      this._horzScrollBar.setZIndex( 1e8 );
      this._horzScrollBar.setVisibility( false );
      this._horzScrollBar.addEventListener( "dragstart", dragBlocker );
      this._vertScrollBar.setZIndex( 1e8 );
      this._vertScrollBar.setVisibility( false );
      this._vertScrollBar.setIncrement( 1 );
      this._vertScrollBar.addEventListener( "dragstart", dragBlocker );
    },

    _registerListeners : function() {
      this._rootItem.addEventListener( "update", this._onItemUpdate, this );
      this.addEventListener( "mousedown", this._onMouseDown, this );
      this.addEventListener( "mouseup", this._onMouseUp, this );
      this.addEventListener( "click", this._onClick, this );
      this.addEventListener( "mouseout", this._onMouseOut, this );
      this.addEventListener( "keypress", this._onKeyPress, this );
      this.addEventListener( "focusin", this._onFocusIn, this );
      this._rowContainer.addEventListener( "mousewheel", this._onClientAreaMouseWheel, this );
      this._horzScrollBar.addEventListener( "changeValue", this._onHorzScrollBarChangeValue, this );
      this._horzScrollBar.addEventListener( "changeMaximum",
                                            this._onHorzScrollBarChangeMaximum,
                                            this );
      this._vertScrollBar.addEventListener( "changeValue", this._onVertScrollBarChangeValue, this );
      this._rowContainer.setSelectionProvider( this.isItemSelected, this );
      this._rowContainer.addEventListener( "appear", this._onChangeSeeable );
      this._rowContainer.addEventListener( "disappear", this._onChangeSeeable );
    },

    _parseArgsMap : function( map ) {
      if( map.noScroll ) {
        this._rowContainer.removeEventListener( "mousewheel", this._onClientAreaMouseWheel, this );
      }
      this._config.hideSelection = !!map.hideSelection;
      this._hasMultiSelection = !!map.multiSelection;
      this._config.fullSelection = !!map.fullSelection;
      this._config.markupEnabled = map.markupEnabled;
      this._hasFixedColumns = map.splitContainer;
      this._config.baseAppearance = map.appearance;
      this._config.rowTemplate = map.rowTemplate;
      if( !map.fullSelection ) {
        this._config.selectionPadding = map.selectionPadding;
      }
      if( map.check ) {
        this._config.hasCheckBoxes = true;
        this._config.checkBoxLeft = map.checkBoxMetrics[ 0 ];
        this._config.checkBoxWidth = map.checkBoxMetrics[ 1 ];
      }
      if( typeof map.indentionWidth === "number" ) {
        this._config.indentionWidth = map.indentionWidth;
      }
      if( this._config.rowTemplate ) {
        this.addState( "rowtemplate" );
      }
      this._rowContainer.setBaseAppearance( map.appearance );
      this.setAppearance( map.appearance );
    },

    ///////////////////////////
    // API for server - general

    setItemCount : function( value ) {
      this._rootItem.setItemCount( value );
    },

    setHeaderVisible : function( value ) {
      if( value && this._header == null ) {
        this._createHeader();
      } else if( !value ) {
        this._header.destroy();
        this._header = null;
      }
      this._layoutX();
      this._layoutY();
    },

    setFooterVisible : function( value ) {
      if( value && this._footer == null ) {
        this._createFooter();
      } else if( !value ) {
        this._footer.destroy();
        this._footer = null;
      }
      this._scheduleUpdate( "scrollHeight" );
      this._layoutX();
      this._layoutY();
    },

    setHeaderForeground : function( value ) {
      this._config.headerForeground = value;
      this._scheduleColumnUpdate();
    },

    setHeaderBackground : function( value ) {
      this._config.headerBackground = value;
      this._scheduleColumnUpdate();
    },

    setHeaderHeight : function( value ) {
      this._headerHeight = value;
      this._layoutX();
      this._layoutY();
    },

    setFooterHeight : function( value ) {
      this._footerHeight = value;
      this._scheduleUpdate( "scrollHeight" );
      this._layoutX();
      this._layoutY();
    },

    setItemHeight : function( height ) {
      this._itemHeight = height;
      this._rowContainer.setRowHeight( height );
      this._rootItem.setDefaultHeight( height );
      this._updateScrollThumbHeight();
      this._scheduleUpdate( "scrollHeight" );
    },

    setColumnCount : function( count ) {
      this._config.columnCount = count;
      this._scheduleUpdate();
      this._updateScrollWidth();
    },

    setItemMetrics : function( columnIndex,
                               left,
                               width,
                               imageLeft,
                               imageWidth,
                               textLeft,
                               textWidth,
                               checkLeft,
                               checkWidth )
    {
      this._config.itemLeft[ columnIndex ] = left;
      this._config.itemWidth[ columnIndex ] = width;
      this._config.itemImageLeft[ columnIndex ] = imageLeft;
      this._config.itemImageWidth[ columnIndex ] = imageWidth;
      this._config.itemTextLeft[ columnIndex ] = textLeft;
      this._config.itemTextWidth[ columnIndex ] = textWidth;
      if( !isNaN( checkLeft ) ) {
        this._config.itemCellCheckLeft[ columnIndex ] = checkLeft;
        this._config.itemCellCheckWidth[ columnIndex ] = checkWidth;
      }
      this._scheduleUpdate();
      this._updateScrollWidth();
    },

    setTreeColumn : function( columnIndex ) {
      this._config.treeColumn = columnIndex;
    },

    scrollItemIntoView : function( item ) {
      this._disableRender = true;
      this._scrollIntoView( item.getFlatIndex(), item );
      delete this._disableRender;
    },

    setTopItemIndex : function( index ) {
      this._disableRender = true;
      this._setTopItemIndex( index );
      delete this._disableRender;
    },

    getTopItemIndex : function() {
      return this._topItemIndex;
    },

    setScrollLeft: function( value ) {
      this._horzScrollBar.setValue( value );
    },

    selectItem : function( item ) {
      this._selectItem( item, false );
      this._scheduleItemUpdate( item );
    },

    deselectItem : function( item ) {
      this._deselectItem( item, false );
      this._scheduleItemUpdate( item );
    },

    setFocusItem : function( item ) {
      this._focusItem = item;
      this.dispatchSimpleEvent( "focusItemChanged" );
    },

    getFocusItem : function() {
      return this._focusItem;
    },

    setSortDirection : function( direction ) {
      this._sortDirection = direction;
      if( this._sortColumn !== null ) {
        this._sortColumn.setSortDirection( this._sortDirection );
      }
    },

    setSortColumn : function( column ) {
      if( this._sortColumn !== null ) {
        this._sortColumn.setSortDirection( "none" );
      }
      this._sortColumn = column;
      if( this._sortColumn !== null ) {
        this._sortColumn.setSortDirection( this._sortDirection );
      }
    },

    setScrollBarsVisible : function( horzVisible, vertVisible ) {
      this._horzScrollBar.setVisibility( horzVisible );
      this._vertScrollBar.setVisibility( vertVisible );
      this._config.vBarWidth = this._getVerticalBarWidth();
      this._layoutX();
      this._layoutY();
    },

    getVerticalBar : function() {
      return this._vertScrollBar;
    },

    getHorizontalBar : function() {
      return this._horzScrollBar;
    },

    isVerticalBarVisible : function() {
      return this._vertScrollBar.getVisibility();
    },

    isHorizontalBarVisible : function() {
      return this._horzScrollBar.getVisibility();
    },

    setAlignment : function( column, value ) {
      this._config.alignment[ column ] = value;
      this._scheduleUpdate();
    },

    setWordWrap : function( column, value ) {
      this._config.wordWrap[ column ] = value;
      this._scheduleUpdate();
    },

    setAutoHeight : function( value ) {
      this._config.autoHeight = value;
      this._scheduleUpdate();
    },

    setCellCheck : function( column, value ) {
      this._config.itemCellCheck[ column ] = value;
      this._scheduleUpdate();
    },

    setLinesVisible : function( value ) {
      this._config.linesVisible = value;
      this.toggleState( "linesvisible", value );
      this._rowContainer.updateGridLines();
      this._scheduleUpdate();
    },

    setAlwaysHideSelection : function( value ) {
      this._config.alwaysHideSelection = value;
      this._scheduleUpdate();
    },

    setIndentionWidth : function( value ) {
      this._config.indentionWidth = value;
      this._scheduleUpdate();
    },

    //////////////
    // Overwritten

    addState : function( state ) {
      this.base( arguments, state );
      if( state.slice( 0, 8 ) === "variant_" ) {
        this._config.variant = state;
        this._rootItem.setVariant( state );
        this._rowContainer.updateGridLines();
        this._scheduleColumnUpdate();
      }
    },

    removeState : function( state ) {
      if( this._config.variant === state ) {
        this._config.variant = null;
        this._rootItem.setVariant( null );
        this._rowContainer.updateGridLines();
        this._scheduleColumnUpdate();
      }
      this.base( arguments, state );
    },

    ///////////////////////////////////////////////
    // API for Tests, DND, TreeUtil and TableColumn

    getRenderConfig : function() {
      return this._config;
    },

    getRootItem : function() {
      return this._rootItem;
    },

    isFocusItem : function( item ) {
      return this._focusItem === item;
    },

    isItemSelected : function( item ) {
      return this._selection.indexOf( item ) != -1;
    },

    getSelection : function() {
      return this._selection.slice( 0 );
    },

    getRowContainer : function() {
      return this._rowContainer;
    },

    getTableHeader : function() {
      return this._header;
    },

    getFooter : function() {
      return this._footer;
    },

    update : function() {
      this._scheduleUpdate();
    },

    addColumn : function( column ) {
      //this.getTableHeader().addColumn( column );
      this._columns[ column.toHashCode() ] = column;
      column.addEventListener( "update", this._scheduleColumnUpdate, this );
      this._scheduleColumnUpdate();
    },

    setColumnOrder : function( columnOrder ) {
      this._columnOrder = columnOrder;
      if( columnOrder && columnOrder.length > 0 ) {
        this._config.cellOrder = columnOrder.map( function( column ) {
          return column.getIndex();
        } );
      } else {
        this._config.cellOrder = [ 0 ];
      }
    },

    getColumnOrder : function() {
      return this._columnOrder;
    },

    removeColumn : function( column ) {
      //this.getTableHeader().removeColumn( column );
      delete this._columns[ column.toHashCode() ];
      column.removeEventListener( "update", this._scheduleColumnUpdate, this );
      this._scheduleColumnUpdate();
    },

    ////////////////
    // event handler

    _onItemUpdate : function( event ) {
      var item = event.target;
      if( event.msg === "collapsed" ) {
        if(    this._focusItem
            && ( this._focusItem.isDisposed() || this._focusItem.isChildOf( item ) )
        ) {
          this.setFocusItem( item );
        }
      } else if( event.msg === "remove" ) {
        this._scheduleUpdate( "checkDisposedItems" );
      } else if( event.msg === "height" ) {
        this._enableAltScrolling();
        this._scheduleUpdate( "scrollHeight" );
      }
      if( !event.rendering ) {
        this._renderItemUpdate( item, event );
      }
      return false;
    },

    _beforeAppear : function() {
      this.base( arguments );
      this._scheduleColumnUpdate();
    },

    _afterInsertDom : function() {
      this.base( arguments );
      if( this._config.autoHeight ) {
        this._rowContainer.renderRowHeight();
      }
    },

    _onChangeSeeable : function() {
      this._config.seeable = this.isSeeable();
    },

    _scheduleColumnUpdate : function() {
      rwt.widgets.base.Widget.addToGlobalWidgetQueue( this );
      this._scheduleUpdate();
    },

    flushWidgetQueue : function() {
      this._updateColumns();
    },

    _onVertScrollBarChangeValue : function() {
      this._updateTopItemIndex();
    },

    _updateTopItemIndex : function() {
      this._topItemIndex = this._vertScrollBar.getValue();
      this._topItem = null;
      if( this._allowRender() ) {
        this._updateTopItem( true );
      } else {
        this._scheduleUpdate( "topItem" );
      }
      this.dispatchSimpleEvent( "topItemChanged" );
    },

    _onHorzScrollBarChangeValue : function() {
      this._setScrollLeft( this._adjustScrollLeft( this._horzScrollBar.getValue() ) );
      this.dispatchSimpleEvent( "scrollLeftChanged" );
    },

    _onHorzScrollBarChangeMaximum : function() {
      this._setScrollLeft( this._adjustScrollLeft( this._horzScrollBar.getValue() ) );
    },

    _setScrollLeft : function( scrollLeft ) {
      this._rowContainer.setScrollLeft( scrollLeft );
      if( this._header ) {
        this._header.setScrollLeft( scrollLeft );
      }
      if( this._footer ) {
        this._footer.setScrollLeft( scrollLeft );
      }
    },

    _onMouseDown : function( event ) {
      this._delayedSelection = false;
      if( !this._checkAndProcessHyperlink( event ) ) {
        var row = this._rowContainer.findRowByElement( event.getDomTarget() );
        if( row ) {
          this._onRowMouseDown( row, event );
        }
      }
    },

    _onMouseUp : function( event ) {
      if( this._delayedSelection ) {
        this._onMouseDown( event );
      } else {
        this._checkAndProcessHyperlink( event );
      }
    },

    _onClick : function( event ) {
      this._checkAndProcessHyperlink( event );
    },

    _onRowMouseDown : function( row, event ) {
      var item = this._rowContainer.findItemByRow( row );
      if( item != null ) {
        var identifier = row.identify( event.getDomTarget() );
        if( identifier[ 0 ] === "expandIcon" && item.hasChildren() ) {
          this._onExpandClick( item );
        } else if( identifier[ 0 ] === "checkBox" || identifier[ 0 ] === "cellCheckBox" ) {
          this._toggleCheckSelection( item, identifier[ 1 ] );
        } else if( identifier[ 0 ] === "selectableCell" ) {
          this._fireSelectionChanged( item, "cell", null, identifier[ 1 ] );
        } else if( identifier[ 0 ] === "treeColumn" || this._acceptsGlobalSelection() ) {
          this._onSelectionClick( event, item );
        }
      }
    },

    _acceptsGlobalSelection : function() {
      return this._config.fullSelection || this._config.rowTemplate;
    },

    _checkAndProcessHyperlink : function( event ) {
      var hyperlink = null;
      var target = event.getOriginalTarget();
      if( this._config.markupEnabled && target instanceof rwt.widgets.base.GridRowContainer ) {
        hyperlink = this._findHyperlink( event );
        if( hyperlink !== null && this._isRWTHyperlink( hyperlink ) ) {
          event.setDefaultPrevented( true );
          if( event.getType() === "click" ) {
            var row = this._rowContainer.findRowByElement( event.getDomTarget() );
            var item = this._rowContainer.findItemByRow( row );
            var text = hyperlink.getAttribute( "href" );
            if( !text ) {
              text = hyperlink.innerHTML;
            }
            this._fireSelectionChanged( item, "hyperlink", null, text );
          }
        }
      }
      return hyperlink !== null;
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

    _onExpandClick : function( item ) {
       var expanded = !item.isExpanded();
       if( !expanded ) {
         this._deselectVisibleChildren( item );
       }
       item.setExpanded( expanded );
    },

    _onSelectionClick : function( event, item ) {
      // NOTE: Using a listener for "dblclick" does not work because the
      //       item is re-rendered on mousedown which prevents the dom-event.
      var doubleClick = this._isDoubleClicked( event, item );
      if( doubleClick ) {
        this._fireSelectionChanged( item, "defaultSelection" );
      } else if( !this._hasMultiSelection ) {
        this._singleSelectItem( event, item );
      } else if( !this._delayMultiSelect( event, item ) ) {
        this._multiSelectItem( event, item );
      }
    },

    _delayMultiSelect : function( event, item ) {
      if( this._isDragSource() && this.isItemSelected( item ) && event.getType() === "mousedown" ) {
        this._delayedSelection = true;
      }
      return this._delayedSelection;
    },

    _onMouseOut : function() {
      this._delayedSelection = false;
    },

    _onClientAreaMouseWheel : function( event ) {
      var delta = event.getWheelDelta();
      var direction = Math.abs( delta ) / delta;
      var change = Math.ceil( Math.abs( delta ) * 2 ) * direction;
      var orgValue = this._vertScrollBar.getValue();
      this._vertScrollBar.setValue( orgValue - change );
      var newValue = this._vertScrollBar.getValue();
      this._vertScrollBar.setValue( newValue ); // See Bug 396309
      if( newValue !== orgValue ) {
        event.preventDefault();
        event.stopPropagation();
      }
    },

    _onKeyPress : function( event ) {
      var rtl = this.getDirection() === "rtl";
      if( this._focusItem != null ) {
        switch( event.getKeyIdentifier() ) {
          case "Enter":
            this._handleKeyEnter( event );
          break;
          case "Space":
            this._handleKeySpace( event );
          break;
          case "Up":
            this._handleKeyUp( event );
          break;
          case "Down":
            this._handleKeyDown( event );
          break;
          case "PageUp":
            this._handleKeyPageUp( event );
          break;
          case "PageDown":
            this._handleKeyPageDown( event );
          break;
          case "Home":
            this._handleKeyHome( event );
          break;
          case "End":
            this._handleKeyEnd( event );
          break;
          case "Left":
            if( rtl ) {
              this._handleKeyRight( event );
            } else {
              this._handleKeyLeft( event );
            }
          break;
          case "Right":
            if( rtl ) {
              this._handleKeyLeft( event );
            } else {
              this._handleKeyRight( event );
            }
          break;
        }
      }
      this._stopKeyEvent( event );
    },

    _stopKeyEvent : function( event ) {
      switch( event.getKeyIdentifier() ) {
        case "Up":
        case "Down":
        case "Left":
        case "Right":
        case "Home":
        case "End":
        case "PageUp":
        case "PageDown":
          event.preventDefault();
          event.stopPropagation();
        break;
      }
    },

    _onShowResizeLine : function( event ) {
      var x = event.position;
      if( this._resizeLine === null ) {
        this._resizeLine = new rwt.widgets.base.Terminator();
        this._resizeLine.setAppearance( "table-column-resizer" );
        this.add( this._resizeLine );
        rwt.widgets.base.Widget.flushGlobalQueues();
      }
      var top = this._rowContainer.getTop();
      this._resizeLine._renderRuntimeTop( top );
      var left = x - 2 - this._horzScrollBar.getValue();
      if( this.getDirection() === "rtl" ) {
        this._resizeLine._renderRuntimeRight( left );
      } else {
        this._resizeLine._renderRuntimeLeft( left );
      }
      this._resizeLine._renderRuntimeHeight( this._rowContainer.getHeight() );
      this._resizeLine.removeStyleProperty( "visibility" );
    },

    _onHideResizeLine : function() {
      this._resizeLine.setStyleProperty( "visibility", "hidden" );
    },

    _handleKeyEnter : function() {
      this._fireSelectionChanged( this._focusItem, "defaultSelection" );
    },

    _handleKeySpace : function( event ) {
      if( event.isCtrlPressed() || !this.isItemSelected( this._focusItem ) ) {
        // NOTE: When space does not change the selection, the SWT Tree still fires an selection
        //       event, while the Table doesnt. Table behavior is used since it makes more sense.
        var itemIndex = this._focusItem.getFlatIndex();
        this._handleKeyboardSelect( event, this._focusItem, itemIndex );
      }
      if( this._config.hasCheckBoxes ) {
        this._toggleCheckSelection( this._focusItem );
      }
    },

    _handleKeyUp : function( event ) {
      var item = this._focusItem.getPreviousItem();
      if( item != null ) {
        var itemIndex = item.getFlatIndex();
        this._handleKeyboardSelect( event, item, itemIndex );
      }
    },

    _handleKeyDown : function( event ) {
      var item = this._focusItem.getNextItem();
      if( item != null ) {
        var itemIndex = item.getFlatIndex();
        this._handleKeyboardSelect( event, item, itemIndex );
      }
    },

    _handleKeyPageUp : function( event ) {
      var oldOffset = this._focusItem.getOffset();
      var diff = this._getClientAreaHeight();
      var newOffset = Math.max( 0, oldOffset - diff );
      var item = this._rootItem.findItemByOffset( newOffset );
      if( newOffset !== 0 ) {
        item = item.getNextItem();
      }
      var itemIndex = item.getFlatIndex();
      this._handleKeyboardSelect( event, item, itemIndex );
    },

    _handleKeyPageDown : function( event ) {
      var oldOffset = this._focusItem.getOffset();
      var diff = this._getClientAreaHeight();
      var max = this.getRootItem().getOffsetHeight() - 1;
      var newOffset = Math.min( max, oldOffset + diff );
      var item = this._rootItem.findItemByOffset( newOffset );
      if( newOffset !== max ) {
        item = item.getPreviousItem();
      }
      var itemIndex = item.getFlatIndex();
      this._handleKeyboardSelect( event, item, itemIndex );
    },

    _handleKeyHome : function( event ) {
      var item = this.getRootItem().getChild( 0 );
      this._handleKeyboardSelect( event, item, 0 );
    },

    _handleKeyEnd : function( event ) {
      var item = this.getRootItem().getLastChild();
      var itemIndex = this.getRootItem().getVisibleChildrenCount() - 1;
      this._handleKeyboardSelect( event, item, itemIndex );
    },

    _handleKeyLeft : function( event ) {
      if( event.isCtrlPressed() ) {
        this._scrollLeft();
      } else if( this._focusItem.isExpanded() ) {
        this._focusItem.setExpanded( false );
      } else if( !this._focusItem.getParent().isRootItem() ) {
        var item = this._focusItem.getParent();
        var itemIndex = item.getFlatIndex();
        this._handleKeyboardSelect( event, item, itemIndex, true );
      } else {
        this._scrollLeft();
      }
    },

    _handleKeyRight : function( event ) {
      if( event.isCtrlPressed() ) {
        this._scrollRight();
      } else if( this._focusItem.hasChildren() ) {
        if( !this._focusItem.isExpanded() ) {
          this._focusItem.setExpanded( true );
        } else {
          var item = this._focusItem.getChild( 0 );
          var itemIndex = item.getFlatIndex();
          this._handleKeyboardSelect( event, item, itemIndex, true );
        }
      } else {
        this._scrollRight();
      }
    },

    _handleKeyboardSelect : function( event, item, itemIndex, suppressMulti ) {
      if( this._hasMultiSelection && !suppressMulti ) {
        this._multiSelectItem( event, item );
      } else {
        this._singleSelectItem( event, item );
      }
      this._scrollIntoView( itemIndex, item );
    },

    _onFocusIn : function() {
      if( this._focusItem === null ) {
        var firstItem = this._rootItem.getChild( 0 );
        if( firstItem ) {
          this.setFocusItem( firstItem );
        }
      }
    },

    _scrollLeft : function() {
      this._horzScrollBar.setValue( this._horzScrollBar.getValue() - 10 );
    },

    _scrollRight : function() {
      this._horzScrollBar.setValue( this._horzScrollBar.getValue() + 10 );
    },

    /////////////////
    // render content

    _updateColumns : function() {
      this.setColumnOrder( this._columnOrder );
      this._updateScrollWidth();
      if( this._header != null ) {
        this._header.renderColumns( this._columns );
      }
      if( this._footer != null ) {
        this._footer.renderColumns( this._columns );
      }
    },

    _renderItemUpdate : function( item, event ) {
      if( item.isDisplayable() ) {
        switch( event.msg ) {
          case "expanded":
          case "collapsed":
            this._topItem = null;
            this._scheduleUpdate( "scrollHeight" );
          break;
          case "add":
          case "remove":
            // NOTE: the added/removed item is a child of this item
            if( item.isExpanded() ) {
              this._scheduleUpdate( "scrollHeight" );
            } else {
              this._scheduleItemUpdate( item );
            }
            this._topItem = null;
          break;
          case "content":
            this._scheduleItemUpdate( item );
          break;
          default:
            if( this._allowRender() ) {
              this._rowContainer.renderItem( item );
            } else {
              this._scheduleItemUpdate( item );
            }
          break;
        }
      }
    },

    /**
     * This will schedule the entire content of the tree (visible rows and gridlines)
     * to be re-rendered. Additional tasks my be executed depending on "task" parameter.
     * Is only used within a server-response or when expanding/collapsing. Not used
     * when user is scrolling.
     */
    _scheduleUpdate : function( task ) {
      if( task !== undefined ) {
        this.addToQueue( task );
      }
      this._renderQueue[ "allItems" ] = true;
      this.addToQueue( "updateRows" );
    },

    /**
     * Optimized version of _scheduleUpdate. Used when server only changes specific items.
     */
    _scheduleItemUpdate : function( item ) {
      this._renderQueue[ item.toHashCode() ] = item;
      this.addToQueue( "updateRows" );
    },

    _layoutPost : function( changes ) {
      this.base( arguments, changes );
      if( changes[ "checkDisposedItems" ] ) {
        this._checkDisposedItems();
      }
      if( changes[ "scrollHeight" ] ) {
        this._updateScrollHeight();
      }
      if( changes[ "scrollHeight" ] || changes[ "topItem" ] ) {
        this._updateTopItem( false );
      }
      if( changes[ "updateRows" ] ) {
        if( this._renderQueue[ "allItems" ] ) {
          this._rowContainer.renderAll();
        } else {
          this._rowContainer.renderItemQueue( this._renderQueue );
        }
        this._renderQueue = {};
      }
    },

    ////////////
    // scrolling

    _enableAltScrolling : function() {
      if( !this._altScrollingEnabled ) {
        this._altScrollingEnabled = true;
        this._vertScrollBar.setThumb( 1 );
      }
    },

    _updateScrollHeight : function() {
      var max = this.getRootItem().getVisibleChildrenCount();
      if( this._altScrollingEnabled ) {
        max = Math.min( max,  max - this._getLastPageRowCount() + 1 );
        this._setVerticalScrollBarVisible( max > 1 );
      }
      if( !this._vertScrollBar.getDisposed() && ( this._vertScrollBar.getMaximum() !== max ) ) {
        this._vertScrollBar.setMaximum( max );
      }
    },

    _getLastPageRowCount : function() {
      var availableHeight = this._getClientAreaHeight();
      var item = this._getLastVisibleItem();
      var result = 0;
      while( item && availableHeight > 0 ) {
        availableHeight -= item.getOwnHeight();
        if( availableHeight > 0 ) {
          result++;
          item = item.getPreviousItem();
        }
      }
      return result;
    },

    _getLastVisibleItem : function() {
      var item = this.getRootItem().getLastChild();
      while( item && item.hasChildren() && item.isExpanded() ) {
        item = item.getLastChild();
      }
      return item;
    },

    _updateScrollThumbHeight : function() {
      if( !this._altScrollingEnabled ) {
        var value = Math.max( 1, Math.floor( this._getClientAreaHeight() / this._itemHeight ) );
        this._vertScrollBar.setThumb( value );
      }
    },

    _setVerticalScrollBarVisible : function( value ) {
      var oldValue = this._vertScrollBar.getVisibility();
      if( value !== oldValue ) {
        this._vertScrollBar.setVisibility( value );
        this._layoutX();
      }
    },

    /**
     * NOTE: If render is true, the content will be updated immediately. The rendering
     * assumes that no other parameter than topItem have changed and may optimize accordingly.
     */
    _updateTopItem : function( render ) {
      this._rowContainer.setTopItem( this._getTopItem(), this._topItemIndex, render );
    },

    _updateScrollWidth : function() {
      var width = this._getItemWidth();
      this._rowContainer.setRowWidth( this._getRowWidth() );
      if( !this._horzScrollBar.getDisposed() ) {
        this._horzScrollBar.setMaximum( width );
      }
      if( this._header ) {
        this._header.setScrollWidth( width + this._getVerticalBarWidth() );
      }
      if( this._footer ) {
        this._footer.setScrollWidth( width + this._getVerticalBarWidth() );
      }
    },

    _scrollIntoView : function( index, item ) {
      if( index < this._topItemIndex ) {
        this._setTopItemIndex( index );
      } else if( index > this._topItemIndex ) {
        var topItem = this._getTopItem();
        var topItemOffset = topItem.getOffset();
        var itemOffset = item.getOffset();
        var pageSize = this._getClientAreaHeight() - item.getOwnHeight();
        if( itemOffset > topItemOffset + pageSize ) {
          var newTopOffset = itemOffset - pageSize - 1;
          var newTopItem = this.getRootItem().findItemByOffset( newTopOffset );
          var newTopIndex = newTopItem.getFlatIndex() + 1;
          this._setTopItemIndex( newTopIndex );
        }
      }
      if( this._allowRender() ) {
        rwt.widgets.base.Widget.flushGlobalQueues();
      }
    },

    _setTopItemIndex : function( index ) {
      this._updateScrollHeight();
      this._vertScrollBar.setValue( index );
    },

    //////////////
    // Fire events

    _fireSelectionChanged : function( item, type, index, text ) {
      var data = {
        "item" : item,
        "type" : type,
        "index" : typeof index === "number" ? index : undefined,
        "text" : text != null ? text : undefined
      };
      this.dispatchSimpleEvent( "selectionChanged", data );
    },

    _isDoubleClicked : function( event, item ) {
      var result = false;
      var mousedown = event.getType() === "mousedown";
      var leftClick = event.getButton() === "left";
      if( leftClick && mousedown && this.isFocusItem( item ) && this._selectionTimestamp != null ) {
        var stamp = new Date();
        var offset = event.getPageX();
        var timeDiff = rwt.remote.EventUtil.DOUBLE_CLICK_TIME;
        var offsetDiff = 8;
        if (    stamp.getTime() - this._selectionTimestamp.getTime() < timeDiff
             && Math.abs( this._selectionOffsetX - offset ) < offsetDiff )
        {
          result = true;
        }
      }
      if( mousedown && leftClick && !result ) {
        this._selectionTimestamp = new Date();
        this._selectionOffsetX = event.getPageX();
      } else if( mousedown ) {
        this._selectionTimestamp = null;
      }
      return result;
    },

    ////////////////////
    // focus & selection

    _singleSelectItem : function( event, item ) {
      if( event.isCtrlPressed() && this.isItemSelected( item ) ) {
        // NOTE: Apparently in SWT this is only supported by Table, not Tree.
        //       No reason not to support it in RAP though.
        this._ctrlSelectItem( item );
      } else {
        this._exclusiveSelectItem( item );
      }
    },

    _multiSelectItem : function( event, item ) {
      if( event instanceof rwt.event.MouseEvent && event.isRightButtonPressed() ) {
        if( !this.isItemSelected( item ) ) {
          this._exclusiveSelectItem( item );
        }
      } else if( event.isCtrlPressed() ) {
        if( event instanceof rwt.event.KeyEvent && item != this._focusItem  ) {
          this.setFocusItem( item );
        } else {
          this._ctrlSelectItem( item );
        }
      } else if( event.isShiftPressed() ) {
        if( this._focusItem != null ) {
          this._shiftSelectItem( item );
        } else {
          this._exclusiveSelectItem( item );
        }
      } else {
        this._exclusiveSelectItem( item );
      }
    },

    _exclusiveSelectItem : function( item ) {
      this.deselectAll();
      this._leadItem = null;
      this._selectItem( item, true );
      this._fireSelectionChanged( item, "selection" );
      this.setFocusItem( item );
    },

    _ctrlSelectItem : function( item ) {
      if( !this.isItemSelected( item ) ) {
        this._selectItem( item, true );
      } else {
        this._deselectItem( item, true );
      }
      this._fireSelectionChanged( item, "selection" );
      this.setFocusItem( item );
    },

    _shiftSelectItem : function( item ) {
      this.deselectAll();
      var currentItem = this._leadItem != null ? this._leadItem : this._focusItem;
      this._leadItem = currentItem;
      var targetItem = item;
      var startIndex = currentItem.getFlatIndex();
      var endIndex = targetItem.getFlatIndex();
      if( startIndex > endIndex ) {
        var temp = currentItem;
        currentItem = targetItem;
        targetItem = temp;
      }
      this._selectItem( currentItem, true );
      while( currentItem !== targetItem ) {
        currentItem = currentItem.getNextItem();
        this._selectItem( currentItem, true );
      }
      this._fireSelectionChanged( item, "selection" );
      this.setFocusItem( item );
    },

    _selectItem : function( item, render ) {
      if( !this.isItemSelected( item ) ) {
        this._selection.push( item );
      }
      if( render ) {
        this._rowContainer.renderItem( item );
      }
    },

    _deselectItem : function( item, render ) {
      if( this.isItemSelected( item ) ) {
        this._selection.splice( this._selection.indexOf( item ), 1 );
      }
      if( render ) {
        this._rowContainer.renderItem( item );
      }
    },

    deselectAll : function() {
      this._checkDisposedItems();
      var oldSelection = this._selection;
      this._selection = [];
      for( var i = 0; i < oldSelection.length; i++ ) {
        this._rowContainer.renderItem( oldSelection[ i ] );
      }
    },

    _toggleCheckSelection : function( item, cell ) {
      if( item.isCached() ) {
        if( isNaN( cell ) ) {
          item.setChecked( !item.isChecked() );
          this._fireSelectionChanged( item, "check" );
        } else if( item.isCellCheckable( cell ) ) {
          item.toggleCellChecked( cell );
          this._fireSelectionChanged( item, "cellCheck", cell );
        }
      }
    },

    _deselectVisibleChildren : function( item ) {
      var currentItem = item.getNextItem();
      var finalItem = item.getNextItem( true );
      while( currentItem !== finalItem ) {
        this._deselectItem( currentItem, false );
        currentItem = currentItem.getNextItem();
      }
    },

    _applyFocused : function( newValue, oldValue ) {
      this.base( arguments, newValue, oldValue );
      this._config.focused = newValue;
      this._scheduleUpdate();
    },

    _applyEnabled : function( newValue, oldValue ) {
      this.base( arguments, newValue, oldValue );
      this._config.enabled = newValue;
      this._scheduleUpdate();
    },

    _checkDisposedItems : function() {
      // NOTE : FocusItem might already been fixed by the server. But since this is not
      //        always the case (depending on the server-side widget), we also do it here.
      if( this._focusItem && this._focusItem.isDisposed() ) {
        this._focusItem = null;
      }
      if( this._leadItem && this._leadItem.isDisposed() ) {
        this._leadItem = null;
      }
      var i = 0;
      while( i < this._selection.length ) {
        if( this._selection[ i ].isDisposed() ) {
          this._deselectItem( this._selection[ i ], false );
        } else {
          i++;
        }
      }
    },

    ////////////////////////////
    // internal layout & theming

    _applyTextColor : function( newValue, oldValue ) {
      this.base( arguments, newValue, oldValue );
      this._config.textColor = newValue;
      this._scheduleUpdate();
    },

    _applyFont : function( newValue, oldValue ) {
      this.base( arguments, newValue, oldValue );
      this._config.font = newValue;
      this._scheduleUpdate();
    },

    _applyBackgroundColor : function( newValue ) {
      this._rowContainer.setBackgroundColor( newValue );
    },

    _applyBackgroundImage : function( newValue ) {
      this._rowContainer.setBackgroundImage( newValue );
    },

    _applyDirection : function( value ) {
      this.base( arguments, value );
      this._config.rtl = value === "rtl";
      this.getLayoutImpl().setMirror( value === "rtl" );
      this._rowContainer.setDirection( value );
      this._horzScrollBar.setDirection( value );
      if( this._header ) {
        this._header.setDirection( value );
      }
      if( this._footer ) {
        this._footer.setDirection( value );
      }
      this._onHorzScrollBarChangeValue();
      this._scheduleUpdate();
    },

    _layoutX : function() {
      var width = Math.max( 0, this.getWidth() - this.getFrameWidth() );
      if( this._header ) {
        this._header.setLeft( 0 );
        this._header.setWidth( width );
      }
      if( this._footer ) {
        this._footer.setLeft( 0 );
        this._footer.setWidth( width );
      }
      if( this._vertScrollBar.getVisibility() ) {
        this._vertScrollBar.setLeft( width - this._vertScrollBar.getWidth() );
      }
      this._horzScrollBar.setLeft( 0 );
      this._horzScrollBar.setWidth( width - this._getVerticalBarWidth() );
      this._rowContainer.setWidth( width );
      this._updateScrollWidth();
      this._scheduleUpdate();
    },

    _layoutY : function() {
      var headerHeight = this._header ? this._headerHeight : 0;
      var footerHeight = this._footer ? this._footerHeight : 0;
      var top = headerHeight;
      var height = this.getHeight() - this.getFrameHeight() - headerHeight - footerHeight;
      height = Math.max( 0, height );
      if( this._header ) {
        this._header.setHeight( this._headerHeight );
      }
      if( this._footer ) {
        this._footer.setHeight( this._footerHeight );
        this._footer.setTop( top + height );
      }
      if( this._horzScrollBar.getVisibility() ) {
        this._horzScrollBar.setTop( top + height + footerHeight - this._getHorizontalBarHeight() );
      }
      this._vertScrollBar.setHeight( height + footerHeight - this._getHorizontalBarHeight() );
      this._vertScrollBar.setTop( top );
      this._rowContainer.setTop( top );
      this._rowContainer.setHeight( height );
      this._updateScrollThumbHeight();
      this._scheduleUpdate( "scrollHeight" );
    },

    _getItemWidth : function() {
      var result = 0;
      if( this._config.rowTemplate ) {
        result = this._rowContainer.getWidth();
      } else if( this._config.itemLeft.length > 0 ) {
        var columnCount = Math.max( 1, this._config.columnCount );
        for( var i = 0; i < columnCount; i++ ) {
          result = Math.max( result, this._config.itemLeft[ i ] + this._config.itemWidth[ i ] );
        }
      }
      return result;
    },

    _getRowWidth : function() {
      var width = this._rowContainer.getWidth();
      var bar = this._getVerticalBarWidth();
      return Math.max( this._getItemWidth() + ( this.isHorizontalBarVisible() ? bar : 0 ), width );
    },

    /////////
    // helper

    _allowRender : function() {
      return !this._disableRender && !rwt.remote.EventUtil.getSuspended() && this.isSeeable();
    },

    _isDragSource : function() {
      return this.hasEventListeners( "dragstart" );
    },

    _getTopItem : function() {
      if( this._topItem === null ) {
        this._topItem = this._rootItem.findItemByFlatIndex( this._topItemIndex );
      }
      return this._topItem;
    },

    _getHorizontalBarHeight : function() {
      return this._horzScrollBar.getVisibility() ? this._horzScrollBar.getHeight() : 0;
    },

    _getVerticalBarWidth : function() {
      return this._vertScrollBar.getVisibility() ? this._vertScrollBar.getWidth() : 0;
    },

    _getClientAreaHeight : function() {
      var height = this._rowContainer.getHeight();
      return this._footer ? height : height - this._getHorizontalBarHeight();
    },

    _adjustScrollLeft : function( scrollLeft ) {
      return rwt.widgets.base.Scrollable.adjustScrollLeft( this, scrollLeft );
    },

    ////////////////////////
    // Cell tooltip handling

    setEnableCellToolTip : function( value ) { // protocol name
      this.setCellToolTipsEnabled( value );
    },

    setCellToolTipsEnabled : function( value ) {
      this._rowContainer.setCellToolTipsEnabled( value );
      rwt.widgets.util.GridCellToolTipSupport.setEnabled( this, value );
    },

    getCellToolTipsEnabled : function() {
      return this._rowContainer.getCellToolTipsEnabled();
    },

    setCellToolTipText : function( value ) {
      var text = value;
      if( this.getUserData( "toolTipMarkupEnabled" ) !== true ) {
        var EncodingUtil = rwt.util.Encoding;
        text = EncodingUtil.escapeText( text, false );
        text = EncodingUtil.replaceNewLines( text, "<br/>" );
      }
      rwt.widgets.util.GridCellToolTipSupport.showToolTip( text );
    }

  }

} );
