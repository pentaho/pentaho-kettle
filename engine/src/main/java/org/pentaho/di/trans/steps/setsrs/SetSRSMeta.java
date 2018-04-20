package org.pentaho.di.trans.steps.setsrs;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.geospatial.SRS;
import org.pentaho.di.core.geospatial.SRSFactory;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;

public class SetSRSMeta extends BaseStepMeta implements StepMetaInterface {

	private static Class<?> PKG = SetSRS.class;

	private static final String MESSAGE_CHECK = "SetSRSMeta.CheckResult.";
	private static final String MESSAGE_EXC = "SetSRSMeta.Exception.";
	
	/** The selected spatial reference system **/
	private SRS selectedSRS;
	/** On which field is the transformation applied? **/
	/** Stores the actual status **/
	private int actualStatus;
	private String actualPath, fieldName;
	public static final int STATUS_WKT = 3;
	public static final int STATUS_FILE = 2;
	public static final int STATUS_EPSGCODE = 1;
	
	public SetSRSMeta() {
		super();
		setSelectedSRSMeta(SRS.UNKNOWN);
		setActualStatus(SetSRSMeta.STATUS_EPSGCODE);
		setFieldName("");
	}
	
	public SRS getSelectedSRS() {
		return (selectedSRS != null) ? selectedSRS : SRS.UNKNOWN;
	}
	
	public void setSelectedSRSMeta(SRS selectedSRS) {
		this.selectedSRS = (selectedSRS != null) ? selectedSRS : SRS.UNKNOWN;
	}
	
	public String getFieldName() {
    	return fieldName;
    }
    
    public void setFieldName(String fieldName) {
    	this.fieldName = fieldName;
    }
    
    public int getActualStatus(){
		return actualStatus;
	}
	
	public void setActualStatus(int actualStatus) {
		this.actualStatus = actualStatus;
	}
	
	public String getActualPath(){
		return actualPath;
	}
	
