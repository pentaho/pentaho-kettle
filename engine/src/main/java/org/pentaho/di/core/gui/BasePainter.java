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

package org.pentaho.di.core.gui;

import org.pentaho.di.base.BaseHopMeta;
import org.pentaho.di.base.BaseMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.gui.AreaOwner.AreaType;
import org.pentaho.di.core.gui.PrimitiveGCInterface.EColor;
import org.pentaho.di.core.gui.PrimitiveGCInterface.EImage;
import org.pentaho.di.core.gui.PrimitiveGCInterface.ELineStyle;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.trans.step.errorhandling.StreamIcon;

import java.util.List;

public abstract class BasePainter<Hop extends BaseHopMeta<?>, Part extends BaseMeta> {

  public final double theta = Math.toRadians( 11 ); // arrowhead sharpness

  public static final int MINI_ICON_SIZE = 16;
  public static final int MINI_ICON_MARGIN = 5;
  public static final int MINI_ICON_TRIANGLE_BASE = 20;
  public static final int MINI_ICON_DISTANCE = 4;
  public static final int MINI_ICON_SKEW = 0;

  public static final int CONTENT_MENU_INDENT = 4;

  public static final int CORNER_RADIUS_5 = 10;
  public static final int CORNER_RADIUS_4 = 8;
  public static final int CORNER_RADIUS_3 = 6;
  public static final int CORNER_RADIUS_2 = 4;

  public static final float FACTOR_1_TO_1 = 1.0f;

  protected Point area;

  protected ScrollBarInterface hori, vert;

  protected List<AreaOwner> areaOwners;

  protected Point offset;
  protected Point drop_candidate;
  protected int iconsize;
  protected int gridSize;
  protected Rectangle selrect;
  protected int linewidth;
  protected float magnification;
  protected float translationX;
  protected float translationY;
  protected boolean shadow;

  protected Object subject;

  protected GCInterface gc;

  protected int shadowSize;

  private String noteFontName;

  private int noteFontHeight;

  protected Hop candidate;

  public BasePainter( GCInterface gc, Object subject, Point area, ScrollBarInterface hori,
    ScrollBarInterface vert, Point drop_candidate, Rectangle selrect, List<AreaOwner> areaOwners, int iconsize,
    int linewidth, int gridsize, int shadowSize, boolean antiAliasing, String noteFontName, int noteFontHeight ) {
    this.gc = gc;
    this.subject = subject;
    this.area = area;
    this.hori = hori;
    this.vert = vert;

    this.selrect = selrect;
    this.drop_candidate = drop_candidate;

    this.areaOwners = areaOwners;
    areaOwners.clear(); // clear it before we start filling it up again.

    // props = PropsUI.getInstance();
    this.iconsize = iconsize;
    this.linewidth = linewidth;
    this.gridSize = gridsize;

    this.shadowSize = shadowSize;
    this.shadow = shadowSize > 0;

    this.magnification = 1.0f;

    gc.setAntialias( antiAliasing );

    this.noteFontName = noteFontName;
    this.noteFontHeight = noteFontHeight;
  }

  public static EImage getStreamIconImage( StreamIcon streamIcon ) {
    switch ( streamIcon ) {
      case TRUE:
        return EImage.TRUE;
      case FALSE:
        return EImage.FALSE;
      case ERROR:
        return EImage.ERROR;
      case INFO:
        return EImage.INFO;
      case TARGET:
        return EImage.TARGET;
      case INPUT:
        return EImage.INPUT;
      case OUTPUT:
        return EImage.OUTPUT;
      default:
        return EImage.ARROW;
    }
  }

