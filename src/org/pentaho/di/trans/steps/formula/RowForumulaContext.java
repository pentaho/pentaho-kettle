/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.trans.steps.formula;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.formula.ContextEvaluationException;
import org.pentaho.reporting.libraries.formula.DefaultFormulaContext;
import org.pentaho.reporting.libraries.formula.ErrorValue;
import org.pentaho.reporting.libraries.formula.FormulaContext;
import org.pentaho.reporting.libraries.formula.LibFormulaErrorValue;
import org.pentaho.reporting.libraries.formula.LocalizationContext;
import org.pentaho.reporting.libraries.formula.function.FunctionRegistry;
import org.pentaho.reporting.libraries.formula.operators.OperatorFactory;
import org.pentaho.reporting.libraries.formula.typing.Type;
import org.pentaho.reporting.libraries.formula.typing.TypeRegistry;
import org.pentaho.reporting.libraries.formula.typing.coretypes.AnyType;

public class RowForumulaContext implements FormulaContext
{
    private RowMetaInterface rowMeta;
    private FormulaContext formulaContext;
    private Map<String, Integer> valueIndexMap;
	private Object[] rowData;
    
    public RowForumulaContext(RowMetaInterface row)
    {
        this.formulaContext = new DefaultFormulaContext();
        this.rowMeta = row;
        this.rowData = null;
        this.valueIndexMap = new Hashtable<String, Integer>();
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
            ValueMetaInterface valueMeta;
            Integer idx = (Integer) valueIndexMap.get(name);
            if (idx!=null)
            {
                valueMeta = rowMeta.getValueMeta(idx.intValue());
            }
            else
            {
                int index = rowMeta.indexOfValue((String) name);
                if (index<0)
                {
                	ErrorValue errorValue = new LibFormulaErrorValue(LibFormulaErrorValue.ERROR_INVALID_ARGUMENT);
    				throw new ContextEvaluationException(errorValue);
                }
                valueMeta = rowMeta.getValueMeta(index);
                idx = new Integer(index);
                valueIndexMap.put((String)name, idx);
            }
            Object valueData = rowData[idx];
            try {
				return getPrimitive(valueMeta, valueData);
			} catch (KettleValueException e) {
				throw new ContextEvaluationException(LibFormulaErrorValue.ERROR_ARITHMETIC_VALUE);
			}
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
    public RowMetaInterface getRowMeta()
    {
        return rowMeta;
    }

    /**
     * @param rowMeta the row to set
     */
    public void setRowMeta(RowMetaInterface rowMeta)
    {
        this.rowMeta = rowMeta;
    }
    
    /**
     * @param rowData the new row of data to inject
     */
    public void setRowData(Object[] rowData)
    {
    	this.rowData = rowData;
    }
    
    /**
     * @return the current row of data
     */
    public Object[] getRowData() 
    {
		return rowData;
	}
    

    public static Object getPrimitive(ValueMetaInterface valueMeta, Object valueData) throws KettleValueException
    {
        switch(valueMeta.getType())
        {
        case ValueMetaInterface.TYPE_BIGNUMBER: return valueMeta.getBigNumber(valueData);
        case ValueMetaInterface.TYPE_BINARY: return valueMeta.getBinary(valueData);
        case ValueMetaInterface.TYPE_BOOLEAN: return valueMeta.getBoolean(valueData);
        case ValueMetaInterface.TYPE_DATE: return valueMeta.getDate(valueData);
        case ValueMetaInterface.TYPE_INTEGER: valueMeta.getInteger(valueData);
        case ValueMetaInterface.TYPE_NUMBER: return valueMeta.getNumber(valueData);
        // case ValueMetaInterface.TYPE_SERIALIZABLE: return valueMeta.(valueData);
        case ValueMetaInterface.TYPE_STRING: return valueMeta.getString(valueData);
        default: return null;
        }
    }
    
    public static Class<?> getPrimitiveClass(int valueType)
    {
        switch(valueType)
        {
        case ValueMetaInterface.TYPE_BIGNUMBER: return BigDecimal.class;
        case ValueMetaInterface.TYPE_BINARY: return (new byte[] {}).getClass();
        case ValueMetaInterface.TYPE_BOOLEAN: return Boolean.class;
        case ValueMetaInterface.TYPE_DATE: return Date.class;
        case ValueMetaInterface.TYPE_INTEGER: return Long.class;
        case ValueMetaInterface.TYPE_NUMBER: return Double.class;
        // case Value.VALUE_TYPE_SERIALIZABLE: return Serializable.class;
        case ValueMetaInterface.TYPE_STRING: return String.class;
        default: return null;
        }
    }
}
