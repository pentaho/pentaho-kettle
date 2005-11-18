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
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.PixelGrabber;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Display;

/**
 * This class contains static final methods to convert an image from Java2D (awt) to SWT.
 * 
 * @author Matt
 * @since 28-03-2004
 *
 */
public class ImageConverter 
{
	public static final Image convertBufferedImage(BufferedImage awt, Display disp)
	{
		Image img = null;
		
		int width = awt.getWidth();
		int height = awt.getHeight();

		PixelGrabber grabber = new PixelGrabber(awt, 0, 0, width, height, true);
		try
		{
			if (grabber.grabPixels())
			{
				DirectColorModel cm = (DirectColorModel)ColorModel.getRGBdefault();
				int[] pixels = (int[])grabber.getPixels();
				PaletteData palette = new PaletteData(cm.getRedMask(), cm.getGreenMask(), cm.getBlueMask());
				ImageData imgdata = new ImageData(width, height, cm.getPixelSize(), palette);
				imgdata.setPixels(0, 0, width * height, pixels, 0);
								
				img = new Image(disp, imgdata);
			}
		}
		catch(InterruptedException e)
		{
		}
		
		return img;
	}
}
