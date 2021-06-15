/*******************************************************************************
 * Copyright (c) 2009, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.util.FocusIndicator", {

  extend : rwt.qx.Object,

  statics : {

    getInstance : function() {
      return rwt.runtime.Singletons.get( rwt.widgets.util.FocusIndicator );
    }

  },

  construct : function() {
    this.base( arguments );
    this._frame = null;
  },

  members : {

    _createFrame : function() {
      this._frame = document.createElement( "div" );
      this._frame.setAttribute( "id", "focusIndicator" ); // used by unit tests
      this._frame.style.position = "absolute";
      this._frame.style.fontSize = 0;
      this._frame.style.lineHeight = 0;
      this._frame.style.zIndex = 0;
    },

    _createTheme : function( widget, cssSelector, focusElement ) {
      var theme = null;
      var states = typeof widget.__states != "undefined" ? widget.__states : {};
      var tv = new rwt.theme.ThemeValues( states );
      var border = tv.getCssBorder( cssSelector, "border" );
      var opacity = tv.getCssFloat( cssSelector, "opacity" );
      var hasValidBorder = border instanceof rwt.html.Border;
      var margin = tv.getCssBoxDimensions( cssSelector, "margin" );
      var padding = tv.getCssBoxDimensions( cssSelector, "padding" );
      if( !padding ) {
        padding = [ 0, 0, 0, 0 ];
      }
      var paddingIsZero =    padding[ 0 ] === 0
                          && padding[ 1 ] === 0
                          && padding[ 2 ] === 0
                          && padding[ 3 ] === 0;
      var usePadding = !paddingIsZero && focusElement != null;
      if( hasValidBorder && ( margin != null || usePadding ) ) {
        var color = tv.getCssColor( cssSelector, "background-color" );
        theme = {
          "backgroundColor" : color != "undefined" ? color : "",
          "opacity" : opacity,
          "border" : border,
          "margin" : margin,
          "padding" : usePadding ? padding : null
        };
      }
      return theme;
    },

    show : function( widget, cssSelector, focusElement ) {
      this.hide();
      if( widget.isCreated() ) {
        var theme = this._createTheme( widget, cssSelector, focusElement );
        if( theme != null ) {
          var parentNode = widget._getTargetNode();
          if( this._frame == null ) {
            this._createFrame();
          }
          if( this._frame.parentNode != parentNode ) {
            if( parentNode.hasChildNodes() ) {
              var firstChild = parentNode.firstChild;
              parentNode.insertBefore( this._frame, firstChild );
            } else {
              parentNode.appendChild( this._frame );
            }
          }
          this._styleFocusIndicator( theme );
          this._layoutFocusIndicator( widget, theme, focusElement );
        }
      }
    },


    hide : function() {
      if( this._frame != null && this._frame.parentNode != null ) {
        this._frame.parentNode.removeChild( this._frame );
      }
    },

    _styleFocusIndicator : function( theme ) {
      // ignores complex borders and color-manager (for now):
      var border = theme[ "border" ];
      var style = this._frame.style;
      var edges = [ "Left", "Top", "Bottom", "Right" ];
      for( var i = 0; i < 4; i++ ) {
        var edge = edges[ i ];
        var borderColor = border[ "getColor" + edge ]();
        var borderStyle = border[ "getStyle" + edge ]();
        var borderWidth = border[ "getWidth" + edge ]();
        style[ "border" + edge + "Width" ] = borderWidth == null ? "0px" : borderWidth + "px";
        style[ "border" + edge + "Style" ] = borderStyle == null ? "none" : borderStyle;
        style[ "border" + edge + "Color" ] = borderColor == null ? "" : borderColor;
      }
      style.backgroundColor = theme[ "backgroundColor" ];
      this._styleFocusIndiactorOpacity( theme[ "opacity" ] );
    },

    _styleFocusIndiactorOpacity : function( value ) {
      var style = this._frame.style;
      var opacity = value == 1 ? "" : value;
      style.opacity = opacity;
      style.KhtmlOpacity = opacity;
      style.MozOpacity = opacity;
    },

    _layoutFocusIndicator : function( widget, theme, focusElement ) {
      // NOTE : It is assumed that a focusElement, if given, has
      //        valid css-bounds (top,left,width,height) set in "px".
      var bounds = [];
      if( theme[ "padding" ] != null ) {
        var padding = theme[ "padding" ];
        bounds[ 0 ] = focusElement.offsetLeft - padding[ 3 ];
        bounds[ 1 ] = parseInt( focusElement.style.top, 10 ) - padding[ 0 ];
        bounds[ 2 ] = parseInt( focusElement.style.width, 10 ) + padding[ 1 ] + padding[ 3 ];
        bounds[ 3 ] = parseInt( focusElement.style.height, 10 ) + padding[ 2 ] + padding[ 0 ];
      } else {
        var margin = theme[ "margin" ];
        // Fix for bug 312544
        widget._invalidateBoxWidth();
        widget._invalidateBoxHeight();
        var parentWidth = widget.getBoxWidth() - widget._cachedBorderLeft - widget._cachedBorderRight;
        var parentHeight =   widget.getBoxHeight()
                           - widget._cachedBorderTop
                           - widget._cachedBorderBottom;
        widget._invalidateBoxWidth();
        widget._invalidateBoxHeight();
        bounds[ 0 ] = margin[ 3 ];
        bounds[ 1 ] = margin[ 0 ];
        bounds[ 2 ] = parentWidth - ( margin[ 3 ] + margin[ 1 ] );
        bounds[ 3 ] = parentHeight - ( margin[ 0 ] + margin[ 2 ] );
        bounds[ 2 ] = Math.max( 0, bounds[ 2 ] );
        bounds[ 3 ] = Math.max( 0, bounds[ 3 ] );
      }
      this._frame.style.left = bounds[ 0 ] + "px";
      this._frame.style.top = bounds[ 1 ] + "px";
      this._frame.style.width = bounds[ 2 ] + "px";
      this._frame.style.height = bounds[ 3 ] + "px";
    }
  }

} );

