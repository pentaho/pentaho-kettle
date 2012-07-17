package org.pentaho.di.trans.steps.mongodboutput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.pentaho.di.trans.steps.mongodboutput.MongoDbOutputData.kettleRowToMongo;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;

import com.mongodb.DBObject;

/**
 * Unit tests for MongoDbOutput
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 */
public class MongoDbOutputTest {

  @Test
  public void testTopLevelObjectStructureNoNestedDocs() throws KettleException {
    List<MongoDbOutputMeta.MongoField> paths = new ArrayList<MongoDbOutputMeta.MongoField>();

    MongoDbOutputMeta.MongoField mf = new MongoDbOutputMeta.MongoField();
    mf.m_incomingFieldName = "field1";
    mf.m_mongoDocPath = "";
    mf.m_useIncomingFieldNameAsMongoFieldName = true;
    paths.add(mf);

    mf = new MongoDbOutputMeta.MongoField();
    mf.m_incomingFieldName = "field2";
    mf.m_mongoDocPath = "";
    mf.m_useIncomingFieldNameAsMongoFieldName = true;
    paths.add(mf);

    RowMetaInterface rmi = new RowMeta();
    ValueMetaInterface vm = new ValueMeta();
    vm.setName("field1");
    vm.setType(ValueMetaInterface.TYPE_STRING);
    rmi.addValueMeta(vm);
    vm = new ValueMeta();
    vm.setName("field2");
    vm.setType(ValueMetaInterface.TYPE_INTEGER);
    rmi.addValueMeta(vm);

    Object[] row = new Object[2];
    row[0] = "value1";
    row[1] = new Long(12);
    VariableSpace vs = new Variables();

    for (MongoDbOutputMeta.MongoField f : paths) {
      f.init(vs);
    }

    DBObject result = kettleRowToMongo(paths, rmi, row, vs,
        MongoDbOutputData.MongoTopLevel.RECORD);

    assertEquals(result.toString(),
        "{ \"field1\" : \"value1\" , \"field2\" : 12}");

  }

  @Test
  public void testTopLevelArrayStructureWithPrimitives() throws KettleException {
    List<MongoDbOutputMeta.MongoField> paths = new ArrayList<MongoDbOutputMeta.MongoField>();

    MongoDbOutputMeta.MongoField mf = new MongoDbOutputMeta.MongoField();
    mf.m_incomingFieldName = "field1";
    mf.m_mongoDocPath = "[0]";
    mf.m_useIncomingFieldNameAsMongoFieldName = false;
    paths.add(mf);

    mf = new MongoDbOutputMeta.MongoField();
    mf.m_incomingFieldName = "field2";
    mf.m_mongoDocPath = "[1]";
    mf.m_useIncomingFieldNameAsMongoFieldName = false;
    paths.add(mf);

    RowMetaInterface rmi = new RowMeta();
    ValueMetaInterface vm = new ValueMeta();
    vm.setName("field1");
    vm.setType(ValueMetaInterface.TYPE_STRING);
    rmi.addValueMeta(vm);
    vm = new ValueMeta();
    vm.setName("field2");
    vm.setType(ValueMetaInterface.TYPE_INTEGER);
    rmi.addValueMeta(vm);

    Object[] row = new Object[2];
    row[0] = "value1";
    row[1] = new Long(12);
    VariableSpace vs = new Variables();

    for (MongoDbOutputMeta.MongoField f : paths) {
      f.init(vs);
    }

    DBObject result = kettleRowToMongo(paths, rmi, row, vs,
        MongoDbOutputData.MongoTopLevel.ARRAY);

    assertEquals(result.toString(), "[ \"value1\" , 12]");
  }

  @Test
  public void testTopLevelArrayStructureWithObjects() throws KettleException {
    List<MongoDbOutputMeta.MongoField> paths = new ArrayList<MongoDbOutputMeta.MongoField>();

    MongoDbOutputMeta.MongoField mf = new MongoDbOutputMeta.MongoField();
    mf.m_incomingFieldName = "field1";
    mf.m_mongoDocPath = "[0]";
    mf.m_useIncomingFieldNameAsMongoFieldName = true;
    paths.add(mf);

    mf = new MongoDbOutputMeta.MongoField();
    mf.m_incomingFieldName = "field2";
    mf.m_mongoDocPath = "[1]";
    mf.m_useIncomingFieldNameAsMongoFieldName = true;
    paths.add(mf);

    RowMetaInterface rmi = new RowMeta();
    ValueMetaInterface vm = new ValueMeta();
    vm.setName("field1");
    vm.setType(ValueMetaInterface.TYPE_STRING);
    rmi.addValueMeta(vm);
    vm = new ValueMeta();
    vm.setName("field2");
    vm.setType(ValueMetaInterface.TYPE_INTEGER);
    rmi.addValueMeta(vm);

    Object[] row = new Object[2];
    row[0] = "value1";
    row[1] = new Long(12);
    VariableSpace vs = new Variables();

    for (MongoDbOutputMeta.MongoField f : paths) {
      f.init(vs);
    }

    DBObject result = kettleRowToMongo(paths, rmi, row, vs,
        MongoDbOutputData.MongoTopLevel.ARRAY);

    assertEquals(result.toString(),
        "[ { \"field1\" : \"value1\"} , { \"field2\" : 12}]");
  }

