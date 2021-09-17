/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.gui.AreaOwner;
import org.pentaho.di.core.gui.AreaOwner.AreaType;
import org.pentaho.di.core.gui.BasePainter;
import org.pentaho.di.core.gui.GCInterface;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.PrimitiveGCInterface.EColor;
import org.pentaho.di.core.gui.PrimitiveGCInterface.EFont;
import org.pentaho.di.core.gui.PrimitiveGCInterface.EImage;
import org.pentaho.di.core.gui.PrimitiveGCInterface.ELineStyle;
import org.pentaho.di.core.gui.Rectangle;
import org.pentaho.di.core.gui.ScrollBarInterface;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.job.entry.JobEntryCopy;

public class JobPainter extends BasePainter<JobHopMeta, JobEntryCopy> {

  private JobMeta jobMeta;

  private List<JobEntryCopy> mouseOverEntries;
  private Map<JobEntryCopy, String> entryLogMap;
  private JobEntryCopy startHopEntry;
  private Point endHopLocation;
  private JobEntryCopy endHopEntry;
  private JobEntryCopy noInputEntry;
  private List<JobEntryCopy> activeJobEntries;
  private List<JobEntryResult> jobEntryResults;

  public JobPainter( GCInterface gc, JobMeta jobMeta, Point area, ScrollBarInterface hori,
    ScrollBarInterface vert, JobHopMeta candidate, Point drop_candidate, Rectangle selrect,
    List<AreaOwner> areaOwners, List<JobEntryCopy> mouseOverEntries, int iconsize, int linewidth, int gridsize,
    int shadowSize, boolean antiAliasing, String noteFontName, int noteFontHeight ) {
    super(
      gc, jobMeta, area, hori, vert, drop_candidate, selrect, areaOwners, iconsize, linewidth, gridsize,
      shadowSize, antiAliasing, noteFontName, noteFontHeight );
    this.jobMeta = jobMeta;

    this.candidate = candidate;

    this.mouseOverEntries = mouseOverEntries;

    entryLogMap = null;
  }

  public void drawJob() {

    Point max = jobMeta.getMaximum();
    Point thumb = getThumb( area, max );
    offset = getOffset( thumb, area );

    // Make sure the canvas is scaled 100%
    gc.setTransform( 0, 0, 0, 1.0f );
    // First clear the image in the background color
    gc.setBackground( EColor.BACKGROUND );
    gc.fillRectangle( 0, 0, area.x, area.y );

    if ( hori != null ) {
      hori.setThumb( thumb.x );
    }
    if ( vert != null ) {
      vert.setThumb( thumb.y );
    }

    // If there is a shadow, we draw the transformation first with an alpha
    // setting
    //
    if ( shadowSize > 0 ) {
      gc.setAlpha( 20 );
      gc.setTransform( translationX, translationY, shadowSize, magnification );
      shadow = true;
      drawJobElements();
    }

    // Draw the transformation onto the image
    //
    gc.setAlpha( 255 );
    gc.setTransform( translationX, translationY, 0, magnification );

    shadow = false;
    drawJobElements();

    gc.dispose();

  }

