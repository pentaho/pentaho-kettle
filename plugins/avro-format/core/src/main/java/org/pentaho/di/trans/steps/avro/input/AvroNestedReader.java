/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2022-2024 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.avro.input;

import org.pentaho.di.trans.steps.avro.AvroSpec;
import org.pentaho.di.trans.steps.avro.AvroToPdiConverter;
import org.apache.avro.Conversions;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.util.Utf8;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.codehaus.jackson.node.NullNode;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.avro.AvroToPdiConverter;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;


/**
 * The bulk of this code was taken from legacy AvroInputData and then logic from legacy AvroInputField was merged into
 * it.  The idea here is to remove the dependency on the schema from AvroInputField so that the current new
 * AvroInputField can remain visible by AEL.  AEL cannot see any classes dependant on shim.  So code that normally be in
 * format-meta is here, instead.
 */
public class AvroNestedReader {
  /**
   * For reading container files - will be null if file is not a container file
   */
  protected DataFileStream m_containerReader;

  /**
   * If the top level is a record
   */
  protected GenericData.Record m_topLevelRecord;

  /**
   * If the top level is an array
   */
  protected GenericData.Array m_topLevelArray;

  /**
   * If the top level is a map
   */
  protected Map<Utf8, Object> m_topLevelMap;

  /**
   * True if decoding from an incoming field
   */
  protected boolean m_decodingFromField;

  /**
   * For logging
   */
  protected LogChannelInterface m_log;

  /**
   * The input data format
   */
  protected RowMetaInterface m_incomingRowMeta;

  /**
   * The output data format
   */
  protected RowMetaInterface m_outputRowMeta;

  /**
   * For reading from files of just serialized objects
   */
  protected GenericDatumReader m_datumReader;
  protected Decoder m_decoder;
  protected InputStream m_inStream;

  /**
   * The schema used to write the file - will be null if the file is not a container file
   */
  protected Schema m_writerSchema;

  /**
   * The schema to use for extracting values
   */
  protected Schema m_schemaToUse;

  /**
   * The default schema to use (in the case where the schema is in an incoming field and a particular row has a null (or
   * unparsable/unavailable) schema
   */
  protected Schema m_defaultSchema;

  /**
   * The default datum reader (constructed with the default schema)
   */
  protected GenericDatumReader m_defaultDatumReader;
  protected Object m_defaultTopLevelObject;

  /**
   * Schema cache. Map of strings (actual schema or path to schema) to two element array. Element 0 = GenericDatumReader
   * configured with schema; 2 = top level structure object to use.
   */
  protected Map<String, Object[]> m_schemaCache = new HashMap<String, Object[]>();

  /**
   * True if the data to be decoded is json rather than binary
   */
  protected boolean m_jsonEncoded;

  protected List<AvroInputField> m_normalFields;
  protected AvroArrayExpansion m_expansionHandler;

  /**
   * The index that the decoded fields start at in the output row
   */
  protected int m_newFieldOffset;

  /**
   * If decoding from an incoming field, this holds its index
   */
  protected int m_fieldToDecodeIndex = -1;

  /**
   * True if schema is in an incoming field
   */
  protected boolean m_schemaInField;

  /**
   * If decoding from an incoming field and schema is in an incoming field, then this holds the schema field's index
   */
  protected int m_schemaFieldIndex = -1;

  /**
   * True if the schema field contains a path to a schema rather than the schema itself
   */
  protected boolean m_schemaFieldIsPath;

  /**
   * True if schemas read from incoming fields are to be cached in memory
   */
  protected boolean m_cacheSchemas;

  /**
   * True if null should be output for a field if it is not present in the schema being used (otherwise an exeption is
   * raised)
   */
  protected boolean m_dontComplainAboutMissingFields;

  protected DataFileStream<Object> m_avroDataFileStream;

  protected AvroToPdiConverter m_avroToPdiConverter;

  /**
   * Factory for obtaining a decoder
   */
  protected DecoderFactory m_factory;

  //protected static Class<?> PKG = AvroInputMeta.class;
  protected static Class<?> PKG = AvroNestedReader.class;


  protected void init() throws KettleException {
    if ( m_schemaToUse != null ) {
      m_avroToPdiConverter = new AvroToPdiConverter( m_schemaToUse );
      initTopLevelStructure( m_schemaToUse, true );
    }

    if ( m_normalFields == null || m_normalFields.size() == 0 ) {
      throw new KettleException( BaseMessages.getString( PKG, "AvroInput.Error.NoFieldPathsDefined" ) );
    }

    m_expansionHandler = checkFieldPaths( m_normalFields, m_outputRowMeta );

    int killmeIndex = 0;
    for ( AvroInputField f : m_normalFields ) {
      //bypass this for now: int outputIndex = m_outputRowMeta.indexOfValue( f.getPentahoFieldName() );
      int outputIndex = killmeIndex++;
      fieldInit( f, outputIndex );
    }

    if ( m_expansionHandler != null ) {
      m_expansionHandler.init();
    }
    m_factory = new DecoderFactory();
  }

  /**
   * Reset this field. Should be called prior to processing a new field value from the avro file
   *
   * @param space environment variables (values that environment variables resolve to cannot contain "."s)
   */
  public void resetField(AvroInputField avroInputField, VariableSpace space ) {
    // first clear because there may be stuff left over from processing
    // the previous avro object (especially if a path exited early due to
    // non-existent map key or array index out of bounds)
    avroInputField.getTempParts().clear();

    for ( String part : avroInputField.getPathParts() ) {
      if ( space == null ) {
        avroInputField.getTempParts().add( part );
      } else {
        avroInputField.getTempParts().add( space.environmentSubstitute( part ) );
      }
    }
  }

  /**
   * Initialize this field by parsing the path etc.
   *
   * @param outputIndex the index in the output row structure for this field
   * @throws KettleException if a problem occurs
   */
  public void fieldInit(AvroInputField avroInputField, int outputIndex ) throws KettleException {
    if ( Const.isEmpty( avroInputField.getAvroFieldName() ) ) {
      throw new KettleException( BaseMessages.getString( PKG, "AvroInput.Error.NoPathSet" ) );
    }
    if ( avroInputField.getPathParts() != null ) {
      return;
    }

    String fieldPath = cleansePath( avroInputField.getAvroFieldName() );

    String[] temp = fieldPath.split( "\\." );
    List<String> pathParts = new ArrayList<>();
    avroInputField.setPathParts( pathParts );
    for ( String part : temp ) {
      pathParts.add( part );
    }

    if ( pathParts.get( 0 ).equals( "$" ) ) {
      pathParts.remove( 0 ); // root record indicator
    } else if ( pathParts.get( 0 ).startsWith( "$[" ) ) {

      // strip leading $ off of array
      String r = pathParts.get( 0 ).substring( 1, pathParts.get( 0 ).length() );
      pathParts.set( 0, r );
    }

    //Re-init the temp vars?  Should probably move them here (tk)
    avroInputField.setTempParts( new ArrayList<String>() );
    ValueMeta resetMeta = new ValueMeta();
    resetMeta.setType( avroInputField.getPentahoType() );
    avroInputField.setTempValueMeta( resetMeta );
    avroInputField.setOutputIndex( outputIndex );
  }


