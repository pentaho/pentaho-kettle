package org.pentaho.di.trans.steps.avroinput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.pentaho.di.trans.steps.avroinput.AvroInputData.checkFieldPaths;
import static org.pentaho.di.trans.steps.avroinput.AvroInputData.getLeafFields;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.util.Utf8;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.steps.avroinput.AvroInputData.AvroArrayExpansion;

/**
 * Unit tests for AvroInput. Tests basic path handling logic and map expansion
 * mechanism.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: $
 */
public class AvroInputTest {

  protected static String s_schemaTopLevelRecordManyFields = "{"
      + "\"type\": \"record\"," + "\"name\": \"Test\"," + "\"fields\": ["
      + "{\"name\": \"field1\", \"type\": \"string\"},"
      + "{\"name\": \"field2\", \"type\": \"string\"},"
      + "{\"name\": \"field3\", \"type\": \"string\"},"
      + "{\"name\": \"field4\", \"type\": \"string\"},"
      + "{\"name\": \"field5\", \"type\": \"string\"},"
      + "{\"name\": \"field6\", \"type\": \"string\"},"
      + "{\"name\": \"field7\", \"type\": \"string\"},"
      + "{\"name\": \"field8\", \"type\": \"string\"},"
      + "{\"name\": \"field9\", \"type\": \"string\"},"
      + "{\"name\": \"field10\", \"type\": \"string\"},"
      + "{\"name\": \"field11\", \"type\": \"string\"},"
      + "{\"name\": \"field12\", \"type\": \"string\"}" + "]" + "}";

  protected static String[] s_jsonDataTopLevelRecordManyFields = new String[] { "{\"field1\":\"value1\",\"field2\":\"value2\",\"field3\":\"value3\",\"field4\":\"value4\","
      + "\"field5\":\"value5\",\"field6\":\"value6\",\"field7\":\"value7\",\"field8\":\"value8\","
      + "\"field9\":\"value9\",\"field10\":\"value10\",\"field11\":\"value11\",\"field12\":\"value12\"}" };

  protected static String s_schemaTopLevelRecord = "{"
      + "\"type\": \"record\","
      + "\"name\": \"Person\","
      + "\"fields\": ["
      + "{\"name\": \"name\", \"type\": \"string\"},"
      + "{\"name\": \"age\", \"type\": \"int\"},"
      + "{\"name\": \"emails\", \"type\": {\"type\": \"array\", \"items\": \"string\"}}"
      + "]" + "}";

  protected static String[] s_jsonDataTopLevelRecord = new String[] {
      "{\"name\":\"bob\",\"age\":20,\"emails\":[\"here is an email\",\"and another one\"]}",
      "{\"name\":\"fred\",\"age\":25,\"emails\":[\"hi there bob\",\"good to see you!\",\"Yarghhh!\"]}",
      "{\"name\":\"zaphod\",\"age\":254,\"emails\":[\"I'm from beetlejuice\",\"yeah yeah yeah\"]}" };

  protected static String s_schemaTopLevelRecordWithUnion = "{"
      + "\"type\": \"record\","
      + "\"name\": \"Person\","
      + "\"fields\": ["
      + "{\"name\": \"name\", \"type\": [\"string\", \"null\"]},"
      + "{\"name\": \"age\", \"type\": \"int\"},"
      + "{\"name\": \"emails\", \"type\": {\"type\": \"array\", \"items\": \"string\"}}"
      + "]" + "}";

  protected static String[] s_jsonDataTopLevelRecordWithUnion = new String[] {
      "{\"name\":{\"string\":\"bob\"},\"age\":20,\"emails\":[\"here is an email\",\"and another one\"]}",
      "{\"name\":{\"string\":\"fred\"},\"age\":25,\"emails\":[\"hi there bob\",\"good to see you!\",\"Yarghhh!\"]}",
      "{\"name\":null,\"age\":254,\"emails\":[\"I'm from beetlejuice\",\"yeah yeah yeah\"]}" };

