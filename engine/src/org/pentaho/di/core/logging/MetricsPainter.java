package org.pentaho.di.core.logging;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.gui.GCInterface;
import org.pentaho.di.core.gui.GCInterface.EColor;
import org.pentaho.di.core.gui.GCInterface.EFont;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.Rectangle;
import org.pentaho.di.core.metrics.MetricsDuration;

public class MetricsPainter {
  private GCInterface gc;
  
  private Long periodStart=null;
  private Long periodEnd=null;
  
  public class MetricsDrawArea {
    private Rectangle area;
    private MetricsDuration duration;

    public MetricsDrawArea(Rectangle area, MetricsDuration duration) {
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

  public MetricsPainter(GCInterface gc) {
    this.gc = gc;   
  }
  
  public List<MetricsDrawArea> paint(List<MetricsDuration> durations) {
    int width = gc.getArea().x-4;
    int height = gc.getArea().y-4;
    
    List<MetricsDrawArea> areas = new ArrayList<MetricsDrawArea>();
    // First determine the period
    //
    determinePeriod(durations);
    if (periodStart==null || periodEnd==null || periodEnd<=periodStart) {
      return areas; // nothing to do;
    }
    double pixelsPerMs = (double)width/((double)(periodEnd-periodStart));
    int barHeight = ((height-10)/durations.size())-2;
    
    
    // Draw some lines in the background.
    //
    long periodInMs = periodEnd - periodStart;
    int log10 = (int)Math.log10(periodInMs);
    
    // 150 --> 2
    // 1243 --> 3
    // 
    
    int timeLineDistance = (int)Math.pow(10, log10-1);
    
    for (int time=timeLineDistance;time<periodInMs;time+=timeLineDistance) {
      int x = (int)(time * pixelsPerMs);
      gc.setForeground(EColor.LIGHTGRAY);
      gc.drawLine(x, 0, x, height);
      String marker=Integer.toString(time);
      Point point = gc.textExtent(marker);
      gc.setForeground(EColor.DARKGRAY);
      gc.drawText(marker, x-(point.x/2), 0, true);
    }
    
    
    int y=14;
    
    // Draw the durations...
    //
    for (int i=0;i<durations.size();i++) {
      MetricsDuration duration = durations.get(i);
      
      Long realDuration = duration.getEndDate().getTime() - duration.getDate().getTime();
      
      // How many pixels does it take to drawn this duration?
      //
      int durationWidth = (int)(realDuration*pixelsPerMs);
      int x = 2+  (int)((duration.getDate().getTime()-periodStart)*pixelsPerMs);
      
      gc.setForeground(EColor.BLACK);
      gc.setBackground(EColor.LIGHTGRAY);
      gc.fillRectangle(x, y, durationWidth, barHeight);
      gc.drawRectangle(x, y, durationWidth, barHeight);
      areas.add(new MetricsDrawArea(new Rectangle(x, y, durationWidth, barHeight), duration));
      
      LoggingObjectInterface loggingObject = LoggingRegistry.getInstance().getLoggingObject(duration.getLogChannelId());
      
      String message = duration.getDescription()+" - "+loggingObject.getObjectName()+" : "+duration.getDuration()+"ms";
      if (duration.getCount()>1) {
        message+=" "+duration.getCount()+" calls, avg="+(duration.getDuration()/duration.getCount());
      }
      
      gc.setFont(EFont.GRAPH);
      gc.textExtent(message);
      gc.drawText(message, x+4, y+4, true);
      
      y+=barHeight+2;
    }
    return areas;
  }

  private void determinePeriod(List<MetricsDuration> durations) {
    periodStart=null;
    periodEnd=null;
    
    for (int i=0;i<durations.size();i++) {
      MetricsDuration duration = durations.get(i);
      long periodStartTime = duration.getDate().getTime();
      if (periodStart==null || periodStartTime<periodStart) {
        periodStart = periodStartTime;
      }
      long periodEndTime = duration.getEndDate().getTime();
      if (periodEnd==null || periodEnd<periodEndTime) {
        periodEnd = periodEndTime;
      }
    }
  }
}