  private void drawJobElements() {
    try {
      ExtensionPointHandler.callExtensionPoint( LogChannel.GENERAL, KettleExtensionPoint.JobPainterStart.id, this );
    } catch ( KettleException e ) {
      LogChannel.GENERAL.logError( "Error in JobPainterStart extension point", e );
    }

    // First draw the notes...
    gc.setFont( EFont.NOTE );

    for ( int i = 0; i < jobMeta.nrNotes(); i++ ) {
      NotePadMeta ni = jobMeta.getNote( i );
      drawNote( ni );
    }

    gc.setFont( EFont.GRAPH );

    // ... and then the rest on top of it...
    for ( int i = 0; i < jobMeta.nrJobHops(); i++ ) {
      JobHopMeta hi = jobMeta.getJobHop( i );
      drawJobHop( hi, false );
    }

    EImage arrow;
    if ( candidate != null ) {
      drawJobHop( candidate, true );
    } else {
      if ( startHopEntry != null && endHopLocation != null ) {
        Point fr = startHopEntry.getLocation();
        Point to = endHopLocation;
        if ( endHopEntry == null ) {
          gc.setForeground( EColor.GRAY );
          arrow = EImage.ARROW_DISABLED;
        } else {
          gc.setForeground( EColor.BLUE );
          arrow = EImage.ARROW_DEFAULT;
        }
        Point start = real2screen( fr.x + iconsize / 2, fr.y + iconsize / 2 );
        Point end = real2screen( to.x, to.y );
        drawArrow( arrow, start.x, start.y, end.x, end.y, theta, calcArrowLength(), 1.2, null, startHopEntry,
            endHopEntry == null ? endHopLocation : endHopEntry );
      } else if ( endHopEntry != null && endHopLocation != null ) {
        Point fr = endHopLocation;
        Point to = endHopEntry.getLocation();
        if ( startHopEntry == null ) {
          gc.setForeground( EColor.GRAY );
          arrow = EImage.ARROW_DISABLED;
        } else {
          gc.setForeground( EColor.BLUE );
          arrow = EImage.ARROW_DEFAULT;
        }
        Point start = real2screen( fr.x, fr.y );
        Point end = real2screen( to.x + iconsize / 2, to.y + iconsize / 2 );
        drawArrow( arrow, start.x, start.y, end.x, end.y + iconsize / 2, theta, calcArrowLength(), 1.2, null,
            startHopEntry == null ? endHopLocation : startHopEntry, endHopEntry );
      }
    }

    for ( int j = 0; j < jobMeta.nrJobEntries(); j++ ) {
      JobEntryCopy je = jobMeta.getJobEntry( j );
      drawJobEntryCopy( je );
    }

    // Display an icon on the indicated location signaling to the user that the step in question does not accept input
    //
    if ( noInputEntry != null ) {
      gc.setLineWidth( 2 );
      gc.setForeground( EColor.RED );
      Point n = noInputEntry.getLocation();
      gc.drawLine( offset.x + n.x - 5, offset.y + n.y - 5, offset.x + n.x + iconsize + 5, offset.y
        + n.y + iconsize + 5 );
      gc.drawLine( offset.x + n.x - 5, offset.y + n.y + iconsize + 5, offset.x + n.x + iconsize + 5, offset.y
        + n.y - 5 );
    }

    if ( drop_candidate != null ) {
      gc.setLineStyle( ELineStyle.SOLID );
      gc.setForeground( EColor.BLACK );
      Point screen = real2screen( drop_candidate.x, drop_candidate.y );
      gc.drawRectangle( screen.x, screen.y, iconsize, iconsize );
    }

    try {
      ExtensionPointHandler.callExtensionPoint( LogChannel.GENERAL, KettleExtensionPoint.JobPainterEnd.id, this );
    } catch ( KettleException e ) {
      LogChannel.GENERAL.logError( "Error in JobPainterEnd extension point", e );
    }

    if ( !shadow ) {
      drawRect( selrect );
    }
  }