  protected void initTopLevelStructure( Schema schema, boolean setDefault ) throws KettleException {
    // what top-level structure are we using?
    if ( schema.getType() == Schema.Type.RECORD ) {
      m_topLevelRecord = new GenericData.Record( schema );
      if ( setDefault ) {
        m_defaultTopLevelObject = m_topLevelRecord;
      }
    } else if ( schema.getType() == Schema.Type.UNION ) {
      // ASSUMPTION: if the top level structure is a union then each
      // object we will read will be a record. We'll assume that any
      // non-record types in the top-level union are named types that
      // are referenced in the record types. We'll scan the union for the
      // first record type to construct our
      // our initial top-level object. When reading, the read method will give
      // us a new object (with appropriate schema) if this top level object's
      // schema does not match the schema of the record being currently read
      Schema firstUnion = null;
      for ( Schema uS : schema.getTypes() ) {
        if ( uS.getType() == Schema.Type.RECORD ) {
          firstUnion = uS;
          break;
        }
      }

      m_topLevelRecord = new GenericData.Record( firstUnion );
      if ( setDefault ) {
        m_defaultTopLevelObject = m_topLevelRecord;
      }
    } else if ( schema.getType() == Schema.Type.ARRAY ) {
      m_topLevelArray = new GenericData.Array( 1, schema ); // capacity,
      // schema
      if ( setDefault ) {
        m_defaultTopLevelObject = m_topLevelArray;
      }
    } else if ( schema.getType() == Schema.Type.MAP ) {
      m_topLevelMap = new HashMap<Utf8, Object>();
      if ( setDefault ) {
        m_defaultTopLevelObject = m_topLevelMap;
      }
    } else {
      throw new KettleException( BaseMessages.getString( PKG,
        "AvroInput.Error.UnsupportedTopLevelStructure" ) );
    }
  }

  /**
   * Examines the user-specified paths for the presence of a map/array expansion. If such an expansion is detected it
   * checks that it is valid and, if so, creates an expansion handler for processing it.
   *
   * @param normalFields  the original user-specified paths. This is modified to contain only non-expansion paths.
   * @param outputRowMeta the output row format
   * @return an AvroArrayExpansion object to handle expansions or null if no expansions are present in the user-supplied
   * path definitions.
   * @throws KettleException if a problem occurs
   */
  protected AvroArrayExpansion checkFieldPaths( List<AvroInputField> normalFields,
                                                RowMetaInterface outputRowMeta ) throws
    KettleException {
    // here we check whether there are any full map/array expansions
    // specified in the paths (via [*]). If so, we want to make sure
    // that only one is present across all paths. E.g. we can handle
    // multiple fields like $.person[*].first, $.person[*].last etc.
    // but not $.person[*].first, $.person[*].address[*].street.

    String expansion = null;
    List<AvroInputField> normalList = new ArrayList<AvroInputField>();
    List<AvroInputField> expansionList = new ArrayList<AvroInputField>();
    for ( AvroInputField f : normalFields ) {
      String path = f.getAvroFieldName();

      if ( path != null && path.lastIndexOf( "[*]" ) >= 0 ) {

        if ( path.indexOf( "[*]" ) != path.lastIndexOf( "[*]" ) ) {
          throw new KettleException( BaseMessages.getString( PKG,
            "AvroInput.Error.PathContainsMultipleExpansions", path ) );
        }
        String pathPart = path.substring( 0, path.lastIndexOf( "[*]" ) + 3 );

        if ( expansion == null ) {
          expansion = pathPart;
        } else {
          if ( !expansion.equals( pathPart ) ) {
            throw new KettleException( BaseMessages.getString( PKG,
              "AvroInput.Error.MutipleDifferentExpansions" ) );
          }
        }

        expansionList.add( f );
      } else {
        normalList.add( f );
      }
    }

    normalFields.clear();
    for ( AvroInputField f : normalList ) {
      normalFields.add( f );
    }

    if ( expansionList.size() > 0 ) {

      List<AvroInputField> subFields = new ArrayList<AvroInputField>();

      for ( AvroInputField ef : expansionList ) {
        AvroInputField subField = new AvroInputField();
        subField.setPentahoFieldName( ef.getPentahoFieldName() );
        String path = ef.getAvroFieldName();
        if ( path.charAt( path.length() - 2 ) == '*' ) {
          path = "dummy"; // pulling a primitive out of the map/array (path
          // doesn't matter)
        } else {
          path = path.substring( path.lastIndexOf( "[*]" ) + 3, path.length() );
          path = "$" + path;
        }

        subField.setAvroFieldName( path );
        subField.setIndexedVals( ef.getIndexedVals() );
        subField.setPentahoType( ef.getPentahoType() );

        subFields.add( subField );
      }

      AvroArrayExpansion exp = this.new AvroArrayExpansion( subFields );
      exp.m_expansionPath = expansion;
      exp.m_outputRowMeta = outputRowMeta;

      return exp;
    }

    return null;
  }

  /////////////////////// ******* from Legacy AvroInputField *************** ///////////////////////////

  /**
   * Processes a map at this point in the path.
   *
   * @param map           the map to process
   * @param s             the current schema at this point in the path
   * @param ignoreMissing true if null is to be returned for user fields that don't appear in the schema
   * @return the field value or null for out-of-bounds array indexes, non-existent map keys or unsupported avro types.
   * @throws KettleException if a problem occurs
   */
  public Object convertToKettleValue( AvroInputField avroInputField,
                                      Map<Utf8, Object> map, Schema s, Schema defaultSchema, boolean ignoreMissing )
    throws KettleException {

    if ( map == null ) {
      return null;
    }

    if ( avroInputField.getTempParts().size() == 0 ) {
      throw new KettleException( BaseMessages.getString( PKG, "AvroInput.Error.MalformedPathMap" ) );
    }

    String part = avroInputField.getTempParts().remove( 0 );
    if ( !( part.charAt( 0 ) == '[' ) ) {
      throw new KettleException( BaseMessages.getString( PKG, "AvroInput.Error.MalformedPathMap2", part ) );
    }

    String key = part.substring( 1, part.indexOf( ']' ) );

    if ( part.indexOf( ']' ) < part.length() - 1 ) {
      // more dimensions to the array/map
      part = part.substring( part.indexOf( ']' ) + 1, part.length() );
      avroInputField.getTempParts().add( 0, part );
    }

    Object value = map.get( new Utf8( key ) );
    if ( value == null ) {
      return null;
    }

    Schema valueType = s.getValueType();

    if ( valueType.getType() == Schema.Type.UNION ) {
      if ( value instanceof GenericContainer ) {
        // we can ask these things for their schema (covers
        // records, arrays, enums and fixed)
        valueType = ( (GenericContainer) value ).getSchema();
      } else {
        // either have a map or primitive here
        if ( value instanceof Map ) {
          // now have to look for the schema of the map
          Schema mapSchema = null;
          for ( Schema ts : valueType.getTypes() ) {
            if ( ts.getType() == Schema.Type.MAP ) {
              mapSchema = ts;
              break;
            }
          }
          if ( mapSchema == null ) {
            throw new KettleException( BaseMessages.getString( PKG,
              "AvroInput.Error.UnableToFindSchemaForUnionMap" ) );
          }
          valueType = mapSchema;
        } else {
          if ( avroInputField.getTempValueMeta().getType() != ValueMetaInterface.TYPE_STRING ) {
            // we have a two element union, where one element is the type
            // "null". So in this case we actually have just one type and can
            // output specific values of it (instead of using String as a
            // catch all for varying primitive types in the union)
            valueType = checkUnion( valueType );
          } else {
            // use the string representation of the value
            valueType = Schema.create( Schema.Type.STRING );
          }
        }
      }
    }

    // what have we got?
    if ( valueType.getType() == Schema.Type.RECORD ) {
      return convertToKettleValue( avroInputField, (GenericData.Record) value, valueType, defaultSchema,
        ignoreMissing );
    } else if ( valueType.getType() == Schema.Type.ARRAY ) {
      return convertToKettleValue( avroInputField, (GenericData.Array) value, valueType, defaultSchema, ignoreMissing );
    } else if ( valueType.getType() == Schema.Type.MAP ) {
      return convertToKettleValue( avroInputField, (Map<Utf8, Object>) value, valueType, defaultSchema, ignoreMissing );
    } else {
      // assume a primitive
      return getPrimitive( avroInputField, value, valueType );
    }
  }

