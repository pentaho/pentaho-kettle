package be.ibridge.kettle.trans.step.excelinput;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.KettleStepUseCase;
import be.ibridge.kettle.trans.step.errorhandling.FileErrorHandlerMissingFiles;

public class ExcelInputReplayTest extends KettleStepUseCase {

	public void testInputOKIgnoreErrorsTrueStrictFalse() throws Exception {
		directory = "test/useCases/replay/excelInputReplayGoodIgnoreTrueStrictFalse/";
		expectFiles(directory, 2);
		meta = new TransMeta(directory + "transform.ktr");
		trans = new Trans(log, meta);
		boolean ok = trans.execute(null);
		assertTrue(ok);
		trans.waitUntilFinished();
		trans.endProcessing("end");
		assertEquals(0, trans.getErrors());
		expectFiles(directory, 2);
	}

	public void testInputOKIgnoreErrorsTrue() throws Exception {
		directory = "test/useCases/replay/excelInputReplayGoodIgnoreTrue/";
		expectFiles(directory, 2);
		meta = new TransMeta(directory + "transform.ktr");
		trans = new Trans(log, meta);
		boolean ok = trans.execute(null);
		assertTrue(ok);
		trans.waitUntilFinished();
		trans.endProcessing("end");
		assertEquals(0, trans.getErrors());
		expectFiles(directory, 2);
	}

	public void testInputOKIgnoreErrorsFalse() throws Exception {
		directory = "test/useCases/replay/excelInputReplayGoodIgnoreFalse/";
		expectFiles(directory, 2);
		meta = new TransMeta(directory + "transform.ktr");
		trans = new Trans(log, meta);
		boolean ok = trans.execute(null);
		assertTrue(ok);
		trans.waitUntilFinished();
		trans.endProcessing("end");
		assertEquals(0, trans.getErrors());
		expectFiles(directory, 2);
	}

	public void testInputErrorIgnoreErrorRequiredNoFiles() throws Exception {
		directory = "test/useCases/replay/excelInputErrorIgnoreErrorRequiredNoFiles/";
		expectFiles(directory, 1);
		meta = new TransMeta(directory + "transform.ktr");
		trans = new Trans(log, meta);
		boolean ok = trans.execute(null);
		assertTrue(ok);
		trans.waitUntilFinished();
		trans.endProcessing("end");
		assertEquals(0, trans.getErrors());
		expectFiles(directory, 2);
		expectContent(directory + "input.xls." + getDateFormatted() + ".error",
				FileErrorHandlerMissingFiles.THIS_FILE_DOES_NOT_EXIST
						+ Const.CR);
	}

	public void testInputErrorIgnoreErrorsFalse() throws Exception {
		directory = "test/useCases/replay/excelInputReplayErrorIgnoreFalse/";
		expectFiles(directory, 2);
		meta = new TransMeta(directory + "transform.ktr");
		trans = new Trans(log, meta);
		boolean ok = trans.execute(null);
		assertTrue(ok);
		trans.waitUntilFinished();
		trans.endProcessing("end");
		assertEquals(1, trans.getErrors());
		expectFiles(directory, 2);
	}

	public void testInputErrorIgnoreErrorsTrue() throws Exception {
		directory = "test/useCases/replay/excelInputReplayErrorIgnoreTrue/";
		expectFiles(directory, 2);
		meta = new TransMeta(directory + "transform.ktr");
		trans = new Trans(log, meta);
		boolean ok = trans.execute(null);
		assertTrue(ok);
		trans.waitUntilFinished();
		trans.endProcessing("end");
		assertEquals(0, trans.getErrors());
		expectFiles(directory, 4);
		expectContent(directory + "input.xls_Sheet1." + getDateFormatted()
				+ ".line", "2" + Const.CR + "19" + Const.CR);
		expectContent(directory + "input.xls_Sheet2." + getDateFormatted()
				+ ".line", "10" + Const.CR + "17" + Const.CR);
	}

	public void testInputErrorIgnoreErrorsTrueRowNrOnly() throws Exception {
		directory = "test/useCases/replay/excelInputReplayErrorIgnoreTrueRowNrOnly/";
		expectFiles(directory, 2);
		meta = new TransMeta(directory + "transform.ktr");
		trans = new Trans(log, meta);
		boolean ok = trans.execute(null);
		assertTrue(ok);
		trans.waitUntilFinished();
		trans.endProcessing("end");
		assertEquals(0, trans.getErrors());
		expectFiles(directory, 4);
		expectContent(directory + "input.xls_Sheet1." + getDateFormatted()
				+ ".line", "2" + Const.CR + "19" + Const.CR);
		expectContent(directory + "input.xls_Sheet2." + getDateFormatted()
				+ ".line", "10" + Const.CR + "17" + Const.CR);
	}

	public void testInputErrorIgnoreErrorsTrueErrorOnly() throws Exception {
		directory = "test/useCases/replay/excelInputReplayErrorIgnoreTrueErrorOnly/";
		expectFiles(directory, 2);
		meta = new TransMeta(directory + "transform.ktr");
		trans = new Trans(log, meta);
		boolean ok = trans.execute(null);
		assertTrue(ok);
		trans.waitUntilFinished();
		trans.endProcessing("end");
		assertEquals(0, trans.getErrors());
		expectFiles(directory, 3);
		expectContent(
				directory + "input2.xls." + getDateFormatted() + ".error",
				FileErrorHandlerMissingFiles.THIS_FILE_DOES_NOT_EXIST
						+ Const.CR);
	}

