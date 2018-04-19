package org.pentaho.di.trans.steps.gisfileinput;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.geospatial.GeotoolsReader;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
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

public class GISFileInputMeta extends BaseStepMeta implements StepMetaInterface {

    private static Class<?> PKG = GISFileInput.class;

    private static final String YES = "Y";
    private int rowLimit;
    private boolean rowNrAdded;
    private String rowNrField;
    private String fileName;
    private String gisFileCharset;
    private boolean isFileNameInField;
    private String fileNameField;
    private boolean passingThruFields;
    private String acceptingStepName;
    private StepMeta acceptingStep;

    public GISFileInputMeta() {
        super(); // allocate BaseStepMeta
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileNameField() {
        return fileNameField;
    }

    public void setFileNameField(String fileNameField) {
        this.fileNameField = fileNameField;
    }

    public boolean isFileNameInField() {
        return isFileNameInField;
    }

    public void setFileNameInField(boolean isfileNameInField) {
        this.isFileNameInField = isfileNameInField;
    }

    public int getRowLimit() {
        return rowLimit;
    }

    public void setRowLimit(int rowLimit) {
        this.rowLimit = rowLimit;
    }

    public String getRowNrField() {
        return rowNrField;
    }

    public void setRowNrField(String rowNrField) {
        this.rowNrField = rowNrField;
    }

    public boolean isRowNrAdded() {
        return rowNrAdded;
    }

    public void setRowNrAdded(boolean rowNrAdded) {
        this.rowNrAdded = rowNrAdded;
    }

    public String getAcceptingStepName() {
        return acceptingStepName;
    }

    public void setAcceptingStepName(String acceptingStep) {
        this.acceptingStepName = acceptingStep;
    }

    public StepMeta getAcceptingStep() {
        return acceptingStep;
    }

    public void setAcceptingStep(StepMeta acceptingStep) {
        this.acceptingStep = acceptingStep;
    }

    public boolean isPassingThruFields() {
        return passingThruFields;
    }

    public void setPassingThruFields(boolean passingThruFields) {
        this.passingThruFields = passingThruFields;
    }

    public String getLookupStepname() {
        if (isFileNameInField && acceptingStep != null && !Const.isEmpty(acceptingStep.getName()))
            return acceptingStep.getName();
        return null;
    }

    /**
     * @param steps optionally search the info step in a list of steps
     */
    public void searchInfoAndTargetSteps(List<StepMeta> steps) {
        acceptingStep = StepMeta.findStep(steps, acceptingStepName);
    }

    public String[] getInfoSteps() {
        if (isFileNameInField && acceptingStep != null)
            return new String[]{acceptingStep.getName()};
        return null;
    }

    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
        readData(stepnode);
    }

    public Object clone() {
        GISFileInputMeta retval = (GISFileInputMeta) super.clone();
        return retval;
    }

