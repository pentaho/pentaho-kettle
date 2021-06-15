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

rwt.remote.HandlerRegistry.add( "rwt.widgets.List", {

  factory : function( properties ) {
    var multiSelection = properties.style.indexOf( "MULTI" ) != -1;
    var result = new rwt.widgets.List( multiSelection );
    result.setMarkupEnabled( properties.markupEnabled === true );
    rwt.remote.HandlerUtil.addStatesForStyles( result, properties.style );
    result.setUserData( "isControl", true );
    rwt.remote.HandlerUtil.setParent( result, properties.parent );
    return result;
  },

  destructor : rwt.remote.HandlerUtil.getControlDestructor(),

  getDestroyableChildren : rwt.remote.HandlerUtil.getDestroyableChildrenFinder(),

  properties : rwt.remote.HandlerUtil.extendControlProperties( [
    // order of items, selection, focus is crucial
    "items",
    "selectionIndices",
    "topIndex",
    "focusIndex",
    "itemDimensions"
  ] ),

  propertyHandler : rwt.remote.HandlerUtil.extendControlPropertyHandler( {
    "selectionIndices" : function( widget, value ) {
      if( widget.hasState( "rwt_MULTI" ) ) {
        if( widget.getItemsCount() === value.length ) {
          widget.selectAll();
        } else {
          widget.selectItems( value );
        }
      } else {
        widget.selectItem( value[ 0 ] !== undefined ? value[ 0 ] : -1 );
      }
    },
    "focusIndex" : function( widget, value ) {
      widget.focusItem( value );
    },
    "scrollBarsVisible" : function( widget, value ) {
      widget.setScrollBarsVisible( value[ 0 ], value[ 1 ] );
    },
    "itemDimensions" : function( widget, value ) {
      widget.setItemDimensions( value[ 0 ], value[ 1 ] );
    }
  } ),

  events : [ "Selection", "DefaultSelection" ],

  listeners : rwt.remote.HandlerUtil.extendControlListeners( [] ),

  listenerHandler : rwt.remote.HandlerUtil.extendControlListenerHandler( {} ),

  scriptingMethods : rwt.remote.HandlerUtil.extendControlScriptingMethods( {
    "getSelection" : function() {
      var items = this.getSelectedItems();
      var result = [];
      for( var i = 0; i < items.length; i++ ) {
        result[ i ] = rwt.util.Encoding.unescape( items[ i ].getLabel() );
      }
      return result;
    }
  } )

} );
