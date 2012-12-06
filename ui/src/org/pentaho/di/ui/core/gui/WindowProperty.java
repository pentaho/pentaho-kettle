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

package org.pentaho.di.ui.core.gui;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
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
     *            Unused argument.  If the window is outside the viewable client are,
     *            it must be resized to prevent inaccessibility.
     * @param minWidth
     * @param minHeight
     */
	public void setShell(Shell shell, boolean onlyPosition, int minWidth, int minHeight)
	{
		shell.setMaximized(maximized);
		shell.setBounds(rectangle);
        
        if (minWidth > 0 || minHeight > 0)
        {
            Rectangle bounds = shell.getBounds();
            if (bounds.width < minWidth) bounds.width = minWidth;
            if (bounds.height < minHeight) bounds.height = minHeight;
            shell.setSize(bounds.width, bounds.height);
        }

        // Just to double check: what is the preferred size of this dialog?
        // This computed is a minimum.  If the minimum is smaller than the 
        // size of the current shell, we make it larger.
        //
		Point computedSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Rectangle shellSize = shell.getBounds();
		if (shellSize.width<computedSize.x) shellSize.width=computedSize.x;
		if (shellSize.height<computedSize.y) shellSize.height=computedSize.y;
		shell.setBounds(shellSize);

        Rectangle entireClientArea = shell.getDisplay().getClientArea();
        Rectangle resizedRect = Geometry.copy(shellSize);
        constrainRectangleToContainer(resizedRect, entireClientArea);
        		
		// If the persisted size/location doesn't perfectly fit
		// into the entire client area, the persisted settings
		// likely were not meant for this configuration of monitors.
		// Relocate the shell into either the parent monitor or if
		// there is no parent, the primary monitor then center it.
		//
        if (!resizedRect.equals(shellSize) || isClippedByUnalignedMonitors(resizedRect, shell.getDisplay()))
        {
            Monitor monitor = shell.getDisplay().getPrimaryMonitor();
            if (shell.getParent() != null)
            {
                monitor = shell.getParent().getMonitor();
            }
            Rectangle monitorClientArea = monitor.getClientArea();
            constrainRectangleToContainer(resizedRect, monitorClientArea);
            
            resizedRect.x = monitorClientArea.x + (monitorClientArea.width - resizedRect.width) / 2;
            resizedRect.y = monitorClientArea.y + (monitorClientArea.height - resizedRect.height) / 2;

            shell.setBounds(resizedRect);
        }
	}

    /**
     * @param constrainee
     * @param container
     */
    private void constrainRectangleToContainer(Rectangle constrainee, Rectangle container)
    {
        Point originalSize = Geometry.getSize(constrainee);
        Point containerSize = Geometry.getSize(container);
        Point oversize = Geometry.subtract(originalSize, containerSize);
        if (oversize.x > 0)
        {
            constrainee.width = originalSize.x - oversize.x;
        }
        if (oversize.y > 0)
        {
            constrainee.height = originalSize.y - oversize.y;
        }
        // Detect if the dialog was positioned outside the container
        Geometry.moveInside(constrainee, container);
    }
    
    /**
     * This method is needed in the case where the display has multiple monitors, but
     * they do not form a uniform rectangle.  In this case, it is possible for Geometry.moveInside()
     * to not detect that the window is partially or completely clipped.
     * We check to make sure at least the upper left portion of the rectangle is visible to give the
     * user the ability to reposition the dialog in this rare case. 
     * @param constrainee
     * @param display
     * @return
     */
    private boolean isClippedByUnalignedMonitors(Rectangle constrainee, Display display)
    {
        boolean isClipped;
        Monitor[] monitors = display.getMonitors();
        if (monitors.length > 0)
        {
            // Loop searches for a monitor proving false
            isClipped = true;
            for (Monitor monitor : monitors)
            {
                if (monitor.getClientArea().contains(constrainee.x + 10, constrainee.y + 10))
                {
                    isClipped = false;
                    break;
                }
            }
        }
        else
        {
            isClipped = false;
        }
        
        return isClipped;
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