	public void testInputOutputSkipErrorLineFalse() throws Exception {
		directory = "test/useCases/replay/excelInputOutputSkipErrorLineFalse/";
		expectFiles(directory, 2);
		meta = new TransMeta(directory + "transform.ktr");
		trans = new Trans(log, meta);
		boolean ok = trans.execute(null);
		assertTrue(ok);
		trans.waitUntilFinished();
		trans.endProcessing("end");
		assertEquals(0, trans.getErrors());
		expectFiles(directory, 4);
		expectContent(directory + "input.xls_Sheet1." + getDateFormatted()
				+ ".line", "3" + Const.CR + "6" + Const.CR);
		expectContent(directory + "result.out", "name;age" + Const.CR
				+ "john; 23" + Const.CR + "dennis; 0" + Const.CR + "ward; 15"
				+ Const.CR + "john; 24" + Const.CR + "roel; 0" + Const.CR
				+ "ward; 16" + Const.CR + "john; 25" + Const.CR);
	}

	public void testInputOutputSkipErrorLineTrue() throws Exception {
		directory = "test/useCases/replay/excelInputOutputSkipErrorLineTrue/";
		expectFiles(directory, 2);
		meta = new TransMeta(directory + "transform.ktr");
		trans = new Trans(log, meta);
		boolean ok = trans.execute(null);
		assertTrue(ok);
		trans.waitUntilFinished();
		trans.endProcessing("end");
		assertEquals(0, trans.getErrors());
		expectFiles(directory, 4);
		expectContent(directory + "input.xls_Sheet1." + getDateFormatted()
				+ ".line", "3" + Const.CR + "6" + Const.CR);
		expectContent(directory + "result.out", "name;age" + Const.CR
				+ "john; 23" + Const.CR + "ward; 15" + Const.CR + "john; 24"
				+ Const.CR + "ward; 16" + Const.CR + "john; 25" + Const.CR);
	}

	public void testReplayErrorOnly() throws Exception {
		directory = "test/useCases/replay/excelInputDoReplayErrorOnly/";
		expectFiles(directory, 6);
		meta = new TransMeta(directory + "transform.ktr");
		trans = new Trans(log, meta);
		trans.setReplayDate(getReplayDate());
		boolean ok = trans.execute(null);
		assertTrue(ok);
		trans.waitUntilFinished();
		trans.endProcessing("end");
		assertEquals(0, trans.getErrors());
		expectFiles(directory, 9);
		expectContent(directory + "input3.xls_Sheet1." + getDateFormatted()
				+ ".line", "6" + Const.CR + "10" + Const.CR + "14" + Const.CR
				+ "17" + Const.CR + "18" + Const.CR);
		expectContent(directory + "input3.xls_Sheet2." + getDateFormatted()
				+ ".line", "12" + Const.CR);
		expectContent(directory + "input3.xls_Sheet3." + getDateFormatted()
				+ ".line", "6" + Const.CR + "10" + Const.CR + "14" + Const.CR
				+ "17" + Const.CR + "18" + Const.CR);
	}
	
	public void testReplayErrorOnlyWildcards() throws Exception {
		directory = "test/useCases/replay/excelInputDoReplayErrorOnlyWildcards/";
		expectFiles(directory, 6);
		meta = new TransMeta(directory + "transform.ktr");
		trans = new Trans(log, meta);
		trans.setReplayDate(getReplayDate());
		boolean ok = trans.execute(null);
		assertTrue(ok);
		trans.waitUntilFinished();
		trans.endProcessing("end");
		assertEquals(0, trans.getErrors());
		expectFiles(directory, 9);
		expectContent(directory + "input3.xls_Sheet1." + getDateFormatted()
				+ ".line", "6" + Const.CR + "10" + Const.CR + "14" + Const.CR
				+ "17" + Const.CR + "18" + Const.CR);
		expectContent(directory + "input3.xls_Sheet2." + getDateFormatted()
				+ ".line", "12" + Const.CR);
		expectContent(directory + "input3.xls_Sheet3." + getDateFormatted()
				+ ".line", "6" + Const.CR + "10" + Const.CR + "14" + Const.CR
				+ "17" + Const.CR + "18" + Const.CR);
	}
	
	public void testReplayErrorMultipleFiles() throws Exception {
		directory = "test/useCases/replay/excelInputDoReplayMultipleFiles/";
		expectFiles(directory, 11);
		meta = new TransMeta(directory + "transform.ktr");
		trans = new Trans(log, meta);
		trans.setReplayDate(getReplayDate());
		boolean ok = trans.execute(null);
		assertTrue(ok);
		trans.waitUntilFinished();
		trans.endProcessing("end");
		assertEquals(0, trans.getErrors());
		expectFiles(directory, 14);
		expectContent(directory + "input1.xls_sh2." + getDateFormatted()
				+ ".line", "18" + Const.CR);
		expectContent(directory + "input3.xls_sh1." + getDateFormatted()
				+ ".line", "7" + Const.CR+"8" + Const.CR);
		expectContent(directory + "input3.xls_sh3." + getDateFormatted()
				+ ".line", "14" + Const.CR+"18" + Const.CR);
	}

	public String getFileExtension() {
		return "xls";
	}
}
