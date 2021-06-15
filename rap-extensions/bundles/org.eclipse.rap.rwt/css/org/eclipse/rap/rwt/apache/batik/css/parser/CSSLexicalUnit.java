/*

   Copyright 2000-2001,2003  The Apache Software Foundation 

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.eclipse.rap.rwt.apache.batik.css.parser;

import org.w3c.css.sac.LexicalUnit;

/**
 * This class implements the {@link LexicalUnit} interface.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id: CSSLexicalUnit.java,v 1.1 2010/04/14 15:16:16 rherrmann Exp $
 */
public abstract class CSSLexicalUnit implements LexicalUnit {

         public static final String UNIT_TEXT_CENTIMETER  = "cm";
         public static final String UNIT_TEXT_DEGREE      = "deg";
         public static final String UNIT_TEXT_EM          = "em";
         public static final String UNIT_TEXT_EX          = "ex";
         public static final String UNIT_TEXT_GRADIAN     = "grad";
         public static final String UNIT_TEXT_HERTZ       = "Hz";
         public static final String UNIT_TEXT_INCH        = "in";
         public static final String UNIT_TEXT_KILOHERTZ   = "kHz";
         public static final String UNIT_TEXT_MILLIMETER  = "mm";
         public static final String UNIT_TEXT_MILLISECOND = "ms";
         public static final String UNIT_TEXT_PERCENTAGE  = "%";
         public static final String UNIT_TEXT_PICA        = "pc";
         public static final String UNIT_TEXT_PIXEL       = "px";
         public static final String UNIT_TEXT_POINT       = "pt";
         public static final String UNIT_TEXT_RADIAN      = "rad";
         public static final String UNIT_TEXT_REAL        = "";
         public static final String UNIT_TEXT_SECOND      = "s";


    /**
     * The lexical unit type.
     */
    protected short lexicalUnitType;

    /**
     * The next lexical unit.
     */
    protected LexicalUnit nextLexicalUnit;

    /**
     * The previous lexical unit.
     */
    protected LexicalUnit previousLexicalUnit;

    /**
     * Creates a new LexicalUnit.
     */
    protected CSSLexicalUnit(short t, LexicalUnit prev) {
        lexicalUnitType = t;
        previousLexicalUnit = prev;
        if (prev != null) {
            ((CSSLexicalUnit)prev).nextLexicalUnit = this;
        }
    }

    /**
     * <b>SAC</b>: Implements {@link LexicalUnit#getLexicalUnitType()}.
     */
    public short getLexicalUnitType() {
        return lexicalUnitType;
    }
    
    /**
     * <b>SAC</b>: Implements {@link LexicalUnit#getNextLexicalUnit()}.
     */
    public LexicalUnit getNextLexicalUnit() {
        return nextLexicalUnit;
    }

    /**
     * Sets the next lexical unit.
     */
    public void setNextLexicalUnit(LexicalUnit lu) {
        nextLexicalUnit = lu;
    }
    
    /**
     * <b>SAC</b>: Implements {@link LexicalUnit#getPreviousLexicalUnit()}.
     */
    public LexicalUnit getPreviousLexicalUnit() {
        return previousLexicalUnit;
    }
    
    /**
     * Sets the previous lexical unit.
     */
    public void setPreviousLexicalUnit(LexicalUnit lu) {
        previousLexicalUnit = lu;
    }
    
    /**
     * <b>SAC</b>: Implements {@link LexicalUnit#getIntegerValue()}.
     */
    public int getIntegerValue() {
        throw new IllegalStateException();
    }
    
    /**
     * <b>SAC</b>: Implements {@link LexicalUnit#getFloatValue()}.
     */
    public float getFloatValue() {
        throw new IllegalStateException();
    }
    
