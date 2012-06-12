/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.avroinput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.util.Utf8;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

import com.sun.org.apache.bcel.internal.generic.Type;

/**
 * Class providing an input step for reading data from an Avro serialized file or an incoming field. 
 * Handles both container files (where the schema is serialized into the file) and 
 * schemaless files. In the case of the later (and incoming field), the user must supply a schema in order 
 * to read objects from the file/field. In the case of the former, a schema can be optionally 
 * supplied.
 * 
 * Currently supports Avro records, arrays, maps, unions and primitive types. Union types are
 * limited to two base types, where one of the base types must be "null". Paths use the "dot" 
 * notation and "$" indicates the root of the object. Arrays and maps are accessed via "[]" 
 * and differ only in that array elements are accessed via zero-based integer indexes and 
 * map values are accessed by string keys.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
@Step(id = "AvroInput", image = "Avro.png", name = "Avro Input", description="Reads data from an Avro file", categoryDescription="Big Data")
public class AvroInputMeta extends BaseStepMeta implements StepMetaInterface {
  
  protected static Class<?> PKG = AvroInputMeta.class;
  
  /**
   * Inner class encapsulating a field to provide lookup values. Field values (non-avro) 
   * from the incoming row stream can be substituted into the avro paths used to
   * extract avro fields from an incoming binary/json avro field.
   * 
   * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
   *
   */
  public static class LookupField {
    
    /** The name of the field in the incoming rows to use for a lookup */
    public String m_fieldName = "";
    
    /** The name of the variable to hold this field's values */
    public String m_variableName = "";
    
    /** A default value to use if the incoming field is null */
    public String m_defaultValue = "";
    
    protected String m_cleansedVariableName;
    protected String m_resolvedFieldName;
    protected String m_resolvedDefaultValue;
    
    /** False if this field does not exist in the incoming row stream */
    protected boolean m_isValid = true;
    
    /** Index of this field in the incoming row stream */
    protected int m_inputIndex = -1;
    
    protected ValueMetaInterface m_fieldVM;
    
    public boolean init(RowMetaInterface inRowMeta, VariableSpace space) {
//      System.out.println("*** Initializing lookup field...");
      
      m_resolvedFieldName = space.environmentSubstitute(m_fieldName);
      
      m_inputIndex = inRowMeta.indexOfValue(m_resolvedFieldName);
      if (m_inputIndex < 0) {
        m_isValid = false;
        
        return m_isValid;
      }
      
      m_fieldVM = inRowMeta.getValueMeta(m_inputIndex);
      
      if (!Const.isEmpty(m_variableName)) {
        m_cleansedVariableName = m_variableName.replaceAll("\\.", "_");
      } else {
        m_isValid = false;
        return m_isValid;
      }
      
      m_resolvedDefaultValue = space.environmentSubstitute(m_defaultValue);
      
      return m_isValid;
    }        
    
    public void setVariable(VariableSpace space, Object[] inRow) {
      if (!m_isValid) {
        return;
      }
      
//      System.out.println("+++++++++++++ Resolved field name " + m_resolvedFieldName);
      String valueToSet = "";
      try {
        if (m_fieldVM.isNull(inRow[m_inputIndex])) {
          if (!Const.isEmpty(m_resolvedDefaultValue)) {
            valueToSet = m_resolvedDefaultValue;
          } else {
            valueToSet = "null";
          }
        } else {
          valueToSet = m_fieldVM.getString(inRow[m_inputIndex]);
        }
      } catch (KettleValueException e) {
        valueToSet = "null";
      }
      
//      System.out.println("Setting value " + valueToSet);
      space.setVariable(m_cleansedVariableName, valueToSet);
//      System.out.println("++ Value set successfully...");
    }        
  }
  
  /**
   * Inner class for encapsulating name, path and type information for a field to
   * be extracted from an Avro file.
   * 
   * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
   * @version $Revision$
   */
  public static class AvroField {
    
    /** the name that the field will take in the outputted kettle stream */
    public String m_fieldName = "";    
    
    /** the path to the field in the avro file */
    public String m_fieldPath = "";
    
    /** the kettle type for this field */
    public String m_kettleType = "";
    
    /** any indexed values (i.e. enum types in avro) */
    public List<String> m_indexedVals;

    protected int m_outputIndex; // the index that this field is in the output row structure
    private ValueMeta m_tempValueMeta;    
    private List<String> m_pathParts;    
    private List<String> m_tempParts;
    
    /**
     * Initialize this field by parsing the path etc.
     * 
     * @param outputIndex the index in the output row structure for this field
     * @throws KettleException if a problem occurs
     */
    public void init(int outputIndex) throws KettleException {
      if (Const.isEmpty(m_fieldPath)) {
        throw new KettleException(BaseMessages.getString(PKG, "AvroInput.Error.NoPathSet"));
      }
      if (m_pathParts != null) {
        return;
      }
      
//      System.out.println("%%%%% About to cleanse path: " + m_fieldPath);
      String fieldPath = AvroInputData.cleansePath(m_fieldPath);
      
      String[] temp = fieldPath.split("\\.");
      m_pathParts = new ArrayList<String>();
      for (String part : temp) {
        m_pathParts.add(part);
      }
      
      if (m_pathParts.get(0).equals("$")) {
        m_pathParts.remove(0); // root record indicator
      } else if (m_pathParts.get(0).startsWith("$[")) {
        
        // strip leading $ off of array
        String r = m_pathParts.get(0).substring(1, m_pathParts.get(0).length());
        m_pathParts.set(0, r);
      }
      
//      System.out.println("*** (init) Number of path parts " + m_pathParts.size());
//      System.out.println("** " + m_pathParts.get(0));
      
      
      m_tempParts = new ArrayList<String>();
      
      m_tempValueMeta = new ValueMeta();
      m_tempValueMeta.setType(ValueMeta.getType(m_kettleType));
      m_outputIndex = outputIndex;
    }
      
    /**
     * Reset this field. Should be called prior to processing a new field 
     * value from the avro file
     * 
     * @param space environment variables (values that environment variables
     * resolve to cannot contain "."s)
     */
    public void reset(VariableSpace space) {
      // first clear because there may be stuff left over from processing
      // the previous avro object (especially if a path exited early due to
      // non-existent map key or array index out of bounds)
      m_tempParts.clear();
      //m_tempParts.addAll(m_pathParts);
      
      for (String part : m_pathParts) {
        m_tempParts.add(space.environmentSubstitute(part));
      }
    }
    
    protected Object getKettleValue(Object fieldValue) 
      throws KettleException {
      
      switch (m_tempValueMeta.getType()) {
      case ValueMetaInterface.TYPE_BIGNUMBER:
        return m_tempValueMeta.getBigNumber(fieldValue);
      case ValueMetaInterface.TYPE_BINARY:
        return m_tempValueMeta.getBinary(fieldValue);
      case ValueMetaInterface.TYPE_BOOLEAN:
        return m_tempValueMeta.getBoolean(fieldValue);
      case ValueMetaInterface.TYPE_DATE:
        return m_tempValueMeta.getDate(fieldValue);
      case ValueMetaInterface.TYPE_INTEGER:
        return m_tempValueMeta.getInteger(fieldValue);
      case ValueMetaInterface.TYPE_NUMBER:
        return m_tempValueMeta.getNumber(fieldValue);
      case ValueMetaInterface.TYPE_STRING:
        return m_tempValueMeta.getString(fieldValue);
      default:
        return null;
      }      
    }
    
    protected Object getPrimitive(Object fieldValue, Schema s) 
      throws KettleException {      
      
      if (fieldValue == null) {
        return null;
      }
      
      switch(s.getType()) {
      case BOOLEAN:
      case LONG:
      case DOUBLE:
      case BYTES:
      case ENUM:
      case STRING:
        return getKettleValue(fieldValue);
      case INT:
        return getKettleValue(new Long((Integer)fieldValue));
      case FLOAT:
        return getKettleValue(new Double((Float)fieldValue));
      default:
        return null;       
      }      
    }
    
    /**
     * Processes a map at this point in the path.
     * 
     * @param map the map to process
     * @param s the current schema at this point in the path
     * @return the field value or null for out-of-bounds array indexes, non-existent
     * map keys or unsupported avro types.
     * @throws KettleException if a problem occurs
     */
    public Object convertToKettleValue(Map<Utf8, Object> map, Schema s) 
      throws KettleException {
      
      if (map == null) {
        return null;
      }
      
      if (m_tempParts.size() == 0) {
        throw new KettleException(BaseMessages.getString(PKG, "AvroInput.Error.MalformedPathMap"));
      }
      
      String part = m_tempParts.remove(0);
      if (!(part.charAt(0) == '[')) {
        throw new KettleException(BaseMessages.getString(PKG, "AvroInput.Error.MalformedPathMap2", part));
      }
      
      String key = part.substring(1, part.indexOf(']'));
      
      if (part.indexOf(']') < part.length() - 1) {
        // more dimensions to the array/map
        part = part.substring(part.indexOf(']') + 1, part.length());
        m_tempParts.add(0, part);
      }      
      
      Object value = map.get(new Utf8(key));
      if (value == null) {
        return null;
      }
      
      Schema valueType = s.getValueType();
      
      if (valueType.getType() == Schema.Type.UNION) {
        valueType = AvroInputData.checkUnion(valueType);
      }
      
      // what have we got?
      if (valueType.getType() == Schema.Type.RECORD) {
        return convertToKettleValue((GenericData.Record)value, valueType);
      } else if (valueType.getType() == Schema.Type.ARRAY) {
        return convertToKettleValue((GenericData.Array)value, valueType);
      } else if (valueType.getType() == Schema.Type.MAP) {
        return convertToKettleValue((Map<Utf8, Object>)value, valueType);
      } else {
        // assume a primitive
        return getPrimitive(value, valueType);
      }      
    }
    
    /**
     * Processes an array at this point in the path.
     * 
     * @param array the array to process
     * @param s the current schema at this point in the path
     * @return the field value or null for out-of-bounds array indexes, non-existent
     * map keys or unsupported avro types.
     * @throws KettleException if a problem occurs
     */
    public Object convertToKettleValue(GenericData.Array array, Schema s) 
      throws KettleException {
      
      if (array == null) {
        return null;
      }
      
      if (m_tempParts.size() == 0) {
        throw new KettleException(BaseMessages.getString(PKG, "AvroInput.Error.MalformedPathArray"));        
      }
      
      String part = m_tempParts.remove(0);
      if (!(part.charAt(0) == '[')) {
        throw new KettleException(BaseMessages.getString(PKG, "AvroInput.Error.MalformedPathArray2", part));               
      }
      
      String index = part.substring(1, part.indexOf(']'));
      int arrayI = 0;
      try {
        arrayI = Integer.parseInt(index.trim());        
      } catch (NumberFormatException e) {
        throw new KettleException(BaseMessages.getString(PKG, "AvroInput.Error.UnableToParseArrayIndex", index));
      }
      
      if (part.indexOf(']') < part.length() - 1) {
        // more dimensions to the array
        part = part.substring(part.indexOf(']') + 1, part.length());
        m_tempParts.add(0, part);
      }
            
      if (arrayI >= array.size() || arrayI < 0) {
//        System.out.println("*** array index " + arrayI + " array size " + array.size());
        return null;
      }
      
      Object element = array.get(arrayI);
      Schema elementType = s.getElementType();
      
      if (element == null) {
        return null;
      }
      
      if (elementType.getType() == Schema.Type.UNION) {
        elementType = AvroInputData.checkUnion(elementType);
      }
      
      // what have we got?
      if (elementType.getType() == Schema.Type.RECORD) {
        return convertToKettleValue((GenericData.Record)element, elementType);
      } else if (elementType.getType() == Schema.Type.ARRAY) {
        return convertToKettleValue((GenericData.Array)element, elementType);
      } else if (elementType.getType() == Schema.Type.MAP) {
        return convertToKettleValue((Map<Utf8, Object>)element, elementType);
      } else {
        // assume a primitive until we handle fixed types
        return getPrimitive(element, elementType);
      }      
    }
    
    /**
     * Processes a record at this point in the path.
     * 
     * @param record the record to process
     * @param s the current schema at this point in the path
     * @return the field value or null for out-of-bounds array indexes, non-existent
     * map keys or unsupported avro types.
     * @throws KettleException if a problem occurs
     */
    public Object convertToKettleValue(GenericData.Record record, Schema s) 
      throws KettleException {
      
      if (record == null) {
        return null;
      }
      
      if (m_tempParts.size() == 0) {
        throw new KettleException(BaseMessages.getString(PKG, "AvroInput.Error.MalformedPathRecord"));
      }
      
      String part = m_tempParts.remove(0);
      if (part.charAt(0) == '[') {
        throw new KettleException(BaseMessages.getString(PKG, "AvroInput.Error.InvalidPath") + m_tempParts);
      }
      
      if (part.indexOf('[') > 0) {
        String arrayPart = part.substring(part.indexOf('['));
        part = part.substring(0, part.indexOf('['));
        
        // put the array section back into location zero
        m_tempParts.add(0, arrayPart);
      }
      
      // part is a named field of the record
      Schema.Field fieldS = s.getField(part);
//      System.out.println("Record part: " + part);
      if (fieldS == null) {
        throw new KettleException(BaseMessages.getString(PKG, "AvroInput.Error.NonExistentField", part));
      }      
      Object field = record.get(part);
      
      if (field == null) {
        return null;
      }
      
      Schema.Type fieldT = fieldS.schema().getType();
      Schema fieldSchema = fieldS.schema();
      
      if (fieldT == Schema.Type.UNION) {
        // get the non-null part of the union
        fieldSchema = AvroInputData.checkUnion(fieldSchema);
        fieldT = fieldSchema.getType();
      }
      
      // what have we got?
      if (fieldT == Schema.Type.RECORD) {
        //System.out.println("Calling record...");
        return convertToKettleValue((GenericData.Record)field, fieldSchema);
      } else if (fieldT == Schema.Type.ARRAY) {
        //System.out.println("Calling array...");
        return convertToKettleValue((GenericData.Array)field, fieldSchema);
      } else if (fieldT == Schema.Type.MAP) {
        //System.out.println("Calling map...");
        return convertToKettleValue((Map<Utf8, Object>)field, fieldSchema);
      } else {
        // assume primitive until we handle fixed types
//        System.out.println("Calling primitive...");
        return getPrimitive(field, fieldSchema);
      }      
    }    
  }
  
  /** The avro file to read */
  protected String m_filename = "";
  
  /** The schema to use if not reading from a container file */
  protected String m_schemaFilename = "";
  
  /** True if the user's avro file is json encoded rather than binary */
  protected boolean m_isJsonEncoded = false;
  
  /** True if the avro to be decoded is contained in an incoming field */
  protected boolean m_avroInField = false;
  
  /** Holds the source field name (if decoding from an incoming field) */
  protected String m_avroFieldName = "";
  
  /** The fields to emit */
  protected List<AvroField> m_fields;
  
  /** Incoming field values to use for lookup/substitution in avro paths */
  protected List<LookupField> m_lookups;
  
  /**
   * Set whether the avro to be decoded is contained in an incoming field
   * 
   * @param a true if the avro to be decoded is contained in an incoming field
   */
  public void setAvroInField(boolean a) {
    m_avroInField = a;
  }
  
  /**
   * Get whether the avro to be decoded is contained in an incoming field
   * 
   * @return true if the avro to be decoded is contained in an incoming field
   */
  public boolean getAvroInField() {
    return m_avroInField;
  }
  
  /**
   * Set the name of the incoming field to decode avro from (if decoding from a
   * field rather than a file)
   * 
   * @param f the name of the incoming field to decode from
   */
  public void setAvroFieldName(String f) {
    m_avroFieldName = f;
  }
  
  /**
   * Get the name of the incoming field to decode avro from (if decoding from a
   * field rather than a file)
   * 
   * @return the name of the incoming field to decode from
   */
  public String getAvroFieldName() {
    return m_avroFieldName;
  }
  
  /**
   * Set the avro filename
   * 
   * @param filename the avro filename
   */
  public void setFilename(String filename) {
    m_filename = filename;
  }

  /**
   * Get the avro filename
   * 
   * @return the avro filename
   */
  public String getFilename() {
    return m_filename;
  }
  
  /**
   * Set the schema filename to use
   * 
   * @param schemaFile the name of the schema file to use
   */
  public void setSchemaFilename(String schemaFile) {
    m_schemaFilename = schemaFile;
  }
  
  /**
   * Get the schema filename to use
   * 
   * @return the name of the schema file to use
   */
  public String getSchemaFilename() {
    return m_schemaFilename;
  }
  
  /**
   * Get whether the avro file to read is json encoded rather 
   * than binary
   * 
   * @return true if the file to read is json encoded
   */
  public boolean getAvroIsJsonEncoded() {
    return m_isJsonEncoded;
  }
  
  /**
   * Set whether the avro file to read is json encoded rather 
   * than binary
   * 
   * @param j true if the file to read is json encoded
   */
  public void setAvroIsJsonEncoded(boolean j) {
    m_isJsonEncoded = j;
  }
  
  /**
   * Set the Avro fields that will be extracted
   * 
   * @param fields the Avro fields that will be extracted
   */
  public void setAvroFields(List<AvroField> fields) {
    m_fields = fields;
  }
  
  /**
   * Get the Avro fields that will be extracted
   * 
   * @return the Avro fields that will be extracted
   */
  public List<AvroField> getAvroFields() {
    return m_fields;
  }
  
  /**
   * Get the incoming field values that will be used for lookup/substitution in 
   * the avro paths
   * 
   * @return the lookup fields
   */
  public List<LookupField> getLookupFields() {
    return m_lookups;
  }
  
  /**
   * Set the incoming field values that will be used for lookup/substitution in 
   * the avro paths
   * 
   * @param lookups the lookup fields
   */
  public void setLookupFields(List<LookupField> lookups) {
    m_lookups = lookups;
  }
  
  public void getFields(RowMetaInterface rowMeta, String origin,
      RowMetaInterface[] info, StepMeta nextStep, VariableSpace space)
  throws KettleStepException {
    
    //rowMeta.clear();
    
    List<AvroField> fieldsToOutput = null;
    
    if (m_fields != null && m_fields.size() > 0) {
      // we have some stored field info - use this
      fieldsToOutput = m_fields;      
    } else {
      // outputting all fields from either supplied schema or schema embedded
      // in a container file
      
      if (!Const.isEmpty(getSchemaFilename())) {
        String fn = space.environmentSubstitute(m_schemaFilename);
        
        try {
          Schema s = AvroInputData.loadSchema(fn);
          fieldsToOutput = AvroInputData.getLeafFields(s);          
        } catch (KettleException e) {
          throw new KettleStepException(BaseMessages.getString(PKG, 
              "AvroInput.Error.UnableToLoadSchema", fn), e);
        }
      } else {
        
        if (m_avroInField) {
          throw new KettleStepException(BaseMessages.getString(PKG, 
              "AvroInput.Error.NoSchemaSupplied"));
        }
        // assume a container file and grab from there...
        String avroFilename = m_filename;
        avroFilename = space.environmentSubstitute(avroFilename);
        try {
          Schema s = AvroInputData.loadSchemaFromContainer(avroFilename);
          fieldsToOutput = AvroInputData.getLeafFields(s);
        } catch (KettleException e) {
          throw new KettleStepException(BaseMessages.
              getString(PKG, "AvroInput.Error.UnableToLoadSchemaFromContainerFile", avroFilename));
        }
      }
    }
    
    for (AvroField f : fieldsToOutput) {
      ValueMetaInterface vm = new ValueMeta();
      vm.setName(f.m_fieldName);
      vm.setOrigin(origin);
      vm.setType(ValueMeta.getType(f.m_kettleType));
      if (f.m_indexedVals != null) {
        vm.setIndex(f.m_indexedVals.toArray()); // indexed values
      }
      rowMeta.addValueMeta(vm);
    }
  }

  public void check(List<CheckResultInterface> remarks, TransMeta transMeta,
      StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output,
      RowMetaInterface info) {
  }

  public StepInterface getStep(StepMeta stepMeta,
      StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans) {
    
    return new AvroInput(stepMeta, stepDataInterface, copyNr,
        transMeta, trans);
  }

  public StepDataInterface getStepData() {
    return new AvroInputData();
  }
  
  protected static String indexedValsList(List<String> indexedVals) {
    StringBuffer temp = new StringBuffer();
    
    for (int i = 0; i < indexedVals.size(); i++) {
      temp.append(indexedVals.get(i));
      if (i < indexedVals.size() - 1){
        temp.append(",");
      }
    }
    
    return temp.toString();
  }
  
  protected static List<String> indexedValsList(String indexedVals) {
    
    String[] parts = indexedVals.split(",");
    List<String> list = new ArrayList<String>();
    for (String s : parts) {
      list.add(s.trim());
    }
    
    return list;
  }
  
  public String getXML() {
    StringBuffer retval = new StringBuffer();
    
    if (!Const.isEmpty(m_filename)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("avro_filename", 
          m_filename));
    }
    
    if (!Const.isEmpty(m_schemaFilename)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("schema_filename", 
          m_schemaFilename));
    }
    
    retval.append("\n    ").append(XMLHandler.addTagValue("json_encoded", 
        m_isJsonEncoded));
    
    retval.append("\n    ").append(XMLHandler.addTagValue("avro_in_field", 
        m_avroInField));
    
    if (!Const.isEmpty(m_avroFieldName)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("avro_field_name", 
          m_avroFieldName));
    }
    
    if (m_fields != null && m_fields.size() > 0) {
      retval.append("\n    ").append(XMLHandler.openTag("avro_fields"));
      
      for (AvroField f : m_fields) {
        retval.append("\n      ").append(XMLHandler.openTag("avro_field"));
        
        retval.append("\n        ").append(XMLHandler.addTagValue("field_name", f.m_fieldName));
        retval.append("\n        ").append(XMLHandler.addTagValue("field_path", f.m_fieldPath));
        retval.append("\n        ").append(XMLHandler.addTagValue("field_type", f.m_kettleType));
        if (f.m_indexedVals != null && f.m_indexedVals.size() > 0) {
          retval.append("\n        ").append(XMLHandler.addTagValue("indexed_vals", indexedValsList(f.m_indexedVals)));
        }
        retval.append("\n      ").append(XMLHandler.closeTag("avro_field"));
      }
      
      retval.append("\n    ").append(XMLHandler.closeTag("avro_fields"));
    }
    
    if (m_lookups != null && m_lookups.size() > 0) {
      retval.append("\n    ").append(XMLHandler.openTag("lookup_fields"));
      
      for (LookupField f : m_lookups) {
        retval.append("\n      ").append(XMLHandler.openTag("lookup_field"));
        
        retval.append("\n        ").append(XMLHandler.addTagValue("lookup_field_name", f.m_fieldName));
        retval.append("\n        ").append(XMLHandler.addTagValue("variable_name", f.m_variableName));
        retval.append("\n        ").append(XMLHandler.addTagValue("default_value", f.m_defaultValue));
        
        retval.append("\n      ").append(XMLHandler.closeTag("lookup_field"));
      }
      
      retval.append("\n    ").append(XMLHandler.closeTag("lookup_fields"));
    }
    
    return retval.toString();
  }

  public void loadXML(Node stepnode, List<DatabaseMeta> databases,
      Map<String, Counter> counters) throws KettleXMLException {
    m_filename = XMLHandler.getTagValue(stepnode, "avro_filename");
    m_schemaFilename = XMLHandler.getTagValue(stepnode, "schema_filename");
    
    String jsonEnc = XMLHandler.getTagValue(stepnode, "json_encoded");
    if (!Const.isEmpty(jsonEnc)) {
      m_isJsonEncoded = jsonEnc.equalsIgnoreCase("Y");
    }
    
    String avroInField = XMLHandler.getTagValue(stepnode, "avro_in_field");
    if (!Const.isEmpty(avroInField)) {
      m_avroInField = avroInField.equalsIgnoreCase("Y");
    }
    m_avroFieldName = XMLHandler.getTagValue(stepnode, "avro_field_name");
    
    
    Node fields = XMLHandler.getSubNode(stepnode, "avro_fields");
    if (fields != null && XMLHandler.countNodes(fields, "avro_field") > 0) {
      int nrfields = XMLHandler.countNodes(fields, "avro_field");
      
      m_fields = new ArrayList<AvroField>();
      for (int i = 0; i < nrfields; i++) {
        Node fieldNode = XMLHandler.getSubNodeByNr(fields, "avro_field", i);
        
        AvroField newField = new AvroField();
        newField.m_fieldName = XMLHandler.getTagValue(fieldNode, "field_name");
        newField.m_fieldPath = XMLHandler.getTagValue(fieldNode, "field_path");
        newField.m_kettleType = XMLHandler.getTagValue(fieldNode, "field_type");
        String indexedVals = XMLHandler.getTagValue(fieldNode, "indexed_vals");
        if (indexedVals != null && indexedVals.length() > 0) {
          newField.m_indexedVals = indexedValsList(indexedVals);
        }
        
        m_fields.add(newField);
      }
    }
    
    Node lFields = XMLHandler.getSubNode(stepnode, "lookup_fields");
    if (lFields != null && XMLHandler.countNodes(lFields, "lookup_field") > 0) {
      int nrfields = XMLHandler.countNodes(lFields, "lookup_field");
      
      m_lookups = new ArrayList<LookupField>();      
      
      for (int i = 0; i < nrfields; i++) {
        Node fieldNode = XMLHandler.getSubNodeByNr(lFields, "lookup_field", i);
        
        LookupField newField = new LookupField();
        newField.m_fieldName = XMLHandler.getTagValue(fieldNode, "lookup_field_name");
        newField.m_variableName = XMLHandler.getTagValue(fieldNode, "variable_name");
        newField.m_defaultValue = XMLHandler.getTagValue(fieldNode, "default_value");
        
        m_lookups.add(newField);
      }
    }
  }

  public void readRep(Repository rep, ObjectId id_step,
      List<DatabaseMeta> databases, Map<String, Counter> counters)
    throws KettleException {
    
    m_filename = rep.getStepAttributeString(id_step, 0, "avro_filename");
    m_schemaFilename = rep.getStepAttributeString(id_step, 0, "schema_filename");
    
    m_isJsonEncoded = rep.getStepAttributeBoolean(id_step, 0, "json_encoded");
    
    m_avroInField = rep.getStepAttributeBoolean(id_step, 0, "avro_in_field");
    m_avroFieldName = rep.getStepAttributeString(id_step, 0, "avro_field_name");
    
    int nrfields = rep.countNrStepAttributes(id_step, "field_name");
    if (nrfields > 0) {
      m_fields = new ArrayList<AvroField>();
      
      for (int i = 0; i < nrfields; i++) {
        AvroField newField = new AvroField();
        
        newField.m_fieldName = 
          rep.getStepAttributeString(id_step, i, "field_name");
        newField.m_fieldPath = 
          rep.getStepAttributeString(id_step, i, "field_path");
        newField.m_kettleType = 
          rep.getStepAttributeString(id_step, i, "field_type");
        String indexedVals = 
            rep.getStepAttributeString(id_step, i, "indexed_vals");        
        if (indexedVals != null && indexedVals.length() > 0) {
          newField.m_indexedVals = indexedValsList(indexedVals);
        }
        
        m_fields.add(newField);
      }
    }
    
    nrfields = rep.countNrStepAttributes(id_step, "lookup_field_name");
    if (nrfields > 0) {
      m_lookups = new ArrayList<LookupField>();
      
      for (int i = 0; i < nrfields; i++) {
        LookupField newField = new LookupField();
        
        newField.m_fieldName =
          rep.getStepAttributeString(id_step, i, "lookup_field_name");
        newField.m_variableName =
          rep.getStepAttributeString(id_step, i, "variable_name");
        newField.m_defaultValue =
          rep.getStepAttributeString(id_step, i, "default_value");
        
        m_lookups.add(newField);
      }
    }
  }

  public void saveRep(Repository rep, ObjectId id_transformation,
      ObjectId id_step) throws KettleException {
    
    if (!Const.isEmpty(m_filename)) {
      rep.saveStepAttribute(id_transformation, id_step, 0, "avro_filename", 
          m_filename);
    }
    if (!Const.isEmpty(m_schemaFilename)) {
      rep.saveStepAttribute(id_transformation, id_step, 0, "schema_filename", 
          m_schemaFilename);
    }
    
    rep.saveStepAttribute(id_transformation, id_step, 0, "json_encoded", m_isJsonEncoded);
    
    rep.saveStepAttribute(id_transformation, id_step, 0, "avro_in_field", m_avroInField);
    if (!Const.isEmpty(m_avroFieldName)) {
      rep.saveStepAttribute(id_transformation, id_step, 0, "avro_field_name", 
          m_avroFieldName);
    }

    if (m_fields != null && m_fields.size() > 0) {
      for (int i = 0; i < m_fields.size(); i++) {
        AvroField f = m_fields.get(i);
        
        rep.saveStepAttribute(id_transformation, id_step, i, "field_name", 
            f.m_fieldName);
        rep.saveStepAttribute(id_transformation, id_step, i, "field_path", 
            f.m_fieldPath);
        rep.saveStepAttribute(id_transformation, id_step, i, "field_type", 
            f.m_kettleType);
        if (f.m_indexedVals != null && f.m_indexedVals.size() > 0) {
          String indexedVals = indexedValsList(f.m_indexedVals);
          
          rep.saveStepAttribute(id_transformation, id_step, i, "indexed_vals", 
              indexedVals);
        }
      }
    }
    
    if (m_lookups != null && m_lookups.size() > 0) {
      for (int i = 0; i < m_lookups.size(); i++) {
        LookupField f = m_lookups.get(i);
        
        rep.saveStepAttribute(id_transformation, id_step, i, "lookup_field_name", 
            f.m_fieldName);
        rep.saveStepAttribute(id_transformation, id_step, i, "variable_name", 
            f.m_variableName);
        rep.saveStepAttribute(id_transformation, id_step, i, "default_value", 
            f.m_defaultValue);
      }
    }
  }

  public void setDefault() {

  }
  
  /* (non-Javadoc)
   * @see org.pentaho.di.trans.step.BaseStepMeta#getDialogClassName()
   */
  public String getDialogClassName() {
    return "org.pentaho.di.trans.steps.avroinput.AvroInputDialog";
  }  

  public boolean supportsErrorHandling() {
    return true;
  }
}