  /**
   * Processes an array at this point in the path.
   *
   * @param array         the array to process
   * @param s             the current schema at this point in the path
   * @param ignoreMissing true if null is to be returned for user fields that don't appear in the schema
   * @return the field value or null for out-of-bounds array indexes, non-existent map keys or unsupported avro types.
   * @throws KettleException if a problem occurs
   */
  public Object convertToKettleValue(AvroInputField avroInputField, GenericData.Array array, Schema s,
                                     Schema defaultSchema, boolean ignoreMissing )
    throws KettleException {

    if ( array == null ) {
      return null;
    }

    if ( avroInputField.getTempParts().size() == 0 ) {
      throw new KettleException( BaseMessages.getString( PKG, "AvroInput.Error.MalformedPathArray" ) );
    }

    String part = avroInputField.getTempParts().remove( 0 );
    if ( !( part.charAt( 0 ) == '[' ) ) {
      throw new KettleException( BaseMessages.getString( PKG, "AvroInput.Error.MalformedPathArray2", part ) );
    }

    String index = part.substring( 1, part.indexOf( ']' ) );
    int arrayI = 0;
    try {
      arrayI = Integer.parseInt( index.trim() );
    } catch ( NumberFormatException e ) {
      throw new KettleException( BaseMessages.getString( PKG, "AvroInput.Error.UnableToParseArrayIndex", index ) );
    }

    if ( part.indexOf( ']' ) < part.length() - 1 ) {
      // more dimensions to the array
      part = part.substring( part.indexOf( ']' ) + 1, part.length() );
      avroInputField.getTempParts().add( 0, part );
    }

    if ( arrayI >= array.size() || arrayI < 0 ) {
      return null;
    }

    Object element = array.get( arrayI );
    Schema elementType = s.getElementType();

    if ( element == null ) {
      return null;
    }

    if ( elementType.getType() == Schema.Type.UNION ) {
      if ( element instanceof GenericContainer ) {
        // we can ask these things for their schema (covers
        // records, arrays, enums and fixed)
        elementType = ( (GenericContainer) element ).getSchema();
      } else {
        // either have a map or primitive here
        if ( element instanceof Map ) {
          // now have to look for the schema of the map
          Schema mapSchema = null;
          for ( Schema ts : elementType.getTypes() ) {
            if ( ts.getType() == Schema.Type.MAP ) {
              mapSchema = ts;
              break;
            }
          }
          if ( mapSchema == null ) {
            throw new KettleException( BaseMessages.getString( PKG,
              "AvroInput.Error.UnableToFindSchemaForUnionMap" ) );
          }
          elementType = mapSchema;
        } else {
          if ( avroInputField.getTempValueMeta().getType() != ValueMetaInterface.TYPE_STRING ) {
            // we have a two element union, where one element is the type
            // "null". So in this case we actually have just one type and can
            // output specific values of it (instead of using String as a
            // catch all for varying primitive types in the union)
            elementType = checkUnion( elementType );
          } else {
            // use the string representation of the value
            elementType = Schema.create( Schema.Type.STRING );
          }
        }
      }
    }

    // what have we got?
    if ( elementType.getType() == Schema.Type.RECORD ) {
      return convertToKettleValue( avroInputField, (GenericData.Record) element, elementType, defaultSchema,
        ignoreMissing );
    } else if ( elementType.getType() == Schema.Type.ARRAY ) {
      return convertToKettleValue( avroInputField, (GenericData.Array) element, elementType, defaultSchema,
        ignoreMissing );
    } else if ( elementType.getType() == Schema.Type.MAP ) {
      return convertToKettleValue( avroInputField, (Map<Utf8, Object>) element, elementType, defaultSchema,
        ignoreMissing );
    } else {
      // assume a primitive (covers bytes encapsulated in FIXED type)
      return getPrimitive( avroInputField, element, elementType );
    }
  }

  /**
   * Processes a record at this point in the path.
   *
   * @param record        the record to process
   * @param s             the current schema at this point in the path
   * @param ignoreMissing true if null is to be returned for user fields that don't appear in the schema
   * @return the field value or null for out-of-bounds array indexes, non-existent map keys or unsupported avro types.
   * @throws KettleException if a problem occurs
   */
  public Object convertToKettleValue(AvroInputField avroInputField, GenericData.Record record, Schema s,
                                     Schema defaultSchema, boolean ignoreMissing )
    throws KettleException {

    if ( record == null ) {
      return null;
    }

    if ( avroInputField.getTempParts().size() == 0 ) {
      throw new KettleException( BaseMessages.getString( PKG, "AvroInput.Error.MalformedPathRecord" ) );
    }

    String part = avroInputField.getTempParts().remove( 0 );
    if ( part.charAt( 0 ) == '[' ) {
      throw new KettleException(
        BaseMessages.getString( PKG, "AvroInput.Error.InvalidPath" ) + avroInputField.getTempParts() );
    }

    if ( part.indexOf( '[' ) > 0 ) {
      String arrayPart = part.substring( part.indexOf( '[' ) );
      part = part.substring( 0, part.indexOf( '[' ) );

      // put the array section back into location zero
      avroInputField.getTempParts().add( 0, arrayPart );
    }

    // part is a named field of the record
    Schema.Field fieldS = s.getField( part );
    if ( fieldS == null && !ignoreMissing ) {
      throw new KettleException( BaseMessages.getString( PKG, "AvroInput.Error.NonExistentField", part ) );
    }
    Object field = record.get( part );

    if ( field == null ) {
      if ( defaultSchema != null ) {
        fieldS = defaultSchema.getField( part );
      }

      if ( fieldS == null || fieldS.defaultVal() == null ) {
        return null;
      }
      field = fieldS.defaultVal();
    }

    Schema.Type fieldT = fieldS.schema().getType();
    Schema fieldSchema = fieldS.schema();

    if ( fieldT == Schema.Type.UNION ) {
      if ( field instanceof GenericContainer ) {
        // we can ask these things for their schema (covers
        // records, arrays, enums and fixed)
        fieldSchema = ( (GenericContainer) field ).getSchema();
        fieldT = fieldSchema.getType();
      } else {
        // either have a map or primitive here
        if ( field instanceof Map ) {
          // now have to look for the schema of the map
          Schema mapSchema = null;
          for ( Schema ts : fieldSchema.getTypes() ) {
            if ( ts.getType() == Schema.Type.MAP ) {
              mapSchema = ts;
              break;
            }
          }
          if ( mapSchema == null ) {
            throw new KettleException( BaseMessages.getString( PKG,
              "AvroInput.Error.UnableToFindSchemaForUnionMap" ) );

          }
          fieldSchema = mapSchema;
          fieldT = Schema.Type.MAP;
        } else {
          fieldSchema = checkUnion( fieldSchema );
          fieldT = fieldSchema.getType();
        }
      }
    }

    // what have we got?
    if ( !( field instanceof NullNode) ) {
      if ( fieldT == Schema.Type.RECORD ) {
        if ( field instanceof GenericData.Record ) {
          return convertToKettleValue(avroInputField, (GenericData.Record) field, fieldSchema, defaultSchema,
                  ignoreMissing);
        }
      } else if ( fieldT == Schema.Type.ARRAY ) {
        if ( field instanceof GenericData.Array ) {
          return convertToKettleValue(avroInputField, (GenericData.Array) field, fieldSchema, defaultSchema,
                  ignoreMissing);
        }
      } else if ( fieldT == Schema.Type.MAP ) {
        if ( field instanceof Map ) {
          return convertToKettleValue(avroInputField, (Map<Utf8, Object>) field, fieldSchema, defaultSchema,
                  ignoreMissing);
        }
      } else if ( fieldT == Schema.Type.BYTES ) {
        if ( field instanceof ByteBuffer ) {
          return convertToKettleValue(avroInputField, (ByteBuffer) field, fieldSchema);
        }
      } else {
        // assume primitive (covers bytes encapsulated in FIXED type)
        return getPrimitive( avroInputField, field, fieldSchema );
      }
    }
    return null;
  }

