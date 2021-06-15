/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.internal.image;


import java.io.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

/**
 * Abstract factory class for loading/unloading images from files or streams
 * in various image file formats.
 *
 */
public abstract class FileFormat {
	static final String FORMAT_PACKAGE = "org.eclipse.swt.internal.image"; //$NON-NLS-1$
	static final String FORMAT_SUFFIX = "FileFormat"; //$NON-NLS-1$
	static final String[] FORMATS = {"WinBMP", "WinBMP", "GIF", "WinICO", "JPEG", "PNG", "TIFF", "OS2BMP"}; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$//$NON-NLS-5$ //$NON-NLS-6$//$NON-NLS-7$//$NON-NLS-8$
	
	LEDataInputStream inputStream;
	LEDataOutputStream outputStream;
	ImageLoader loader;
	int compression;

static FileFormat getFileFormat (LEDataInputStream stream, String format) throws Exception {
	Class<?> clazz = Class.forName(FORMAT_PACKAGE + '.' + format + FORMAT_SUFFIX);
	FileFormat fileFormat = (FileFormat) clazz.newInstance();
	if (fileFormat.isFileFormat(stream)) return fileFormat;
	return null;
}

/**
 * Return whether or not the specified input stream
 * represents a supported file format.
 */
abstract boolean isFileFormat(LEDataInputStream stream);

abstract ImageData[] loadFromByteStream();

/**
 * Read the specified input stream, and return the
 * device independent image array represented by the stream.
 */	
public ImageData[] loadFromStream(LEDataInputStream stream) {
	try {
		inputStream = stream;
		return loadFromByteStream();
	} catch (Exception e) {
		if (e instanceof IOException) {
			SWT.error(SWT.ERROR_IO, e);
		} else {
			SWT.error(SWT.ERROR_INVALID_IMAGE, e);
		}
		return null;
	}
}

/**
 * Read the specified input stream using the specified loader, and
 * return the device independent image array represented by the stream.
 */	
public static ImageData[] load(InputStream is, ImageLoader loader) {
	FileFormat fileFormat = null;
	LEDataInputStream stream = new LEDataInputStream(is);
	for (int i = 1; i < FORMATS.length; i++) {
		if (FORMATS[i] != null) {
			try {
				fileFormat = getFileFormat (stream, FORMATS[i]);
				if (fileFormat != null) break;
			} catch (ClassNotFoundException e) {
				FORMATS[i] = null;
			} catch (Exception e) {
			}
		}
	}
	if (fileFormat == null) SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT);
	fileFormat.loader = loader;
	return fileFormat.loadFromStream(stream);
}

/**
 * Write the device independent image array stored in the specified loader
 * to the specified output stream using the specified file format.
 */	
public static void save(OutputStream os, int format, ImageLoader loader) {
	if (format < 0 || format >= FORMATS.length) SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT);
	if (FORMATS[format] == null) SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT);
	if (loader.data == null || loader.data.length < 1) SWT.error(SWT.ERROR_INVALID_ARGUMENT);

	LEDataOutputStream stream = new LEDataOutputStream(os);
	FileFormat fileFormat = null;
	try {
		Class<?> clazz = Class.forName(FORMAT_PACKAGE + '.' + FORMATS[format] + FORMAT_SUFFIX);
		fileFormat = (FileFormat) clazz.newInstance();
	} catch (Exception e) {
		SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT);
	}
	if (format == SWT.IMAGE_BMP_RLE) {
		switch (loader.data[0].depth) {
			case 8: fileFormat.compression = 1; break;
			case 4: fileFormat.compression = 2; break;
		}
	}
	fileFormat.unloadIntoStream(loader, stream);
}

abstract void unloadIntoByteStream(ImageLoader loader);

/**
 * Write the device independent image array stored in the specified loader
 * to the specified output stream.
 */	
public void unloadIntoStream(ImageLoader loader, LEDataOutputStream stream) {
	try {
		outputStream = stream;
		unloadIntoByteStream(loader);
		outputStream.flush();
	} catch (Exception e) {
		try {outputStream.flush();} catch (Exception f) {}
		SWT.error(SWT.ERROR_IO, e);
	}
}
}
