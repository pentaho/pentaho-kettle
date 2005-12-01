 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

 

package be.ibridge.kettle.core;
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
		boolean sized=false;
		
		shell.setMaximized(maximized);
		Rectangle r = rectangle;
		if (r!=null && r.x>=0 && r.y>=0 && r.width>=0 && r.height>=0)
		{
			if (r.x>0 && r.y>0) shell.setSize(r.width, r.height);
			if (r.width>0 && r.height>0) shell.setLocation(r.x, r.y);
			
			sized=true;
		}
		
		if (!sized)
		{
			shell.pack();
		}
        
        // Sometimes the size of the shell is WAY too great!
        // What's the maximum size for this window?
        // Let's say the size of the Display...
        
        Point     shLoc  = shell.getLocation();
        Rectangle shRect = shell.getBounds();
        Rectangle diRect = shell.getDisplay().getBounds();
        
        boolean resizex=false;
        boolean resizey=false;
        if (shRect.width>diRect.width)
        {
            shRect.width = diRect.width;
            resizex=true;
        }
        
        if (shRect.height > diRect.height)
        {
            shRect.height = diRect.height;
            resizey=true;
        }
        
        if (resizex || resizey)
        {
            // Make the shell smaller
            shell.setSize(shRect.width, shRect.height);
            
            // re-set the position
            if (resizex) shLoc.x=0;
            if (resizey) shLoc.y=0;
            shell.setLocation(shLoc.x, shLoc.y);
        }

        // Sometimes it happens that Windows places part of a Window outside the viewing area.
        // Perhaps we can correct this too?
        boolean moveLeft=false;
        boolean moveUp  =false;
        if (shLoc.x + shRect.width > diRect.width)
        {
            shLoc.x = diRect.width - shRect.width - 50; // Move left...
            if (shLoc.x<0) shLoc.x = 0;
            moveLeft=true;
        }
        if (shLoc.y + shRect.height > diRect.height)
        {
            shLoc.y = diRect.height- shRect.height - 50; // Move up...
            if (shLoc.y<0) shLoc.y = 0;
            moveUp=true;
        }
        if (moveLeft||moveUp)
        {
            shell.setLocation(shLoc.x, shLoc.y);
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
