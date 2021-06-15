/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.datetimekit;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_DEFAULT_SELECTION;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_SELECTION;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.ControlOperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Event;


public class DateTimeOperationHandler extends ControlOperationHandler<DateTime> {

  private static final String PROP_YEAR = "year";
  private static final String PROP_MONTH = "month";
  private static final String PROP_DAY = "day";
  private static final String PROP_HOURS = "hours";
  private static final String PROP_MINUTES = "minutes";
  private static final String PROP_SECONDS = "seconds";

  public DateTimeOperationHandler( DateTime dateTime ) {
    super( dateTime );
  }

  @Override
  public void handleSet( DateTime dateTime, JsonObject properties ) {
    super.handleSet( dateTime, properties );
    handleSetDate( dateTime, properties );
    handleSetTime( dateTime, properties );
  }

  @Override
  public void handleNotify( DateTime dateTime, String eventName, JsonObject properties ) {
    if( EVENT_SELECTION.equals( eventName ) ) {
      handleNotifySelection( dateTime, properties );
    } else if( EVENT_DEFAULT_SELECTION.equals( eventName ) ) {
      handleNotifyDefaultSelection( dateTime, properties );
    } else {
      super.handleNotify( dateTime, eventName, properties );
    }
  }

  /*
   * PROTOCOL SET date
   *
   * @param year (int) value in range ( 1752 - 9999 )
   * @param month (int) value in range ( 0 - 11 )
   * @param day (int) value in range ( 1 - 31 )
   */
  public void handleSetDate( DateTime dateTime, JsonObject properties ) {
    JsonValue yearValue = properties.get( PROP_YEAR );
    JsonValue monthValue = properties.get( PROP_MONTH );
    JsonValue dayValue = properties.get( PROP_DAY );
    if( yearValue != null && monthValue != null && dayValue != null ) {
      dateTime.setDate( yearValue.asInt(), monthValue.asInt(), dayValue.asInt() );
    }
  }

  /*
   * PROTOCOL SET time
   *
   * @param hours (int) value in range ( 0 - 23 )
   * @param minutes (int) value in range ( 0 - 59 )
   * @param seconds (int) value in range ( 0 - 59 )
   */
  public void handleSetTime( DateTime dateTime, JsonObject properties ) {
    JsonValue hoursValue = properties.get( PROP_HOURS );
    JsonValue minutesValue = properties.get( PROP_MINUTES );
    JsonValue secondsValue = properties.get( PROP_SECONDS );
    if( hoursValue != null && minutesValue != null && secondsValue != null ) {
      dateTime.setTime( hoursValue.asInt(), minutesValue.asInt(), secondsValue.asInt() );
    }
  }

  /*
   * PROTOCOL NOTIFY Selection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   */
  public void handleNotifySelection( DateTime dateTime, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.Selection, properties );
    dateTime.notifyListeners( SWT.Selection, event );
  }

  /*
   * PROTOCOL NOTIFY DefaultSelection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   */
  public void handleNotifyDefaultSelection( DateTime dateTime, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.DefaultSelection, properties );
    dateTime.notifyListeners( SWT.DefaultSelection, event );
  }

}
