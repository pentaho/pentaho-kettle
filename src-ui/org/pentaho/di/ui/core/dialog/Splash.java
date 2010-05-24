/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/

package org.pentaho.di.ui.core.dialog;

import java.io.BufferedReader;
import java.io.InputStreamReader;

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
import org.mortbay.log.Log;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.laf.BasePropertyHandler;
import org.pentaho.di.ui.util.ImageUtil;

/**
 * Displays the Kettle splash screen
 * 
 * @author Matt
 * @since  14-mrt-2005
 */
public class Splash {
  private Shell splash;

  private static Class<?> PKG = Splash.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  public Splash(Display display) throws KettleException {
    Rectangle displayBounds = display.getPrimaryMonitor().getBounds();

    final Image kettle_image = ImageUtil.getImageAsResource(display, BasePropertyHandler.getProperty("splash_image")); // "kettle_splash.png" //$NON-NLS-1$
    final Image kettle_icon = ImageUtil.getImageAsResource(display, BasePropertyHandler.getProperty("splash_icon")); // "spoon.ico" //$NON-NLS-1$

    splash = new Shell(display, SWT.NONE /*SWT.ON_TOP*/);
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
          Log.warn(BaseMessages.getString(PKG, "SplashDialog.LicenseTextNotFound")); //$NON-NLS-1$
        }
        
        String licenseText = sb.toString();
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
            versionText = BaseMessages.getString(PKG, "SplashDialog.GARelease") + " - " + versionText;  //$NON-NLS-1$//$NON-NLS-2$
          }

        Font verFont = new Font(e.display, "Helvetica", 11, SWT.BOLD); //$NON-NLS-1$
        e.gc.setFont(verFont);
        e.gc.drawText(versionText, 290, 205, true);
                
        // try using the desired font size for the license text
        int fontSize = 8;
        Font licFont = new Font(e.display, "Helvetica", fontSize, SWT.NORMAL); //$NON-NLS-1$
        e.gc.setFont(licFont);

        // if the text will not fit the allowed space 
        while (!willLicenseTextFit(licenseText, e.gc)) {
          fontSize--;
          licFont = new Font(e.display, "Helvetica", fontSize, SWT.NORMAL); //$NON-NLS-1$
          e.gc.setFont(licFont);          
        }
        
        e.gc.drawText(licenseText, 290, 290, true);
      }
    });

    splash.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent arg0) {
        kettle_image.dispose();
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
    final Image exclamation_image = ImageUtil.getImageAsResource(display, BasePropertyHandler
        .getProperty("exclamation_image")); // "exclamation.png" //$NON-NLS-1$
    
    gc.setBackground(new Color(gc.getDevice(), 255, 253, 213));
    gc.setForeground(new Color(gc.getDevice(), 220, 177, 20));
    gc.fillRectangle(290, 231, 367, 49);
    gc.drawRectangle(290, 231, 367, 49);
    gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
    gc.drawImage(exclamation_image, 304, 243);

    Font font = new Font(display, "Helvetica", 10, SWT.NORMAL); //$NON-NLS-1$
    gc.setFont(font);
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
