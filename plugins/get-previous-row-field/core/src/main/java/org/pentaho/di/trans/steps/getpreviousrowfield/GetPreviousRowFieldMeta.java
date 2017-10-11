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

package org.pentaho.di.trans.steps.getpreviousrowfield;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
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
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This class takes care of the meta data for the GetPreviousRowField step.
 *
 * @author Samatar Hassan
 * @since 07 September 2008
 */

@Step( id = "GetPreviousRowField", image = "PRV.svg",
  i18nPackageName = "org.pentaho.di.trans.steps.getpreviousrowfield", name = "GetPreviousRowFieldMeta.Name",
  description = "GetPreviousRowFieldMeta.Description",
  categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.Deprecated" )

public class GetPreviousRowFieldMeta extends BaseStepMeta implements StepMetaInterface {

  private static Class<?> PKG = GetPreviousRowFieldMeta.class; // for i18n purposes, needed by Translator2!!

  /** The target schema name */
  private String schema;

  /** which field in input stream to compare with? */
  private String[] fieldInStream;

  /** output field */
  private String[] fieldOutStream;

  public GetPreviousRowFieldMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the fieldInStream.
   */
  public String[] getFieldInStream() {
    return fieldInStream;
  }

  /**
   * @param fieldInStream
   *          The fieldInStream to set.
   */
  public void setFieldInStream( String[] keyStream ) {
    this.fieldInStream = keyStream;
  }

  /**
   * @return Returns the fieldOutStream.
   */
  public String[] getFieldOutStream() {
    return fieldOutStream;
  }

  /**
   * @param keyStream
   *          The fieldOutStream to set.
   */
  public void setFieldOutStream( String[] keyStream ) {
    this.fieldOutStream = keyStream;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public void allocate( int nrkeys ) {
    fieldInStream = new String[nrkeys];
    fieldOutStream = new String[nrkeys];
  }

  @Override
  public Object clone() {
    GetPreviousRowFieldMeta retval = (GetPreviousRowFieldMeta) super.clone();
    int nrkeys = fieldInStream.length;

    retval.allocate( nrkeys );

    for ( int i = 0; i < nrkeys; i++ ) {
      retval.fieldInStream[i] = fieldInStream[i];
      retval.fieldOutStream[i] = fieldOutStream[i];
    }

    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      int nrkeys;

      Node lookup = XMLHandler.getSubNode( stepnode, "fields" );
      nrkeys = XMLHandler.countNodes( lookup, "field" );

      allocate( nrkeys );

      for ( int i = 0; i < nrkeys; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( lookup, "field", i );

        fieldInStream[i] = Const.NVL( XMLHandler.getTagValue( fnode, "in_stream_name" ), "" );
        fieldOutStream[i] = Const.NVL( XMLHandler.getTagValue( fnode, "out_stream_name" ), "" );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "GetPreviousRowFieldMeta.Exception.UnableToReadStepInfoFromXML" ), e );
    }
  }

  @Override
  public void setDefault() {
    fieldInStream = null;
    fieldOutStream = null;
    schema = "";

    int nrkeys = 0;

    allocate( nrkeys );
  }

  @Override
  public String getXML() {
    StringBuffer retval = new StringBuffer( 500 );

    retval.append( "    <fields>" ).append( Const.CR );

    for ( int i = 0; i < fieldInStream.length; i++ ) {
      retval.append( "      <field>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "in_stream_name", fieldInStream[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "out_stream_name", fieldOutStream[i] ) );
      retval.append( "      </field>" ).append( Const.CR );
    }

    retval.append( "    </fields>" ).append( Const.CR );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {

    try {
      schema = rep.getStepAttributeString( id_step, "schema" );

      int nrkeys = rep.countNrStepAttributes( id_step, "in_stream_name" );

      // TODO NVL
      allocate( nrkeys );
      for ( int i = 0; i < nrkeys; i++ ) {
        fieldInStream[i] = Const.NVL( rep.getStepAttributeString( id_step, i, "in_stream_name" ), "" );
        fieldOutStream[i] = Const.NVL( rep.getStepAttributeString( id_step, i, "out_stream_name" ), "" );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "GetPreviousRowFieldMeta.Exception.UnexpectedErrorInReadingStepInfo" ), e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "schema", schema );

      for ( int i = 0; i < fieldInStream.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "in_stream_name", fieldInStream[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "out_stream_name", fieldOutStream[i] );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "GetPreviousRowFieldMeta.Exception.UnableToSaveStepInfo" )
        + id_step, e );
    }
  }

  @Override
  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    // Add new field?
    for ( int i = 0; i < fieldOutStream.length; i++ ) {
      if ( !Utils.isEmpty( fieldOutStream[i] ) ) {
        int index = inputRowMeta.indexOfValue( fieldInStream[i] );
        if ( index >= 0 ) {
          ValueMetaInterface in = inputRowMeta.getValueMeta( index );
          try {
            ValueMetaInterface v =
              ValueMetaFactory.createValueMeta( space.environmentSubstitute( fieldOutStream[i] ), in.getType() );
            v.setName( space.environmentSubstitute( fieldOutStream[i] ) );
            v.setLength( in.getLength() );
            v.setPrecision( in.getPrecision() );
            v.setConversionMask( in.getConversionMask() );
            v.setOrigin( name );
            inputRowMeta.addValueMeta( v );
          } catch ( Exception e ) {
            throw new KettleStepException( e );
          }
        }
      }
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {

    CheckResult cr;
    String error_message = "";
    boolean first = true;
    boolean error_found = false;

    if ( prev == null ) {

      error_message +=
        BaseMessages.getString( PKG, "GetPreviousRowFieldMeta.CheckResult.NoInputReceived" ) + Const.CR;
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    } else {

      for ( int i = 0; i < fieldInStream.length; i++ ) {
        String field = fieldInStream[i];

        ValueMetaInterface v = prev.searchValueMeta( field );
        if ( v == null ) {
          if ( first ) {
            first = false;
            error_message +=
              BaseMessages.getString( PKG, "GetPreviousRowFieldMeta.CheckResult.MissingInStreamFields" )
                + Const.CR;
          }
          error_found = true;
          error_message += "\t\t" + field + Const.CR;
        }
      }
      if ( error_found ) {
        cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "GetPreviousRowFieldMeta.CheckResult.FoundInStreamFields" ), stepMeta );
      }
      remarks.add( cr );

      // Check whether all output specified
      first = true;
      error_found = false;
      for ( int i = 0; i < fieldInStream.length; i++ ) {
        String field = fieldOutStream[i];
        if ( Utils.isEmpty( field ) ) {
          if ( first ) {
            first = false;
            error_message =
              BaseMessages.getString( PKG, "GetPreviousRowFieldMeta.CheckResult.OutputFieldEmpty", "" + i )
                + Const.CR;
          }
          error_found = true;
          error_message += "\t\t" + field + Const.CR;
        }
      }
      if ( error_found ) {
        cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "GetPreviousRowFieldMeta.CheckResult.OutputFieldSpecified" ), stepMeta );
      }
      remarks.add( cr );

      if ( fieldInStream.length > 0 ) {
        for ( int idx = 0; idx < fieldInStream.length; idx++ ) {
          if ( Utils.isEmpty( fieldInStream[idx] ) ) {
            cr =
              new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
                PKG, "GetPreviousRowFieldMeta.CheckResult.InStreamFieldMissing", new Integer( idx + 1 )
                  .toString() ), stepMeta );
            remarks.add( cr );

          }
        }
      }

      // Check if all input fields are distinct.
      for ( int idx = 0; idx < fieldInStream.length; idx++ ) {
        for ( int jdx = 0; jdx < fieldInStream.length; jdx++ ) {
          if ( fieldInStream[idx].equals( fieldInStream[jdx] ) && idx != jdx && idx < jdx ) {
            error_message =
              BaseMessages.getString(
                PKG, "GetPreviousRowFieldMeta.CheckResult.FieldInputError", fieldInStream[idx] );
            cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
            remarks.add( cr );
          }
        }
      }

    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new GetPreviousRowField( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new GetPreviousRowFieldData();
  }

  /**
   * @return the schema
   */
  public String getSchema() {
    return schema;
  }

  /**
   * @param schema
   *          the schema to set
   */
  public void setSchema( String schemaName ) {
    this.schema = schemaName;
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
  }
}
