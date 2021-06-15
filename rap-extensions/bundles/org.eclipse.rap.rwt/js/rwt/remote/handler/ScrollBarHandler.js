/*******************************************************************************
 * Copyright (c) 2011, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.remote.HandlerRegistry.add( "rwt.widgets.ScrollBar", {

  factory : function( properties ) {
    // NOTE : In this case the parent HAS to be already created by the protocol
    var styleMap = rwt.remote.HandlerUtil.createStyleMap( properties.style );
    var parent = rwt.remote.ObjectRegistry.getObject( properties.parent );
    var result;
    if( styleMap.HORIZONTAL ) {
      if( parent.getHorizontalBar ) {
        result = parent.getHorizontalBar();
      } else {
        result = new rwt.widgets.base.ScrollBar( true );
        rwt.remote.HandlerUtil.setParent( result, properties.parent );
      }
    } else {
      if( parent.getVerticalBar ) {
        result = parent.getVerticalBar();
      } else {
        result = new rwt.widgets.base.ScrollBar( false );
        rwt.remote.HandlerUtil.setParent( result, properties.parent );
      }
    }
    rwt.remote.HandlerUtil.addDestroyableChild( parent, result );
    result.setUserData( "protocolParent", parent );
    return result;
  },

  destructor : function( widget ) {
    var parent = widget.getUserData( "protocolParent" );
    if( parent ) {
      rwt.remote.HandlerUtil.removeDestroyableChild( parent, widget );
    }
  },

  properties : [
    "visibility"
  ],

  propertyHandler : {
    "visibility" : function( widget, value ) {
      var parent = widget.getParent();
      // NOTE : use parent.setScrollBarsVisible because "visibility" or "display" my be used
      // TODO [tb] : always use display
      if( parent.setScrollBarsVisible ) {
        if( widget.isHorizontal() ) {
          parent.setScrollBarsVisible( value, parent.isVerticalBarVisible() );
        } else {
          parent.setScrollBarsVisible( parent.isHorizontalBarVisible(), value );
        }
      } else {
        widget.setDisplay( value );
      }
    }
  },

  listeners : rwt.remote.HandlerUtil.extendControlListeners( [
    "Selection"
  ] ),

  listenerHandler : {},

  methods : []

} );
