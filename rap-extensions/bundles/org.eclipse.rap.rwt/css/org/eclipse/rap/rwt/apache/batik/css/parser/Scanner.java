/*

   Copyright 2000-2004  The Apache Software Foundation 

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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.eclipse.rap.rwt.apache.batik.util.io.NormalizingReader;
import org.eclipse.rap.rwt.apache.batik.util.io.StreamNormalizingReader;
import org.eclipse.rap.rwt.apache.batik.util.io.StringNormalizingReader;

/**
 * This class represents a CSS scanner - an object which decodes CSS lexical
 * units.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id: Scanner.java,v 1.2 2011/06/02 20:48:56 rherrmann Exp $
 */
@SuppressWarnings("all")
public class Scanner {

    /**
     * The reader.
     */
    protected NormalizingReader reader;

    /**
     * The current char.
     */
    protected int current;

    /**
     * The recording buffer.
     */
    protected char[] buffer = new char[128];

    /**
     * The current position in the buffer.
     */
    protected int position;

    /**
     * The type of the current lexical unit.
     */
    protected int type;

    /**
     * The start offset of the last lexical unit.
     */
    protected int start;

    /**
     * The end offset of the last lexical unit.
     */
    protected int end;

    /**
     * The characters to skip to create the string which represents the
     * current token.
     */
    protected int blankCharacters;

