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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.util.Utf8;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Data class for the AvroInput step. Contains methods to determine the type of 
 * Avro file (i.e. container or just serialized objects), extract all the leaf 
 * fields from the object structure described in the schema and convert Avro leaf 
 * fields to kettle values.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 *
 */
public class AvroInputData extends BaseStepData implements StepDataInterface {
  
  /** The output data format */
  protected RowMetaInterface m_outputRowMeta;
  
  /** For reading container files - will be null if file is not a container file*/
  protected DataFileStream m_containerReader;
  
  /** For reading from files of just serialized objects */
  protected GenericDatumReader m_datumReader;
  protected BinaryDecoder m_decoder;
  protected InputStream m_inStream;
  
  /** The schema used to write the file - will be null if the file is not a container file */
  protected Schema m_writerSchema;
  
  /** The final schema to use for extracting values */
  protected Schema m_schemaToUse;
  
  /** If the top level is a record */
  protected GenericData.Record m_topLevelRecord;
  
  /** If the top level is an array */
  protected GenericData.Array m_topLevelArray;
  
  /** If the top level is a map */
  protected Map<Utf8, Object> m_topLevelMap;
  
  protected List<AvroInputMeta.AvroField> m_fields;
    
  /**
   * Get the output row format
   * 
   * @return the output row format
   */
  public RowMetaInterface getOutputRowMeta() {
    return m_outputRowMeta;
  }
  
  protected static AvroInputMeta.AvroField createAvroField(String path, Schema s) {
    AvroInputMeta.AvroField newField = new AvroInputMeta.AvroField();
    // newField.m_fieldName = s.getName(); // this will set the name to the primitive type if the schema is for a primitive
    newField.m_fieldName = path; // set the name to the path, so that for primitives within arrays we can at least distinguish among them
    newField.m_fieldPath = path;// + "." + s.getName();
    switch (s.getType()) {
    case BOOLEAN:
      newField.m_kettleType = ValueMeta.getAllTypes()[ValueMetaInterface.TYPE_BOOLEAN];
      break;
    case ENUM:
    case STRING: {
      newField.m_kettleType = ValueMeta.getAllTypes()[ValueMetaInterface.TYPE_STRING];
      if (s.getType() ==  Schema.Type.ENUM) {
        newField.m_indexedVals = s.getEnumSymbols();
      }
    }
    break;
    case FLOAT:
    case DOUBLE:
      newField.m_kettleType = ValueMeta.getAllTypes()[ValueMetaInterface.TYPE_NUMBER];
      break;
    case INT:
    case LONG:
      newField.m_kettleType = ValueMeta.getAllTypes()[ValueMetaInterface.TYPE_INTEGER];
      break;
    case BYTES:
      newField.m_kettleType = ValueMeta.getAllTypes()[ValueMetaInterface.TYPE_BINARY];
      break;
      // TODO: more types - fixed is probably the only other type we could handle
    default:
      // unhandled type
      newField = null;
    }

    return newField;
  }
  
  protected static List<AvroInputMeta.AvroField> getLeafFields(Schema s) {
    List<AvroInputMeta.AvroField> fields = new ArrayList<AvroInputMeta.AvroField>();
    
    String root = "$";
    
    if (s.getType() == Schema.Type.ARRAY || s.getType() == Schema.Type.MAP) {
//      root += "[0]";
//      s = s.getElementType();
      while (s.getType() == Schema.Type.ARRAY || s.getType() == Schema.Type.MAP) {
        if (s.getType() == Schema.Type.ARRAY) {
          root += "[0]";
        } else {
          root += "[*key*]";
        }
        s = s.getElementType();
      }
    }
    
    if (s.getType() == Schema.Type.RECORD) {
      processRecord(root, s, fields);
    } else {
      
      // our top-level array/map structure bottoms out with primitive types
      // we'll create one zero-indexed path through to a primitive - the
      // user can copy and paste the path if they want to extract other
      // indexes out to separate Kettle fields
      AvroInputMeta.AvroField newField = createAvroField(root, s);
      if (newField != null) {
        fields.add(newField);
      }
    }
    
    return fields;
  }
  
  protected static void processRecord(String path, Schema s, 
      List<AvroInputMeta.AvroField> fields) {
    
    List<Schema.Field> recordFields = s.getFields();
    for (Schema.Field rField : recordFields) {
      if (rField.schema().getType() == Schema.Type.RECORD) {
        processRecord(path + "." + rField.name(), rField.schema(), fields);
      } else if (rField.schema().getType() == Schema.Type.ARRAY) {
        processArray(path + "." + rField.name() + "[0]", rField.schema(), fields);
      } else if (rField.schema().getType() == Schema.Type.MAP) {
        processMap(path + "." + rField.name() + "[*key*]", rField.schema(), fields);
      } else {
        // primitive
        AvroInputMeta.AvroField newField = createAvroField(path + "." + rField.name(), rField.schema());
        if (newField != null) {
          fields.add(newField);
        }
      }
    }
  }
  
