/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.internal.util.SerializableRunnable;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

/**
 * A font registry maintains a mapping between symbolic font names 
 * and SWT fonts.
 * <p>
 * A font registry owns all of the font objects registered
 * with it, and automatically disposes of them when the SWT Display
 * that creates the fonts is disposed. Because of this, clients do 
 * not need to (indeed, must not attempt to) dispose of font 
 * objects themselves.
 * </p>
 * <p>
 * A special constructor is provided for populating a font registry
 * from a property files using the standard Java resource bundle mechanism.
 * </p>
 * <p>
 * Methods are provided for registering listeners that will be kept
 * apprised of changes to list of registered fonts.
 * </p>
 * <p>
 * Clients may instantiate this class (it was not designed to be subclassed).
 * </p>
 * 
 * Since 3.0 this class extends ResourceRegistry.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class FontRegistry extends ResourceRegistry {

    /**
     * FontRecord is a private helper class that holds onto a font
     * and can be used to generate its bold and italic version. 
     */
    private class FontRecord implements Serializable {

        Font baseFont;

        Font boldFont;

        Font italicFont;

        FontData[] baseData;

        /**
         * Create a new instance of the receiver based on the 
         * plain font and the data for it.
         * @param plainFont The base looked up font.
         * @param data The data used to look it up.
         */
        FontRecord(Font plainFont, FontData[] data) {
            baseFont = plainFont;
            baseData = data;
        }

        /**
         * Dispose any of the fonts created for this record.
         */
        void dispose() {
            baseFont.dispose();
            if (boldFont != null) {
				boldFont.dispose();
			}
            if (italicFont != null) {
				italicFont.dispose();
			}
        }

        /**
         * Return the base Font.
         * @return Font
         */
        public Font getBaseFont() {
            return baseFont;
        }

        /**
         * Return the bold Font. Create a bold version
         * of the base font to get it.
         * @return Font
         */
        public Font getBoldFont() {
            if (boldFont != null) {
				return boldFont;
			}

            FontData[] boldData = getModifiedFontData(SWT.BOLD);
            boldFont = new Font(Display.getCurrent(), boldData);
            return boldFont;
        }

        /**
         * Get a version of the base font data with the specified 
         * style.
         * @param style the new style
         * @return the font data with the style {@link FontData#FontData(String, int, int)}
         * @see SWT#ITALIC
         * @see SWT#NORMAL
         * @see SWT#BOLD
         * @todo Generated comment
         */
        private FontData[] getModifiedFontData(int style) {
            FontData[] styleData = new FontData[baseData.length];
            for (int i = 0; i < styleData.length; i++) {
                FontData base = baseData[i];
                styleData[i] = new FontData(base.getName(), base.getHeight(),
                        base.getStyle() | style);
            }

            return styleData;
        }

        /**
         * Return the italic Font. Create an italic version of the
         * base font to get it.
         * @return Font
         */
        public Font getItalicFont() {
            if (italicFont != null) {
				return italicFont;
			}

            FontData[] italicData = getModifiedFontData(SWT.ITALIC);
            italicFont = new Font(Display.getCurrent(), italicData);
            return italicFont;
        }

        /**
         * Add any fonts that were allocated for this record to the
         * stale fonts. Anything that matches the default font will
         * be skipped.
         * @param defaultFont The system default.
         */
        void addAllocatedFontsToStale(Font defaultFont) {
            //Return all of the fonts allocated by the receiver.
            //if any of them are the defaultFont then don't bother.
            if (defaultFont != baseFont && baseFont != null) {
				staleFonts.add(baseFont);
			}
            if (defaultFont != boldFont && boldFont != null) {
				staleFonts.add(boldFont);
			}
            if (defaultFont != italicFont && italicFont != null) {
				staleFonts.add(italicFont);
			}
        }
    }

    /**
     * Table of known fonts, keyed by symbolic font name
     * (key type: <code>String</code>, 
     *  value type: <code>FontRecord</code>.
     */
    private Map stringToFontRecord = new HashMap(7);

    /**
     * Table of known font data, keyed by symbolic font name
     * (key type: <code>String</code>, 
     *  value type: <code>org.eclipse.swt.graphics.FontData[]</code>).
     */
    private Map stringToFontData = new HashMap(7);

    /**
     * Collection of Fonts that are now stale to be disposed
     * when it is safe to do so (i.e. on shutdown).
     * @see List
     */
    private List staleFonts = new ArrayList();

    /**
     * Runnable that cleans up the manager on disposal of the display.
     */
    protected Runnable displayRunnable = new SerializableRunnable() {
        public void run() {
            clearCaches();
        }
    };

	private boolean displayDisposeHooked;

	private final boolean cleanOnDisplayDisposal;

    /**
     * Creates an empty font registry.
     * <p>
     * There must be an SWT Display created in the current 
     * thread before calling this method.
     * </p>
     */
    public FontRegistry() {
    	this(Display.getCurrent(), true);
    }

    /**
     * Creates a font registry and initializes its content from
     * a property file.
     * <p>
     * There must be an SWT Display created in the current 
     * thread before calling this method.
     * </p>
     * <p>
     * The OS name (retrieved using <code>System.getProperty("os.name")</code>)
     * is converted to lowercase, purged of whitespace, and appended 
     * as suffix (separated by an underscore <code>'_'</code>) to the given 
     * location string to yield the base name of a resource bundle
     * acceptable to <code>ResourceBundle.getBundle</code>.
     * The standard Java resource bundle mechanism is then used to locate
     * and open the appropriate properties file, taking into account
     * locale specific variations.
     * </p>
     * <p>
     * For example, on the Windows 2000 operating system the location string
     * <code>"com.example.myapp.Fonts"</code> yields the base name 
     * <code>"com.example.myapp.Fonts_windows2000"</code>. For the US English locale,
     * this further elaborates to the resource bundle name
     * <code>"com.example.myapp.Fonts_windows2000_en_us"</code>.
     * </p>
     * <p>
     * If no appropriate OS-specific resource bundle is found, the
     * process is repeated using the location as the base bundle name.
     * </p>
     * <p>
     * The property file contains entries that look like this:
     * <pre>
     *	textfont.0=MS Sans Serif-regular-10
     *	textfont.1=Times New Roman-regular-10
     *	
     *	titlefont.0=MS Sans Serif-regular-12
     *	titlefont.1=Times New Roman-regular-12
     * </pre>
     * Each entry maps a symbolic font names (the font registry keys) with
     * a "<code>.<it>n</it></code> suffix to standard font names
     * on the right. The suffix indicated order of preference: 
     * "<code>.0</code>" indicates the first choice,
     * "<code>.1</code>" indicates the second choice, and so on.
     * </p>
     * The following example shows how to use the font registry:
     * <pre>
     *	FontRegistry registry = new FontRegistry("com.example.myapp.fonts");
     *  Font font = registry.get("textfont");
     *  control.setFont(font);
     *  ...
     * </pre>
     *
     * @param location the name of the resource bundle
     * @param loader the ClassLoader to use to find the resource bundle
     * @exception MissingResourceException if the resource bundle cannot be found
     * @since 1.0
     */
    public FontRegistry(String location, ClassLoader loader)
            throws MissingResourceException {
        Display display = Display.getCurrent();
        Assert.isNotNull(display);
        // FIXE: need to respect loader
        //readResourceBundle(location, loader);
        readResourceBundle(location);

        cleanOnDisplayDisposal = true;
        hookDisplayDispose(display);
    }

    /**
     * Load the FontRegistry using the ClassLoader from the PlatformUI
     * plug-in
     * <p>
     * This method should only be called from the UI thread. If you are not on the UI
     * thread then wrap the call with a
     * <code>PlatformUI.getWorkbench().getDisplay().synchExec()</code> in order to
     * guarantee the correct result. Failure to do this may result in an {@link
     * SWTException} being thrown.
     * </p>
     * @param location the location to read the resource bundle from
     * @throws MissingResourceException Thrown if a resource is missing
     */
    public FontRegistry(String location) throws MissingResourceException {
        // FIXE:
        //	this(location, WorkbenchPlugin.getDefault().getDescriptor().getPluginClassLoader());
        this(location, null);
    }

    /**
     * Read the resource bundle at location. Look for a file with the
     * extension _os_ws first, then _os then just the name.
     * @param location - String - the location of the file.
     */

    private void readResourceBundle(String location) {

    	// RAP [bm]: ui has nothing to do with the server os 
//        String osname = System.getProperty("os.name").trim(); //$NON-NLS-1$
//        String wsname = SWT.getPlatform();
//        osname = StringConverter.removeWhiteSpaces(osname).toLowerCase();
//        wsname = StringConverter.removeWhiteSpaces(wsname).toLowerCase();
//        String OSLocation = location;
//        String WSLocation = location;
//        ResourceBundle bundle = null;
//        if (osname != null) {
//            OSLocation = location + "_" + osname; //$NON-NLS-1$
//            if (wsname != null) {
//				WSLocation = OSLocation + "_" + wsname; //$NON-NLS-1$
//			}
//        }
//
//        try {
//            bundle = ResourceBundle.getBundle(WSLocation);
//            readResourceBundle(bundle, WSLocation);
//        } catch (MissingResourceException wsException) {
//            try {
//                bundle = ResourceBundle.getBundle(OSLocation);
//                readResourceBundle(bundle, WSLocation);
//            } catch (MissingResourceException osException) {
//                if (location != OSLocation) {
//                    bundle = ResourceBundle.getBundle(location);
//                    readResourceBundle(bundle, WSLocation);
//                } else {
//					throw osException;
//				}
//            }
//        }

        // add default fonts (see bug 280773)
        Display display = Display.getCurrent();
        FontData systemFont = display.getSystemFont().getFontData()[ 0 ];
        String systemFontName = systemFont.getName();
        int normalHeight = systemFont.getHeight();
        int increasedHeight = ( int )( normalHeight * 1.3 );
        String textFont = systemFontName + "-regular-" + normalHeight; //$NON-NLS-1$
        String bannerFont = systemFontName + "-bold-" + normalHeight; //$NON-NLS-1$
        String headerFont = systemFontName + "-bold-" + increasedHeight; //$NON-NLS-1$
        stringToFontData.put( JFaceResources.TEXT_FONT,
                              new FontData[] { makeFontData( textFont ) } );
        stringToFontData.put( JFaceResources.BANNER_FONT,
                              new FontData[] { makeFontData( bannerFont ) } );
        stringToFontData.put( JFaceResources.HEADER_FONT,
                              new FontData[] { makeFontData( headerFont ) } );

        ResourceBundle bundle = ResourceBundle.getBundle(location);
        readResourceBundle(bundle, location);

    	// RAPEND: [bm] 

    }

    /**
     * Creates an empty font registry.
     *
     * @param display the Display
     */
    public FontRegistry(Display display) {
        this(display, true);
    }
    
    /**
	 * Creates an empty font registry.
	 * 
	 * @param display
	 *            the <code>Display</code>
	 * @param cleanOnDisplayDisposal
	 *            whether all fonts allocated by this <code>FontRegistry</code>
	 *            should be disposed when the display is disposed
	 * @since 1.0
	 */
	public FontRegistry(Display display, boolean cleanOnDisplayDisposal) {
		Assert.isNotNull(display);
		this.cleanOnDisplayDisposal = cleanOnDisplayDisposal;
		if (cleanOnDisplayDisposal) {
			hookDisplayDispose(display);
		}
	}

	// RAP [bm]: 