  protected static String s_schemaTopLevelMap = "{"
      + "\"type\": \"map\","
      + "\"values\":{"
      + "\"type\": \"record\","
      + "\"name\":\"person\","
      + "\"fields\": ["
      + "{\"name\": \"name\", \"type\": \"string\"},"
      + "{\"name\": \"age\", \"type\": \"int\"},"
      + "{\"name\": \"emails\", \"type\": {\"type\": \"array\", \"items\": \"string\"}}"
      + "]" + "}" + "}";

  protected static String s_jsonDataTopLevelMap = "{\"bob\":{\"name\":\"bob\",\"age\":20,\"emails\":[\"here is an email\",\"and another one\"]},"
      + "\"fred\":{\"name\":\"fred\",\"age\":25,\"emails\":[\"hi there bob\",\"good to see you!\",\"Yarghhh!\"]},"
      + "\"zaphod\":{\"name\":\"zaphod\",\"age\":254,\"emails\":[\"I'm from beetlejuice\",\"yeah yeah yeah\"]}}";

  @Test
  public void testGetLeafFieldsFromSchema() throws KettleException {

    Schema.Parser parser = new Schema.Parser();
    Schema schema = parser.parse(s_schemaTopLevelRecord);
    List<AvroInputMeta.AvroField> leafFields = getLeafFields(schema);

    assertTrue(leafFields.size() == 3);
  }

  @Test
  public void testGetSimpleTopLevelRecordFieldsInteger()
      throws KettleException, IOException {
    Schema.Parser parser = new Schema.Parser();
    Schema schema = parser.parse(s_schemaTopLevelRecord);

    Decoder decoder;
    DecoderFactory factory = new DecoderFactory();

    GenericData.Record topLevel = new GenericData.Record(schema);
    GenericDatumReader reader = new GenericDatumReader(schema);

    AvroInputMeta.AvroField field = new AvroInputMeta.AvroField();

    field.m_fieldName = "test";
    field.m_fieldPath = "$.age";
    field.m_kettleType = ValueMeta.getAllTypes()[ValueMetaInterface.TYPE_INTEGER];

    Long[] actualVals = new Long[] { 20L, 25L, 254L };
    int i = 0;
    for (String row : s_jsonDataTopLevelRecord) {
      decoder = factory.jsonDecoder(schema, row);
      reader.read(topLevel, decoder);

      field.init(0); // output index isn't needed for the test
      field.reset(new Variables());

      Object result = field.convertToKettleValue(topLevel, schema);

      assertTrue(result != null);
      assertTrue(result instanceof Long);
      assertEquals(result, actualVals[i++]);
    }
  }

  @Test
  public void testGetSimpleTopLevelRecordFieldsString() throws KettleException,
      IOException {
    Schema.Parser parser = new Schema.Parser();
    Schema schema = parser.parse(s_schemaTopLevelRecord);

    Decoder decoder;
    DecoderFactory factory = new DecoderFactory();

    GenericData.Record topLevel = new GenericData.Record(schema);
    GenericDatumReader reader = new GenericDatumReader(schema);

    AvroInputMeta.AvroField field = new AvroInputMeta.AvroField();
    field.m_fieldName = "test";
    field.m_fieldPath = "$.name";
    field.m_kettleType = ValueMeta.getAllTypes()[ValueMetaInterface.TYPE_STRING];

    String[] actualVals = new String[] { "bob", "fred", "zaphod" };
    int i = 0;
    for (String row : s_jsonDataTopLevelRecord) {
      decoder = factory.jsonDecoder(schema, row);
      reader.read(topLevel, decoder);

      field.init(0); // output index isn't needed for the test
      field.reset(new Variables());

      Object result = field.convertToKettleValue(topLevel, schema);

      assertTrue(result != null);
      assertTrue(result instanceof String);
      assertEquals(result.toString(), actualVals[i++]);
    }
  }