  /**
   * @param pentahoType
   * @param avroData
   * @param fieldSchema
   * @return
   */
  public Object convertToKettleValue(AvroInputField pentahoType, ByteBuffer avroData, Schema fieldSchema ) {
    Object pentahoData = null;
    if ( avroData != null ) {
      try {
        switch ( pentahoType.getPentahoType() ) {
          case ValueMetaInterface.TYPE_BIGNUMBER:
            Conversions.DecimalConversion converter = new Conversions.DecimalConversion();
            Schema schema = fieldSchema;
            if ( schema.getType().equals( Schema.Type.UNION ) ) {
              List<Schema> schemas = schema.getTypes();
              for ( Schema s : schemas ) {
                if ( !s.getName().equalsIgnoreCase( "null" ) ) {
                  schema = s;
                  break;
                }
              }
            }
            Object precision = schema.getObjectProp( AvroSpec.DECIMAL_PRECISION );
            Object scale = schema.getObjectProp( AvroSpec.DECIMAL_SCALE );
            LogicalTypes.Decimal decimalType =
              LogicalTypes.decimal( Integer.parseInt( precision.toString() ), Integer.parseInt( scale.toString() ) );
            pentahoData = converter.fromBytes( avroData, m_schemaToUse, decimalType );
            break;
          case ValueMetaInterface.TYPE_BINARY:
            pentahoData = new byte[ avroData.remaining() ];
            avroData.get( (byte[]) pentahoData );
            break;
        }
      } catch ( Exception e ) {
        // If unable to do the type conversion just ignore. null will be returned.
      }
    }
    return pentahoData;
  }

  /**
   * Get the value of the Avro leaf primitive with respect to the Kettle type for this path.
   *
   * @param fieldValue the Avro leaf value
   * @param s          the schema for the leaf value
   * @return the appropriate Kettle typed value
   * @throws KettleException if a problem occurs
   */
  protected Object getPrimitive(AvroInputField avroInputField, Object fieldValue, Schema s ) throws KettleException {

    return m_avroToPdiConverter.converAvroToPdi( fieldValue, avroInputField, s );
  }

  /**
   * Perform Kettle type conversions for the Avro leaf field value.
   *
   * @param fieldValue the leaf value from the Avro structure
   * @return an Object of the appropriate Kettle type
   * @throws KettleException if a problem occurs
   */
  protected Object getKettleValue(AvroInputField avroInputField, Object fieldValue ) throws KettleException {

    switch ( avroInputField.getTempValueMeta().getType() ) {
      case ValueMetaInterface.TYPE_BIGNUMBER:
        return avroInputField.getTempValueMeta().getBigNumber( fieldValue );
      case ValueMetaInterface.TYPE_BINARY:
        return avroInputField.getTempValueMeta().getBinary( fieldValue );
      case ValueMetaInterface.TYPE_BOOLEAN:
        return avroInputField.getTempValueMeta().getBoolean( fieldValue );
      case ValueMetaInterface.TYPE_DATE:
        if ( avroInputField.getAvroType().getBaseType() == AvroSpec.DataType.INTEGER.getBaseType() ) {
          LocalDate localDate = LocalDate.ofEpochDay( 0 ).plusDays( (Long) fieldValue );
          return Date.from( localDate.atStartOfDay( ZoneId.systemDefault() ).toInstant() );
        } else if ( avroInputField.getAvroType().getBaseType() == AvroSpec.DataType.STRING.getBaseType() ) {
          Object pentahoData = null;
          String dateFormatStr = avroInputField.getStringFormat();
          if ( ( dateFormatStr == null ) || ( dateFormatStr.trim().length() == 0 ) ) {
            dateFormatStr = ValueMetaBase.DEFAULT_DATE_FORMAT_MASK;
          }
          SimpleDateFormat datePattern = new SimpleDateFormat( dateFormatStr );
          try {
            return datePattern.parse( fieldValue.toString() );
          } catch ( Exception e ) {
            return null;
          }
        }
        return avroInputField.getTempValueMeta().getDate( fieldValue );
      case ValueMetaInterface.TYPE_TIMESTAMP:
        return new Timestamp( (Long) fieldValue );
      case ValueMetaInterface.TYPE_INTEGER:
        return avroInputField.getTempValueMeta().getInteger( fieldValue );
      case ValueMetaInterface.TYPE_NUMBER:
        return avroInputField.getTempValueMeta().getNumber( fieldValue );
      case ValueMetaInterface.TYPE_STRING:
        return avroInputField.getTempValueMeta().getString( fieldValue );
      case ValueMetaInterface.TYPE_INET:
        try {
          return InetAddress.getByName( fieldValue.toString() );
        } catch ( UnknownHostException ex ) {
          return null;
        }
      default:
        return null;
    }
  }

  /**
   * Helper function that checks the validity of a union. We can only handle unions that contain two elements: a type
   * and null.
   *
   * @param s the union schema to check
   * @return the type of the element that is not null.
   * @throws KettleException if a problem occurs.
   */
  protected static Schema checkUnion( Schema s ) throws KettleException {
    boolean ok = false;
    List<Schema> types = s.getTypes();

    // the type other than null
    Schema otherSchema = null;

    if ( types.size() != 2 ) {
      throw new KettleException( BaseMessages.getString( PKG, "AvroInput.Error.UnionError1" ) );
    }

    for ( Schema p : types ) {
      if ( p.getType() == Schema.Type.NULL ) {
        ok = true;
      } else {
        otherSchema = p;
      }
    }

    if ( !ok ) {
      throw new KettleException( BaseMessages.getString( PKG, "AvroInput.Error.UnionError2" ) );
    }

    return otherSchema;
  }

  /**
   * Cleanses a string path by ensuring that any variables names present in the path do not contain "."s (replaces any
   * dots with underscores).
   *
   * @param path the path to cleanse
   * @return the cleansed path
   */
  public static String cleansePath( String path ) {
    // look for variables and convert any "." to "_"

    int index = path.indexOf( "${" );

    int endIndex = 0;
    String tempStr = path;
    while ( index >= 0 ) {
      index += 2;
      endIndex += tempStr.indexOf( "}" );
      if ( endIndex > 0 && endIndex > index + 1 ) {
        String key = path.substring( index, endIndex );

        String cleanKey = key.replace( '.', '_' );
        path = path.replace( key, cleanKey );
      } else {
        break;
      }

      if ( endIndex + 1 < path.length() ) {
        tempStr = path.substring( endIndex + 1, path.length() );
      } else {
        break;
      }

      index = tempStr.indexOf( "${" );

      if ( index > 0 ) {
        index += endIndex;
      }
    }

    return path;
  }

  /**
   * Inner class that handles a single array/map expansion process. Expands an array or map to multiple Kettle rows.
   * Delegates to AvroInptuField objects to handle the extraction of leaf primitives.
   *
   * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
   * @version $Revision$
   */
  public class AvroArrayExpansion {

    /**
     * The prefix of the full path that defines the expansion
     */
    public String m_expansionPath;

    /**
     * Subfield objects that handle the processing of the path after the expansion prefix
     */
    protected List<AvroInputField> m_subFields;

    private List<String> m_pathParts;
    private List<String> m_tempParts;

    protected RowMetaInterface m_outputRowMeta;

    public AvroArrayExpansion( List<AvroInputField> subFields ) {
      m_subFields = subFields;
    }

