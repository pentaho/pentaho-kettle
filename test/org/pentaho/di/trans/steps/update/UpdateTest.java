package org.pentaho.di.trans.steps.update;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.injector.InjectorMeta;

public class UpdateTest extends TestCase {

	public static final String[] databasesXML = { "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<connection>" +
			"<name>db</name>" +
			"<server>127.0.0.1</server>" +
			"<type>H2</type>" +
			"<access>Native</access>" +
			"<database>mem:db</database>" +
			"<port></port>" +
			"<username>sa</username>" +
			"<password></password>" +
			"</connection>",
			};

	public static final String TARGET_TABLE = "update_step_test_case_table";

	private static String insertStatement[] = {
				// New rows for the source
			"INSERT INTO " + TARGET_TABLE + "(ID, CODE, VALUE, ROW_ORDER) " +
					"VALUES (NULL, NULL, 'null_id_code', 1)",

			"INSERT INTO " + TARGET_TABLE + "(ID, CODE, VALUE, ROW_ORDER) " +
					"VALUES (NULL, 1, 'null_id', 2)",

			"INSERT INTO " + TARGET_TABLE + "(ID, CODE, VALUE, ROW_ORDER) " +
					"VALUES (1, NULL, 'null_code', 3)",

			"INSERT INTO " + TARGET_TABLE + "(ID, CODE, VALUE, ROW_ORDER) " +
					"VALUES (2, 2, 'non_null_keys', 4)",

	};

	// this points to the transformation
	Trans trans;

	// this points to the update step being tested
	public UpdateMeta upd;

	// these are used to write and read rows in the test transformation
	public RowStepCollector rc;
	public RowProducer rp;

	// the database used for the transformation run
	public Database db;

	// returns the structure of the target table
	public RowMetaInterface getTargetTableRowMeta() {
		RowMetaInterface rm = new RowMeta();

		ValueMetaInterface valuesMeta[] = {
				new ValueMeta("ID", ValueMeta.TYPE_INTEGER, 8, 0),
				new ValueMeta("CODE", ValueMeta.TYPE_INTEGER, 8, 0),
				new ValueMeta("VALUE", ValueMeta.TYPE_STRING, 255, 0),
				new ValueMeta("ROW_ORDER", ValueMeta.TYPE_INTEGER, 8, 0),
				};

		for (int i = 0; i < valuesMeta.length; i++) {
			rm.addValueMeta(valuesMeta[i]);
		}
		return rm;
	}

	// adds lookup key line definition to the update step
	// input is in format {key, condition, stream, stream2}
	public void addLookup(String[] def) {

		// make sure to initialize the step
		if (upd.getKeyLookup() == null) {
			upd.setKeyLookup(new String[0]);
			upd.setKeyCondition(new String[0]);
			upd.setKeyStream(new String[0]);
			upd.setKeyStream2(new String[0]);
		}

		int newLength = upd.getKeyLookup().length + 1;

		ArrayList<String> newKeyLookup = new ArrayList<String>(newLength);
		newKeyLookup.addAll(Arrays.asList(upd.getKeyLookup()));
		newKeyLookup.add(def[0]);
		upd.setKeyLookup(newKeyLookup.toArray(new String[0]));

		ArrayList<String> newKeyCondition = new ArrayList<String>(newLength);
		newKeyCondition.addAll(Arrays.asList(upd.getKeyCondition()));
		newKeyCondition.add(def[1]);
		upd.setKeyCondition(newKeyCondition.toArray(new String[0]));

		ArrayList<String> newKeyStream = new ArrayList<String>(newLength);
		newKeyStream.addAll(Arrays.asList(upd.getKeyStream()));
		newKeyStream.add(def[2]);
		upd.setKeyStream(newKeyStream.toArray(new String[0]));

		ArrayList<String> newKeyStream2 = new ArrayList<String>(newLength);
		newKeyStream2.addAll(Arrays.asList(upd.getKeyStream2()));
		newKeyStream2.add(def[3]);
		upd.setKeyStream2(newKeyStream2.toArray(new String[0]));

	}