  @Test
  public void testConvertToKettleRowManyFields() throws KettleException,
      IOException {
    Schema.Parser parser = new Schema.Parser();
    Schema schema = parser.parse(s_schemaTopLevelRecordManyFields);

    // Decoder decoder;
    DecoderFactory factory = new DecoderFactory();

    GenericData.Record topLevel = new GenericData.Record(schema);
    GenericDatumReader reader = new GenericDatumReader(schema);

    List<AvroInputMeta.AvroField> paths = new ArrayList<AvroInputMeta.AvroField>();
    for (int i = 1; i <= 12; i++) {
      AvroInputMeta.AvroField field = new AvroInputMeta.AvroField();
      field.m_fieldName = "test" + i;
      field.m_fieldPath = "$.field" + i;
      field.m_kettleType = ValueMeta.getAllTypes()[ValueMetaInterface.TYPE_STRING];
      paths.add(field);
    }

    String incomingAvro = s_jsonDataTopLevelRecordManyFields[0];
    Object[] incomingKettleRow = new Object[1];
    incomingKettleRow[0] = incomingAvro;
    // decoder = factory.jsonDecoder(schema, row);

    AvroInputData data = new AvroInputData();

    VariableSpace space = new Variables();
    RowMetaInterface outputMeta = new RowMeta();
    ValueMetaInterface vm = new ValueMeta();
    vm.setName("IncomingAvro");
    vm.setOrigin("Dummy");
    vm.setType(ValueMetaInterface.TYPE_STRING);
    outputMeta.addValueMeta(vm);

    for (int i = 0; i < 12; i++) {
      AvroInputMeta.AvroField f = paths.get(i);
      vm = new ValueMeta();
      vm.setName(f.m_fieldName);
      vm.setOrigin("Dummy");
      vm.setType(ValueMetaInterface.TYPE_STRING);
      outputMeta.addValueMeta(vm);
    }

    data.m_normalFields = paths;
    data.m_decodingFromField = true;
    data.m_jsonEncoded = true;
    data.m_newFieldOffset = 1;
    data.m_inStream = null;
    data.m_fieldToDecodeIndex = 0;
    data.m_schemaToUse = schema;
    data.m_topLevelRecord = topLevel;
    data.m_factory = factory;
    data.m_datumReader = reader;
    data.m_outputRowMeta = outputMeta;
    data.init();

    Object[][] result = data.avroObjectToKettle(incomingKettleRow, space);

    assertTrue(result != null);
    assertTrue(result.length == 1); // one output row

    int count = 0;
    for (int i = 0; i < result[0].length; i++) {
      if (result[0][i] != null) {
        count++;
      }
    }

    // should be 13 output fields (incoming + 12 decoded) in over allocated
    // kettle output row
    assertTrue(count == 13);
  }

  @Test
  public void testGetNonExistentFieldFromTopLevelRecord()
      throws KettleException, IOException {
    Schema.Parser parser = new Schema.Parser();
    Schema schema = parser.parse(s_schemaTopLevelRecord);

    Decoder decoder;
    DecoderFactory factory = new DecoderFactory();

    GenericData.Record topLevel = new GenericData.Record(schema);
    GenericDatumReader reader = new GenericDatumReader(schema);

    AvroInputMeta.AvroField field = new AvroInputMeta.AvroField();
    field.m_fieldName = "test";
    field.m_fieldPath = "$.nonExistent.notThere";
    field.m_kettleType = ValueMeta.getAllTypes()[ValueMetaInterface.TYPE_STRING];

    decoder = factory.jsonDecoder(schema, s_jsonDataTopLevelRecord[0]);
    reader.read(topLevel, decoder);

    field.init(0);
    field.reset(new Variables());

    try {
      Object result = field.convertToKettleValue(topLevel, schema);
      fail("Was expecting an exception as $.nonExistent.notThere does not exist in the schma");
    } catch (Exception ex) {
      assertTrue(ex.getMessage().contains(
          "Field nonExistent does not seem to exist in the schema!"));
    }
  }

  @Test
  public void testGetTopLevelRecordArrayElement() throws KettleException,
      IOException {
    Schema.Parser parser = new Schema.Parser();
    Schema schema = parser.parse(s_schemaTopLevelRecord);

    Decoder decoder;
    DecoderFactory factory = new DecoderFactory();

    GenericData.Record topLevel = new GenericData.Record(schema);
    GenericDatumReader reader = new GenericDatumReader(schema);

    AvroInputMeta.AvroField field = new AvroInputMeta.AvroField();

    field.m_fieldName = "test";
    field.m_fieldPath = "$.emails[1]";
    field.m_kettleType = ValueMeta.getAllTypes()[ValueMetaInterface.TYPE_STRING];

    String actualVals[] = new String[] { "and another one", "good to see you!",
        "yeah yeah yeah" };
    int i = 0;
    for (String row : s_jsonDataTopLevelRecord) {
      decoder = factory.jsonDecoder(schema, row);
      reader.read(topLevel, decoder);

      field.init(0); // output index isn't needed for the test
      field.reset(new Variables());

      Object result = field.convertToKettleValue(topLevel, schema);

      assertTrue(result != null);
      assertTrue(result instanceof String);
      assertEquals(result.toString(), actualVals[i++]);
    }
  }

