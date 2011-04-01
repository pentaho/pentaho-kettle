/**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

package org.pentaho.di.trans.steps.pentahoreporting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
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
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/*
 * Created on 4-apr-2003
 *
 */

/**
 * 
 */
@Step(
    id = "PentahoReportingOutput", 
    image = "org/pentaho/reporting/images/JFR.png", 
    description = "PentahoReportingOutput.Description", 
    name = "PentahoReportingOutput.Name", 
    categoryDescription = "PentahoReportingOutput.Category", 
    i18nPackageName = "org.pentaho.reporting.plugin"
)
public class PentahoReportingOutputMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = PentahoReportingOutput.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  public enum ProcessorType {
    PDF("PDF", "PDF"), 
    PagedHTML("PagedHtml", "Paged HTML"), 
    StreamingHTML("StreamingHtml", "Streaming HTML"), 
    CSV("CSV", "CSV"), 
    Excel("Excel", "Excel"),
    RTF("RTF", "RTF"),
    ;

    private String code;
    private String description;

    private ProcessorType(String code, String description) {
      this.code = code;
      this.description = description;
    }

    public String getCode() {
      return code;
    }

    public String getDescription() {
      return description;
    }
    
    public static String[] getDescriptions() {
      String[] desc = new String[values().length];
      for (int i=0;i<values().length;i++) {
        desc[i] = values()[i].getDescription();
      }
      return desc;
    }

