/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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
import java.util.Calendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.laf.BasePropertyHandler;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.util.ImageUtil;

/**
 * Displays the Kettle splash screen
 * 
 * @author Matt
 * @since  14-mrt-2005
 */
public class Splash {
  private Shell splash;
  
  private Image kettle_image;
  private Image kettle_icon;
  private Image exclamation_image;
  
  private Font verFont;
  private Font licFont;
  private Font devWarningFont;

  private Color versionWarningBackgroundColor;
  private Color versionWarningForegroundColor;
  
  private int licFontSize = 8;

  private static Class<?> PKG = Splash.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
  
  private static LogChannelInterface log;

  public Splash(Display display) throws KettleException {
    log = new LogChannel(Spoon.APP_NAME);
    
    Rectangle displayBounds = display.getPrimaryMonitor().getBounds();

    kettle_image = ImageUtil.getImageAsResource(display, BasePropertyHandler.getProperty("splash_image")); // "kettle_splash.png" //$NON-NLS-1$
    kettle_icon = ImageUtil.getImageAsResource(display, BasePropertyHandler.getProperty("splash_icon")); // "spoon.ico" //$NON-NLS-1$
    exclamation_image = ImageUtil.getImageAsResource(display, BasePropertyHandler.getProperty("exclamation_image")); // "exclamation.png" //$NON-NLS-1$

    verFont = new Font(display, "Helvetica", 11, SWT.BOLD); //$NON-NLS-1$
    licFont = new Font(display, "Helvetica", licFontSize, SWT.NORMAL); //$NON-NLS-1$
    devWarningFont = new Font(display, "Helvetica", 10, SWT.NORMAL); //$NON-NLS-1$
    
    versionWarningBackgroundColor = new Color(display, 255, 253, 213);
    versionWarningForegroundColor = new Color(display, 220, 177, 20);
    
    splash = new Shell(display, SWT.ON_TOP);
    splash.setImage(kettle_icon);

    splash.setText(BaseMessages.getString(PKG, "SplashDialog.Title")); // "Pentaho Data Integration" //$NON-NLS-1$

    FormLayout splashLayout = new FormLayout();
    splash.setLayout(splashLayout);

    Canvas canvas = new Canvas(splash, SWT.NO_BACKGROUND);

    FormData fdCanvas = new FormData();
    fdCanvas.left = new FormAttachment(0, 0);
    fdCanvas.top = new FormAttachment(0, 0);
    fdCanvas.right = new FormAttachment(100, 0);
    fdCanvas.bottom = new FormAttachment(100, 0);
    canvas.setLayoutData(fdCanvas);
    
    canvas.addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        String versionText = BaseMessages.getString(PKG, "SplashDialog.Version") + " " + Const.VERSION; //$NON-NLS-1$ //$NON-NLS-2$
        
        StringBuilder sb = new StringBuilder();
        String line = null;
        
        try {
          BufferedReader reader = new BufferedReader(new InputStreamReader(Splash.class.getClassLoader().getResourceAsStream("org/pentaho/di/ui/core/dialog/license/license.txt"))); //$NON-NLS-1$
          
          while((line = reader.readLine()) != null) {
            sb.append(line + System.getProperty("line.separator")); //$NON-NLS-1$
          }
        } catch (Exception ex) {
          sb.append(""); //$NON-NLS-1$
          log.logError(BaseMessages.getString(PKG, "SplashDialog.LicenseTextNotFound"), ex); //$NON-NLS-1$
        }
        Calendar cal = Calendar.getInstance();
        String licenseText = String.format(sb.toString(), cal);
        e.gc.drawImage(kettle_image, 0, 0);

        // If this is a Milestone or RC release, warn the user
        if (Const.RELEASE.equals(Const.ReleaseType.MILESTONE)) {
        versionText = BaseMessages.getString(PKG, "SplashDialog.DeveloperRelease") + " - " + versionText; //$NON-NLS-1$ //$NON-NLS-2$
          drawVersionWarning(e);
        } else if (Const.RELEASE.equals(Const.ReleaseType.RELEASE_CANDIDATE)) {
          versionText = BaseMessages.getString(PKG, "SplashDialog.ReleaseCandidate") + " - " + versionText;  //$NON-NLS-1$//$NON-NLS-2$
        }
        else if (Const.RELEASE.equals(Const.ReleaseType.PREVIEW)) {
          versionText = BaseMessages.getString(PKG, "SplashDialog.PreviewRelease") + " - " + versionText;  //$NON-NLS-1$//$NON-NLS-2$
        }
        else if (Const.RELEASE.equals(Const.ReleaseType.GA)) {
            versionText = BaseMessages.getString(PKG, "SplashDialog.GA") + " - " + versionText;  //$NON-NLS-1$//$NON-NLS-2$
          }
        
        e.gc.setFont(verFont);
        e.gc.drawText(versionText, 290, 205, true);
                
        // try using the desired font size for the license text
        e.gc.setFont(licFont);

        // if the text will not fit the allowed space 
        while (!willLicenseTextFit(licenseText, e.gc)) {
          licFontSize--;
          if (licFont != null) {
            licFont.dispose();
          }
          licFont = new Font(e.display, "Helvetica", licFontSize, SWT.NORMAL); //$NON-NLS-1$
          e.gc.setFont(licFont);          
        }
        
        e.gc.drawText(licenseText, 290, 290, true);
      }
    });

    splash.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent arg0) {
        kettle_image.dispose();
        kettle_icon.dispose();
        exclamation_image.dispose();
        verFont.dispose();
        licFont.dispose();
        devWarningFont.dispose();
        versionWarningForegroundColor.dispose();
        versionWarningBackgroundColor.dispose();
      }
    });
    Rectangle bounds = kettle_image.getBounds();
    int x = (displayBounds.width - bounds.width) / 2;
    int y = (displayBounds.height - bounds.height) / 2;

    splash.setSize(bounds.width, bounds.height);
    splash.setLocation(x, y);

    splash.open();
  }
  
  // determine if the license text will fit the allocated space
  private boolean willLicenseTextFit(String licenseText, GC gc) {
    Point splashSize = splash.getSize();
    Point licenseDrawLocation = new Point(290, 290);
    Point requiredSize = gc.textExtent(licenseText);
    
    int width = splashSize.x - licenseDrawLocation.x;
    int height = splashSize.y - licenseDrawLocation.y;
    
    boolean fitsVertically = width >= requiredSize.x;
    boolean fitsHorizontally = height >= requiredSize.y;
    
    return (fitsVertically && fitsHorizontally);
    
  }
  
  private void drawVersionWarning(PaintEvent e) {
    drawVersionWarning(e.gc, e.display);
  }
  
  private void drawVersionWarning(GC gc, Display display) {
    gc.setBackground(versionWarningBackgroundColor);
    gc.setForeground(versionWarningForegroundColor);
    gc.fillRectangle(290, 231, 367, 49);
    gc.drawRectangle(290, 231, 367, 49);
    gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
    gc.drawImage(exclamation_image, 304, 243);

    gc.setFont(devWarningFont);
    gc.drawText(BaseMessages.getString(PKG, "SplashDialog.DevelopmentWarning"), 335, 241); //$NON-NLS-1$
  }

  public void dispose() {
    if (!splash.isDisposed())
      splash.dispose();
  }

  public void hide() {
    splash.setVisible(false);
  }

  public void show() {
    splash.setVisible(true);
  }
}
