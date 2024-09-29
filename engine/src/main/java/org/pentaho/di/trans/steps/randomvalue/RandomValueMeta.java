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

package org.pentaho.di.trans.steps.randomvalue;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNone;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
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
 * Created on 08-07-2008
 */
public class RandomValueMeta extends BaseStepMeta implements StepMetaInterface {

  private static Class<?> PKG = RandomValueMeta.class; // for i18n purposes, needed by Translator2!!

  public static final int TYPE_RANDOM_NONE = 0;

  public static final int TYPE_RANDOM_NUMBER = 1;

  public static final int TYPE_RANDOM_INTEGER = 2;

  public static final int TYPE_RANDOM_STRING = 3;

  public static final int TYPE_RANDOM_UUID = 4;

  public static final int TYPE_RANDOM_UUID4 = 5;

  public static final int TYPE_RANDOM_MAC_HMACMD5 = 6;

  public static final int TYPE_RANDOM_MAC_HMACSHA1 = 7;

  public static final RandomValueMetaFunction[] functions = new RandomValueMetaFunction[] {
    null,
    new RandomValueMetaFunction( TYPE_RANDOM_NUMBER, "random number", BaseMessages.getString(
      PKG, "RandomValueMeta.TypeDesc.RandomNumber" ) ),
    new RandomValueMetaFunction( TYPE_RANDOM_INTEGER, "random integer", BaseMessages.getString(
      PKG, "RandomValueMeta.TypeDesc.RandomInteger" ) ),
    new RandomValueMetaFunction( TYPE_RANDOM_STRING, "random string", BaseMessages.getString(
      PKG, "RandomValueMeta.TypeDesc.RandomString" ) ),
    new RandomValueMetaFunction( TYPE_RANDOM_UUID, "random uuid", BaseMessages.getString(
      PKG, "RandomValueMeta.TypeDesc.RandomUUID" ) ),
    new RandomValueMetaFunction( TYPE_RANDOM_UUID4, "random uuid4", BaseMessages.getString(
      PKG, "RandomValueMeta.TypeDesc.RandomUUID4" ) ),
    new RandomValueMetaFunction( TYPE_RANDOM_MAC_HMACMD5, "random machmacmd5", BaseMessages.getString(
      PKG, "RandomValueMeta.TypeDesc.RandomHMACMD5" ) ),
    new RandomValueMetaFunction( TYPE_RANDOM_MAC_HMACSHA1, "random machmacsha1", BaseMessages.getString(
      PKG, "RandomValueMeta.TypeDesc.RandomHMACSHA1" ) ) };

  private String[] fieldName;

  private int[] fieldType;

  public RandomValueMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the fieldName.
   */
  public String[] getFieldName() {
    return fieldName;
  }

  /**
   * @param fieldName
   *          The fieldName to set.
   */
  public void setFieldName( String[] fieldName ) {
    this.fieldName = fieldName;
  }

  /**
   * @return Returns the fieldType.
   */
  public int[] getFieldType() {
    return fieldType;
  }

  /**
   * @param fieldType
   *          The fieldType to set.
   */
  public void setFieldType( int[] fieldType ) {
    this.fieldType = fieldType;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public void allocate( int count ) {
    fieldName = new String[count];
    fieldType = new int[count];
  }

  @Override
  public Object clone() {
    RandomValueMeta retval = (RandomValueMeta) super.clone();

    int count = fieldName.length;

    retval.allocate( count );
    System.arraycopy( fieldName, 0, retval.fieldName, 0, count );
    System.arraycopy( fieldType, 0, retval.fieldType, 0, count );

    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int count = XMLHandler.countNodes( fields, "field" );
      String type;

      allocate( count );

      for ( int i = 0; i < count; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        fieldName[i] = XMLHandler.getTagValue( fnode, "name" );
        type = XMLHandler.getTagValue( fnode, "type" );
        fieldType[i] = getType( type );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to read step information from XML", e );
    }
  }

  public static final int getType( String type ) {
    for ( int i = 1; i < functions.length; i++ ) {
      if ( functions[i].getCode().equalsIgnoreCase( type ) ) {
        return i;
      }
      if ( functions[i].getDescription().equalsIgnoreCase( type ) ) {
        return i;
      }
    }
    return 0;
  }

  public static final String getTypeDesc( int t ) {
    if ( functions == null || functions.length == 0 ) {
      return null;
    }
    if ( t < 0 || t >= functions.length || functions[t] == null ) {
      return null;
    }
    return functions[t].getDescription();
  }

  @Override
  public void setDefault() {
    int count = 0;

    allocate( count );

    for ( int i = 0; i < count; i++ ) {
      fieldName[i] = "field" + i;
      fieldType[i] = TYPE_RANDOM_NUMBER;
    }
  }

  @Override
  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    for ( int i = 0; i < fieldName.length; i++ ) {
      ValueMetaInterface v;

      switch ( fieldType[i] ) {
        case TYPE_RANDOM_NUMBER:
          v = new ValueMetaNumber( fieldName[i], 10, 5 );
          break;
        case TYPE_RANDOM_INTEGER:
          v = new ValueMetaInteger( fieldName[i], 10, 0 );
          break;
        case TYPE_RANDOM_STRING:
          v = new ValueMetaString( fieldName[i], 13, 0 );
          break;
        case TYPE_RANDOM_UUID:
          v = new ValueMetaString( fieldName[i], 36, 0 );
          break;
        case TYPE_RANDOM_UUID4:
          v = new ValueMetaString( fieldName[i], 36, 0 );
          break;
        case TYPE_RANDOM_MAC_HMACMD5:
          v = new ValueMetaString( fieldName[i], 100, 0 );
          break;
        case TYPE_RANDOM_MAC_HMACSHA1:
          v = new ValueMetaString( fieldName[i], 100, 0 );
          break;
        default:
          v = new ValueMetaNone( fieldName[i] );
          break;
      }
      v.setOrigin( name );
      row.addValueMeta( v );
    }
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 200 );

    retval.append( "    <fields>" ).append( Const.CR );

    for ( int i = 0; i < fieldName.length; i++ ) {
      retval.append( "      <field>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", fieldName[i] ) );
      retval.append( "        " ).append(
        XMLHandler
          .addTagValue( "type", functions[fieldType[i]] != null ? functions[fieldType[i]].getCode() : "" ) );
      retval.append( "      </field>" ).append( Const.CR );
    }
    retval.append( "    </fields>" + Const.CR );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      int nrfields = rep.countNrStepAttributes( id_step, "field_name" );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        fieldName[i] = rep.getStepAttributeString( id_step, i, "field_name" );
        fieldType[i] = getType( rep.getStepAttributeString( id_step, i, "field_type" ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      for ( int i = 0; i < fieldName.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", fieldName[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_type", functions[fieldType[i]] != null
          ? functions[fieldType[i]].getCode() : "" );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }

  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    // See if we have input streams leading to this step!
    int nrRemarks = remarks.size();
    for ( int i = 0; i < fieldName.length; i++ ) {
      if ( fieldType[i] <= TYPE_RANDOM_NONE ) {
        CheckResult cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "RandomValueMeta.CheckResult.FieldHasNoType", fieldName[i] ), stepMeta );
        remarks.add( cr );
      }
    }
    if ( remarks.size() == nrRemarks ) {
      CheckResult cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "RandomValueMeta.CheckResult.AllTypesSpecified" ), stepMeta );
      remarks.add( cr );
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new RandomValue( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new RandomValueData();
  }
}
