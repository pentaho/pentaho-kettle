package be.ibridge.kettle.trans.step.textfileinput;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import junit.framework.TestCase;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;

public class TextFileInputReplayTest extends TestCase {
	private LogWriter log;

	private TransMeta meta;

	private Trans trans;

	private String directory;

	protected void setUp() throws Exception {
		super.setUp();
		log = LogWriter.getInstance(Const.SPOON_LOG_FILE, false,
				LogWriter.LOG_LEVEL_ROWLEVEL);
		StepLoader.getInstance().read();
	}

	protected void tearDown() throws Exception {
		File[] files = new File(directory).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return !(name.endsWith("ktr") || name.endsWith("txt") || name
						.endsWith(".svn"));
			}
		});
		if (files != null)
			for (int i = 0; i < files.length; i++) {
				files[i].delete();
			}
		super.tearDown();
	}

	private void expectFiles(String directory, int expected) {
		String[] files = new File(directory).list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return !(name.endsWith(".svn"));
			}
		});
		if (files == null)
			assertEquals(0, expected);
		else
			assertEquals(expected, files.length);
	}

	private void expectContent(String filename, String expectedContent)
			throws IOException {
		FileInputStream stream = new FileInputStream(filename);
		try {
			StringBuffer buffer = new StringBuffer();
			int read = 0;
			while ((read = stream.read()) != -1) {
				buffer.append((char) read);
			}
			assertEquals(expectedContent, buffer.toString());
		} finally {
			stream.close();
		}
	}

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
		expectContent(directory + "input.txt.line", "5" + Const.CR + "18"
				+ Const.CR);
		expectContent(
				directory + "input.txt.dataerror",
				"I changed this; 00000009,87;2005/abba/26 21:01:23.000; 1234"
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

}
