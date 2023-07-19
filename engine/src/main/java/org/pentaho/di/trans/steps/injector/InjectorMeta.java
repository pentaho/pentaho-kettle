/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.injector;

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
import org.pentaho.di.metastore.MetaStoreConst;
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

// TODO: check conversion of types from strings to numbers and back.
//       As compared in the old version.

/**
 * Metadata class to allow a java program to inject rows of data into a transformation. This step can be used as a
 * starting point in such a "headless" transformation.
 *
 * @since 22-jun-2006
 */
public class InjectorMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = InjectorMeta.class; // for i18n purposes, needed by Translator2!!

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
   * @param fieldname
   *          The name to set.
   */
  public void setFieldname( String[] fieldname ) {
    this.fieldname = fieldname;
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

  public InjectorMeta() {
    super(); // allocate BaseStepMeta
    allocate( 0 );
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  public void allocate( int nrFields ) {
    fieldname = new String[nrFields];
    type = new int[nrFields];
    length = new int[nrFields];
    precision = new int[nrFields];
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );
    retval.append( "    <fields>" );
    for ( int i = 0; i < fieldname.length; i++ ) {
      retval.append( "      <field>" );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", fieldname[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "type",
        ValueMetaFactory.getValueMetaName( type[i] ) ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "length", length[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "precision", precision[i] ) );
      retval.append( "      </field>" );
    }
    retval.append( "    </fields>" );

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
        PKG, "InjectorMeta.Exception.ErrorReadingStepInfoFromRepository" ), e );
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
        PKG, "InjectorMeta.Exception.UnableToSaveStepInfoToRepository" )
        + id_step, e );
    }
  }

  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    for ( int i = 0; i < this.fieldname.length; i++ ) {
      ValueMetaInterface v;
      try {
        v = ValueMetaFactory.createValueMeta( this.fieldname[i], type[i], length[i], precision[i] );
        inputRowMeta.addValueMeta( v );
      } catch ( KettlePluginException e ) {
        throw new KettleStepException( e );
      }
    }
  }

  @Override
  @Deprecated
  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space ) throws KettleStepException {
    getFields( inputRowMeta, name, info, nextStep, space, null, null );
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      CheckResult cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "InjectorMeta.CheckResult.StepExpectingNoReadingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      CheckResult cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "InjectorMeta.CheckResult.NoInputReceivedError" ), stepMeta );
      remarks.add( cr );
    }
  }

  @Deprecated
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info ) {
    check( remarks, transMeta, stepMeta, prev, input, output, info, transMeta, repository,
      MetaStoreConst.getDefaultMetastore() );
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new Injector( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new InjectorData();
  }
}
