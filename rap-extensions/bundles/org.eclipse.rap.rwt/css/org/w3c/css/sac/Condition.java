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
 * $Id: Condition.java,v 1.1 2008/12/03 15:25:51 rsternber Exp $
 */
package org.w3c.css.sac;

/**
 * @version $Revision: 1.1 $
 * @author  Philippe Le Hegaret
 */
public interface Condition {

    /**
     * This condition checks exactly two conditions.
     * example:
     * <pre class="example">
     *   .part1:lang(fr)
     * </pre>
     * @see CombinatorCondition
     */    
    public static final short SAC_AND_CONDITION		        = 0;

    /**
     * This condition checks one of two conditions.
     * @see CombinatorCondition
     */    
    public static final short SAC_OR_CONDITION		        = 1;

    /**
     * This condition checks that a condition can't be applied to a node.
     * @see NegativeCondition
     */    
    public static final short SAC_NEGATIVE_CONDITION		= 2;

    /**
     * This condition checks a specified position.
     * example:
     * <pre class="example">
     *   :first-child
     * </pre>
     * @see PositionalCondition
     */    
    public static final short SAC_POSITIONAL_CONDITION		= 3;

    /**
     * This condition checks an attribute.
     * example:
     * <pre class="example">
     *   [simple]
     *   [restart="never"]
     * </pre>
     * @see AttributeCondition
     */    
    public static final short SAC_ATTRIBUTE_CONDITION		= 4;
    /**
     * This condition checks an id attribute.
     * example:
     * <pre class="example">
     *   #myId
     * </pre>
     * @see AttributeCondition
     */    
    public static final short SAC_ID_CONDITION		        = 5;
    /**
     * This condition checks the language of the node.
     * example:
     * <pre class="example">
     *   :lang(fr)
     * </pre>
     * @see LangCondition
     */    
    public static final short SAC_LANG_CONDITION		= 6;
    /**
     * This condition checks for a value in a space-separated values in a
     * specified attribute.
     * example:
     * <pre class="example">
     *   [values~="10"]
     * </pre>
     * @see AttributeCondition
     */
    public static final short SAC_ONE_OF_ATTRIBUTE_CONDITION	= 7;
    /**
     * This condition checks if the value is in a hypen-separated list of values
     * in a specified attribute.
     * example:
     * <pre class="example">
     *   [languages|="fr"]
     * </pre>
     * @see AttributeCondition
     */
    public static final short SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION = 8;
    /**
     * This condition checks for a specified class.
     * example:
     * <pre class="example">
     *   .example
     * </pre>
     * @see AttributeCondition
     */
    public static final short SAC_CLASS_CONDITION		= 9;
    /**
     * This condition checks for the link pseudo class.
     * example:
     * <pre class="example">
     *   :link
     *   :visited
     *   :hover
     * </pre>
     * @see AttributeCondition
     */
    public static final short SAC_PSEUDO_CLASS_CONDITION	= 10;
    /**
     * This condition checks if a node is the only one in the node list.
     */
    public static final short SAC_ONLY_CHILD_CONDITION		= 11;
    /**
     * This condition checks if a node is the only one of his type.
     */
    public static final short SAC_ONLY_TYPE_CONDITION		= 12;
    /**
     * This condition checks the content of a node.
     * @see ContentCondition
     */
    public static final short SAC_CONTENT_CONDITION		= 13;

    /**
     * An integer indicating the type of <code>Condition</code>.
     */    
    public short getConditionType();
}
