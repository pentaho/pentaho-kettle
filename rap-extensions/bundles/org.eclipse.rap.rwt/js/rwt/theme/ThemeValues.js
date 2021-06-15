/*******************************************************************************
 * Copyright (c) 2007, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

/**
 * An adapter class for accessing theme values.
 * Values for the widget state given in the construtor are selected if available.
 */
rwt.qx.Class.define( "rwt.theme.ThemeValues", {

  extend : rwt.qx.Object,

  /**
   * Creates a new ThemeValues instance for the variant defined in the given
   * widget states.
   */
  construct : function( states ) {
    if( states === undefined ) {
      throw new Error( "no states given" );
    }
    this._states = states;
    this._store = rwt.theme.ThemeStore.getInstance();
  },

  statics : {
    NONE_IMAGE : null,
    NONE_IMAGE_SIZED : [ null, 0, 0 ]
  },

  members : {

    getCssBorder : function( element, key ) {
      if( key === "border" ) {
        return this._store.getBorder( element, this._states, key );
      }
      return this._store.getBorderEdge( element, this._states, key );
    },

    getCssNamedBorder : function( name ) {
      return this._store.getNamedBorder( name );
    },

    getCssColor : function( element, key ) {
      return this._store.getColor( element, this._states, key );
    },

    getCssAlpha : function( element, key ) {
      return this._store.getAlpha( element, this._states, key );
    },

    getCssNamedColor : function( name ) {
      return this._store.getNamedColor( name );
    },

    getCssFont : function( element, key ) {
      return this._store.getFont( element, this._states, key );
    },

    getCssDimension : function( element, key ) {
      return this._store.getDimension( element, this._states, key );
    },

    getCssBoxDimensions : function( element, key ) {
      return this._store.getBoxDimensions( element, this._states, key );
    },

    getCssFloat : function( element, key ) {
      return this._store.getFloat( element, this._states, key );
    },

    getCssIdentifier : function( element, key ) {
      return this._store.getIdentifier( element, this._states, key );
    },

    getCssImage : function( element, key ) {
      return this._store.getImage( element, this._states, key );
    },

    getCssSizedImage : function( element, key ) {
      return this._store.getSizedImage( element, this._states, key );
    },

    getCssGradient : function( element, key ) {
      return this._store.getGradient( element, this._states, key );
    },

    getCssCursor : function( element, key ) {
      return this._store.getCursor( element, this._states, key );
    },

    getCssAnimation : function( element, key ) {
      return this._store.getAnimation( element, this._states, key );
    },

    getCssShadow : function( element, key ) {
      return this._store.getShadow( element, this._states, key );
    },

    mergeBorders : function( border, borderTop, borderRight, borderBottom, borderLeft ) {
      if( border == null ) {
        throw new Error( "Unable to merge borders. Base border is null" );
      }
      var result = border;
      var changed = false;
      var borderColors = border.getColors();
      var borderWidths = border.getWidths();
      var borderStyles = border.getStyles();
      if( borderTop != null && borderTop.getWidthTop() !== 0 ) {
        changed = true;
        borderColors[ 0 ] = borderTop.getColorTop();
        borderWidths[ 0 ] = borderTop.getWidthTop();
        borderStyles[ 0 ] = borderTop.getStyleTop();
      }
      if( borderRight != null && borderRight.getWidthRight() !== 0 ) {
        changed = true;
        borderColors[ 1 ] = borderRight.getColorRight();
        borderWidths[ 1 ] = borderRight.getWidthRight();
        borderStyles[ 1 ] = borderRight.getStyleRight();
      }
      if( borderBottom != null && borderBottom.getWidthBottom() !== 0 ) {
        changed = true;
        borderColors[ 2 ] = borderBottom.getColorBottom();
        borderWidths[ 2 ] = borderBottom.getWidthBottom();
        borderStyles[ 2 ] = borderBottom.getStyleBottom();
      }
      if( borderLeft != null && borderLeft.getWidthLeft() !== 0 ) {
        changed = true;
        borderColors[ 3 ] = borderLeft.getColorLeft();
        borderWidths[ 3 ] = borderLeft.getWidthLeft();
        borderStyles[ 3 ] = borderLeft.getStyleLeft();
      }
      if( changed ) {
        // TODO: Border radii are ingnored during the merge.
        result = new rwt.html.Border( borderWidths, borderStyles, borderColors );
      }
      return result;
    }

  }

} );
