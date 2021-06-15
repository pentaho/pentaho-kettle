/*
 * Copyright (c) 1999 World Wide Web Consortium
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 *
 * $Id: LexicalUnit.java,v 1.1 2008/12/03 15:25:51 rsternber Exp $
 */
package org.w3c.css.sac;

/**
 * This is a lexical unit for CSS values.
 * <p><b>Remarks</b>: Not all the following lexical units are supported (or
 * will be supported) by CSS.
 * <p>All examples are CSS2 compliant.
 *
 * @version $Revision: 1.1 $
 * @author Philippe Le Hegaret
 */
public interface LexicalUnit {
    
    /**
     * ,
     */
    public static final short SAC_OPERATOR_COMMA	= 0;
    /**
     * +
     */
    public static final short SAC_OPERATOR_PLUS		= 1;
    /**
     * -
     */
    public static final short SAC_OPERATOR_MINUS	= 2;
    /**
     * *
     */
    public static final short SAC_OPERATOR_MULTIPLY	= 3;
    /**
     * /
     */
    public static final short SAC_OPERATOR_SLASH	= 4;
    /**
     * %
     */
    public static final short SAC_OPERATOR_MOD		= 5;
    /**
     * ^
     */
    public static final short SAC_OPERATOR_EXP		= 6;
    /**
     * <
     */
    public static final short SAC_OPERATOR_LT		= 7;
    /**
     * >
     */
    public static final short SAC_OPERATOR_GT		= 8;
    /**
     * <=
     */
    public static final short SAC_OPERATOR_LE		= 9;
    /**
     * >=
     */
    public static final short SAC_OPERATOR_GE		= 10;
    /**
     * ~
     */
    public static final short SAC_OPERATOR_TILDE	= 11;
    
    /**
     * identifier <code>inherit</code>.
     */
    public static final short SAC_INHERIT		= 12;
    /**
     * Integers.
     * @see #getIntegerValue
     */
    public static final short SAC_INTEGER		= 13;
    /**
     * reals.
     * @see #getFloatValue
     * @see #getDimensionUnitText
     */
    public static final short SAC_REAL		        = 14;
    /**
     * Relative length<code>em</code>.
     * @see #getFloatValue
     * @see #getDimensionUnitText
     */
    public static final short SAC_EM		= 15;
    /**
     * Relative length<code>ex</code>.
     * @see #getFloatValue
     * @see #getDimensionUnitText
     */
    public static final short SAC_EX		= 16;
    /**
     * Relative length <code>px</code>.
     * @see #getFloatValue
     * @see #getDimensionUnitText
     */
    public static final short SAC_PIXEL		= 17;
    /**
     * Absolute length <code>in</code>.
     * @see #getFloatValue
     * @see #getDimensionUnitText
     */
    public static final short SAC_INCH		= 18;
    /**
     * Absolute length <code>cm</code>.
     * @see #getFloatValue
     * @see #getDimensionUnitText
     */
    public static final short SAC_CENTIMETER	= 19;
    /**
     * Absolute length <code>mm</code>.
     * @see #getFloatValue
     * @see #getDimensionUnitText
     */
    public static final short SAC_MILLIMETER	= 20;
    /**
     * Absolute length <code>pt</code>.
     * @see #getFloatValue
     * @see #getDimensionUnitText
     */
    public static final short SAC_POINT		= 21;
    /**
     * Absolute length <code>pc</code>.
     * @see #getFloatValue
     * @see #getDimensionUnitText
     */
    public static final short SAC_PICA		= 22;
    /**
     * Percentage.
     * @see #getFloatValue
     * @see #getDimensionUnitText
     */
    public static final short SAC_PERCENTAGE		= 23;
    /**
     * URI: <code>uri(&#x2e;&#x2e;&#x2e;)</code>.
     * @see #getStringValue
     */
    public static final short SAC_URI		        = 24;
    /**
     * function <code>counter</code>.
     * @see #getFunctionName
     * @see #getParameters
     */
    public static final short SAC_COUNTER_FUNCTION	= 25;
    /**
     * function <code>counters</code>.
     * @see #getFunctionName
     * @see #getParameters
     */
    public static final short SAC_COUNTERS_FUNCTION	= 26;
    /**
     * RGB Colors.
     * <code>rgb(0, 0, 0)</code> and <code>#000</code>
     * @see #getFunctionName
     * @see #getParameters
     */
    public static final short SAC_RGBCOLOR		= 27;
    /**
     * Angle <code>deg</code>.
     * @see #getFloatValue
     * @see #getDimensionUnitText
     */
    public static final short SAC_DEGREE		= 28;
    /**
     * Angle <code>grad</code>.
     * @see #getFloatValue
     * @see #getDimensionUnitText
     */
    public static final short SAC_GRADIAN		= 29;
    /**
     * Angle <code>rad</code>.
     * @see #getFloatValue
     * @see #getDimensionUnitText
     */
    public static final short SAC_RADIAN		= 30;
    /**
     * Time <code>ms</code>.
     * @see #getFloatValue
     * @see #getDimensionUnitText
     */
    public static final short SAC_MILLISECOND		= 31;
    /**
     * Time <code>s</code>.
     * @see #getFloatValue
     * @see #getDimensionUnitText
     */
    public static final short SAC_SECOND		= 32;
    /**
     * Frequency <code>Hz</code>.
     * @see #getFloatValue
     * @see #getDimensionUnitText
     */
    public static final short SAC_HERTZ		        = 33;
    /**
     * Frequency <code>kHz</code>.
     * @see #getFloatValue
     * @see #getDimensionUnitText
     */
    public static final short SAC_KILOHERTZ		= 34;
    
