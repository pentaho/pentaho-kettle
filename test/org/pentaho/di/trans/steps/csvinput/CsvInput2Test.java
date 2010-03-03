package org.pentaho.di.trans.steps.csvinput;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleValueException;
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
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

/**
 * Regression test case for PDI JIRA-1317 (case 2): a csv input step with more
 * columns in certain rows than the number of columns defined in the step.
 * 
 * In the original problem (in v3.1-M2) this caused the filename column to
 * be in the wrong places.
 * 
 * @author Sven Boden
 */
public class CsvInput2Test extends TestCase {
	
	/**
	 * Write the file to be used as input (as a temporary file).
	 * 
	 * @return Absolute file name/path of the created file.
	 * @throws IOException UPON  
	 */
	public String writeInputFile() 
	    throws IOException  {
		
		String rcode = null;
		
		File tempFile =	File.createTempFile("PDI_tmp", ".tmp");		
		tempFile.deleteOnExit();
		
		rcode = tempFile.getAbsolutePath();

		FileWriter fout = new FileWriter(tempFile);
		fout.write("A;B;C;D;E\n");
		fout.write("1;b0;c0\n");
		fout.write("2;b1;c1;d1;e1\n");
		fout.write("3;b2;c2\n");
		
		fout.close();
		
		return rcode;
	}
	
	public RowMetaInterface createRowMetaInterface() {
		RowMetaInterface rm = new RowMeta();

		ValueMetaInterface[] valuesMeta = { new ValueMeta("filename",
				ValueMeta.TYPE_STRING), };

		for (int i = 0; i < valuesMeta.length; i++) {
			rm.addValueMeta(valuesMeta[i]);
		}

		return rm;
	}


	public List<RowMetaAndData> createData(String fileName) {
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

		RowMetaInterface rm = createRowMetaInterface();

		Object[] r1 = new Object[] { fileName };

		list.add(new RowMetaAndData(rm, r1));
		
		return list;
	}

