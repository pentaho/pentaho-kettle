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

package org.pentaho.di.trans.steps.googleanalytics;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.AfterInjection;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.Utils;
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

@Step( id = "TypeExitGoogleAnalyticsInputStep",
  i18nPackageName = "org.pentaho.di.trans.steps.googleanalytics",
  name = "GoogleAnalytics.TypeLongDesc.GoogleAnalyticsStep",
  description = "GoogleAnalytics.TypeTooltipDesc.GoogleAnalyticsStep",
  categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Deprecated",
  image = "ui/images/deprecated.svg",

  documentationUrl = "http://wiki.pentaho.com/display/EAI/Google+Analytics" )
@InjectionSupported( localizationPrefix = "GoogleAnalytics.Injection.", groups = { "OUTPUT_FIELDS" } )
public class GaInputStepMeta extends BaseStepMeta implements StepMetaInterface {

  private static Class<?> PKG = GaInputStepMeta.class; // for i18n purposes

  public static final String FIELD_TYPE_DIMENSION = "Dimension";
  public static final String FIELD_TYPE_METRIC = "Metric";
  public static final String FIELD_TYPE_DATA_SOURCE_PROPERTY = "Data Source Property";
  public static final String FIELD_TYPE_DATA_SOURCE_FIELD = "Data Source Field";
  public static final String FIELD_DATA_SOURCE_TABLE_ID = "dxp:tableId";
  public static final String FIELD_DATA_SOURCE_TABLE_NAME = "dxp:tableName";
  public static final String PROPERTY_DATA_SOURCE_PROFILE_ID = "ga:profileId";
  public static final String PROPERTY_DATA_SOURCE_WEBPROP_ID = "ga:webPropertyId";
  public static final String PROPERTY_DATA_SOURCE_ACCOUNT_NAME = "ga:accountName";
  public static final String DEFAULT_GA_APPLICATION_NAME = "pdi-google-analytics-app";

  // The following is deprecated and removed by Google, and remains here only to allow old transformations to load
  // successfully in Spoon.
  public static final String DEPRECATED_FIELD_TYPE_CONFIDENCE_INTERVAL = "Confidence Interval for Metric";

  @Injection( name = "OAUTH_SERVICE_EMAIL" )
  private String oauthServiceAccount;
  @Injection( name = "OAUTH_KEYFILE" )
  private String oauthKeyFile;

  @Injection( name = "APPLICATION_NAME" )
  private String gaAppName;
  @Injection( name = "PROFILE_TABLE" )
  private String gaProfileTableId;
  @Injection( name = "PROFILE_NAME" )
  private String gaProfileName;
  @Injection( name = "USE_CUSTOM_TABLE_ID" )
  private boolean useCustomTableId;
  @Injection( name = "CUSTOM_TABLE_ID" )
  private String gaCustomTableId;
  @Injection( name = "START_DATE" )
  private String startDate;
  @Injection( name = "END_DATE" )
  private String endDate;
  @Injection( name = "DIMENSIONS" )
  private String dimensions;
  @Injection( name = "METRICS" )
  private String metrics;
  @Injection( name = "FILTERS" )
  private String filters;
  @Injection( name = "SORT" )
  private String sort;
  @Injection( name = "USE_SEGMENT" )
  private boolean useSegment;

  @Injection( name = "USE_CUSTOM_SEGMENT" )
  private boolean useCustomSegment;
  @Injection( name = "ROW_LIMIT" )
  private int rowLimit;

  @Injection( name = "CUSTOM_SEGMENT" )
  private String customSegment;
  @Injection( name = "SEGMENT_NAME" )
  private String segmentName;
  @Injection( name = "SEGMENT_ID" )
  private String segmentId;

  private String samplingLevel;
  public static final String[] TYPE_SAMPLING_LEVEL_CODE = new String[] { "DEFAULT", "FASTER", "HIGHER_PRECISION" };

