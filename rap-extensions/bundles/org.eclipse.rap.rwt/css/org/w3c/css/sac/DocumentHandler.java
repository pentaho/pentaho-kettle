/*
 * Copyright (c) 1999 World Wide Web Consortium
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 *
 * $Id: DocumentHandler.java,v 1.1 2008/12/03 15:25:51 rsternber Exp $
 */
package org.w3c.css.sac;

/**
 * This is the main interface that most CSS applications implement: if the
 * application needs to be informed of basic parsing events, it implements this
 * interface and registers an instance with the CSS parser using the
 * setCSSHandler method.
 *
 * @version $Revision: 1.1 $
 * @author Philippe Le Hegaret 
 */
public interface DocumentHandler {
    
    /**
     * Receive notification of the beginning of a style sheet.
     *
     * The CSS parser will invoke this method only once, before any other
     * methods in this interface.
     *
     * @param source The source of the style sheet.
     * @exception CSSException Any CSS exception, possibly wrapping another
     *                         exception.  
     */
    public void startDocument(InputSource source)
        throws CSSException;
    
    /**
     * Receive notification of the end of a document. 
     *
     * The CSS parser will invoke this method only once, and it will be the
     * last method invoked during the parse. The parser shall not invoke this
     * method until it has either abandoned parsing (because of an
     * unrecoverable error) or reached the end of input.  
     *
     * @param source The source of the style sheet.
     * @exception CSSException Any CSS exception, possibly wrapping another
     *                         exception.  
     */
    public void endDocument(InputSource source) throws CSSException;

    /**
     * Receive notification of a comment.
     * If the comment appears in a declaration (e.g. color: /* comment * / blue;),
     * the parser notifies the comment before the declaration.
     *
     * @param text The comment.
     * @exception CSSException Any CSS exception, possibly wrapping another
     *                         exception.  
     */
    public void comment(String text) throws CSSException;

    /**
     * Receive notification of an unknown rule t-rule not supported by this
     * parser.
     *
     * @param atRule The complete ignored at-rule.
     * @exception CSSException Any CSS exception, possibly wrapping another
     *                         exception.  
     */
    public void ignorableAtRule(String atRule) throws CSSException;

    /**
     * Receive notification of an unknown rule t-rule not supported by this
     * parser.
     *
     * @param prefix <code>null</code> if this is the default namespace
     * @param uri The URI for this namespace.
     * @exception CSSException Any CSS exception, possibly wrapping another
     *                         exception.  
     */
    public void namespaceDeclaration(String prefix, String uri) 
	throws CSSException;

    /**
     * Receive notification of a import statement in the style sheet.
     *
     * @param uri The URI of the imported style sheet.
     * @param media The intended destination media for style information.
     * @param defaultNamespaceURI The default namespace URI for the imported
     *                            style sheet.
     * @exception CSSException Any CSS exception, possibly wrapping another
     *                         exception.
     */
    public void importStyle(String uri, SACMediaList media, 
			    String defaultNamespaceURI)
	throws CSSException;

    /**
     * Receive notification of the beginning of a media statement.
     *
     * The Parser will invoke this method at the beginning of every media
     * statement in the style sheet. there will be a corresponding endMedia()
     * event for every startElement() event.
     *
     * @param media The intended destination media for style information.
     * @exception CSSException Any CSS exception, possibly wrapping another
     *                         exception.  
     */
    public void startMedia(SACMediaList media) throws CSSException;

    /**
     * Receive notification of the end of a media statement.
     *
     * @param media The intended destination media for style information.
     * @exception CSSException Any CSS exception, possibly wrapping another
     *                         exception.  
     */
    public void endMedia(SACMediaList media) throws CSSException;

    /**
     * Receive notification of the beginning of a page statement.
     *
     * The Parser will invoke this method at the beginning of every page
     * statement in the style sheet. there will be a corresponding endPage()
     * event for every startPage() event.
     *
     * @param name the name of the page (if any, null otherwise)
     * @param pseudo_page the pseudo page (if any, null otherwise)
     * @exception CSSException Any CSS exception, possibly wrapping another
     *                         exception.  
     */    
    public void startPage(String name, String pseudo_page) throws CSSException;

    /**
     * Receive notification of the end of a media statement.
     *
     * @param name the name of the page (if any, null otherwise)
     * @param pseudo_page the pseudo page (if any, null otherwise)
     * @exception CSSException Any CSS exception, possibly wrapping another
     *                         exception.  
     */
    public void endPage(String name, String pseudo_page) throws CSSException;

    /**
     * Receive notification of the beginning of a font face statement.
     *
     * The Parser will invoke this method at the beginning of every font face
     * statement in the style sheet. there will be a corresponding endFontFace()
     * event for every startFontFace() event.
     *
     * @exception CSSException Any CSS exception, possibly wrapping another
     *                         exception.
     */
    public void startFontFace() throws CSSException;

    /**
     * Receive notification of the end of a font face statement.
     *
     * @exception CSSException Any CSS exception, possibly wrapping another
     *                         exception.  
     */
    public void endFontFace() throws CSSException;

    /**
     * Receive notification of the beginning of a rule statement.
     *
     * @param selectors All intended selectors for all declarations.
     * @exception CSSException Any CSS exception, possibly wrapping another
     *                         exception.
     */
    public void startSelector(SelectorList selectors) throws CSSException;

    /**
     * Receive notification of the end of a rule statement.
     *
     * @param selectors All intended selectors for all declarations.
     * @exception CSSException Any CSS exception, possibly wrapping another
     *                         exception.
     */
    public void endSelector(SelectorList selectors) throws CSSException;

    /**
     * Receive notification of a declaration.
     *
     * @param name the name of the property.
     * @param value the value of the property. All whitespace are stripped.
     * @param important is this property important ?
     * @exception CSSException Any CSS exception, possibly wrapping another
     *                         exception.
     */
    public void property(String name, LexicalUnit value, boolean important)
        throws CSSException;
}