    public static ProcessorType getProcessorTypeByCode(String code) {
      for (ProcessorType type : values()) {
        if (type.getCode().equals(code))
          return type;
      }
      return null;
    }
  }
 
  public static final String XML_TAG_PARAMETERS = "parameters";
  public static final String XML_TAG_PARAMETER = "parameter";
  

  private String              inputFileField;
  private String              outputFileField;
  private Map<String, String> parameterFieldMap;

  private ProcessorType       outputProcessorType;

  public PentahoReportingOutputMeta() {
    super(); // allocate BaseStepMeta
    parameterFieldMap = new HashMap<String, String>();
  }

  public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
    readData(stepnode);
  }

  public Object clone() {
    PentahoReportingOutputMeta retval = (PentahoReportingOutputMeta) super.clone();

    return retval;
  }

  private void readData(Node stepnode) throws KettleXMLException {
    try {
      inputFileField = XMLHandler.getTagValue(stepnode, "input_file_field");
      outputFileField = XMLHandler.getTagValue(stepnode, "output_file_field");
      parameterFieldMap = new HashMap<String, String>();
      Node parsNode = XMLHandler.getSubNode(stepnode, XML_TAG_PARAMETERS);
      List<Node> nodes = XMLHandler.getNodes(parsNode, XML_TAG_PARAMETER);
      for (Node node : nodes) {
        String parameter = XMLHandler.getTagValue(node, "name");
        String fieldname = XMLHandler.getTagValue(node, "field");
        if (!Const.isEmpty(parameter) && !Const.isEmpty(fieldname)) {
          parameterFieldMap.put(parameter, fieldname);
        }
      }
      
      outputProcessorType = ProcessorType.getProcessorTypeByCode(XMLHandler.getTagValue(stepnode, "processor_type")); //$NON-NLS-1$
    } catch (Exception e) {
      throw new KettleXMLException(BaseMessages.getString(PKG, "PentahoReportingOutputMeta.Exception.UnableToLoadStepInfo"), e); //$NON-NLS-1$
    }
  }

  public void setDefault() {
    outputProcessorType = ProcessorType.PDF;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer();

    retval.append("  " + XMLHandler.addTagValue("input_file_field", inputFileField)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("  " + XMLHandler.addTagValue("output_file_field", outputFileField)); //$NON-NLS-1$ //$NON-NLS-2$

    retval.append("  " + XMLHandler.openTag(XML_TAG_PARAMETERS)); //$NON-NLS-1$ //$NON-NLS-2$
    List<String> parameters = new ArrayList<String>();
    parameters.addAll(parameterFieldMap.keySet());
    Collections.sort(parameters);
    for (String name : parameters) {
      String field = parameterFieldMap.get(name);
      retval.append("   " + XMLHandler.openTag(XML_TAG_PARAMETER)); //$NON-NLS-1$ //$NON-NLS-2$
      retval.append("   " + XMLHandler.addTagValue("name", name, false)); //$NON-NLS-1$ //$NON-NLS-2$
      retval.append("   " + XMLHandler.addTagValue("field", field, false)); //$NON-NLS-1$ //$NON-NLS-2$
      retval.append("   " + XMLHandler.closeTag(XML_TAG_PARAMETER)).append(Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
    }
    retval.append("  " + XMLHandler.closeTag(XML_TAG_PARAMETERS)); //$NON-NLS-1$ //$NON-NLS-2$

    retval.append("    " + XMLHandler.addTagValue("processor_type", outputProcessorType.getCode())); //$NON-NLS-1$

    return retval.toString();
  }

  public void readRep(Repository rep, ObjectId idStep, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
    try {
      inputFileField = rep.getStepAttributeString(idStep, "file_input_field"); //$NON-NLS-1$
      outputFileField = rep.getStepAttributeString(idStep, "file_output_field"); //$NON-NLS-1$
      
      parameterFieldMap = new HashMap<String, String>();
      int nrParameters = rep.countNrStepAttributes(idStep, "parameter_name");
      for (int i=0;i<nrParameters;i++) {
        String parameter = rep.getStepAttributeString(idStep, i, "parameter_name");
        String fieldname = rep.getStepAttributeString(idStep, i, "parameter_field");
        if (!Const.isEmpty(parameter) && !Const.isEmpty(fieldname)) {
          parameterFieldMap.put(parameter, fieldname);
        }
      }
      
      outputProcessorType = ProcessorType.getProcessorTypeByCode( rep.getStepAttributeString(idStep, "processor_type") ); //$NON-NLS-1$
    } catch (Exception e) {
      throw new KettleException(BaseMessages.getString(PKG, "PentahoReportingOutputMeta.Exception.UnexpectedErrorInReadingStepInfo"), e); //$NON-NLS-1$
    }
  }

  public void saveRep(Repository rep, ObjectId idTransformation, ObjectId idStep) throws KettleException {
    try {
      rep.saveStepAttribute(idTransformation, idStep, "file_input_field", inputFileField); //$NON-NLS-1$
      rep.saveStepAttribute(idTransformation, idStep, "file_output_field", outputFileField); //$NON-NLS-1$

      List<String> pars = new ArrayList<String>(parameterFieldMap.keySet());
      Collections.sort(pars);
      for (int i=0;i<pars.size();i++) {
        String parameter = pars.get(i);
        String fieldname = parameterFieldMap.get(parameter);
        rep.saveStepAttribute(idTransformation, idStep, i, "parameter_name", parameter);
        rep.saveStepAttribute(idTransformation, idStep, i, "parameter_field", fieldname);
      }

      rep.saveStepAttribute(idTransformation, idStep, "processor_type", outputProcessorType.getCode()); //$NON-NLS-1$
      
    } catch (Exception e) {
      throw new KettleException(BaseMessages.getString(PKG, "PentahoReportingOutputMeta.Exception.UnableToSaveStepInfo") + idStep, e); //$NON-NLS-1$
    }
  }

  public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info) {
    CheckResult cr;

    // Check output fields
    if (prev != null && prev.size() > 0) {
      cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "PentahoReportingOutputMeta.CheckResult.ReceivingFields", String.valueOf(prev.size())), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
      remarks.add(cr);
    }

    cr = new CheckResult(CheckResult.TYPE_RESULT_COMMENT, BaseMessages.getString(PKG, "PentahoReportingOutputMeta.CheckResult.FileSpecificationsNotChecked"), stepMeta); //$NON-NLS-1$
    remarks.add(cr);
  }

  public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans) {
    return new PentahoReportingOutput(stepMeta, stepDataInterface, cnr, transMeta, trans);
  }

  public StepDataInterface getStepData() {
    return new PentahoReportingOutputData();
  }

  /**
   * @return the inputFileField
   */
  public String getInputFileField() {
    return inputFileField;
  }

  /**
   * @param inputFileField the inputFileField to set
   */
  public void setInputFileField(String inputFileField) {
    this.inputFileField = inputFileField;
  }

  /**
   * @return the outputFileField
   */
  public String getOutputFileField() {
    return outputFileField;
  }

  /**
   * @param outputFileField the outputFileField to set
   */
  public void setOutputFileField(String outputFileField) {
    this.outputFileField = outputFileField;
  }

  /**
   * @return the parameterFieldMap
   */
  public Map<String, String> getParameterFieldMap() {
    return parameterFieldMap;
  }

  /**
   * @param parameterFieldMap the parameterFieldMap to set
   */
  public void setParameterFieldMap(Map<String, String> parameterFieldMap) {
    this.parameterFieldMap = parameterFieldMap;
  }

  /**
   * @return the outputProcessorType
   */
  public ProcessorType getOutputProcessorType() {
    return outputProcessorType;
  }

  /**
   * @param outputProcessorType the outputProcessorType to set
   */
  public void setOutputProcessorType(ProcessorType outputProcessorType) {
    this.outputProcessorType = outputProcessorType;
  }
}
