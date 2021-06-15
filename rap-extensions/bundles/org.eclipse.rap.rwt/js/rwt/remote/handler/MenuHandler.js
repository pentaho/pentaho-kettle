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

rwt.remote.HandlerRegistry.add( "rwt.widgets.Menu", {

  factory : function( properties ) {
    var result;
    // TODO [tb] : split into Menu and MenuBar, or unify parent handling
    if( properties.style.indexOf( "BAR" ) != -1 ) {
      result = new rwt.widgets.MenuBar();
      rwt.remote.HandlerUtil.callWithTarget( properties.parent, function( parent ) {
        result.setParent( parent );
      } );
    } else {
      result = new rwt.widgets.Menu();
    }
    rwt.remote.HandlerUtil.addStatesForStyles( result, properties.style );
    return result;
  },

  destructor : rwt.remote.HandlerUtil.getWidgetDestructor(),

  getDestroyableChildren : rwt.remote.HandlerUtil.getDestroyableChildrenFinder(),

  properties : [
    "bounds",
    "enabled",
    "customVariant",
    "direction"
  ],

  propertyHandler : {
    "bounds" : function( widget, value ) {
      if( widget.hasState( "rwt_BAR" ) ) {
        widget.setLeft( value[ 0 ] );
        widget.setTop( value[ 1 ] );
        widget.setWidth( value[ 2 ] );
        widget.setHeight( value[ 3 ] );
      }
    }
  },

  events : [ "Show", "Hide" ],

  listeners : [ "Help" ],

  listenerHandler : {
    "Help" : rwt.remote.HandlerUtil.getControlListenerHandler( "Help" )
  },

  methods : [
    "unhideItems",
    "showMenu"
  ],

  methodHandler : {
    "unhideItems" : function( widget, args ) {
      if( !widget.hasState( "rwt_BAR" ) ) {
        widget.unhideItems( args.reveal );
      }
    },
    "showMenu" : function( widget, args ) {
      if( widget.hasState( "rwt_POP_UP" ) ) {
        widget.showMenu( widget, args.x, args.y );
      }
    }
  }

} );