	@Before
	public void setUp() throws Exception {

		KettleEnvironment.init();

		/* SET UP TRANSFORMATION */

		// Create a new transformation...
		TransMeta transMeta = new TransMeta();
		transMeta.setName("update test");

		// Add the database connections
		for (int i = 0; i < databasesXML.length; i++) {
			DatabaseMeta databaseMeta = new DatabaseMeta(databasesXML[i]);
			transMeta.addDatabase(databaseMeta);
		}

		DatabaseMeta dbInfo = transMeta.findDatabase("db");

		/* SET UP DATABASE */
		// Create target table
		db = new Database(transMeta, dbInfo);
		db.connect();

		String source = db.getCreateTableStatement(TARGET_TABLE, getTargetTableRowMeta(), null, false, null, true);
		db.execStatement(source);

		// populate target table
		for (String sql : insertStatement) {
			db.execStatement(sql);
		}

		/* SET UP TRANSFORMATION STEPS */

		PluginRegistry registry = PluginRegistry.getInstance();

		// create an injector step...
		String injectorStepName = "injector step";
		InjectorMeta im = new InjectorMeta();

		// Set the information of the injector.
		String injectorPid = registry.getPluginId(StepPluginType.class, im);
		StepMeta injectorStep = new StepMeta(injectorPid, injectorStepName, (StepMetaInterface) im);
		transMeta.addStep(injectorStep);

		// create the update step...
		String updateStepName = "update [" + TARGET_TABLE + "]";
		upd = new UpdateMeta();
		upd.setDatabaseMeta(transMeta.findDatabase("db"));
		upd.setTableName(TARGET_TABLE);
		upd.setUpdateLookup(new String[] { "VALUE" });
		upd.setUpdateStream(new String[] { "VALUE" });
		upd.setErrorIgnored(true);

		String fromid = registry.getPluginId(StepPluginType.class, upd);
		StepMeta updateStep = new StepMeta(fromid, updateStepName, (StepMetaInterface) upd);
		updateStep.setDescription("update data in table [" + TARGET_TABLE + "] on database [" + dbInfo + "]");
		transMeta.addStep(updateStep);

		TransHopMeta hi = new TransHopMeta(injectorStep, updateStep);
		transMeta.addTransHop(hi);

		/* PREPARE TRANSFORMATION EXECUTION */

		trans = new Trans(transMeta);
		trans.prepareExecution(null);

		StepInterface si = trans.getStepInterface(updateStepName, 0);
		rc = new RowStepCollector();
		si.addRowListener(rc);

		rp = trans.addRowProducer(injectorStepName, 0);

	}

	@After
	public void tearDown() throws Exception {

		/* DROP THE TEST TABLE */

		if (db != null) {
			db.execStatement("DROP TABLE " + TARGET_TABLE + ";");
			db.disconnect();
		}

		db = null;
		upd = null;
		trans = null;
		rc = null;
		rp = null;
	}

	public List<RowMetaAndData> createMatchingDataRows() {
		RowMetaInterface rm = getTargetTableRowMeta();
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

		list.add(new RowMetaAndData(rm, new Object[] { null, null, "updated" }));
		list.add(new RowMetaAndData(rm, new Object[] { null, 1L, "updated" }));
		list.add(new RowMetaAndData(rm, new Object[] { 1L, null, "updated" }));
		list.add(new RowMetaAndData(rm, new Object[] { 2L, 2L, "updated" }));

		return list;
	}

	// this method pumps rows to the update step;
	public void pumpMatchingRows() throws Exception {
		pumpRows(createMatchingDataRows());
	}
	
	public void pumpRows(List<RowMetaAndData> inputList) throws Exception {
		
		trans.startThreads();

		// add rows
		for (RowMetaAndData rm : inputList) {
			rp.putRow(rm.getRowMeta(), rm.getData());
		}
		rp.finished();

		trans.waitUntilFinished();
		if (trans.getErrors() > 0){
			fail("test transformation failed, check logs!");
		}		
		
	}

	public String[] getDbRows() throws Exception {

		ResultSet rs = db.openQuery("SELECT VALUE FROM " + TARGET_TABLE + " ORDER BY ROW_ORDER ASC;");

		ArrayList<String> rows = new ArrayList<String>();

		while (rs.next()) {
			rows.add(rs.getString("VALUE"));
		}

		return rows.toArray(new String[0]);

	}

	public void testUpdateEquals() throws Exception {
		
		addLookup(new String[] { "ID", "=", "ID", "" });
		pumpMatchingRows();
		String[] rows = getDbRows();
		
		// now the 1,null and 2,2 record should have been updated 
		String[] expected = {"null_id_code", "null_id", "updated", "updated"};
		assertArrayEquals("Unexpected changes by update step", expected, rows);

	}
	
	public void testUpdateEqualsSkip() throws Exception{
		upd.setSkipLookup(true);
		testUpdateEquals();
	}
	
	public void testUpdateEqualsTwoKeys() throws Exception {
		
		addLookup(new String[] { "ID", "=", "ID", "" });
		addLookup(new String[] { "CODE", "=", "CODE", "" });
		pumpMatchingRows();
		String[] rows = getDbRows();
		
		// now the 2,2 record should have been updated 
		String[] expected = {"null_id_code", "null_id", "null_code", "updated"};
		assertArrayEquals("Unexpected changes by update step", expected, rows);

	}	
	
