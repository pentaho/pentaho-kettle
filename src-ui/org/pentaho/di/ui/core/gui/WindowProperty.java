 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

 

package org.pentaho.di.ui.core.gui;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

/**
 * This class stores information about a screen, window, etc.
 * 
 * @author Matt
 * @since 08-04-2004
 *
 */
public class WindowProperty 
{
	private String  name;
	private boolean maximized;
	private Rectangle rectangle; 
	
	public WindowProperty(String name, boolean maximized, Rectangle rectangle)
	{
		this.name      = name;
		this.maximized = maximized;
		this.rectangle = rectangle;
	}
	
	public WindowProperty(String name, boolean maximized, int x, int y, int width, int height)
	{
		this.name      = name;
		this.maximized = maximized;
		this.rectangle = new Rectangle(x, y, width, height);
	}
	
	public WindowProperty(Shell shell)
	{
		name      = shell.getText();

		maximized = shell.getMaximized();
		rectangle = shell.getBounds();
	}
	
	public void setShell(Shell shell)
	{
		setShell(shell, false);
	}

	public void setShell(Shell shell, boolean onlyPosition)
	{
		setShell(shell, onlyPosition, -1, -1);
	}

	public void setShell(Shell shell, int minWidth, int minHeight)
	{
		setShell(shell, false, minWidth, minHeight);
	}

	/**
     * Performs calculations to size and position a dialog If the size passed in
     * is too large for the primary monitor client area, it is shrunk to fit. If
     * the positioning leaves part of the dialog outside the client area, it is
     * centered instead Note that currently, many of the defaults in
     * org.pentaho.di.ui.core/default.properties have crazy values. This causes
     * the failsafe code in here to fire a lot more than is really necessary.
     * 
     * @param shell
     *            The dialog to position and size
     * @param onlyPosition
     *            Avoid resizing if true
     * @param minWidth
     * @param minHeight
     */
	public void setShell(Shell shell, boolean onlyPosition, int minWidth, int minHeight)
	{
		boolean sized=false;
		
		if (!onlyPosition)
		{
			shell.setMaximized(maximized);
		}
		Rectangle r = rectangle;
		if (r!=null && r.x>=0 && r.y>=0 && r.width>=0 && r.height>=0)
		{
			if (r.width > 0 && r.height > 0)
			{
			    if (!onlyPosition)
			    {
			        shell.setSize(r.width, r.height);
			    }
			}
			if (r.x > 0 && r.y > 0)
			{
			    shell.setLocation(r.x, r.y);
			}
			
			sized=true;
		}
		
		if (!onlyPosition)
		{
			if (!sized)
			{
				// shell.pack();
				shell.layout();
			}
		}
		
		// System.out.println("Shell ["+shell.getText()+"] size : "+shell.getBounds());
        
        if (minWidth > 0 || minHeight > 0)
        {
            Rectangle bounds = shell.getBounds();
            if (bounds.width < minWidth) bounds.width = minWidth;
            if (bounds.height < minHeight) bounds.height = minHeight;
            shell.setSize(bounds.width, bounds.height);
        }
        // Sometimes the size of the shell is WAY too great!
        // What's the maximum size for this window?
        // Let's say the size of the Display...

        Rectangle shRect = shell.getBounds();
        Point shSize = Geometry.getSize(shRect);
        Rectangle diRect = shell.getDisplay().getPrimaryMonitor().getClientArea();
        Point diSize = Geometry.getSize(diRect);
        
        Rectangle resizedRect = Geometry.copy(shRect);
        Point oversize = Geometry.subtract(shSize, diSize);
        if (oversize.x > 0)
        {
            resizedRect.width = shSize.x - oversize.x;
        }
        if (oversize.y > 0)
        {
            resizedRect.height = shSize.y - oversize.y;
        }
        
        if (shRect.width != resizedRect.width || shRect.height != resizedRect.height)
        {
            // Make the shell smaller
        	if (!onlyPosition)
        	{
        		shell.setSize(resizedRect.width, resizedRect.height);
        	}
        }
        
        // Detect if the dialog was positioned outside the current client area
        Geometry.moveInside(resizedRect, diRect);
        if (shRect.x != resizedRect.x || shRect.y != resizedRect.y)
        {
            int middleX = diRect.x + (diRect.width - resizedRect.width) / 2;
            int middleY = diRect.y + (diRect.height - resizedRect.height) / 2;

            shell.setLocation(middleX, middleY);
        }
	}

	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public boolean isMaximized()
	{
		return maximized;
	}
	
	public void setMaximized(boolean maximized)
	{
		this.maximized = maximized;
	}
	
	public Rectangle getRectangle()
	{
		return rectangle;
	}
	
	public void setRectangle(Rectangle rectangle)
	{
		this.rectangle = rectangle;
	}
	
	public int getX()      { return rectangle.x; }
	public int getY()      { return rectangle.y; }
	public int getWidth()  { return rectangle.width; }
	public int getHeight() { return rectangle.height; }
	
	public int hashCode()
	{
		return name.hashCode();
	}
	
	public boolean equal(Object obj)
	{
		return ((WindowProperty)obj).getName().equalsIgnoreCase(name);
	}

}
