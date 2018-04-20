/**
 * 
 */
package org.pentaho.di.trans.steps.srstransformation;

import com.vividsolutions.jts.geom.Geometry;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.geospatial.SRS;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

/**
 * Transform between spatial reference systems.
 * 
 * @author phobus, sgoldinger
 * @since 29-oct-2008
 */
public class SRSTransformation extends BaseStep implements StepInterface{

//	private final static String MESSAGE_LOG = "SRSTransformation.Log.";
//	private final static String MESSAGE_EXP = "SRSTransformation.Exception.";
	private SRSTransformationMeta meta;
	private SRSTransformationData data;
	private SRSTransformator transformator;
	

	public SRSTransformation(StepMeta stepMeta, StepDataInterface stepDataInterface,
                             int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	@Override
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		meta = (SRSTransformationMeta) smi;
		data = (SRSTransformationData) sdi;
		
		// Finish if there is no more input
		Object[] inputRow = getRow();	// Get next usable row from input RowSet(s)!
		if (inputRow == null) {
			setOutputDone();
			return false;
		}
		
		// Initialization block which is executed when the first row in the set is processed.
		if (first) {
        	first = false;
        	
        	RowMetaInterface outputRowMeta = getInputRowMeta().clone();
        	meta.getFields(outputRowMeta, getStepname(), null, null, this);
            data.setOutputRowMeta(outputRowMeta);
            
            SRS sourceSRS = meta.getSourceSRS(getInputRowMeta());
            SRS targetSRS = meta.getTargetSRS();
        	transformator = new SRSTransformator(sourceSRS, targetSRS);
        }
		
		// Perform spatial reference transformation. Abort on errors
		try {
			Object[] outputRow = transformSpatialReferenceSystem(getInputRowMeta(), inputRow);
			putRow(data.getOutputRowMeta(), outputRow);	// copy row to output RowSet(s)
		} catch (KettleStepException ke) {
			log.logError("GeoKettle", ke.getSuperMessage());
			setErrors(1);
			setOutputDone();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Transforms a row's geometry objects to a new spatial reference system. 
	 * 
	 * INVARIANT:
	 *  - The RowMeta remains the same.
	 * PRECONDITION:
	 *  - The row contains at least one geometry.
	 *  - The type of is Geometry.
	 * POSTCONDITION:
	 *  - The spatial reference system is correctly transformed.
	 * 
	 * @param rowMeta The metadata of the row.
	 * @param row The actual objects in the row.
	 * @return The row-objects with the transformed geometry-value.
	 * @throws KettleStepException
	 */
	private synchronized Object[] transformSpatialReferenceSystem(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
		final int LENGTH = row.length;
		Object[] result = new Object[LENGTH];
		
		for (int i=0; i < LENGTH; i++) {
			if (row[i] != null) {
				// Transform geometry-values from the selected field...
				ValueMetaInterface vm = rowMeta.getValueMeta(i);
				if (vm.getName().equals(meta.getFieldName()) && vm.isGeometry()) {
					// Read geometry, transform, write back to result-row
					result[i] = transformator.transform( (Geometry) row[i] );
				} else {
					result[i] = row[i];
				}
			} else {
				result[i] = null;
			}
		}
		
		return result;
	}
	
	@Override
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (SRSTransformationMeta) smi;
		data = (SRSTransformationData) sdi;
		
		return super.init(smi, sdi);
	}

//	@Override
//	public void run() {
//		BaseStep.runStepThread(this, meta, data);
//	}

}