	public void testUpdateEqualsTwoKeysSkip() throws Exception{
		upd.setSkipLookup(true);
		testUpdateEqualsTwoKeys();
	}	

	public void testUpdateEqualsSupportsNull() throws Exception {

		addLookup(new String[] { "ID", "= ~NULL", "ID", "" });
		pumpMatchingRows();
		String[] rows = getDbRows();
		
		// now all records should have been updated 
		String[] expected = {"updated", "updated", "updated", "updated"};
		assertArrayEquals("Unexpected changes by update step", expected, rows);		
		
	}
	
	
	public void testUpdateEqualsSupportsNullSkip() throws Exception{
		upd.setSkipLookup(true);
		testUpdateEqualsSupportsNull();
	}		
	
	public void testUpdateEqualsSupportsNullTwoKeys() throws Exception {

		addLookup(new String[] { "ID", "= ~NULL", "ID", "" });
		addLookup(new String[] { "CODE", "= ~NULL", "CODE", "" });
		
		pumpMatchingRows();
		String[] rows = getDbRows();
		
		// now all records should have been updated 
		String[] expected = {"updated", "updated", "updated", "updated"};
		assertArrayEquals("Unexpected changes by update step", expected, rows);				
		
	}	
	
	public void testUpdateEqualsSupportsNullTwoKeysSkip() throws Exception{
		upd.setSkipLookup(true);
		testUpdateEqualsSupportsNullTwoKeys();
	}		
	
	public void testUpdateEqualsSupportsNullTwoKeysMixed() throws Exception {

		addLookup(new String[] { "ID", "= ~NULL", "ID", "" });
		addLookup(new String[] { "CODE", "=", "CODE", "" });
		
		pumpMatchingRows();
		String[] rows = getDbRows();
		
		// now [null,1], [2,2] records should have been updated 
		String[] expected = {"null_id_code", "updated", "null_code", "updated"};
		assertArrayEquals("Unexpected changes by update step", expected, rows);				
		
	}		

	public void testUpdateEqualsSupportsNullTwoKeysMixedSkip() throws Exception{
		upd.setSkipLookup(true);
		testUpdateEqualsSupportsNullTwoKeysMixed();
	}		
	
	public void testUpdateIsNull() throws Exception {

		addLookup(new String[] { "CODE", "IS NULL", "", "" });
		
		pumpMatchingRows();
		String[] rows = getDbRows();
		
		// now [null, null], [1,null] records should have been updated 
		String[] expected = {"updated", "null_id", "updated", "non_null_keys"};
		assertArrayEquals("Unexpected changes by update step", expected, rows);				
		
	}
	
	public void testUpdateIsNullSkip() throws Exception{
		upd.setSkipLookup(true);
		testUpdateIsNull();
	}		

	public void testUpdateIsNotNull() throws Exception {

		addLookup(new String[] { "CODE", "IS NOT NULL", "", "" });
		
		pumpMatchingRows();
		String[] rows = getDbRows();
		
		// now [null, 1], [2,2] records should have been updated 
		String[] expected = {"null_id_code", "updated", "null_code", "updated"};
		assertArrayEquals("Unexpected changes by update step", expected, rows);				
		
	}
	
	public void testUpdateIsNotNullSkip() throws Exception{
		upd.setSkipLookup(true);
		testUpdateIsNotNull();
	}		

	public void testUpdateBetween() throws Exception {
		addLookup(new String[] { "ID", "BETWEEN", "ID", "CODE" });
		
		pumpMatchingRows();
		String[] rows = getDbRows();
		
		// now [2,2] record should have been updated 
		String[] expected = {"null_id_code", "null_id", "null_code", "updated"};
		assertArrayEquals("Unexpected changes by update step", expected, rows);				

	}

	public void testUpdateBetweenSkip() throws Exception{
		upd.setSkipLookup(true);
		testUpdateBetween();
	}			

	public void testUpdateEqualsSupportsNullTwoKeysMixed2() throws Exception {

		addLookup(new String[] { "ID", "=", "ID", "" });
		addLookup(new String[] { "CODE", "= ~NULL", "CODE", "" });
		
		pumpMatchingRows();
		String[] rows = getDbRows();
		
		// now [1,null], [2,2] records should have been updated 
		String[] expected = {"null_id_code", "null_id", "updated", "updated"};
		assertArrayEquals("Unexpected changes by update step", expected, rows);				
		
	}	

	public void testUpdateEqualsSupportsNullTwoKeysMixed2Skip() throws Exception{
		upd.setSkipLookup(true);
		testUpdateEqualsSupportsNullTwoKeysMixed2();
	}		
	
}
