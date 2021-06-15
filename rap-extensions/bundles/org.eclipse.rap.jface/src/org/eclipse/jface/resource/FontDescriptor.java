/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

/**
 * Lightweight descriptor for a font. Creates the described font on demand.
 * Subclasses can implement different ways of describing a font. These objects
 * will be compared, so hashCode(...) and equals(...) must return something 
 * meaningful.
 * 
 * @since 1.0
 */
public abstract class FontDescriptor extends DeviceResourceDescriptor {
    
    /**
     * Creates a FontDescriptor that describes an existing font. The resulting
     * descriptor depends on the Font. Disposing the Font while the descriptor
     * is still in use may throw a graphic disposed exception.
     * 
     * @deprecated use {@link FontDescriptor#createFrom(Font)}
     *
     * @param font a font to describe
     * @param originalDevice must be the same Device that was passed into
     * the font's constructor when it was first created.
     * @return a newly created FontDescriptor.
     */
    public static FontDescriptor createFrom(Font font, Device originalDevice) {
        return new ArrayFontDescriptor(font);
    }
    
    /**
     * Creates a FontDescriptor that describes an existing font. The resulting
     * descriptor depends on the original Font, and disposing the original Font
     * while the descriptor is still in use may cause SWT to throw a graphic
     * disposed exception.
     * 
     * @param font font to create
     * @return a newly created FontDescriptor that describes the given font
     */
    public static FontDescriptor createFrom(Font font) {
        return new ArrayFontDescriptor(font);
    }
    
    /**
     * Creates a new FontDescriptor given the an array of FontData that describes 
     * the font.
     * 
     * @param data an array of FontData that describes the font (will be passed into
     * the Font's constructor)
     * @return a FontDescriptor that describes the given font
     */
    public static FontDescriptor createFrom(FontData[] data) {
        return new ArrayFontDescriptor(data);
    }
    
    /**
     * Creates a new FontDescriptor given the associated FontData
     * 
     * @param data FontData describing the font to create
     * @return a newly created FontDescriptor
     */
    public static FontDescriptor createFrom(FontData data) {
        return new ArrayFontDescriptor(new FontData[]{data});
    }
    
    /**
     * Creates a new FontDescriptor given an OS-specific font name, height, and style.
     * 
     * @see Font#Font(org.eclipse.swt.graphics.Device, java.lang.String, int, int)
     *
     * @param name os-specific font name
     * @param height height (pixels)
     * @param style a bitwise combination of NORMAL, BOLD, ITALIC 
     * @return a new FontDescriptor
     */
    public static FontDescriptor createFrom(String name, int height, int style) {
        return createFrom(new FontData(name, height, style));
    }
    
    /**
     * Returns the set of FontData associated with this font. Modifying the elements
     * in the returned array has no effect on the original FontDescriptor.
     * 
     * @return the set of FontData associated with this font
     */
    public FontData[] getFontData() {
    	Font tempFont = createFont(Display.getCurrent());
    	FontData[] result = tempFont.getFontData();
    	destroyFont(tempFont);
    	return result;
    }
    
    /**
     * Returns an array of FontData containing copies of the FontData
     * from the original. 
     * 
     * @param original array to copy
     * @return a deep copy of the original array
     */
    public static FontData[] copy(FontData[] original) {
    	FontData[] result = new FontData[original.length];
    	for (int i = 0; i < original.length; i++) {
			FontData next = original[i];
			
			result[i] = copy(next);
		}
    	
    	return result;
    }
    
    /**
     * Returns a copy of the original FontData
     * 
	 * @param next FontData to copy
	 * @return a copy of the given FontData
	 */
	public static FontData copy(FontData next) {
		FontData result = new FontData(next.getName(), next.getHeight(), next.getStyle());
		result.setLocale(next.getLocale());
		return result;
	}

