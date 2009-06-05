package org.pentaho.di.repository.delegates;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.repository.KettleDatabaseRepository;

public class RepositoryValueDelegate extends BaseRepositoryDelegate {

//	private static Class<?> PKG = ValueMetaAndData.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public RepositoryValueDelegate(KettleDatabaseRepository repository) {
		super(repository);
	}

	public RowMetaAndData getValue(long id_value) throws KettleException
	{
		return repository.connectionDelegate.getOneRow(quoteTable(KettleDatabaseRepository.TABLE_R_VALUE), quote(KettleDatabaseRepository.FIELD_VALUE_ID_VALUE), id_value);
	}

	public ValueMetaAndData loadValueMetaAndData(long id_value) throws KettleException
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