	public void setActualPath(String path){
		this.actualPath = path;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.pentaho.di.trans.step.BaseStepMeta#getFields(org.pentaho.di.core.row.RowMetaInterface, java.lang.String, org.pentaho.di.core.row.RowMetaInterface[], org.pentaho.di.trans.step.StepMeta, org.pentaho.di.core.variables.VariableSpace)
	 */
	public void getFields(RowMetaInterface inputRowMeta, String name,
                          RowMetaInterface info[], StepMeta nextStep, VariableSpace space)
			throws KettleStepException {
		// Set the SRS in ValueMeta, if it has changed or leave everything at is
		// is, if there are no changes to make.
		if (!selectedSRS.equals(SRS.UNKNOWN) && !Const.isEmpty(fieldName)) {
			int idx = inputRowMeta.indexOfValue(fieldName);
			// Value found
			if (idx >= 0) {
				// This is the value we need to change:
				ValueMetaInterface v = inputRowMeta.getValueMeta(idx);
				// Do we need to set the SRID?
				// if (v.getGeometrySRS().equals(SRS.UNKNOWN)) {
				v.setGeometrySRS(selectedSRS);
				v.setOrigin(name);
				// }
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#check(java.util.List, org.pentaho.di.trans.TransMeta, org.pentaho.di.trans.step.StepMeta, org.pentaho.di.core.row.RowMetaInterface, java.lang.String[], java.lang.String[], org.pentaho.di.core.row.RowMetaInterface)
	 */
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta,
                      StepMeta stepMeta, RowMetaInterface prev, String[] input,
                      String[] output, RowMetaInterface info)
	{
		// TODO: GeoKettle: add correct check operations
		
		// Look up fields in the input stream <prev>
		if (prev != null && prev.size() > 0) {
			addRemarkOk(remarks, "StepReceivingFields", Integer.toString(prev.size()), stepMeta); //$NON-NLS-1$
		} else {
			addRemarkError(remarks, "NoInputReceivedFromOtherSteps"+ Const.CR, stepMeta); //$NON-NLS-1$
		}

		// See if we have input streams leading to this step!
		if (input.length > 0) {
			addRemarkOk(remarks, "StepReceivingInfoFromOtherSteps", stepMeta); //$NON-NLS-1$
		} else {
			addRemarkError(remarks, "NoInputReceivedFromOtherSteps", stepMeta); //$NON-NLS-1$
		}
	}
	
	private void addRemarkOk(List<CheckResultInterface> remarks, String remark, StepMeta stepMeta) {
		CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG,MESSAGE_CHECK+remark), stepMeta); //$NON-NLS-1$
		remarks.add(cr);
	}
	
	private void addRemarkOk(List<CheckResultInterface> remarks, String remark, String param, StepMeta stepMeta) {
		CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG,MESSAGE_CHECK+remark), param, stepMeta); //$NON-NLS-1$
		remarks.add(cr);
	}
	
	private void addRemarkError(List<CheckResultInterface> remarks, String remark, StepMeta stepMeta) {
		CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG,MESSAGE_CHECK+remark), stepMeta); //$NON-NLS-1$
		remarks.add(cr);
	}

	/*
	 * (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#getStep(org.pentaho.di.trans.step.StepMeta, org.pentaho.di.trans.step.StepDataInterface, int, org.pentaho.di.trans.TransMeta, org.pentaho.di.trans.Trans)
	 */
	public StepInterface getStep(StepMeta stepMeta,
                                 StepDataInterface stepDataInterface, int copyNr,
                                 TransMeta transMeta, Trans trans) {
		return new SetSRS(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	/*
	 * (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#getStepData()
	 */
	public StepDataInterface getStepData() {
		return new SetSRSData();
	}

	@Override
	public String getXML() {
		StringBuffer retval = new StringBuffer(200);
		
		// XML: the fieldname where the result-rows are sent to
		retval.append(XMLHandler.addTagValue("field_name", getFieldName())); //$NON-NLS-1$
		
		// XML: the GUI status
		retval.append(XMLHandler.addTagValue("actual_gui_status", getActualStatus())); //$NON-NLS-1$
		
		// XML: the selected spatial reference system
		retval.append("    <selected_srs>").append(Const.CR); //$NON-NLS-1$
		if (selectedSRS == null) {
			selectedSRS = SRS.UNKNOWN;
		}
		retval.append(selectedSRS.getXML());
		retval.append("    </selected_srs>").append(Const.CR); //$NON-NLS-1$
		
		return retval.toString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#loadXML(org.w3c.dom.Node, java.util.List, java.util.Map)
	 */
	public void loadXML(Node stepnode, List<DatabaseMeta> databases,
			Map<String, Counter> counters) throws KettleXMLException {
		try {
			// XML: read the stepname
			fieldName = Const.NVL(XMLHandler.getTagValue(stepnode, "field_name"), ""); //$NON-NLS-1$ //$NON-NLS-2$
			
			// XML: read the GUI status
			actualStatus = Const.toInt(XMLHandler.getTagValue(stepnode, "actual_gui_status"), SetSRSMeta.STATUS_EPSGCODE); //$NON-NLS-1$
			
			// XML: read the SRS object
			Node selectedSRSMetaNode = XMLHandler.getSubNode(stepnode, "selected_srs"); //$NON-NLS-1$
			selectedSRS = (selectedSRSMetaNode != null) ? SRSFactory.createSRS(selectedSRSMetaNode) : SRS.UNKNOWN;
		}
		catch (Exception e) {
			throw new KettleXMLException(BaseMessages.getString(PKG,MESSAGE_EXC+"UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#readRep(org.pentaho.di.repository.Repository, long, java.util.List, java.util.Map)
	 */
	public void readRep(Repository rep, ObjectId id_step,
                        List<DatabaseMeta> databases, Map<String, Counter> counters)
			throws KettleException {
		try {
			fieldName = Const.NVL(rep.getStepAttributeString(id_step, "field_name"), ""); //$NON-NLS-1$ //$NON-NLS-2$
			actualStatus		= (int) rep.getStepAttributeInteger(id_step, "actual_gui_status"); //$NON-NLS-1$
			selectedSRS = SRSFactory.createSRS(rep, id_step, "selected");
		}
		catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG,MESSAGE_EXC+"UnexpectedErrorInReadingStepInfoFromRepository"), e); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#saveRep(org.pentaho.di.repository.Repository, long, long)
	 */
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
		try {
			rep.saveStepAttribute(id_transformation, id_step, "field_name", getFieldName()); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "actual_gui_status", getActualStatus()); //$NON-NLS-1$
			
			if (selectedSRS.is_custom) {
				rep.saveStepAttribute(id_transformation, id_step, "sourcesrs_wkt", selectedSRS.getCRS().toWKT());
			} else {
				rep.saveStepAttribute(id_transformation, id_step, "selectedsrs_authority", selectedSRS.authority); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, "selectedsrs_srid",	selectedSRS.srid); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, "selectedsrs_description", selectedSRS.description); //$NON-NLS-1$
			}
		}
		catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG,MESSAGE_EXC+"UnableToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
		}
	}

	@Override
	public Object clone() {
		SetSRSMeta retval = (SetSRSMeta) super.clone();
		if (selectedSRS != null) {
			retval.setSelectedSRSMeta((SRS) selectedSRS.clone());
		} else {
			retval.setSelectedSRSMeta(SRS.UNKNOWN);
		}
		retval.setFieldName(new String(Const.NVL(fieldName, "")));
		retval.setActualStatus(actualStatus);
		return retval;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#setDefault()
	 */
	public void setDefault() {}
}
