/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.core.dialog;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SwtUniversalImage;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.laf.BasePropertyHandler;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.util.SwtSvgImageUtil;
import org.pentaho.di.version.BuildVersion;

/**
 * Displays the Kettle splash screen
 *
 * @author Matt
 * @since 14-mrt-2005
 */
public class Splash {

  private static final Class<?> PKG = Splash.class; // for i18n purposes, needed by Translator2!!
  private static final String FONT_TYPE = "Helvetica";

  private Shell shell;

  private Image kettleImage;
  private Image kettleIcon;
  private Image exclamationImage;

  private Font verFont;
  private Font licFont;
  private Font devWarningFont;

  private Color versionWarningBackgroundColor;
  private Color versionWarningForegroundColor;

  private int licFontSize = 12;

  private LogChannelInterface log;

  public Splash( Display display ) {
    this( display, new Shell( display, SWT.APPLICATION_MODAL ) );
  }

  protected Splash( Display display, Shell splashShell ) {
    log = new LogChannel( Spoon.APP_NAME );

    Rectangle displayBounds = display.getPrimaryMonitor().getBounds();

    // "kettle_splash.png"
    kettleImage = loadAsResource( display, BasePropertyHandler.getProperty( "splash_image" ) );
    // "spoon.ico"
    kettleIcon = loadAsResource( display, BasePropertyHandler.getProperty( "splash_icon" ) );
    // "exclamation.png"
    exclamationImage = loadAsResource( display, BasePropertyHandler.getProperty( "exclamation_image" ) );

    verFont = new Font( display, FONT_TYPE, 14, SWT.BOLD );
    licFont = new Font( display, FONT_TYPE, licFontSize, SWT.NORMAL );
    devWarningFont = new Font( display, FONT_TYPE, 10, SWT.NORMAL );

    versionWarningBackgroundColor = new Color( display, 255, 255, 255 );
    versionWarningForegroundColor = new Color( display, 220, 177, 20 );

    shell = splashShell;
    shell.setImage( kettleIcon );

    shell.setText( BaseMessages.getString( PKG, "SplashDialog.Title" ) ); // "Pentaho Data Integration"

    shell.addPaintListener( e -> {
      StringBuilder sb = new StringBuilder();
      String line;

      try {
        BufferedReader reader =
                new BufferedReader( new InputStreamReader( Splash.class.getClassLoader().getResourceAsStream(
                        "org/pentaho/di/ui/core/dialog/license/license.txt" ) ) );

        while ( ( line = reader.readLine() ) != null ) {
          sb.append( line + System.getProperty( "line.separator" ) );
        }
      } catch ( Exception ex ) {
        log.logError( BaseMessages.getString( PKG, "SplashDialog.LicenseTextNotFound" ), ex );
      }
      Calendar cal = Calendar.getInstance();
      String licenseText = String.format( sb.toString(), cal );
      e.gc.drawImage( kettleImage, 0, 0 );

      String fullVersionText = BaseMessages.getString( PKG, "SplashDialog.Version" );
      String buildVersion = BuildVersion.getInstance().getVersion();
      if ( StringUtils.ordinalIndexOf( buildVersion, ".", 2 ) > 0 ) {
        fullVersionText = fullVersionText + " " + buildVersion.substring( 0, StringUtils.ordinalIndexOf( buildVersion, ".", 2 ) );
      } else {
        fullVersionText = fullVersionText + " " + buildVersion;
      }
      e.gc.setFont( verFont );
      e.gc.setForeground( new Color( display, 65, 65, 65 ) );
      e.gc.drawText( fullVersionText, 290, 162, true );

      String inputStringDate = BuildVersion.getInstance().getBuildDate();
      String outputStringDate = "";
      SimpleDateFormat inputFormat = null;
      SimpleDateFormat outputFormat = null;

      if ( inputStringDate.matches( "^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}.\\d{3}$" ) ) {
        inputFormat = new SimpleDateFormat( "yyyy/MM/dd hh:mm:ss.SSS" );
      }
      if ( inputStringDate.matches( "^\\d{4}-\\d{1,2}-\\d{1,2}\\_\\d{1,2}-\\d{2}-\\d{2}$" ) ) {
        inputFormat = new SimpleDateFormat( "yyyy-MM-dd_hh-mm-ss" );
      }
      if ( inputStringDate.matches( "^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}.\\d{2}.\\d{2}$" ) ) {
        inputFormat = new SimpleDateFormat( "yyyy-MM-dd hh.mm.ss" );
      }
      outputFormat = new SimpleDateFormat( "MMMM d, yyyy hh:mm:ss" );
      try {
        if ( inputFormat != null ) {
          Date date = inputFormat.parse( inputStringDate );
          outputStringDate = outputFormat.format( date );
        } else {
          // If date isn't correspond to formats above just show date in origin format
          outputStringDate = inputStringDate;
        }
      } catch ( ParseException pe ) {
        // Just show date in origin format
        outputStringDate = inputStringDate;
      }

      // try using the desired font size for the license text
      e.gc.setFont( licFont );
      e.gc.setForeground( new Color( display, 65, 65, 65 ) );

      while ( !willLicenseTextFit( licenseText, e.gc ) ) {
        licFontSize--;
        if ( licFont != null ) {
          licFont.dispose();
        }
        licFont = new Font( e.display, "Helvetica", licFontSize, SWT.NORMAL );
        e.gc.setFont( licFont );
      }

      e.gc.drawText( licenseText, 290, 225, true );

      String version = buildVersion;
      // If this is a Milestone or RC release, warn the user
      if ( Const.RELEASE.equals( Const.ReleaseType.MILESTONE ) ) {
        version = BaseMessages.getString( PKG, "SplashDialog.DeveloperRelease" ) + " - " + version;
        drawVersionWarning( e );
      } else if ( Const.RELEASE.equals( Const.ReleaseType.RELEASE_CANDIDATE ) ) {
        version = BaseMessages.getString( PKG, "SplashDialog.ReleaseCandidate" ) + " - " + version;
      } else if ( Const.RELEASE.equals( Const.ReleaseType.PREVIEW ) ) {
        version = BaseMessages.getString( PKG, "SplashDialog.PreviewRelease" ) + " - " + version;
      } else if ( Const.RELEASE.equals( Const.ReleaseType.GA ) ) {
        version = BaseMessages.getString( PKG, "SplashDialog.GA" ) + " - " + version;
      }
      String buildDate = BaseMessages.getString( PKG, "SplashDialog.BuildDate" ) + " " + outputStringDate;
      // use the same font/size as the license text
      e.gc.setForeground( new Color( display, 65, 65, 65 ) );
      e.gc.setFont( new Font( display, FONT_TYPE, 10, SWT.NORMAL ) );
      e.gc.drawText( version, 290, 182, true );
      e.gc.drawText( buildDate, 290, 198, true );
    } );

    shell.addDisposeListener( disposeEvent -> {
      kettleImage.dispose();
      kettleIcon.dispose();
      exclamationImage.dispose();
      verFont.dispose();
      licFont.dispose();
      devWarningFont.dispose();
      versionWarningForegroundColor.dispose();
      versionWarningBackgroundColor.dispose();
    } );
    Rectangle bounds = kettleImage.getBounds();
    int x = ( displayBounds.width - bounds.width ) / 2;
    int y = ( displayBounds.height - bounds.height ) / 2;

    shell.setSize( bounds.width, bounds.height );
    shell.setLocation( x, y );

    shell.open();

    if ( isMacOS() ) {  // This forces the splash screen to display on the Mac.
      long endTime = System.currentTimeMillis() + 5000; // 5 second delay... can you read the splash that fast?
      while ( !shell.isDisposed() && endTime > System.currentTimeMillis() ) {
        if ( !display.readAndDispatch() ) {
          display.sleep();
        }
      }
    }

    TimerTask timerTask = new TimerTask() {

      @Override
      public void run() {
        try {
          shell.redraw();
          LogChannel.UI.logBasic( "Redraw!" );
        } catch ( Throwable e ) {
          // ignore.
        }
      }
    };
    final Timer timer = new Timer();
    timer.schedule( timerTask, 0, 100 );

    shell.addDisposeListener( arg0 -> timer.cancel() );
  }

