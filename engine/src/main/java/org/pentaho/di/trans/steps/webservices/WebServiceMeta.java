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

package org.pentaho.di.trans.steps.webservices;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
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

public class WebServiceMeta extends BaseStepMeta implements StepMetaInterface {
  public static final String XSD_NS_URI = "http://www.w3.org/2001/XMLSchema";

  public static final int DEFAULT_STEP = 1000;

  /** The input web service fields */
  private List<WebServiceField> fieldsIn;

  /** The output web service fields */
  private List<WebServiceField> fieldsOut;

  /** Web service URL */
  private String url;

  /** Name of the web service operation to use */
  private String operationName;

  /** Name of the operation request name: optional, can be different from the operation name */
  private String operationRequestName;

  /** The name-space of the operation */
  private String operationNamespace;

  /** The name of the object that encapsulates the input fields in case we're dealing with a table */
  private String inFieldContainerName;

  /** Name of the input object */
  private String inFieldArgumentName;

  /** Name of the object that encapsulates the output fields in case we're dealing with a table */
  private String outFieldContainerName;

  /** Name of the output object */
  private String outFieldArgumentName;

  private String proxyHost;

  private String proxyPort;

  private String httpLogin;

  private String httpPassword;

  /** Flag to allow input data to pass to the output */
  private boolean passingInputData;

  /** The number of rows to send with each call */
  private int callStep = DEFAULT_STEP;

  /** Use the 2.5/3.0 parsing logic (available for compatibility reasons) */
  private boolean compatible;

  /** The name of the repeating element name. Empty = a single row return */
  private String repeatingElementName;

  /** Is this step giving back the complete reply from the service as an XML string? */
  private boolean returningReplyAsString;

  public WebServiceMeta() {
    super();
    fieldsIn = new ArrayList<WebServiceField>();
    fieldsOut = new ArrayList<WebServiceField>();
  }

  public WebServiceMeta( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    this();
    loadXML( stepnode, databases, metaStore );
  }

  public WebServiceMeta( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    this();
    readRep( rep, metaStore, id_step, databases );
  }

  @Override
  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // Input rows and output rows are different in the webservice step
    //
    if ( !isPassingInputData() ) {
      r.clear();
    }

