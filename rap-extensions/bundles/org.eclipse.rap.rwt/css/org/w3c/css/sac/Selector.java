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
 * $Id: Selector.java,v 1.1 2008/12/03 15:25:52 rsternber Exp $
 */
package org.w3c.css.sac;

/**
 * This interface defines a selector.
 * <p><b>Remarks</b>: Not all the following selectors are supported (or will be
 * supported) by CSS.
 * <p>All examples are CSS2 compliant.
 *
 * @version $Revision: 1.1 $
 * @author Philippe Le Hegaret
 */
public interface Selector {
    
    /* simple selectors */

    /**
     * This is a conditional selector.
     * example:
     * <pre class="example">
     *   simple[role="private"]
     *   .part1
     *   H1#myId
     *   P:lang(fr).p1
     * </pre>
     *
     * @see ConditionalSelector
     */
    public static final short SAC_CONDITIONAL_SELECTOR		= 0;

    /**
     * This selector matches any node.
     * @see SimpleSelector
     */    
    public static final short SAC_ANY_NODE_SELECTOR		= 1;

    /**
     * This selector matches the root node.
     * @see SimpleSelector
     */    
    public static final short SAC_ROOT_NODE_SELECTOR		= 2;

    /**
     * This selector matches only node that are different from a specified one.
     * @see NegativeSelector
     */    
    public static final short SAC_NEGATIVE_SELECTOR		= 3;

    /**
     * This selector matches only element node.
     * example:
     * <pre class="example">
     *   H1
     *   animate
     * </pre>
     * @see ElementSelector
     */
    public static final short SAC_ELEMENT_NODE_SELECTOR		= 4;

    /**
     * This selector matches only text node.
     * @see CharacterDataSelector
     */
    public static final short SAC_TEXT_NODE_SELECTOR		= 5;

    /**
     * This selector matches only cdata node.
     * @see CharacterDataSelector
     */
    public static final short SAC_CDATA_SECTION_NODE_SELECTOR	= 6;

    /**
     * This selector matches only processing instruction node.
     * @see ProcessingInstructionSelector
     */
    public static final short SAC_PROCESSING_INSTRUCTION_NODE_SELECTOR	= 7;

    /**
     * This selector matches only comment node.
     * @see CharacterDataSelector
     */    
    public static final short SAC_COMMENT_NODE_SELECTOR		= 8;
    /**
     * This selector matches the 'first line' pseudo element.
     * example:
     * <pre class="example">
     *   :first-line
     * </pre>
     * @see ElementSelector
     */
    public static final short SAC_PSEUDO_ELEMENT_SELECTOR	= 9;

    /* combinator selectors */

    /**
     * This selector matches an arbitrary descendant of some ancestor element.
     * example:
     * <pre class="example">
     *   E F
     * </pre>
     * @see DescendantSelector
     */    
    public static final short SAC_DESCENDANT_SELECTOR		= 10;

    /**
     * This selector matches a childhood relationship between two elements.
     * example:
     * <pre class="example">
     *   E > F
     * </pre>
     * @see DescendantSelector
     */    
    public static final short SAC_CHILD_SELECTOR		= 11;
    /**
     * This selector matches two selectors who shared the same parent in the
     * document tree and the element represented by the first sequence
     * immediately precedes the element represented by the second one.
     * example:
     * <pre class="example">
     *   E + F
     * </pre>
     * @see SiblingSelector
     */
    public static final short SAC_DIRECT_ADJACENT_SELECTOR	= 12;

    /**
     * An integer indicating the type of <code>Selector</code>
     */
    public short getSelectorType();

}