  @Test
  public void testTopLevelArrayStructureContainingOneObjectMutipleFields()
      throws KettleException {
    List<MongoDbOutputMeta.MongoField> paths = new ArrayList<MongoDbOutputMeta.MongoField>();

    MongoDbOutputMeta.MongoField mf = new MongoDbOutputMeta.MongoField();
    mf.m_incomingFieldName = "field1";
    mf.m_mongoDocPath = "[0]";
    mf.m_useIncomingFieldNameAsMongoFieldName = true;
    paths.add(mf);

    mf = new MongoDbOutputMeta.MongoField();
    mf.m_incomingFieldName = "field2";
    mf.m_mongoDocPath = "[0]";
    mf.m_useIncomingFieldNameAsMongoFieldName = true;
    paths.add(mf);

    RowMetaInterface rmi = new RowMeta();
    ValueMetaInterface vm = new ValueMeta();
    vm.setName("field1");
    vm.setType(ValueMetaInterface.TYPE_STRING);
    rmi.addValueMeta(vm);
    vm = new ValueMeta();
    vm.setName("field2");
    vm.setType(ValueMetaInterface.TYPE_INTEGER);
    rmi.addValueMeta(vm);

    Object[] row = new Object[2];
    row[0] = "value1";
    row[1] = new Long(12);
    VariableSpace vs = new Variables();

    for (MongoDbOutputMeta.MongoField f : paths) {
      f.init(vs);
    }

    DBObject result = kettleRowToMongo(paths, rmi, row, vs,
        MongoDbOutputData.MongoTopLevel.ARRAY);

    assertEquals(result.toString(),
        "[ { \"field1\" : \"value1\" , \"field2\" : 12}]");
  }

  @Test
  public void testTopLevelArrayStructureContainingObjectWithArray()
      throws KettleException {
    List<MongoDbOutputMeta.MongoField> paths = new ArrayList<MongoDbOutputMeta.MongoField>();

    MongoDbOutputMeta.MongoField mf = new MongoDbOutputMeta.MongoField();
    mf.m_incomingFieldName = "field1";
    mf.m_mongoDocPath = "[0].inner[0]";
    mf.m_useIncomingFieldNameAsMongoFieldName = true;
    paths.add(mf);

    mf = new MongoDbOutputMeta.MongoField();
    mf.m_incomingFieldName = "field2";
    mf.m_mongoDocPath = "[0].inner[1]";
    mf.m_useIncomingFieldNameAsMongoFieldName = true;
    paths.add(mf);

    RowMetaInterface rmi = new RowMeta();
    ValueMetaInterface vm = new ValueMeta();
    vm.setName("field1");
    vm.setType(ValueMetaInterface.TYPE_STRING);
    rmi.addValueMeta(vm);
    vm = new ValueMeta();
    vm.setName("field2");
    vm.setType(ValueMetaInterface.TYPE_INTEGER);
    rmi.addValueMeta(vm);

    Object[] row = new Object[2];
    row[0] = "value1";
    row[1] = new Long(12);
    VariableSpace vs = new Variables();

    for (MongoDbOutputMeta.MongoField f : paths) {
      f.init(vs);
    }

    DBObject result = kettleRowToMongo(paths, rmi, row, vs,
        MongoDbOutputData.MongoTopLevel.ARRAY);

    assertEquals(result.toString(),
        "[ { \"inner\" : [ { \"field1\" : \"value1\"} , { \"field2\" : 12}]}]");
  }

