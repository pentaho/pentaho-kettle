package be.ibridge.kettle.trans.step.textfileinput;

import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.KettleStepUseCase;

// WARNING: DOESN'T WORK YET, IT'S JUST A PLACEHOLDER AT THE MOMENT.
// matt
public class TextFileInputTest extends KettleStepUseCase {

	public void testInputOKIgnoreErrorsFalse() throws Exception {
		directory = "test/useCases/normal/textFileInputPaged/";
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

	public String getFileExtension() {
		return "txt";
	}
}
