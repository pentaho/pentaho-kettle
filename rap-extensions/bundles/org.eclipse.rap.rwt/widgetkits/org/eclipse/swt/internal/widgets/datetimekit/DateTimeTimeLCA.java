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

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.widgets.IDateTimeAdapter;
import org.eclipse.swt.internal.widgets.datetimekit.DateTimeLCAUtil.SubWidgetBounds;
import org.eclipse.swt.widgets.DateTime;

final class DateTimeTimeLCA extends AbstractDateTimeLCADelegate {

  static final DateTimeTimeLCA INSTANCE = new DateTimeTimeLCA();

  private static final String PROP_HOURS = "hours";
  private static final String PROP_MINUTES = "minutes";
  private static final String PROP_SECONDS = "seconds";

  @Override
  void preserveValues( DateTime dateTime ) {
    preserveProperty( dateTime, PROP_HOURS, dateTime.getHours() );
    preserveProperty( dateTime, PROP_MINUTES, dateTime.getMinutes() );
    preserveProperty( dateTime, PROP_SECONDS, dateTime.getSeconds() );
    DateTimeLCAUtil.preserveSubWidgetsBounds( dateTime, getSubWidgetsBounds( dateTime ) );
    DateTimeLCAUtil.preserveMinMaxLimit( dateTime );
  }

  @Override
  void renderInitialization( DateTime dateTime ) throws IOException {
    DateTimeLCAUtil.renderInitialization( dateTime );
  }

  @Override
  void renderChanges( DateTime dateTime ) throws IOException {
    DateTimeLCAUtil.renderChanges( dateTime );
    renderProperty( dateTime, PROP_HOURS, dateTime.getHours(), SWT.DEFAULT );
    renderProperty( dateTime, PROP_MINUTES, dateTime.getMinutes(), SWT.DEFAULT );
    renderProperty( dateTime, PROP_SECONDS, dateTime.getSeconds(), SWT.DEFAULT );
    DateTimeLCAUtil.renderSubWidgetsBounds( dateTime, getSubWidgetsBounds( dateTime ) );
    DateTimeLCAUtil.renderMinMaxLimit( dateTime );
  }

  ///////////////////////////////////////////////////
  // Helping methods to render the changed properties

  private static SubWidgetBounds[] getSubWidgetsBounds( DateTime dateTime ) {
    return new SubWidgetBounds[] {
      DateTimeLCAUtil.getSubWidgetBounds( dateTime, IDateTimeAdapter.HOURS_TEXTFIELD ),
      DateTimeLCAUtil.getSubWidgetBounds( dateTime, IDateTimeAdapter.HOURS_MINUTES_SEPARATOR ),
      DateTimeLCAUtil.getSubWidgetBounds( dateTime, IDateTimeAdapter.MINUTES_TEXTFIELD ),
      DateTimeLCAUtil.getSubWidgetBounds( dateTime, IDateTimeAdapter.MINUTES_SECONDS_SEPARATOR ),
      DateTimeLCAUtil.getSubWidgetBounds( dateTime, IDateTimeAdapter.SECONDS_TEXTFIELD ),
      DateTimeLCAUtil.getSubWidgetBounds( dateTime, IDateTimeAdapter.SPINNER )
    };
  }

  private DateTimeTimeLCA() {
    // prevent instantiation
  }

}