    /**
     * <b>SAC</b>: Implements {@link LexicalUnit#getDimensionUnitText()}.
     */
    public String getDimensionUnitText() {
        switch (lexicalUnitType) {
        case LexicalUnit.SAC_CENTIMETER:  return UNIT_TEXT_CENTIMETER;
        case LexicalUnit.SAC_DEGREE:      return UNIT_TEXT_DEGREE;
        case LexicalUnit.SAC_EM:          return UNIT_TEXT_EM;
        case LexicalUnit.SAC_EX:          return UNIT_TEXT_EX;
        case LexicalUnit.SAC_GRADIAN:     return UNIT_TEXT_GRADIAN;
        case LexicalUnit.SAC_HERTZ:       return UNIT_TEXT_HERTZ;
        case LexicalUnit.SAC_INCH:        return UNIT_TEXT_INCH;
        case LexicalUnit.SAC_KILOHERTZ:   return UNIT_TEXT_KILOHERTZ;
        case LexicalUnit.SAC_MILLIMETER:  return UNIT_TEXT_MILLIMETER;
        case LexicalUnit.SAC_MILLISECOND: return UNIT_TEXT_MILLISECOND;
        case LexicalUnit.SAC_PERCENTAGE:  return UNIT_TEXT_PERCENTAGE;
        case LexicalUnit.SAC_PICA:        return UNIT_TEXT_PICA;
        case LexicalUnit.SAC_PIXEL:       return UNIT_TEXT_PIXEL;
        case LexicalUnit.SAC_POINT:       return UNIT_TEXT_POINT;
        case LexicalUnit.SAC_RADIAN:      return UNIT_TEXT_RADIAN;
        case LexicalUnit.SAC_REAL:        return UNIT_TEXT_REAL;
        case LexicalUnit.SAC_SECOND:      return UNIT_TEXT_SECOND;
        default:
            throw new IllegalStateException("No Unit Text for type: " + 
                                            lexicalUnitType);
        }
    }
    
    /**
     * <b>SAC</b>: Implements {@link LexicalUnit#getFunctionName()}.
     */
    public String getFunctionName() {
        throw new IllegalStateException();
    }
    
    /**
     * <b>SAC</b>: Implements {@link LexicalUnit#getParameters()}.
     */
    public LexicalUnit getParameters() {
        throw new IllegalStateException();
    }

    /**
     * <b>SAC</b>: Implements {@link LexicalUnit#getStringValue()}.
     */
    public String getStringValue() {
        throw new IllegalStateException();
    }

    /**
     * <b>SAC</b>: Implements {@link LexicalUnit#getSubValues()}.
     */
    public LexicalUnit getSubValues() {
        throw new IllegalStateException();
    }

    /**
     * Creates a new integer lexical unit.
     */
    public static CSSLexicalUnit createSimple(short t, LexicalUnit prev) {
        return new SimpleLexicalUnit(t, prev);
    }

    /**
     * This class represents a simple unit.
     */
    protected static class SimpleLexicalUnit extends CSSLexicalUnit {

        /**
         * Creates a new LexicalUnit.
         */
        public SimpleLexicalUnit(short t, LexicalUnit prev) {
            super(t, prev);
        }
    }

    /**
     * Creates a new integer lexical unit.
     */
    public static CSSLexicalUnit createInteger(int val, LexicalUnit prev) {
        return new IntegerLexicalUnit(val, prev);
    }

    /**
     * This class represents an integer unit.
     */
    protected static class IntegerLexicalUnit extends CSSLexicalUnit {

        /**
         * The integer value.
         */
        protected int value;

        /**
         * Creates a new LexicalUnit.
         */
        public IntegerLexicalUnit(int val, LexicalUnit prev) {
            super(LexicalUnit.SAC_INTEGER, prev);
            value = val;
        }

        /**
         * <b>SAC</b>: Implements {@link LexicalUnit#getIntegerValue()}.
         */
        public int getIntegerValue() {
            return value;
        }
    }

    /**
     * Creates a new float lexical unit.
     */
    public static CSSLexicalUnit createFloat(short t, float val, LexicalUnit prev) {
        return new FloatLexicalUnit(t, val, prev);
    }

    /**
     * This class represents a float unit.
     */
    protected static class FloatLexicalUnit extends CSSLexicalUnit {

        /**
         * The float value.
         */
        protected float value;

