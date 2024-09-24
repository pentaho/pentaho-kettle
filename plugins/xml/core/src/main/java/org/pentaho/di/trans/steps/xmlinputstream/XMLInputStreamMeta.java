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

package org.pentaho.di.trans.steps.xmlinputstream;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.Utils;
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

@Step( id = "XMLInputStream", image = "xml_input_stream.svg",
    i18nPackageName = "org.pentaho.di.trans.steps.xmlinputstream", name = "XMLInputStream.name",
    description = "XMLInputStream.description", categoryDescription = "XMLInputStream.category",
    documentationUrl = "mk-95pdia003/pdi-transformation-steps/xml-input-stream-stax" )
public class XMLInputStreamMeta extends BaseStepMeta implements StepMetaInterface {
  private static final int DEFAULT_STRING_LEN_FILENAME = 256; // default length for XML path
  private static final int DEFAULT_STRING_LEN_PATH = 1024; // default length for XML path
  public static final String DEFAULT_STRING_LEN = "1024"; // used by defaultStringLen
  public static final String DEFAULT_ENCODING = "UTF-8"; // used by encoding

  private String filename;
  private boolean addResultFile;

  /** The number of rows to ignore before sending rows to the next step */
  private String nrRowsToSkip; // String for variable usage, enables chunk loading defined in an outer loop

  /** The maximum number of lines to read */
  private String rowLimit; // String for variable usage, enables chunk loading defined in an outer loop

  /** This is the default String length for name/value elements & attributes */
  private String defaultStringLen; // default set to DEFAULT_STRING_LEN

  /** Encoding to be used */
  private String encoding; // default set to DEFAULT_ENCODING

  /** Enable Namespaces in the output? (will be slower) */
  private boolean enableNamespaces;

  /** Trim all name/value elements & attributes? */
  private boolean enableTrim; // trim is also eliminating white spaces, tab, cr, lf at the beginning and end of the
                              // string

  // The fields in the output stream
  private boolean includeFilenameField;
  private String filenameField;

  private boolean includeRowNumberField;
  private String rowNumberField;

  private boolean includeXmlDataTypeNumericField;
  private String xmlDataTypeNumericField;

  private boolean includeXmlDataTypeDescriptionField;
  private String xmlDataTypeDescriptionField;

  private boolean includeXmlLocationLineField;
  private String xmlLocationLineField;

  private boolean includeXmlLocationColumnField;
  private String xmlLocationColumnField;

  private boolean includeXmlElementIDField;
  private String xmlElementIDField;

  private boolean includeXmlParentElementIDField;
  private String xmlParentElementIDField;

  private boolean includeXmlElementLevelField;
  private String xmlElementLevelField;

  private boolean includeXmlPathField;
  private String xmlPathField;

  private boolean includeXmlParentPathField;
  private String xmlParentPathField;

  private boolean includeXmlDataNameField;
  private String xmlDataNameField;

  private boolean includeXmlDataValueField;
  private String xmlDataValueField;

  /** Are we accepting filenames in input rows? */
  public boolean sourceFromInput;

  /** The field in which the filename is placed */
  public String sourceFieldName;

  public XMLInputStreamMeta() {
    super(); // allocate BaseStepMeta
  }