    /**
     * Initialize this field by parsing the path etc.
     *
     * @throws KettleException if a problem occurs
     */
    public void init() throws KettleException {
      if ( Const.isEmpty( m_expansionPath ) ) {
        throw new KettleException( BaseMessages
          .getString( PKG, "AvroInput.Error.NoPathSet" ) );
      }
      if ( m_pathParts != null ) {
        return;
      }

      String expansionPath = AvroNestedReader.cleansePath( m_expansionPath );

      String[] temp = expansionPath.split( "\\." );
      m_pathParts = new ArrayList<String>();
      for ( String part : temp ) {
        m_pathParts.add( part );
      }

      if ( m_pathParts.get( 0 ).equals( "$" ) ) {
        m_pathParts.remove( 0 ); // root record indicator
      } else if ( m_pathParts.get( 0 ).startsWith( "$[" ) ) {

        // strip leading $ off of array
        String r = m_pathParts.get( 0 ).substring( 1, m_pathParts.get( 0 ).length() );
        m_pathParts.set( 0, r );
      }
      m_tempParts = new ArrayList<String>();

      // initialize the sub fields
      if ( m_subFields != null ) {
        for ( AvroInputField f : m_subFields ) {
          int outputIndex = m_outputRowMeta.indexOfValue( f.getPentahoFieldName() );
          fieldInit( f, outputIndex );
        }
      }
    }

    /**
     * Reset this field. Should be called prior to processing a new field value from the avro file
     *
     * @param space environment variables (values that environment variables resolve to cannot contain "."s)
     */
    public void reset( VariableSpace space ) {
      m_tempParts.clear();

      for ( String part : m_pathParts ) {
        if ( space == null ) {
          m_tempParts.add( part );
        } else {
          m_tempParts.add( space.environmentSubstitute( part ) );
        }
      }

      // reset sub fields
      for ( AvroInputField f : m_subFields ) {
        resetField( f, space );
      }
    }

    /**
     * Processes a map at this point in the path.
     *
     * @param map           the map to process
     * @param s             the current schema at this point in the path
     * @param space         environment variables
     * @param ignoreMissing true if null is to be returned for user fields that don't appear in the schema
     * @return an array of Kettle rows corresponding to the expanded map/array and containing all leaf values as defined
     * in the paths
     * @throws KettleException if a problem occurs
     */
    public Object[][] convertToKettleValues(
      Map<Utf8, Object> map, Schema s, Schema defaultSchema, VariableSpace space, boolean ignoreMissing )
      throws KettleException {

      if ( map == null ) {
        return null;
      }

      if ( m_tempParts.size() == 0 ) {
        throw new KettleException( BaseMessages.getString( PKG, "AvroInput.Error.MalformedPathMap" ) );
      }

      String part = m_tempParts.remove( 0 );
      if ( !( part.charAt( 0 ) == '[' ) ) {
        throw new KettleException( BaseMessages
          .getString( PKG, "AvroInput.Error.MalformedPathMap2", part ) );
      }

      String key = part.substring( 1, part.indexOf( ']' ) );

      if ( part.indexOf( ']' ) < part.length() - 1 ) {
        // more dimensions to the array/map
        part = part.substring( part.indexOf( ']' ) + 1, part.length() );
        m_tempParts.add( 0, part );
      }

      if ( key.equals( "*" ) ) {
        // start the expansion - we delegate conversion to our subfields
        Schema valueType = s.getValueType();
        Object[][] result =
          new Object[ map.keySet().size() ][ m_outputRowMeta.size() + RowDataUtil.OVER_ALLOCATE_SIZE ];

        int i = 0;
        for ( Utf8 mk : map.keySet() ) {
          Object value = map.get( mk );

          for ( int j = 0; j < m_subFields.size(); j++ ) {
            AvroInputField sf = m_subFields.get( j );
            resetField( sf, space );

            // what have we got
            if ( valueType.getType() == Schema.Type.RECORD ) {
              result[ i ][ sf.getOutputIndex() ] =
                convertToKettleValue( sf, (GenericData.Record) value, valueType, defaultSchema, ignoreMissing );
            } else if ( valueType.getType() == Schema.Type.ARRAY ) {
              result[ i ][ sf.getOutputIndex() ] =
                convertToKettleValue( sf, (GenericData.Array) value, valueType, defaultSchema, ignoreMissing );
            } else if ( valueType.getType() == Schema.Type.MAP ) {
              result[ i ][ sf.getOutputIndex() ] =
                convertToKettleValue( sf, (Map<Utf8, Object>) value, valueType, defaultSchema, ignoreMissing );
            } else {
              // assume a primitive
              result[ i ][ sf.getOutputIndex() ] = getPrimitive( sf, value, valueType );
            }
          }
          i++; // next row
        }

        return result;
      } else {
        Object value = map.get( new Utf8( key ) );

        if ( value == null ) {
          // key doesn't exist in map
          Object[][] result = new Object[ 1 ][ m_outputRowMeta.size() + RowDataUtil.OVER_ALLOCATE_SIZE ];

          for ( int i = 0; i < m_subFields.size(); i++ ) {
            AvroInputField sf = m_subFields.get( i );
            result[ 0 ][ sf.getOutputIndex() ] = null;
          }

          return result;
        }

        Schema valueType = s.getValueType();
        if ( valueType.getType() == Schema.Type.UNION ) {
          if ( value instanceof GenericContainer ) {
            // we can ask these things for their schema (covers
            // records, arrays, enums and fixed)
            valueType = ( (GenericContainer) value ).getSchema();
          } else {
            // either have a map or primitive here
            if ( value instanceof Map ) {
              // now have to look for the schema of the map
              Schema mapSchema = null;
              for ( Schema ts : valueType.getTypes() ) {
                if ( ts.getType() == Schema.Type.MAP ) {
                  mapSchema = ts;
                  break;
                }
              }
              if ( mapSchema == null ) {
                throw new KettleException( BaseMessages.getString( PKG,
                  "AvroInput.Error.UnableToFindSchemaForUnionMap" ) );
              }
              valueType = mapSchema;
            } else {
              // We shouldn't have a primitive here
              if ( !ignoreMissing ) {
                throw new KettleException( BaseMessages.getString( PKG,
                  "AvroInput.Error.EncounteredAPrimitivePriorToMapExpansion" ) );
              }
              Object[][] result = new Object[ 1 ][ m_outputRowMeta.size() + RowDataUtil.OVER_ALLOCATE_SIZE ];
              return result;
            }
          }
        }

        // what have we got?
        if ( valueType.getType() == Schema.Type.RECORD ) {
          return convertToKettleValues( (GenericData.Record) value, valueType, defaultSchema, space, ignoreMissing );
        } else if ( valueType.getType() == Schema.Type.ARRAY ) {
          return convertToKettleValues( (GenericData.Array) value, valueType, defaultSchema, space, ignoreMissing );
        } else if ( valueType.getType() == Schema.Type.MAP ) {
          return convertToKettleValues( (Map<Utf8, Object>) value, valueType, defaultSchema, space, ignoreMissing );
        } else {
          // we shouldn't have a primitive at this point. If we are
          // extracting a particular key from the map then we're not to the
          // expansion phase,
          // so normally there must be a non-primitive sub-structure. Only if
          // the user is switching schema versions on a per-row basis or the
          // schema is a union at the top level could we end up here
          if ( !ignoreMissing ) {
            throw new KettleException( BaseMessages.getString( PKG,
              "AvroInput.Error.UnexpectedMapValueTypeAtNonExpansionPoint" ) );
          }
          Object[][] result = new Object[ 1 ][ m_outputRowMeta.size() + RowDataUtil.OVER_ALLOCATE_SIZE ];
          return result;
        }
      }
    }

