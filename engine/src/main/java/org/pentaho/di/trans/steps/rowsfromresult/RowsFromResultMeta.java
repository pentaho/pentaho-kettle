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

package org.pentaho.di.trans.steps.rowsfromresult;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
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

/*
 * Created on 02-jun-2003
 *
 */

public class RowsFromResultMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = RowsFromResult.class; // for i18n purposes, needed by Translator2!!

  private String[] fieldname;
  private int[] type;
  private int[] length;
  private int[] precision;

  /**
   * @return Returns the length.
   */
  public int[] getLength() {
    return length;
  }

  /**
   * @param length
   *          The length to set.
   */
  public void setLength( int[] length ) {
    this.length = length;
  }

  /**
   * @return Returns the name.
   */
  public String[] getFieldname() {
    return fieldname;
  }

  /**
   * @param name
   *          The name to set.
   */
  public void setFieldname( String[] name ) {
    this.fieldname = name;
  }

  /**
   * @return Returns the precision.
   */
  public int[] getPrecision() {
    return precision;
  }

  /**
   * @param precision
   *          The precision to set.
   */
  public void setPrecision( int[] precision ) {
    this.precision = precision;
  }

  /**
   * @return Returns the type.
   */
  public int[] getType() {
    return type;
  }

  /**
   * @param type
   *          The type to set.
   */
  public void setType( int[] type ) {
    this.type = type;
  }

  public RowsFromResultMeta() {
    super(); // allocate BaseStepMeta
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public Object clone() {
    RowsFromResultMeta retval = (RowsFromResultMeta) super.clone();
    int nrFields = fieldname.length;
    retval.allocate( nrFields );
    System.arraycopy( fieldname, 0, retval.fieldname, 0, nrFields );
    System.arraycopy( type, 0, retval.type, 0, nrFields );
    System.arraycopy( length, 0, retval.length, 0, nrFields );
    System.arraycopy( precision, 0, retval.precision, 0, nrFields );
    return retval;
  }

  public void allocate( int nrFields ) {
    fieldname = new String[nrFields];
    type = new int[nrFields];
    length = new int[nrFields];
    precision = new int[nrFields];
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();
    retval.append( "    <fields>" );
    for ( int i = 0; i < fieldname.length; i++ ) {
      retval.append( "      <field>" );
      retval.append( "        " + XMLHandler.addTagValue( "name", fieldname[i] ) );
      retval.append( "        " + XMLHandler.addTagValue( "type", ValueMetaFactory.getValueMetaName( type[i] ) ) );
      retval.append( "        " + XMLHandler.addTagValue( "length", length[i] ) );
      retval.append( "        " + XMLHandler.addTagValue( "precision", precision[i] ) );
      retval.append( "        </field>" );
    }
    retval.append( "      </fields>" );

    return retval.toString();
  }

  private void readData( Node stepnode ) {
    Node fields = XMLHandler.getSubNode( stepnode, "fields" );
    int nrfields = XMLHandler.countNodes( fields, "field" );

    allocate( nrfields );

    for ( int i = 0; i < nrfields; i++ ) {
      Node line = XMLHandler.getSubNodeByNr( fields, "field", i );
      fieldname[i] = XMLHandler.getTagValue( line, "name" );
      type[i] = ValueMetaFactory.getIdForValueMeta( XMLHandler.getTagValue( line, "type" ) );
      length[i] = Const.toInt( XMLHandler.getTagValue( line, "length" ), -2 );
      precision[i] = Const.toInt( XMLHandler.getTagValue( line, "precision" ), -2 );
    }

  }

  public void setDefault() {
    allocate( 0 );
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      int nrfields = rep.countNrStepAttributes( id_step, "field_name" );
      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        fieldname[i] = rep.getStepAttributeString( id_step, i, "field_name" );
        type[i] = ValueMetaFactory.getIdForValueMeta( rep.getStepAttributeString( id_step, i, "field_type" ) );
        length[i] = (int) rep.getStepAttributeInteger( id_step, i, "field_length" );
        precision[i] = (int) rep.getStepAttributeInteger( id_step, i, "field_precision" );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "RowsFromResultMeta.Exception.ErrorReadingStepInfoFromRepository" ), e );
    }

  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      for ( int i = 0; i < fieldname.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", fieldname[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_type",
          ValueMetaFactory.getValueMetaName( type[i] ) );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_length", length[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_precision", precision[i] );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "RowsFromResultMeta.Exception.UnableToSaveStepInfoToRepository" )
        + id_step, e );
    }
  }

  public void getFields( RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    for ( int i = 0; i < this.fieldname.length; i++ ) {
      ValueMetaInterface v;
      try {
        v = ValueMetaFactory.createValueMeta( fieldname[i], type[i], length[i], precision[i] );
        v.setOrigin( origin );
        r.addValueMeta( v );
      } catch ( KettlePluginException e ) {
        throw new KettleStepException( e );
      }
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      CheckResult cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "RowsFromResultMeta.CheckResult.StepExpectingNoReadingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      CheckResult cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "RowsFromResultMeta.CheckResult.NoInputReceivedError" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new RowsFromResult( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new RowsFromResultData();
  }

}
