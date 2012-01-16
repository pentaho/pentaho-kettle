/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

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