/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.calculator;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

public class CalculatorMetaFunction implements Cloneable
{
    public static final String XML_TAG = "calculation";  

    public static final int CALC_NONE                 =  0;
    public static final int CALC_CONSTANT             =  1;
    public static final int CALC_COPY_OF_FIELD        =  2;
    public static final int CALC_ADD                  =  3;
    public static final int CALC_SUBTRACT             =  4;
    public static final int CALC_MULTIPLY             =  5;
    public static final int CALC_DIVIDE               =  6;
    public static final int CALC_SQUARE               =  7;
    public static final int CALC_SQUARE_ROOT          =  8;
    public static final int CALC_PERCENT_1            =  9;
    public static final int CALC_PERCENT_2            = 10;
    public static final int CALC_PERCENT_3            = 11;
    public static final int CALC_COMBINATION_1        = 12;
    public static final int CALC_COMBINATION_2        = 13;
    public static final int CALC_ROUND_1              = 14;
    public static final int CALC_ROUND_2              = 15;
    public static final int CALC_NVL                  = 16;
    public static final int CALC_ADD_DAYS             = 17;
    public static final int CALC_YEAR_OF_DATE         = 18;
    public static final int CALC_MONTH_OF_DATE        = 19;
    public static final int CALC_DAY_OF_YEAR          = 20;
    public static final int CALC_DAY_OF_MONTH         = 21;
    public static final int CALC_DAY_OF_WEEK          = 22;
    public static final int CALC_WEEK_OF_YEAR         = 23;
    public static final int CALC_WEEK_OF_YEAR_ISO8601 = 24;
    public static final int CALC_YEAR_OF_DATE_ISO8601 = 25;
    public static final int CALC_BYTE_TO_HEX_ENCODE   = 26;
    public static final int CALC_HEX_TO_BYTE_DECODE   = 27;
    public static final int CALC_CHAR_TO_HEX_ENCODE   = 28;
    public static final int CALC_HEX_TO_CHAR_DECODE   = 29;
    public static final int CALC_CRC32   = 30;
    public static final int CALC_ADLER32   = 31;
    public static final int CALC_MD5   = 32;
    public static final int CALC_SHA1   = 33;
    public static final int CALC_LEVENSHTEIN_DISTANCE  = 34;
    public static final int CALC_METAPHORE  = 35;
    public static final int CALC_DOUBLE_METAPHORE  = 36;

    public static final String calc_desc[] = 
        { 
            "-", 
            "CONSTANT",
            "COPY_FIELD",
            "ADD",
            "SUBTRACT",
            "MULTIPLY",
            "DIVIDE",
            "SQUARE",
            "SQUARE_ROOT",
            "PERCENT_1",
            "PERCENT_2",
            "PERCENT_3",
            "COMBINATION_1",
            "COMBINATION_2",
            "ROUND_1",
            "ROUND_2",
            "NVL",
            "ADD_DAYS",
            "YEAR_OF_DATE",
            "MONTH_OF_DATE",
            "DAY_OF_YEAR",
            "DAY_OF_MONTH",
            "DAY_OF_WEEK",
            "WEEK_OF_YEAR",
            "WEEK_OF_YEAR_ISO8601",
            "YEAR_OF_DATE_ISO8601",
            "BYTE_TO_HEX_ENCODE",
            "HEX_TO_BYTE_DECODE",
            "CHAR_TO_HEX_ENCODE",
            "HEX_TO_CHAR_DECODE",
            "CRC32",
            "ADLER32",
            "MD5",
            "SHA1",
            "LEVENSHTEIN_DISTANCE",
            "METAPHORE",
            "DOUBLE_METAPHORE"
        };
    