  @Test
  public void testGetTopLevelRecordPositiveIndexOutOfBoundsArrayElement()
      throws KettleException, IOException {

    Schema.Parser parser = new Schema.Parser();
    Schema schema = parser.parse(s_schemaTopLevelRecord);

    Decoder decoder;
    DecoderFactory factory = new DecoderFactory();

    GenericData.Record topLevel = new GenericData.Record(schema);
    GenericDatumReader reader = new GenericDatumReader(schema);

    AvroInputMeta.AvroField field = new AvroInputMeta.AvroField();

    field.m_fieldName = "test";
    field.m_fieldPath = "$.emails[4]"; // non existent in all records
    field.m_kettleType = ValueMeta.getAllTypes()[ValueMetaInterface.TYPE_STRING];

    // no exception is thrown in this case - the step just outputs null for the
    // corresponding field
    for (String row : s_jsonDataTopLevelRecord) {
      decoder = factory.jsonDecoder(schema, row);
      reader.read(topLevel, decoder);

      field.init(0); // output index isn't needed for the test
      field.reset(new Variables());

      Object result = field.convertToKettleValue(topLevel, schema);
      assertTrue(result == null);
    }
  }

  @Test
  public void testGetTopLevelMapSimpleRecordField() throws KettleException,
      IOException {
    Schema.Parser parser = new Schema.Parser();
    Schema schema = parser.parse(s_schemaTopLevelMap);

    Decoder decoder;
    DecoderFactory factory = new DecoderFactory();

    Map<Utf8, Object> topLevel = new HashMap<Utf8, Object>();
    GenericDatumReader reader = new GenericDatumReader(schema);

    AvroInputMeta.AvroField field = new AvroInputMeta.AvroField();
    field.m_fieldName = "test";
    field.m_fieldPath = "$[bob].age";
    field.m_kettleType = ValueMeta.getAllTypes()[ValueMetaInterface.TYPE_INTEGER];

    decoder = factory.jsonDecoder(schema, s_jsonDataTopLevelMap);
    reader.read(topLevel, decoder);

    field.init(0); // output index isn't needed for the test
    field.reset(new org.pentaho.di.core.variables.Variables());

    Object result = field.convertToKettleValue(topLevel, schema);

    assertTrue(result != null);
    assertTrue(result instanceof Long);
    assertEquals(result, new Long(20));
  }

  // test getting an array element from an array in a record that is itself
  // stored in a
  // top-level map
  @Test
  public void testGetTopLevelMapArrayElementFromRecord()
      throws KettleException, IOException {
    Schema.Parser parser = new Schema.Parser();
    Schema schema = parser.parse(s_schemaTopLevelMap);

    Decoder decoder;
    DecoderFactory factory = new DecoderFactory();

    Map<Utf8, Object> topLevel = new HashMap<Utf8, Object>();
    GenericDatumReader reader = new GenericDatumReader(schema);

    AvroInputMeta.AvroField field = new AvroInputMeta.AvroField();
    field.m_fieldName = "test";
    field.m_fieldPath = "$[bob].emails[0]";
    field.m_kettleType = ValueMeta.getAllTypes()[ValueMetaInterface.TYPE_STRING];

    decoder = factory.jsonDecoder(schema, s_jsonDataTopLevelMap);
    reader.read(topLevel, decoder);

    field.init(0); // output index isn't needed for the test
    field.reset(new org.pentaho.di.core.variables.Variables());

    Object result = field.convertToKettleValue(topLevel, schema);

    assertTrue(result != null);
    assertTrue(result instanceof String);
    assertEquals(result, "here is an email");
  }

