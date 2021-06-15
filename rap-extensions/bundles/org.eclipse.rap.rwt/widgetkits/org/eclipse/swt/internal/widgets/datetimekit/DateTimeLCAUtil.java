/*******************************************************************************
 * Copyright (c) 2008, 2017 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.datetimekit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.hasChanged;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenDefaultSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;

import java.util.Date;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.IDateTimeAdapter;
import org.eclipse.swt.widgets.DateTime;


final class DateTimeLCAUtil {

  private static final String TYPE = "rwt.widgets.DateTime";
  private static final String[] ALLOWED_STYLES = {
    "DATE", "TIME", "CALENDAR", "SHORT", "MEDIUM", "LONG", "DROP_DOWN", "BORDER"
  };

  private static final String PROP_CELL_SIZE = "cellSize";
  private static final String PROP_MONTH_NAMES = "monthNames";
  private static final String PROP_WEEKDAY_NAMES = "weekdayNames";
  private static final String PROP_WEEKDAY_SHORT_NAMES = "weekdayShortNames";
  private static final String PROP_DATE_SEPARATOR = "dateSeparator";
  private static final String PROP_DATE_PATTERN = "datePattern";
  private static final String PROP_SUB_WIDGETS_BOUNDS = "subWidgetsBounds";
  private static final String PROP_MINIMUM = "minimum";
  private static final String PROP_MAXIMUM = "maximum";
  private static final String PROP_DATE = "date";

  private DateTimeLCAUtil() {
    // prevent instantiation
  }

  static void renderInitialization( DateTime dateTime ) {
    RemoteObject remoteObject = createRemoteObject( dateTime, TYPE );
    remoteObject.setHandler( new DateTimeOperationHandler( dateTime ) );
    remoteObject.set( "parent", getId( dateTime.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( dateTime, ALLOWED_STYLES ) ) );
  }

  static void renderChanges( DateTime dateTime ) {
    ControlLCAUtil.renderChanges( dateTime );
    WidgetLCAUtil.renderCustomVariant( dateTime );
    renderListenSelection( dateTime );
    renderListenDefaultSelection( dateTime );
  }

  static void renderCellSize( DateTime dateTime ) {
    Point cellSize = getDateTimeAdapter( dateTime ).getCellSize();
    RemoteObject remoteObject = getRemoteObject( dateTime );
    remoteObject.set( PROP_CELL_SIZE, new JsonArray().add( cellSize.x ).add( cellSize.y ) );
  }

  static void renderMonthNames( DateTime dateTime ) {
    String[] monthNames = getDateTimeAdapter( dateTime ).getMonthNames();
    RemoteObject remoteObject = getRemoteObject( dateTime );
    remoteObject.set( PROP_MONTH_NAMES, createJsonArray( monthNames ) );
  }

  static void renderWeekdayNames( DateTime dateTime ) {
    String[] weekdayNames = getDateTimeAdapter( dateTime ).getWeekdayNames();
    RemoteObject remoteObject = getRemoteObject( dateTime );
    remoteObject.set( PROP_WEEKDAY_NAMES, createJsonArray( weekdayNames ) );
  }

  static void renderWeekdayShortNames( DateTime dateTime ) {
    String[] weekdayShortNames = getDateTimeAdapter( dateTime ).getWeekdayShortNames();
    RemoteObject remoteObject = getRemoteObject( dateTime );
    remoteObject.set( PROP_WEEKDAY_SHORT_NAMES, createJsonArray( weekdayShortNames ) );
  }

  static void renderDateSeparator( DateTime dateTime ) {
    String dateSeparator = getDateTimeAdapter( dateTime ).getDateSeparator();
    getRemoteObject( dateTime ).set( PROP_DATE_SEPARATOR, dateSeparator );
  }

  static void renderDatePattern( DateTime dateTime ) {
    String datePattern = getDateTimeAdapter( dateTime ).getDatePattern();
    getRemoteObject( dateTime ).set( PROP_DATE_PATTERN, datePattern );
  }

  static void preserveDate( DateTime dateTime ) {
    int[] date = { dateTime.getYear(), dateTime.getMonth(), dateTime.getDay() };
    preserveProperty( dateTime, PROP_DATE, date );
  }

  static void renderDate( DateTime dateTime ) {
    int[] date = { dateTime.getYear(), dateTime.getMonth(), dateTime.getDay() };
    renderProperty( dateTime, PROP_DATE, date, new int[ 0 ] );
  }

  static void preserveMinMaxLimit( DateTime dateTime ) {
    preserveProperty( dateTime, PROP_MINIMUM, getMinLimit( dateTime ) );
    preserveProperty( dateTime, PROP_MAXIMUM, getMaxLimit( dateTime ) );
  }

  static void renderMinMaxLimit( DateTime dateTime ) {
    RemoteObject remoteObject = getRemoteObject( dateTime );
    Long minimum = getMinLimit( dateTime );
    if( hasChanged( dateTime, PROP_MINIMUM, minimum, null ) ) {
      JsonValue value = minimum == null ? JsonValue.NULL : JsonValue.valueOf( minimum.longValue() );
      remoteObject.set( PROP_MINIMUM, value );
    }
    Long maximum = getMaxLimit( dateTime );
    if( hasChanged( dateTime, PROP_MAXIMUM, maximum, null ) ) {
      JsonValue value = maximum == null ? JsonValue.NULL : JsonValue.valueOf( maximum.longValue() );
      remoteObject.set( PROP_MAXIMUM, value );
    }
  }

  static void preserveSubWidgetsBounds( DateTime dateTime, SubWidgetBounds[] subWidgetBounds ) {
    preserveProperty( dateTime, PROP_SUB_WIDGETS_BOUNDS, subWidgetBounds );
  }

  static void renderSubWidgetsBounds( DateTime dateTime, SubWidgetBounds[] subWidgetBounds ) {
    if( hasChanged( dateTime, PROP_SUB_WIDGETS_BOUNDS, subWidgetBounds ) ) {
      JsonArray bounds = new JsonArray();
      for( int i = 0; i < subWidgetBounds.length; i++ ) {
        bounds.add( new JsonArray().add( subWidgetBounds[ i ].id )
                                   .add( subWidgetBounds[ i ].x )
                                   .add( subWidgetBounds[ i ].y )
                                   .add( subWidgetBounds[ i ].width )
                                   .add( subWidgetBounds[ i ].height ) );
      }
      getRemoteObject( dateTime ).set( PROP_SUB_WIDGETS_BOUNDS, bounds );
    }
  }

  static SubWidgetBounds getSubWidgetBounds( DateTime dateTime, int subWidgetId ) {
    Rectangle bounds = getDateTimeAdapter( dateTime ).getBounds( subWidgetId );
    return new SubWidgetBounds( subWidgetId, bounds );
  }

  private static IDateTimeAdapter getDateTimeAdapter( DateTime dateTime ) {
    return dateTime.getAdapter( IDateTimeAdapter.class );
  }

  private static Long getMinLimit( DateTime dateTime ) {
    Date minimum = dateTime.getMinimum();
    return minimum == null ? null : Long.valueOf( minimum.getTime() );
  }

  private static Long getMaxLimit( DateTime dateTime ) {
    Date maximum = dateTime.getMaximum();
    return maximum == null ? null : Long.valueOf( maximum.getTime() );
  }

  static final class SubWidgetBounds {
    public final int id;
    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public SubWidgetBounds( int id, Rectangle bounds ) {
      ParamCheck.notNull( bounds, "subWidgetBounds" );
      this.id = id;
      x = bounds.x;
      y = bounds.y;
      width = bounds.width;
      height = bounds.height;
    }

    @Override
    public boolean equals( Object obj ) {
      boolean result;
      if( obj == this ) {
        result = true;
      } else  if( obj instanceof SubWidgetBounds ) {
        SubWidgetBounds other = ( SubWidgetBounds )obj;
        result =  other.id == id
               && other.x == x
               && other.y == y
               && other.width == width
               && other.height == height;
      } else {
        result = false;
      }
      return result;
    }

    @Override
    public int hashCode() {
      String msg = "SubWidgetBounds#hashCode() not implemented";
      throw new UnsupportedOperationException( msg );
    }

  }

}
