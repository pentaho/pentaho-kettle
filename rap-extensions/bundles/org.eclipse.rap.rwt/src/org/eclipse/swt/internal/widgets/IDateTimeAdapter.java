/*******************************************************************************
 * Copyright (c) 2008, 2010 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 *     EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public interface IDateTimeAdapter {

  // Date
  int WEEKDAY_TEXTFIELD = 0;
  int DAY_TEXTFIELD = 1;
  int MONTH_TEXTFIELD = 2;
  int YEAR_TEXTFIELD = 3;
  int WEEKDAY_MONTH_SEPARATOR = 4;
  int MONTH_DAY_SEPARATOR = 5;
  int DAY_YEAR_SEPARATOR = 6;
  int SPINNER = 7;
  // Time
  int HOURS_TEXTFIELD = 8;
  int MINUTES_TEXTFIELD = 9;
  int SECONDS_TEXTFIELD = 10;
  int HOURS_MINUTES_SEPARATOR = 11;
  int MINUTES_SECONDS_SEPARATOR = 12;
  // Date - drop down button
  int DROP_DOWN_BUTTON = 13;

  Rectangle getBounds( int widget );
  Point getCellSize();

  String[] getMonthNames();
  String[] getWeekdayNames();
  String[] getWeekdayShortNames();
  String getDateSeparator();
  String getDatePattern();
}