	public RowMetaInterface createResultRowMetaInterface() {
		RowMetaInterface rm = new RowMeta();

		ValueMetaInterface[] valuesMeta = { 
				new ValueMeta("a",	  		ValueMeta.TYPE_INTEGER),
				new ValueMeta("b",		    ValueMeta.TYPE_STRING),
				new ValueMeta("c",		    ValueMeta.TYPE_STRING),
				new ValueMeta("filename",	ValueMeta.TYPE_STRING),
				};		
		
		for (int i = 0; i < valuesMeta.length; i++) {
			rm.addValueMeta(valuesMeta[i]);
		}

		return rm;
	}

	
	/**
	 * Create result data for test case 1.
	 * 
	 * @return list of metadata/data couples of how the result should look like.
	 */
	public List<RowMetaAndData> createResultData1() {
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

		RowMetaInterface rm = createResultRowMetaInterface();

		Object[] r1 = new Object[] { new Long(1L), "b0", "c0", "fileName" };
		Object[] r2 = new Object[] { new Long(2L), "b1", "c1", "fileName" };
		Object[] r3 = new Object[] { new Long(3L), "b2", "c2", "fileName" };

		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));

		return list;
	}

	/**
	 * Check the 2 lists comparing the rows in order. If they are not the same
	 * fail the test.
	 * 
	 * @param rows1
	 *            set 1 of rows to compare
	 * @param rows2
	 *            set 2 of rows to compare
	 * @param fileNameColumn
	 *            Number of the column containing the filename. This is only checked
	 *            for being non-null (some systems maybe canonize names differently 
	 *            than we input).                       
	 */
	public void checkRows(List<RowMetaAndData> rows1, List<RowMetaAndData> rows2, int fileNameColumn) {
		int idx = 1;
		if (rows1.size() != rows2.size()) {
			fail("Number of rows is not the same: " + rows1.size() + " and "
					+ rows2.size());
		}
		Iterator<RowMetaAndData> it1 = rows1.iterator();
		Iterator<RowMetaAndData> it2 = rows2.iterator();

		while (it1.hasNext() && it2.hasNext()) {
			RowMetaAndData rm1 = it1.next();
			RowMetaAndData rm2 = it2.next();

			Object[] r1 = rm1.getData();
			Object[] r2 = rm2.getData();

			if (rm1.size() != rm2.size()) {
				fail("row nr " + idx + " is not equal");
			}
			int[] fields = new int[r1.length];
			for (int ydx = 0; ydx < r1.length; ydx++) {
				fields[ydx] = ydx;
			}
			try {
				r1[fileNameColumn] = r2[fileNameColumn];
			    if (rm1.getRowMeta().compare(r1, r2, fields) != 0) {
				    fail("row nr " + idx + " is not equal");
			    }
			} catch (KettleValueException e) {
				fail("row nr " + idx + " is not equal");
			}

			idx++;
		}
	}

	/**
	 * Test case for Get XML Data step, very simple example.
	 * 
	 * @throws Exception
	 *             Upon any exception
	 */
	public void testCSVInput1() throws Exception {
        KettleEnvironment.init();

		//
		// Create a new transformation...
		//
		TransMeta transMeta = new TransMeta();
		transMeta.setName("csvinput1");

		PluginRegistry registry = PluginRegistry.getInstance();
		
		String fileName = writeInputFile();

		// 
		// create an injector step...
		//
		String injectorStepname = "injector step";
		InjectorMeta im = new InjectorMeta();

		// Set the information of the injector.
		String injectorPid = registry.getPluginId(StepPluginType.getInstance(), im);
		StepMeta injectorStep = new StepMeta(injectorPid, injectorStepname, im);
		transMeta.addStep(injectorStep);		
		
		// 
		// Create a Csv Input step
		//
		String csvInputName = "csv input step";
		CsvInputMeta cim = new CsvInputMeta();

		String csvInputPid = registry.getPluginId(StepPluginType.getInstance(), cim);
		StepMeta csvInputStep = new StepMeta(csvInputPid, csvInputName,
				cim);
		transMeta.addStep(csvInputStep);

		TextFileInputField[] fields = new TextFileInputField[3];

		for (int idx = 0; idx < fields.length; idx++) {
			fields[idx] = new TextFileInputField();
		}

		fields[0].setName("a");
		fields[0].setType(ValueMetaInterface.TYPE_INTEGER);
		fields[0].setFormat("");
		fields[0].setLength(-1);
		fields[0].setPrecision(-1);
		fields[0].setCurrencySymbol("");
		fields[0].setDecimalSymbol("");
		fields[0].setGroupSymbol("");
		fields[0].setTrimType(ValueMetaInterface.TRIM_TYPE_NONE);

		fields[1].setName("b");
		fields[1].setType(ValueMetaInterface.TYPE_STRING);
		fields[1].setFormat("");
		fields[1].setLength(-1);
		fields[1].setPrecision(-1);
		fields[1].setCurrencySymbol("");
		fields[1].setDecimalSymbol("");
		fields[1].setGroupSymbol("");
		fields[1].setTrimType(ValueMetaInterface.TRIM_TYPE_NONE);

		fields[2].setName("c");
		fields[2].setType(ValueMetaInterface.TYPE_STRING);
		fields[2].setFormat("");
		fields[2].setLength(-1);
		fields[2].setPrecision(-1);
		fields[2].setCurrencySymbol("");
		fields[2].setDecimalSymbol("");
		fields[2].setGroupSymbol("");
		fields[2].setTrimType(ValueMetaInterface.TRIM_TYPE_NONE);
		
		cim.setIncludingFilename(true);
		cim.setFilename("");
		cim.setFilenameField("filename");
		cim.setDelimiter(";");
		cim.setEnclosure("\"");
		cim.setBufferSize("50000");
		cim.setLazyConversionActive(false);
		cim.setHeaderPresent(true);
		cim.setAddResultFile(false);
		cim.setIncludingFilename(true);
		cim.setRowNumField("");
		cim.setRunningInParallel(false);
		cim.setInputFields(fields);
		
		TransHopMeta hi = new TransHopMeta(injectorStep, csvInputStep);
		transMeta.addTransHop(hi);

		// 
		// Create a dummy step 1
		//
		String dummyStepname1 = "dummy step 1";
		DummyTransMeta dm1 = new DummyTransMeta();

		String dummyPid1 = registry.getPluginId(StepPluginType.getInstance(), dm1);
		StepMeta dummyStep1 = new StepMeta(dummyPid1, dummyStepname1, dm1);
		transMeta.addStep(dummyStep1);

		TransHopMeta hi1 = new TransHopMeta(csvInputStep, dummyStep1);
		transMeta.addTransHop(hi1);

		// Now execute the transformation...
		Trans trans = new Trans(transMeta);

		trans.prepareExecution(null);

		StepInterface si = trans.getStepInterface(dummyStepname1, 0);
		RowStepCollector dummyRc1 = new RowStepCollector();
		si.addRowListener(dummyRc1);

		RowProducer rp = trans.addRowProducer(injectorStepname, 0);
		trans.startThreads();
		
		// add rows
		List<RowMetaAndData> inputList = createData(fileName);
		Iterator<RowMetaAndData> it = inputList.iterator();
		while (it.hasNext()) {
			RowMetaAndData rm = it.next();
			rp.putRow(rm.getRowMeta(), rm.getData());
		}
		rp.finished();
		
		trans.waitUntilFinished();

		// Compare the results
		List<RowMetaAndData> resultRows = dummyRc1.getRowsWritten();
		List<RowMetaAndData> goldenImageRows = createResultData1();

		checkRows(goldenImageRows, resultRows, 3);
	}
}