    /**
     * Processes an array at this point in the path.
     *
     * @param array         the array to process
     * @param s             the current schema at this point in the path
     * @param space         environment variables
     * @param ignoreMissing true if null is to be returned for user fields that don't appear in the schema
     * @return an array of Kettle rows corresponding to the expanded map/array and containing all leaf values as defined
     * in the paths
     * @throws KettleException if a problem occurs
     */
    public Object[][] convertToKettleValues( GenericData.Array array, Schema s, Schema defaultSchema,
                                             VariableSpace space,
                                             boolean ignoreMissing ) throws KettleException {

      if ( array == null ) {
        return null;
      }

      if ( m_tempParts.size() == 0 ) {
        throw new KettleException( BaseMessages.getString( PKG, "AvroInput.Error.MalformedPathArray" ) );
      }

      String part = m_tempParts.remove( 0 );
      if ( !( part.charAt( 0 ) == '[' ) ) {
        throw new KettleException( BaseMessages.getString( PKG, "AvroInput.Error.MalformedPathArray2",
          part ) );
      }

      String index = part.substring( 1, part.indexOf( ']' ) );

      if ( part.indexOf( ']' ) < part.length() - 1 ) {
        // more dimensions to the array
        part = part.substring( part.indexOf( ']' ) + 1, part.length() );
        m_tempParts.add( 0, part );
      }

      if ( index.equals( "*" ) ) {
        // start the expansion - we delegate conversion to our subfields

        Schema elementType = s.getElementType();
        Object[][] result = new Object[ array.size() ][ m_outputRowMeta.size() + RowDataUtil.OVER_ALLOCATE_SIZE ];

        for ( int i = 0; i < array.size(); i++ ) {
          Object value = array.get( i );

          for ( int j = 0; j < m_subFields.size(); j++ ) {
            AvroInputField sf = m_subFields.get( j );
            resetField( sf, space );
            // what have we got
            if ( elementType.getType() == Schema.Type.RECORD ) {
              result[ i ][ sf.getOutputIndex() ] =
                convertToKettleValue( sf, (GenericData.Record) value, elementType, defaultSchema, ignoreMissing );
            } else if ( elementType.getType() == Schema.Type.ARRAY ) {
              result[ i ][ sf.getOutputIndex() ] =
                convertToKettleValue( sf, (GenericData.Array) value, elementType, defaultSchema, ignoreMissing );
            } else if ( elementType.getType() == Schema.Type.MAP ) {
              result[ i ][ sf.getOutputIndex() ] =
                convertToKettleValue( sf, (Map<Utf8, Object>) value, elementType, defaultSchema, ignoreMissing );
            } else {
              // assume a primitive
              result[ i ][ sf.getOutputIndex() ] = getPrimitive( sf, value, elementType );
            }
          }

        }
        return result;
      } else {
        int arrayI = 0;
        try {
          arrayI = Integer.parseInt( index.trim() );
        } catch ( NumberFormatException e ) {
          throw new KettleException( BaseMessages.getString( PKG,
            "AvroInput.Error.UnableToParseArrayIndex", index ) );
        }

        if ( arrayI >= array.size() || arrayI < 0 ) {

          // index is out of bounds
          Object[][] result = new Object[ 1 ][ m_outputRowMeta.size() + RowDataUtil.OVER_ALLOCATE_SIZE ];
          for ( int i = 0; i < m_subFields.size(); i++ ) {
            AvroInputField sf = m_subFields.get( i );
            result[ 0 ][ sf.getOutputIndex() ] = null;
          }

          return result;
        }

        Object value = array.get( arrayI );
        Schema elementType = s.getElementType();

        if ( elementType.getType() == Schema.Type.UNION ) {
          if ( value instanceof GenericContainer ) {
            // we can ask these things for their schema (covers
            // records, arrays, enums and fixed)
            elementType = ( (GenericContainer) value ).getSchema();
          } else {
            // either have a map or primitive here
            if ( value instanceof Map ) {
              // now have to look for the schema of the map
              Schema mapSchema = null;
              for ( Schema ts : elementType.getTypes() ) {
                if ( ts.getType() == Schema.Type.MAP ) {
                  mapSchema = ts;
                  break;
                }
              }
              if ( mapSchema == null ) {
                throw new KettleException( BaseMessages.getString( PKG,
                  "AvroInput.Error.UnableToFindSchemaForUnionMap" ) );
              }
              elementType = mapSchema;
            } else {
              // We shouldn't have a primitive here
              if ( !ignoreMissing ) {
                throw new KettleException( BaseMessages.getString( PKG,
                  "AvroInput.Error.EncounteredAPrimitivePriorToMapExpansion" ) );
              }
              Object[][] result = new Object[ 1 ][ m_outputRowMeta.size() + RowDataUtil.OVER_ALLOCATE_SIZE ];
              return result;
            }
          }
        }

        // what have we got?
        if ( elementType.getType() == Schema.Type.RECORD ) {
          return convertToKettleValues( (GenericData.Record) value, elementType, defaultSchema, space, ignoreMissing );
        } else if ( elementType.getType() == Schema.Type.ARRAY ) {
          return convertToKettleValues( (GenericData.Array) value, elementType, defaultSchema, space, ignoreMissing );
        } else if ( elementType.getType() == Schema.Type.MAP ) {
          return convertToKettleValues( (Map<Utf8, Object>) value, elementType, defaultSchema, space, ignoreMissing );
        } else {
          // we shouldn't have a primitive at this point. If we are
          // extracting a particular index from the array then we're not to the
          // expansion phase,
          // so normally there must be a non-primitive sub-structure. Only if
          // the user is switching schema versions on a per-row basis or the
          // schema is a union at the top level could we end up here
          if ( !ignoreMissing ) {
            throw new KettleException( BaseMessages.getString( PKG,
              "AvroInput.Error.UnexpectedArrayElementTypeAtNonExpansionPoint" ) );
          } else {
            Object[][] result = new Object[ 1 ][ m_outputRowMeta.size() + RowDataUtil.OVER_ALLOCATE_SIZE ];
            return result;
          }
        }
      }
    }

    /**
     * Processes a record at this point in the path.
     *
     * @param record        the record to process
     * @param s             the current schema at this point in the path
     * @param space         environment variables
     * @param ignoreMissing true if null is to be returned for user fields that don't appear in the schema
     * @return an array of Kettle rows corresponding to the expanded map/array and containing all leaf values as defined
     * in the paths
     * @throws KettleException if a problem occurs
     */
    public Object[][] convertToKettleValues( GenericData.Record record, Schema s, Schema defaultSchema,
                                             VariableSpace space,
                                             boolean ignoreMissing ) throws KettleException {

      if ( record == null ) {
        return null;
      }

      if ( m_tempParts.size() == 0 ) {
        throw new KettleException( BaseMessages.getString( PKG, "AvroInput.Error.MalformedPathRecord" ) );
      }

      String part = m_tempParts.remove( 0 );
      if ( part.charAt( 0 ) == '[' ) {
        throw new KettleException( BaseMessages.getString( PKG, "AvroInput.Error.InvalidPath" )
          + m_tempParts );
      }

      if ( part.indexOf( '[' ) > 0 ) {
        String arrayPart = part.substring( part.indexOf( '[' ) );
        part = part.substring( 0, part.indexOf( '[' ) );

        // put the array section back into location zero
        m_tempParts.add( 0, arrayPart );
      }

      // part is a named field of the record
      Schema.Field fieldS = s.getField( part );

      if ( fieldS == null ) {
        if ( !ignoreMissing ) {
          throw new KettleException( BaseMessages.getString( PKG, "AvroInput.Error.NonExistentField",
            part ) );
        }
      }

      Object field = record.get( part );

      if ( field == null ) {
        // field is null and we haven't hit the expansion yet. There will be
        // nothing
        // to return for all the sub-fields grouped in the expansion
        Object[][] result = new Object[ 1 ][ m_outputRowMeta.size() + RowDataUtil.OVER_ALLOCATE_SIZE ];
        return result;
      }

      Schema.Type fieldT = fieldS.schema().getType();
      Schema fieldSchema = fieldS.schema();
      if ( fieldT == Schema.Type.UNION ) {
        if ( field instanceof GenericContainer ) {
          // we can ask these things for their schema (covers
          // records, arrays, enums and fixed)
          fieldSchema = ( (GenericContainer) field ).getSchema();
          fieldT = fieldSchema.getType();
        } else {
          // either have a map or primitive here
          if ( field instanceof Map ) {
            // now have to look for the schema of the map
            Schema mapSchema = null;
            for ( Schema ts : fieldSchema.getTypes() ) {
              if ( ts.getType() == Schema.Type.MAP ) {
                mapSchema = ts;
                break;
              }
            }
            if ( mapSchema == null ) {
              throw new KettleException( BaseMessages.getString( PKG,
                "AvroInput.Error.UnableToFindSchemaForUnionMap" ) );
            }
            fieldSchema = mapSchema;
            fieldT = Schema.Type.MAP;
          } else {
            // We shouldn't have a primitive here
            if ( !ignoreMissing ) {
              throw new KettleException( BaseMessages.getString( PKG,
                "AvroInput.Error.EncounteredAPrimitivePriorToMapExpansion" ) );
            }
            Object[][] result = new Object[ 1 ][ m_outputRowMeta.size() + RowDataUtil.OVER_ALLOCATE_SIZE ];
            return result;
          }
        }
      }

      // what have we got?
      if ( fieldT == Schema.Type.RECORD ) {
        return convertToKettleValues( (GenericData.Record) field, fieldSchema, defaultSchema, space, ignoreMissing );
      } else if ( fieldT == Schema.Type.ARRAY ) {
        return convertToKettleValues( (GenericData.Array) field, fieldSchema, defaultSchema, space, ignoreMissing );
      } else if ( fieldT == Schema.Type.MAP ) {

        return convertToKettleValues( (Map<Utf8, Object>) field, fieldSchema, defaultSchema, space, ignoreMissing );
      } else {
        // primitives will always be handled by the subField delegates, so we
        // should'nt
        // get here
        throw new KettleException( BaseMessages.getString( PKG,
          "AvroInput.Error.UnexpectedRecordFieldTypeAtNonExpansionPoint" ) );
      }
    }
  }

