/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.googleanalytics;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
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

public class GaInputStepMeta extends BaseStepMeta implements StepMetaInterface {

  private static Class<?> PKG = GaInputStepMeta.class; // for i18n purposes

  public static final String GA_MANAGEMENT_URL = "https://www.googleapis.com/analytics/v2.4/management";
  public static final String GA_DATA_URL = "https://www.googleapis.com/analytics/v2.4/data";

  public static final String FIELD_TYPE_CONFIDENCE_INTERVAL = "Confidence Interval for Metric";
  public static final String FIELD_TYPE_DIMENSION = "Dimension";
  public static final String FIELD_TYPE_METRIC = "Metric";
  public static final String FIELD_TYPE_DATA_SOURCE_PROPERTY = "Data Source Property";
  public static final String FIELD_TYPE_DATA_SOURCE_FIELD = "Data Source Field";
  public static final String FIELD_DATA_SOURCE_TABLE_ID = "dxp:tableId";
  public static final String FIELD_DATA_SOURCE_TABLE_NAME = "dxp:tableName";

  public static final String DEFAULT_GA_APPLICATION_NAME = "type-exit.org kettle plugin";

  private String gaAppName;
  private String gaEmail;
  private String gaPassword;
  private String gaProfileTableId;
  private String gaProfileName;
  private String gaApiKey;
  private boolean useCustomTableId;
  private String gaCustomTableId;
  private String startDate;
  private String endDate;
  private String dimensions;
  private String metrics;
  private String filters;
  private String sort;
  private boolean useSegment;

  private boolean useCustomSegment;
  private int rowLimit;

  private String customSegment;
  private String segmentName;
  private String segmentId;

  private String[] feedField;
  private String[] feedFieldType;
  private String[] outputField;
  private int[] outputType;
  private String[] conversionMask;

  public GaInputStepMeta() {
    super();
  }

  public int getRowLimit() {
    return rowLimit;
  }

  public void setRowLimit( int rowLimit ) {
    if ( rowLimit < 0 ) {
      rowLimit = 0;
    }
    this.rowLimit = rowLimit;
  }

  public String[] getConversionMask() {
    return conversionMask;
  }

  public String getGaAppName() {
    return gaAppName;
  }

  public void setGaAppName( String gaAppName ) {
    this.gaAppName = gaAppName;
  }

  public boolean isUseCustomTableId() {
    return useCustomTableId;
  }

  public void setUseCustomTableId( boolean useCustomTableId ) {
    this.useCustomTableId = useCustomTableId;
  }

  public String getGaCustomTableId() {
    return gaCustomTableId;
  }

  public void setGaCustomTableId( String gaCustomTableId ) {
    this.gaCustomTableId = gaCustomTableId;
  }

  public boolean isUseSegment() {
    return useSegment;
  }

  public void setUseSegment( boolean useSegment ) {
    this.useSegment = useSegment;
  }

  public String getSegmentName() {
    return segmentName;
  }

  public void setSegmentName( String segmentName ) {
    this.segmentName = segmentName;
  }

  public String getSegmentId() {
    return segmentId;
  }

  public void setSegmentId( String segmentId ) {
    this.segmentId = segmentId;
  }

  public boolean isUseCustomSegment() {
    return useCustomSegment;
  }

  public void setUseCustomSegment( boolean useCustomSegment ) {
    this.useCustomSegment = useCustomSegment;
  }

  public String getCustomSegment() {
    return customSegment;
  }

  public void setCustomSegment( String customSegment ) {
    this.customSegment = customSegment;
  }

  public String getDimensions() {
    return dimensions;
  }

  public void setDimensions( String dimensions ) {
    this.dimensions = dimensions;
  }

  public String getMetrics() {
    return metrics;
  }

  public void setMetrics( String metrics ) {
    this.metrics = metrics;
  }

  public String getFilters() {
    return filters;
  }

  public void setFilters( String filters ) {
    this.filters = filters;
  }

  public String getSort() {
    return sort;
  }

