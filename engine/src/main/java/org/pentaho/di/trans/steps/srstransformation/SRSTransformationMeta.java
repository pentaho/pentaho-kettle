/**
 * 
 */
package org.pentaho.di.trans.steps.srstransformation;

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

/**
 * Metadata describing the {@link SRSTransformation} class.
 * 
 * @author phobus, sgoldinger
 * @since 29-oct-2008
 */
public class SRSTransformationMeta extends BaseStepMeta implements StepMetaInterface {

	private static Class<?> PKG = SRSTransformation.class;

	private static final String MESSAGE_CHECK = "SRSTransformationMeta.CheckResult.";
	public static final int STATUS_EXISTING = 1;
	public static final int STATUS_WKT = 2;
	public static final int STATUS_AUTO = 3;
	
	/** On which field is the transformation applied? **/
	private String fieldName;
	/** The source spatial reference system. **/
	private SRS sourceSRS;
	/** The target spatial reference system. **/
	private SRS targetSRS;
	/** The status of the source-SRS GUI **/
	private int sourceGUIStatus;
	/** The status of the target-SRS GUI **/
	private int targetGUIStatus;
	
	public SRSTransformationMeta() {
		super();
		setSourceSRS(null);
		setTargetSRS(null);
		fieldName = "";
		sourceGUIStatus = STATUS_AUTO;
		targetGUIStatus = STATUS_EXISTING;
	}
	
    public String getFieldName() {
    	return fieldName;
    }
    
    public void setFieldName(String fieldName) {
    	this.fieldName = fieldName;
    }
    
    public int getSourceGUIStatus() {
    	return sourceGUIStatus;
    }
    
    public void setSourceGUIStatus(int sourceGUIStatus) {
    	this.sourceGUIStatus = sourceGUIStatus;
    }
    
    public int getTargetGUIStatus() {
    	return targetGUIStatus;
    }
    
    public void setTargetGUIStatus(int targetGUIStatus) {
    	this.targetGUIStatus = targetGUIStatus;
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
		if (!targetSRS.equals(SRS.UNKNOWN) && !Const.isEmpty(fieldName)) {
			int idx = inputRowMeta.indexOfValue(fieldName);
			// Value found
			if (idx >= 0) {
				// This is the value we need to change:
				ValueMetaInterface v = inputRowMeta.getValueMeta(idx);
				// Do we need to set the SRID?
				// if (v.getGeometrySRS().equals(SRS.UNKNOWN)) {
				v.setGeometrySRS(targetSRS);
				v.setOrigin(name);
				// }
			}
		}
	}
    
    /**
	 * Get the source-SRS from the metadata.
	 * 
	 * @param inputRowMeta The {@link RowMetaInterface} that may contain a SRS.
	 * @return The {@link SRS} from the {@link RowMetaInterface} if there is
	 *         one. If there is no, return the {@link SRS} from this
	 *         {@link SRSTransformationMeta} and if there is no, return a
	 *         {@link SRS}.UNKNOWN.
	 */
	public SRS getSourceSRS(RowMetaInterface inputRowMeta) {
		// Return the SRS from the RowMetaInterface, if possible.
		// TODO: GeoKettle: check this
		if (/*!sourceSRS.equals(SRS.UNKNOWN) && */!Const.isEmpty(fieldName) && sourceGUIStatus == STATUS_AUTO) {
			int idx = inputRowMeta.indexOfValue(fieldName);
			if (idx >= 0) {
				ValueMetaInterface v = inputRowMeta.getValueMeta(idx);
				if (!v.getGeometrySRS().equals(SRS.UNKNOWN)) {
					return v.getGeometrySRS();
				}
			}
		}
		
		return (sourceSRS != null) ? sourceSRS : SRS.UNKNOWN;
	}
	
	public SRS getTargetSRS() {
		return (sourceSRS != null) ? targetSRS : SRS.UNKNOWN;
	}
	
	public void setSourceSRS(SRS sourceSRS) {
		this.sourceSRS = (sourceSRS != null) ? sourceSRS : SRS.UNKNOWN;
	}
	
	public void setTargetSRS(SRS targetSRS) {
		this.targetSRS = (targetSRS != null) ? targetSRS : SRS.UNKNOWN;
	}
	
    @Override
	public Object clone() {
		SRSTransformationMeta retval = (SRSTransformationMeta) super.clone();
		if (sourceSRS != null) {
			retval.setSourceSRS((SRS) sourceSRS.clone());
		} else {
			retval.setSourceSRS(SRS.UNKNOWN);
		}
		if (targetSRS != null) {
			retval.setTargetSRS((SRS) targetSRS.clone());
		} else {
			retval.setTargetSRS(SRS.UNKNOWN);
		}
		retval.setFieldName(new String(Const.NVL(fieldName, "")));
		retval.setSourceGUIStatus(sourceGUIStatus);
		retval.setTargetGUIStatus(targetGUIStatus);
		return retval;
	}
	
