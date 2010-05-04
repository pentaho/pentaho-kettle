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
package org.pentaho.di.repository.kdr.delegates;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;

public class KettleDatabaseRepositoryValueDelegate extends KettleDatabaseRepositoryBaseDelegate {

//	private static Class<?> PKG = ValueMetaAndData.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public KettleDatabaseRepositoryValueDelegate(KettleDatabaseRepository repository) {
		super(repository);
	}

	public RowMetaAndData getValue(ObjectId id_value) throws KettleException
	{
		return repository.connectionDelegate.getOneRow(quoteTable(KettleDatabaseRepository.TABLE_R_VALUE), quote(KettleDatabaseRepository.FIELD_VALUE_ID_VALUE), id_value);
	}

	public ValueMetaAndData loadValueMetaAndData(ObjectId id_value) throws KettleException
    {
		ValueMetaAndData valueMetaAndData = new ValueMetaAndData();
        try
        {
            RowMetaAndData r = getValue(id_value);
            if (r!=null)
            {
                String name    = r.getString(KettleDatabaseRepository.FIELD_VALUE_NAME, null);
                int valtype    = ValueMeta.getType( r.getString(KettleDatabaseRepository.FIELD_VALUE_VALUE_TYPE, null) );
                boolean isNull = r.getBoolean(KettleDatabaseRepository.FIELD_VALUE_IS_NULL, false);
                valueMetaAndData.setValueMeta(new ValueMeta(name, valtype));

                if (isNull)
                {
                	valueMetaAndData.setValueData(null);
                }
                else
                {
                    ValueMetaInterface stringValueMeta = new ValueMeta(name, ValueMetaInterface.TYPE_STRING);
                    ValueMetaInterface valueMeta = valueMetaAndData.getValueMeta();
                    stringValueMeta.setConversionMetadata(valueMeta);
                    
                    valueMeta.setDecimalSymbol(ValueMetaAndData.VALUE_REPOSITORY_DECIMAL_SYMBOL);
                    valueMeta.setGroupingSymbol(ValueMetaAndData.VALUE_REPOSITORY_GROUPING_SYMBOL);
                    
                    switch(valueMeta.getType())
                    {
                    case ValueMetaInterface.TYPE_NUMBER:
                    	valueMeta.setConversionMask(ValueMetaAndData.VALUE_REPOSITORY_NUMBER_CONVERSION_MASK);
                    	break;
                    case ValueMetaInterface.TYPE_INTEGER:
                    	valueMeta.setConversionMask(ValueMetaAndData.VALUE_REPOSITORY_INTEGER_CONVERSION_MASK);
                    	break;
                    default:
                    	break;
                    }
                    
                    String string = r.getString("VALUE_STR", null);
                    valueMetaAndData.setValueData(stringValueMeta.convertDataUsingConversionMetaData(string));
                    
                    // OK, now comes the dirty part...
                    // We want the defaults back on there...
                    //
                    valueMeta = new ValueMeta(name, valueMeta.getType());
                }
            }
            
            return valueMetaAndData;
        }
        catch(KettleException dbe)
        {
            throw new KettleException("Unable to load Value from repository with id_value="+id_value, dbe);
        }
    }
 

}
