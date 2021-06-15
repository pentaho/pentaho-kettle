/*******************************************************************************
 * Copyright (c) 2008, 2016 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.ExpandBar", {

  extend : rwt.widgets.base.Scrollable,

  construct : function() {
    this.base( arguments, new rwt.widgets.base.Parent() );
    this.setAppearance( "expand-bar" );
    this.setHideFocus( true );
    this.setScrollBarsVisible( false, false );
    // This object is needed for proper scrolling behaviour
    this._bottomSpacing = new rwt.widgets.base.Parent();
    this._markupEnabled = false;
    this._clientArea.add( this._bottomSpacing );
  },

  destruct : function() {
    this._disposeObjects( "_bottomSpacing" );
  },

  members : {

    _applyDirection : function( value ) {
      this.base( arguments, value );
      this._clientArea.forEachChild( function() {
        if( this instanceof rwt.widgets.ExpandItem ) {
          this.setDirection( value );
        }
      } );
    },

    addWidget : function( widget ) {
      this._clientArea.add( widget );
    },

    setBottomSpacingBounds : function( x, y, width, height ) {
      this._bottomSpacing.setLeft( x );
      this._bottomSpacing.setTop( y );
      this._bottomSpacing.setWidth( width );
      this._bottomSpacing.setHeight( height );
    },

    setVScrollBarVisible : function( show ) {
      this.setScrollBarsVisible( false, show );
      if( !show ) {
        this.setVBarSelection( 0 );
      }
    },

    setVScrollBarMax : function( value ) {
      this._vertScrollBar.setMaximum( value );
    },

    setMarkupEnabled : function( value ) {
      this._markupEnabled = value;
    },

    isMarkupEnabled : function() {
      return this._markupEnabled;
    }

  }

} );
