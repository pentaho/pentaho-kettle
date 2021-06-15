/*
 * Copyright (c) 1999 World Wide Web Consortium
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 *
 * The original version of this interface comes from SAX :
 * http://www.megginson.com/SAX/
 *
 * $Id: ErrorHandler.java,v 1.1 2008/12/03 15:25:52 rsternber Exp $
 */
package org.w3c.css.sac;

/**
  * Basic interface for CSS error handlers.
  *
  * <p>If a CSS application needs to implement customized error
  * handling, it must implement this interface and then register an
  * instance with the CSS parser using the parser's setErrorHandler
  * method.  The parser will then report all errors and warnings
  * through this interface.</p>
  *
  * <p> The parser shall use this interface instead of throwing an
  * exception: it is up to the application whether to throw an
  * exception for different types of errors and warnings.  Note,
  * however, that there is no requirement that the parser continue to
  * provide useful information after a call to fatalError (in other
  * words, a CSS driver class could catch an exception and report a
  * fatalError).</p>
  *
  * <p>The HandlerBase class provides a default implementation of this
  * interface, ignoring warnings and recoverable errors and throwing a
  * SAXParseException for fatal errors.  An application may extend
  * that class rather than implementing the complete interface
  * itself.</p>
  *
 * @version $Revision: 1.1 $
 * @author  Philippe Le Hegaret
  */
public interface ErrorHandler {


  /**
    * Receive notification of a warning.
    *
    * <p>CSS parsers will use this method to report conditions that
    * are not errors or fatal errors as defined by the XML 1.0
    * recommendation.  The default behaviour is to take no action.</p>
    *
    * <p>The CSS parser must continue to provide normal parsing events
    * after invoking this method: it should still be possible for the
    * application to process the document through to the end.</p>
    *
    * @param exception The warning information encapsulated in a
    *                  CSS parse exception.
    * @exception CSSException Any CSS exception, possibly
    *            wrapping another exception.
    * @see CSSParseException 
    */
  public void warning(CSSParseException exception) throws CSSException;

  /**
    * Receive notification of a recoverable error.
    *
    * <p>This corresponds to the definition of "error" in section 1.2
    * of the W3C XML 1.0 Recommendation.  For example, a validating
    * parser would use this callback to report the violation of a
    * validity constraint.  The default behaviour is to take no
    * action.</p>
    *
    * <p>The CSS parser must continue to provide normal parsing events
    * after invoking this method: it should still be possible for the
    * application to process the document through to the end.  If the
    * application cannot do so, then the parser should report a fatal
    * error even if the XML 1.0 recommendation does not require it to
    * do so.</p>
    *
    * @param exception The error information encapsulated in a
    *                  CSS parse exception.
    * @exception CSSException Any CSS exception, possibly
    *            wrapping another exception.
    * @see CSSParseException 
    */
  public void error(CSSParseException exception) throws CSSException;

  /**
    * Receive notification of a non-recoverable error.
    *
    * <p>This corresponds to the definition of "fatal error" in
    * section 1.2 of the W3C XML 1.0 Recommendation.  For example, a
    * parser would use this callback to report the violation of a
    * well-formedness constraint.</p>
    *
    * <p>The application must assume that the document is unusable
    * after the parser has invoked this method, and should continue
    * (if at all) only for the sake of collecting addition error
    * messages: in fact, CSS parsers are free to stop reporting any
    * other events once this method has been invoked.</p>
    *
    * @param exception The error information encapsulated in a
    *                  CSS parse exception.  
    * @exception CSSException Any CSS exception, possibly
    *            wrapping another exception.
    * @see CSSParseException
    */
  public void fatalError(CSSParseException exception) throws CSSException;

}
