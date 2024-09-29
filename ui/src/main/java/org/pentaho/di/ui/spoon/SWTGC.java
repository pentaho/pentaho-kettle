/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SwtUniversalImage;
import org.pentaho.di.core.gui.GCInterface;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.util.ImageUtil;
import org.pentaho.di.ui.util.SwtSvgImageUtil;

public class SWTGC implements GCInterface {

  protected Color background;

  protected Color black;
  protected Color white;
  protected Color red;
  protected Color yellow;
  protected Color orange;
  protected Color green;
  protected Color blue;
  protected Color magenta;
  protected Color gray;
  protected Color lightGray;
  protected Color darkGray;
  protected Color lightBlue;
  protected Color crystal;
  protected Color hopDefault;
  protected Color hopOK;
  protected Color deprecated;

  private GC gc;

  private int iconsize;

  //TODO should be changed to PropsUI usage
  private int small_icon_size = ConstUI.SMALL_ICON_SIZE;

  private Map<String, SwtUniversalImage> images;

  private float currentMagnification = 1.0f;

  private List<Color> colors;
  private List<Font> fonts;

  private Image image;

  private Point area;
  private Transform transform;

  public SWTGC( Device device, Point area, int iconsize ) {
    this.image = new Image( device, area.x, area.y );
    GC gc = new GC( image );
    init( gc, area, iconsize );
  }
  public SWTGC( GC gc, Point area, int iconsize ) {
    init( gc, area, iconsize );
  }
  private void init( GC gc, Point area, int iconsize ) {
    this.gc = gc;
    this.images = GUIResource.getInstance().getImagesSteps();
    this.iconsize = iconsize;
    this.area = area;

    this.colors = new ArrayList<Color>();
    this.fonts = new ArrayList<Font>();

    this.background = GUIResource.getInstance().getColorGraph();
    this.black = GUIResource.getInstance().getColorBlack();
    this.white = GUIResource.getInstance().getColorWhite();
    this.red = GUIResource.getInstance().getColorRed();
    this.yellow = GUIResource.getInstance().getColorYellow();
    this.orange = GUIResource.getInstance().getColorOrange();
    this.green = GUIResource.getInstance().getColorGreen();
    this.blue = GUIResource.getInstance().getColorBlue();
    this.magenta = GUIResource.getInstance().getColorMagenta();
    this.gray = GUIResource.getInstance().getColorGray();
    this.lightGray = GUIResource.getInstance().getColorLightGray();
    this.darkGray = GUIResource.getInstance().getColorDarkGray();
    this.lightBlue = GUIResource.getInstance().getColorLightBlue();
    this.crystal = GUIResource.getInstance().getColorCrystalTextPentaho();
    this.hopDefault = GUIResource.getInstance().getColorHopDefault();
    this.hopOK = GUIResource.getInstance().getColorHopOK();
    this.deprecated = GUIResource.getInstance().getColorDeprecated();
  }

  public void dispose() {
    if ( !Const.isRunningOnWebspoonMode() ) {
      gc.dispose();
    }
    if ( transform != null && transform.isDisposed() == false ) {
      transform.dispose();
    }
    for ( Color color : colors ) {
      color.dispose();
    }
    for ( Font font : fonts ) {
      font.dispose();
    }
  }

  public void drawLine( int x, int y, int x2, int y2 ) {
    gc.drawLine( x, y, x2, y2 );
  }

  public void drawImage( String location, ClassLoader classLoader, int x, int y ) {
    Image img = SwtSvgImageUtil.getImage( PropsUI.getDisplay(), classLoader, location,
      Math.round( small_icon_size * currentMagnification ),
      Math.round( small_icon_size * currentMagnification ) );
    if ( img != null ) {
      Rectangle bounds = img.getBounds();
      gc.drawImage( img, 0, 0, bounds.width, bounds.height, x, y, small_icon_size, small_icon_size );
    }
  }

  @Override
  public void drawImage( EImage image, int x, int y ) {
    drawImage( image, x, y, currentMagnification );
  }

  public void drawImage( EImage image, int x, int y, float magnification ) {
    Image img = getNativeImage( image ).getAsBitmapForSize( gc.getDevice(), Math.round( small_icon_size * magnification ),
        Math.round( small_icon_size * magnification ) );
    if ( img != null ) {
      Rectangle bounds = img.getBounds();
      gc.drawImage( img, 0, 0, bounds.width, bounds.height, x, y, small_icon_size, small_icon_size );
    }
  }

