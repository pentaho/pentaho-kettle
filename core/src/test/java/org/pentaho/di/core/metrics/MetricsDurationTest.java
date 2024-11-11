/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core.metrics;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;

public class MetricsDurationTest {

  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();

  @Test
  @SuppressWarnings( "deprecation" )
  public void test() {
    Date startDate = new Date( ( 2016 - 1900 ), Calendar.JANUARY, 10 );
    Long duration = Long.valueOf( 4L );
    MetricsDuration metric =
      new MetricsDuration( startDate, "theDesc", "theSubj", "theLogChannel", duration );

    assertEquals( "theDesc", metric.getDescription() );
    assertEquals( "theSubj", metric.getSubject() );
    assertEquals( "theLogChannel", metric.getLogChannelId() );
    assertEquals( startDate, metric.getDate() );
    assertEquals( duration, metric.getDuration() );

    assertEquals( Long.valueOf( 1L ), metric.getCount() );
    metric.incrementCount();
    assertEquals( Long.valueOf( 2L ), metric.getCount() );
    assertEquals( new Date( startDate.getTime() + duration ), metric.getEndDate() );
  }
}
