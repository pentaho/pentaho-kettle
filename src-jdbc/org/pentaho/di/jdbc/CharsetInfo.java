/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/lgpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 Bayon Technologies, Inc.  All rights reserved. 
 * Copyright (C) 2004 The jTDS Project
 */
package org.pentaho.di.jdbc;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Enumeration;
import java.io.InputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.i18n.BaseMessages;




public final class CharsetInfo {
  
  private static Class<?> PKG = KettleDriver.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  
    //
    // Static fields and methods
    //
	private transient static final Log log = LogFactory.getLog(CharsetInfo.class);

    /** Name of the <code>Charsets.properties</code> resource. */
    private static final String CHARSETS_RESOURCE_NAME
            = "org/pentaho/di/jdbc/Charsets.properties";

    /** Server charset to Java charset map. */
    private static final HashMap<String, CharsetInfo> charsets = new HashMap<String, CharsetInfo>();

    /** Locale id to Java charset map. */
    private static final HashMap<Integer, CharsetInfo> lcidToCharsetMap = new HashMap<Integer, CharsetInfo>();

    /** Sort order to Java charset map. */
    private static final CharsetInfo[] sortToCharsetMap = new CharsetInfo[256];

    static {
        // Load character set mappings
        try {
            InputStream stream = null;
            // getContextClassLoader needed to ensure driver
            // works with Tomcat class loading rules.
            ClassLoader classLoader =
                    Thread.currentThread().getContextClassLoader();

            if (classLoader != null) {
                stream = classLoader.getResourceAsStream(CHARSETS_RESOURCE_NAME);
            }

            if (stream == null) {
                // The doPrivileged() call stops the SecurityManager from
                // checking further in the stack trace whether all callers have
                // the permission to load Charsets.properties
                stream = (InputStream) java.security.AccessController.doPrivileged(
                        new java.security.PrivilegedAction<Object>() {
                            public Object run() {
                                ClassLoader loader = CharsetInfo.class.getClassLoader();
                                // getClassLoader() may return null if the class was loaded by
                                // the bootstrap ClassLoader
                                if (loader == null) {
                                    loader = ClassLoader.getSystemClassLoader();
                                }

                                return loader.getResourceAsStream(CHARSETS_RESOURCE_NAME);
                            }
                        }
                );
            }

            if (stream != null) {
                Properties tmp = new Properties();
                tmp.load(stream);

                HashMap<String, CharsetInfo> instances = new HashMap<String, CharsetInfo>();

                for (Enumeration<?> e = tmp.propertyNames(); e.hasMoreElements();) {
                    String key = (String) e.nextElement();
                    CharsetInfo value = new CharsetInfo(tmp.getProperty(key));

                    // Ensure only one CharsetInfo instance exists per charset
                    CharsetInfo prevInstance = (CharsetInfo) instances.get(
                            value.getCharset());
                    if (prevInstance != null) {
                        if (prevInstance.isWideChars() != value.isWideChars()) {
                            throw new IllegalStateException(
                                    "Inconsistent Charsets.properties");
                        }
                        value = prevInstance;
                    }

                    if (key.startsWith("LCID_")) {
                        Integer lcid = new Integer(key.substring(5));
                        lcidToCharsetMap.put(lcid, value);
                    } else if (key.startsWith("SORT_")) {
                        sortToCharsetMap[Integer.parseInt(key.substring(5))] = value;
                    } else {
                        charsets.put(key, value);
                    }
                }
            } else {
                log.error("Can't load Charsets.properties");
            }
        } catch (IOException e) {
            // Can't load properties file for some reason
        	log.error(e.getMessage());
        }
    }

    /**
     * Retrieves the <code>CharsetInfo</code> instance asociated with the
     * specified server charset.
     *
     * @param serverCharset the server-specific character set name
     * @return the associated <code>CharsetInfo</code>
     */
    public static CharsetInfo getCharset(String serverCharset) {
        return (CharsetInfo) charsets.get(serverCharset.toUpperCase());
    }

    /**
     * Retrieves the <code>CharsetInfo</code> instance asociated with the
     * specified LCID.
     *
     * @param lcid the server LCID
     * @return the associated <code>CharsetInfo</code>
     */
    public static CharsetInfo getCharsetForLCID(int lcid) {
        return (CharsetInfo) lcidToCharsetMap.get(new Integer(lcid));
    }

    /**
     * Retrieves the <code>CharsetInfo</code> instance asociated with the
     * specified sort order.
     *
     * @param sortOrder the server sort order
     * @return the associated <code>CharsetInfo</code>
     */
    public static CharsetInfo getCharsetForSortOrder(int sortOrder) {
        return sortToCharsetMap[sortOrder];
    }

    /**
     * Retrieves the <code>CharsetInfo</code> instance asociated with the
     * specified collation.
     *
     * @param collation the server LCID
     * @return the associated <code>CharsetInfo</code>
     */
    public static CharsetInfo getCharset(byte[] collation)
            throws SQLException {
        CharsetInfo charset;

        if (collation[4] != 0) {
            // The charset is determined by the sort order
            charset = getCharsetForSortOrder((int) collation[4] & 0xFF);
        } else {
            // The charset is determined by the LCID
            charset = getCharsetForLCID(
                    ((int) collation[2] & 0x0F) << 16
                    | ((int) collation[1] & 0xFF) << 8
                    | ((int) collation[0] & 0xFF));
        }

        if (charset == null) {
            throw new SQLException(
                    BaseMessages.getString(PKG, "error.charset.nocollation", Support.toHex(collation)),
                    "2C000");
        }

        return charset;
    }

    //
    // Non-static fields and methods
    //

    /** The Java character set name. */
    private final String charset;
    /** Indicates whether current charset is wide (ie multi-byte). */
    private final boolean wideChars;

    /**
     * Constructs a <code>CharsetInfo</code> object from a character set
     * descriptor of the form: charset preceded by a numeric value indicating
     * whether it's a multibyte character set (&gt;1) or not (1) and a vertical
     * bar (|), eg &quot;1|Cp1252&quot; or &quot;2|MS936&quot;.
     *
     * @param descriptor the charset descriptor
     */
    public CharsetInfo(String descriptor) {
        wideChars = !"1".equals(descriptor.substring(0, 1));
        charset = descriptor.substring(2);
    }

    

	/**
     * Retrieves the charset name.
     */
    public String getCharset() {
        return charset;
    }

    /**
     * Retrieves whether the caracter set is wide (ie multi-byte).
     */
    public boolean isWideChars() {
        return wideChars;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CharsetInfo)) {
            return false;
        }

        final CharsetInfo charsetInfo = (CharsetInfo) o;
        if (!charset.equals(charsetInfo.charset)) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return charset.hashCode();
    }

    public String toString() {
        return charset;
    }
}
