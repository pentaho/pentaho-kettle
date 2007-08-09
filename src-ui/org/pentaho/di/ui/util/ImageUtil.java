package org.pentaho.di.ui.util;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.pentaho.di.core.vfs.KettleVFS;

public class ImageUtil
{
    private static FileObject base;
    
    static
    {
    	try
    	{
    		base = VFS.getManager().resolveFile(System.getProperty("user.dir"));    		
    	}
    	catch(FileSystemException e)
    	{
    		e.printStackTrace();
    		base = null;
    	}
    }
    
    public static Image makeImageTransparent(Display display, Image tempImage, RGB transparentColor)
    {
        ImageData imageData = tempImage.getImageData();
        int pixelIndex = imageData.palette.getPixel(transparentColor);
        imageData.transparentPixel = pixelIndex;
        Image image = new Image(display, imageData);
        tempImage.dispose();       
        return image;
    }
    
    public static Image getImageAsResource(Display display, String location) {
    	try
    	{
    		return new Image(display,VFS.getManager().resolveFile(base,location).getContent().getInputStream());
    	}
    	catch(FileSystemException e)
    	{
    		e.printStackTrace();
    		return new Image(display,ImageUtil.class.getClassLoader().getResourceAsStream(location));
    	}
    }
    
    public static Image getImage(Display display,String location)
    {	
    	// TODO: find other instances of getImage (plugin, steps) and transition them to new model through an laf manager
    	try
    	{
    		return new Image(display, KettleVFS.getInputStream(location));
    		
    	}
    	catch(Exception e)
    	{
    		try
    		{
    			return new Image(display,ImageUtil.class.getClassLoader().getResourceAsStream(location));
    		}
    		catch(Exception npe)
    		{
    			throw new RuntimeException("Unable to load image with name ["+location+"]", e);
    		}
    	}

    }
}
