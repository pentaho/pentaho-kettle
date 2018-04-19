package org.pentaho.di.ui.core.util.geo.renderer.util;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import java.awt.Color;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Helper class allowing the use of Java 2D on SWT or Draw2D graphical
 * context.The initial code from this class comes from the following website:
 *  
 *  Article    : http://www-128.ibm.com/developerworks/java/library/j-2dswt/
 *  Sample code: http://download.boulder.ibm.com/ibmdl/pub/software/dw/library/j-2dswtsrc.zip
 *
 * @author Yannick Saillet, mouattara, jmathieu & tbadard
 */
public class GraphicsConverter 
{
	private static final PaletteData PALETTE_DATA = new PaletteData(0xFF0000, 0xFF00, 0xFF);
	private static final int TRANSPARENT_COLOR = 0x123456; // RGB value to use as transparent color */

	private BufferedImage awtImage;
	private Image swtImage;
	private ImageData swtImageData;
	private int[] awtPixels;

	public void prepareRendering(GC gc) {
		Rectangle clip = gc.getClipping();
		prepareRendering(clip.x, clip.y, clip.width, clip.height);
	}

	private void prepareRendering(int clipX, int clipY, int clipW, int clipH) {
		// check that the offscreen images are initialized and large enough
		checkOffScreenImages(clipW, clipH);
		
		// fill the region in the AWT image with the transparent color
		Graphics awtGraphics = awtImage.getGraphics();
		awtGraphics.setColor(new Color(TRANSPARENT_COLOR));
		awtGraphics.fillRect(clipX, clipY, clipW, clipH);
	}

	public Graphics2D getGraphics2D() {
		return awtImage == null?null:(Graphics2D) awtImage.getGraphics();
	}

	public void render(GC gc) {
		if (awtImage != null){
			Rectangle clip = gc.getClipping();
			transferPixels(clip.x, clip.y, clip.width, clip.height);
			gc.drawImage(swtImage, clip.x, clip.y, clip.width, clip.height, clip.x, clip.y, clip.width, clip.height);
		}
	}

	private void transferPixels(int clipX, int clipY, int clipW, int clipH) {
		int step = swtImageData.depth / 8;
		byte[] data = swtImageData.data;
		awtImage.getRGB(clipX, clipY, clipW, clipH, awtPixels, 0, clipW);
		
		for (int i = 0; i < clipH; i++) {
			int idx = (clipY + i) * swtImageData.bytesPerLine + clipX * step;
		    
			for (int j = 0; j < clipW; j++) {
				int rgb = awtPixels[j + i * clipW];
				for (int k = swtImageData.depth - 8; k >= 0; k -= 8) {
					data[idx++] = (byte) ((rgb >> k) & 0xFF);
		        }
			}
		}

		if (swtImage != null) 
			swtImage.dispose();
		swtImage = new Image(Display.getDefault(), swtImageData);
	}

	public void dispose() {
		if (awtImage != null) 
			awtImage.flush();
		if (swtImage != null) 
			swtImage.dispose();

		awtImage = null;
		swtImageData = null;
		awtPixels = null;
	}

	private void checkOffScreenImages(int width, int height) {
		int currentImageWidth = 0;
		int currentImageHeight = 0;
		
		if (swtImage != null){
			currentImageWidth = swtImage.getImageData().width;
			currentImageHeight = swtImage.getImageData().height;
		}

		// if the offscreen images are too small, recreate them
		if (width > currentImageWidth || height > currentImageHeight){
			dispose();
			width = Math.max(width, currentImageWidth);
			height = Math.max(height, currentImageHeight);
			awtImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			swtImageData = new ImageData(width, height, 24, PALETTE_DATA);
			swtImageData.transparentPixel = TRANSPARENT_COLOR;
			awtPixels = new int[width * height];
		}
	}
}