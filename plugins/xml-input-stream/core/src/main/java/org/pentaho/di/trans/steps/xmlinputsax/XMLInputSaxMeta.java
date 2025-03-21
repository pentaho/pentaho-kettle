/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.xmlinputsax;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

@Step( id = "XMLInputSax", image = "XIS.svg",
i18nPackageName = "org.pentaho.di.trans.steps.xmlinputsax", name = "XMLInputSaxDialog.Name",
description = "XMLInputSaxDialog.Description",
categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.Deprecated" )

public class XMLInputSaxMeta extends BaseStepMeta implements StepMetaInterface {
  /** Array of filenames */
  private String[] fileName;

  /** Wildcard or filemask (regular expression) */
  private String[] fileMask;

  /** Flag indicating that we should include the filename in the output */
  private boolean includeFilename;

  /** The name of the field in the output containing the filename */
  private String filenameField;

  /** Flag indicating that a row number field should be included in the output */
  private boolean includeRowNumber;

  /** The name of the field in the output containing the row number */
  private String rowNumberField;

  /** The maximum number or lines to read */
  private long rowLimit;

  /** The fields to import... */
  private XMLInputSaxField[] inputFields;

  /** The position in the XML documents to start (elements) */
  private XMLInputSaxFieldPosition[] inputPosition;

  private List<String> definitionElement;
  private List<String> definitionAttribute;

  public XMLInputSaxMeta() {
    super(); // allocate BaseStepMeta
    definitionElement = new ArrayList<String>();
    definitionAttribute = new ArrayList<String>();
  }

  /**
   * @return Returns the input fields.
   */
  public XMLInputSaxField[] getInputFields() {
    return inputFields;
  }

  /**
   * @param inputFields
   *          The input fields to set.
   */
  public void setInputFields( XMLInputSaxField[] inputFields ) {
    this.inputFields = inputFields;
  }

  /**
   * @return Returns the fileMask.
   */
  public String[] getFileMask() {
    return fileMask;
  }

  /**
   * @param fileMask
   *          The fileMask to set.
   */
  public void setFileMask( String[] fileMask ) {
    this.fileMask = fileMask;
  }

  /**
   * @return Returns the fileName.
   */
  public String[] getFileName() {
    return fileName;
  }

  /**
   * @param fileName
   *          The fileName to set.
   */
  public void setFileName( String[] fileName ) {
    this.fileName = fileName;
  }

  /**
   * @return Returns the filenameField.
   */
  public String getFilenameField() {
    return filenameField;
  }

  /**
   * @param filenameField
   *          The filenameField to set.
   */
  public void setFilenameField( String filenameField ) {
    this.filenameField = filenameField;
  }

  /**
   * @return Returns the includeFilename.
   */
  public boolean includeFilename() {
    return includeFilename;
  }

  /**
   * @param includeFilename
   *          The includeFilename to set.
   */
  public void setIncludeFilename( boolean includeFilename ) {
    this.includeFilename = includeFilename;
  }

  /**
   * @return Returns the includeRowNumber.
   */
  public boolean includeRowNumber() {
    return includeRowNumber;
  }

  /**
   * @param includeRowNumber
   *          The includeRowNumber to set.
   */
  public void setIncludeRowNumber( boolean includeRowNumber ) {
    this.includeRowNumber = includeRowNumber;
  }

  /**
   * @return Returns the rowLimit.
   */
  public long getRowLimit() {
    return rowLimit;
  }

  /**
   * @param rowLimit
   *          The rowLimit to set.
   */
  public void setRowLimit( long rowLimit ) {
    this.rowLimit = rowLimit;
  }

  /**
   * @return Returns the rowNumberField.
   */
  public String getRowNumberField() {
    return rowNumberField;
  }

  /**
   * @param rowNumberField
   *          The rowNumberField to set.
   */
  public void setRowNumberField( String rowNumberField ) {
    this.rowNumberField = rowNumberField;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public Object clone() {
    XMLInputSaxMeta retval = (XMLInputSaxMeta) super.clone();

    int nrFiles = fileName.length;
    int nrAttributes = getDefinitionLength();
    int nrFields = inputFields.length;
    int nrPositions = inputPosition.length;

    retval.allocate( nrFiles, nrFields, nrPositions );

    for ( int i = 0; i < nrAttributes; i++ ) {
      retval.setDefiningAttribute( getDefiningElement( i ), getDefiningAttribute( i ) );
    }

    for ( int i = 0; i < nrFields; i++ ) {
      if ( inputFields[i] != null ) {
        retval.inputFields[i] = (XMLInputSaxField) inputFields[i].clone();
      }
    }

    for ( int i = 0; i < nrPositions; i++ ) {
      retval.inputPosition[i] = inputPosition[i];
    }

    return retval;
  }

  public String getXML() {
    String retval = "";

    retval += "    " + XMLHandler.addTagValue( "include", includeFilename );
    retval += "    " + XMLHandler.addTagValue( "include_field", filenameField );
    retval += "    " + XMLHandler.addTagValue( "rownum", includeRowNumber );
    retval += "    " + XMLHandler.addTagValue( "rownum_field", rowNumberField );

    retval += "    <file>" + Const.CR;
    for ( int i = 0; i < fileName.length; i++ ) {
      retval += "      " + XMLHandler.addTagValue( "name", fileName[i] );
      retval += "      " + XMLHandler.addTagValue( "filemask", fileMask[i] );
    }
    retval += "      </file>" + Const.CR;

    retval += "    <def_attributes>" + Const.CR;
    for ( int i = 0; i < definitionElement.size(); i++ ) {
      retval += "      " + XMLHandler.addTagValue( "def_element", getDefiningElement( i ) );
      retval += "      " + XMLHandler.addTagValue( "def_attribute", getDefiningAttribute( i ) );
    }
    retval += "      </def_attributes>" + Const.CR;

    retval += "    <fields>" + Const.CR;
    for ( int i = 0; i < inputFields.length; i++ ) {
      XMLInputSaxField field = inputFields[i];
      retval += field.getXML();
    }
    retval += "      </fields>" + Const.CR;

    retval += "    <positions>" + Const.CR;
    for ( int i = 0; i < inputPosition.length; i++ ) {
      retval += "      " + XMLHandler.addTagValue( "position", inputPosition[i].toString() );
    }

    retval += "      </positions>" + Const.CR;

    retval += "    " + XMLHandler.addTagValue( "limit", rowLimit );

    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      String lim;

      includeFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "include" ) );
      filenameField = XMLHandler.getTagValue( stepnode, "include_field" );
      includeRowNumber = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "rownum" ) );
      rowNumberField = XMLHandler.getTagValue( stepnode, "rownum_field" );

      Node filenode = XMLHandler.getSubNode( stepnode, "file" );
      Node attributes = XMLHandler.getSubNode( stepnode, "def_attributes" );
      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      Node positions = XMLHandler.getSubNode( stepnode, "positions" );
      int nrFiles = XMLHandler.countNodes( filenode, "name" );
      int nrAttributes = XMLHandler.countNodes( attributes, "def_element" );
      int nrFields = XMLHandler.countNodes( fields, "field" );
      int nrPositions = XMLHandler.countNodes( positions, "position" );

      allocate( nrFiles, nrFields, nrPositions );

      for ( int i = 0; i < nrFiles; i++ ) {
        Node filenamenode = XMLHandler.getSubNodeByNr( filenode, "name", i );
        Node filemasknode = XMLHandler.getSubNodeByNr( filenode, "filemask", i );
        fileName[i] = XMLHandler.getNodeValue( filenamenode );
        fileMask[i] = XMLHandler.getNodeValue( filemasknode );
      }

      this.clearDefinition();
      for ( int i = 0; i < nrAttributes; i++ ) {
        Node elementnode = XMLHandler.getSubNodeByNr( attributes, "def_element", i );
        Node attributenode = XMLHandler.getSubNodeByNr( attributes, "def_attribute", i );
        String a = XMLHandler.getNodeValue( elementnode );
        String b = XMLHandler.getNodeValue( attributenode );
        this.setDefiningAttribute( a, b );
      }

      for ( int i = 0; i < nrFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
        XMLInputSaxField field = new XMLInputSaxField( fnode );
        inputFields[i] = field;
      }

      for ( int i = 0; i < nrPositions; i++ ) {
        Node positionnode = XMLHandler.getSubNodeByNr( positions, "position", i );
        String encoded = XMLHandler.getNodeValue( positionnode );
        inputPosition[i] = new XMLInputSaxFieldPosition( encoded );
      }

      // Is there a limit on the number of rows we process?
      lim = XMLHandler.getTagValue( stepnode, "limit" );
      rowLimit = Const.toLong( lim, 0L );
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public void allocate( int nrfiles, int nrfields, int nrPositions ) {
    fileName = new String[nrfiles];
    fileMask = new String[nrfiles];

    inputFields = new XMLInputSaxField[nrfields];

    inputPosition = new XMLInputSaxFieldPosition[nrPositions];
    definitionElement = new ArrayList<String>();
    definitionAttribute = new ArrayList<String>();
  }

  public void setDefault() {
    includeFilename = false;
    filenameField = "";
    includeRowNumber = false;
    rowNumberField = "";

    int nrFiles = 0;
    int nrFields = 0;
    int nrPositions = 0;

    allocate( nrFiles, nrFields, nrPositions );

    for ( int i = 0; i < nrFiles; i++ ) {
      fileName[i] = "filename" + ( i + 1 );
      fileMask[i] = "";
    }

    for ( int i = 0; i < nrFields; i++ ) {
      inputFields[i] = new XMLInputSaxField( "field" + ( i + 1 ), null );
    }

    for ( int i = 0; i < nrPositions; i++ ) {
      try {
        inputPosition[i] =
          new XMLInputSaxFieldPosition( "position" + ( i + 1 ), XMLInputSaxFieldPosition.XML_ELEMENT_POS );
      } catch ( KettleValueException e ) {
        log.logError( Const.getStackTracker( e ) );
      }
    }

    rowLimit = 0L;
  }

  @Override
  public void getFields( Bowl bowl, RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    for ( int i = 0; i < inputFields.length; i++ ) {
      XMLInputSaxField field = inputFields[i];

      int type = field.getType();
      if ( type == ValueMeta.TYPE_NONE ) {
        type = ValueMeta.TYPE_STRING;
      }

      try {
        ValueMetaInterface v = ValueMetaFactory.createValueMeta( field.getName(), type );
        v.setLength( field.getLength() );
        v.setPrecision( field.getPrecision() );
        v.setConversionMask( field.getFormat() );
        v.setGroupingSymbol( field.getGroupSymbol() );
        v.setDecimalSymbol( field.getDecimalSymbol() );
        v.setCurrencySymbol( field.getCurrencySymbol() );

        v.setOrigin( name );
        row.addValueMeta( v );
      } catch ( Exception e ) {
        throw new KettleStepException( e );
      }
    }
    if ( includeFilename ) {
      ValueMeta v = new ValueMeta( filenameField, ValueMeta.TYPE_STRING );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      row.addValueMeta( v );
    }
    if ( includeRowNumber ) {
      ValueMeta v = new ValueMeta( rowNumberField, ValueMeta.TYPE_NUMBER );
      v.setLength( 7, 0 );
      v.setOrigin( name );
      row.addValueMeta( v );
    }
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      includeFilename = rep.getStepAttributeBoolean( id_step, "include" );
      filenameField = rep.getStepAttributeString( id_step, "include_field" );

      includeRowNumber = rep.getStepAttributeBoolean( id_step, "rownum" );
      rowNumberField = rep.getStepAttributeString( id_step, "rownum_field" );
      rowLimit = (int) rep.getStepAttributeInteger( id_step, "limit" );

      int nrFiles = rep.countNrStepAttributes( id_step, "file_name" );
      int nrAttributes = rep.countNrStepAttributes( id_step, "def_element" );
      int nrFields = rep.countNrStepAttributes( id_step, "field_name" );
      int nrPositions = rep.countNrStepAttributes( id_step, "input_position" );

      allocate( nrFiles, nrFields, nrPositions );

      for ( int i = 0; i < nrFiles; i++ ) {
        fileName[i] = rep.getStepAttributeString( id_step, i, "file_name" );
        fileMask[i] = rep.getStepAttributeString( id_step, i, "file_mask" );
      }

      clearDefinition();
      for ( int i = 0; i < nrAttributes; i++ ) {
        String a = rep.getStepAttributeString( id_step, i, "def_element" );
        String b = rep.getStepAttributeString( id_step, i, "def_attribute" );
        this.setDefiningAttribute( a, b );
      }

      for ( int i = 0; i < nrFields; i++ ) {
        XMLInputSaxField field = new XMLInputSaxField();

        field.setName( rep.getStepAttributeString( id_step, i, "field_name" ) );
        field.setType( ValueMeta.getType( rep.getStepAttributeString( id_step, i, "field_type" ) ) );
        field.setFormat( rep.getStepAttributeString( id_step, i, "field_format" ) );
        field.setCurrencySymbol( rep.getStepAttributeString( id_step, i, "field_currency" ) );
        field.setDecimalSymbol( rep.getStepAttributeString( id_step, i, "field_decimal" ) );
        field.setGroupSymbol( rep.getStepAttributeString( id_step, i, "field_group" ) );
        field.setLength( (int) rep.getStepAttributeInteger( id_step, i, "field_length" ) );
        field.setPrecision( (int) rep.getStepAttributeInteger( id_step, i, "field_precision" ) );
        field.setTrimType( XMLInputSaxField.getTrimType( rep
          .getStepAttributeString( id_step, i, "field_trim_type" ) ) );
        field.setRepeated( rep.getStepAttributeBoolean( id_step, i, "field_repeat" ) );

        String fieldPositionCode = rep.getStepAttributeString( id_step, i, "field_position_code" );
        if ( fieldPositionCode != null ) {
          field.setFieldPosition( fieldPositionCode );
        }

        inputFields[i] = field;
      }

      for ( int i = 0; i < nrPositions; i++ ) {
        String encoded = rep.getStepAttributeString( id_step, i, "input_position" );
        inputPosition[i] = new XMLInputSaxFieldPosition( encoded );
      }

    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "include", includeFilename );
      rep.saveStepAttribute( id_transformation, id_step, "include_field", filenameField );
      rep.saveStepAttribute( id_transformation, id_step, "rownum", includeRowNumber );
      rep.saveStepAttribute( id_transformation, id_step, "rownum_field", rowNumberField );
      rep.saveStepAttribute( id_transformation, id_step, "limit", rowLimit );

      for ( int i = 0; i < fileName.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "file_name", fileName[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "file_mask", fileMask[i] );
      }

      for ( int i = 0; i < this.getDefinitionLength(); i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "def_element", this.getDefiningElement( i ) );
        rep.saveStepAttribute( id_transformation, id_step, i, "def_attribute", this.getDefiningAttribute( i ) );
      }

      for ( int i = 0; i < inputFields.length; i++ ) {
        XMLInputSaxField field = inputFields[i];

        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", field.getName() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_type", field.getTypeDesc() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_format", field.getFormat() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_currency", field.getCurrencySymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_decimal", field.getDecimalSymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_group", field.getGroupSymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_length", field.getLength() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_precision", field.getPrecision() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_trim_type", field.getTrimTypeDesc() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_repeat", field.isRepeated() );
        rep
          .saveStepAttribute( id_transformation, id_step, i, "field_position_code", field
            .getFieldPositionsCode() );
      }

      for ( int i = 0; i < inputPosition.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "input_position", inputPosition[i].toString() );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  public String[] getFilePaths( Bowl bowl, VariableSpace space ) {
    String[] fileRequired = new String[fileName.length];
    for ( int i = 0; i < fileRequired.length; i++ ) {
      fileRequired[i] = "N";
    }
    return FileInputList.createFilePathList( bowl, space, fileName, fileMask, new String[] { null }, fileRequired );
  }

  public FileInputList getTextFileList( Bowl bowl, VariableSpace space ) {
    String[] fileRequired = new String[fileName.length];
    for ( int i = 0; i < fileRequired.length; i++ ) {
      fileRequired[i] = "N";
    }
    return FileInputList.createFileList( bowl, space, fileName, fileMask, new String[] { null }, fileRequired );
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    // See if we get input...
    if ( input.length > 0 ) {
      cr =
        new CheckResult(
          CheckResultInterface.TYPE_RESULT_ERROR, "This step is not expecting nor reading any input", stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult(
          CheckResultInterface.TYPE_RESULT_OK, "Not receiving any input from other steps.", stepMeta );
      remarks.add( cr );
    }

    String[] files = getFilePaths( transMeta.getBowl(), transMeta );
    if ( files == null || files.length == 0 ) {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "No files can be found to read.", stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult(
          CheckResultInterface.TYPE_RESULT_OK, "This step is reading " + files.length + " files.", stepMeta );
      remarks.add( cr );
    }

    if ( getInputPosition().length == 0 ) {
      cr =
        new CheckResult(
          CheckResultInterface.TYPE_RESULT_ERROR,
          "No location elements given. Please specify the location of the repeating node in the XML document.",
          stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult(
          CheckResultInterface.TYPE_RESULT_OK, "At least one location element specified.", stepMeta );
      remarks.add( cr );
    }

    if ( getInputFields().length == 0 ) {
      cr =
        new CheckResult(
          CheckResultInterface.TYPE_RESULT_ERROR,
          "No field elements given. Please specify the fields you wish to extract from the XML document.",
          stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, "At least one field element specified.", stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new XMLInputSax( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new XMLInputSaxData();
  }

  /**
   * @return Returns the inputPosition.
   */
  public XMLInputSaxFieldPosition[] getInputPosition() {
    return inputPosition;
  }

  /**
   * @param inputPosition
   *          The inputPosition to set.
   */
  public void setInputPosition( XMLInputSaxFieldPosition[] inputPosition ) {
    this.inputPosition = inputPosition;
  }

  public void clearDefinition() {
    definitionElement.clear();
    definitionAttribute.clear();
  }

  public String getDefiningAttribute( String elementName ) {
    for ( int i = 0; i < definitionElement.size(); i++ ) {
      if ( definitionElement.get( i ).equals( elementName ) ) {
        return definitionAttribute.get( i );
      }
    }

    // Also look for a normal attribute...
    for ( int i = 0; i < inputFields.length; i++ ) {
      XMLInputSaxField field = inputFields[i];
      XMLInputSaxFieldPosition[] positions = field.getFieldPosition();
      for ( int p = 0; p < positions.length; p++ ) {
        XMLInputSaxFieldPosition position = positions[p];
        if ( position.getType() == XMLInputSaxFieldPosition.XML_ATTRIBUTE ) {
          return position.getName();
        }
      }
    }
    return null;
  }

  public int getDefiningAttributeNormalID( String attributeName ) {

    // look for a normal attribute...
    for ( int i = 0; i < inputFields.length; i++ ) {
      XMLInputSaxField field = inputFields[i];
      XMLInputSaxFieldPosition[] positions = field.getFieldPosition();
      for ( int p = 0; p < positions.length; p++ ) {
        XMLInputSaxFieldPosition position = positions[p];
        if ( position.getType() == XMLInputSaxFieldPosition.XML_ATTRIBUTE
          && position.getName().equals( attributeName ) ) {
          return i;
        }
      }
    }
    return -1;
  }

  public void setDefiningAttribute( String elementName, String attributeName ) {
    int index = definitionElement.indexOf( elementName );
    if ( index >= 0 ) {
      definitionAttribute.set( index, attributeName );
    } else {
      definitionAttribute.add( attributeName );
      definitionElement.add( elementName );
    }
  }

  public String getDefiningAttribute( int i ) {
    return definitionAttribute.get( i );
  }

  public String getDefiningElement( int i ) {
    return definitionElement.get( i );
  }

  public int getDefinitionLength() {
    return definitionElement.size();
  }

  @Override
  public List<ResourceReference> getResourceDependencies( TransMeta transMeta, StepMeta stepInfo ) {
    List<ResourceReference> references = new ArrayList<ResourceReference>( 5 );
    ResourceReference reference = new ResourceReference( stepInfo );
    references.add( reference );

    String[] textFiles = getFilePaths( transMeta.getBowl(), transMeta );
    if ( textFiles != null ) {
      for ( int i = 0; i < textFiles.length; i++ ) {
        reference.getEntries().add( new ResourceEntry( textFiles[i], ResourceType.FILE ) );
      }
    }
    return references;
  }

  /**
   * Since the exported transformation that runs this will reside in a ZIP file, we can't reference files relatively. So
   * what this does is turn the name of files into absolute paths OR it simply includes the resource in the ZIP file.
   * For now, we'll simply turn it into an absolute path and pray that the file is on a shared drive or something like
   * that.
   *
   * @param executionBowl
   *          For file access
   * @param globalManagementBowl
   *          if needed for access to the current "global" (System or Repository) level config for export. If null, no
   *          global config will be exported.
   * @param space
   *          the variable space to use
   * @param definitions
   * @param resourceNamingInterface
   * @param repository
   *          The repository to optionally load other resources from (to be converted to XML)
   * @param metaStore
   *          the metaStore in which non-kettle metadata could reside.
   *
   * @return the filename of the exported resource
   */
  @Override
  public String exportResources( Bowl executionBowl, Bowl globalManagementBowl, VariableSpace space,
      Map<String, ResourceDefinition> definitions, ResourceNamingInterface namingInterface,
      Repository repository, IMetaStore metaStore ) throws KettleException {
    try {
      // The object that we're modifying here is a copy of the original!
      // So let's change the filename from relative to absolute by grabbing the file object...
      //
      for ( int i = 0; i < fileName.length; i++ ) {
        FileObject fileObject = KettleVFS.getInstance( executionBowl )
          .getFileObject( space.environmentSubstitute( fileName[i] ), space );
        fileName[i] = namingInterface.nameResource( fileObject, space, Utils.isEmpty( fileMask[i] ) );
      }
      return null;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }
}
