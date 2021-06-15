/*
 * Copyright (c) 1999 World Wide Web Consortium
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 *
 * The original version of this interface comes from SAX :
 * http://www.megginson.com/SAX/
 *
 * $Id: CSSException.java,v 1.2 2011/06/02 20:48:28 rherrmann Exp $
 */
package org.w3c.css.sac;

/**
 * @version $Revision: 1.2 $
 * @author  Philippe Le Hegaret
 */
@SuppressWarnings("all")
public class CSSException extends RuntimeException {

    protected String s;

    /**
     * this error is unspecified.
     */    
    public static short SAC_UNSPECIFIED_ERR   = 0;

    /**
     * If the operation is not supported
     */    
    public static short SAC_NOT_SUPPORTED_ERR = 1;

    /**
     * If an invalid or illegal string is specified
     */    
    public static short SAC_SYNTAX_ERR        = 2;

    /**
     * The internal exception.
     */    
    protected Exception e;

    protected short     code;

    /**
     * Creates a new CSSException
     */
    public CSSException() {
    }

    /**
     * Creates a new CSSException
     */
    public CSSException(String s) {
	this.code = SAC_UNSPECIFIED_ERR;
        this.s = s;
    }
    
    /**
     * Creates a new CSSException with an embeded exception.
     * @param e the embeded exception.
     */
    public CSSException(Exception e) {
	this.code = SAC_UNSPECIFIED_ERR;
        this.e = e;
    }

    /**
     * Creates a new CSSException with a specific code.
     * @param code the embeded exception.
     */
    public CSSException(short code) {
        this.code = code;
    }

    /**
     * Creates a new CSSException with an embeded exception and a specified
     * message.
     * @param code the specified code.
     * @param e the embeded exception.  
     */
    public CSSException(short code, String s, Exception e) {
	this.code = code;
	this.s = s;
        this.e = e;
    }

    /**
     * Returns the detail message of this throwable object. 
     *
     * @return the detail message of this Throwable, or null if this Throwable
     *         does not have a detail message.  
     */
    public String getMessage() {
	if (s != null) {
	    return s;
	} else if (e != null) {
	    return e.getMessage();
	} else {
	    return null;
	}
    }

    /**
     * returns the error code for this exception.
     */    
    public short getCode() {
	return code;
    }

    /**
     * Returns the internal exception if any, null otherwise.
     */    
    public Exception getException() {
	return e;
    }

}
