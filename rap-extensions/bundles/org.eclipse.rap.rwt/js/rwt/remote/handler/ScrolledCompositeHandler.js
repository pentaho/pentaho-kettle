/*******************************************************************************
 * Copyright (c) 2011, 2017 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.remote.HandlerRegistry.add( "rwt.widgets.ScrolledComposite", {

  factory : function( properties ) {
    var result = new rwt.widgets.ScrolledComposite();
    rwt.remote.HandlerUtil.addStatesForStyles( result, properties.style );
    result.setUserData( "isControl", true );
    rwt.remote.HandlerUtil.setParent( result, properties.parent );
    return result;
  },

  destructor : rwt.remote.HandlerUtil.getControlDestructor(),

  getDestroyableChildren : rwt.remote.HandlerUtil.getDestroyableChildrenFinder(),

  properties : rwt.remote.HandlerUtil.extendControlProperties( [
    "origin",
    "content",
    "showFocusedControl"
  ] ),

  propertyHandler : rwt.remote.HandlerUtil.extendControlPropertyHandler( {
    // Override original bounds handler to set clipWidth and clipHeight
    "bounds" : function( widget, value ) {
      rwt.remote.HandlerUtil.getControlPropertyHandler( "bounds" )( widget, value );
      widget.setClipWidth( value[ 2 ] );
      widget.setClipHeight( value[ 3 ] );
    },
    // Order is important: origin before scrollBarsVisible
    "origin" : function( widget, value ) {
      widget.setHBarSelection( value[ 0 ] );
      widget.setVBarSelection( value[ 1 ] );
    },
    "content" : function( widget, value ) {
      rwt.remote.HandlerUtil.callWithTarget( value, function( content ) {
        widget.setContent( content );
      } );
    },
    "scrollBarsVisible" : function( widget, value ) {
      widget.setScrollBarsVisible( value[ 0 ], value[ 1 ] );
    }
  } ),

  listeners : rwt.remote.HandlerUtil.extendControlListeners( [] ),

  listenerHandler : rwt.remote.HandlerUtil.extendControlListenerHandler( {} ),

  /**
   * @class RWT Scripting analoge to org.eclipse.swt.custom.ScrolledComposite
   * @name ScrolledComposite
   * @extends Control
   * @description The constructor is not public.
   * @since 3.2
   */
  scriptingMethods : rwt.remote.HandlerUtil.extendControlScriptingMethods(
    /** @lends ScrolledComposite.prototype */
  {
    /**
      * @description Sets the 'origin', which is the receiver's scroll position.
      * @return {int, int} the origin
      */
    setOrigin : function(x, y) {
      this.setHBarSelection( x );
      this.setVBarSelection( y );
    },
    /**
      * @description Returns the 'origin', which is the receiver's scroll position.
      * @return {[int, int]} the origin
      */
    getOrigin : function() {
      return [ this.getHorizontalBar().getValue(), this.getVerticalBar().getValue()];
    }
  } )

} );