  protected void drawJobEntryCopy( JobEntryCopy jobEntryCopy ) {
    if ( !jobEntryCopy.isDrawn() ) {
      return;
    }

    int alpha = gc.getAlpha();

    Point pt = jobEntryCopy.getLocation();
    if ( pt == null ) {
      pt = new Point( 50, 50 );
    }

    Point screen = real2screen( pt.x, pt.y );
    int x = screen.x;
    int y = screen.y;

    String name = jobEntryCopy.getName();
    if ( jobEntryCopy.isSelected() ) {
      gc.setLineWidth( 3 );
    } else {
      gc.setLineWidth( 1 );
    }

    gc.setBackground( EColor.BACKGROUND );
    gc.fillRoundRectangle( x - 1, y - 1, iconsize + 1, iconsize + 1, 7, 7 );
    gc.drawJobEntryIcon( x, y, jobEntryCopy, magnification );

    if ( !shadow ) {
      areaOwners
          .add( new AreaOwner( AreaType.JOB_ENTRY_ICON, x, y, iconsize, iconsize, offset, subject, jobEntryCopy ) );
    }

    if ( jobEntryCopy.isMissing() ) {
      gc.setForeground( EColor.RED );
    } else if ( jobEntryCopy.isDeprecated() ) {
      gc.setForeground( EColor.DEPRECATED );
    } else {
      gc.setForeground( EColor.CRYSTAL );
    }
    gc.drawRoundRectangle( x - 1, y - 1, iconsize + 1, iconsize + 1, 7, 7 );
    gc.setForeground( EColor.CRYSTAL );
    Point textsize = new Point( gc.textExtent( "" + name ).x, gc.textExtent( "" + name ).y );

    gc.setBackground( EColor.BACKGROUND );
    gc.setLineWidth( 1 );

    int xpos = x + ( iconsize / 2 ) - ( textsize.x / 2 );
    int ypos = y + iconsize + 5;

    gc.setForeground( EColor.BLACK );
    gc.drawText( name, xpos, ypos, true );

    if ( activeJobEntries != null && activeJobEntries.contains( jobEntryCopy ) ) {
      gc.setForeground( EColor.BLUE );
      int iconX = ( x + iconsize ) - ( MINI_ICON_SIZE / 2 );
      int iconY = y - ( MINI_ICON_SIZE / 2 );
      gc.drawImage( EImage.BUSY, iconX, iconY, magnification );
      areaOwners.add( new AreaOwner( AreaType.JOB_ENTRY_BUSY, iconX, iconY, MINI_ICON_SIZE, MINI_ICON_SIZE, offset, subject, jobEntryCopy ) );
    } else {
      gc.setForeground( EColor.BLACK );
    }

    JobEntryResult jobEntryResult = findJobEntryResult( jobEntryCopy );
    if ( jobEntryResult != null ) {
      Result result = jobEntryResult.getResult();
      int iconX = ( x + iconsize ) - ( MINI_ICON_SIZE / 2 );
      int iconY = y - ( MINI_ICON_SIZE / 2 );

      // Draw an execution result on the top right corner...
      //
      if ( jobEntryResult.isCheckpoint() ) {
        gc.drawImage( EImage.CHECKPOINT, iconX, iconY, magnification );
        areaOwners.add( new AreaOwner( AreaType.JOB_ENTRY_RESULT_CHECKPOINT, iconX, iconY, MINI_ICON_SIZE,
            MINI_ICON_SIZE, offset, jobEntryCopy, jobEntryResult ) );
      } else {
        if ( result.getResult() ) {
          gc.drawImage( EImage.TRUE, iconX, iconY, magnification );
          areaOwners.add( new AreaOwner( AreaType.JOB_ENTRY_RESULT_SUCCESS, iconX, iconY, MINI_ICON_SIZE,
              MINI_ICON_SIZE, offset, jobEntryCopy, jobEntryResult ) );
        } else {
          gc.drawImage( EImage.FALSE, iconX, iconY, magnification );
          areaOwners.add( new AreaOwner( AreaType.JOB_ENTRY_RESULT_FAILURE, iconX, iconY, MINI_ICON_SIZE,
              MINI_ICON_SIZE, offset, jobEntryCopy, jobEntryResult ) );
        }
      }
    }

    // Optionally drawn the mouse-over information
    //
    if ( mouseOverEntries.contains( jobEntryCopy ) && !jobEntryCopy.isDeprecated() ) {
      gc.setTransform( translationX, translationY, 0, BasePainter.FACTOR_1_TO_1 );

      EImage[] miniIcons = new EImage[] { EImage.INPUT, EImage.EDIT, EImage.CONTEXT_MENU, EImage.OUTPUT, };

      // First drawn the mini-icons balloon below the job entry
      //
      int totalHeight = 0;
      int totalIconsWidth = 0;
      int totalWidth = 2 * MINI_ICON_MARGIN;
      for ( EImage miniIcon : miniIcons ) {
        Point bounds = gc.getImageBounds( miniIcon );
        totalWidth += bounds.x + MINI_ICON_MARGIN;
        totalIconsWidth += bounds.x + MINI_ICON_MARGIN;
        if ( bounds.y > totalHeight ) {
          totalHeight = bounds.y;
        }
      }
      totalHeight += 2 * MINI_ICON_MARGIN;

      gc.setFont( EFont.SMALL );
      String trimmedName =
          jobEntryCopy.getName().length() < 30 ? jobEntryCopy.getName() : jobEntryCopy.getName().substring( 0, 30 );
      Point nameExtent = gc.textExtent( trimmedName );
      nameExtent.y += 2 * MINI_ICON_MARGIN;
      nameExtent.x += 3 * MINI_ICON_MARGIN;
      totalHeight += nameExtent.y;
      if ( nameExtent.x > totalWidth ) {
        totalWidth = nameExtent.x;
      }

      int areaX =
          translateToCurrentScale( x ) + translateToCurrentScale( iconsize ) / 2 - totalWidth / 2 + MINI_ICON_SKEW;
      int areaY =
          translateToCurrentScale( y ) + translateToCurrentScale( iconsize ) + MINI_ICON_DISTANCE
              + BasePainter.CONTENT_MENU_INDENT;

      gc.setForeground( EColor.CRYSTAL );
      gc.setBackground( EColor.CRYSTAL );
      gc.setLineWidth( 1 );
      gc.fillRoundRectangle( areaX, areaY, totalWidth, totalHeight, BasePainter.CORNER_RADIUS_5, BasePainter.CORNER_RADIUS_5 );

      gc.setBackground( EColor.WHITE );

      gc.fillRoundRectangle( areaX, areaY + nameExtent.y, totalWidth, ( totalHeight - nameExtent.y ),
          BasePainter.CORNER_RADIUS_5,
          BasePainter.CORNER_RADIUS_5 );
      gc.fillRectangle( areaX, areaY + nameExtent.y, totalWidth, ( totalHeight - nameExtent.y ) / 2 );

      gc.drawRoundRectangle( areaX, areaY, totalWidth, totalHeight, BasePainter.CORNER_RADIUS_5, BasePainter.CORNER_RADIUS_5 );

      gc.setForeground( EColor.WHITE );

      gc.drawText( trimmedName, areaX + ( totalWidth - nameExtent.x ) / 2 + MINI_ICON_MARGIN, areaY
        + MINI_ICON_MARGIN, true );
      gc.setForeground( EColor.CRYSTAL );
      gc.setBackground( EColor.CRYSTAL );

      gc.setFont( EFont.GRAPH );
      areaOwners.add( new AreaOwner(
        AreaType.MINI_ICONS_BALLOON, translateTo1To1( areaX ), translateTo1To1( areaY ), translateTo1To1( totalWidth ), translateTo1To1( totalHeight ), offset, jobMeta, jobEntryCopy ) );

      gc.fillPolygon( new int[] {
        areaX + totalWidth / 2 - MINI_ICON_TRIANGLE_BASE / 2 + 1, areaY + 2,
        areaX + totalWidth / 2 + MINI_ICON_TRIANGLE_BASE / 2, areaY + 2,
        areaX + totalWidth / 2 - MINI_ICON_SKEW, areaY - MINI_ICON_DISTANCE - 3, } );
      gc.setBackground( EColor.WHITE );

      // Put on the icons...
      //
      int xIcon = areaX + ( totalWidth - totalIconsWidth ) / 2 + MINI_ICON_MARGIN;
      int yIcon = areaY + 5 + nameExtent.y;

      for ( int i = 0; i < miniIcons.length; i++ ) {
        EImage miniIcon = miniIcons[i];
        Point bounds = gc.getImageBounds( miniIcon );
        boolean enabled = false;
        switch ( i ) {
          case 0: // INPUT
            enabled = !jobEntryCopy.isStart();
            areaOwners.add( new AreaOwner( AreaType.JOB_ENTRY_MINI_ICON_INPUT, translateTo1To1( xIcon ),
                translateTo1To1( yIcon ), translateTo1To1( bounds.x ), translateTo1To1( bounds.y ), offset, jobMeta,
                jobEntryCopy ) );
            break;
          case 1: // EDIT
            enabled = true;
            areaOwners.add( new AreaOwner( AreaType.JOB_ENTRY_MINI_ICON_EDIT, translateTo1To1( xIcon ),
                translateTo1To1( yIcon ), translateTo1To1( bounds.x ), translateTo1To1( bounds.y ), offset, jobMeta,
                jobEntryCopy ) );
            break;
          case 2: // Job entry context menu
            enabled = true;
            areaOwners.add( new AreaOwner( AreaType.JOB_ENTRY_MINI_ICON_CONTEXT, translateTo1To1( xIcon ),
                translateTo1To1( yIcon ), translateTo1To1( bounds.x ), translateTo1To1( bounds.y ), offset, jobMeta,
                jobEntryCopy ) );
            break;
          case 3: // OUTPUT
            enabled = true;
            areaOwners.add( new AreaOwner( AreaType.JOB_ENTRY_MINI_ICON_OUTPUT, translateTo1To1( xIcon ),
                translateTo1To1( yIcon ), translateTo1To1( bounds.x ), translateTo1To1( bounds.y ), offset, jobMeta,
                jobEntryCopy ) );
            break;
          default:
            break;
        }

        if ( enabled ) {
          gc.setAlpha( 255 );
        } else {
          gc.setAlpha( 100 );
        }
        gc.drawImage( miniIcon, xIcon, yIcon, BasePainter.FACTOR_1_TO_1 );
        xIcon += bounds.x + 5;
      }
      gc.setTransform( translationX, translationY, 0, magnification );
    }

    // Restore the previous alpha value
    //
    gc.setAlpha( alpha );
  }