    /**
     * Creates a new Scanner object.
     * @param r The reader to scan.
     */
    public Scanner(Reader r) throws ParseException {
        try {
            reader = new StreamNormalizingReader(r);
            current = nextChar();
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }

    /**
     * Creates a new Scanner object.
     * @param is The input stream to scan.
     * @param enc The encoding to use to decode the input stream, or null.
     */
    public Scanner(InputStream is, String enc) throws ParseException {
        try {
            reader = new StreamNormalizingReader(is, enc);
            current = nextChar();
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }

    /**
     * Creates a new Scanner object.
     * @param s The string to scan.
     */
    public Scanner(String s) throws ParseException {
        try {
            reader = new StringNormalizingReader(s);
            current = nextChar(); 
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }

    /**
     * Returns the current line.
     */
    public int getLine() {
        return reader.getLine();
    }

    /**
     * Returns the current column.
     */
    public int getColumn() {
        return reader.getColumn();
    }

    /**
     * Returns the buffer used to store the chars.
     */
    public char[] getBuffer() {
        return buffer;
    }

    /**
     * Returns the start offset of the last lexical unit.
     */
    public int getStart() {
        return start;
    }

    /**
     * Returns the end offset of the last lexical unit.
     */
    public int getEnd() {
        return end;
    }

    /**
     * Clears the buffer.
     */
    public void clearBuffer() {
        if (position <= 0) {
            position = 0;
        } else {
            buffer[0] = buffer[position-1];
            position = 1;
        }
    }

    /**
     * The current lexical unit type like defined in LexicalUnits.
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the string representation of the current lexical unit.
     */
    public String getStringValue() {
        return new String(buffer, start, end - start);
    }

    /**
     * Scans a @rule value. This method assumes that the current
     * lexical unit is a at keyword.
     */
    public void scanAtRule() throws ParseException {
        try {
            // waiting for EOF, ';' or '{'
            loop: for (;;) {
                switch (current) {
                case '{':
                    int brackets = 1;
                    for (;;) {
                        nextChar();
                        switch (current) {
                        case '}':
                            if (--brackets > 0) {
                                break;
                            }
                        case -1:
                            break loop;
                        case '{':
                            brackets++;
                        }
                    }
                case -1:
                case ';':
                    break loop;
                }
                nextChar();
            }
            end = position;
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }
    
    /**
     * Returns the next token.
     */
    public int next() throws ParseException {
        blankCharacters = 0;
        start = position - 1;
        nextToken();
        end = position - endGap();
        return type;
    }

    /**
     * Returns the end gap of the current lexical unit.
     */
    protected int endGap() {
        int result = (current == -1) ? 0 : 1;
        switch (type) {
        case LexicalUnits.FUNCTION:
        case LexicalUnits.STRING:
        case LexicalUnits.S:
        case LexicalUnits.PERCENTAGE:
            result += 1;
            break;
        case LexicalUnits.COMMENT:
        case LexicalUnits.HZ:
        case LexicalUnits.EM:
        case LexicalUnits.EX:
        case LexicalUnits.PC:
        case LexicalUnits.PT:
        case LexicalUnits.PX:
        case LexicalUnits.CM:
        case LexicalUnits.MM:
        case LexicalUnits.IN:
        case LexicalUnits.MS:
            result += 2;
            break;
        case LexicalUnits.KHZ:
        case LexicalUnits.DEG:
        case LexicalUnits.RAD:
            result += 3;
            break;
        case LexicalUnits.GRAD:
            result += 4;
        }
        return result + blankCharacters;
    }

    /**
     * Returns the next token.
     */
    protected void nextToken() throws ParseException {
        try {
            switch (current) {
            case -1:
                type = LexicalUnits.EOF;
                return;
            case '{':
                nextChar();
                type = LexicalUnits.LEFT_CURLY_BRACE;
                return;
            case '}':
                nextChar();
                type = LexicalUnits.RIGHT_CURLY_BRACE;
                return;
            case '=':
                nextChar();
                type = LexicalUnits.EQUAL;
                return;
            case '+':
                nextChar();
                type = LexicalUnits.PLUS;
                return;
            case ',':
                nextChar();
                type = LexicalUnits.COMMA;
                return;
            case ';':
                nextChar();
                type = LexicalUnits.SEMI_COLON;
                return;
            case '>':
                nextChar();
                type = LexicalUnits.PRECEDE;
                return;
            case '[':
                nextChar();
                type = LexicalUnits.LEFT_BRACKET;
                return;
            case ']':
                nextChar();
                type = LexicalUnits.RIGHT_BRACKET;
                return;
            case '*':
                nextChar();
                type = LexicalUnits.ANY;
                return;
            case '(':
                nextChar();
                type = LexicalUnits.LEFT_BRACE;
                return;
            case ')':
                nextChar();
                type = LexicalUnits.RIGHT_BRACE;
                return;
            case ':':
                nextChar();
                type = LexicalUnits.COLON;
                return;
            case ' ':
            case '\t':
            case '\r':
            case '\n':
            case '\f':
                do {
                    nextChar();
                } while (ScannerUtilities.isCSSSpace((char)current));
                type = LexicalUnits.SPACE;
                return;
            case '/':
                nextChar();
                if (current != '*') {
                    type = LexicalUnits.DIVIDE;
                    return;
                }
                // Comment
                nextChar();
                start = position - 1;
                do {
                    while (current != -1 && current != '*') {
                        nextChar();
                    }
                    do {
                        nextChar();
                    } while (current != -1 && current == '*');
                } while (current != -1 && current != '/');
                if (current == -1) {
                    throw new ParseException("eof",
                                             reader.getLine(),
                                             reader.getColumn());
                }
                nextChar();
                type = LexicalUnits.COMMENT; 
                return;
            case '\'': // String1
                type = string1();
                return;
            case '"': // String2
                type = string2();
                return;
            case '<':
                nextChar();
                if (current != '!') {
                    throw new ParseException("character",
                                             reader.getLine(),
                                             reader.getColumn());
                }
                nextChar();
                if (current == '-') {
                    nextChar();
                    if (current == '-') {
                        nextChar();
                        type = LexicalUnits.CDO;
                        return;
                    }
                }
                throw new ParseException("character",
                                         reader.getLine(),
                                         reader.getColumn());
            case '-':
                nextChar();
                if (current != '-') {
                    type = LexicalUnits.MINUS;
                    return;
                }
                nextChar();
                if (current == '>') {
                    nextChar();
                    type = LexicalUnits.CDC;
                    return;
                }
                throw new ParseException("character",
                                         reader.getLine(),
                                         reader.getColumn());
            case '|':
                nextChar();
                if (current == '=') {
                    nextChar();
                    type = LexicalUnits.DASHMATCH;
                    return;
                }
                throw new ParseException("character",
                                         reader.getLine(),
                                         reader.getColumn());
            case '~':
                nextChar();
                if (current == '=') {
                    nextChar();
                    type = LexicalUnits.INCLUDES;
                    return;
                }
                throw new ParseException("character",
                                         reader.getLine(),
                                         reader.getColumn());
            case '#':
                nextChar();
                if (ScannerUtilities.isCSSNameCharacter((char)current)) {
                    start = position - 1;
                    do {
                        nextChar();
                        if (current == '\\') {
                            nextChar();
                            escape();
                        }
                    } while (current != -1 &&
                             ScannerUtilities.isCSSNameCharacter
                                 ((char)current));
                    type = LexicalUnits.HASH;
                    return;
                }
                throw new ParseException("character",
                                         reader.getLine(),
                                         reader.getColumn());
            case '@':
                nextChar();
                switch (current) {
                case 'c':
                case 'C':
                    start = position - 1;
                    if (isEqualIgnoreCase(nextChar(), 'h') &&
                        isEqualIgnoreCase(nextChar(), 'a') &&
                        isEqualIgnoreCase(nextChar(), 'r') &&
                        isEqualIgnoreCase(nextChar(), 's') &&
                        isEqualIgnoreCase(nextChar(), 'e') &&
                        isEqualIgnoreCase(nextChar(), 't')) {
                        nextChar();
                        type = LexicalUnits.CHARSET_SYMBOL;
                        return;
                    }
                    break;
                case 'f':
                case 'F':
                    start = position - 1;
                    if (isEqualIgnoreCase(nextChar(), 'o') &&
                        isEqualIgnoreCase(nextChar(), 'n') &&
                        isEqualIgnoreCase(nextChar(), 't') &&
                        isEqualIgnoreCase(nextChar(), '-') &&
                        isEqualIgnoreCase(nextChar(), 'f') &&
                        isEqualIgnoreCase(nextChar(), 'a') &&
                        isEqualIgnoreCase(nextChar(), 'c') &&
                        isEqualIgnoreCase(nextChar(), 'e')) {
                        nextChar();
                        type = LexicalUnits.FONT_FACE_SYMBOL;
                        return;
                    }
                    break;
                case 'i':
                case 'I':
                    start = position - 1;
                    if (isEqualIgnoreCase(nextChar(), 'm') &&
                        isEqualIgnoreCase(nextChar(), 'p') &&
                        isEqualIgnoreCase(nextChar(), 'o') &&
                        isEqualIgnoreCase(nextChar(), 'r') &&
                        isEqualIgnoreCase(nextChar(), 't')) {
                        nextChar();
                        type = LexicalUnits.IMPORT_SYMBOL;
                        return;
                    }
                    break;
                case 'm':
                case 'M':
                    start = position - 1;
                    if (isEqualIgnoreCase(nextChar(), 'e') &&
                        isEqualIgnoreCase(nextChar(), 'd') &&
                        isEqualIgnoreCase(nextChar(), 'i') &&
                        isEqualIgnoreCase(nextChar(), 'a')) {
                        nextChar();
                        type = LexicalUnits.MEDIA_SYMBOL;
                        return;
                    }
                    break;
                case 'p':
                case 'P':
                    start = position - 1;
                    if (isEqualIgnoreCase(nextChar(), 'a') &&
                        isEqualIgnoreCase(nextChar(), 'g') &&
                        isEqualIgnoreCase(nextChar(), 'e')) {
                        nextChar();
                        type = LexicalUnits.PAGE_SYMBOL;
                        return;
                    }
                    break;
                default:
                    if (!ScannerUtilities.isCSSIdentifierStartCharacter
                        ((char)current)) {
                        throw new ParseException("identifier.character",
                                                 reader.getLine(),
                                                 reader.getColumn());
                    }
                    start = position - 1;
                }
                do {
                    nextChar();
                    if (current == '\\') {
                        nextChar();
                        escape();
                    }
                } while (current != -1 &&
                         ScannerUtilities.isCSSNameCharacter((char)current));
                type = LexicalUnits.AT_KEYWORD;
                return;
            case '!':
                do {
                    nextChar();
                } while (current != -1 &&
                         ScannerUtilities.isCSSSpace((char)current));
                if (isEqualIgnoreCase(current, 'i') &&
                    isEqualIgnoreCase(nextChar(), 'm') &&
                    isEqualIgnoreCase(nextChar(), 'p') &&
                    isEqualIgnoreCase(nextChar(), 'o') &&
                    isEqualIgnoreCase(nextChar(), 'r') &&
                    isEqualIgnoreCase(nextChar(), 't') &&
                    isEqualIgnoreCase(nextChar(), 'a') &&
                    isEqualIgnoreCase(nextChar(), 'n') &&
                    isEqualIgnoreCase(nextChar(), 't')) {
                    nextChar();
                    type = LexicalUnits.IMPORTANT_SYMBOL;
                    return;
                }
                if (current == -1) {
                    throw new ParseException("eof",
                                             reader.getLine(),
                                             reader.getColumn());
                } else {
                    throw new ParseException("character",
                                             reader.getLine(),
                                             reader.getColumn());
                }
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                type = number();
                return;
            case '.':
                switch (nextChar()) {
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    type = dotNumber();
                    return;
                default:
                    type = LexicalUnits.DOT;
                    return;
                }
            case 'u':
            case 'U':
                nextChar();
                switch (current) {
                case '+':
                    boolean range = false;
                    for (int i = 0; i < 6; i++) {
                        nextChar();
                        switch (current) {
                        case '?':
                            range = true;
                            break;
                        default:
                            if (range &&
                            !ScannerUtilities.isCSSHexadecimalCharacter
                                ((char)current)) {
                                throw new ParseException("character",
                                                         reader.getLine(),
                                                         reader.getColumn());
                            }
                        }
                    }
                    nextChar();
                    if (range) {
                        type = LexicalUnits.UNICODE_RANGE;
                        return;
                    }
                    if (current == '-') {
                        nextChar();
                        if (!ScannerUtilities.isCSSHexadecimalCharacter
                            ((char)current)) {
                            throw new ParseException("character",
                                                     reader.getLine(),
                                                     reader.getColumn());
                        }
                        nextChar();
                        if (!ScannerUtilities.isCSSHexadecimalCharacter
                            ((char)current)) {
                            type = LexicalUnits.UNICODE_RANGE;
                            return;
                        }
                        nextChar();
                        if (!ScannerUtilities.isCSSHexadecimalCharacter
                            ((char)current)) {
                            type = LexicalUnits.UNICODE_RANGE;
                            return;
                        }
                        nextChar();
                        if (!ScannerUtilities.isCSSHexadecimalCharacter
                            ((char)current)) {
                            type = LexicalUnits.UNICODE_RANGE;
                            return;
                        }
                        nextChar();
                        if (!ScannerUtilities.isCSSHexadecimalCharacter
                            ((char)current)) {
                            type = LexicalUnits.UNICODE_RANGE;
                            return;
                        }
                        nextChar();
                        if (!ScannerUtilities.isCSSHexadecimalCharacter
                            ((char)current)) {
                            type = LexicalUnits.UNICODE_RANGE;
                            return;
                        }
                        nextChar();
                        type = LexicalUnits.UNICODE_RANGE;
                        return;
                    }
                case 'r':
                case 'R':
                    nextChar();
                    switch (current) {
                    case 'l':
                    case 'L':
                        nextChar();
                        switch (current) {
                        case '(':
                            do {
                                nextChar();
                            } while (current != -1 &&
                                     ScannerUtilities.isCSSSpace
                                     ((char)current));
                            switch (current) {
                            case '\'':
                                string1();
                                blankCharacters += 2;
                                while (current != -1 &&
                                       ScannerUtilities.isCSSSpace
                                       ((char)current)) {
                                    blankCharacters++;
                                    nextChar();
                                }
                                if (current == -1) {
                                    throw new ParseException
                                        ("eof",
                                         reader.getLine(),
                                         reader.getColumn());
                                }
                                if (current != ')') {
                                    throw new ParseException
                                        ("character",
                                         reader.getLine(),
                                         reader.getColumn());
                                }
                                nextChar();
                                type = LexicalUnits.URI;
                                return;
                            case '"':
                                string2();
                                blankCharacters += 2;
                                while (current != -1 &&
                                       ScannerUtilities.isCSSSpace
                                       ((char)current)) {
                                    blankCharacters++;
                                    nextChar();
                                }
                                if (current == -1) {
                                    throw new ParseException
                                        ("eof",
                                         reader.getLine(),
                                         reader.getColumn());
                                }
                                if (current != ')') {
                                    throw new ParseException
                                        ("character",
                                         reader.getLine(),
                                         reader.getColumn());
                                }
                                nextChar();
                                type = LexicalUnits.URI;
                                return;
                            case ')':
                                throw new ParseException("character",
                                                         reader.getLine(),
                                                         reader.getColumn());
                            default:
                                if (!ScannerUtilities.isCSSURICharacter
                                    ((char)current)) {
                                    throw new ParseException
                                        ("character",
                                         reader.getLine(),
                                         reader.getColumn());
                                }
                                start = position - 1;
                                do {
                                    nextChar();
                                } while (current != -1 &&
                                      ScannerUtilities.isCSSURICharacter
                                         ((char)current));
                                blankCharacters++;
                                while (current != -1 &&
                                       ScannerUtilities.isCSSSpace
                                       ((char)current)) {
                                    blankCharacters++;
                                    nextChar();
                                }
                                if (current == -1) {
                                    throw new ParseException
                                        ("eof",
                                         reader.getLine(),
                                         reader.getColumn());
                                }
                                if (current != ')') {
                                    throw new ParseException
                                        ("character",
                                         reader.getLine(),
                                         reader.getColumn());
                                }
                                nextChar();
                                type = LexicalUnits.URI;
                                return;
                            }
                        }
                    }
                }
                while (current != -1 &&
                       ScannerUtilities.isCSSNameCharacter((char)current)) {
                    nextChar();
                }
                if (current == '(') {
                    nextChar();
                    type = LexicalUnits.FUNCTION;
                    return;
                }
                type = LexicalUnits.IDENTIFIER;
                return;
            default:
                if (ScannerUtilities.isCSSIdentifierStartCharacter
                    ((char)current)) {
                    // Identifier
                    do {
                        nextChar();
                        if (current == '\\') {
                            nextChar();
                            escape();
                        }
                    } while (current != -1 && 
                             ScannerUtilities.isCSSNameCharacter
                             ((char)current));
                    if (current == '(') {
                        nextChar();
                        type = LexicalUnits.FUNCTION;
                        return;
                    }
                    type = LexicalUnits.IDENTIFIER;
                    return;
                }
                nextChar();
                throw new ParseException("identifier.character",
                                         reader.getLine(),
                                         reader.getColumn());
            }
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }

    /**
     * Scans a single quoted string.
     */
    protected int string1() throws IOException {
        start = position;  // fix bug #29416
        loop: for (;;) {
            switch (nextChar()) {
            case -1:
                throw new ParseException("eof",
                                         reader.getLine(),
                                         reader.getColumn());
            case '\'':
                break loop;
            case '"':
                break;
            case '\\':
                switch (nextChar()) {
                case '\n':
                case '\f':
                    break;
                default:
                    escape();
                }
                break;
            default:
                if (!ScannerUtilities.isCSSStringCharacter((char)current)) {
                    throw new ParseException("character",
                                             reader.getLine(),
                                             reader.getColumn());
                }
            }
        }
        nextChar();
        return LexicalUnits.STRING;
    }

    /**
     * Scans a double quoted string.
     */
    protected int string2() throws IOException {
        start = position;  // fix bug #29416
        loop: for (;;) {
            switch (nextChar()) {
            case -1:
                throw new ParseException("eof",
                                         reader.getLine(),
                                         reader.getColumn());
            case '\'':
                break;
            case '"':
                break loop;
            case '\\':
                switch (nextChar()) {
                case '\n':
                case '\f':
                    break;
                default:
                    escape();
                }
                break;
            default:
                if (!ScannerUtilities.isCSSStringCharacter((char)current)) {
                    throw new ParseException("character",
                                             reader.getLine(),
                                             reader.getColumn());
                }
            }
        }
        nextChar();
        return LexicalUnits.STRING;
    }

    /**
     * Scans a number.
     */
    protected int number() throws IOException {
        loop: for (;;) {
            switch (nextChar()) {
            case '.':
                switch (nextChar()) {
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    return dotNumber();
                }
                throw new ParseException("character",
                                         reader.getLine(),
                                         reader.getColumn());
            default:
                break loop;
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
            }
        }
        return numberUnit(true);
    }        

    /**
     * Scans the decimal part of a number.
     */
    protected int dotNumber() throws IOException {
        loop: for (;;) {
            switch (nextChar()) {
            default:
                break loop;
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
            }
        }
        return numberUnit(false);
    }

    /**
     * Scans the unit of a number.
     */
    protected int numberUnit(boolean integer) throws IOException {
        switch (current) {
        case '%':
            nextChar();
            return LexicalUnits.PERCENTAGE;
        case 'c':
        case 'C':
            switch(nextChar()) {
            case 'm':
            case 'M':
                nextChar();
                if (current != -1 &&
                    ScannerUtilities.isCSSNameCharacter((char)current)) {
                    do {
                        nextChar();
                    } while (current != -1 &&
                             ScannerUtilities.isCSSNameCharacter
                             ((char)current));
                    return LexicalUnits.DIMENSION;
                }
                return LexicalUnits.CM;
            default:
                while (current != -1 &&
                       ScannerUtilities.isCSSNameCharacter((char)current)) {
                    nextChar();
                }
                return LexicalUnits.DIMENSION;
            }
        case 'd':
        case 'D':
            switch(nextChar()) {
            case 'e':
            case 'E':
                switch(nextChar()) {
                case 'g':
                case 'G':
                    nextChar();
                    if (current != -1 &&
                        ScannerUtilities.isCSSNameCharacter((char)current)) {
                        do {
                            nextChar();
                        } while (current != -1 &&
                                 ScannerUtilities.isCSSNameCharacter
                                 ((char)current));
                        return LexicalUnits.DIMENSION;
                    }
                    return LexicalUnits.DEG;
                }
            default:
                while (current != -1 &&
                       ScannerUtilities.isCSSNameCharacter((char)current)) {
                    nextChar();
                }
                return LexicalUnits.DIMENSION;
            }
        case 'e':
        case 'E':
            switch(nextChar()) {
            case 'm':
            case 'M':
                nextChar();
                if (current != -1 &&
                    ScannerUtilities.isCSSNameCharacter((char)current)) {
                    do {
                        nextChar();
                    } while (current != -1 &&
                             ScannerUtilities.isCSSNameCharacter
                             ((char)current));
                    return LexicalUnits.DIMENSION;
                }
                return LexicalUnits.EM;
            case 'x':
            case 'X':
                nextChar();
                if (current != -1 &&
                    ScannerUtilities.isCSSNameCharacter((char)current)) {
                    do {
                        nextChar();
                    } while (current != -1 &&
                             ScannerUtilities.isCSSNameCharacter
                             ((char)current));
                    return LexicalUnits.DIMENSION;
                }
                return LexicalUnits.EX;
            default:
                while (current != -1 &&
                       ScannerUtilities.isCSSNameCharacter((char)current)) {
                    nextChar();
                }
                return LexicalUnits.DIMENSION;
            }
        case 'g':
        case 'G':
            switch(nextChar()) {
            case 'r':
            case 'R':
                switch(nextChar()) {
                case 'a':
                case 'A':
                    switch(nextChar()) {
                    case 'd':
                    case 'D':
                        nextChar();
                        if (current != -1 &&
                            ScannerUtilities.isCSSNameCharacter
                            ((char)current)) {
                            do {
                                nextChar();
                            } while (current != -1 &&
                                     ScannerUtilities.isCSSNameCharacter
                                     ((char)current));
                            return LexicalUnits.DIMENSION;
                        }
                        return LexicalUnits.GRAD;
                    }
                }
            default:
                while (current != -1 &&
                       ScannerUtilities.isCSSNameCharacter((char)current)) {
                    nextChar();
                }
                return LexicalUnits.DIMENSION;
            }
        case 'h':
        case 'H':
            nextChar();
            switch(current) {
            case 'z':
            case 'Z':
                nextChar();
                if (current != -1 &&
                    ScannerUtilities.isCSSNameCharacter((char)current)) {
                    do {
                        nextChar();
                    } while (current != -1 &&
                             ScannerUtilities.isCSSNameCharacter
                             ((char)current));
                    return LexicalUnits.DIMENSION;
                }
                return LexicalUnits.HZ;
            default:
                while (current != -1 &&
                       ScannerUtilities.isCSSNameCharacter((char)current)) {
                    nextChar();
                }
                return LexicalUnits.DIMENSION;
            }
        case 'i':
        case 'I':
            switch(nextChar()) {
            case 'n':
            case 'N':
                nextChar();
                if (current != -1 &&
                    ScannerUtilities.isCSSNameCharacter((char)current)) {
                    do {
                        nextChar();
                    } while (current != -1 &&
                             ScannerUtilities.isCSSNameCharacter
                             ((char)current));
                    return LexicalUnits.DIMENSION;
                }
                return LexicalUnits.IN;
            default:
                while (current != -1 &&
                       ScannerUtilities.isCSSNameCharacter((char)current)) {
                    nextChar();
                }
                return LexicalUnits.DIMENSION;
            }
        case 'k':
        case 'K':
            switch(nextChar()) {
            case 'h':
            case 'H':
                switch(nextChar()) {
                case 'z':
                case 'Z':
                    nextChar();
                    if (current != -1 &&
                        ScannerUtilities.isCSSNameCharacter((char)current)) {
                        do {
                            nextChar();
                        } while (current != -1 &&
                                 ScannerUtilities.isCSSNameCharacter
                                 ((char)current));
                        return LexicalUnits.DIMENSION;
                    }
                    return LexicalUnits.KHZ;
                }
            default:
                while (current != -1 &&
                       ScannerUtilities.isCSSNameCharacter((char)current)) {
                    nextChar();
                }
                return LexicalUnits.DIMENSION;
            }
        case 'm':
        case 'M':
            switch(nextChar()) {
            case 'm':
            case 'M':
                nextChar();
                if (current != -1 &&
                    ScannerUtilities.isCSSNameCharacter((char)current)) {
                    do {
                        nextChar();
                    } while (current != -1 &&
                             ScannerUtilities.isCSSNameCharacter
                             ((char)current));
                    return LexicalUnits.DIMENSION;
                }
                return LexicalUnits.MM;
            case 's':
            case 'S':
                nextChar();
                if (current != -1 &&
                    ScannerUtilities.isCSSNameCharacter((char)current)) {
                    do {
                        nextChar();
                    } while (current != -1 &&
                             ScannerUtilities.isCSSNameCharacter
                             ((char)current));
                    return LexicalUnits.DIMENSION;
                }
                return LexicalUnits.MS;
            default:
                while (current != -1 &&
                       ScannerUtilities.isCSSNameCharacter((char)current)) {
                    nextChar();
                }
                return LexicalUnits.DIMENSION;
            }
        case 'p':
        case 'P':
            switch(nextChar()) {
            case 'c':
            case 'C':
                nextChar();
                if (current != -1 &&
                    ScannerUtilities.isCSSNameCharacter((char)current)) {
                    do {
                        nextChar();
                    } while (current != -1 &&
                             ScannerUtilities.isCSSNameCharacter
                             ((char)current));
                    return LexicalUnits.DIMENSION;
                }
                return LexicalUnits.PC;
            case 't':
            case 'T':
                nextChar();
                if (current != -1 &&
                    ScannerUtilities.isCSSNameCharacter((char)current)) {
                    do {
                        nextChar();
                    } while (current != -1 &&
                             ScannerUtilities.isCSSNameCharacter
                             ((char)current));
                    return LexicalUnits.DIMENSION;
                }
                return LexicalUnits.PT;
            case 'x':
            case 'X':
                nextChar();
                if (current != -1 &&
                    ScannerUtilities.isCSSNameCharacter((char)current)) {
                    do {
                        nextChar();
                    } while (current != -1 &&
                             ScannerUtilities.isCSSNameCharacter
                             ((char)current));
                    return LexicalUnits.DIMENSION;
                }
                return LexicalUnits.PX;
            default:
                while (current != -1 &&
                       ScannerUtilities.isCSSNameCharacter((char)current)) {
                    nextChar();
                }
                return LexicalUnits.DIMENSION;
            }            
        case 'r':
        case 'R':
            switch(nextChar()) {
            case 'a':
            case 'A':
                switch(nextChar()) {
                case 'd':
                case 'D':
                    nextChar();
                    if (current != -1 &&
                        ScannerUtilities.isCSSNameCharacter((char)current)) {
                        do {
                            nextChar();
                        } while (current != -1 &&
                                 ScannerUtilities.isCSSNameCharacter
                                 ((char)current));
                        return LexicalUnits.DIMENSION;
                    }
                    return LexicalUnits.RAD;
                }
            default:
                while (current != -1 &&
                       ScannerUtilities.isCSSNameCharacter((char)current)) {
                    nextChar();
                }
                return LexicalUnits.DIMENSION;
            }
        case 's':
        case 'S':
            nextChar();
            return LexicalUnits.S;
        default:
            if (current != -1 &&
                ScannerUtilities.isCSSIdentifierStartCharacter
                ((char)current)) {
                do {
                    nextChar();
                } while (current != -1 &&
                         ScannerUtilities.isCSSNameCharacter((char)current));
                return LexicalUnits.DIMENSION;
            }
            return (integer) ? LexicalUnits.INTEGER : LexicalUnits.REAL;
        }
    }

    /**
     * Scans an escape sequence, if one.
     */
    protected void escape() throws IOException {
        if (ScannerUtilities.isCSSHexadecimalCharacter((char)current)) {
            nextChar();
            if (!ScannerUtilities.isCSSHexadecimalCharacter((char)current)) {
                if (ScannerUtilities.isCSSSpace((char)current)) {
                    nextChar();
                }
                return;
            }
            nextChar();
            if (!ScannerUtilities.isCSSHexadecimalCharacter((char)current)) {
                if (ScannerUtilities.isCSSSpace((char)current)) {
                    nextChar();
                }
                return;
            }
            nextChar();
            if (!ScannerUtilities.isCSSHexadecimalCharacter((char)current)) {
                if (ScannerUtilities.isCSSSpace((char)current)) {
                    nextChar();
                }
                return;
            }
            nextChar();
            if (!ScannerUtilities.isCSSHexadecimalCharacter((char)current)) {
                if (ScannerUtilities.isCSSSpace((char)current)) {
                    nextChar();
                }
                return;
            }
            nextChar();
            if (!ScannerUtilities.isCSSHexadecimalCharacter((char)current)) {
                if (ScannerUtilities.isCSSSpace((char)current)) {
                    nextChar();
                }
                return;
            }
        }
        if ((current >= ' ' && current <= '~') || current >= 128) {
            nextChar();
            return;
        }
        throw new ParseException("character",
                                 reader.getLine(),
                                 reader.getColumn());
    }

    /**
     * Compares the given int with the given character, ignoring case.
     */
    protected static boolean isEqualIgnoreCase(int i, char c) {
        return (i == -1) ? false : Character.toLowerCase((char)i) == c;
    }

    /**
     * Sets the value of the current char to the next character or -1 if the
     * end of stream has been reached.
     */
    protected int nextChar() throws IOException {
        current = reader.read();

        if (current == -1) {
            return current;
        }

        if (position == buffer.length) {
            char[] t = new char[position * 3 / 2];
            for (int i = 0; i < position; i++) {
                t[i] = buffer[i];
            }
            buffer = t;
        }

        return buffer[position++] = (char)current;
    }
}
