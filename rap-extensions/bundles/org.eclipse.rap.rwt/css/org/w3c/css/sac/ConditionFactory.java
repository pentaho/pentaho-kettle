/*
 * Copyright (c) 1999 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See W3C License http://www.w3.org/Consortium/Legal/ for more details.
 *
 * $Id: ConditionFactory.java,v 1.1 2008/12/03 15:25:51 rsternber Exp $
 */
package org.w3c.css.sac;

/**
 * @version $Revision: 1.1 $
 * @author  Philippe Le Hegaret
 */
public interface ConditionFactory {

    /**
     * Creates an and condition
     *
     * @param first the first condition
     * @param second the second condition
     * @return A combinator condition
     * @exception CSSException if this exception is not supported.
     */    
    CombinatorCondition createAndCondition(Condition first, Condition second)
	throws CSSException;

    /**
     * Creates an or condition
     *
     * @param first the first condition
     * @param second the second condition
     * @return A combinator condition
     * @exception CSSException if this exception is not supported.
     */    
    CombinatorCondition createOrCondition(Condition first, Condition second)
	throws CSSException;

    /**
     * Creates a negative condition
     *
     * @param condition the condition
     * @return A negative condition
     * @exception CSSException if this exception is not supported.
     */    
    NegativeCondition createNegativeCondition(Condition condition)
	throws CSSException;

    /**
     * Creates a positional condition
     *
     * @param position the position of the node in the list.
     * @param typeNode <code>true</code> if the list should contain
     *                 only nodes of the same type (element, text node, ...).
     * @param type <code>true</code> true if the list should contain
     *             only nodes of the same node (for element, same localName
     *             and same namespaceURI).
     * @return A positional condition
     * @exception CSSException if this exception is not supported.
     */    
    PositionalCondition createPositionalCondition(int position, 
						  boolean typeNode, 
						  boolean type)
	throws CSSException;
    
    /**
     * Creates an attribute condition
     *
     * @param localName the localName of the attribute
     * @param namespaceURI the namespace URI of the attribute
     * @param specified <code>true</code> if the attribute must be specified
     *                  in the document.
     * @param value the value of this attribute.
     * @return An attribute condition
     * @exception CSSException if this exception is not supported.
     */    
    AttributeCondition createAttributeCondition(String localName,
						String namespaceURI,
						boolean specified,
						String value)
	throws CSSException;

    /**
     * Creates an id condition
     *
     * @param value the value of the id.
     * @return An Id condition
     * @exception CSSException if this exception is not supported.
     */    
    AttributeCondition createIdCondition(String value)
	throws CSSException;

    /**
     * Creates a lang condition
     *
     * @param lang the value of the language.
     * @return A lang condition
     * @exception CSSException if this exception is not supported.
     */    
    LangCondition createLangCondition(String lang)
	throws CSSException;

    /**
     * Creates a "one of" attribute condition
     *
     * @param localName the localName of the attribute
     * @param namespaceURI the namespace URI of the attribute
     * @param specified <code>true</code> if the attribute must be specified
     *                  in the document.
     * @param value the value of this attribute.
     * @return A "one of" attribute condition
     * @exception CSSException if this exception is not supported.
     */    
    AttributeCondition createOneOfAttributeCondition(String localName,
						     String namespaceURI,
						     boolean specified,
						     String value)
	throws CSSException;

    /**
     * Creates a "begin hyphen" attribute condition
     *
     * @param localName the localName of the attribute
     * @param namespaceURI the namespace URI of the attribute
     * @param specified <code>true</code> if the attribute must be specified
     *                  in the document.
     * @param value the value of this attribute.
     * @return A "begin hyphen" attribute condition
     * @exception CSSException if this exception is not supported.
     */    
    AttributeCondition createBeginHyphenAttributeCondition(String localName,
							   String namespaceURI,
							   boolean specified,
							   String value)
	throws CSSException;

    /**
     * Creates a class condition
     *
     * @param namespaceURI the namespace URI of the attribute
     * @param value the name of the class.
     * @return A class condition
     * @exception CSSException if this exception is not supported.
     */    
    AttributeCondition createClassCondition(String namespaceURI,
					    String value)
	throws CSSException;

    /**
     * Creates a pseudo class condition
     *
     * @param namespaceURI the namespace URI of the attribute
     * @param value the name of the pseudo class
     * @return A pseudo class condition
     * @exception CSSException if this exception is not supported.
     */    
    AttributeCondition createPseudoClassCondition(String namespaceURI,
						  String value)
	throws CSSException;

    /**
     * Creates a "only one" child condition
     *
     * @return A "only one" child condition
     * @exception CSSException if this exception is not supported.
     */    
    Condition createOnlyChildCondition() throws CSSException;


    /**
     * Creates a "only one" type condition
     *
     * @return A "only one" type condition
     * @exception CSSException if this exception is not supported.
     */    
    Condition createOnlyTypeCondition() throws CSSException;

    /**
     * Creates a content condition
     *
     * @param data the data in the content
     * @return A content condition
     * @exception CSSException if this exception is not supported.
     */    
    ContentCondition createContentCondition(String data)
	throws CSSException;

    
}
