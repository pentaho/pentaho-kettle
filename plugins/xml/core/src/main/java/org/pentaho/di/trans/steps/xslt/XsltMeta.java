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

package org.pentaho.di.trans.steps.xslt;

import java.io.File;
import java.util.List;

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
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
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/*
 * Created on 15-Oct-2007
 *
 */
@Step( id = "XSLT", image = "XSLT.svg", i18nPackageName = "org.pentaho.di.trans.steps.xslt", name = "XSLT.name",
    description = "XSLT.description", categoryDescription = "XSLT.category",
    documentationUrl = "http://wiki.pentaho.com/display/EAI/XSL+Transformation" )
public class XsltMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = XsltMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String[] outputProperties = new String[] { "method", "version", "encoding", "standalone",
    "indent", "omit-xml-declaration", "doctype-public", "doctype-system", "media-type" };

  private String xslFilename;
  private String fieldName;
  private String resultFieldname;
  private String xslFileField;
  private boolean xslFileFieldUse;
  private boolean xslFieldIsAFile;
  private String xslFactory;

  /** output property name */
  private String[] outputPropertyName;

  /** output property value */
  private String[] outputPropertyValue;

  /** parameter name */
  private String[] parameterName;

  /** parameter field */
  private String[] parameterField;

  public XsltMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the parameterName.
   */
  public String[] getParameterName() {
    return parameterName;
  }

  /**
   * @param argumentDirection
   *          The parameterName to set.
   */
  public void setParameterName( String[] argumentDirection ) {
    this.parameterName = argumentDirection;
  }

  /**
   * @return Returns the parameterField.
   */
  public String[] getParameterField() {
    return parameterField;
  }

  /**
   * @param argumentDirection
   *          The parameterField to set.
   */
  public void setParameterField( String[] argumentDirection ) {
    this.parameterField = argumentDirection;
  }

  /**
   * @return Returns the XSL filename.
   */
  public String getXslFilename() {
    return xslFilename;
  }

  /**
   * @return Returns the OutputPropertyName.
   */
  public String[] getOutputPropertyName() {
    return outputPropertyName;
  }

  /**
   * @param argumentDirection
   *          The OutputPropertyName to set.
   */
  public void setOutputPropertyName( String[] argumentDirection ) {
    this.outputPropertyName = argumentDirection;
  }

  /**
   * @return Returns the OutputPropertyField.
   */
  public String[] getOutputPropertyValue() {
    return outputPropertyValue;
  }

  /**
   * @param argumentDirection
   *          The outputPropertyValue to set.
   */
  public void setOutputPropertyValue( String[] argumentDirection ) {
    this.outputPropertyValue = argumentDirection;
  }

  public void setXSLFileField( String xslfilefieldin ) {
    xslFileField = xslfilefieldin;
  }

  public void setXSLFactory( String xslfactoryin ) {
    xslFactory = xslfactoryin;
  }

  /**
   * @return Returns the XSL factory type.
   */
  public String getXSLFactory() {
    return xslFactory;
  }

  public String getXSLFileField() {
    return xslFileField;
  }

  public String getResultfieldname() {
    return resultFieldname;
  }

  public String getFieldname() {
    return fieldName;
  }

  /**
   * @param xslFilename
   *          The Xsl filename to set.
   */
  public void setXslFilename( String xslFilename ) {
    this.xslFilename = xslFilename;
  }

  public void setResultfieldname( String resultfield ) {
    this.resultFieldname = resultfield;
  }

  public void setFieldname( String fieldnamein ) {
    this.fieldName = fieldnamein;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public void allocate( int nrParameters, int outputProps ) {
    parameterName = new String[nrParameters];
    parameterField = new String[nrParameters];

    outputPropertyName = new String[outputProps];
    outputPropertyValue = new String[outputProps];
  }

  public Object clone() {
    XsltMeta retval = (XsltMeta) super.clone();
    int nrparams = parameterName.length;
    int nroutputprops = outputPropertyName.length;
    retval.allocate( nrparams, nroutputprops );

    for ( int i = 0; i < nrparams; i++ ) {
      retval.parameterName[i] = parameterName[i];
      retval.parameterField[i] = parameterField[i];
    }
    for ( int i = 0; i < nroutputprops; i++ ) {
      retval.outputPropertyName[i] = outputPropertyName[i];
      retval.outputPropertyValue[i] = outputPropertyValue[i];
    }

    return retval;
  }

  public boolean useXSLField() {
    return xslFileFieldUse;
  }

  public void setXSLField( boolean value ) {
    this.xslFileFieldUse = value;
  }

  public void setXSLFieldIsAFile( boolean xslFieldisAFile ) {
    this.xslFieldIsAFile = xslFieldisAFile;
  }

  public boolean isXSLFieldIsAFile() {
    return xslFieldIsAFile;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      xslFilename = XMLHandler.getTagValue( stepnode, "xslfilename" );
      fieldName = XMLHandler.getTagValue( stepnode, "fieldname" );
      resultFieldname = XMLHandler.getTagValue( stepnode, "resultfieldname" );
      xslFileField = XMLHandler.getTagValue( stepnode, "xslfilefield" );
      xslFileFieldUse = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "xslfilefielduse" ) );
      String isAFile = XMLHandler.getTagValue( stepnode, "xslfieldisafile" );
      if ( xslFileFieldUse && Utils.isEmpty( isAFile ) ) {
        xslFieldIsAFile = true;
      } else {
        xslFieldIsAFile = "Y".equalsIgnoreCase( isAFile );
      }

      xslFactory = XMLHandler.getTagValue( stepnode, "xslfactory" );

      Node parametersNode = XMLHandler.getSubNode( stepnode, "parameters" );
      int nrparams = XMLHandler.countNodes( parametersNode, "parameter" );

      Node parametersOutputProps = XMLHandler.getSubNode( stepnode, "outputproperties" );
      int nroutputprops = XMLHandler.countNodes( parametersOutputProps, "outputproperty" );
      allocate( nrparams, nroutputprops );

      for ( int i = 0; i < nrparams; i++ ) {
        Node anode = XMLHandler.getSubNodeByNr( parametersNode, "parameter", i );
        parameterField[i] = XMLHandler.getTagValue( anode, "field" );
        parameterName[i] = XMLHandler.getTagValue( anode, "name" );
      }
      for ( int i = 0; i < nroutputprops; i++ ) {
        Node anode = XMLHandler.getSubNodeByNr( parametersOutputProps, "outputproperty", i );
        outputPropertyName[i] = XMLHandler.getTagValue( anode, "name" );
        outputPropertyValue[i] = XMLHandler.getTagValue( anode, "value" );
      }

    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "XsltMeta.Exception.UnableToLoadStepInfoFromXML" ), e );
    }
  }

  public void setDefault() {
    xslFilename = null;
    fieldName = null;
    resultFieldname = "result";
    xslFactory = "JAXP";
    xslFileField = null;
    xslFileFieldUse = false;
    xslFieldIsAFile = true;

    int nrparams = 0;
    int nroutputproperties = 0;
    allocate( nrparams, nroutputproperties );

    for ( int i = 0; i < nrparams; i++ ) {
      parameterField[i] = "param" + i;
      parameterName[i] = "param";
    }
    for ( int i = 0; i < nroutputproperties; i++ ) {
      outputPropertyName[i] = "outputprop" + i;
      outputPropertyValue[i] = "outputprop";
    }
  }

  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // Output field (String)
    ValueMetaInterface v = new ValueMeta( space.environmentSubstitute( getResultfieldname() ), ValueMeta.TYPE_STRING );
    v.setOrigin( name );
    inputRowMeta.addValueMeta( v );
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer();

    retval.append( "    " + XMLHandler.addTagValue( "xslfilename", xslFilename ) );
    retval.append( "    " + XMLHandler.addTagValue( "fieldname", fieldName ) );
    retval.append( "    " + XMLHandler.addTagValue( "resultfieldname", resultFieldname ) );
    retval.append( "    " + XMLHandler.addTagValue( "xslfilefield", xslFileField ) );
    retval.append( "    " + XMLHandler.addTagValue( "xslfilefielduse", xslFileFieldUse ) );
    retval.append( "    " + XMLHandler.addTagValue( "xslfieldisafile", xslFieldIsAFile ) );

    retval.append( "    " + XMLHandler.addTagValue( "xslfactory", xslFactory ) );
    retval.append( "    <parameters>" ).append( Const.CR );

    for ( int i = 0; i < parameterName.length; i++ ) {
      retval.append( "      <parameter>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "field", parameterField[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", parameterName[i] ) );
      retval.append( "      </parameter>" ).append( Const.CR );
    }

    retval.append( "    </parameters>" ).append( Const.CR );
    retval.append( "    <outputproperties>" ).append( Const.CR );

    for ( int i = 0; i < outputPropertyName.length; i++ ) {
      retval.append( "      <outputproperty>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", outputPropertyName[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "value", outputPropertyValue[i] ) );
      retval.append( "      </outputproperty>" ).append( Const.CR );
    }

    retval.append( "    </outputproperties>" ).append( Const.CR );
    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      xslFilename = rep.getStepAttributeString( id_step, "xslfilename" );
      fieldName = rep.getStepAttributeString( id_step, "fieldname" );
      resultFieldname = rep.getStepAttributeString( id_step, "resultfieldname" );
      xslFileField = rep.getStepAttributeString( id_step, "xslfilefield" );
      xslFileFieldUse = rep.getStepAttributeBoolean( id_step, "xslfilefielduse" );
      String isAfile = rep.getStepAttributeString( id_step, "xslfieldisafile" );
      if ( xslFileFieldUse && Utils.isEmpty( isAfile ) ) {
        xslFieldIsAFile = true;
      } else {
        xslFieldIsAFile = "true".equalsIgnoreCase( isAfile );
      }
      xslFactory = rep.getStepAttributeString( id_step, "xslfactory" );

      int nrparams = rep.countNrStepAttributes( id_step, "param_name" );
      int nroutputprops = rep.countNrStepAttributes( id_step, "output_property_name" );
      allocate( nrparams, nroutputprops );

      for ( int i = 0; i < nrparams; i++ ) {
        parameterField[i] = rep.getStepAttributeString( id_step, i, "param_field" );
        parameterName[i] = rep.getStepAttributeString( id_step, i, "param_name" );
      }
      for ( int i = 0; i < nroutputprops; i++ ) {
        outputPropertyName[i] = rep.getStepAttributeString( id_step, i, "output_property_name" );
        outputPropertyValue[i] = rep.getStepAttributeString( id_step, i, "output_property_value" );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "XsltMeta.Exception.UnexpectedErrorInReadingStepInfo" ),
          e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "xslfilename", xslFilename );
      rep.saveStepAttribute( id_transformation, id_step, "fieldname", fieldName );
      rep.saveStepAttribute( id_transformation, id_step, "resultfieldname", resultFieldname );

      rep.saveStepAttribute( id_transformation, id_step, "xslfilefield", xslFileField );

      rep.saveStepAttribute( id_transformation, id_step, "xslfilefielduse", xslFileFieldUse );
      rep.saveStepAttribute( id_transformation, id_step, "xslfieldisafile", xslFieldIsAFile );

      rep.saveStepAttribute( id_transformation, id_step, "xslfactory", xslFactory );

      for ( int i = 0; i < parameterName.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "param_field", parameterField[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "param_name", parameterName[i] );
      }
      for ( int i = 0; i < outputPropertyName.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "output_property_name", outputPropertyName[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "output_property_value", outputPropertyValue[i] );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "XsltMeta.Exception.UnableToSaveStepInfo" ) + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {
    CheckResult cr;

    if ( prev != null && prev.size() > 0 ) {
      cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "XsltMeta.CheckResult.ConnectedStepOK", String.valueOf( prev.size() ) ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "XsltMeta.CheckResult.NoInputReceived" ), stepMeta );
      remarks.add( cr );
    }
    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "XsltMeta.CheckResult.ExpectedInputOk" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "XsltMeta.CheckResult.ExpectedInputError" ), stepMeta );
      remarks.add( cr );
    }

    // Check if The result field is given
    if ( getResultfieldname() == null ) {
      // Result Field is missing !
      cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "XsltMeta.CheckResult.ErrorResultFieldNameMissing" ), stepMeta );
      remarks.add( cr );

    }

    // Check if XSL Filename field is provided
    if ( xslFileFieldUse ) {
      if ( getXSLFileField() == null ) {
        // Result Field is missing !
        cr =
            new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                "XsltMeta.CheckResult.ErrorResultXSLFieldNameMissing" ), stepMeta );
        remarks.add( cr );
      } else {
        // Result Field is provided !
        cr =
            new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                "XsltMeta.CheckResult.ErrorResultXSLFieldNameOK" ), stepMeta );
        remarks.add( cr );
      }
    } else {
      if ( xslFilename == null ) {
        // Result Field is missing !
        cr =
            new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                "XsltMeta.CheckResult.ErrorXSLFileNameMissing" ), stepMeta );
        remarks.add( cr );

      } else {
        // Check if it's exist and it's a file
        String RealFilename = transMeta.environmentSubstitute( xslFilename );
        File f = new File( RealFilename );

        if ( f.exists() ) {
          if ( f.isFile() ) {
            cr =
                new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                    "XsltMeta.CheckResult.FileExists", RealFilename ), stepMeta );
            remarks.add( cr );
          } else {
            cr =
                new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                    "XsltMeta.CheckResult.ExistsButNoFile", RealFilename ), stepMeta );
            remarks.add( cr );
          }
        } else {
          cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                  "XsltMeta.CheckResult.FileNotExists", RealFilename ), stepMeta );
          remarks.add( cr );
        }
      }
    }

  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
      Trans trans ) {
    return new Xslt( stepMeta, stepDataInterface, cnr, transMeta, trans );

  }

  public StepDataInterface getStepData() {
    return new XsltData();
  }

  public boolean supportsErrorHandling() {
    return true;
  }
}