    // Add the output fields...
    //
    for ( WebServiceField field : getFieldsOut() ) {
      int valueType = field.getType();

      // If the type is unrecognized we give back the XML as a String...
      //
      if ( field.getType() == ValueMetaInterface.TYPE_NONE ) {
        valueType = ValueMetaInterface.TYPE_STRING;
      }

      try {
        ValueMetaInterface vValue = ValueMetaFactory.createValueMeta( field.getName(), valueType );
        vValue.setOrigin( name );
        r.addValueMeta( vValue );
      } catch ( Exception e ) {
        throw new KettleStepException( e );
      }
    }
  }

  public WebServiceMeta clone() {
    WebServiceMeta retval = (WebServiceMeta) super.clone();
    retval.fieldsIn = new ArrayList<WebServiceField>();
    for ( WebServiceField field : fieldsIn ) {
      retval.fieldsIn.add( field.clone() );
    }
    retval.fieldsOut = new ArrayList<WebServiceField>();
    for ( WebServiceField field : fieldsOut ) {
      retval.fieldsOut.add( field.clone() );
    }
    return retval;
  }

  public void setDefault() {
    passingInputData = true; // Pass input data by default.
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    if ( prev == null || prev.size() == 0 ) {
      cr =
        new CheckResult(
          CheckResult.TYPE_RESULT_WARNING, "Not receiving any fields from previous steps!", stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, "Step is connected to previous one, receiving "
          + prev.size() + " fields", stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr = new CheckResult( CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepMeta );
      remarks.add( cr );
    } else if ( getInFieldArgumentName() != null || getInFieldContainerName() != null ) {
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepMeta );
      remarks.add( cr );
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    // Store the WebService URL
    //
    retval.append( "    " + XMLHandler.addTagValue( "wsURL", getUrl() ) );

    // Store the operation
    //
    retval.append( "    " + XMLHandler.addTagValue( "wsOperation", getOperationName() ) );
    retval.append( "    " + XMLHandler.addTagValue( "wsOperationRequest", getOperationRequestName() ) );
    retval.append( "    " + XMLHandler.addTagValue( "wsOperationNamespace", getOperationNamespace() ) );
    retval.append( "    " + XMLHandler.addTagValue( "wsInFieldContainer", getInFieldContainerName() ) );
    retval.append( "    " + XMLHandler.addTagValue( "wsInFieldArgument", getInFieldArgumentName() ) );
    retval.append( "    " + XMLHandler.addTagValue( "wsOutFieldContainer", getOutFieldContainerName() ) );
    retval.append( "    " + XMLHandler.addTagValue( "wsOutFieldArgument", getOutFieldArgumentName() ) );
    retval.append( "    " + XMLHandler.addTagValue( "proxyHost", getProxyHost() ) );
    retval.append( "    " + XMLHandler.addTagValue( "proxyPort", getProxyPort() ) );
    retval.append( "    " + XMLHandler.addTagValue( "httpLogin", getHttpLogin() ) );
    retval.append( "    " + XMLHandler.addTagValue( "httpPassword", getHttpPassword() ) );
    retval.append( "    " + XMLHandler.addTagValue( "callStep", getCallStep() ) );
    retval.append( "    " + XMLHandler.addTagValue( "passingInputData", isPassingInputData() ) );
    retval.append( "    " + XMLHandler.addTagValue( "compatible", isCompatible() ) );
    retval.append( "    " + XMLHandler.addTagValue( "repeating_element", getRepeatingElementName() ) );
    retval.append( "    " + XMLHandler.addTagValue( "reply_as_string", isReturningReplyAsString() ) );

    // Store the field parameters
    //

    // Store the link between the input fields and the WebService input
    //
    retval.append( "    <fieldsIn>" + Const.CR );
    for ( int i = 0; i < getFieldsIn().size(); i++ ) {
      WebServiceField vField = getFieldsIn().get( i );
      retval.append( "    <field>" + Const.CR );
      retval.append( "        " + XMLHandler.addTagValue( "name", vField.getName() ) );
      retval.append( "        " + XMLHandler.addTagValue( "wsName", vField.getWsName() ) );
      retval.append( "        " + XMLHandler.addTagValue( "xsdType", vField.getXsdType() ) );
      retval.append( "    </field>" + Const.CR );
    }
    retval.append( "      </fieldsIn>" + Const.CR );

    // Store the link between the input fields and the WebService output
    //
    retval.append( "    <fieldsOut>" + Const.CR );
    for ( int i = 0; i < getFieldsOut().size(); i++ ) {
      WebServiceField vField = getFieldsOut().get( i );
      retval.append( "    <field>" + Const.CR );
      retval.append( "        " + XMLHandler.addTagValue( "name", vField.getName() ) );
      retval.append( "        " + XMLHandler.addTagValue( "wsName", vField.getWsName() ) );
      retval.append( "        " + XMLHandler.addTagValue( "xsdType", vField.getXsdType() ) );
      retval.append( "    </field>" + Const.CR );
    }
    retval.append( "      </fieldsOut>" + Const.CR );

    return retval.toString();
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    // Load the URL
    //
    setUrl( XMLHandler.getTagValue( stepnode, "wsURL" ) );

    // Load the operation
    //
    setOperationName( XMLHandler.getTagValue( stepnode, "wsOperation" ) );
    setOperationRequestName( XMLHandler.getTagValue( stepnode, "wsOperationRequest" ) );
    setOperationNamespace( XMLHandler.getTagValue( stepnode, "wsOperationNamespace" ) );
    setInFieldContainerName( XMLHandler.getTagValue( stepnode, "wsInFieldContainer" ) );
    setInFieldArgumentName( XMLHandler.getTagValue( stepnode, "wsInFieldArgument" ) );
    setOutFieldContainerName( XMLHandler.getTagValue( stepnode, "wsOutFieldContainer" ) );
    setOutFieldArgumentName( XMLHandler.getTagValue( stepnode, "wsOutFieldArgument" ) );
    setProxyHost( XMLHandler.getTagValue( stepnode, "proxyHost" ) );
    setProxyPort( XMLHandler.getTagValue( stepnode, "proxyPort" ) );
    setHttpLogin( XMLHandler.getTagValue( stepnode, "httpLogin" ) );
    setHttpPassword( XMLHandler.getTagValue( stepnode, "httpPassword" ) );
    setCallStep( Const.toInt( XMLHandler.getTagValue( stepnode, "callStep" ), DEFAULT_STEP ) );
    setPassingInputData( "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "passingInputData" ) ) );
    String compat = XMLHandler.getTagValue( stepnode, "compatible" );
    setCompatible( Utils.isEmpty( compat ) || "Y".equalsIgnoreCase( compat ) );
    setRepeatingElementName( XMLHandler.getTagValue( stepnode, "repeating_element" ) );
    setReturningReplyAsString( "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "reply_as_string" ) ) );

    // Load the input fields mapping
    //
    getFieldsIn().clear();
    Node fields = XMLHandler.getSubNode( stepnode, "fieldsIn" );
    int nrfields = XMLHandler.countNodes( fields, "field" );

    for ( int i = 0; i < nrfields; ++i ) {
      Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

      WebServiceField field = new WebServiceField();
      field.setName( XMLHandler.getTagValue( fnode, "name" ) );
      field.setWsName( XMLHandler.getTagValue( fnode, "wsName" ) );
      field.setXsdType( XMLHandler.getTagValue( fnode, "xsdType" ) );
      getFieldsIn().add( field );

    }

    // Load the output fields mapping
    //
    getFieldsOut().clear();

    fields = XMLHandler.getSubNode( stepnode, "fieldsOut" );
    nrfields = XMLHandler.countNodes( fields, "field" );

    for ( int i = 0; i < nrfields; ++i ) {
      Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

      WebServiceField field = new WebServiceField();
      field.setName( XMLHandler.getTagValue( fnode, "name" ) );
      field.setWsName( XMLHandler.getTagValue( fnode, "wsName" ) );
      field.setXsdType( XMLHandler.getTagValue( fnode, "xsdType" ) );
      getFieldsOut().add( field );
    }
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    // Load the URL
    //
    setUrl( rep.getStepAttributeString( id_step, "wsUrl" ) );

    // Load the operation
    //
    setOperationName( rep.getStepAttributeString( id_step, "wsOperation" ) );
    setOperationRequestName( rep.getStepAttributeString( id_step, "wsOperationRequest" ) );
    setOperationNamespace( rep.getStepAttributeString( id_step, "wsOperationNamespace" ) );
    setInFieldContainerName( rep.getStepAttributeString( id_step, "wsInFieldContainer" ) );
    setInFieldArgumentName( rep.getStepAttributeString( id_step, "wsInFieldArgument" ) );
    setOutFieldContainerName( rep.getStepAttributeString( id_step, "wsOutFieldContainer" ) );
    setOutFieldArgumentName( rep.getStepAttributeString( id_step, "wsOutFieldArgument" ) );
    setProxyHost( rep.getStepAttributeString( id_step, "proxyHost" ) );
    setProxyPort( rep.getStepAttributeString( id_step, "proxyPort" ) );
    setHttpLogin( rep.getStepAttributeString( id_step, "httpLogin" ) );
    setHttpPassword( rep.getStepAttributeString( id_step, "httpPassword" ) );
    setCallStep( (int) rep.getStepAttributeInteger( id_step, "callStep" ) );
    setPassingInputData( rep.getStepAttributeBoolean( id_step, "passingInputData" ) );
    setCompatible( rep.getStepAttributeBoolean( id_step, 0, "compatible", true ) ); // Default to true for backward
                                                                                    // compatibility
    setRepeatingElementName( rep.getStepAttributeString( id_step, "repeating_element" ) );
    setReturningReplyAsString( rep.getStepAttributeBoolean( id_step, 0, "reply_as_string" ) );

    // Load the input fields mapping
    //
    int nb = rep.countNrStepAttributes( id_step, "fieldIn_ws_name" );
    getFieldsIn().clear();
    for ( int i = 0; i < nb; ++i ) {
      WebServiceField field = new WebServiceField();
      field.setName( rep.getStepAttributeString( id_step, i, "fieldIn_name" ) );
      field.setWsName( rep.getStepAttributeString( id_step, i, "fieldIn_ws_name" ) );
      field.setXsdType( rep.getStepAttributeString( id_step, i, "fieldIn_xsd_type" ) );
      getFieldsIn().add( field );
    }

    // Load the output fields mapping
    //
    nb = rep.countNrStepAttributes( id_step, "fieldOut_ws_name" );
    getFieldsOut().clear();
    for ( int i = 0; i < nb; ++i ) {
      WebServiceField field = new WebServiceField();
      field.setName( rep.getStepAttributeString( id_step, i, "fieldOut_name" ) );
      field.setWsName( rep.getStepAttributeString( id_step, i, "fieldOut_ws_name" ) );
      field.setXsdType( rep.getStepAttributeString( id_step, i, "fieldOut_xsd_type" ) );
      getFieldsOut().add( field );
    }

  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    // Store the URL
    //
    rep.saveStepAttribute( id_transformation, id_step, "wsUrl", getUrl() );

    // Store the WS Operation
    //
    rep.saveStepAttribute( id_transformation, id_step, "wsOperation", getOperationName() );
    rep.saveStepAttribute( id_transformation, id_step, "wsOperationRequest", getOperationRequestName() );
    rep.saveStepAttribute( id_transformation, id_step, "wsOperationNamespace", getOperationNamespace() );
    rep.saveStepAttribute( id_transformation, id_step, "wsInFieldContainer", getInFieldContainerName() );
    rep.saveStepAttribute( id_transformation, id_step, "wsInFieldArgument", getInFieldArgumentName() );
    rep.saveStepAttribute( id_transformation, id_step, "wsOutFieldContainer", getOutFieldContainerName() );
    rep.saveStepAttribute( id_transformation, id_step, "wsOutFieldArgument", getOutFieldArgumentName() );
    rep.saveStepAttribute( id_transformation, id_step, "proxyHost", getProxyHost() );
    rep.saveStepAttribute( id_transformation, id_step, "proxyPort", getProxyPort() );
    rep.saveStepAttribute( id_transformation, id_step, "httpLogin", getHttpLogin() );
    rep.saveStepAttribute( id_transformation, id_step, "httpPassword", getHttpPassword() );
    rep.saveStepAttribute( id_transformation, id_step, "callStep", getCallStep() );
    rep.saveStepAttribute( id_transformation, id_step, "passingInputData", isPassingInputData() );
    rep.saveStepAttribute( id_transformation, id_step, "compatible", isCompatible() );
    rep.saveStepAttribute( id_transformation, id_step, "repeating_element", getRepeatingElementName() );
    rep.saveStepAttribute( id_transformation, id_step, "reply_as_string", isReturningReplyAsString() );

    // Load the input fields mapping
    //
    for ( int i = 0; i < getFieldsIn().size(); ++i ) {
      WebServiceField vField = getFieldsIn().get( i );
      rep.saveStepAttribute( id_transformation, id_step, i, "fieldIn_name", vField.getName() );
      rep.saveStepAttribute( id_transformation, id_step, i, "fieldIn_ws_name", vField.getWsName() );
      rep.saveStepAttribute( id_transformation, id_step, i, "fieldIn_xsd_type", vField.getXsdType() );
    }

    // Load the output fields mapping
    //
    for ( int i = 0; i < getFieldsOut().size(); ++i ) {
      WebServiceField vField = getFieldsOut().get( i );
      rep.saveStepAttribute( id_transformation, id_step, i, "fieldOut_name", vField.getName() );
      rep.saveStepAttribute( id_transformation, id_step, i, "fieldOut_ws_name", vField.getWsName() );
      rep.saveStepAttribute( id_transformation, id_step, i, "fieldOut_xsd_type", vField.getXsdType() );
    }
  }

  public String getOperationName() {
    return operationName;
  }

  public void setOperationName( String operationName ) {
    this.operationName = operationName;
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans disp ) {
    return new WebService( stepMeta, stepDataInterface, cnr, transMeta, disp );
  }

  public StepDataInterface getStepData() {
    return new WebServiceData();
  }

  public WebServiceField getFieldInFromName( String name ) {
    WebServiceField param = null;
    for ( Iterator<WebServiceField> iter = getFieldsIn().iterator(); iter.hasNext(); ) {
      WebServiceField paramCour = iter.next();
      if ( name.equals( paramCour.getName() ) ) {
        param = paramCour;
        break;
      }
    }
    return param;
  }

  /**
   * Returns the WebServicesField for the given wsName.
   *
   * @param wsName
   *          The name of the WebServiceField to return
   * @param ignoreWsNsPrefix
   *          If true the lookup of the cache of WebServiceFields will not include the target namespace prefix.
   * @return
   */
  public WebServiceField getFieldOutFromWsName( String wsName, boolean ignoreWsNsPrefix ) {
    WebServiceField param = null;

    if ( Utils.isEmpty( wsName ) ) {
      return param;
    }

    // if we are ignoring the name space prefix
    if ( ignoreWsNsPrefix ) {

      // we split the wsName and set it to the last element of what was parsed
      String[] wsNameParsed = wsName.split( ":" );
      wsName = wsNameParsed[wsNameParsed.length - 1];
    }

    // we now look for the wsname
    for ( Iterator<WebServiceField> iter = getFieldsOut().iterator(); iter.hasNext(); ) {
      WebServiceField paramCour = iter.next();
      if ( paramCour.getWsName().equals( wsName ) ) {
        param = paramCour;
        break;
      }
    }
    return param;
  }

  public List<WebServiceField> getFieldsIn() {
    return fieldsIn;
  }

  public void setFieldsIn( List<WebServiceField> fieldsIn ) {
    this.fieldsIn = fieldsIn;
  }

  public boolean hasFieldsIn() {
    return fieldsIn != null && !fieldsIn.isEmpty();
  }

  public void addFieldIn( WebServiceField field ) {
    fieldsIn.add( field );
  }

  public List<WebServiceField> getFieldsOut() {
    return fieldsOut;
  }

  public void setFieldsOut( List<WebServiceField> fieldsOut ) {
    this.fieldsOut = fieldsOut;
  }

  public void addFieldOut( WebServiceField field ) {
    fieldsOut.add( field );
  }

  public String getInFieldArgumentName() {
    return inFieldArgumentName;
  }

  public void setInFieldArgumentName( String inFieldArgumentName ) {
    this.inFieldArgumentName = inFieldArgumentName;
  }

  public String getOutFieldArgumentName() {
    return outFieldArgumentName;
  }

  public void setOutFieldArgumentName( String outFieldArgumentName ) {
    this.outFieldArgumentName = outFieldArgumentName;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl( String url ) {
    this.url = url;
  }

  public int getCallStep() {
    return callStep;
  }

  public void setCallStep( int callStep ) {
    this.callStep = callStep;
  }

  public String getOperationNamespace() {
    return operationNamespace;
  }

  public void setOperationNamespace( String operationNamespace ) {
    this.operationNamespace = operationNamespace;
  }

  public String getHttpLogin() {
    return httpLogin;
  }

  public void setHttpLogin( String httpLogin ) {
    this.httpLogin = httpLogin;
  }

  public String getHttpPassword() {
    return httpPassword;
  }

  public void setHttpPassword( String httpPassword ) {
    this.httpPassword = httpPassword;
  }

  public String getProxyHost() {
    return proxyHost;
  }

  public void setProxyHost( String proxyHost ) {
    this.proxyHost = proxyHost;
  }

  public String getProxyPort() {
    return proxyPort;
  }

  public void setProxyPort( String proxyPort ) {
    this.proxyPort = proxyPort;
  }

  public String getInFieldContainerName() {
    return inFieldContainerName;
  }

  public void setInFieldContainerName( String inFieldContainerName ) {
    this.inFieldContainerName = inFieldContainerName;
  }

  public String getOutFieldContainerName() {
    return outFieldContainerName;
  }

  public void setOutFieldContainerName( String outFieldContainerName ) {
    this.outFieldContainerName = outFieldContainerName;
  }

  /**
   * @return the passingInputData
   */
  public boolean isPassingInputData() {
    return passingInputData;
  }

  /**
   * @param passingInputData
   *          the passingInputData to set
   */
  public void setPassingInputData( boolean passingInputData ) {
    this.passingInputData = passingInputData;
  }

  /**
   * @return the compatible
   */
  public boolean isCompatible() {
    return compatible;
  }

  /**
   * @param compatible
   *          the compatible to set
   */
  public void setCompatible( boolean compatible ) {
    this.compatible = compatible;
  }

  /**
   * @return the repeatingElementName
   */
  public String getRepeatingElementName() {
    return repeatingElementName;
  }

  /**
   * @param repeatingElementName
   *          the repeatingElementName to set
   */
  public void setRepeatingElementName( String repeatingElementName ) {
    this.repeatingElementName = repeatingElementName;
  }

  /**
   * @return true if the reply from the service is simply passed on as a String, mostly in XML
   */
  public boolean isReturningReplyAsString() {
    return returningReplyAsString;
  }

  /**
   * @param returningReplyAsString
   *          true if the reply from the service is simply passed on as a String, mostly in XML
   */
  public void setReturningReplyAsString( boolean returningReplyAsString ) {
    this.returningReplyAsString = returningReplyAsString;
  }

  /**
   * @return the operationRequestName
   */
  public String getOperationRequestName() {
    return operationRequestName;
  }

  /**
   * @param operationRequestName
   *          the operationRequestName to set
   */
  public void setOperationRequestName( String operationRequestName ) {
    this.operationRequestName = operationRequestName;
  }
}
