/**
 *
 */
package org.pentaho.di.trans.steps.setsrs;


import com.vividsolutions.jts.geom.Geometry;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.geospatial.SRS;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;


/**
 * Changes the SRID and saves it to metadata
 *
 * @author phobus, sgoldinger
 * @since 25-nov-2008
 */
public class SetSRS extends BaseStep implements StepInterface {

    private static Class<?> PKG = SetSRS.class;

    private SetSRSMeta meta;
    private SetSRSData data;

    public SetSRS(StepMeta stepMeta, StepDataInterface stepDataInterface,
                  int copyNr, TransMeta transMeta, Trans trans) {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }


    @Override
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
        meta = (SetSRSMeta) smi;
        data = (SetSRSData) sdi;

        // Finish if there is no more input
        Object[] inputRow = getRow();    // Get next usable row from input RowSet(s)!
        if (inputRow == null) {
            setOutputDone();
            return false;
        }

        // The SRID in the metadata must be modified only once for all fields
        if (first) {
            first = false;

            RowMetaInterface outputRowMeta = getInputRowMeta().clone();
            meta.getFields(outputRowMeta, getStepname(), null, null, this);
            data.setOutputRowMeta(outputRowMeta);
        }

        // The SRID must be changed for every single Geometry-object
        changeSRID(inputRow);
        putRow(data.getOutputRowMeta(), inputRow);    // copy row to output RowSet(s)

        return true;
    }

    /**
     * Find the value, that must be modified with the new SRID and change
     * the SRS object in it.
     */
    // REVIEW: etdub 20090129 not needed, using SRSTransformationMeta.getFields(...)
//	private void changeSRIDMeta(RowMetaInterface outputRowMeta) throws KettleStepException {
//		int idx = outputRowMeta.indexOfValue(meta.getFieldName());
//		if (idx >= 0) {
//			ValueMetaInterface vm = outputRowMeta.getValueMeta(idx);
//			String log_old_srid = vm.getGeometrySRS().is_custom ? "CUSTOM:" + vm.getGeometrySRS().srid : vm.getGeometrySRS().srid;
//			vm.setGeometrySRS(meta.getSelectedSRS());
//			
//			// logging...
//			log.logDetailed("GeoKettle", "Changed SRID in metadata from " + log_old_srid + " to " + meta.getSelectedSRS().srid);
//		}
//	}


    /**
     * The SRID must be set in every Geometry-object.
     *
     * @param inputRow The row with the data.
     * @return The modified Geometry-objects with the new SRID.
     */
    private synchronized void changeSRID(Object[] inputRow) {
        for (int i = 0; i < inputRow.length; i++) {
            Object field = inputRow[i];
            if (field != null) {
                // Set the new SRID in the Geometry-object
                ValueMetaInterface vm = getInputRowMeta().getValueMeta(i);
                if (vm.getName().equals(meta.getFieldName()) && vm.isGeometry()) {
                    SRS newSRS = meta.getSelectedSRS();
                    String log_old_srid = vm.getGeometrySRS().is_custom ? "CUSTOM:" + vm.getGeometrySRS().srid : vm.getGeometrySRS().srid;

                    // FIXME: is this right? review this!
                    // vm.setGeometrySRS(newSRS);						// Meta-SRID

                    ((Geometry) field).setSRID(newSRS.getSRID());    // Geometry-SRID

                    // logging...
                    log.logDetailed("GeoKettle", "Changed SRID in geometry from " + log_old_srid + " to " + meta.getSelectedSRS().srid);
                }
            }
            i++;
        }
    }

    @Override
    public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
        meta = (SetSRSMeta) smi;
        data = (SetSRSData) sdi;

        return super.init(smi, sdi);
    }

//    @Override
//    public void run() {
//        BaseStep.runStepThread(this, meta, data);
//    }

}
