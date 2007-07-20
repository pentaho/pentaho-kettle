package org.pentaho.di.ui.util;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.pentaho.di.core.Const;

public class ImageUtil
{
    private static FileObject base;
    private static final String IMAGE_DIR = Const.IMAGE_DIRECTORY.startsWith("/")?Const.IMAGE_DIRECTORY.substring(1):Const.IMAGE_DIRECTORY;
    
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
    
    public static Image getImage(Display display,String location)
    {	
    	try
    	{
    		String vsfLocation = IMAGE_DIR + (location.startsWith(Const.IMAGE_DIRECTORY)?
    				location.substring(Const.IMAGE_DIRECTORY.length()):location);
    		
    		return new Image(display,VFS.getManager().resolveFile(base,vsfLocation).getContent().getInputStream());
    	}
    	catch(FileSystemException e)
    	{
    		e.printStackTrace();
    		return new Image(display,ImageUtil.class.getClassLoader().getResourceAsStream(Const.IMAGE_DIRECTORY+location));
    	}

    }
}
