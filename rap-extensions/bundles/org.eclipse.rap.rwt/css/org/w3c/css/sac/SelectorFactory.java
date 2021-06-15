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
 * $Id: SelectorFactory.java,v 1.1 2008/12/03 15:25:51 rsternber Exp $
 */
package org.w3c.css.sac;

/**
 * @version $Revision: 1.1 $
 * @author  Philippe Le Hegaret
 * @see org.w3c.css.sac.Selector
 */
public interface SelectorFactory {

    /**
     * Creates a conditional selector.
     * 
     * @param selector a selector.
     * @param condition a condition
     * @return the conditional selector.
     * @exception CSSException If this selector is not supported.
     */    
    ConditionalSelector createConditionalSelector(SimpleSelector selector,
						  Condition condition) 
	throws CSSException;

    /**
     * Creates an any node selector.
     * 
     * @return the any node selector.
     * @exception CSSException If this selector is not supported.
     */    
    SimpleSelector createAnyNodeSelector() throws CSSException;

    /**
     * Creates an root node selector.
     * 
     * @return the root node selector.
     * @exception CSSException If this selector is not supported.
     */    
    SimpleSelector createRootNodeSelector() throws CSSException;

    /**
     * Creates an negative selector.
     * 
     * @param selector a selector.
     * @return the negative selector.
     * @exception CSSException If this selector is not supported.
     */    
    NegativeSelector createNegativeSelector(SimpleSelector selector) 
	throws CSSException;

    /**
     * Creates an element selector.
     * 
     * @param namespaceURI the <a href="http://www.w3.org/TR/REC-xml-names/#dt-NSName">namespace
     *                     URI</a> of the element selector.
     * @param tagName the <a href="http://www.w3.org/TR/REC-xml-names/#NT-LocalPart">local
     *        part</a> of the element name. <code>NULL</code> if this element
     *        selector can match any element.</p>
     * @return the element selector
     * @exception CSSException If this selector is not supported.
     */    
    ElementSelector createElementSelector(String namespaceURI, String tagName) 
	throws CSSException;

    /**
     * Creates a text node selector.
     * 
     * @param data the data
     * @return the text node selector
     * @exception CSSException If this selector is not supported.
     */    
    CharacterDataSelector createTextNodeSelector(String data)
	throws CSSException;

    /**
     * Creates a cdata section node selector.
     * 
     * @param data the data
     * @return the cdata section node selector
     * @exception CSSException If this selector is not supported.
     */    
    CharacterDataSelector createCDataSectionSelector(String data)
	throws CSSException;

    /**
     * Creates a processing instruction node selector.
     * 
     * @param target the target
     * @param data the data
     * @return the processing instruction node selector
     * @exception CSSException If this selector is not supported.
     */    
    ProcessingInstructionSelector 
	createProcessingInstructionSelector(String target,
					    String data)
	throws CSSException;

    /**
     * Creates a comment node selector.
     * 
     * @param data the data
     * @return the comment node selector
     * @exception CSSException If this selector is not supported.
     */    
    CharacterDataSelector createCommentSelector(String data)
	throws CSSException;

    /**
     * Creates a pseudo element selector.
     * 
     * @param pseudoName the pseudo element name. <code>NULL</code> if this
     *                   element selector can match any pseudo element.</p>
     * @return the element selector
     * @exception CSSException If this selector is not supported.
     */    
    ElementSelector createPseudoElementSelector(String namespaceURI, 
						String pseudoName) 
	throws CSSException;

    /**
     * Creates a descendant selector.
     *
     * @param parent the parent selector
     * @param descendant the descendant selector
     * @return the combinator selector.
     * @exception CSSException If this selector is not supported.
     */    
    DescendantSelector createDescendantSelector(Selector parent,
					     SimpleSelector descendant)
	throws CSSException;

    /**
     * Creates a child selector.
     *
     * @param parent the parent selector
     * @param child the child selector
     * @return the combinator selector.
     * @exception CSSException If this selector is not supported.
     */    
    DescendantSelector createChildSelector(Selector parent,
					   SimpleSelector child)
	throws CSSException;

    /**
     * Creates a sibling selector.
     *
     * @param nodeType the type of nodes in the siblings list.
     * @param child the child selector
     * @param directAdjacent the direct adjacent selector
     * @return the sibling selector with nodeType 
     *         equals to org.w3c.dom.Node.ELEMENT_NODE
     * @exception CSSException If this selector is not supported.
     */
    SiblingSelector createDirectAdjacentSelector(short nodeType,
						 Selector child,
						 SimpleSelector directAdjacent)
	throws CSSException;
}
