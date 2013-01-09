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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

import org.pentaho.di.i18n.BaseMessages;



public class ClobImpl implements Clob {
  
    private static Class<?> PKG = KettleDriver.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    /**
     * 0 length <code>String</code> as initial value for empty
     * <code>Clob</code>s.
     */
    private static final String EMPTY_CLOB = "";

    /** The underlying <code>BlobBuffer</code>. */
    private final BlobBuffer blobBuffer;

    /**
     * Constructs a new empty <code>Clob</code> instance.
     *
     * @param connection a reference to the parent connection object
     */
    ClobImpl(ConnectionJDBC3 connection) {
        this(connection, EMPTY_CLOB);
    }

    /**
     * Constructs a new initialized <code>Clob</code> instance.
     *
     * @param connection a reference to the parent connection object
     * @param str        the <code>String</code> object to encapsulate
     */
    ClobImpl(ConnectionJDBC3 connection, String str) {
        if (str == null) {
            throw new IllegalArgumentException("str cannot be null");
        }
        blobBuffer = new BlobBuffer(connection.getBufferDir(), connection.getLobBuffer());
        try {
            byte[] data = str.getBytes("UTF-16LE");
            blobBuffer.setBuffer(data, false);
        } catch (UnsupportedEncodingException e) {
            // This should never happen!
            throw new IllegalStateException("UTF-16LE encoding is not supported.");
        }
    }

    /**
     * Obtain this object's backing <code>BlobBuffer</code> object.
     *
     * @return the underlying <code>BlobBuffer</code>
     */
    BlobBuffer getBlobBuffer() {
        return this.blobBuffer;
    }

    //
    // ---- java.sql.Blob interface methods from here ----
    //

    public InputStream getAsciiStream() throws SQLException {
        return blobBuffer.getBinaryStream(true);
    }

    public Reader getCharacterStream() throws SQLException {
        try {
            return new BufferedReader(new InputStreamReader(
                    blobBuffer.getBinaryStream(false), "UTF-16LE"));
        } catch (UnsupportedEncodingException e) {
            // This should never happen!
            throw new IllegalStateException(
                    "UTF-16LE encoding is not supported.");
        }
    }

    public String getSubString(long pos, int length) throws SQLException {
        if (length == 0) {
            return EMPTY_CLOB;
        }
        try {
            byte data[] = blobBuffer.getBytes((pos - 1) * 2 + 1, length * 2);
            return new String(data, "UTF-16LE");
        } catch (IOException e) {
            throw new SQLException(BaseMessages.getString(PKG, "error.generic.ioerror",
                    e.getMessage()),
                    "HY000");
        }
    }

    public long length() throws SQLException {
        return blobBuffer.getLength() / 2;
    }

    public long position(String searchStr, long start) throws SQLException {
        if (searchStr == null) {
            throw new SQLException(
                    BaseMessages.getString(PKG, "error.clob.searchnull"), "HY009");
        }
        try {
            byte[] pattern = searchStr.getBytes("UTF-16LE");
            int pos = blobBuffer.position(pattern, (start - 1) * 2 + 1);
            return (pos < 0) ? pos : (pos - 1) / 2 + 1;
        } catch (UnsupportedEncodingException e) {
            // This should never happen!
            throw new IllegalStateException(
                    "UTF-16LE encoding is not supported.");
        }
    }

    public long position(Clob searchStr, long start) throws SQLException {
        if (searchStr == null) {
            throw new SQLException(
                    BaseMessages.getString(PKG, "error.clob.searchnull"), "HY009");
        }
        BlobBuffer bbuf = ((ClobImpl) searchStr).getBlobBuffer();
        byte[] pattern = bbuf.getBytes(1, (int) bbuf.getLength());
        int pos = blobBuffer.position(pattern, (start - 1) * 2 + 1);
        return (pos < 0) ? pos : (pos - 1) / 2 + 1;
    }

    public OutputStream setAsciiStream(final long pos) throws SQLException {
        return blobBuffer.setBinaryStream((pos - 1) * 2 + 1, true);
    }

    public Writer setCharacterStream(final long pos) throws SQLException {
        try {
            return new BufferedWriter(new OutputStreamWriter(
                    blobBuffer.setBinaryStream((pos - 1) * 2 + 1, false),
                    "UTF-16LE"));
        } catch (UnsupportedEncodingException e) {
            // Should never happen
            throw new IllegalStateException("UTF-16LE encoding is not supported.");
        }
    }

    public int setString(long pos, String str) throws SQLException {
        if (str == null) {
            throw new SQLException(
                    BaseMessages.getString(PKG, "error.clob.strnull"), "HY009");
        }
        return setString(pos, str, 0, str.length());
    }

    public int setString(long pos, String str, int offset, int len)
            throws SQLException {
        if (offset < 0 || offset > str.length()) {
            throw new SQLException(BaseMessages.getString(PKG, 
                    "error.blobclob.badoffset"), "HY090");
        }
        if (len < 0 || offset + len > str.length()) {
            throw new SQLException(
                    BaseMessages.getString(PKG, "error.blobclob.badlen"), "HY090");
        }
        try {
            byte[] data = str.substring(offset, offset + len)
                    .getBytes("UTF-16LE");
            // No need to force BlobBuffer to copy the bytes as this is a local
            // buffer and cannot be corrupted by the user.
            return blobBuffer.setBytes(
                    (pos - 1) * 2 + 1, data, 0, data.length, false);
        } catch (UnsupportedEncodingException e) {
            // This should never happen!
            throw new IllegalStateException(
                    "UTF-16LE encoding is not supported.");
        }
    }

    public void truncate(long len) throws SQLException {
        blobBuffer.truncate(len * 2);
    }

    public void free() throws SQLException {
    }

    public Reader getCharacterStream(long arg0, long arg1) throws SQLException {
      return null;
    }
}