  public void setSort( String sort ) {
    this.sort = sort;
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate( String startDate ) {
    this.startDate = startDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate( String endDate ) {
    this.endDate = endDate;
  }

  public String getGaEmail() {
    return gaEmail;
  }

  public void setGaEmail( String email ) {
    this.gaEmail = email;
  }

  public String getGaPassword() {
    return gaPassword;
  }

  public void setGaPassword( String gaPassword ) {
    this.gaPassword = gaPassword;
  }

  public String getGaProfileTableId() {
    return gaProfileTableId;
  }

  public void setGaProfileTableId( String gaProfile ) {
    this.gaProfileTableId = gaProfile;
  }

  public String getGaProfileName() {
    return gaProfileName;
  }

  public void setGaProfileName( String gaProfileName ) {
    this.gaProfileName = gaProfileName;
  }

  public String[] getFeedFieldType() {
    return feedFieldType;
  }

  public String[] getFeedField() {
    return feedField;
  }

  public String[] getOutputField() {
    return outputField;
  }

  public int[] getOutputType() {
    return outputType;
  }

  public String getGaApiKey() {
    return gaApiKey;
  }

  public void setGaApiKey( String gaApiKey ) {
    this.gaApiKey = gaApiKey;
  }

  // set sensible defaults for a new step
  public void setDefault() {
    gaEmail = "your.account@googlemail.com";
    useSegment = true;
    segmentId = "gaid::-1";
    segmentName = "All Visits";
    dimensions = "ga:browser";
    metrics = "ga:visits";
    startDate = new SimpleDateFormat( "yyyy-MM-dd" ).format( new Date() );
    endDate = new String( startDate );
    sort = "-ga:visits";
    gaAppName = DEFAULT_GA_APPLICATION_NAME;
    rowLimit = 0;
    gaApiKey = "";
    // default is to have no key lookup settings
    allocate( 0 );

  }

  // helper method to allocate the arrays
  public void allocate( int nrkeys ) {

    feedField = new String[nrkeys];
    outputField = new String[nrkeys];
    outputType = new int[nrkeys];
    feedFieldType = new String[nrkeys];
    conversionMask = new String[nrkeys];
  }

  public void getFields( RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) {

    // clear the output
    r.clear();
    // append the outputFields to the output
    for ( int i = 0; i < outputField.length; i++ ) {
      ValueMetaInterface v = new ValueMeta( outputField[i], outputType[i] );
      // that would influence the output
      // v.setConversionMask(conversionMask[i]);
      v.setOrigin( origin );
      r.addValueMeta( v );
    }

  }

  public Object clone() {

    // field by field copy is default
    GaInputStepMeta retval = (GaInputStepMeta) super.clone();

    // add proper deep copy for the collections
    int nrKeys = feedField.length;

    retval.allocate( nrKeys );

    for ( int i = 0; i < nrKeys; i++ ) {
      retval.feedField[i] = feedField[i];
      retval.outputField[i] = outputField[i];
      retval.outputType[i] = outputType[i];
      retval.feedFieldType[i] = feedFieldType[i];
      retval.conversionMask[i] = conversionMask[i];
    }

    return retval;
  }

  private boolean getBooleanAttributeFromNode( Node node, String tag ) {
    String sValue = XMLHandler.getTagValue( node, tag );
    return ( sValue != null && sValue.equalsIgnoreCase( "Y" ) );

  }

  public String getXML() throws KettleValueException {

    StringBuffer retval = new StringBuffer( 800 );

    retval.append( "    " ).append( XMLHandler.addTagValue( "user", gaEmail ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "pass", "Encrypted " + Encr.encryptPassword( gaPassword ) ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "appName", gaAppName ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "apiKey", "Encrypted " + Encr.encryptPassword( gaApiKey ) ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "profileName", gaProfileName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "profileTableId", gaProfileTableId ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "customTableId", gaCustomTableId ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "useCustomTableId", useCustomTableId ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "startDate", startDate ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "endDate", endDate ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "dimensions", dimensions ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "metrics", metrics ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "filters", filters ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "sort", sort ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "useSegment", useSegment ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "useCustomSegment", useCustomSegment ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "customSegment", customSegment ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "segmentId", segmentId ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "segmentName", segmentName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rowLimit", rowLimit ) );

    for ( int i = 0; i < feedField.length; i++ ) {
      retval.append( "      <feedField>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "feedFieldType", feedFieldType[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "feedField", feedField[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "outField", outputField[i] ) );
      retval
        .append( "        " ).append( XMLHandler.addTagValue( "type", ValueMeta.getTypeDesc( outputType[i] ) ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "conversionMask", conversionMask[i] ) );
      retval.append( "      </feedField>" ).append( Const.CR );
    }

    return retval.toString();
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {

    try {

      gaEmail = XMLHandler.getTagValue( stepnode, "user" );
      gaPassword = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, "pass" ) );
      gaAppName = XMLHandler.getTagValue( stepnode, "appName" );
      gaApiKey = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, "apiKey" ) );
      gaProfileName = XMLHandler.getTagValue( stepnode, "profileName" );
      gaProfileTableId = XMLHandler.getTagValue( stepnode, "profileTableId" );
      gaCustomTableId = XMLHandler.getTagValue( stepnode, "customTableId" );
      useCustomTableId = getBooleanAttributeFromNode( stepnode, "useCustomTableId" );
      startDate = XMLHandler.getTagValue( stepnode, "startDate" );
      endDate = XMLHandler.getTagValue( stepnode, "endDate" );
      dimensions = XMLHandler.getTagValue( stepnode, "dimensions" );
      metrics = XMLHandler.getTagValue( stepnode, "metrics" );
      filters = XMLHandler.getTagValue( stepnode, "filters" );
      sort = XMLHandler.getTagValue( stepnode, "sort" );
      useSegment =
        XMLHandler.getTagValue( stepnode, "useSegment" ) == null ? true : getBooleanAttributeFromNode(
          stepnode, "useSegment" ); // assume true for non-present
      useCustomSegment = getBooleanAttributeFromNode( stepnode, "useCustomSegment" );
      customSegment = XMLHandler.getTagValue( stepnode, "customSegment" );
      segmentId = XMLHandler.getTagValue( stepnode, "segmentId" );
      segmentName = XMLHandler.getTagValue( stepnode, "segmentName" );
      rowLimit = Const.toInt( XMLHandler.getTagValue( stepnode, "rowLimit" ), 0 );

      allocate( 0 );

      int nrFields = XMLHandler.countNodes( stepnode, "feedField" );
      allocate( nrFields );

      for ( int i = 0; i < nrFields; i++ ) {
        Node knode = XMLHandler.getSubNodeByNr( stepnode, "feedField", i );

        feedFieldType[i] = XMLHandler.getTagValue( knode, "feedFieldType" );
        feedField[i] = XMLHandler.getTagValue( knode, "feedField" );
        outputField[i] = XMLHandler.getTagValue( knode, "outField" );
        outputType[i] = ValueMeta.getType( XMLHandler.getTagValue( knode, "type" ) );
        conversionMask[i] = XMLHandler.getTagValue( knode, "conversionMask" );

        if ( outputType[i] < 0 ) {
          outputType[i] = ValueMetaInterface.TYPE_STRING;
        }

      }

    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "GoogleAnalytics.Error.UnableToReadFromXML" ), e );
    }

  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {

      gaEmail = rep.getStepAttributeString( id_step, "user" );
      gaPassword = Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( id_step, "pass" ) );
      gaApiKey = Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( id_step, "apiKey" ) );
      gaProfileName = rep.getStepAttributeString( id_step, "profileName" );
      gaAppName = rep.getStepAttributeString( id_step, "appName" );
      gaProfileTableId = rep.getStepAttributeString( id_step, "profileTableId" );
      gaCustomTableId = rep.getStepAttributeString( id_step, "customTableId" );
      useCustomTableId = rep.getStepAttributeBoolean( id_step, "useCustomTableId" );
      startDate = rep.getStepAttributeString( id_step, "startDate" );
      endDate = rep.getStepAttributeString( id_step, "endDate" );
      dimensions = rep.getStepAttributeString( id_step, "dimensions" );
      metrics = rep.getStepAttributeString( id_step, "metrics" );
      filters = rep.getStepAttributeString( id_step, "filters" );
      sort = rep.getStepAttributeString( id_step, "sort" );
      useSegment = rep.getStepAttributeBoolean( id_step, 0, "useSegment", true ); // assume default true, if not present
      useCustomSegment = rep.getStepAttributeBoolean( id_step, "useCustomSegment" );
      customSegment = rep.getStepAttributeString( id_step, "customSegment" );
      segmentId = rep.getStepAttributeString( id_step, "segmentId" );
      segmentName = rep.getStepAttributeString( id_step, "segmentName" );
      rowLimit = (int) rep.getStepAttributeInteger( id_step, "rowLimit" );

