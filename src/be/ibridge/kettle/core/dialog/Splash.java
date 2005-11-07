
package be.ibridge.kettle.core.dialog;

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

import be.ibridge.kettle.core.Const;

/**
 * Displays the Kettle splash screen
 * 
 * @author Matt
 * @since  14-mrt-2005
 */
public class Splash
{
	private Shell splash;
	
	public Splash(Display display)
	{
		Rectangle displayBounds = display.getBounds();
		
		splash = new Shell(display, SWT.NONE /*SWT.ON_TOP*/);
		final Image kettle_image = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "kettle_splash.png"));

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