    private void readData(Node stepnode) throws KettleXMLException {
        try {
            fileNameField = XMLHandler.getTagValue(stepnode, "filenamefield");
            isFileNameInField = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "isfilenameinfield"));
            fileName = XMLHandler.getTagValue(stepnode, "filename");
            rowLimit = Const.toInt(XMLHandler.getTagValue(stepnode, "limit"), 0); //$NON-NLS-1$
            rowNrAdded = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "add_rownr")); //$NON-NLS-1$ //$NON-NLS-2$
            rowNrField = XMLHandler.getTagValue(stepnode, "field_rownr"); //$NON-NLS-1$
            gisFileCharset = XMLHandler.getTagValue(stepnode, "gis_file_charset"); //$NON-NLS-1$
            passingThruFields = YES.equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "passing_through_fields"));
            acceptingStepName = XMLHandler.getTagValue(stepnode, "accept_stepname");
        } catch (Exception e) {
            throw new KettleXMLException(BaseMessages.getString(PKG, "GISFileInputMeta.Exception.UnableToReadStepInformationFromXML"), e); //$NON-NLS-1$
        }
    }

    public void setDefault() {
        fileName = null;
        fileNameField = null;
        isFileNameInField = false;
        rowLimit = 0;
        rowNrAdded = false;
        rowNrField = null;
        gisFileCharset = null;
    }

    @Override
    public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {
        if (info != null && isPassingThruFields() && isFileNameInField) {
            boolean found = false;
            for (int i = 0; i < info.length && !found; i++) {
                if (info[i] != null) {
                    row.mergeRowMeta(info[i]);
                    found = true;
                }
            }
        }
        if (!isFileNameInField()) {
            FileInputList fileList = getFileList(space);
            if (fileList.nrOfFiles() == 0)
                throw new KettleStepException(BaseMessages.getString(PKG, "GISFileInputMeta.Exception.NoFilesFoundToProcess")); //$NON-NLS-1$

            row.clear();
            row.addRowMeta(getOutputFields(fileList, name));
        }
    }

    public RowMetaInterface getOutputFields(FileInputList files, String name) throws KettleStepException {
        RowMetaInterface rowMeta = new RowMeta();
        if (files.nrOfFiles() == 0)
            throw new KettleStepException(BaseMessages.getString(PKG, "GISFileInputMeta.Exception.NoFilesFoundToProcess")); //$NON-NLS-1$

        GeotoolsReader gtr = null;
        try {
            gtr = new GeotoolsReader(files.getFile(0).getURL(), gisFileCharset);
            gtr.open();
            RowMetaInterface add = gtr.getFields();
            for (int i = 0; i < add.size(); i++) {
                ValueMetaInterface v = add.getValueMeta(i);
                v.setOrigin(name);
            }
            rowMeta.addRowMeta(add);
        } catch (Exception ke) {
            throw new KettleStepException(BaseMessages.getString(PKG, "GISFileInputMeta.Exception.UnableToReadMetaDataFromGISFile"), ke); //$NON-NLS-1$
        } finally {
            if (gtr != null) gtr.close();
        }

        if (rowNrAdded && rowNrField != null && rowNrField.length() > 0) {
            ValueMetaInterface rnr = new ValueMeta(rowNrField, ValueMetaInterface.TYPE_INTEGER);
            rnr.setOrigin(name);
            rowMeta.addValueMeta(rnr);
        }
        return rowMeta;
    }

    public String getXML() {
        StringBuffer retval = new StringBuffer();

        retval.append("    ").append(XMLHandler.addTagValue("filename", fileName));
        retval.append("    ").append(XMLHandler.addTagValue("isfilenameinfield", isFileNameInField));
        retval.append("    ").append(XMLHandler.addTagValue("filenamefield", fileNameField));
        retval.append("    " + XMLHandler.addTagValue("limit", rowLimit)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    " + XMLHandler.addTagValue("add_rownr", rowNrAdded)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    " + XMLHandler.addTagValue("field_rownr", rowNrField)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    " + XMLHandler.addTagValue("gis_file_charset", gisFileCharset)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    ").append(XMLHandler.addTagValue("passing_through_fields", passingThruFields));
        retval.append("    ").append(XMLHandler.addTagValue("accept_stepname", (acceptingStep != null ? acceptingStep.getName() : "")));

        return retval.toString();
    }

    public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
        try {
            fileName = rep.getStepAttributeString(id_step, "filename");
            isFileNameInField = rep.getStepAttributeBoolean(id_step, "isfilenameinfield");
            fileNameField = rep.getStepAttributeString(id_step, "filenamefield");
            rowLimit = (int) rep.getStepAttributeInteger(id_step, "limit"); //$NON-NLS-1$
            rowNrAdded = rep.getStepAttributeBoolean(id_step, "add_rownr"); //$NON-NLS-1$
            rowNrField = rep.getStepAttributeString(id_step, "field_rownr"); //$NON-NLS-1$
            gisFileCharset = rep.getStepAttributeString(id_step, "gis_file_charset"); //$NON-NLS-1$
            passingThruFields = rep.getStepAttributeBoolean(id_step, "passing_through_fields");
            acceptingStepName = rep.getStepAttributeString(id_step, "accept_stepname");
        } catch (Exception e) {
            throw new KettleException(BaseMessages.getString(PKG, "GISFileInputMeta.Exception.UnexpectedErrorReadingMetaDataFromRepository"), e); //$NON-NLS-1$
        }
    }

    public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
        try {
            rep.saveStepAttribute(id_transformation, id_step, "filenamefield", fileNameField);
            rep.saveStepAttribute(id_transformation, id_step, "filename", fileName);
            rep.saveStepAttribute(id_transformation, id_step, "isfilenameinfield", isFileNameInField);
            rep.saveStepAttribute(id_transformation, id_step, "limit", rowLimit); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "add_rownr", rowNrAdded); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "field_rownr", rowNrField); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "gis_file_charset", gisFileCharset); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "passing_through_fields", passingThruFields);
            rep.saveStepAttribute(id_transformation, id_step, "accept_stepname", (acceptingStep != null ? acceptingStep.getName() : ""));
        } catch (Exception e) {
            throw new KettleException(BaseMessages.getString(PKG, "GISFileInputMeta.Exception.UnableToSaveMetaDataToRepository") + id_step, e); //$NON-NLS-1$
        }
    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info) {
        CheckResult cr;

        if (!isFileNameInField) {
            if (fileName == null) {
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "GISFileInputMeta.Remark.PleaseSelectFileToUse"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            } else {
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "GISFileInputMeta.Remark.FileToUseIsSpecified"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);

                GeotoolsReader gtr = null;
                try {
                    gtr = new GeotoolsReader(getURLfromFileName(transMeta.environmentSubstitute(fileName)), gisFileCharset);
                    gtr.open();
                    cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "GISFileInputMeta.Remark.FileExistsAndCanBeOpened"), stepMeta); //$NON-NLS-1$
                    remarks.add(cr);

                    RowMetaInterface r = gtr.getFields();

                    cr = new CheckResult(CheckResult.TYPE_RESULT_OK, r.size() + BaseMessages.getString(PKG, "GISFileInputMeta.Remark.OutputFieldsCouldBeDetermined"), stepMeta); //$NON-NLS-1$
                    remarks.add(cr);
                } catch (KettleException ke) {
                    cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "GISFileInputMeta.Remark.NoFieldsCouldBeFoundInFileBecauseOfError") + Const.CR + ke.getMessage(), stepMeta); //$NON-NLS-1$
                    remarks.add(cr);
                } finally {
                    if (gtr != null) gtr.close();
                }
            }
        } else if (fileNameField == null) {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "GISFileInputMeta.Remark.PleaseSelectFileField"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        } else {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "GISFileInputMeta.Remark.FileToUseIsSpecified"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
            if (input.length > 0) {
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "GISFileInputMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            } else {
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "GISFileInputMeta.CheckResult.NoInpuReceived"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            }
        }
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans) {
        return new GISFileInput(stepMeta, stepDataInterface, cnr, tr, trans);
    }

    public StepDataInterface getStepData() {
        return new GISFileInputData();
    }

    public FileInputList getFileList(VariableSpace space) {
        return FileInputList.createFileList(space, new String[]{fileName}, new String[]{null}, new String[]{null}, new String[]{"N"});
    }

    private java.net.URL getURLfromFileName(String filename) {
        try {
            return (new java.io.File(filename)).toURI().toURL();
        } catch (java.net.MalformedURLException urle) {
            return null;
        }
    }

    public String getGisFileCharset() {
        return gisFileCharset;
    }

    public void setGisFileCharset(String gisFileCharset) {
        this.gisFileCharset = gisFileCharset;
    }
}