  @Test
  public void testGetNonExistentTopLevelMapEntry() throws KettleException,
      IOException {
    Schema.Parser parser = new Schema.Parser();
    Schema schema = parser.parse(s_schemaTopLevelMap);

    Decoder decoder;
    DecoderFactory factory = new DecoderFactory();

    Map<Utf8, Object> topLevel = new HashMap<Utf8, Object>();
    GenericDatumReader reader = new GenericDatumReader(schema);

    AvroInputMeta.AvroField field = new AvroInputMeta.AvroField();
    field.m_fieldName = "test";
    field.m_fieldPath = "$[noddy].emails[0]";
    field.m_kettleType = ValueMeta.getAllTypes()[ValueMetaInterface.TYPE_STRING];

    decoder = factory.jsonDecoder(schema, s_jsonDataTopLevelMap);
    reader.read(topLevel, decoder);

    field.init(0); // output index isn't needed for the test
    field.reset(new org.pentaho.di.core.variables.Variables());

    Object result = field.convertToKettleValue(topLevel, schema);

    assertTrue(result == null);
  }

  @Test
  public void testUnionHandling() throws KettleException, IOException {
    Schema.Parser parser = new Schema.Parser();
    Schema schema = parser.parse(s_schemaTopLevelRecordWithUnion);

    Decoder decoder;
    DecoderFactory factory = new DecoderFactory();

    GenericData.Record topLevel = new GenericData.Record(schema);
    GenericDatumReader reader = new GenericDatumReader(schema);

    AvroInputMeta.AvroField field = new AvroInputMeta.AvroField();
    field.m_fieldName = "test";
    field.m_fieldPath = "$.name";
    field.m_kettleType = ValueMeta.getAllTypes()[ValueMetaInterface.TYPE_STRING];

    String[] actualVals = new String[] { "bob", "fred", null };
    int i = 0;
    for (String row : s_jsonDataTopLevelRecordWithUnion) {
      decoder = factory.jsonDecoder(schema, row);
      reader.read(topLevel, decoder);

      field.init(0); // output index isn't needed for the test
      field.reset(new Variables());

      Object result = field.convertToKettleValue(topLevel, schema);

      if (i != 2) {
        assertTrue(result != null);
        assertTrue(result instanceof String);
        assertEquals(result.toString(), actualVals[i++]);
      } else {
        assertTrue(result == null);
      }
    }
  }

  @Test
  public void testMapExpansion() throws KettleException, IOException {
    Schema.Parser parser = new Schema.Parser();
    Schema schema = parser.parse(s_schemaTopLevelMap);

    Decoder decoder;
    DecoderFactory factory = new DecoderFactory();

    Map<Utf8, Object> topLevel = new HashMap<Utf8, Object>();
    GenericDatumReader reader = new GenericDatumReader(schema);

    AvroInputMeta.AvroField field = new AvroInputMeta.AvroField();
    field.m_fieldName = "test";
    field.m_fieldPath = "$[*].name";
    field.m_kettleType = ValueMeta.getAllTypes()[ValueMetaInterface.TYPE_STRING];
    List<AvroInputMeta.AvroField> normalFields = new ArrayList<AvroInputMeta.AvroField>();
    normalFields.add(field);
    RowMetaInterface rowMeta = new RowMeta();
    ValueMetaInterface vm = new ValueMeta("test",
        ValueMetaInterface.TYPE_STRING);
    rowMeta.addValueMeta(vm);

    AvroArrayExpansion expansion = checkFieldPaths(normalFields, rowMeta);
    expansion.init();
    expansion.reset(new Variables());

    decoder = factory.jsonDecoder(schema, s_jsonDataTopLevelMap);
    reader.read(topLevel, decoder);

    Object[][] result = expansion.convertToKettleValues(topLevel, schema,
        new Variables());

    assertTrue(result != null);
    assertTrue(result.length == 3);

    List<String> expectedNames = new ArrayList<String>();
    expectedNames.add("zaphod");
    expectedNames.add("bob");
    expectedNames.add("fred");

    for (int i = 0; i < result.length; i++) {
      assertTrue(result[i][0] != null);
      assertTrue(expectedNames.contains(result[i][0]));
    }
  }
}