  @Injection( name = "FEED_FIELD", group = "OUTPUT_FIELDS" )
  private String[] feedField;
  @Injection( name = "FEED_FIELD_TYPE", group = "OUTPUT_FIELDS" )
  private String[] feedFieldType;
  @Injection( name = "OUTPUT_FIELD", group = "OUTPUT_FIELDS" )
  private String[] outputField;
  @Injection( name = "OUTPUT_TYPE", group = "OUTPUT_FIELDS", converter = OutputTypeConverter.class )
  private int[] outputType;
  @Injection( name = "CONVERSION_MASK", group = "OUTPUT_FIELDS" )
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

  public void setConversionMask( String[] conversionMask ) {
    this.conversionMask = conversionMask;
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

  public String getSamplingLevel() {
    return samplingLevel;
  }

  public void setSamplingLevel( String samplingLevel ) {
    this.samplingLevel = samplingLevel;
  }

  public String[] getFeedFieldType() {
    return feedFieldType;
  }

  public void setFeedFieldType( String[] feedFieldType ) {
    this.feedFieldType = feedFieldType;
  }

  public String[] getFeedField() {
    return feedField;
  }

  public void setFeedField( String[] feedField ) {
    this.feedField = feedField;
  }

  public String[] getOutputField() {
    return outputField;
  }

  public void setOutputField( String[] outputField ) {
    this.outputField = outputField;
  }

  public int[] getOutputType() {
    return outputType;
  }

  public void setOutputType( int[] outputType ) {
    this.outputType = outputType;
  }

  public int getFieldsCount() {
    int count = Math.min( getFeedField().length, getFeedFieldType().length );
    count = Math.min( count, getOutputField().length );
    count = Math.min( count, getOutputType().length );
    count = Math.min( count, getConversionMask().length );
    return count;
  }

  // set sensible defaults for a new step
  @Override
  public void setDefault() {
    oauthServiceAccount = "service.account@developer.gserviceaccount.com";
    oauthKeyFile = "";
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
    samplingLevel = TYPE_SAMPLING_LEVEL_CODE[0];

    // default is to have no key lookup settings
    allocate( 0 );

  }

  // helper method to allocate the arrays
  public void allocate( int nrkeys ) {

    feedField = new String[ nrkeys ];
    outputField = new String[ nrkeys ];
    outputType = new int[ nrkeys ];
    feedFieldType = new String[ nrkeys ];
    conversionMask = new String[ nrkeys ];
  }

  @Override
  public void getFields( RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep,
                         VariableSpace space, Repository repository, IMetaStore metaStore ) {

    // clear the output
    r.clear();
    // append the outputFields to the output
    for ( int i = 0; i < outputField.length; i++ ) {
      ValueMetaInterface v;
      try {
        v = ValueMetaFactory.createValueMeta( outputField[ i ], outputType[ i ] );
      } catch ( KettlePluginException e ) {
        v = new ValueMetaString( outputField[ i ] );
      }
      // that would influence the output
      // v.setConversionMask(conversionMask[i]);
      v.setOrigin( origin );
      r.addValueMeta( v );
    }

  }

  @Override
  public Object clone() {

    // field by field copy is default
    GaInputStepMeta retval = (GaInputStepMeta) super.clone();

    // add proper deep copy for the collections
    int nrKeys = feedField.length;

    retval.allocate( nrKeys );

    for ( int i = 0; i < nrKeys; i++ ) {
      retval.feedField[ i ] = feedField[ i ];
      retval.outputField[ i ] = outputField[ i ];
      retval.outputType[ i ] = outputType[ i ];
      retval.feedFieldType[ i ] = feedFieldType[ i ];
      retval.conversionMask[ i ] = conversionMask[ i ];
    }

    return retval;
  }

  private boolean getBooleanAttributeFromNode( Node node, String tag ) {
    String sValue = XMLHandler.getTagValue( node, tag );
    return ( sValue != null && sValue.equalsIgnoreCase( "Y" ) );

  }

  @Override
  public String getXML() throws KettleValueException {

    StringBuilder retval = new StringBuilder( 800 );
    retval.append( "    " ).append( XMLHandler.addTagValue( "oauthServiceAccount", oauthServiceAccount ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "appName", gaAppName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "oauthKeyFile", oauthKeyFile ) );
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
    retval.append( "    " ).append( XMLHandler.addTagValue( "samplingLevel", samplingLevel ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rowLimit", rowLimit ) );

    for ( int i = 0; i < feedField.length; i++ ) {
      retval.append( "      <feedField>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "feedFieldType", feedFieldType[ i ] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "feedField", feedField[ i ] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "outField", outputField[ i ] ) );
      retval.append( "        " )
        .append( XMLHandler.addTagValue( "type", ValueMetaFactory.getValueMetaName( outputType[ i ] ) ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "conversionMask", conversionMask[ i ] ) );
      retval.append( "      </feedField>" ).append( Const.CR );
    }
    return retval.toString();
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {

    try {
      // Check for legacy fields (user/pass/API key), present an error if found
      String user = XMLHandler.getTagValue( stepnode, "user" );
      String pass = XMLHandler.getTagValue( stepnode, "pass" );
      String apiKey = XMLHandler.getTagValue( stepnode, "apiKey" );

      oauthServiceAccount = XMLHandler.getTagValue( stepnode, "oauthServiceAccount" );
      oauthKeyFile = XMLHandler.getTagValue( stepnode, "oauthKeyFile" );

      // Are we loading a legacy transformation?
      if ( ( user != null || pass != null || apiKey != null )
        && ( oauthServiceAccount == null && oauthKeyFile == null ) ) {
        logError( BaseMessages.getString( PKG, "GoogleAnalytics.Error.TransformationUpdateNeeded" ) );
      }
      gaAppName = XMLHandler.getTagValue( stepnode, "appName" );
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
      samplingLevel = XMLHandler.getTagValue( stepnode, "samplingLevel" );
      rowLimit = Const.toInt( XMLHandler.getTagValue( stepnode, "rowLimit" ), 0 );

      allocate( 0 );

      int nrFields = XMLHandler.countNodes( stepnode, "feedField" );
      allocate( nrFields );

      for ( int i = 0; i < nrFields; i++ ) {
        Node knode = XMLHandler.getSubNodeByNr( stepnode, "feedField", i );

        feedFieldType[ i ] = XMLHandler.getTagValue( knode, "feedFieldType" );
        feedField[ i ] = XMLHandler.getTagValue( knode, "feedField" );
        outputField[ i ] = XMLHandler.getTagValue( knode, "outField" );
        outputType[ i ] = ValueMetaFactory.getIdForValueMeta( XMLHandler.getTagValue( knode, "type" ) );
        conversionMask[ i ] = XMLHandler.getTagValue( knode, "conversionMask" );

        if ( outputType[ i ] < 0 ) {
          outputType[ i ] = ValueMetaInterface.TYPE_STRING;
        }

      }

    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "GoogleAnalytics.Error.UnableToReadFromXML" ), e );
    }

  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      String user = rep.getStepAttributeString( id_step, "user" );
      String pass = rep.getStepAttributeString( id_step, "pass" );
      String apiKey = rep.getStepAttributeString( id_step, "apiKey" );

      oauthServiceAccount = rep.getStepAttributeString( id_step, "oauthServiceAccount" );
      oauthKeyFile = rep.getStepAttributeString( id_step, "oauthKeyFile" );

      // Are we loading a legacy transformation?
      if ( ( user != null || pass != null || apiKey != null )
        && ( oauthServiceAccount == null && oauthKeyFile == null ) ) {
        logError( BaseMessages.getString( PKG, "GoogleAnalytics.Error.TransformationUpdateNeeded" ) );
      }

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
      samplingLevel = rep.getStepAttributeString( id_step, "samplingLevel" );
      rowLimit = (int) rep.getStepAttributeInteger( id_step, "rowLimit" );

      int nrFields = rep.countNrStepAttributes( id_step, "feedField" );
      allocate( nrFields );

      for ( int i = 0; i < nrFields; i++ ) {

        feedFieldType[ i ] = rep.getStepAttributeString( id_step, i, "feedFieldType" );
        feedField[ i ] = rep.getStepAttributeString( id_step, i, "feedField" );
        outputField[ i ] = rep.getStepAttributeString( id_step, i, "outField" );
        outputType[ i ] = ValueMetaFactory.getIdForValueMeta( rep.getStepAttributeString( id_step, i, "type" ) );
        conversionMask[ i ] = rep.getStepAttributeString( id_step, i, "conversionMask" );

        if ( outputType[ i ] < 0 ) {
          outputType[ i ] = ValueMetaInterface.TYPE_STRING;
        }
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "GoogleAnalytics.Error.UnableToReadFromRep" ), e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "oauthServiceAccount", oauthServiceAccount );
      rep.saveStepAttribute( id_transformation, id_step, "oauthKeyFile", oauthKeyFile );
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
      rep.saveStepAttribute( id_transformation, id_step, "samplingLevel", samplingLevel );
      rep.saveStepAttribute( id_transformation, id_step, "rowLimit", rowLimit );

      for ( int i = 0; i < feedField.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "feedFieldType", feedFieldType[ i ] );
        rep.saveStepAttribute( id_transformation, id_step, i, "feedField", feedField[ i ] );
        rep.saveStepAttribute( id_transformation, id_step, i, "outField", outputField[ i ] );
        rep.saveStepAttribute( id_transformation, id_step, i, "conversionMask", conversionMask[ i ] );
        rep.saveStepAttribute( id_transformation, id_step, i, "type", ValueMetaFactory.getValueMetaName( outputType[ i ] ) );

      }

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "GoogleAnalytics.Error.UnableToSaveToRep" )
        + id_step, e );
    }
  }

  @Override
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

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
                                TransMeta transMeta, Trans disp ) {
    return new GaInputStep( stepMeta, stepDataInterface, cnr, transMeta, disp );
  }

  @Override
  public StepDataInterface getStepData() {
    return new GaInputStepData();
  }

  public String getOAuthKeyFile() {
    return oauthKeyFile;
  }

  public void setOAuthKeyFile( String oauthKeyFile ) {
    this.oauthKeyFile = oauthKeyFile;
  }

  public String getOAuthServiceAccount() {
    return oauthServiceAccount;
  }


  public void setOAuthServiceAccount( String oauthServiceAccount ) {
    this.oauthServiceAccount = oauthServiceAccount;
  }

  /**
   * @deprecated use {@link #setOAuthServiceAccount(String)} instead
   * @param oauthServiceAccount
   */
  @Deprecated
  public void setOauthServiceAccount( String oauthServiceAccount ) {
    setOAuthServiceAccount( oauthServiceAccount );
  }

  /**
   * If we use injection we can have different arrays lengths.
   * We need synchronize them for consistency behavior with UI
   */
  @AfterInjection
  public void afterInjectionSynchronization() {
    int nrFields = ( feedField == null ) ? -1 : feedField.length;
    if ( nrFields <= 0 ) {
      return;
    }
    String[][] rtnStringArray = Utils.normalizeArrays( nrFields, feedFieldType, outputField, conversionMask );
    feedFieldType = rtnStringArray[ 0 ];
    outputField = rtnStringArray[ 1 ];
    conversionMask = rtnStringArray[ 2 ];

    int[][] rtnIntArray = Utils.normalizeArrays( nrFields, outputType );
    outputType = rtnIntArray[ 0 ];

  }

}
