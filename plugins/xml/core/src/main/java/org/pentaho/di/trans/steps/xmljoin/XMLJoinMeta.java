/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.xmljoin;

import java.util.Arrays;
import java.util.List;

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
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

/**
 * This class knows how to handle the MetaData for the XML join step
 * 
 * @since 30-04-2008
 * 
 */
@Step( id = "XMLJoin", image = "XJN.svg", i18nPackageName = "org.pentaho.di.trans.steps.xmljoin",
    name = "XMLJoin.name", description = "XMLJoin.description", categoryDescription = "XMLJoin.category",
    documentationUrl = "http://wiki.pentaho.com/display/EAI/XML+Join" )
@InjectionSupported( localizationPrefix = "XMLJoin.Injection." )
public class XMLJoinMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = XMLJoinMeta.class; // for i18n purposes, needed by Translator2!!

  /** The base name of the output file */

  /** Flag: execute complex join */
  @Injection( name = "COMPLEX_JOIN" )
  private boolean complexJoin;

  /** What step holds the xml string to join into */
  @Injection( name = "TARGET_XML_STEP" )
  private String targetXMLstep;

  /** What field holds the xml string to join into */
  @Injection( name = "TARGET_XML_FIELD" )
  private String targetXMLfield;

  /** What field holds the XML tags to join */
  @Injection( name = "SOURCE_XML_FIELD" )
  private String sourceXMLfield;

  /** The name value containing the resulting XML fragment */
  @Injection( name = "VALUE_XML_FIELD" )
  private String valueXMLfield;

  /** The name of the repeating row XML element */
  @Injection( name = "TARGET_XPATH" )
  private String targetXPath;

  /** What step holds the xml strings to join */
  @Injection( name = "SOURCE_XML_STEP" )
  private String sourceXMLstep;

  /** What field holds the join compare value */
  @Injection( name = "JOIN_COMPARE_FIELD" )
  private String joinCompareField;

  /** The encoding to use for reading: null or empty string means system default encoding */
  @Injection( name = "ENCODING" )
  private String encoding;

  /** Flag: execute complex join */
  @Injection( name = "OMIT_XML_HEADER" )
  private boolean omitXMLHeader;

  /** Flag: omit null values from result xml */
  @Injection( name = "OMIT_NULL_VALUES" )
  private boolean omitNullValues;

  public XMLJoinMeta() {
    super(); // allocate BaseStepMeta
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public Object clone() {
    XMLJoinMeta retval = (XMLJoinMeta) super.clone();
    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      valueXMLfield = XMLHandler.getTagValue( stepnode, "valueXMLfield" );
      targetXMLstep = XMLHandler.getTagValue( stepnode, "targetXMLstep" );
      targetXMLfield = XMLHandler.getTagValue( stepnode, "targetXMLfield" );
      sourceXMLstep = XMLHandler.getTagValue( stepnode, "sourceXMLstep" );
      sourceXMLfield = XMLHandler.getTagValue( stepnode, "sourceXMLfield" );
      targetXPath = XMLHandler.getTagValue( stepnode, "targetXPath" );
      joinCompareField = XMLHandler.getTagValue( stepnode, "joinCompareField" );
      encoding = XMLHandler.getTagValue( stepnode, "encoding" );
      complexJoin = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "complexJoin" ) );
      omitXMLHeader = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "omitXMLHeader" ) );
      omitNullValues = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "omitNullValues" ) );

    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public void setDefault() {
    // complexJoin = false;
    encoding = Const.XML_ENCODING;
  }

  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    ValueMetaInterface v = new ValueMeta( this.getValueXMLfield(), ValueMetaInterface.TYPE_STRING );
    v.setOrigin( name );

    TransMeta transMeta = (TransMeta) space;
    try {
      // Row should only include fields from the target and not the source. During the preview table generation
      // the fields from all previous steps (source and target) are included in the row so lets remove the
      // source fields.
      List<String> targetFieldNames = null;
      RowMetaInterface targetRowMeta = transMeta.getStepFields( transMeta.findStep( getTargetXMLstep() ),
        null,
        null );
      if ( targetRowMeta != null ) {
        targetFieldNames = Arrays.asList( targetRowMeta.getFieldNames() );
      }
      for ( String fieldName : transMeta.getStepFields( transMeta.findStep( getSourceXMLstep() ),
                                                                            null,
                                                                            null ).getFieldNames() ) {
        if ( targetFieldNames == null || !targetFieldNames.contains( fieldName ) ) {
          row.removeValueMeta( fieldName );
        }
      }
    } catch ( KettleValueException e ) {
      // Pass
    }

    row.addValueMeta( v );
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer( 500 );

    retval.append( "    " ).append( XMLHandler.addTagValue( "valueXMLField", valueXMLfield ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "targetXMLstep", targetXMLstep ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "targetXMLfield", targetXMLfield ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "sourceXMLstep", sourceXMLstep ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "sourceXMLfield", sourceXMLfield ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "complexJoin", complexJoin ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "joinCompareField", joinCompareField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "targetXPath", targetXPath ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "encoding", encoding ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "omitXMLHeader", omitXMLHeader ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "omitNullValues", omitNullValues ) );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      targetXMLstep = rep.getStepAttributeString( id_step, "targetXMLstep" );
      targetXMLfield = rep.getStepAttributeString( id_step, "targetXMLfield" );
      sourceXMLstep = rep.getStepAttributeString( id_step, "sourceXMLstep" );
      sourceXMLfield = rep.getStepAttributeString( id_step, "sourceXMLfield" );
      targetXPath = rep.getStepAttributeString( id_step, "targetXPath" );
      complexJoin = rep.getStepAttributeBoolean( id_step, "complexJoin" );
      joinCompareField = rep.getStepAttributeString( id_step, "joinCompareField" );
      valueXMLfield = rep.getStepAttributeString( id_step, "valueXMLfield" );
      encoding = rep.getStepAttributeString( id_step, "encoding" );
      omitXMLHeader = rep.getStepAttributeBoolean( id_step, "omitXMLHeader" );
      omitNullValues = rep.getStepAttributeBoolean( id_step, "omitNullValues" );

    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "valueXMLfield", valueXMLfield );
      rep.saveStepAttribute( id_transformation, id_step, "targetXMLstep", targetXMLstep );
      rep.saveStepAttribute( id_transformation, id_step, "targetXMLfield", targetXMLfield );
      rep.saveStepAttribute( id_transformation, id_step, "sourceXMLstep", sourceXMLstep );
      rep.saveStepAttribute( id_transformation, id_step, "sourceXMLfield", sourceXMLfield );
      rep.saveStepAttribute( id_transformation, id_step, "complexJoin", complexJoin );
      rep.saveStepAttribute( id_transformation, id_step, "targetXPath", targetXPath );
      rep.saveStepAttribute( id_transformation, id_step, "joinCompareField", joinCompareField );
      rep.saveStepAttribute( id_transformation, id_step, "encoding", encoding );
      rep.saveStepAttribute( id_transformation, id_step, "omitXMLHeader", omitXMLHeader );
      rep.saveStepAttribute( id_transformation, id_step, "omitNullValues", omitNullValues );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {

    CheckResult cr;
    // checks for empty field which are required
    if ( this.targetXMLstep == null || this.targetXMLstep.length() == 0 ) {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "XMLJoin.CheckResult.TargetXMLStepNotSpecified" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "XMLJoin.CheckResult.TargetXMLStepSpecified" ), stepMeta );
      remarks.add( cr );
    }
    if ( this.targetXMLfield == null || this.targetXMLfield.length() == 0 ) {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "XMLJoin.CheckResult.TargetXMLFieldNotSpecified" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "XMLJoin.CheckResult.TargetXMLFieldSpecified" ), stepMeta );
      remarks.add( cr );
    }
    if ( this.sourceXMLstep == null || this.sourceXMLstep.length() == 0 ) {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "XMLJoin.CheckResult.SourceXMLStepNotSpecified" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "XMLJoin.CheckResult.SourceXMLStepSpecified" ), stepMeta );
      remarks.add( cr );
    }
    if ( this.sourceXMLfield == null || this.sourceXMLfield.length() == 0 ) {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "XMLJoin.CheckResult.SourceXMLFieldNotSpecified" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "XMLJoin.CheckResult.SourceXMLFieldSpecified" ), stepMeta );
      remarks.add( cr );
    }
    if ( this.valueXMLfield == null || this.valueXMLfield.length() == 0 ) {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "XMLJoin.CheckResult.ResultFieldNotSpecified" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "XMLJoin.CheckResult.ResultFieldSpecified" ), stepMeta );
      remarks.add( cr );
    }
    if ( this.targetXPath == null || this.targetXPath.length() == 0 ) {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "XMLJoin.CheckResult.TargetXPathNotSpecified" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "XMLJoin.CheckResult.TargetXPathSpecified" ), stepMeta );
      remarks.add( cr );
    }

    // See if we have the right input streams leading to this step!
    if ( input.length > 0 ) {
      boolean targetStepFound = false;
      boolean sourceStepFound = false;
      for ( int i = 0; i < input.length; i++ ) {
        if ( this.targetXMLstep != null && this.targetXMLstep.equals( input[i] ) ) {
          targetStepFound = true;
          cr =
              new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                  "XMLJoin.CheckResult.TargetXMLStepFound", this.targetXMLstep ), stepMeta );
          remarks.add( cr );
        }
        if ( this.sourceXMLstep != null && this.sourceXMLstep.equals( input[i] ) ) {
          sourceStepFound = true;
          cr =
              new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                  "XMLJoin.CheckResult.SourceXMLStepFound", this.sourceXMLstep ), stepMeta );
          remarks.add( cr );
        }
      }

      if ( !targetStepFound ) {
        cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                "XMLJoin.CheckResult.TargetXMLStepNotFound", this.targetXMLstep ), stepMeta );
        remarks.add( cr );
      }
      if ( !sourceStepFound ) {
        cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                "XMLJoin.CheckResult.SourceXMLStepNotFound", this.sourceXMLstep ), stepMeta );
        remarks.add( cr );
      }
    } else {
      cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "XMLJoin.CheckResult.ExpectedInputError" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
      Trans trans ) {
    return new XMLJoin( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new XMLJoinData();
  }

  public boolean isComplexJoin() {
    return complexJoin;
  }

  public void setComplexJoin( boolean complexJoin ) {
    this.complexJoin = complexJoin;
  }

  public String getTargetXMLstep() {
    return targetXMLstep;
  }

  public void setTargetXMLstep( String targetXMLstep ) {
    this.targetXMLstep = targetXMLstep;
  }

  public String getTargetXMLfield() {
    return targetXMLfield;
  }

  public void setTargetXMLfield( String targetXMLfield ) {
    this.targetXMLfield = targetXMLfield;
  }

  public String getSourceXMLstep() {
    return sourceXMLstep;
  }

  public void setSourceXMLstep( String targetXMLstep ) {
    this.sourceXMLstep = targetXMLstep;
  }

  public String getSourceXMLfield() {
    return sourceXMLfield;
  }

  public void setSourceXMLfield( String sourceXMLfield ) {
    this.sourceXMLfield = sourceXMLfield;
  }

  public String getValueXMLfield() {
    return valueXMLfield;
  }

  public void setValueXMLfield( String valueXMLfield ) {
    this.valueXMLfield = valueXMLfield;
  }

  public String getTargetXPath() {
    return targetXPath;
  }

  public void setTargetXPath( String targetXPath ) {
    this.targetXPath = targetXPath;
  }

  public String getJoinCompareField() {
    return joinCompareField;
  }

  public void setJoinCompareField( String joinCompareField ) {
    this.joinCompareField = joinCompareField;
  }

  public boolean excludeFromRowLayoutVerification() {
    return true;
  }

  public boolean isOmitXMLHeader() {
    return omitXMLHeader;
  }

  public void setOmitXMLHeader( boolean omitXMLHeader ) {
    this.omitXMLHeader = omitXMLHeader;
  }

  public void setOmitNullValues( boolean omitNullValues ) {

    this.omitNullValues = omitNullValues;

  }

  public boolean isOmitNullValues() {

    return omitNullValues;

  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding( String encoding ) {
    this.encoding = encoding;
  }

}
