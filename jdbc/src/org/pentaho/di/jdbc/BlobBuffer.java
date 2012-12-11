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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.i18n.BaseMessages;




public class BlobBuffer {

  private static Class<?> PKG = KettleDriver.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
  
	private transient static final Log log = LogFactory.getLog(BlobBuffer.class);

    /**
     * Default zero length buffer.
     */
    private static final byte[] EMPTY_BUFFER = new byte[0];
    /**
     * Default page size (must be power of 2).
     */
    private static final int PAGE_SIZE = 1024;
    /**
     * Mask for page component of read/write pointer.
     */
    private static final int PAGE_MASK = 0xFFFFFC00;
    /**
     * Mask for page offset component of R/W pointer.
     */
    private static final int BYTE_MASK = 0x000003FF;
    /**
     * Maximum buffer increment.
     */
    private static final int MAX_BUF_INC = 16384;
    /**
     * Invalid page marker.
     */
    private static final int INVALID_PAGE = -1;

    /**
     * The BLOB buffer or the current page buffer.
     */
    private byte[] buffer;
    /**
     * The total length of the valid data in buffer.
     */
    private int length;
    /**
     * The number of the current page in memory.
     */
    private int currentPage;
    /**
     * The name of the temporary BLOB disk file.
     */
    private File blobFile;
    /**
     * The RA file object reference or null if closed.
     */
    private RandomAccessFile raFile;
    /**
     * Indicates page in memory must be saved.
     */
    private boolean bufferDirty;
    /**
     * Count of callers that have opened the BLOB file.
     */
    private int openCount;
    /**
     * True if attempts to create a BLOB file have failed.
     */
    private boolean isMemOnly;
    /**
     * The directory to buffer data to.
     */
    private final File bufferDir;
    /**
     * The maximum size of an in memory buffer.
     */
    private final int maxMemSize;

    /**
     * Creates a blob buffer.
     *
     * @param bufferDir
     * @param maxMemSize the maximum size of the in memory buffer
     */
    public BlobBuffer(File bufferDir, long maxMemSize) {
    	this.bufferDir = bufferDir;
        this.maxMemSize = (int) maxMemSize;
        buffer = EMPTY_BUFFER;
    }

    /**
     * Finalizes this object by deleting any work files.
     */
    protected void finalize() throws Throwable {
        try {
            if (raFile != null) {
                raFile.close();
            }
        } catch (IOException e) {
            // Ignore we are going to delete anyway
        } finally {
            if (blobFile != null) {
                blobFile.delete();
            }
        }
    }

    /**
     * Creates a random access disk file to use as backing storage for the LOB
     * data.
     * <p/>
     * This method may fail due to security exceptions or local disk problems,
     * in which case the blob storage will remain entirely in memory.
     */
    public void createBlobFile() {
        try {
            blobFile = File.createTempFile("jtds", ".tmp", bufferDir);
            blobFile.deleteOnExit();
            raFile = new RandomAccessFile(blobFile, "rw");
            if (length > 0) {
                raFile.write(buffer, 0, (int) length);
            }
            buffer = new byte[PAGE_SIZE];
            currentPage = INVALID_PAGE;
            openCount = 0;
        } catch (SecurityException e) {
            blobFile = null;
            raFile = null;
            isMemOnly = true;
            
            log.error(e);
        } catch (IOException ioe) {
            blobFile = null;
            raFile = null;
            isMemOnly = true;
            
            log.error(ioe);
        }
    }

    /**
     * Opens the BLOB disk file.
     * <p/>
     * A count of open and close requests is kept so that the file may be
     * closed when no longer required thus keeping the number of open files to
     * a minimum.
     *
     * @throws IOException if an I/O error occurs
     */
    public void open() throws IOException {
        if (raFile == null && blobFile != null) {
            // reopen file
            raFile = new RandomAccessFile(blobFile, "rw");
            openCount = 1;
            currentPage = INVALID_PAGE;
            buffer = new byte[PAGE_SIZE];
            return;
        }
        if (raFile != null) {
            openCount++;
        }
    }

