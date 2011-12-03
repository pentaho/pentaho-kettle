/*
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.ui.trans.steps.hadoopenter;

import org.pentaho.di.trans.steps.hadoopenter.HadoopEnterMeta;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class HadoopEnterMetaMapper extends XulEventSourceAdapter {
  
  private class FieldPositions {
    private int key;
    private int value;
    
    public FieldPositions(String[] fieldnames) {
      setKeyIndex(-1);
      setValueIndex(-1);
      
      // Determine the key and value field indices
      if(fieldnames != null && fieldnames.length == 2) {
        for(int index = 0; index < fieldnames.length; index++) {
          if(fieldnames[index].equals(HadoopEnterMeta.KEY_FIELDNAME)) {
            setKeyIndex(index);
          } else if(fieldnames[index].equals(HadoopEnterMeta.VALUE_FIELDNAME)) {
            setValueIndex(index);
          }
        }
      }
    }

    public void setKeyIndex(int key) {
      this.key = key;
    }

    public int getKeyIndex() {
      return key;
    }

    public void setValueIndex(int value) {
      this.value = value;
    }

    public int getValueIndex() {
      return value;
    }
    
    public boolean isValid() {
      return ((getKeyIndex() >= 0 && getValueIndex() >= 0) && getKeyIndex() != getValueIndex());
    }
  }
  
  public static String IN_KEY_TYPE = "in-key-type";
  public static String IN_KEY_LENGTH = "in-key-length";
  public static String IN_KEY_PRECISION = "in-key-precision";
  
  public static String IN_VALUE_TYPE = "in-value-type";
  public static String IN_VALUE_LENGTH = "in-value-length";
  public static String IN_VALUE_PRECISION = "in-value-precision";

  
  private int inKeyType = -1;
  private int inKeyLength = -1;
  private int inKeyPrecision = -1;
  
  private int inValueType = -1;
  private int inValueLength = -1;
  private int inValuePrecision = -1;
  
  
  public void setInKeyType(int arg) {
    int previousVal = inKeyType;
    inKeyType = arg;
    firePropertyChange(IN_KEY_TYPE, previousVal, inKeyType);
  }
  
  public void setInKeyLength(int arg) {
    int previousVal = inKeyLength;
    inKeyLength = arg;
    firePropertyChange(IN_KEY_LENGTH, previousVal, inKeyLength);
  }
  
  public void setInKeyPrecision(int arg) {
    int previousVal = inKeyPrecision;
    inKeyPrecision = arg;
    firePropertyChange(IN_KEY_PRECISION, previousVal, inKeyPrecision);
  }
  
  public void setInValueType(int arg) {
    int previousVal = inValueType;
    inValueType = arg;
    firePropertyChange(IN_VALUE_TYPE, previousVal, inValueType);
  }
  
  public void setInValueLength(int arg) {
    int previousVal = inValueLength;
    inValueLength = arg;
    firePropertyChange(IN_VALUE_LENGTH, previousVal, inValueLength);
  }
  
  public void setInValuePrecision(int arg) {
    int previousVal = inValuePrecision;
    inValuePrecision = arg;
    firePropertyChange(IN_VALUE_PRECISION, previousVal, inValuePrecision);
  }
  
  public int getInKeyType() {
    return inKeyType;
  }
  
  public int getInKeyLength() {
    return inKeyLength;
  }
  
  public int getInKeyPrecision() {
    return inKeyPrecision;
  }
  
  public int getInValueType() {
    return inValueType;
  }
  
  public int getInValueLength() {
    return inValueLength;
  }
  
  public int getInValuePrecision() {
    return inValuePrecision;
  }
  
  /**
   * Load data into the MetaMapper from the HadoopExitMeta
   * @param meta
   */
  public void loadMeta(HadoopEnterMeta meta) {
    FieldPositions fields = new FieldPositions(meta.getFieldname());
    
    if(!fields.isValid()) {
      // We require both the key and value fields to be present
      return;
    }
    
    int[] type = meta.getType();
    int[] length = meta.getLength();
    int[] precision = meta.getPrecision();
    
    setInKeyType(type[fields.getKeyIndex()]);
    setInKeyLength(length[fields.getKeyIndex()]);
    setInKeyPrecision(precision[fields.getKeyIndex()]);
    
    setInValueType(type[fields.getValueIndex()]);
    setInValueLength(length[fields.getValueIndex()]);
    setInValuePrecision(precision[fields.getValueIndex()]);
  }
  
  /**
   * Save data from the MetaMapper into the HadoopExitMeta
   * @param meta
   */
  public void saveMeta(HadoopEnterMeta meta) {
    // Set outKey
    FieldPositions fields = new FieldPositions(meta.getFieldname());
    
    if(!fields.isValid()) {
      // Replace the field names with the key / value names
      meta.allocate(2);
      
      fields.setKeyIndex(0);
      fields.setValueIndex(1);
      
      meta.getFieldname()[fields.getKeyIndex()] = HadoopEnterMeta.KEY_FIELDNAME;
      meta.getFieldname()[fields.getValueIndex()] = HadoopEnterMeta.VALUE_FIELDNAME;
      
      meta.setChanged();
    }
    
    int[] type = new int[2];
    int[] length = new int[2];
    int[] precision = new int[2];
    
    // Set Types
    if(getInKeyType() >= 0) {
      type[fields.getKeyIndex()] = getInKeyType();
    }
    
    if(getInValueType() >= 0) {
      type[fields.getValueIndex()] = getInValueType();
    }
    
    int[] metaType = meta.getType();
    if(metaType == null || metaType.length != 2) {
      meta.setChanged();
    }
    
    for(int index = 0; index < type.length; index++) {
      if(type[index] != metaType[index]) {
        meta.setChanged(true);
      }
    }
    
    meta.setType(type);
    
    // Set Lengths
    if(getInKeyLength() >= 0) {
      length[fields.getKeyIndex()] = getInKeyLength();
    }
    
    if(getInValueLength() >= 0) {
      length[fields.getValueIndex()] = getInValueLength();
    }
    
    int[] metaLength = meta.getLength();
    if(metaLength == null || metaLength.length != 2) {
      meta.setChanged();
    }
    
    for(int index = 0; index < length.length; index++) {
      if(length[index] != metaLength[index]) {
        meta.setChanged(true);
      }
    }
    
    meta.setLength(length);
    
    // Set Precisions
    if(getInKeyPrecision() >= 0) {
      precision[fields.getKeyIndex()] = getInKeyPrecision();
    }
    
    if(getInValuePrecision() >= 0) {
      precision[fields.getValueIndex()] = getInValuePrecision();
    }
    
    int[] metaPrecision = meta.getPrecision();
    if(metaPrecision == null || metaPrecision.length != 2) {
      meta.setChanged();
    }
    
    for(int index = 0; index < precision.length; index++) {
      if(precision[index] != metaPrecision[index]) {
        meta.setChanged(true);
      }
    }
    
    meta.setPrecision(type);
  }
}
