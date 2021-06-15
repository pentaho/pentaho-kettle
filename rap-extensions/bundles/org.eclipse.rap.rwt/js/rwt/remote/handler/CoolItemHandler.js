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

rwt.remote.HandlerRegistry.add( "rwt.widgets.CoolItem", {

  factory : function( properties ) {
    var styles = rwt.remote.HandlerUtil.createStyleMap( properties.style );
    var orientation = styles.VERTICAL ? "vertical" : "horizontal";
    var result = new rwt.widgets.CoolItem( orientation );
    rwt.remote.HandlerUtil.setParent( result, properties.parent );
    result.setMinWidth( 0 );
    result.setMinHeight( 0 );
    return result;
  },

  destructor : rwt.remote.HandlerUtil.getWidgetDestructor(),

  properties : [ "bounds", "control", "customVariant", "data" ],

  propertyHandler : {
    "data" : rwt.remote.HandlerUtil.getControlPropertyHandler( "data" ),
    "bounds" : function( widget, bounds ) {
      widget.setLeft( bounds[ 0 ] );
      widget.setTop( bounds[ 1 ] );
      widget.setWidth( bounds[ 2 ] );
      widget.setHeight( bounds[ 3 ] );
      widget.updateHandleBounds();
    },
    "control" : function( widget, controlId ) {
      rwt.remote.HandlerUtil.callWithTarget( controlId, function( control ) {
        widget.setControl( control );
      } );
    }
  }

} );