  public void drawImage( EImage image, int x, int y, int width, int height, float magnification ) {
    Image img = getNativeImage( image ).getAsBitmapForSize( gc.getDevice(), Math.round( width * magnification ),
        Math.round( height * magnification ) );
    if ( img != null ) {
      Rectangle bounds = img.getBounds();
      gc.drawImage( img, 0, 0, bounds.width, bounds.height, x, y, width, height );
    }
  }

  public void drawImage( EImage image, int x, int y, float magnification, double angle ) {
    Image img =
        getNativeImage( image ).getAsBitmapForSize( gc.getDevice(), Math.round( small_icon_size * magnification ),
            Math.round( small_icon_size * magnification ), angle );
    if ( img != null ) {
      Rectangle bounds = img.getBounds();
      int hx = Math.round( bounds.width / magnification );
      int hy = Math.round( bounds.height / magnification );
      gc.drawImage( img, 0, 0, bounds.width, bounds.height, x - hx / 2, y - hy / 2, hx, hy );
    }
  }

  public Point getImageBounds( EImage image ) {
    return new Point( small_icon_size, small_icon_size );
  }

  public static final SwtUniversalImage getNativeImage( EImage image ) {
    switch ( image ) {
      case LOCK:
        return GUIResource.getInstance().getSwtImageLocked();
      case STEP_ERROR:
        return GUIResource.getInstance().getSwtImageStepError();
      case STEP_ERROR_RED:
        return GUIResource.getInstance().getSwtImageRedStepError();
      case EDIT:
        return GUIResource.getInstance().getSwtImageEdit();
      case CONTEXT_MENU:
        return GUIResource.getInstance().getSwtImageContextMenu();
      case TRUE:
        return GUIResource.getInstance().getSwtImageTrue();
      case FALSE:
        return GUIResource.getInstance().getSwtImageFalse();
      case ERROR:
        return GUIResource.getInstance().getSwtImageErrorHop();
      case INFO:
        return GUIResource.getInstance().getSwtImageInfoHop();
      case TARGET:
        return GUIResource.getInstance().getSwtImageHopTarget();
      case INPUT:
        return GUIResource.getInstance().getSwtImageHopInput();
      case OUTPUT:
        return GUIResource.getInstance().getSwtImageHopOutput();
      case ARROW:
        return GUIResource.getInstance().getSwtImageArrow();
      case COPY_ROWS:
        return GUIResource.getInstance().getSwtImageCopyHop();
      case LOAD_BALANCE:
        return GUIResource.getInstance().getSwtImageBalance();
      case CHECKPOINT:
        return GUIResource.getInstance().getSwtImageCheckpoint();
      case DB:
        return GUIResource.getInstance().getSwtImageConnection();
      case PARALLEL:
        return GUIResource.getInstance().getSwtImageParallelHop();
      case UNCONDITIONAL:
        return GUIResource.getInstance().getSwtImageUnconditionalHop();
      case BUSY:
        return GUIResource.getInstance().getSwtImageBusy();
      case INJECT:
        return GUIResource.getInstance().getSwtImageInject();
      case ARROW_DEFAULT:
        return GUIResource.getInstance().getDefaultArrow();
      case ARROW_OK:
        return GUIResource.getInstance().getOkArrow();
      case ARROW_ERROR:
        return GUIResource.getInstance().getErrorArrow();
      case ARROW_DISABLED:
        return GUIResource.getInstance().getDisabledArrow();
      case ARROW_CANDIDATE:
        return GUIResource.getInstance().getCandidateArrow();
      default:
        break;
    }
    return null;
  }

  public void drawPoint( int x, int y ) {
    gc.drawPoint( x, y );
  }

  public void drawPolygon( int[] polygon ) {
    gc.drawPolygon( polygon );
  }

  public void drawPolyline( int[] polyline ) {
    gc.drawPolyline( polyline );
  }

  public void drawRectangle( int x, int y, int width, int height ) {
    gc.drawRectangle( x, y, width, height );
  }

  public void drawRoundRectangle( int x, int y, int width, int height, int circleWidth, int circleHeight ) {
    gc.drawRoundRectangle( x, y, width, height, circleWidth, circleHeight );
  }