  private JobEntryResult findJobEntryResult( JobEntryCopy jobEntryCopy ) {
    if ( jobEntryResults == null ) {
      return null;
    }

    Iterator<JobEntryResult> iterator = jobEntryResults.iterator();
    while ( iterator.hasNext() ) {
      JobEntryResult jobEntryResult = iterator.next();

      if ( jobEntryResult.getJobEntryName().equals( jobEntryCopy.getName() )
        && jobEntryResult.getJobEntryNr() == jobEntryCopy.getNr() ) {
        return jobEntryResult;
      }
    }

    return null;
  }

  protected void drawJobHop( JobHopMeta hop, boolean candidate ) {
    if ( hop == null || hop.getFromEntry() == null || hop.getToEntry() == null ) {
      return;
    }
    if ( !hop.getFromEntry().isDrawn() || !hop.getToEntry().isDrawn() ) {
      return;
    }

    drawLine( hop, candidate );
  }

  /**
   * Calculates line coordinates from center to center.
   */
  protected void drawLine( JobHopMeta jobHop, boolean is_candidate ) {
    int[] line = getLine( jobHop.getFromEntry(), jobHop.getToEntry() );

    gc.setLineWidth( linewidth );
    EColor col;

    if ( jobHop.getFromEntry().isLaunchingInParallel() ) {
      gc.setLineStyle( ELineStyle.PARALLEL );
    } else {
      gc.setLineStyle( ELineStyle.SOLID );
    }

    EImage arrow;
    if ( is_candidate ) {
      col = EColor.BLUE;
      arrow = EImage.ARROW_CANDIDATE;
    } else if ( jobHop.isEnabled() ) {
      if ( jobHop.isUnconditional() ) {
        col = EColor.HOP_DEFAULT;
        arrow = EImage.ARROW_DEFAULT;
      } else {
        if ( jobHop.getEvaluation() ) {
          col = EColor.HOP_OK;
          arrow = EImage.ARROW_OK;
        } else {
          col = EColor.RED;
          arrow = EImage.ARROW_ERROR;
          gc.setLineStyle( ELineStyle.DASH );
        }
      }
    } else {
      col = EColor.GRAY;
      arrow = EImage.ARROW_DISABLED;
    }

    gc.setForeground( col );

    if ( jobHop.isSplit() ) {
      gc.setLineWidth( linewidth + 2 );
    }
    drawArrow( arrow, line, jobHop );
    if ( jobHop.isSplit() ) {
      gc.setLineWidth( linewidth );
    }

    gc.setForeground( EColor.BLACK );
    gc.setBackground( EColor.BACKGROUND );
    gc.setLineStyle( ELineStyle.SOLID );
  }

