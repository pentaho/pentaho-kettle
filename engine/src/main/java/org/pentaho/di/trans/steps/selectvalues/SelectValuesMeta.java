/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.selectvalues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.lineage.FieldnameLineage;
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
 * Meta Data class for the Select Values Step.
 *
 * Created on 02-jun-2003
 */
@InjectionSupported( localizationPrefix = "SelectValues.Injection.", groups = { "FIELDS", "REMOVES", "METAS" } )
public class SelectValuesMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = SelectValuesMeta.class; // for i18n purposes, needed by Translator2!!

  public static final int UNDEFINED = -2;

  // SELECT mode
  @InjectionDeep
  private SelectField[] selectFields = {};

  /**
   * Select: flag to indicate that the non-selected fields should also be taken along, ordered by fieldname
   */
  @Injection( name = "SELECT_UNSPECIFIED" )
  private boolean selectingAndSortingUnspecifiedFields;

  // DE-SELECT mode
  /** Names of the fields to be removed! */
  @Injection( name = "REMOVE_NAME", group = "REMOVES" )
  private String[] deleteName = {};

  // META-DATA mode
  @InjectionDeep
  private SelectMetadataChange[] meta = {};

  public SelectValuesMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the deleteName.
   */
  public String[] getDeleteName() {
    return deleteName;
  }

  /**
   * @param deleteName
   *          The deleteName to set.
   */
  public void setDeleteName( String[] deleteName ) {
    this.deleteName = deleteName == null ? new String[0] : deleteName;
  }

  /**
   * @param selectName
   *          The selectName to set.
   */
  public void setSelectName( String[] selectName ) {
    resizeSelectFields( selectName.length );
    for ( int i = 0; i < selectFields.length; i++ ) {
      selectFields[i].setName( selectName[i] );
    }
  }

  public String[] getSelectName() {
    String[] selectName = new String[selectFields.length];
    for ( int i = 0; i < selectName.length; i++ ) {
      selectName[i] = selectFields[i].getName();
    }
    return selectName;
  }

  /**
   * @param selectRename
   *          The selectRename to set.
   */
  public void setSelectRename( String[] selectRename ) {
    if ( selectRename.length > selectFields.length ) {
      resizeSelectFields( selectRename.length );
    }
    for ( int i = 0; i < selectFields.length; i++ ) {
      if ( i < selectRename.length ) {
        selectFields[i].setRename( selectRename[i] );
      } else {
        selectFields[i].setRename( null );
      }
    }
  }

  public String[] getSelectRename() {
    String[] selectRename = new String[selectFields.length];
    for ( int i = 0; i < selectRename.length; i++ ) {
      selectRename[i] = selectFields[i].getRename();
    }
    return selectRename;
  }

  /**
   * @param selectLength
   *          The selectLength to set.
   */
  public void setSelectLength( int[] selectLength ) {
    if ( selectLength.length > selectFields.length ) {
      resizeSelectFields( selectLength.length );
    }
    for ( int i = 0; i < selectFields.length; i++ ) {
      if ( i < selectLength.length ) {
        selectFields[i].setLength( selectLength[i] );
      } else {
        selectFields[i].setLength( UNDEFINED );
      }
    }
  }

  public int[] getSelectLength() {
    int[] selectLength = new int[selectFields.length];
    for ( int i = 0; i < selectLength.length; i++ ) {
      selectLength[i] = selectFields[i].getLength();
    }
    return selectLength;
  }

  /**
   * @param selectPrecision
   *          The selectPrecision to set.
   */
  public void setSelectPrecision( int[] selectPrecision ) {
    if ( selectPrecision.length > selectFields.length ) {
      resizeSelectFields( selectPrecision.length );
    }
    for ( int i = 0; i < selectFields.length; i++ ) {
      if ( i < selectPrecision.length ) {
        selectFields[i].setPrecision( selectPrecision[i] );
      } else {
        selectFields[i].setPrecision( UNDEFINED );
      }
    }
  }

  public int[] getSelectPrecision() {
    int[] selectPrecision = new int[selectFields.length];
    for ( int i = 0; i < selectPrecision.length; i++ ) {
      selectPrecision[i] = selectFields[i].getPrecision();
    }
    return selectPrecision;
  }

  private void resizeSelectFields( int length ) {
    int fillStartIndex = selectFields.length;
    selectFields = Arrays.copyOf( selectFields, length );
    for ( int i = fillStartIndex; i < selectFields.length; i++ ) {
      selectFields[i] = new SelectField();
      selectFields[i].setLength( UNDEFINED );
      selectFields[i].setPrecision( UNDEFINED );
    }
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public void allocate( int nrFields, int nrRemove, int nrMeta ) {
    allocateSelect( nrFields );
    allocateRemove( nrRemove );
    allocateMeta( nrMeta );
  }

  private void allocateSelect( int nrFields ) {
    selectFields = new SelectField[nrFields];
    for ( int i = 0; i < nrFields; i++ ) {
      selectFields[i] = new SelectField();
    }
  }

  private void allocateRemove( int nrRemove ) {
    deleteName = new String[nrRemove];
  }

  private void allocateMeta( int nrMeta ) {
    meta = new SelectMetadataChange[nrMeta];
    for ( int i = 0; i < nrMeta; i++ ) {
      meta[i] = new SelectMetadataChange( this );
    }
  }

  @Override
  public Object clone() {
    SelectValuesMeta retval = (SelectValuesMeta) super.clone();

    int nrfields = selectFields == null ? 0 : selectFields.length;
    int nrremove = deleteName == null ? 0 : deleteName.length;
    int nrmeta = meta == null ? 0 : meta.length;

    retval.allocate( nrfields, nrremove, nrmeta );
    for ( int i = 0; i < nrfields; i++ ) {
      retval.getSelectFields()[i] = selectFields[i].clone();
    }

    System.arraycopy( deleteName, 0, retval.deleteName, 0, nrremove );

    for ( int i = 0; i < nrmeta; i++ ) {
      // CHECKSTYLE:Indentation:OFF
      retval.getMeta()[i] = meta[i].clone();
    }

    return retval;
  }

  private void readData( Node step ) throws KettleXMLException {
    try {
      Node fields = XMLHandler.getSubNode( step, "fields" );

      int nrfields = XMLHandler.countNodes( fields, "field" );
      int nrremove = XMLHandler.countNodes( fields, "remove" );
      int nrmeta = XMLHandler.countNodes( fields, SelectMetadataChange.XML_TAG );
      allocate( nrfields, nrremove, nrmeta );

      for ( int i = 0; i < nrfields; i++ ) {
        Node line = XMLHandler.getSubNodeByNr( fields, "field", i );
        selectFields[i] = new SelectField();
        selectFields[i].setName( XMLHandler.getTagValue( line, "name" ) );
        selectFields[i].setRename( XMLHandler.getTagValue( line, "rename" ) );
        selectFields[i].setLength( Const.toInt( XMLHandler.getTagValue( line, "length" ), UNDEFINED ) ); // $NON-NtagLS-1$
        selectFields[i].setPrecision( Const.toInt( XMLHandler.getTagValue( line, "precision" ), UNDEFINED ) );
      }
      selectingAndSortingUnspecifiedFields =
          "Y".equalsIgnoreCase( XMLHandler.getTagValue( fields, "select_unspecified" ) );

      for ( int i = 0; i < nrremove; i++ ) {
        Node line = XMLHandler.getSubNodeByNr( fields, "remove", i );
        deleteName[i] = XMLHandler.getTagValue( line, "name" );
      }

      for ( int i = 0; i < nrmeta; i++ ) {
        Node metaNode = XMLHandler.getSubNodeByNr( fields, SelectMetadataChange.XML_TAG, i );
        meta[i] = new SelectMetadataChange( this );
        meta[i].loadXML( metaNode );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG,
          "SelectValuesMeta.Exception.UnableToReadStepInfoFromXML" ), e );
    }
  }

  @Override
  public void setDefault() {
    allocate( 0, 0, 0 );
  }

  public void getSelectFields( RowMetaInterface inputRowMeta, String name ) throws KettleStepException {
    RowMetaInterface row;

    if ( selectFields != null && selectFields.length > 0 ) { // SELECT values

      // 0. Start with an empty row
      // 1. Keep only the selected values
      // 2. Rename the selected values
      // 3. Keep the order in which they are specified... (not the input order!)
      //

      row = new RowMeta();
      for ( int i = 0; i < selectFields.length; i++ ) {
        ValueMetaInterface v = inputRowMeta.searchValueMeta( selectFields[i].getName() );

        if ( v != null ) { // We found the value

          v = v.clone();
          // Do we need to rename ?
          if ( !v.getName().equals( selectFields[i].getRename() ) && selectFields[i].getRename() != null
              && selectFields[i].getRename().length() > 0 ) {
            v.setName( selectFields[i].getRename() );
            v.setOrigin( name );
          }
          if ( selectFields[i].getLength() != UNDEFINED ) {
            v.setLength( selectFields[i].getLength() );
            v.setOrigin( name );
          }
          if ( selectFields[i].getPrecision() != UNDEFINED ) {
            v.setPrecision( selectFields[i].getPrecision() );
            v.setOrigin( name );
          }

          // Add to the resulting row!
          row.addValueMeta( v );
        }
      }

      if ( selectingAndSortingUnspecifiedFields ) {
        // Select the unspecified fields.
        // Sort the fields
        // Add them after the specified fields...
        //
        List<String> extra = new ArrayList<>();
        for ( int i = 0; i < inputRowMeta.size(); i++ ) {
          String fieldName = inputRowMeta.getValueMeta( i ).getName();
          if ( Const.indexOfString( fieldName, getSelectName() ) < 0 ) {
            extra.add( fieldName );
          }
        }
        Collections.sort( extra );
        for ( String fieldName : extra ) {
          ValueMetaInterface extraValue = inputRowMeta.searchValueMeta( fieldName );
          row.addValueMeta( extraValue );
        }
      }

      // OK, now remove all from r and re-add row:
      inputRowMeta.clear();
      inputRowMeta.addRowMeta( row );
    }
  }

  public void getDeleteFields( RowMetaInterface inputRowMeta ) throws KettleStepException {
    if ( deleteName != null && deleteName.length > 0 ) { // DESELECT values from the stream...
      for ( int i = 0; i < deleteName.length; i++ ) {
        try {
          inputRowMeta.removeValueMeta( deleteName[i] );
        } catch ( KettleValueException e ) {
          throw new KettleStepException( e );
        }
      }
    }
  }

  // Not called anywhere else in Hitachi Vantara. It's important to call the method below passing in the VariableSpace
  @Deprecated
  public void getMetadataFields( RowMetaInterface inputRowMeta, String name ) throws KettlePluginException {
    getMetadataFields( inputRowMeta, name, null );
  }

  public void getMetadataFields( RowMetaInterface inputRowMeta, String name, VariableSpace space ) throws KettlePluginException {
    if ( meta != null && meta.length > 0 ) {
      // METADATA mode: change the meta-data of the values mentioned...

      for ( int i = 0; i < meta.length; i++ ) {
        SelectMetadataChange metaChange = meta[i];

        int idx = inputRowMeta.indexOfValue( metaChange.getName() );
        boolean metaTypeChangeUsesNewTypeDefaults = false; // Normal behavior as of 5.x or so
        if ( space != null ) {
          metaTypeChangeUsesNewTypeDefaults = ValueMetaBase.convertStringToBoolean(
              space.getVariable( Const.KETTLE_COMPATIBILITY_SELECT_VALUES_TYPE_CHANGE_USES_TYPE_DEFAULTS, "N" ) );
        }
        if ( idx >= 0 ) { // We found the value

          // This is the value we need to change:
          ValueMetaInterface v = inputRowMeta.getValueMeta( idx );

          // Do we need to rename ?
          if ( !v.getName().equals( metaChange.getRename() ) && !Utils.isEmpty( metaChange.getRename() ) ) {
            v.setName( metaChange.getRename() );
            v.setOrigin( name );
          }
          // Change the type?
          if ( metaChange.getType() != ValueMetaInterface.TYPE_NONE && v.getType() != metaChange.getType() ) {
            // Fix for PDI-16388 - clone copies over the conversion mask instead of using the default for the new type
            if ( !metaTypeChangeUsesNewTypeDefaults ) {
              v = ValueMetaFactory.cloneValueMeta( v, metaChange.getType() );
            } else {
              v = ValueMetaFactory.createValueMeta( v.getName(), metaChange.getType() );
            }

            // This is now a copy, replace it in the row!
            //
            inputRowMeta.setValueMeta( idx, v );

            // This also moves the data to normal storage type
            //
            v.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
          }
          if ( metaChange.getLength() != UNDEFINED ) {
            v.setLength( metaChange.getLength() );
            v.setOrigin( name );
          }
          if ( metaChange.getPrecision() != UNDEFINED ) {
            v.setPrecision( metaChange.getPrecision() );
            v.setOrigin( name );
          }
          if ( metaChange.getStorageType() >= 0 ) {
            v.setStorageType( metaChange.getStorageType() );
            v.setOrigin( name );
          }
          if ( !Utils.isEmpty( metaChange.getConversionMask() ) ) {
            v.setConversionMask( metaChange.getConversionMask() );
            v.setOrigin( name );
          }

          v.setDateFormatLenient( metaChange.isDateFormatLenient() );
          v.setDateFormatLocale( EnvUtil.createLocale( metaChange.getDateFormatLocale() ) );
          v.setDateFormatTimeZone( EnvUtil.createTimeZone( metaChange.getDateFormatTimeZone() ) );
          v.setLenientStringToNumber( metaChange.isLenientStringToNumber() );

          if ( !Utils.isEmpty( metaChange.getEncoding() ) ) {
            v.setStringEncoding( metaChange.getEncoding() );
            v.setOrigin( name );
          }
          if ( !Utils.isEmpty( metaChange.getDecimalSymbol() ) ) {
            v.setDecimalSymbol( metaChange.getDecimalSymbol() );
            v.setOrigin( name );
          }
          if ( !Utils.isEmpty( metaChange.getGroupingSymbol() ) ) {
            v.setGroupingSymbol( metaChange.getGroupingSymbol() );
            v.setOrigin( name );
          }
          if ( !Utils.isEmpty( metaChange.getCurrencySymbol() ) ) {
            v.setCurrencySymbol( metaChange.getCurrencySymbol() );
            v.setOrigin( name );
          }
        }
      }
    }
  }

  @Override
  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    try {
      RowMetaInterface rowMeta = inputRowMeta.clone();
      inputRowMeta.clear();
      inputRowMeta.addRowMeta( rowMeta );

      getSelectFields( inputRowMeta, name );
      getDeleteFields( inputRowMeta );
      getMetadataFields( inputRowMeta, name );
    } catch ( Exception e ) {
      throw new KettleStepException( e );
    }
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( "    <fields>" );
    for ( int i = 0; i < selectFields.length; i++ ) {
      retval.append( "      <field>" );
      retval.append( "        " ).append( XMLHandler.addTagValue( getXmlCode( "FIELD_NAME" ), selectFields[i]
          .getName() ) );
      if ( selectFields[i].getRename() != null ) {
        retval.append( "        " ).append( XMLHandler.addTagValue( getXmlCode( "FIELD_RENAME" ), selectFields[i]
            .getRename() ) );
      }
      if ( selectFields[i].getPrecision() > 0 ) {
        retval.append( "        " ).append( XMLHandler.addTagValue( getXmlCode( "FIELD_LENGTH" ), selectFields[i]
            .getLength() ) );
      }
      if ( selectFields[i].getPrecision() > 0 ) {
        retval.append( "        " ).append( XMLHandler.addTagValue( getXmlCode( "FIELD_PRECISION" ), selectFields[i]
            .getPrecision() ) );
      }
      retval.append( "      </field>" );
    }
    retval.append( "        " ).append( XMLHandler.addTagValue( getXmlCode( "SELECT_UNSPECIFIED" ),
        selectingAndSortingUnspecifiedFields ) );
    for ( int i = 0; i < deleteName.length; i++ ) {
      retval.append( "      <remove>" );
      retval.append( "        " ).append( XMLHandler.addTagValue( getXmlCode( "REMOVE_NAME" ), deleteName[i] ) );
      retval.append( "      </remove>" );
    }
    for ( int i = 0; i < meta.length; i++ ) {
      retval.append( meta[i].getXML() );
    }
    retval.append( "    </fields>" );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      int nrfields = rep.countNrStepAttributes( id_step, getRepCode( "FIELD_NAME" ) );
      int nrremove = rep.countNrStepAttributes( id_step, getRepCode( "REMOVE_NAME" ) );
      int nrmeta = rep.countNrStepAttributes( id_step, getRepCode( "META_NAME" ) );

      allocate( nrfields, nrremove, nrmeta );

      for ( int i = 0; i < nrfields; i++ ) {
        selectFields[i].setName( rep.getStepAttributeString( id_step, i, getRepCode( "FIELD_NAME" ) ) );
        selectFields[i].setRename( rep.getStepAttributeString( id_step, i, getRepCode( "FIELD_RENAME" ) ) );
        selectFields[i].setLength( (int) rep.getStepAttributeInteger( id_step, i, getRepCode( "FIELD_LENGTH" ) ) );
        selectFields[i].setPrecision( (int) rep.getStepAttributeInteger( id_step, i, getRepCode(
            "FIELD_PRECISION" ) ) );
      }
      selectingAndSortingUnspecifiedFields = rep.getStepAttributeBoolean( id_step, getRepCode( "SELECT_UNSPECIFIED" ) );

      for ( int i = 0; i < nrremove; i++ ) {
        deleteName[i] = rep.getStepAttributeString( id_step, i, getRepCode( "REMOVE_NAME" ) );
      }

      for ( int i = 0; i < nrmeta; i++ ) {
        meta[i] = new SelectMetadataChange( this );
        meta[i].setName( rep.getStepAttributeString( id_step, i, getRepCode( "META_NAME" ) ) );
        meta[i].setRename( rep.getStepAttributeString( id_step, i, getRepCode( "META_RENAME" ) ) );
        meta[i].setType( (int) rep.getStepAttributeInteger( id_step, i, getRepCode( "META_TYPE" ) ) );
        meta[i].setLength( (int) rep.getStepAttributeInteger( id_step, i, getRepCode( "META_LENGTH" ) ) );
        meta[i].setPrecision( (int) rep.getStepAttributeInteger( id_step, i, getRepCode( "META_PRECISION" ) ) );
        meta[i].setStorageType( ValueMetaBase.getStorageType( rep.getStepAttributeString( id_step, i, getRepCode(
            "META_STORAGE_TYPE" ) ) ) );
        meta[i].setConversionMask( rep.getStepAttributeString( id_step, i, getRepCode( "META_CONVERSION_MASK" ) ) );
        meta[i].setDateFormatLenient( Boolean.parseBoolean( rep.getStepAttributeString( id_step, i, getRepCode(
            "META_DATE_FORMAT_LENIENT" ) ) ) );
        meta[i].setDateFormatLocale( rep.getStepAttributeString( id_step, i, getRepCode(
            "META_DATE_FORMAT_LOCALE" ) ) );
        meta[i].setDateFormatTimeZone( rep.getStepAttributeString( id_step, i, getRepCode(
            "META_DATE_FORMAT_TIMEZONE" ) ) );
        meta[i].setLenientStringToNumber( Boolean.parseBoolean( rep.getStepAttributeString( id_step, i, getRepCode(
            "META_LENIENT_STRING_TO_NUMBER" ) ) ) );
        meta[i].setDecimalSymbol( rep.getStepAttributeString( id_step, i, getRepCode( "META_DECIMAL" ) ) );
        meta[i].setGroupingSymbol( rep.getStepAttributeString( id_step, i, getRepCode( "META_GROUPING" ) ) );
        meta[i].setCurrencySymbol( rep.getStepAttributeString( id_step, i, getRepCode( "META_CURRENCY" ) ) );
        meta[i].setEncoding( rep.getStepAttributeString( id_step, i, getRepCode( "META_ENCODING" ) ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG,
          "SelectValuesMeta.Exception.UnexpectedErrorReadingStepInfoFromRepository" ), e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      for ( int i = 0; i < selectFields.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "FIELD_NAME" ), selectFields[i].getName() );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "FIELD_RENAME" ), selectFields[i]
            .getRename() );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "FIELD_LENGTH" ), selectFields[i]
            .getLength() );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "FIELD_PRECISION" ), selectFields[i]
            .getPrecision() );
      }
      rep.saveStepAttribute( id_transformation, id_step, getRepCode( "SELECT_UNSPECIFIED" ),
          selectingAndSortingUnspecifiedFields );

      for ( int i = 0; i < deleteName.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "REMOVE_NAME" ), deleteName[i] );
      }

      for ( int i = 0; i < meta.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "META_NAME" ), meta[i].getName() );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "META_RENAME" ), meta[i].getRename() );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "META_TYPE" ), meta[i].getType() );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "META_LENGTH" ), meta[i].getLength() );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "META_PRECISION" ), meta[i].getPrecision() );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "META_STORAGE_TYPE" ), ValueMetaBase
            .getStorageTypeCode( meta[i].getStorageType() ) );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "META_CONVERSION_MASK" ), meta[i]
            .getConversionMask() );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "META_DATE_FORMAT_LENIENT" ), Boolean
            .toString( meta[i].isDateFormatLenient() ) );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "META_DATE_FORMAT_LOCALE" ), meta[i]
            .getDateFormatLocale() == null ? null : meta[i].getDateFormatLocale().toString() );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "META_DATE_FORMAT_TIMEZONE" ), meta[i]
            .getDateFormatTimeZone() == null ? null : meta[i].getDateFormatTimeZone().toString() );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "META_LENIENT_STRING_TO_NUMBER" ), Boolean
            .toString( meta[i].isLenientStringToNumber() ) );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "META_DECIMAL" ), meta[i]
            .getDecimalSymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "META_GROUPING" ), meta[i]
            .getGroupingSymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "META_CURRENCY" ), meta[i]
            .getCurrencySymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "META_ENCODING" ), meta[i].getEncoding() );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG,
          "SelectValuesMeta.Exception.UnableToSaveStepInfoToRepository" ) + id_step, e );
    }

  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {
    CheckResult cr;

    if ( prev != null && prev.size() > 0 ) {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "SelectValuesMeta.CheckResult.StepReceivingFields", prev.size() + "" ), stepMeta );
      remarks.add( cr );

      /*
       * Take care of the normal SELECT fields...
       */
      String error_message = "";
      boolean error_found = false;

      // Starting from selected fields in ...
      for ( int i = 0; i < this.selectFields.length; i++ ) {
        int idx = prev.indexOfValue( selectFields[i].getName() );
        if ( idx < 0 ) {
          error_message += "\t\t" + selectFields[i].getName() + Const.CR;
          error_found = true;
        }
      }
      if ( error_found ) {
        error_message =
            BaseMessages.getString( PKG, "SelectValuesMeta.CheckResult.SelectedFieldsNotFound" ) + Const.CR + Const.CR
                + error_message;

        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } else {
        cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                "SelectValuesMeta.CheckResult.AllSelectedFieldsFound" ), stepMeta );
        remarks.add( cr );
      }

      if ( this.selectFields.length > 0 ) {
        // Starting from prev...
        for ( int i = 0; i < prev.size(); i++ ) {
          ValueMetaInterface pv = prev.getValueMeta( i );
          int idx = Const.indexOfString( pv.getName(), getSelectName() );
          if ( idx < 0 ) {
            error_message += "\t\t" + pv.getName() + " (" + pv.getTypeDesc() + ")" + Const.CR;
            error_found = true;
          }
        }
        if ( error_found ) {
          error_message =
              BaseMessages.getString( PKG, "SelectValuesMeta.CheckResult.FieldsNotFound" ) + Const.CR + Const.CR
                  + error_message;

          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_COMMENT, error_message, stepMeta );
          remarks.add( cr );
        } else {
          cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                  "SelectValuesMeta.CheckResult.AllSelectedFieldsFound2" ), stepMeta );
          remarks.add( cr );
        }
      }

      /*
       * How about the DE-SELECT (remove) fields...
       */

      error_message = "";
      error_found = false;

      // Starting from selected fields in ...
      for ( int i = 0; i < this.deleteName.length; i++ ) {
        int idx = prev.indexOfValue( deleteName[i] );
        if ( idx < 0 ) {
          error_message += "\t\t" + deleteName[i] + Const.CR;
          error_found = true;
        }
      }
      if ( error_found ) {
        error_message =
            BaseMessages.getString( PKG, "SelectValuesMeta.CheckResult.DeSelectedFieldsNotFound" ) + Const.CR + Const.CR
                + error_message;

        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } else {
        cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                "SelectValuesMeta.CheckResult.AllDeSelectedFieldsFound" ), stepMeta );
        remarks.add( cr );
      }

      /*
       * How about the Meta-fields...?
       */
      error_message = "";
      error_found = false;

      // Starting from selected fields in ...
      for ( int i = 0; i < this.meta.length; i++ ) {
        int idx = prev.indexOfValue( this.meta[i].getName() );
        if ( idx < 0 ) {
          error_message += "\t\t" + this.meta[i].getName() + Const.CR;
          error_found = true;
        }
      }
      if ( error_found ) {
        error_message =
            BaseMessages.getString( PKG, "SelectValuesMeta.CheckResult.MetadataFieldsNotFound" ) + Const.CR + Const.CR
                + error_message;

        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } else {
        cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                "SelectValuesMeta.CheckResult.AllMetadataFieldsFound" ), stepMeta );
        remarks.add( cr );
      }
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "SelectValuesMeta.CheckResult.FieldsNotFound2" ), stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "SelectValuesMeta.CheckResult.StepReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "SelectValuesMeta.CheckResult.NoInputReceivedError" ), stepMeta );
      remarks.add( cr );
    }

    // Check for doubles in the selected fields...
    int[] cnt = new int[selectFields.length];
    boolean error_found = false;
    String error_message = "";

    for ( int i = 0; i < selectFields.length; i++ ) {
      cnt[i] = 0;
      for ( int j = 0; j < selectFields.length; j++ ) {
        if ( selectFields[i].getName().equals( selectFields[j].getName() ) ) {
          cnt[i]++;
        }
      }

      if ( cnt[i] > 1 ) {
        if ( !error_found ) { // first time...
          error_message =
              BaseMessages.getString( PKG, "SelectValuesMeta.CheckResult.DuplicateFieldsSpecified" ) + Const.CR;
        } else {
          error_found = true;
        }
        error_message +=
            BaseMessages.getString( PKG, "SelectValuesMeta.CheckResult.OccurentRow", i + " : " + selectFields[i]
                .getName() + "  (" + cnt[i] ) + Const.CR;
        error_found = true;
      }
    }
    if ( error_found ) {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
      Trans trans ) {
    return new SelectValues( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new SelectValuesData();
  }

  /**
   * @return the selectingAndSortingUnspecifiedFields
   */
  public boolean isSelectingAndSortingUnspecifiedFields() {
    return selectingAndSortingUnspecifiedFields;
  }

  /**
   * @param selectingAndSortingUnspecifiedFields
   *          the selectingAndSortingUnspecifiedFields to set
   */
  public void setSelectingAndSortingUnspecifiedFields( boolean selectingAndSortingUnspecifiedFields ) {
    this.selectingAndSortingUnspecifiedFields = selectingAndSortingUnspecifiedFields;
  }

  /**
   * @return the meta
   */
  public SelectMetadataChange[] getMeta() {
    return meta;
  }

  /**
   * @param meta
   *          the meta to set
   */
  public void setMeta( SelectMetadataChange[] meta ) {
    this.meta = meta == null ? new SelectMetadataChange[0] : meta;
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
  }

  public SelectField[] getSelectFields() {
    return selectFields;
  }

  public void setSelectFields( SelectField[] selectFields ) {
    this.selectFields = selectFields == null ? new SelectField[0] : selectFields;
  }

  /**
   * We will describe in which way the field names change between input and output in this step.
   *
   * @return The list of field name lineage objects
   */
  public List<FieldnameLineage> getFieldnameLineage() {
    List<FieldnameLineage> lineages = new ArrayList<>();

    // Select values...
    //
    for ( int i = 0; i < selectFields.length; i++ ) {
      String input = selectFields[i].getName();
      String output = selectFields[i].getRename();

      // See if the select tab renames a column!
      //
      if ( !Utils.isEmpty( output ) && !input.equalsIgnoreCase( output ) ) {
        // Yes, add it to the list
        //
        lineages.add( new FieldnameLineage( input, output ) );
      }
    }

    // Metadata
    //
    for ( int i = 0; i < getMeta().length; i++ ) {
      String input = getMeta()[i].getName();
      String output = getMeta()[i].getRename();

      // See if the select tab renames a column!
      //
      if ( !Utils.isEmpty( output ) && !input.equalsIgnoreCase( output ) ) {
        // See if the input is not the output of a row in the Select tab
        //
        int idx = Const.indexOfString( input, getSelectRename() );

        if ( idx < 0 ) {
          // nothing special, add it to the list
          //
          lineages.add( new FieldnameLineage( input, output ) );
        } else {
          // Modify the existing field name lineage entry
          //
          FieldnameLineage lineage = FieldnameLineage.findFieldnameLineageWithInput( lineages, input );
          lineage.setOutputFieldname( output );
        }
      }
    }

    return lineages;
  }

  public static class SelectField implements Cloneable {

    /** Select: Name of the selected field */
    @Injection( name = "FIELD_NAME", group = "FIELDS" )
    private String name;

    /** Select: Rename to ... */
    @Injection( name = "FIELD_RENAME", group = "FIELDS" )
    private String rename;

    /** Select: length of field */
    @Injection( name = "FIELD_LENGTH", group = "FIELDS" )
    private int length = UNDEFINED;

    /** Select: Precision of field (for numbers) */
    @Injection( name = "FIELD_PRECISION", group = "FIELDS" )
    private int precision = UNDEFINED;

    public String getName() {
      return name;
    }

    public void setName( String name ) {
      this.name = name;
    }

    public String getRename() {
      return rename;
    }

    public void setRename( String rename ) {
      this.rename = rename;
    }

    public int getLength() {
      return length;
    }

    public void setLength( int length ) {
      this.length = length;
    }

    public int getPrecision() {
      return precision;
    }

    public void setPrecision( int precision ) {
      this.precision = precision;
    }

    @Override
    public SelectField clone() {
      try {
        return (SelectField) super.clone();
      } catch ( CloneNotSupportedException e ) {
        throw new RuntimeException( e );
      }
    }
  }
}
