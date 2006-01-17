package be.ibridge.kettle.trans.step.unpivot;

import be.ibridge.kettle.core.value.Value;

public class UnpivotTargetField
{
    private String  fieldName;
    private String  keyValue;
    private String  targetName;
    private int     targetType;
    private int     targetLength;
    private int     targetPrecision;
    private String  targetCurrencySymbol;
    private String  targetDecimalSymbol;
    private String  targetGroupingSymbol;
    private String  targetNullString;
    private String  targetFormat;
    
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
     * @return Returns the targetFormat.
     */
    public String getTargetFormat()
    {
        return targetFormat;
    }

    /**
     * @param targetFormat The targetFormat to set.
     */
    public void setTargetFormat(String targetFormat)
    {
        this.targetFormat = targetFormat;
    }

    /**
     * Create an empty pivot target field
     */
    public UnpivotTargetField()
    {
    }

    /**
     * @return Returns the keyValue.
     */
    public String getKeyValue()
    {
        return keyValue;
    }
    /**
     * @param keyValue The keyValue to set.
     */
    public void setKeyValue(String keyValue)
    {
        this.keyValue = keyValue;
    }
    /**
     * @return Returns the targetCurrencySymbol.
     */
    public String getTargetCurrencySymbol()
    {
        return targetCurrencySymbol;
    }
    /**
     * @param targetCurrencySymbol The targetCurrencySymbol to set.
     */
    public void setTargetCurrencySymbol(String targetCurrencySymbol)
    {
        this.targetCurrencySymbol = targetCurrencySymbol;
    }
    /**
     * @return Returns the targetDecimalSymbol.
     */
    public String getTargetDecimalSymbol()
    {
        return targetDecimalSymbol;
    }
    /**
     * @param targetDecimalSymbol The targetDecimalSymbol to set.
     */
    public void setTargetDecimalSymbol(String targetDecimalSymbol)
    {
        this.targetDecimalSymbol = targetDecimalSymbol;
    }
    /**
     * @return Returns the targetGroupingSymbol.
     */
    public String getTargetGroupingSymbol()
    {
        return targetGroupingSymbol;
    }
    /**
     * @param targetGroupingSymbol The targetGroupingSymbol to set.
     */
    public void setTargetGroupingSymbol(String targetGroupingSymbol)
    {
        this.targetGroupingSymbol = targetGroupingSymbol;
    }
    /**
     * @return Returns the targetLength.
     */
    public int getTargetLength()
    {
        return targetLength;
    }
    /**
     * @param targetLength The targetLength to set.
     */
    public void setTargetLength(int targetLength)
    {
        this.targetLength = targetLength;
    }
    /**
     * @return Returns the targetName.
     */
    public String getTargetName()
    {
        return targetName;
    }
    /**
     * @param targetName The targetName to set.
     */
    public void setTargetName(String targetName)
    {
        this.targetName = targetName;
    }
    /**
     * @return Returns the targetNullString.
     */
    public String getTargetNullString()
    {
        return targetNullString;
    }
    /**
     * @param targetNullString The targetNullString to set.
     */
    public void setTargetNullString(String targetNullString)
    {
        this.targetNullString = targetNullString;
    }
    /**
     * @return Returns the targetPrecision.
     */
    public int getTargetPrecision()
    {
        return targetPrecision;
    }
    /**
     * @param targetPrecision The targetPrecision to set.
     */
    public void setTargetPrecision(int targetPrecision)
    {
        this.targetPrecision = targetPrecision;
    }
    /**
     * @return Returns the targetType.
     */
    public int getTargetType()
    {
        return targetType;
    }
    /**
     * @param targetType The targetType to set.
     */
    public void setTargetType(int targetType)
    {
        this.targetType = targetType;
    }
 
    /**
     * @return The description of the target Value type
     */
    public String getTargetTypeDesc()
    {
        return Value.getTypeDesc(targetType);
    }
    
    /**
     * Set the target type
     * @param typeDesc the target value type description
     */
    public void setTargetType(String typeDesc)
    {
        targetType=Value.getType(typeDesc);
    }
    
}