	/* (non-Javadoc)
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
	
	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#getStep(org.pentaho.di.trans.step.StepMeta, org.pentaho.di.trans.step.StepDataInterface, int, org.pentaho.di.trans.TransMeta, org.pentaho.di.trans.Trans)
	 */
	public StepInterface getStep(StepMeta stepMeta,
                                 StepDataInterface stepDataInterface, int copyNr,
                                 TransMeta transMeta, Trans trans) {
		return new SRSTransformation(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	
	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#getStepData()
	 */
	public StepDataInterface getStepData() {
		return new SRSTransformationData();
	}

	
	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#loadXML(org.w3c.dom.Node, java.util.List, java.util.Map)
	 */
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		try {
			// XML: read the fielname
			fieldName = Const.NVL(XMLHandler.getTagValue(stepnode, "field_name"), ""); //$NON-NLS-1$
			
			// XML: read the GUI status
			sourceGUIStatus = Const.toInt(XMLHandler.getTagValue(stepnode, "source_gui_status"), STATUS_AUTO); //$NON-NLS-1$
			targetGUIStatus = Const.toInt(XMLHandler.getTagValue(stepnode, "target_gui_status"), STATUS_EXISTING); //$NON-NLS-1$
			
			// XML: read the SRS source object
			Node sourceSRSMetaNode = XMLHandler.getSubNode(stepnode, "source_srs"); //$NON-NLS-1$
			sourceSRS = (sourceSRSMetaNode != null) ? SRSFactory.createSRS(sourceSRSMetaNode) : SRS.UNKNOWN;
			
			// XML: read the SRS target object
			Node targetSRSMetaNode = XMLHandler.getSubNode(stepnode, "target_srs"); //$NON-NLS-1$
			targetSRS = (targetSRSMetaNode != null) ? SRSFactory.createSRS(targetSRSMetaNode) : SRS.UNKNOWN;
		}
		catch (Exception e) {
			throw new KettleXMLException(BaseMessages.getString(PKG,"SRSTransformationMeta.Exception.UnableToLoadStepInfoFromXML"), e);	//$NON-NLS-1$
		}
	}
	
	
	@Override
	public String getXML() {
		StringBuffer retval = new StringBuffer(200);
		
		// XML: the fieldname
		retval.append(XMLHandler.addTagValue("field_name", getFieldName())); //$NON-NLS-1$
		
		// XML: the GUI status
		retval.append(XMLHandler.addTagValue("source_gui_status", getSourceGUIStatus())); //$NON-NLS-1$
		retval.append(XMLHandler.addTagValue("target_gui_status", getTargetGUIStatus())); //$NON-NLS-1$
		
		// XML: the source spatial reference system
		retval.append("    <source_srs>").append(Const.CR); //$NON-NLS-1$
		if (sourceSRS == null) {
			sourceSRS = SRS.UNKNOWN;
		}
		retval.append(sourceSRS.getXML());
		retval.append("    </source_srs>").append(Const.CR); //$NON-NLS-1$
		
		// XML: the target spatial reference system
		retval.append("    <target_srs>").append(Const.CR); //$NON-NLS-1$
		if (targetSRS == null) {
			targetSRS = SRS.UNKNOWN;
		}
		retval.append(targetSRS.getXML());
		retval.append("    </target_srs>").append(Const.CR); //$NON-NLS-1$
		return retval.toString();
	}

	
	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#readRep(org.pentaho.di.repository.Repository, long, java.util.List, java.util.Map)
	 */
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
		try {
			fieldName = Const.NVL(rep.getStepAttributeString(id_step, "field_name"), ""); //$NON-NLS-1$ //$NON-NLS-2$
			
			sourceGUIStatus		= (int) rep.getStepAttributeInteger(id_step, "source_gui_status"); //$NON-NLS-1$
			targetGUIStatus		= (int) rep.getStepAttributeInteger(id_step, "target_gui_status"); //$NON-NLS-1$
			
			sourceSRS = SRSFactory.createSRS(rep, id_step, "source");
			targetSRS = SRSFactory.createSRS(rep, id_step, "target");
		}
		catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG,"SRSTransformationMeta.Exception.UnexpectedErrorInReadingStepInfoFromRepository"), e); //$NON-NLS-1$
		}
	}

	
	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#saveRep(org.pentaho.di.repository.Repository, long, long)
	 */
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
		try {
			rep.saveStepAttribute(id_transformation, id_step, "field_name", getFieldName()); //$NON-NLS-1$
			
			rep.saveStepAttribute(id_transformation, id_step, "source_gui_status", getSourceGUIStatus()); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "target_gui_status", getTargetGUIStatus()); //$NON-NLS-1$
			
			if (sourceSRS.is_custom) {
				rep.saveStepAttribute(id_transformation, id_step, "sourcesrs_wkt", sourceSRS.getCRS().toWKT());
			} else {
				rep.saveStepAttribute(id_transformation, id_step, "sourcesrs_authority", sourceSRS.authority); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, "sourcesrs_srid",	sourceSRS.srid); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, "sourcesrs_description", sourceSRS.description); //$NON-NLS-1$
			}
			
			rep.saveStepAttribute(id_transformation, id_step, "targetsrs_authority", targetSRS.authority); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "targetsrs_srid",	targetSRS.srid); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "targetsrs_description", targetSRS.description); //$NON-NLS-1$
		}
		catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG,"SRSTransformationMeta.Exception.UnableToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#setDefault()
	 */
	public void setDefault() { }
}
