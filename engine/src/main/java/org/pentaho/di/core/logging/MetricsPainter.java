/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.logging;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.gui.GCInterface;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.PrimitiveGCInterface.EColor;
import org.pentaho.di.core.gui.PrimitiveGCInterface.EFont;
import org.pentaho.di.core.gui.Rectangle;
import org.pentaho.di.core.metrics.MetricsDuration;

public class MetricsPainter {
  private GCInterface gc;

  private Long periodStart = null;
  private Long periodEnd = null;

  private int barHeight;

  public class MetricsDrawArea {
    private Rectangle area;
    private MetricsDuration duration;

    public MetricsDrawArea( Rectangle area, MetricsDuration duration ) {
      this.area = area;
      this.duration = duration;
    }

    public MetricsDuration getDuration() {
      return duration;
    }

    public Rectangle getArea() {
      return area;
    }
  }

  public MetricsPainter( GCInterface gc, int barHeight ) {
    this.setGc( gc );
    this.barHeight = barHeight;
  }

  /**
   * Draws a metrics tab.
   *
   * @param durations
   *          is a list of metrics durations
   * @return list of drawing areas.Throw IllegalArgumentException in case if input parameter is null or an empty
   */
  public List<MetricsDrawArea> paint( List<MetricsDuration> durations ) {
    if ( Utils.isEmpty( durations ) ) {
      throw new IllegalArgumentException();
    }

    int width = getGc().getArea().x - 4;
    int height = getGc().getArea().y - 4;

    List<MetricsDrawArea> areas = new ArrayList<MetricsDrawArea>();
    // First determine the period
    //
    determinePeriod( durations );
    if ( periodStart == null || periodEnd == null || periodEnd <= periodStart ) {
      return areas; // nothing to do;
    }
    double pixelsPerMs = (double) width / ( (double) ( periodEnd - periodStart ) );
    long periodInMs = periodEnd - periodStart;

    drawTimeScaleLine( height, pixelsPerMs, periodInMs );

    drawDurations( durations, areas, pixelsPerMs );

    return areas;
  }

  void drawTimeScaleLine( int height, double pixelsPerMs, long periodInMs ) {
    int log10 = (int) Math.log10( periodInMs ) + 1;
    int timeLineDistance = (int) Math.pow( 10, log10 - 1 ) / 2;
    int incrementUnit = Math.max( timeLineDistance, 1 );

    for ( int time = timeLineDistance; time <= periodInMs; time += incrementUnit ) {
      int x = (int) ( time * pixelsPerMs );
      getGc().setForeground( EColor.LIGHTGRAY );
      getGc().drawLine( x, 0, x, height );
      String marker = Integer.toString( time );
      Point point = getGc().textExtent( marker );
      getGc().setForeground( EColor.DARKGRAY );
      getGc().drawText( marker, x - ( point.x / 2 ), 0, true );
    }
  }

  private void drawDurations( List<MetricsDuration> durations, List<MetricsDrawArea> areas, double pixelsPerMs ) {
    // set top indent
    int y = 20;

    for ( MetricsDuration duration : durations ) {
      Long realDuration = duration.getEndDate().getTime() - duration.getDate().getTime();

      // How many pixels does it take to drawn this duration?
      //
      int durationWidth = (int) ( realDuration * pixelsPerMs );
      int x = 2 + (int) ( ( duration.getDate().getTime() - periodStart ) * pixelsPerMs );

      getGc().setBackground( EColor.BACKGROUND );
      getGc().setForeground( EColor.LIGHTBLUE );
      getGc().fillGradientRectangle( x, y, durationWidth, barHeight, false );
      getGc().setForeground( EColor.BLACK );
      getGc().drawRectangle( x, y, durationWidth, barHeight );
      areas.add( new MetricsDrawArea( new Rectangle( x, y, durationWidth, barHeight ), duration ) );

      LoggingObjectInterface loggingObject =
          LoggingRegistry.getInstance().getLoggingObject( duration.getLogChannelId() );

      String message =
          duration.getDescription() + " - " + loggingObject.getObjectName() + " : " + duration.getDuration() + "ms";
      if ( duration.getCount() > 1 ) {
        message += " " + duration.getCount() + " calls, avg=" + ( duration.getDuration() / duration.getCount() );
      }

      getGc().setFont( EFont.GRAPH );
      getGc().textExtent( message );
      getGc().drawText( message, x + 3, y + 4, true );

      y += barHeight + 5;
    }
  }

  private void determinePeriod( List<MetricsDuration> durations ) {
    periodStart = null;
    periodEnd = null;

    for ( MetricsDuration duration : durations ) {
      long periodStartTime = duration.getDate().getTime();
      if ( periodStart == null || periodStartTime < periodStart ) {
        periodStart = periodStartTime;
      }
      long periodEndTime = duration.getEndDate().getTime();
      if ( periodEnd == null || periodEnd < periodEndTime ) {
        periodEnd = periodEndTime;
      }
    }
  }

  // Method is defined as package-protected in order to be accessible by unit tests
  GCInterface getGc() {
    return gc;
  }

  // Method is defined as package-protected in order to be accessible by unit tests
  void setGc( GCInterface gc ) {
    this.gc = gc;
  }
}