  protected static void processMap(String path, Schema s,
      List<AvroInputMeta.AvroField> fields) {
    
    s = s.getValueType(); // type of the values of the map
    if (s.getType() == Schema.Type.ARRAY) {
      processArray(path + "[0]", s, fields);
    } else if (s.getType() == Schema.Type.RECORD) {
      processRecord(path, s, fields);
    } else if (s.getType() == Schema.Type.MAP) {
      processMap(path + "[*key*]", s, fields);
    } else {
      AvroInputMeta.AvroField newField = createAvroField(path, s);
      if (newField != null) {
        fields.add(newField);
      }
    }
  }
  
  protected static void processArray(String path, Schema s, 
      List<AvroInputMeta.AvroField> fields) {
    
    s = s.getElementType(); // type of the array elements
    if (s.getType() == Schema.Type.ARRAY) {
      processArray(path + "[0]", s, fields);
    } else if (s.getType() == Schema.Type.RECORD) {
      processRecord(path, s, fields);
    } else {
      AvroInputMeta.AvroField newField = createAvroField(path, s);
      if (newField != null) {
        fields.add(newField);
      }
    }
  }
  
  protected static Schema loadSchema(String schemaFile) throws KettleException {
    
    Schema s = null;
    Schema.Parser p = new Schema.Parser();
    
    FileObject fileO = KettleVFS.getFileObject(schemaFile);
    try {
      InputStream in = KettleVFS.getInputStream(fileO);
      s = p.parse(in);
      
      in.close();
    } catch (FileSystemException e) {    
      throw new KettleException(BaseMessages.getString(AvroInputMeta.PKG, 
          "AvroInput.Error.SchemaError"), e);
    } catch (IOException e) {
      throw new KettleException(BaseMessages.getString(AvroInputMeta.PKG, 
          "AvroInput.Error.SchemaError"), e);      
    }
    
    return s;
  }
  
  protected static Schema loadSchemaFromContainer(String containerFilename)
    throws KettleException {
    Schema s = null;
    
    FileObject fileO = KettleVFS.getFileObject(containerFilename);
    InputStream in = null;
    
    try {
      in = KettleVFS.getInputStream(fileO);
      GenericDatumReader dr = new GenericDatumReader();      
      DataFileStream reader = new DataFileStream(in, dr);
      s = reader.getSchema();
      
      reader.close();
    } catch (FileSystemException e) {
      throw new KettleException(BaseMessages.getString(AvroInputMeta.PKG, 
          "AvroInputDialog.Error.KettleFileException"), e);
    } catch (IOException e) {
      throw new KettleException(BaseMessages.getString(AvroInputMeta.PKG, 
        "AvroInputDialog.Error.KettleFileException"), e);
    }
    
    return s;
  }
  
  /**
   * Set the output row format
   * 
   * @param rmi the output row format
   */
  public void setOutputRowMeta(RowMetaInterface rmi) {
    m_outputRowMeta = rmi;
  }   
  
