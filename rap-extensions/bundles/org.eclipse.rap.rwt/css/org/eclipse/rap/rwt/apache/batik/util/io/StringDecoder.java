/*

   Copyright 2002  The Apache Software Foundation 

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

/**
 * This class reads a string.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id: StringDecoder.java,v 1.1 2010/04/14 15:16:16 rherrmann Exp $
 */
public class StringDecoder implements CharDecoder {

    /**
     * The string which contains the decoded characters.
     */
    protected String string;

    /**
     * The number of chars in the string.
     */
    protected int length;

    /**
     * The next char index.
     */
    protected int next;

    /**
     * Creates a new StringDecoder.
     */
    public StringDecoder(String s) {
        string = s;
        length = s.length();
    }

    /**
     * Reads the next character.
     * @return a character or END_OF_STREAM.
     */
    public int readChar() throws IOException {
        if (next == length) {
            return END_OF_STREAM;
        }
        return string.charAt(next++);
    }

    /**
     * Disposes the associated resources.
     */
    public void dispose() throws IOException {
        string = null;
    }
}
