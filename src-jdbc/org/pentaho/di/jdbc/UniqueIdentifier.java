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


public class UniqueIdentifier {
    private final byte[] bytes;

    /**
     * Construct a unique identifier with the supplied byte array.
     */
    public UniqueIdentifier(byte[] id) {
        bytes = id;
    }

    /**
     * Retrieve the unique identifier as a byte array.
     *
     * @return The unique identifier as a <code>byte[]</code>.
     */
    public byte[] getBytes() {
        return (byte [])bytes.clone();
    }

    /**
     * Retrieve the unique identifier as a formatted string.
     * <p>Format is NNNNNNNN-NNNN-NNNN-NNNN-NNNNNNNNNNNN.
     *
     * @return The uniqueidentifier as a <code>String</code>.
     */
    public String toString() {
        byte[] tmp = bytes;

        if (bytes.length == 16) {
            // Need to swap some bytes for correct text version
            tmp = new byte[bytes.length];
            System.arraycopy(bytes, 0, tmp, 0, bytes.length);
            tmp[0] = bytes[3];
            tmp[1] = bytes[2];
            tmp[2] = bytes[1];
            tmp[3] = bytes[0];
            tmp[4] = bytes[5];
            tmp[5] = bytes[4];
            tmp[6] = bytes[7];
            tmp[7] = bytes[6];
        }

        byte bb[] = new byte[1];

        StringBuffer buf = new StringBuffer(36);

        for (int i = 0; i < bytes.length; i++) {
            bb[0] = tmp[i];
            buf.append(Support.toHex(bb));

            if (i == 3 || i == 5 || i == 7 || i == 9) {
                buf.append('-');
            }
        }

        return buf.toString();
    }
}