  // load image from svg
  private Image loadAsResource( Display display, String location ) {
    SwtUniversalImage img = SwtSvgImageUtil.getImageAsResource( display, location );
    Image image = new Image( display, img.getAsBitmap( display ), SWT.IMAGE_COPY );
    img.dispose();
    return image;
  }

  // determine if the license text will fit the allocated space
  private boolean willLicenseTextFit( String licenseText, GC gc ) {
    Point splashSize = shell.getSize();
    Point licenseDrawLocation = new Point( 290, 290 );
    Point requiredSize = gc.textExtent( licenseText );

    int width = splashSize.x - licenseDrawLocation.x;
    int height = splashSize.y - licenseDrawLocation.y;

    boolean fitsVertically = width >= requiredSize.x;
    boolean fitsHorizontally = height >= requiredSize.y;

    return ( fitsVertically && fitsHorizontally );

  }

  private void drawVersionWarning( PaintEvent e ) {
    drawVersionWarning( e.gc, e.display );
  }

  private void drawVersionWarning( GC gc, Display display ) {
    gc.setBackground( versionWarningBackgroundColor );
    gc.setForeground( new Color( display, 65, 65, 65 ) );
    gc.drawImage( exclamationImage, 304, 243 );

    gc.setFont( devWarningFont );
    gc.drawText( BaseMessages.getString( PKG, "SplashDialog.DevelopmentWarning" ), 335, 241, true );
  }

  public void dispose() {
    if ( !shell.isDisposed() ) {
      shell.dispose();
    }
  }

  public void hide() {
    if ( !shell.isDisposed() ) {
      shell.setVisible( false );
    }
  }

  public void show() {
    if ( !shell.isDisposed() ) {
      shell.setVisible( true );
    }
  }

  public static boolean isMacOS() {
    String osName = System.getProperty( "os.name" ).toLowerCase();
    return osName.startsWith( "mac os x" );
  }
}
