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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * This class delegates to a reader the decoding of an input stream.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id: GenericDecoder.java,v 1.1 2010/04/14 15:16:16 rherrmann Exp $
 */
public class GenericDecoder implements CharDecoder {

    /**
     * The reader used to decode the stream.
     */
    protected Reader reader;

    /**
     * Creates a new GenericDecoder.
     * @param is The input stream to decode.
     * @param enc The Java encoding name.
     */
    public GenericDecoder(InputStream is, String enc) throws IOException {
        reader = new InputStreamReader(is, enc);
        reader = new BufferedReader(reader);
    }

    /**
     * Creates a new GenericDecoder.
     * @param r The reader to use.
     */
    public GenericDecoder(Reader r) {
        reader = r;
        if (!(r instanceof BufferedReader)) {
            reader = new BufferedReader(reader);
        }
    }

    /**
     * Reads the next character.
     * @return a character or END_OF_STREAM.
     */
    public int readChar() throws IOException {
        return reader.read();
    }

    /**
     * Disposes the associated resources.
     */
    public void dispose() throws IOException {
        reader.close();
        reader = null;
    }
}