  public void establishFileType(FileObject avroFile, String readerSchemaFile, 
      List<AvroInputMeta.AvroField> fields) 
    throws KettleException {
    
    // four possibilities:
    // 1. No schema file provided and no fields defined - can only
    // process a container file, under the assumption that all leaf primitives are
    // to be output
    // 2. No schema file provided but fields/paths defined - can only
    // process a container file, and assume that supplied paths match schema
    // 3. Schema file provided, no fields defined - output all leaf primitives
    // from schema and have to determine if input is a container file or just
    // serialized data
    // 4. Schema file provided and fields defined - output leaf primitives associated
    // with paths. Have to determine if file is container or not. If container, assume
    // supplied schema overrides encapsulated schema
    
    m_fields = fields;
    m_inStream = null;
    
    try {
      m_inStream = KettleVFS.getInputStream(avroFile);
    } catch (FileSystemException e1) {
      throw new KettleException("Unable to open avro file", e1);
    }
    
    // load and handle reader schema....
    if (!Const.isEmpty(readerSchemaFile)) {
      m_schemaToUse = loadSchema(readerSchemaFile);
    }
    
    m_datumReader = new GenericDatumReader();
    
    try {
      m_containerReader = new DataFileStream(m_inStream, m_datumReader);
      m_writerSchema = m_containerReader.getSchema();
      
      // resolve reader/writer schemas
      if (!Const.isEmpty(readerSchemaFile)) {
        // map any aliases for schema migration
        m_schemaToUse = Schema.applyAliases(m_writerSchema, m_schemaToUse);
      } else {      
        m_schemaToUse = m_writerSchema;
      }           
    } catch (IOException e) {
      // doesn't look like a container file....
      try {
        try {
          m_inStream.close();
        } catch (IOException e1) { } 
        m_inStream = KettleVFS.getInputStream(avroFile);
      } catch (FileSystemException e1) {
        throw new KettleException(BaseMessages.getString(AvroInputMeta.PKG, 
        "AvroInputDialog.Error.KettleFileException"), e1);        
      }
      
      m_containerReader = null;
      
      if (Const.isEmpty(readerSchemaFile)) {
        throw new KettleException(BaseMessages.getString(AvroInputMeta.PKG, "AvroInput.Error.NoSchema"));        
      }
      
      DecoderFactory factory = new DecoderFactory();
      m_decoder = factory.binaryDecoder(m_inStream, null);
      m_datumReader = new GenericDatumReader(m_schemaToUse);
      // System.out.println("Using schema: " + m_schemaToUse.toString());
    }
    
    // what top-level structure are we using?
    if (m_schemaToUse.getType() == Schema.Type.RECORD) {
      m_topLevelRecord = new GenericData.Record(m_schemaToUse);
    } else if (m_schemaToUse.getType() == Schema.Type.ARRAY) {
      m_topLevelArray = new GenericData.Array(1, m_schemaToUse); // capacity, schema
    } else if (m_schemaToUse.getType() == Schema.Type.MAP) {
      m_topLevelMap = new HashMap<Utf8, Object>();
    } else {
      throw new KettleException(BaseMessages.getString(AvroInputMeta.PKG, 
          "AvroInput.Error.UnsupportedTopLevelStructure"));
    }
    
    // any fields specified by the user, or do we need to read all leaves
    // from the schema?
    if (m_fields == null || m_fields.size() == 0) {
      m_fields = getLeafFields(m_schemaToUse);
    }
    
    for (AvroInputMeta.AvroField f : m_fields) {
      f.init();
    }
  }
    
  private void setKettleFields(Object[] outputRow) throws KettleException {
    Object value = null;
    for (AvroInputMeta.AvroField f : m_fields) {
      int outputIndex = m_outputRowMeta.indexOfValue(f.m_fieldName);
      
      if (outputIndex >= 0) {                
        f.reset();
        
        if (m_schemaToUse.getType() == Schema.Type.RECORD) {
          value = f.convertToKettleValue(m_topLevelRecord, m_schemaToUse);          
        } else if (m_schemaToUse.getType() == Schema.Type.ARRAY) {
          value = f.convertToKettleValue(m_topLevelArray, m_schemaToUse);
        } else {
          value = f.convertToKettleValue(m_topLevelMap, m_schemaToUse);
        }
        
        outputRow[outputIndex] = value;
      }
    }
  }
  
  public Object[] avroObjectToKettle() 
    throws KettleException {
    Object[] outputRowData = RowDataUtil.allocateRowData(m_outputRowMeta.size());
    
    if (m_containerReader != null) {
      // container file
      try {
        if (m_containerReader.hasNext()) {
          if (m_topLevelRecord != null) {
            m_containerReader.next(m_topLevelRecord);
          } else if (m_topLevelArray != null) {
            m_containerReader.next(m_topLevelArray);
          } else {
            m_containerReader.next(m_topLevelMap);
          }
          
          setKettleFields(outputRowData);
        } else {
          return null; // no more input
        }
      } catch (IOException e) {
        throw new KettleException(BaseMessages.getString(AvroInputMeta.PKG, 
            "AvroInput.Error.ObjectReadError"));
      }
    } else {
      // non-container file
      try {
        if (m_decoder.isEnd()) {
          return null;
        }
        if (m_topLevelRecord != null) {
          m_datumReader.read(m_topLevelRecord, m_decoder);
          System.out.println(m_topLevelRecord);
        } else if (m_topLevelArray != null) {
          m_datumReader.read(m_topLevelArray, m_decoder);
        } else {
          m_datumReader.read(m_topLevelMap, m_decoder);
        }
        
        setKettleFields(outputRowData);
      } catch (IOException ex) {
        // some IO problem or no more input
        return null;
      }      
    }
    
    return outputRowData;
  }
  
  public void close() throws IOException {
    if (m_containerReader != null) {
      m_containerReader.close();
    }
    if (m_inStream != null) {
      m_inStream.close();
    }
  }  
}
