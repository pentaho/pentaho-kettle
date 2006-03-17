package be.ibridge.kettle.trans.step.textfileinput;

import java.text.ParseException;
import java.util.Date;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.KettleStepUseCase;

public class TextFileInputReplayTest extends KettleStepUseCase {

	public void testInputOKIgnoreErrorsFalse() throws Exception {
		directory = "test/useCases/replay/textFileInputReplayGoodIgnoreFalse/";
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
		directory = "test/useCases/replay/textFileInputReplayGoodIgnoreTrue/";
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

	public void testInputErrorIgnoreErrorsFalse() throws Exception {
		directory = "test/useCases/replay/textFileInputReplayErrorIgnoreFalse/";
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
		directory = "test/useCases/replay/textFileInputReplayErrorIgnoreTrue/";
		expectFiles(directory, 2);
		meta = new TransMeta(directory + "transform.ktr");
		trans = new Trans(log, meta);
		boolean ok = trans.execute(null);
		assertTrue(ok);
		trans.waitUntilFinished();
		trans.endProcessing("end");
		assertEquals(0, trans.getErrors());
		expectFiles(directory, 3);
		expectContent(directory + "input.txt." + getDateFormatted() + ".line",
				"5" + Const.CR + "10" + Const.CR + "18" + Const.CR);
	}

	public void testInputErrorIgnoreErrorsTrueLineNrOnly() throws Exception {
		directory = "test/useCases/replay/textFileInputReplayErrorIgnoreTrueLineNrOnly/";
		expectFiles(directory, 2);
		meta = new TransMeta(directory + "transform.ktr");
		trans = new Trans(log, meta);
		boolean ok = trans.execute(null);
		assertTrue(ok);
		trans.waitUntilFinished();
		trans.endProcessing("end");
		assertEquals(0, trans.getErrors());
		expectFiles(directory, 3);
		expectContent(directory + "input.txt." + getDateFormatted() + ".line",
				"5" + Const.CR + "18" + Const.CR);
	}

	public void testInputErrorIgnoreErrorsTrueErrorOnly() throws Exception {
		directory = "test/useCases/replay/textFileInputReplayErrorIgnoreTrueErrorOnly/";
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
				directory + "input2.txt." + getDateFormatted() + ".error",
				TextFileErrorHandlerMissingFiles.THIS_FILE_DOES_NOT_EXIST
						+ Const.CR);
	}

	public void testReplay() throws Exception {
		directory = "test/useCases/replay/textFileInputDoReplay/";
		expectFiles(directory, 3);
		meta = new TransMeta(directory + "transform.ktr");
		trans = new Trans(log, meta);
		trans.setReplayDate(getReplayDate());
		boolean ok = trans.execute(null);
		assertTrue(ok);
		trans.waitUntilFinished();
		trans.endProcessing("end");
		assertEquals(0, trans.getErrors());
		expectFiles(directory, 4);
		expectContent(directory + "input.txt." + getDateFormatted() + ".line",
				"14" + Const.CR + "18" + Const.CR);
	}

	public void testReplayMultipleFiles() throws Exception {
		directory = "test/useCases/replay/textFileInputDoReplayMultipleFiles/";
		expectFiles(directory, 7);
		meta = new TransMeta(directory + "transform.ktr");
		trans = new Trans(log, meta);
		trans.setReplayDate(getReplayDate());
		boolean ok = trans.execute(null);
		assertTrue(ok);
		trans.waitUntilFinished();
		trans.endProcessing("end");
		assertEquals(0, trans.getErrors());
		expectFiles(directory, 9);
		expectContent(directory + "input1.txt." + getDateFormatted() + ".line",
				"7" + Const.CR);
		expectContent(directory + "input3.txt." + getDateFormatted() + ".line",
				"18" + Const.CR + "19" + Const.CR);
	}

	private Date getReplayDate() throws ParseException {
		return AbstractTextFileErrorHandler.createDateFormat().parse(
				REPLAY_DATE);
	}

	private String getDateFormatted() {
		return AbstractTextFileErrorHandler.createDateFormat().format(
				trans.getCurrentDate());
	}

	public String getFileExtension() {
		return "txt";
	}
}