	/**
     * Returns a FontDescriptor that is equivalent to the reciever, but uses
     * the given style bits. 
     * 
     * <p>Does not modify the reciever.</p>
     * 
     * @param style a bitwise combination of SWT.NORMAL, SWT.ITALIC and SWT.BOLD
     * @return a new FontDescriptor with the given style
     * 
     * @since 1.1
     */
    public final FontDescriptor setStyle(int style) {
    	FontData[] data = getFontData();
    	
    	for (int i = 0; i < data.length; i++) {
			FontData next = data[i];
			
			next.setStyle(style);
		}

    	// Optimization: avoid holding onto extra instances by returning the reciever if
    	// if it is exactly the same as the result
    	FontDescriptor result = new ArrayFontDescriptor(data);
    	if (result.equals(this)) {
    		return this;
    	}
    	
    	return result;
    }
    
    /**
     * <p>Returns a FontDescriptor that is equivalent to the reciever, but
     * has the given style bits, in addition to any styles the reciever already has.</p>
     * 
     * <p>Does not modify the reciever.</p>
     * 
     * @param style a bitwise combination of SWT.NORMAL, SWT.ITALIC and SWT.BOLD
     * @return a new FontDescriptor with the given additional style bits
     * @since 1.1
     */
    public final FontDescriptor withStyle(int style) {
    	FontData[] data = getFontData();
    	
    	for (int i = 0; i < data.length; i++) {
			FontData next = data[i];
			
			next.setStyle(next.getStyle() | style);
		}
    	
    	// Optimization: avoid allocating extra instances by returning the reciever if
    	// if it is exactly the same as the result
    	FontDescriptor result = new ArrayFontDescriptor(data);
    	if (result.equals(this)) {
    		return this;
    	}
    	
    	return result;    	
    }
    
    /**
     * <p>Returns a new FontDescriptor that is equivalent to the reciever, but
     * has the given height.</p>
     * 
     * <p>Does not modify the reciever.</p>
     * 
     * @param height a height, in points
     * @return a new FontDescriptor with the height, in points
     * @since 1.1
     */
    public final FontDescriptor setHeight(int height) {
    	FontData[] data = getFontData();
    	
    	for (int i = 0; i < data.length; i++) {
			FontData next = data[i];
			
			next.setHeight(height);
		}
    	
    	// Optimization: avoid holding onto extra instances by returning the reciever if
    	// if it is exactly the same as the result
    	FontDescriptor result = new ArrayFontDescriptor(data);
    	if (result.equals(this)) {
    		return this;
    	}
    	
    	return result;    	
    }

    /**
     * <p>Returns a FontDescriptor that is equivalent to the reciever, but whose height
     * is larger by the given number of points.</p>
     * 
     * <p>Does not modify the reciever.</p>
     * 
     * @param heightDelta a change in height, in points. Negative values will return smaller
     * fonts. 
     * @return a FontDescriptor whose height differs from the reciever by the given number
     * of points. 
     * @since 1.2
     */
    public final FontDescriptor increaseHeight(int heightDelta) {
    	if (heightDelta == 0) {
    		return this;
    	}
    	FontData[] data = getFontData();
    	
    	for (int i = 0; i < data.length; i++) {
			FontData next = data[i];
			
			next.setHeight(next.getHeight() + heightDelta);
		}
    	
    	return new ArrayFontDescriptor(data);    	
    }
    
    /**
     * Creates the Font described by this descriptor. 
     * 
     * @param device device on which to allocate the font
     * @return a newly allocated Font (never null)
     * @throws DeviceResourceException if unable to allocate the Font
     */
    public abstract Font createFont(Device device) throws DeviceResourceException;
    
    /**
     * Deallocates anything that was allocated by createFont, given a font
     * that was allocated by an equal FontDescriptor.
     * 
     * @param previouslyCreatedFont previously allocated font
     */
    public abstract void destroyFont(Font previouslyCreatedFont);
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.DeviceResourceDescriptor#create(org.eclipse.swt.graphics.Device)
     */
    public final Object createResource(Device device) throws DeviceResourceException {
        return createFont(device);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.DeviceResourceDescriptor#destroy(java.lang.Object)
     */
    public final void destroyResource(Object previouslyCreatedObject) {
        destroyFont((Font)previouslyCreatedObject);
    }
}
