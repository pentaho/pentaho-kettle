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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.ui.util.ImageUtil;
import org.pentaho.di.laf.BasePropertyHandler;

/**
 * Displays the Kettle splash screen
 * 
 * @author Matt
 * @since  14-mrt-2005
 */
public class Splash
{
	private Shell splash;
	 
	public Splash(Display display) throws KettleException
	{
		Rectangle displayBounds = display.getPrimaryMonitor().getBounds();

		final Image kettle_image = ImageUtil.getImageAsResource(display, BasePropertyHandler.getProperty("splash_image")); // "kettle_splash.png"
        final Image kettle_icon  = ImageUtil.getImageAsResource(display, BasePropertyHandler.getProperty("splash_icon")); // "spoon32.png");
        
        splash = new Shell(display, SWT.NONE /*SWT.ON_TOP*/);
        splash.setImage(kettle_icon);
        //TODO: move to BaseMessage to track i18n
        splash.setText(BasePropertyHandler.getProperty("splash_text")); // "Pentaho Data Integration"
        
		FormLayout splashLayout = new FormLayout();
		splash.setLayout(splashLayout);
        
		Canvas canvas = new Canvas(splash, SWT.NO_BACKGROUND);
		
		FormData fdCanvas = new FormData();
		fdCanvas.left   = new FormAttachment(0,0);
		fdCanvas.top    = new FormAttachment(0,0);
		fdCanvas.right  = new FormAttachment(100,0);
		fdCanvas.bottom = new FormAttachment(100,0);
		canvas.setLayoutData(fdCanvas);

		canvas.addPaintListener(new PaintListener()
			{
				public void paintControl(PaintEvent e)
				{
					e.gc.drawImage(kettle_image, 0, 0);
				}
			}
		);
		
		splash.addDisposeListener(new DisposeListener()
			{
				public void widgetDisposed(DisposeEvent arg0)
				{
					kettle_image.dispose();
				}
			}
		);
		Rectangle bounds = kettle_image.getBounds();
		int x = (displayBounds.width - bounds.width)/2;
		int y = (displayBounds.height - bounds.height)/2;
		
		splash.setSize(bounds.width, bounds.height);
		splash.setLocation(x,y);
		
		splash.open();
	}
	
	public void dispose()
	{
		if (!splash.isDisposed()) splash.dispose();
	}
	
	public void hide()
	{
		splash.setVisible(false);
	}
	
	public void show()
	{
		splash.setVisible(true);
	}
}