    public static final String calcLongDesc[] = 
        { 
            "-", 
            Messages.getString("CalculatorMetaFunction.CalcFunctions.SetFieldToConstant"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.CreateCopyOfField"),
            "A + B", 
            "A - B", 
            "A * B",
            "A / B", 
            "A * A", 
            Messages.getString("CalculatorMetaFunction.CalcFunctions.SQRT"), 
            "100 * A / B", 
            "A - ( A * B / 100 )", 
            "A + ( A * B / 100 )", 
            "A + B * C", 
            Messages.getString("CalculatorMetaFunction.CalcFunctions.Hypotenuse"), 
            Messages.getString("CalculatorMetaFunction.CalcFunctions.Round"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.Round2"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.NVL"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.DatePlusDays"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.YearOfDate"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.MonthOfDate"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.DayOfYear"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.DayOfMonth"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.DayOfWeek"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.WeekOfYear"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.WeekOfYearISO8601"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.YearOfDateISO8601"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.ByteToHexEncode"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.HexToByteDecode"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.CharToHexEncode"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.HexToCharDecode"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.CRC32"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.Adler32"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.MD5"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.SHA1"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.LevenshteinDistance"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.Metaphore"),
            Messages.getString("CalculatorMetaFunction.CalcFunctions.DoubleMetaphore")
        };
   
    private String fieldName;
    private int    calcType;
    private String fieldA;
    private String fieldB;
    private String fieldC;

    private int    valueType;
    private int    valueLength;
    private int    valuePrecision;
    
    private String conversionMask;
    private String decimalSymbol;
    private String groupingSymbol;
    private String currencySymbol;
    
    private boolean removedFromResult;
    
    /**
     * @param fieldName
     * @param calcType
     * @param fieldA
     * @param fieldB
     * @param fieldC
     * @param valueType
     * @param valueLength
     * @param valuePrecision
     * @param conversionMask 
     * @param decimalSymbol 
     * @param groupingSymbol 
     * @param currencySymbol 
     */
    public CalculatorMetaFunction(String fieldName, int calcType, String fieldA, String fieldB, String fieldC, int valueType, int valueLength, int valuePrecision, boolean removedFromResult, String conversionMask, String decimalSymbol, String groupingSymbol, String currencySymbol)
    {
        this.fieldName = fieldName;
        this.calcType = calcType;
        this.fieldA = fieldA;
        this.fieldB = fieldB;
        this.fieldC = fieldC;
        this.valueType = valueType;
        this.valueLength = valueLength;
        this.valuePrecision = valuePrecision;
        this.removedFromResult = removedFromResult;
        this.conversionMask = conversionMask;
        this.decimalSymbol = decimalSymbol;
        this.groupingSymbol = groupingSymbol;
        this.currencySymbol = currencySymbol;
    }

    public boolean equals(Object obj)
    {       
        if (obj != null && (obj.getClass().equals(this.getClass())))
        {
        	CalculatorMetaFunction mf = (CalculatorMetaFunction)obj;
            return (getXML() == mf.getXML());
        }

        return false;
    }    
    
    public Object clone()
    {
        try
        {
            CalculatorMetaFunction retval = (CalculatorMetaFunction) super.clone();
            return retval;
        }
        catch(CloneNotSupportedException e)
        {
            return null;
        }
    }
    
    public String getXML()
    {
        String xml="";
        
        xml+="<"+XML_TAG+">";
        
        xml+=XMLHandler.addTagValue("field_name",      fieldName);
        xml+=XMLHandler.addTagValue("calc_type",       getCalcTypeDesc());
        xml+=XMLHandler.addTagValue("field_a",         fieldA);
        xml+=XMLHandler.addTagValue("field_b",         fieldB);
        xml+=XMLHandler.addTagValue("field_c",         fieldC);
        xml+=XMLHandler.addTagValue("value_type",      ValueMeta.getTypeDesc(valueType));
        xml+=XMLHandler.addTagValue("value_length",    valueLength);
        xml+=XMLHandler.addTagValue("value_precision", valuePrecision);
        xml+=XMLHandler.addTagValue("remove",          removedFromResult);
        xml+=XMLHandler.addTagValue("conversion_mask", conversionMask);
        xml+=XMLHandler.addTagValue("decimal_symbol",  decimalSymbol);
        xml+=XMLHandler.addTagValue("grouping_symbol", groupingSymbol);
        xml+=XMLHandler.addTagValue("currency_symbol", currencySymbol);
        
        xml+="</"+XML_TAG+">";
     
        return xml;
    }
    
    public CalculatorMetaFunction(Node calcnode)
    {
        fieldName      = XMLHandler.getTagValue(calcnode, "field_name");
        calcType       = getCalcFunctionType( XMLHandler.getTagValue(calcnode, "calc_type") );
        fieldA         = XMLHandler.getTagValue(calcnode, "field_a");
        fieldB         = XMLHandler.getTagValue(calcnode, "field_b");
        fieldC         = XMLHandler.getTagValue(calcnode, "field_c");
        valueType      = ValueMeta.getType( XMLHandler.getTagValue(calcnode, "value_type") );
        valueLength    = Const.toInt( XMLHandler.getTagValue(calcnode, "value_length"), -1 );
        valuePrecision = Const.toInt( XMLHandler.getTagValue(calcnode, "value_precision"), -1 );
        removedFromResult = "Y".equalsIgnoreCase(XMLHandler.getTagValue(calcnode, "remove"));
        conversionMask = XMLHandler.getTagValue(calcnode, "conversion_mask");
        decimalSymbol  = XMLHandler.getTagValue(calcnode, "decimal_symbol");
        groupingSymbol = XMLHandler.getTagValue(calcnode, "grouping_symbol");
        currencySymbol = XMLHandler.getTagValue(calcnode, "currency_symbol");
        
        // Fix 2.x backward compatibility
        // The conversion mask was added in a certain revision.
        // Anything that we load from before then should get masks set to retain backward compatibility
        //
        if (XMLHandler.getSubNode(calcnode, "conversion_mask")==null) {
            fixBackwardCompatibility();
        }
    }
    
    private void fixBackwardCompatibility() {
        if (valueType==ValueMetaInterface.TYPE_INTEGER) {
        	if (Const.isEmpty(conversionMask)) conversionMask="0"; 
        	if (Const.isEmpty(decimalSymbol)) decimalSymbol="."; 
        	if (Const.isEmpty(groupingSymbol)) groupingSymbol=","; 
        }
        if (valueType==ValueMetaInterface.TYPE_NUMBER) {
        	if (Const.isEmpty(conversionMask)) conversionMask="0.0"; 
        	if (Const.isEmpty(decimalSymbol)) decimalSymbol="."; 
        	if (Const.isEmpty(groupingSymbol)) groupingSymbol=","; 
        }
	}

	public void saveRep(Repository rep, long id_transformation, long id_step, int nr) throws KettleException
    {
        rep.saveStepAttribute(id_transformation, id_step, nr, "field_name",          fieldName);
        rep.saveStepAttribute(id_transformation, id_step, nr, "calc_type",           getCalcTypeDesc());
        rep.saveStepAttribute(id_transformation, id_step, nr, "field_a",             fieldA);
        rep.saveStepAttribute(id_transformation, id_step, nr, "field_b",             fieldB);
        rep.saveStepAttribute(id_transformation, id_step, nr, "field_c",             fieldC);
        rep.saveStepAttribute(id_transformation, id_step, nr, "value_type",          ValueMeta.getTypeDesc(valueType));
        rep.saveStepAttribute(id_transformation, id_step, nr, "value_length",        valueLength);
        rep.saveStepAttribute(id_transformation, id_step, nr, "value_precision",     valuePrecision);
        rep.saveStepAttribute(id_transformation, id_step, nr, "remove",              removedFromResult);
        rep.saveStepAttribute(id_transformation, id_step, nr, "conversion_mask",     conversionMask);
        rep.saveStepAttribute(id_transformation, id_step, nr, "decimal_symbol",      decimalSymbol);
        rep.saveStepAttribute(id_transformation, id_step, nr, "grouping_symbol",     groupingSymbol);
        rep.saveStepAttribute(id_transformation, id_step, nr, "currency_symbol",     currencySymbol);
    }

    public CalculatorMetaFunction(Repository rep, long id_step, int nr) throws KettleException
    {
        fieldName         = rep.getStepAttributeString(id_step, nr, "field_name");
        calcType          = getCalcFunctionType( rep.getStepAttributeString(id_step, nr, "calc_type") );
        fieldA            = rep.getStepAttributeString(id_step, nr, "field_a");
        fieldB            = rep.getStepAttributeString(id_step, nr, "field_b");
        fieldC            = rep.getStepAttributeString(id_step, nr, "field_c");
        valueType         = ValueMeta.getType( rep.getStepAttributeString(id_step, nr, "value_type") );
        valueLength       = (int)rep.getStepAttributeInteger(id_step, nr,  "value_length");
        valuePrecision    = (int)rep.getStepAttributeInteger(id_step, nr, "value_precision");
        removedFromResult = rep.getStepAttributeBoolean(id_step, nr, "remove");
        conversionMask    = rep.getStepAttributeString(id_step, nr, "conversion_mask");
        decimalSymbol     = rep.getStepAttributeString(id_step, nr, "decimal_symbol");
        groupingSymbol    = rep.getStepAttributeString(id_step, nr, "grouping_symbol");
        currencySymbol    = rep.getStepAttributeString(id_step, nr, "currency_symbol");
        
        // Fix 2.x backward compatibility
        // The conversion mask was added in a certain revision.
        // Anything that we load from before then should get masks set to retain backward compatibility
        //
        if (rep.findStepAttributeID(id_step, nr, "conversion_mask")<0) {
        	fixBackwardCompatibility();
        }
    }
    
    public static final int getCalcFunctionType(String desc)
    {
        for (int i=1;i<calc_desc.length;i++) if (calc_desc[i].equalsIgnoreCase(desc)) return i;
        for (int i=1;i<calcLongDesc.length;i++) if (calcLongDesc[i].equalsIgnoreCase(desc)) return i;
        
        return CALC_NONE;
    }
    
    public static final String getCalcFunctionDesc(int type)
    {
        if (type<0 || type>=calc_desc.length) return null;
        return calc_desc[type];
    }

    public static final String getCalcFunctionLongDesc(int type)
    {
        if (type<0 || type>=calcLongDesc.length) return null;
        return calcLongDesc[type];
    }

    
    /**
     * @return Returns the calcType.
     */
    public int getCalcType()
    {
        return calcType;
    }

    /**
     * @param calcType The calcType to set.
     */
    public void setCalcType(int calcType)
    {
        this.calcType = calcType;
    }
    
    public String getCalcTypeDesc()
    {
        return getCalcFunctionDesc(calcType);
    }

    public String getCalcTypeLongDesc()
    {
        return getCalcFunctionLongDesc(calcType);
    }

    /**
     * @return Returns the fieldA.
     */
    public String getFieldA()
    {
        return fieldA;
    }

    /**
     * @param fieldA The fieldA to set.
     */
    public void setFieldA(String fieldA)
    {
        this.fieldA = fieldA;
    }

    /**
     * @return Returns the fieldB.
     */
    public String getFieldB()
    {
        return fieldB;
    }

    /**
     * @param fieldB The fieldB to set.
     */
    public void setFieldB(String fieldB)
    {
        this.fieldB = fieldB;
    }

    /**
     * @return Returns the fieldC.
     */
    public String getFieldC()
    {
        return fieldC;
    }

    /**
     * @param fieldC The fieldC to set.
     */
    public void setFieldC(String fieldC)
    {
        this.fieldC = fieldC;
    }

    /**
     * @return Returns the fieldName.
     */
    public String getFieldName()
    {
        return fieldName;
    }

    /**
     * @param fieldName The fieldName to set.
     */
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    /**
     * @return Returns the valueLength.
     */
    public int getValueLength()
    {
        return valueLength;
    }

    /**
     * @param valueLength The valueLength to set.
     */
    public void setValueLength(int valueLength)
    {
        this.valueLength = valueLength;
    }

    /**
     * @return Returns the valuePrecision.
     */
    public int getValuePrecision()
    {
        return valuePrecision;
    }

    /**
     * @param valuePrecision The valuePrecision to set.
     */
    public void setValuePrecision(int valuePrecision)
    {
        this.valuePrecision = valuePrecision;
    }

    /**
     * @return Returns the valueType.
     */
    public int getValueType()
    {
        return valueType;
    }

    /**
     * @param valueType The valueType to set.
     */
    public void setValueType(int valueType)
    {
        this.valueType = valueType;
    }

    /**
     * @return Returns the removedFromResult.
     */
    public boolean isRemovedFromResult()
    {
        return removedFromResult;
    }

    /**
     * @param removedFromResult The removedFromResult to set.
     */
    public void setRemovedFromResult(boolean removedFromResult)
    {
        this.removedFromResult = removedFromResult;
    }

	/**
	 * @return the conversionMask
	 */
	public String getConversionMask() {
		return conversionMask;
	}

	/**
	 * @param conversionMask the conversionMask to set
	 */
	public void setConversionMask(String conversionMask) {
		this.conversionMask = conversionMask;
	}

	/**
	 * @return the decimalSymbol
	 */
	public String getDecimalSymbol() {
		return decimalSymbol;
	}

	/**
	 * @param decimalSymbol the decimalSymbol to set
	 */
	public void setDecimalSymbol(String decimalSymbol) {
		this.decimalSymbol = decimalSymbol;
	}

	/**
	 * @return the groupingSymbol
	 */
	public String getGroupingSymbol() {
		return groupingSymbol;
	}

	/**
	 * @param groupingSymbol the groupingSymbol to set
	 */
	public void setGroupingSymbol(String groupingSymbol) {
		this.groupingSymbol = groupingSymbol;
	}

	/**
	 * @return the currencySymbol
	 */
	public String getCurrencySymbol() {
		return currencySymbol;
	}

	/**
	 * @param currencySymbol the currencySymbol to set
	 */
	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}
}
