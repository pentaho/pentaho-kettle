package org.pentaho.di.core.util;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ImageUtil
{
    
    public static Image makeImageTransparent(Display display, Image tempImage, RGB transparentColor)
    {
        ImageData imageData = tempImage.getImageData();
        int pixelIndex = imageData.palette.getPixel(transparentColor);
        imageData.transparentPixel = pixelIndex;
        Image image = new Image(display, imageData);
        tempImage.dispose();
        
        return image;
    }
}