  public void drawText( String text, int x, int y ) {
    gc.drawText( text, x, y );
  }

  public void drawText( String text, int x, int y, boolean transparent ) {
    gc.drawText( text, x, y, SWT.DRAW_DELIMITER | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT );
  }

  public void fillPolygon( int[] polygon ) {
    gc.fillPolygon( polygon );
  }

  public void fillRectangle( int x, int y, int width, int height ) {
    gc.fillRectangle( x, y, width, height );
  }

  public void fillGradientRectangle( int x, int y, int width, int height, boolean vertical ) {
    gc.fillGradientRectangle( x, y, width, height, vertical );
  }

  public void fillRoundRectangle( int x, int y, int width, int height, int circleWidth, int circleHeight ) {
    gc.fillRoundRectangle( x, y, width, height, circleWidth, circleHeight );
  }

  public Point getDeviceBounds() {
    org.eclipse.swt.graphics.Rectangle p = gc.getDevice().getBounds();
    return new Point( p.width, p.height );
  }

  public void setAlpha( int alpha ) {
    gc.setAlpha( alpha );
  }

  public int getAlpha() {
    return gc.getAlpha();
  }

  public void setBackground( EColor color ) {
    gc.setBackground( getColor( color ) );
  }

  private Color getColor( EColor color ) {
    switch ( color ) {
      case BACKGROUND:
        return background;
      case BLACK:
        return black;
      case WHITE:
        return white;
      case RED:
        return red;
      case YELLOW:
        return yellow;
      case ORANGE:
        return orange;
      case GREEN:
        return green;
      case BLUE:
        return blue;
      case MAGENTA:
        return magenta;
      case GRAY:
        return gray;
      case LIGHTGRAY:
        return lightGray;
      case DARKGRAY:
        return darkGray;
      case LIGHTBLUE:
        return lightBlue;
      case CRYSTAL:
        return crystal;
      case HOP_DEFAULT:
        return hopDefault;
      case HOP_OK:
        return hopOK;
      case DEPRECATED:
        return deprecated;
      default:
        break;
    }
    return null;
  }

  public void setFont( EFont font ) {
    switch ( font ) {
      case GRAPH:
        gc.setFont( GUIResource.getInstance().getFontGraph() );
        break;
      case NOTE:
        gc.setFont( GUIResource.getInstance().getFontNote() );
        break;
      case SMALL:
        gc.setFont( GUIResource.getInstance().getFontSmall() );
        break;
      default:
        break;
    }
  }

  public void setForeground( EColor color ) {
    gc.setForeground( getColor( color ) );
  }

  public void setLineStyle( ELineStyle lineStyle ) {
    switch ( lineStyle ) {
      case DASHDOT:
        gc.setLineStyle( SWT.LINE_DASHDOT );
        break;
      case SOLID:
        gc.setLineStyle( SWT.LINE_SOLID );
        break;
      case DOT:
        gc.setLineStyle( SWT.LINE_DOT );
        break;
      case DASH:
        gc.setLineStyle( SWT.LINE_DASH );
        break;
      case PARALLEL:
        gc.setLineAttributes( new LineAttributes(
          gc.getLineWidth(), SWT.CAP_FLAT, SWT.JOIN_MITER, SWT.LINE_CUSTOM, new float[] { 5, 3, }, 0, 10 ) );
        break;
      default:
        break;
    }
  }

  public void setLineWidth( int width ) {
    gc.setLineWidth( width );
  }

  public void setTransform( float translationX, float translationY, int shadowsize, float magnification ) {
    if ( transform != null ) { // dispose of previous to prevent leaking of handles
      transform.dispose();
    }
    transform = new Transform( gc.getDevice() );
    transform.translate( translationX + shadowsize * magnification, translationY + shadowsize * magnification );
    transform.scale( magnification, magnification );
    gc.setTransform( transform );
    currentMagnification = magnification;
  }

  public Point textExtent( String text ) {
    org.eclipse.swt.graphics.Point p = gc.textExtent( text );
    return new Point( p.x, p.y );
  }

