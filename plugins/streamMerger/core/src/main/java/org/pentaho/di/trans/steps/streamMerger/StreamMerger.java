package org.pentaho.di.trans.steps.streamMerger;

import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_STRING;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;

public class StreamMerger {
    RowMetaInterface row;  // resolved row meta
    int[][] mapping;
    Set<Integer> convertToString = new HashSet<Integer>();

    public StreamMerger(RowMetaInterface info[]) throws KettlePluginException {
        unionMerge(info);
    }

    private void unionMerge(RowMetaInterface info[]) throws KettlePluginException {
        mapping = new int[info.length][];
        RowMetaInterface base = null;
        for (int i = 0; i < info.length; i++) {
            if (info[i] != null) {  
                base = info[i].clone();
                break;
            }
        }
        
        HashSet<String> fieldNames = new HashSet<String>();
        Collections.addAll(fieldNames, base.getFieldNames());

        for (int i = 0; i < info.length; i++) {
            int[] rowMapping = null;
            if (info[i] != null) {  
                rowMapping = new int[info[i].size()];
                for (int x = 0; x < rowMapping.length; x++) {
                    ValueMetaInterface field = info[i].getValueMeta(x);
                    String name = field.getName();
                    if (!fieldNames.contains(name)) {
                        base.addValueMeta(field);
                        fieldNames.add(name);
                    }
                    int basePosition = base.indexOfValue(name);
                    rowMapping[x] = basePosition;  

                    ValueMetaInterface baseField = base.getValueMeta(basePosition);
                    if (baseField.getType() != field.getType()) {
                        ValueMetaInterface updatedField = ValueMetaFactory.cloneValueMeta(baseField, TYPE_STRING);
                        base.setValueMeta(basePosition, updatedField);
                        convertToString.add(basePosition);  // we need to change the data type of these fields
                    }
                }
            }
            mapping[i] = rowMapping; 
        }
        row = base; 
    }


    public int[][] getMapping() {
        return mapping;
    }


    public RowMetaInterface getRowMeta() {
        return row;
    }


    public Set<Integer> getConvertToString() {
        return convertToString;
    }
}
