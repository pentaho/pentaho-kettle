/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans.steps.xmloutput;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceNamingInterface.FileNamingType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/**
 * This class knows how to handle the MetaData for the XML output step
 * 
 * @since 14-jan-2006
 * 
 */
public class XMLOutputMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = XMLOutputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  /** The base name of the output file */
  private String fileName;

  /** The file extention in case of a generated filename */
  private String extension;

  /**
   * if this value is larger then 0, the text file is split up into parts of
   * this number of lines
   */
  private int splitEvery;

  /** Flag: add the stepnr in the filename */
  private boolean stepNrInFilename;

  /** Flag: add the date in the filename */
  private boolean dateInFilename;

  /** Flag: add the time in the filename */
  private boolean timeInFilename;

  /** Flag: put the destination file in a zip archive */
  private boolean zipped;

  /**
   * The encoding to use for reading: null or empty string means system
   * default encoding
   */
  private String encoding;

  /**
   * The name space for the XML document: null or empty string means no 
   * xmlns is written 
   */
  private String nameSpace;

  /** The name of the parent XML element */
  private String mainElement;

  /** The name of the repeating row XML element */
  private String repeatElement;

  /** Flag: add the filenames to result filenames */
  private boolean addToResultFilenames;

  /* THE FIELD SPECIFICATIONS ... */

  /** The output fields */
  private XMLField outputFields[];

  /** Flag : Do not open new file when transformation start  */
  private boolean doNotOpenNewFileInit;

  /** Omit null elements from xml output*/
  private boolean omitNullValues;

  private boolean SpecifyFormat;

  private String date_time_format;

  public XMLOutputMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the dateInFilename.
   */
  public boolean isDateInFilename() {
    return dateInFilename;
  }

  /**
   * @param dateInFilename
   *            The dateInFilename to set.
   */
  public void setDateInFilename(boolean dateInFilename) {
    this.dateInFilename = dateInFilename;
  }

  /**
   * @return Returns the extension.
   */
  public String getExtension() {
    return extension;
  }

  /**
   * @param extension
   *            The extension to set.
   */
  public void setExtension(String extension) {
    this.extension = extension;
  }

  /**
   * @return Returns the "do not open new file at init" flag.
   */
  public boolean isDoNotOpenNewFileInit() {
    return doNotOpenNewFileInit;
  }

  /**
   * @param doNotOpenNewFileInit The "do not open new file at init" flag to set.
   */
  public void setDoNotOpenNewFileInit(boolean doNotOpenNewFileInit) {
    this.doNotOpenNewFileInit = doNotOpenNewFileInit;
  }

  /**
   * @return Returns the fileName.
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * @param fileName
   *            The fileName to set.
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /**
   * @return Returns the splitEvery.
   */
  public int getSplitEvery() {
    return splitEvery;
  }

  /**
   * @param splitEvery
   *            The splitEvery to set.
   */
  public void setSplitEvery(int splitEvery) {
    this.splitEvery = splitEvery;
  }

  /**
   * @return Returns the stepNrInFilename.
   */
  public boolean isStepNrInFilename() {
    return stepNrInFilename;
  }

  /**
   * @param stepNrInFilename
   *            The stepNrInFilename to set.
   */
  public void setStepNrInFilename(boolean stepNrInFilename) {
    this.stepNrInFilename = stepNrInFilename;
  }

  /**
   * @return Returns the timeInFilename.
   */
  public boolean isTimeInFilename() {
    return timeInFilename;
  }

  /**
   * @param timeInFilename
   *            The timeInFilename to set.
   */
  public void setTimeInFilename(boolean timeInFilename) {
    this.timeInFilename = timeInFilename;
  }

  /**
   * @return Returns the add to result filesname.
   */
  public boolean isAddToResultFiles() {
    return addToResultFilenames;
  }

  /**
   * @param addtoresultfilenamesin The addtoresultfilenames to set.
   */
  public void setAddToResultFiles(boolean addtoresultfilenamesin) {
    this.addToResultFilenames = addtoresultfilenamesin;
  }

  public boolean isSpecifyFormat() {
    return SpecifyFormat;
  }

  public void setSpecifyFormat(boolean SpecifyFormat) {
    this.SpecifyFormat = SpecifyFormat;
  }

  public String getDateTimeFormat() {
    return date_time_format;
  }

  public void setDateTimeFormat(String date_time_format) {
    this.date_time_format = date_time_format;
  }

  /**
   * @return Returns the zipped.
   */
  public boolean isZipped() {
    return zipped;
  }

  /**
   * @param zipped
   *            The zipped to set.
   */
  public void setZipped(boolean zipped) {
    this.zipped = zipped;
  }

  /**
   * @return Returns the outputFields.
   */
  public XMLField[] getOutputFields() {
    return outputFields;
  }

  /**
   * @param outputFields
   *            The outputFields to set.
   */
  public void setOutputFields(XMLField[] outputFields) {
    this.outputFields = outputFields;
  }

  public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
      throws KettleXMLException {
    readData(stepnode);
  }

  public void allocate(int nrfields) {
    outputFields = new XMLField[nrfields];
  }

  public Object clone() {
    XMLOutputMeta retval = (XMLOutputMeta) super.clone();
    int nrfields = outputFields.length;

    retval.allocate(nrfields);

    for (int i = 0; i < nrfields; i++) {
      retval.outputFields[i] = (XMLField) outputFields[i].clone();
    }

    return retval;
  }

  private void readData(Node stepnode) throws KettleXMLException {
    try {
      encoding = XMLHandler.getTagValue(stepnode, "encoding");
      nameSpace = XMLHandler.getTagValue(stepnode, "name_space");
      mainElement = XMLHandler.getTagValue(stepnode, "xml_main_element");
      repeatElement = XMLHandler.getTagValue(stepnode, "xml_repeat_element");

      fileName = XMLHandler.getTagValue(stepnode, "file", "name");
      extension = XMLHandler.getTagValue(stepnode, "file", "extention");

      doNotOpenNewFileInit = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "do_not_open_newfile_init"));
      stepNrInFilename = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "split"));
      dateInFilename = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "add_date"));
      timeInFilename = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "add_time"));
      SpecifyFormat = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "SpecifyFormat"));
      omitNullValues = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "omit_null_values"));
      date_time_format = XMLHandler.getTagValue(stepnode, "file", "date_time_format");

      addToResultFilenames = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "add_to_result_filenames"));

      zipped = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "zipped"));
      splitEvery = Const.toInt(XMLHandler.getTagValue(stepnode, "file", "splitevery"), 0);

      Node fields = XMLHandler.getSubNode(stepnode, "fields");
      int nrfields = XMLHandler.countNodes(fields, "field");

      allocate(nrfields);

      for (int i = 0; i < nrfields; i++) {
        Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);

        outputFields[i] = new XMLField();
        String fieldName = XMLHandler.getTagValue(fnode, "name");
        outputFields[i].setFieldName(fieldName);
        String elementName = XMLHandler.getTagValue(fnode, "element");
        outputFields[i].setElementName(elementName == null ? "" : elementName);
        outputFields[i].setType(XMLHandler.getTagValue(fnode, "type"));
        outputFields[i].setFormat(XMLHandler.getTagValue(fnode, "format"));
        outputFields[i].setCurrencySymbol(XMLHandler.getTagValue(fnode, "currency"));
        outputFields[i].setDecimalSymbol(XMLHandler.getTagValue(fnode, "decimal"));
        outputFields[i].setGroupingSymbol(XMLHandler.getTagValue(fnode, "group"));
        outputFields[i].setNullString(XMLHandler.getTagValue(fnode, "nullif"));
        outputFields[i].setLength(Const.toInt(XMLHandler.getTagValue(fnode, "length"), -1));
        outputFields[i].setPrecision(Const.toInt(XMLHandler.getTagValue(fnode, "precision"), -1));
      }
    } catch (Exception e) {
      throw new KettleXMLException("Unable to load step info from XML", e);
    }
  }

  public String getNewLine(String fformat) {
    String nl = System.getProperty("line.separator");

    if (fformat != null) {
      if (fformat.equalsIgnoreCase("DOS")) {
        nl = "\r\n";
      } else if (fformat.equalsIgnoreCase("UNIX")) {
        nl = "\n";
      }
    }

    return nl;
  }

  public void setDefault() {
    fileName = "file";
    extension = "xml";
    stepNrInFilename = false;
    doNotOpenNewFileInit = false;
    dateInFilename = false;
    timeInFilename = false;
    addToResultFilenames = false;
    zipped = false;
    splitEvery = 0;
    encoding = Const.XML_ENCODING;
    nameSpace = "";
    date_time_format = null;
    SpecifyFormat = false;
    omitNullValues = false;
    mainElement = "Rows";
    repeatElement = "Row";

    int nrfields = 0;

    allocate(nrfields);

    for (int i = 0; i < nrfields; i++) {
      outputFields[i] = new XMLField();

      outputFields[i].setFieldName("field" + i);
      outputFields[i].setElementName("field" + i);
      outputFields[i].setType("Number");
      outputFields[i].setFormat(" 0,000,000.00;-0,000,000.00");
      outputFields[i].setCurrencySymbol("");
      outputFields[i].setDecimalSymbol(",");
      outputFields[i].setGroupingSymbol(".");
      outputFields[i].setNullString("");
      outputFields[i].setLength(-1);
      outputFields[i].setPrecision(-1);
    }
  }

  public String[] getFiles(VariableSpace space) {
    int copies = 1;
    int splits = 1;

    if (stepNrInFilename) {
      copies = 3;
    }

    if (splitEvery != 0) {
      splits = 3;
    }

    int nr = copies * splits;
    if (nr > 1)
      nr++;

    String retval[] = new String[nr];

    int i = 0;
    for (int copy = 0; copy < copies; copy++) {
      for (int split = 0; split < splits; split++) {
        retval[i] = buildFilename(space, copy, split, false);
        i++;
      }
    }
    if (i < nr) {
      retval[i] = "...";
    }

    return retval;
  }

  public String buildFilename(VariableSpace space, int stepnr, int splitnr, boolean ziparchive) {
    SimpleDateFormat daf = new SimpleDateFormat();
    DecimalFormat df = new DecimalFormat("00000");

    // Replace possible environment variables...
    String retval = space.environmentSubstitute(fileName);
    String realextension = space.environmentSubstitute(extension);

    Date now = new Date();

    if (SpecifyFormat && !Const.isEmpty(date_time_format)) {
      daf.applyPattern(date_time_format);
      String dt = daf.format(now);
      retval += dt;
    } else {
      if (dateInFilename) {
        daf.applyPattern("yyyyMMdd");
        String d = daf.format(now);
        retval += "_" + d;
      }
      if (timeInFilename) {
        daf.applyPattern("HHmmss");
        String t = daf.format(now);
        retval += "_" + t;
      }
    }

    if (stepNrInFilename) {
      retval += "_" + stepnr;
    }
    if (splitEvery > 0) {
      retval += "_" + df.format(splitnr + 1);
    }

    if (zipped) {
      if (ziparchive) {
        retval += ".zip";
      } else {
        if (realextension != null && realextension.length() != 0) {
          retval += "." + realextension;
        }
      }
    } else {
      if (realextension != null && realextension.length() != 0) {
        retval += "." + realextension;
      }
    }
    return retval;
  }

  public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space) {

    // No values are added to the row in this type of step
    // However, in case of Fixed length records,
    // the field precisions and lengths are altered!

    for (int i = 0; i < outputFields.length; i++) {
      XMLField field = outputFields[i];
      ValueMetaInterface v = row.searchValueMeta(field.getFieldName());
      if (v != null) {
        v.setLength(field.getLength(), field.getPrecision());
      }
    }

  }

  public String getXML() {
    StringBuffer retval = new StringBuffer(600);

    retval.append("    ").append(XMLHandler.addTagValue("encoding", encoding));
    retval.append("    ").append(XMLHandler.addTagValue("name_space", nameSpace));
    retval.append("    ").append(XMLHandler.addTagValue("xml_main_element", mainElement));
    retval.append("    ").append(XMLHandler.addTagValue("xml_repeat_element", repeatElement));

    retval.append("    <file>").append(Const.CR);
    retval.append("      ").append(XMLHandler.addTagValue("name", fileName));
    retval.append("      ").append(XMLHandler.addTagValue("extention", extension));

    retval.append("      ").append(XMLHandler.addTagValue("do_not_open_newfile_init", doNotOpenNewFileInit));
    retval.append("      ").append(XMLHandler.addTagValue("split", stepNrInFilename));
    retval.append("      ").append(XMLHandler.addTagValue("add_date", dateInFilename));
    retval.append("      ").append(XMLHandler.addTagValue("add_time", timeInFilename));
    retval.append("      ").append(XMLHandler.addTagValue("SpecifyFormat", SpecifyFormat));
    retval.append("      ").append(XMLHandler.addTagValue("omit_null_values", omitNullValues));
    retval.append("      ").append(XMLHandler.addTagValue("date_time_format", date_time_format));
    retval.append("      ").append(XMLHandler.addTagValue("add_to_result_filenames", addToResultFilenames));
    retval.append("      ").append(XMLHandler.addTagValue("zipped", zipped));
    retval.append("      ").append(XMLHandler.addTagValue("splitevery", splitEvery));
    retval.append("    </file>").append(Const.CR);
    retval.append("    <fields>").append(Const.CR);
    for (int i = 0; i < outputFields.length; i++) {
      XMLField field = outputFields[i];

      if (field.getFieldName() != null && field.getFieldName().length() != 0) {
        retval.append("      <field>").append(Const.CR);
        retval.append("        ").append(XMLHandler.addTagValue("name", field.getFieldName()));
        retval.append("        ").append(XMLHandler.addTagValue("element", field.getElementName()));
        retval.append("        ").append(XMLHandler.addTagValue("type", field.getTypeDesc()));
        retval.append("        ").append(XMLHandler.addTagValue("format", field.getFormat()));
        retval.append("        ").append(XMLHandler.addTagValue("currency", field.getCurrencySymbol()));
        retval.append("        ").append(XMLHandler.addTagValue("decimal", field.getDecimalSymbol()));
        retval.append("        ").append(XMLHandler.addTagValue("group", field.getGroupingSymbol()));
        retval.append("        ").append(XMLHandler.addTagValue("nullif", field.getNullString()));
        retval.append("        ").append(XMLHandler.addTagValue("length", field.getLength()));
        retval.append("        ").append(XMLHandler.addTagValue("precision", field.getPrecision()));
        retval.append("      </field>").append(Const.CR);
      }
    }
    retval.append("    </fields>").append(Const.CR);

    return retval.toString();
  }

  public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
      throws KettleException {
    try {
      encoding = rep.getStepAttributeString(id_step, "encoding");
      nameSpace = rep.getStepAttributeString(id_step, "name_space");
      mainElement = rep.getStepAttributeString(id_step, "xml_main_element");
      repeatElement = rep.getStepAttributeString(id_step, "xml_repeat_element");

      fileName = rep.getStepAttributeString(id_step, "file_name");
      extension = rep.getStepAttributeString(id_step, "file_extention");

      doNotOpenNewFileInit = rep.getStepAttributeBoolean(id_step, "do_not_open_newfile_init");
      splitEvery = (int) rep.getStepAttributeInteger(id_step, "file_split");
      stepNrInFilename = rep.getStepAttributeBoolean(id_step, "file_add_stepnr");
      dateInFilename = rep.getStepAttributeBoolean(id_step, "file_add_date");
      timeInFilename = rep.getStepAttributeBoolean(id_step, "file_add_time");
      SpecifyFormat = rep.getStepAttributeBoolean(id_step, "SpecifyFormat");
      omitNullValues = rep.getStepAttributeBoolean(id_step, "omit_null_values");
      date_time_format = rep.getStepAttributeString(id_step, "date_time_format");

      addToResultFilenames = rep.getStepAttributeBoolean(id_step, "add_to_result_filenames");
      zipped = rep.getStepAttributeBoolean(id_step, "file_zipped");

      int nrfields = rep.countNrStepAttributes(id_step, "field_name");

      allocate(nrfields);

      for (int i = 0; i < nrfields; i++) {
        outputFields[i] = new XMLField();

        outputFields[i].setFieldName(rep.getStepAttributeString(id_step, i, "field_name"));
        outputFields[i].setElementName(rep.getStepAttributeString(id_step, i, "field_element"));
        outputFields[i].setType(rep.getStepAttributeString(id_step, i, "field_type"));
        outputFields[i].setFormat(rep.getStepAttributeString(id_step, i, "field_format"));
        outputFields[i].setCurrencySymbol(rep.getStepAttributeString(id_step, i, "field_currency"));
        outputFields[i].setDecimalSymbol(rep.getStepAttributeString(id_step, i, "field_decimal"));
        outputFields[i].setGroupingSymbol(rep.getStepAttributeString(id_step, i, "field_group"));
        outputFields[i].setNullString(rep.getStepAttributeString(id_step, i, "field_nullif"));
        outputFields[i].setLength((int) rep.getStepAttributeInteger(id_step, i, "field_length"));
        outputFields[i].setPrecision((int) rep.getStepAttributeInteger(id_step, i, "field_precision"));
      }
    } catch (Exception e) {
      throw new KettleException("Unexpected error reading step information from the repository", e);
    }
  }

  public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
    try {
      rep.saveStepAttribute(id_transformation, id_step, "encoding", encoding);
      rep.saveStepAttribute(id_transformation, id_step, "name_space", nameSpace);
      rep.saveStepAttribute(id_transformation, id_step, "xml_main_element", mainElement);
      rep.saveStepAttribute(id_transformation, id_step, "xml_repeat_element", repeatElement);
      rep.saveStepAttribute(id_transformation, id_step, "file_name", fileName);
      rep.saveStepAttribute(id_transformation, id_step, "file_extention", extension);

      rep.saveStepAttribute(id_transformation, id_step, "do_not_open_newfile_init", doNotOpenNewFileInit);
      rep.saveStepAttribute(id_transformation, id_step, "file_split", splitEvery);
      rep.saveStepAttribute(id_transformation, id_step, "file_add_stepnr", stepNrInFilename);
      rep.saveStepAttribute(id_transformation, id_step, "file_add_date", dateInFilename);
      rep.saveStepAttribute(id_transformation, id_step, "file_add_time", timeInFilename);
      rep.saveStepAttribute(id_transformation, id_step, "SpecifyFormat", SpecifyFormat);
      rep.saveStepAttribute(id_transformation, id_step, "omit_null_values", omitNullValues);
      rep.saveStepAttribute(id_transformation, id_step, "date_time_format", date_time_format);

      rep.saveStepAttribute(id_transformation, id_step, "add_to_result_filenames", addToResultFilenames);
      rep.saveStepAttribute(id_transformation, id_step, "file_zipped", zipped);

      for (int i = 0; i < outputFields.length; i++) {
        XMLField field = outputFields[i];

        rep.saveStepAttribute(id_transformation, id_step, i, "field_name", field.getFieldName());
        rep.saveStepAttribute(id_transformation, id_step, i, "field_element", field.getElementName());
        rep.saveStepAttribute(id_transformation, id_step, i, "field_type", field.getTypeDesc());
        rep.saveStepAttribute(id_transformation, id_step, i, "field_format", field.getFormat());
        rep.saveStepAttribute(id_transformation, id_step, i, "field_currency", field.getCurrencySymbol());
        rep.saveStepAttribute(id_transformation, id_step, i, "field_decimal", field.getDecimalSymbol());
        rep.saveStepAttribute(id_transformation, id_step, i, "field_group", field.getGroupingSymbol());
        rep.saveStepAttribute(id_transformation, id_step, i, "field_nullif", field.getNullString());
        rep.saveStepAttribute(id_transformation, id_step, i, "field_length", field.getLength());
        rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", field.getPrecision());
      }
    } catch (Exception e) {
      throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
    }
  }

  public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev,
      String input[], String output[], RowMetaInterface info) {
    CheckResult cr;

    // Check output fields
    if (prev != null && prev.size() > 0) {
      cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "XMLOutputMeta.CheckResult.FieldsReceived", "" + prev.size()), stepinfo);
      remarks.add(cr);

      String error_message = "";
      boolean error_found = false;

      // Starting from selected fields in ...
      for (int i = 0; i < outputFields.length; i++) {
        int idx = prev.indexOfValue(outputFields[i].getFieldName());
        if (idx < 0) {
          error_message += "\t\t" + outputFields[i].getFieldName() + Const.CR;
          error_found = true;
        }
      }
      if (error_found) {
        error_message = BaseMessages.getString(PKG, "XMLOutputMeta.CheckResult.FieldsNotFound", error_message);
        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
        remarks.add(cr);
      } else {
        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "XMLOutputMeta.CheckResult.AllFieldsFound"), stepinfo);
        remarks.add(cr);
      }
    }

    // See if we have input streams leading to this step!
    if (input.length > 0) {
      cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "XMLOutputMeta.CheckResult.ExpectedInputOk"), stepinfo);
      remarks.add(cr);
    } else {
      cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "XMLOutputMeta.CheckResult.ExpectedInputError"), stepinfo);
      remarks.add(cr);
    }

    cr = new CheckResult(CheckResultInterface.TYPE_RESULT_COMMENT, BaseMessages.getString(PKG, "XMLOutputMeta.CheckResult.FilesNotChecked"), stepinfo);
    remarks.add(cr);
  }

  public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
      Trans trans) {
    return new XMLOutput(stepMeta, stepDataInterface, cnr, transMeta, trans);
  }

  public StepDataInterface getStepData() {
    return new XMLOutputData();
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  /**
   * @return Returns the mainElement.
   */
  public String getMainElement() {
    return mainElement;
  }

  /**
   * @param mainElement
   *            The mainElement to set.
   */
  public void setMainElement(String mainElement) {
    this.mainElement = mainElement;
  }

  /**
   * @return Returns the repeatElement.
   */
  public String getRepeatElement() {
    return repeatElement;
  }

  /**
   * @param repeatElement
   *            The repeatElement to set.
   */
  public void setRepeatElement(String repeatElement) {
    this.repeatElement = repeatElement;
  }

  /**
   * @return Returns the nameSpace.
   */
  public String getNameSpace() {
    return nameSpace;
  }

  /**
   * @param nameSpace
   *            The nameSpace to set.
   */
  public void setNameSpace(String nameSpace) {
    this.nameSpace = nameSpace;
  }

  public void setOmitNullValues(boolean omitNullValues) {

    this.omitNullValues = omitNullValues;

  }

  public boolean isOmitNullValues() {

    return omitNullValues;

  }

  /**
   * Since the exported transformation that runs this will reside in a ZIP file, we can't reference files relatively.
   * So what this does is turn the name of the base path into an absolute path.
   */
  public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions,
      ResourceNamingInterface resourceNamingInterface, Repository repository) throws KettleException {
    try {
      // The object that we're modifying here is a copy of the original!
      // So let's change the filename from relative to absolute by grabbing the file object...
      // 
      if (!Const.isEmpty(fileName)) {
        FileObject fileObject = KettleVFS.getFileObject(space.environmentSubstitute(fileName), space);
        fileName = resourceNamingInterface.nameResource(fileObject.getName().getBaseName(), fileObject.getParent()
            .getName().getPath(), space.toString(), FileNamingType.DATA_FILE);
      }

      return null;
    } catch (Exception e) {
      throw new KettleException(e); //$NON-NLS-1$
    }
  }

}