      int nrFields = rep.countNrStepAttributes( id_step, "feedField" );
      allocate( nrFields );

      for ( int i = 0; i < nrFields; i++ ) {

        feedFieldType[i] = rep.getStepAttributeString( id_step, i, "feedFieldType" );
        feedField[i] = rep.getStepAttributeString( id_step, i, "feedField" );
        outputField[i] = rep.getStepAttributeString( id_step, i, "outField" );
        outputType[i] = ValueMeta.getType( rep.getStepAttributeString( id_step, i, "type" ) );
        conversionMask[i] = rep.getStepAttributeString( id_step, i, "conversionMask" );

        if ( outputType[i] < 0 ) {
          outputType[i] = ValueMetaInterface.TYPE_STRING;
        }

      }

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "GoogleAnalytics.Error.UnableToReadFromRep" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "user", gaEmail );
      rep
        .saveStepAttribute( id_transformation, id_step, "pass", "Encrypted " + Encr.encryptPassword( gaPassword ) );
      rep
        .saveStepAttribute( id_transformation, id_step, "apiKey", "Encrypted " + Encr.encryptPassword( gaApiKey ) );
      rep.saveStepAttribute( id_transformation, id_step, "appName", gaAppName );
      rep.saveStepAttribute( id_transformation, id_step, "profileName", gaProfileName );
      rep.saveStepAttribute( id_transformation, id_step, "profileTableId", gaProfileTableId );
      rep.saveStepAttribute( id_transformation, id_step, "customTableId", gaCustomTableId );
      rep.saveStepAttribute( id_transformation, id_step, "useCustomTableId", useCustomTableId );
      rep.saveStepAttribute( id_transformation, id_step, "startDate", startDate );
      rep.saveStepAttribute( id_transformation, id_step, "endDate", endDate );
      rep.saveStepAttribute( id_transformation, id_step, "dimensions", dimensions );
      rep.saveStepAttribute( id_transformation, id_step, "metrics", metrics );
      rep.saveStepAttribute( id_transformation, id_step, "filters", filters );
      rep.saveStepAttribute( id_transformation, id_step, "sort", sort );
      rep.saveStepAttribute( id_transformation, id_step, "useSegment", useSegment );
      rep.saveStepAttribute( id_transformation, id_step, "useCustomSegment", useCustomSegment );
      rep.saveStepAttribute( id_transformation, id_step, "customSegment", customSegment );
      rep.saveStepAttribute( id_transformation, id_step, "segmentId", segmentId );
      rep.saveStepAttribute( id_transformation, id_step, "segmentName", segmentName );
      rep.saveStepAttribute( id_transformation, id_step, "rowLimit", rowLimit );

      for ( int i = 0; i < feedField.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "feedFieldType", feedFieldType[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "feedField", feedField[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "outField", outputField[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "conversionMask", conversionMask[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "type", ValueMeta.getTypeDesc( outputType[i] ) );

      }

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "GoogleAnalytics.Error.UnableToSaveToRep" )
        + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    if ( prev == null || prev.size() == 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "GoogleAnalytics.CheckResult.NotReceivingFields" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "GoogleAnalytics.CheckResult.StepRecevingData", prev.size() + "" ), stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "GoogleAnalytics.CheckResult.StepRecevingData2" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "GoogleAnalytics.CheckResult.NoInputReceivedFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    }

  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans disp ) {
    return new GaInputStep( stepMeta, stepDataInterface, cnr, transMeta, disp );
  }

  public StepDataInterface getStepData() {
    return new GaInputStepData();
  }

}
