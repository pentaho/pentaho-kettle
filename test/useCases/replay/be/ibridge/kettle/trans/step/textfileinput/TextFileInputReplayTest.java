package be.ibridge.kettle.trans.step.textfileinput;

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
		expectFiles(directory, 4);
		expectContent(directory + "input.txt.line", "5" + Const.CR + "10"
				+ Const.CR + "18" + Const.CR);
		expectContent(
				directory + "input.txt.dataerror",
				"I changed this; 00000009,87;2005/abba/26 21:01:23.000; 1234"
						+ Const.CR
						+ "This is a text; 00001234,50;2005/95/26 12:34:56.000; 9876"
						+ Const.CR
						+ "This is a text; 00001234,50;2005/05/26 12:34:56.000; test"
						+ Const.CR);
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
		expectContent(directory + "input.txt.line", "5" + Const.CR + "18"
				+ Const.CR);
	}

	public void testInputErrorIgnoreErrorsTrueDataErrorOnly() throws Exception {
		directory = "test/useCases/replay/textFileInputReplayErrorIgnoreTrueDataErrorOnly/";
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
				directory + "input.txt.dataerror",
				"I changed this; 00000009,87;2005/abba/26 21:01:23.000; 1234"
						+ Const.CR
						+ "This is a text; 00001234,50;2005/05/26 12:34:56.000; test"
						+ Const.CR);
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
				directory + "input.txt.dataerror",
				"I changed this; 00000009,87;2005/abba/26 21:01:23.000; 1234"
						+ Const.CR
						+ "This is a text; 00001234,50;2005/05/26 12:34:56.000; test"
						+ Const.CR);
	}

	public String getFileExtension() {
		return "txt";
	}

}
