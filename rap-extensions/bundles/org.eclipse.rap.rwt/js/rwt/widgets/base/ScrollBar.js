/*******************************************************************************
 * Copyright (c) 2011, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.base.ScrollBar", {

  extend : rwt.widgets.base.AbstractSlider,

  construct : function( horizontal ) {
    this.base( arguments, horizontal );
    this._idealValue = 0;
    this._idealThumb = 10;
    this._lastDispatchedValue = 0;
    this._renderSum = 0;
    this._renderSamples = 0;
    this.setMinimum( 0 );
    this._hasSelectionListener = false;
    var themeValues = new rwt.theme.ThemeValues( this.__states );
    this.setMinThumbSize( themeValues.getCssDimension( "ScrollBar-Thumb", "min-height" ) );
    this._autoThumbSize = true;
    this.setIncrement( 20 );
    this.addEventListener( "mousedown", this._stopEvent, this );
    this.addEventListener( "mouseup", this._stopEvent, this );
    this.addEventListener( "click", this._stopEvent, this );
    this.addEventListener( "dblclick", this._stopEvent, this );
  },

  members : {

    _configureAppearance : function() {
      this.setAppearance( "scrollbar" );
      this._thumb.setAppearance( "scrollbar-thumb" );
      this._minButton.setAppearance( "scrollbar-min-button" );
      this._maxButton.setAppearance( "scrollbar-max-button" );
    },

    _applyOpacity : function( value, old ) {
      this.base( arguments, value, old );
      // No transition for the very first time opacity is applied (widget creation)
      rwt.html.Style.setTransition( this, "opacity 250ms" );
    },

    //////
    // API

    setValue : function( value ) {
      this._idealValue = value;
      this._setSelection( value );
    },

    getValue : function() {
      return this._selection;
    },

    setMaximum : function( value ) {
      this.base( arguments, value );
      this._checkValue();
    },

    setThumb : function( value ) {
      this.base( arguments, value );
      this._checkValue();
      this._updatePageIncrement();
    },

    setIncrement : function( value ) {
      this._setIncrement( value );
    },

    setHasSelectionListener : function( value ) {
      this._hasSelectionListener = value;
    },

    getHasSelectionListener : function() {
      return this._hasSelectionListener;
    },

    isHorizontal : function() {
      return this._horizontal;
    },

    setAutoThumbSize : function( autoThumbSize ) {
      this._autoThumbSize = autoThumbSize;
    },

    ////////////
    // Internals

    _updateThumbLength : function() {
      if( this._autoThumbSize ) {
        var size = this._getSliderSize();
        if( size > 0) {
          this.setThumb( size );
        }
      }
    },

    _updatePageIncrement : function() {
      this._setPageIncrement( this.getThumb() );
    },

    _stopEvent : function( event ) {
      event.stopPropagation();
      event.preventDefault();
    },

    _dispatchValueChanged : function() {
      this._lastDispatchedValue = this._selection;
      this.createDispatchEvent( "changeValue" );
    },

    _updateStepsize : function() {
      var oldValue = this._selection;
      this.base( arguments );
      if( oldValue !== this._selection ) {
        this._dispatchValueChanged();
      }
    },

    //////////////
    // Overwritten

    _onChangeSize : function() {
      this._updateThumbLength();
      this._updatePageIncrement();
      this.base( arguments );
    },

    _renderThumbSize : function() {
      if( this.base( arguments ) ) {
        this._renderThumbIcon();
      }
    },

    _renderThumbIcon : function() {
      if( this._horizontal ) {
        var iconWidth = this._thumb.getCellWidth( 1 );
        var iconVisible = this._thumbLengthPx >= ( iconWidth + 6 );
        this._thumb.setCellVisible( 1, iconVisible );
      } else {
        var iconHeight = this._thumb.getCellHeight( 1 );
        var iconVisible = this._thumbLengthPx >= ( iconHeight + 6 );
        this._thumb.setCellVisible( 1, iconVisible );
      }
    },

    _checkValue : function() {
      if( this._idealValue !== null && this._idealValue ) {
        this._setSelection( this._idealValue );
      } else {
        this._setSelection( this._selection );
      }
    },

    _setSelection : function( value ) {
      if( value !== this._idealValue ) {
        this._idealValue = null;
      }
      this.base( arguments, value );
    },

    _selectionChanged : function() {
      this.base( arguments );
      this._dispatchValueChanged();
    }

  }

} );
