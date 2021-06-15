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

import java.io.IOException;

import org.eclipse.swt.internal.widgets.IDateTimeAdapter;
import org.eclipse.swt.internal.widgets.datetimekit.DateTimeLCAUtil.SubWidgetBounds;
import org.eclipse.swt.widgets.DateTime;


final class DateTimeDateLCA extends AbstractDateTimeLCADelegate {

  static final DateTimeDateLCA INSTANCE = new DateTimeDateLCA();

  @Override
  void preserveValues( DateTime dateTime ) {
    DateTimeLCAUtil.preserveDate( dateTime );
    DateTimeLCAUtil.preserveSubWidgetsBounds( dateTime, getSubWidgetsBounds( dateTime ) );
    DateTimeLCAUtil.preserveMinMaxLimit( dateTime );
  }

  @Override
  void renderInitialization( DateTime dateTime ) throws IOException {
    DateTimeLCAUtil.renderInitialization( dateTime );
    DateTimeLCAUtil.renderCellSize( dateTime );
    DateTimeLCAUtil.renderMonthNames( dateTime );
    DateTimeLCAUtil.renderWeekdayNames( dateTime );
    DateTimeLCAUtil.renderWeekdayShortNames( dateTime );
    DateTimeLCAUtil.renderDateSeparator( dateTime );
    DateTimeLCAUtil.renderDatePattern( dateTime );
  }

  @Override
  void renderChanges( DateTime dateTime ) throws IOException {
    DateTimeLCAUtil.renderChanges( dateTime );
    DateTimeLCAUtil.renderDate( dateTime );
    DateTimeLCAUtil.renderSubWidgetsBounds( dateTime, getSubWidgetsBounds( dateTime ) );
    DateTimeLCAUtil.renderMinMaxLimit( dateTime );
  }

  ///////////////////////////////////////////////////
  // Helping methods to render the changed properties

  private static SubWidgetBounds[] getSubWidgetsBounds( DateTime dateTime ) {
    return new SubWidgetBounds[] {
      DateTimeLCAUtil.getSubWidgetBounds( dateTime, IDateTimeAdapter.WEEKDAY_TEXTFIELD ),
      DateTimeLCAUtil.getSubWidgetBounds( dateTime, IDateTimeAdapter.WEEKDAY_MONTH_SEPARATOR ),
      DateTimeLCAUtil.getSubWidgetBounds( dateTime, IDateTimeAdapter.MONTH_TEXTFIELD ),
      DateTimeLCAUtil.getSubWidgetBounds( dateTime, IDateTimeAdapter.MONTH_DAY_SEPARATOR ),
      DateTimeLCAUtil.getSubWidgetBounds( dateTime, IDateTimeAdapter.DAY_TEXTFIELD ),
      DateTimeLCAUtil.getSubWidgetBounds( dateTime, IDateTimeAdapter.DAY_YEAR_SEPARATOR ),
      DateTimeLCAUtil.getSubWidgetBounds( dateTime, IDateTimeAdapter.YEAR_TEXTFIELD ),
      DateTimeLCAUtil.getSubWidgetBounds( dateTime, IDateTimeAdapter.SPINNER ),
      DateTimeLCAUtil.getSubWidgetBounds( dateTime, IDateTimeAdapter.DROP_DOWN_BUTTON )
    };
  }

  private DateTimeDateLCA() {
    // prevent instantiation
  }

}
