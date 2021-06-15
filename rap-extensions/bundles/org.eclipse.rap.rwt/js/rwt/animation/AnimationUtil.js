/*******************************************************************************
 * Copyright (c) 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

(function(){

var AnimationRenderer = rwt.animation.AnimationRenderer;
var Animation = rwt.animation.Animation;

rwt.animation.AnimationUtil = {

  snapTo : function( widget, time, left, top, hide ) {
    var animation = this._createAnimation( widget, time, "easeOut" );
    var rendererX = this._createRenderer( animation, widget, "left" );
    var rendererY = this._createRenderer( animation, widget, "top" );
    var startLeft = parseInt( widget.getLeft(), 10 );
    var startTop = parseInt( widget.getTop(), 10 );
    rendererX.setStartValue( isNaN( startLeft ) ? 0 : startLeft );
    rendererY.setStartValue( isNaN( startTop ) ? 0 : startTop );
    rendererX.setEndValue( left );
    rendererY.setEndValue( top );
    animation.addEventListener( "finish", function() {
      widget.setLeft( left );
      widget.setTop( top );
      if( hide ) {
        widget.setDisplay( false );
      }
    } );
    animation.start();
  },

  _createAnimation : function( widget, time, transition ) {
    var result = new Animation();
    result.setDuration( time );
    result.setTransition( transition );
    var abort = function() {
      result.cancel();
      result.dispose();
    };
    widget.addEventListener( "cancelAnimations", abort );
    widget.addEventListener( "dispose", abort );
    result.addEventListener( "cancel", function() {
      widget.removeEventListener( "dispose", abort );
      widget.removeEventListener( "cancelAnimations", abort );
      // animation may still need to dispatch "finish", dispose later
      rwt.client.Timer.once( result.dispose, result, 0 );
    } );
    return result;
  },

  _createRenderer : function( animation, widget, property ) {
    var converter = AnimationRenderer.converterByRenderType;
    var adapter = widget.getAdapter( rwt.widgets.util.WidgetRenderAdapter );
    var result = new AnimationRenderer( animation );
    result.setRenderFunction( adapter.getOriginalRenderer( property ), widget );
    result.setConverter( converter[ property ] );
    return result;
  }

};

}());
