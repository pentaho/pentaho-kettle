package be.ibridge.kettle.core.formula;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.jfree.formula.ContextEvaluationException;
import org.jfree.formula.DefaultFormulaContext;
import org.jfree.formula.FormulaContext;
import org.jfree.formula.LocalizationContext;
import org.jfree.formula.function.FunctionRegistry;
import org.jfree.formula.operators.OperatorFactory;
import org.jfree.formula.typing.Type;
import org.jfree.formula.typing.TypeRegistry;
import org.jfree.formula.typing.coretypes.AnyType;
import org.jfree.util.Configuration;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.value.Value;

public class RowForumulaContext implements FormulaContext
{
    private Row row;
    private FormulaContext formulaContext;
    private Map valueIndexMap;
    
    public RowForumulaContext(Row row)
    {
        this.formulaContext = new DefaultFormulaContext();
        this.row = row;
        this.valueIndexMap = new Hashtable();
    }
    
    public Type resolveReferenceType(Object name) throws ContextEvaluationException
    {
        return AnyType.TYPE;
    }

    /**
     * We return the content of a Value with the given name.  We cache the position of the field indexes. 
     * 
     * @see org.jfree.formula.FormulaContext#resolveReference(java.lang.Object)
     */
    public Object resolveReference(Object name) throws ContextEvaluationException
    {
        if(name instanceof String)
        {
            Value value;
            Integer idx = (Integer) valueIndexMap.get(name);
            if (idx!=null)
            {
                value = row.getValue(idx.intValue());
                
            }
            else
            {
                int index = row.searchValueIndex((String) name);
                if (index<0)
                {
                    return null; // TODO: throw not found ContextEvaluationException? 
                }
                value = row.getValue(index);
                valueIndexMap.put(name, new Integer(index));
            }
            return getPrimitive(value);
        }
        return null;
    }

    public Configuration getConfiguration()
    {
      return formulaContext.getConfiguration();
    }

    public FunctionRegistry getFunctionRegistry()
    {
      return formulaContext.getFunctionRegistry();
    }

    public LocalizationContext getLocalizationContext()
    {
      return formulaContext.getLocalizationContext();
    }

    public OperatorFactory getOperatorFactory()
    {
      return formulaContext.getOperatorFactory();
    }

    public TypeRegistry getTypeRegistry()
    {
      return formulaContext.getTypeRegistry();
    }

    public boolean isReferenceDirty(Object name) throws ContextEvaluationException
    {
      return formulaContext.isReferenceDirty(name);
    }

    /**
     * @return the row
     */
    public Row getRow()
    {
        return row;
    }

    /**
     * @param row the row to set
     */
    public void setRow(Row row)
    {
        this.row = row;
    }
    

    public static Object getPrimitive(Value value)
    {
        switch(value.getType())
        {
        case Value.VALUE_TYPE_BIGNUMBER: return value.getBigNumber();
        case Value.VALUE_TYPE_BINARY: return value.getBytes();
        case Value.VALUE_TYPE_BOOLEAN: return new Boolean(value.getBoolean());
        case Value.VALUE_TYPE_DATE: return value.getDate();
        case Value.VALUE_TYPE_INTEGER: return new Long(value.getInteger());
        case Value.VALUE_TYPE_NUMBER: return new Double(value.getNumber());
        case Value.VALUE_TYPE_SERIALIZABLE: return value.getSerializable();
        case Value.VALUE_TYPE_STRING: return value.getString();
        default: return null;
        }
    }
    
    public static Class getPrimitiveClass(int valueType)
    {
        switch(valueType)
        {
        case Value.VALUE_TYPE_BIGNUMBER: return BigDecimal.class;
        case Value.VALUE_TYPE_BINARY: return (new byte[] {}).getClass();
        case Value.VALUE_TYPE_BOOLEAN: return Boolean.class;
        case Value.VALUE_TYPE_DATE: return Date.class;
        case Value.VALUE_TYPE_INTEGER: return Long.class;
        case Value.VALUE_TYPE_NUMBER: return Double.class;
        case Value.VALUE_TYPE_SERIALIZABLE: return Serializable.class;
        case Value.VALUE_TYPE_STRING: return String.class;
        default: return null;
        }
    }
}
