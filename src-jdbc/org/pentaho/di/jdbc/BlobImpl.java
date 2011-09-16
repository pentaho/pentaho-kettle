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

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;




public class BlobImpl implements Blob {
    /**
     * 0 length <code>byte[]</code> as initial value for empty
     * <code>Blob</code>s.
     */
    private static final byte[] EMPTY_BLOB = new byte[0];

    /** The underlying <code>BlobBuffer</code>. */
    private final BlobBuffer blobBuffer;

    /**
     * Constructs a new empty <code>Blob</code> instance.
     *
     * @param connection a reference to the parent connection object
     */
    BlobImpl(ConnectionJDBC3 connection) {
        this(connection, EMPTY_BLOB);
    }

    /**
     * Constructs a new <code>Blob</code> instance initialized with data.
     *
     * @param connection a reference to the parent connection object
     * @param bytes      the blob object to encapsulate
     */
    BlobImpl(ConnectionJDBC3 connection, byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes cannot be null");
        }

        blobBuffer = new BlobBuffer(connection.getBufferDir(), connection.getLobBuffer());
        blobBuffer.setBuffer(bytes, false);
    }

    //
    // ------ java.sql.Blob interface methods from here -------
    //

    public InputStream getBinaryStream() throws SQLException {
        return blobBuffer.getBinaryStream(false);
    }

    public byte[] getBytes(long pos, int length) throws SQLException {
        return blobBuffer.getBytes(pos, length);
    }

    public long length() throws SQLException {
        return blobBuffer.getLength();
    }

    public long position(byte[] pattern, long start) throws SQLException {
        return blobBuffer.position(pattern, start);
    }

    public long position(Blob pattern, long start) throws SQLException {
        if (pattern == null) {
            throw new SQLException(Messages.get("error.blob.badpattern"), "HY009");
        }
        return blobBuffer.position(pattern.getBytes(1, (int) pattern.length()), start);
    }

    public OutputStream setBinaryStream(final long pos) throws SQLException {
        return blobBuffer.setBinaryStream(pos, false);
    }

    public int setBytes(long pos, byte[] bytes) throws SQLException {
        if (bytes == null) {
            throw new SQLException(Messages.get("error.blob.bytesnull"), "HY009");
        }
        return setBytes(pos, bytes, 0, bytes.length);
    }

    public int setBytes(long pos, byte[] bytes, int offset, int len)
            throws SQLException {
        if (bytes == null) {
            throw new SQLException(Messages.get("error.blob.bytesnull"), "HY009");
        }
        // Force BlobBuffer to take a copy of the byte array
        // In many cases this is wasteful but the user may
        // reuse the byte buffer corrupting the original set
        return blobBuffer.setBytes(pos, bytes, offset, len, true);
    }

    public void truncate(long len) throws SQLException {
        blobBuffer.truncate(len);
    }

    public void free() throws SQLException {
    }

    public InputStream getBinaryStream(long pos, long length) throws SQLException {
      return null;
    }
}
