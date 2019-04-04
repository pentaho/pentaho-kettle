/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.pentahoreporting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
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
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * Created on 4-apr-2003
 *
 */
@InjectionSupported( localizationPrefix = "PentahoReportingOutputMeta.Injection.", groups = { "PARAMETERS" } )
public class PentahoReportingOutputMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = PentahoReportingOutput.class; // for i18n purposes, needed by Translator2!!

  public enum ProcessorType {
    PDF( "PDF", "PDF" ), PagedHTML( "PagedHtml", "Paged HTML" ),
      StreamingHTML( "StreamingHtml", "Streaming HTML" ), CSV( "CSV", "CSV" ), Excel( "Excel", "Excel" ),
      Excel_2007( "Excel 2007", "Excel 2007" ), RTF( "RTF", "RTF" );

    private String code;
    private String description;

    private ProcessorType( String code, String description ) {
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
      for ( int i = 0; i < values().length; i++ ) {
        desc[i] = values()[i].getDescription();
      }
      return desc;
    }

    public static ProcessorType getProcessorTypeByCode( String code ) {
      for ( ProcessorType type : values() ) {
        if ( type.getCode().equals( code ) ) {
          return type;
        }
      }
      return null;
    }
  }

  public static final String XML_TAG_PARAMETERS = "parameters";
  public static final String XML_TAG_PARAMETER = "parameter";

  @Injection( name = "INPUT_FILE_FIELD" )
  private String inputFileField;
  @Injection( name = "OUTPUT_FILE_FIELD" )
  private String outputFileField;
  private Map<String, String> parameterFieldMap;

  @Injection( name = "OUTPUT_PROCESSOR_TYPE" )
  private ProcessorType outputProcessorType;

  @InjectionDeep
  private Param[] params;

  @Injection( name = "CREATE_PARENT_FOLDER" )
  private Boolean createParentfolder;

  public PentahoReportingOutputMeta() {
    super(); // allocate BaseStepMeta
    parameterFieldMap = new HashMap<String, String>();
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public Object clone() {
    PentahoReportingOutputMeta retval = (PentahoReportingOutputMeta) super.clone();

    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      inputFileField = XMLHandler.getTagValue( stepnode, "input_file_field" );
      outputFileField = XMLHandler.getTagValue( stepnode, "output_file_field" );
      createParentfolder = "Y".equals( XMLHandler.getTagValue( stepnode, "create_parent_folder" ) );
      parameterFieldMap = new HashMap<String, String>();
      Node parsNode = XMLHandler.getSubNode( stepnode, XML_TAG_PARAMETERS );
      List<Node> nodes = XMLHandler.getNodes( parsNode, XML_TAG_PARAMETER );
      for ( Node node : nodes ) {
        String parameter = XMLHandler.getTagValue( node, "name" );
        String fieldname = XMLHandler.getTagValue( node, "field" );
        if ( !Utils.isEmpty( parameter ) && !Utils.isEmpty( fieldname ) ) {
          parameterFieldMap.put( parameter, fieldname );
        }
      }

      outputProcessorType =
        ProcessorType.getProcessorTypeByCode( XMLHandler.getTagValue( stepnode, "processor_type" ) );
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "PentahoReportingOutputMeta.Exception.UnableToLoadStepInfo" ), e );
    }
  }

  public void setDefault() {
    outputProcessorType = ProcessorType.PDF;
    createParentfolder = false;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "  " + XMLHandler.addTagValue( "input_file_field", inputFileField ) );
    retval.append( "  " + XMLHandler.addTagValue( "output_file_field", outputFileField ) );
    retval.append( "  " + XMLHandler.addTagValue( "create_parent_folder", createParentfolder ) );
    retval.append( "  " + XMLHandler.openTag( XML_TAG_PARAMETERS ) );
    List<String> parameters = new ArrayList<String>();
    parameters.addAll( parameterFieldMap.keySet() );
    Collections.sort( parameters );
    for ( String name : parameters ) {
      String field = parameterFieldMap.get( name );
      retval.append( "   " + XMLHandler.openTag( XML_TAG_PARAMETER ) );
      retval.append( "   " + XMLHandler.addTagValue( "name", name, false ) );
      retval.append( "   " + XMLHandler.addTagValue( "field", field, false ) );
      retval.append( "   " + XMLHandler.closeTag( XML_TAG_PARAMETER ) ).append( Const.CR );
    }
    retval.append( "  " + XMLHandler.closeTag( XML_TAG_PARAMETERS ) );

    retval.append( "    " + XMLHandler.addTagValue( "processor_type", outputProcessorType.getCode() ) );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId idStep, List<DatabaseMeta> databases ) throws KettleException {
    try {
      inputFileField = rep.getStepAttributeString( idStep, "file_input_field" );
      outputFileField = rep.getStepAttributeString( idStep, "file_output_field" );
      createParentfolder = rep.getStepAttributeBoolean( idStep, "create_parent_folder" );
      parameterFieldMap = new HashMap<String, String>();
      int nrParameters = rep.countNrStepAttributes( idStep, "parameter_name" );
      for ( int i = 0; i < nrParameters; i++ ) {
        String parameter = rep.getStepAttributeString( idStep, i, "parameter_name" );
        String fieldname = rep.getStepAttributeString( idStep, i, "parameter_field" );
        if ( !Utils.isEmpty( parameter ) && !Utils.isEmpty( fieldname ) ) {
          parameterFieldMap.put( parameter, fieldname );
        }
      }

      outputProcessorType =
        ProcessorType.getProcessorTypeByCode( rep.getStepAttributeString( idStep, "processor_type" ) );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "PentahoReportingOutputMeta.Exception.UnexpectedErrorInReadingStepInfo" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId idTransformation, ObjectId idStep ) throws KettleException {
    try {
      rep.saveStepAttribute( idTransformation, idStep, "file_input_field", inputFileField );
      rep.saveStepAttribute( idTransformation, idStep, "file_output_field", outputFileField );
      rep.saveStepAttribute( idTransformation, idStep, "create_parent_folder", createParentfolder );
      List<String> pars = new ArrayList<String>( parameterFieldMap.keySet() );
      Collections.sort( pars );
      for ( int i = 0; i < pars.size(); i++ ) {
        String parameter = pars.get( i );
        String fieldname = parameterFieldMap.get( parameter );
        rep.saveStepAttribute( idTransformation, idStep, i, "parameter_name", parameter );
        rep.saveStepAttribute( idTransformation, idStep, i, "parameter_field", fieldname );
      }

      rep.saveStepAttribute( idTransformation, idStep, "processor_type", outputProcessorType.getCode() );

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "PentahoReportingOutputMeta.Exception.UnableToSaveStepInfo" )
        + idStep, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    // Check output fields
    if ( prev != null && prev.size() > 0 ) {
      cr =
        new CheckResult(
          CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "PentahoReportingOutputMeta.CheckResult.ReceivingFields", String.valueOf( prev.size() ) ),
          stepMeta );
      remarks.add( cr );
    }

    cr =
      new CheckResult( CheckResult.TYPE_RESULT_COMMENT, BaseMessages.getString(
        PKG, "PentahoReportingOutputMeta.CheckResult.FileSpecificationsNotChecked" ), stepMeta );
    remarks.add( cr );
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new PentahoReportingOutput( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new PentahoReportingOutputData();
  }

  /**
  * @return the createParentfolder
   */
  public Boolean getCreateParentfolder() {
    return createParentfolder;
  }
  /**
   * @param createParentfolder
   *          the createParentfolder to set
   */
  public void setCreateParentfolder( Boolean createParentfolder ) {
    this.createParentfolder = createParentfolder;
  }

  /**
   * @return the inputFileField
   */
  public String getInputFileField() {
    return inputFileField;
  }

  /**
   * @param inputFileField
   *          the inputFileField to set
   */
  public void setInputFileField( String inputFileField ) {
    this.inputFileField = inputFileField;
  }

  /**
   * @return the outputFileField
   */
  public String getOutputFileField() {
    return outputFileField;
  }

  /**
   * @param outputFileField
   *          the outputFileField to set
   */
  public void setOutputFileField( String outputFileField ) {
    this.outputFileField = outputFileField;
  }

  /**
   * @return the parameterFieldMap
   */
  public Map<String, String> getParameterFieldMap() {
    return parameterFieldMap;
  }

  /**
   * @param parameterFieldMap
   *          the parameterFieldMap to set
   */
  public void setParameterFieldMap( Map<String, String> parameterFieldMap ) {
    this.parameterFieldMap = parameterFieldMap;
  }

  /**
   * @return the outputProcessorType
   */
  public ProcessorType getOutputProcessorType() {
    return outputProcessorType;
  }

  /**
   * @param outputProcessorType
   *          the outputProcessorType to set
   */
  public void setOutputProcessorType( ProcessorType outputProcessorType ) {
    this.outputProcessorType = outputProcessorType;
  }

  /**
   * Initializer for one parameter.
   */
  public class Param {

    private String parameter;

    private String field;

    @Injection( name = "PARAMETER_NAME", group = "PARAMETERS" )
    public void setParameter( String value ) {
      parameter = value;
      if ( parameter != null && field != null ) {
        setup();
      }
    }

    @Injection( name = "FIELDNAME", group = "PARAMETERS" )
    public void setField( String value ) {
      field = value;
      if ( parameter != null && field != null ) {
        setup();
      }
    }

    private void setup() {
      parameterFieldMap.put( parameter, field );
    }
  }
}