  public void drawStepIcon( int x, int y, StepMeta stepMeta, float magnification ) {
    String steptype = stepMeta.getStepID();
    Image im = null;
    if ( stepMeta.isMissing() ) {
      im = GUIResource.getInstance().getImageMissing();
    } else if ( stepMeta.isDeprecated() ) {
      im = GUIResource.getInstance().getImageDeprecated();
    } else {
      im =
          images.get( steptype ).getAsBitmapForSize( gc.getDevice(), Math.round( iconsize * magnification ),
              Math.round( iconsize * magnification ) );
    }
    if ( im != null ) { // Draw the icon!
      org.eclipse.swt.graphics.Rectangle bounds = im.getBounds();
      gc.drawImage( im, 0, 0, bounds.width, bounds.height, x, y, iconsize, iconsize );
    }
  }

  public void drawJobEntryIcon( int x, int y, JobEntryCopy jobEntryCopy, float magnification ) {
    if ( jobEntryCopy == null ) {
      return; // Don't draw anything
    }

    SwtUniversalImage swtImage = null;

    int w = Math.round( iconsize * magnification );
    int h = Math.round( iconsize * magnification );

    if ( jobEntryCopy.isSpecial() ) {
      if ( jobEntryCopy.isStart() ) {
        swtImage = GUIResource.getInstance().getSwtImageStart();
      }
      if ( jobEntryCopy.isDummy() ) {
        swtImage = GUIResource.getInstance().getSwtImageDummy();
      }
    } else {
      String configId = jobEntryCopy.getEntry().getPluginId();
      if ( configId != null ) {
        swtImage = GUIResource.getInstance().getImagesJobentries().get( configId );
      }
    }
    if ( jobEntryCopy.isMissing() ) {
      swtImage = GUIResource.getInstance().getSwtImageMissing();
    }
    if ( swtImage == null ) {
      return;
    }

    Image image = swtImage.getAsBitmapForSize( gc.getDevice(), w, h );

    org.eclipse.swt.graphics.Rectangle bounds = image.getBounds();
    gc.drawImage( image, 0, 0, bounds.width, bounds.height, x, y, iconsize, iconsize );
  }

  @Override
  public void drawJobEntryIcon( int x, int y, JobEntryCopy jobEntryCopy ) {
    drawJobEntryIcon( x, y, jobEntryCopy, currentMagnification );
  }

  @Override
  public void drawStepIcon( int x, int y, StepMeta stepMeta ) {
    drawStepIcon( x, y, stepMeta, currentMagnification );
  }

  public void setAntialias( boolean antiAlias ) {
    if ( antiAlias ) {
      gc.setAntialias( SWT.ON );
    } else {
      gc.setAntialias( SWT.OFF );
    }
  }

  public void setBackground( int r, int g, int b ) {
    Color color = getColor( r, g, b );
    gc.setBackground( color );
  }

  public void setForeground( int r, int g, int b ) {
    Color color = getColor( r, g, b );
    gc.setForeground( color );
  }

  private Color getColor( int r, int g, int b ) {
    Color color = new Color( PropsUI.getDisplay(), new RGB( r, g, b ) );
    int index = colors.indexOf( color );
    if ( index < 0 ) {
      colors.add( color );
    } else {
      color.dispose();
      color = colors.get( index );
    }
    return color;
  }

  public void setFont( String fontName, int fontSize, boolean fontBold, boolean fontItalic ) {
    int swt = SWT.NORMAL;
    if ( fontBold ) {
      swt = SWT.BOLD;
    }
    if ( fontItalic ) {
      swt = swt | SWT.ITALIC;
    }

    Font font = new Font( PropsUI.getDisplay(), fontName, fontSize, swt );
    int index = fonts.indexOf( font );
    if ( index < 0 ) {
      fonts.add( font );
    } else {
      font.dispose();
      font = fonts.get( index );
    }
    gc.setFont( font );
  }

  public Object getImage() {
    return image;
  }

  public void switchForegroundBackgroundColors() {
    Color fg = gc.getForeground();
    Color bg = gc.getBackground();

    gc.setForeground( bg );
    gc.setBackground( fg );
  }

  public Point getArea() {
    return area;
  }

  @Override
  public void drawImage( BufferedImage image, int x, int y ) {
    ImageData imageData = ImageUtil.convertToSWT( image );
    Image swtImage = new Image( gc.getDevice(), imageData );
    gc.drawImage( swtImage, x, y );
    swtImage.dispose();
  }
}