        /**
         * Creates a new LexicalUnit.
         */
        public FloatLexicalUnit(short t, float val, LexicalUnit prev) {
            super(t, prev);
            value = val;
        }

        /**
         * <b>SAC</b>: Implements {@link LexicalUnit#getFloatValue()}.
         */
        public float getFloatValue() {
            return value;
        }
    }

    /**
     * Creates a new float lexical unit.
     */
    public static CSSLexicalUnit createDimension(float val, String dim,
                                                 LexicalUnit prev) {
        return new DimensionLexicalUnit(val, dim, prev);
    }

    /**
     * This class represents a dimension unit.
     */
    protected static class DimensionLexicalUnit extends CSSLexicalUnit {

        /**
         * The float value.
         */
        protected float value;

        /**
         * The dimension.
         */
        protected String dimension;

        /**
         * Creates a new LexicalUnit.
         */
        public DimensionLexicalUnit(float val, String dim, LexicalUnit prev) {
            super(SAC_DIMENSION, prev);
            value = val;
            dimension = dim;
        }

        /**
         * <b>SAC</b>: Implements {@link LexicalUnit#getFloatValue()}.
         */
        public float getFloatValue() {
            return value;
        }

        /**
         * <b>SAC</b>: Implements {@link LexicalUnit#getDimensionUnitText()}.
         */
        public String getDimensionUnitText() {
            return dimension;
        }
    }

    /**
     * Creates a new function lexical unit.
     */
    public static CSSLexicalUnit createFunction(String f, LexicalUnit params,
                                                LexicalUnit prev) {
        return new FunctionLexicalUnit(f, params, prev);
    }

    /**
     * This class represents a function unit.
     */
    protected static class FunctionLexicalUnit extends CSSLexicalUnit {

        /**
         * The function name.
         */
        protected String name;

        /**
         * The function parameters.
         */
        protected LexicalUnit parameters;

        /**
         * Creates a new LexicalUnit.
         */
        public FunctionLexicalUnit(String f, LexicalUnit params, LexicalUnit prev) {
            super(SAC_FUNCTION, prev);
            name = f;
            parameters = params;
        }

        /**
         * <b>SAC</b>: Implements {@link LexicalUnit#getFunctionName()}.
         */
        public String getFunctionName() {
            return name;
        }
    
        /**
         * <b>SAC</b>: Implements {@link LexicalUnit#getParameters()}.
         */
        public LexicalUnit getParameters() {
            return parameters;
        }

    }

    /**
     * Creates a new function lexical unit.
     */
    public static CSSLexicalUnit createPredefinedFunction(short t, LexicalUnit params,
                                                          LexicalUnit prev) {
        return new PredefinedFunctionLexicalUnit(t, params, prev);
    }

    /**
     * This class represents a function unit.
     */
    protected static class PredefinedFunctionLexicalUnit extends CSSLexicalUnit {

        /**
         * The function parameters.
         */
        protected LexicalUnit parameters;

        /**
         * Creates a new LexicalUnit.
         */
        public PredefinedFunctionLexicalUnit(short t, LexicalUnit params,
                                             LexicalUnit prev) {
            super(t, prev);
            parameters = params;
        }

        /**
         * <b>SAC</b>: Implements {@link LexicalUnit#getParameters()}.
         */
        public LexicalUnit getParameters() {
            return parameters;
        }
    }

    /**
     * Creates a new string lexical unit.
     */
    public static CSSLexicalUnit createString(short t, String val, LexicalUnit prev) {
        return new StringLexicalUnit(t, val, prev);
    }

    /**
     * This class represents a string unit.
     */
    protected static class StringLexicalUnit extends CSSLexicalUnit {

        /**
         * The string value.
         */
        protected String value;

        /**
         * Creates a new LexicalUnit.
         */
        public StringLexicalUnit(short t, String val, LexicalUnit prev) {
            super(t, prev);
            value = val;
        }

        /**
         * <b>SAC</b>: Implements {@link LexicalUnit#getStringValue()}.
         */
        public String getStringValue() {
            return value;
        }
    }
}