    /**
     * Reads byte from the BLOB buffer at the specified location.
     * <p/>
     * The read pointer is partitioned into a page number and an offset within
     * the page. This routine will read new pages as required. The page size
     * must be a power of 2 and is currently set to 1024 bytes.
     *
     * @param readPtr the offset in the buffer of the required byte
     * @return the byte value as an <code>int</code> or -1 if at EOF
     * @throws IOException if an I/O error occurs
     */
    public int read(int readPtr) throws IOException {
        if (readPtr >= length) {
            // At end of file.
            return -1;
        }
        if (raFile != null) {
            // Paged storage as a file exists
            if (currentPage != (readPtr & PAGE_MASK)) {
                // Requested page not in memory so read it
                readPage(readPtr);
            }
            // Use the byte offset to return the correct
            // byte from the page.
            return buffer[readPtr & BYTE_MASK] & 0xFF;
        } else {
            // In memory buffer just return byte.
            return buffer[readPtr] & 0xFF;
        }
    }

    /**
     * Reads bytes from the BLOB buffer at the specified location.
     *
     * @param readPtr the offset in the buffer of the required byte
     * @param bytes   the byte array to fill
     * @param offset  the start position in the byte array
     * @param len     the number of bytes to read
     * @return the number of bytes read or -1 if at end of file
     * @throws IOException if an I/O error occurs
     */
    public int read(int readPtr, byte[] bytes, int offset, int len)
            throws IOException {
        // Validate parameters
        if (bytes == null) {
            throw new NullPointerException();
        } else if ((offset < 0) || (offset > bytes.length) || (len < 0)
                || ((offset + len) > bytes.length) || ((offset + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        if (readPtr >= length) {
            // At end of file
            return -1;
        }

        if (raFile != null) {
            // Need to read from disk file
            len = Math.min(length - readPtr, len);
            if (len >= PAGE_SIZE) {
                // This is a big write so we optimize by reading directly
                // from the RA File.
                if (bufferDirty) {
                    writePage(currentPage);
                }
                currentPage = INVALID_PAGE;
                raFile.seek(readPtr);
                raFile.readFully(bytes, offset, len);
            } else {
                //
                // Partial read so buffer locally
                //
                int count = len;
                while (count > 0) {
                    if (currentPage != (readPtr & PAGE_MASK)) {
                        // Requested page not in memory so read it
                        readPage(readPtr);
                    }
                    int inBuffer = Math.min(PAGE_SIZE - (readPtr & BYTE_MASK), count);
                    System.arraycopy(buffer, readPtr & BYTE_MASK, bytes, offset, inBuffer);
                    offset += inBuffer;
                    readPtr += inBuffer;
                    count -= inBuffer;
                }
            }
        } else {
            // In memory buffer
            len = Math.min(length - readPtr, len);
            System.arraycopy(buffer, readPtr, bytes, offset, len);
        }

        return len;
    }

    /**
     * Inserts a byte into the buffer at the specified location.
     * <p/>
     * The write pointer is partitioned into a page number and an offset within
     * the page. This routine will write new pages as required. The page size
     * must be a power of 2 and is currently set to 1024 bytes.
     *
     * @param writePtr the offset in the buffer of the required byte
     * @param b        the byte value to write
     * @throws IOException if an I/O error occurs
     */
    public void write(int writePtr, int b) throws IOException {
        if (writePtr >= length) {
            if (writePtr > length) {
                // Probably because the user called truncate at
                // the same time as writing to the blob!
                throw new IOException("BLOB buffer has been truncated");
            }
            // We are writing beyond the current length
            // of the buffer and need to update the total length.
            if (++length < 0) {
                // We have wrapped 31 bits!
                // This should ensure that the disk file is limited to 2GB.
                // If in memory JVM will probably have failed by now anyway.
                throw new IOException("BLOB may not exceed 2GB in size");
            }
        }

        if (raFile != null) {
            // OK we have a disk based buffer
            if (currentPage != (writePtr & PAGE_MASK)) {
                // The page we need is not in memory
                readPage(writePtr);
            }
            buffer[writePtr & BYTE_MASK] = (byte) b;
            // Ensure change will saved if buffer is replaced
            bufferDirty = true;
        } else {
            // In memory buffer only (only used here if disk unavailable
            if (writePtr >= buffer.length) {
                growBuffer(writePtr + 1);
            }
            buffer[writePtr] = (byte) b;
        }
    }

    /**
     * Inserts bytes into the buffer at the specified location.
     *
     * @param writePtr the offset in the buffer of the required byte
     * @param bytes    the byte array value to write
     * @param offset   the start position in the byte array
     * @param len      the number of bytes to write
     * @throws IOException if an I/O error occurs
     */
    void write(int writePtr, byte[] bytes, int offset, int len)
            throws IOException {
        // Validate parameters
        if (bytes == null) {
            throw new NullPointerException();
        } else if ((offset < 0) || (offset > bytes.length) || (len < 0) ||
                ((offset + len) > bytes.length) || ((offset + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        if ((long) writePtr + len > (long) Integer.MAX_VALUE) {
            throw new IOException("BLOB may not exceed 2GB in size");
        }
        if (writePtr > length) {
            // Probably because the user called truncate at
            // the same time as writing to the blob!
            throw new IOException("BLOB buffer has been truncated");
        }

        if (raFile != null) {
            // dealing with disk storage (normal case)
            //
            if (len >= PAGE_SIZE) {
                // This is a big write so we optimize by writing directly
                // to the RA File.
                if (bufferDirty) {
                    writePage(currentPage);
                }
                currentPage = INVALID_PAGE;
                raFile.seek(writePtr);
                raFile.write(bytes, offset, len);
                writePtr += len;
            } else {
                // Small writes so use the page buffer for
                // effeciency.
                int count = len;
                while (count > 0) {
                    // Paged storage as a file exists
                    if (currentPage != (writePtr & PAGE_MASK)) {
                        // Requested page not in memory so read it
                        readPage(writePtr);
                    }
                    int inBuffer = Math.min(
                            PAGE_SIZE - (writePtr & BYTE_MASK), count);
                    System.arraycopy(bytes, offset, buffer,
                            writePtr & BYTE_MASK, inBuffer);
                    bufferDirty = true;
                    offset += inBuffer;
                    writePtr += inBuffer;
                    count -= inBuffer;
                }
            }
        } else {
            // In memory (only used here if disk not available)
            if (writePtr + len > buffer.length) {
                growBuffer(writePtr + len);
            }
            System.arraycopy(bytes, offset, buffer, writePtr, len);
            writePtr += len;
        }
        if (writePtr > length) {
            length = writePtr;
        }
    }

    /**
     * Reads in the specified page from the disk buffer.
     * <p/>
     * Any existing dirty page is first saved to disk.
     *
     * @param page the page number
     * @throws IOException if an I/O error occurs
     */
    public void readPage(int page) throws IOException {
        page = page & PAGE_MASK;
        if (bufferDirty) {
            writePage(currentPage);
        }
        if (page > raFile.length()) {
            throw new IOException("readPage: Invalid page number " + page);
        }
        currentPage = page;
        // Locate and read requested page
        // NB. Page may not be completely filled.
        raFile.seek(currentPage);
        // Repeat reading until buffer is filled or EOF is reached
        int count = 0, res;
        do {
            res = raFile.read(buffer, count, buffer.length - count);
            count += (res == -1) ? 0 : res;
        } while (count < PAGE_SIZE && res != -1);
    }

    /**
     * Writes the specified page to the disk buffer.
     *
     * @param page the page number
     * @throws IOException if an I/O error occurs
     */
    public void writePage(int page) throws IOException {
        page = page & PAGE_MASK;
        if (page > raFile.length()) {
            throw new IOException("writePage: Invalid page number " + page);
        }
        if (buffer.length != PAGE_SIZE) {
            throw new IllegalStateException("writePage: buffer size invalid");
        }
        raFile.seek(page);
        raFile.write(buffer);
        bufferDirty = false;
    }

    /**
     * Logically closes the file or physically close it if the open count is
     * now zero.
     * <p/>
     * Any updated buffer in memory is flushed to disk before the file is
     * closed.
     *
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException {
        if (openCount > 0) {
            if (--openCount == 0 && raFile != null) {
                if (bufferDirty) {
                    writePage(currentPage);
                }
                raFile.close();
                raFile = null;
                // Allow buffer to be garbage collected
                buffer = EMPTY_BUFFER;
                currentPage = INVALID_PAGE;
            }
        }
    }

    /**
     * Increases the size of the in memory buffer for situations where disk
     * storage of BLOB is not possible.
     *
     * @param minSize the minimum size of buffer required
     */
    public void growBuffer(int minSize) {
        if (buffer.length == 0) {
            // Assign initial buffer
            buffer = new byte[Math.max(PAGE_SIZE, minSize)];
        } else {
            byte[] tmp;
            if (buffer.length * 2 > minSize && buffer.length <= MAX_BUF_INC) {
                tmp = new byte[buffer.length * 2];
            } else {
                tmp = new byte[minSize + MAX_BUF_INC];
            }
            // Copy over existing data
            System.arraycopy(buffer, 0, tmp, 0, buffer.length);
            buffer = tmp; // Assign new buffer.
        }
    }

    /**
     * Sets the initial buffer to an existing byte array.
     *
     * @param bytes the byte array containing the BLOB data
     * @param copy  true if a local copy of the data is required
     */
    public void setBuffer(byte[] bytes, boolean copy) {
        if (copy) {
            this.buffer = new byte[bytes.length];
            System.arraycopy(bytes, 0, this.buffer, 0, buffer.length);
        } else {
            this.buffer = bytes;
        }
        this.length = buffer.length;
    }

    //
    // ---- Inner classes implementing the various input/output stream classes ---
    //

    /**
     * An <code>InputStream</code> over the BLOB buffer.
     */
    private class BlobInputStream extends InputStream {
        private int readPtr;
        private boolean open;

        /**
         * Costructs an <code>InputStream</code> object over the BLOB buffer.
         *
         * @param pos  the starting position (from 0)
         * @throws IOException if an I/O error occurs
         */
        public BlobInputStream(long pos) throws IOException {
            BlobBuffer.this.open();
            open = true;
            readPtr = (int) pos;
        }

        /**
         * Ensures underlying BLOB file can be closed even if user does not
         * close this stream.
         */
        protected void finalize() throws Throwable {
            if (open) {
                try {
                    close();
                } catch (IOException e) {
                    // Ignore closing anyway
                } finally {
                    super.finalize();
                }
            }
        }

        /**
         * Returns the number of bytes available to read.
         *
         * @throws IOException if an I/O error occurs
         */
        public int available() throws IOException {
            return (int) BlobBuffer.this.getLength() - readPtr;
        }

        /**
         * Reads the next byte from the stream.
         *
         * @return the next byte as an <code>int</code> or -1 if at EOF
         * @throws IOException if an I/O error occurs
         */
        public int read() throws IOException {
            int b = BlobBuffer.this.read(readPtr);
            if (b >= 0) {
                readPtr++;
            }
            return b;
        }

        /**
         * Reads a bytes from the stream.
         *
         * @param bytes  the byte array to fill
         * @param offset the start position in the byte array
         * @param len    the number of bytes to read
         * @return the number of bytes read or -1 if at end of file
         * @throws IOException if an I/O error occurs
         */
        public int read(byte[] bytes, int offset, int len) throws IOException {
            int b = BlobBuffer.this.read(readPtr, bytes, offset, len);
            if (b > 0) {
                readPtr += b;
            }
            return b;
        }

        /**
         * Closes the output stream.
         *
         * @throws IOException if an I/O error occurs
         */
        public void close() throws IOException {
            if (open) {
                BlobBuffer.this.close();
                open = false;
            }
        }
    }

    /**
     * A Big Endian Unicode <code>InputStream</code> over the CLOB buffer.
     */
    private class UnicodeInputStream extends InputStream {
        private int readPtr;
        private boolean open;

        /**
         * Costructs an InputStream object over the BLOB buffer.
         *
         * @param pos  the starting position (from 0)
         * @throws IOException if an I/O error occurs
         */
        public UnicodeInputStream(long pos) throws IOException {
            BlobBuffer.this.open();
            open = true;
            readPtr = (int) pos;
        }

        /**
         * Ensures underlying BLOB file can be closed even if user does not
         * close this stream.
         */
        protected void finalize() throws Throwable {
            if (open) {
                try {
                    close();
                } catch (IOException e) {
                    // Ignore closing anyway
                } finally {
                    super.finalize();
                }
            }
        }

        /**
         * Returns the number of bytes available to read.
         *
         * @throws IOException if an I/O error occurs
         */
        public int available() throws IOException {
            return (int) BlobBuffer.this.getLength() - readPtr;
        }

        /**
         * Reads the next byte from the stream.
         *
         * @return the next byte as an <code>int</code> or -1 if at EOF
         * @throws IOException if an I/O error occurs
         */
        public int read() throws IOException {
            //
            // The XOR of 1 with the readPtr forces the bytes to be returned
            // in big endian order.
            //
            int b = BlobBuffer.this.read(readPtr ^ 1);
            if (b >= 0) {
                readPtr++;
            }
            return b;
        }

        /**
         * Close the output stream.
         *
         * @throws IOException if an I/O error occurs
         */
        public void close() throws IOException {
            if (open) {
                BlobBuffer.this.close();
                open = false;
            }
        }
    }

    /**
     * An ASCII <code>InputStream</code> over the CLOB buffer.
     * <p/>
     * This class interprets ASCII as anything which has a value below 0x80.
     * This is more rigid than other drivers which allow any character below
     * 0x100 to be converted to returned. The more relaxed coding is useful
     * when dealing with most single byte character sets and if this behaviour
     * is desired, comment out the line indicated in the read method.
     */
    private class AsciiInputStream extends InputStream {
        private int readPtr;
        private boolean open;

        /**
         * Costructs an InputStream object over the BLOB buffer.
         *
         * @param pos  the starting position (from 0)
         * @throws IOException if an I/O error occurs
         */
        public AsciiInputStream(long pos) throws IOException {
            BlobBuffer.this.open();
            open = true;
            readPtr = (int) pos;
        }

        /**
         * Ensures underlying BLOB file can be closed even if user does not
         * close this stream.
         */
        protected void finalize() throws Throwable {
            if (open) {
                try {
                    close();
                } catch (IOException e) {
                    // Ignore closing anyway
                } finally {
                    super.finalize();
                }
            }
        }

        /**
         * Returns the number of bytes available to read.
         *
         * @throws IOException if an I/O error occurs
         */
        public int available() throws IOException {
            return ((int) BlobBuffer.this.getLength() - readPtr) / 2;
        }

        /**
         * Read the next byte from the stream.
         *
         * @return the next byte as an <code>int</code> or -1 if at EOF
         * @throws IOException if an I/O error occurs
         */
        public int read() throws IOException {
            int b1 = BlobBuffer.this.read(readPtr);
            if (b1 >= 0) {
                readPtr++;
                int b2 = BlobBuffer.this.read(readPtr);
                if (b2 >= 0) {
                    readPtr++;
                    if (b2 != 0
                            || b1 > 0x7F // Comment out this line for a more
                    // permissive interpretation of 'ASCII'.
                    ) {
                        b1 = '?'; // Not ASCII set to '?'
                    }
                    return b1;
                }
            }
            return -1;
        }

        /**
         * Closes the output stream.
         *
         * @throws IOException if an I/O error occurs
         */
        public void close() throws IOException {
            if (open) {
                BlobBuffer.this.close();
                open = false;
            }
        }
    }

    /**
     * Implements an <code>OutputStream</code> for BLOB data.
     */
    private class BlobOutputStream extends OutputStream {
        private int writePtr;
        private boolean open;

        /**
         * Costructs an OutputStream object over the BLOB buffer.
         *
         * @param pos  the starting position (from 0)
         * @throws IOException if an I/O error occurs
         */
        BlobOutputStream(long pos) throws IOException {
            BlobBuffer.this.open();
            open = true;
            writePtr = (int) pos;
        }

        /**
         * Ensures underlying BLOB file can be closed even if user does not
         * close this stream.
         */
        protected void finalize() throws Throwable {
            if (open) {
                try {
                    close();
                } catch (IOException e) {
                    // Ignore closing anyway
                } finally {
                    super.finalize();
                }
            }
        }

        /**
         * Write a byte to the BLOB buffer.
         *
         * @param b the byte value to write
         * @throws IOException if an I/O error occurs
         */
        public void write(int b) throws IOException {
            BlobBuffer.this.write(writePtr++, b);
        }

        /**
         * Write bytes to the BLOB buffer.
         *
         * @param bytes  the byte array value to write
         * @param offset the start position in the byte array
         * @param len    the number of bytes to write
         * @throws IOException if an I/O error occurs
         */
        public void write(byte[] bytes, int offset, int len) throws IOException {
            BlobBuffer.this.write(writePtr, bytes, offset, len);
            writePtr += len;
        }

        /**
         * Close the output stream.
         *
         * @throws IOException if an I/O error occurs
         */
        public void close() throws IOException {
            if (open) {
                BlobBuffer.this.close();
                open = false;
            }
        }
    }

    /**
     * Implements an ASCII <code>OutputStream</code> for CLOB data.
     */
    private class AsciiOutputStream extends OutputStream {
        private int writePtr;
        private boolean open;

        /**
         * Costructs an ASCII <code>OutputStream</code> object over the BLOB
         * buffer.
         *
         * @param pos  the starting position (from 0)
         * @throws IOException if an I/O error occurs
         */
        AsciiOutputStream(long pos) throws IOException {
            BlobBuffer.this.open();
            open = true;
            writePtr = (int) pos;
        }

        /**
         * Ensures underlying BLOB file can be closed even if user does not
         * close this stream.
         */
        protected void finalize() throws Throwable {
            if (open) {
                try {
                    close();
                } catch (IOException e) {
                    // Ignore closing anyway
                } finally {
                    super.finalize();
                }
            }
        }

        /**
         * Writes a byte to the BLOB buffer.
         *
         * @param b the byte value to write
         * @throws IOException if an I/O error occurs
         */
        public void write(int b) throws IOException {
            BlobBuffer.this.write(writePtr++, b);
            BlobBuffer.this.write(writePtr++, 0);
        }

        /**
         * Closes the output stream.
         *
         * @throws IOException if an I/O error occurs
         */
        public void close() throws IOException {
            if (open) {
                BlobBuffer.this.close();
                open = false;
            }
        }
    }

    //
    // ---- Support methods for CLOB/BLOB ----
    //

    /**
     * Returns the BLOB data as a byte array.
     *
     * @param pos the start position in the BLOB buffer (from 1)
     * @param len the number of bytes to copy
     * @return the requested data as a <code>byte[]</code>
     */
    public byte[] getBytes(long pos, int len) throws SQLException {
        pos--;
        if (pos < 0) {
            throw new SQLException(BaseMessages.getString(PKG, "error.blobclob.badpos"), "HY090");
        }
        if (pos > this.length) {
            throw new SQLException(BaseMessages.getString(PKG, "error.blobclob.badposlen"), "HY090");
        }
        if (len < 0) {
            throw new SQLException(BaseMessages.getString(PKG, "error.blobclob.badlen"), "HY090");
        }
        if (pos + len > this.length) {
            // Don't throw an exception, just return as much data as available
            len = (int) (this.length - pos);
        }
        try {
            // Should not do this. It could cause trouble.
//            if (pos == 0 && len == buffer.length && blobFile == null) {
//                // There is no file and we do not need a subset of the data.
//                // We should copy the buffer as the user may modify its
//                // contents but this would be wasteful in most cases.
//                return buffer;
//            }
            // We do need a subset or we are reading from the file
            byte[] data = new byte[len];
            if (blobFile == null) {
                // Just copy subset from memory buffer
                System.arraycopy(buffer, (int) (pos), data, 0, len);
            } else {
                // Copy data from disk buffer
                InputStream is = new BlobInputStream(pos);
                int bc = is.read(data);
                is.close();
                if (bc != data.length) {
                    throw new IOException("Unexpected EOF on BLOB data file bc=" +
                            bc + " data.len=" + data.length);
                }
            }
            return data;
        } catch (IOException e) {
            throw new SQLException(BaseMessages.getString(PKG, "error.generic.ioerror", e.getMessage()),
                    "HY000");
        }
    }

    /**
     * Retrieve the BLOB data as an <code>InputStream</code>.
     *
     * @param ascii true if an ASCII input stream should be returned
     * @return the <code>InputStream</code> built over the BLOB data
     * @throws SQLException if an error occurs
     */
    public InputStream getBinaryStream(boolean ascii) throws SQLException {
        try {
            if (ascii) {
                return new AsciiInputStream(0);
            } else {
                return new BlobInputStream(0);
            }
        } catch (IOException e) {
            throw new SQLException(BaseMessages.getString(PKG, "error.generic.ioerror",
                    e.getMessage()),
                    "HY000");
        }
    }

    /**
     * Retrieve the BLOB data as an Big Endian Unicode
     * <code>InputStream</code>.
     *
     * @return the <code>InputStream</code> built over the BLOB data
     * @throws SQLException if an error occurs
     */
    public InputStream getUnicodeStream() throws SQLException {
        try {
            return new UnicodeInputStream(0);
        } catch (IOException e) {
            throw new SQLException(BaseMessages.getString(PKG, "error.generic.ioerror",
                    e.getMessage()),
                    "HY000");
        }
    }

    /**
     * Creates an <code>OutputStream</code> that can be used to update the
     * BLOB.
     * <p/>
     * Given that we cannot know the final size of a BLOB created by the caller
     * of this method, we assume the worst and create a disk BLOB by default.
     *
     * @param pos   the start position in the buffer (from 1)
     * @param ascii true if an ASCII output stream is required
     * @return the <code>OutputStream</code> to be used to update the BLOB
     * @throws SQLException if an error occurs
     */
    public OutputStream setBinaryStream(long pos, boolean ascii)
            throws SQLException {
        pos--;
        if (pos < 0) {
            throw new SQLException(BaseMessages.getString(PKG, "error.blobclob.badpos"),
                    "HY090");
        }
        if (pos > this.length) {
            throw new SQLException(BaseMessages.getString(PKG, "error.blobclob.badposlen"),
                    "HY090");
        }
        try {
            if (!isMemOnly && blobFile == null) {
                createBlobFile();
            }
            if (ascii) {
                return new AsciiOutputStream(pos);
            } else {
                return new BlobOutputStream(pos);
            }
        } catch (IOException e) {
            throw new SQLException(BaseMessages.getString(PKG, "error.generic.ioerror",
                    e.getMessage()),
                    "HY000");
        }
    }

    /**
     * Sets the content of the BLOB to the supplied byte array value.
     * <p/>
     * If the following conditions are met:
     * <ol>
     *   <li>The start position is 1
     *   <li>The existing BLOB length is smaller or the same as the length of
     *     the new data
     *   <li>The new data length does not exceed the in memory limit
     * </ol>
     * then the new data is buffered entirely in memory, otherwise a disk file
     * is created.
     *
     * @param pos    the start position in the buffer (from 1)
     * @param bytes  the byte array containing the data to copy
     * @param offset the start position in the byte array (from 0)
     * @param len    the number of bytes to copy
     * @param copy   true if a local copy of the byte array is required
     * @return the number of bytes copied
     * @throws SQLException if an error occurs
     */
    public int setBytes(long pos, byte[] bytes, int offset, int len, boolean copy)
            throws SQLException {
        pos--;
        if (pos < 0) {
            throw new SQLException(BaseMessages.getString(PKG, "error.blobclob.badpos"),
                    "HY090");
        }
        if (pos > this.length) {
            throw new SQLException(BaseMessages.getString(PKG, "error.blobclob.badposlen"),
                    "HY090");
        }
        if (bytes == null) {
            throw new SQLException(BaseMessages.getString(PKG, "error.blob.bytesnull"),
                    "HY009");
        }
        if (offset < 0 || offset > bytes.length) {
            throw new SQLException(BaseMessages.getString(PKG, "error.blobclob.badoffset"),
                    "HY090");
        }
        if (len < 0 || pos + len > (long) Integer.MAX_VALUE
                || offset + len > bytes.length) {
            throw new SQLException(BaseMessages.getString(PKG, "error.blobclob.badlen"),
                    "HY090");
        }
        //
        // If there is no disk file and this data will replace the
        // existing contents of the BLOB then just copy byte data to
        // a new buffer array if the size is small enough.
        //
        if (blobFile == null && pos == 0
                && len >= this.length
                && len <= maxMemSize) {
            if (copy) {
                buffer = new byte[len];
                System.arraycopy(bytes, offset, buffer, 0, len);
            } else {
                // A copy is not always required
                buffer = bytes;
            }
            length = len;
            return len;
        }
        try {
            //
            // OK we will now try and create a BLOB file as this
            // is a more complex update.
            //
            if (!isMemOnly && blobFile == null) {
                createBlobFile();
            }
            //
            // Open the BLOB file
            //
            open();
            int ptr = (int) pos;
            write(ptr, bytes, offset, len);
            close();
            return len;
        } catch (IOException e) {
            throw new SQLException(BaseMessages.getString(PKG, "error.generic.ioerror",
                    e.getMessage()),
                    "HY000");
        }
    }

    /**
     * Retrieves the length of this BLOB buffer in bytes.
     *
     * @return the length of the BLOB data in bytes
     */
    public long getLength() {
        return this.length;
    }

    /**
     * Retrieves the length of the BLOB buffer (in memory version only).
     *
     * @param length the length of the valid data in the buffer
     */
    public void setLength(long length) {
        this.length = (int) length;
    }

    /**
     * Truncates the BLOB buffer to the specified size.
     *
     * @param len the required length
     * @throws SQLException if an error occurs
     */
    public void truncate(long len) throws SQLException {
        if (len < 0) {
            throw new SQLException(BaseMessages.getString(PKG, "error.blobclob.badlen"),
                    "HY090");
        }
        if (len > this.length) {
            throw new SQLException(BaseMessages.getString(PKG, "error.blobclob.lentoolong"),
                    "HY090");
        }

        length = (int) len;
        if (len == 0) {
            try {
                // Try to discard and delete work file
                // Any open input streams will get EOF
                // open write streams will probably fail.
                if (blobFile != null) {
                    if (raFile != null) {
                        raFile.close();
                    }
                    blobFile.delete();
                }
            } catch (IOException e) {
                throw new SQLException(BaseMessages.getString(PKG, "error.generic.ioerror",
                        e.getMessage()),
                        "HY000");
            } finally {
                buffer = EMPTY_BUFFER;
                blobFile = null;
                raFile = null;
                openCount = 0;
                currentPage = INVALID_PAGE;
            }
        }
    }

    /**
     * Provides support for pattern searching methods.
     *
     * @param pattern the byte array containg the search pattern
     * @param start   the start position in the BLOB (from 1)
     * @return the <code>int</code> start index for the pattern (from 1) or -1
     *         if the pattern is not found.
     * @throws SQLException if an error occurs
     */
    public int position(byte[] pattern, long start) throws SQLException {
        try {
            start--;
            if (start < 0) {
                throw new SQLException(BaseMessages.getString(PKG, "error.blobclob.badpos"),
                        "HY090");
            }
            if (start >= this.length) {
                throw new SQLException(BaseMessages.getString(PKG, "error.blobclob.badposlen"),
                        "HY090");
            }
            if (pattern == null) {
                throw new SQLException(BaseMessages.getString(PKG, "error.blob.badpattern"),
                        "HY009");
            }
            if (pattern.length == 0 || length == 0 || pattern.length > length) {
                // Impossible for there to be a match
                return -1;
            }
            // FIXME Implement a better (O(n)) search algorithm
            int limit = (int) length - pattern.length;
            if (blobFile == null) {
                for (int i = (int) start; i <= limit; i++) {
                    int p;
                    for (p = 0;
                         p < pattern.length && buffer[i + p] == pattern[p];
                         p++);
                    if (p == pattern.length) {
                        return i + 1;
                    }
                }
            } else {
                open();
                for (int i = (int) start; i <= limit; i++) {
                    int p;
                    for (p = 0;
                         p < pattern.length && read(i + p) == (pattern[p] & 0xFF);
                         p++);
                    if (p == pattern.length) {
                        close();
                        return i + 1;
                    }
                }
                close();
            }
            return -1;
        } catch (IOException e) {
            throw new SQLException(BaseMessages.getString(PKG, "error.generic.ioerror",
                    e.getMessage()),
                    "HY000");
        }
    }
}
