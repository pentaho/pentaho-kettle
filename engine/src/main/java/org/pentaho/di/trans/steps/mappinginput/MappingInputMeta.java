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

package org.pentaho.di.trans.steps.mappinginput;

import java.util.ArrayList;
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
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
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
import org.pentaho.di.trans.steps.mapping.MappingValueRename;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/*
 * Created on 02-jun-2003
 *
 */

public class MappingInputMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = MappingInputMeta.class; // for i18n purposes, needed by Translator2!!

  private String[] fieldName;

  private int[] fieldType;

  private int[] fieldLength;

  private int[] fieldPrecision;

  /**
   * Select: flag to indicate that the non-selected fields should also be taken along, ordered by fieldname
   */
  private boolean selectingAndSortingUnspecifiedFields;

  private volatile RowMetaInterface inputRowMeta;

  private volatile List<MappingValueRename> valueRenames;

  public MappingInputMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the fieldLength.
   */
  public int[] getFieldLength() {
    return fieldLength;
  }

  /**
   * @param fieldLength The fieldLength to set.
   */
  public void setFieldLength( int[] fieldLength ) {
    this.fieldLength = fieldLength;
  }

  /**
   * @return Returns the fieldName.
   */
  public String[] getFieldName() {
    return fieldName;
  }

  /**
   * @param fieldName The fieldName to set.
   */
  public void setFieldName( String[] fieldName ) {
    this.fieldName = fieldName;
  }

  /**
   * @return Returns the fieldPrecision.
   */
  public int[] getFieldPrecision() {
    return fieldPrecision;
  }

  /**
   * @param fieldPrecision The fieldPrecision to set.
   */
  public void setFieldPrecision( int[] fieldPrecision ) {
    this.fieldPrecision = fieldPrecision;
  }

  /**
   * @return Returns the fieldType.
   */
  public int[] getFieldType() {
    return fieldType;
  }

  /**
   * @param fieldType The fieldType to set.
   */
  public void setFieldType( int[] fieldType ) {
    this.fieldType = fieldType;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public Object clone() {
    MappingInputMeta retval = (MappingInputMeta) super.clone();

    int nrfields = fieldName.length;

    retval.allocate( nrfields );

    System.arraycopy( fieldName, 0, retval.fieldName, 0, nrfields );
    System.arraycopy( fieldType, 0, retval.fieldType, 0, nrfields );
    System.arraycopy( fieldLength, 0, retval.fieldLength, 0, nrfields );
    System.arraycopy( fieldPrecision, 0, retval.fieldPrecision, 0, nrfields );
    return retval;
  }

  public void allocate( int nrfields ) {
    fieldName = new String[ nrfields ];
    fieldType = new int[ nrfields ];
    fieldLength = new int[ nrfields ];
    fieldPrecision = new int[ nrfields ];
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int nrfields = XMLHandler.countNodes( fields, "field" );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        fieldName[ i ] = XMLHandler.getTagValue( fnode, "name" );
        fieldType[ i ] = ValueMetaFactory.getIdForValueMeta( XMLHandler.getTagValue( fnode, "type" ) );
        String slength = XMLHandler.getTagValue( fnode, "length" );
        String sprecision = XMLHandler.getTagValue( fnode, "precision" );

        fieldLength[ i ] = Const.toInt( slength, -1 );
        fieldPrecision[ i ] = Const.toInt( sprecision, -1 );
      }

      selectingAndSortingUnspecifiedFields =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( fields, "select_unspecified" ) );
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "MappingInputMeta.Exception.UnableToLoadStepInfoFromXML" ), e );
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( "    <fields>" ).append( Const.CR );
    for ( int i = 0; i < fieldName.length; i++ ) {
      if ( fieldName[ i ] != null && fieldName[ i ].length() != 0 ) {
        retval.append( "      <field>" ).append( Const.CR );
        retval.append( "        " ).append( XMLHandler.addTagValue( "name", fieldName[ i ] ) );
        retval
          .append( "        " ).append( XMLHandler.addTagValue( "type",
            ValueMetaFactory.getValueMetaName( fieldType[ i ] ) ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "length", fieldLength[ i ] ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "precision", fieldPrecision[ i ] ) );
        retval.append( "      </field>" ).append( Const.CR );
      }
    }

    retval.append( "        " ).append(
      XMLHandler.addTagValue( "select_unspecified", selectingAndSortingUnspecifiedFields ) );

    retval.append( "    </fields>" ).append( Const.CR );

    return retval.toString();
  }

  public void setDefault() {
    int nrfields = 0;

    selectingAndSortingUnspecifiedFields = false;

    allocate( nrfields );

    for ( int i = 0; i < nrfields; i++ ) {
      fieldName[ i ] = "field" + i;
      fieldType[ i ] = ValueMetaInterface.TYPE_STRING;
      fieldLength[ i ] = 30;
      fieldPrecision[ i ] = -1;
    }
  }

  public void getFields( RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep,
                         VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // Row should normally be empty when we get here.
    // That is because there is no previous step to this mapping input step from the viewpoint of this single
    // sub-transformation.
    // From the viewpoint of the transformation that executes the mapping, it's important to know what comes out at the
    // exit points.
    // For that reason we need to re-order etc, based on the input specification...
    //
    if ( inputRowMeta != null && !inputRowMeta.isEmpty() ) {
      // this gets set only in the parent transformation...
      // It includes all the renames that needed to be done
      //
      // First rename any fields...
      if ( valueRenames != null ) {
        for ( MappingValueRename valueRename : valueRenames ) {
          ValueMetaInterface valueMeta = inputRowMeta.searchValueMeta( valueRename.getSourceValueName() );
          if ( valueMeta == null ) {
            // ok, let's search once again, now using target name
            valueMeta = inputRowMeta.searchValueMeta( valueRename.getTargetValueName() );
            if ( valueMeta == null ) {
              throw new KettleStepException( BaseMessages.getString(
                PKG, "MappingInput.Exception.UnableToFindMappedValue", valueRename.getSourceValueName() ) );
            }
          } else {
            valueMeta.setName( valueRename.getTargetValueName() );
          }
        }
      }

      if ( selectingAndSortingUnspecifiedFields ) {
        // Select the specified fields from the input, re-order everything and put the other fields at the back,
        // sorted...
        //
        RowMetaInterface newRow = new RowMeta();

        for ( int i = 0; i < fieldName.length; i++ ) {
          int index = inputRowMeta.indexOfValue( fieldName[ i ] );
          if ( index < 0 ) {
            throw new KettleStepException( BaseMessages.getString(
              PKG, "MappingInputMeta.Exception.UnknownField", fieldName[ i ] ) );
          }

          newRow.addValueMeta( inputRowMeta.getValueMeta( index ) );
        }

        // Now get the unspecified fields.
        // Sort the fields
        // Add them after the specified fields...
        //
        List<String> extra = new ArrayList<String>();
        for ( int i = 0; i < inputRowMeta.size(); i++ ) {
          String fieldName = inputRowMeta.getValueMeta( i ).getName();
          if ( newRow.indexOfValue( fieldName ) < 0 ) {
            extra.add( fieldName );
          }
        }
        Collections.sort( extra );
        for ( String fieldName : extra ) {
          ValueMetaInterface extraValue = inputRowMeta.searchValueMeta( fieldName );
          newRow.addValueMeta( extraValue );
        }

        // now merge the new row...
        // This is basically the input row meta data with the fields re-ordered.
        //
        row.mergeRowMeta( newRow );
      } else {
        row.mergeRowMeta( inputRowMeta );

        // Validate the existence of all the specified fields...
        //
        if ( !row.isEmpty() ) {
          for ( int i = 0; i < fieldName.length; i++ ) {
            if ( row.indexOfValue( fieldName[ i ] ) < 0 ) {
              throw new KettleStepException( BaseMessages.getString(
                PKG, "MappingInputMeta.Exception.UnknownField", fieldName[ i ] ) );
            }
          }
        }
      }
    } else {
      if ( row.isEmpty() ) {
        // We'll have to work with the statically provided information
        for ( int i = 0; i < fieldName.length; i++ ) {
          if ( !Utils.isEmpty( fieldName[ i ] ) ) {
            int valueType = fieldType[ i ];
            if ( valueType == ValueMetaInterface.TYPE_NONE ) {
              valueType = ValueMetaInterface.TYPE_STRING;
            }
            ValueMetaInterface v;
            try {
              v = ValueMetaFactory.createValueMeta( fieldName[ i ], valueType );
              v.setLength( fieldLength[ i ] );
              v.setPrecision( fieldPrecision[ i ] );
              v.setOrigin( origin );
              row.addValueMeta( v );
            } catch ( KettlePluginException e ) {
              throw new KettleStepException( e );
            }
          }
        }
      }

      // else: row is OK, keep it as it is.
    }
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      int nrfields = rep.countNrStepAttributes( id_step, "field_name" );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        fieldName[ i ] = rep.getStepAttributeString( id_step, i, "field_name" );
        fieldType[ i ] = ValueMetaFactory.getIdForValueMeta( rep.getStepAttributeString( id_step, i, "field_type" ) );
        fieldLength[ i ] = (int) rep.getStepAttributeInteger( id_step, i, "field_length" );
        fieldPrecision[ i ] = (int) rep.getStepAttributeInteger( id_step, i, "field_precision" );
      }

      selectingAndSortingUnspecifiedFields = rep.getStepAttributeBoolean( id_step, "select_unspecified" );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "MappingInputMeta.Exception.UnexpectedErrorInReadingStepInfo" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      for ( int i = 0; i < fieldName.length; i++ ) {
        if ( fieldName[ i ] != null && fieldName[ i ].length() != 0 ) {
          rep.saveStepAttribute( id_transformation, id_step, i, "field_name", fieldName[ i ] );
          rep.saveStepAttribute( id_transformation, id_step, i, "field_type",
            ValueMetaFactory.getValueMetaName( fieldType[ i ] ) );
          rep.saveStepAttribute( id_transformation, id_step, i, "field_length", fieldLength[ i ] );
          rep.saveStepAttribute( id_transformation, id_step, i, "field_precision", fieldPrecision[ i ] );
        }
      }

      rep.saveStepAttribute(
        id_transformation, id_step, "select_unspecified", selectingAndSortingUnspecifiedFields );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "MappingInputMeta.Exception.UnableToSaveStepInfo" )
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
          PKG, "MappingInputMeta.CheckResult.NotReceivingFieldsError" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "MappingInputMeta.CheckResult.StepReceivingDatasFromPreviousOne", prev.size() + "" ), stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "MappingInputMeta.CheckResult.StepReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "MappingInputMeta.CheckResult.NoInputReceived" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
                                Trans trans ) {
    return new MappingInput( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  public StepDataInterface getStepData() {
    return new MappingInputData();
  }

  public void setInputRowMeta( RowMetaInterface inputRowMeta ) {
    this.inputRowMeta = inputRowMeta;
  }

  /**
   * @return the inputRowMeta
   */
  public RowMetaInterface getInputRowMeta() {
    return inputRowMeta;
  }

  /**
   * @return the valueRenames
   */
  public List<MappingValueRename> getValueRenames() {
    return valueRenames;
  }

  /**
   * @param valueRenames the valueRenames to set
   */
  public void setValueRenames( List<MappingValueRename> valueRenames ) {
    this.valueRenames = valueRenames;
  }

  /**
   * @return the selectingAndSortingUnspecifiedFields
   */
  public boolean isSelectingAndSortingUnspecifiedFields() {
    return selectingAndSortingUnspecifiedFields;
  }

  /**
   * @param selectingAndSortingUnspecifiedFields the selectingAndSortingUnspecifiedFields to set
   */
  public void setSelectingAndSortingUnspecifiedFields( boolean selectingAndSortingUnspecifiedFields ) {
    this.selectingAndSortingUnspecifiedFields = selectingAndSortingUnspecifiedFields;
  }

}
