/*******************************************************************************
 * Copyright (c) 2011, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.remote.HandlerRegistry.add( "rwt.widgets.TabFolder", {

  factory : function( properties ) {
    var result = new rwt.widgets.TabFolder();
    rwt.remote.HandlerUtil.addStatesForStyles( result, properties.style );
    result.setUserData( "isControl", true );
    rwt.remote.HandlerUtil.setParent( result, properties.parent );
    result.setHideFocus( true );
    result.setPlaceBarOnTop( properties.style.indexOf( "BOTTOM" ) === -1 );
    return result;
  },

  destructor : rwt.remote.HandlerUtil.getControlDestructor(),

  getDestroyableChildren : rwt.remote.HandlerUtil.getDestroyableChildrenFinder(),

  properties : rwt.remote.HandlerUtil.extendControlProperties( [
    "selection"
  ] ),

  propertyHandler : rwt.remote.HandlerUtil.extendControlPropertyHandler( {
    "selection" : function( widget, value ) {
      rwt.remote.HandlerUtil.callWithTarget( value, function( item ) {
        var items = widget.getBar().getChildren();
        for( var index = 0; index < items.length; index++ ) {
          if( items[ index ] === item ) {
            items[ index ].setChecked( true );
          } else if( items[ index ].getChecked() ) {
            items[ index ].setChecked( false );
          }
        }
      } );
    }
  } ),

  events : [ "Selection" ],

  listeners : rwt.remote.HandlerUtil.extendControlListeners( [] ),

  listenerHandler : rwt.remote.HandlerUtil.extendControlListenerHandler( {} )

} );
