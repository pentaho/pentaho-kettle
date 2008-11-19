package org.pentaho.di.lineage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

/**
 * This class will help calculate and contain the data lineage for all values in the transformation.<br>
 * What we will get is a List of ValueLineage objects for all the values steps in the transformation.<br>  
 * Each of these ValueLineage objects contains a list of all the steps it passed through.<br>
 * As such, it's a hierarchical view of the transformation.<br>
 * 
 * This view will allow us to see immediately where a certain value is being manipulated.<br>
 * 
 * @author matt
 *
 */
public class TransDataLineage {
	private TransMeta transMeta;
	
	private List<ValueLineage> valueLineages;
	
	private Map<ValueMetaInterface, List<StepMeta>> fieldStepsMap;
	
	public TransDataLineage(TransMeta transMeta) {
		this.transMeta = transMeta;
		this.valueLineages = new ArrayList<ValueLineage>();
	}
	
	public TransMeta getTransMeta() {
		return transMeta;
	}
	
	public void setTransMeta(TransMeta transMeta) {
		this.transMeta = transMeta;
	}

	/**
	 * @return the valueLineages
	 */
	public List<ValueLineage> getValueLineages() {
		return valueLineages;
	}

	/**
	 * @param valueLineages the valueLineages to set
	 */
	public void setValueLineages(List<ValueLineage> valueLineages) {
		this.valueLineages = valueLineages;
	}

	/**
	 * Using the transformation, we will calculate the data lineage for each field in each step.
	 * @throws KettleStepException In case there is an exception calculating the lineage. 
	 *                             This is usually caused by unavailable data sources etc.
	 */
	public void calculateLineage() throws KettleStepException {
		fieldStepsMap = new HashMap<ValueMetaInterface, List<StepMeta>>();
		
		int nrUsedSteps = transMeta.nrUsedSteps();
		for (int i=0;i<nrUsedSteps;i++) {
			StepMeta stepMeta = transMeta.getUsedStep(i);
			calculateLineage(stepMeta);
		}
	}

	/**
	 * Calculate the lineage for the specified step only...
	 * @param stepMeta The step to calculate the lineage for.
	 * @throws KettleStepException In case there is an exception calculating the lineage. 
	 *                             This is usually caused by unavailable data sources etc.
	 */
	private void calculateLineage(StepMeta stepMeta) throws KettleStepException {
		RowMetaInterface outputMeta = transMeta.getStepFields(stepMeta);
		
		// The lineage is basically a calculation of origin for each output of a certain step.
		//
		for (ValueMetaInterface valueMeta : outputMeta.getValueMetaList()) {
			
			StepMeta originStepMeta = transMeta.findStep(valueMeta.getOrigin(), stepMeta);
			if (originStepMeta!=null) {
				List<StepMeta> list = fieldStepsMap.get(originStepMeta);
			}
		}
	}

}
