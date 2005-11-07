 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** It belongs to, is maintained by and is copyright 1999-2005 by     **
 **                                                                   **
 **      i-Bridge bvba                                                **
 **      Fonteinstraat 70                                             **
 **      9400 OKEGEM                                                  **
 **      Belgium                                                      **
 **      http://www.kettle.be                                         **
 **      info@kettle.be                                               **
 **                                                                   **
 **********************************************************************/
 

package be.ibridge.kettle.core;
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
		boolean notsized=true;
		
		shell.setMaximized(maximized);
		Rectangle r = rectangle;
		if (r!=null && r.x>=0 && r.y>=0 && r.width>=0 && r.height>=0)
		{
			if (r.x>0 && r.y>0) shell.setSize(r.width, r.height);
			if (r.width>0 && r.height>0) shell.setLocation(r.x, r.y);
			
			notsized=false;
		}
		
		if (notsized)
		{
			shell.pack();
			
			// Sometimes the size of the shell is WAY too great!
			// What's the maximum size for this window?
			// Let's say the size of the Display...
			
			Rectangle shRect = shell.getBounds();
			Rectangle diRect = shell.getDisplay().getBounds();
			
			boolean resize=false;
			if (shRect.width>diRect.width)
			{
				shRect.width = diRect.width;
				resize=true;
			}
			
			if (shRect.height > diRect.height)
			{
				shRect.height = diRect.height;
				resize=true;
			}
			
			if (resize)
			{
				shell.setSize(shRect.width, shRect.height);
			}
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
