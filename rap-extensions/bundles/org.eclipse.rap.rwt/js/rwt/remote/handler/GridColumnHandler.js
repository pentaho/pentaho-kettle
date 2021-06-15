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

rwt.remote.HandlerRegistry.add( "rwt.widgets.GridColumn", {

  factory : function( properties ) {
    var result;
    rwt.remote.HandlerUtil.callWithTarget( properties.parent, function( parent ) {
      result = new rwt.widgets.GridColumn( parent );
      rwt.remote.HandlerUtil.addDestroyableChild( parent, result );
    } );
    return result;
  },

  destructor : function( column ) {
    rwt.remote.HandlerUtil.removeDestroyableChild( column._grid, column );
    column.dispose();
  },

  properties : [
    // Always set column index first
    "index",
    "left",
    "width",
    "text",
    "image",
    "font",
    "footerText",
    "footerImage",
    "footerFont",
    "toolTipMarkupEnabled",
    "toolTip",
    "resizable",
    "moveable",
    "alignment",
    "fixed",
    "group",
    "customVariant",
    "visibility",
    "check",
    "wordWrap",
    "headerWordWrap",
    "footerSpan",
    "data"
  ],

  propertyHandler : {
    "data" : rwt.remote.HandlerUtil.getControlPropertyHandler( "data" ),
    "toolTipMarkupEnabled" : rwt.remote.HandlerUtil.getControlPropertyHandler( "toolTipMarkupEnabled" ),
    "toolTip" : rwt.remote.HandlerUtil.getControlPropertyHandler( "toolTip" ),
    "group" : function( widget, value ) {
      rwt.remote.HandlerUtil.callWithTarget( value, function( group ) {
        widget.setGroup( group );
      } );
    }
  },

  events : [ "Selection" ]

} );