  @Test
  public void testTopLevelObjectStructureOneLevelNestedDoc()
      throws KettleException {
    List<MongoDbOutputMeta.MongoField> paths = new ArrayList<MongoDbOutputMeta.MongoField>();

    MongoDbOutputMeta.MongoField mf = new MongoDbOutputMeta.MongoField();
    mf.m_incomingFieldName = "field1";
    mf.m_mongoDocPath = "";
    mf.m_useIncomingFieldNameAsMongoFieldName = true;
    paths.add(mf);

    mf = new MongoDbOutputMeta.MongoField();
    mf.m_incomingFieldName = "field2";
    mf.m_mongoDocPath = "nestedDoc";
    mf.m_useIncomingFieldNameAsMongoFieldName = true;
    paths.add(mf);

    RowMetaInterface rmi = new RowMeta();
    ValueMetaInterface vm = new ValueMeta();
    vm.setName("field1");
    vm.setType(ValueMetaInterface.TYPE_STRING);
    rmi.addValueMeta(vm);
    vm = new ValueMeta();
    vm.setName("field2");
    vm.setType(ValueMetaInterface.TYPE_INTEGER);
    rmi.addValueMeta(vm);

    Object[] row = new Object[2];
    row[0] = "value1";
    row[1] = new Long(12);
    VariableSpace vs = new Variables();

    for (MongoDbOutputMeta.MongoField f : paths) {
      f.init(vs);
    }

    DBObject result = kettleRowToMongo(paths, rmi, row, vs,
        MongoDbOutputData.MongoTopLevel.RECORD);

    assertEquals(result.toString(),
        "{ \"field1\" : \"value1\" , \"nestedDoc\" : { \"field2\" : 12}}");
  }

  @Test
  public void testTopLevelObjectStructureTwoLevelNested()
      throws KettleException {
    List<MongoDbOutputMeta.MongoField> paths = new ArrayList<MongoDbOutputMeta.MongoField>();

    MongoDbOutputMeta.MongoField mf = new MongoDbOutputMeta.MongoField();
    mf.m_incomingFieldName = "field1";
    mf.m_mongoDocPath = "nestedDoc.secondNested";
    mf.m_useIncomingFieldNameAsMongoFieldName = true;
    paths.add(mf);

    mf = new MongoDbOutputMeta.MongoField();
    mf.m_incomingFieldName = "field2";
    mf.m_mongoDocPath = "nestedDoc";
    mf.m_useIncomingFieldNameAsMongoFieldName = true;
    paths.add(mf);

    RowMetaInterface rmi = new RowMeta();
    ValueMetaInterface vm = new ValueMeta();
    vm.setName("field1");
    vm.setType(ValueMetaInterface.TYPE_STRING);
    rmi.addValueMeta(vm);
    vm = new ValueMeta();
    vm.setName("field2");
    vm.setType(ValueMetaInterface.TYPE_INTEGER);
    rmi.addValueMeta(vm);

    Object[] row = new Object[2];
    row[0] = "value1";
    row[1] = new Long(12);
    VariableSpace vs = new Variables();

    for (MongoDbOutputMeta.MongoField f : paths) {
      f.init(vs);
    }

    DBObject result = kettleRowToMongo(paths, rmi, row, vs,
        MongoDbOutputData.MongoTopLevel.RECORD);

    assertEquals(
        result.toString(),
        "{ \"nestedDoc\" : { \"secondNested\" : { \"field1\" : \"value1\"} , \"field2\" : 12}}");
  }

  @Test
  public void testModifierUpdateWithMultipleModifiersOfSameType()
      throws KettleException {
    List<MongoDbOutputMeta.MongoField> paths = new ArrayList<MongoDbOutputMeta.MongoField>();
    MongoDbOutputData data = new MongoDbOutputData();

    MongoDbOutputMeta.MongoField mf = new MongoDbOutputMeta.MongoField();
    mf.m_incomingFieldName = "field1";
    mf.m_mongoDocPath = "";
    mf.m_useIncomingFieldNameAsMongoFieldName = true;
    mf.m_modifierUpdateOperation = "$set";
    mf.m_modifierOperationApplyPolicy = "Insert&Update";
    paths.add(mf);

    mf = new MongoDbOutputMeta.MongoField();
    mf.m_incomingFieldName = "field2";
    mf.m_mongoDocPath = "nestedDoc";
    mf.m_useIncomingFieldNameAsMongoFieldName = true;
    mf.m_modifierUpdateOperation = "$set";
    mf.m_modifierOperationApplyPolicy = "Insert&Update";
    paths.add(mf);

    RowMetaInterface rm = new RowMeta();
    ValueMetaInterface vm = new ValueMeta("field1");
    vm.setType(ValueMetaInterface.TYPE_STRING);
    rm.addValueMeta(vm);
    vm = new ValueMeta("field2");
    vm.setType(ValueMetaInterface.TYPE_STRING);
    rm.addValueMeta(vm);

    VariableSpace vars = new Variables();
    Object[] dummyRow = new Object[] { "value1", "value2" };

    // test to see that having more than one path specify the $set operation
    // results
    // in an update object with two entries
    DBObject modifierUpdate = data.getModifierUpdateObject(paths, rm, dummyRow,
        vars, MongoDbOutputData.MongoTopLevel.RECORD);

    assertTrue(modifierUpdate != null);
    assertTrue(modifierUpdate.get("$set") != null);
    DBObject setOpp = (DBObject) modifierUpdate.get("$set");
    assertEquals(setOpp.keySet().size(), 2);
  }

