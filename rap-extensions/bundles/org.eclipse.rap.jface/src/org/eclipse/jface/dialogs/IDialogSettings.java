/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.dialogs;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * An interface to a storage mechanism for making dialog settings persistent.
 * The store manages a collection of key/value pairs. The keys must be strings
 * and the values can be either, strings or array of strings. Convenience API to
 * convert primitive types to strings is provided.
 * 
 * @see DialogSettings
 */
public interface IDialogSettings {
    /**
     * Create a new section in the receiver and return it.
     * 
     * @param name
     *            the name of the new section
     * @return the new section
     * 
     * @see DialogSettings#getOrCreateSection(IDialogSettings, String)
     */
    public IDialogSettings addNewSection(String name);

    /**
     * Add a section in the receiver.
     * 
     * @param section
     *            the section to be added
     */
    public void addSection(IDialogSettings section);

    /**
     * Returns the value of the given key in this dialog settings.
     * 
     * @param key
     *            the key
     * @return the value, or <code>null</code> if none
     */
    public String get(String key);

    /**
     * Returns the value, an array of strings, of the given key in this dialog
     * settings.
     * 
     * @param key
     *            the key
     * @return the array of string, or <code>null</code> if none
     */
    public String[] getArray(String key);

    /**
     * Convenience API. Convert the value of the given key in this dialog
     * settings to a boolean and return it.
     * 
     * @param key
     *            the key
     * @return the boolean value, or <code>false</code> if none
     */
    public boolean getBoolean(String key);

    /**
     * Convenience API. Convert the value of the given key in this dialog
     * settings to a double and return it.
     * 
     * @param key
     *            the key
     * @return the value coverted to double, or throws
     *         <code>NumberFormatException</code> if none
     * 
     * @exception NumberFormatException
     *                if the string value does not contain a parsable number.
     * @see java.lang.Double#valueOf(java.lang.String)
     */
    public double getDouble(String key) throws NumberFormatException;

    /**
     * Convenience API. Convert the value of the given key in this dialog
     * settings to a float and return it.
     * 
     * @param key
     *            the key
     * @return the value coverted to float, or throws
     *         <code>NumberFormatException</code> if none
     * 
     * @exception NumberFormatException
     *                if the string value does not contain a parsable number.
     * @see java.lang.Float#valueOf(java.lang.String)
     */
    public float getFloat(String key) throws NumberFormatException;

    /**
     * Convenience API. Convert the value of the given key in this dialog
     * settings to a int and return it.
     * 
     * @param key
     *            the key
     * @return the value coverted to int, or throws
     *         <code>NumberFormatException</code> if none
     * 
     * @exception NumberFormatException
     *                if the string value does not contain a parsable number.
     * @see java.lang.Integer#valueOf(java.lang.String)
     */
    public int getInt(String key) throws NumberFormatException;

    /**
     * Convenience API. Convert the value of the given key in this dialog
     * settings to a long and return it.
     * 
     * @param key
     *            the key
     * @return the value coverted to long, or throws
     *         <code>NumberFormatException</code> if none
     * 
     * @exception NumberFormatException
     *                if the string value does not contain a parsable number.
     * @see java.lang.Long#valueOf(java.lang.String)
     */
    public long getLong(String key) throws NumberFormatException;

    /**
     * Returns the IDialogSettings name.
     * 
     * @return the name
     */
    public String getName();

    /**
     * Returns the section with the given name in this dialog settings.
     * 
     * @param sectionName
     *            the key
     * @return IDialogSettings (the section), or <code>null</code> if none
     * 
     * @see DialogSettings#getOrCreateSection(IDialogSettings, String)
     */
    public IDialogSettings getSection(String sectionName);

    /**
     * Returns all the sections in this dialog settings.
     * 
     * @return the section, or <code>null</code> if none
     */
    public IDialogSettings[] getSections();

    /**
     * Load a dialog settings from a stream and fill the receiver with its
     * content.
     * 
     * @param reader
     *            a Reader specifying the stream where the settings are read
     *            from.
     * @throws IOException
     */
    public void load(Reader reader) throws IOException;

    /**
     * Load a dialog settings from a file and fill the receiver with its
     * content.
     * 
     * @param fileName
     *            the name of the file the settings are read from.
     * @throws IOException
     */
    public void load(String fileName) throws IOException;

    /**
     * Adds the pair <code>key/value</code> to this dialog settings.
     * 
     * @param key
     *            the key.
     * @param value
     *            the value to be associated with the <code>key</code>
     */
    public void put(String key, String[] value);

    /**
     * Convenience API. Converts the double <code>value</code> to a string and
     * adds the pair <code>key/value</code> to this dialog settings.
     * 
     * @param key
     *            the key.
     * @param value
     *            the value to be associated with the <code>key</code>
     */
    public void put(String key, double value);

    /**
     * Convenience API. Converts the float <code>value</code> to a string and
     * adds the pair <code>key/value</code> to this dialog settings.
     * 
     * @param key
     *            the key.
     * @param value
     *            the value to be associated with the <code>key</code>
     */
    public void put(String key, float value);

    /**
     * Convenience API. Converts the int <code>value</code> to a string and
     * adds the pair <code>key/value</code> to this dialog settings.
     * 
     * @param key
     *            the key.
     * @param value
     *            the value to be associated with the <code>key</code>
     */
    public void put(String key, int value);

    /**
     * Convenience API. Converts the long <code>value</code> to a string and
     * adds the pair <code>key/value</code> to this dialog settings.
     * 
     * @param key
     *            the key.
     * @param value
     *            the value to be associated with the <code>key</code>
     */
    public void put(String key, long value);

    /**
     * Adds the pair <code>key/value</code> to this dialog settings.
     * 
     * @param key
     *            the key.
     * @param value
     *            the value to be associated with the <code>key</code>
     */
    public void put(String key, String value);

    /**
     * Convenience API. Converts the boolean <code>value</code> to a string
     * and adds the pair <code>key/value</code> to this dialog settings.
     * 
     * @param key
     *            the key.
     * @param value
     *            the value to be associated with the <code>key</code>
     */
    public void put(String key, boolean value);

    /**
     * Save a dialog settings to a stream
     * 
     * @param writer
     *            a Writer specifying the stream the settings are written in.
     * @throws IOException
     */
    public void save(Writer writer) throws IOException;

    /**
     * Save a dialog settings to a file.
     * 
     * @param fileName
     *            the name of the file the settings are written in.
     * @throws IOException
     */
    public void save(String fileName) throws IOException;
}
