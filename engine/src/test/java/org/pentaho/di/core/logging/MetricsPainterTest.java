/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.core.logging;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.gui.GCInterface;
import org.pentaho.di.core.metrics.MetricsDuration;
import org.pentaho.di.core.gui.Point;

public class MetricsPainterTest {
  MetricsPainter metricsPainter;
  List<MetricsDuration> durations = new ArrayList<>();
  final int heightStub = 0;
  final double pixelsPerMsStub = 0;
  final long periodInMsStub = 0;

  @Before
  public void initialize() {
    metricsPainter = mock( MetricsPainter.class );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testIllegalArgumentExceptionNullArgPaint() {
    callPaint( durations );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testIllegalArgumentExceptionEmptyArgPaint() {
    durations = new ArrayList<MetricsDuration>();
    callPaint( durations );
  }

  @Test( timeout = 1000 )
  public void testDrawTimeScaleLineInfinityLoop() {
    GCInterface gCInterfaceMock = mock( GCInterface.class );
    when( metricsPainter.getGc() ).thenReturn( gCInterfaceMock );
    doCallRealMethod().when( metricsPainter ).drawTimeScaleLine( heightStub, pixelsPerMsStub, periodInMsStub );
    when( gCInterfaceMock.textExtent( anyString() ) ).thenReturn( mock( Point.class ) );

    metricsPainter.drawTimeScaleLine( heightStub, pixelsPerMsStub, periodInMsStub );
  }

  private void callPaint( List<MetricsDuration> durations ) {
    doCallRealMethod().when( metricsPainter ).paint( anyList() );
    metricsPainter.paint( durations );

  }

}
