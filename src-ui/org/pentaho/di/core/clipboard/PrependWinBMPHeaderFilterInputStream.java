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
package org.pentaho.di.core.clipboard;

import java.io.IOException;
import java.io.InputStream;

/**
 * Converts a DIB (Device Independent Bitmap) taken from the clipboard, into a
 * Bitmap File.
 *
 * To do this, this InputStream prepends a BITMAPFILEHEADER to the stream.
 *
 * See
 * msdn.microsoft.com/library/en-us/winui/winui/windowsuserinterface/dataexchange/clipboard/clipboardformats.asp#_win32_Standard_Clipboard_Formats
 *
 *
 * If you found this class useful (or made some improvements) drop me a line.
 *
 *
 * @author Philip Schatz ( www.philschatz.com )
 */
class PrependWinBMPHeaderFilterInputStream extends InputStream {

        public static final int BITMAPFILEHEADER_SIZEOF = 14;
        private final InputStream in;
        private final byte[] buffer;
        private int index = 0;

        public PrependWinBMPHeaderFilterInputStream(InputStream dibStream) {
                //defined as 54
                final int offset = PrependWinBMPHeaderFilterInputStream.BITMAPFILEHEADER_SIZEOF
                                + BITMAPINFOHEADER.sizeof;

                this.in = dibStream;
                this.buffer = new byte[PrependWinBMPHeaderFilterInputStream.BITMAPFILEHEADER_SIZEOF];

                //see BITMAPFILEHEADER in windows documentation
                this.buffer[0] = 'B';
                this.buffer[1] = 'M';
                // write the offset in byte format
                this.buffer[10] = offset;
        }
        public int read() throws IOException {
                if (this.index < this.buffer.length) {
                        return 0xff & this.buffer[this.index++];
                }
                return this.in.read();
        }
}