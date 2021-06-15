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

rwt.remote.HandlerRegistry.add( "rwt.widgets.DateTime", {

  factory : function( properties ) {
    var result;
    var styleMap = rwt.remote.HandlerUtil.createStyleMap( properties.style );
    var styles;
    if( styleMap.SHORT ) {
      styles = "short";
    } else if( styleMap.LONG ) {
      styles = "long";
    } else {
      styles = "medium";
    }
    if( styleMap.DROP_DOWN ) {
      styles += "|drop_down";
    }
    if( styleMap.TIME ) {
      result = new rwt.widgets.DateTimeTime( styles );
    } else if( styleMap.CALENDAR ) {
      rwt.widgets.base.Calendar.CELL_WIDTH = properties.cellSize[ 0 ];
      rwt.widgets.base.Calendar.CELL_HEIGHT = properties.cellSize[ 1 ];
      result = new rwt.widgets.DateTimeCalendar( styles,
                                                 properties.monthNames,
                                                 properties.weekdayShortNames );
    } else {
      rwt.widgets.base.Calendar.CELL_WIDTH = properties.cellSize[ 0 ];
      rwt.widgets.base.Calendar.CELL_HEIGHT = properties.cellSize[ 1 ];
      result = new rwt.widgets.DateTimeDate( styles,
                                             properties.monthNames,
                                             properties.weekdayNames,
                                             properties.weekdayShortNames,
                                             properties.dateSeparator,
                                             properties.datePattern );
    }
    rwt.remote.HandlerUtil.addStatesForStyles( result, properties.style );
    result.setUserData( "isControl", true );
    rwt.remote.HandlerUtil.setParent( result, properties.parent );
    return result;
  },

  destructor : rwt.remote.HandlerUtil.getControlDestructor(),

  getDestroyableChildren : rwt.remote.HandlerUtil.getDestroyableChildrenFinder(),

  properties : rwt.remote.HandlerUtil.extendControlProperties( [
    "date",
    "hours",
    "minutes",
    "seconds",
    "subWidgetsBounds",
    "minimum",
    "maximum"
  ] ),

  propertyHandler : rwt.remote.HandlerUtil.extendControlPropertyHandler( {
    "date" : function( widget, value ) {
      widget.setDate.apply( widget, value );
    },
    "subWidgetsBounds" : function( widget, value ) {
      for( var i = 0; i < value.length; i++ ) {
        widget.setBounds.apply( widget, value[ i ] );
      }
    }
  } ),

  events : [ "Selection", "DefaultSelection" ],

  listeners : rwt.remote.HandlerUtil.extendControlListeners( [] ),

  listenerHandler : rwt.remote.HandlerUtil.extendControlListenerHandler( {} )

} );
