/*
 * Copyright (c) 1999 World Wide Web Consortium
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 *
 * The original version of this interface comes from SAX :
 * http://www.megginson.com/SAX/
 *
 * $Id: Locator.java,v 1.1 2008/12/03 15:25:52 rsternber Exp $
 */
package org.w3c.css.sac;

/**
 * Interface for associating a CSS event with a document location.
 *
 * <p>If a SAX parser provides location information to the SAX
 * application, it does so by implementing this interface and then
 * passing an instance to the application using the document
 * handler's setDocumentLocator method.  The application can use the
 * object to obtain the location of any other document handler event
 * in the CSS source document.</p>
 *
 * <p>Note that the results returned by the object will be valid only
 * during the scope of each document handler method: the application
 * will receive unpredictable results if it attempts to use the
 * locator at any other time.</p>
 *
 * <p>CSS parsers are not required to supply a locator, but they are
 * very strong encouraged to do so.  If the parser supplies a
 * locator, it must do so before reporting any other document events.
 * If no locator has been set by the time the application receives
 * the startDocument event, the application should assume that a
 * locator is not available.</p>
 *
 * @version $Revision: 1.1 $
 * @author  Philippe Le Hegaret
 */
public interface Locator {
    
    /**
     * Return the URI for the current document event.
     *
     * <p>The parser must resolve the URI fully before passing it to the
     * application.</p>
     *
     * @return A string containing the URI, or null
     *         if none is available.
     */
    public String getURI();
    
    /**
     * Return the line number where the current document event ends.
     * Note that this is the line position of the first character
     * after the text associated with the document event.
     * @return The line number, or -1 if none is available.
     * @see #getColumnNumber
     */
    public int getLineNumber();
    
    /**
     * Return the column number where the current document event ends.
     * Note that this is the column number of the first
     * character after the text associated with the document
     * event.  The first column in a line is position 1.
     * @return The column number, or -1 if none is available.
     * @see #getLineNumber
     */
    public int getColumnNumber();
}
