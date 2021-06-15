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

import org.eclipse.swt.widgets.DateTime;

final class DateTimeCalendarLCA extends AbstractDateTimeLCADelegate {

  static final DateTimeCalendarLCA INSTANCE = new DateTimeCalendarLCA();

  @Override
  void preserveValues( DateTime dateTime ) {
    DateTimeLCAUtil.preserveDate( dateTime );
    DateTimeLCAUtil.preserveMinMaxLimit( dateTime );
  }

  @Override
  void renderInitialization( DateTime dateTime ) throws IOException {
    DateTimeLCAUtil.renderInitialization( dateTime );
    DateTimeLCAUtil.renderCellSize( dateTime );
    DateTimeLCAUtil.renderMonthNames( dateTime );
    DateTimeLCAUtil.renderWeekdayShortNames( dateTime );
  }

  @Override
  void renderChanges( DateTime dateTime ) throws IOException {
    DateTimeLCAUtil.renderChanges( dateTime );
    DateTimeLCAUtil.renderDate( dateTime );
    DateTimeLCAUtil.renderMinMaxLimit( dateTime );
  }

  private DateTimeCalendarLCA() {
    // prevent instantiation
  }

}