  private void drawArrow( EImage arrow, int[] line, JobHopMeta jobHop ) {
    drawArrow( arrow, line, jobHop, jobHop.getFromEntry(), jobHop.getToEntry() );
  }

  @Override
  protected void drawArrow( EImage arrow, int x1, int y1, int x2, int y2, double theta, int size, double factor,
      JobHopMeta jobHop, Object startObject, Object endObject ) {
    int mx, my;
    int a, b, dist;
    double angle;

    // gc.setLineWidth(1);
    // WuLine(gc, black, x1, y1, x2, y2);

    gc.drawLine( x1, y1, x2, y2 );

    // What's the distance between the 2 points?
    a = Math.abs( x2 - x1 );
    b = Math.abs( y2 - y1 );
    dist = (int) Math.sqrt( a * a + b * b );

    // determine factor (position of arrow to left side or right side
    // 0-->100%)
    if ( factor < 0 ) {
      if ( dist >= 2 * iconsize ) {
        factor = 1.3;
      } else {
        factor = 1.2;
      }
    }

    // in between 2 points
    mx = (int) ( x1 + factor * ( x2 - x1 ) / 2 );
    my = (int) ( y1 + factor * ( y2 - y1 ) / 2 );

    // calculate points for arrowhead
    angle = Math.atan2( y2 - y1, x2 - x1 ) + ( Math.PI / 2 );

    boolean q1 = Math.toDegrees( angle ) >= 0 && Math.toDegrees( angle ) <= 90;
    boolean q2 = Math.toDegrees( angle ) > 90 && Math.toDegrees( angle ) <= 180;
    boolean q3 = Math.toDegrees( angle ) > 180 && Math.toDegrees( angle ) <= 270;
    boolean q4 = Math.toDegrees( angle ) > 270 || Math.toDegrees( angle ) < 0;

    if ( q1 || q3 ) {
      gc.drawImage( arrow, mx + 1, my, magnification, angle );
    } else if ( q2 || q4 ) {
      gc.drawImage( arrow, mx, my, magnification, angle );
    }
    // Display an icon above the hop...
    //
    factor = 0.8;

    // in between 2 points
    mx = (int) ( x1 + factor * ( x2 - x1 ) / 2 ) - 8;
    my = (int) ( y1 + factor * ( y2 - y1 ) / 2 ) - 8;

    if ( jobHop != null ) {
      EImage hopsIcon;
      if ( jobHop.isUnconditional() ) {
        hopsIcon = EImage.UNCONDITIONAL;
      } else {
        if ( jobHop.getEvaluation() ) {
          hopsIcon = EImage.TRUE;
        } else {
          hopsIcon = EImage.FALSE;
        }
      }

      Point bounds = gc.getImageBounds( hopsIcon );
      gc.drawImage( hopsIcon, mx, my, magnification );
      if ( !shadow ) {
        areaOwners
          .add( new AreaOwner( AreaType.JOB_HOP_ICON, mx, my, bounds.x, bounds.y, offset, subject, jobHop ) );
      }

      if ( jobHop.getFromEntry().isLaunchingInParallel() ) {

        factor = 1;

        // in between 2 points
        mx = (int) ( x1 + factor * ( x2 - x1 ) / 2 ) - 8;
        my = (int) ( y1 + factor * ( y2 - y1 ) / 2 ) - 8;

        hopsIcon = EImage.PARALLEL;
        gc.drawImage( hopsIcon, mx, my, magnification );
        if ( !shadow ) {
          areaOwners.add( new AreaOwner(
            AreaType.JOB_HOP_PARALLEL_ICON, mx, my, bounds.x, bounds.y, offset, subject, jobHop ) );
        }
      }

      JobPainterExtension extension =
        new JobPainterExtension( gc, shadow, areaOwners, jobMeta, jobHop, x1, y1, x2, y2, mx, my, offset );
      try {
        ExtensionPointHandler.callExtensionPoint(
          LogChannel.GENERAL, KettleExtensionPoint.JobPainterArrow.id, extension );
      } catch ( Exception e ) {
        LogChannel.GENERAL.logError( "Error calling extension point(s) for the job painter arrow", e );
      }
    }
  }

