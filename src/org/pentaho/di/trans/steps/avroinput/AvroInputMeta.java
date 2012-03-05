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

/**
 * Class providing an input step for reading data from an Avro serialized file. 
 * Handles both container files (where the schema is serialized into the file) and 
 * schemaless files. In the case of the later, the user must supply a schema in order 
 * to read objects from the file. In the case of the former, a schema can be optionally 
 * supplied.
 * 
 * Currently supports Avro records, arrays, maps and primitive types. Paths use the "dot" 
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
    
    private ValueMeta m_tempValueMeta;    
    private List<String> m_pathParts;    
    private List<String> m_tempParts;
    
    /**
     * Initialize this field by parsing the path etc.
     * 
     * @throws KettleException if a problem occurs
     */
    public void init() throws KettleException {
      if (Const.isEmpty(m_fieldPath)) {
        throw new KettleException("No path has been set!");
      }
      if (m_pathParts != null) {
        return;
      }
      
      String[] temp = m_fieldPath.split("\\.");
      m_pathParts = new ArrayList<String>();
      for (String part : temp) {
        m_pathParts.add(part);
      }
      
      if (m_pathParts.get(0).equals("$")) {
        m_pathParts.remove(0); // root record indicator
      } else if (m_pathParts.get(0).startsWith("$")) {
        
        // strip leading $ off of array
        String r = m_pathParts.get(0).substring(1, m_pathParts.get(0).length());
        m_pathParts.set(0, r);
      }
      m_tempParts = new ArrayList<String>();
      
      m_tempValueMeta = new ValueMeta();
      m_tempValueMeta.setType(ValueMeta.getType(m_kettleType));
    }
      
    /**
     * Reset this field. Should be called prior to processing a new field 
     * value from the avro file
     */
    public void reset() {
      m_tempParts.addAll(m_pathParts);
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
        return null;
      }
      
      Object element = array.get(arrayI);
      Schema elementType = s.getElementType();
      
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
      if (fieldS == null) {
        throw new KettleException(BaseMessages.getString(PKG, "AvroInput.Error.NonExistentField", part));
      }      
      Object field = record.get(part);
      
      // what have we got?
      if (fieldS.schema().getType() == Schema.Type.RECORD) {
        return convertToKettleValue((GenericData.Record)field, fieldS.schema());
      } else if (fieldS.schema().getType() == Schema.Type.ARRAY) {
        return convertToKettleValue((GenericData.Array)field, fieldS.schema());
      } else if (fieldS.schema().getType() == Schema.Type.MAP) {
        
        return convertToKettleValue((Map<Utf8, Object>)field, fieldS.schema());
      } else {
        // assume primitive until we handle fixed types
        return getPrimitive(field, fieldS.schema());
      }      
    }
  }
  
  /** The avro file to read */
  protected String m_filename = "";
  
  /** The schema to use if not reading from a container file */
  protected String m_schemaFilename = "";
  
  /** The fields to emit */
  protected List<AvroField> m_fields;
  
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
  
  public void getFields(RowMetaInterface rowMeta, String origin,
      RowMetaInterface[] info, StepMeta nextStep, VariableSpace space)
  throws KettleStepException {
    
    rowMeta.clear();
    
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
          throw new KettleStepException("Unable to load schema from file '" + fn + "'", e);
        }
      } else {
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
    
    return retval.toString();
  }

  public void loadXML(Node stepnode, List<DatabaseMeta> databases,
      Map<String, Counter> counters) throws KettleXMLException {
    m_filename = XMLHandler.getTagValue(stepnode, "avro_filename");
    m_schemaFilename = XMLHandler.getTagValue(stepnode, "schema_filename");
    
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
  }

  public void readRep(Repository rep, ObjectId id_step,
      List<DatabaseMeta> databases, Map<String, Counter> counters)
    throws KettleException {
    
    m_filename = rep.getStepAttributeString(id_step, 0, "avro_filename");
    m_schemaFilename = rep.getStepAttributeString(id_step, 0, "schema_filename");
    
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
  }

  public void setDefault() {

  }
  
  /**
   * Get the UI for this step.
   *
   * @param shell a <code>Shell</code> value
   * @param meta a <code>StepMetaInterface</code> value
   * @param transMeta a <code>TransMeta</code> value
   * @param name a <code>String</code> value
   * @return a <code>StepDialogInterface</code> value
   */
  public StepDialogInterface getDialog(Shell shell, 
                                       StepMetaInterface meta,
                                       TransMeta transMeta, 
                                       String name) {
   

    return new AvroInputDialog(shell, meta, transMeta, name);
  }

}