  protected void drawNote( NotePadMeta notePadMeta ) {
    if ( notePadMeta.isSelected() ) {
      gc.setLineWidth( 2 );
    } else {
      gc.setLineWidth( 1 );
    }

    Point ext;
    if ( Utils.isEmpty( notePadMeta.getNote() ) ) {
      ext = new Point( 10, 10 ); // Empty note
    } else {

      gc.setFont( Const.NVL( notePadMeta.getFontName(), noteFontName ), notePadMeta.getFontSize() == -1
        ? noteFontHeight : notePadMeta.getFontSize(), notePadMeta.isFontBold(), notePadMeta.isFontItalic() );

      ext = gc.textExtent( notePadMeta.getNote() );
    }
    Point p = new Point( ext.x, ext.y );
    Point loc = notePadMeta.getLocation();
    Point note = real2screen( loc.x, loc.y );
    int margin = Const.NOTE_MARGIN;
    p.x += 2 * margin;
    p.y += 2 * margin;
    int width = notePadMeta.width;
    int height = notePadMeta.height;
    if ( p.x > width ) {
      width = p.x;
    }
    if ( p.y > height ) {
      height = p.y;
    }

    int[] noteshape = new int[] {
      note.x, note.y, // Top left
      note.x + width + 2 * margin, note.y, // Top right
      note.x + width + 2 * margin, note.y + height, // bottom right 1
      note.x + width, note.y + height + 2 * margin, // bottom right 2
      note.x + width, note.y + height, // bottom right 3
      note.x + width + 2 * margin, note.y + height, // bottom right 1
      note.x + width, note.y + height + 2 * margin, // bottom right 2
      note.x, note.y + height + 2 * margin // bottom left
    };

    // Draw shadow around note?
    if ( notePadMeta.isDrawShadow() ) {
      int s = shadowSize;
      int[] shadowa = new int[] {
        note.x + s, note.y + s, // Top left
        note.x + width + 2 * margin + s, note.y + s, // Top right
        note.x + width + 2 * margin + s, note.y + height + s, // bottom right 1
        note.x + width + s, note.y + height + 2 * margin + s, // bottom right 2
        note.x + s, note.y + height + 2 * margin + s // bottom left
      };
      gc.setBackground( EColor.LIGHTGRAY );
      gc.fillPolygon( shadowa );
    }
    gc.setBackground( notePadMeta.getBackGroundColorRed(), notePadMeta.getBackGroundColorGreen(), notePadMeta
      .getBackGroundColorBlue() );
    gc.setForeground( notePadMeta.getBorderColorRed(), notePadMeta.getBorderColorGreen(), notePadMeta
      .getBorderColorBlue() );

    gc.fillPolygon( noteshape );
    gc.drawPolygon( noteshape );

    if ( !Utils.isEmpty( notePadMeta.getNote() ) ) {
      gc.setForeground( notePadMeta.getFontColorRed(), notePadMeta.getFontColorGreen(), notePadMeta
        .getFontColorBlue() );
      gc.drawText( notePadMeta.getNote(), note.x + margin, note.y + margin, true );
    }

    notePadMeta.width = width; // Save for the "mouse" later on...
    notePadMeta.height = height;

    if ( notePadMeta.isSelected() ) {
      gc.setLineWidth( 1 );
    } else {
      gc.setLineWidth( 2 );
    }

    // Add to the list of areas...
    //
    if ( !shadow ) {
      areaOwners.add( new AreaOwner( AreaType.NOTE, note.x, note.y, width, height, offset, subject, notePadMeta ) );
    }
  }

  protected int translateTo1To1( int value ) {
    return Math.round( value / magnification );
  }

  protected int translateToCurrentScale( int value ) {
    return Math.round( value * magnification );
  }

  protected Point real2screen( int x, int y ) {
    Point screen = new Point( x + offset.x, y + offset.y );

    return screen;
  }

  protected Point getThumb( Point area, Point transMax ) {
    Point resizedMax = magnifyPoint( transMax );

    Point thumb = new Point( 0, 0 );
    if ( resizedMax.x <= area.x ) {
      thumb.x = 100;
    } else {
      thumb.x = (int) Math.floor( 100d * area.x / resizedMax.x );
    }

    if ( resizedMax.y <= area.y ) {
      thumb.y = 100;
    } else {
      thumb.y = (int) Math.floor( 100d * area.y / resizedMax.y );
    }

    return thumb;
  }

  protected Point magnifyPoint( Point p ) {
    return new Point( Math.round( p.x * magnification ), Math.round( p.y * magnification ) );
  }

  protected Point getOffset( Point thumb, Point area ) {
    Point p = new Point( 0, 0 );

    if ( hori == null || vert == null ) {
      return p;
    }

    Point sel = new Point( hori.getSelection(), vert.getSelection() );

    if ( thumb.x == 0 || thumb.y == 0 ) {
      return p;
    }

    p.x = Math.round( -sel.x * area.x / thumb.x / magnification );
    p.y = Math.round( -sel.y * area.y / thumb.y / magnification );

    return p;
  }

  protected void drawRect( Rectangle rect ) {
    if ( rect == null ) {
      return;
    }
    gc.setLineStyle( ELineStyle.DASHDOT );
    gc.setLineWidth( linewidth );
    gc.setForeground( EColor.GRAY );
    // PDI-2619: SWT on Windows doesn't cater for negative rect.width/height so handle here.
    Point s = real2screen( rect.x, rect.y );
    if ( rect.width < 0 ) {
      s.x = s.x + rect.width;
    }
    if ( rect.height < 0 ) {
      s.y = s.y + rect.height;
    }
    gc.drawRectangle( s.x, s.y, Math.abs( rect.width ), Math.abs( rect.height ) );
    gc.setLineStyle( ELineStyle.SOLID );
  }

