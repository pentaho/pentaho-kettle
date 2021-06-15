/*
 * Copyright (c) 1999 World Wide Web Consortium
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 *
 * The original version of this interface comes from SAX :
 * http://www.megginson.com/SAX/
 *
 * $Id: CSSParseException.java,v 1.2 2011/06/02 20:48:28 rherrmann Exp $
 */
package org.w3c.css.sac;

/**
 * Encapsulate a CSS parse error or warning.
 *
 * <p>This exception will include information for locating the error
 * in the original CSS document.  Note that although the application
 * will receive a CSSParseException as the argument to the handlers
 * in the ErrorHandler interface, the application is not actually
 * required to throw the exception; instead, it can simply read the
 * information in it and take a different action.</p>
 *
 * <p>Since this exception is a subclass of CSSException, it
 * inherits the ability to wrap another exception.</p>
 *
 * @version $Revision: 1.2 $
 * @author  Philippe Le Hegaret
 */
@SuppressWarnings("all")
public class CSSParseException extends CSSException {
    
    private String uri;
    private int lineNumber;
    private int columnNumber;
    
    /**
     * Create a new CSSParseException from a message and a Locator.
     *
     * <p>This constructor is especially useful when an application is
     * creating its own exception from within a DocumentHandler
     * callback.</p>
     *
     * @param message The error or warning message.
     * @param locator The locator object for the error or warning.
     * @see Locator
     * @see Parser#setLocale 
     */
    public CSSParseException(String message, Locator locator) {
	super(message);
	this.code = SAC_SYNTAX_ERR;
	this.uri = locator.getURI();
	this.lineNumber = locator.getLineNumber();
	this.columnNumber = locator.getColumnNumber();
    }
    
    
    /**

     * Wrap an existing exception in a CSSParseException.
     *
     * <p>This constructor is especially useful when an application is
     * creating its own exception from within a DocumentHandler
     * callback, and needs to wrap an existing exception that is not a
     * subclass of CSSException.</p>
     *
     * @param message The error or warning message, or null to
     *                use the message from the embedded exception.
     * @param locator The locator object for the error or warning.
     * @param e Any exception
     * @see Locator
     * @see Parser#setLocale
     */
    public CSSParseException(String message, Locator locator,
			     Exception e) {
	super(SAC_SYNTAX_ERR, message, e);
	this.uri = locator.getURI();
	this.lineNumber = locator.getLineNumber();
	this.columnNumber = locator.getColumnNumber();
    }
    
    
    /**
     * Create a new CSSParseException.
     *
     * <p>This constructor is most useful for parser writers.</p>
     *
     * <p>the parser must resolve the URI fully before creating the exception.</p>
     *
     * @param message The error or warning message.
     * @param uri The URI of the document that generated the error or warning.
     * @param lineNumber The line number of the end of the text that
     *                   caused the error or warning.
     * @param columnNumber The column number of the end of the text that
     *                     cause the error or warning.
     * @see Parser#setLocale
     */
    public CSSParseException(String message, String uri,
			     int lineNumber, int columnNumber) {
	super(message);
	this.code = SAC_SYNTAX_ERR;
	this.uri = uri;
	this.lineNumber = lineNumber;
	this.columnNumber = columnNumber;
    }
        
    /**
     * Create a new CSSParseException with an embedded exception.
     *
     * <p>This constructor is most useful for parser writers who
     * need to wrap an exception that is not a subclass of
     * CSSException.</p>
     *
     * <p>The parser must resolve the URI fully before creating the
     * exception.</p>
     *
     * @param message The error or warning message, or null to use
     *                the message from the embedded exception.
     * @param uri The URI of the document that generated
     *                 the error or warning.
     * @param lineNumber The line number of the end of the text that
     *                   caused the error or warning.
     * @param columnNumber The column number of the end of the text that
     *                     cause the error or warning.
     * @param e Another exception to embed in this one.
     * @see Parser#setLocale 
     */
    public CSSParseException(String message, String uri,
			     int lineNumber, int columnNumber, Exception e) {
	super(SAC_SYNTAX_ERR, message, e);
	this.uri = uri;
	this.lineNumber = lineNumber;
	this.columnNumber = columnNumber;
    }
    
    /**
     * Get the URI of the document where the exception occurred.
     *
     * <p>The URI will be resolved fully.</p>
     *
     * @return A string containing the URI, or null
     *         if none is available.
     * @see Locator#getURI
     */
    public String getURI() {
	return this.uri;
    }
    
    
    /**
     * The line number of the end of the text where the exception occurred.
     *
     * @return An integer representing the line number, or -1
     *         if none is available.
     * @see Locator#getLineNumber
     */
    public int getLineNumber() {
	return this.lineNumber;
    }
    
    
    /**
     * The column number of the end of the text where the exception occurred.
     *
     * <p>The first column in a line is position 1.</p>
     *
     * @return An integer representing the column number, or -1
     *         if none is available.
     * @see Locator#getColumnNumber
     */
    public int getColumnNumber() {
	return this.columnNumber;
    }
}
