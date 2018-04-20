package org.pentaho.di.trans.steps.gisfileoutput;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
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


public class GISFileOutputMeta extends BaseStepMeta implements StepMetaInterface {

	private static Class<?> PKG = GISFileOutput.class;

	private  String  fileName; 
	private boolean isFileNameInField;
	private String fileNameField;
	private String  gisFileCharset;
	private String  acceptingStepName;
	private StepMeta acceptingStep;

	public GISFileOutputMeta(){
		super(); // allocate BaseStepMeta
	}
	
    public String getFileName(){
        return fileName;
    }
    
    public void setFileName(String  fileName){
        this.fileName = fileName;
    }
    
    public String getFileNameField(){
        return fileNameField;
    }
    
    public void setFileNameField(String fileNameField){
        this.fileNameField = fileNameField;
    }
    
    public boolean isFileNameInField(){
        return isFileNameInField;
    }
    
    public void setFileNameInField(boolean isfileNameInField){
        this.isFileNameInField = isfileNameInField;
    }
    
	public String getGisFileCharset() {
		return gisFileCharset;
	}

	public void setGisFileCharset(String gisFileCharset) {
		this.gisFileCharset = gisFileCharset;
	}
	
	public String getLookupStepname(){
		if (isFileNameInField && acceptingStep!=null && !Const.isEmpty(acceptingStep.getName()))
			return acceptingStep.getName();
		return null;
	}
	
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}

	public Object clone(){
		GISFileOutputMeta retval = (GISFileOutputMeta)super.clone();
		return retval;
	}
	
	public void searchInfoAndTargetSteps(List<StepMeta> steps){
		acceptingStep = StepMeta.findStep(steps, acceptingStepName);
	}

	public String[] getInfoSteps(){
		if (isFileNameInField && acceptingStep!=null)
			return new String[] { acceptingStep.getName() };		
		return null;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException {
		try{				
			fileNameField     = XMLHandler.getTagValue(stepnode, "filenamefield");
			isFileNameInField  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "isfilenameinfield"));
			fileName    = XMLHandler.getTagValue(stepnode, "filename");
			gisFileCharset     = XMLHandler.getTagValue(stepnode, "gis_file_charset"); //$NON-NLS-1$
			acceptingStepName = XMLHandler.getTagValue(stepnode, "accept_stepname");
		}catch(Exception e){
			throw new KettleXMLException(BaseMessages.getString(PKG,"GISFileOutputMeta.Exception.UnableToReadStepInformationFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault(){
		fileName    = null;
		fileNameField = null;
		isFileNameInField = false;
		gisFileCharset = null;
	}

	public String getXML(){
		StringBuffer retval=new StringBuffer();	
		retval.append("    " + XMLHandler.addTagValue("filename", fileName));
		retval.append("    " + XMLHandler.addTagValue("isfilenameinfield", isFileNameInField));
		retval.append("    " + XMLHandler.addTagValue("filenamefield", fileNameField));
		retval.append("    " + XMLHandler.addTagValue("gis_file_charset", gisFileCharset)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("accept_stepname", (acceptingStep!=null?acceptingStep.getName():"") ));
		return retval.toString();
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException {
		try{
			fileName    = rep.getStepAttributeString (id_step, "filename");
			isFileNameInField   = rep.getStepAttributeBoolean(id_step, "isfilenameinfield");	
			fileNameField     = rep.getStepAttributeString (id_step, "filenamefield");
			gisFileCharset     = rep.getStepAttributeString (id_step, "gis_file_charset"); //$NON-NLS-1$
			acceptingStepName  = rep.getStepAttributeString (id_step, "accept_stepname");
		}catch(Exception e){
			throw new KettleException(BaseMessages.getString(PKG,"GISFileOutputMeta.Exception.UnexpectedErrorReadingMetaDataFromRepository"), e); //$NON-NLS-1$
		}
	}
	
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException {
		try{
			rep.saveStepAttribute(id_transformation, id_step, "filenamefield", fileNameField);
			rep.saveStepAttribute(id_transformation, id_step, "filename", fileName);
			rep.saveStepAttribute(id_transformation, id_step, "isfilenameinfield", isFileNameInField);
			rep.saveStepAttribute(id_transformation, id_step, "gis_file_charset", gisFileCharset); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "accept_stepname", (acceptingStep!=null?acceptingStep.getName():"") );
		}catch(Exception e){
			throw new KettleException(BaseMessages.getString(PKG,"GISFileOutputMeta.Exception.UnableToSaveMetaDataToRepository")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
                      String[] input, String[] output, RowMetaInterface info){
		CheckResult cr = null;
		
		if (!isFileNameInField){
			if (fileName ==null){
			    cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG,"GISFileOutputMeta.Remark.PleaseSelectFileToUse"), stepMeta); //$NON-NLS-1$
			    remarks.add(cr);
			}
		}else if (fileNameField == null){
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG,"GISFileOutputMeta.Remark.PleaseSelectFileField"), stepMeta); //$NON-NLS-1$
		    remarks.add(cr);
		}else{	
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG,"GISFileOutputMeta.Remark.FileToUseIsSpecified"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
            if (input.length > 0){
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG,"GISFileOutputMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            }else{
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG,"GISFileOutputMeta.CheckResult.NoInpuReceived"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            }
        }
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans){
		return new GISFileOutput(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData(){
		return new GISFileOutputData();
	}

	public void setAcceptingStepName(String acceptingStepName) {
		this.acceptingStepName = acceptingStepName;
	}

	public String getAcceptingStepName() {
		return acceptingStepName;
	}

	public void setAcceptingStep(StepMeta acceptingStep) {
		this.acceptingStep = acceptingStep;
	}

	public StepMeta getAcceptingStep() {
		return acceptingStep;
	} 
}