  /**
   * @return the mouseOverEntries
   */
  public List<JobEntryCopy> getMouseOverEntries() {
    return mouseOverEntries;
  }

  /**
   * @param mouseOverEntries
   *          the mouseOverEntries to set
   */
  public void setMouseOverEntries( List<JobEntryCopy> mouseOverEntries ) {
    this.mouseOverEntries = mouseOverEntries;
  }

  /**
   * @return the entryLogMap
   */
  public Map<JobEntryCopy, String> getEntryLogMap() {
    return entryLogMap;
  }

  /**
   * @param entryLogMap
   *          the entryLogMap to set
   */
  public void setEntryLogMap( Map<JobEntryCopy, String> entryLogMap ) {
    this.entryLogMap = entryLogMap;
  }

  public void setStartHopEntry( JobEntryCopy startHopEntry ) {
    this.startHopEntry = startHopEntry;
  }

  public void setEndHopLocation( Point endHopLocation ) {
    this.endHopLocation = endHopLocation;
  }

  public void setEndHopEntry( JobEntryCopy endHopEntry ) {
    this.endHopEntry = endHopEntry;
  }

  public void setNoInputEntry( JobEntryCopy noInputEntry ) {
    this.noInputEntry = noInputEntry;
  }

  public void setActiveJobEntries( List<JobEntryCopy> activeJobEntries ) {
    this.activeJobEntries = activeJobEntries;
  }

  /**
   * @return the jobEntryResults
   */
  public List<JobEntryResult> getJobEntryResults() {
    return jobEntryResults;
  }

  /**
   * @param jobEntryResults
   *          Sets AND sorts the job entry results by name and number
   */
  public void setJobEntryResults( List<JobEntryResult> jobEntryResults ) {
    this.jobEntryResults = jobEntryResults;
    Collections.sort( this.jobEntryResults );
  }

  public JobMeta getJobMeta() {
    return jobMeta;
  }

  public void setJobMeta( JobMeta jobMeta ) {
    this.jobMeta = jobMeta;
  }

  public JobEntryCopy getStartHopEntry() {
    return startHopEntry;
  }

  public Point getEndHopLocation() {
    return endHopLocation;
  }

  public JobEntryCopy getEndHopEntry() {
    return endHopEntry;
  }

  public JobEntryCopy getNoInputEntry() {
    return noInputEntry;
  }

  public List<JobEntryCopy> getActiveJobEntries() {
    return activeJobEntries;
  }

}
