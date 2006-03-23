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

	public void testInputErrorRequiredNoFiles() throws Exception {
		directory = "test/useCases/replay/excelInputErrorRequiredNoFiles/";
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

	public String getFileExtension() {
		return "xls";
	}
}
