/*

   Copyright 1999-2003  The Apache Software Foundation 

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

package org.eclipse.rap.rwt.apache.batik.util.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class represents an object which decodes UTF-8 characters from
 * a stream of bytes.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id: UTF8Decoder.java,v 1.2 2011/06/02 20:48:36 rherrmann Exp $
 */
@SuppressWarnings("all")
public class UTF8Decoder extends AbstractCharDecoder {
    
    /**
     * The number of bytes of a UTF-8 sequence indexed by the first
     * byte of the sequence.
     */
    protected final static byte[] UTF8_BYTES = {
        1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
        1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
        1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
        1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
        3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,4,4,4,4,4,4,4,4,0,0,0,0,0,0,0,0,
    };

    /**
     * The next char, in case of a 4 bytes sequence.
     */
    protected int nextChar = -1;

    /**
     * Creates a new UTF8Decoder.
     */
    public UTF8Decoder(InputStream is) {
        super(is);
    }

    /**
     * Reads the next character.
     * @return a character or END_OF_STREAM.
     */
    public int readChar() throws IOException {
        if (nextChar != -1) {
            int result = nextChar;
            nextChar = -1;
            return result;
        }
        if (position == count) {
            fillBuffer();
        }
        if (count == -1) {
            return END_OF_STREAM;
        }
        int b1 = buffer[position++] & 0xff;
        switch (UTF8_BYTES[b1]) {
        default:
            charError("UTF-8");

        case 1:
            return b1;

        case 2:
            if (position == count) {
                fillBuffer();
            }
            if (count == -1) {
                endOfStreamError("UTF-8");
            }
            return ((b1 & 0x1f) << 6) | (buffer[position++] & 0x3f);

        case 3:
            if (position == count) {
                fillBuffer();
            }
            if (count == -1) {
                endOfStreamError("UTF-8");
            }
            int b2 = buffer[position++];
            if (position == count) {
                fillBuffer();
            }
            if (count == -1) {
                endOfStreamError("UTF-8");
            }
            int b3 = buffer[position++];
            if ((b2 & 0xc0) != 0x80 || (b3 & 0xc0) != 0x80) {
                charError("UTF-8");
            }
            return ((b1 & 0x1f) << 12) | ((b2 & 0x3f) << 6) | (b3 & 0x1f);

        case 4:
            if (position == count) {
                fillBuffer();
            }
            if (count == -1) {
                endOfStreamError("UTF-8");
            }
            b2 = buffer[position++];
            if (position == count) {
                fillBuffer();
            }
            if (count == -1) {
                endOfStreamError("UTF-8");
            }
            b3 = buffer[position++];
            if (position == count) {
                fillBuffer();
            }
            if (count == -1) {
                endOfStreamError("UTF-8");
            }
            int b4 = buffer[position++];
            if ((b2 & 0xc0) != 0x80 ||
                (b3 & 0xc0) != 0x80 ||
                (b4 & 0xc0) != 0x80) {
                charError("UTF-8");
            }
            int c = ((b1 & 0x1f) << 18)
                | ((b2 & 0x3f) << 12)
                | ((b3 & 0x1f) << 6)
                | (b4 & 0x1f);
            nextChar = (c - 0x10000) % 0x400 + 0xdc00;            
            return (c - 0x10000) / 0x400 + 0xd800;
        }
    }
}
