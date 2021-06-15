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

package org.eclipse.rap.rwt.apache.batik.css.parser;

/**
 * A collection of utility functions for a CSS scanner.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id: ScannerUtilities.java,v 1.2 2010/10/15 07:12:12 ifurnadjiev Exp $
 */
public class ScannerUtilities {

    /**
     * The set of the valid identifier start characters.
     */
    protected final static int[] IDENTIFIER_START = { 0, 0, -2013265922, 134217726 };

    /**
     * The set of the valid name characters.
     */
    protected final static int[] NAME = { 0, 67051520, -2013265922, 134217726 };

    /**
     * The set of the valid hexadecimal characters.
     */
    protected final static int[] HEXADECIMAL = { 0, 67043328, 126, 126 };

    /**
     * The set of the valid string characters.
     */
    protected final static int[] STRING = { 512, -133, -1, 2147483647 };

    /**
     * The set of the valid uri characters.
     */
    protected final static int[] URI = { 0, -902, -1, 2147483647 };

    /**
     * This class does not need to be instantiated.
     */
    protected ScannerUtilities() {
    }

    /**
     * Tests whether the given character is a valid space.
     */
    public static boolean isCSSSpace(char c) {
      return (c <= 0x0020) &&
             (((((1L << '\t') |
                 (1L << '\n') |
                 (1L << '\r') |
                 (1L << '\f') |
                 (1L << 0x0020)) >> c) & 1L) != 0);
    }

    /**
     * Tests whether the given character is a valid identifier start character.
     */
    public static boolean isCSSIdentifierStartCharacter(char c) {
	return c >= 128 || ((IDENTIFIER_START[c / 32] & (1 << (c % 32))) != 0);
    }

    /**
     * Tests whether the given character is a valid name character.
     */
    public static boolean isCSSNameCharacter(char c) {
	return c >= 128 || ((NAME[c / 32] & (1 << (c % 32))) != 0);
    }

    /**
     * Tests whether the given character is a valid hexadecimal character.
     */
    public static boolean isCSSHexadecimalCharacter(char c) {
	return c < 128 && ((HEXADECIMAL[c / 32] & (1 << (c % 32))) != 0);
    }

    /**
     * Tests whether the given character is a valid string character.
     */
    public static boolean isCSSStringCharacter(char c) {
	return c >= 128 || ((STRING[c / 32] & (1 << (c % 32))) != 0);
    }

    /**
     * Tests whether the given character is a valid URI character.
     */
    public static boolean isCSSURICharacter(char c) {
	return c >= 128 || ((URI[c / 32] & (1 << (c % 32))) != 0);
    }
}