  protected void drawGrid() {
    Point bounds = gc.getDeviceBounds();
    for ( int x = 0; x < bounds.x; x += gridSize ) {
      for ( int y = 0; y < bounds.y; y += gridSize ) {
        gc.drawPoint( x + ( offset.x % gridSize ), y + ( offset.y % gridSize ) );
      }
    }
  }

  protected int calcArrowLength() {
    return 19 + ( linewidth - 1 ) * 5; // arrowhead length;
  }

  /**
   * @return the magnification
   */
  public float getMagnification() {
    return magnification;
  }

  /**
   * @param magnification
   *          the magnification to set
   */
  public void setMagnification( float magnification ) {
    this.magnification = magnification;
  }

  public Point getArea() {
    return area;
  }

  public void setArea( Point area ) {
    this.area = area;
  }

  public ScrollBarInterface getHori() {
    return hori;
  }

  public void setHori( ScrollBarInterface hori ) {
    this.hori = hori;
  }

  public ScrollBarInterface getVert() {
    return vert;
  }

  public void setVert( ScrollBarInterface vert ) {
    this.vert = vert;
  }

  public List<AreaOwner> getAreaOwners() {
    return areaOwners;
  }

  public void setAreaOwners( List<AreaOwner> areaOwners ) {
    this.areaOwners = areaOwners;
  }

  public Point getOffset() {
    return offset;
  }

  public void setOffset( Point offset ) {
    this.offset = offset;
  }

  public Point getDrop_candidate() {
    return drop_candidate;
  }

  public void setDrop_candidate( Point drop_candidate ) {
    this.drop_candidate = drop_candidate;
  }

  public int getIconsize() {
    return iconsize;
  }

  public void setIconsize( int iconsize ) {
    this.iconsize = iconsize;
  }

  public int getGridSize() {
    return gridSize;
  }

  public void setGridSize( int gridSize ) {
    this.gridSize = gridSize;
  }

  public Rectangle getSelrect() {
    return selrect;
  }

  public void setSelrect( Rectangle selrect ) {
    this.selrect = selrect;
  }

  public int getLinewidth() {
    return linewidth;
  }

  public void setLinewidth( int linewidth ) {
    this.linewidth = linewidth;
  }

  public float getTranslationX() {
    return translationX;
  }

  public void setTranslationX( float translationX ) {
    this.translationX = translationX;
  }

  public float getTranslationY() {
    return translationY;
  }

  public void setTranslationY( float translationY ) {
    this.translationY = translationY;
  }

  public boolean isShadow() {
    return shadow;
  }

  public void setShadow( boolean shadow ) {
    this.shadow = shadow;
  }

  public Object getSubject() {
    return subject;
  }

  public void setSubject( Object subject ) {
    this.subject = subject;
  }

  public GCInterface getGc() {
    return gc;
  }

  public void setGc( GCInterface gc ) {
    this.gc = gc;
  }

  public int getShadowSize() {
    return shadowSize;
  }

  public void setShadowSize( int shadowSize ) {
    this.shadowSize = shadowSize;
  }

  public String getNoteFontName() {
    return noteFontName;
  }

  public void setNoteFontName( String noteFontName ) {
    this.noteFontName = noteFontName;
  }

  public int getNoteFontHeight() {
    return noteFontHeight;
  }

  public void setNoteFontHeight( int noteFontHeight ) {
    this.noteFontHeight = noteFontHeight;
  }

  public double getTheta() {
    return theta;
  }


  public Hop getCandidate() {
    return candidate;
  }

  public void setCandidate( Hop candidate ) {
    this.candidate = candidate;
  }

  protected int[] getLine( Part fs, Part ts ) {
    if ( fs == null || ts == null ) {
      return null;
    }

    Point from = fs.getLocation();
    Point to = ts.getLocation();

    int x1 = from.x + iconsize / 2;
    int y1 = from.y + iconsize / 2;

    int x2 = to.x + iconsize / 2;
    int y2 = to.y + iconsize / 2;

    return new int[] { x1, y1, x2, y2 };
  }

  protected void drawArrow( EImage arrow, int[] line, Hop hop, Object startObject, Object endObject ) {
    Point screen_from = real2screen( line[0], line[1] );
    Point screen_to = real2screen( line[2], line[3] );

    drawArrow( arrow, screen_from.x, screen_from.y, screen_to.x, screen_to.y, theta, calcArrowLength(), -1, hop,
      startObject, endObject );
  }

  protected abstract void drawArrow( EImage arrow, int x1, int y1, int x2, int y2, double theta, int size, double factor,
                            Hop jobHop, Object startObject, Object endObject );
}
