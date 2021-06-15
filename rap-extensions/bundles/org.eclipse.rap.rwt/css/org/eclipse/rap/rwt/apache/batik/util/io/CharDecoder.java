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
 * This interface represents an object which decodes characters from a
 * stream of bytes.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id: CharDecoder.java,v 1.1 2010/04/14 15:16:16 rherrmann Exp $
 */
public interface CharDecoder {
    
    /**
     * This constant represents the end of stream character.
     */
    int END_OF_STREAM = -1;

    /**
     * Reads the next character.
     * @return a character or END_OF_STREAM.
     */
    int readChar() throws IOException;

    /**
     * Disposes the associated resources.
     */
    void dispose() throws IOException;
}