    /**
     * any identifier except <code>inherit</code>.
     * @see #getStringValue
     */
    public static final short SAC_IDENT		        = 35;
    /**
     * A string.
     * @see #getStringValue
     */
    public static final short SAC_STRING_VALUE		= 36;
    /**
     * Attribute: <code>attr(&#x2e;&#x2e;&#x2e;)</code>.
     * @see #getStringValue
     */
    public static final short SAC_ATTR		        = 37;
    /**
     * function <code>rect</code>.
     * @see #getFunctionName
     * @see #getParameters
     */
    public static final short SAC_RECT_FUNCTION		= 38;
    /**
     * A unicode range. @@TO BE DEFINED
     */
    public static final short SAC_UNICODERANGE		= 39;
    
    /**
     * sub expressions
     * <code>(a)</code> <code>(a + b)</code> <code>(normal/none)</code>
     * @see #getSubValues
     */
    public static final short SAC_SUB_EXPRESSION	= 40;
    
    /**
     * unknown function.
     * @see #getFunctionName
     * @see #getParameters
     */
    public static final short SAC_FUNCTION		= 41;
    /**
     * unknown dimension.
     * @see #getFloatValue
     * @see #getDimensionUnitText
     */
    public static final short SAC_DIMENSION		= 42;
    
    /**
     * An integer indicating the type of <code>LexicalUnit</code>.
     */
    public short       getLexicalUnitType();
    
    /**
     * Returns the next value or <code>null</code> if any.
     */
    public LexicalUnit getNextLexicalUnit();
    
    /**
     * Returns the previous value or <code>null</code> if any.
     */
    public LexicalUnit getPreviousLexicalUnit();
    
    /**
     * Returns the integer value.
     * @see #SAC_INTEGER
     */
    public int getIntegerValue();
    
    
    /**
     * Returns the float value.
     * <p>If the type of <code>LexicalUnit</code> is one of SAC_DEGREE,
     * SAC_GRADIAN, SAC_RADIAN, SAC_MILLISECOND, SAC_SECOND, SAC_HERTZ
     * or SAC_KILOHERTZ, the value can never be negative.</p>
     *
     * @see #SAC_REAL
     * @see #SAC_DIMENSION
     * @see #SAC_EM
     * @see #SAC_EX
     * @see #SAC_PIXEL
     * @see #SAC_INCH
     * @see #SAC_CENTIMETER
     * @see #SAC_MILLIMETER
     * @see #SAC_POINT
     * @see #SAC_PICA
     * @see #SAC_PERCENTAGE
     * @see #SAC_DEGREE
     * @see #SAC_GRADIAN
     * @see #SAC_RADIAN
     * @see #SAC_MILLISECOND
     * @see #SAC_SECOND
     * @see #SAC_HERTZ
     * @see #SAC_KILOHERTZ
     */
    public float getFloatValue();
    
    /**
     * Returns the string representation of the unit.
     * <p>if this lexical unit represents a float, the dimension is an empty
     * string.</p>
     * @see #SAC_REAL
     * @see #SAC_DIMENSION
     * @see #SAC_EM
     * @see #SAC_EX
     * @see #SAC_PIXEL
     * @see #SAC_INCH
     * @see #SAC_CENTIMETER
     * @see #SAC_MILLIMETER
     * @see #SAC_POINT
     * @see #SAC_PICA
     * @see #SAC_PERCENTAGE
     * @see #SAC_DEGREE
     * @see #SAC_GRADIAN
     * @see #SAC_RADIAN
     * @see #SAC_MILLISECOND
     * @see #SAC_SECOND
     * @see #SAC_HERTZ
     * @see #SAC_KILOHERTZ 
     */
    public String getDimensionUnitText();
    
    /**
     * Returns the name of the function.
     * @see #SAC_COUNTER_FUNCTION
     * @see #SAC_COUNTERS_FUNCTION
     * @see #SAC_RECT_FUNCTION
     * @see #SAC_FUNCTION
     * @see #SAC_RGBCOLOR
     */
    public String      getFunctionName();
    
    /**
     * The function parameters including operators (like the comma).
     * <code>#000</code> is converted to <code>rgb(0, 0, 0)</code>
     * can return <code>null</code> if <code>SAC_FUNCTION</code>.
     * @see #SAC_COUNTER_FUNCTION
     * @see #SAC_COUNTERS_FUNCTION
     * @see #SAC_RECT_FUNCTION
     * @see #SAC_FUNCTION
     * @see #SAC_RGBCOLOR
     */
    public LexicalUnit getParameters();

    /**
     * Returns the string value.
     * <p>If the type is <code>SAC_URI</code>, the return value doesn't contain
     * <code>uri(....)</code> or quotes.
     * <p>If the type is <code>SAC_ATTR</code>, the return value doesn't contain
     * <code>attr(....)</code>.
     *
     * @see #SAC_URI
     * @see #SAC_ATTR
     * @see #SAC_IDENT
     * @see #SAC_STRING_VALUE
     * @see #SAC_UNICODERANGE @@TO BE DEFINED 
     */
    public String getStringValue();

    /**
     * Returns a list of values inside the sub expression.
     * @see #SAC_SUB_EXPRESSION
     */
    public LexicalUnit getSubValues();
    
}
