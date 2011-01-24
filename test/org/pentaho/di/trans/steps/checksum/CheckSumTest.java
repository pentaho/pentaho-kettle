package org.pentaho.di.trans.steps.checksum;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;

public class CheckSumTest extends TestCase {

  private Trans buildHexadecimalChecksumTrans(int checkSumType, boolean compatibilityMode) throws Exception {
    KettleEnvironment.init();

    // Create a new transformation...
    TransMeta transMeta = new TransMeta();
    transMeta.setName(getClass().getName());

    // Create a CheckSum Step
    String checkSumStepname = "CheckSum"; //$NON-NLS-1$
    CheckSumMeta meta = new CheckSumMeta();

    // Set the compatibility mode and other required fields
    meta.setCompatibilityMode(compatibilityMode);
    meta.setResultFieldName("hex"); //$NON-NLS-1$
    meta.setCheckSumType(checkSumType);
    meta.setResultType(CheckSumMeta.result_TYPE_HEXADECIMAL);
    meta.setFieldName(new String[] { "test" }); //$NON-NLS-1$

    String checkSumPluginPid = PluginRegistry.getInstance().getPluginId(StepPluginType.class, meta);
    StepMeta checkSumStep = new StepMeta(checkSumPluginPid, checkSumStepname, meta);
    transMeta.addStep(checkSumStep);

    // Create a Dummy step
    String dummyStepname = "Output"; //$NON-NLS-1$
    DummyTransMeta dummyMeta = new DummyTransMeta();
    String dummyStepPid = PluginRegistry.getInstance().getPluginId(StepPluginType.class, dummyMeta);
    StepMeta dummyStep = new StepMeta(dummyStepPid, dummyStepname, dummyMeta);
    transMeta.addStep(dummyStep);

    // Create a hop from CheckSum to Output
    TransHopMeta hop = new TransHopMeta(checkSumStep, dummyStep);
    transMeta.addTransHop(hop);

    return new Trans(transMeta);
  }

  private RowMeta createStringRowMeta() throws Exception {
    RowMeta rowMeta = new RowMeta();
    ValueMeta meta = new ValueMeta();
    meta.setType(ValueMeta.TYPE_STRING);
    meta.setName("test"); //$NON-NLS-1$
    rowMeta.addValueMeta(meta);
    return rowMeta;
  }

  private class MockRowListener extends RowAdapter {
    private List<Object[]> written;

    private List<Object[]> read;

    private List<Object[]> error;

    public MockRowListener() {
      written = new ArrayList<Object[]>();
      read = new ArrayList<Object[]>();
      error = new ArrayList<Object[]>();
    }

    public List<Object[]> getWritten() {
      return written;
    }

    public List<Object[]> getRead() {
      return read;
    }

    public List<Object[]> getError() {
      return error;
    }

    @Override
    public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
      written.add(row);
    }

    @Override
    public void rowReadEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
      read.add(row);
    }

    @Override
    public void errorRowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
      error.add(row);
    }
  }

  /**
   * Create, execute, and return the row listener attached to the output step with complete results from the
   * execution.
   * 
   * @param checkSumType Type of checksum to use (the array index of {@link CheckSumMeta#checksumtypeCodes})
   * @param compatibilityMode Use compatibility mode for CheckSum
   * @param input String to calculate checksum for
   * @return RowListener with results.
   */
  private MockRowListener executeHexTest(int checkSumType, boolean compatibilityMode, String input) throws Exception {
    Trans trans = buildHexadecimalChecksumTrans(checkSumType, compatibilityMode);

    trans.prepareExecution(null);

    StepInterface output = trans.getRunThread("Output", 0); //$NON-NLS-1$
    MockRowListener listener = new MockRowListener();
    output.addRowListener(listener);

    RowProducer rp = trans.addRowProducer("CheckSum", 0); //$NON-NLS-1$
    RowMeta inputRowMeta = createStringRowMeta();
    ((BaseStep) trans.getRunThread("CheckSum", 0)).setInputRowMeta(inputRowMeta); //$NON-NLS-1$

    trans.startThreads();

    rp.putRow(inputRowMeta, new Object[] { input }); //$NON-NLS-1$
    rp.finished();

    trans.waitUntilFinished();
    trans.stopAll();
    trans.cleanup();
    return listener;
  }

  public void testHexOutput_md5() throws Exception {
    MockRowListener results = executeHexTest(2, false, "xyz"); //$NON-NLS-1$
    assertEquals(1, results.getWritten().size());
    assertEquals("d16fb36f0911f878998c136191af705e", results.getWritten().get(0)[1]); //$NON-NLS-1$
  }
  
  public void testHexOutput_md5_compatibilityMode() throws Exception {
    MockRowListener results = executeHexTest(2, true, "xyz"); //$NON-NLS-1$
    assertEquals(1, results.getWritten().size());
    assertEquals("D16FB36F0911F878998C136191AF705E", results.getWritten().get(0)[1]); //$NON-NLS-1$
  }

  public void testHexOutput_sha1()  throws Exception {
    MockRowListener results = executeHexTest(3, false, "xyz"); //$NON-NLS-1$
    assertEquals(1, results.getWritten().size());
    assertEquals("66b27417d37e024c46526c2f6d358a754fc552f3", results.getWritten().get(0)[1]); //$NON-NLS-1$
  }
  
  public void testHexOutput_sha1_compatibilityMode()  throws Exception {
    MockRowListener results = executeHexTest(3, true, "xyz"); //$NON-NLS-1$
    assertEquals(1, results.getWritten().size());
    assertEquals("66B27417D37E024C46526C2F6D358A754FC552F3", results.getWritten().get(0)[1]); //$NON-NLS-1$
  }
}