  @Test
  public void testModifierSetComplexArrayGrouping() throws KettleException {
    List<MongoDbOutputMeta.MongoField> paths = new ArrayList<MongoDbOutputMeta.MongoField>();
    MongoDbOutputData data = new MongoDbOutputData();

    MongoDbOutputMeta.MongoField mf = new MongoDbOutputMeta.MongoField();
    mf.m_incomingFieldName = "field1";
    mf.m_mongoDocPath = "bob.fred[0].george";
    mf.m_useIncomingFieldNameAsMongoFieldName = true;
    mf.m_modifierUpdateOperation = "$set";
    mf.m_modifierOperationApplyPolicy = "Insert&Update";
    paths.add(mf);

    mf = new MongoDbOutputMeta.MongoField();
    mf.m_incomingFieldName = "field2";
    mf.m_mongoDocPath = "bob.fred[0].george";
    mf.m_useIncomingFieldNameAsMongoFieldName = true;
    mf.m_modifierUpdateOperation = "$set";
    mf.m_modifierOperationApplyPolicy = "Insert&Update";
    paths.add(mf);

    RowMetaInterface rm = new RowMeta();
    ValueMetaInterface vm = new ValueMeta("field1");
    vm.setType(ValueMetaInterface.TYPE_STRING);
    rm.addValueMeta(vm);
    vm = new ValueMeta("field2");
    vm.setType(ValueMetaInterface.TYPE_STRING);
    rm.addValueMeta(vm);

    VariableSpace vars = new Variables();
    Object[] dummyRow = new Object[] { "value1", "value2" };

    DBObject modifierUpdate = data.getModifierUpdateObject(paths, rm, dummyRow,
        vars, MongoDbOutputData.MongoTopLevel.RECORD);

    assertTrue(modifierUpdate != null);
    assertTrue(modifierUpdate.get("$set") != null);
    DBObject setOpp = (DBObject) modifierUpdate.get("$set");

    // in this case, we have the same path up to the array (bob.fred). The
    // remaining
    // terminal fields should be grouped into one record "george" as the first
    // entry in
    // the array - so there should be one entry for $set
    assertEquals(setOpp.keySet().size(), 1);

    // check the resulting update structure
    assertEquals(
        modifierUpdate.toString(),
        "{ \"$set\" : { \"bob.fred\" : [ { \"george\" : { \"field1\" : \"value1\" , \"field2\" : \"value2\"}}]}}");
  }

  @Test
  public void testModifierPushComplexObject() throws KettleException {
    List<MongoDbOutputMeta.MongoField> paths = new ArrayList<MongoDbOutputMeta.MongoField>();
    MongoDbOutputData data = new MongoDbOutputData();

    MongoDbOutputMeta.MongoField mf = new MongoDbOutputMeta.MongoField();
    mf.m_incomingFieldName = "field1";
    mf.m_mongoDocPath = "bob.fred[].george";
    mf.m_useIncomingFieldNameAsMongoFieldName = true;
    mf.m_modifierUpdateOperation = "$push";
    mf.m_modifierOperationApplyPolicy = "Insert&Update";
    paths.add(mf);

    mf = new MongoDbOutputMeta.MongoField();
    mf.m_incomingFieldName = "field2";
    mf.m_mongoDocPath = "bob.fred[].george";
    mf.m_useIncomingFieldNameAsMongoFieldName = true;
    mf.m_modifierUpdateOperation = "$push";
    mf.m_modifierOperationApplyPolicy = "Insert&Update";
    paths.add(mf);

    RowMetaInterface rm = new RowMeta();
    ValueMetaInterface vm = new ValueMeta("field1");
    vm.setType(ValueMetaInterface.TYPE_STRING);
    rm.addValueMeta(vm);
    vm = new ValueMeta("field2");
    vm.setType(ValueMetaInterface.TYPE_STRING);
    rm.addValueMeta(vm);

    VariableSpace vars = new Variables();
    Object[] dummyRow = new Object[] { "value1", "value2" };

    DBObject modifierUpdate = data.getModifierUpdateObject(paths, rm, dummyRow,
        vars, MongoDbOutputData.MongoTopLevel.RECORD);

    assertTrue(modifierUpdate != null);
    assertTrue(modifierUpdate.get("$push") != null);
    DBObject setOpp = (DBObject) modifierUpdate.get("$push");

    // in this case, we have the same path up to the array (bob.fred). The
    // remaining
    // terminal fields should be grouped into one record "george" for $push to
    // append
    // to the end of the array
    assertEquals(setOpp.keySet().size(), 1);

    assertEquals(
        modifierUpdate.toString(),
        "{ \"$push\" : { \"bob.fred\" : { \"george\" : { \"field1\" : \"value1\" , \"field2\" : \"value2\"}}}}");
  }
}