  // ----------------- End AvroArrayExpansion inner class --------------------------------

  /**
   * Converts an incoming row to outgoing format. Extracts fields from either an Avro object in the incoming row or from
   * the next structure in the container or non-container Avro file. May return more than one row if a map/array is
   * being expanded.
   *
   * @param incoming incoming kettle row - may be null if decoding from a file rather than a field
   * @param space    the variables to use
   * @return one or more rows in the outgoing format
   * @throws KettleException if a problem occurs
   */
  public Object[][] avroObjectToKettle( Object[] incoming, VariableSpace space ) throws KettleException {

    if ( m_containerReader != null ) {
      // container file
      try {
        if ( m_containerReader.hasNext() ) {
          if ( m_topLevelRecord != null ) {
            // special case for top-level record. In case we actually
            // have a top level union, reassign the record so that
            // we have the correctly populated object in the case
            // where our last record instance can't be reused (i.e.
            // the next record read is a different one from the union
            // than the last one).
            m_topLevelRecord = (GenericData.Record) m_containerReader.next( m_topLevelRecord );
          } else if ( m_topLevelArray != null ) {
            m_containerReader.next( m_topLevelArray );
          } else {
            m_containerReader.next( m_topLevelMap );
          }

          return setKettleFields( incoming, space );
        } else {
          return null; // no more input
        }
      } catch ( IOException e ) {
        throw new KettleException( BaseMessages
          .getString( PKG, "AvroInput.Error.ObjectReadError" ) );
      }
    } else {
      // non-container file
      try {
        /*
         * if (m_decoder.isEnd()) { return null; }
         */

        // reading from an incoming field
        if ( m_decodingFromField ) {
          if ( incoming == null || incoming.length == 0 ) {
            // must be done - just return null
            return null;
          }
          ValueMetaInterface fieldMeta = m_incomingRowMeta.getValueMeta( m_fieldToDecodeIndex );

          // incoming avro field null? - all decoded fields are null
          if ( fieldMeta.isNull( incoming[ m_fieldToDecodeIndex ] ) ) {
            Object[][] result = new Object[ 1 ][];
            // just resize the existing incoming array (if necessary) and return
            // the incoming values
            result[ 0 ] = RowDataUtil.resizeArray( incoming, m_outputRowMeta.size() );
            return result;
          }

          // if necessary, set the current datum reader and top level structure
          // for the incoming schema
          if ( m_schemaInField ) {
            ValueMetaInterface schemaMeta = m_incomingRowMeta.getValueMeta( m_schemaFieldIndex );
            String schemaToUse = schemaMeta.getString( incoming[ m_schemaFieldIndex ] );
            setSchemaToUse( schemaToUse, m_cacheSchemas, space );
          }
          if ( m_jsonEncoded ) {
            try {
              String fieldValue = fieldMeta.getString( incoming[ m_fieldToDecodeIndex ] );
              m_decoder = m_factory.jsonDecoder( m_schemaToUse, fieldValue );
            } catch ( IOException e ) {
              throw new KettleException(
                BaseMessages.getString( PKG,
                  "AvroInput.Error.JsonDecoderError" ) );
            }
          } else {
            byte[] fieldValue = fieldMeta.getBinary( incoming[ m_fieldToDecodeIndex ] );
            m_decoder = m_factory.binaryDecoder( fieldValue, null );
          }
        }

        if ( m_topLevelRecord != null ) {
          // special case for top-level record. In case we actually
          // have a top level union, reassign the record so that
          // we have the correctly populated object in the case
          // where our last record instance can't be reused (i.e.
          // the next record read is a different one from the union
          // than the last one).

          m_topLevelRecord = (GenericData.Record) m_datumReader.read( m_topLevelRecord, m_decoder );
        } else if ( m_topLevelArray != null ) {
          m_datumReader.read( m_topLevelArray, m_decoder );
        } else {
          m_datumReader.read( m_topLevelMap, m_decoder );
        }

        return setKettleFields( incoming, space );
      } catch ( IOException ex ) {
        // some IO problem or no more input
        return null;
      }
    }
  }

  private Object[][] setKettleFields( Object[] outputRowData, VariableSpace space ) throws KettleException {
    Object[][] result = null;

    // expand map/array in path structure to multiple rows (if necessary)
    if ( m_expansionHandler != null ) {
      m_expansionHandler.reset( space );

      if ( m_schemaToUse.getType() == Schema.Type.RECORD || m_schemaToUse.getType() == Schema.Type.UNION ) {
        // call getSchema() on the top level record here in case it has been
        // read as one of the elements from a top-level union
        result =
          m_expansionHandler
            .convertToKettleValues( m_topLevelRecord, m_topLevelRecord.getSchema(), m_defaultSchema, space,
              m_dontComplainAboutMissingFields );
      } else if ( m_schemaToUse.getType() == Schema.Type.ARRAY ) {
        result =
          m_expansionHandler.convertToKettleValues( m_topLevelArray, m_schemaToUse, m_defaultSchema, space,
            m_dontComplainAboutMissingFields );
      } else {
        result =
          m_expansionHandler.convertToKettleValues( m_topLevelMap, m_schemaToUse, m_defaultSchema, space,
            m_dontComplainAboutMissingFields );
      }
    } else {
      result = new Object[ 1 ][];
    }

    // if there are no incoming rows (i.e. we're decoding from a file rather
    // than a field
    if ( outputRowData == null ) {
      outputRowData = RowDataUtil.allocateRowData( m_outputRowMeta.size() );
    } else {
      // make sure we allocate enough space for the new fields
      outputRowData = RowDataUtil.createResizedCopy( outputRowData, m_outputRowMeta.size() );
    }

    // get the normal (non expansion-related fields)
    Object value = null;
    int incomingFieldsOffset = m_outputRowMeta.size() - m_normalFields.size();

    for ( AvroInputField f : m_normalFields ) {
      resetField( f, space );

      if ( m_schemaToUse.getType() == Schema.Type.RECORD || m_schemaToUse.getType() == Schema.Type.UNION ) {
        // call getSchema() on the top level record here in case it has been
        // read as one of the elements from a top-level union
        value =
          convertToKettleValue( f, m_topLevelRecord, m_topLevelRecord.getSchema(), m_defaultSchema,
            m_dontComplainAboutMissingFields );
      } else if ( m_schemaToUse.getType() == Schema.Type.ARRAY ) {
        value =
          convertToKettleValue( f, m_topLevelArray, m_schemaToUse, m_defaultSchema, m_dontComplainAboutMissingFields );
      } else {
        value =
          convertToKettleValue( f, m_topLevelMap, m_schemaToUse, m_defaultSchema, m_dontComplainAboutMissingFields );
      }

      outputRowData[ f.getOutputIndex() + incomingFieldsOffset ] = value;
    }

    // copy normal fields and existing incoming over to each expansion row (if
    // necessary)
    if ( m_expansionHandler == null ) {
      result[ 0 ] = outputRowData;
    } else if ( m_normalFields.size() > 0 || m_newFieldOffset > 0 ) {
      for ( int i = 0; i < result.length; i++ ) {
        Object[] row = result[ i ];

        // existing incoming fields
        for ( int j = 0; j < m_newFieldOffset; j++ ) {
          row[ j ] = outputRowData[ j ];
        }
        int rowIndex = 0;
        for ( int x = 0; x < outputRowData.length; x++ ) {
          if ( outputRowData[ x ] != null && rowIndex < row.length ) {
            row[ rowIndex++ ] = outputRowData[ x ];
          }
        }
      }
    }

    return result;
  }

