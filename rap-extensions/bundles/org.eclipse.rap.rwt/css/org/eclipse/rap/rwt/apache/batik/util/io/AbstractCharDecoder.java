/*

   Copyright 2002-2003  The Apache Software Foundation

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
import java.text.MessageFormat;

/**
 * This class is the superclass of all the char decoders.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id: AbstractCharDecoder.java,v 1.1 2010/04/14 15:16:16 rherrmann Exp $
 */
public abstract class AbstractCharDecoder implements CharDecoder {

    /**
     * The buffer size.
     */
    protected final static int BUFFER_SIZE = 8192;

    /**
     * The input stream to read.
     */
    protected InputStream inputStream;

    /**
     * The input buffer.
     */
    protected byte[] buffer = new byte[BUFFER_SIZE];

    /**
     * The current position in the buffer.
     */
    protected int position;

    /**
     * The byte count in the buffer.
     */
    protected int count;

    /**
     * Creates a new CharDecoder object.
     * @param is The stream to read.
     */
    protected AbstractCharDecoder(final InputStream is) {
        inputStream = is;
    }

    /**
     * Disposes the associated resources.
     */
    public void dispose() throws IOException {
        inputStream.close();
        inputStream = null;
    }

    /**
     * Fills the input buffer.
     */
    protected void fillBuffer() throws IOException {
        count = inputStream.read(buffer, 0, BUFFER_SIZE);
        position = 0;
    }

    /**
     * To throws an exception when the input stream contains an
     * invalid character.
     * @param encoding The encoding name.
     */
    protected void charError(final String encoding) throws IOException {
        String pattern = "The input stream represents an invalid {0} stream.";
        Object[] arguments = new Object[] { encoding };
        String mesg = MessageFormat.format( pattern, arguments );
        throw new IOException( mesg );
    }

    /**
     * To throws an exception when the end of stream was unexpected.
     * @param encoding The encoding name.
     */
    protected void endOfStreamError(final String encoding) throws IOException {
      String pattern = "Unexpected end of stream while decoding a {0} stream.";
      Object[] arguments = new Object[] { encoding };
      String mesg = MessageFormat.format( pattern, arguments );
      throw new IOException( mesg );
    }
}
