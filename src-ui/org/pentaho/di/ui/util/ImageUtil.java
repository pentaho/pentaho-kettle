/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.ui.util;

import java.io.IOException;
import java.net.URL;

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
    	// assume the classloader for the active thread
    	ClassLoader cl = Thread.currentThread().getContextClassLoader();
		URL res = cl.getResource(location);
		if (res != null) {
			try {
				java.io.InputStream s = res.openStream();
				if (s != null) {
					return new Image(display,s);
				}
			} catch (IOException e) {
				//do nothing. just move on to trying to load via file system
			}
		}
		try {
			return new Image(display,VFS.getManager().resolveFile(base,location).getContent().getInputStream());
		} catch (FileSystemException e) {
			throw new RuntimeException("Unable to load image with name ["+location+"]", e);
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