//    /**
//	 * Find the first valid fontData in the provided list. If none are valid
//	 * return the first one regardless. If the list is empty return null. Return
//	 * <code>null</code> if one cannot be found.
//	 * 
//     * @param fonts the font list
//     * @param display the display used 
//     * @return the font data of the like describe above
//	 * 
//	 * @deprecated use bestDataArray in order to support Motif multiple entry
//	 *             fonts.
//	 */
//    public FontData bestData(FontData[] fonts, Display display) {
//        for (int i = 0; i < fonts.length; i++) {
//            FontData fd = fonts[i];
//
//            if (fd == null) {
//				break;
//			}
//
//            FontData[] fixedFonts = display.getFontList(fd.getName(), false);
//            if (isFixedFont(fixedFonts, fd)) {
//                return fd;
//            }
//
//            FontData[] scalableFonts = display.getFontList(fd.getName(), true);
//            if (scalableFonts.length > 0) {
//                return fd;
//            }
//        }
//
//        //None of the provided datas are valid. Return the
//        //first one as it is at least the first choice.
//        if (fonts.length > 0) {
//			return fonts[0];
//		}
//        
//        //Nothing specified 
//        return null;
//    }

	// RAP [bm]: 
//    /**
//     * Find the first valid fontData in the provided list. 
//     * If none are valid return the first one regardless.
//     * If the list is empty return <code>null</code>.
//     * 
//     * @param fonts list of fonts
//     * @param display the display
//     * @return font data like described above
//     * @deprecated use filterData in order to preserve 
//     * multiple entry fonts on Motif
//     */
//    public FontData[] bestDataArray(FontData[] fonts, Display display) {
//
//        FontData bestData = bestData(fonts, display);
//        if (bestData == null) {
//			return null;
//		}
//        
//        FontData[] datas = new FontData[1];
//        datas[0] = bestData;
//        return datas;
//    }
    
    /**
     * Removes from the list all fonts that do not exist in this system.  
     * If none are valid, return the first irregardless.  If the list is 
     * empty return <code>null</code>.
     * 
     * @param fonts the fonts to check
     * @param display the display to check against
     * @return the list of fonts that have been found on this system
     * @since 1.0
     */
    public FontData [] filterData(FontData [] fonts, Display display) {
    	ArrayList good = new ArrayList(fonts.length);
    	// RAP [bm]: 
//    	for (int i = 0; i < fonts.length; i++) {
//            FontData fd = fonts[i];
//
//            if (fd == null) {
//				continue;
//			}
//
//            FontData[] fixedFonts = display.getFontList(fd.getName(), false);
//            if (isFixedFont(fixedFonts, fd)) {
//                good.add(fd);
//            }
//
//            FontData[] scalableFonts = display.getFontList(fd.getName(), true);
//            if (scalableFonts.length > 0) {
//                good.add(fd);
//            }
//        }

    	
        //None of the provided datas are valid. Return the
        //first one as it is at least the first choice.
        if (good.isEmpty() && fonts.length > 0) {
        	good.add(fonts[0]);
        }
        else if (fonts.length == 0) {
        	return null;
        }
        
        return (FontData[]) good.toArray(new FontData[good.size()]);    	
    }
    

    /**
     * Creates a new font with the given font datas or <code>null</code>
     * if there is no data.
     * @return FontRecord for the new Font or <code>null</code>.
     */
    private FontRecord createFont(String symbolicName, FontData[] fonts) {
        Display display = Display.getCurrent();
        if (display == null) {
        	return null;
        }
        if (cleanOnDisplayDisposal && !displayDisposeHooked) {
        	hookDisplayDispose(display);
        }

        FontData[] validData = filterData(fonts, display);
        if (validData.length == 0) {
            //Nothing specified 
            return null;
        } 

        //Do not fire the update from creation as it is not a property change
        put(symbolicName, validData, false);
        Font newFont = new Font(display, validData);
        return new FontRecord(newFont, validData);
    }

    /**
     * Calculates the default font and returns the result.
     * This method creates a font that must be disposed.
     */
    Font calculateDefaultFont() {
        Display current = Display.getCurrent();
        if (current == null) // can't do much without Display
        	SWT.error(SWT.ERROR_THREAD_INVALID_ACCESS);
		return new Font(current, current.getSystemFont().getFontData());
    }

    /**
     * Returns the default font data.  Creates it if necessary.
     * <p>
     * This method should only be called from the UI thread. If you are not on the UI
     * thread then wrap the call with a
     * <code>PlatformUI.getWorkbench().getDisplay().synchExec()</code> in order to
     * guarantee the correct result. Failure to do this may result in an {@link
     * SWTException} being thrown.
     * </p>
     * @return Font
     */
    public Font defaultFont() {
        return defaultFontRecord().getBaseFont();
    }

    /**
     * Returns the font descriptor for the font with the given symbolic
     * font name. Returns the default font if there is no special value
     * associated with that name
     * 
     * @param symbolicName symbolic font name
     * @return the font descriptor (never null)
     * 
     * @since 1.0
     */
    public FontDescriptor getDescriptor(String symbolicName) {
        Assert.isNotNull(symbolicName);
        return FontDescriptor.createFrom(getFontData(symbolicName));
    }
    
    
    
    /**
     * Returns the default font record.
     */
    private FontRecord defaultFontRecord() {

        FontRecord record = (FontRecord) stringToFontRecord
                .get(JFaceResources.DEFAULT_FONT);
        if (record == null) {
            Font defaultFont = calculateDefaultFont();
            record = createFont(JFaceResources.DEFAULT_FONT, defaultFont
                    .getFontData());
            defaultFont.dispose();
            stringToFontRecord.put(JFaceResources.DEFAULT_FONT, record);
        }
        return record;
    }

    /**
     * Returns the default font data.  Creates it if necessary.
     */
    private FontData[] defaultFontData() {
        return defaultFontRecord().baseData;
    }

    /**
     * Returns the font data associated with the given symbolic font name.
     * Returns the default font data if there is no special value associated
     * with that name.
     *
     * @param symbolicName symbolic font name
     * @return the font
     */
    public FontData[] getFontData(String symbolicName) {

        Assert.isNotNull(symbolicName);
        Object result = stringToFontData.get(symbolicName);
        if (result == null) {
			return defaultFontData();
		}

        return (FontData[]) result;
    }

    /**
     * Returns the font associated with the given symbolic font name.
     * Returns the default font if there is no special value associated
     * with that name.
     * <p>
     * This method should only be called from the UI thread. If you are not on the UI
     * thread then wrap the call with a
     * <code>PlatformUI.getWorkbench().getDisplay().synchExec()</code> in order to
     * guarantee the correct result. Failure to do this may result in an {@link
     * SWTException} being thrown.
     * </p>
     * @param symbolicName symbolic font name
     * @return the font
     */
    public Font get(String symbolicName) {

        return getFontRecord(symbolicName).getBaseFont();
    }

    /**
     * Returns the bold font associated with the given symbolic font name.
     * Returns the bolded default font if there is no special value associated
     * with that name.
     * <p>
     * This method should only be called from the UI thread. If you are not on the UI
     * thread then wrap the call with a
     * <code>PlatformUI.getWorkbench().getDisplay().synchExec()</code> in order to
     * guarantee the correct result. Failure to do this may result in an {@link
     * SWTException} being thrown.
     * </p>
     * @param symbolicName symbolic font name
     * @return the font
     * @since 1.0
     */
    public Font getBold(String symbolicName) {

        return getFontRecord(symbolicName).getBoldFont();
    }

    /**
     * Returns the italic font associated with the given symbolic font name.
     * Returns the italic default font if there is no special value associated
     * with that name.
     * <p>
     * This method should only be called from the UI thread. If you are not on the UI
     * thread then wrap the call with a
     * <code>PlatformUI.getWorkbench().getDisplay().synchExec()</code> in order to
     * guarantee the correct result. Failure to do this may result in an {@link
     * SWTException} being thrown.
     * </p>
     * @param symbolicName symbolic font name
     * @return the font
     * @since 1.0
     */
    public Font getItalic(String symbolicName) {

        return getFontRecord(symbolicName).getItalicFont();
    }

    /**
     * Return the font record for the key.
     * @param symbolicName The key for the record.
     * @return FontRecord
     */
    private FontRecord getFontRecord(String symbolicName) {
        Assert.isNotNull(symbolicName);
        Object result = stringToFontRecord.get(symbolicName);
        if (result != null) {
			return (FontRecord) result;
		}

        result = stringToFontData.get(symbolicName);

        FontRecord fontRecord;

        if (result == null) {
			fontRecord = defaultFontRecord();
		} else {
			fontRecord = createFont(symbolicName, (FontData[]) result);
		}

        if (fontRecord == null) {
			fontRecord = defaultFontRecord();
			if (Display.getCurrent() == null) { // log error but don't throw an exception to preserve existing functionality
				String msg = "Unable to create font \"" + symbolicName + "\" in a non-UI thread. Using default font instead."; //$NON-NLS-1$ //$NON-NLS-2$
				Policy.logException(new SWTException(msg));
				return fontRecord; // don't add it to the cache; if later asked from UI thread, a proper font will be created
			}
		}

        stringToFontRecord.put(symbolicName, fontRecord);
        return fontRecord;

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.ResourceRegistry#getKeySet()
     */
    public Set getKeySet() {
        return Collections.unmodifiableSet(stringToFontData.keySet());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.ResourceRegistry#hasValueFor(java.lang.String)
     */
    public boolean hasValueFor(String fontKey) {
        return stringToFontData.containsKey(fontKey);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.ResourceRegistry#clearCaches()
     */
    protected void clearCaches() {

        Iterator iterator = stringToFontRecord.values().iterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            ((FontRecord) next).dispose();
        }

        disposeFonts(staleFonts.iterator());
        stringToFontRecord.clear();
        staleFonts.clear();
        
        displayDisposeHooked = false;
    }

    /**
     * Dispose of all of the fonts in this iterator.
     * @param iterator over Collection of Font
     */
    private void disposeFonts(Iterator iterator) {
        while (iterator.hasNext()) {
            Object next = iterator.next();
            ((Font) next).dispose();
        }
    }

    /**
     * Hook a dispose listener on the SWT display.
     */
    private void hookDisplayDispose(Display display) {
    	displayDisposeHooked = true;
    	display.disposeExec(displayRunnable);
    }
    
    // RAP [bm]: 
//    /**
//     * Checks whether the given font is in the list of fixed fonts.
//     */
//    private boolean isFixedFont(FontData[] fixedFonts, FontData fd) {
//        // Can't use FontData.equals() since some values aren't
//        // set if a fontdata isn't used.
//        int height = fd.getHeight();
//        String name = fd.getName();
//        for (int i = 0; i < fixedFonts.length; i++) {
//            FontData fixed = fixedFonts[i];
//            if (fixed.getHeight() == height && fixed.getName().equals(name)) {
//				return true;
//			}
//        }
//        return false;
//    }

    /**
     * Converts a String into a FontData object.
     */
    private FontData makeFontData(String value) throws MissingResourceException {
        try {
            return StringConverter.asFontData(value.trim());
        } catch (DataFormatException e) {
            throw new MissingResourceException(
                    "Wrong font data format. Value is: \"" + value + "\"", getClass().getName(), value); //$NON-NLS-2$//$NON-NLS-1$
        }
    }

    /**
     * Adds (or replaces) a font to this font registry under the given
     * symbolic name.
     * <p>
     * A property change event is reported whenever the mapping from
     * a symbolic name to a font changes. The source of the event is
     * this registry; the property name is the symbolic font name.
     * </p>
     *
     * @param symbolicName the symbolic font name
     * @param fontData an Array of FontData
     */
    public void put(String symbolicName, FontData[] fontData) {
        put(symbolicName, fontData, true);
    }

    /**
     * Adds (or replaces) a font to this font registry under the given
     * symbolic name.
     * <p>
     * A property change event is reported whenever the mapping from
     * a symbolic name to a font changes. The source of the event is
     * this registry; the property name is the symbolic font name.
     * </p>
     *
     * @param symbolicName the symbolic font name
     * @param fontData an Array of FontData
     * @param update - fire a font mapping changed if true. False
     * 	if this method is called from the get method as no setting
     *  has changed.
     */
    private void put(String symbolicName, FontData[] fontData, boolean update) {

        Assert.isNotNull(symbolicName);
        Assert.isNotNull(fontData);

        FontData[] existing = (FontData[]) stringToFontData.get(symbolicName);
        if (Arrays.equals(existing, fontData)) {
			return;
		}

        FontRecord oldFont = (FontRecord) stringToFontRecord
                .remove(symbolicName);
        stringToFontData.put(symbolicName, fontData);
        if (update) {
			fireMappingChanged(symbolicName, existing, fontData);
		}

        if (oldFont != null) {
			oldFont.addAllocatedFontsToStale(defaultFontRecord().getBaseFont());
		}
    }

    /**
     * Reads the resource bundle.  This puts FontData[] objects
     * in the mapping table.  These will lazily be turned into
     * real Font objects when requested.
     */
    private void readResourceBundle(ResourceBundle bundle, String bundleName)
            throws MissingResourceException {
        Enumeration keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            int pos = key.lastIndexOf('.');
            if (pos == -1) {
                stringToFontData.put(key, new FontData[] { makeFontData(bundle
                        .getString(key)) });
            } else {
                String name = key.substring(0, pos);
                int i = 0;
                try {
                    i = Integer.parseInt(key.substring(pos + 1));
                } catch (NumberFormatException e) {
                    //Panic the file can not be parsed.
                    throw new MissingResourceException(
                            "Wrong key format ", bundleName, key); //$NON-NLS-1$
                }
                FontData[] elements = (FontData[]) stringToFontData.get(name);
                if (elements == null) {
                    elements = new FontData[8];
                    stringToFontData.put(name, elements);
                }
                // RAP [rst] fix for bug 309357
                if (i >= elements.length) {
                    FontData[] na = new FontData[i + 8];
                    System.arraycopy(elements, 0, na, 0, elements.length);
                    elements = na;
                    stringToFontData.put(name, elements);
                }
                elements[i] = makeFontData(bundle.getString(key));
            }
        }
    }

	/**
	 * Returns the font descriptor for the JFace default font.
	 * 
	 * @return the font descriptor for the JFace default font
     * @since 1.0
	 */
	public FontDescriptor defaultFontDescriptor() {
		return FontDescriptor.createFrom(defaultFontData());
	}
}
