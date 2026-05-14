package org.pentaho.di.trans.steps.teststepwithfields;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Test step implementation that adds fields to the output.
 * This is a minimal implementation for testing purposes.
 */
public class TestStepWithFields extends BaseStep {

  public TestStepWithFields( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
                             TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    Object[] r = getRow();
    if ( r == null ) {
      setOutputDone();
      return false;
    }

    // Just pass through the row - the fields are added by the meta's getFields() method
    putRow( getInputRowMeta(), r );
    return true;
  }
}