  public void close() throws IOException {
    if ( m_containerReader != null ) {
      m_containerReader.close();
    }
    if ( m_inStream != null ) {
      m_inStream.close();
    }

  }

  protected void setSchemaToUse( String schemaKey, boolean useCache, VariableSpace space ) throws KettleException {

    if ( Const.isEmpty( schemaKey ) ) {
      // switch to default
      if ( m_defaultDatumReader == null ) {
        // no key, no default schema - can't continue with this row
        throw new KettleException( BaseMessages.getString( PKG,
          "AvroInput.Error.IncommingSchemaIsMissingAndNoDefault" ) );
      }
      if ( m_log.isDetailed() ) {
        m_log.logDetailed( BaseMessages.getString( PKG, "AvroInput.Message.IncommingSchemaIsMissing" ) );
      }
      m_datumReader = m_defaultDatumReader;
      m_schemaToUse = m_datumReader.getSchema();
      setTopLevelStructure( m_defaultTopLevelObject );
      return;
    } else {
      schemaKey = schemaKey.trim();
      schemaKey = space.environmentSubstitute( schemaKey );
    }

    Object[] cached = null;
    if ( useCache ) {
      cached = m_schemaCache.get( schemaKey );
      if ( m_log.isDetailed() && cached != null ) {
        m_log.logDetailed(
          BaseMessages.getString( PKG, "AvroInput.Message.UsingCachedSchema", schemaKey ) );
      }
    }

    if ( !useCache || cached == null ) {
      Schema toUse = null;
      if ( m_schemaFieldIsPath ) {
        // load the schema from disk
        if ( m_log.isDetailed() ) {
          m_log.logDetailed(
            BaseMessages.getString( PKG, "AvroInput.Message.LoadingSchema", schemaKey ) );
        }
        try {
          toUse = loadSchema( schemaKey );
        } catch ( KettleException ex ) {
          // fall back to default (if possible)
          if ( m_defaultDatumReader != null ) {
            if ( m_log.isBasic() ) {
              m_log.logBasic( BaseMessages.getString( PKG,
                "AvroInput.Message.FailedToLoadSchmeaUsingDefault", schemaKey ) );
            }
            m_datumReader = m_defaultDatumReader;
            m_schemaToUse = m_datumReader.getSchema();
            setTopLevelStructure( m_defaultTopLevelObject );
            return;
          } else {
            throw new KettleException( BaseMessages.getString( PKG,
              "AvroInput.Error.CantLoadIncommingSchemaAndNoDefault", schemaKey ) );
          }
        }
      } else {
        // use the supplied schema
        if ( m_log.isDetailed() ) {
          m_log.logDetailed(
            BaseMessages.getString( PKG, "AvroInput.Message.ParsingSchema", schemaKey ) );
        }
        Schema.Parser p = new Schema.Parser();
        toUse = p.parse( schemaKey );
      }
      m_schemaToUse = toUse;
      m_datumReader = new GenericDatumReader( toUse );
      initTopLevelStructure( toUse, false );
      if ( useCache ) {
        Object[] schemaInfo = new Object[ 2 ];
        schemaInfo[ 0 ] = m_datumReader;
        schemaInfo[ 1 ] =
          ( m_topLevelArray != null ) ? m_topLevelArray : ( ( m_topLevelRecord != null ) ? m_topLevelRecord
            : m_topLevelMap );
        if ( m_log.isDetailed() ) {
          m_log.logDetailed( BaseMessages.getString( PKG, "AvroInput.Message.StoringSchemaInCache" ) );
        }
        m_schemaCache.put( schemaKey, schemaInfo );
      }
    } else if ( useCache ) {
      // got one from the cache
      m_datumReader = (GenericDatumReader) cached[ 0 ];
      m_schemaToUse = m_datumReader.getSchema();
      setTopLevelStructure( cached[ 1 ] );
    }
  }

  protected void setTopLevelStructure( Object topLevel ) {
    if ( topLevel instanceof GenericData.Record ) {
      m_topLevelRecord = (GenericData.Record) topLevel;
      m_topLevelArray = null;
      m_topLevelMap = null;
    } else if ( topLevel instanceof GenericData.Array ) {
      m_topLevelArray = (GenericData.Array<?>) topLevel;
      m_topLevelRecord = null;
      m_topLevelMap = null;
    } else {
      m_topLevelMap = (HashMap<Utf8, Object>) topLevel;
      m_topLevelRecord = null;
      m_topLevelArray = null;
    }
  }

  /**
   * Load a schema from a file
   *
   * @param schemaFile the file to load from
   * @return the schema
   * @throws KettleException if a problem occurs
   */
  protected static Schema loadSchema( String schemaFile ) throws KettleException {

    Schema s = null;
    Schema.Parser p = new Schema.Parser();

    FileObject fileO = KettleVFS.getFileObject( schemaFile );
    try {
      InputStream in = KettleVFS.getInputStream( fileO );
      s = p.parse( in );

      in.close();
    } catch ( FileSystemException e ) {
      throw new KettleException( BaseMessages.getString( PKG, "AvroInput.Error.SchemaError" ), e );
    } catch ( IOException e ) {
      throw new KettleException( BaseMessages.getString( PKG, "AvroInput.Error.SchemaError" ), e );
    }

    return s;
  }

  /**
   * Load a schema from a Avro container file
   *
   * @param containerFilename the name of the Avro container file
   * @return the schema
   * @throws KettleException if a problem occurs
   */
  protected static Schema loadSchemaFromContainer( String containerFilename ) throws KettleException {
    Schema s = null;

    FileObject fileO = KettleVFS.getFileObject( containerFilename );
    InputStream in = null;

    try {
      in = KettleVFS.getInputStream( fileO );
      GenericDatumReader dr = new GenericDatumReader();
      DataFileStream reader = new DataFileStream( in, dr );
      s = reader.getSchema();

      reader.close();
    } catch ( FileSystemException e ) {
      throw new KettleException( BaseMessages
        .getString( PKG, "AvroInputDialog.Error.KettleFileException" ), e );
    } catch ( IOException e ) {
      throw new KettleException( BaseMessages
        .getString( PKG, "AvroInputDialog.Error.KettleFileException" ), e );
    }

    return s;
  }

}

