package org.pentaho.di.trans.steps.edi2xml;


import java.util.List;
import java.util.Map;

import org.pentaho.di.core.*;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.*;
import org.pentaho.di.core.row.*;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.*;
import org.pentaho.di.trans.*;
import org.pentaho.di.trans.step.*;
import org.w3c.dom.Node;

public class Edi2XmlMeta extends BaseStepMeta implements StepMetaInterface {

	private static Class<?> PKG = Edi2XmlMeta.class; // for i18n purposes

	private String outputField;
	private String inputField;

	public Edi2XmlMeta() {
		super();
	}

	public String getInputField() {
		return inputField;
	}

	public void setInputField(String inputField) {
		this.inputField = inputField;
	}

	public String getOutputField() {
		return outputField;
	}

	public void setOutputField(String outputField) {
		this.outputField = outputField;
	}

	public String getXML() throws KettleValueException {
		StringBuffer retval = new StringBuffer();

		retval.append("   " + XMLHandler.addTagValue("inputfield", inputField)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("   " + XMLHandler.addTagValue("outputfield", outputField)); //$NON-NLS-1$ //$NON-NLS-2$

		return retval.toString();
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {

		try {
			setInputField(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "inputfield")));
			setOutputField(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "outputfield")));
		} catch (Exception e) {
			throw new KettleXMLException("Template Plugin Unable to read step info from XML node", e);
		}

	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
		try {
			inputField = rep.getStepAttributeString(id_step, "inputfield"); //$NON-NLS-1$
			outputField = rep.getStepAttributeString(id_step, "outputfield"); //$NON-NLS-1$
		} catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "Edi2Xml.Exception.UnexpectedErrorInReadingStepInfo"), e);
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
		try {
			rep.saveStepAttribute(id_transformation, id_step, "inputfield", inputField); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "outputfield", outputField); //$NON-NLS-1$
		} catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "Edi2Xml.Exception.UnableToSaveStepInfoToRepository") + id_step, e);
		}
	}

	public void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) {

		ValueMetaInterface extra = null;

		if (!Const.isEmpty(getOutputField())) {
			extra = new ValueMeta(space.environmentSubstitute(getOutputField()), ValueMetaInterface.TYPE_STRING);
			extra.setOrigin(origin);
			r.addValueMeta(extra);
		} else {
			if (!Const.isEmpty(getInputField())) {
				extra = r.searchValueMeta(space.environmentSubstitute(getInputField()));
			}
		}

		if (extra != null) {
			extra.setStorageType(ValueMetaInterface.STORAGE_TYPE_NORMAL);
		}

	}

	public void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info) {
		CheckResult cr;

		// See if we have input streams leading to this step!
		if (input.length > 0) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving input from other steps.", stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepMeta);
			remarks.add(cr);
		}

		// is the input field there?
		String realInputField = transmeta.environmentSubstitute(getInputField());
		if (prev.searchValueMeta(realInputField) != null){
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is seeing input field: "+realInputField, stepMeta);
			remarks.add(cr);
			
			if (prev.searchValueMeta(realInputField).isString()){
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Field "+realInputField+" is a string type", stepMeta);
				remarks.add(cr);
			}
			else{
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Field "+realInputField+" is not a string type!", stepMeta);
				remarks.add(cr);
			}
			
		}
		else{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Step is not seeing input field: "+realInputField+"!", stepMeta);
			remarks.add(cr);
		}

	}

	public Object clone() {
		Object retval = super.clone();
		return retval;
	}

	public void setDefault() {
		outputField = "edi_xml";
		inputField = "";
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp) {
		return new Edi2Xml(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	public StepDataInterface getStepData() {
		return new Edi2XmlData();
	}

	@Override
	public boolean supportsErrorHandling() {
		return true;
	}
	
	
}