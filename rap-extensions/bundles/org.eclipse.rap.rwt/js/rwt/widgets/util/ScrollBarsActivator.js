/*******************************************************************************
 * Copyright (c) 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

/*jshint nonew:false */
namespace( "rwt.widgets.util" );

rwt.widgets.util.ScrollBarsActivator = function( scrollable ) {
  this._scrollable = scrollable;
  this._hovered = false;
  this._activateScrollBarsTimer = new rwt.client.Timer( 150 );
  this._activateScrollBarsTimer.addEventListener( "interval", this._onActivationTimer, this );
  this._deactivateScrollBarsTimer = new rwt.client.Timer( 1000 );
  this._deactivateScrollBarsTimer.addEventListener( "interval", this._onDeactivationTimer, this );
  this._registerListeners();
};

rwt.widgets.util.ScrollBarsActivator.install = function( scrollable ) {
  new rwt.widgets.util.ScrollBarsActivator( scrollable );
};

rwt.widgets.util.ScrollBarsActivator.prototype = {

  _registerListeners : function() {
    this._scrollable.addEventListener( "elementOver", this._onClientOver, this );
    this._scrollable.addEventListener( "elementOut", this._onClientOut, this );
    if( this._scrollable instanceof rwt.widgets.Grid ) {
      this._scrollable.addEventListener( "topItemChanged", this._onScroll, this );
      this._scrollable.addEventListener( "scrollLeftChanged", this._onScroll, this );
    } else {
      this._scrollable.addEventListener( "scroll", this._onScroll, this );
    }
    this._scrollable.addEventListener( "dispose", this._onDispose, this );
  },

  _onDispose : function() {
    this._activateScrollBarsTimer.dispose();
    this._activateScrollBarsTimer = null;
    this._deactivateScrollBarsTimer.dispose();
    this._deactivateScrollBarsTimer = null;
  },

  _onScroll : function() {
    this._activateScrollBars( !this._hovered );
  },

  _onClientOver : function( event ) {
    if( !this._hovered && this._scrollable.contains( event.getOriginalTarget() ) ) {
      this._hovered = true;
      this._activateScrollBarsTimer.stop();
      this._activateScrollBarsTimer.start();
    }
  },

  _onClientOut : function( event ) {
    var related = event.getRelatedTarget();
    if( !this._scrollable.contains( related ) ) {
      this._hovered = false;
      this._deactivateScrollBars();
    }
  },

  _onChangeCapture : function() {
    if( !this._hovered ) {
      this._deactivateScrollBars();
    }
  },

  _onActivationTimer : function() {
    if( this._hovered ) {
      this._activateScrollBars( false );
    }
  },

  _onDeactivationTimer : function() {
    if( !this._hovered ) {
      this._deactivateScrollBars();
    }
  },

  _activateScrollBars : function( autoDeactivate ) {
    if( !this._scrollable.isDisposed() ) {
      this._scrollable.getHorizontalBar().addState( "active" );
      this._scrollable.getVerticalBar().addState( "active" );
      this._activateScrollBarsTimer.stop();
      this._deactivateScrollBarsTimer.stop();
      if( autoDeactivate ) {
        this._deactivateScrollBarsTimer.start();
      }
    }
  },

  _deactivateScrollBars : function() {
    if( !this._scrollable.isDisposed() ) {
      var capture = rwt.event.EventHandler.getCaptureWidget();
      if( capture && this._scrollable.contains( capture ) ) {
        capture.addEventListener( "changeCapture", this._onChangeCapture, this );
      } else {
        this._scrollable.getHorizontalBar().removeState( "active" );
        this._scrollable.getVerticalBar().removeState( "active" );
        this._activateScrollBarsTimer.stop();
        this._deactivateScrollBarsTimer.stop();
      }
    }
  }

};
