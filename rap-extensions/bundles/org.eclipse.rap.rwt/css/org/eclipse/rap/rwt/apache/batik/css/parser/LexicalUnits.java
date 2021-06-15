/*

   Copyright 2000-2001  The Apache Software Foundation 

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
 * This interface defines the constants that represent CSS lexical units.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id: LexicalUnits.java,v 1.1 2010/04/14 15:16:16 rherrmann Exp $
 */
public interface LexicalUnits {

    /**
     * Represents the EOF lexical unit.
     */
    int EOF = 0;

    /**
     * Represents the '{' lexical unit.
     */
    int LEFT_CURLY_BRACE = 1;

    /**
     * Represents the '}' lexical unit.
     */
    int RIGHT_CURLY_BRACE = 2;

    /**
     * Represents the '=' lexical unit.
     */
    int EQUAL = 3;

    /**
     * Represents the '+' lexical unit.
     */
    int PLUS = 4;

    /**
     * Represents the '-' lexical unit.
     */
    int MINUS = 5;

    /**
     * Represents the ',' lexical unit.
     */
    int COMMA = 6;

    /**
     * Represents the '.' lexical unit.
     */
    int DOT = 7;

    /**
     * Represents the ';' lexical unit.
     */
    int SEMI_COLON = 8;

    /**
     * Represents the '&gt;' lexical unit.
     */
    int PRECEDE = 9;

    /**
     * Represents the '/' lexical unit.
     */
    int DIVIDE = 10;

    /**
     * Represents the '[' lexical unit.
     */
    int LEFT_BRACKET = 11;

    /**
     * Represents the ']' lexical unit.
     */
    int RIGHT_BRACKET = 12;

    /**
     * Represents the '*' lexical unit.
     */
    int ANY = 13;

    /**
     * Represents the '(' lexical unit.
     */
    int LEFT_BRACE = 14;

    /**
     * Represents the ')' lexical unit.
     */
    int RIGHT_BRACE = 15;

    /**
     * Represents the ':' lexical unit.
     */
    int COLON = 16;

    /**
     * Represents the white space lexical unit.
     */
    int SPACE = 17;

    /**
     * Represents the comment lexical unit.
     */
    int COMMENT = 18;

    /**
     * Represents the string lexical unit.
     */
    int STRING = 19;

    /**
     * Represents the identifier lexical unit.
     */
    int IDENTIFIER = 20;

    /**
     * Represents the '&lt;&#x21;--' lexical unit.
     */
    int CDO = 21;

    /**
     * Represents the '--&gt;' lexical unit.
     */
    int CDC = 22;

    /**
     * Represents the '&#x21;important' lexical unit.
     */
    int IMPORTANT_SYMBOL = 23;

    /**
     * Represents an integer.
     */
    int INTEGER = 24;

    /**
     * Represents the '|=' lexical unit.
     */
    int DASHMATCH = 25;

    /**
     * Represents the '~=' lexical unit.
     */
    int INCLUDES = 26;

    /**
     * Represents the '#name' lexical unit.
     */
    int HASH = 27;

    /**
     * Represents the '@import' lexical unit.
     */
    int IMPORT_SYMBOL = 28;

    /**
     * Represents the '@ident' lexical unit.
     */
    int AT_KEYWORD = 29;

    /**
     * Represents the '@charset' lexical unit.
     */
    int CHARSET_SYMBOL = 30;

    /**
     * Represents the '@font-face' lexical unit.
     */
    int FONT_FACE_SYMBOL = 31;

    /**
     * Represents the '@media' lexical unit.
     */
    int MEDIA_SYMBOL = 32;

    /**
     * Represents the '@page' lexical unit.
     */
    int PAGE_SYMBOL = 33;

    /**
     * Represents a dimension lexical unit.
     */
    int DIMENSION = 34;

    /**
     * Represents a ex lexical unit.
     */
    int EX = 35;

    /**
     * Represents a em lexical unit.
     */
    int EM = 36;

    /**
     * Represents a cm lexical unit.
     */
    int CM = 37;

    /**
     * Represents a mm lexical unit.
     */
    int MM = 38;

    /**
     * Represents a in lexical unit.
     */
    int IN = 39;

    /**
     * Represents a ms lexical unit.
     */
    int MS = 40;

    /**
     * Represents a hz lexical unit.
     */
    int HZ = 41;

    /**
     * Represents a % lexical unit.
     */
    int PERCENTAGE = 42;

    /**
     * Represents a s lexical unit.
     */
    int S = 43;

    /**
     * Represents a pc lexical unit.
     */
    int PC = 44;

    /**
     * Represents a pt lexical unit.
     */
    int PT = 45;

    /**
     * Represents a px lexical unit.
     */
    int PX = 46;

    /**
     * Represents a deg lexical unit.
     */
    int DEG = 47;

    /**
     * Represents a rad lexical unit.
     */
    int RAD = 48;

    /**
     * Represents a grad lexical unit.
     */
    int GRAD = 49;

    /**
     * Represents a khz lexical unit.
     */
    int KHZ = 50;

    /**
     * Represents a 'url(URI)' lexical unit.
     */
    int URI = 51;

    /**
     * Represents a 'ident(' lexical unit.
     */
    int FUNCTION = 52;

    /**
     * Represents a unicode range lexical unit.
     */
    int UNICODE_RANGE = 53;

    /**
     * represents a real number.
     */
    int REAL = 54;
}