  @Override
  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space, Repository repository, IMetaStore metaStore ) {
    int defaultStringLenNameValueElements =
        Const.toInt( space.environmentSubstitute( defaultStringLen ), new Integer( DEFAULT_STRING_LEN ) );

    if ( includeFilenameField ) {
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( filenameField ) );
      v.setLength( DEFAULT_STRING_LEN_FILENAME );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( includeRowNumberField ) {
      ValueMetaInterface v = new ValueMetaInteger( space.environmentSubstitute( rowNumberField ) );
      v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( includeXmlDataTypeNumericField ) {
      ValueMetaInterface vdtn =
          new ValueMetaInteger( space.environmentSubstitute( xmlDataTypeNumericField ) );
      vdtn.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH );
      vdtn.setOrigin( name );
      r.addValueMeta( vdtn );
    }

    if ( includeXmlDataTypeDescriptionField ) {
      ValueMetaInterface vdtd =
          new ValueMetaString( space.environmentSubstitute( xmlDataTypeDescriptionField ) );
      vdtd.setLength( 25 );
      vdtd.setOrigin( name );
      r.addValueMeta( vdtd );
    }

    if ( includeXmlLocationLineField ) {
      ValueMetaInterface vline =
          new ValueMetaInteger( space.environmentSubstitute( xmlLocationLineField ) );
      vline.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH );
      vline.setOrigin( name );
      r.addValueMeta( vline );
    }

    if ( includeXmlLocationColumnField ) {
      ValueMetaInterface vcol =
          new ValueMetaInteger( space.environmentSubstitute( xmlLocationColumnField ) );
      vcol.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH );
      vcol.setOrigin( name );
      r.addValueMeta( vcol );
    }

    if ( includeXmlElementIDField ) {
      ValueMetaInterface vdid = new ValueMetaInteger( "xml_element_id" );
      vdid.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH );
      vdid.setOrigin( name );
      r.addValueMeta( vdid );
    }

    if ( includeXmlParentElementIDField ) {
      ValueMetaInterface vdparentid = new ValueMetaInteger( "xml_parent_element_id" );
      vdparentid.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH );
      vdparentid.setOrigin( name );
      r.addValueMeta( vdparentid );
    }

    if ( includeXmlElementLevelField ) {
      ValueMetaInterface vdlevel = new ValueMetaInteger( "xml_element_level" );
      vdlevel.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH );
      vdlevel.setOrigin( name );
      r.addValueMeta( vdlevel );
    }

    if ( includeXmlPathField ) {
      ValueMetaInterface vdparentxp = new ValueMetaString( "xml_path" );
      vdparentxp.setLength( DEFAULT_STRING_LEN_PATH );
      vdparentxp.setOrigin( name );
      r.addValueMeta( vdparentxp );
    }

    if ( includeXmlParentPathField ) {
      ValueMetaInterface vdparentpxp = new ValueMetaString( "xml_parent_path" );
      vdparentpxp.setLength( DEFAULT_STRING_LEN_PATH );
      vdparentpxp.setOrigin( name );
      r.addValueMeta( vdparentpxp );
    }

    if ( includeXmlDataNameField ) {
      ValueMetaInterface vdname = new ValueMetaString( "xml_data_name" );
      vdname.setLength( defaultStringLenNameValueElements );
      vdname.setOrigin( name );
      r.addValueMeta( vdname );
    }

    if ( includeXmlDataValueField ) {
      ValueMetaInterface vdval = new ValueMetaString( "xml_data_value" );
      vdval.setLength( defaultStringLenNameValueElements );
      vdval.setOrigin( name );
      r.addValueMeta( vdval );
    }

  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    try {
      sourceFromInput = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "sourceFromInput" ) );
      sourceFieldName = Const.NVL( XMLHandler.getTagValue( stepnode, "sourceFieldName" ), "" );

      filename = Const.NVL( XMLHandler.getTagValue( stepnode, "filename" ), "" );
      addResultFile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "addResultFile" ) );

      nrRowsToSkip = Const.NVL( XMLHandler.getTagValue( stepnode, "nrRowsToSkip" ), "0" );
      rowLimit = Const.NVL( XMLHandler.getTagValue( stepnode, "rowLimit" ), "0" );
      defaultStringLen = Const.NVL( XMLHandler.getTagValue( stepnode, "defaultStringLen" ), DEFAULT_STRING_LEN );
      encoding = Const.NVL( XMLHandler.getTagValue( stepnode, "encoding" ), DEFAULT_ENCODING );
      enableNamespaces = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "enableNamespaces" ) );
      enableTrim = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "enableTrim" ) );

      // The fields in the output stream
      // When they are undefined (checked with NVL) the original default value will be taken
      includeFilenameField = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "includeFilenameField" ) );
      filenameField = Const.NVL( XMLHandler.getTagValue( stepnode, "filenameField" ), filenameField );

      includeRowNumberField = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "includeRowNumberField" ) );
      rowNumberField = Const.NVL( XMLHandler.getTagValue( stepnode, "rowNumberField" ), rowNumberField );

      includeXmlDataTypeNumericField =
          "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "includeDataTypeNumericField" ) );
      xmlDataTypeNumericField =
          Const.NVL( XMLHandler.getTagValue( stepnode, "dataTypeNumericField" ), xmlDataTypeNumericField );

      includeXmlDataTypeDescriptionField =
          "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "includeDataTypeDescriptionField" ) );
      xmlDataTypeDescriptionField =
          Const.NVL( XMLHandler.getTagValue( stepnode, "dataTypeDescriptionField" ), xmlDataTypeDescriptionField );

      includeXmlLocationLineField =
          "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "includeXmlLocationLineField" ) );
      xmlLocationLineField =
          Const.NVL( XMLHandler.getTagValue( stepnode, "xmlLocationLineField" ), xmlLocationLineField );

      includeXmlLocationColumnField =
          "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "includeXmlLocationColumnField" ) );
      xmlLocationColumnField =
          Const.NVL( XMLHandler.getTagValue( stepnode, "xmlLocationColumnField" ), xmlLocationColumnField );

      includeXmlElementIDField = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "includeXmlElementIDField" ) );
      xmlElementIDField = Const.NVL( XMLHandler.getTagValue( stepnode, "xmlElementIDField" ), xmlElementIDField );

      includeXmlParentElementIDField =
          "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "includeXmlParentElementIDField" ) );
      xmlParentElementIDField =
          Const.NVL( XMLHandler.getTagValue( stepnode, "xmlParentElementIDField" ), xmlParentElementIDField );

      includeXmlElementLevelField =
          "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "includeXmlElementLevelField" ) );
      xmlElementLevelField =
          Const.NVL( XMLHandler.getTagValue( stepnode, "xmlElementLevelField" ), xmlElementLevelField );

      includeXmlPathField = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "includeXmlPathField" ) );
      xmlPathField = Const.NVL( XMLHandler.getTagValue( stepnode, "xmlPathField" ), xmlPathField );

      includeXmlParentPathField =
          "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "includeXmlParentPathField" ) );
      xmlParentPathField = Const.NVL( XMLHandler.getTagValue( stepnode, "xmlParentPathField" ), xmlParentPathField );

      includeXmlDataNameField = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "includeXmlDataNameField" ) );
      xmlDataNameField = Const.NVL( XMLHandler.getTagValue( stepnode, "xmlDataNameField" ), xmlDataNameField );

      includeXmlDataValueField = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "includeXmlDataValueField" ) );
      xmlDataValueField = Const.NVL( XMLHandler.getTagValue( stepnode, "xmlDataValueField" ), xmlDataValueField );

    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  @Override
  public Object clone() {
    XMLInputStreamMeta retval = (XMLInputStreamMeta) super.clone();
    // TODO check

    return retval;
  }

  @Override
  public String getXML() {
    StringBuffer retval = new StringBuffer();
    retval.append( "    " + XMLHandler.addTagValue( "sourceFromInput", sourceFromInput ) );
    retval.append( "    " + XMLHandler.addTagValue( "sourceFieldName", sourceFieldName ) );
    retval.append( "    " + XMLHandler.addTagValue( "filename", filename ) );
    retval.append( "    " + XMLHandler.addTagValue( "addResultFile", addResultFile ) );

    retval.append( "    " + XMLHandler.addTagValue( "nrRowsToSkip", nrRowsToSkip ) );
    retval.append( "    " + XMLHandler.addTagValue( "rowLimit", rowLimit ) );
    retval.append( "    " + XMLHandler.addTagValue( "defaultStringLen", defaultStringLen ) );
    retval.append( "    " + XMLHandler.addTagValue( "encoding", encoding ) );
    retval.append( "    " + XMLHandler.addTagValue( "enableNamespaces", enableNamespaces ) );
    retval.append( "    " + XMLHandler.addTagValue( "enableTrim", enableTrim ) );

    // The fields in the output stream
    retval.append( "    " + XMLHandler.addTagValue( "includeFilenameField", includeFilenameField ) );
    retval.append( "    " + XMLHandler.addTagValue( "filenameField", filenameField ) );

    retval.append( "    " + XMLHandler.addTagValue( "includeRowNumberField", includeRowNumberField ) );
    retval.append( "    " + XMLHandler.addTagValue( "rowNumberField", rowNumberField ) );

    retval.append( "    " + XMLHandler.addTagValue( "includeDataTypeNumericField", includeXmlDataTypeNumericField ) );
    retval.append( "    " + XMLHandler.addTagValue( "dataTypeNumericField", xmlDataTypeNumericField ) );

    retval.append( "    "
        + XMLHandler.addTagValue( "includeDataTypeDescriptionField", includeXmlDataTypeDescriptionField ) );
    retval.append( "    " + XMLHandler.addTagValue( "dataTypeDescriptionField", xmlDataTypeDescriptionField ) );

    retval.append( "    " + XMLHandler.addTagValue( "includeXmlLocationLineField", includeXmlLocationLineField ) );
    retval.append( "    " + XMLHandler.addTagValue( "xmlLocationLineField", xmlLocationLineField ) );

    retval.append( "    " + XMLHandler.addTagValue( "includeXmlLocationColumnField", includeXmlLocationColumnField ) );
    retval.append( "    " + XMLHandler.addTagValue( "xmlLocationColumnField", xmlLocationColumnField ) );

    retval.append( "    " + XMLHandler.addTagValue( "includeXmlElementIDField", includeXmlElementIDField ) );
    retval.append( "    " + XMLHandler.addTagValue( "xmlElementIDField", xmlElementIDField ) );

    retval.append( "    " + XMLHandler.addTagValue( "includeXmlParentElementIDField", includeXmlParentElementIDField ) );
    retval.append( "    " + XMLHandler.addTagValue( "xmlParentElementIDField", xmlParentElementIDField ) );

    retval.append( "    " + XMLHandler.addTagValue( "includeXmlElementLevelField", includeXmlElementLevelField ) );
    retval.append( "    " + XMLHandler.addTagValue( "xmlElementLevelField", xmlElementLevelField ) );

    retval.append( "    " + XMLHandler.addTagValue( "includeXmlPathField", includeXmlPathField ) );
    retval.append( "    " + XMLHandler.addTagValue( "xmlPathField", xmlPathField ) );

    retval.append( "    " + XMLHandler.addTagValue( "includeXmlParentPathField", includeXmlParentPathField ) );
    retval.append( "    " + XMLHandler.addTagValue( "xmlParentPathField", xmlParentPathField ) );

    retval.append( "    " + XMLHandler.addTagValue( "includeXmlDataNameField", includeXmlDataNameField ) );
    retval.append( "    " + XMLHandler.addTagValue( "xmlDataNameField", xmlDataNameField ) );

    retval.append( "    " + XMLHandler.addTagValue( "includeXmlDataValueField", includeXmlDataValueField ) );
    retval.append( "    " + XMLHandler.addTagValue( "xmlDataValueField", xmlDataValueField ) );

    return retval.toString();
  }

  @Override
  public void setDefault() {
    filename = "";
    addResultFile = false;

    nrRowsToSkip = "0";
    rowLimit = "0";
    defaultStringLen = DEFAULT_STRING_LEN;
    encoding = DEFAULT_ENCODING;
    enableNamespaces = false;
    enableTrim = true;

    // The fields in the output stream
    includeFilenameField = false;
    filenameField = "xml_filename";

    includeRowNumberField = false;
    rowNumberField = "xml_row_number";

    includeXmlDataTypeNumericField = false;
    xmlDataTypeNumericField = "xml_data_type_numeric";

    includeXmlDataTypeDescriptionField = true;
    xmlDataTypeDescriptionField = "xml_data_type_description";

    includeXmlLocationLineField = false;
    xmlLocationLineField = "xml_location_line";

    includeXmlLocationColumnField = false;
    xmlLocationColumnField = "xml_location_column";

    includeXmlElementIDField = true;
    xmlElementIDField = "xml_element_id";

    includeXmlParentElementIDField = true;
    xmlParentElementIDField = "xml_parent_element_id";

    includeXmlElementLevelField = true;
    xmlElementLevelField = "xml_element_level";

    includeXmlPathField = true;
    xmlPathField = "xml_path";

    includeXmlParentPathField = true;
    xmlParentPathField = "xml_parent_path";

    includeXmlDataNameField = true;
    xmlDataNameField = "xml_data_name";

    includeXmlDataValueField = true;
    xmlDataValueField = "xml_data_value";

  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      sourceFromInput = rep.getStepAttributeBoolean( id_step, "sourceFromInput" );
      sourceFieldName = Const.NVL( rep.getStepAttributeString( id_step, "sourceFieldName" ), "" );
      filename = Const.NVL( rep.getStepAttributeString( id_step, "filename" ), "" );
      addResultFile = rep.getStepAttributeBoolean( id_step, "addResultFile" );

      nrRowsToSkip = Const.NVL( rep.getStepAttributeString( id_step, "nrRowsToSkip" ), "0" );
      rowLimit = Const.NVL( rep.getStepAttributeString( id_step, "rowLimit" ), "0" );
      defaultStringLen = Const.NVL( rep.getStepAttributeString( id_step, "defaultStringLen" ), DEFAULT_STRING_LEN );
      encoding = Const.NVL( rep.getStepAttributeString( id_step, "encoding" ), DEFAULT_ENCODING );
      enableNamespaces = rep.getStepAttributeBoolean( id_step, "enableNamespaces" );
      enableTrim = rep.getStepAttributeBoolean( id_step, "enableTrim" );

      // The fields in the output stream
      // When they are undefined (checked with NVL) the original default value will be taken
      includeFilenameField = rep.getStepAttributeBoolean( id_step, "includeFilenameField" );
      filenameField = Const.NVL( rep.getStepAttributeString( id_step, "filenameField" ), filenameField );

      includeRowNumberField = rep.getStepAttributeBoolean( id_step, "includeRowNumberField" );
      rowNumberField = Const.NVL( rep.getStepAttributeString( id_step, "rowNumberField" ), rowNumberField );

      includeXmlDataTypeNumericField = rep.getStepAttributeBoolean( id_step, "includeDataTypeNumericField" );
      xmlDataTypeNumericField =
          Const.NVL( rep.getStepAttributeString( id_step, "dataTypeNumericField" ), xmlDataTypeNumericField );

      includeXmlDataTypeDescriptionField = rep.getStepAttributeBoolean( id_step, "includeDataTypeDescriptionField" );
      xmlDataTypeDescriptionField =
          Const.NVL( rep.getStepAttributeString( id_step, "dataTypeDescriptionField" ), xmlDataTypeDescriptionField );

      includeXmlLocationLineField = rep.getStepAttributeBoolean( id_step, "includeXmlLocationLineField" );
      xmlLocationLineField =
          Const.NVL( rep.getStepAttributeString( id_step, "xmlLocationLineField" ), xmlLocationLineField );

      includeXmlLocationColumnField = rep.getStepAttributeBoolean( id_step, "includeXmlLocationColumnField" );
      xmlLocationColumnField =
          Const.NVL( rep.getStepAttributeString( id_step, "xmlLocationColumnField" ), xmlLocationColumnField );

      includeXmlElementIDField = rep.getStepAttributeBoolean( id_step, "includeXmlElementIDField" );
      xmlElementIDField = Const.NVL( rep.getStepAttributeString( id_step, "xmlElementIDField" ), xmlElementIDField );

      includeXmlParentElementIDField = rep.getStepAttributeBoolean( id_step, "includeXmlParentElementIDField" );
      xmlParentElementIDField =
          Const.NVL( rep.getStepAttributeString( id_step, "xmlParentElementIDField" ), xmlParentElementIDField );

      includeXmlElementLevelField = rep.getStepAttributeBoolean( id_step, "includeXmlElementLevelField" );
      xmlElementLevelField =
          Const.NVL( rep.getStepAttributeString( id_step, "xmlElementLevelField" ), xmlElementLevelField );

      includeXmlPathField = rep.getStepAttributeBoolean( id_step, "includeXmlPathField" );
      xmlPathField = Const.NVL( rep.getStepAttributeString( id_step, "xmlPathField" ), xmlPathField );

      includeXmlParentPathField = rep.getStepAttributeBoolean( id_step, "includeXmlParentPathField" );
      xmlParentPathField = Const.NVL( rep.getStepAttributeString( id_step, "xmlParentPathField" ), xmlParentPathField );

      includeXmlDataNameField = rep.getStepAttributeBoolean( id_step, "includeXmlDataNameField" );
      xmlDataNameField = Const.NVL( rep.getStepAttributeString( id_step, "xmlDataNameField" ), xmlDataNameField );

      includeXmlDataValueField = rep.getStepAttributeBoolean( id_step, "includeXmlDataValueField" );
      xmlDataValueField = Const.NVL( rep.getStepAttributeString( id_step, "xmlDataValueField" ), xmlDataValueField );

    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "sourceFromInput", sourceFromInput );
      rep.saveStepAttribute( id_transformation, id_step, "sourceFieldName", sourceFieldName );

      rep.saveStepAttribute( id_transformation, id_step, "filename", filename );
      rep.saveStepAttribute( id_transformation, id_step, "addResultFile", addResultFile );

      rep.saveStepAttribute( id_transformation, id_step, "nrRowsToSkip", nrRowsToSkip );
      rep.saveStepAttribute( id_transformation, id_step, "rowLimit", rowLimit );
      rep.saveStepAttribute( id_transformation, id_step, "defaultStringLen", defaultStringLen );
      rep.saveStepAttribute( id_transformation, id_step, "encoding", encoding );
      rep.saveStepAttribute( id_transformation, id_step, "enableNamespaces", enableNamespaces );
      rep.saveStepAttribute( id_transformation, id_step, "enableTrim", enableTrim );

      // The fields in the output stream
      rep.saveStepAttribute( id_transformation, id_step, "includeFilenameField", includeFilenameField );
      rep.saveStepAttribute( id_transformation, id_step, "filenameField", filenameField );

      rep.saveStepAttribute( id_transformation, id_step, "includeRowNumberField", includeRowNumberField );
      rep.saveStepAttribute( id_transformation, id_step, "rowNumberField", rowNumberField );

      rep.saveStepAttribute( id_transformation, id_step, "includeDataTypeNumericField", includeXmlDataTypeNumericField );
      rep.saveStepAttribute( id_transformation, id_step, "dataTypeNumericField", xmlDataTypeNumericField );

      rep.saveStepAttribute( id_transformation, id_step, "includeDataTypeDescriptionField",
          includeXmlDataTypeDescriptionField );
      rep.saveStepAttribute( id_transformation, id_step, "dataTypeDescriptionField", xmlDataTypeDescriptionField );

      rep.saveStepAttribute( id_transformation, id_step, "includeXmlLocationLineField", includeXmlLocationLineField );
      rep.saveStepAttribute( id_transformation, id_step, "xmlLocationLineField", xmlLocationLineField );

      rep.saveStepAttribute( id_transformation, id_step, "includeXmlLocationColumnField", includeXmlLocationColumnField );
      rep.saveStepAttribute( id_transformation, id_step, "xmlLocationColumnField", xmlLocationColumnField );

      rep.saveStepAttribute( id_transformation, id_step, "includeXmlElementIDField", includeXmlElementIDField );
      rep.saveStepAttribute( id_transformation, id_step, "xmlElementIDField", xmlElementIDField );

      rep.saveStepAttribute( id_transformation, id_step, "includeXmlParentElementIDField",
          includeXmlParentElementIDField );
      rep.saveStepAttribute( id_transformation, id_step, "xmlParentElementIDField", xmlParentElementIDField );

      rep.saveStepAttribute( id_transformation, id_step, "includeXmlElementLevelField", includeXmlElementLevelField );
      rep.saveStepAttribute( id_transformation, id_step, "xmlElementLevelField", xmlElementLevelField );

      rep.saveStepAttribute( id_transformation, id_step, "includeXmlPathField", includeXmlPathField );
      rep.saveStepAttribute( id_transformation, id_step, "xmlPathField", xmlPathField );

      rep.saveStepAttribute( id_transformation, id_step, "includeXmlParentPathField", includeXmlParentPathField );
      rep.saveStepAttribute( id_transformation, id_step, "xmlParentPathField", xmlParentPathField );

      rep.saveStepAttribute( id_transformation, id_step, "includeXmlDataNameField", includeXmlDataNameField );
      rep.saveStepAttribute( id_transformation, id_step, "xmlDataNameField", xmlDataNameField );

      rep.saveStepAttribute( id_transformation, id_step, "includeXmlDataValueField", includeXmlDataValueField );
      rep.saveStepAttribute( id_transformation, id_step, "xmlDataValueField", xmlDataValueField );

    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {
    // TODO externalize messages
    CheckResult cr;
    if ( Utils.isEmpty( filename ) ) {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "Filename is not given", stepMeta );
    } else {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, "Filename is given", stepMeta );
    }
    remarks.add( cr );

    if ( transMeta.findNrPrevSteps( stepMeta ) > 0 ) {
      RowMetaInterface previousFields;
      try {
        previousFields = transMeta.getPrevStepFields( stepMeta );
        if ( null == previousFields.searchValueMeta( filename ) ) {
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "Field name is not in previous step", stepMeta );
        } else {
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, "Field name is in previous step", stepMeta );
        }
      } catch ( KettleStepException e ) {
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "Could not find previous step", stepMeta );
      }
      remarks.add( cr );
    }

    if ( includeXmlDataTypeNumericField || includeXmlDataTypeDescriptionField ) {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_COMMENT,
              "At least one Data Type field (numeric or description) is in the data stream", stepMeta );
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING,
              "Data Type field (numeric or description) is missing in the data stream", stepMeta );
    }
    remarks.add( cr );

    if ( includeXmlDataValueField && includeXmlDataNameField ) {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_COMMENT,
              "Data Name and Data Value fields are in the data stream", stepMeta );
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING,
              "Both Data Name and Data Value fields should be in the data stream", stepMeta );
    }
    remarks.add( cr );
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
      Trans trans ) {
    return new XMLInputStream( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new XMLInputStreamData();
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename( String filename ) {
    this.filename = filename;
  }

  public boolean isAddResultFile() {
    return addResultFile;
  }

  public void setAddResultFile( boolean addResultFile ) {
    this.addResultFile = addResultFile;
  }

  public String getNrRowsToSkip() {
    return nrRowsToSkip;
  }

  public void setNrRowsToSkip( String nrRowsToSkip ) {
    this.nrRowsToSkip = nrRowsToSkip;
  }

  public String getRowLimit() {
    return rowLimit;
  }

  public void setRowLimit( String rowLimit ) {
    this.rowLimit = rowLimit;
  }

  public String getDefaultStringLen() {
    return defaultStringLen;
  }

  public void setDefaultStringLen( String defaultStringLen ) {
    this.defaultStringLen = defaultStringLen;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding( String encoding ) {
    this.encoding = encoding;
  }

  public boolean isEnableNamespaces() {
    return enableNamespaces;
  }

  public void setEnableNamespaces( boolean enableNamespaces ) {
    this.enableNamespaces = enableNamespaces;
  }

  public boolean isEnableTrim() {
    return enableTrim;
  }

  public void setEnableTrim( boolean enableTrim ) {
    this.enableTrim = enableTrim;
  }

  public boolean isIncludeFilenameField() {
    return includeFilenameField;
  }

  public void setIncludeFilenameField( boolean includeFilenameField ) {
    this.includeFilenameField = includeFilenameField;
  }

  public String getFilenameField() {
    return filenameField;
  }

  public void setFilenameField( String filenameField ) {
    this.filenameField = filenameField;
  }

  public boolean isIncludeRowNumberField() {
    return includeRowNumberField;
  }

  public void setIncludeRowNumberField( boolean includeRowNumberField ) {
    this.includeRowNumberField = includeRowNumberField;
  }

  public String getRowNumberField() {
    return rowNumberField;
  }

  public void setRowNumberField( String rowNumberField ) {
    this.rowNumberField = rowNumberField;
  }

  public boolean isIncludeXmlDataTypeNumericField() {
    return includeXmlDataTypeNumericField;
  }

  public void setIncludeXmlDataTypeNumericField( boolean includeXmlDataTypeNumericField ) {
    this.includeXmlDataTypeNumericField = includeXmlDataTypeNumericField;
  }

  public String getXmlDataTypeNumericField() {
    return xmlDataTypeNumericField;
  }

  public void setXmlDataTypeNumericField( String xmlDataTypeNumericField ) {
    this.xmlDataTypeNumericField = xmlDataTypeNumericField;
  }

  public boolean isIncludeXmlDataTypeDescriptionField() {
    return includeXmlDataTypeDescriptionField;
  }

  public void setIncludeXmlDataTypeDescriptionField( boolean includeXmlDataTypeDescriptionField ) {
    this.includeXmlDataTypeDescriptionField = includeXmlDataTypeDescriptionField;
  }

  public String getXmlDataTypeDescriptionField() {
    return xmlDataTypeDescriptionField;
  }

  public void setXmlDataTypeDescriptionField( String xmlDataTypeDescriptionField ) {
    this.xmlDataTypeDescriptionField = xmlDataTypeDescriptionField;
  }

  public boolean isIncludeXmlLocationLineField() {
    return includeXmlLocationLineField;
  }

  public void setIncludeXmlLocationLineField( boolean includeXmlLocationLineField ) {
    this.includeXmlLocationLineField = includeXmlLocationLineField;
  }

  public String getXmlLocationLineField() {
    return xmlLocationLineField;
  }

  public void setXmlLocationLineField( String xmlLocationLineField ) {
    this.xmlLocationLineField = xmlLocationLineField;
  }

  public boolean isIncludeXmlLocationColumnField() {
    return includeXmlLocationColumnField;
  }

  public void setIncludeXmlLocationColumnField( boolean includeXmlLocationColumnField ) {
    this.includeXmlLocationColumnField = includeXmlLocationColumnField;
  }

  public String getXmlLocationColumnField() {
    return xmlLocationColumnField;
  }

  public void setXmlLocationColumnField( String xmlLocationColumnField ) {
    this.xmlLocationColumnField = xmlLocationColumnField;
  }

  public boolean isIncludeXmlElementIDField() {
    return includeXmlElementIDField;
  }

  public void setIncludeXmlElementIDField( boolean includeXmlElementIDField ) {
    this.includeXmlElementIDField = includeXmlElementIDField;
  }

  public String getXmlElementIDField() {
    return xmlElementIDField;
  }

  public void setXmlElementIDField( String xmlElementIDField ) {
    this.xmlElementIDField = xmlElementIDField;
  }

  public boolean isIncludeXmlParentElementIDField() {
    return includeXmlParentElementIDField;
  }

  public void setIncludeXmlParentElementIDField( boolean includeXmlParentElementIDField ) {
    this.includeXmlParentElementIDField = includeXmlParentElementIDField;
  }

  public String getXmlParentElementIDField() {
    return xmlParentElementIDField;
  }

  public void setXmlParentElementIDField( String xmlParentElementIDField ) {
    this.xmlParentElementIDField = xmlParentElementIDField;
  }

  public boolean isIncludeXmlElementLevelField() {
    return includeXmlElementLevelField;
  }

  public void setIncludeXmlElementLevelField( boolean includeXmlElementLevelField ) {
    this.includeXmlElementLevelField = includeXmlElementLevelField;
  }

  public String getXmlElementLevelField() {
    return xmlElementLevelField;
  }

  public void setXmlElementLevelField( String xmlElementLevelField ) {
    this.xmlElementLevelField = xmlElementLevelField;
  }

  public boolean isIncludeXmlPathField() {
    return includeXmlPathField;
  }

  public void setIncludeXmlPathField( boolean includeXmlPathField ) {
    this.includeXmlPathField = includeXmlPathField;
  }

  public String getXmlPathField() {
    return xmlPathField;
  }

  public void setXmlPathField( String xmlPathField ) {
    this.xmlPathField = xmlPathField;
  }

  public boolean isIncludeXmlParentPathField() {
    return includeXmlParentPathField;
  }

  public void setIncludeXmlParentPathField( boolean includeXmlParentPathField ) {
    this.includeXmlParentPathField = includeXmlParentPathField;
  }

  public String getXmlParentPathField() {
    return xmlParentPathField;
  }

  public void setXmlParentPathField( String xmlParentPathField ) {
    this.xmlParentPathField = xmlParentPathField;
  }

  public boolean isIncludeXmlDataNameField() {
    return includeXmlDataNameField;
  }

  public void setIncludeXmlDataNameField( boolean includeXmlDataNameField ) {
    this.includeXmlDataNameField = includeXmlDataNameField;
  }

  public String getXmlDataNameField() {
    return xmlDataNameField;
  }

  public void setXmlDataNameField( String xmlDataNameField ) {
    this.xmlDataNameField = xmlDataNameField;
  }

  public boolean isIncludeXmlDataValueField() {
    return includeXmlDataValueField;
  }

  public void setIncludeXmlDataValueField( boolean includeXmlDataValueField ) {
    this.includeXmlDataValueField = includeXmlDataValueField;
  }

  public String getXmlDataValueField() {
    return xmlDataValueField;
  }

  public void setXmlDataValueField( String xmlDataValueField ) {
    this.xmlDataValueField = xmlDataValueField;
  }

}
