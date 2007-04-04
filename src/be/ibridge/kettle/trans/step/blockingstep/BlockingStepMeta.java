package be.ibridge.kettle.trans.step.blockingstep;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;

public class BlockingStepMeta  extends BaseStepMeta implements StepMetaInterface {

    public void check(ArrayList remarks, StepMeta stepMeta, Row prev, String[] input, String[] output, Row info) {
        // See if we have input streams leading to this step!
        if (input.length>0) 
        {
            CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("BlockingStepMeta.CheckResult.StepExpectingRowsFromOtherSteps"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        } 
        else 
        {
            CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("BlockingStepMeta.CheckResult.NoInputReceivedError"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }
    }

    public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String stepname) {
        return new BlockingStepDialog(shell, info, transMeta, stepname);
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
        return new BlockingStep(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }

    public StepDataInterface getStepData() {
        return new BlockingStepData();
    }

    public void loadXML(Node stepnode, ArrayList databases, Hashtable counters) throws KettleXMLException {
        readData(stepnode);        
    }

    public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters) throws KettleException {
    }

    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException {
    }

    public void setDefault() {        
    }

    private void readData(Node stepnode)
    {
    }
}
